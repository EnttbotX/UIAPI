package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Enchanting extends InvType {

    Enchanting() {
        super("ENCHANTING", 2, linear(2, 2));
        role(SlotRole.INPUT, 0);
        role(SlotRole.SECONDARY_INPUT, 1);
    }
}
