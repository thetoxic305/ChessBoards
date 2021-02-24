package water.of.cup.chessboards.listeners;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.chessBoard.ChessBoardManager;
import water.of.cup.chessboards.chessBoard.ChessGame;
import water.of.cup.chessboards.chessBoard.ChessGameState;
import water.of.cup.chessboards.chessBoard.ChessUtils;
import water.of.cup.chessboards.inventories.ChessCreateGameInventory;

public class BoardInteract implements Listener {

	private ChessBoards instance = ChessBoards.getInstance();
	private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

	@EventHandler
	public void clickBoard(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		// attempt to find a chess game
		Vector direction = player.getEyeLocation().getDirection();

//		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
//				direction, 3.5);

		// get nearby chessgames
		Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), 4, 4, 4);

		RayTraceResult result = null;
		ItemFrame gameFrame = null;

		for (Entity entity : nearbyEntities) {

			if (!(entity instanceof ItemFrame))
				continue;
			ItemFrame frame = (ItemFrame) entity;
			ItemStack item = frame.getItem();
			if (item != null && item.getType() == Material.FILLED_MAP && ((MapMeta) item.getItemMeta()).hasMapId()) {
				ChessGame tempGame = chessBoardManager.getGameByGameId(((MapMeta) item.getItemMeta()).getMapId());
				if (tempGame != null) {
					Vector pos = frame.getLocation().toVector();
					double x = pos.getX();
					double y = pos.getY();
					double z = pos.getZ();
					BoundingBox box = new BoundingBox(x - 0.5, y - .0313, z - 0.5, x + 0.5, y + 0.0313, z + 0.5);
					RayTraceResult tempResult = box.rayTrace(player.getEyeLocation().toVector(), direction, 5);
					if (tempResult != null) {
						result = tempResult;
						gameFrame = frame;
						break;
					}
				}
			}
		}

		if (result != null) {
//			if (gameFrame.getAttachedFace().getOppositeFace() != result.getHitBlockFace())
//				return;

			ItemStack gameItem = gameFrame.getItem();

			if (gameItem.getItemMeta() == null || !(gameItem.getItemMeta() instanceof MapMeta))
				return;

			MapMeta mapMeta = (MapMeta) gameItem.getItemMeta();

			if (mapMeta.getMapView() == null)
				return;

			int gameId = mapMeta.getMapView().getId();

			if (chessBoardManager.getGameByGameId(gameId) != null) {
				// chess game found
				if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if (e.getHand().equals(EquipmentSlot.HAND)) {
						return;
					}
				}

				if (instance.getConfig().getBoolean("settings.chessboard.permissions")
						&& !player.hasPermission("chessboard.interact"))
					return;

				ChessGame game = chessBoardManager.getGameByGameId(gameId);

				player.sendMessage("Game found! Status: " + game.getGameState().toString());

				if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_AIR)) {
					e.setCancelled(true);

					if(game.getGameState().equals(ChessGameState.IDLE)) {
						if(instance.getConfig().getBoolean("settings.chessboard.permissions")
								&& !player.hasPermission("chessboard.destroy")) return;

						game.delete();
						Location frameLoc = gameFrame.getLocation();
						gameFrame.remove();

						frameLoc.getBlock().setType(Material.AIR);

						player.getWorld().dropItem(frameLoc, ChessUtils.getChessBoardItem());
						return;
					}

					if(game.hasPlayer(player)) {
						game.getChessConfirmGameInventory().display(player, true);
					}
					return;
				}

				if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if (chessBoardManager.getGameByPlayer(player) != null
							&& chessBoardManager.getGameByPlayer(player) != game) {
						player.sendMessage("You must finish your game before joining another.");
						return;
					}

					if (game.getGameState().equals(ChessGameState.IDLE)
							|| game.getGameState().equals(ChessGameState.WAITING_PLAYER)) {
						if (game.getGameState().equals(ChessGameState.IDLE)) {
							ChessCreateGameInventory chessCreateGameInventory = new ChessCreateGameInventory(game);
							chessCreateGameInventory.displayCreateGame(player, true);
							instance.getCreateGameManager().put(player, chessCreateGameInventory);
						} else {
							if (game.getPlayerQueue().size() < 3) {
								game.addPlayerToDecisionQueue(player);
							} else {
								player.sendMessage(ChatColor.RED + "Too many players queuing!");
							}
						}
						return;
					}

					double hitx = result.getHitPosition().getX();
					double hity = result.getHitPosition().getZ();

					int loc[] = ChessUtils.getChessBoardClickLocation(hitx, hity, gameFrame.getRotation(), direction);

					game.click(loc, player);

					e.setCancelled(true);
				}

			}
		}
	}
}
