package net.impactdev.gts.common.discord.internal

import java.util.function.Supplier

interface DiscordPlaceholderParser {
    val iD: String
    fun parse(sources: List<Supplier<Any>>): String?
}