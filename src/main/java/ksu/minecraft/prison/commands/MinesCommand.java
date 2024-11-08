package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.Mine;
import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MinesCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final MineManager mineManager;
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public MinesCommand(JavaPlugin plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission checks can be added here if needed
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                // List all mines
                player.sendMessage("Mines:");
                for (String mineName : mineManager.listMineNames()) {
                    player.sendMessage(ChatColor.GREEN + mineName);
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                // Reset a specific mine
                if (args.length >= 2) {
                    String mineName = args[1];

                    if (mineManager.resetMineCommand(mineName, player)) {
                        player.sendMessage(ChatColor.GREEN + "Mine " + mineName + " has been reset.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Mine " + mineName + " not found or failed to reset.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /mine reset <mineName>");
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                // Reload mines configuration
                mineManager.reloadMinesConfig();
                player.sendMessage(ChatColor.GREEN + "Mines configuration reloaded.");
            } else if (args[0].equalsIgnoreCase("create")) {
                // Create a new mine
                // Usage: /mine create <mineName>
                if (args.length >= 2) {
                    String mineName = args[1];

                    if (mineManager.mineExists(mineName)) {
                        player.sendMessage(ChatColor.RED + "A mine with that name already exists.");
                        return true;
                    }

                    UUID playerUUID = player.getUniqueId();
                    Location pos1 = pos1Map.get(playerUUID);
                    Location pos2 = pos2Map.get(playerUUID);

                    if (pos1 == null || pos2 == null) {
                        player.sendMessage(ChatColor.RED + "You need to set both positions using /mine pos1 and /mine pos2.");
                        return true;
                    }

                    // Define default composition
                    Map<Material, Double> composition = new HashMap<>();
                    composition.put(Material.STONE, 1.0);

                    // Create the new mine
                    Mine newMine = new Mine(
                            mineName,
                            player.getWorld(),
                            pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                            pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                            false, // fillMode
                            "",    // surface
                            0,     // resetDelay
                            Collections.emptyList(), // resetWarnings
                            false, // isSilent
                            composition
                    );

                    mineManager.addMine(newMine);
                    player.sendMessage(ChatColor.GREEN + "Mine '" + mineName + "' has been created.");

                    // Clear the positions
                    pos1Map.remove(playerUUID);
                    pos2Map.remove(playerUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /mine create <mineName>");
                }
            } else if (args[0].equalsIgnoreCase("pos1")) {
                // Set position 1
                UUID playerUUID = player.getUniqueId();
                Location location = player.getLocation();
                pos1Map.put(playerUUID, location);
                player.sendMessage(ChatColor.GREEN + "Position 1 set to your current location.");
            } else if (args[0].equalsIgnoreCase("pos2")) {
                // Set position 2
                UUID playerUUID = player.getUniqueId();
                Location location = player.getLocation();
                pos2Map.put(playerUUID, location);
                player.sendMessage(ChatColor.GREEN + "Position 2 set to your current location.");
            } else {
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage:");
                sendUsage(player);
            }
        } else {
            sendUsage(player);
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Mine Commands:");
        player.sendMessage(ChatColor.GREEN + "/mine list" + ChatColor.WHITE + " - List all mines");
        player.sendMessage(ChatColor.GREEN + "/mine reset <mineName>" + ChatColor.WHITE + " - Reset a mine");
        player.sendMessage(ChatColor.GREEN + "/mine reload" + ChatColor.WHITE + " - Reload mines configuration");
        player.sendMessage(ChatColor.GREEN + "/mine create <mineName>" + ChatColor.WHITE + " - Create a new mine");
        player.sendMessage(ChatColor.GREEN + "/mine pos1" + ChatColor.WHITE + " - Set position 1");
        player.sendMessage(ChatColor.GREEN + "/mine pos2" + ChatColor.WHITE + " - Set position 2");
    }
}
