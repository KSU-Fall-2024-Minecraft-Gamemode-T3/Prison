package ksu.minecraft.prison.commands;

import com.sk89q.worldguard.protection.flags.StateFlag;
import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class CellsCommand implements CommandExecutor {

    private final Prison plugin;
    private final EconomyManager economyManager;
    private final WorldGuardPlugin worldGuard;

    public CellsCommand(Prison plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.worldGuard = WorldGuardPlugin.inst();
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
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /cells list, /cells rent <cell>, or /cells check");
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
        // Check if player already has a rented cell
        String rentedCell = plugin.getConfig().getString("rentedCells." + player.getUniqueId());

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

        int price = cellsSection.getInt("price");
        int time = cellsSection.getInt("time");

        if (economyManager.getBalance(player) < price) {
            player.sendMessage(ChatColor.RED + "You do not have enough money to rent this cell.");
            return;
        }

        // Deduct money and give region permission
        economyManager.deductMoney(player, price);
        grantCellAccess(player, cell);

        // Record rented cell in configuration
        plugin.getConfig().set("rentedCells." + player.getUniqueId(), cell);
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "Successfully rented cell " + cell + " for " + time + " days for $" + price + ".");

        // Set permission node indicating player has rented a cell
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set cell.rented true");
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
        String rentedCell = plugin.getConfig().getString("rentedCells." + player.getUniqueId());

        if (rentedCell == null) {
            player.sendMessage(ChatColor.YELLOW + "You currently do not have a rented cell.");
        } else {
            player.sendMessage(ChatColor.GREEN + "You have rented cell: " + rentedCell + ".");
        }
    }
}
