package water.of.cup.chessboards.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import water.of.cup.chessboards.ChessBoards;

import java.util.ArrayList;
import java.util.List;

public class ChessBoardCommandsTabCompleter implements TabCompleter {

    private ChessBoards instance = ChessBoards.getInstance();

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> arguments = new ArrayList<>();
        if(strings.length == 1) {
            arguments.add("leaderboard");
            arguments.add("give");
        } else if(strings.length == 2 && strings[0].equalsIgnoreCase("leaderboard")) {
            int numChessPlayers = instance.getDataStore().getChessPlayerTotal();
            int extraPages = (numChessPlayers / 10);
            for(int i = 0; i < extraPages; i++) {
                arguments.add((i + 2) + "");
            }
        }

        return arguments;
    }
}
