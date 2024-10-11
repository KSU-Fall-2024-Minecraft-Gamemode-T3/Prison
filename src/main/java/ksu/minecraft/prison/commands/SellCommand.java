package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

public class SellCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SellCommand(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            openSellMenu(player);

            return true;
        }
        return false;
    }

    public void openSellMenu(Player player) {
        // TODO Make sure this is a inventory holder
        Inventory sellMenu = Bukkit.createInventory(null, 27, Component.text("Sell Items"));

        int slot = 0;
        for (String itemName : plugin.getConfig().getConfigurationSection("sellable-items").getKeys(false)) {
            Material material = Material.getMaterial(itemName);

            if (material != null) {
                double price = plugin.getConfig().getDouble("sellable-items." + itemName + ".price");

                ItemStack displayItem = new ItemStack(material);
                ItemMeta meta = displayItem.getItemMeta();
                meta.displayName(Component.text(material.name() + " - $" + price));
                displayItem.setItemMeta(meta);
                sellMenu.setItem(slot, displayItem);
                slot++;
            }
        }
        player.openInventory(sellMenu);
    }
}
