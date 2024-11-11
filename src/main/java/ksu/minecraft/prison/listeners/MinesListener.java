package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.Mine;
import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.File;
import java.io.IOException;

public class MinesListener implements Listener {

    private final Prison plugin;
    private final MineManager mineManager;
    private FileConfiguration mineSignsConfig;
    private File mineSignsFile;

    public MinesListener(Prison plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        loadMineSignsConfig();
    }

    private void loadMineSignsConfig() {
        mineSignsFile = new File(plugin.getDataFolder(), "minesigns.yml");
        if (!mineSignsFile.exists()) {
            mineSignsFile.getParentFile().mkdirs();
            try {
                mineSignsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mineSignsConfig = YamlConfiguration.loadConfiguration(mineSignsFile);
    }

    private void saveMineSignsConfig() {
        try {
            mineSignsConfig.save(mineSignsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        try {
            Player player = event.getPlayer();
            String[] lines = event.getLines();

            if (!lines[0].equalsIgnoreCase("[mines]")) return;

            if (!player.hasPermission("prison.mines.createsign")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to create mine signs.");
                event.setCancelled(true);
                return;
            }

            String mineName = lines[1];

            if (!mineManager.mineExists(mineName)) {
                player.sendMessage(ChatColor.RED + "Mine '" + mineName + "' does not exist.");
                event.setCancelled(true);
                return;
            }

            // Store sign location associated with the mine
            String path = "mineSigns." + mineName + "." + event.getBlock().getWorld().getName() + "," + event.getBlock().getX() + "," + event.getBlock().getY() + "," + event.getBlock().getZ();
            mineSignsConfig.set(path + ".world", event.getBlock().getWorld().getName());
            mineSignsConfig.set(path + ".x", event.getBlock().getX());
            mineSignsConfig.set(path + ".y", event.getBlock().getY());
            mineSignsConfig.set(path + ".z", event.getBlock().getZ());
            saveMineSignsConfig();

            // Update the sign's text
            updateMineSign(event.getBlock(), mineName);

            player.sendMessage(ChatColor.GREEN + "Mine sign for '" + mineName + "' created.");
        } catch (Exception e) {
            event.getPlayer().sendMessage(ChatColor.RED + "An error occurred while creating the mine sign.");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();

        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;
        String[] lines = sign.getLines();

        String line0 = ChatColor.stripColor(lines[0]);
        if (!line0.startsWith("Mine:")) return;

        String mineName = ChatColor.stripColor(lines[0]).replace("Mine:", "").trim();

        // Remove sign from minesigns.yml
        String path = "mineSigns." + mineName + "." + block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
        if (mineSignsConfig.contains(path)) {
            mineSignsConfig.set(path, null);
            saveMineSignsConfig();
        }
    }

    public void updateAllMineSigns() {
        ConfigurationSection mineSignsSection = mineSignsConfig.getConfigurationSection("mineSigns");
        if (mineSignsSection == null) return;

        for (String mineName : mineSignsSection.getKeys(false)) {
            ConfigurationSection signsSection = mineSignsConfig.getConfigurationSection("mineSigns." + mineName);
            if (signsSection == null) continue;

            for (String signKey : signsSection.getKeys(false)) {
                String path = "mineSigns." + mineName + "." + signKey;
                String worldName = mineSignsConfig.getString(path + ".world");
                int x = mineSignsConfig.getInt(path + ".x");
                int y = mineSignsConfig.getInt(path + ".y");
                int z = mineSignsConfig.getInt(path + ".z");

                Block block = Bukkit.getWorld(worldName).getBlockAt(x, y, z);
                if (block != null) {
                    updateMineSign(block, mineName);
                }
            }
        }
    }

    public void updateMineSign(Block block, String mineName) {
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;

        Mine mine = mineManager.getMine(mineName);
        if (mine == null) return;

        // Use event-based tracking
        double percentMined = 100.0 - mine.getCurrentResourcePercentage();
        int blocksMined = mine.getBlocksMined();

        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "Mine: &a" + mineName));
        sign.setLine(1, ChatColor.translateAlternateColorCodes('&', "&b" + blocksMined + " &fmined"));
        sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&b" + String.format(" %.2f", percentMined) + "&r% mined"));
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', "Reset @ &c" + mine.getResetPercentage() + "&r%"));
        sign.update();
    }
}
