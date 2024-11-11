package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.MineManager;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PrisonTabCompleter implements TabCompleter {

    private final MineManager mineManager;
    private final Prison plugin;

    public PrisonTabCompleter(Prison plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (command.getName().toLowerCase()) {
            case "mine":
                handleMineTabCompletion(args, completions);
                break;
            case "prison":
                handlePrisonTabCompletion(args, completions);
                break;
            case "cells":
                handleCellsTabCompletion(sender, args, completions);
                break;
            case "ranks":
            case "rankup":
                return Collections.emptyList(); // No player name autocomplete for these commands
            default:
                break;
        }

        return completions;
    }

    private void handlePrisonTabCompletion(String[] args, List<String> completions) {
        if (args.length == 1) {
            List<String> subCommands = List.of("help", "ksu");
            completions.addAll(filterCompletions(subCommands, args[0]));
        }
    }

    private void handleMineTabCompletion(String[] args, List<String> completions) {
        if (args.length == 1) {
            List<String> subCommands = List.of("list", "reset", "reload", "create", "pos1", "pos2");
            completions.addAll(filterCompletions(subCommands, args[0]));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            completions.addAll(filterCompletions(mineManager.listMineNames(), args[1]));
        }
    }

    private void handleCellsTabCompletion(CommandSender sender, String[] args, List<String> completions) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();

        if (args.length == 1) {
            List<String> subCommands = List.of("list", "check", "rent", "remove");
            completions.addAll(filterCompletions(subCommands, args[0]));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("rent")) {
            // Suggest ranks D, C, B, A
            List<String> ranks = List.of("D", "C", "B", "A");
            completions.addAll(filterCompletions(ranks, args[1]));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("rent")) {
            // Autocomplete cell numbers based on the rank provided
            String rank = args[1].toUpperCase();
            if (config.contains("cells." + rank)) {
                List<String> cellNumbers = getCellNumbersForRank(rank);
                completions.addAll(filterCompletions(cellNumbers, args[2]));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            // Fetch the available cells from cells.yml
            FileConfiguration cellsConfig = plugin.getCellsConfig();
            if (cellsConfig.contains("rentedCells")) {
                Set<String> rentedCells = cellsConfig.getConfigurationSection("rentedCells").getKeys(false);
                completions.addAll(filterCompletions(new ArrayList<>(rentedCells), args[1]));
            }
        }
    }

    private List<String> getCellNumbersForRank(String rank) {
        FileConfiguration config = plugin.getConfig();
        List<String> cellNumbers = new ArrayList<>();

        if (config.contains("cells." + rank)) {
            String range = config.getString("cells." + rank + ".range");
            if (range != null) {
                String[] parts = range.split("-");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);

                    for (int number = start; number <= end; number++) {
                        cellNumbers.add(String.valueOf(number));
                    }
                }
            }
        }
        return cellNumbers;
    }

    private String getPlayerRank(Player player) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        return (user != null) ? user.getPrimaryGroup().toUpperCase() : "DEFAULT";
    }

    private List<String> filterCompletions(List<String> options, String input) {
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
