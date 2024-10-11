package ksu.minecraft.prison;

import ksu.minecraft.prison.commands.MinesCommand;
import ksu.minecraft.prison.commands.SellAllCommand;
import ksu.minecraft.prison.commands.SellCommand;
import ksu.minecraft.prison.listeners.EventListener;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.managers.MineManager;
import ksu.minecraft.prison.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Prison extends JavaPlugin {

    private LuckPerms luckPerms;
    private Economy economy;
    private EconomyManager economyManager;
    private RankManager rankManager;
    private Menus menus;
    private MineManager mineManager;
    private File minesConfigFile;
    private FileConfiguration minesConfig;

    @Override
    public void onEnable() {
        getLogger().info("Prison plugin has been enabled!");
        saveDefaultConfig();
        createMinesConfig();

        luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this, luckPerms);
        menus = new Menus(this);
        mineManager = new MineManager(this);

        this.getCommand("sellall").setExecutor(new SellAllCommand(this, economyManager));
        this.getCommand("sell").setExecutor(new SellCommand(this, economyManager));
        this.getCommand("mines").setExecutor(new MinesCommand(this));

        getServer().getPluginManager().registerEvents(new ksu.minecraft.prison.listeners.SellMenuListener(this, economyManager), this);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getScheduler().runTaskTimer(this, () -> mineManager.monitorMines(), 0L, 20L * 60); // Every minute
    }

    @Override
    public void onDisable() {
        getLogger().info("Prison plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("prison")) {
                if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                    menus.showHelpMenu(player);
                    return true;
                } else if (args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
                    menus.openPrisonRanksMenu(player);
                    return true;
                } else {
                    menus.openPrisonMenu(player);
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

    public Menus getMenus() {
        return menus;
    }

    public FileConfiguration getMinesConfig() {
        return minesConfig;
    }

    private void createMinesConfig() {
        minesConfigFile = new File(getDataFolder(), "mines.yml");

        if (!minesConfigFile.exists()) {
            getLogger().info("mines.yml not found, generating blank template...");

            try {
                if (minesConfigFile.createNewFile()) {
                    generateBlankMinesTemplate();
                }
            } catch (IOException e) {
                getLogger().severe("Could not create mines.yml file!");
            }
        } else {
            minesConfig = YamlConfiguration.loadConfiguration(minesConfigFile);
        }
    }

    // Generation for a blank mines.yml template
    //TODO Move to a resources files
    private void generateBlankMinesTemplate() {
        try (FileWriter writer = new FileWriter(minesConfigFile)) {
            writer.write("# Mines Configuration Template\n");
            writer.write("# Add your mines below in the following format:\n\n");

            writer.write("# mines:\n");
            writer.write("#   MineName:\n");
            writer.write("#     resetDelay: <number>\n");
            writer.write("#     surface: <material>\n");
            writer.write("#     maxZ: <number>\n");
            writer.write("#     maxY: <number>\n");
            writer.write("#     maxX: <number>\n");
            writer.write("#     fillMode: <true/false>\n");
            writer.write("#     world: <world_name>\n");
            writer.write("#     minY: <number>\n");
            writer.write("#     resetWarnings: []\n");
            writer.write("#     minX: <number>\n");
            writer.write("#     composition:\n");
            writer.write("#       <material>: <percentage>\n");
            writer.write("#     name: <mine_name>\n");
            writer.write("#     resetClock: <number>\n");
            writer.write("#     minZ: <number>\n");
            writer.write("#     isSilent: <true/false>\n\n");

            writer.write("# Example:\n");
            writer.write("#   D_Ore:\n");
            writer.write("#     resetDelay: 0\n");
            writer.write("#     surface: 'STONE'\n");
            writer.write("#     maxZ: -289\n");
            writer.write("#     maxY: 115\n");
            writer.write("#     maxX: 170\n");
            writer.write("#     fillMode: false\n");
            writer.write("#     world: 'world'\n");
            writer.write("#     minY: 85\n");
            writer.write("#     resetWarnings: []\n");
            writer.write("#     minX: 154\n");
            writer.write("#     composition:\n");
            writer.write("#       'STONE': 0.985\n");
            writer.write("#       'COAL_ORE': 0.0075\n");
            writer.write("#       'IRON_ORE': 0.0075\n");
            writer.write("#     name: 'D_Mine'\n");
            writer.write("#     resetClock: 0\n");
            writer.write("#     minZ: -305\n");
            writer.write("#     isSilent: false\n");

        } catch (IOException e) {
            getLogger().severe("Could not write to mines.yml file!");
        }
    }

    public void saveMinesConfig() {
        try {
            minesConfig.save(minesConfigFile);
        } catch (IOException e) {
            getLogger().severe("Could not save mines.yml!");
        }
    }
}
