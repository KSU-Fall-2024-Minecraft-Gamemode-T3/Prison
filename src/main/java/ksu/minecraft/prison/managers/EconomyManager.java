package ksu.minecraft.prison.managers;

import ksu.minecraft.prison.Prison;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final Prison plugin;
    private Economy economy;

    public EconomyManager(Prison plugin) {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        //Check for if Vault is part of the plugins, if not throw an error
        if (rsp == null) {
            throw new IllegalStateException("Vault not found! Make sure Vault is installed.");
        }
        this.economy = rsp.getProvider();
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        //if Vault was previously missing from the server, disable plugin to prevent hard crash
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found! Disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        //Final check to see if Vault's economy services are unavailable and disables plugin to prevent crash
        if (rsp == null) {
            plugin.getLogger().severe("Economy provider not found! Disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        economy = rsp.getProvider();
    }

    public boolean canAfford(Player player, int price) {
        //check if a player can purchase an item with their current money balance
        return economy.getBalance(player) >= price;
    }

    public void deductMoney(Player player, int price) {
        //take out money in player's balance if they can afford the price
        economy.withdrawPlayer(player, price);
    }
    public void depositMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }
}
