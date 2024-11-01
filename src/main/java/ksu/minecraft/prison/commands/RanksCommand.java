package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RanksCommand implements CommandExecutor {

    private final Prison plugin;
    private final RankManager rankManager;

    public RanksCommand(Prison plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("Your ranks:");
            // Display ranks and prices dynamically from config
            //Edited

            plugin.getConfig().getConfigurationSection("ranks").getKeys(false)
                    .forEach(rank -> player.sendMessage(rank + ": $" + plugin.getConfig().getInt("ranks." + rank + ".price")));

            /*plugin.getPluginConfig().getConfigurationSection("ranks").getKeys(false)
                    .forEach(rank -> player.sendMessage(rank + ": $" + plugin.getPluginConfig().getInt("ranks" + rank)));
             */
            return true;
        }
        return false;
    }
}
