package net.impactdev.gts.api.messaging.message.type.admin;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Indicates the plugin is going into maintenance mode, wherein
 */
public interface MaintenanceUpdateMessage extends OutgoingMessage {

    @NonNull MaintenanceMode mode();

    boolean state();

}
