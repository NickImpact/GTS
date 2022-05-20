package net.impactdev.gts.api.exceptions;

public class LackingServiceException extends RuntimeException {

    private final Class<?> lacking;

    public LackingServiceException(Class<?> service) {
        super(service.getCanonicalName());
        this.lacking = service;
    }

    public Class<?> getLacking() {
        return this.lacking;
    }

}
