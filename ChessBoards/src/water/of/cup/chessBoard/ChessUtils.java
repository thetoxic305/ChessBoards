package water.of.cup.chessBoard;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_14_R1.ArgumentScoreholder.c;

public class ChessUtils {
	
	private static String[] xPositionLetters = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
			
	public static boolean locationThreatened(int[] location, ChessPiece[][] originalBoard) {
		// returns true if a piece could be taken at this location
		ChessPiece[][] board = cloneBoard(originalBoard);

		// place King
		//board[location[1]][location[0]] = piece;

		// check if piece in danger
		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[0].length; x++) {
				ChessPiece attackingPiece = board[y][x];
				if (attackingPiece != null && !attackingPiece.getColor().equals(board[location[1]][location[0]].getColor())
						&& attackingPiece.getMoves(board, new int[] { x, y }, new boolean[8][8], new ArrayList<String>(), true)[location[1]][location[0]])
					return true;
			}
		}
		return false;
	}

	public static int[] locateKing(ChessPiece[][] board, String color) {
		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[0].length; x++) {
				if (board[y][x] != null && board[y][x].equals(ChessPiece.valueOf(color + "_KING")))
					return new int[] { x, y };
			}
		}
		return new int[] { -1, -1 };
	}

	public static boolean giveChessBoard(Player player) {
		if (player.getInventory().firstEmpty() != -1) { // check to make sure there is room in the inventory for the map
			player.getInventory().addItem(getChessBoardItem());
			return true;
		}
		return false;
	}

	public static ItemStack getChessBoardItem() {
		ItemStack chessboard = new ItemStack(Material.PAPER);
		ItemMeta chessboardMeta = (ItemMeta) chessboard.getItemMeta();
		chessboardMeta.setDisplayName(ChatColor.DARK_BLUE + "Chess Board");
		chessboard.setItemMeta(chessboardMeta);

		return chessboard;
	}

	public static int[] getChessBoardClickLocation(double hitx, double hity, Rotation rotation, Vector direction) {
		// move back 1/16th block y
		direction = direction.normalize();

		double vX = direction.getX();
		double vY = direction.getY();
		double vZ = direction.getZ();

		double multiplier = 1.0 / 16 / vY;
		hitx += vX * multiplier;
		hity += vZ * multiplier;

		// get square selected
		int x = (int) ((hitx - Math.floor(hitx)) * 8);
		int y = (int) ((hity - Math.floor(hity)) * 8);

//		int oldx = x;
//		switch (rotation) {
//		case CLOCKWISE:
//			x = y;
//			y = 8 - oldx;
//			break;
//		case FLIPPED:
//			x = 8 - x;
//			y = 8 - y;
//			break;
//		case COUNTER_CLOCKWISE:
//			x = 8 - y;
//			break;
//		default:
//			break;
//		}
		int[] loc = new int[] { x, y };
		return loc;
	}

	public static ChessPiece[][] cloneBoard(ChessPiece[][] originalBoard) {
		ChessPiece[][] board = new ChessPiece[originalBoard.length][originalBoard[0].length];

		// clone board
		int i = 0;
		for (ChessPiece[] line : originalBoard) {
			board[i] = line.clone();
			i++;
		}
		return board;
	}

	public static boolean[][] allMovesForColor(ChessPiece[][] board, String color, boolean[][] movedPieces) {
		boolean[][] moves = new boolean[board.length][board[0].length];

		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[0].length; x++) {
				ChessPiece attackingPiece = board[y][x];
				if (attackingPiece != null && !attackingPiece.getColor().equals(color)) {
					moves = combineMoves(moves, attackingPiece.getMoves(board, new int[] { x, y }, movedPieces, new ArrayList<String>()));
				}
			}
		}
		return moves;
	}

	public static boolean[][] combineMoves(boolean[][] moves1, boolean[][] moves2) {
		boolean[][] moves = new boolean[moves1.length][moves1[0].length];
		for (int y = 0; y < moves1.length; y++) {
			for (int x = 0; x < moves1[0].length; x++) {
				if (moves1[y][x] || moves2[y][x]) {
					moves[y][x] = true;
				}
			}
		}
		return moves;
	}
	
	public static boolean colorHasMoves(ChessPiece[][] board, String color) {
		boolean[][] moves = allMovesForColor(board, color, new boolean[8][8]);
		for (boolean[] line : moves) {
			for (boolean move : line) {
				if (move)
					return true;
			}
		}
		return false;
	}

	public static String getNotationPosition(int x, int y) {
		return xPositionLetters[x] + (8 - y);
	}
}
