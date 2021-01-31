package water.of.cup.chessboards.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import water.of.cup.chessboards.Utils.GUIUtils;
import water.of.cup.chessboards.chessBoard.ChessGame;

public class ChessWaitingPlayerInventory implements InventoryHolder {

    private Inventory inv;
    private ChessGame chessGame;
    public static final String INVENTORY_NAME = "Chess | Waiting For Player";

    public ChessWaitingPlayerInventory(ChessGame chessGame) {
        inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
        this.chessGame = chessGame;
    }

    public void display(Player player, boolean openInv) {
        ItemStack whiteTile = GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE);
        ItemStack blackTile = GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE);
        GUIUtils.fillBackground(this.inv, blackTile);
        GUIUtils.fillRect(this.inv, new int[]{1, 1}, new int[]{3, 4}, whiteTile);
        GUIUtils.fillRect(this.inv, new int[]{5, 1}, new int[]{7, 4}, whiteTile);

        GUIUtils.renderGameData(this.inv, this.chessGame, 11, true);
        this.renderQueue();

        this.inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));
        if(openInv) player.openInventory(inv);
    }

    private void renderQueue() {
        int index = 0;
        for(Player player : this.chessGame.getPlayerQueue()) {
            ItemStack playerHead = GUIUtils.createGuiPlayerItem(player);
            ItemStack acceptButton = GUIUtils.createItemStack(ChatColor.GREEN + "Accept", Material.GREEN_STAINED_GLASS_PANE);
            ItemStack declineButton = GUIUtils.createItemStack(ChatColor.RED + "Decline", Material.RED_STAINED_GLASS_PANE);

            this.inv.setItem(14 + index, playerHead);
            this.inv.setItem(32 + index, acceptButton);
            this.inv.setItem(41 + index, declineButton);

            index++;
        }

    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }
}
