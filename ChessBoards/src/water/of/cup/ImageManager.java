package water.of.cup;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import water.of.cup.chessBoard.ChessPiece;

public class ImageManager {

	private HashMap<ChessPiece, BufferedImage> images;

	public void loadImages() {
		boolean customImages = ChessBoards.getInstance().getConfig().getBoolean("settings.chessboard.customImages");
		images = new HashMap<ChessPiece, BufferedImage>();

		// Make sure all images are places inside /ChessBoards/images on the server
		for (ChessPiece piece : ChessPiece.values()) {
			try {
				if(!customImages) {
					// TODO: FIX
					InputStream is = getClass().getClassLoader().getResourceAsStream("water/of/cup/images/" + piece.toString() + ".png");
					BufferedImage image = ImageIO.read(is);
					images.put(piece, image);
				} else {
					File file = new File(
							ChessBoards.getInstance().getDataFolder() + "/images/" + piece.toString() + ".png");
					BufferedImage image = ImageIO.read(file);
					images.put(piece, image);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public BufferedImage getImage(ChessPiece piece) {
		BufferedImage image = images.get(piece);
		if (piece.getColor().equals("BLACK")) {
			// flip black images
			AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
			tx.translate(-image.getWidth(null), -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			image = op.filter(image, null);
		}
		return image;
	}
}
