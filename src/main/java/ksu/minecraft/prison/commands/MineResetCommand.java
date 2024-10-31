package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class MineResetCommand implements CommandExecutor {
    //TODO not reading from config properly

    private final MineManager mineManager;

    public MineResetCommand(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                String mineName = args[0];

                // TODO use minimessage color
                if (mineManager.resetMineCommand(mineName, player)) {
                    player.sendMessage(ChatColor.GREEN + "Mine " + mineName + " has been reset.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "Mine " + mineName + " not found or failed to reset.");
                    return true;
                }
            } else {
                // TODO need to use mini message color
                player.sendMessage(ChatColor.RED + "Usage: /minereset <minename>");
                return true;
            }
        }

        // If not a player
        //TODO console should be able to run this
        sender.sendMessage("This command can only be run by players.");
        return false;
    }
}
