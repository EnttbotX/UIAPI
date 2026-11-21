package x.Entt.UIAPI.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHeadCache {

    private static final Map<String, OfflinePlayer> cache = new ConcurrentHashMap<>();

    public static OfflinePlayer get(String name) {
        return cache.computeIfAbsent(name.toLowerCase(), key -> Bukkit.getOfflinePlayer(name));
    }
}