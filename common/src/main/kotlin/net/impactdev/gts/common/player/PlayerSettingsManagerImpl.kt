package net.impactdev.gts.common.player

import com.github.benmanes.caffeine.cache.Caffeine
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.player.PlayerSettings.Companion.create
import net.impactdev.gts.api.player.PlayerSettingsManager
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import java.util.*
import java.util.concurrent.*
import java.util.function.Function

class PlayerSettingsManagerImpl : PlayerSettingsManager {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .buildAsync<UUID?, PlayerSettings?> { uuid: UUID? -> fetch(uuid) }

    override fun cache(uuid: UUID?, settings: PlayerSettings?) {
        cache.put(uuid!!, CompletableFuture.completedFuture(settings))
        GTSPlugin.Companion.getInstance().getStorage().applyPlayerSettings(uuid, settings)
            .exceptionally(Function<Throwable, Boolean?> { e: Throwable? ->
                ExceptionWriter.write(e)
                null
            })
    }

    override fun retrieve(uuid: UUID?): CompletableFuture<PlayerSettings?>? {
        return cache[uuid!!]
    }

    private fun fetch(uuid: UUID?): PlayerSettings? {
        return try {
            GTSPlugin.Companion.getInstance().getStorage().getPlayerSettings(uuid)
                .thenApply(Function { opt: Optional<PlayerSettings?> -> opt.orElse(create()) })
                .exceptionally(Function<Throwable, PlayerSettings> { e: Throwable? -> throw RuntimeException(e) })
                .get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            GTSPlugin.Companion.getInstance().getPluginLogger().error("Unable to retrieve player settings for $uuid")
            create()
        }
    }
}