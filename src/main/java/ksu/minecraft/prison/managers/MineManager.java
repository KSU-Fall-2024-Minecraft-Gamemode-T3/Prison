package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    // Initializes the mines configuration file (mines.yml).
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

    // Loads mines from the configuration into memory.
    private void loadMines() {
        ConfigurationSection minesSection = minesConfig.getConfigurationSection("mines");
        if (minesSection != null) {
            for (String mineKey : minesSection.getKeys(false)) {
                ConfigurationSection mineSection = minesSection.getConfigurationSection(mineKey);
                if (mineSection != null) {
                    Mine mine = new Mine(mineKey, mineSection);
                    mines.put(mineKey.toLowerCase(), mine);
                    plugin.getLogger().info("Loaded mine: " + mineKey);
                } else {
                    plugin.getLogger().warning("Mine section for " + mineKey + " is null.");
                }
            }
        } else {
            plugin.getLogger().warning("No mines found in configuration.");
        }
    }

    // Saves any changes to mines.yml if there are modifications.
    public void saveMinesConfig() {
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

    // Reloads the mines.yml configuration.
    public void reloadMinesConfig() {
        if (minesFile == null) {
            minesFile = new File(plugin.getDataFolder(), "mines.yml");
        }
        minesConfig = YamlConfiguration.loadConfiguration(minesFile);
        mines.clear();
        loadMines();
        plugin.getLogger().info("Mines configuration reloaded.");
    }

    // Checks if a mine with the given name exists.
    public boolean mineExists(String mineName) {
        return mines.containsKey(mineName.toLowerCase());
    }

    // Adds a new mine to the manager.
    public void addMine(Mine mine) {
        mines.put(mine.getName().toLowerCase(), mine);
        saveMinesConfig();   // Save immediately
    }

    // Resets the specified mine by clearing it and repopulating it with specified materials.
    public boolean resetMineCommand(String mineName, Player player) {
        Mine mine = mines.get(mineName.toLowerCase());
        if (mine == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Mine '" + mineName + "' does not exist.");
            }
            return false;
        }

        mine.reset();
        saveMinesConfig(); // Save after reset

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Mine '" + mineName + "' has been successfully reset.");
        }
        return true;
    }

    // Lists all mine names.
    public List<String> listMineNames() {
        return new ArrayList<>(mines.keySet());
    }

    public void monitorMines() {
        for (Mine mine : mines.values()) {
            double currentPercentage = mine.getCurrentResourcePercentage();
            double percentageMined = 100.0 - currentPercentage; // Calculate percentage mined
            if (percentageMined >= mine.getResetPercentage()) {
                mine.reset();
                saveMinesConfig(); // Save after reset
            }
        }
    }


    public Mine getMine(String mineName) {
        return mines.get(mineName.toLowerCase());
    }

    public Mine getMineByLocation(@NotNull Location location) {
        for (Mine mine : mines.values()) {
            if (mine.containsLocation(location)) {
                return mine;
            }
        }
        return null;
    }
}
