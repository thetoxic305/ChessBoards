package water.of.cup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessBoardManager;

public class InventoryClose implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();
    private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if(instance.getCreateGameManager().containsKey(player)) {
            player.sendMessage("Leaving create game");
            instance.getCreateGameManager().remove(player);
        }
    }

}
