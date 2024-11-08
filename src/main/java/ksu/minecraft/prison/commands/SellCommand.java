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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.stream.Collectors;

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
        Inventory sellMenu = Bukkit.createInventory(null, 27, Component.text("Sell Items"));

        int slot = 0;
        for (String itemName : plugin.getConfig().getConfigurationSection("sellable-items").getKeys(false)) {
            Material material = Material.getMaterial(itemName);

            if (material != null) {
                double price = plugin.getConfig().getDouble("sellable-items." + itemName + ".price");

                ItemStack displayItem = new ItemStack(material);
                ItemMeta meta = displayItem.getItemMeta();

                // Convert item name to Pascal case
                String pascalCaseName = Arrays.stream(material.name().toLowerCase().split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" "));

                // Set display name with Pascal case item name and price on second line in green
                meta.displayName(Component.text(pascalCaseName)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("\n$" + String.format("%.2f", price), NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, false)));

                displayItem.setItemMeta(meta);
                sellMenu.setItem(slot, displayItem);
                slot++;
            }
        }
        player.openInventory(sellMenu);
    }
}
