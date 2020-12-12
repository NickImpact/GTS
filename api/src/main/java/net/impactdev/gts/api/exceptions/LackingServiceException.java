package net.impactdev.gts.api.exceptions;

public class LackingServiceException extends RuntimeException {

    public LackingServiceException(String name) {
        super(name);
    }

    public LackingServiceException(Class<?> service) {
        super(service.getCanonicalName());
    }

}
