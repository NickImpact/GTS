package net.impactdev.gts.common.discord.internal;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiscordSourceSpecificPlaceholderParser<T> implements DiscordPlaceholderParser {

    private final Class<T> sourceType;
    private final String id;
    private final Function<T, String> parser;

    public DiscordSourceSpecificPlaceholderParser(Class<T> sourceType, String id, Function<T, String> parser) {
        this.sourceType = sourceType;
        this.id = id;
        this.parser = parser;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public String parse(List<Supplier<Object>> sources) {
        return sources.stream()
                .map(Supplier::get)
                .filter(x -> this.sourceType.isAssignableFrom(x.getClass()))
                .map(this.sourceType::cast)
                .map(this.parser)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

}
