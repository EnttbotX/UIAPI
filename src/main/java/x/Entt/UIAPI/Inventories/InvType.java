package x.Entt.UIAPI.Inventories;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.SlotRole;
import x.Entt.UIAPI.GUI.UIHolder;
import x.Entt.UIAPI.Utils.Text;

public abstract class InvType {
    private static final Map<String, InvType> REGISTRY = new LinkedHashMap<>();

    public static final Chest CHEST = register(new Chest());
    public static final Dispenser DISPENSER = register(new Dispenser());
    public static final Dropper DROPPER = register(new Dropper());
    public static final Hopper HOPPER = register(new Hopper());
    public static final Anvil ANVIL = register(new Anvil());
    public static final Furnace FURNACE = register(new Furnace("FURNACE"));
    public static final Furnace BLAST_FURNACE = register(new Furnace("BLAST_FURNACE"));
    public static final Furnace SMOKER = register(new Furnace("SMOKER"));
    public static final Workbench WORKBENCH = register(new Workbench());
    public static final Brewing BREWING = register(new Brewing());
    public static final Enchanting ENCHANTING = register(new Enchanting());
    public static final Grindstone GRINDSTONE = register(new Grindstone());
    public static final Cartography CARTOGRAPHY = register(new Cartography());
    public static final Loom LOOM = register(new Loom());
    public static final Stonecutter STONECUTTER = register(new Stonecutter());
    public static final Beacon BEACON = register(new Beacon());
    public static final Smithing SMITHING = register(new Smithing());

    private final String name;
    private final InventoryType bukkit;
    private final int size;
    private final int[][] grid;
    private final Map<SlotRole, int[]> roles = new EnumMap<>(SlotRole.class);

    protected InvType(String name, int size, int[][] grid) {
        this.name = name;
        this.bukkit = lookup(name);
        this.size = size;
        this.grid = grid;
    }

    private static <T extends InvType> T register(T type) {
        REGISTRY.put(type.getName(), type);
        return type;
    }

    protected static InventoryType lookup(String name) {
        try {
            return InventoryType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    protected static int[][] linear(int size, int columns) {
        int rows = (size + columns - 1) / columns;
        int[][] grid = new int[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = row * columns + column;
                grid[row][column] = slot < size ? slot : -1;
            }
        }
        return grid;
    }

    protected InvType role(SlotRole role, int... slots) {
        roles.put(role, slots);
        return this;
    }

    public static synchronized InvType of(InventoryType type) {
        InvType known = REGISTRY.get(type.name());
        if (known != null) {
            return known;
        }
        return register(new Generic(type));
    }

    public static InvType of(String name) {
        InvType known = REGISTRY.get(name.toUpperCase());
        if (known != null) {
            return known;
        }
        InventoryType type = lookup(name.toUpperCase());
        if (type == null) {
            throw new IllegalArgumentException("Unknown inventory type '" + name + "'.");
        }
        return of(type);
    }

    public static Collection<InvType> values() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void registerListeners(Plugin plugin) {
        for (InvType type : values()) {
            if (type instanceof Listener listener) {
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            }
        }
    }

    public String getName() {
        return name;
    }

    public InventoryType getBukkitType() {
        return bukkit;
    }

    public boolean isDynamic() {
        return false;
    }

    public int maxRows() {
        return grid.length;
    }

    public int maxColumns() {
        return grid[0].length;
    }

    public int size(int layoutRows) {
        return size;
    }

    public int physicalSlot(int row, int column) {
        if (row < 0 || column < 0 || row >= grid.length || column >= grid[0].length) {
            return -1;
        }
        return grid[row][column];
    }

    public boolean hasRole(SlotRole role) {
        return roles.containsKey(role);
    }

    public Collection<SlotRole> roles() {
        return Collections.unmodifiableSet(roles.keySet());
    }

    public int[] slotsOf(SlotRole role) {
        int[] slots = roles.get(role);
        return slots == null ? new int[0] : slots.clone();
    }

    public SlotRole roleOf(int physicalSlot) {
        for (Map.Entry<SlotRole, int[]> entry : roles.entrySet()) {
            for (int slot : entry.getValue()) {
                if (slot == physicalSlot) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public List<Integer> slotsOfAsList(SlotRole role) {
        List<Integer> list = new ArrayList<>();
        for (int slot : slotsOf(role)) {
            list.add(slot);
        }
        return list;
    }

    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        if (bukkit == null) {
            throw new IllegalStateException("Inventory type " + name + " is not available on this server version.");
        }
        try {
            return Bukkit.createInventory(holder, bukkit, title);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Inventory type " + name + " cannot be created by plugins.", ex);
        }
    }

    public Inventory create(GuiInstance instance) {
        return createInventory(
                new UIHolder(instance),
                instance.getDefinition().getLayout().getPhysicalSize(),
                Text.color(instance.getDefinition().getTitle())
        );
    }

    public void show(GuiInstance instance) {
        Inventory inventory = instance.getInventory();
        if (inventory != null) {
            instance.getPlayer().openInventory(inventory);
        }
    }

    public void render(GuiInstance instance) {
    }

    public void onClick(GuiInstance instance, InventoryClickEvent event) {
    }

    public void onClose(GuiInstance instance) {
    }

    @Override
    public String toString() {
        return name;
    }
}
