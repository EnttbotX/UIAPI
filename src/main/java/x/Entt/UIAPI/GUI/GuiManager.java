package x.Entt.UIAPI.GUI;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {

    private static final Map<UUID, GuiInstance> active = new ConcurrentHashMap<>();

    public static void register(UUID player, GuiInstance instance) {
        GuiInstance previous = active.put(player, instance);
        if (previous != null && previous != instance) {
            previous.dispose();
        }
    }

    public static void unregister(UUID player, GuiInstance instance) {
        active.remove(player, instance);
    }

    public static void unregister(UUID player) {
        active.remove(player);
    }

    public static GuiInstance get(UUID player) {
        return active.get(player);
    }

    public static void closeAll() {
        for (GuiInstance instance : new ArrayList<>(active.values())) {
            instance.getPlayer().closeInventory();
            instance.handleClose();
        }
        active.clear();
    }
}