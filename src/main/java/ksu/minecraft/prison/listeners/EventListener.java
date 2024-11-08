package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.Prison;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import net.luckperms.api.model.user.User;


public class EventListener implements Listener {
    /*
    ---------------------------------------
            Common Player Listeners
    ---------------------------------------
     */


    private final Prison plugin;
    private final World world;
    public EventListener(Prison plugin) {
        this.plugin = plugin;
        this.world = Prison.world;
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
                        player.sendMessage("Hello!");
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

                int slot = event.getSlot();
                switch (slot) {
                    case 1:
                        player.performCommand("warp D");
                        break;
                    case 2:
                        player.performCommand("warp C");
                        break;
                    case 3:
                        player.performCommand("warp B");
                        break;
                    case 4:
                        player.performCommand("warp A");
                        break;
                    case 5:
                        player.performCommand("warp K");
                        break;
                    case 6:
                        player.performCommand("warp S");
                        break;
                    case 7:
                        player.performCommand("warp U");
                        break;
                    case 10:
                        player.performCommand("warp spawn");
                        break;
                    case 11:
                        player.performCommand("warp info");
                        break;
                    case 12:
                        player.performCommand("warp rules");
                        break;
                    case 13:
                        player.performCommand("warp cells");
                        break;
                    default:
                        //player.sendMessage("Sorry, that warp does not exist!");
                        break;
                }
            }
        }


    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //Give all players the prison menu (compass)
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
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        //player.sendMessage(Component.text("User is ") + user.getUsername());

        if (user != null) {

            //If check is successful then take their primary group (rank) and place into string
            String primaryGroup = user.getPrimaryGroup();

            if (primaryGroup != null && primaryGroup.length() == 1) {
                return primaryGroup.toUpperCase();
            }else if(primaryGroup.equals("default")){
                //Luckperms uses default as the first rank, better to change it to D
                return "D";
            }

        }
        return null;
    }

}