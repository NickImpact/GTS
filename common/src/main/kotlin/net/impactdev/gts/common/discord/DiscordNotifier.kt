package net.impactdev.gts.common.discord

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.gts.api.util.ThrowingRunnable
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.discord.Embed
import net.impactdev.gts.common.discord.internal.DiscordPlaceholderParser
import net.impactdev.gts.common.discord.internal.DiscordSourceSpecificPlaceholderParser
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.EconomicFormatter
import net.impactdev.gts.common.utils.datetime.DateTimeFormatUtils
import net.impactdev.gts.common.utils.future.CompletableFutureManager
import net.impactdev.gts.common.utils.lang.StringComposer
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.configuration.ConfigKey
import net.impactdev.impactor.api.services.text.MessageService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set

class DiscordNotifier(private val plugin: GTSPlugin) {
    private var parser: MessageParser? = null
    fun forgeMessage(
        option: DiscordOption,
        template: ConfigKey<List<String>>?,
        listing: Listing,
        vararg additional: Any
    ): Message? {
        Preconditions.checkArgument(!listing.entry.details!!.isEmpty(), "Details must be specified for an entry")
        val sources: MutableList<Supplier<Any>> = Lists.newArrayList()
        sources.add(Supplier { listing })
        for (o in additional) {
            sources.add(Supplier { o })
        }
        val base = Field("Listing Information", parser!!.interpret(plugin.msgConfig.get(template), sources), true)
        val entry = Field(
            StringComposer.readNameFromComponent(listing.entry.name),
            StringComposer.composeListAsString(listing.entry.details),
            true
        )
        val embed: Embed.Builder = Embed.Companion.builder()
            .title(option.descriptor)
            .color(option.color.rgb)
            .timestamp(LocalDateTime.now())
            .field(base)
            .field(entry)
        listing.entry.thumbnailURL!!.ifPresent { thumbnail: String? -> embed.thumbnail(thumbnail) }
        return Message(
            plugin.configuration.get(ConfigKeys.DISCORD_TITLE),
            plugin.configuration.get(ConfigKeys.DISCORD_AVATAR),
            option
        ).addEmbed(embed.build())
    }

    fun sendMessage(message: Message): CompletableFuture<Void?>? {
        return CompletableFutureManager.makeFuture(ThrowingRunnable {
            if (plugin.configuration.get(ConfigKeys.DISCORD_LOGGING_ENABLED)) {
                val URLS = message.webhooks
                for (URL in URLS!!) {
                    plugin.pluginLogger.debug("[WebHook-Debug] Sending webhook payload to $URL")
                    plugin.pluginLogger.debug("[WebHook-Debug] Payload: " + message.jsonString)
                    val connection = message.send(URL)
                    val status = connection!!.responseCode
                    plugin.pluginLogger.debug("[WebHook-Debug] Payload info received, status code: $status")
                }
            }
        })
    }

    private fun initialize() {
        parser = MessageParser()
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Listing::class.java,
            "discord:listing_id"
        ) { listing: Listing -> listing.iD.toString() })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Listing::class.java,
            "discord:publisher"
        ) { listing: Listing -> GTSPlugin.Companion.getInstance().getPlayerDisplayName(listing.lister) })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Listing::class.java,
            "discord:publisher_id"
        ) { listing: Listing -> listing.lister.toString() })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            BuyItNow::class.java,
            "discord:price"
        ) { listing: BuyItNow ->
            val price = listing.price!!.text
            val out = StringBuilder(price!!.content())
            for (child in price.children().stream().filter { x: Component? -> x is TextComponent }
                .collect(Collectors.toList())) {
                out.append((child as TextComponent).content())
            }
            out.toString()
        })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Auction::class.java,
            "discord:starting_bid"
        ) { auction: Auction ->
            Impactor.getInstance().registry.get(
                EconomicFormatter::class.java
            ).format(auction.startingPrice)
        })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Listing::class.java,
            "discord:expiration"
        ) { obj: Listing? -> DateTimeFormatUtils.formatExpiration() })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            UUID::class.java,
            "discord:actor"
        ) { actor: UUID? -> GTSPlugin.Companion.getInstance().getPlayerDisplayName(actor) })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            UUID::class.java,
            "discord:actor_id"
        ) { obj: UUID -> obj.toString() })
        parser!!.addPlaceholder(DiscordSourceSpecificPlaceholderParser(
            Double::class.java,
            "discord:bid"
        ) { amount: Double ->
            Impactor.getInstance().registry.get(
                EconomicFormatter::class.java
            ).format(amount)
        })
    }

    private class MessageParser : MessageService<String> {
        private val placeholders: MutableMap<String?, DiscordPlaceholderParser> = Maps.newHashMap()
        fun interpret(base: List<String>, sources: List<Supplier<Any>>?): String? {
            val out: MutableList<String?> = Lists.newArrayList()
            for (s in base) {
                out.add(this.parse(s, sources))
            }
            return StringComposer.composeListAsString(out)
        }

        override fun parse(message: String, sources: List<Supplier<Any>>): String {
            val matcher = TOKEN_LOCATOR.matcher(message)
            val result = AtomicReference(message)
            while (matcher.find()) {
                val placeholder = matcher.group(1)
                Optional.ofNullable(placeholders[placeholder.toLowerCase()])
                    .ifPresent { parser: DiscordPlaceholderParser ->
                        val out = parser.parse(sources)
                        if (out != null) {
                            result.set(result.get().replace("{{$placeholder}}", out))
                        }
                    }
            }
            return result.get()
        }

        override fun getServiceName(): String {
            return "Discord Message Service Populator"
        }

        fun addPlaceholder(parser: DiscordPlaceholderParser) {
            placeholders[parser.id] = parser
        }

        companion object {
            private val TOKEN_LOCATOR = Pattern.compile("[{][{]([\\w-:]+)[}][}]")
        }
    }

    init {
        initialize()
    }
}