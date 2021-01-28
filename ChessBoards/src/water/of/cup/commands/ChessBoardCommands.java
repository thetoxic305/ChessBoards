package water.of.cup.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import water.of.cup.ChessBoards;
import water.of.cup.chessBoard.ChessUtils;
import water.of.cup.data.ChessPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class ChessBoardCommands implements CommandExecutor {

	private ChessBoards instance = ChessBoards.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player p = (Player) sender;

		boolean permissionsEnabled = instance.getConfig().getBoolean("settings.chessboard.permissions");
		boolean databaseEnabled = instance.getConfig().getBoolean("settings.database.enabled");

		if (cmd.getName().equalsIgnoreCase("chessboards")) {
			if (permissionsEnabled && !p.hasPermission("chessboard.command"))
				return false;

			if(args.length == 0) {
				// Send help message
				p.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Chess" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Boards");
				p.sendMessage("/chessboards give" + ChatColor.GRAY + ": Gives you chessboard");
				p.sendMessage("/chessboards leaderboard" + ChatColor.GRAY + ": Lists top chess players");
				return false;
			}

			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("give")) {
					if (permissionsEnabled && !p.hasPermission("chessboard.command.give"))
						return false;

					boolean chessBoardGiven = ChessUtils.giveChessBoard(p);

					if (!chessBoardGiven)
						p.sendMessage("You need room in your inventory for this command to work");
					return chessBoardGiven;
				}

				if(args[0].equalsIgnoreCase("leaderboard")) {
					if (permissionsEnabled && !p.hasPermission("chessboard.command.leaderboard"))
						return false;

					if(!databaseEnabled) {
						p.sendMessage("Database configuration must be on in order to view leaderboard");
						return false;
					}

					int numChessPlayers = instance.getDataStore().getChessPlayerTotal();

					int page = 0;
					if(args.length > 1) {
						try {
							page = Integer.parseInt(args[1]);
						} catch (NumberFormatException e) {}
					}

					if(page < 0) page = 0;

					if(page > numChessPlayers / 10) page = numChessPlayers / 10;

					ArrayList<ChessPlayer> topPlayers = instance.getDataStore().getTopPlayers(page);

					if(topPlayers == null) {
						p.sendMessage("There was an error while trying to fetch top players");
						return false;
					}

					p.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Chess" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Boards " + ChatColor.RESET + "Leaderboard (1/" + ((numChessPlayers / 10) + 1) + ")");
					int num = 1;
					for(ChessPlayer chessPlayer : topPlayers) {
						UUID chessPlayerUUID = UUID.fromString(chessPlayer.getUuid());
						Player player = Bukkit.getPlayer(chessPlayerUUID);
						if(player == null) continue;

						double ratingRounded = (double) Math.round(chessPlayer.getRating() * 100) / 100;
						p.sendMessage(ChatColor.GRAY + "" + num + ". " + ChatColor.RESET + "" + player.getDisplayName() + " - " + ratingRounded);
						num++;
					}
					return true;
				}
			}
		}
		return false;
	}
}
