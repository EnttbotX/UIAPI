package x.Entt.UIAPI.GUI;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public class GUI {
    private final String title;
    private final GuiLayout layout;
    private final Map<Character, Item> slotItems;
    private final Map<Character, List<Item>> pageItems;
    private final Map<Character, PageFill> pageFills;
    private final Predicate<Player> permission;
    private final boolean allowPlayerInventory;
    private final String noPermsMessage;

    public GUI(String title,
               GuiLayout layout,
               Map<Character, Item> slotItems,
               Map<Character, List<Item>> pageItems,
               Map<Character, PageFill> pageFills,
               Predicate<Player> permission,
               boolean allowPlayerInventory,
               String noPermsMessage) {
        this.title = title;
        this.layout = layout;
        this.slotItems = slotItems;
        this.pageItems = pageItems;
        this.pageFills = pageFills;
        this.permission = permission;
        this.allowPlayerInventory = allowPlayerInventory;
        this.noPermsMessage = noPermsMessage;
    }

    public GuiInstance open(Player player) {
        if (permission != null && !permission.test(player)) {
            if (noPermsMessage != null) {
                player.sendMessage(Text.color(prefix + noPermsMessage));
            }

            return null;
        }

        GuiInstance instance = new GuiInstance(this, player);
        instance.open();
        return instance;
    }

    public String getTitle() {
        return title;
    }

    public GuiLayout getLayout() {
        return layout;
    }

    public Map<Character, Item> getSlotItems() {
        return slotItems;
    }

    public Map<Character, List<Item>> getPageItems() {
        return pageItems;
    }

    public PageFill getPageFill(char key) {
        return pageFills.getOrDefault(key, PageFill.LEFT);
    }

    public boolean isAllowPlayerInventory() {
        return allowPlayerInventory;
    }
}