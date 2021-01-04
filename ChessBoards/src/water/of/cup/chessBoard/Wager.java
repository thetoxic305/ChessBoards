package water.of.cup.chessBoard;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import water.of.cup.ChessBoards;

public class Wager {
	private ChessBoards instance = ChessBoards.getInstance();
	
	Player player1;
	Player player2;

	String player1Side;

	double amount;
	
	public Wager(Player player1, Player player2, String player1Side, double amount) {
		this.player1 = player1;
		this.player2 = player2;
		
		this.player1Side = player1Side;
		
		this.amount = amount;
	}
	
	public Wager(String wagerString) {
		for (String arg : wagerString.split(";")) {

			String key = arg.substring(0, arg.indexOf(":"));
			String result = arg.substring(arg.indexOf(":") + 1);

			if (key.equals("Player1")) {
				player1 = Bukkit.getPlayer(UUID.fromString(result));
				continue;
			}
			if (key.equals("Player2")) {
				player2 = Bukkit.getPlayer(UUID.fromString(result));
				continue;
			}
			if (key.equals("Player1Side")) {
				player1Side = result;
				continue;
			}
			if (key.equals("Amount")) {
				amount = Double.valueOf(result);
				continue;
			}
		}
	}

	public void complete(String winningColor) {
		if (!(winningColor.equals("WHITE") || winningColor.equals("BLACK"))) {
			//give players their money back
			instance.getEconomy().depositPlayer(player1, amount);
			instance.getEconomy().depositPlayer(player2, amount);
		}
			

		if (player1Side.equals(winningColor)) {
			//player1 won
			instance.getEconomy().depositPlayer(player1, amount * 2);
		} else {
			//player2 won
			instance.getEconomy().depositPlayer(player2, amount * 2);
		}
	}
	
	public String toString() {
		String wagerString = "";
		wagerString += "Player1:" + player1.getUniqueId().toString() + ";";
		wagerString += "Player2:" + player2.getUniqueId().toString() + ";";
		
		wagerString += "Player1Side:" + player1Side + ";";
		
		wagerString += "Amount:" + amount + ";";
		
		return wagerString;
	}
}
