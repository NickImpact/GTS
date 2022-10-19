package net.impactdev.gts.service;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.GTSServiceProvider;

import java.lang.reflect.Method;

public class APIRegistrar {

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

    public static GTSService register(GTSService service) {
        try {
            REGISTER.invoke(null, service);
            return service;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
