package net.impactdev.gts.api.exceptions;

public final class RegistryLockedException extends RuntimeException {

    public RegistryLockedException() {
        super("The target registry is locked");
    }

}
