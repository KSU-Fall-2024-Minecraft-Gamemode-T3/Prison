package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.EconomyManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SellAllCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    public SellAllCommand(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            double totalEarned = 0.0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    Material material = item.getType();
                    int amount = item.getAmount();

                    if (plugin.getConfig().contains("sellable-items." + material.name())) {
                        double price = plugin.getConfig().getDouble("sellable-items." + material.name() + ".price");

                        totalEarned += price * amount;
                        player.getInventory().removeItem(new ItemStack(material, amount));
                    }
                }
            }

            if (totalEarned > 0) {
                economyManager.depositMoney(player, totalEarned);
                player.sendMessage("You earned $" + String.format("%.2f", totalEarned) + " from selling all items!");
            } else {
                player.sendMessage("You have no sellable items in your inventory.");
            }

            return true;
        }
        return false;
    }
}
