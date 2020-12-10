package water.of.cup.chessBoard;

public enum ChessPiece {
	BLACK_PAWN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_QUEEN, BLACK_KING, WHITE_PAWN, WHITE_BISHOP, WHITE_KNIGHT,
	WHITE_ROOK, WHITE_QUEEN, WHITE_KING;

	public boolean[][] getMoves(ChessPiece[][] board, int[] pieceLoc) {
		return getMoves(board, pieceLoc, false);
	}

	public boolean[][] getMoves(ChessPiece[][] board, int[] pieceLoc, boolean canEndangerKing) {
		boolean[][] moves = new boolean[8][8];

		int x = pieceLoc[0];
		int y = pieceLoc[1];

		int[][] directions;

		// moves for each piece
		switch (this) {
		case BLACK_PAWN:
		case WHITE_PAWN:
			int forward = 0;
			int row = 0;
			if (this.getColor().equals("BLACK")) {
				forward = 1;
				row = 1;
			} else {
				forward = -1;
				row = 6;
			}
			// pawn move up
			if (checkMovePossible(pieceLoc, board, x, y + forward, true, false)) {
				if (checkMovePossible(pieceLoc, board, x, y + forward, canEndangerKing, false))
					moves[y + forward][x] = true;

				// pawn move second up
				if (y == row && checkMovePossible(pieceLoc, board, x, y + forward * 2, canEndangerKing, false)) {
					moves[y + forward * 2][x] = true;
				}
			}

			// pawn take sideways
			if (checkMovePossible(pieceLoc, board, x + 1, y + forward, canEndangerKing, true, true)) {
				moves[y + forward][x + 1] = true;
			}
			if (checkMovePossible(pieceLoc, board, x - 1, y + forward, canEndangerKing, true, true)) {
				moves[y + forward][x - 1] = true;

			}
			break;
		case BLACK_BISHOP:
		case WHITE_BISHOP:
			directions = new int[][] { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

			// loop through directions to see what moves are available
			for (int[] direction : directions) {
				int cX = x;
				int cY = y;

				while (checkMovePossible(pieceLoc, board, cX + direction[0], cY + direction[1], true)) {

					cX += direction[0];
					cY += direction[1];
					if (checkMovePossible(pieceLoc, board, cX, cY, canEndangerKing))
						moves[cY][cX] = true;
					if (checkMovePossible(pieceLoc, board, cX, cY, true, true, true))
						break;
				}
			}
			break;
		case BLACK_KING:
		case WHITE_KING:
			directions = new int[][] { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }, { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } };
			// loop through directions to see what moves are available
			for (int[] direction : directions) {
				if (checkMovePossible(pieceLoc, board, x + direction[0], y + direction[1], canEndangerKing)) {
					moves[y + direction[1]][x + direction[0]] = true;
				}
			}

			break;
		case BLACK_KNIGHT:
		case WHITE_KNIGHT:
			directions = new int[][] { { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 }, { 1, 2 }, { -1, 2 }, { 1, -2 },
					{ -1, -2 } };
			// loop through directions to see what moves are available
			for (int[] direction : directions) {
				if (checkMovePossible(pieceLoc, board, x + direction[0], y + direction[1], canEndangerKing)) {
					moves[y + direction[1]][x + direction[0]] = true;
				}
			}
			break;
		case BLACK_QUEEN:
		case WHITE_QUEEN:
			directions = new int[][] { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }, { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } };

			// loop through directions to see what moves are available
			for (int[] direction : directions) {
				int cX = x;
				int cY = y;

				while (checkMovePossible(pieceLoc, board, cX + direction[0], cY + direction[1], canEndangerKing)) {
					cX += direction[0];
					cY += direction[1];

					if (checkMovePossible(pieceLoc, board, cX, cY, true))
						moves[cY][cX] = true;
					if (checkMovePossible(pieceLoc, board, cX, cY, true, true, true))
						break;
				}
			}

			break;
		case BLACK_ROOK:
		case WHITE_ROOK:
			directions = new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

			// loop through directions to see what moves are available
			for (int[] direction : directions) {
				int cX = x;
				int cY = y;

				while (checkMovePossible(pieceLoc, board, cX + direction[0], cY + direction[1], true)) {
					cX += direction[0];
					cY += direction[1];
					if (checkMovePossible(pieceLoc, board, cX + direction[0], cY + direction[1], canEndangerKing))
						moves[cY][cX] = true;
					if (checkMovePossible(pieceLoc, board, cX, cY, true, true, true))
						break;
				}
			}
			break;
		default:
			break;

		}

		return moves;
	}

	public String getColor() {
		// return the color of the chess piece
		if (this.toString().contains("BLACK"))
			return "BLACK";
		if (this.toString().contains("WHITE"))
			return "WHITE";
		return "EMPTY";
	}

	public boolean checkMovePossible(int[] pieceLoc, ChessPiece[][] board, int x, int y, boolean canEndangerKing) {
		return checkMovePossible(pieceLoc, board, x, y, canEndangerKing, true, false);
	}

	public boolean checkMovePossible(int[] pieceLoc, ChessPiece[][] board, int x, int y, boolean canEndangerKing,
			boolean canTake) {
		return checkMovePossible(pieceLoc, board, x, y, canEndangerKing, canTake, false);
	}

	public boolean checkMovePossible(int[] pieceLoc, ChessPiece[][] board, int x, int y, boolean canEndangerKing,
			boolean canTake, boolean mustTake) {

		// check if newLoc is within the boards size
		if (!(x < 8 && y >= 0 && y < 8 && x >= 0))
			return false;

		// check if newLoc is empty
		if ((board[y][x] == null)) {
			// check if piece can only take
			if (mustTake)
				return false;
		} else
		// check if pieces are the same color
		if (this.getColor().equals(board[y][x].getColor())) {
			return false;
		} else
		// pieces are opposite colors
		if (canTake == false) {
			return false;
		}

		// check if move endangers king
		ChessPiece[][] testBoard = ChessUtils.cloneBoard(board);
		testBoard[y][x] = this;
		testBoard[pieceLoc[1]][pieceLoc[0]] = null;
		if (!canEndangerKing && !getColor().equals("EMPTY") && ChessUtils.locationThreatened(
				ChessUtils.locateKing(testBoard, getColor()), testBoard))
			return false;

		// checks concluded
		return true;
	}

}