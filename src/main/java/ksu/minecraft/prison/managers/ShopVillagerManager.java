package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

public class ShopVillagerManager {

    private final Prison plugin;

    public ShopVillagerManager(Prison plugin) {
        this.plugin = plugin;
    }

    public void spawnShopVillagers() {
        World world = Bukkit.getWorld("world"); // Change world name if different
        if (world == null) return;

        double[][] coords = {
                {190.5, 117, -274.5}, {228.5, 117, -318.5}, {216.5, 117, -213.5},
                {192.5, 117, -238.5}, {262.5, 108, -236.5}, {174.5, 104, -243.5},
                {278.5, 117, -282.5}
        };

        for (double[] coord : coords) {
            Location location = new Location(world, coord[0], coord[1], coord[2]);
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            villager.setAI(false); // Disable movement
            villager.setCustomName("Shop");
            villager.setCustomNameVisible(true);
            villager.getPersistentDataContainer().set(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE, (byte) 1);
        }
    }

    //Edited
    @EventHandler
    //TEST attempt at changing PlayerInteractAtEntityEvent to PlayerInteractEntityEvent
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        //Check to see if the entity clicked has the shop key i.e. a villager. Could be a point of issue if the data types aren't right for some reason
        if (event.getRightClicked().getPersistentDataContainer().has(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE)) {

            //event.setCancelled(true);

            //trying to open new sell menus found in Menus.java
            plugin.getMenus().openSellMenu(event.getPlayer());

            //event.getPlayer().openInventory(sellMenu);
        }
    }

    //Makes the Villagers invincible since it would be bad to kill the only shop in a mine
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        event.setCancelled(true);
    }
}
