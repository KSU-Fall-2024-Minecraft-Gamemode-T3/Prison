package ksu.minecraft.prison.listeners;

import ksu.minecraft.prison.managers.Mine;
import ksu.minecraft.prison.managers.MineManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MineBlockListener implements Listener {
    private final MineManager mineManager;

    public MineBlockListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Mine mine = mineManager.getMineByLocation(block.getLocation());
        if (mine != null) {
            mine.incrementBlocksMined();
            //Bukkit.getLogger().info("Block mined in mine '" + mine.getName() + "'. blocksMined: " + mine.getBlocksMined());
            mineManager.saveMinesConfig(); // Save after updating blocksMined
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Mine mine = mineManager.getMineByLocation(block.getLocation());
        if (mine != null) {
            mine.decrementBlocksMined();
            //Bukkit.getLogger().info("Block placed in mine '" + mine.getName() + "'. blocksMined: " + mine.getBlocksMined());
            mineManager.saveMinesConfig(); // Save after updating blocksMined
        }
    }
}
