package ksu.minecraft.prison;

import ksu.minecraft.prison.commands.*;
//import ksu.minecraft.prison.commands.MineResetCommand;
import ksu.minecraft.prison.listeners.*;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.managers.MineManager;
import ksu.minecraft.prison.managers.RankManager;
import ksu.minecraft.prison.managers.ShopVillagerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
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

    @Override
    public void onEnable() {
        //Initialize configuration settings from the .yml files
        getLogger().info("Prison plugin has been enabled!");
        this.config = this.loadConfigFile("config.yml");
        this.minesConfig = this.loadConfigFile("mines.yml");

        //Test to see if mines.yml exists

        //Initalize all plugins + managers
        luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this, luckPerms);
        mineManager = new MineManager(this);
        shopVillagerManager = new ShopVillagerManager(this);
        menus = new Menus(this, economyManager);

        //check for luckperms to make sure the server has the plugin
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        //this.getCommand("minereset").setExecutor(new MineResetCommand(mineManager));
        this.getCommand("mine").setExecutor(new MinesCommand(this, mineManager));
        this.getCommand("cells").setExecutor(new CellsCommand(this, economyManager,null));
        getCommand("ranks").setExecutor(new RanksCommand(this, rankManager, luckPerms));
        getCommand("rankup").setExecutor(new RankUpCommand(rankManager));

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new SellMenuListener(this, economyManager), this);
        getServer().getPluginManager().registerEvents(new FishingListener(), this);

        this.world = Bukkit.getWorld("world");
        spawnVillagers();

        getCommand("prison").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("mine").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("ranks").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("rankup").setTabCompleter(new PrisonTabCompleter(this, mineManager));
        getCommand("cells").setTabCompleter(new PrisonTabCompleter(this, mineManager));


        loadCellsConfig();

        // Initialize CellsListener without CellsCommand reference
        CellsListener cellsListener = new CellsListener(this, economyManager);

        // Initialize CellsCommand with CellsListener
        CellsCommand cellsCommand = new CellsCommand(this, economyManager, cellsListener);

        // Set the CellsCommand reference in CellsListener
        cellsListener.setCellsCommand(cellsCommand);

        getCommand("cells").setExecutor(cellsCommand);
        getServer().getPluginManager().registerEvents(cellsListener, this);

        // Reschedule cell expirations on startup
        rescheduleCellExpirations(cellsCommand); // broke
        cellsCommand.scheduleSignUpdates();

        // Initialize MineManager
        mineManager = new MineManager(this);

        // Initialize MinesListener
        minesListener = new MinesListener(this, mineManager); // Add this line

        // Register commands
        getCommand("mine").setExecutor(new MinesCommand(this, mineManager));

        // Register event listeners
        getServer().getPluginManager().registerEvents(minesListener, this); // Use minesListener
        getServer().getPluginManager().registerEvents(new MineBlockListener(mineManager), this);

        // Schedule tasks
        getServer().getScheduler().runTaskTimer(this, () -> {
            mineManager.monitorMines();
            minesListener.updateAllMineSigns(); // Use the stored instance
        }, 0L, 120L); // Every minute

        //getServer().getPluginManager().registerEvents(new MineBlockListener(mineManager), this);

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
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //List of in game commands that players can write and their functions
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("prison")) { //shows basic lists of commands, some maybe only for admins
                if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                    sendHelpMenu(player);
                    return true;
                } else if (args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
                    openPrisonRanksMenu(player);
                    return true;
                } else if(args.length > 0 && args[0].equalsIgnoreCase("ksu")){
                    goKSU(player);
                    return true;
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

    private void sendHelpMenu(Player player) {
        MiniMessage mm = MiniMessage.miniMessage();

        Component helpMessage = mm.deserialize(
                "<green><bold>Prison Plugin Help:</bold></green>\n" +
                        "<hover:show_text:'<yellow>Open the main prison menu'><click:run_command:/prison>/prison</click></hover> - Main prison menu\n" +
                        "<hover:show_text:'<yellow>View available ranks and their costs'><click:run_command:/ranks>/ranks</click></hover> - Shows rank progression and prices\n" +
                        "<hover:show_text:'<yellow>Rank up if you can afford it'><click:run_command:/rankup>/rankup</click></hover> - Rank up to the next rank\n" +
                        "<hover:show_text:'<yellow>View and rent a prison cell'><click:run_command:/cells>/cells</click></hover> - Rent a cell in the prison\n" +
                        "<hover:show_text:'<yellow>List and manage available mines (Admin only)'><click:run_command:/mine>/mine</click></hover> - Manage mines\n" +
                        "<hover:show_text:'<yellow>Manually reset a specific mine (Admin only)'><click:suggest_command:/mine reset >Reset mine</click></hover> - Reset a specific mine"
        );

        player.sendMessage(Component.text("\n")); // Add an empty line before the title
        player.sendMessage(helpMessage);
    }






    private void spawnVillagers(){
        //This for loop will attempt to remove all the shop villagers before populating the world with them
        //otherwise multiple villagers will spawn on top of each other.
        if(world != null){
            for(Entity entity : world.getEntities()){
                //TODO Target only shop villagers
                //This code should only target shop villagers but more keep getting added with each server reset.
                //Either not every villager is assigned the 'is_shop' key properly or more villagers are being called
                //from another command which shouldn't be possible
                if(entity.getPersistentDataContainer().has(this.getNamespacedKey("is_shop"), PersistentDataType.BYTE)){
                    entity.remove();
                }
            }
        }
        shopVillagerManager.spawnShopVillagers(world); // Spawn shop villagers
    }


    private void goKSU(Player player){
        MiniMessage mm = MiniMessage.miniMessage();

        Component ksuMessage = mm.deserialize("<hover:show_text:Check out the CCSE website, that's where we're from!><yellow>G<green>o <yellow>K<green>S<yellow>U<green>!");

        player.sendMessage(ksuMessage);
    }

    private void openPrisonRanksMenu(Player player) {
        Inventory ranksMenu = Bukkit.createInventory(null, 27, Component.text("Ranks Menu"));
        // Populate the ranks menu with rank information (example items here)
        player.openInventory(ranksMenu);
    }

    private void openPrisonMenu(Player player) {


        //opens compass prison menu for player
        this.getMenus().openPrisonMenu(player);
    }

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }

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


    public void loadCellsConfig() {
        cellsFile = new File(getDataFolder(), "cells.yml");
        if (!cellsFile.exists()) {
            cellsFile.getParentFile().mkdirs();
            try {
                // Create a new empty cells.yml file
                cellsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cellsConfig = new YamlConfiguration();
        try {
            cellsConfig.load(cellsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getCellsConfig() {
        if (cellsConfig == null) {
            loadCellsConfig();
        }
        return cellsConfig;
    }

    public void saveCellsConfig() {
        try {
            cellsConfig.save(cellsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
