package net.impactdev.gts.commands.elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.entries.EntryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntryManagerElement extends CommandElement {

    private final Map<String, EntryManager<?, ?>> entries = Maps.newHashMap();

    public EntryManagerElement(@Nullable Text key) {
        super(key);
        GTSService.getInstance().getGTSComponentManager().getAllEntryManagers().values().forEach(em -> {
            for(String alias : em.getEntryCommandCreator().getAliases()) {
                this.entries.put(alias.toLowerCase(), em);
            }
        });
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String key = args.next();
        EntryManager<?, ?> match = this.entries.get(key);
        if(match == null) {
            throw new ArgumentParseException(Text.of("No matching element"), key, key.length() - 1);
        }

        return match;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return ImmutableList.copyOf(this.entries.keySet());
    }
}
