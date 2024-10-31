package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getPersistentDataContainer().has(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            Inventory sellMenu = Bukkit.createInventory(null, 27, "Sell Items");
            // Populate sell menu items based on configuration
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            diamond.getItemMeta().setDisplayName("Diamond\n$50");
            sellMenu.addItem(diamond);
            event.getPlayer().openInventory(sellMenu);
        }
    }
}
