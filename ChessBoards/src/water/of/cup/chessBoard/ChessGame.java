package water.of.cup.chessBoard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import water.of.cup.ChessBoards;
import water.of.cup.inventories.ChessConfirmGameInventory;
import water.of.cup.inventories.ChessCreateGameInventory;
import water.of.cup.inventories.ChessJoinGameInventory;
import water.of.cup.inventories.ChessWaitingPlayerInventory;

public class ChessGame {
	private ChessBoards instance = ChessBoards.getInstance();

	private String turn;
	private ChessPiece[][] board;
	private int[] selectedPiece;
	private ItemStack gameItem;
	private boolean[][] movedPieces;
	private ArrayList<String> record;
	private ChessGameState gameState;
	private Player whitePlayer;
	private Player blackPlayer;
	private ChessWaitingPlayerInventory chessWaitingPlayerInventory;
	private ChessConfirmGameInventory chessConfirmGameInventory;
	private boolean ranked;
	private Set<Player> playerQueue = new HashSet<>();
	private Set<Player> playerDecideQueue = new HashSet<>();
	private String pawnPromotion = "NONE"; // "NONE" if no pawn promotion; ChessPiece.getColor() if pawn promotion
	private ArrayList<String> boardStates;
	private int fiftyMoveDrawCount;
	private String gameTimeString;
	private int gameId;

	private Clock clock;
	private int clockTime;
	private int clockIncrement;

	public ChessGame(ItemStack item, int gameId) {
		this.gameItem = item;
		this.gameId = gameId; // gameId is equal to the mapId

		gameState = ChessGameState.IDLE;
		whitePlayer = null;
		blackPlayer = null;
		gameTimeString = null;

		resetBoard(false);
	}

	public ChessGame(ItemStack item, String gameString, int gameId) {
		this.gameItem = item;
		this.gameId = gameId;

		gameState = ChessGameState.IDLE;
		whitePlayer = null;
		blackPlayer = null;
		gameTimeString = null;
		resetBoard(false);

		for (String arg : gameString.split(";")) {

			String key = arg.substring(0, arg.indexOf(":"));
			String result = arg.substring(arg.indexOf(":") + 1);

			if (key.equals("Turn")) {
				gameState = ChessGameState.INGAME;
				turn = result;
				clock = new Clock(0, this);
				continue;
			}

			if (key.equals("BoardStates")) {
				String lastState = "";
				for (String state : result.split(",")) {
					boardStates.add(state);
					lastState = state;
				}
				Bukkit.getLogger().info(lastState);
				board = ChessUtils.boardFromString(lastState);
			}

			if (key.equals("Record")) {
				for (String line : result.split(","))
					record.add(line);
			}

			if (key.equals("BlackPlayer")) {
				blackPlayer = Bukkit.getPlayer(UUID.fromString(result));
			}

			if (key.equals("WhitePlayer")) {
				whitePlayer = Bukkit.getPlayer(UUID.fromString(result));
			}

			if (key.equals("Ranked")) {
				ranked = Boolean.parseBoolean(result);
			}

			if (key.equals("PawnPromotion")) {
				pawnPromotion = result;
			}

			if (key.equals("FiftyMoveDrawCount")) {
				fiftyMoveDrawCount = Integer.parseInt(result);
			}

			if (key.equals("ClockIncrement")) {
				clockIncrement = Integer.parseInt(result);
			}

			if (key.equals("WhiteTime")) {
				clock.incementTime("WHITE", Double.parseDouble(result));
			}

			if (key.equals("BlackTime")) {
				clock.incementTime("BLACK", Double.parseDouble(result));
			}

		}
		
		if (gameState == ChessGameState.INGAME) {
			renderBoardForPlayers();
			clock.runTaskTimer(instance, 1, 1);
		}

	}

	public void resetBoard(boolean renderBoard) {
		// set base values
		clock = null;

		record = new ArrayList<String>();
		boardStates = new ArrayList<String>();
		selectedPiece = new int[] { -1, -1 };
		turn = "WHITE";
		movedPieces = new boolean[8][8];
		pawnPromotion = "NONE";
		clockTime = 1;
		clockIncrement = 0;
		gameTimeString = null;

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

		if (renderBoard)
			renderBoardForPlayers();
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

	public void click(int[] loc, Player player) {
		if (this.gameState != ChessGameState.INGAME)
			return;

		if (!this.hasPlayer(player))
			return;

		String color = "WHITE";
		if (blackPlayer.equals(player))
			color = "BLACK";

		// Make sure player is ingame and correct turn
		if (!this.turn.equals(color))
			return;

		// pawn promotion
		if (!pawnPromotion.equals("NONE")) {

			if (loc[1] <= 2 || loc[1] >= 5 || loc[0] <= 1 || loc[0] >= 6) // no piece selected
				return;

			String[] promotionPieces = new String[] { "ROOK", "KNIGHT", "BISHOP", "QUEEN" };
			int[] pawnPosition = ChessUtils.locatePromotedPawn(board, pawnPromotion);

			board[pawnPosition[1]][pawnPosition[0]] = ChessPiece
					.valueOf(pawnPromotion + "_" + promotionPieces[loc[0] - 2]);

			record.set(record.size() - 1, record.get(record.size() - 1) + "="
					+ board[pawnPosition[1]][pawnPosition[0]].getNotationCharacter());

			switchTurn();

			// Check if move creates check
			if (ChessUtils.locationThreatened(ChessUtils.locateKing(board, turn), board)) {
				record.set(record.size() - 1, record.get(record.size() - 1) + "+");
				// check if move creates checkmate
				if (!ChessUtils.colorHasMoves(board, turn)) {
					record.set(record.size() - 1, record.get(record.size() - 1) + "+");
				}
			}

			pawnPromotion = "NONE";
			selectedPiece = new int[] { -1, -1 };

			renderBoardForPlayers();
			return;
		}

		// move piece if possible
		if (locationOnBoard(selectedPiece)) {
			ChessPiece piece = board[selectedPiece[1]][selectedPiece[0]];

			// check if move is possible
			if (piece != null && piece.getColor().equals(turn)
					&& piece.getMoves(board, selectedPiece, getMovedPieces(), record)[loc[1]][loc[0]]) {
				// MoveMade!
				String notation = "";

				// reset fiftyMoveDrawCount if piece is pawn
				if (piece.toString().contains("PAWN")) {
					fiftyMoveDrawCount = 0;
				}

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
					// check if pawn reached end
					if ((piece == ChessPiece.BLACK_PAWN && loc[1] == 7)
							|| (piece == ChessPiece.WHITE_PAWN && loc[1] == 0)) {
						pawnPromotion = piece.getColor();
					} else {
						pawnPromotion = "NONE";
					}

					// check if move is en passent
					if (piece.toString().contains("PAWN") && otherPiece == null && loc[0] != selectedPiece[0]) {
						// take piece passed over
						board[selectedPiece[1]][loc[0]] = null;
					}

					// non-castle move made
					notation = piece.getNotationCharacter()
							+ ChessUtils.getNotationPosition(selectedPiece[0], selectedPiece[1]);

					// add piece taken to notation and reset fiftyMoveDrawCount
					if (otherPiece != null) {
						notation += "x";
						fiftyMoveDrawCount = 0;
					}
					notation += ChessUtils.getNotationPosition(loc[0], loc[1]);

					board[loc[1]][loc[0]] = piece;
					board[selectedPiece[1]][selectedPiece[0]] = null;

					movedPieces[loc[1]][loc[0]] = true;
					movedPieces[selectedPiece[1]][selectedPiece[0]] = true;

					selectedPiece = new int[] { -1, -1 };

				}

				// run only if no pawn promotion
				if (pawnPromotion.equals("NONE")) {

					switchTurn();

					// Check if move creates check
					if (ChessUtils.locationThreatened(ChessUtils.locateKing(board, turn), board)) {
						notation += "+";
						// check if move creates checkmate
						if (!ChessUtils.colorHasMoves(board, turn)) {
							notation += "+";
						}
					}

				}

				// log move
				record.add(notation);
				Bukkit.getLogger().info(notation);

				// CheckIfGameOver
				if (!ChessUtils.colorHasMoves(board, turn)) {
					// Check if winner
					if (ChessUtils.locationThreatened(ChessUtils.locateKing(board, turn), board)) {
						// Other side won
						if (turn.equals("WHITE")) {
							gameOver("BLACK");
						} else {
							gameOver("WHITE");
						}
					} else {
						// tied game
						gameOver("DRAW");
					}
					return;
				}

				// check if draw from position repeat three times
				String boardString = ChessUtils.boardToString(board);
				boardStates.add(boardString);
				if (getBoardRepeats(boardString) >= 3) {
					// draw
					gameOver("DRAW");
				}

				// check fifty move draw
				if (fiftyMoveDrawCount >= 50) {
					// draw
					gameOver("DRAW");
				}
			}
		}

		selectedPiece = loc.clone();

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

	public void gameOver(String winningColor) {
		gameOver(winningColor, "won");
	}

	public void gameOver(String winningColor, String winMessage) {
		renderBoardForPlayers();

		Player winner = whitePlayer;
		Player loser = blackPlayer;
		String losingColor = "BLACK";
		if (winningColor.equals("WHITE")) {
			// White player won

			record.add("1-0");
		} else if (winningColor.equals("BLACK")) {
			// Black player won

			losingColor = "WHITE";
			winner = blackPlayer;
			loser = whitePlayer;

			record.add("0-1");
		} else {
			// Tied game
			record.add("1/2-1/2");
			String endMessage = winner.getDisplayName() + " tied as " + winningColor.toLowerCase() + " against "
					+ loser.getDisplayName() + " as " + losingColor;

			winner.sendMessage(endMessage);
			loser.sendMessage(endMessage);

			whitePlayer = null;
			blackPlayer = null;

			return;
		}

		whitePlayer = null;
		blackPlayer = null;

		String endMessage = winner.getDisplayName() + " " + winMessage + " as " + winningColor.toLowerCase()
				+ " against " + loser.getDisplayName() + " as " + losingColor.toLowerCase();

		winner.sendMessage(endMessage);
		loser.sendMessage(endMessage);

		setGameState(ChessGameState.IDLE);
	}

	private int getBoardRepeats(String boardString) {
		int count = 0;

		for (String pastBoard : boardStates) {
			if (pastBoard.equals(boardString))
				count++;
		}
		return count;
	}

	public void startClocks() {
		clock = new Clock(clockTime, this);
		clock.runTaskTimer(instance, 1, 1);
	}

	private void switchTurn() {
		if (clock != null) {
			clock.incementTime(turn, clockIncrement);
			clock.run();
		}

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

	public ChessGameState getGameState() {
		return this.gameState;
	}

	public void setGameState(ChessGameState gameState) {
		this.gameState = gameState;
	}

	public void setWhitePlayer(Player player) {
		this.whitePlayer = player;
	}

	public void setBlackPlayer(Player player) {
		this.blackPlayer = player;
	}

	public Player getWhitePlayer() {
		return this.whitePlayer;
	}

	public Player getBlackPlayer() {
		return this.blackPlayer;
	}

	public void openWaitingPlayerInventory() {
		this.chessWaitingPlayerInventory = new ChessWaitingPlayerInventory(this);
		this.chessWaitingPlayerInventory.display(this.whitePlayer, true);
	}

	public void openConfirmGameInventory() {
		this.chessConfirmGameInventory = new ChessConfirmGameInventory(this);
		this.chessConfirmGameInventory.display(this.whitePlayer, true);
		this.chessConfirmGameInventory.display(this.blackPlayer, true);
	}

	public ChessConfirmGameInventory getChessConfirmGameInventory() {
		return this.chessConfirmGameInventory;
	}

	public ChessWaitingPlayerInventory getChessWaitingPlayerInventory() {
		return this.chessWaitingPlayerInventory;
	}

	public boolean hasPlayer(Player player) {
		if (this.blackPlayer != null && this.blackPlayer.equals(player))
			return true;
		if (this.whitePlayer != null && this.whitePlayer.equals(player))
			return true;
		return false;
	}

	public void addPlayerToQueue(Player player) {
		if (this.playerQueue.size() >= 3)
			return;

		if (this.playerDecideQueue.contains(player))
			this.playerDecideQueue.remove(player);

		this.playerQueue.add(player);

		// Re-render for the player waiting for match
		this.chessWaitingPlayerInventory.display(this.whitePlayer, false);
	}

	public void addPlayerToDecisionQueue(Player player) {
		this.playerDecideQueue.add(player);

		// Display game options to new player
		new ChessJoinGameInventory(this, player).display(player, true, false);
	}

	public void removePlayerFromQueue(Player player) {
		// Remove player from queue
		if (this.getPlayerQueue().contains(player))
			this.getPlayerQueue().remove(player);

		// Re-render for the player waiting for match
		this.chessWaitingPlayerInventory.display(this.whitePlayer, false);
	}

	public void setPlayerQueue(Set<Player> playerQueue) {
		this.playerQueue = playerQueue;
	}

	public void setPlayerDecideQueue(Set<Player> playerDecideQueue) {
		this.playerDecideQueue = playerDecideQueue;
	}

	public Set<Player> getPlayerQueue() {
		return this.playerQueue;
	}

	public Set<Player> getPlayerDecideQueue() {
		return this.playerDecideQueue;
	}

	public boolean isRanked() {
		return ranked;
	}

	public void setRanked(boolean ranked) {
		this.ranked = ranked;
	}

	public String getPawnPromotion() {
		return pawnPromotion;
	}

	public void setClocks(String timeType) {
		gameTimeString = timeType;
		int minutes = new Integer(timeType.substring(0, timeType.indexOf(" ")));
		setClocks(minutes * 60);

		if (timeType.contains("|")) {
			int stringPos = timeType.indexOf("|");
			clockIncrement = new Integer(timeType.substring(stringPos + 2, stringPos + 3));
		}
	}

	public void setClocks(double time) {
		clockTime = (int) time;
	}

	public String getGameTimeString() {
		return gameTimeString;
	}

	public String getTurn() {
		return turn;
	}

	public String toString() {
		String gameString = "";

		gameString += "MapID:" + ((MapMeta) gameItem.getItemMeta()).getMapView().getId() + ";";

		if (gameState == ChessGameState.INGAME) {
			gameString += "Turn:" + turn + ";";

			gameString += "BoardStates:";
			for (String boardState : boardStates) {
				gameString += boardState + ",";
			}
			gameString = gameString.substring(0, gameString.length() - 1);
			gameString += ";";

			gameString += "Record:";
			for (String line : record) {
				gameString += line + ",";
			}
			gameString = gameString.substring(0, gameString.length() - 1);
			gameString += ";";

			gameString += "BlackPlayer:" + blackPlayer.getUniqueId().toString() + ";";
			gameString += "WhitePlayer:" + whitePlayer.getUniqueId().toString() + ";";

			gameString += "Ranked:" + ranked + ";";
			gameString += "PawnPromotion:" + pawnPromotion + ";";
			gameString += "FiftyMoveDrawCount:" + fiftyMoveDrawCount + ";";

			gameString += "ClockIncrement:" + clockIncrement + ";";

			gameString += "WhiteTime:" + clock.getWhiteTime() + ";";
			gameString += "BlackTime:" + clock.getBlackTime() + ";";

		}

		return gameString;
	}

	public void storeGame() {
		String mapData = this.toString();
		String id = ((MapMeta) gameItem.getItemMeta()).getMapView().getId() + "";
		File file = new File(instance.getDataFolder(), "saved_games/game_" + id + ".txt");

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

	public int getGameId() {
		return this.gameId;
	}
}
