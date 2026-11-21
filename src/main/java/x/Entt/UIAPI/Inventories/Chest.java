package x.Entt.UIAPI.Inventories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class Chest extends InvType {

    Chest() {
        super("CHEST", 54, null);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public int maxRows() {
        return 6;
    }

    @Override
    public int maxColumns() {
        return 9;
    }

    @Override
    public int size(int layoutRows) {
        return layoutRows * 9;
    }

    @Override
    public int physicalSlot(int row, int column) {
        if (row < 0 || column < 0 || row >= 6 || column >= 9) {
            return -1;
        }
        return row * 9 + column;
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, title);
    }
}
