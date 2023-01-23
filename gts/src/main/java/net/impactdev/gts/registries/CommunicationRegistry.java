package net.impactdev.gts.registries;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;
import net.impactdev.gts.communication.implementation.communicators.Communicator;
import net.impactdev.gts.communication.implementation.messages.Message;
import net.impactdev.gts.communication.implementation.messages.MessageDecoder;
import net.impactdev.gts.communication.implementation.messages.MessageSubscription;
import net.impactdev.gts.communication.implementation.messages.types.MessageType;
import net.impactdev.gts.communication.implementation.messages.types.utility.PingMessage;
import net.impactdev.gts.communication.implementation.messages.types.utility.PongMessage;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.logging.PluginLogger;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CommunicationRegistry {

    private final Map<Key, MessageDecoder<?>> decoders = Maps.newHashMap();
    private final Map<Key, MessageSubscription<?>> subscriptions = Maps.newHashMap();

    public CommunicationRegistry() {
        this.init();
    }

    public void registerDecoder(final @NotNull Key key, final @NotNull MessageDecoder<?> decoder) {
        Preconditions.checkNotNull(key, "Decoder key cannot be null");
        Preconditions.checkNotNull(decoder, "Decoder cannot be null");

        this.decoders.put(key, decoder);
    }

    public <T extends Message> MessageDecoder<T> decoder(final @NotNull Key key) {
        Preconditions.checkNotNull(key, "Decoder key cannot be null");

        MessageDecoder<T> decoder = (MessageDecoder<T>) this.decoders.get(key);
        if(decoder == null) {
            throw new IllegalArgumentException("Invalid key reference for decoder: " + key);
        }

        return decoder;
    }

    public void subscribe(final @NotNull Key key, final @NotNull MessageSubscription<?> subscription) {
        Preconditions.checkNotNull(key, subscription);

        this.subscriptions.put(key, subscription);
    }

    public <T extends Message> Optional<MessageSubscription<T>> subscription(final @NotNull Key key) {
        Preconditions.checkNotNull(key, "Subscription key cannot be null");

        return Optional.ofNullable(this.subscriptions.get(key))
                .map(subscription -> (MessageSubscription<T>) subscription);
    }

    public void init() {
        // TODO - This code will work post 1.16.5 in all environments. Until then, we need to do
        // TODO - things manually
//        ClassGraphLoader.init(this);

        this.registerDecoder(PingMessage.KEY, PingMessage.DECODER);
        this.registerDecoder(PongMessage.KEY, PongMessage.DECODER);

        // TODO - Replace this subscription section entirely with a modular class reference system
        this.subscribe(PingMessage.KEY, message -> {
            Communicator communicator = GTSPlugin.instance().communication().communicator();
            MessageType.Request<PongMessage> request = (MessageType.Request<PongMessage>) message;
            communicator.publish(request.respond().join());
        });
        this.subscribe(PongMessage.KEY, message -> {
            MessageType.Response response = (MessageType.Response) message;
            GTSPlugin.instance().logger().info(
                    "Received pong response from ping, took " + response.duration() + " ms!"
            );
        });
    }

    /**
     * A message loader featuring class lookup and execution via a scan of a particular package.
     * Due to mod launcher designs, this capability is isolated to 1.17+ implementations, and should
     * be used over alternatives to ensure any new messages are registered and verified without the
     * need for manual input.
     */
    private static class ClassGraphLoader {

        public static void init(CommunicationRegistry registry) {
            ClassGraph graph = new ClassGraph()
                    .enableClassInfo()
                    .enableFieldInfo()
                    .acceptPackages("net.impactdev.gts.communication.implementation.messages.types")
                    .addClassLoader(registry.getClass().getClassLoader());

            try (ScanResult scan = graph.scan()) {
                ClassInfoList classes = scan.getClassesImplementing(Message.class);
                PluginLogger logger = GTSPlugin.instance().logger();
                classes.stream().filter(info -> !info.isInterface() && !info.isAbstract())
                        .forEach(info -> {
                            try {
                                Key key = translateFromFieldInfo(info, "KEY", Key.class);
                                MessageDecoder<?> decoder = translateFromFieldInfo(info,
                                        "DECODER",
                                        MessageDecoder.class);

                                logger.debug("Registering message decoder: " + key.asString());
                                registry.registerDecoder(key, decoder);
                            }
                            catch (Exception e) {
                                logger.severe("Failed to register a message: " + info.getSimpleName(), e);
                            }
                        });
            }
        }

        private static <T> T translateFromFieldInfo(ClassInfo info, String name, Class<T> type) throws Exception {
            FieldInfo field = Objects.requireNonNull(info.getDeclaredFieldInfo(name), "Could not locate target field: " + name);
            Preconditions.checkArgument(field.isStatic());

            Object value = field.loadClassAndGetField().get(null);
            if(type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }

            throw new IllegalArgumentException(String.format(
                    "Field type is not expected typing. Wanted %s, got %s",
                    type.getSimpleName(), value.getClass().getSimpleName()
            ));
        }

    }
}
