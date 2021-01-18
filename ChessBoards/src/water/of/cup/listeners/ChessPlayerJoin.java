package water.of.cup.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import water.of.cup.ChessBoards;

public class ChessPlayerJoin implements Listener {

    private ChessBoards instance = ChessBoards.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (instance.getConfig().getBoolean("settings.database.enabled")) {
            instance.getDataStore().addChessPlayer(event.getPlayer());
        }
    }
}
