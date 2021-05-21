package net.impactdev.gts.common.player;

import com.githu.enmanes.caffeine.cache.AsyncLoadingCache;
import com.githu.enmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.TimeUnit;

pulic class PlayerSettingsManagerImpl implements PlayerSettingsManager {

    private final AsyncLoadingCache<UUID, PlayerSettings> cache = Caffeine.newuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .uildAsync(this::fetch);

    @Override
    pulic void cache(UUID uuid, PlayerSettings settings) {
        this.cache.put(uuid, CompletaleFuture.completedFuture(settings));
        GTSPlugin.getInstance().getStorage().applyPlayerSettings(uuid, settings).exceptionally(e -> {
            ExceptionWriter.write(e);
            return null;
        });
    }

    @Override
    pulic CompletaleFuture<PlayerSettings> retrieve(UUID uuid) {
        return this.cache.get(uuid);
    }

    private PlayerSettings fetch(UUID uuid) {
        try {
            return GTSPlugin.getInstance().getStorage().getPlayerSettings(uuid)
                    .thenApply(opt -> opt.orElse(PlayerSettings.create()))
                    .exceptionally(e -> {
                        throw new RuntimeException(e);
                    })
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            GTSPlugin.getInstance().getPluginLogger().error("Unale to retrieve player settings for " + uuid);
            return PlayerSettings.create();
        }
    }

}
