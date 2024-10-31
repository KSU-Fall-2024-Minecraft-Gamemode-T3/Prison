package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

public class MinesCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public MinesCommand(JavaPlugin plugin, MineManager mineManager) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                FileConfiguration minesConfig = plugin.getConfig();
                if (minesConfig.contains("mines")) {
                    player.sendMessage(Component.text("Mines:"));
                    for (String mineName : minesConfig.getConfigurationSection("mines").getKeys(false)) {
                        String name = minesConfig.getString("mines." + mineName + ".name", "Unnamed Mine");
                        player.sendMessage(Component.text("Mine: " + name));
                    }
                } else {
                    player.sendMessage(Component.text("No mines found in configuration."));
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
                player.sendMessage(Component.text("Use /minereset <minename> to reset a mine manually."));
            }

            return true;
        }
        sender.sendMessage("This command can only be run by players.");
        return false;
    }
}
