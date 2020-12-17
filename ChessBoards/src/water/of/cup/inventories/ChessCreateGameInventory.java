package water.of.cup.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import water.of.cup.Utils.GUIUtils;
import water.of.cup.chessBoard.ChessGame;

public class ChessCreateGameInventory implements InventoryHolder {

    private Inventory inv;
    private boolean ranked;
    private int gameTime = 60*60;
    private int wager;
    private ChessGame chessGame;
    public static final String INVENTORY_NAME = "Chess | Create Game";

    public ChessCreateGameInventory(ChessGame chessGame) {
        inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
        this.chessGame = chessGame;
    }

    public void displayCreateGame(Player player, boolean openInv) {
        GUIUtils.fillBackground(this.inv, GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE));

        GUIUtils.fillRect(this.inv, new int[]{1, 1}, new int[]{7, 3}, GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

        this.createRankedToggle();
        this.createGameTimeToggle();
        this.createWagerToggle();

        for(int i = 0; i < 2; i++) {
            this.inv.setItem(47 + i, GUIUtils.createItemStack(ChatColor.YELLOW + "Create Casual Game (disabled)", Material.YELLOW_STAINED_GLASS_PANE));
        }

        String rankGameString = ranked ? "Ranked" : "Unranked";
        for(int i = 0; i < 2; i++) {
            this.inv.setItem(51 + i, GUIUtils.createItemStack(ChatColor.GREEN + "Create " + rankGameString + " Game", Material.LIME_STAINED_GLASS_PANE));
        }

        this.inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));

        if(openInv) player.openInventory(inv);
    }

    private void createRankedToggle() {
        ItemStack button = GUIUtils.createItemStack(ChatColor.RED + "Ranked", Material.RED_STAINED_GLASS_PANE);
        if(ranked) {
            button = GUIUtils.createItemStack(ChatColor.GREEN + "Ranked", Material.GREEN_STAINED_GLASS_PANE);
        }

        this.inv.setItem(19, GUIUtils.createItemStack(ChatColor.GREEN + "Ranked", Material.EXPERIENCE_BOTTLE));
        this.inv.setItem(28, button);
    }

    private void createGameTimeToggle() {
        ItemStack increment = GUIUtils.createItemStack(ChatColor.GREEN + "/\\", Material.SKELETON_SKULL);
        ItemStack decrement = GUIUtils.createItemStack(ChatColor.GREEN + "\\/", Material.SKELETON_SKULL);

        String gameTimeString = this.gameTime / (60*60) + "h" + (this.gameTime % (60*60)) / 60 + "m";

        ItemStack item = GUIUtils.createItemStack(ChatColor.GREEN + "Game Time: " + ChatColor.DARK_GREEN + gameTimeString, Material.CLOCK);

        this.inv.setItem(12, increment);
        this.inv.setItem(21, item);
        this.inv.setItem(30, decrement);
    }

    private void createWagerToggle() {
        ItemStack increment = GUIUtils.createItemStack(ChatColor.GREEN + "/\\", Material.SKELETON_SKULL);
        ItemStack decrement = GUIUtils.createItemStack(ChatColor.GREEN + "\\/", Material.SKELETON_SKULL);

        ItemStack item = GUIUtils.createItemStack(ChatColor.GREEN + "Wager Amount: $" + ChatColor.DARK_GREEN + this.wager, Material.GOLD_INGOT);

        this.inv.setItem(14, increment);
        this.inv.setItem(23, item);
        this.inv.setItem(32, decrement);
    }

    public void toggleRanked() {
        this.ranked = !this.ranked;
    }

    public void incrementGameTime() {
        this.gameTime += 60 * 15;
    }

    public void decrementGameTime() {
        this.gameTime -= 60 * 15;
        if(this.gameTime < 0) this.gameTime = 0;
    }

    public void incrementWager() {
        this.wager += 5;
    }

    public void decrementWager() {
        this.wager -= 5;
        if(this.wager < 0) this.wager = 0;
    }

    public ChessGame getChessGame() {
        return this.chessGame;
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public boolean isRanked() {
        return ranked;
    }

    public int getGameTime() {
        return gameTime;
    }

    public int getWager() {
        return wager;
    }
}
