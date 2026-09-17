package x.Entt.UIAPI;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.UIAPI.Events.Events;
import x.Entt.UIAPI.Events.UIListener;
import x.Entt.UIAPI.Test.CMD;
import x.Entt.UIAPI.Utils.Text;
import x.Entt.UIAPI.Utils.Updater;

import java.util.Objects;

public class UIAPI extends JavaPlugin {
    public static String prefix = "&x&F&F&8&D&8&D«&x&F&F&7&E&7&EU&x&F&F&6&F&6&FI&x&F&F&5&F&5&FA&x&F&F&5&0&5&0P&x&F&F&4&9&4&9I&x&F&F&4&3&4&3» &x&F&F&3&6&3&6→ ";
    public String version = getDescription().getVersion();
    private Updater updater;
    private Metrics metrics;

    @Override
    public void onEnable() {
        metrics = new Metrics(this, 34106);
        updater = new Updater(this, 138877);

        getServer().getPluginManager().registerEvents(new UIListener(), this);
        getServer().getPluginManager().registerEvents(new Events(this), this);
        Objects.requireNonNull(getCommand("uiapi-test")).setExecutor(new CMD(this));

        Text.log(prefix + "&av" + version + " &2Enabled!");
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }

        Text.log(prefix + "&av" + version + " &cDisabled");
    }

    public Updater getUpdater() {
        return updater;
    }
}