package net.impactdev.gts.velocity.messaging;

import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.MessengerProvider;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.gts.velocity.GTSVelocityPlugin;
import net.impactdev.gts.velocity.messaging.processor.VelocityIncomingMessageConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class VelocityMessagingFactory extends MessagingFactory<GTSVelocityPlugin> {

    public VelocityMessagingFactory(GTSVelocityPlugin plugin) {
        super(plugin);
    }

    @Override
    protected InternalMessagingService getServiceFor(String messageType) {
        if(messageType.equalsIgnoreCase("pluginmsg") || messageType.equalsIgnoreCase("velocity")) {
            try {
                return new GTSMessagingService(this.getPlugin(), new PluginMessageMessengerProvider(), new VelocityIncomingMessageConsumer(this.getPlugin()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private class PluginMessageMessengerProvider implements MessengerProvider {

        @Override
        public @NonNull String getName() {
            return "Plugin Message";
        }

        @Override
        public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
            PluginMessageMessenger messenger = new PluginMessageMessenger(VelocityMessagingFactory.this.getPlugin(), incomingMessageConsumer);
            messenger.init();
            return messenger;
        }

    }
}
