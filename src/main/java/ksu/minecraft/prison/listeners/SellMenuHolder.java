// SellMenuHolder.java
package ksu.minecraft.prison.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class SellMenuHolder implements InventoryHolder {
    public Inventory inventory;

    public void createInventory(int size, String titleMsg){
        Component title = MiniMessage.miniMessage().deserialize(titleMsg);
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public @NotNull Inventory getInventory(){
        return this.inventory;
    }

    public void setInventory(Inventory inventory){
        this.inventory = inventory;
    }

    public abstract void onClick(InventoryClickEvent event);

}
