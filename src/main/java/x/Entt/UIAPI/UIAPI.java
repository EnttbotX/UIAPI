package x.Entt.UIAPI;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.UIAPI.Events.Events;
import x.Entt.UIAPI.Events.UIListener;
import x.Entt.UIAPI.GUI.GuiManager;
import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.Test.CMD;
import x.Entt.UIAPI.Utils.Text;
import x.Entt.UIAPI.Utils.Updater;

import java.util.Objects;

public class UIAPI extends JavaPlugin {
    public static String prefix = "&x&F&F&8&D&8&D«&x&F&F&7&E&7&EU&x&F&F&6&F&6&FI&x&F&F&5&F&5&FA&x&F&F&5&0&5&0P&x&F&F&4&9&4&9I&x&F&F&4&3&4&3» &x&F&F&3&6&3&6→ ";
    public String version = getDescription().getVersion();
    private static UIAPI instance;
    private Updater updater;
    private Metrics metrics;

    public static UIAPI getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        metrics = new Metrics(this, 34106);
        updater = new Updater(this, 138877);
        updater.start();

        InvType.registerListeners(this);
        getServer().getPluginManager().registerEvents(new UIListener(), this);
        getServer().getPluginManager().registerEvents(new Events(this), this);

        CMD command = new CMD(this);
        PluginCommand testCommand = Objects.requireNonNull(getCommand("uiapi-test"), "Command uiapi-test is missing in plugin.yml");
        testCommand.setExecutor(command);
        testCommand.setTabCompleter(command);

        Text.log(prefix + "&av" + version + " &2Enabled!");
    }

    @Override
    public void onDisable() {
        GuiManager.closeAll();

        if (metrics != null) {
            metrics.shutdown();
        }

        Text.log(prefix + "&av" + version + " &cDisabled");
    }

    public Updater getUpdater() {
        return updater;
    }
}