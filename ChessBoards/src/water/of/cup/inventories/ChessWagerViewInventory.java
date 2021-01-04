package water.of.cup.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import water.of.cup.Utils.GUIUtils;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.RequestWager;

public class ChessWagerViewInventory implements InventoryHolder {

	private Inventory inv;
	private ChessGame chessGame;
	private Player player;
	private RequestWager selectedWager;
	private int page;
	private String createWagerColor;
	private String createWagerAmount;

	public static final String INVENTORY_NAME = "Chess | Wagers";

	public ChessWagerViewInventory(ChessGame chessGame, Player player) {
		inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
		this.chessGame = chessGame;
		this.player = player;
		selectedWager = null;
		page = 0;
	}

	public void display(boolean openInv) {
		ItemStack whiteTile = GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE);
		ItemStack blackTile = GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE);
		GUIUtils.fillBackground(this.inv, blackTile);
		GUIUtils.fillRect(this.inv, new int[] { 0, 1 }, new int[] { 1, 4 }, whiteTile);
		GUIUtils.fillRect(this.inv, new int[] { 3, 1 }, new int[] { 4, 4 }, whiteTile);
		GUIUtils.fillRect(this.inv, new int[] { 6, 1 }, new int[] { 8, 4 }, whiteTile);

		ItemStack playerSkull = GUIUtils.createGuiPlayerItem(player);
		if (selectedWager == null) {
			inv.setItem(16, playerSkull);
			inv.setItem(25, GUIUtils.createItemStack(createWagerColor, Material.valueOf(createWagerColor + "_WOOL")));
			inv.setItem(34, GUIUtils.createItemStack("Wager Amount: " + createWagerAmount, Material.GOLD_INGOT));
			inv.setItem(33, GUIUtils.getCustomTextureHead(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==",
					"Decrease", 1));
			inv.setItem(35, GUIUtils.getCustomTextureHead(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJmM2EyZGZjZTBjM2RhYjdlZTEwZGIzODVlNTIyOWYxYTM5NTM0YThiYTI2NDYxNzhlMzdjNGZhOTNiIn19fQ==",
					"Increase", 1));
			inv.setItem(43, GUIUtils.createItemStack("Create Wager", Material.GREEN_STAINED_GLASS_PANE));
		} else {
			inv.setItem(15, GUIUtils.createGuiPlayerItem(selectedWager.getOwner()));
			inv.setItem(17, playerSkull);
			inv.setItem(16,
					GUIUtils.createItemStack("Wager Amount: " + selectedWager.getAmount(), Material.GOLD_INGOT));
			inv.setItem(24, GUIUtils.createItemStack(selectedWager.getOwnerColor(),
					Material.valueOf(selectedWager.getOwnerColor() + "_WOOL")));
			inv.setItem(26, GUIUtils.createItemStack(selectedWager.getOponentColor(),
					Material.valueOf(selectedWager.getOponentColor() + "_WOOL")));
			inv.setItem(34, GUIUtils.createItemStack("Accept Wager", Material.GREEN_STAINED_GLASS_PANE));
		}

		this.inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));

		if (openInv)
			player.openInventory(inv);
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

}
