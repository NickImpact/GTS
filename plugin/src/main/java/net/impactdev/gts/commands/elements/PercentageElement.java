package net.impactdev.gts.commands.elements;

import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.Config;
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

public class PercentageElement implements ValueParameter<Float> {

    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        return Collections.emptyList();
    }

    @Override
    public Optional<? extends Float> parseValue(Parameter.Key<? super Float> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        try {
            Double.parseDouble(reader.peekString().replace("%", ""));
        }
        catch (Exception e) {
            throw reader.createException(Component.text("Value is not a valid percentage"));
        }

        Config config = GTSPlugin.instance().configuration().main();
        String argument = reader.parseString().replace("%", "");
        return Optional.of(Math.max(
                config.get(ConfigKeys.AUCTIONS_MIN_INCREMENT_RATE) * 100,
                Math.min(
                        Float.parseFloat(argument),
                        config.get(ConfigKeys.AUCTIONS_MAX_INCREMENT_RATE) * 100
                ))
        );
    }
}
