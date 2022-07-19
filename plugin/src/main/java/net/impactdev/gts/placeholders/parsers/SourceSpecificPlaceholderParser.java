package net.impactdev.gts.placeholders.parsers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.spongepowered.api.placeholder.PlaceholderContext;

import java.util.function.Function;

public class SourceSpecificPlaceholderParser<T> implements IdentifiableParser {

    private final Class<T> sourceType;
    private final String id;
    private final Function<T, Component> parser;

    public SourceSpecificPlaceholderParser(Class<T> sourceType, String id, Function<T, Component> parser) {
        this.sourceType = sourceType;
        this.id = id;
        this.parser = parser;
    }

    @Override
    public Component parse(PlaceholderContext context) {
        return context.associatedObject()
                .filter(x -> this.sourceType.isAssignableFrom(x.getClass()))
                .map(this.sourceType::cast)
                .map(this.parser)
                .orElse(Component.empty());
    }

    public Class<T> getSourceType() {
        return this.sourceType;
    }

    public Function<T, Component> getParser() {
        return this.parser;
    }

    @Override
    public String key() {
        return this.id;
    }

    public static class Decorative<T> extends SourceSpecificPlaceholderParser<T> {

        public Decorative(Class<T> sourceType, String id, Function<T, Component> parser) {
            super(sourceType, id, parser.andThen(component -> component.append(Component.text().style(Style.empty()))));
        }

    }
}
