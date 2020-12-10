package water.of.cup.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessUtils;

public class ChessBoardCommands implements CommandExecutor {

	private ChessBoards instance = ChessBoards.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("newChessBoard")) {
			if (instance.getConfig().getBoolean("settings.chessboard.permissions")) {
				if (!p.hasPermission("chessboard.command"))
					return false;
			}
			boolean chessBoardGiven = ChessUtils.giveChessBoard(p);
			
			if (!chessBoardGiven)
				p.sendMessage("You need room in your inventory for this command to work");
			return chessBoardGiven;	
		}
		return false;
	}

}
