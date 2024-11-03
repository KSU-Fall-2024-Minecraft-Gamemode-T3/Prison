package ksu.minecraft.prison.commands;

import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MinesCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final MineManager mineManager;



    public MinesCommand(JavaPlugin plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        File dir = plugin.getDataFolder();
        File mineFile = new File(dir, "mines.yml");

        Player player = (Player) sender;
        FileConfiguration config = YamlConfiguration.loadConfiguration(mineFile);


        if (sender instanceof Player) {

            //Test Command
            // player.sendMessage("This command can only be run by players.");
            if (args.length == 0) {
                //FileConfiguration minesConfig = plugin.getConfig();
                if (config.contains("mines")) {
                    player.sendMessage(Component.text("Mines:"));
                    for (String mineName : config.getConfigurationSection("mines").getKeys(false)) {
                        String name = config.getString("mines." + mineName + ".name", "Unnamed Mine");
                        player.sendMessage(Component.text("Mine: " + name));
                    }
                } else {
                    //Message that should only occur if the mines.yml file is not discovered in the plugins/Prison folder
                    player.sendMessage(Component.text("No mines found in configuration."));
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
                player.sendMessage(Component.text("Use /minereset <minename> to reset a mine manually."));
            }

            return true;
        }

        return false;
    }
}
