package x.Entt.UIAPI.Events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import x.Entt.UIAPI.UIAPI;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public class Events implements Listener {
    private final UIAPI plugin;

    public Events(UIAPI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("uiapi.updates")) {
            if (plugin.getUpdater().newUpdateAvailable()) {
                TextComponent link = new TextComponent(Text.color("&e&l[ DOWNLOAD ]"));
                link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getUpdater().getResourceURL()));

                player.sendMessage(Text.color(""));
                player.sendMessage(Text.color("&2======== " + prefix + "&2&l========"));
                player.sendMessage(Text.color("&eNew Update Available!"));
                player.sendMessage(Text.color("&eVersion: &f" + plugin.getDescription().getVersion()));
                player.sendMessage(Text.color("&eNew Version: &f" + plugin.getUpdater().getLatestVersion()));
                player.sendMessage(Text.color("&eDownload Here:"));
                player.spigot().sendMessage(link);
                player.sendMessage(Text.color("&2======== " + prefix + "&2&l========"));
                player.sendMessage(Text.color(""));
            }
        }
    }
}