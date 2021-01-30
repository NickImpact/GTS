package net.impactdev.gts.placeholders.parsers;

import net.impactdev.gts.sponge.utils.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class SourceSpecificPlaceholderParser<T> implements PlaceholderParser {

    private final Class<T> sourceType;
    private final String id;
    private final String name;
    private final Function<T, TextComponent> parser;

    public SourceSpecificPlaceholderParser(Class<T> sourceType, String id, String name, Function<T, TextComponent> parser) {
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
                .map(this.parser.andThen(Utilities::translateComponent))
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

    public Function<T, TextComponent> getParser() {
        return this.parser;
    }

    public static class Decorative<T> extends SourceSpecificPlaceholderParser<T> {

        public Decorative(Class<T> sourceType, String id, String name, Function<T, TextComponent> parser) {
            super(sourceType, id, name, parser.andThen(component -> component.append(Component.text().style(Style.empty()))));
        }

    }
}
