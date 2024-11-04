package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;


import java.util.List;

public class ShopVillagerManager {

    //initiates and spawns shop villagers

    private final Prison plugin;
    private static int count = 0;

    public ShopVillagerManager(Prison plugin) {
        this.plugin = plugin;
    }

    public void spawnShopVillagers(World world) {

        if (world == null) return;
        List<Location> villagerSpawnLocations = List.of(
                new Location(world, 190.5, 117, -274.5), //Shop Villager in D
                new Location(world, 228.5, 117, -318.5), //Shop Villager in C - Note: Dupes Spawn
                new Location(world, 216.5, 117, -213.5), //Shop Villager in B
                new Location(world, 192.5, 117, -238.5), //Shop Villager in A
                new Location(world, 262.5, 108, -236.5), //Shop Villager in K - Note: Dupes Spawn
                new Location(world, 174.5, 104, -243.5), //Shop Villager in S
                new Location(world, 278.5, 117, -282.5)  //Shop Villager in U - Note: Dupes Spawn
        );

        for(Location location : villagerSpawnLocations){
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            villager.setAI(false); // Disable movement
            villager.setCustomName("Shop");
            villager.setCustomNameVisible(true);
            villager.getPersistentDataContainer().set(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE, (byte) 1);
        }

        /*double[][] coords = {
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

         */
    }





    //Commented out since there is no listener here

    /*@EventHandler
    //TEST attempt at changing PlayerInteractAtEntityEvent to PlayerInteractEntityEvent
    public void onPlayerInteract(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();
        //Check to see if the entity clicked has the shop key i.e. a villager. Could be a point of issue if the data types aren't right for some reason
        if (event.getRightClicked().getPersistentDataContainer().has(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE)) {

            //event.setCancelled(true);

            //trying to open new sell menus found in Menus.java
            player.sendMessage(Component.text("Entity is recognized as 'is_shop'"));
            plugin.getMenus().openSellMenu(player);

            //event.getPlayer().openInventory(sellMenu);
        }else{
            player.sendMessage(Component.text("Entity is not recognized as 'is_shop'"));
            plugin.getMenus().openSellMenu(player);
        }
    }

    //Makes the Villagers invincible since it would be bad to kill the only shop in a mine
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        event.setCancelled(true);
    }

     */
}
