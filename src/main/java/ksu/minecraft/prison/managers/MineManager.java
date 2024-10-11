package ksu.minecraft.prison.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MineManager {

    private final JavaPlugin plugin;
    private final FileConfiguration minesConfig;

    public MineManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.minesConfig = plugin.getConfig();
    }

    public void monitorMines() {
        for (String mineName : minesConfig.getConfigurationSection("mines").getKeys(false)) {
            double filledPercentage = getMineFillPercentage(mineName);

            if (filledPercentage < 35.0) { // 35% reset
                // TODO have the mine config define this value
                resetMine(mineName);
                announceMineReset(mineName);
            }
        }
    }

    public double getMineFillPercentage(String mineName) {
        if (!minesConfig.contains("mines." + mineName)) {
            return 100.0; // TODO temp value
        }

        int minX = minesConfig.getInt("mines." + mineName + ".minX");
        int minY = minesConfig.getInt("mines." + mineName + ".minY");
        int minZ = minesConfig.getInt("mines." + mineName + ".minZ");
        int maxX = minesConfig.getInt("mines." + mineName + ".maxX");
        int maxY = minesConfig.getInt("mines." + mineName + ".maxY");
        int maxZ = minesConfig.getInt("mines." + mineName + ".maxZ");

        String worldName = minesConfig.getString("mines." + mineName + ".world", "world");
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            Bukkit.getLogger().severe("World " + worldName + " not found!"); //TODO have the config define the world or remove
            return 100.0;
        }

        World world = BukkitAdapter.adapt(bukkitWorld);
        CuboidRegion region = new CuboidRegion(world, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));

        long totalBlocks = region.getArea();

        Map<Material, Double> expectedComposition = new HashMap<>();
        for (String blockKey : minesConfig.getConfigurationSection("mines." + mineName + ".composition").getKeys(false)) {
            Material material = Material.getMaterial(blockKey.toUpperCase());
            if (material != null) {
                double percentage = minesConfig.getDouble("mines." + mineName + ".composition." + blockKey);
                expectedComposition.put(material, percentage);
            }
        }

        long matchingBlocks = 0;
        for (BlockVector3 pos : region) {
            Material currentMaterial = BukkitAdapter.adapt(world.getBlock(pos).getBlockType());
            if (expectedComposition.containsKey(currentMaterial)) {
                matchingBlocks++;
            }
        }

        // Calculate fill percentage
        return ((double) matchingBlocks / totalBlocks) * 100;
    }

    public void resetMine(String mineName) {
        if (!minesConfig.contains("mines." + mineName)) {
            return; // Mine not found
        }

        int minX = minesConfig.getInt("mines." + mineName + ".minX");
        int minY = minesConfig.getInt("mines." + mineName + ".minY");
        int minZ = minesConfig.getInt("mines." + mineName + ".minZ");
        int maxX = minesConfig.getInt("mines." + mineName + ".maxX");
        int maxY = minesConfig.getInt("mines." + mineName + ".maxY");
        int maxZ = minesConfig.getInt("mines." + mineName + ".maxZ");

        String worldName = minesConfig.getString("mines." + mineName + ".world", "world");
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            Bukkit.getLogger().severe("World " + worldName + " not found!");
            return;
        }

        World world = BukkitAdapter.adapt(bukkitWorld);

        CuboidRegion region = new CuboidRegion(world, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                for (BlockVector3 pos : region) {
                    BlockState blockState = BlockTypes.STONE.getDefaultState(); // TODO block state
                    editSession.setBlock(pos, blockState);
                }
                editSession.flushQueue();
                Bukkit.getLogger().info("Mine " + mineName + " has been reset.");
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to reset mine " + mineName + ": " + e.getMessage());
            }
        });
    }

    public void announceMineReset(String mineName) {
        String name = minesConfig.getString("mines." + mineName + ".name", "Unnamed Mine");
        Bukkit.broadcastMessage("Mine " + name + " has been reset!");
    }

    public boolean resetMineCommand(Player player, String mineName) {
        if (minesConfig.contains("mines." + mineName)) {
            resetMine(mineName);
            player.sendMessage("Mine " + mineName + " has been reset.");
            announceMineReset(mineName);
            return true;
        } else {
            player.sendMessage("Mine " + mineName + " not found.");
            return false;
        }
    }
}
