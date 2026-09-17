package x.Entt.UIAPI.GUI;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {

    private static final Map<UUID, GuiInstance> active = new ConcurrentHashMap<>();

    public static void register(UUID player, GuiInstance instance) {
        active.put(player, instance);
    }

    public static void unregister(UUID player) {
        active.remove(player);
    }

    public static GuiInstance get(UUID player) {
        return active.get(player);
    }
}