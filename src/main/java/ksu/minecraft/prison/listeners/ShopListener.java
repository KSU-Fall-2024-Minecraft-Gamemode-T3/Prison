package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class ShopListener implements Listener {

    private final Prison plugin;

    public ShopListener(Prison plugin){
        this.plugin = plugin;
    }

    @EventHandler

    public void onPlayerInteract(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();
        //Check to see if the entity clicked has the shop key i.e. a villager. Could be a point of issue if the data types aren't right for some reason
        if (event.getRightClicked().getPersistentDataContainer().has(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE)) {

            //event.setCancelled(true);

            //trying to open new sell menus found in Menus.java

            plugin.getMenus().openSellMenu(player);

            //event.getPlayer().openInventory(sellMenu);
        }else{
            //This is an error message that should let you know if something is wrong with how
            //the villager is not assigned the 'is_shop' tag for some reason, shouldn't happen.
            //player.sendMessage(Component.text("Entity is not recognized as 'is_shop'"));
        }
    }

    //Makes the Villagers invincible since it would be bad to kill the only shop in a mine
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getEntity().getPersistentDataContainer().has(plugin.getNamespacedKey("is_shop"), PersistentDataType.BYTE)){
            event.setCancelled(true);
        }
    }
}
