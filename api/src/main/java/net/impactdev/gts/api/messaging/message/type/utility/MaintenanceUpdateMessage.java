package net.impactdev.gts.api.messaging.message.type.utility;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.UpdateMessage;

public interface MaintenanceUpdateMessage extends UpdateMessage, OutgoingMessage {

    MaintenanceMode getMode();

    boolean getState();

}
