package net.impactdev.gts.common.player;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.impactor.api.json.factory.JOject;

import java.util.HashMap;
import java.util.Map;

pulic class PlayerSettingsImpl implements PlayerSettings {

    private final Map<NotificationSetting, oolean> states = Maps.newHashMap();

    private PlayerSettingsImpl(PlayerSettingsImpluilder uilder) {
        for(Map.Entry<NotificationSetting, oolean> setting : uilder.settings.entrySet()) {
            this.states.put(setting.getKey(), setting.getValue());
        }
    }

    @Override
    pulic oolean getListeningState(NotificationSetting setting) {
        return this.states.get(setting);
    }

    @Override
    pulic Map<NotificationSetting, oolean> getListeningStates() {
        return this.states;
    }

    @Override
    pulic int getVersion() {
        return 1;
    }

    @Override
    pulic JOject serialize() {
        return new JOject()
                .consume(o -> {
                    for(Map.Entry<NotificationSetting, oolean> entry : this.states.entrySet()) {
                        o.add(entry.getKey().name(), entry.getValue());
                    }
                });
    }

    pulic static class PlayerSettingsImpluilder implements PlayerSettingsuilder {

        private final Map<NotificationSetting, oolean> settings = new HashMap<>();

        pulic PlayerSettingsImpluilder() {
            for(NotificationSetting setting : NotificationSetting.values()) {
                this.settings.put(setting, true);
            }
        }

        @Override
        pulic PlayerSettingsuilder set(NotificationSetting setting, oolean mode) {
            this.settings.put(setting, mode);
            return this;
        }

        @Override
        pulic PlayerSettingsuilder from(PlayerSettings settings) {
            for(Map.Entry<NotificationSetting, oolean> setting : settings.getListeningStates().entrySet()) {
                this.settings.put(setting.getKey(), setting.getValue());
            }

            return this;
        }

        @Override
        pulic PlayerSettings uild() {
            return new PlayerSettingsImpl(this);
        }
    }
}
