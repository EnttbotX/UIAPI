package x.Entt.UIAPI.GUI;

import x.Entt.UIAPI.GUI.Builders.BaseBuilder;
import x.Entt.UIAPI.Inventories.InvType;

public class GuiBuilder extends BaseBuilder<GuiBuilder> {

    public GuiBuilder(String title) {
        super(title, InvType.CHEST);
    }

    public GuiBuilder(String title, InvType type) {
        super(title, type);
    }

    @Override
    protected GuiBuilder self() {
        return this;
    }
}