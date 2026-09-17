package x.Entt.UIAPI.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.Layout.SlotMapper;
import x.Entt.UIAPI.Utils.Pagination;
import x.Entt.UIAPI.Utils.Text;

public class GuiInstance {
    private final GUI definition;
    private final Player player;
    private final Inventory inventory;
    private final Map<Character, Item> items;
    private final Map<Character, Pagination<Item>> paginations = new HashMap<>();
    private final Map<Integer, Item> renderedItems = new HashMap<>();

    public GuiInstance(GUI definition, Player player) {
        this.definition = definition;
        this.player = player;
        this.items = new HashMap<>(definition.getSlotItems());
        this.inventory = Bukkit.createInventory(new UIHolder(this), definition.getLayout().getPhysicalSize(), Text.color(definition.getTitle()));

        GuiLayout layout = definition.getLayout();
        for (Map.Entry<Character, List<Item>> entry : definition.getPageItems().entrySet()) {
            char key = entry.getKey();
            int slots = layout.count(key);

            paginations.put(key, new Pagination<>(entry.getValue(), slots));
        }

        render();
    }

    public void open() {
        GuiManager.register(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    public void update(char key, Item item) {
        items.put(key, item);
        render();
    }

    public void update() {
        render();
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

    private void render() {
        renderedItems.clear();

        GuiLayout layout = definition.getLayout();
        for (int row = 0; row < layout.getRows(); row++) {
            for (int column = 0; column < layout.getColumns(); column++) {
                char key = layout.charAt(row, column);
                if (paginations.containsKey(key)) {
                    continue;
                }

                Item item = items.get(key);
                int slot = SlotMapper.toPhysical(row, column);
                if (item != null) {
                    renderedItems.put(slot, item);
                }
                inventory.setItem(slot, item != null ? item.build() : null);
            }
        }

        for (Map.Entry<Character, Pagination<Item>> entry : paginations.entrySet()) {
            renderPage(entry.getKey(), entry.getValue());
        }
    }

    private void renderPage(char key, Pagination<Item> pagination) {
        List<Integer> slots = definition.getLayout().physicalSlots(key);
        List<Item> current = pagination.getCurrentItems();
        int[] positions = definition.getPageFill(key).assign(slots.size(), current.size());

        for (int slot : slots) {
            renderedItems.remove(slot);
            inventory.setItem(slot, null);
        }

        for (int i = 0; i < current.size(); i++) {
            int slot = slots.get(positions[i]);
            Item item = current.get(i);
            renderedItems.put(slot, item);
            inventory.setItem(slot, item.build());
        }
    }

    public Item getItem(char key) {
        return items.get(key);
    }

    public Item getItemAt(int physicalSlot) {
        return renderedItems.get(physicalSlot);
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
}
