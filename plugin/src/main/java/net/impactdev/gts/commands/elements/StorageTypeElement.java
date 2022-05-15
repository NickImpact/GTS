package net.impactdev.gts.commands.elements;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.storage.StorageType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

public class StorageTypeElement extends CommandElement {

    public StorageTypeElement(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if(args.peek().isEmpty()) {
            return null;
        }

        String input = args.next();
        try {
            return StorageType.valueOf(input);
        } catch (Exception e) {
            throw new ArgumentParseException(Text.of(e.getMessage()), input, 0);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }
}
