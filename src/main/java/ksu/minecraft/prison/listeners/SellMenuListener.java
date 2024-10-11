package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.managers.EconomyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

public class SellMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    public SellMenuListener(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory is the sell menu
        // TODO confirm this is a inventory holder
        if (event.getView().title().equals(Component.text("Sell Items"))) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                Material material = clickedItem.getType();

                if (plugin.getConfig().contains("sellable-items." + material.name())) {
                    double price = plugin.getConfig().getDouble("sellable-items." + material.name() + ".price");

                    if (player.getInventory().contains(material)) {
                        player.getInventory().removeItem(new ItemStack(material, 1));
                        economyManager.depositMoney(player, price);
                        player.sendMessage("You sold 1 " + material.name() + " for $" + price);
                    } else {
                        player.sendMessage("You don't have any " + material.name() + " to sell.");
                    }
                }
            }
        }
    }
}
