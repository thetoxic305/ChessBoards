package water.of.cup.chessboards.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import water.of.cup.chessboards.ChessBoards;

public enum ConfigMessage {

    // GUI MESSAGES
    MESSAGE_GUI_UP("settings.messages.gui.up", "&a/\\"),
    MESSAGE_GUI_DOWN("settings.messages.gui.down", "&a\\/"),
    MESSAGE_GUI_GAMETIME("settings.messages.gui.gametime", "&aGame Time: &2"),
    MESSAGE_GUI_WAGERAMOUNT("settings.messages.gui.wageramount", "&aWager Amount: &2$"),

    // CHAT MESSAGES
    MESSAGE_CHAT_PLAYER_INGAME("settings.messages.chat.playeringame", "&cYou must finish your game before joining another."),
    MESSAGE_CHAT_QUEUE_FULL("settings.messages.chat.queuefull", "&cToo many players queuing!"),
    MESSAGE_CHAT_GAME_ALREADY_CREATED("settings.messages.chat.gamealreadycreated", "&cA game has already been created for this board."),
    MESSAGE_CHAT_NOT_ENOUGH_MONEY_CREATE_WAGER("settings.messages.chat.insufficientwagercreatefunds", "&cYou do not have enough money to create the wager for this game."),
    MESSAGE_CHAT_GAME_ALREADY_STARTED("settings.messages.chat.gamealreadystarted", "&cGame owner has started the game."),
    MESSAGE_CHAT_NOT_ENOUGH_MONEY_ACCEPT_WAGER("settings.messages.chat.insufficientwageracceptfunds", "&cYou do not have enough money to accept this wager."),

    MESSAGE_CHAT_GAME_OVER_TIE("settings.messages.chat.tie", "%player_winnner% tied as %color_winner% against %player_loser% as %color_loser%"),
    MESSAGE_CHAT_GAME_OVER_WIN("settings.messages.chat.win", "%player_winnner% won as %color_winner% against %player_loser% as %color_loser%"),
    MESSAGE_CHAT_GAME_OVER_TIMEWIN("settings.messages.chat.timewin", "%player_winnner% won on time as %color_winner% against %player_loser% as %color_loser%"),
    MESSAGE_CHAT_GAME_OVER_FORFEIT("settings.messages.chat.forfeit", "%player_winnner% won by forfeit as %color_winner% against %player_loser% as %color_loser%"),

    MESSAGE_CHAT_GAME_ACCEPT_WAGER("settings.messages.chat.acceptwager", "%wager_player% has accepted your wager of %wager_amount%."),
    MESSAGE_CHAT_GAME_WAGER_ACCEPTED("settings.messages.chat.wageraccepted", "You have accepted %wager_player%'s wager of %wager_amount%."),

    MESSAGE_CHAT_COMMANDS_PLAYER_NOT_FOUND("settings.messages.chat.commands.playernotfound", "Could not find specified player."),
    MESSAGE_CHAT_COMMANDS_NO_INV_ROOM("settings.messages.chat.commands.noinvroom", "The receiving player does not have room in their inventory"),
    MESSAGE_CHAT_COMMANDS_NO_DB("settings.messages.chat.commands.nodb", "Database configuration must be on in order to view leaderboard"),
    MESSAGE_CHAT_COMMANDS_ERROR_FETCHING_PLAYERS("settings.messages.chat.commands.errortopplayers", "There was an error while trying to fetch top players"),

    MESSAGE_CHAT_COMMANDS_CHESSTEXT("settings.messages.chat.commands.chesstext", "&f&lChess&8&lBoards"),
    MESSAGE_CHAT_COMMANDS_LBTEXT("settings.messages.chat.commands.leaderboardtext", "&rLeaderboard"),
    MESSAGE_CHAT_COMMANDS_STATTEXT("settings.messages.chat.commands.stattext", "&7's stats"),
    MESSAGE_CHAT_COMMANDS_WLDTEXT("settings.messages.chat.commands.wldtext", "&7W/L/D: "),
    MESSAGE_CHAT_COMMANDS_RATINGTEXT("settings.messages.chat.commands.ratingtext", "&7Rating: "),
    MESSAGE_CHAT_COMMANDS_RATINGDEVIATIONTEXT("settings.messages.chat.commands.ratingdeviationtext", "&7Rating Deviation: "),
    MESSAGE_CHAT_COMMANDS_VOLATILITYTEXT("settings.messages.chat.commands.volatilitytext", "&7Volatility: "),

    MESSAGE_CHAT_COMMANDS_HELP_STATS("settings.messages.chat.commands.help.stats", "/chessboards stats [player name]&7: Show stats for a player"),
    MESSAGE_CHAT_COMMANDS_HELP_GIVE("settings.messages.chat.commands.help.give", "/chessboards give [player name]&7: Give a player a chessboard"),
    MESSAGE_CHAT_COMMANDS_HELP_LB("settings.messages.chat.commands.help.lb", "/chessboards leaderboard&7: Lists top chess players");

    //    MESSAGE_CHAT_COMMANDS_("settings.messages.chat.commands.", ""),
    // GUI titles
    // Create Ranked/Unranked Game
    // Ranked/Unranked
    // Join Game/Decline
    // Waiting for game creator
    // White/Black, Ready/Unready, Forfeit


    private final ChessBoards instance = ChessBoards.getInstance();
    private final String path;
    private final String defaultMessage;

    ConfigMessage(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String toString() {
        String configString = instance.getConfig().getString(this.path);

        if(configString == null) return "";

        return ChatColor.translateAlternateColorCodes('&', configString);
    }

    public String buildString(Player winner, String winnerColor, Player loser, String loserColor) {
        String formatted = this.toString();

        formatted = formatted.replace("%player_winnner%", winner.getDisplayName())
                .replace("%player_loser%", loser.getDisplayName())
                .replace("%color_winner%", winnerColor)
                .replace("%color_loser%", loserColor);

        return formatted;
    }

    public String buildString(Player wagerPlayer, double amount) {
        String formatted = this.toString();

        formatted = formatted.replace("%wager_player%", wagerPlayer.getDisplayName())
                .replace("%wager_amount%", amount + "");

        return formatted;
    }

    public String toRawString() {
        return ChatColor.stripColor(this.toString());
    }

    public String getPath() {
        return this.path;
    }

    public String getDefaultMessage() {
        return this.defaultMessage;
    }

}
