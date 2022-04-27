package net.impactdev.gts.commands.elements;

import net.impactdev.impactor.api.utilities.Time;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TimeElement implements ValueParameter<Time> {

    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        return Collections.emptyList();
    }

    @Override
    public Optional<? extends Time> parseValue(Parameter.Key<? super Time> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        return Optional.ofNullable(reader.parseString())
                .map(Time::new);
    }

}
