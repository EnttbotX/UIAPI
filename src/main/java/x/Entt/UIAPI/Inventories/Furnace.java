package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Furnace extends InvType {

    Furnace(String name) {
        super(name, 3, linear(3, 3));
        role(SlotRole.INPUT, 0);
        role(SlotRole.FUEL, 1);
        role(SlotRole.OUTPUT, 2);
    }
}
