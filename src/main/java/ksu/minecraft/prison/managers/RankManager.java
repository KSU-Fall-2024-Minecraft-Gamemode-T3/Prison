package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RankManager {

    private final Prison plugin;
    private final LuckPerms luckPerms;

    public RankManager(Prison plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    /**
     * Retrieves the player's current rank.
     * If the rank is 'default', it will be displayed as 'D'.
     */
    public String getCurrentRank(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            // Treat 'default' as 'D' and return the rank in uppercase
            return user.getPrimaryGroup().equalsIgnoreCase("default") ? "D" : user.getPrimaryGroup().toUpperCase();
        }
        return null;
    }

    /**
     * Handles the player's rank-up process.
     */
    public void rankUp(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(ChatColor.RED + "Could not determine your current rank!");
            return;
        }

        // Get the player's current rank
        String currentRankName = user.getPrimaryGroup();

        // Display 'D' instead of 'default' in messages, and convert all to uppercase
        String displayRank = currentRankName.equalsIgnoreCase("default") ? "D" : currentRankName.toUpperCase();
        player.sendMessage(ChatColor.YELLOW + "Your current rank is: " + displayRank);

        // Handle the 'default' rank as starting rank
        if (currentRankName.equalsIgnoreCase("default")) {
            currentRankName = "default";
        }

        // Get the 'ranks' track from LuckPerms
        Track track = luckPerms.getTrackManager().getTrack("ranks");
        if (track == null) {
            player.sendMessage(ChatColor.RED + "Rank track 'ranks' does not exist!");
            return;
        }

        // Get the current Group object
        Group currentGroup = luckPerms.getGroupManager().getGroup(currentRankName);
        if (currentGroup == null) {
            player.sendMessage(ChatColor.RED + "Current group '" + currentRankName + "' does not exist!");
            return;
        }

        // Get the name of the next group in the track
        String nextRankName = track.getNext(currentGroup);
        if (nextRankName == null) {
            player.sendMessage(ChatColor.GREEN + "You are at the highest rank!");
            return;
        }

        // Convert the next rank name to uppercase for display
        player.sendMessage(ChatColor.YELLOW + "Next rank is: " + nextRankName.toUpperCase());

        // Fetch the price for the next rank from the config
        FileConfiguration config = plugin.getConfig();
        int price = config.getInt("ranks." + nextRankName.toUpperCase() + ".price", -1);

        if (price == -1) {
            player.sendMessage(ChatColor.RED + "Price for rank " + nextRankName.toUpperCase() + " is not set in the config!");
            return;
        }

        // Check if the player can afford the rank-up
        if (plugin.getEconomyManager().canAfford(player, price)) {
            // Deduct the money and promote the player
            plugin.getEconomyManager().deductMoney(player, price);

            // Use an empty context set (global context)
            ImmutableContextSet context = ImmutableContextSet.empty();

            // Promote the player to the next rank
            PromotionResult result = track.promote(user, context);

            if (result.wasSuccessful()) {
                luckPerms.getUserManager().saveUser(user);
                player.sendMessage(ChatColor.GREEN + "You ranked up to " + nextRankName.toUpperCase() + "!");
                Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " has ranked up to " + nextRankName.toUpperCase() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Could not promote you to " + nextRankName.toUpperCase() + "!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You need $" + price + " to rank up!");
        }
    }
}
