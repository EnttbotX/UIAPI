package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Loom extends InvType {

    Loom() {
        super("LOOM", 4, linear(4, 4));
        role(SlotRole.INPUT, 0);
        role(SlotRole.SECONDARY_INPUT, 1);
        role(SlotRole.TERTIARY_INPUT, 2);
        role(SlotRole.OUTPUT, 3);
    }
}
