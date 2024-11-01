package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class EventListener implements Listener {

    private final Prison plugin;

    public EventListener(Prison plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text("Prison Menu"))) {
            event.setCancelled(true);  // Prevent moving items in the Prison Menu

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = MiniMessage.miniMessage().serialize(clickedItem.getItemMeta().displayName());

                switch (itemName) {
                    case "Teleport to Spawn":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp spawn");
                        break;
                    case "Teleport to Rank":
                        player.closeInventory();
                        String rankWarp = getPlayerRankWarp(player);
                        if (rankWarp != null) {
                            plugin.getServer().dispatchCommand(player, "warp " + rankWarp);
                        } else {
                            player.sendMessage("Rank warp not found.");
                        }
                        break;
                    case "Warps":
                        plugin.getMenus().openWarpsMenu(player);
                        break;
                    default:
                        player.sendMessage("Warp does not exist.");
                }
            }
        }

        //Check for warp menu
        if(event.getView().title().equals(Component.text("Warps Menu"))){
            event.setCancelled(true);  // Prevent moving items in the warp Menu

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (clickedItem != null && clickedItem.hasItemMeta()){
                String warpName = MiniMessage.miniMessage().serialize(clickedItem.getItemMeta().displayName());

                switch(warpName){
                    case "Warp to D":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp d");
                        break;
                    case "Warp to C":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp c");
                        break;
                    case "Warp to B":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp b");
                        break;
                    case "Warp to A":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp a");
                        break;
                    case "Warp to K":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp k");
                        break;
                    case "Warp to S":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp s");
                        break;
                    case "Warp to U":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp u");
                        break;
                    case "Warp to Cells":
                        player.closeInventory();
                        plugin.getServer().dispatchCommand(player, "warp cells");
                        break;
                    default:
                        player.sendMessage("Sorry, that warp does not exist!");
                }
            }
        }
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

        if (item != null && item.getType() == Material.COMPASS && event.getAction().isRightClick()) {
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
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("You cannot drop the Prison Menu compass!"));
            }
        }
    }

    private String getPlayerRankWarp(Player player) {
        //Looks at the player as a user based on the unique ID of the player
        //Now assign player.getUniqueID() to a UUID object
        UUID userId = player.getUniqueId();
        User user = plugin.getLuckPerms().getUserManager().getUser(userId);

        if (user != null) {

            //If check is successful then take their primary group (rank) and place into string
            String primaryGroup = user.getPrimaryGroup();


            if (primaryGroup != null && primaryGroup.length() == 1) {
                return primaryGroup.toUpperCase();
            }else{ //just in case something weird occured
                return null;
            }
        }else{
            //TEST to see if this section of code is reached

        }
        return null;
    }
}
