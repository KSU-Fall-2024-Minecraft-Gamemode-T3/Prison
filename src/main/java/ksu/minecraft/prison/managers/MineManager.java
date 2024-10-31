package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class MineManager {

    private final Prison plugin;
    private FileConfiguration minesConfig;
    private File minesFile;

    public MineManager(Prison plugin) {
        this.plugin = plugin;
        initializeMinesConfig();
    }

    /**
     * Initializes the mines configuration file (mines.yml).
     * Loads the configuration or creates a default one if it does not exist.
     */
    private void initializeMinesConfig() {
        minesFile = new File(plugin.getDataFolder(), "mines.yml");

        // Check if the file exists, and if not, create it with default content
        if (!minesFile.exists()) {
            plugin.saveResource("mines.yml", false);
        }

        minesConfig = YamlConfiguration.loadConfiguration(minesFile);
    }

    /**
     * Gets the mines configuration.
     *
     * @return FileConfiguration object for mines.yml
     */
    public FileConfiguration getMinesConfig() {
        return minesConfig;
    }

    /**
     * Saves any changes to mines.yml.
     */
    public void saveMinesConfig() {
        if (minesConfig == null || minesFile == null) {
            return;
        }
        try {
            minesConfig.save(minesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mines.yml: " + e.getMessage());
        }
    }

    /**
     * Reloads the mines.yml configuration.
     */
    public void reloadMinesConfig() {
        if (minesFile == null) {
            minesFile = new File(plugin.getDataFolder(), "mines.yml");
        }
        minesConfig = YamlConfiguration.loadConfiguration(minesFile);
    }

    /**
     * Resets the specified mine by clearing it and repopulating it with specified materials.
     *
     * @param mineName The name of the mine to reset
     * @param player   The player who initiated the reset command (for feedback)
     * @return true if the reset was successful, false otherwise
     */
    public boolean resetMineCommand(String mineName, Player player) {
        if (!minesConfig.contains(mineName)) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Mine '" + mineName + "' does not exist.");
            }
            return false;
        }

        // Retrieve the mine boundaries and material from config
        int x1 = minesConfig.getInt(mineName + ".x1");
        int y1 = minesConfig.getInt(mineName + ".y1");
        int z1 = minesConfig.getInt(mineName + ".z1");
        int x2 = minesConfig.getInt(mineName + ".x2");
        int y2 = minesConfig.getInt(mineName + ".y2");
        int z2 = minesConfig.getInt(mineName + ".z2");
        String materialName = minesConfig.getString(mineName + ".material", "STONE");

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            }
            return false;
        }

        World world = Bukkit.getWorld(minesConfig.getString(mineName + ".world"));
        if (world == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Invalid world specified for mine: " + mineName);
            }
            return false;
        }

        // Clear and reset the mine area with the specified material
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Location loc = new Location(world, x, y, z);
                    loc.getBlock().setType(material);
                }
            }
        }

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Mine '" + mineName + "' has been successfully reset.");
        }
        return true;
    }

    /**
     * Monitors each mine to check if it needs resetting.
     * Resets a mine if it falls below a certain resource threshold.
     */
    public void monitorMines() {
        for (String mineName : minesConfig.getKeys(false)) {
            int x1 = minesConfig.getInt(mineName + ".x1");
            int y1 = minesConfig.getInt(mineName + ".y1");
            int z1 = minesConfig.getInt(mineName + ".z1");
            int x2 = minesConfig.getInt(mineName + ".x2");
            int y2 = minesConfig.getInt(mineName + ".y2");
            int z2 = minesConfig.getInt(mineName + ".z2");
            String worldName = minesConfig.getString(mineName + ".world");
            World world = Bukkit.getWorld(worldName);
            String materialName = minesConfig.getString(mineName + ".material", "STONE");
            Material material = Material.matchMaterial(materialName);

            if (world == null || material == null) {
                Bukkit.getLogger().warning("Invalid configuration for mine: " + mineName);
                continue;
            }

            int blockCount = 0;
            int targetBlocks = 0;

            // Count total blocks and target material blocks
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        Location loc = new Location(world, x, y, z);
                        Block block = loc.getBlock();
                        blockCount++;
                        if (block.getType() == material) {
                            targetBlocks++;
                        }
                    }
                }
            }

            // Calculate the percentage of target material remaining
            double percentage = (targetBlocks * 100.0) / blockCount;
            int threshold = minesConfig.getInt(mineName + ".reset_threshold", 35); // default threshold of 35%

            if (percentage < threshold) {
                // Reset the mine if below threshold
                Bukkit.getLogger().info("Resetting mine '" + mineName + "' as it is below threshold.");
                resetMineCommand(mineName, null); // Null player, as it is a system reset
            }
        }
    }
}
