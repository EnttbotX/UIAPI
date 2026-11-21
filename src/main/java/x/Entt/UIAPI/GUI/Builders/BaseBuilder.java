package x.Entt.UIAPI.GUI.Builders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import x.Entt.UIAPI.GUI.*;
import x.Entt.UIAPI.Inventories.Anvil;
import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.Layout.LayoutParser;

public abstract class BaseBuilder<B extends BaseBuilder<B>> {
    protected final InvType type;
    protected final String title;

    private GuiLayout layout;
    private final Map<Character, Item> slotItems = new HashMap<>();
    private final Map<Character, List<Item>> pageItems = new HashMap<>();
    private final Map<Character, PageFill> pageFills = new HashMap<>();
    private final Map<SlotRole, Item> roleItems = new EnumMap<>(SlotRole.class);
    private final Map<Character, SlotMode> keyModes = new HashMap<>();
    private final Map<SlotRole, SlotMode> roleModes = new EnumMap<>(SlotRole.class);
    private Predicate<Player> permission;
    private String noPermsMessage;
    private boolean allowPlayerInventory;
    private Consumer<GuiInstance> onInput;

    protected BaseBuilder(String title, InvType type) {
        this.title = title;
        this.type = type;
    }

    protected abstract B self();

    public B layout(String layout) {
        this.layout = LayoutParser.parse(layout, type);
        return self();
    }

    public B slot(char key, Item item) {
        if (slotItems.containsKey(key)) {
            throw new IllegalStateException("UI slot '" + key + "' is already defined.");
        }
        slotItems.put(key, item);
        return self();
    }

    public B slot(SlotRole role, Item item) {
        requireRole(role);
        if (roleItems.containsKey(role)) {
            throw new IllegalStateException("UI role " + role + " is already defined.");
        }
        roleItems.put(role, item);
        return self();
    }

    public B page(char key, List<Item> items) {
        return page(key, items, PageFill.LEFT);
    }

    public B page(char key, List<Item> items, PageFill fill) {
        if (pageItems.containsKey(key)) {
            throw new IllegalStateException("UI page '" + key + "' is already defined.");
        }

        pageItems.put(key, items);
        pageFills.put(key, fill == null ? PageFill.LEFT : fill);
        return self();
    }

    public B editable(char key) {
        keyModes.put(key, SlotMode.INPUT);
        return self();
    }

    public B editable(SlotRole role) {
        requireRole(role);
        roleModes.put(role, SlotMode.INPUT);
        return self();
    }

    public B onInput(Consumer<GuiInstance> callback) {
        this.onInput = callback;
        return self();
    }

    public B permission(String permission) {
        this.permission = player -> player.hasPermission(permission);
        return self();
    }

    public B permission(Predicate<Player> permission) {
        this.permission = permission;
        return self();
    }

    public B noPermsMessage(String message) {
        this.noPermsMessage = message;
        return self();
    }

    public B allowPlayerInventory() {
        this.allowPlayerInventory = true;
        return self();
    }

    protected Anvil.Config anvil() {
        return null;
    }

    public GUI build() {
        GuiLayout finalLayout = layout;
        if (finalLayout == null) {
            if (type.isDynamic()) {
                throw new IllegalStateException("UI layout is required before calling build().");
            }
            finalLayout = GuiLayout.blank(type);
        }

        for (char key : slotItems.keySet()) {
            if (!finalLayout.contains(key)) {
                throw new IllegalStateException(
                        "UI slot '" + key + "' is not present in the layout."
                );
            }
        }

        for (char key : pageItems.keySet()) {
            if (!finalLayout.contains(key)) {
                throw new IllegalStateException(
                        "UI page '" + key + "' is not present in the layout."
                );
            }
        }

        for (char key : keyModes.keySet()) {
            if (!finalLayout.contains(key)) {
                throw new IllegalStateException(
                        "UI editable slot '" + key + "' is not present in the layout."
                );
            }
        }

        for (int row = 0; row < finalLayout.getRows(); row++) {
            for (int column = 0; column < finalLayout.getColumns(); column++) {
                char key = finalLayout.charAt(row, column);

                if (key == ' ' || finalLayout.physicalSlot(row, column) < 0) {
                    continue;
                }

                if (!slotItems.containsKey(key) && !pageItems.containsKey(key) && !keyModes.containsKey(key)) {
                    Bukkit.getLogger().warning(
                            "UI character '" + key + "' is used in the layout but has no slot() or page() defined."
                    );
                }
            }
        }

        return new GUI(new GUI.Spec(
                type,
                title,
                finalLayout,
                slotItems,
                pageItems,
                pageFills,
                roleItems,
                keyModes,
                roleModes,
                allowPlayerInventory,
                permission,
                noPermsMessage,
                onInput,
                anvil()
        ));
    }

    public GuiInstance open(Player player) {
        return build().open(player);
    }

    private void requireRole(SlotRole role) {
        if (!type.hasRole(role)) {
            throw new IllegalStateException(
                    "Inventory type " + type + " has no role " + role + ". Available roles: " + type.roles() + "."
            );
        }
    }
}