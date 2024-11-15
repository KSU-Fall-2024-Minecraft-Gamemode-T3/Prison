package ksu.minecraft.prison;

import ksu.minecraft.prison.commands.*;
import ksu.minecraft.prison.listeners.*;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.managers.MineManager;
import ksu.minecraft.prison.managers.RankManager;
import ksu.minecraft.prison.managers.ShopVillagerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Prison extends JavaPlugin {

    private LuckPerms luckPerms;
    private Economy economy;
    private EconomyManager economyManager;
    private RankManager rankManager;
    private MineManager mineManager;
    private ShopVillagerManager shopVillagerManager;
    private File minesConfigFile;
    private FileConfiguration minesConfig;
    private FileConfiguration config;
    private Menus menus;
    public static World world;
    private File cellsFile;
    private FileConfiguration cellsConfig;
    private MinesListener minesListener;

    // Define all shop villager spawn locations
    private List<Location> villagerSpawnLocations;

    @Override
    public void onEnable() {
        // Initialize configuration settings from the .yml files
        getLogger().info("Prison plugin has been enabled!");
        this.config = this.loadConfigFile("config.yml");
        this.minesConfig = this.loadConfigFile("mines.yml");

        // Initialize all managers
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this, luckPerms);
        mineManager = new MineManager(this);
        shopVillagerManager = new ShopVillagerManager(this);
        menus = new Menus(this, economyManager);

        // Check for LuckPerms
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms found and loaded.");
        } else {
            getLogger().severe("LuckPerms not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        this.getCommand("mine").setExecutor(new MinesCommand(this, mineManager));
        this.getCommand("cells").setExecutor(new CellsCommand(this, economyManager, null));
        getCommand("ranks").setExecutor(new RanksCommand(this, rankManager, luckPerms));
        getCommand("rankup").setExecutor(new RankUpCommand(rankManager));
        getCommand("prison").setExecutor(this); // Delegate to onCommand

        // Register tab completers
        getCommand("prison").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("mine").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("ranks").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("rankup").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("cells").setTabCompleter(new PrisonTabCompleter(this, mineManager));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new SellMenuListener(this, economyManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(), this);

        // Initialize world
        this.world = Bukkit.getWorld("world");
        if (this.world == null) {
            getLogger().severe("World 'world' not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize villager spawn locations after the world is loaded
        initializeVillagerSpawnLocations();

        // Spawn villagers
        spawnVillagers();

        // Load and initialize cells configuration
        loadCellsConfig();

        // Initialize CellsListener and CellsCommand
        CellsListener cellsListener = new CellsListener(this, economyManager);
        CellsCommand cellsCommand = new CellsCommand(this, economyManager, cellsListener);
        cellsListener.setCellsCommand(cellsCommand);
        getServer().getPluginManager().registerEvents(cellsListener, this);

        // Reschedule cell expirations on startup
        rescheduleCellExpirations(cellsCommand);
        cellsCommand.scheduleSignUpdates();

        // Initialize MinesListener
        minesListener = new MinesListener(this, mineManager);
        getServer().getPluginManager().registerEvents(minesListener, this);
        getServer().getPluginManager().registerEvents(new MineBlockListener(mineManager), this);

        // Schedule tasks
        getServer().getScheduler().runTaskTimer(this, () -> {
            mineManager.monitorMines();
            minesListener.updateAllMineSigns();
        }, 0L, 120L); // Every minute
    }

    @Override
    public void onDisable() {
        getLogger().info("Prison plugin has been disabled!");
        mineManager.saveMinesConfig();
    }

    public Menus getMenus() {
        return menus;
    }

    private FileConfiguration loadConfigFile(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
            getLogger().info(name + " created.");
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Initializes the list of villager spawn locations after ensuring the world is loaded.
     */
    private void initializeVillagerSpawnLocations() {
        villagerSpawnLocations = List.of(
                new Location(world, 190.5, 117, -274.5), // Shop Villager in D
                new Location(world, 228.5, 117, -318.5), // Shop Villager in C - Note: Dupes Spawn
                new Location(world, 216.5, 117, -213.5), // Shop Villager in B
                new Location(world, 192.5, 117, -238.5), // Shop Villager in A
                new Location(world, 262.5, 108, -236.5), // Shop Villager in K - Note: Dupes Spawn
                new Location(world, 174.5, 104, -243.5), // Shop Villager in S
                new Location(world, 278.5, 117, -282.5)  // Shop Villager in U - Note: Dupes Spawn
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // List of in-game commands that players can execute
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("prison")) { // Handles /prison command
                if (args.length > 0) {
                    switch(args[0].toLowerCase()) {
                        case "help":
                            sendHelpMenu(player);
                            return true;
                        case "ranks":
                            openPrisonRanksMenu(player);
                            return true;
                        case "ksu":
                            goKSU(player);
                            return true;
                        case "resetshops":
                            // Permission Check
                            if (!player.hasPermission("prison.resetshops")) {
                                player.sendMessage(Component.text("<red>You do not have permission to execute this command."));
                                return true;
                            }
                            // Reset Shop Villagers
                            resetShopVillagers(player);
                            return true;
                        default:
                            player.sendMessage(Component.text("<red>Unknown subcommand. Use /prison help for a list of commands."));
                            return true;
                    }
                } else {
                    openPrisonMenu(player);
                    return true;
                }
            } else if (command.getName().equalsIgnoreCase("rankup")) {
                rankManager.rankUp(player);
                return true;
            }
        }
        return false;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * Sends the help menu to the player.
     */
    private void sendHelpMenu(Player player) {
        MiniMessage mm = MiniMessage.miniMessage();

        Component helpMessage = mm.deserialize(
                "<green><bold>Prison Plugin Help:</bold></green>\n" +
                        "<hover:show_text:'<yellow>Open the main prison menu'><click:run_command:/prison>/prison</click></hover> - Main prison menu\n" +
                        "<hover:show_text:'<yellow>View available ranks and their costs'><click:run_command:/ranks>/ranks</click></hover> - Shows rank progression and prices\n" +
                        "<hover:show_text:'<yellow>Rank up if you can afford it'><click:run_command:/rankup>/rankup</click></hover> - Rank up to the next rank\n" +
                        "<hover:show_text:'<yellow>View and rent a prison cell'><click:run_command:/cells>/cells</click></hover> - Rent a cell in the prison\n" +
                        "<hover:show_text:'<yellow>List and manage available mines (Admin only)'><click:run_command:/mine>/mine</click></hover> - Manage mines\n" +
                        "<hover:show_text:'<yellow>Manually reset a specific mine (Admin only)'><click:suggest_command:/mine reset >Reset mine</click></hover> - Reset a specific mine\n" +
                        "<hover:show_text:'<yellow>Reset all shop villagers (Admin only)'><click:run_command:/prison resetshops>/prison resetshops</click></hover> - Reset all shop villagers"
        );

        player.sendMessage(Component.text("\n")); // Add an empty line before the title
        player.sendMessage(helpMessage);
    }

    /**
     * Spawns shop villagers by ensuring their spawn chunks are loaded first.
     */
    private void spawnVillagers(){
        // Load all chunks where shop villagers will spawn
        for(Location location : villagerSpawnLocations){
            World villagerWorld = location.getWorld();
            if(villagerWorld == null){
                getLogger().severe("World is null for location: " + location.toString());
                continue;
            }

            // Calculate chunk coordinates
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;

            // Load the chunk if it's not already loaded
            if(!villagerWorld.isChunkLoaded(chunkX, chunkZ)){
                boolean loaded = villagerWorld.loadChunk(chunkX, chunkZ, true);
                if(loaded){
                    getLogger().info("Loaded chunk at " + location.toString());
                } else {
                    getLogger().severe("Failed to load chunk at " + location.toString());
                }
            } else {
                getLogger().info("Chunk already loaded at " + location.toString());
            }
        }

        // Remove existing shop villagers to prevent duplicates
        for(Entity entity : world.getEntities()){
            if(entity.getPersistentDataContainer().has(this.getNamespacedKey("is_shop"), PersistentDataType.BYTE)){
                entity.remove();
                getLogger().info("Removed an existing shop villager during initial spawn.");
            }
        }

        // Spawn new shop villagers
        shopVillagerManager.spawnShopVillagers(world);
        getLogger().info("Shop villagers have been spawned.");
    }

    /**
     * Resets all shop villagers by removing existing ones and spawning new ones.
     */
    private void resetShopVillagers(Player player){
        // Remove existing shop villagers
        if(world != null){
            int removedCount = 0;
            for(Entity entity : world.getEntities()){
                if(entity instanceof Villager){
                    Villager villager = (Villager) entity;
                    boolean isShop = false;

                    // Check for 'is_shop' key
                    if(villager.getPersistentDataContainer().has(this.getNamespacedKey("is_shop"), PersistentDataType.BYTE)){
                        isShop = true;
                    }

                    // Check for custom name "Shop" if 'is_shop' key is absent
                    if(!isShop && villager.getCustomName() != null && villager.getCustomName().equals(Component.text("Shop"))){
                        isShop = true;
                    }

                    if(isShop){
                        villager.remove();
                        removedCount++;
                        getLogger().info("Removed a shop villager during reset.");
                    }
                }
            }
            getLogger().info("Removed " + removedCount + " existing shop villager(s).");
            player.sendMessage(Component.text("Removed " + removedCount + " existing shop villager(s)."));
        }

        // Load all chunks where shop villagers will spawn
        for(Location location : villagerSpawnLocations){
            World villagerWorld = location.getWorld();
            if(villagerWorld == null){
                getLogger().severe("World is null for location: " + location.toString());
                continue;
            }

            // Calculate chunk coordinates
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;

            // Load the chunk if it's not already loaded
            if(!villagerWorld.isChunkLoaded(chunkX, chunkZ)){
                boolean loaded = villagerWorld.loadChunk(chunkX, chunkZ, true);
                if(loaded){
                    getLogger().info("Loaded chunk at " + location.toString());
                } else {
                    getLogger().severe("Failed to load chunk at " + location.toString());
                }
            } else {
                getLogger().info("Chunk already loaded at " + location.toString());
            }
        }

        // Spawn new shop villagers
        shopVillagerManager.spawnShopVillagers(world);
        getLogger().info("Shop villagers have been reset.");
        player.sendMessage(Component.text("Shop villagers have been reset."));
    }

    /**
     * Sends a "Go KSU!" message to the player with hover text.
     */
    private void goKSU(Player player){
        MiniMessage mm = MiniMessage.miniMessage();

        Component ksuMessage = mm.deserialize("<hover:show_text:'Check out the CCSE website, that's where we're from!'><yellow>G<green>o <yellow>K<green>S<yellow>U<green>!</yellow></green></yellow></green>");

        player.sendMessage(ksuMessage);
    }

    /**
     * Opens the ranks menu for the player.
     */
    private void openPrisonRanksMenu(Player player) {
        Inventory ranksMenu = Bukkit.createInventory(null, 27, "Ranks Menu");
        // Populate the ranks menu with rank information (example items here)
        player.openInventory(ranksMenu);
    }

    /**
     * Opens the main prison menu for the player.
     */
    private void openPrisonMenu(Player player) {
        // Opens prison menu for player
        this.getMenus().openPrisonMenu(player);
    }

    /**
     * Generates a NamespacedKey with the given key.
     */
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }

    /**
     * Reschedules cell expirations based on the configuration.
     */
    private void rescheduleCellExpirations(CellsCommand cellsCommand) {
        FileConfiguration cellsConfig = getCellsConfig();
        ConfigurationSection playerRentals = cellsConfig.getConfigurationSection("playerRentals");
        if (playerRentals == null) return;

        for (String uuidStr : playerRentals.getKeys(false)) {
            UUID playerUUID = UUID.fromString(uuidStr);
            String cell = cellsConfig.getString("playerRentals." + uuidStr + ".cell");
            long expiresAt = cellsConfig.getLong("playerRentals." + uuidStr + ".expiresAt");

            if (cell != null && expiresAt > Instant.now().getEpochSecond()) {
                cellsCommand.scheduleCellExpiration(playerUUID, cell, expiresAt);
            }
        }
    }

    /**
     * Loads the cells configuration file.
     */
    public void loadCellsConfig() {
        cellsFile = new File(getDataFolder(), "cells.yml");
        if (!cellsFile.exists()) {
            cellsFile.getParentFile().mkdirs();
            try {
                // Create a new empty cells.yml file
                cellsFile.createNewFile();
                getLogger().info("cells.yml created.");
            } catch (IOException e) {
                getLogger().severe("Failed to create cells.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }

        cellsConfig = new YamlConfiguration();
        try {
            cellsConfig.load(cellsFile);
            getLogger().info("cells.yml loaded successfully.");
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load cells.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the cells configuration.
     */
    public FileConfiguration getCellsConfig() {
        if (cellsConfig == null) {
            loadCellsConfig();
        }
        return cellsConfig;
    }

    /**
     * Saves the cells configuration to disk.
     */
    public void saveCellsConfig() {
        try {
            cellsConfig.save(cellsFile);
            getLogger().info("cells.yml saved successfully.");
        } catch (IOException e) {
            getLogger().severe("Failed to save cells.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
