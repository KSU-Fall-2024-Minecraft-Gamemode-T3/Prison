// SellMenuListener.java
package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;  // Adjust this import to match your main plugin class package
import ksu.minecraft.prison.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

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
    /*
    private final Prison plugin;


    public SellMenuListener(Prison plugin, EconomyManager economyManager) {
        this.plugin = plugin;
    }


     // Creates a Sell Menu inventory with a custom InventoryHolder

    public Inventory createSellMenu() {
        Inventory sellInventory = Bukkit.createInventory(new SellMenuHolder(), 27, Component.text("Sell Items"));

        // Create an example item with PDC tag
        ItemStack sellableItem = new ItemStack(Material.DIAMOND);
        ItemMeta meta = sellableItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Sellable Diamond"));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "sell_item"), PersistentDataType.STRING, "true");
            sellableItem.setItemMeta(meta);
        }

        sellInventory.setItem(13, sellableItem); // Add item to the middle slot
        return sellInventory;
    }


     //Opens the Sell Menu

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof SellMenuHolder) {
            event.getPlayer().sendMessage(Component.text("Welcome to the Sell Menu!"));
        }
    }


     //Handles clicks within the Sell Menu

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory is the Sell Menu by checking the InventoryHolder
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof SellMenuHolder)) {
            return;
        }

        event.setCancelled(true); // Prevent default behavior in this custom inventory

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "sell_item"), PersistentDataType.STRING)) {
            // Custom logic for selling the item
            event.getWhoClicked().sendMessage(Component.text("You have successfully sold the item!"));
            event.getWhoClicked().getInventory().removeItem(clickedItem); // Remove item from player’s inventory
        } else {
            event.getWhoClicked().sendMessage(Component.text("This item cannot be sold."));
        }
    }


     //Handles Sell Menu close event

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SellMenuHolder) {
            event.getPlayer().sendMessage(Component.text("Thank you for using the Sell Menu!"));
        }
    }

     */
}
