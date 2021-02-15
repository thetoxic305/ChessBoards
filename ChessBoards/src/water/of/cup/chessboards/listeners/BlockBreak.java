package water.of.cup.chessboards.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.chessBoard.ChessBoardManager;
import water.of.cup.chessboards.chessBoard.ChessGame;

import java.util.Collection;

public class BlockBreak implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();
    private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if(block.getType().equals(Material.BARRIER)) {
            Collection<Entity> nearbyEntities = event.getPlayer().getWorld().getNearbyEntities(block.getBoundingBox());
            for (Entity entity : nearbyEntities) {
                if (!(entity instanceof ItemFrame))
                    continue;
                ItemFrame frame = (ItemFrame) entity;
                ItemStack item = frame.getItem();
                if (item != null && item.getType() == Material.FILLED_MAP && ((MapMeta) item.getItemMeta()).hasMapId()) {
                    ChessGame tempGame = chessBoardManager.getGameByGameId(((MapMeta) item.getItemMeta()).getMapId());
                    if (tempGame != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

}
