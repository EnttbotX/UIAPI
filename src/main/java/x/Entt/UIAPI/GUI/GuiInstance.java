package x.Entt.UIAPI.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.UIAPI;
import x.Entt.UIAPI.Utils.Pagination;

public class GuiInstance {
    private final GUI definition;
    private final Player player;
    private final Map<Character, Item> items;
    private final Map<Character, Pagination<Item>> paginations = new HashMap<>();
    private final Set<Integer> seededInputs = new HashSet<>();
    private final Map<Integer, Item> renderedItems = new HashMap<>();
    private Inventory inventory;
    private boolean closed;

    public GuiInstance(GUI definition, Player player) {
        this.definition = definition;
        this.player = player;
        this.items = new HashMap<>(definition.getSlotItems());
        for (Map.Entry<Character, List<Item>> entry : definition.getPageItems().entrySet()) {
            int slots = definition.getLayout().count(entry.getKey());
            paginations.put(entry.getKey(), new Pagination<>(entry.getValue(), slots));
        }
    }

    public void open() {
        GuiManager.register(player.getUniqueId(), this);
        InvType type = definition.getType();
        try {
            inventory = type.create(this);
            render();
            type.show(this);
        } catch (RuntimeException | Error ex) {
            handleClose();
            throw ex;
        }
    }

    public void handleClose() {
        dispose();
        GuiManager.unregister(player.getUniqueId(), this);
    }

    public void dispose() {
        if (closed) {
            return;
        }
        closed = true;
        returnInputs();
        definition.getType().onClose(this);
    }

    public void handleClick(InventoryClickEvent event) {
        interact(event);
        definition.getType().onClick(this, event);
    }

    public void handleDrag(InventoryDragEvent event) {
        int topSize = inventory.getSize();
        boolean touchesInputs = false;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                if (!definition.isInputSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
                touchesInputs = true;
            } else if (!definition.isAllowPlayerInventory()) {
                event.setCancelled(true);
                return;
            }
        }
        if (touchesInputs) {
            notifyInputChanged();
        }
    }

    private void interact(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }
        if (owns(clicked)) {
            int slot = event.getSlot();
            if (definition.isInputSlot(slot)) {
                notifyInputChanged();
                return;
            }
            event.setCancelled(true);
            Item item = renderedItems.get(slot);
            if (item != null) {
                item.handleClick((Player) event.getWhoClicked(), event.getClick());
            }
            return;
        }
        if (!definition.isAllowPlayerInventory() || event.getClick().isShiftClick()) {
            event.setCancelled(true);
        }
    }

    private void notifyInputChanged() {
        Consumer<GuiInstance> callback = definition.getOnInput();
        if (callback == null) {
            return;
        }
        Bukkit.getScheduler().runTask(UIAPI.getInstance(), () -> {
            if (!closed) {
                callback.accept(this);
            }
        });
    }

    private void returnInputs() {
        if (inventory == null) {
            return;
        }
        for (int slot : definition.getInputSlots()) {
            ItemStack stack = inventory.getItem(slot);
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            for (ItemStack rest : player.getInventory().addItem(stack).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), rest);
            }
        }
        inventory.clear();
    }

    private void render() {
        if (inventory == null) {
            return;
        }
        renderedItems.clear();
        renderContent();
        definition.getType().render(this);
    }

    private void renderContent() {
        GuiLayout layout = definition.getLayout();
        InvType type = definition.getType();
        Map<Integer, Item> desired = new HashMap<>();
        for (int row = 0; row < layout.getRows(); row++) {
            for (int column = 0; column < layout.getColumns(); column++) {
                int slot = layout.physicalSlot(row, column);
                char key = layout.charAt(row, column);
                if (slot < 0 || paginations.containsKey(key) || !items.containsKey(key)) {
                    continue;
                }
                desired.put(slot, items.get(key));
            }
        }
        for (Map.Entry<Character, Pagination<Item>> entry : paginations.entrySet()) {
            char key = entry.getKey();
            List<Integer> slots = layout.physicalSlots(key);
            List<Item> current = entry.getValue().getCurrentItems();
            int[] positions = definition.getPageFill(key).assign(slots.size(), current.size());
            for (int slot : slots) {
                desired.put(slot, null);
            }
            for (int i = 0; i < current.size(); i++) {
                desired.put(slots.get(positions[i]), current.get(i));
            }
        }
        for (Map.Entry<SlotRole, Item> entry : definition.getRoleItems().entrySet()) {
            for (int slot : type.slotsOf(entry.getKey())) {
                desired.put(slot, entry.getValue());
            }
        }
        for (Map.Entry<Integer, Item> entry : desired.entrySet()) {
            int slot = entry.getKey();
            Item item = entry.getValue();
            if (definition.isInputSlot(slot)) {
                if (item != null && seededInputs.add(slot)) {
                    inventory.setItem(slot, item.build());
                }
                continue;
            }
            setSlot(slot, item);
        }
    }

    public void update() {
        render();
    }

    public void update(char key, Item item) {
        items.put(key, item);
        render();
    }

    public void setSlot(int slot, Item item) {
        trackItem(slot, item);
        ItemStack desired = item != null ? item.build() : null;
        ItemStack current = inventory.getItem(slot);
        if (!sameStack(current, desired)) {
            inventory.setItem(slot, desired);
        }
    }

    public void trackItem(int slot, Item item) {
        if (item != null) {
            renderedItems.put(slot, item);
        } else {
            renderedItems.remove(slot);
        }
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        boolean emptyA = a == null || a.getType().isAir();
        boolean emptyB = b == null || b.getType().isAir();
        if (emptyA || emptyB) {
            return emptyA && emptyB;
        }
        return a.equals(b);
    }

    public boolean nextPage(char key) {
        Pagination<Item> pagination = paginations.get(key);
        if (pagination == null || !pagination.next()) {
            return false;
        }
        render();
        return true;
    }

    public boolean previousPage(char key) {
        Pagination<Item> pagination = paginations.get(key);
        if (pagination == null || !pagination.previous()) {
            return false;
        }
        render();
        return true;
    }

    public boolean nextPage() {
        if (paginations.size() != 1) {
            return false;
        }
        return nextPage(paginations.keySet().iterator().next());
    }

    public boolean previousPage() {
        if (paginations.size() != 1) {
            return false;
        }
        return previousPage(paginations.keySet().iterator().next());
    }

    public Item getItem(char key) {
        return items.get(key);
    }

    public Item getItemAt(int physicalSlot) {
        return renderedItems.get(physicalSlot);
    }

    public List<ItemStack> getContents(char key) {
        return contentsOf(definition.getLayout().physicalSlots(key));
    }

    public List<ItemStack> getContents(SlotRole role) {
        return contentsOf(definition.getType().slotsOfAsList(role));
    }

    private List<ItemStack> contentsOf(List<Integer> slots) {
        List<ItemStack> result = new ArrayList<>();
        if (inventory == null) {
            return result;
        }
        for (int slot : slots) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && !stack.getType().isAir()) {
                result.add(stack);
            }
        }
        return result;
    }

    public boolean owns(Inventory other) {
        return inventory != null && other != null && (inventory == other || inventory.equals(other));
    }

    public GUI getDefinition() {
        return definition;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isClosed() {
        return closed;
    }
}
