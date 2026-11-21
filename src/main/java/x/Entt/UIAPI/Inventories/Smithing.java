package x.Entt.UIAPI.Inventories;

import org.bukkit.event.inventory.InventoryType;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Smithing extends InvType {

    Smithing() {
        this(sizeOnServer());
    }

    private Smithing(int size) {
        super("SMITHING", size, linear(size, size));
        if (size >= 4) {
            role(SlotRole.TEMPLATE, 0);
            role(SlotRole.INPUT, 1);
            role(SlotRole.SECONDARY_INPUT, 2);
            role(SlotRole.OUTPUT, 3);
        } else {
            role(SlotRole.INPUT, 0);
            role(SlotRole.SECONDARY_INPUT, 1);
            role(SlotRole.OUTPUT, 2);
        }
    }

    private static int sizeOnServer() {
        InventoryType type = lookup("SMITHING");
        return type != null ? type.getDefaultSize() : 4;
    }
}
