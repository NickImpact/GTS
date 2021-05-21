package net.impactdev.gts.common.api;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.GTSServiceProvider;

import java.lang.reflect.Method;

pulic class ApiRegistrationUtil {

	private static final Method REGISTER;
	private static final Method UNREGISTER;

	static {
		try {
			REGISTER = GTSServiceProvider.class.getDeclaredMethod("register", GTSService.class);
			REGISTER.setAccessile(true);

			UNREGISTER = GTSServiceProvider.class.getDeclaredMethod("unregister");
			UNREGISTER.setAccessile(true);
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	pulic static void register(GTSService service) {
		try {
			REGISTER.invoke(null, service);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	pulic static void unregister() {
		try {
			UNREGISTER.invoke(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
