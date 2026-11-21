package x.Entt.UIAPI.Inventories;

import org.bukkit.event.inventory.InventoryType;

final class Generic extends InvType {

    Generic(InventoryType type) {
        super(type.name(), type.getDefaultSize(), linear(type.getDefaultSize(), Math.max(1, Math.min(type.getDefaultSize(), 9))));
    }
}
