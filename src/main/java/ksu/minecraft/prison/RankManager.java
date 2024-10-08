package ksu.minecraft.prison;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
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
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null ? user.getPrimaryGroup() : null;
    }

    public void rankUp(Player player) {
        String currentRank = getCurrentRank(player);
        if (currentRank == null) {
            player.sendMessage(Component.text("Could not determine your current rank!"));
            return;
        }

        String nextRank = plugin.getConfig().getString("ranks." + currentRank + ".next_rank");
        int price = plugin.getConfig().getInt("ranks." + currentRank + ".price");

        if (nextRank == null) {
            player.sendMessage(Component.text("You are at the highest rank!"));
            return;
        }

        if (plugin.getEconomyManager().canAfford(player, price)) {
            plugin.getEconomyManager().deductMoney(player, price);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user " + player.getName() + " promote ranks");
            player.sendMessage(Component.text("You ranked up to " + nextRank + "!"));
        } else {
            player.sendMessage(Component.text("You need " + price + " to rank up!"));
        }
    }
}
