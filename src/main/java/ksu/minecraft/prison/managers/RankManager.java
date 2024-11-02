package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class RankManager {

    private final Prison plugin;
    private final LuckPerms luckPerms;

    public RankManager(Prison plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public String getCurrentRank(Player player) {
        //May not work as intended because lucky perms does not really consider what is in the config file
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null ? user.getPrimaryGroup() : null;
    }

    public void rankUp(Player player) {
        //
        String currentRank = getCurrentRank(player); //Tries to look at the player's current rank
        if (currentRank == null) {
            player.sendMessage(Component.text("Could not determine your current rank!"));
            return;
        }
        //For a d rank player, their current rank when issuing the command,
        //is just 'default' which will cause problems so we convert the current
        //rank to d in this instance
        if(currentRank.equals("default")){
            currentRank = "D";
        }

        // Fetch next rank and price from the config
        FileConfiguration config = plugin.getConfig();

        //Make sure the current rank is uppercase just like how it is in the config file
        String nextRank = config.getString("ranks." + currentRank.toUpperCase() + ".next_rank");
        int price = config.getInt("ranks." + currentRank.toUpperCase() + ".price");
        //player.sendMessage(Component.text(price));





        //Check to see if the player is at U, the highest rank, there shouldn't be another
        //rank in the config file here
        if (nextRank == null) {
            player.sendMessage(Component.text("You are at the highest rank!"));
            return;
        }

        if (plugin.getEconomyManager().canAfford(player, price)) {
            // Deduct the money and promote the player
            plugin.getEconomyManager().deductMoney(player, price);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user " + player.getName() + " promote ranks");
            player.sendMessage(Component.text("You ranked up to " + nextRank + "!"));
        } else {
            player.sendMessage(Component.text("You need $" + price + " to rank up!"));
        }
    }

}
