package water.of.cup.listeners;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessGame;

public class EntityDamageByEntity implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) return;

        if(!(event.getEntity() instanceof ItemFrame)) return;

        Player player = (Player) event.getDamager();

        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        ItemStack gameFrame = itemFrame.getItem();

        if(gameFrame.getItemMeta() == null || !(gameFrame.getItemMeta() instanceof MapMeta)) return;

        MapMeta mapMeta = (MapMeta) gameFrame.getItemMeta();

        if(mapMeta.getMapView() == null) return;

        int gameId = mapMeta.getMapView().getId();

        ChessGame chessGame = instance.getChessBoardManager().getGameByGameId(gameId);

        if(chessGame == null) return;

        event.setCancelled(true);

        if(chessGame.hasPlayer(player)) {
            chessGame.getChessConfirmGameInventory().display(player, true);
        }
    }
}
