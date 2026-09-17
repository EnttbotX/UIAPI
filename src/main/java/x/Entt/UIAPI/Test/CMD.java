package x.Entt.UIAPI.Test;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import x.Entt.UIAPI.API;
import x.Entt.UIAPI.GUI.PageFill;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.UIAPI;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public class CMD implements CommandExecutor {
    private final UIAPI plugin;

    public CMD(UIAPI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.color(prefix + "&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission("uiapi.test")) {
            sender.sendMessage(Text.color(prefix + "&cYou don't have permissions to use this command."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Text.color(prefix + "&cUsage: /uitest <test>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("1")) {
            test1(player);
        } else {
            sender.sendMessage(Text.color(prefix + "&cUNKNOWN SUBCOMMAND!"));
        }

        return true;
    }

    public void test1(Player player) {
        Item glass = Item.of(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ");

        Item playerHead = Item.playerHead(player).name(Text.color("&c&l" + player.getName()));

        List<Item> pagedItems = List.of(
                Item.of(Material.DIAMOND)
                        .name("§bDiamante")
                        .price(500)
                        .onClick(p -> buy(p, Material.DIAMOND)),

                Item.of(Material.EMERALD)
                        .name("§aEsmeralda")
                        .price(750)
                        .onClick(p -> buy(p, Material.EMERALD)),

                Item.of(Material.GOLD_INGOT)
                        .name("§6Oro")
                        .price(300)
                        .onClick(p -> buy(p, Material.GOLD_INGOT)),

                Item.of(Material.IRON_INGOT)
                        .name("§fHierro")
                        .price(150)
                        .onClick(p -> buy(p, Material.IRON_INGOT))
        );

        API.create("Papozo")
                .layout("""
                #########
                # X   D #
                # +++++ #
                # +++++ #
                #  < >  #
                #########
                """)
                .page('+', pagedItems, PageFill.JUSTIFY)
                .slot('#', glass)
                .slot('D', playerHead)
                .slot('X', API.closeInv().name("&c&lCerrar"))
                .slot('<', API.prevPage().name("&c&lAtrás"))
                .slot('>', API.nextPage().name("&c&lSiguiente"))
                .build()
                .open(player);
    }

    public void buy(Player player, Material material) {
        player.sendMessage(Text.color(prefix + "&2Comprado " + material.name() + " :)"));
    }
}