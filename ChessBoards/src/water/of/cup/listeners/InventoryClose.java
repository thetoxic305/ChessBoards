package water.of.cup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessGameState;

import java.util.HashSet;
import java.util.Set;

public class InventoryClose implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();
    private ChessBoardManager chessBoardManager = instance.getChessBoardManager();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        // Player closing create game menu
        if(instance.getCreateGameManager().containsKey(player)) {
            player.sendMessage("Leaving create game");
            instance.getCreateGameManager().remove(player);
            return;
        }

        // Player closing waiting menu
        if(chessBoardManager.getGameByPlayer(player) != null
                && chessBoardManager.getGameByPlayer(player).getGameState().equals(ChessGameState.WAITING_PLAYER)) {
            ChessGame chessGame = chessBoardManager.getGameByPlayer(player);

            player.sendMessage("Leaving waiting player menu");

            chessGame.setWhitePlayer(null);
            chessGame.setGameState(ChessGameState.IDLE);

            for(Player player1 : chessGame.getPlayerQueue()) {
                player1.sendMessage("Game owner has ended the game.");
                player1.closeInventory();
            }

            for(Player player1 : chessGame.getPlayerDecideQueue()) {
                player1.sendMessage("Game owner has ended the game.");
                player1.closeInventory();
            }

            chessGame.getPlayerQueue().clear();
            chessGame.getPlayerDecideQueue().clear();
            return;
        }

        // Player closing confirm game menu
        if(chessBoardManager.getGameByPlayer(player) != null
                && chessBoardManager.getGameByPlayer(player).getGameState().equals(ChessGameState.CONFIRM_GAME)) {

            player.sendMessage("closing confirm game");
            ChessGame chessGame = chessBoardManager.getGameByPlayer(player);
            Player otherPlayer = player.equals(chessGame.getBlackPlayer()) ? chessGame.getWhitePlayer() : chessGame.getBlackPlayer();

            chessGame.setBlackPlayer(null);
            chessGame.setWhitePlayer(null);
            chessGame.setGameState(ChessGameState.IDLE);

            otherPlayer.closeInventory();
        }

        // Player is leaving decision screen
        if(chessBoardManager.getGameByDecisionQueuePlayer(player) != null) {
            chessBoardManager.getGameByDecisionQueuePlayer(player).getPlayerDecideQueue().remove(player);
            return;
        }

        // Player is leaving the queue
        if(chessBoardManager.getGameByQueuePlayer(player) != null) {
            chessBoardManager.getGameByQueuePlayer(player).removePlayerFromQueue(player);
            return;
        }
    }

}
