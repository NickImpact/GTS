package net.impactdev.gts.api.registries.players;

import net.impactdev.gts.api.players.PlayerPreferences;
import net.impactdev.gts.api.registries.Registry;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides a registry of player settings. This registry adopts an asynchronous loading cache
 * with an expiration rate of configurable length.
 */
public interface PlayerSettingsRegistry extends Registry {

    void apply(PlayerPreferences settings);

    /**
     * Locates a set of settings representing a player's preferences for the plugin on their end.
     * If not presently cached, this will create a new set of preferences for the user before
     * appending these settings to the cache. As this call may need to attempt to load a set of
     * settings first from the given storage provider, this call will run asynchronously as to
     * not affect the server thread.
     *
     * @param user The unique ID of the user being queried against
     * @return A set of preferences based on the player's desire.
     */
    CompletableFuture<PlayerPreferences> findOrCreate(UUID user);

}
