package water.of.cup.chessboards.chessBoard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;
import water.of.cup.chessboards.ChessBoards;

public class ChessBoardManager {

	private ArrayList<ChessGame> games = new ArrayList<>();;
	
	public void addGame(ChessGame game) {
		if (!games.contains(game)) {
			games.add(game);
		}
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
		File folder = new File(ChessBoards.getInstance().getDataFolder() + "/saved_games");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				try {
					int gameId = Integer.parseInt(file.getName().split("_")[1].split(Pattern.quote("."))[0]);
					BufferedReader br = new BufferedReader(new FileReader(file));
					String encodedData = br.readLine();

					ItemStack chessBoardItem = new ItemStack(Material.FILLED_MAP, 1);
					MapMeta mapMeta = (MapMeta) chessBoardItem.getItemMeta();
					MapView mapView = Bukkit.getMap(gameId);
					mapMeta.setMapView(mapView);
					chessBoardItem.setItemMeta(mapMeta);

					ChessGame newChessGame = new ChessGame(chessBoardItem, encodedData, gameId);
					newChessGame.renderBoardForPlayers();

					games.add(newChessGame);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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

	public void saveGames() {
		for(ChessGame game : this.games) {
			this.storeGame(game);
		}
	}

	private void storeGame(ChessGame game) {
		String mapData = game.toString();
		String id = ((MapMeta) game.getItem().getItemMeta()).getMapView().getId() + "";
		File file = new File(ChessBoards.getInstance().getDataFolder(), "saved_games/game_" + id + ".txt");

		if (!file.exists()) {
			try {
				file.createNewFile();
				Bukkit.getLogger().severe("[ChessBoards] Created game file for gameId: " + id);
			} catch (IOException e1) {
				Bukkit.getLogger().severe("Error creating game file for gameId: " + id);
				e1.printStackTrace();
			}
		}

		try {
			Bukkit.getLogger().severe("[ChessBoards] Writing game data to gameId: " + id);
			Files.write(Paths.get(file.getPath()), mapData.getBytes());
		} catch (IOException e) {
			Bukkit.getLogger().severe("Error writing to gameId: " + id);
			e.printStackTrace();
		}
	}
}
