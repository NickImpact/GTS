package net.impactdev.gts.common.player;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.Map;
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
        GTSPlugin.getInstance().getStorage().applyPlayerSettings(uuid, settings);
    }

    @Override
    public CompletableFuture<PlayerSettings> retrieve(UUID uuid) {
        return this.cache.get(uuid);
    }

    @Override
    public PlayerSettings deserialize(JsonObject object) {
        PlayerSettings.PlayerSettingsBuilder builder = PlayerSettings.builder();
        for(Map.Entry<String, JsonElement> element : object.entrySet()) {
            builder.set(NotificationSetting.valueOf(element.getKey()), element.getValue().getAsBoolean());
        }

        return builder.build();
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
            GTSPlugin.getInstance().getPluginLogger().error("Unable to retrieve player settings for " + uuid);
            return PlayerSettings.create();
        }
    }

}
