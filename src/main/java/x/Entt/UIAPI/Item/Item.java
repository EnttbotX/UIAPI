package x.Entt.UIAPI.Item;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import x.Entt.UIAPI.Utils.PlayerHeadCache;
import x.Entt.UIAPI.Utils.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Item {
    private ItemStack baseStack;
    private Material material;
    private int amount = 1;
    private boolean amountSet;
    private String name;
    private List<String> lore = new ArrayList<>();
    private Integer customModelData;
    private Double price;
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private final Set<ItemFlag> flags = EnumSet.noneOf(ItemFlag.class);
    private OfflinePlayer skullOwner;
    private Consumer<Player> defaultClickAction;
    private final Map<ClickType, Consumer<Player>> clickActions = new EnumMap<>(ClickType.class);

    public Item() {
    }

    public static Item of(Material material) {
        Item item = new Item();
        item.material = material;
        return item;
    }

    public static Item of(String material) {
        Material matched = Material.matchMaterial(material);
        if (matched == null) {
            throw new IllegalArgumentException("Unknown material '" + material + "'.");
        }
        return of(matched);
    }

    public static Item fromItemStack(ItemStack stack) {
        Item item = new Item();
        item.baseStack = stack.clone();
        item.material = stack.getType();
        return item;
    }

    public static Item playerHead(OfflinePlayer player) {
        Item item = new Item();
        item.material = Material.PLAYER_HEAD;
        item.skullOwner = player;
        return item;
    }

    public static Item playerHead(String name) {
        return playerHead(PlayerHeadCache.get(name));
    }

    public Item name(String name) {
        this.name = Text.color(name);
        return this;
    }

    public Item lore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
        return this;
    }

    public Item amount(int amount) {
        this.amount = amount;
        this.amountSet = true;
        return this;
    }

    public Item material(Material material) {
        this.material = material;
        return this;
    }

    public Item customModelData(int data) {
        this.customModelData = data;
        return this;
    }

    public Item price(double price) {
        this.price = price;
        return this;
    }

    public Double getPrice() {
        return price;
    }

    public Item enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public Item flag(ItemFlag... flags) {
        this.flags.addAll(List.of(flags));
        return this;
    }

    public Item onClick(Consumer<Player> action) {
        this.defaultClickAction = action;
        return this;
    }

    public Item onClick(ClickType type, Consumer<Player> action) {
        this.clickActions.put(type, action);
        return this;
    }

    public void handleClick(Player player, ClickType type) {
        Consumer<Player> action = clickActions.get(type);
        if (action != null) {
            action.accept(player);
            return;
        }
        if (defaultClickAction != null) {
            defaultClickAction.accept(player);
        }
    }

    public ItemStack build() {
        ItemStack stack = baseStack != null ? baseStack.clone() : new ItemStack(material, amount);
        if (baseStack != null && amountSet) {
            stack.setAmount(amount);
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        if (name != null) {
            meta.setDisplayName(Text.color(name));
        }
        List<String> finalLore = new ArrayList<>(lore);
        if (price != null) {
            finalLore.add("&7Precio: &e" + formatPrice(price));
        }
        if (!finalLore.isEmpty()) {
            meta.setLore(Text.colorList(finalLore));
        }
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        if (!flags.isEmpty()) {
            meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        }
        if (skullOwner != null && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(skullOwner);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) {
            return String.valueOf((long) price);
        }
        return String.valueOf(price);
    }
}