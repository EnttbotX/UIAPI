package x.Entt.UIAPI.GUI.Builders;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import x.Entt.UIAPI.Inventories.Anvil;
import x.Entt.UIAPI.Inventories.InvType;
import x.Entt.UIAPI.GUI.SlotRole;
import x.Entt.UIAPI.Item.Item;

public class AnvilBuilder extends BaseBuilder<AnvilBuilder> {

    private String initialText;
    private Function<String, Item> dynamicOutput;
    private BiConsumer<Player, String> onText;
    private BiConsumer<Player, String> onSubmit;
    private List<String> chatMessage;
    private String chatInvalidMessage;
    private String chatCancelMessage;
    private String chatCancelWord = "cancel";
    private boolean chatOnly;

    public AnvilBuilder(String title) {
        super(title, InvType.ANVIL);
    }

    @Override
    protected AnvilBuilder self() {
        return this;
    }

    public AnvilBuilder input(Item item) {
        return slot(SlotRole.INPUT, item);
    }

    public AnvilBuilder secondInput(Item item) {
        return slot(SlotRole.SECONDARY_INPUT, item);
    }

    public AnvilBuilder output(Item item) {
        return slot(SlotRole.OUTPUT, item);
    }

    public AnvilBuilder output(Function<String, Item> provider) {
        this.dynamicOutput = provider;
        return this;
    }

    public AnvilBuilder text(String text) {
        this.initialText = text;
        return this;
    }

    public AnvilBuilder onText(BiConsumer<Player, String> callback) {
        this.onText = callback;
        return this;
    }

    public AnvilBuilder onSubmit(BiConsumer<Player, String> callback) {
        this.onSubmit = callback;
        return this;
    }

    public AnvilBuilder chatMessage(String... lines) {
        this.chatMessage = lines == null || lines.length == 0 ? null : List.of(lines);
        return this;
    }

    public AnvilBuilder chatInvalidMessage(String message) {
        this.chatInvalidMessage = message;
        return this;
    }

    public AnvilBuilder chatCancelMessage(String message) {
        this.chatCancelMessage = message;
        return this;
    }

    public AnvilBuilder chatCancelWord(String word) {
        this.chatCancelWord = word;
        return this;
    }

    public AnvilBuilder chatOnly() {
        this.chatOnly = true;
        return this;
    }

    @Override
    protected Anvil.Config anvil() {
        return new Anvil.Config(
                initialText,
                dynamicOutput,
                onText,
                onSubmit,
                chatMessage,
                chatInvalidMessage,
                chatCancelMessage,
                chatCancelWord,
                chatOnly
        );
    }
}