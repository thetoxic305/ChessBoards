package water.of.cup.chessboards.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import water.of.cup.chessboards.ChessBoards;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class DataSource {

    private ChessBoards instance = ChessBoards.getInstance();
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;
    private HashMap<Player, ChessPlayer> chessPlayers = new HashMap<>();
    private final ExecutorService executorService = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

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

        try (Connection con = getConnection();
             Statement statement = con.createStatement();) {
            statement.execute(sql);
        }
    }

    public void addChessPlayer(Player player) {
        String uuidString = player.getUniqueId().toString();
        getOfflineChessPlayerAsync(uuidString, chessPlayer -> {
            if(chessPlayer == null) {
                createChessPlayerAsync(player, newChessPlayer -> {
                    if(newChessPlayer == null) return;

                    this.chessPlayers.put(player, newChessPlayer);
                });
            } else {
                this.chessPlayers.put(player, chessPlayer);
            }
        });
    }

    private void createChessPlayerAsync(Player player, Consumer<ChessPlayer> consumer)  {
        executorService.submit(() -> {
            try {
                String playerUUID = player.getUniqueId().toString();
                String createPlayerSql = "INSERT INTO `chess_players` (uuid) VALUES "
                        + "('" + playerUUID + "');";

                try (Connection con = getConnection();
                     PreparedStatement sql = con.prepareStatement(createPlayerSql, Statement.RETURN_GENERATED_KEYS);
                ) {
                    int result = sql.executeUpdate();

                    if(result == 0) {
                        consumer.accept(null);
                        return;
                    }

                    try (ResultSet generatedKeys = sql.getGeneratedKeys()) {
                        if(!generatedKeys.next()) {
                            consumer.accept(null);
                            return;
                        }

                        int id = generatedKeys.getInt(1);
                        ChessPlayer newPlayer = new ChessPlayer(id, playerUUID, 0, 0, 0, 0.0, 0.0, 0.0, 0);

                        consumer.accept(newPlayer);
                    }

                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void updateColumn(String playerUUID, String column, String updated) {
        executorService.submit(() -> {
            try {
                String updateSql = "UPDATE chess_players SET "
                        + column + " = ? "
                        + "WHERE uuid='" + playerUUID + "'";

                try (Connection con = getConnection();
                     PreparedStatement updateQuery = con.prepareStatement(updateSql);) {
                    updateQuery.setString(1, updated);
                    updateQuery.execute();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public ChessPlayer getChessPlayer(Player player) {
        if(chessPlayers.containsKey(player)) {
            return chessPlayers.get(player);
        }

        return null;
    }

    public ChessPlayer getChessPlayerByUUID(String uuid) {
        for (ChessPlayer chessPlayer : chessPlayers.values()) {
            if (chessPlayer.getUuid().equals(uuid))
                return chessPlayer;
        }

        return null;
    }

    public void getTopPlayers(int page, Consumer<ArrayList<ChessPlayer>> topPlayersConsumer) {
        executorService.submit(() -> {
            try {
                ArrayList<ChessPlayer> topPlayers = new ArrayList<>();
                try (Connection con = getConnection();
                     PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players ORDER BY rating DESC LIMIT " + (page * 10) + ", 10");
                     ResultSet playerData = sql.executeQuery();
                ) {
                    while (playerData.next()) {
                        ChessPlayer newPlayer = new ChessPlayer(playerData);
                        topPlayers.add(newPlayer);
                    }
                }

                topPlayersConsumer.accept(topPlayers);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void getAllPlayers(Consumer<ArrayList<ChessPlayer>> chessPlayerConsumer) {
        executorService.submit(() -> {
            try {
                ArrayList<ChessPlayer> allPlayers = new ArrayList<>();

                try (Connection con = getConnection();
                     PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players;");
                     ResultSet playerData = sql.executeQuery();) {

                    while (playerData.next()) {
                        ChessPlayer newPlayer = new ChessPlayer(playerData);
                        allPlayers.add(newPlayer);
                    }
                }

                chessPlayerConsumer.accept(allPlayers);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void getOfflineChessPlayerAsync(String playerUUID, Consumer<ChessPlayer> chessPlayerConsumer) {
        executorService.submit(() -> {
            try {
                try (Connection con = getConnection();
                     PreparedStatement sql = con.prepareStatement("SELECT * FROM `chess_players` WHERE "
                             + "uuid='" + playerUUID + "'");
                     ResultSet playerData = sql.executeQuery();
                ) {
                    if(!playerData.next()) {
                        chessPlayerConsumer.accept(null);
                        return;
                    }

                    ChessPlayer newPlayer = new ChessPlayer(playerData);
                    chessPlayerConsumer.accept(newPlayer);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void getChessPlayerTotal(Consumer<Integer> totalConsumer) {
        executorService.submit(() -> {
            try {
                int num = 0;
                try (Connection con = getConnection();
                     PreparedStatement sql = con.prepareStatement("SELECT * FROM chess_players");
                     ResultSet playerData = sql.executeQuery();) {
                    while (playerData.next()) {
                        num++;
                    }
                }

                totalConsumer.accept(num);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void closeConnection() {
        if(ds != null)
            ds.close();
    }
}
