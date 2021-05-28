package water.of.cup.chessboards.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import water.of.cup.chessboards.utils.ConfigMessageUtils;
import water.of.cup.chessboards.utils.GUIUtils;
import water.of.cup.chessboards.chessBoard.ChessGame;
import water.of.cup.chessboards.chessBoard.RequestWager;

public class ChessWagerViewInventory implements InventoryHolder {

	private Inventory inv;
	private ChessGame chessGame;
	private Player player;
	private RequestWager selectedWager;
	private int page;
	private String createWagerColor;
	private String createWagerAmount;
	private int wagerAmount;
	private static final int WAGER_INCREMENT = 1;
	private static final int WAGER_SHIFT_INCREMENT = 10;
	private static final int WAGER_MAX = 9999;

	public static final String INVENTORY_NAME = "Chess | Wagers";

	public ChessWagerViewInventory(ChessGame chessGame, Player player) {
		inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
		this.chessGame = chessGame;
		this.player = player;
		selectedWager = null;
		page = 0;
		wagerAmount = 0;
		createWagerColor = "WHITE";
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
			if (createWagerColor != null)
				inv.setItem(25,
						GUIUtils.createItemStack(ChatColor.RESET + createWagerColor, Material.valueOf(createWagerColor + "_WOOL")));
			inv.setItem(34, GUIUtils.createItemStack(ChatColor.GREEN + ConfigMessageUtils.MESSAGE_GUI_WAGERAMOUNT.toString() + ChatColor.DARK_GREEN + wagerAmount, Material.GOLD_INGOT));
			inv.setItem(33, GUIUtils.getCustomTextureHead(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==",
					ChatColor.RED + "Decrease", 1));
			inv.setItem(35, GUIUtils.getCustomTextureHead(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJmM2EyZGZjZTBjM2RhYjdlZTEwZGIzODVlNTIyOWYxYTM5NTM0YThiYTI2NDYxNzhlMzdjNGZhOTNiIn19fQ==",
					ChatColor.GREEN + "Increase", 1));

			boolean hasWager = this.chessGame.getRequestWagerByPlayer(player) != null;

			if(!hasWager) {
				inv.setItem(43, GUIUtils.createItemStack(ChatColor.GREEN + "Create Wager", Material.GREEN_STAINED_GLASS_PANE));
			} else {
				inv.setItem(44, GUIUtils.createItemStack(ChatColor.RED + "Cancel Wager", Material.YELLOW_STAINED_GLASS_PANE));
			}

		} else {
			inv.setItem(15, GUIUtils.createGuiPlayerItem(selectedWager.getOwner()));
			inv.setItem(17, playerSkull);
			inv.setItem(16,
					GUIUtils.createItemStack(ChatColor.GREEN + ConfigMessageUtils.MESSAGE_GUI_WAGERAMOUNT.toString() + ChatColor.DARK_GREEN + selectedWager.getAmount(), Material.GOLD_INGOT));
			inv.setItem(24, GUIUtils.createItemStack(ChatColor.RESET + selectedWager.getOwnerColor(),
					Material.valueOf(selectedWager.getOwnerColor() + "_WOOL")));
			inv.setItem(26, GUIUtils.createItemStack(ChatColor.RESET + selectedWager.getOponentColor(),
					Material.valueOf(selectedWager.getOponentColor() + "_WOOL")));
			inv.setItem(34, GUIUtils.createItemStack(ChatColor.GREEN + "Accept Wager", Material.GREEN_STAINED_GLASS_PANE));
		}

		inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));

		this.displayAvailableWagers();
		
		if (openInv)
			player.openInventory(inv);
	}

	private void displayAvailableWagers() {
		int num = 0;
		int pos = 9;
		for(RequestWager requestWager : this.chessGame.getRequestWagers()) {
			if(requestWager.getOwner().equals(this.player)) continue;

			ItemStack playerItem = GUIUtils.createGuiPlayerItem(requestWager.getOwner());
			ItemStack playerWagerColor = GUIUtils.createItemStack(ChatColor.RESET + requestWager.getOwnerColor(), Material.valueOf(requestWager.getOwnerColor() + "_WOOL"));

			// Highlight selected wager
			if(this.selectedWager != null) {
				if(this.selectedWager.equals(requestWager)) {
					GUIUtils.addEnchants(playerItem);
					GUIUtils.addEnchants(playerWagerColor);
				}
			}

			inv.setItem(pos, playerItem);
			inv.setItem(pos + 1, playerWagerColor);

			pos += 9;
			if(pos > 36) pos =  12;

			num++;
			if(num == 8) break;
		}
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	public void incrementWager(boolean shift) {
		this.wagerAmount += shift ? WAGER_SHIFT_INCREMENT : WAGER_INCREMENT;
		if(this.wagerAmount >= WAGER_MAX) this.wagerAmount = WAGER_MAX;
	}

	public void decrementWager(boolean shift) {
		this.wagerAmount -= shift ? WAGER_SHIFT_INCREMENT : WAGER_INCREMENT;
		if(this.wagerAmount <= 0) this.wagerAmount = 0;
	}

	public int getWagerAmount() {
		return this.wagerAmount;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getCreateWagerColor() {
		return this.createWagerColor;
	}

	public RequestWager getSelectedWager() {
		return this.selectedWager;
	}

	public void toggleWagerColor() {
		if(this.createWagerColor.equals("WHITE")) {
			this.createWagerColor = "BLACK";
		} else {
			this.createWagerColor = "WHITE";
		}
	}

	public void setSelectedWager(RequestWager requestWager) {
		this.selectedWager = requestWager;
	}

	public ChessGame getChessGame() {
		return this.chessGame;
	}
}
