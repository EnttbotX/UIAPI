package x.Entt.UIAPI.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.Layout.LayoutParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GuiBuilder {
    private final String title;
    private GuiLayout layout;
    private final Map<Character, Item> slotItems = new HashMap<>();
    private final Map<Character, List<Item>> pageItems = new HashMap<>();
    private final Map<Character, PageFill> pageFills = new HashMap<>();
    private Predicate<Player> permission;
    private String noPermsMessage;
    private boolean allowPlayerInventory;

    public GuiBuilder(String title) {
        this.title = title;
    }

    public GuiBuilder layout(String layout) {
        this.layout = LayoutParser.parse(layout);
        return this;
    }

    public GuiBuilder slot(char key, Item item) {
        if (slotItems.containsKey(key)) {
            throw new IllegalStateException("UI slot '" + key + "' is already defined.");
        }
        slotItems.put(key, item);
        return this;
    }

    public GuiBuilder page(char key, List<Item> items) {
        return page(key, items, PageFill.LEFT);
    }

    public GuiBuilder page(char key, List<Item> items, PageFill fill) {
        if (pageItems.containsKey(key)) {
            throw new IllegalStateException("UI page '" + key + "' is already defined.");
        }

        pageItems.put(key, items);
        pageFills.put(key, fill == null ? PageFill.LEFT : fill);
        return this;
    }

    public GuiBuilder permission(String permission) {
        this.permission = player -> player.hasPermission(permission);
        return this;
    }

    public GuiBuilder permission(Predicate<Player> permission) {
        this.permission = permission;
        return this;
    }

    public GuiBuilder noPermsMessage(String message) {
        this.noPermsMessage = message;
        return this;
    }

    public GuiBuilder allowPlayerInventory() {
        this.allowPlayerInventory = true;
        return this;
    }

    public GUI build() {
        if (layout == null) {
            throw new IllegalStateException("UI layout is required before calling build().");
        }

        for (char key : slotItems.keySet()) {
            if (!layout.contains(key)) {
                throw new IllegalStateException(
                        "UI slot '" + key + "' is not present in the layout."
                );
            }
        }

        for (char key : pageItems.keySet()) {
            if (!layout.contains(key)) {
                throw new IllegalStateException(
                        "UI page '" + key + "' is not present in the layout."
                );
            }
        }

        for (int row = 0; row < layout.getRows(); row++) {
            for (int column = 0; column < layout.getColumns(); column++) {
                char key = layout.charAt(row, column);

                if (key == ' ') {
                    continue;
                }

                if (!slotItems.containsKey(key) && !pageItems.containsKey(key)) {
                    Bukkit.getLogger().warning(
                            "UI character '" + key + "' is used in the layout but has no slot() or page() defined."
                    );
                }
            }
        }

        return new GUI(
                title,
                layout,
                slotItems,
                pageItems,
                pageFills,
                permission,
                allowPlayerInventory,
                noPermsMessage
        );
    }

    public GuiInstance open(Player player) {
        return build().open(player);
    }
}