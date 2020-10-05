package net.impactdev.gts.api.player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerSettingsManager {

    /**
     * Caches the set of settings to a binding UUID.
     *
     * @param uuid The ID that will be used to map to this set of settings
     * @param settings The actual settings applied by a player
     */
    void cache(UUID uuid, PlayerSettings settings);

    /**
     * Attempts to locate a set of player settings from the internal cache. A server will maintain
     * its own cache in regards to player settings as a means of assuring they are within quick access
     * of a server's request for information.
     *
     * <p>Unless populated via other means, this call will never attempt to lookup offline player data.</p>
     *
     * @param uuid The ID of the user we wish to find individual player settings for
     * @return A set of player settings if any are available, otherwise, an empty option.
     */
    Optional<PlayerSettings> retrieve(UUID uuid);

    /**
     * Attempts to retrieve a set of player settings for a particular player's UUID via an async
     * thread separate from the main server thread.
     *
     * <p>NOTE: This should be what is used when trying to access offline player settings.
     * Unlike {@link #retrieve(UUID)}, this call will access the database for information
     * rather than simply ask the internally loaded cache.</p>
     *
     * @param uuid The ID of the user we wish to find individual player settings for
     * @return A completable future run on an async thread separate from the main server thread
     * that will result in an optionally filed set of settings specific to a player.
     */
    CompletableFuture<Optional<PlayerSettings>> retrieveAsync(UUID uuid);

}
