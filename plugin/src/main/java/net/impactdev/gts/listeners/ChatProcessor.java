package net.impactdev.gts.listeners;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Identifiable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatProcessor {

    private static final Map<UUID, Consumer<String>> callbacks = Maps.newHashMap();

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerChat(PlayerChatEvent event) {
        event.cause().first(ServerPlayer.class).map(Identifiable::uniqueId).ifPresent(id -> {
            if(callbacks.containsKey(id)) {
                event.setCancelled(true);
                callbacks.remove(id).accept(PlainTextComponentSerializer.plainText().serialize(event.originalMessage()));
            }
        });
    }

    public static void register(UUID user, Consumer<String> callback) {
        callbacks.put(user, callback);
    }

}
