package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Brewing extends InvType {

    Brewing() {
        super("BREWING", 5, linear(5, 5));
        role(SlotRole.BOTTLE, 0, 1, 2);
        role(SlotRole.INGREDIENT, 3);
        role(SlotRole.FUEL, 4);
    }
}
