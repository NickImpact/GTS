package net.impactdev.gts.common.discord

import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.plugin.GTSPlugin
import java.awt.Color

class DiscordOption(val descriptor: String, val color: Color, val webhookChannels: List<String>) {

    enum class Options {
        List_BIN, List_Auction, Purchase, Remove, Bid, Claim
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun fetch(option: Options?): DiscordOption {
            return GTSPlugin.Companion.getInstance().getConfiguration().get(ConfigKeys.DISCORD_LINKS).get(option)
        }
    }
}