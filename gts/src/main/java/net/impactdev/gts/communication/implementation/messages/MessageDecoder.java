package net.impactdev.gts.communication.implementation.messages;

import com.google.gson.JsonObject;

import java.util.UUID;

@FunctionalInterface
public interface MessageDecoder<T extends Message> {

    T decode(UUID id, JsonObject content);

}
