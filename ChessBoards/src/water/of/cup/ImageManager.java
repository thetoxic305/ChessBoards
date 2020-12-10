package water.of.cup;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import water.of.cup.chessBoard.ChessPiece;

public class ImageManager {
	HashMap<ChessPiece, BufferedImage> images;

	public void loadImages() {
		images = new HashMap<ChessPiece, BufferedImage>();

		for (ChessPiece piece : ChessPiece.values()) {
			try {
				InputStream is = getClass().getClassLoader().getResourceAsStream("water/of/cup/images/" + piece.toString() + ".png");
				BufferedImage image = ImageIO.read(is);
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
