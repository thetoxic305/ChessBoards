package water.of.cup.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import water.of.cup.chessBoard.ChessGame;

public class GUIUtils {

    public static void fillBackground(Inventory inventory, ItemStack itemStack) {
        for(int i = 0; i < 54; i++) {
            inventory.setItem(i, itemStack);
        }
    }

    public static void fillRect(Inventory inventory, int[] upper, int[] lower, ItemStack itemStack) {
        int index = 0;
        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 9; x++) {
                if(y >= upper[1] && x >= upper[0] && y <= lower[1] && x <= lower[0]) {
                    inventory.setItem(index, itemStack);
                }
                index++;
            }
        }
    }
    
    public static ItemStack getCustomTextureHead(String value, String name, int count) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, count, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), "");
		profile.getProperties().put("textures", new Property("textures", value));
		Field profileField = null;
		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

		meta.setDisplayName(" ");
		meta.setDisplayName(name);
		head.setItemMeta(meta);
		return head;
	}

    public static ItemStack createItemStack(String displayName, Material material) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack createItemStack(String displayName, Material material, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createGuiPlayerItem(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + player.getName());

        // TODO: Add lore containing player's stats

        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwningPlayer(player);
        item.setItemMeta(skullMeta);
        return item;
    }

    public static void renderGameData(Inventory inventory, ChessGame chessGame, int startPos, boolean playerHeadEnabled) {
        if(chessGame.getWhitePlayer() == null) return;

        ItemStack playerHead = GUIUtils.createGuiPlayerItem(chessGame.getWhitePlayer());

        String rankedString = chessGame.isRanked() ? ChatColor.RED + "Ranked" : ChatColor.GREEN + "Unranked";
        ItemStack ranked =  GUIUtils.createItemStack(rankedString, Material.EXPERIENCE_BOTTLE);

        String gameTimeString = chessGame.getGameTimeString();
        int minutes = new Integer(gameTimeString.substring(0, gameTimeString.indexOf(" ")));
        ArrayList<String> timeLore = new ArrayList<String>();
        
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
        
        
        ItemStack gameTimeItem = GUIUtils.createItemStack(ChatColor.GREEN + "Game Time: " + ChatColor.DARK_GREEN + gameTimeString, Material.CLOCK, timeLore);

//		int wager = chessGame.getWager();
        int wagerNum = 0;
        ItemStack wager = GUIUtils.createItemStack(ChatColor.GREEN + "Wager Amount: $" + ChatColor.DARK_GREEN + "" + wagerNum, Material.GOLD_INGOT);

        if(playerHeadEnabled) {
            inventory.setItem(startPos, playerHead);
            startPos += 9;
        }

        inventory.setItem(startPos, ranked);
        inventory.setItem(startPos + 9, gameTimeItem);
        inventory.setItem(startPos + 18, wager);
    }
}
