package water.of.cup.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChessCreateGameInventory implements InventoryHolder {

    private Inventory inv;
    private boolean ranked;
    private int gameTime = 60*60;
    private int wager;
    public static final String INVENTORY_NAME = "Chess | Create Game";

    public ChessCreateGameInventory() {
        inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
    }

    public void displayCreateGame(Player player, boolean openInv) {
        this.fillBackground(this.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE));

        this.fillRect(new int[]{1, 1}, new int[]{7, 3}, this.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

        this.createRankedToggle();
        this.createGameTimeToggle();
        this.createWagerToggle();

        for(int i = 0; i < 2; i++) {
            this.inv.setItem(47 + i, this.createItemStack(ChatColor.YELLOW + "Create Casual Game (disabled)", Material.YELLOW_STAINED_GLASS_PANE));
        }

        String rankGameString = ranked ? "Ranked" : "Unranked";
        for(int i = 0; i < 2; i++) {
            this.inv.setItem(51 + i, this.createItemStack(ChatColor.GREEN + "Create " + rankGameString + " Game", Material.GREEN_STAINED_GLASS_PANE));
        }

        this.inv.setItem(8, this.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));

        if(openInv) player.openInventory(inv);
    }

    private void fillBackground(ItemStack itemStack) {
        for(int i = 0; i < 54; i++) {
            this.inv.setItem(i, itemStack);
        }
    }

    private void fillRect(int[] upper, int[] lower, ItemStack itemStack) {
        int index = 0;
        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 9; x++) {
                if(y >= upper[1] && x >= upper[0] && y <= lower[1] && x <= lower[0]) {
                    this.inv.setItem(index, itemStack);
                }
                index++;
            }
        }
    }

    private ItemStack createItemStack(String displayName, Material material) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void createRankedToggle() {
        ItemStack button = this.createItemStack(ChatColor.RED + "Ranked", Material.RED_STAINED_GLASS_PANE);
        if(ranked) {
            button = this.createItemStack(ChatColor.GREEN + "Ranked", Material.GREEN_STAINED_GLASS_PANE);
        }

        this.inv.setItem(19, this.createItemStack(ChatColor.GREEN + "Ranked", Material.EXPERIENCE_BOTTLE));
        this.inv.setItem(28, button);
    }

    private void createGameTimeToggle() {
        ItemStack increment = this.createItemStack(ChatColor.GREEN + "/\\", Material.SKELETON_SKULL);
        ItemStack decrement = this.createItemStack(ChatColor.GREEN + "\\/", Material.SKELETON_SKULL);

        String gameTimeString = this.gameTime / (60*60) + "h" + (this.gameTime % (60*60)) / 60 + "m";

        ItemStack item = this.createItemStack(ChatColor.GREEN + "Game Time: " + ChatColor.DARK_GREEN + gameTimeString, Material.CLOCK);

        this.inv.setItem(12, increment);
        this.inv.setItem(21, item);
        this.inv.setItem(30, decrement);
    }

    private void createWagerToggle() {
        ItemStack increment = this.createItemStack(ChatColor.GREEN + "/\\", Material.SKELETON_SKULL);
        ItemStack decrement = this.createItemStack(ChatColor.GREEN + "\\/", Material.SKELETON_SKULL);

        ItemStack item = this.createItemStack(ChatColor.GREEN + "Wager Amount: $" + ChatColor.DARK_GREEN + this.wager, Material.GOLD_INGOT);

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

    public Inventory getInventory() {
        return this.inv;
    }
}
