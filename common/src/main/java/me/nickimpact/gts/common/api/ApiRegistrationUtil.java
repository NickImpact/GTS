package me.nickimpact.gts.common.api;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.GtsServiceProvider;

import java.lang.reflect.Method;

public class ApiRegistrationUtil {

	private static final Method REGISTER;
	private static final Method UNREGISTER;

	static {
		try {
			REGISTER = GtsServiceProvider.class.getDeclaredMethod("register", GtsService.class);
			REGISTER.setAccessible(true);

			UNREGISTER = GtsServiceProvider.class.getDeclaredMethod("unregister");
			UNREGISTER.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static void register(GtsService service) {
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
