package net.impactdev.gts.common.player;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.impactor.api.json.factory.JObject;

import java.util.HashMap;
import java.util.Map;

public class PlayerSettingsImpl implements PlayerSettings {

    private final Map<NotificationSetting, Boolean> states = Maps.newHashMap();

    private PlayerSettingsImpl(PlayerSettingsImplBuilder builder) {
        for(Map.Entry<NotificationSetting, Boolean> setting : builder.settings.entrySet()) {
            this.states.put(setting.getKey(), setting.getValue());
        }
    }

    @Override
    public boolean getListeningState(NotificationSetting setting) {
        return this.states.get(setting);
    }

    @Override
    public Map<NotificationSetting, Boolean> getListeningStates() {
        return this.states;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject()
                .consume(o -> {
                    for(Map.Entry<NotificationSetting, Boolean> entry : this.states.entrySet()) {
                        o.add(entry.getKey().name(), entry.getValue());
                    }
                });
    }

    public static class PlayerSettingsImplBuilder implements PlayerSettingsBuilder {

        private final Map<NotificationSetting, Boolean> settings = new HashMap<>();

        public PlayerSettingsImplBuilder() {
            for(NotificationSetting setting : NotificationSetting.values()) {
                this.settings.put(setting, true);
            }
        }

        @Override
        public PlayerSettingsBuilder set(NotificationSetting setting, boolean mode) {
            this.settings.put(setting, mode);
            return this;
        }

        @Override
        public PlayerSettingsBuilder from(PlayerSettings settings) {
            for(Map.Entry<NotificationSetting, Boolean> setting : settings.getListeningStates().entrySet()) {
                this.settings.put(setting.getKey(), setting.getValue());
            }

            return this;
        }

        @Override
        public PlayerSettings build() {
            return new PlayerSettingsImpl(this);
        }
    }
}
