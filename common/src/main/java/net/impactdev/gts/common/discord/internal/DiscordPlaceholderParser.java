package net.impactdev.gts.common.discord.internal;

import net.impactdev.impactor.api.placeholders.PlaceholderSources;

import java.util.List;
import java.util.function.Supplier;

public interface DiscordPlaceholderParser {

    String getID();

    String parse(PlaceholderSources sources);

}
