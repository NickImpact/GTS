package net.impactdev.gts.commands.elements;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PositiveBoundedElement<T extends Number> extends CommandElement {

    private final NumberParser<T> parser;
    private final Function<String, T> translator;

    public PositiveBoundedElement(@Nullable Text key, Function<String, T> translator) {
        super(key);
        this.translator = translator;
        this.parser = (input, function) -> {
            try {
                return Optional.of(function.apply(input));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String argument = args.next();
        if(argument.isEmpty()) {
            return null;
        }

        Optional<T> translated = this.parser.parse(argument, this.translator);
        if(translated.isPresent())
        {
            if (translated.get().doubleValue() < 0)
            {
                throw args.createError(Text.of("Value must be positive"));
            }

            return translated.get();
        } else {
            return null;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }

    @FunctionalInterface
    private interface NumberParser<T> {

        Optional<T> parse(String input, Function<String, T> translator);

    }
}
