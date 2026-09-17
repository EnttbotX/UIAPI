package x.Entt.UIAPI.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.GuiManager;
import x.Entt.UIAPI.GUI.UIHolder;
import x.Entt.UIAPI.Item.Item;

public class UIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof UIHolder holder)) {
            return;
        }
        GuiInstance instance = holder.getInstance();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            event.setCancelled(true);
            return;
        }
        if (clicked.equals(instance.getInventory())) {
            event.setCancelled(true);
            Item item = instance.getItemAt(event.getSlot());
            if (item == null) {
                return;
            }
            item.handleClick((Player) event.getWhoClicked(), event.getClick());
            return;
        }
        if (!instance.getDefinition().isAllowPlayerInventory() || event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof UIHolder holder)) {
            return;
        }
        int topSize = event.getInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
        if (!holder.getInstance().getDefinition().isAllowPlayerInventory()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof UIHolder)) {
            return;
        }
        GuiManager.unregister(event.getPlayer().getUniqueId());
    }
}