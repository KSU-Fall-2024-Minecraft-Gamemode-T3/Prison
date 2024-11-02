package ksu.minecraft.prison;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Menus {

    private final Prison plugin;

    public Menus(Prison plugin) {
        this.plugin = plugin;
    }

    public void openPrisonMenu(Player player) {
        Inventory prisonMenu = Bukkit.createInventory(null, 9, Component.text("Prison Menu"));

        //Commented out emerald since that is kind of unneccesary now
        /*
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.displayName(Component.text("Sell All Items"));
        emerald.setItemMeta(emeraldMeta);
        prisonMenu.setItem(0, emerald);
         */
        //Edited

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.displayName(Component.text("Teleport to Spawn"));
        compass.setItemMeta(compassMeta);
        prisonMenu.setItem(4, compass);

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.displayName(Component.text("Teleport to Rank"));
        pickaxe.setItemMeta(pickaxeMeta);
        prisonMenu.setItem(3, pickaxe);

        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.displayName(Component.text("Warps"));
        chest.setItemMeta(chestMeta);
        prisonMenu.setItem(5, chest);

        player.openInventory(prisonMenu);
    }

    public void openPrisonRanksMenu(Player player) {
        // Limit the menu to 9 slots (1 row)
        Inventory ranksMenu = Bukkit.createInventory(null, 9, Component.text("Prison Ranks"));

        FileConfiguration config = plugin.getConfig();
        int slot = 0;  // Track slots for 1-row inventory
        for (String rank : config.getConfigurationSection("ranks").getKeys(false)) {
            if (slot >= 9) break;  // Prevent more than 9 items from being added to the inventory

            String nextRank = config.getString("ranks." + rank + ".next_rank");
            int price = config.getInt("ranks." + rank + ".price");

            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.displayName(Component.text(rank + " >> " + nextRank + " for $" + price));
            emerald.setItemMeta(emeraldMeta);

            ranksMenu.setItem(slot, emerald);
            slot++;  // Increment slot to fill one row
        }

        player.openInventory(ranksMenu);
    }


    public void openWarpsMenu(Player player) {
        Inventory warpsMenu = Bukkit.createInventory(null, 9, Component.text("Warps Menu"));

        String[] warps = {"D", "C", "B", "A", "K", "S", "U", "Cells"};
        for (String warp : warps) {
            ItemStack warpItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta warpMeta = warpItem.getItemMeta();
            warpMeta.displayName(Component.text("Warp to " + warp));
            warpItem.setItemMeta(warpMeta);
            warpsMenu.addItem(warpItem);
        }

        player.openInventory(warpsMenu);
    }

    public void openSellMenu(Player player){
        //Changed sell menu to be more like the other menus, see Component.text

        /*
        Inventory sellMenu = Bukkit.createInventory(null, 27, Component.text("Sell Items"));
        // Populate sell menu items based on configuration
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta =  diamond.getItemMeta();
        diamondMeta.displayName(Component.text("Diamond\n$50"));
        diamond.setItemMeta(diamondMeta);
        sellMenu.addItem(diamond);

        player.openInventory(sellMenu);
         */

        // TODO Make sure this is a inventory holder
        Inventory sellMenu = Bukkit.createInventory(null, 27, Component.text("Sell Items"));

        int slot = 0;
        for (String itemName : plugin.getConfig().getConfigurationSection("sellable-items").getKeys(false)) {
            Material material = Material.getMaterial(itemName);

            if (material != null) {
                double price = plugin.getConfig().getDouble("sellable-items." + itemName + ".price");

                ItemStack displayItem = new ItemStack(material);
                ItemMeta meta = displayItem.getItemMeta();
                meta.displayName(Component.text(material.name() + " - $" + price));
                displayItem.setItemMeta(meta);
                sellMenu.setItem(slot, displayItem);
                slot++;
            }
        }
        player.openInventory(sellMenu);
    }

    public void showHelpMenu(Player player) {
        String helpMessage = """
        <green>Prison Plugin Help</green>
        <yellow><click:run_command:'/prison'>/prison</click></yellow> - Opens the main prison menu.
        <yellow><click:run_command:'/prison help'>/prison help</click></yellow> - Shows this help menu.
        <yellow><click:run_command:'/ranks'>/ranks</click></yellow> - Shows rank progression.
        <yellow><click:run_command:'/rankup'>/rankup</click></yellow> - Rank up to the next rank.
        """;

        Component parsedHelpMessage = MiniMessage.miniMessage().deserialize(helpMessage);
        player.sendMessage(parsedHelpMessage);
    }

}
