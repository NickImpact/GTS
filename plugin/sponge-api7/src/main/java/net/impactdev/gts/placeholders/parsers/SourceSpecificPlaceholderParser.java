package net.impactdev.gts.placeholders.parsers;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class SourceSpecificPlaceholderParser<T> implements PlaceholderParser {

    private final Class<T> sourceType;
    private final String id;
    private final String name;
    private final Function<T, Text> parser;

    public SourceSpecificPlaceholderParser(Class<T> sourceType, String id, String name, Function<T, Text> parser) {
        this.sourceType = sourceType;
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public Text parse(PlaceholderContext context) {
        return context.getAssociatedObject()
                .filter(x -> this.sourceType.isAssignableFrom(x.getClass()))
                .map(this.sourceType::cast)
                .map(this.parser)
                .orElse(Text.EMPTY);
    }

    @Override
    public String getId() {
        return "gts:" + this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Class<T> getSourceType() {
        return this.sourceType;
    }

    public Function<T, Text> getParser() {
        return this.parser;
    }
}
