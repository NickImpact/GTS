package net.impactdev.gts.commands.elements;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PositiveBoundedElement<T extends Number> implements ValueParameter<T> {

    private final NumberParser<T> parser;
    private final Function<String, T> translator;

    public PositiveBoundedElement(Function<String, T> translator) {
        this.translator = translator;
        this.parser = (input, function) -> {
            try {
                return Optional.ofNullable(function.apply(input));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }


    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        return Collections.emptyList();
    }

    @Override
    public Optional<? extends T> parseValue(Parameter.Key<? super T> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        String argument = reader.parseString();
        if(argument.isEmpty()) {
            return Optional.empty();
        }

        Optional<T> result = this.parser.parse(argument, this.translator).filter(value -> value.doubleValue() >= 0);
        if(!result.isPresent()) {
            throw reader.createException(Component.text("Value must be positive"));
        }

        return result;
    }

    @FunctionalInterface
    private interface NumberParser<T extends Number> {

        Optional<T> parse(String input, Function<String, T> translator);

    }
}
