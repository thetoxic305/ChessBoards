package water.of.cup.chessboards.chessBoard;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import water.of.cup.chessboards.ChessBoards;

public class RequestWager {
	private ChessBoards instance = ChessBoards.getInstance();

	Player owner;

	String ownerSide;

	double amount;

	public RequestWager(Player owner, String ownerSide, double amount) {
		this.owner = owner;
		this.ownerSide = ownerSide;
		this.amount = amount;
	}

	public Wager createWager(Player otherPlayer) {
		if (instance.getEconomy().getBalance(otherPlayer) < amount) {
			otherPlayer.sendMessage(ChatColor.RED + "You do not have enough money to accept this wager.");
			return null;
		}

		return new Wager(owner, otherPlayer, ownerSide, amount);
	}

	public Player getOwner() {
		return owner;
	}

	public String getOwnerColor() {
		return ownerSide;
	}

	public String getOponentColor() {
		if (ownerSide.equals("WHITE"))
			return "BLACK";
		return "WHITE";
	}

	public double getAmount() {
		return amount;
	}
}
