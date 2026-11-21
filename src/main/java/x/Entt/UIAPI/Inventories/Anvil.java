package x.Entt.UIAPI.Inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import x.Entt.UIAPI.GUI.GuiInstance;
import x.Entt.UIAPI.GUI.SlotRole;
import x.Entt.UIAPI.Item.Item;
import x.Entt.UIAPI.UIAPI;
import x.Entt.UIAPI.Utils.Text;

import static x.Entt.UIAPI.UIAPI.prefix;

public final class Anvil extends InvType implements Listener {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 2;

    private static final String DEFAULT_PROMPT = "&eWrite the text in the chat to continue.";
    private static final String DEFAULT_INVALID = "&cThat text is not valid, write another one in the chat.";
    private static final String DEFAULT_CANCELLED = "&cCancelled.";

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public record Config(
            String initialText,
            Function<String, Item> output,
            BiConsumer<Player, String> onText,
            BiConsumer<Player, String> onSubmit,
            List<String> chatMessage,
            String chatInvalidMessage,
            String chatCancelMessage,
            String chatCancelWord,
            boolean chatOnly
    ) {
        public Config {
            chatMessage = chatMessage == null ? null : List.copyOf(chatMessage);
        }

        public boolean usesText() {
            return chatOnly || initialText != null || output != null || onText != null || onSubmit != null;
        }

        boolean hasCancelWord() {
            return chatCancelWord != null && !chatCancelWord.isBlank();
        }
    }

    private enum Mode {
        VIRTUAL,
        CHAT
    }

    private static final class Session {
        final GuiInstance instance;
        final Mode mode;
        final InventoryView view;
        String text;
        boolean submitting;

        Session(GuiInstance instance, Mode mode, InventoryView view, String text) {
            this.instance = instance;
            this.mode = mode;
            this.view = view;
            this.text = text;
        }
    }

    Anvil() {
        super("ANVIL", 3, linear(3, 3));
        role(SlotRole.INPUT, INPUT_SLOT);
        role(SlotRole.SECONDARY_INPUT, 1);
        role(SlotRole.OUTPUT, OUTPUT_SLOT);
    }

    public static boolean isVirtualSupported() {
        return Bridge.available();
    }

    @Override
    public Inventory create(GuiInstance instance) {
        Config config = instance.getDefinition().getAnvil();

        if (config == null || !config.usesText()) {
            return super.create(instance);
        }

        Player player = instance.getPlayer();
        String text = config.initialText() == null ? "" : config.initialText();

        InventoryView view = config.chatOnly()
                ? null
                : Bridge.create(player, Text.color(instance.getDefinition().getTitle()));

        if (view == null) {
            sessions.put(player.getUniqueId(), new Session(instance, Mode.CHAT, null, text));
            return null;
        }

        sessions.put(player.getUniqueId(), new Session(instance, Mode.VIRTUAL, view, text));
        return view.getTopInventory();
    }

    @Override
    public void show(GuiInstance instance) {
        Session session = session(instance);

        if (session == null) {
            super.show(instance);
            return;
        }

        Player player = instance.getPlayer();

        if (session.mode == Mode.VIRTUAL) {
            player.openInventory(session.view);
            return;
        }

        player.closeInventory();
        sendPrompt(session);
    }

    @Override
    public void render(GuiInstance instance) {
        Session session = session(instance);

        if (session == null || session.mode != Mode.VIRTUAL) {
            return;
        }

        boolean hasInput = instance.getDefinition().getRoleItems().containsKey(SlotRole.INPUT);

        if (!hasInput && !instance.getDefinition().isInputSlot(INPUT_SLOT)) {
            Config config = instance.getDefinition().getAnvil();
            String name = config.initialText() == null || config.initialText().isEmpty()
                    ? " "
                    : config.initialText();

            instance.setSlot(INPUT_SLOT, Item.of(Material.PAPER).name(name));
        }

        instance.setSlot(OUTPUT_SLOT, resolveOutput(session));
    }

    @Override
    public void onClick(GuiInstance instance, InventoryClickEvent event) {
        Session session = session(instance);

        if (session == null || session.mode != Mode.VIRTUAL) {
            return;
        }

        Inventory clicked = event.getClickedInventory();

        if (clicked == null || !instance.owns(clicked) || event.getSlot() != OUTPUT_SLOT) {
            return;
        }

        ItemStack result = event.getCurrentItem();

        if (result == null || result.getType().isAir()) {
            return;
        }

        event.setCancelled(true);

        if (session.submitting) {
            return;
        }

        String text = Bridge.renameText(session.view);

        if (text != null) {
            session.text = text;
        }

        session.submitting = true;
        event.setCurrentItem(null);
        instance.getPlayer().updateInventory();

        Bukkit.getScheduler().runTask(UIAPI.getInstance(), () -> {
            try {
                submit(session);
            } finally {
                session.submitting = false;
            }

            if (!instance.isClosed() && session(instance) == session) {
                instance.update();
            }
        });
    }

    @Override
    public void onClose(GuiInstance instance) {
        UUID id = instance.getPlayer().getUniqueId();
        Session session = sessions.get(id);

        if (session != null && session.instance == instance) {
            sessions.remove(id, session);
        }
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        Session session = find(event);

        if (session == null) {
            return;
        }

        GuiInstance instance = session.instance;
        Config config = instance.getDefinition().getAnvil();

        String text = Bridge.renameText(session.view);

        if (text != null && !text.equals(session.text)) {
            session.text = text;

            if (config.onText() != null) {
                config.onText().accept(instance.getPlayer(), text);
            }
        }

        Item output = resolveOutput(session);

        instance.trackItem(OUTPUT_SLOT, output);
        event.setResult(output == null ? null : output.build());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());

        if (session == null || session.mode != Mode.CHAT) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();

        Bukkit.getScheduler().runTask(UIAPI.getInstance(), () -> handleChat(session, message));
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Session session = sessions.get(id);

        if (session != null && session.mode == Mode.CHAT && sessions.remove(id, session)) {
            session.instance.handleClose();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Session session = sessions.remove(id);

        if (session != null) {
            session.instance.handleClose();
        }
    }

    private void handleChat(Session session, String message) {
        GuiInstance instance = session.instance;
        Player player = instance.getPlayer();
        UUID id = player.getUniqueId();

        if (instance.isClosed() || sessions.get(id) != session) {
            return;
        }

        Config config = instance.getDefinition().getAnvil();

        if (config.hasCancelWord() && message.trim().equalsIgnoreCase(config.chatCancelWord().trim())) {
            sessions.remove(id, session);
            player.sendMessage(format(session, config.chatCancelMessage() == null
                    ? prefix + DEFAULT_CANCELLED
                    : config.chatCancelMessage()));
            instance.handleClose();
            return;
        }

        session.text = message;

        if (config.onText() != null) {
            config.onText().accept(player, message);
        }

        if (instance.isClosed() || sessions.get(id) != session) {
            return;
        }

        Item output = resolveOutput(session);

        if (config.output() != null && output == null) {
            player.sendMessage(format(session, config.chatInvalidMessage() == null
                    ? prefix + DEFAULT_INVALID
                    : config.chatInvalidMessage()));
            return;
        }

        sessions.remove(id, session);

        if (output != null) {
            output.handleClick(player, ClickType.LEFT);
        }

        if (!instance.isClosed()) {
            submit(session);
        }

        instance.handleClose();
    }

    private void sendPrompt(Session session) {
        Config config = session.instance.getDefinition().getAnvil();
        Player player = session.instance.getPlayer();

        if (config.chatMessage() != null) {
            for (String line : config.chatMessage()) {
                player.sendMessage(format(session, line));
            }
            return;
        }

        player.sendMessage(format(session, "{title}"));
        player.sendMessage(format(session, prefix + DEFAULT_PROMPT));

        if (config.hasCancelWord()) {
            player.sendMessage(format(session, prefix + "&7Type &f" + config.chatCancelWord().trim() + " &7to close."));
        }
    }

    private String format(Session session, String line) {
        GuiInstance instance = session.instance;

        return Text.color(line
                .replace("{player}", instance.getPlayer().getName())
                .replace("{title}", instance.getDefinition().getTitle())
                .replace("{text}", session.text == null ? "" : session.text));
    }

    private void submit(Session session) {
        GuiInstance instance = session.instance;

        if (instance.isClosed()) {
            return;
        }

        Config config = instance.getDefinition().getAnvil();

        if (config.onSubmit() != null) {
            config.onSubmit().accept(instance.getPlayer(), session.text);
        }
    }

    private Session session(GuiInstance instance) {
        Session session = sessions.get(instance.getPlayer().getUniqueId());

        return session != null && session.instance == instance ? session : null;
    }

    private Session find(PrepareAnvilEvent event) {
        for (HumanEntity viewer : event.getViewers()) {
            Session session = sessions.get(viewer.getUniqueId());

            if (session != null
                    && session.mode == Mode.VIRTUAL
                    && session.instance.owns(event.getInventory())) {
                return session;
            }
        }

        return null;
    }

    private Item resolveOutput(Session session) {
        GuiInstance instance = session.instance;
        Config config = instance.getDefinition().getAnvil();

        if (config.output() != null) {
            return config.output().apply(session.text);
        }

        return instance.getDefinition().getRoleItems().get(SlotRole.OUTPUT);
    }

    private static final class Bridge {
        private static final Object TYPE;
        private static final Method CREATE;
        private static final Method RENAME;

        static {
            Object type = null;
            Method create = null;
            Method rename = null;

            try {
                type = Class.forName("org.bukkit.inventory.MenuType")
                        .getField("ANVIL")
                        .get(null);

                create = Class.forName("org.bukkit.inventory.MenuType$Typed")
                        .getMethod("create", HumanEntity.class, String.class);

                rename = Class.forName("org.bukkit.inventory.view.AnvilView")
                        .getMethod("getRenameText");

            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                type = null;
                create = null;
                rename = null;
            }

            TYPE = type;
            CREATE = create;
            RENAME = rename;
        }

        static boolean available() {
            return TYPE != null && CREATE != null && RENAME != null;
        }

        static InventoryView create(Player player, String title) {
            if (!available()) {
                return null;
            }

            try {
                return (InventoryView) CREATE.invoke(TYPE, player, title);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
        }

        static String renameText(InventoryView view) {
            if (RENAME == null || view == null) {
                return null;
            }

            try {
                return (String) RENAME.invoke(view);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
        }
    }
}
