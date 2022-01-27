package net.impactdev.gts.commands.elements;

import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.Config;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;

public class PercentageElement extends CommandElement {

    public PercentageElement(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        try {
            Double.parseDouble(args.peek().replace("%", ""));
        } catch (Exception e) {
            throw args.createError(Text.of("Value is not a valid percentage"));
        }

        Config config = GTSPlugin.getInstance().getConfiguration();
        String argument = args.next().replace("%", "");
        return Math.max(
                config.get(ConfigKeys.AUCTIONS_MIN_INCREMENT_RATE) * 100,
                Math.min(
                        Float.parseFloat(argument),
                        config.get(ConfigKeys.AUCTIONS_MAX_INCREMENT_RATE) * 100
                )
        );
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}
