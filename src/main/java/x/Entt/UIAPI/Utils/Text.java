package x.Entt.UIAPI.Utils;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.stream.Collectors;

public class Text {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorList(List<String> lines) {
        return lines.stream()
                .map(Text::color)
                .collect(Collectors.toList());
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }
}