package water.of.cup.chessBoard;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import water.of.cup.ChessBoards;
import water.of.cup.ImageManager;

public class GameRenderer extends MapRenderer {
	private ChessBoards instance = ChessBoards.getInstance();
	private ImageManager imageManager = instance.getImageManager();
	private ChessGame game;

	public GameRenderer(ChessGame game) {
		this.game = game;
	}

	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		//used to prevent map from continuously rendering
		if (map.isLocked())
			return;
		
		int[] selectedLocation = game.getSelectedLocation();
		ChessPiece[][] board = game.getBoard();
		
		boolean[][] moves = new boolean[8][8];
		if (game.locationOnBoard(selectedLocation) && board[selectedLocation[1]][selectedLocation[0]] != null) {
			player.sendMessage("movesFound");
			moves = board[selectedLocation[1]][selectedLocation[0]].getMoves(board, selectedLocation);
		}
		
		// clear board
		for (int x = 0; x < 128; x++) {
			for (int y = 0; y < 128; y++) {
				canvas.setPixel(x, y, MapPalette.TRANSPARENT);
			}
		}

		// set pieces
		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[0].length; x++) {
				if (board[y][x] != null) {
					canvas.drawImage(x * 16, y * 16, imageManager.getImage(board[y][x]));
				}
			}
		}

		// loop through every pixel on map and set board background
		for (int x = 0; x < 128; x++) {
			for (int y = 0; y < 128; y++) {
				//check if a coords are already filled in
				if (canvas.getPixel(x, y) != MapPalette.TRANSPARENT)
					continue;
					
				int yPos = y / 16;
				int xPos = x / 16;
				
				if ((yPos + xPos) % 2 == 0) {
					canvas.setPixel(x, y, MapPalette.matchColor(235, 236, 208));
				} else {
					canvas.setPixel(x, y, MapPalette.matchColor(119, 149, 86));
				}
				
				//fill selected Location
				if (xPos == selectedLocation[0] && yPos == selectedLocation[1]) {
					canvas.setPixel(x, y, MapPalette.matchColor(255, 255, 0));
				}
				
				//fill moves
				if (moves[yPos][xPos]) {
					canvas.setPixel(x,  y, MapPalette.RED);
				}
			}
		}
		map.setLocked(true);
	}
}
