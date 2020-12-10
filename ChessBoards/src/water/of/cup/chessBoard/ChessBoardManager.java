package water.of.cup.chessBoard;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class ChessBoardManager {
	ArrayList<ChessGame> games;
	
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
}
