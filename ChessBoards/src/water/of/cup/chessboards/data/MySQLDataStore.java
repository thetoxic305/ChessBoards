package water.of.cup.chessboards.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import water.of.cup.chessboards.ChessBoards;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MySQLDataStore {

    private Connection connection;
    private ChessBoards instance = ChessBoards.getInstance();
    private String username;
    private String password;
    private String database;
    private HashMap<Player, ChessPlayer> chessPlayers = new HashMap<>();

    public void initialize() {
        String host = instance.getConfig().getString("settings.database.host");
        String port = instance.getConfig().getString("settings.database.port");
        this.database = instance.getConfig().getString("settings.database.database");
        this.username = instance.getConfig().getString("settings.database.username");
        this.password = instance.getConfig().getString("settings.database.password");

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");

            String connectionString = "jdbc:mysql://"
                    + host + ":" + port + "/";

            this.createDatabaseIfNotExists(connectionString);

            connection = DriverManager.getConnection(connectionString + database + "?autoReconnect=true&useSSL=false",
                    username, password);

            this.checkSchema();

            Bukkit.getLogger().info("[ChessBoards] Connected to database.");
        } catch (SQLException | ClassNotFoundException throwables) {
            Bukkit.getLogger().warning("[ChessBoards] Error while connecting to database.");
            throwables.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists(String connString) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(connString + "?autoReconnect=true&useSSL=false", this.username, this.password);

            Statement createSql = conn.createStatement();
            createSql.execute("CREATE DATABASE IF NOT EXISTS " + this.database);

            if(createSql != null)
                createSql.close();

            if(conn != null)
                conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
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
            if (connection == null || connection.isClosed()) {
                return;
            }

            if(!playerExistsInDatabase(player)) createChessPlayer(player);

            String playerUUID = player.getUniqueId().toString();
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `chess_players` WHERE "
                    + "uuid='" + playerUUID + "'");
            ResultSet playerData = sql.executeQuery();
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
        String playerUUID = player.getUniqueId().toString();
        String sql = "SELECT uuid FROM `chess_players` WHERE uuid='" + playerUUID + "'";
        PreparedStatement playerQuery = connection.prepareStatement(sql);
        ResultSet playerResults = playerQuery.executeQuery();

        boolean found = false;
        while (playerResults.next() && !found) {
            if(playerResults.getString(1).equals(playerUUID)) {
                found = true;
            }
        }

        return found;
    }

    public void execute(String query) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    public void updateColumn(Player player, String column, String updated) throws SQLException {
        String playerUUID = player.getUniqueId().toString();
        String updateSql = "UPDATE chess_players SET "
                + column + " = ? "
                + "WHERE uuid='" + playerUUID + "'";

        PreparedStatement updateQuery = connection.prepareStatement(updateSql);
        updateQuery.setString(1, updated);
        updateQuery.execute();
    }

    public boolean tableExists(String table) throws SQLException {
        ResultSet tableData = null;
        boolean exists = false;
        try {
            tableData = connection.getMetaData().getTables(null, null, table, null);
            exists = tableData.next();
        } finally {
            close(tableData);
        }
        return exists;
    }

    public boolean columnExists(String table, String column) throws SQLException {
        ResultSet columnData = null;
        boolean exists = false;
        try {
            columnData = connection.getMetaData().getColumns(null, null, table, column);
            exists = columnData.next();
        } finally {
            close(columnData);
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
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM chess_players ORDER BY rating DESC LIMIT " + (page * 10) + ", 10");
            ResultSet playerData = sql.executeQuery();
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

            return topPlayers;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public int getChessPlayerTotal() {
        try {
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM chess_players");
            ResultSet playerData = sql.executeQuery();
            int num = 0;
            while(playerData.next()) {
                num++;
            }
            return num;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
    }
}
