package water.of.cup.chessboards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import water.of.cup.chessboards.chessBoard.ChessBoardManager;
import water.of.cup.chessboards.chessBoard.ChessGame;
import water.of.cup.chessboards.chessBoard.ChessUtils;
import water.of.cup.chessboards.commands.ChessBoardCommandsTabCompleter;
import water.of.cup.chessboards.data.DataSource;
import water.of.cup.chessboards.inventories.ChessCreateGameInventory;
import water.of.cup.chessboards.listeners.*;
import water.of.cup.chessboards.commands.ChessBoardCommands;
import water.of.cup.chessboards.metrics.Metrics;
import water.of.cup.chessboards.utils.ConfigMessage;

public class ChessBoards extends JavaPlugin {
	
	private static NamespacedKey key;
	private static ChessBoards instance;
	private static ChessBoardManager chessBoardManager = new ChessBoardManager();
	private static ImageManager imageManager = new ImageManager();
	private HashMap<Player, ChessCreateGameInventory> createGameManager = new HashMap<>();
	private File configFile;
	private FileConfiguration config;
	private static Economy economy = null;
	private DataSource dataStore;

	@Override
	public void onEnable() {
		instance = this;
		
		key = new NamespacedKey(this, "chess_game_item");

		loadConfig();

		boolean loadedImages = imageManager.loadImages();
		if(!loadedImages) {
			Bukkit.getLogger().warning("[ChessBoards] Could not start ChessBoards due to invalid images");
			return;
		}

		Bukkit.getLogger().info("[ChessBoards] Successfully loaded piece images");
		
		getCommand("chessboards").setExecutor(new ChessBoardCommands());
		getCommand("chessboards").setTabCompleter(new ChessBoardCommandsTabCompleter());

		registerListeners(new BoardInteract(), new BlockPlace(), new InventoryClose(), new InventoryClick(), new HangingBreakByEntity(), new EntityDamageByEntity(), new HangingBreak(), new ChessPlayerJoin(), new BlockBreak(), new ChessPlayerQuit());

		if(config.getBoolean("settings.chessboard.recipe.enabled"))
			addChessBoardRecipe();

		if(config.getBoolean("settings.database.enabled")) {
			this.dataStore = new DataSource();;
			this.dataStore.initialize();

			for(Player player : Bukkit.getOnlinePlayers()) {
				this.dataStore.addChessPlayer(player);
			}
		}

		if(config.getBoolean("settings.chessboard.wagers")) {
			boolean hasEconomy = setupEconomy();
			if (!hasEconomy) {
				Bukkit.getLogger().info("Server must have Vault in order to place wagers on chess games.");
			}
		}

		chessBoardManager.loadGames();

		// Add bStats
		Metrics metrics = new Metrics(this, 10153);
		Bukkit.getLogger().info("[ChessBoards] bStats: " + metrics.isEnabled() + " plugin ver: " + getDescription().getVersion());

		metrics.addCustomChart(new Metrics.SimplePie("plugin_version", () -> getDescription().getVersion()));
	}

	@Override
	public void onDisable() {
		boolean databaseEnabled = instance.getConfig().getBoolean("settings.database.enabled");
		if(databaseEnabled && this.dataStore != null)
			this.dataStore.closeConnection();

		chessBoardManager.saveGames();

		/* Disable all current async tasks */
		Bukkit.getScheduler().cancelTasks(this);
	}

	private void registerListeners(Listener... listeners) {
		Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
	}

	public static ChessBoards getInstance() {
		return instance;
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
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

		/* Perms:
		chessboard.interact - interact with chessboard
		chessboard.place - place chessboard
		chessboard.destroy - break chessboard
		chessboard.command - use chessboard command
			- chessboard.command.give - use give command
			- chessboard.command.leaderboard - use leaderboard command
		 */
		defaultConfig.put("settings.chessboard.permissions", true);
		defaultConfig.put("settings.chessboard.wagers", true);
		defaultConfig.put("settings.chessboard.customImages", false); // Default false

		defaultConfig.put("settings.sounds.move", "BLOCK_WOOD_PLACE");
		defaultConfig.put("settings.sounds.check", "BLOCK_WOOD_HIT");
		defaultConfig.put("settings.sounds.enabled", true);

        defaultConfig.put("settings.database.host", "localhost");
        defaultConfig.put("settings.database.port", "3306");
        defaultConfig.put("settings.database.database", "chessboards");
        defaultConfig.put("settings.database.username", "root");
        defaultConfig.put("settings.database.password", " ");
        defaultConfig.put("settings.database.enabled", false); // Database disabled by default

		HashMap<String, String> defaultRecipe = new HashMap<>();
		defaultRecipe.put("B", Material.BLACK_DYE.toString());
		defaultRecipe.put("W", Material.WHITE_DYE.toString());
		defaultRecipe.put("L", Material.LEATHER.toString());
		defaultRecipe.put("Q", Material.QUARTZ.toString());

		defaultConfig.put("settings.chessboard.recipe.enabled", true);
		defaultConfig.put("settings.chessboard.recipe.shape", new ArrayList<String>() {
			{
				add("BW");
				add("QQ");
				add("LL");
			}
		});

		// Load in config messages
		for(ConfigMessage configMessage : ConfigMessage.values()) {
			defaultConfig.put(configMessage.getPath(), configMessage.getDefaultMessage());
		}

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

		File savedGamesDir = new File(getDataFolder(), "saved_games");
		if (!savedGamesDir.exists()) {
			savedGamesDir.mkdir();
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
	
	public Economy getEconomy() {
        return economy;
    }

	public static NamespacedKey getKey() {
		return key;
	}

	public DataSource getDataStore() {
		return dataStore;
	}

	public ChessCreateGameInventory getCreateGameInventory(Player player) {
		return this.createGameManager.get(player);
	}

	public void removeCreateGamePlayer(Player player) {
		this.createGameManager.remove(player);
	}

	public void addCreateGamePlayer(Player player, ChessCreateGameInventory inventory) {
		this.createGameManager.put(player, inventory);
	}

	public boolean playerHasCreateGame(Player player) {
		return this.createGameManager.containsKey(player);
	}

	public Set<Player> getCreateGamePlayers() {
		return this.createGameManager.keySet();
	}
}
