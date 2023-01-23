package net.impactdev.gts.communication.implementation;

import net.impactdev.gts.registries.CommunicationRegistry;
import net.impactdev.gts.communication.implementation.communicators.Communicator;
import net.impactdev.gts.communication.implementation.providers.CommunicatorProvider;
import net.impactdev.gts.communication.implementation.processing.IncomingMessageConsumer;

public final class CommunicationService {

    private final Communicator messenger;
    private final CommunicatorProvider provider;
    private final CommunicationRegistry registry;

    public CommunicationService(CommunicatorProvider provider) {
        this.provider = provider;
        this.messenger = this.provider.obtain(new IncomingMessageConsumer());
        this.registry = new CommunicationRegistry();
    }

    public String name() {
        return this.provider.name();
    }

    public Communicator communicator() {
        return this.messenger;
    }

    public CommunicatorProvider provider() {
        return this.provider;
    }

    public CommunicationRegistry messages() {
        return this.registry;
    }

    public void close() {

    }
}
