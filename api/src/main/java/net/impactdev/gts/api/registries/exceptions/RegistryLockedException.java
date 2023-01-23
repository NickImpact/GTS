package net.impactdev.gts.api.registries.exceptions;

public final class RegistryLockedException extends RuntimeException {

    public RegistryLockedException() {
        super("The target registry is locked");
    }

}
