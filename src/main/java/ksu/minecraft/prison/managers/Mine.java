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
    private final int resetDelay;
    private int resetClock;
    private final List<Integer> resetWarnings = new ArrayList<>();
    private final boolean isSilent;
    private final Map<Material, Double> composition = new HashMap<>();
    private final Random random = new Random();

    // Constructor for loading mine from configuration
    public Mine(String name, ConfigurationSection config) {
        this.name = config.getString("name", name);
        this.world = Bukkit.getWorld(config.getString("world"));
        this.minX = config.getInt("minX");
        this.minY = config.getInt("minY");
        this.minZ = config.getInt("minZ");
        this.maxX = config.getInt("maxX");
        this.maxY = config.getInt("maxY");
        this.maxZ = config.getInt("maxZ");
        this.fillMode = config.getBoolean("fillMode", false);
        this.surface = config.getString("surface", "");
        this.resetDelay = config.getInt("resetDelay", 0);
        this.resetClock = config.getInt("resetClock", resetDelay);
        this.isSilent = config.getBoolean("isSilent", false);

        // Load reset warnings
        List<?> warningsList = config.getList("resetWarnings", Collections.emptyList());
        for (Object obj : warningsList) {
            if (obj instanceof Integer) {
                resetWarnings.add((Integer) obj);
            } else if (obj instanceof String) {
                try {
                    resetWarnings.add(Integer.parseInt((String) obj));
                } catch (NumberFormatException ignored) {
                }
            }
        }

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
    }

    // Constructor for creating a new mine via commands
    public Mine(String name, World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                boolean fillMode, String surface, int resetDelay, List<Integer> resetWarnings, boolean isSilent,
                Map<Material, Double> composition) {
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
        this.resetDelay = resetDelay;
        this.resetClock = resetDelay;
        this.isSilent = isSilent;
        this.resetWarnings.addAll(resetWarnings);
        this.composition.putAll(composition);
    }

    public String getName() {
        return name;
    }

    public boolean isSilent() {
        return isSilent;
    }


     //Checks if a player's location is inside the mine.

    public boolean isPlayerInside(Player player) {
        Location loc = player.getLocation();
        if (!loc.getWorld().equals(world)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }


     // Teleports a player to the top of the mine.

    public void teleportPlayerToTop(Player player) {
        Location teleportLocation = new Location(world,
                (minX + maxX) / 2.0,
                maxY + 1,
                (minZ + maxZ) / 2.0);
        player.teleport(teleportLocation);
    }


     //Resets the mine area based on its composition.

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
            Bukkit.getLogger().warning("Invalid composition for mine: " + name);
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


    public boolean shouldReset() {
        if (resetDelay <= 0) {
            return false;
        }
        resetClock--;
        if (resetClock <= 0) {
            resetClock = resetDelay;
            return true;
        }

        // Send warnings if needed
        if (!isSilent && resetWarnings.contains(resetClock)) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Mine " + name + " will reset in " + resetClock + " minutes!");
        }

        return false;
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
        mineSection.set("resetDelay", resetDelay);
        mineSection.set("resetClock", resetClock);
        mineSection.set("resetWarnings", resetWarnings);
        mineSection.set("isSilent", isSilent);

        // Save composition
        ConfigurationSection compSection = mineSection.createSection("composition");
        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            compSection.set(entry.getKey().name(), entry.getValue());
        }
    }
}
