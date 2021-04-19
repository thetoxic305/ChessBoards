package water.of.cup.chessboards.inventories;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import water.of.cup.chessboards.ChessBoards;
import water.of.cup.chessboards.utils.GUIUtils;
import water.of.cup.chessboards.chessBoard.ChessGame;
import water.of.cup.chessboards.chessBoard.ChessGameState;

public class ChessInGameInventory implements InventoryHolder {

	private ChessBoards instance = ChessBoards.getInstance();

	private Inventory inv;
	private ChessGame chessGame;
	private boolean whitePlayerReady;
	private boolean blackPlayerReady;
	public static final String INVENTORY_NAME = "Chess | Ingame";

	public ChessInGameInventory(ChessGame chessGame) {
		inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
		this.chessGame = chessGame;
	}

	public void display(Player player, boolean openInv) {
//        Player whitePlayer = this.chessGame.getWhitePlayer();
//        Player blackPlayer = this.chessGame.getBlackPlayer();

//        if(whitePlayer == null || blackPlayer == null) {
//            Bukkit.getLogger().warning("[ChessBoards] Error displaying in game inventory, players not set.");
//            return;
//        }

		GUIUtils.fillBackground(this.inv, GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE));

		GUIUtils.fillRect(this.inv, new int[] { 1, 1 }, new int[] { 3, 4 },
				GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

		GUIUtils.fillRect(this.inv, new int[] { 5, 1 }, new int[] { 7, 4 },
				GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

		ArrayList<String> buttons = new ArrayList<String>();
		if (chessGame.hasPlayer(player)) {
			buttons.add("forfeit");
		} else if (instance.getEconomy() != null){
			buttons.add("wagers");
		}
		int c = 0;
		for (String button : buttons) {
			int place = 24 + c * 9;
			switch (button) {
			case "forfeit":
				this.renderForfeitButton(place);
				break;
			case "wagers":
				this.renderWagerButton(place);
				break;
			}
			c++;
		}
		
		//render game data and players only if a game has been created
		if (chessGame.getGameState() == ChessGameState.CONFIRM_GAME
				|| chessGame.getGameState() == ChessGameState.INGAME) {
			this.renderPlayerReady(true, 10);
			this.renderPlayerReady(false, 12);

			GUIUtils.renderGameData(this.inv, this.chessGame, 11, false);
		}
		
		if (openInv)
			player.openInventory(inv);
	}

	public void togglePlayerReady(Player player) {
		boolean isWhite = player.equals(this.chessGame.getWhitePlayer());
		if (isWhite) {
			whitePlayerReady = !whitePlayerReady;
		} else {
			blackPlayerReady = !blackPlayerReady;
		}

		this.refresh();

		if (whitePlayerReady && blackPlayerReady) {
			this.chessGame.setGameState(ChessGameState.INGAME);
			this.chessGame.startClocks();
			this.chessGame.clearWagerViewInventories();

			Player otherPlayer = player.equals(this.chessGame.getBlackPlayer()) ? this.chessGame.getWhitePlayer()
					: this.chessGame.getBlackPlayer();
			player.closeInventory();
			otherPlayer.closeInventory();

		}
	}

	private void refresh() {
		this.display(this.chessGame.getWhitePlayer(), false);
		this.display(this.chessGame.getBlackPlayer(), false);
	}

	private void renderForfeitButton(int slot) {
		ItemStack item = GUIUtils.createItemStack(ChatColor.RED + "Forfeit", Material.RED_STAINED_GLASS_PANE);
		this.inv.setItem(slot, item);
	}

	private void renderWagerButton(int slot) {
		ItemStack item = GUIUtils.createItemStack(ChatColor.BLUE + "Wagers", Material.BOOK);
		this.inv.setItem(slot, item);
	}

	private void renderPlayerReady(boolean isWhite, int startPos) {
		Player player = isWhite ? this.chessGame.getWhitePlayer() : this.chessGame.getBlackPlayer();
		boolean playerReady = isWhite ? whitePlayerReady : blackPlayerReady;

		Material woolColor = isWhite ? Material.WHITE_WOOL : Material.BLACK_WOOL;
		String colorString = isWhite ? ChatColor.WHITE + "WHITE" : ChatColor.BLACK + "BLACK";

		ItemStack playerHead = GUIUtils.createGuiPlayerItem(player);
		ItemStack playerColor = GUIUtils.createItemStack(colorString, woolColor);
		ItemStack readyButton = this.getButtonStack(playerReady);

		this.inv.setItem(startPos, playerHead);
		this.inv.setItem(startPos + 9, playerColor);
		this.inv.setItem(startPos + 27, readyButton);

	}

	private ItemStack getButtonStack(boolean bool) {
		Material mat = bool ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
		String str = bool ? ChatColor.GREEN + "READY" : ChatColor.RED + "NOT READY";
		return GUIUtils.createItemStack(str, mat);
	}

	public ChessGame getChessGame() {
		return this.chessGame;
	}

	@Override
	public Inventory getInventory() {
		return this.inv;
	}
}
