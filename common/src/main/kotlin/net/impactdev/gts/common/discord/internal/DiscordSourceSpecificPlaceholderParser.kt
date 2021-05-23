package net.impactdev.gts.common.discord.internal

import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class DiscordSourceSpecificPlaceholderParser<T>(
    private val sourceType: Class<T>,
    override val iD: String,
    private val parser: Function<T, String?>
) : DiscordPlaceholderParser {
    override fun parse(sources: List<Supplier<Any>>): String? {
        return sources.stream()
            .map { obj: Supplier<Any> -> obj.get() }
            .filter { x: Any -> sourceType.isAssignableFrom(x.javaClass) }
            .map { obj: Any? -> sourceType.cast(obj) }
            .map(parser)
            .filter { obj: String? -> Objects.nonNull(obj) }
            .findAny()
            .orElse(null)
    }
}