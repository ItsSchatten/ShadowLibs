package com.itsschatten.libs;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class InteractiveMessages {

    private BaseComponent current;
    private final List<BaseComponent> parts = new ArrayList<>();

    public InteractiveMessages(@NotNull InteractiveMessages original) {
        current = original.current.duplicate();

        for (final BaseComponent baseComponent : original.parts)
            parts.add(baseComponent.duplicate());
    }

    public InteractiveMessages(String text) {
        current = new TextComponent(TextComponent.fromLegacy(Utils.colorize(text)));
    }

    public InteractiveMessages(@NotNull BaseComponent component) {
        current = component.duplicate();
    }

    public InteractiveMessages append(BaseComponent component) {
        return append(component, FormatRetention.ALL);
    }

    @Contract("_, _ -> this")
    public InteractiveMessages append(@NotNull BaseComponent component, FormatRetention retention) {
        parts.add(current);

        final BaseComponent previous = current;
        current = component.duplicate();
        current.copyFormatting(previous, retention, false);
        return this;
    }

    public InteractiveMessages append(BaseComponent[] components) {
        return append(components, FormatRetention.ALL);
    }

    @Contract("_, _ -> this")
    public InteractiveMessages append(BaseComponent @NotNull [] components, FormatRetention retention) {
        Preconditions.checkArgument(components.length != 0, "No components to append");

        final BaseComponent previous = current;
        for (final BaseComponent component : components) {
            parts.add(current);

            current = component.duplicate();
            current.copyFormatting(previous, retention, false);
        }

        return this;
    }

    public InteractiveMessages append(String text) {
        return append(text, FormatRetention.FORMATTING);
    }

    public InteractiveMessages append(String text, FormatRetention retention) {
        parts.add(current);

        final BaseComponent old = current;
        current = new TextComponent(TextComponent.fromLegacy(Utils.colorize(text)));
        current.copyFormatting(old, retention, false);

        return this;
    }

    public InteractiveMessages onClickRunCmd(String text) {
        return onClick(Action.RUN_COMMAND, text);
    }

    public InteractiveMessages onClickSuggestCmd(String text) {
        return onClick(Action.SUGGEST_COMMAND, text);
    }

    public InteractiveMessages onClick(Action action, String text) {
        current.setClickEvent(new ClickEvent(action, Utils.colorize(text)));
        return this;
    }

    public InteractiveMessages onHover(String text) {
        return onHover(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, text);
    }

    public InteractiveMessages onHover(net.md_5.bungee.api.chat.HoverEvent.Action action, String text) {
        current.setHoverEvent(new HoverEvent(action, new Text(TextComponent.fromLegacy(Utils.colorize(text)))));

        return this;
    }

    public InteractiveMessages retain(FormatRetention retention) {
        current.retain(retention);
        return this;
    }

    public BaseComponent @NotNull [] create() {
        final BaseComponent[] result = parts.toArray(new BaseComponent[parts.size() + 1]);
        result[parts.size()] = current;

        return result;
    }

    public void send(Player @NotNull ... players) {
        final BaseComponent[] comp = create();

        for (final Player player : players)
            player.spigot().sendMessage(comp);
    }

    @Contract("_ -> new")
    public static @NotNull InteractiveMessages builder(String text) {
        return new InteractiveMessages(text);
    }
}