package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Stonecutter extends InvType {

    Stonecutter() {
        super("STONECUTTER", 2, linear(2, 2));
        role(SlotRole.INPUT, 0);
        role(SlotRole.OUTPUT, 1);
    }
}
