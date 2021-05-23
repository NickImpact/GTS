package net.impactdev.gts.bungee.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.ProxyServer
import java.util.*

class PlayerDetailFetcher {
    fun getPlayerNameFromUUID(user: UUID?): TextComponent {
        val player = ProxyServer.getInstance().getPlayer(user)
        return Component.text(player.displayName)
    }
}