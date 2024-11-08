package ksu.minecraft.prison;

import ksu.minecraft.prison.listeners.SellMenuHolder;
import ksu.minecraft.prison.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.Arrays;
import net.kyori.adventure.text.format.TextDecoration;
import java.text.DecimalFormat;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Menus {

    private final Prison plugin;
    private final EconomyManager economyManager;

    public Menus(Prison plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public void openPrisonMenu(Player player) {
        Inventory prisonMenu = Bukkit.createInventory(null, 27, Component.text("Prison Menu"));

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();

        if (headMeta != null) {
            // Set display name to the player's username, non-italicized
            headMeta.displayName(Component.text(player.getName(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

            // Set the head texture to the current player
            headMeta.setOwningPlayer(player);

            // Retrieve player stats (replace these with actual methods to get the values)
            double money = plugin.getEconomyManager().getBalance(player); // Assuming you have an economy manager
            int playtimeMinutes = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 1200; // Converts ticks to minutes
            int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);

            // Format money with commas
            DecimalFormat moneyFormat = new DecimalFormat("#,###.00");

            // Convert playtime from minutes to days, hours, and minutes
            int days = playtimeMinutes / 1440;
            int hours = (playtimeMinutes % 1440) / 60;
            int minutes = playtimeMinutes % 60;

            // Set lore with stats, formatted in white text, non-italicized
            headMeta.lore(Arrays.asList(
                    Component.text("Money: $" + moneyFormat.format(money), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                    Component.text("Time: " + days + "d " + hours + "h " + minutes + "m", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                    Component.text("Deaths: " + deaths, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
            ));

            // Apply the meta to the player head item
            playerHead.setItemMeta(headMeta);
        }

// Place the player's head in the menu at slot 0
        prisonMenu.setItem(13, playerHead);


        ItemStack iron_bars = new ItemStack(Material.IRON_BARS);
        ItemMeta iron_barsMeta = iron_bars.getItemMeta();
        iron_barsMeta.displayName(Component.text("Cells"));
        iron_bars.setItemMeta(iron_barsMeta);
        prisonMenu.setItem(26, iron_bars);

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.displayName(Component.text("Teleport to Rank"));
        pickaxe.setItemMeta(pickaxeMeta);
        prisonMenu.setItem(11, pickaxe);

        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.displayName(Component.text("Warps"));
        chest.setItemMeta(chestMeta);
        prisonMenu.setItem(15, chest);

        player.openInventory(prisonMenu);
    }

    public void openPrisonRanksMenu(Player player) {
        // Limit the menu to 9 slots (1 row)
        //TODO COME BACK TO
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
        Inventory warpsMenu = Bukkit.createInventory(null, 18, Component.text("Warps Menu"));

        // First Line
        // Slot 0: Glass Pane named "Mines"
        ItemStack minesPane = new ItemStack(Material.GLASS_PANE);
        ItemMeta minesMeta = minesPane.getItemMeta();
        minesMeta.displayName(Component.text("Mines").decoration(TextDecoration.ITALIC, false));
        minesPane.setItemMeta(minesMeta);
        warpsMenu.setItem(0, minesPane);

        // Slot 1: D - Coal
        ItemStack dItem = new ItemStack(Material.COAL);
        ItemMeta dMeta = dItem.getItemMeta();
        dMeta.displayName(Component.text("Warp to D").decoration(TextDecoration.ITALIC, false));
        dItem.setItemMeta(dMeta);
        warpsMenu.setItem(1, dItem);

        // Slot 2: C - Lapis Lazuli
        ItemStack cItem = new ItemStack(Material.LAPIS_LAZULI);
        ItemMeta cMeta = cItem.getItemMeta();
        cMeta.displayName(Component.text("Warp to C").decoration(TextDecoration.ITALIC, false));
        cItem.setItemMeta(cMeta);
        warpsMenu.setItem(2, cItem);

        // Slot 3: B - Gold Nugget
        ItemStack bItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta bMeta = bItem.getItemMeta();
        bMeta.displayName(Component.text("Warp to B").decoration(TextDecoration.ITALIC, false));
        bItem.setItemMeta(bMeta);
        warpsMenu.setItem(3, bItem);

        // Slot 4: A - Redstone Dust
        ItemStack aItem = new ItemStack(Material.REDSTONE);
        ItemMeta aMeta = aItem.getItemMeta();
        aMeta.displayName(Component.text("Warp to A").decoration(TextDecoration.ITALIC, false));
        aItem.setItemMeta(aMeta);
        warpsMenu.setItem(4, aItem);

        // Slot 5: K - Diamond
        ItemStack kItem = new ItemStack(Material.DIAMOND);
        ItemMeta kMeta = kItem.getItemMeta();
        kMeta.displayName(Component.text("Warp to K").decoration(TextDecoration.ITALIC, false));
        kItem.setItemMeta(kMeta);
        warpsMenu.setItem(5, kItem);

        // Slot 6: S - Emerald
        ItemStack sItem = new ItemStack(Material.EMERALD);
        ItemMeta sMeta = sItem.getItemMeta();
        sMeta.displayName(Component.text("Warp to S").decoration(TextDecoration.ITALIC, false));
        sItem.setItemMeta(sMeta);
        warpsMenu.setItem(6, sItem);

        // Slot 7: U - Snowball
        ItemStack uItem = new ItemStack(Material.SNOWBALL);
        ItemMeta uMeta = uItem.getItemMeta();
        uMeta.displayName(Component.text("Warp to U").decoration(TextDecoration.ITALIC, false));
        uItem.setItemMeta(uMeta);
        warpsMenu.setItem(7, uItem);

        // Second Line
        // Slot 9: Glass Pane named "Utilities"
        ItemStack utilitiesPane = new ItemStack(Material.GLASS_PANE);
        ItemMeta utilitiesMeta = utilitiesPane.getItemMeta();
        utilitiesMeta.displayName(Component.text("Utilities").decoration(TextDecoration.ITALIC, false));
        utilitiesPane.setItemMeta(utilitiesMeta);
        warpsMenu.setItem(9, utilitiesPane);

        // Slot 10: Spawn - Ender Chest
        ItemStack spawnItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta spawnMeta = spawnItem.getItemMeta();
        spawnMeta.displayName(Component.text("Warp to Spawn").decoration(TextDecoration.ITALIC, false));
        spawnItem.setItemMeta(spawnMeta);
        warpsMenu.setItem(10, spawnItem);

        // Slot 11: Info - Book
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(Component.text("Warp to Info").decoration(TextDecoration.ITALIC, false));
        infoItem.setItemMeta(infoMeta);
        warpsMenu.setItem(11, infoItem);

        // Slot 12: Rules - Oak Sign
        ItemStack rulesItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta rulesMeta = rulesItem.getItemMeta();
        rulesMeta.displayName(Component.text("Warp to Rules").decoration(TextDecoration.ITALIC, false));
        rulesItem.setItemMeta(rulesMeta);
        warpsMenu.setItem(12, rulesItem);

        // Slot 13: Cells - Iron Bars
        ItemStack cellsItem = new ItemStack(Material.IRON_BARS);
        ItemMeta cellsMeta = cellsItem.getItemMeta();
        cellsMeta.displayName(Component.text("Warp to Cells").decoration(TextDecoration.ITALIC, false));
        cellsItem.setItemMeta(cellsMeta);
        warpsMenu.setItem(13, cellsItem);

        player.openInventory(warpsMenu);
    }

    public void openSellMenu(Player player) {
        SellMenuHolder sellM = new SellMenuHolder() {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    Material material = clickedItem.getType();
                    if (plugin.getConfig().contains("sellable-items." + material.name())) {
                        double price = plugin.getConfig().getDouble("sellable-items." + material.name() + ".price");
                        if (player.getInventory().contains(material)) {
                            player.getInventory().removeItem(new ItemStack(material, 1));
                            economyManager.depositMoney(player, price);

                            // Convert item name to Pascal case
                            String pascalCaseName = Arrays.stream(material.name().toLowerCase().split("_"))
                                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                    .collect(Collectors.joining(" "));

                            // Send the message after selling
                            player.sendMessage(Component.text("Sold " + pascalCaseName + " for ")
                                    .append(Component.text("$" + String.format("%.2f", price), NamedTextColor.GREEN))
                                    .decoration(TextDecoration.ITALIC, false));
                        } else {
                            player.sendMessage("You don't have any " + material.name() + " to sell.");
                        }
                    }
                }
            }
        };

        sellM.createInventory(27, "Sell Items");

        int slot = 0;
        for (String itemName : plugin.getConfig().getConfigurationSection("sellable-items").getKeys(false)) {
            Material material = Material.getMaterial(itemName);

            if (material != null) {
                double price = plugin.getConfig().getDouble("sellable-items." + itemName + ".price");

                ItemStack displayItem = new ItemStack(material);
                ItemMeta meta = displayItem.getItemMeta();

                // Convert item name to Pascal case
                String pascalCaseName = Arrays.stream(material.name().toLowerCase().split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" "));

                // Set display name with Pascal case item name and price as lore in green, non-italicized
                meta.displayName(Component.text(pascalCaseName).decoration(TextDecoration.ITALIC, false));
                meta.lore(Collections.singletonList(
                        Component.text("$" + String.format("%.2f", price), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                ));

                displayItem.setItemMeta(meta);
                sellM.inventory.setItem(slot, displayItem);
                slot++;
            }
        }

        player.openInventory(sellM.inventory);
    }

}
