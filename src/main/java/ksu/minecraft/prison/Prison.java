package ksu.minecraft.prison;

import ksu.minecraft.prison.commands.CellsCommand;
import ksu.minecraft.prison.commands.MineResetCommand;
import ksu.minecraft.prison.commands.RanksCommand;
import ksu.minecraft.prison.listeners.EventListener;
import ksu.minecraft.prison.listeners.ShopListener;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.managers.MineManager;
import ksu.minecraft.prison.managers.RankManager;
import ksu.minecraft.prison.managers.ShopVillagerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
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
import java.util.Objects;

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

    @Override
    public void onEnable() {
        //Initialize configuration settings from the .yml files
        getLogger().info("Prison plugin has been enabled!");
        this.config = this.loadConfigFile("config.yml");
        this.minesConfig = this.loadConfigFile("mines.yml");

        //Initalize all plugins
        luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this, luckPerms);
        mineManager = new MineManager(this);
        shopVillagerManager = new ShopVillagerManager(this);
        menus = new Menus(this);





        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        this.getCommand("minereset").setExecutor(new MineResetCommand(mineManager));
        this.getCommand("cells").setExecutor(new CellsCommand(this));
        this.getCommand("ranks").setExecutor(new RanksCommand(this, rankManager));


        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ksu.minecraft.prison.listeners.SellMenuListener(this, economyManager), this);

        //This for loop will attempt to remove all the shop villagers before populating the world with them
        //otherwise multiple villagers will spawn on top of each other.
        for(Entity entity : Objects.requireNonNull(Bukkit.getWorld("world")).getEntities()){
            if(entity instanceof  Villager){
                entity.remove();
            }
        }
        shopVillagerManager.spawnShopVillagers(); // Spawn shop villagers


        //Mine timer
        getServer().getScheduler().runTaskTimer(this, () -> mineManager.monitorMines(), 0L, 20L * 60); // Every minute
    }

    @Override
    public void onDisable() {
        getLogger().info("Prison plugin has been disabled!");
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

        Component helpMessage = mm.deserialize("""
            <green><bold>Prison Plugin Help:</bold></green>
            <hover:show_text:'<yellow>Open the main prison menu'><click:run_command:/prison>/prison</click></hover> - Main prison menu
            <hover:show_text:'<yellow>View available ranks and costs'><click:run_command:/ranks>/ranks</click></hover> - Show rank progression and costs
            <hover:show_text:'<yellow>Rank up if you can afford it'><click:run_command:/rankup>/rankup</click></hover> - Rank up
            <hover:show_text:'<yellow>View and rent a cell'><click:run_command:/cells>/cells</click></hover> - Rent a cell in the prison
            <hover:show_text:'<yellow>List and manage available mines (admin only)'><click:run_command:/mines>/mines</click></hover> - Manage mines
            <hover:show_text:'<yellow>Manually reset a specific mine (admin only)'><click:run_command:/minereset <minename>>/minereset <minename></click></hover> - Reset specific mine
        """);

        player.sendMessage(helpMessage);
    }

    private void openPrisonRanksMenu(Player player) {
        Inventory ranksMenu = Bukkit.createInventory(null, 27, Component.text("Ranks Menu"));
        // Populate the ranks menu with rank information (example items here)
        player.openInventory(ranksMenu);
    }

    private void openPrisonMenu(Player player) {
        //With the compass item implemented, this method will open that instead
        /*
        Inventory prisonMenu = Bukkit.createInventory(null, 27, Component.text("Prison Menu"));
        // Populate the prison menu with items (example items here)
        player.openInventory(prisonMenu);
         */

        //opens compass prison menu for player
        this.getMenus().openPrisonMenu(player);
    }

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }
}
