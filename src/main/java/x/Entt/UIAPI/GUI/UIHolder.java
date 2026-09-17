package x.Entt.UIAPI.GUI;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class UIHolder implements InventoryHolder {

    private final GuiInstance instance;

    public UIHolder(GuiInstance instance) {
        this.instance = instance;
    }

    public GuiInstance getInstance() {
        return instance;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return instance.getInventory();
    }
}