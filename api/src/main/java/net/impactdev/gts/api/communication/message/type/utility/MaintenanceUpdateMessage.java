package net.impactdev.gts.api.communication.message.type.utility;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.api.communication.message.type.UpdateMessage;

public interface MaintenanceUpdateMessage extends UpdateMessage, OutgoingMessage {

    MaintenanceMode getMode();

    boolean getState();

}
