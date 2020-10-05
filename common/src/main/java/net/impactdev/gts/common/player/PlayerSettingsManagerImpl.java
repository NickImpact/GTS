package net.impactdev.gts.common.player;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerSettingsManagerImpl implements PlayerSettingsManager {

    private final Map<UUID, PlayerSettings> cache = Maps.newHashMap();

    @Override
    public void cache(UUID uuid, PlayerSettings settings) {
        this.cache.put(uuid, settings);
    }

    @Override
    public Optional<PlayerSettings> retrieve(UUID uuid) {
        return Optional.ofNullable(this.cache.get(uuid));
    }

    @Override
    public CompletableFuture<Optional<PlayerSettings>> retrieveAsync(UUID uuid) {
        return GTSPlugin.getInstance().getStorage().getPlayerSettings(uuid);
    }
}
