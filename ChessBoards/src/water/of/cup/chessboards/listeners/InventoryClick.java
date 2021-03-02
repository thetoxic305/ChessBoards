package water.of.cup.chessboards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.persistence.PersistentDataType;

import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.Utils.GUIUtils;
import water.of.cup.chessboards.chessBoard.ChessGame;
import water.of.cup.chessboards.chessBoard.ChessGameState;
import water.of.cup.chessboards.chessBoard.RequestWager;
import water.of.cup.chessboards.chessBoard.Wager;
import water.of.cup.chessboards.inventories.ChessInGameInventory;
import water.of.cup.chessboards.inventories.ChessCreateGameInventory;
import water.of.cup.chessboards.inventories.ChessJoinGameInventory;
import water.of.cup.chessboards.inventories.ChessWagerViewInventory;
import water.of.cup.chessboards.inventories.ChessWaitingPlayerInventory;

public class InventoryClick implements Listener {

    private ChessBoards pluginInstance = ChessBoards.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if(event.getClickedInventory() == null) return;

        if (event.getView().getTitle().contains(ChessCreateGameInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {

            if(!pluginInstance.getCreateGameManager().containsKey(player)) return;

            event.setCancelled(true);

            ChessCreateGameInventory chessCreateGameInventory = pluginInstance.getCreateGameManager().get(player);

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            // Exit button
            if(event.getCurrentItem().getType().equals(Material.BARRIER)) {
                player.closeInventory();
                return;
            }

            if(itemName.contains("Create") && (event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE))) {
                if(!chessCreateGameInventory.getChessGame().getGameState().equals(ChessGameState.IDLE)) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "A game has already been created for this board.");
                    return;
                }
                if (pluginInstance.getEconomy().getBalance(player) < chessCreateGameInventory.getWager()) {
        			player.sendMessage(ChatColor.RED + "You do not have enough money to create the wager for this game.");
        			return;
        		}

                player.closeInventory();

                chessCreateGameInventory.getChessGame().resetBoard(true);

                chessCreateGameInventory.getChessGame().setGameState(ChessGameState.WAITING_PLAYER);
                chessCreateGameInventory.getChessGame().setWhitePlayer(player);

                // Sets game settings
                chessCreateGameInventory.getChessGame().setRanked(chessCreateGameInventory.isRanked());
                chessCreateGameInventory.getChessGame().setClocks(chessCreateGameInventory.getGameTimeString());
                chessCreateGameInventory.getChessGame().setGameWager(chessCreateGameInventory.getWager());
//                chessCreateGameInventory.getChessGame().setWager(chessCreateGameInventory.getWager());

                chessCreateGameInventory.getChessGame().openWaitingPlayerInventory();
                return;
            }

            if(itemName.equals("Ranked") && (event.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE) ||
                    event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE))) {
                chessCreateGameInventory.toggleRanked();
                chessCreateGameInventory.displayCreateGame(player, false);
                return;
            }

            if(event.getCurrentItem().getType().equals(Material.SKELETON_SKULL)) {
                // Increment
                if(itemName.equals("/\\")) {
                    Material materialBelow = event.getClickedInventory().getItem(event.getRawSlot() + 9).getType();
                    switch (materialBelow) {
                        case CLOCK:
                            chessCreateGameInventory.incrementGameTime();
                            break;
                        case GOLD_INGOT:
                            chessCreateGameInventory.incrementWager();
                            break;
                    }

                    chessCreateGameInventory.displayCreateGame(player, false);
                    return;
                }

                // Decrement
                if(itemName.equals("\\/")) {
                    Material materialAbove = event.getClickedInventory().getItem(event.getRawSlot() - 9).getType();
                    switch (materialAbove) {
                        case CLOCK:
                            chessCreateGameInventory.decrementGameTime();
                            break;
                        case GOLD_INGOT:
                            chessCreateGameInventory.decrementWager();
                            break;
                    }

                    chessCreateGameInventory.displayCreateGame(player, false);
                    return;
                }

                return;
            }
        }

        if (event.getView().getTitle().contains(ChessWaitingPlayerInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {

            ChessGame chessGame = pluginInstance.getChessBoardManager().getGameByPlayer(player);

            if(chessGame == null) return;

            event.setCancelled(true);

            // Exit button
            if(event.getCurrentItem().getType().equals(Material.BARRIER)) {
                player.closeInventory();
                return;
            }

            // Game owner accepts or declines players in queue
            if(event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE) ||
                    event.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE) ) {

                if(event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
                    String playerName = ChatColor.stripColor(event.getClickedInventory().getItem(event.getRawSlot() - 18).getItemMeta().getDisplayName());

                    Player clickedPlayer = Bukkit.getPlayer(playerName);

                    if(clickedPlayer == null) {
                        Bukkit.getLogger().warning("[ChessBoards] Could not find clicked player " + playerName);
                        return;
                    }

                    player.closeInventory();

                    chessGame.setGameState(ChessGameState.CONFIRM_GAME);

                    if(chessGame.getPlayerQueue().contains(clickedPlayer)) {
                        chessGame.getPlayerQueue().remove(clickedPlayer);
                        clickedPlayer.closeInventory();
                    }

                    // Remove everyone else in GUI
                    for(Player player1 : chessGame.getPlayerQueue()) {
                        player1.sendMessage("Game owner has started the game.");
                        player1.closeInventory();
                    }

                    for(Player player1 : chessGame.getPlayerDecideQueue()) {
                        player1.sendMessage("Game owner has started the game.");
                        player1.closeInventory();
                    }

                    chessGame.getPlayerQueue().clear();
                    chessGame.getPlayerDecideQueue().clear();

                    chessGame.setBlackPlayer(clickedPlayer);
                    chessGame.setWhitePlayer(player);

                    // Adds game wager to chess game
                    Wager wager = new Wager(chessGame.getWhitePlayer(), chessGame.getBlackPlayer(), "WHITE", chessGame.getGameWager());
                    chessGame.addWager(wager);

                    chessGame.openConfirmGameInventory();

                } else  {
                    String playerName = ChatColor.stripColor(event.getClickedInventory().getItem(event.getRawSlot() - 27).getItemMeta().getDisplayName());
                    Player clickedPlayer = Bukkit.getPlayer(playerName);

                    clickedPlayer.closeInventory();
                }

                return;
            }

            return;
        }

        if (event.getView().getTitle().contains(ChessJoinGameInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {

            ChessGame chessGame = pluginInstance.getChessBoardManager().getGameByDecisionQueuePlayer(player);

            if(chessGame == null) chessGame = pluginInstance.getChessBoardManager().getGameByQueuePlayer(player);

            if(chessGame == null) return;

            event.setCancelled(true);

            // Exit button
            if(event.getCurrentItem().getType().equals(Material.BARRIER)) {
                player.closeInventory();
                return;
            }

            // Join game button
            if(event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
            	if (pluginInstance.getEconomy().getBalance(player) < chessGame.getGameWager()) {
        			player.sendMessage(ChatColor.RED + "You do not have enough money to accept this wager.");
        			return;
        		}
            	
                chessGame.addPlayerToQueue(player);

                event.getClickedInventory().setItem(33, GUIUtils.createItemStack(ChatColor.GREEN + "Waiting for game creator...", Material.CLOCK));
                event.getClickedInventory().setItem(42, GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));
                return;
            }

            // Decline game button
            if(event.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                player.closeInventory();
                return;
            }

        }

        if (event.getView().getTitle().contains(ChessInGameInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {


            int chessGameID = event.getClickedInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(ChessBoards.getKey(), PersistentDataType.INTEGER);

            ChessGame chessGame = pluginInstance.getChessBoardManager().getGameByGameId(chessGameID);
            if(chessGame == null) return;

            event.setCancelled(true);

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (event.getCurrentItem().getType().equals(Material.BOOK)) {
            	ChessWagerViewInventory chessWagerViewInventory = new ChessWagerViewInventory(chessGame, player);
            	chessWagerViewInventory.display(true);
            	chessGame.addWagerViewInventory(chessWagerViewInventory);
            	return;
            }

            if(event.getCurrentItem().getType().equals(Material.YELLOW_STAINED_GLASS_PANE) ||
                    event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {

                if(!(chessGame.getGameState().equals(ChessGameState.CONFIRM_GAME))) return;

                String playerName = ChatColor.stripColor(event.getClickedInventory().getItem(event.getRawSlot() - 27).getItemMeta().getDisplayName());
                Player clickedPlayer = Bukkit.getPlayer(playerName);

                if(clickedPlayer == null) {
                    Bukkit.getLogger().warning("[ChessBoards] Could not find clicked player " + playerName);
                    return;
                }

                if(clickedPlayer.equals(player)) {
                    chessGame.getChessConfirmGameInventory().togglePlayerReady(player);
                    return;
                }
                return;
            }

            if(event.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)
                    && itemName.contains("Forfeit")) {
                boolean didForfeit = chessGame.forfeitGame(player);
                if(!didForfeit) Bukkit.getLogger().warning("[ChessBoards] Could not forfeit game " + chessGame.getGameId());
            }
            return;
        }

		if (event.getView().getTitle().contains(ChessWagerViewInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {

            int chessGameID = event.getClickedInventory().getItem(0).getItemMeta().getPersistentDataContainer().get(ChessBoards.getKey(), PersistentDataType.INTEGER);

            ChessGame chessGame = pluginInstance.getChessBoardManager().getGameByGameId(chessGameID);
            if(chessGame == null) return;

            ChessWagerViewInventory chessWagerViewInventory = chessGame.getWagerViewByPlayer(player);
            if(chessWagerViewInventory == null) return;

            event.setCancelled(true);

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            Material itemType = event.getCurrentItem().getType();

            RequestWager playerWager = chessGame.getRequestWagerByPlayer(player);

            if(itemType.equals(Material.PLAYER_HEAD)
                    && (itemName.equals("Decrease") || itemName.equals("Increase")) && playerWager == null) {
                boolean shifting = event.getClick().equals(ClickType.SHIFT_LEFT);

                switch (itemName) {
                    case "Increase":
                        chessWagerViewInventory.incrementWager(shifting);
                        break;
                    case "Decrease":
                        chessWagerViewInventory.decrementWager(shifting);
                        break;
                }

                chessWagerViewInventory.display(false);
                return;
            }

            // Create wager
            if(itemType.equals(Material.GREEN_STAINED_GLASS_PANE)
                    && itemName.contains("Create") && playerWager == null) {

                int wager = chessWagerViewInventory.getWagerAmount();
                if(wager <= 0) return;
                
                if (pluginInstance.getEconomy().getBalance(player) < wager) {
        			player.sendMessage(ChatColor.RED + "You do not have enough money to create this wager.");
        			return;
        		}
                
                RequestWager requestWager = new RequestWager(player, chessWagerViewInventory.getCreateWagerColor(), wager);
                chessGame.addRequestWager(requestWager);
                chessGame.updateRequestWagerInventories();
                return;
            }

            // Cancel wager
            if(itemType.equals(Material.YELLOW_STAINED_GLASS_PANE)
                    && itemName.contains("Cancel") && playerWager != null) {

                chessGame.removeRequestWager(playerWager);
                chessGame.updateRequestWagerInventories();
                return;
            }

            // Accept wager
            if(itemType.equals(Material.GREEN_STAINED_GLASS_PANE)
                    && itemName.contains("Accept Wager") && playerWager != null) {

               boolean didCreateWager = chessGame.requestWagerToWager(playerWager, player);

                if(!didCreateWager) return;

                chessGame.updateRequestWagerInventories();
                return;
            }

            // Switch wager color Only if they have not created a wager yet
            if(event.getRawSlot() == 25 && playerWager == null) {
                chessWagerViewInventory.toggleWagerColor();
                chessWagerViewInventory.display(false);
                return;
            }

            if(event.getRawSlot() % 9 < 5 && playerWager == null) {
                String playerName = null;
                if(itemType.equals(Material.PLAYER_HEAD)) {
                    playerName = itemName;
                } else if((itemType.equals(Material.BLACK_WOOL) || itemType.equals(Material.WHITE_WOOL)) && event.getRawSlot() % 9 > 0) {
                    playerName = player.getOpenInventory().getItem(event.getRawSlot() - 1).getItemMeta().getDisplayName();
                    playerName = ChatColor.stripColor(playerName);
                }

                if(playerName == null) return;

                Player clickedPlayer = Bukkit.getPlayer(playerName);

                if(clickedPlayer == null) return;

                RequestWager requestWager = chessGame.getRequestWagerByPlayer(clickedPlayer);

                if(requestWager == null) {
                    player.sendMessage("Could not find request wager! This should never happen. Please contact plugin authors.");
                    return;
                }

                // If the player re-selects a wager, deselect it
                if(chessWagerViewInventory.getSelectedWager() != null) {
                    if(chessWagerViewInventory.getSelectedWager().equals(requestWager)) {
                        chessWagerViewInventory.setSelectedWager(null);
                        chessWagerViewInventory.display(false);
                        return;
                    }
                }

                chessWagerViewInventory.setSelectedWager(requestWager);
                chessWagerViewInventory.display(false);
            }
        	return;
        }
    }

}
