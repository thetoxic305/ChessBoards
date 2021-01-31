package water.of.cup.chessboards;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jdk.internal.util.xml.impl.Input;
import org.bukkit.Bukkit;
import water.of.cup.chessboards.chessBoard.ChessPiece;

public class ImageManager {

	private HashMap<ChessPiece, BufferedImage> images;

	public boolean loadImages() {
		boolean customImages = ChessBoards.getInstance().getConfig().getBoolean("settings.chessboard.customImages");
		images = new HashMap<>();

		// Make sure all images are places inside /ChessBoards/images on the server
		for (ChessPiece piece : ChessPiece.values()) {
			try {
				if(!customImages) {
					String filePath = "water/of/cup/chessboards/images/" + piece.toString() + ".png";
					InputStream is = ChessBoards.getInstance().getResource(filePath);

					if(is == null) {
						Bukkit.getLogger().warning("[ChessBoards] Error loading default images at path " + filePath);
						return false;
					}

					BufferedImage image = ImageIO.read(is);
					images.put(piece, image);
				} else {
					File file = new File(
							ChessBoards.getInstance().getDataFolder() + "/images/" + piece.toString() + ".png");
					BufferedImage image = ImageIO.read(file);
					images.put(piece, image);
				}
			} catch (IOException e) {
				Bukkit.getLogger().warning("[ChessBoards] Error loading images");
				e.printStackTrace();
				return false;
			}
		}

		return true;
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
