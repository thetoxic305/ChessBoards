package water.of.cup;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import water.of.cup.chessBoard.ChessPiece;

public class ImageManager {
	HashMap<ChessPiece, BufferedImage> images;

	public void loadImages() {
		images = new HashMap<ChessPiece, BufferedImage>();

		// Make sure all images are places inside /ChessBoards/images on the server
		for (ChessPiece piece : ChessPiece.values()) {
			try {
				File file = new File(ChessBoards.getInstance().getDataFolder() + "/images/" + piece.toString() + ".png");
				BufferedImage image = ImageIO.read(file);
				images.put(piece, image);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public BufferedImage getImage(ChessPiece piece) {
		return images.get(piece);
	}
}
