package water.of.cup.chessboards.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.chessBoard.ChessUtils;
import water.of.cup.chessboards.data.ChessPlayer;
import water.of.cup.chessboards.utils.ConfigMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("give")) {
					if (permissionsEnabled && !p.hasPermission("chessboard.command.give"))
						return false;
					
					Player gets = p;
					if (args.length == 2) {
						gets = Bukkit.getPlayer(args[1]);
						if (gets == null) {
							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_PLAYER_NOT_FOUND.toString());
							return false;
						}
					}
					
					boolean chessBoardGiven = ChessUtils.giveChessBoard(gets);

					if (!chessBoardGiven)
						p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_NO_INV_ROOM.toString());
					return chessBoardGiven;
				}

				if (args[0].equalsIgnoreCase("leaderboard")) {
					if (permissionsEnabled && !p.hasPermission("chessboard.command.leaderboard"))
						return false;

					if (!databaseEnabled) {
						p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_NO_DB.toString());
						return false;
					}

					instance.getDataStore().getChessPlayerTotal(numChessPlayers -> {
						int page = 0;
						if (args.length > 1) {
							try {
								page = Integer.parseInt(args[1]) - 1;
							} catch (NumberFormatException e) {
							}
						}

						if (page < 0)
							page = 0;

						int numOfPages = (numChessPlayers / 10) + 1;

						if (page > numOfPages - 1)
							page = numOfPages - 1;

						int finalPage = page;

						instance.getDataStore().getTopPlayers(page, topPlayers -> {
							if (topPlayers == null) {
								p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_ERROR_FETCHING_PLAYERS.toString());
								return;
							}

							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_CHESSTEXT.toString() + " " + ConfigMessage.MESSAGE_CHAT_COMMANDS_LBTEXT.toString() + " (" + (finalPage + 1) + "/"
									+ numOfPages + ")");
							int num = 1 + (finalPage * 10);
							for (ChessPlayer chessPlayer : topPlayers) {
								UUID chessPlayerUUID = UUID.fromString(chessPlayer.getUuid());
								OfflinePlayer player = instance.getServer().getOfflinePlayer(chessPlayerUUID);

								double ratingRounded = (double) Math.round(chessPlayer.getRating() * 100) / 100;
								p.sendMessage(ChatColor.GRAY + "" + num + ". " + ChatColor.RESET + "" + player.getName() + " - "
										+ ratingRounded);
								num++;
							}
						});
					});

					return true;
				}

				if (args[0].equalsIgnoreCase("stats")) {
					if (permissionsEnabled && !p.hasPermission("chessboard.command.stats"))
						return false;

					if (!databaseEnabled) {
						p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_NO_DB.toString());
						return false;
					}

					if (args.length > 1) {
						// next arg is playername
						String name = args[1];

						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

						instance.getDataStore().getOfflineChessPlayerAsync(offlinePlayer.getUniqueId().toString(), chessPlayer -> {
							if(chessPlayer == null) {
								p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_PLAYER_NOT_FOUND.toString());
								return;
							}

							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_CHESSTEXT.toString() + ChatColor.GRAY + name + ChatColor.RESET + ConfigMessage.MESSAGE_CHAT_COMMANDS_STATTEXT);
							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_WLDTEXT.toString() + ChatColor.RESET + chessPlayer.getWins() + " W / " + chessPlayer.getLosses() + " L / " + chessPlayer.getTies() + " D");
							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_RATINGTEXT.toString() + ChatColor.RESET + (double) Math.round(chessPlayer.getRating() * 100) / 100);
							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_RATINGDEVIATIONTEXT.toString() + ChatColor.RESET + chessPlayer.getRatingDeviation());
							p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_VOLATILITYTEXT.toString() + ChatColor.RESET + chessPlayer.getVolatility());
						});

						return true;
					}
					// missing arg
					
					p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_HELP_STATS.toString());
					return false;
				}
			}
			// Send help message
			p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_CHESSTEXT.toString());
			p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_HELP_GIVE.toString());
			p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_HELP_LB.toString());
			p.sendMessage(ConfigMessage.MESSAGE_CHAT_COMMANDS_HELP_STATS.toString());
			return false;

		}
		return false;
	}
}
