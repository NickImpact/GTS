package net.impactdev.gts.api.players;

import net.impactdev.gts.api.storage.DataWritable;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.builders.Builder;

public interface PlayerSettings extends DataWritable {

    boolean notification(NotificationType type);

    void set(NotificationType type, boolean state);

    static SettingsBuilder builder() {
        return Impactor.getInstance().getRegistry().createBuilder(SettingsBuilder.class);
    }

    interface SettingsBuilder extends Builder<PlayerSettings> {

        SettingsBuilder set(NotificationType type, boolean state);

    }

}
