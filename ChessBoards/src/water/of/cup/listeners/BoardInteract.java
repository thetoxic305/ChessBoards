package water.of.cup.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessGameState;
import water.of.cup.chessBoard.ChessUtils;
import water.of.cup.inventories.ChessCreateGameInventory;
import water.of.cup.inventories.ChessJoinGameInventory;

public class BoardInteract implements Listener {

	private ChessBoards instance = ChessBoards.getInstance();
	private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

	@EventHandler
	public void clickBoard(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		
		// attempt to find a chess game
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR) ||
				e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Vector direction = player.getEyeLocation().getDirection();
			RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
					direction, 3.5);
			if (result != null) {
				Collection<Entity> nearbyEntites = result.getHitBlock().getLocation().getWorld()
						.getNearbyEntities(result.getHitBlock().getLocation(), 2, 2, 2);
				for (Entity entity : nearbyEntites) {
					if (entity instanceof ItemFrame) {
						ItemFrame itemFrame = (ItemFrame) entity;
						
						if (!itemFrame.getAttachedFace().equals(result.getHitBlockFace()))
							return;
						
						if (chessBoardManager.hasGame(itemFrame.getItem())) {
							// chess game found
							if(e.getHand().equals(EquipmentSlot.HAND)) {
								e.setCancelled(true);
								return;
							}
							
							ChessGame game = chessBoardManager.getGame(itemFrame.getItem());

							player.sendMessage("Game found! Status: " + game.getGameState().toString());

							if(chessBoardManager.getGameByPlayer(player) != null
									&& chessBoardManager.getGameByPlayer(player) != game) {
								player.sendMessage("You must finish your game before joining another.");
								return;
							}

							if(game.getGameState().equals(ChessGameState.IDLE) || game.getGameState().equals(ChessGameState.WAITING_PLAYER)) {
								if(game.getGameState().equals(ChessGameState.IDLE)) {
									ChessCreateGameInventory chessCreateGameInventory = new ChessCreateGameInventory(game);
									chessCreateGameInventory.displayCreateGame(player, true);
									instance.getCreateGameManager().put(player, chessCreateGameInventory);
								} else {
									if(game.getPlayerQueue().size() < 3) {
										game.addPlayerToDecisionQueue(player);
									} else {
										player.sendMessage(ChatColor.RED + "Too many players queuing!");
									}
								}
								return;
							}

							double hitx = result.getHitPosition().getX();
							double hity = result.getHitPosition().getZ();

							int loc[] = ChessUtils.getChessBoardClickLocation(hitx, hity, itemFrame.getRotation(), direction);
							
							player.sendMessage("x:" + loc[0] + ", y:" + loc[1]);
							game.click(loc, player);
							
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

}
