package net.impactdev.gts.common.exceptions;

import net.impactdev.gts.api.util.Version;

public class DataContentException extends RuntimeException {

    private final Version version;
    private final int content;

    public DataContentException(Version version, int content) {
        super(String.format("Invalid content version for game version (%s:%d)", version.toString(), content));
        this.version = version;
        this.content = content;
    }

    public Version version() {
        return this.version;
    }

    public int content() {
        return this.content;
    }
}
