package water.of.cup.chessboards.listeners;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.chessBoard.ChessGame;

public class HangingBreakByEntity implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.getRemover() == null) return;

        if(!(event.getRemover() instanceof Player)) return;

        if(!(event.getEntity() instanceof ItemFrame)) return;

        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        ItemStack gameFrame = itemFrame.getItem();

        if(gameFrame.getItemMeta() == null || !(gameFrame.getItemMeta() instanceof MapMeta)) return;

        MapMeta mapMeta = (MapMeta) gameFrame.getItemMeta();

        if(mapMeta.getMapView() == null) return;

        int gameId = mapMeta.getMapView().getId();

        ChessGame chessGame = instance.getChessBoardManager().getGameByGameId(gameId);

        if(chessGame == null) return;

        event.setCancelled(true);
    }

}
