package water.of.cup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import water.of.cup.ChessBoards;
import water.of.cup.inventories.ChessCreateGameInventory;

public class InventoryClick implements Listener {

    private ChessBoards pluginInstance = ChessBoards.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().contains(ChessCreateGameInventory.INVENTORY_NAME)
                && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {

            if(!pluginInstance.getCreateGameManager().containsKey(player)) return;

            event.setCancelled(true);

            ChessCreateGameInventory chessCreateGameInventory = pluginInstance.getCreateGameManager().get(player);

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

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
    }

}
