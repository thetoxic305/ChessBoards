package water.of.cup.chessboards.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.chessBoard.ChessGame;

public class ChessPlayerQuit implements Listener {

    private final ChessBoards instance = ChessBoards.getInstance();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ChessGame chessGame = instance.getChessBoardManager().getGameByPlayer(player);
        if(chessGame != null) {
            chessGame.forfeitGame(player, false);
        }
    }

}
