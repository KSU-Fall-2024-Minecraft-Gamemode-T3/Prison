package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.commands.CellsCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CellsListener implements Listener {

    private final Prison plugin;
    private final EconomyManager economyManager;
    public CellsCommand cellsCommand;

    public CellsListener(Prison plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public void setCellsCommand(CellsCommand cellsCommand) {
        this.cellsCommand = cellsCommand;
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        if (!lines[0].equalsIgnoreCase("[rent]")) return;

        if (!player.hasPermission("prison.cells.createsign")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create rent signs.");
            event.setCancelled(true);
            return;
        }

        String cellName = lines[1].toUpperCase();

        // Fetch cell details from config
        String cellType = cellName.substring(0, 1);
        ConfigurationSection cellsSection = plugin.getConfig().getConfigurationSection("cells." + cellType);

        if (cellsSection == null) {
            player.sendMessage(ChatColor.RED + "Invalid cell type.");
            return;
        }

        int price = cellsSection.getInt("price");
        int time = cellsSection.getInt("time");

        // Set sign text with formatting
        event.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2&l[Rent Cell]"));
        event.setLine(1, cellName);
        event.setLine(2, ChatColor.translateAlternateColorCodes('&', "&f" + time + " days"));
        event.setLine(3, ChatColor.translateAlternateColorCodes('&', "&6&l$" + price));

        // Store sign location in cells.yml
        FileConfiguration cellsConfig = plugin.getCellsConfig();
        String path = "cellSigns." + cellName;
        cellsConfig.set(path + ".world", event.getBlock().getWorld().getName());
        cellsConfig.set(path + ".x", event.getBlock().getX());
        cellsConfig.set(path + ".y", event.getBlock().getY());
        cellsConfig.set(path + ".z", event.getBlock().getZ());
        plugin.saveCellsConfig();

        player.sendMessage(ChatColor.GREEN + "Rent sign for cell " + cellName + " created.");
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;
        String[] lines = sign.getLines();

        String line0 = ChatColor.stripColor(lines[0]);
        if (!line0.equalsIgnoreCase("[Rent Cell]") && !line0.equalsIgnoreCase("[Rented]")) return;

        Player player = event.getPlayer();
        String cellName = ChatColor.stripColor(lines[1]);

        if (line0.equalsIgnoreCase("[Rent Cell]")) {
            // Simulate /cells rent <cellName> command
            Bukkit.getScheduler().runTask(plugin, () -> {
                String[] args = {"rent", cellName};
                cellsCommand.onCommand(player, null, "cells", args);
            });
        } else if (line0.equalsIgnoreCase("[Rented]")) {
            player.sendMessage(ChatColor.RED + "This cell is already rented.");
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
        if (!line0.equalsIgnoreCase("[Rent Cell]") && !line0.equalsIgnoreCase("[Rented]")) return;

        String cellName = ChatColor.stripColor(lines[1]);

        // Remove sign location from cells.yml
        FileConfiguration cellsConfig = plugin.getCellsConfig();
        String path = "cellSigns." + cellName;
        if (cellsConfig.contains(path)) {
            cellsConfig.set(path, null);
            plugin.saveCellsConfig();
        }
    }

    public void updateSignToRented(Block block, String cellName, String playerName, long timeLeftSeconds) {
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;

        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&4&l[Rented]"));
        sign.setLine(1, cellName);
        sign.setLine(2, playerName);

        String timeLeftFormatted = formatTimeLeft(timeLeftSeconds);
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', "&f" + timeLeftFormatted));

        sign.update();
    }

    public void updateSignToAvailable(Block block, String cellName, int time, int price) {
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;

        sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2&l[Rent Cell]"));
        sign.setLine(1, cellName);
        sign.setLine(2, ChatColor.translateAlternateColorCodes('&', "&f" + time + " days"));
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', "&6&l$" + price));

        sign.update();
    }

    private String formatTimeLeft(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");

        return sb.toString().trim();
    }
    public void updateSignTimeRemaining(Block block, long timeLeftSeconds) {
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;

        String line0 = ChatColor.stripColor(sign.getLine(0));
        if (!line0.equalsIgnoreCase("[Rented]")) return;

        String timeLeftFormatted = formatTimeLeft(timeLeftSeconds);
        sign.setLine(3, ChatColor.translateAlternateColorCodes('&', "&f" + timeLeftFormatted));

        sign.update();
    }

}
