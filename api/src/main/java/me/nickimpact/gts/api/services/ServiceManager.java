package me.nickimpact.gts.api.services;

import java.util.Optional;

public interface ServiceManager {

	/**
	 * Attempts to locate and return a Service matching the typing of the class marker. If a service has not yet
	 * been registered under this typing, this call will return with an empty value.
	 *
	 * @param type The type of service being requested
	 * @param <T> The type shared between the design and implementation of a service
	 * @return An optionally wrapped value possibly containing the service, if it has been initialized
	 */
	<T extends Service> Optional<T> get(Class<T> type);

	/**
	 * Registers a service that can be later queried and used for quick and essential operations.
	 *
	 * @param type The type of the service being registered
	 * @param service The implementation of that service
	 * @param <T> The common type between the design and implementation of a service
	 * @throws IllegalArgumentException If the service typing has already been registered
	 */
	<T extends Service> void register(Class<T> type, T service) throws IllegalArgumentException;

}
