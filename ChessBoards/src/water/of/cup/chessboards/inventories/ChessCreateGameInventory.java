package water.of.cup.chessboards.inventories;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.utils.ConfigMessage;
import water.of.cup.chessboards.utils.GUIUtils;
import water.of.cup.chessboards.chessBoard.ChessGame;

public class ChessCreateGameInventory implements InventoryHolder {

	private Inventory inv;
	private boolean ranked = true;
	private int gameTime = 5;
	private String[] gameTimes = new String[] { "1 min", "1 | 1", "2 | 1", "3 min", "3 | 2", "5 min", "10 min",
			"30 min", "60 min"};
	private int wager;
	private ChessGame chessGame;
	public static final String INVENTORY_NAME = "Chess | Create Game";
	private ChessBoards instance = ChessBoards.getInstance();

	public ChessCreateGameInventory(ChessGame chessGame) {
		inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
		this.chessGame = chessGame;
	}

	public void displayCreateGame(Player player, boolean openInv) {
		GUIUtils.fillBackground(this.inv, GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE));

		GUIUtils.fillRect(this.inv, new int[] { 1, 1 }, new int[] { 7, 3 },
				GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

		this.createGameTimeToggle();

		// Only show rank option if database is enabled
		boolean databaseEnabled = instance.getConfig().getBoolean("settings.database.enabled");
		if(databaseEnabled)
			this.createRankedToggle();

		// Only create if economy is enabled
		if(instance.getEconomy() != null)
			this.createWagerToggle();

		// Create casual game disabled
//		for (int i = 0; i < 2; i++) {
//			this.inv.setItem(47 + i, GUIUtils.createItemStack(ChatColor.YELLOW + "Create Casual Game (disabled)",
//					Material.YELLOW_STAINED_GLASS_PANE));
//		}

		String rankGameString = ranked ? "Ranked" : "Unranked";
		String fullGameString = ChatColor.GREEN + "Create Game";

		if(databaseEnabled) {
			fullGameString = ChatColor.GREEN + "Create " + rankGameString + " Game";
		}

		for (int i = 0; i < 2; i++) {
			this.inv.setItem(51 + i, GUIUtils.createItemStack(fullGameString, Material.LIME_STAINED_GLASS_PANE));
		}

		this.inv.setItem(8, GUIUtils.createItemStack(ChatColor.RED + "EXIT", Material.BARRIER));

		if (openInv)
			player.openInventory(inv);
	}

	private void createRankedToggle() {
		String name = ChatColor.RED + "Unranked";
		ItemStack button = GUIUtils.createItemStack(name, Material.RED_STAINED_GLASS_PANE);

		if (ranked) {
			name = ChatColor.GREEN + "Ranked";
			button = GUIUtils.createItemStack(name, Material.GREEN_STAINED_GLASS_PANE);
		}

		this.inv.setItem(21, GUIUtils.createItemStack(name, Material.EXPERIENCE_BOTTLE));
		this.inv.setItem(30, button);
	}

	private void createGameTimeToggle() {
		ItemStack increment = GUIUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThmZTI1MWE0MGU0MTY3ZDM1ZDA4MWMyNzg2OWFjMTUxYWY5NmI2YmQxNmRkMjgzNGQ1ZGM3MjM1ZjQ3NzkxZCJ9fX0=",
				ConfigMessage.MESSAGE_GUI_UP.toString(), 1);
		ItemStack decrement = GUIUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3Y2U2ODNkMDg2OGFhNDM3OGFlYjYwY2FhNWVhODA1OTZiY2ZmZGFiNmI1YWYyZDEyNTk1ODM3YTg0ODUzIn19fQ==",
				ConfigMessage.MESSAGE_GUI_DOWN.toString(), 1);

		String gameTimeString = getGameTimeString();
		ArrayList<String> timeLore = new ArrayList<String>();
		int minutes = new Integer(gameTimeString.substring(0, gameTimeString.indexOf(" ")));
		// set the lore to the type of timed Chess Game
		if (minutes <= 1) {
			//Bullet game
			timeLore.add("Bullet");
		} else if (minutes <= 5) {
			//Blitz game
			timeLore.add("Blitz");
		} else {
			//Rapid game
			timeLore.add("Rapid");
		}

		ItemStack item = GUIUtils.createItemStack(
				ConfigMessage.MESSAGE_GUI_GAMETIME.toString() + gameTimeString, Material.CLOCK);
		
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setLore(timeLore);
		item.setItemMeta(itemMeta);
		
		
		this.inv.setItem(10, increment);
		this.inv.setItem(19, item);
		this.inv.setItem(28, decrement);
	}

	private void createWagerToggle() {
		ItemStack increment = GUIUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThmZTI1MWE0MGU0MTY3ZDM1ZDA4MWMyNzg2OWFjMTUxYWY5NmI2YmQxNmRkMjgzNGQ1ZGM3MjM1ZjQ3NzkxZCJ9fX0=",
				ConfigMessage.MESSAGE_GUI_UP.toString(), 1);
		ItemStack decrement = GUIUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3Y2U2ODNkMDg2OGFhNDM3OGFlYjYwY2FhNWVhODA1OTZiY2ZmZGFiNmI1YWYyZDEyNTk1ODM3YTg0ODUzIn19fQ==",
				ConfigMessage.MESSAGE_GUI_DOWN.toString(), 1);

		ItemStack item = GUIUtils.createItemStack(
				ConfigMessage.MESSAGE_GUI_WAGERAMOUNT.toString() + this.wager, Material.GOLD_INGOT);

		this.inv.setItem(14, increment);
		this.inv.setItem(23, item);
		this.inv.setItem(32, decrement);
	}

	public void toggleRanked() {
		this.ranked = !this.ranked;
	}

	public void incrementGameTime() {
		if (gameTime + 1 < gameTimes.length)
			this.gameTime += 1;
	}

	public void decrementGameTime() {
		if (this.gameTime > 0)
			this.gameTime -= 1;
	}

	public void incrementWager() {
		this.wager += 5;
	}

	public void decrementWager() {
		this.wager -= 5;
		if (this.wager < 0)
			this.wager = 0;
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

	public String getGameTimeString() {
		return gameTimes[this.gameTime];
	}
}
