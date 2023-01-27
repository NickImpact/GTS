package net.impactdev.gts.api.elements.listings.deserialization;

public interface DeserializationContext {

    <T> T obtain(DeserializationKey<T> key);

}
