package ksu.minecraft.prison;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Prison extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Prison plugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Prison plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("ksu")) {
                String message = getConfig().getString("ksu-command-message");
                Component parsedMessage = MiniMessage.miniMessage().deserialize(message);
                player.sendMessage(parsedMessage); //Test command
                return true;
            } else if (command.getName().equalsIgnoreCase("prison")) {
                if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                    showHelpMenu(player);
                    return true;  // Help is currently broken
                } else if (args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
                    openPrisonRanksMenu(player);
                    return true;
                } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                    reloadPlugin(player); //Does reload really work?
                    return true;
                } else {
                    openPrisonMenu(player);
                    return true;
                }
            }
        }
        return false;
    }

    private void reloadPlugin(Player player) {
        // Disable the plugin
        Bukkit.getPluginManager().disablePlugin(this);

        // Re-enable the plugin
        Bukkit.getPluginManager().enablePlugin(this);

        // Notify the the plugin has been reloaded
        player.sendMessage(Component.text("Prison plugin reloaded!"));
    }


    private void openPrisonMenu(Player player) {
        Inventory prisonMenu = Bukkit.createInventory(null, 9, Component.text("Prison Menu"));

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.displayName(Component.text("Sell All Items"));
        emerald.setItemMeta(emeraldMeta); // vault eco and luckyperms
        prisonMenu.setItem(0, emerald);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.displayName(Component.text("Teleport to Spawn"));
        compass.setItemMeta(compassMeta); // currently warp spawn
        prisonMenu.setItem(4, compass);

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.displayName(Component.text("Teleport to Rank"));
        pickaxe.setItemMeta(pickaxeMeta); //teleports to nothing
        prisonMenu.setItem(3, pickaxe);

        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.displayName(Component.text("Warps"));
        chest.setItemMeta(chestMeta);
        prisonMenu.setItem(5, chest);



        player.openInventory(prisonMenu);
    }

    private void openPrisonRanksMenu(Player player) {
        Inventory ranksMenu = Bukkit.createInventory(null, 18, Component.text("Prison Ranks"));

        char[] ranks = {'D', 'C', 'B', 'A', 'K', 'S', 'U'};
        for (char c : ranks) {
            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.displayName(Component.text(String.valueOf(c)));
            emerald.setItemMeta(emeraldMeta); // in front of gates
            ranksMenu.addItem(emerald);
        }

        // Adding warps for cells, info, and rules as books
        ItemStack cellsBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta cellsBookMeta = cellsBook.getItemMeta();
        cellsBookMeta.displayName(Component.text("Cells"));
        cellsBook.setItemMeta(cellsBookMeta);
        ranksMenu.setItem(9, cellsBook);

        ItemStack infoBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta infoBookMeta = infoBook.getItemMeta();
        infoBookMeta.displayName(Component.text("Info"));
        infoBook.setItemMeta(infoBookMeta);
        ranksMenu.setItem(10, infoBook);

        ItemStack rulesBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta rulesBookMeta = rulesBook.getItemMeta();
        rulesBookMeta.displayName(Component.text("Rules"));
        rulesBook.setItemMeta(rulesBookMeta);
        ranksMenu.setItem(11, rulesBook);

        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.displayName(Component.text("Back to Prison Menu"));
        netherStar.setItemMeta(netherStarMeta);
        ranksMenu.setItem(17, netherStar);

        player.openInventory(ranksMenu);
    }

    private void showHelpMenu(Player player) {
        String helpMessage = getConfig().getString("messages.help-menu-message");
        Component parsedHelpMessage = MiniMessage.miniMessage().deserialize(helpMessage);
        player.sendMessage(parsedHelpMessage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (title.equals(Component.text("Prison Menu"))) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = MiniMessage.miniMessage().serialize(clickedItem.getItemMeta().displayName());

                switch (itemName) {
                    case "Sell All Items":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sellall " + player.getName());
                        player.sendMessage(Component.text("Sold all items!")); //does this work? define prices
                        break;
                    case "Teleport to Spawn":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp spawn " + player.getName());
                        player.sendMessage(Component.text("Teleported to spawn!")); //need to change to /spawn?
                        break;
                    case "Teleport to Rank":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp rank " + player.getName());
                        player.sendMessage(Component.text("Teleported to your rank!")); //need to get player rank
                        break;
                    case "Warps":
                        openPrisonRanksMenu(player);
                        break;
                    case "Cells":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp cells " + player.getName());
                        player.sendMessage(Component.text("Warped to Cells!"));
                        break;
                    case "Info":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp info " + player.getName());
                        player.sendMessage(Component.text("Warped to Info!"));
                        break;
                    case "Rules":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp rules " + player.getName());
                        player.sendMessage(Component.text("Warped to Rules!"));
                        break;
                    default:
                        break;
                }
            }
        } else if (title.equals(Component.text("Prison Warps"))) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = MiniMessage.miniMessage().serialize(clickedItem.getItemMeta().displayName());

                if (itemName.equals("Back to Prison Menu")) {
                    openPrisonMenu(player);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp " + itemName.toLowerCase() + " " + player.getName());
                    player.sendMessage(Component.text("Warped to " + itemName + "!"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Give the player a compass in the last hotbar slot
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.displayName(Component.text("Prison Menu"));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(8, compass); // Last hotbar slot (index 8)

        // Give the player the prefix '[D]' on first join
        //if (!player.hasPlayedBefore()) {
        //    player.setDisplayName("[D] " + player.getName());
        //} //now handled with luckyperms
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Open the Prison menu when the player right-clicks with the compass
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand(); //cannot step on pressure plates

        if (item != null && item.getType() == Material.COMPASS && item.getItemMeta() != null) {
            if (item.getItemMeta().displayName().equals(Component.text("Prison Menu"))) {
                openPrisonMenu(player);
                event.setCancelled(true); // Prevent other actions
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // Prevent dropping the compass
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (droppedItem.getType() == Material.COMPASS && droppedItem.getItemMeta() != null) {
            if (droppedItem.getItemMeta().displayName().equals(Component.text("Prison Menu"))) {
                event.setCancelled(true); // Cancel the drop
                event.getPlayer().sendMessage(Component.text("You cannot drop the Prison Menu compass!"));
            }
        }
    }
}
