package me.nickimpact.gts.api.data.registry;

import me.nickimpact.gts.api.data.Storable;

import java.util.Optional;

/**
 * Represents a registry that'll hold all instances of storable objects. From GTS, these include the high level
 * {@link me.nickimpact.gts.api.listings.Listing Listing} class, as well as its provided {@link me.nickimpact.gts.api.listings.entries.Entry entry}
 * options.
 *
 * TODO - Anything else needed here?
 */
public interface StorableRegistry {

    <T> Optional<Storable<T>> get(Class<T> type);

    <T> void register(Class<T> type, Storable<T> storable);

    //--------------------------------------------------------------
    //
    // Deprecated functions, available until 6.1.0
    //
    //--------------------------------------------------------------
    @Deprecated
    <T> Optional<Storable<T>> getLegacyStorable(String type);

    @Deprecated
    <T> void registerLegacyStorable(String type, Storable<T> storable);

}
