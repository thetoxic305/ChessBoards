package water.of.cup.chessBoard;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ChessGame {
	String turn;
	ChessPiece[][] board;
	int[] selectedPiece;
	ItemStack gameItem;
	boolean[][] movedPieces;
	ArrayList<String> record;

	public ChessGame(ItemStack item) {
		record = new ArrayList<String>();
		selectedPiece = new int[] { -1, -1 };
		turn = "WHITE";
		this.gameItem = item;
		movedPieces = new boolean[8][8];

		// set up chess board
		board = new ChessPiece[][] {
				{ ChessPiece.BLACK_ROOK, ChessPiece.BLACK_KNIGHT, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_QUEEN,
						ChessPiece.BLACK_KING, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_KNIGHT,
						ChessPiece.BLACK_ROOK },
				{ ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN,
						ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN },
				{ null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null },
				{ null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null },
				{ ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN,
						ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN },
				{ ChessPiece.WHITE_ROOK, ChessPiece.WHITE_KNIGHT, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_QUEEN,
						ChessPiece.WHITE_KING, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_KNIGHT,
						ChessPiece.WHITE_ROOK } };
	}

	public ChessPiece[][] getBoard() {
		return board;
	}

	public void renderBoardForPlayers() {
		MapMeta mapMeta = (MapMeta) gameItem.getItemMeta();
		MapView mapView = mapMeta.getMapView();

		mapView.setTrackingPosition(false);

		for (MapRenderer renderer : mapView.getRenderers())
			mapView.removeRenderer(renderer);
		mapView.setLocked(false);
		GameRenderer customRenderer = new GameRenderer(this);
		mapView.addRenderer(customRenderer);
		mapMeta.setMapView(mapView);

		gameItem.setItemMeta(mapMeta);

		mapView.getWorld().getPlayers().forEach(player -> player.sendMap(mapView));
	}

	public int[] getSelectedLocation() {
		return selectedPiece;
	}

	public void click(int[] loc) {
		// move piece if possible
		if (locationOnBoard(selectedPiece)) {
			ChessPiece piece = board[selectedPiece[1]][selectedPiece[0]];

			// check if move is possible
			if (piece != null && piece.getColor().equals(turn)
					&& piece.getMoves(board, selectedPiece, getMovedPieces(), record)[loc[1]][loc[0]]) {
				// MoveMade!
				String notation = "";
				
				
				// check if move is castle
				ChessPiece otherPiece = board[loc[1]][loc[0]];
				if (otherPiece != null && piece.toString().contains("KING") && otherPiece.toString().contains("ROOK")
						&& piece.getColor().equals(otherPiece.getColor())) {

					if (loc[0] == 0) {
						// left rook castle
						board[selectedPiece[1]][2] = piece;
						board[selectedPiece[1]][selectedPiece[0]] = null;
						board[loc[1]][3] = otherPiece;
						board[loc[1]][loc[0]] = null;

						notation = "0-0-0";

					} else {
						// right rook castle
						board[selectedPiece[1]][6] = piece;
						board[selectedPiece[1]][selectedPiece[0]] = null;
						board[loc[1]][5] = otherPiece;
						board[loc[1]][loc[0]] = null;
						notation = "0-0";
					}
				} else {
					// TODO: check if pawn reached end

					// check if move is en passent
					if (piece.toString().contains("PAWN") && otherPiece == null && loc[0] != selectedPiece[0]) {
						// take piece passed over
						board[selectedPiece[1]][loc[0]] = null;
					}

					// non-castle move made
					notation = piece.getNotationCharacter()
							+ ChessUtils.getNotationPosition(selectedPiece[0], selectedPiece[1]);
					
					//add piece taken to notation
					if (otherPiece != null) {
						notation += "x";
					}
					notation += ChessUtils.getNotationPosition(loc[0], loc[1]);

					board[loc[1]][loc[0]] = piece;
					board[selectedPiece[1]][selectedPiece[0]] = null;

					movedPieces[loc[1]][loc[0]] = true;
					movedPieces[selectedPiece[1]][selectedPiece[0]] = true;

					selectedPiece = new int[] { -1, -1 };

				}
				switchTurn();

				// CheckIfGameOver
				if (!ChessUtils.colorHasMoves(board, turn)) {
					// Check if winner
					if (ChessUtils.locationThreatened(ChessUtils.locateKing(board, turn), board)) {
						// Other side won
						notation += "++";
						
					} else {
						// tied game

					}
				} else
				// Check if move creates check
				if (ChessUtils.locationThreatened(ChessUtils.locateKing(board, turn), board)) {
					// Other side won
					notation += "+";
				}
				
				//log move
				record.add(notation);
				Bukkit.getLogger().info(notation);
			}
		}

		selectedPiece = loc.clone(); // Might cause issues

		// Make sure selectedPiece has correct turn
		if (locationOnBoard(selectedPiece)) {
			ChessPiece piece = board[selectedPiece[1]][selectedPiece[0]];
			if (piece == null || !piece.getColor().equals(turn)) {
				selectedPiece = new int[] { -1, -1 };
			}
		}

		// TODO: make map only render for near by players
		renderBoardForPlayers();
	}

	private void switchTurn() {
		if (turn.equals("WHITE")) {
			turn = "BLACK";
		} else if (turn.equals("BLACK")) {
			turn = "WHITE";
		}
	}

	public ItemStack getItem() {
		return gameItem;
	}

	public boolean locationOnBoard(int[] location) {
		// return whether a location is on the board. 0:x,1:y
		if (location == null)
			return false;

		if (location[1] >= 0 && location[0] >= 0 && location[1] < board.length && location[0] < board[0].length)
			return true;
		return false;
	}

	public boolean[][] getMovedPieces() {
		return movedPieces;
	}

	public ArrayList<String> getRecord() {
		return record;
	}
}
