package water.of.cup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessUtils;
import water.of.cup.commands.ChessBoardCommands;
import water.of.cup.inventories.ChessCreateGameInventory;
import water.of.cup.listeners.*;

public class ChessBoards extends JavaPlugin {

	private static ChessBoards instance;
	private static ChessBoardManager chessBoardManager = new ChessBoardManager();
	private static ImageManager imageManager = new ImageManager();
	private HashMap<Player, ChessCreateGameInventory> createGameManager = new HashMap<>();
	List<Integer> mapIDsNotToRender = new ArrayList<>();
	private File configFile;
	private FileConfiguration config;

	@Override
	public void onEnable() {
		instance = this;

		loadConfig();
		chessBoardManager.loadGames();
		imageManager.loadImages();
		
		getCommand("newChessBoard").setExecutor(new ChessBoardCommands());
		registerListeners(new ItemFrameInteract(), new BoardInteract(), new BlockPlace(), new InventoryClose(), new InventoryClick());

		if(config.getBoolean("settings.chessboard.recipe.enabled"))
			addChessBoardRecipe();
	}

	@Override
	public void onDisable() {
		/* Disable all current async tasks */
		Bukkit.getScheduler().cancelTasks(this);
	}

	private void registerListeners(Listener... listeners) {
		Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
	}

	public static ChessBoards getInstance() {
		return instance;
	}
	
	public void addChessBoardRecipe() {
		ItemStack chessboard = ChessUtils.getChessBoardItem();

		NamespacedKey key = new NamespacedKey(this, "chess_board");
		ShapedRecipe recipe = new ShapedRecipe(key, chessboard);

		ArrayList<String> shapeArr = (ArrayList<String>) config.get("settings.chessboard.recipe.shape");
		recipe.shape(shapeArr.toArray(new String[shapeArr.size()]));

		for(String ingredientKey : config.getConfigurationSection("settings.chessboard.recipe.ingredients").getKeys(false)){
			recipe.setIngredient(ingredientKey.charAt(0), Material.valueOf((String) config.get("settings.chessboard.recipe.ingredients." + ingredientKey)));
		}

		Bukkit.addRecipe(recipe);
	}

	private void loadConfig() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = YamlConfiguration.loadConfiguration(configFile);

		HashMap<String, Object> defaultConfig = new HashMap<>();

		defaultConfig.put("settings.chessboard.permissions", true);

		HashMap<String, String> defaultRecipe = new HashMap<>();
		defaultRecipe.put("I", Material.IRON_INGOT.toString());
		defaultRecipe.put("G", Material.GLASS_PANE.toString());
		defaultRecipe.put("T", Material.GLOWSTONE_DUST.toString());
		defaultRecipe.put("R", Material.REDSTONE.toString());

		defaultConfig.put("settings.chessboard.recipe.enabled", true);
		defaultConfig.put("settings.chessboard.recipe.shape", new ArrayList<String>() {
			{
				add("IGI");
				add("ITI");
				add("IRI");
			}
		});


		if(!config.contains("settings.chessboard.recipe.ingredients")) {
			for (String key : defaultRecipe.keySet()) {
				defaultConfig.put("settings.chessboard.recipe.ingredients." + key, defaultRecipe.get(key));
			}
		}

		for (String key : defaultConfig.keySet()) {
			if(!config.contains(key)) {
				config.set(key, defaultConfig.get(key));
			}
		}

		this.saveConfig();
	}

	@Override
	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}

	public ChessBoardManager getChessBoardManager() {
		return chessBoardManager;
	}
	
	public ImageManager getImageManager() {
		return imageManager;
	}

	public HashMap<Player, ChessCreateGameInventory> getCreateGameManager() {
		return createGameManager;
	}
}
