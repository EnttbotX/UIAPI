package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Beacon extends InvType {

    Beacon() {
        super("BEACON", 1, linear(1, 1));
        role(SlotRole.INPUT, 0);
    }
}
