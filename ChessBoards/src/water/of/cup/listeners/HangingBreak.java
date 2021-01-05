package water.of.cup.listeners;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessGame;

public class HangingBreak  implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
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
