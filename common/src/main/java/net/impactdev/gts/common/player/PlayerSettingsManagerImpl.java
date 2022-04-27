package net.impactdev.gts.common.player;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerSettingsManagerImpl implements PlayerSettingsManager {

    private final AsyncLoadingCache<UUID, PlayerSettings> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .buildAsync(this::fetch);

    @Override
    public void cache(UUID uuid, PlayerSettings settings) {
        this.cache.put(uuid, CompletableFuture.completedFuture(settings));
        GTSPlugin.instance().storage().applyPlayerSettings(uuid, settings).exceptionally(e -> {
            ExceptionWriter.write(e);
            return null;
        });
    }

    @Override
    public CompletableFuture<PlayerSettings> retrieve(UUID uuid) {
        return this.cache.get(uuid);
    }

    private PlayerSettings fetch(UUID uuid) {
        try {
            return GTSPlugin.instance().storage().getPlayerSettings(uuid)
                    .thenApply(opt -> opt.orElse(PlayerSettings.create()))
                    .exceptionally(e -> {
                        throw new RuntimeException(e);
                    })
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            GTSPlugin.instance().logger().error("Unable to retrieve player settings for " + uuid);
            return PlayerSettings.create();
        }
    }

}
