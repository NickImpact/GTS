package net.impactdev.gts;

import net.impactdev.gts.communication.implementation.CommunicationService;
import net.impactdev.gts.communication.implementation.messages.MessageDecoder;
import net.impactdev.gts.communication.implementation.messages.types.utility.PingMessage;
import net.impactdev.gts.communication.implementation.providers.SingleServerProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class MessageTesting {

    private static CommunicationService service;

    @BeforeAll
    public static void init() {
        service = new CommunicationService(new SingleServerProvider());
    }

    @Test
    public void verify() {
        MessageDecoder<PingMessage> decoder = service.messages().decoder(PingMessage.KEY);
//        service.communicator().publish(new PingMessage(UUID.randomUUID()));
    }
}
