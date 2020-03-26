package me.nickimpact.gts.common.api;

import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.GTSServiceProvider;

import java.lang.reflect.Method;

public class ApiRegistrationUtil {

	private static final Method REGISTER;
	private static final Method UNREGISTER;

	static {
		try {
			REGISTER = GTSServiceProvider.class.getDeclaredMethod("register", GTSService.class);
			REGISTER.setAccessible(true);

			UNREGISTER = GTSServiceProvider.class.getDeclaredMethod("unregister");
			UNREGISTER.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static void register(GTSService service) {
		try {
			REGISTER.invoke(null, service);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void unregister() {
		try {
			UNREGISTER.invoke(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
