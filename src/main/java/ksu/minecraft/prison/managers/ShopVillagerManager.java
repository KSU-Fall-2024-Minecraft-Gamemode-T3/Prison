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

    }

}
