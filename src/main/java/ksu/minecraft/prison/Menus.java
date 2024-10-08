package ksu.minecraft.prison;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.displayName(Component.text("Sell All Items"));
        emerald.setItemMeta(emeraldMeta);
        prisonMenu.setItem(0, emerald);

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
        Inventory ranksMenu = Bukkit.createInventory(null, 18, Component.text("Prison Ranks"));

        char[] ranks = {'D', 'C', 'B', 'A', 'K', 'S', 'U'};
        for (char c : ranks) {
            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.displayName(Component.text(String.valueOf(c)));
            emerald.setItemMeta(emeraldMeta);
            ranksMenu.addItem(emerald);
        }

        player.openInventory(ranksMenu);
    }

    public void showHelpMenu(Player player) {
        String helpMessage = """
            <green>Prison Plugin Help</green>
            <yellow>/prison</yellow> - Opens the main prison menu.
            <yellow>/prison help</yellow> - Shows this help menu.
            <yellow>/ranks</yellow> - Shows rank progression.
            <yellow>/rankup</yellow> - Rank up to the next rank.
            """;
        Component parsedHelpMessage = MiniMessage.miniMessage().deserialize(helpMessage);
        player.sendMessage(parsedHelpMessage);
    }
}
