package me.nickimpact.gts.common.data;

import com.google.common.collect.Maps;
import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.data.registry.StorableRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;

public class StorableRegistryImpl implements StorableRegistry {

	private final Map<Class<?>, Storable<?>> managers = Maps.newHashMap();

	@SuppressWarnings("unchecked")
	public <T> Optional<Storable<T>> get(@NonNull Class<T> type) {
		return Optional.ofNullable(this.managers.get(type)).map(x -> (Storable<T>) x);
	}

	/**
	 * Registers an Entry Manager to the registry. If a manager already exists for the specified type,
	 * this request will replace that previously registered option. This will allow for custom implementations
	 * of GTS defaults if that is desired.
	 *
	 * @param type The class representing the type of entry being worked with
	 * @param manager The manager that'll process requests based on a specific entry type
	 * @param <T> The type of the entry
	 */
	public <T> void register(Class<T> type, Storable<T> manager) {
		this.managers.put(type, manager);
	}

	@Override
	public <T> Optional<Storable<T>> getLegacyStorable(String type) {
		return Optional.empty();
	}

	@Override
	public <T> void registerLegacyStorable(String type, Storable<T> storable) {

	}
}
