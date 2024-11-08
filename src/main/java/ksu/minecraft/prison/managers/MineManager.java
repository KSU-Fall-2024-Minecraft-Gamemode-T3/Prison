package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MineManager {

    private final Prison plugin;
    private FileConfiguration minesConfig;
    private File minesFile;
    private final Map<String, Mine> mines = new HashMap<>();
    private boolean minesChanged = false; // Flag to track if mines have been modified

    public MineManager(Prison plugin) {
        this.plugin = plugin;
        initializeMinesConfig();
        loadMines();
    }

    //Initializes the mines configuration file (mines.yml).
     //Loads the configuration or creates an empty one if it does not exist.

    private void initializeMinesConfig() {
        minesFile = new File(plugin.getDataFolder(), "mines.yml");

        if (!minesFile.exists()) {
            try {
                // Create an empty mines.yml file
                minesFile.createNewFile();
                minesConfig = YamlConfiguration.loadConfiguration(minesFile);
                minesConfig.createSection("mines");
                minesConfig.save(minesFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create mines.yml: " + e.getMessage());
            }
        } else {
            minesConfig = YamlConfiguration.loadConfiguration(minesFile);
        }
    }

    //Loads mines from the configuration into memory.

    private void loadMines() {
        ConfigurationSection minesSection = minesConfig.getConfigurationSection("mines");
        if (minesSection != null) {
            for (String mineKey : minesSection.getKeys(false)) {
                ConfigurationSection mineSection = minesSection.getConfigurationSection(mineKey);
                if (mineSection != null) {
                    Mine mine = new Mine(mineKey, mineSection);
                    mines.put(mineKey.toLowerCase(), mine);
                }
            }
        }
    }

    //Saves any changes to mines.yml if there are modifications.

    public void saveMinesConfig() {
        if (minesChanged) {
            try {
                // Update the minesConfig with current mine data
                minesConfig.set("mines", null); // Clear existing data
                ConfigurationSection minesSection = minesConfig.createSection("mines");
                for (Mine mine : mines.values()) {
                    ConfigurationSection mineSection = minesSection.createSection(mine.getName());
                    mine.saveToConfig(mineSection);
                }
                minesConfig.save(minesFile);
                minesChanged = false; // Reset the flag after saving
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save mines.yml: " + e.getMessage());
            }
        }
    }

    //Reloads the mines.yml configuration.

    public void reloadMinesConfig() {
        if (minesFile == null) {
            minesFile = new File(plugin.getDataFolder(), "mines.yml");
        }
        minesConfig = YamlConfiguration.loadConfiguration(minesFile);
        mines.clear();
        loadMines();
        plugin.getLogger().info("Mines configuration reloaded.");
    }


     //Checks if a mine with the given name exists.

    public boolean mineExists(String mineName) {
        return mines.containsKey(mineName.toLowerCase());
    }


     //Adds a new mine to the manager.

    public void addMine(Mine mine) {
        mines.put(mine.getName().toLowerCase(), mine);
        minesChanged = true; // Mark as changed
        saveMinesConfig();   // Save immediately
    }


     //Resets the specified mine by clearing it and repopulating it with specified materials.

    public boolean resetMineCommand(String mineName, Player player) {
        Mine mine = mines.get(mineName.toLowerCase());
        if (mine == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Mine '" + mineName + "' does not exist.");
            }
            return false;
        }

        mine.reset();

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Mine '" + mineName + "' has been successfully reset.");
        }
        return true;
    }


     //Lists all mine names.

    public List<String> listMineNames() {
        return new ArrayList<>(mines.keySet());
    }


     //Monitors each mine to check if it needs resetting.
     // Resets a mine if it falls below a certain resource threshold or time delay.

    public void monitorMines() {
        for (Mine mine : mines.values()) {
            if (mine.shouldReset()) {
                if (!mine.isSilent()) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Mine '" + mine.getName() + "' has been reset automatically.");
                }
                mine.reset();
            }
        }
    }
}
