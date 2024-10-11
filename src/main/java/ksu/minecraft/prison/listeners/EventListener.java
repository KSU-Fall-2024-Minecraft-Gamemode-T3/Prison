package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

    private final Prison plugin;

    public EventListener(Prison plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack compass = new ItemStack(Material.COMPASS);
        compass.editMeta(meta -> meta.displayName(Component.text("Prison Menu")));
        player.getInventory().setItem(8, compass); // Last hotbar slot
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.COMPASS) {
            if (item.getItemMeta().displayName().equals(Component.text("Prison Menu"))) {
                plugin.getMenus().openPrisonMenu(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (droppedItem.getType() == Material.COMPASS && droppedItem.getItemMeta() != null) {
            if (droppedItem.getItemMeta().displayName().equals(Component.text("Prison Menu"))) {
                event.setCancelled(true); // Cancel the drop
                event.getPlayer().sendMessage(Component.text("You cannot drop the Prison Menu compass!"));
            }
        }
    }
}
