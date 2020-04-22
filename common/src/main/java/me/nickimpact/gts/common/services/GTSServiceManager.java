package me.nickimpact.gts.common.services;

import com.google.common.collect.Maps;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.api.services.Service;
import me.nickimpact.gts.api.services.ServiceManager;

import java.util.Map;
import java.util.Optional;

public class GTSServiceManager implements ServiceManager {

	private Map<Class<? extends Service>, GTSRegistry.Provider<? extends Service>> registrar = Maps.newHashMap();

	@Override
	public <T extends Service> Optional<T> get(Class<T> type) {
		return Optional.ofNullable(registrar.get(type)).map(x -> (T) x.getInstance());
	}

	@Override
	public <T extends Service> void register(Class<T> type, T service) throws IllegalArgumentException {
		this.registrar.put(type, new GTSRegistry.Provider<>(service));
	}

}
