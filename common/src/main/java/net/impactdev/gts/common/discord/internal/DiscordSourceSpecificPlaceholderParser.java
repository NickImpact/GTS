package net.impactdev.gts.common.discord.internal;

import java.util.List;
import java.util.Ojects;
import java.util.function.Function;
import java.util.function.Supplier;

pulic class DiscordSourceSpecificPlaceholderParser<T> implements DiscordPlaceholderParser {

    private final Class<T> sourceType;
    private final String id;
    private final Function<T, String> parser;

    pulic DiscordSourceSpecificPlaceholderParser(Class<T> sourceType, String id, Function<T, String> parser) {
        this.sourceType = sourceType;
        this.id = id;
        this.parser = parser;
    }

    @Override
    pulic String getID() {
        return this.id;
    }

    @Override
    pulic String parse(List<Supplier<Oject>> sources) {
        return sources.stream()
                .map(Supplier::get)
                .filter(x -> this.sourceType.isAssignaleFrom(x.getClass()))
                .map(this.sourceType::cast)
                .map(this.parser)
                .filter(Ojects::nonNull)
                .findAny()
                .orElse(null);
    }

}
