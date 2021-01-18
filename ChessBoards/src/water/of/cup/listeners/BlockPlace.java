package water.of.cup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.GameRenderer;

public class BlockPlace implements Listener {

    private ChessBoards pluginInstance = ChessBoards.getInstance();
    private ChessBoardManager chessBoardManager = pluginInstance.getChessBoardManager();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if(itemStack.getItemMeta() == null) return;

        NamespacedKey key = new NamespacedKey(pluginInstance, "chess_board");
        if(itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.DOUBLE)) {
            event.setCancelled(true);

            // remove 1 chessboard
            player.getInventory().getItemInMainHand()
                    .setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

            Bukkit.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {
                ItemStack chessBoardItem = new ItemStack(Material.FILLED_MAP, 1);
                MapMeta mapMeta = (MapMeta) chessBoardItem.getItemMeta();
                MapView mapView = Bukkit.createMap(player.getWorld());

                ChessGame game = new ChessGame(chessBoardItem, mapView.getId());

                mapView.setTrackingPosition(false);

                for (MapRenderer renderer : mapView.getRenderers())
                    mapView.removeRenderer(renderer);

                GameRenderer customRenderer = new GameRenderer(game);
                mapView.addRenderer(customRenderer);
                mapMeta.setMapView(mapView);

                chessBoardItem.setItemMeta(mapMeta);

                ItemFrame frame = player.getWorld().spawn(event.getBlockPlaced().getLocation(), ItemFrame.class);
                frame.setItem(chessBoardItem);
                frame.setFacingDirection(BlockFace.UP, true);
                
                //set Chessboard rotation
                double yaw = player.getEyeLocation().getYaw();
                if ((yaw > 45 && yaw < 135) || (yaw < 315 && yaw > 225))
                	frame.setRotation(Rotation.CLOCKWISE_45);
                	
                
            
                

//                frame.setInvulnerable(true);
//                frame.setFixed(true);

                chessBoardManager.addGame(game);
            });
        }
    }

}
