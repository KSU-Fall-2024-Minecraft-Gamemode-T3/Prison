package ksu.minecraft.prison;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

public final class Prison extends JavaPlugin {

    private LuckPerms luckPerms;
    private Economy economy;
    private EconomyManager economyManager;
    private RankManager rankManager;
    private Menus menus;

    @Override
    public void onEnable() {
        getLogger().info("Prison plugin has been enabled!");
        saveDefaultConfig();

        luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        economyManager = new EconomyManager(this);
        rankManager = new RankManager(this, luckPerms);
        menus = new Menus(this);

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Prison plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("prison")) {
                if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                    menus.showHelpMenu(player);
                    return true;
                } else if (args.length > 0 && args[0].equalsIgnoreCase("ranks")) {
                    menus.openPrisonRanksMenu(player);
                    return true;
                } else {
                    menus.openPrisonMenu(player);
                    return true;
                }
            } else if (command.getName().equalsIgnoreCase("rankup")) {
                rankManager.rankUp(player);
                return true;
            }
        }
        return false;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public Menus getMenus() {
        return menus;
    }
}
