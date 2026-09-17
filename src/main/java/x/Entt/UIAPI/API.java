package x.Entt.UIAPI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import x.Entt.UIAPI.GUI.GuiBuilder;
import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.GuiManager;
import x.Entt.UIAPI.Item.Item;

public class API {

    public static Item item(Material material) {
        return Item.of(material);
    }

    public static Item item(String material) {
        return Item.of(material);
    }

    public static Item fromItemStack(ItemStack stack) {
        return Item.fromItemStack(stack);
    }

    public static Item closeInv() {
        return item(Material.BARRIER).onClick(Player::closeInventory);
    }

    public static Item nextPage() {
        return item(Material.SPECTRAL_ARROW)
                .name("&2&lNext Page")
                .onClick(p -> {
                    GuiInstance instance = GuiManager.get(p.getUniqueId());
                    if (instance != null) {
                        instance.nextPage();
                    }
                });
    }

    public static Item nextPage(char pageKey) {
        return item(Material.SPECTRAL_ARROW)
                .name("&2&lNext Page")
                .onClick(p -> {
                    GuiInstance instance = GuiManager.get(p.getUniqueId());
                    if (instance != null) {
                        instance.nextPage(pageKey);
                    }
                });
    }

    public static Item prevPage() {
        return item(Material.ARROW)
                .name("&c&lPrevious Page")
                .onClick(p -> {
                    GuiInstance instance = GuiManager.get(p.getUniqueId());
                    if (instance != null) {
                        instance.previousPage();
                    }
                });
    }

    public static Item prevPage(char pageKey) {
        return item(Material.ARROW)
                .name("&c&lPrevious Page")
                .onClick(p -> {
                    GuiInstance instance = GuiManager.get(p.getUniqueId());
                    if (instance != null) {
                        instance.previousPage(pageKey);
                    }
                });
    }

    public static GuiBuilder create(String title) {
        return new GuiBuilder(title);
    }
}