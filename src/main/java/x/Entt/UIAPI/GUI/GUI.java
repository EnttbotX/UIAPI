package x.Entt.UIAPI.GUI;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import x.Entt.UIAPI.Inventories.Anvil;
import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public class GUI {
    private final Spec spec;
    private final Set<Integer> inputSlots = new HashSet<>();

    public record Spec(
            InvType type,
            String title,
            GuiLayout layout,
            Map<Character, Item> items,
            Map<Character, List<Item>> pages,
            Map<Character, PageFill> fills,
            Map<SlotRole, Item> roleItems,
            Map<Character, SlotMode> keyModes,
            Map<SlotRole, SlotMode> roleModes,
            boolean allowPlayerInventory,
            Predicate<Player> permission,
            String noPermsMessage,
            Consumer<GuiInstance> onInput,
            Anvil.Config anvil
    ) {
    }

    public GUI(Spec spec) {
        this.spec = spec;
        for (Map.Entry<Character, SlotMode> entry : spec.keyModes().entrySet()) {
            if (entry.getValue() == SlotMode.INPUT) {
                inputSlots.addAll(spec.layout().physicalSlots(entry.getKey()));
            }
        }
        for (Map.Entry<SlotRole, SlotMode> entry : spec.roleModes().entrySet()) {
            if (entry.getValue() == SlotMode.INPUT) {
                for (int slot : spec.type().slotsOf(entry.getKey())) {
                    inputSlots.add(slot);
                }
            }
        }
    }

    public GuiInstance open(Player player) {
        if (spec.permission() != null && !spec.permission().test(player)) {
            if (spec.noPermsMessage() != null) {
                player.sendMessage(Text.color(prefix + spec.noPermsMessage()));
            }
            return null;
        }
        GuiInstance instance = new GuiInstance(this, player);
        instance.open();
        return instance;
    }

    public InvType getType() {
        return spec.type();
    }

    public String getTitle() {
        return spec.title();
    }

    public GuiLayout getLayout() {
        return spec.layout();
    }

    public Map<Character, Item> getSlotItems() {
        return spec.items();
    }

    public Map<Character, List<Item>> getPageItems() {
        return spec.pages();
    }

    public Map<SlotRole, Item> getRoleItems() {
        return spec.roleItems();
    }

    public PageFill getPageFill(char key) {
        return spec.fills().getOrDefault(key, PageFill.LEFT);
    }

    public Consumer<GuiInstance> getOnInput() {
        return spec.onInput();
    }

    public Anvil.Config getAnvil() {
        return spec.anvil();
    }

    public boolean isAllowPlayerInventory() {
        return spec.allowPlayerInventory();
    }

    public boolean isInputSlot(int physicalSlot) {
        return inputSlots.contains(physicalSlot);
    }

    public Set<Integer> getInputSlots() {
        return Set.copyOf(inputSlots);
    }
}
