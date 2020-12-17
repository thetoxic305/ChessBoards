package water.of.cup.chessBoard;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class ChessBoardManager {
	private ArrayList<ChessGame> games;
	
	public boolean addGame(ChessGame game) {
		
		if (games.contains(game)) {
			return false;
		}
		games.add(game);
		return true;
	}
	
	public boolean hasGame(ItemStack item) {
		for (ChessGame game : games) {
			if (game.getItem().equals(item)) {
				return true;
			}
		}
		return false;
	}
	
	public ChessGame getGame(ItemStack item) {
		for (ChessGame game : games) {
			if (game.getItem().equals(item)) {
				return game;
			}
		}
		return null;
	}
	
	public void loadGames() {
		games = new ArrayList<ChessGame>();
		
	}

	public ChessGame getGameByPlayer(Player player) {
		for(ChessGame game : games) {
			if(game.hasPlayer(player)) return game;
		}

		return null;
	}

	public ChessGame getGameByQueuePlayer(Player player) {
		for(ChessGame game : games) {
			if(game.getPlayerQueue().contains(player)) return game;
		}

		return null;
	}

	public ChessGame getGameByDecisionQueuePlayer(Player player) {
		for(ChessGame game : games) {
			if(game.getPlayerDecideQueue().contains(player)) return game;
		}

		return null;
	}
}
