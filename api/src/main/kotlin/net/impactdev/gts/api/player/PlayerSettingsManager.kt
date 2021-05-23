package net.impactdev.gts.api.player

import java.util.*
import java.util.concurrent.CompletableFuture

interface PlayerSettingsManager {
    /**
     * Caches the set of settings to a binding UUID.
     *
     * @param uuid The ID that will be used to map to this set of settings
     * @param settings The actual settings applied by a player
     */
    fun cache(uuid: UUID?, settings: PlayerSettings?)

    /**
     * Attempts to locate a set of player settings from the internal cache. A server will maintain
     * its own cache in regards to player settings as a means of assuring they are within quick access
     * of a server's request for information.
     *
     *
     * Unless populated via other means, this call will never attempt to lookup offline player data.
     *
     * @param uuid The ID of the user we wish to find individual player settings for
     * @return An async wrapped operation responsible for retrieving a set of player settings
     */
    fun retrieve(uuid: UUID?): CompletableFuture<PlayerSettings?>?
}