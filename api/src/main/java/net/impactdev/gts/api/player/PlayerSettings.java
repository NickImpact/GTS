package net.impactdev.gts.api.player;

import net.impactdev.gts.api.data.Storable;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;

import java.util.Map;

public interface PlayerSettings extends Storable {

    static PlayerSettings create() {
        return builder().build();
    }

    boolean getListeningState(NotificationSetting setting);

    Map<NotificationSetting, Boolean> getListeningStates();

    default PlayerSettings set(NotificationSetting setting, boolean mode) {
        return builder().from(this).set(setting, mode).build();
    }

    default boolean getPublishListenState() {
        return this.getListeningState(NotificationSetting.Publish);
    }

    default boolean getSoldListenState() {
        return this.getListeningState(NotificationSetting.Sold);
    }

    default boolean getBidListenState() {
        return this.getListeningState(NotificationSetting.Bid);
    }

    default boolean getOutbidListenState() {
        return this.getListeningState(NotificationSetting.Outbid);
    }

    static PlayerSettingsBuilder builder() {
        return Impactor.getInstance().getRegistry().createBuilder(PlayerSettingsBuilder.class);
    }

    interface PlayerSettingsBuilder extends Builder<PlayerSettings, PlayerSettingsBuilder> {

        /**
         * Applies the setting to the player's settings. All settings are considered on by default.
         *
         * @param setting The setting to set
         * @param mode The mode we wish to set this setting to
         * @return The builder after being modified by this request
         */
        PlayerSettingsBuilder set(NotificationSetting setting, boolean mode);

    }
}
