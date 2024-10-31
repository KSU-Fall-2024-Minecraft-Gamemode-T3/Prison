package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.Prison;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CellsCommand implements CommandExecutor {

    private final Prison plugin;

    public CellsCommand(Prison plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;
        if (args.length == 1) {
            String cell = args[0].toUpperCase();
            int price = plugin.getPluginConfig().getInt("cells." + cell + ".price");
            int duration = plugin.getPluginConfig().getInt("cells." + cell + ".duration");

            player.sendMessage("Renting cell " + cell + " for $" + price + " for " + duration + " days.");
            return true;
        } else {
            player.sendMessage("Usage: /cells <cell>");
            return false;
        }
    }
}
