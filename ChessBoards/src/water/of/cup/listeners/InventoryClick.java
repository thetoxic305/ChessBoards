package water.of.cup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.mojang.datafixers.FunctionType.Instance;

import water.of.cup.ChessBoards;
import water.of.cup.Utils.GUIUtils;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessGameState;
import water.of.cup.chessBoard.Wager;
import water.of.cup.inventories.ChessInGameInventory;
import water.of.cup.inventories.ChessCreateGameInventory;
import water.of.cup.inventories.ChessJoinGameInventory;
import water.of.cup.inventories.ChessWagerViewInventory;
import water.of.cup.inventories.ChessWaitingPlayerInventory;

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

                player.closeInventory();
                
                chessCreateGameInventory.getChessGame().resetBoard(true);
                
                chessCreateGameInventory.getChessGame().setGameState(ChessGameState.WAITING_PLAYER);
                chessCreateGameInventory.getChessGame().setWhitePlayer(player);

                // Sets game settings
                chessCreateGameInventory.getChessGame().setRanked(chessCreateGameInventory.isRanked());
                chessCreateGameInventory.getChessGame().setClocks(chessCreateGameInventory.getGameTimeString());
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
            
            if (event.getCurrentItem().getType().equals(Material.BOOK)) {
            	ChessWagerViewInventory chessWagerViewInventory = new ChessWagerViewInventory(chessGame, player);
            	chessWagerViewInventory.display(true);
            	chessGame.addWagerViewInventory(chessWagerViewInventory);
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


            return;
        }
		if (event.getView().getTitle().contains(ChessWagerViewInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {
        	event.setCancelled(true);
        	
        	
        	
        }
    }

}
