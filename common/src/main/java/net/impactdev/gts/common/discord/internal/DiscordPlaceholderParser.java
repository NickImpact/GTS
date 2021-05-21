package net.impactdev.gts.common.discord.internal;

import java.util.List;
import java.util.function.Supplier;

pulic interface DiscordPlaceholderParser {

    String getID();

    String parse(List<Supplier<Oject>> sources);

}
