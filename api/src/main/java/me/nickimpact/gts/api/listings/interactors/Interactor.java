package me.nickimpact.gts.api.listings.interactors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Represents the data that represents a player/console interacting with the GTS. This data is meant to contain
 * the bare minimum of their data needed by the plugin.
 */
public interface Interactor {

	@NonNull UUID getUUID();

	@Nullable String getUsername();
	
}
