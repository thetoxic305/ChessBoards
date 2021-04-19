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

public class ChessJoinGameInventory implements InventoryHolder {

    private Inventory inv;
    private ChessGame chessGame;
    private Player player;
    public static final String INVENTORY_NAME = "Chess | Join Game";

    public ChessJoinGameInventory(ChessGame chessGame, Player player) {
        inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
        this.chessGame = chessGame;
        this.player = player;
    }

    public void display(Player player, boolean openInv, boolean hasAccepted) {
        ItemStack whiteTile = GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE);
        ItemStack blackTile = GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE);
        GUIUtils.fillBackground(this.inv, blackTile);
        GUIUtils.fillRect(this.inv, new int[]{1, 1}, new int[]{3, 4}, whiteTile);
        GUIUtils.fillRect(this.inv, new int[]{5, 1}, new int[]{7, 4}, whiteTile);

        GUIUtils.renderGameData(this.inv, this.chessGame, 11, true);

        if(!hasAccepted) {
            this.renderAccept();
        } else {
            this.renderHasAccepted();
        }

        this.inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));
        if(openInv) player.openInventory(inv);
    }

    private void renderAccept() {
        ItemStack playerHead = GUIUtils.createGuiPlayerItem(this.player);
        ItemStack acceptButton = GUIUtils.createItemStack(ChatColor.GREEN + "Join Game", Material.GREEN_STAINED_GLASS_PANE);
        ItemStack declineButton = GUIUtils.createItemStack(ChatColor.RED + "Decline", Material.RED_STAINED_GLASS_PANE);

        this.inv.setItem(15, playerHead);
        this.inv.setItem(33, acceptButton);
        this.inv.setItem(42, declineButton);
    }

    private void renderHasAccepted() {
        ItemStack fillTile = GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE);
        ItemStack waiting = GUIUtils.createItemStack(ChatColor.GREEN + "Waiting for game creator...", Material.CLOCK);

        this.inv.setItem(33, waiting);
        this.inv.setItem(42, fillTile);
    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }
}
