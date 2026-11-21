package x.Entt.UIAPI.Test;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import x.Entt.UIAPI.API;
import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.GuiManager;
import x.Entt.UIAPI.GUI.PageFill;
import x.Entt.UIAPI.GUI.SlotRole;
import x.Entt.UIAPI.Inventories.Anvil;
import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.UIAPI;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public class CMD implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "uiapi.test";

    private record Test(String description, InvType requires, Function<Player, GuiInstance> opener) {
    }

    private final UIAPI plugin;
    private final Map<String, Test> tests = new LinkedHashMap<>();

    public CMD(UIAPI plugin) {
        this.plugin = plugin;

        tests.put("chest", new Test("Layout, borders, head, click types and close button", InvType.CHEST, this::chest));
        tests.put("pages", new Test("Pagination with previous/next buttons", InvType.CHEST, this::pages));
        tests.put("deposit", new Test("Editable slots, onInput and returned items on close", InvType.CHEST, this::deposit));
        tests.put("anvil", new Test("Anvil text input (virtual anvil, or chat fallback on servers below 1.21)", InvType.ANVIL, this::anvil));
        tests.put("anvilchat", new Test("Anvil text input forced through chat with custom messages", InvType.ANVIL, this::anvilChat));
        tests.put("furnace", new Test("Furnace roles: input, fuel and output", InvType.FURNACE, this::furnace));
        tests.put("workbench", new Test("Workbench ingredient and output roles", InvType.WORKBENCH, this::workbench));
        tests.put("hopper", new Test("Hopper layout", InvType.HOPPER, this::hopper));
        tests.put("smithing", new Test("Smithing table roles", InvType.SMITHING, this::smithing));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            send(sender, "&cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("info")) {
            sendInfo(sender);
            return true;
        }

        if (sub.equals("types")) {
            sendTypes(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "&cOnly players can open test inventories.");
            return true;
        }

        if (sub.equals("check")) {
            runChecks(player);
            return true;
        }

        Test test = tests.get(sub);

        if (test == null) {
            send(sender, "&cUnknown test '" + args[0] + "'. Use &f/" + label + " help&c.");
            return true;
        }

        if (!available(test)) {
            send(sender, "&cThis inventory type is not available on this server version.");
            return true;
        }

        try {
            test.opener().apply(player);
        } catch (RuntimeException ex) {
            send(sender, "&cTest failed: &f" + ex);
            plugin.getLogger().log(java.util.logging.Level.WARNING, "UIAPI test '" + sub + "' failed", ex);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) || args.length != 1) {
            return List.of();
        }

        List<String> options = new ArrayList<>(tests.keySet());
        options.addAll(List.of("check", "info", "types", "help"));

        String typed = args[0].toLowerCase(Locale.ROOT);

        return options.stream()
                .filter(option -> option.startsWith(typed))
                .collect(Collectors.toList());
    }

    private void sendHelp(CommandSender sender, String label) {
        send(sender, "&eAvailable tests:");

        for (Map.Entry<String, Test> entry : tests.entrySet()) {
            String state = available(entry.getValue()) ? "" : " &c(unavailable)";
            sender.sendMessage(Text.color("&8 - &f/" + label + " " + entry.getKey() + " &7" + entry.getValue().description() + state));
        }

        sender.sendMessage(Text.color("&8 - &f/" + label + " check &7Opens and closes every test and reports PASS/FAIL"));
        sender.sendMessage(Text.color("&8 - &f/" + label + " info &7Server and anvil mode information"));
        sender.sendMessage(Text.color("&8 - &f/" + label + " types &7Inventory types known by the API"));
    }

    private void sendInfo(CommandSender sender) {
        send(sender, "&ePlugin version: &f" + plugin.version);
        send(sender, "&eServer: &f" + Bukkit.getVersion());
        send(sender, "&eBukkit API: &f" + Bukkit.getBukkitVersion());
        send(sender, "&eAnvil text mode: &f" + (Anvil.isVirtualSupported() ? "VIRTUAL (real anvil)" : "CHAT (server is below 1.21)"));
        send(sender, "&eLatest version: &f" + (plugin.getUpdater().getLatestVersion() == null ? "unknown" : plugin.getUpdater().getLatestVersion()));
    }

    private void sendTypes(CommandSender sender) {
        send(sender, "&eInventory types:");

        for (InvType type : InvType.values()) {
            String state = type.getBukkitType() == null ? "&cunavailable" : "&aavailable";
            sender.sendMessage(Text.color("&8 - &f" + type.getName() + " &7(" + state + "&7)"));
        }
    }

    private void runChecks(Player player) {
        send(player, "&eRunning " + tests.size() + " checks...");
        runCheck(player, new ArrayList<>(tests.entrySet()), 0, 0, 0, 0);
    }

    private void runCheck(Player player, List<Map.Entry<String, Test>> entries, int index, int passed, int failed, int skipped) {
        if (!player.isOnline()) {
            return;
        }

        if (index >= entries.size()) {
            send(player, "&eDone: &a" + passed + " passed&7, &c" + failed + " failed&7, &7" + skipped + " skipped");
            return;
        }

        Map.Entry<String, Test> entry = entries.get(index);
        String name = entry.getKey();
        Test test = entry.getValue();

        int nextPassed = passed;
        int nextFailed = failed;
        int nextSkipped = skipped;
        GuiInstance opened = null;

        if (!available(test)) {
            nextSkipped++;
            player.sendMessage(Text.color("&7SKIP &f" + name + " &7(type unavailable)"));
        } else {
            try {
                opened = test.opener().apply(player);

                if (opened == null || opened.isClosed()) {
                    throw new IllegalStateException("instance was not opened");
                }

                if (GuiManager.get(player.getUniqueId()) != opened) {
                    throw new IllegalStateException("instance was not registered");
                }

                nextPassed++;
                player.sendMessage(Text.color("&aPASS &f" + name));
            } catch (RuntimeException | Error ex) {
                nextFailed++;
                player.sendMessage(Text.color("&cFAIL &f" + name + " &7" + ex));
                plugin.getLogger().log(java.util.logging.Level.WARNING, "UIAPI check '" + name + "' failed", ex);
            }
        }

        GuiInstance finalOpened = opened;
        int finalPassed = nextPassed;
        int finalFailed = nextFailed;
        int finalSkipped = nextSkipped;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (finalOpened != null) {
                player.closeInventory();
                finalOpened.handleClose();
            }

            runCheck(player, entries, index + 1, finalPassed, finalFailed, finalSkipped);
        }, 5L);
    }

    private boolean available(Test test) {
        return test.requires() == null || test.requires().getBukkitType() != null;
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Text.color(prefix + message));
    }

    private Item filler() {
        return API.item(Material.BREAD).name(" ");
    }

    private GuiInstance chest(Player player) {
        return API.create("&8UIAPI Test: Chest")
                .layout("""
                        #########
                        #   h   #
                        #  a b  #
                        #   c   #
                        #########""")
                .slot('#', filler())
                .slot('h', filler().name("&e" + player.getName()))
                .slot('a', API.item(Material.DIAMOND)
                        .name("&bLeft / Right click")
                        .onClick(ClickType.LEFT, p -> send(p, "&aLeft click works"))
                        .onClick(ClickType.RIGHT, p -> send(p, "&aRight click works")))
                .slot('b', API.item(Material.EMERALD)
                        .name("&aDefault click")
                        .lore(List.of("&7Any click type"))
                        .onClick(p -> send(p, "&aDefault click works")))
                .slot('c', API.closeInv().name("&cClose"))
                .open(player);
    }

    private GuiInstance pages(Player player) {
        List<Item> items = IntStream.rangeClosed(1, 40)
                .mapToObj(i -> API.item(Material.PAPER)
                        .name("&aItem " + i)
                        .amount(Math.min(i, 64))
                        .onClick(p -> send(p, "&aClicked item " + i)))
                .collect(Collectors.toList());

        return API.create("&8UIAPI Test: Pages")
                .layout("""
                        #########
                        #xxxxxxx#
                        #xxxxxxx#
                        #xxxxxxx#
                        #xxxxxxx#
                        #<  c  >#""")
                .slot('#', filler())
                .slot('<', API.prevPage('x'))
                .slot('>', API.nextPage('x'))
                .slot('c', API.closeInv().name("&cClose"))
                .page('x', items, PageFill.LEFT)
                .open(player);
    }

    private GuiInstance deposit(Player player) {
        return API.create("&8UIAPI Test: Deposit")
                .layout("""
                        #########
                        #DDDDDDD#
                        ###q#c###""")
                .slot('#', filler())
                .slot('q', API.closeInv().name("&cClose (items are returned)"))
                .slot('c', API.item(Material.LIME_DYE)
                        .name("&aCount items")
                        .onClick(p -> {
                            GuiInstance instance = GuiManager.get(p.getUniqueId());

                            if (instance != null) {
                                send(p, "&eDeposited stacks: &f" + instance.getContents('D').size());
                            }
                        }))
                .editable('D')
                .onInput(instance -> send(instance.getPlayer(), "&7Input changed, stacks: &f" + instance.getContents('D').size()))
                .open(player);
    }

    private GuiInstance anvil(Player player) {
        return API.anvil("&8UIAPI Test: Anvil")
                .text("Type here")
                .output(text -> text.isBlank()
                        ? null
                        : API.item(Material.NAME_TAG)
                        .name("&a" + text)
                        .lore(List.of("&7Click to submit")))
                .onText((p, text) -> send(p, "&7Text: &f" + text))
                .onSubmit((p, text) -> {
                    p.closeInventory();
                    send(p, "&aSubmitted: &f" + text);
                })
                .open(player);
    }

    private GuiInstance anvilChat(Player player) {
        return API.anvil("&8UIAPI Test: Anvil (chat)")
                .chatOnly()
                .chatMessage(
                        "&6&l{title}",
                        "&e{player}, write a name with at least 3 characters.",
                        "&7Write &fexit &7to cancel."
                )
                .chatInvalidMessage("&cThe name needs at least 3 characters, try again.")
                .chatCancelMessage("&cYou cancelled the chat input.")
                .chatCancelWord("exit")
                .output(text -> text.trim().length() < 3
                        ? null
                        : API.item(Material.NAME_TAG)
                        .name("&a" + text)
                        .onClick(p -> send(p, "&7Output item click handler ran")))
                .onText((p, text) -> send(p, "&7Text: &f" + text))
                .onSubmit((p, text) -> send(p, "&aSubmitted from chat: &f" + text))
                .open(player);
    }

    private GuiInstance furnace(Player player) {
        return API.create("&8UIAPI Test: Furnace", InvType.FURNACE)
                .editable(SlotRole.INPUT)
                .editable(SlotRole.FUEL)
                .slot(SlotRole.OUTPUT, API.item(Material.LIME_DYE)
                        .name("&aRead slots")
                        .onClick(p -> {
                            GuiInstance instance = GuiManager.get(p.getUniqueId());

                            if (instance != null) {
                                send(p, "&eInput: &f" + describe(instance.getContents(SlotRole.INPUT))
                                        + " &eFuel: &f" + describe(instance.getContents(SlotRole.FUEL)));
                            }
                        }))
                .open(player);
    }

    private GuiInstance workbench(Player player) {
        return API.create("&8UIAPI Test: Workbench", InvType.WORKBENCH)
                .editable(SlotRole.INGREDIENT)
                .slot(SlotRole.OUTPUT, API.item(Material.LIME_DYE)
                        .name("&aRead grid")
                        .onClick(p -> {
                            GuiInstance instance = GuiManager.get(p.getUniqueId());

                            if (instance != null) {
                                send(p, "&eIngredients: &f" + describe(instance.getContents(SlotRole.INGREDIENT)));
                            }
                        }))
                .open(player);
    }

    private GuiInstance hopper(Player player) {
        return API.create("&8UIAPI Test: Hopper", InvType.HOPPER)
                .layout("a b c")
                .slot('a', API.item(Material.REDSTONE).name("&cA").onClick(p -> send(p, "&aClicked A")))
                .slot('b', API.item(Material.GLOWSTONE_DUST).name("&eB").onClick(p -> send(p, "&aClicked B")))
                .slot('c', API.closeInv().name("&cClose"))
                .open(player);
    }

    private GuiInstance smithing(Player player) {
        var builder = API.create("&8UIAPI Test: Smithing", InvType.SMITHING)
                .editable(SlotRole.INPUT)
                .editable(SlotRole.SECONDARY_INPUT)
                .slot(SlotRole.OUTPUT, API.item(Material.LIME_DYE)
                        .name("&aRead slots")
                        .onClick(p -> {
                            GuiInstance instance = GuiManager.get(p.getUniqueId());

                            if (instance != null) {
                                send(p, "&eInput: &f" + describe(instance.getContents(SlotRole.INPUT))
                                        + " &eSecondary: &f" + describe(instance.getContents(SlotRole.SECONDARY_INPUT)));
                            }
                        }));

        if (InvType.SMITHING.hasRole(SlotRole.TEMPLATE)) {
            builder.editable(SlotRole.TEMPLATE);
        }

        return builder.open(player);
    }

    private String describe(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return "empty";
        }

        return stacks.stream()
                .map(stack -> stack.getAmount() + "x " + stack.getType().name())
                .collect(Collectors.joining(", "));
    }
}
