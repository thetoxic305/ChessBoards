package water.of.cup.chessboards.utils;

import org.bukkit.ChatColor;
import water.of.cup.chessboards.ChessBoards;

public enum ConfigMessageUtils {

    MESSAGES_GUI_UP("settings.messages.gui.up"),
    MESSAGE_GUI_DOWN("settings.messages.gui.down"),
    MESSAGE_GUI_GAMETIME("settings.messages.gui.gametime"),
    MESSAGE_GUI_WAGERAMOUNT("settings.messages.gui.wageramount");

    private final ChessBoards instance = ChessBoards.getInstance();
    private final String path;

    ConfigMessageUtils(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        String configString = instance.getConfig().getString(this.path);

        if(configString == null) return null;

        return ChatColor.translateAlternateColorCodes('&', configString);
    }

    public String toRawString() {
        return ChatColor.stripColor(this.toString());
    }

}
