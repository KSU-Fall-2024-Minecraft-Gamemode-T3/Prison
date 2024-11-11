package ksu.minecraft.prison.commands;

import com.sk89q.worldguard.protection.flags.StateFlag;
import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.EconomyManager;
import ksu.minecraft.prison.listeners.CellsListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

import org.bukkit.block.Block;

public class CellsCommand implements CommandExecutor {

    private final Prison plugin;
    private final EconomyManager economyManager;
    private final WorldGuardPlugin worldGuard;
    private final CellsListener cellsListener;

    public CellsCommand(Prison plugin, EconomyManager economyManager, CellsListener cellsListener) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.worldGuard = WorldGuardPlugin.inst();
        this.cellsListener = cellsListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            listAvailableCells(player);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("rent")) {
            rentCell(player, args[1].toUpperCase());
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("check")) {
            checkRentedCell(player);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            if (player.hasPermission("prison.cells.remove")) {
                removeCellCommand(player, args[1].toUpperCase());
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to remove cells.");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /cells list, /cells rent <cell>, /cells check, or /cells remove <cell>");
            return false;
        }
    }

    private void listAvailableCells(Player player) {
        ConfigurationSection cellsSection = plugin.getConfig().getConfigurationSection("cells");
        if (cellsSection == null) {
            player.sendMessage(ChatColor.RED + "No cells are defined in the configuration.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Available Cells:");
        for (String cellType : cellsSection.getKeys(false)) {
            int price = cellsSection.getInt(cellType + ".price");
            int time = cellsSection.getInt(cellType + ".time");
            String range = cellsSection.getString(cellType + ".range");

            player.sendMessage(ChatColor.YELLOW + cellType + ": " + ChatColor.GREEN + "$" + price + ChatColor.YELLOW + " for " + time + " days (Range: " + range + ")");
        }
    }

    private void rentCell(Player player, String cell) {
        FileConfiguration cellsConfig = plugin.getCellsConfig();

        // Check if player already has a rented cell
        String rentedCell = cellsConfig.getString("playerRentals." + player.getUniqueId() + ".cell");

        if (rentedCell != null) {
            player.sendMessage(ChatColor.RED + "You already have a rented cell: " + rentedCell + ".");
            return;
        }

        String cellType = cell.substring(0, 1);
        ConfigurationSection cellsSection = plugin.getConfig().getConfigurationSection("cells." + cellType);

        if (cellsSection == null || !isCellInRange(cell, cellsSection.getString("range"))) {
            player.sendMessage(ChatColor.RED + "Invalid cell type or cell number out of range.");
            return;
        }

        // Check if the cell is already rented
        if (cellsConfig.contains("rentedCells." + cell)) {
            player.sendMessage(ChatColor.RED + "Cell " + cell + " is already rented by someone else.");
            return;
        }

        int price = cellsSection.getInt("price");
        int time = cellsSection.getInt("time");

        if (economyManager.getBalance(player) < price) {
            player.sendMessage(ChatColor.RED + "You do not have enough money to rent this cell.");
            return;
        }

        // Deduct money and give region permission
        economyManager.deductMoney(player, price);
        grantCellAccess(player, cell);

        // Calculate expiration time
        long expiresAt = Instant.now().plus(Duration.ofDays(time)).getEpochSecond();

        // Record rented cell in cells.yml
        cellsConfig.set("playerRentals." + player.getUniqueId() + ".cell", cell);
        cellsConfig.set("playerRentals." + player.getUniqueId() + ".expiresAt", expiresAt);
        cellsConfig.set("rentedCells." + cell + ".playerUUID", player.getUniqueId().toString());
        cellsConfig.set("rentedCells." + cell + ".expiresAt", expiresAt);
        plugin.saveCellsConfig();

        player.sendMessage(ChatColor.GREEN + "Successfully rented cell " + cell + " for " + time + " days for $" + price + ".");

        // Set permission node indicating player has rented a cell
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set cell.rented true");

        // Schedule task to handle cell expiration
        scheduleCellExpiration(player.getUniqueId(), cell, expiresAt);

        // Update sign to show rented status
        Block signBlock = findSignByCellName(player.getWorld(), cell);
        if (signBlock != null) {
            long timeLeftSeconds = expiresAt - Instant.now().getEpochSecond();
            cellsListener.updateSignToRented(signBlock, cell, player.getName(), timeLeftSeconds);
        }
    }

    private void removeCellCommand(Player player, String cell) {
        FileConfiguration cellsConfig = plugin.getCellsConfig();

        // Check if the cell is rented
        if (!cellsConfig.contains("rentedCells." + cell)) {
            player.sendMessage(ChatColor.RED + "Cell " + cell + " is not currently rented.");
            return;
        }

        // Get the player's UUID who rented the cell
        String playerUUIDStr = cellsConfig.getString("rentedCells." + cell + ".playerUUID");
        if (playerUUIDStr == null) {
            player.sendMessage(ChatColor.RED + "Could not find the player who rented cell " + cell + ".");
            return;
        }

        UUID playerUUID = UUID.fromString(playerUUIDStr);

        // Remove cell access
        removeCellAccess(playerUUID, cell);

        player.sendMessage(ChatColor.GREEN + "Cell " + cell + " has been reset.");

        // Optionally notify the player
        Player renter = Bukkit.getPlayer(playerUUID);
        if (renter != null && renter.isOnline()) {
            renter.sendMessage(ChatColor.RED + "Your rental for cell " + cell + " has been removed by an administrator.");
        }
    }

    private boolean isCellInRange(String cell, String range) {
        String[] bounds = range.split("-");
        int cellNumber = Integer.parseInt(cell.substring(1));
        int lowerBound = Integer.parseInt(bounds[0]);
        int upperBound = Integer.parseInt(bounds[1]);

        return cellNumber >= lowerBound && cellNumber <= upperBound;
    }

    private void grantCellAccess(Player player, String cell) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regions == null) {
            player.sendMessage(ChatColor.RED + "WorldGuard region manager not found.");
            return;
        }

        ProtectedRegion region = regions.getRegion(cell);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "No region found for cell " + cell + ".");
            return;
        }

        region.getMembers().addPlayer(worldGuard.wrapPlayer(player));
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        player.sendMessage(ChatColor.GREEN + "Access granted to cell " + cell + ".");
    }

    private void checkRentedCell(Player player) {
        FileConfiguration cellsConfig = plugin.getCellsConfig();
        String rentedCell = cellsConfig.getString("playerRentals." + player.getUniqueId() + ".cell");

        if (rentedCell == null) {
            player.sendMessage(ChatColor.YELLOW + "You currently do not have a rented cell.");
        } else {
            long expiresAt = cellsConfig.getLong("playerRentals." + player.getUniqueId() + ".expiresAt");
            long timeLeft = expiresAt - Instant.now().getEpochSecond();

            if (timeLeft <= 0) {
                player.sendMessage(ChatColor.RED + "Your rental for cell " + rentedCell + " has expired.");
                // Handle expiration
                removeCellAccess(player.getUniqueId(), rentedCell);
            } else {
                Duration duration = Duration.ofSeconds(timeLeft);
                long days = duration.toDays();
                long hours = duration.minusDays(days).toHours();
                long minutes = duration.minusDays(days).minusHours(hours).toMinutes();

                player.sendMessage(ChatColor.GREEN + "You have rented cell: " + rentedCell + ".");
                player.sendMessage(ChatColor.GREEN + "Time left: " + days + "d " + hours + "h " + minutes + "m.");
            }
        }
    }

    public void removeCellAccess(UUID playerUUID, String cell) {
        FileConfiguration cellsConfig = plugin.getCellsConfig();
        Player player = Bukkit.getPlayer(playerUUID);

        // Remove player from region members
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("world"))); // Adjust world name if necessary

        if (regions != null) {
            ProtectedRegion region = regions.getRegion(cell);
            if (region != null) {
                region.getMembers().removePlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Your access to cell " + cell + " has expired.");
                }
            }
        }

        // Remove permission node
        if (player != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset cell.rented");
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerUUID.toString() + " permission unset cell.rented");
        }

        // Remove rental info from cells.yml
        cellsConfig.set("playerRentals." + playerUUID, null);
        cellsConfig.set("rentedCells." + cell, null);
        plugin.saveCellsConfig();

        // Update sign back to available
        Block signBlock = findSignByCellName(Bukkit.getWorld("world"), cell); // Adjust world name if necessary
        if (signBlock != null) {
            // Fetch cell details from config
            String cellType = cell.substring(0, 1);
            ConfigurationSection cellsSection = plugin.getConfig().getConfigurationSection("cells." + cellType);
            if (cellsSection != null) {
                int price = cellsSection.getInt("price");
                int time = cellsSection.getInt("time");
                cellsListener.updateSignToAvailable(signBlock, cell, time, price);
            }
        }
    }

    public void scheduleCellExpiration(UUID playerUUID, String cell, long expiresAt) {
        long delay = (expiresAt - Instant.now().getEpochSecond()) * 20; // Convert seconds to ticks

        if (delay <= 0) {
            // Already expired
            removeCellAccess(playerUUID, cell);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeCellAccess(playerUUID, cell);
        }, delay);
    }

    private Block findSignByCellName(World world, String cellName) {
        FileConfiguration cellsConfig = plugin.getCellsConfig();

        String path = "cellSigns." + cellName;
        if (!cellsConfig.contains(path)) {
            return null;
        }

        String worldName = cellsConfig.getString(path + ".world");
        int x = cellsConfig.getInt(path + ".x");
        int y = cellsConfig.getInt(path + ".y");
        int z = cellsConfig.getInt(path + ".z");

        World signWorld = Bukkit.getWorld(worldName);
        if (signWorld == null) return null;

        return signWorld.getBlockAt(x, y, z);
    }
    public void scheduleSignUpdates() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateAllRentedSigns();
        }, 0L, 1200L); // Runs every 1200 ticks (1 minute)
    }

    private void updateAllRentedSigns() {
        FileConfiguration cellsConfig = plugin.getCellsConfig();
        ConfigurationSection rentedCells = cellsConfig.getConfigurationSection("rentedCells");
        if (rentedCells == null) return;

        for (String cell : rentedCells.getKeys(false)) {
            String playerUUIDStr = cellsConfig.getString("rentedCells." + cell + ".playerUUID");
            long expiresAt = cellsConfig.getLong("rentedCells." + cell + ".expiresAt");
            long timeLeftSeconds = expiresAt - Instant.now().getEpochSecond();

            if (timeLeftSeconds <= 0) {
                // Rental has expired, remove access
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                removeCellAccess(playerUUID, cell);
            } else {
                // Update the sign's time remaining
                Block signBlock = findSignByCellName(Bukkit.getWorld("world"), cell); // Adjust world name if necessary
                if (signBlock != null) {
                    cellsListener.updateSignTimeRemaining(signBlock, timeLeftSeconds);
                }
            }
        }
    }

}
