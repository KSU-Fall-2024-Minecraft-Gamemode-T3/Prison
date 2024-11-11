package ksu.minecraft.prison.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class Mine {
    private final String name;
    private final World world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final boolean fillMode;
    private final String surface;
    private final boolean isSilent;
    private final Map<Material, Double> composition = new HashMap<>();
    private final Random random = new Random();
    private final double resetPercentage; // Percentage at which the mine resets

    private int totalBlocks;
    private int blocksMined;

    // Constructor for loading mine from configuration
    public Mine(String name, ConfigurationSection config) {
        this.name = config.getString("name", name);
        this.world = Bukkit.getWorld(config.getString("world"));

        // Ensure min and max coordinates are properly assigned
        int tempMinX = config.getInt("minX");
        int tempMaxX = config.getInt("maxX");
        this.minX = Math.min(tempMinX, tempMaxX);
        this.maxX = Math.max(tempMinX, tempMaxX);

        int tempMinY = config.getInt("minY");
        int tempMaxY = config.getInt("maxY");
        this.minY = Math.min(tempMinY, tempMaxY);
        this.maxY = Math.max(tempMinY, tempMaxY);

        int tempMinZ = config.getInt("minZ");
        int tempMaxZ = config.getInt("maxZ");
        this.minZ = Math.min(tempMinZ, tempMaxZ);
        this.maxZ = Math.max(tempMinZ, tempMaxZ);

        this.fillMode = config.getBoolean("fillMode", false);
        this.surface = config.getString("surface", "");
        this.isSilent = config.getBoolean("isSilent", false);
        this.resetPercentage = config.getDouble("resetPercentage", 35.0); // Default to 35%

        // Load composition
        if (config.contains("composition")) {
            ConfigurationSection compSection = config.getConfigurationSection("composition");
            for (String key : compSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                double percentage = compSection.getDouble(key);
                if (material != null) {
                    composition.put(material, percentage);
                }
            }
        } else {
            // Default to STONE if no composition is specified
            composition.put(Material.STONE, 1.0);
        }

        // Initialize blocksMined from config
        this.blocksMined = config.getInt("blocksMined", 0);
        //Bukkit.getLogger().info("Mine '" + name + "' blocksMined initialized to: " + blocksMined);

        // Initialize totalBlocks
        initializeTotalBlocks();
    }

    // Constructor for creating a new mine via commands
    public Mine(String name, World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                boolean fillMode, String surface, boolean isSilent,
                Map<Material, Double> composition, double resetPercentage) {
        this.name = name;
        this.world = world;
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);
        this.fillMode = fillMode;
        this.surface = surface;
        this.isSilent = isSilent;
        this.composition.putAll(composition);
        this.resetPercentage = resetPercentage;

        // Initialize blocksMined
        this.blocksMined = 0;
        //Bukkit.getLogger().info("Mine '" + name + "' blocksMined initialized to: " + blocksMined);

        // Initialize totalBlocks
        initializeTotalBlocks();
    }

    public void initializeTotalBlocks() {
        totalBlocks = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        //Bukkit.getLogger().info("Mine '" + name + "' totalBlocks initialized to: " + totalBlocks);
    }

    public String getName() {
        return name;
    }

    public boolean isSilent() {
        return isSilent;
    }

    // Checks if a player's location is inside the mine.
    public boolean isPlayerInside(Player player) {
        return containsLocation(player.getLocation());
    }

    // Checks if a location is inside the mine.
    public boolean containsLocation(Location loc) {
        if (!loc.getWorld().equals(world)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        boolean withinX = x >= minX && x <= maxX;
        boolean withinY = y >= minY && y <= maxY;
        boolean withinZ = z >= minZ && z <= maxZ;

        boolean contains = withinX && withinY && withinZ;

        /*if (contains) {
            Bukkit.getLogger().info("Location " + loc + " is within mine '" + name + "'");
        }*/

        return contains;
    }

    // Teleports a player to the top of the mine.
    public void teleportPlayerToTop(Player player) {
        Location teleportLocation = new Location(world,
                (minX + maxX) / 2.0,
                maxY + 1,
                (minZ + maxZ) / 2.0);
        player.teleport(teleportLocation);
    }

    // Resets the mine area based on its composition.
    public void reset() {
        if (world == null) {
            Bukkit.getLogger().warning("World not found for mine: " + name);
            return;
        }

        // Teleport players inside the mine to the top
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerInside(player)) {
                teleportPlayerToTop(player);
                player.sendMessage(ChatColor.YELLOW + "You have been moved to the top of the mine as it resets.");
            }
        }

        // Prepare materials and their probabilities
        double totalPercentage = composition.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPercentage <= 0) {
            //Bukkit.getLogger().warning("Invalid composition for mine: " + name);
            return;
        }

        // Normalize composition percentages
        Map<Material, Double> normalizedComposition = new HashMap<>();
        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            normalizedComposition.put(entry.getKey(), entry.getValue() / totalPercentage);
        }

        // Build a list of materials and their cumulative probabilities
        List<Map.Entry<Material, Double>> materialChances = new ArrayList<>();
        double cumulative = 0.0;
        for (Map.Entry<Material, Double> entry : normalizedComposition.entrySet()) {
            cumulative += entry.getValue();
            materialChances.add(new AbstractMap.SimpleEntry<>(entry.getKey(), cumulative));
        }

        // Reset the mine area
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    // Handle fillMode
                    if (fillMode && !block.getType().isAir()) {
                        continue;
                    }

                    // Handle surface block
                    if (y == maxY && surface != null && !surface.isEmpty()) {
                        Material surfaceMaterial = Material.matchMaterial(surface);
                        if (surfaceMaterial != null) {
                            block.setType(surfaceMaterial, false);
                            continue;
                        }
                    }

                    Material material = getRandomMaterial(materialChances);
                    block.setType(material, false);
                }
            }
        }

        // Reset blocksMined
        resetBlocksMined();
        Bukkit.getLogger().info("Mine '" + name + "' blocksMined reset to: " + blocksMined);

        // Announce reset
        Bukkit.broadcastMessage(ChatColor.GREEN + "[Mines] " + name + " has been reset!");
    }

    private Material getRandomMaterial(List<Map.Entry<Material, Double>> materialChances) {
        double r = random.nextDouble();
        for (Map.Entry<Material, Double> entry : materialChances) {
            if (r <= entry.getValue()) {
                return entry.getKey();
            }
        }
        // Fallback in case of rounding errors
        return Material.STONE;
    }

    public double getCurrentResourcePercentage() {
        double percentage = ((double) (totalBlocks - blocksMined) / totalBlocks) * 100.0;
        Bukkit.getLogger().info("Mine '" + name + "' resource percentage: " + percentage + "%");
        return percentage;
    }

    public double getResetPercentage() {
        return resetPercentage;
    }

    public void saveToConfig(ConfigurationSection mineSection) {
        mineSection.set("name", name);
        mineSection.set("world", world.getName());
        mineSection.set("minX", minX);
        mineSection.set("minY", minY);
        mineSection.set("minZ", minZ);
        mineSection.set("maxX", maxX);
        mineSection.set("maxY", maxY);
        mineSection.set("maxZ", maxZ);
        mineSection.set("fillMode", fillMode);
        mineSection.set("surface", surface);
        mineSection.set("isSilent", isSilent);
        mineSection.set("resetPercentage", resetPercentage);

        // Save composition
        ConfigurationSection compSection = mineSection.createSection("composition");
        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            compSection.set(entry.getKey().name(), entry.getValue());
        }

        // Save blocksMined
        mineSection.set("blocksMined", blocksMined);
    }

    // Event-based methods
    public void incrementBlocksMined() {
        blocksMined++;
        //Bukkit.getLogger().info("Mine '" + name + "' blocksMined incremented to: " + blocksMined);
    }

    public void decrementBlocksMined() {
        if (blocksMined > 0) {
            blocksMined--;
            //Bukkit.getLogger().info("Mine '" + name + "' blocksMined decremented to: " + blocksMined);
        }
    }

    public void resetBlocksMined() {
        blocksMined = 0;
    }

    public int getBlocksMined() {
        return blocksMined;
    }
}
