package x.Entt.UIAPI.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;

import static x.Entt.UIAPI.UIAPI.prefix;

public class Updater {
    private static final long REFRESH_TICKS = 20L * 60L * 60L * 6L;

    private final JavaPlugin plugin;
    private final int resourceId;
    private volatile String latestVersion;
    private volatile boolean announced;

    public Updater(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refresh, 20L, REFRESH_TICKS);
    }

    public void refresh() {
        String fetched = fetchLatestVersion();

        if (fetched == null) {
            return;
        }

        latestVersion = fetched;

        if (!announced && newUpdateAvailable()) {
            announced = true;
            announce();
        }
    }

    public boolean newUpdateAvailable() {
        String latest = latestVersion;

        return latest != null && compareVersions(plugin.getDescription().getVersion(), latest) < 0;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + resourceId + "/";
    }

    private void announce() {
        Text.log("");
        Text.log("&2&l======== " + prefix + "&2&l========");
        Text.log("&e&lUPDATE AVAILABLE");
        Text.log("&e&lVersion: &f" + plugin.getDescription().getVersion());
        Text.log("&e&lNew Version: &f" + latestVersion);
        Text.log("&e&lDownload Link:");
        Text.log(getResourceURL());
        Text.log("&2&l======== " + prefix + "&2&l========");
        Text.log("");
    }

    private String fetchLatestVersion() {
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId)
                    .toURL()
                    .openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            try (InputStream in = con.getInputStream(); Scanner scanner = new Scanner(in)) {
                if (scanner.hasNext()) {
                    return scanner.next().trim();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Update check failed: " + e.getMessage());
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return null;
    }

    private static int compareVersions(String current, String latest) {
        String[] c = current.split("[.\\-+]");
        String[] l = latest.split("[.\\-+]");

        int max = Math.max(c.length, l.length);

        for (int i = 0; i < max; i++) {
            int cv = i < c.length ? number(c[i]) : 0;
            int lv = i < l.length ? number(l[i]) : 0;

            if (cv < lv) return -1;
            if (cv > lv) return 1;
        }

        return 0;
    }

    private static int number(String part) {
        StringBuilder digits = new StringBuilder();

        for (char ch : part.toCharArray()) {
            if (!Character.isDigit(ch)) {
                break;
            }

            digits.append(ch);
        }

        if (digits.length() == 0 || digits.length() > 9) {
            return 0;
        }

        return Integer.parseInt(digits.toString());
    }
}
