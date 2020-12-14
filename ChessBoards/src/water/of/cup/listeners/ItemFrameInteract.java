package water.of.cup.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessUtils;
import water.of.cup.chessBoard.GameRenderer;

public class ItemFrameInteract implements Listener {

	private ChessBoards instance = ChessBoards.getInstance();
	private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

	@EventHandler
	public void clickItemFrame(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();

		// check if Item Frame is clicked
		if (!e.getRightClicked().getType().equals(EntityType.ITEM_FRAME))
			return;

		ItemFrame itemFrame = (ItemFrame) e.getRightClicked();

		// attempt to find a chess game
		Vector direction = player.getEyeLocation().getDirection();
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
				direction, 4);
		if (result != null) {
			if (chessBoardManager.hasGame(itemFrame.getItem())) {
				// chess game found
				if(player.isSneaking()) {
					player.sendMessage("Opening chest GUI...");
					return;
				}

				player.sendMessage("Game found");

				ChessGame game = chessBoardManager.getGame(itemFrame.getItem());
				double hitx = result.getHitPosition().getX();
				double hity = result.getHitPosition().getZ();

				int loc[] = ChessUtils.getChessBoardClickLocation(hitx, hity, itemFrame.getRotation(), direction);
				player.sendMessage("x:" + loc[0] + ", y:" + loc[1]);
				game.click(loc);

				e.setCancelled(true);
			}
		}
	}
}
