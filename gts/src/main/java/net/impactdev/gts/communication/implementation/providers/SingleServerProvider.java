package net.impactdev.gts.communication.implementation.providers;

import net.impactdev.gts.communication.implementation.communicators.Communicator;
import net.impactdev.gts.communication.implementation.communicators.SingleServerCommunicator;
import net.impactdev.gts.communication.implementation.processing.IncomingMessageConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class SingleServerProvider implements CommunicatorProvider {

    @Override
    public @NonNull String name() {
        return "Single Server";
    }

    @Override
    public @NonNull Communicator obtain(@NonNull IncomingMessageConsumer consumer) {
        return new SingleServerCommunicator(consumer);
    }
}