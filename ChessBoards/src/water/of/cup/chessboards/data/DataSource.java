package water.of.cup.chessboards.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import water.of.cup.chessboards.ChessBoards;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DataSource {

    private ChessBoards instance = ChessBoards.getInstance();
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;
    private HashMap<Player, ChessPlayer> chessPlayers = new HashMap<>();

    public void initialize() {
        if(ds == null) {
            String host = instance.getConfig().getString("settings.database.host");
            String port = instance.getConfig().getString("settings.database.port");
            String database = instance.getConfig().getString("settings.database.database");
            String username = instance.getConfig().getString("settings.database.username");
            String password = instance.getConfig().getString("settings.database.password");

            String connectionString = "jdbc:mysql://"
                    + host + ":" + port + "/";

            createDatabaseIfNotExists(connectionString, username, password, database);

            config.setJdbcUrl(connectionString + database);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useSSL", "false");

            ds = new HikariDataSource(config);

            try {
                checkSchema();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }

    private void createDatabaseIfNotExists(String connString, String username, String password, String database) {
        try {
            HikariConfig checkConfig = new HikariConfig();
            checkConfig.setJdbcUrl(connString);
            checkConfig.setUsername(username);
            checkConfig.setPassword(password);
            checkConfig.addDataSourceProperty("useSSL", "false");

            HikariDataSource checkDs = new HikariDataSource(checkConfig);

            try(Connection con = checkDs.getConnection();
                Statement createSql = con.createStatement();
            ) {
                createSql.execute("CREATE DATABASE IF NOT EXISTS " + database);
            }

            checkDs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void checkSchema() throws SQLException {
        if (!tableExists("chess_players")) {
            String sql = "CREATE TABLE IF NOT EXISTS `chess_players` "
                    + "(`id` int PRIMARY KEY AUTO_INCREMENT,"
                    + "`uuid` varchar(255) UNIQUE,"
                    + "`wins` int default 0,"
                    + "`losses` int default 0,"
                    + "`ties` int default 0,"
                    + "`rating` double default 0,"
                    + "`ratingDeviation` double default 0,"
                    + "`volatility` double default 0,"
                    + "`numberOfResults` int default 0);";
            execute(sql);
        }
    }

    public void addChessPlayer(Player player) {
        try {
            if(!playerExistsInDatabase(player)) createChessPlayer(player);

            String playerUUID = player.getUniqueId().toString();

            try (Connection con = getConnection();
                 PreparedStatement sql = con.prepareStatement("SELECT * FROM `chess_players` WHERE "
                         + "uuid='" + playerUUID + "'");
                 ResultSet playerData = sql.executeQuery();
            ) {
                playerData.next();

                int id = playerData.getInt(1);
                String uuid = playerData.getString(2);
                int wins = playerData.getInt(3);
                int losses = playerData.getInt(4);
                int ties = playerData.getInt(5);
                double rating = playerData.getDouble(6);
                double ratingDeviation = playerData.getDouble(7);
                double volatility = playerData.getDouble(8);
                int numberOfResults = playerData.getInt(9);

                ChessPlayer newPlayer = new ChessPlayer(player, id, uuid, wins, losses, ties, rating, ratingDeviation, volatility, numberOfResults);
                this.chessPlayers.put(player, newPlayer);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void createChessPlayer(Player player) throws SQLException {
        String playerUUID = player.getUniqueId().toString();
        String createPlayerSql = "INSERT INTO `chess_players` (uuid) VALUES "
                + "('" + playerUUID + "');";
        execute(createPlayerSql);
    }

    public boolean playerExistsInDatabase(Player player) throws SQLException {
        return playerExistsInDatabase(player.getUniqueId().toString());
    }

    public boolean playerExistsInDatabase(String playerUUID) throws SQLException {
        String sql = "SELECT uuid FROM `chess_players` WHERE uuid='" + playerUUID + "'";
        boolean found = false;
        try(Connection con = getConnection();
            PreparedStatement playerQuery = con.prepareStatement(sql);
            ResultSet playerResults = playerQuery.executeQuery();) {
           while (playerResults.next() && !found) {
               if(playerResults.getString(1).equals(playerUUID)) {
                   found = true;
               }
           }
       }

        return found;
    }

    public void execute(String query) throws SQLException {
        try (Connection con = getConnection();
             Statement statement = con.createStatement();) {
            statement.execute(query);
        }
    }

    public void updateColumn(Player player, String column, String updated) throws SQLException {
        String playerUUID = player.getUniqueId().toString();
        String updateSql = "UPDATE chess_players SET "
                + column + " = ? "
                + "WHERE uuid='" + playerUUID + "'";

        try (Connection con = getConnection();
             PreparedStatement updateQuery = con.prepareStatement(updateSql);) {
            updateQuery.setString(1, updated);
            updateQuery.execute();
        }
    }

    public boolean tableExists(String table) throws SQLException {
        boolean exists = false;
        try (Connection con = getConnection();
             ResultSet tableData = con.getMetaData().getTables(null, null, table, null);
        ) {
            exists = tableData.next();
        }
        return exists;
    }

    private void close(@Nullable ResultSet results) {
        if (results != null) {
            try {
                results.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public HashMap<Player, ChessPlayer> getChessPlayers() {
        return chessPlayers;
    }

    public ChessPlayer getChessPlayerByUUID(String uuid) {
        for (ChessPlayer chessPlayer : chessPlayers.values()) {
            if (chessPlayer.getUuid().equals(uuid))
                return chessPlayer;
        }
        return null;
    }

    public ArrayList<ChessPlayer> getTopPlayers(int page) {
        try {
            ArrayList<ChessPlayer> topPlayers = new ArrayList<>();
            try (Connection con = getConnection();
                 PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players ORDER BY rating DESC LIMIT " + (page * 10) + ", 10");
                 ResultSet playerData = sql.executeQuery();
            ) {
                while(playerData.next()) {
                    int id = playerData.getInt(1);
                    String uuid = playerData.getString(2);
                    int wins = playerData.getInt(3);
                    int losses = playerData.getInt(4);
                    int ties = playerData.getInt(5);
                    double rating = playerData.getDouble(6);
                    double ratingDeviation = playerData.getDouble(7);
                    double volatility = playerData.getDouble(8);
                    int numberOfResults = playerData.getInt(9);

                    ChessPlayer newPlayer = new ChessPlayer(null, id, uuid, wins, losses, ties, rating, ratingDeviation, volatility, numberOfResults);
                    topPlayers.add(newPlayer);
                }
            }

            return topPlayers;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public ArrayList<ChessPlayer> getAllPlayers() {
        try {
            ArrayList<ChessPlayer> allPlayers = new ArrayList<>();
            try(Connection con = getConnection();
                PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players;");
                ResultSet playerData = sql.executeQuery();) {
                while(playerData.next()) {
                    int id = playerData.getInt(1);
                    String uuid = playerData.getString(2);
                    int wins = playerData.getInt(3);
                    int losses = playerData.getInt(4);
                    int ties = playerData.getInt(5);
                    double rating = playerData.getDouble(6);
                    double ratingDeviation = playerData.getDouble(7);
                    double volatility = playerData.getDouble(8);
                    int numberOfResults = playerData.getInt(9);

                    ChessPlayer newPlayer = new ChessPlayer(null, id, uuid, wins, losses, ties, rating, ratingDeviation, volatility, numberOfResults);
                    allPlayers.add(newPlayer);
                }
            }
            return allPlayers;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public ChessPlayer getOfflineChessPlayer(OfflinePlayer player) {
        return getOfflineChessPlayer(player.getUniqueId().toString());
    }

    public ChessPlayer getOfflineChessPlayer(String playerUUID) {
        try {
            if(!playerExistsInDatabase(playerUUID)) return null;

            try (Connection con = getConnection();
                 PreparedStatement sql = con.prepareStatement("SELECT * FROM `chess_players` WHERE "
                         + "uuid='" + playerUUID + "'");
                 ResultSet playerData = sql.executeQuery();
            ) {
                playerData.next();

                int id = playerData.getInt(1);
                String uuid = playerData.getString(2);
                int wins = playerData.getInt(3);
                int losses = playerData.getInt(4);
                int ties = playerData.getInt(5);
                double rating = playerData.getDouble(6);
                double ratingDeviation = playerData.getDouble(7);
                double volatility = playerData.getDouble(8);
                int numberOfResults = playerData.getInt(9);

                ChessPlayer newPlayer = new ChessPlayer(null, id, uuid, wins, losses, ties, rating, ratingDeviation, volatility, numberOfResults);
                return newPlayer;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public int getChessPlayerTotal() {
        try {
            int num = 0;
            try(Connection con = getConnection();
                PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players");
                ResultSet playerData = sql.executeQuery();) {
                while(playerData.next()) {
                    num++;
                }
            }

            return num;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void closeConnection() {
        if(ds != null)
            ds.close();
    }
}
