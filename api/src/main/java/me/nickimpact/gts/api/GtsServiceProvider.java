package me.nickimpact.gts.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class GtsServiceProvider {

	private static GtsService instance;

	public static @NonNull GtsService get() {
		if(instance == null) {
			throw new IllegalStateException("The GTS API is not loaded");
		}

		return instance;
	}

	static void register(GtsService service) {
		instance = service;
	}

	static void unregister() {
		instance = null;
	}

	private GtsServiceProvider() {
		throw new UnsupportedOperationException("This class cannot be instantiated");
	}
}
