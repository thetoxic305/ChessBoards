package water.of.cup.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import water.of.cup.Utils.GUIUtils;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessGameState;

public class ChessConfirmGameInventory implements InventoryHolder {

    private Inventory inv;
    private ChessGame chessGame;
    private boolean whitePlayerReady;
    private boolean blackPlayerReady;
    public static final String INVENTORY_NAME = "Chess | Confirm Game";

    public ChessConfirmGameInventory(ChessGame chessGame) {
        inv = Bukkit.createInventory(this, 54, INVENTORY_NAME);
        this.chessGame = chessGame;
    }

    public void display(Player player, boolean openInv) {
        Player otherPlayer = player.equals(this.chessGame.getBlackPlayer()) ? this.chessGame.getWhitePlayer() : this.chessGame.getBlackPlayer();

        GUIUtils.fillBackground(this.inv, GUIUtils.createItemStack(" ", Material.BLACK_STAINED_GLASS_PANE));

        GUIUtils.fillRect(this.inv, new int[] { 1, 1 }, new int[] { 3, 4 },
                GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));

        GUIUtils.fillRect(this.inv, new int[] { 5, 1 }, new int[] { 7, 4 },
                GUIUtils.createItemStack(" ", Material.WHITE_STAINED_GLASS_PANE));


        this.renderForfeitButton();

        this.renderPlayerReady(player, 10);
        this.renderPlayerReady(otherPlayer, 12);

        GUIUtils.renderGameData(this.inv, this.chessGame, 11, false);

        if (openInv)
            player.openInventory(inv);
    }

    public void togglePlayerReady(Player player) {
        boolean isWhite = player.equals(this.chessGame.getWhitePlayer());
        if(isWhite) {
            whitePlayerReady = !whitePlayerReady;
        } else {
            blackPlayerReady = !blackPlayerReady;
        }

        this.refresh();

        if(whitePlayerReady && blackPlayerReady) {
            this.chessGame.setGameState(ChessGameState.INGAME);
            this.chessGame.startClocks();

            Player otherPlayer = player.equals(this.chessGame.getBlackPlayer()) ? this.chessGame.getWhitePlayer() : this.chessGame.getBlackPlayer();
            player.closeInventory();
            otherPlayer.closeInventory();

        }
    }

    private void refresh() {
        this.display(this.chessGame.getWhitePlayer(), false);
        this.display(this.chessGame.getBlackPlayer(), false);
    }

    private void renderForfeitButton() {
        ItemStack item = GUIUtils.createItemStack(ChatColor.RED + "Forfeit.", Material.RED_STAINED_GLASS_PANE);
        this.inv.setItem(24, item);
    }

    private void renderPlayerReady(Player player, int startPos) {
        boolean isWhite = player.equals(this.chessGame.getWhitePlayer());
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

    @Override
    public Inventory getInventory() {
        return this.inv;
    }
}
