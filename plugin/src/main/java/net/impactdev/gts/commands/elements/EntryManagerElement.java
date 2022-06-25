package net.impactdev.gts.commands.elements;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntryManagerElement implements ValueParameter<EntryManager<?>> {

    private final Map<String, EntryManager<?>> entries = Maps.newHashMap();

    public EntryManagerElement() {
        GTSService.getInstance().getGTSComponentManager().getAllEntryManagers().values().forEach(em -> {
            for(String alias : em.getEntryCommandCreator().getAliases()) {
                this.entries.put(alias.toLowerCase(), em);
            }
        });
    }

    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        //return ImmutableList.copyOf(this.entries.keySet());
        return Collections.emptyList();
    }

    @Override
    public Optional<? extends EntryManager<?>> parseValue(Parameter.Key<? super EntryManager<?>> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        String key = reader.parseString();
        EntryManager<?> match = this.entries.get(key);
        if(match == null) {
            throw new ArgumentParseException(Component.text("No matching element"), key, key.length() - 1);
        }

        return Optional.of(match);
    }
}
