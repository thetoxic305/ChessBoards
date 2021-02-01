package water.of.cup.chessboards.chessBoard;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.util.Vector;

public class ChessBoardManager {
	private ArrayList<ChessGame> games;
	
	public boolean addGame(ChessGame game) {
		
		if (games.contains(game)) {
			return false;
		}
		games.add(game);
		return true;
	}

	public boolean removeGame(ChessGame game) {
		if (!games.contains(game)) {
			return false;
		}

		games.remove(game);
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

	public ChessGame getGameByGameId(int id) {
		for(ChessGame game : games) {
			if(game.getGameId() == id) return game;
		}

		return null;
	}
	
	public ArrayList<ChessGame> getGamesInRegion(World world, Vector p1, Vector p2) {
		ArrayList<ChessGame> games = new ArrayList<ChessGame>();
		for (Entity entity : world.getEntities()) {
			if (!(entity instanceof ItemFrame))
				continue;
			ItemFrame frame = (ItemFrame) entity;
			ItemStack item = frame.getItem();
			if (item != null && item.getType() == Material.FILLED_MAP && ((MapMeta) item.getItemMeta()).hasMapId()) {
				ChessGame game = getGameByGameId(((MapMeta) item.getItemMeta()).getMapId());
				if (game != null && entity.getLocation().toVector().isInAABB(p1, p2))
					games.add(game);
			}
		}
		return games;
	}

	public ArrayList<ChessGame> getGames() {
		return this.games;
	}
}
