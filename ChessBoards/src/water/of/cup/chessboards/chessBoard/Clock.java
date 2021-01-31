package water.of.cup.chessboards.chessBoard;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Clock extends BukkitRunnable {
	private ChessGame game;

	private double whiteTime;
	private double blackTime;
	private double lastTimeChange;

	private String turn;

	public Clock(int clockTime, ChessGame chessGame) {
		whiteTime = clockTime;
		blackTime = clockTime;
		game = chessGame;
		turn = chessGame.getTurn();
		lastTimeChange = System.currentTimeMillis() / 1000;
	}

	@Override
	public void run() {

		// check if runnable needs to stop
		if (game.getGameState() != ChessGameState.INGAME || game.getBlackPlayer() == null
				|| game.getWhitePlayer() == null) {
			this.cancel();
			return;
		}

		// change color timers
		double timeDifference = System.currentTimeMillis() / 1000 - lastTimeChange;
		lastTimeChange = System.currentTimeMillis() / 1000;
		turn = game.getTurn();
		if (turn.equals("WHITE"))
			whiteTime -= timeDifference;

		if (turn.equals("BLACK"))
			blackTime -= timeDifference;

		// check if game is over
		if (blackTime < 0) {
			game.gameOver("WHITE", "won on time");
			this.cancel();
			return;
		}
		if (whiteTime < 0) {
			game.gameOver("BLACK", "won on time");
			this.cancel();
			return;
		}

		// send players clock times
		String blackTimeText = "BLACK: " + (int) blackTime / 60 + ":" + (int) (blackTime % 60);
		String whiteTimeText = "WHITE: " + (int) whiteTime / 60 + ":" + (int) (whiteTime % 60);
		game.getBlackPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				TextComponent.fromLegacyText(ChatColor.YELLOW + blackTimeText + " | " + whiteTimeText));
		game.getWhitePlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				TextComponent.fromLegacyText(ChatColor.YELLOW + whiteTimeText + " | " + blackTimeText));
	}

	public void incementTime(String color, double amount) {
		if (color.equals("WHITE"))
			whiteTime += amount;

		if (color.equals("BLACK"))
			blackTime += amount;
	}

	public double getWhiteTime() {
		return whiteTime;
	}

	public double getBlackTime() {
		return blackTime;
	}
}
