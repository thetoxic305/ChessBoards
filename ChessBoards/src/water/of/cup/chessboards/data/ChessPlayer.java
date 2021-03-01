package water.of.cup.chessboards.data;

import org.bukkit.entity.Player;
import water.of.cup.chessboards.ChessBoards;

import java.sql.SQLException;

public class ChessPlayer {

    private Player player;
    private String uuid;
    private int wins;
    private int losses;
    private int ties;
    private double rating;
    private double ratingDeviation;
    private double volatility;
    private int numberOfResults;
    private int id;
    private ChessBoards instance = ChessBoards.getInstance();
    private DataSource dataStore = instance.getDataStore();

    public ChessPlayer(Player player, int id, String uuid, int wins, int losses, int ties, double rating, double ratingDeviation, double volatility, int numberOfResults) {
        this.player = player;
        this.id = id;
        this.uuid = uuid;
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
        this.rating = rating;
        this.ratingDeviation = ratingDeviation;
        this.volatility = volatility;
        this.numberOfResults = numberOfResults;
    }

    public Player getPlayer() {
        return player;
    }

    public String getUuid() {
        return uuid;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getTies() {
        return ties;
    }

    public double getRating() {
        return rating;
    }

    public double getRatingDeviation() {
        return ratingDeviation;
    }

    public double getVolatility() {
        return volatility;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public int getId() { return id; }

    public void setWins(int wins) {
        this.wins = wins;
        this.updateColumn("wins", this.wins + "");
    }

    public void setLosses(int losses) {
        this.losses = losses;
        this.updateColumn("losses", this.losses + "");
    }

    public void setTies(int ties) {
        this.ties = ties;
        this.updateColumn("ties", this.ties + "");
    }

    public void setRating(double rating) {
        this.rating = rating;
        this.updateColumn("rating", this.rating + "");
    }

    public void setRatingDeviation(double ratingDeviation) {
        this.ratingDeviation = ratingDeviation;
        this.updateColumn("ratingDeviation", this.ratingDeviation + "");
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
        this.updateColumn("volatility", this.volatility + "");
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
        this.updateColumn("numberOfResults", this.numberOfResults + "");
    }

    private void updateColumn(String column, String updated) {
        if (!instance.getConfig().getBoolean("settings.database.enabled")) return;

        try {
            dataStore.updateColumn(player, column, updated);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
