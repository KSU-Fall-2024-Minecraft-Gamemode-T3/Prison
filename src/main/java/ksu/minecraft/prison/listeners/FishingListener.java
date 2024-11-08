package ksu.minecraft.prison.listeners;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingListener implements Listener {

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // Check if the player actually caught something
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() != null) {
            // Ensure the caught entity is an item
            if (event.getCaught() instanceof Item) {
                // Set the caught item to raw cod
                Item caught = (Item) event.getCaught();
                caught.setItemStack(new ItemStack(Material.COD, 1));
            }
        }
    }
}
