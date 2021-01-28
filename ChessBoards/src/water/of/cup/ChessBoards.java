package water.of.cup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import water.of.cup.chessBoard.ChessBoardManager;
import water.of.cup.chessBoard.ChessGame;
import water.of.cup.chessBoard.ChessUtils;
import water.of.cup.commands.ChessBoardCommands;
import water.of.cup.data.MySQLDataStore;
import water.of.cup.inventories.ChessCreateGameInventory;
import water.of.cup.listeners.*;
import water.of.cup.metrics.Metrics;

public class ChessBoards extends JavaPlugin {
	
	private static NamespacedKey key;
	private static ChessBoards instance;
	private static ChessBoardManager chessBoardManager = new ChessBoardManager();
	private static ImageManager imageManager = new ImageManager();
	private HashMap<Player, ChessCreateGameInventory> createGameManager = new HashMap<>();
	List<Integer> mapIDsNotToRender = new ArrayList<>();
	private File configFile;
	private FileConfiguration config;
	private static Economy economy = null;
	private MySQLDataStore dataStore;

	@Override
	public void onEnable() {
		instance = this;
		
		key = new NamespacedKey(this, "chess_game_item");

		loadConfig();
		chessBoardManager.loadGames();
		imageManager.loadImages();
		
		getCommand("chessboards").setExecutor(new ChessBoardCommands());
		registerListeners(new ItemFrameInteract(), new BoardInteract(), new BlockPlace(), new InventoryClose(), new InventoryClick(), new HangingBreakByEntity(), new EntityDamageByEntity(), new HangingBreak(), new ChessPlayerJoin());

		if(config.getBoolean("settings.chessboard.recipe.enabled"))
			addChessBoardRecipe();

		if(config.getBoolean("settings.database.enabled")) {
			this.dataStore = new MySQLDataStore();;
			this.dataStore.initialize();

			for(Player player : Bukkit.getOnlinePlayers()) {
				this.dataStore.addChessPlayer(player);
			}
		}

		File folder = new File(getDataFolder() + "/saved_games");
		File[] listOfFiles = folder.listFiles();
		
		boolean hasEconomy = setupEconomy();
		if (!hasEconomy) {
			Bukkit.getLogger().info("Server must have Vault in order to place wagers on chess games.");
		}

		for (File file : listOfFiles) {
			if (file.isFile()) {
				try {
					int gameId = Integer.parseInt(file.getName().split("_")[1].split(Pattern.quote("."))[0]);
					BufferedReader br = new BufferedReader(new FileReader(file));
					String encodedData = br.readLine();

					ItemStack chessBoardItem = new ItemStack(Material.FILLED_MAP, 1);
					MapMeta mapMeta = (MapMeta) chessBoardItem.getItemMeta();
					MapView mapView = Bukkit.getMap(gameId);
					mapMeta.setMapView(mapView);
					chessBoardItem.setItemMeta(mapMeta);

					ChessGame newChessGame = new ChessGame(chessBoardItem, encodedData, gameId);
					newChessGame.renderBoardForPlayers();

					chessBoardManager.addGame(newChessGame);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Add bStats
		Metrics metrics = new Metrics(this, 10153);
		Bukkit.getLogger().info("[ChessBoards] bStats: " + metrics.isEnabled() + " plugin ver: " + getDescription().getVersion());

		metrics.addCustomChart(new Metrics.SimplePie("plugin_version", () -> getDescription().getVersion()));
	}
	
	

	@Override
	public void onDisable() {
		for(ChessGame chessGame : chessBoardManager.getGames()) {
			chessGame.storeGame();
		}
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

		// Perms: chessboard.destroy chessboard.command
		defaultConfig.put("settings.chessboard.permissions", true);

        defaultConfig.put("settings.database.host", "localhost");
        defaultConfig.put("settings.database.port", "3306");
        defaultConfig.put("settings.database.database", "chessboards");
        defaultConfig.put("settings.database.username", "root");
        defaultConfig.put("settings.database.password", " ");
        defaultConfig.put("settings.database.enabled", false); // Database disabled by default

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

	public HashMap<Player, ChessCreateGameInventory> getCreateGameManager() {
		return createGameManager;
	}
	
	public Economy getEconomy() {
        return economy;
    }

	public static NamespacedKey getKey() {
		return key;
	}

	public MySQLDataStore getDataStore() {
		return dataStore;
	}
}
