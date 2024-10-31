// SellMenuHolder.java
package ksu.minecraft.prison.listeners;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SellMenuHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null; // Return null as the inventory will be defined elsewhere
    }
}
