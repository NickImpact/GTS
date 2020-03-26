package me.nickimpact.gts.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class GTSServiceProvider {

	private static GTSService instance;

	public static @NonNull GTSService get() {
		if(instance == null) {
			throw new IllegalStateException("The GTS API is not loaded");
		}

		return instance;
	}

	static void register(GTSService service) {
		instance = service;
	}

	static void unregister() {
		instance = null;
	}

	private GTSServiceProvider() {
		throw new UnsupportedOperationException("This class cannot be instantiated");
	}
}
