package x.Entt.UIAPI.Inventories;

import x.Entt.UIAPI.GUI.SlotRole;

public final class Workbench extends InvType {

    Workbench() {
        super("WORKBENCH", 10, new int[][]{
                {1, 2, 3, -1, -1},
                {4, 5, 6, -1, 0},
                {7, 8, 9, -1, -1}
        });
        role(SlotRole.OUTPUT, 0);
        role(SlotRole.INGREDIENT, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }
}
