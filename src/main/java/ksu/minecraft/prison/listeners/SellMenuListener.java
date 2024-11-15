// SellMenuListener.java
package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;  // Adjust this import to match your main plugin class package
import ksu.minecraft.prison.managers.EconomyManager;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;


import org.bukkit.entity.Player;
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
        //Check if the player is interacting with a menu that is not the custom menu, if so do nothing.
        if(!(event.getInventory().getHolder(false) instanceof SellMenuHolder inv)){
            return;
        }

        //Check if the player is clicking in the custom menu, if not, prevent them from using
        //shift clicking or pressing a number in the custom menu to stop an issue where a player
        //will put their items into the shop menu and be unable to get it back
        if(event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER ){
            if(!(event.isShiftClick() || event.getClick() == ClickType.NUMBER_KEY)){
                return;
            }
        }

        //Check that allows the player to move items in their inventory while the custom menu is open,
        if(event.getCurrentItem() == null  && event.getCursor().getType() == Material.AIR){
            return;
        }

        inv.onClick(event);


    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!(event.getInventory().getHolder(false) instanceof SellMenuHolder)) {
            //player.sendMessage(Component.text("Greetings from message 4!"));
            return;
        }
        event.setCancelled(true);
    }


}
