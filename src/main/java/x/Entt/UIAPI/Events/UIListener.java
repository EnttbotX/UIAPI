package x.Entt.UIAPI.Events;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.logging.Level;

import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.GuiManager;
import x.Entt.UIAPI.GUI.UIHolder;
import x.Entt.UIAPI.UIAPI;

public class UIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        GuiInstance instance = find(event.getWhoClicked(), event.getInventory());
        if (instance == null) {
            return;
        }
        try {
            instance.handleClick(event);
        } catch (Throwable ex) {
            event.setCancelled(true);
            UIAPI.getInstance().getLogger().log(Level.SEVERE, "Error handling a UI click", ex);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        GuiInstance instance = find(event.getWhoClicked(), event.getInventory());
        if (instance == null) {
            return;
        }
        try {
            instance.handleDrag(event);
        } catch (Throwable ex) {
            event.setCancelled(true);
            UIAPI.getInstance().getLogger().log(Level.SEVERE, "Error handling a UI drag", ex);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        GuiInstance instance = find(event.getPlayer(), event.getInventory());
        if (instance == null) {
            return;
        }
        try {
            instance.handleClose();
        } catch (Throwable ex) {
            UIAPI.getInstance().getLogger().log(Level.SEVERE, "Error closing a UI", ex);
        }
    }

    private GuiInstance find(HumanEntity human, Inventory inventory) {
        GuiInstance instance = GuiManager.get(human.getUniqueId());
        if (instance != null && instance.owns(inventory)) {
            return instance;
        }
        if (inventory.getHolder() instanceof UIHolder holder) {
            return holder.getInstance();
        }
        return null;
    }
}
