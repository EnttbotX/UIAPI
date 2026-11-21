package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Cartography extends InvType {

    Cartography() {
        super("CARTOGRAPHY", 3, linear(3, 3));
        role(SlotRole.INPUT, 0);
        role(SlotRole.SECONDARY_INPUT, 1);
        role(SlotRole.OUTPUT, 2);
    }
}
