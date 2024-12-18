package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.Prison;
import ksu.minecraft.prison.managers.RankManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.track.Track;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class RanksCommand implements CommandExecutor {

    private final Prison plugin;
    private final RankManager rankManager;
    private final LuckPerms luckPerms;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    public RanksCommand(Prison plugin, RankManager rankManager, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Fetch the 'ranks' track from LuckPerms
        Track track = luckPerms.getTrackManager().getTrack("ranks");
        if (track == null) {
            player.sendMessage(ChatColor.RED + "Rank track 'ranks' does not exist!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Available Ranks:");

        // List all ranks from the track
        List<String> groupNames = track.getGroups();
        for (String rankName : groupNames) {
            // Skip 'default' rank
            if (rankName.equalsIgnoreCase("default")) {
                continue;
            }

            // Fetch the price for the rank from the config
            int price = plugin.getConfig().getInt("ranks." + rankName.toUpperCase() + ".price", -1);
            if (price == -1) {
                player.sendMessage(ChatColor.RED + "Price for rank " + rankName.toUpperCase() + " is not set in the config!");
                continue;
            }

            // Display the rank in uppercase and its cost
            player.sendMessage(ChatColor.GREEN + rankName.toUpperCase() + ChatColor.WHITE + ": $" + moneyFormat.format(price));
        }

        return true;
    }
}
