package net.impactdev.gts.api.translations;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public interface Message {

    TextComponent OPEN = text("(");
    TextComponent CLOSE = text(")");

    ComponentLike PREFIX = translatable()
            .key("gts.plugin.prefix")
            .build();


}
