package net.impactdev.gts.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)(?<snapshot>-SNAPSHOT)?");

    private final short major;
    private final short minor;
    private final short patch;

    private final boolean snapshot;
    private final boolean valid;

    public Version(String input) {
        Matcher matcher = VERSION_PATTERN.matcher(input);
        if(matcher.find()) {
            this.major = Short.parseShort(matcher.group("major"));
            this.minor = Short.parseShort(matcher.group("minor"));
            this.patch = Short.parseShort(matcher.group("patch"));
            this.snapshot = matcher.group("snapshot") != null;
            this.valid = true;
        } else {
            this.major = 0;
            this.minor = 0;
            this.patch = 0;
            this.snapshot = false;
            this.valid = false;
        }
    }

    public Version(short major, short minor, short patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = false;
        this.valid = true;
    }

    public Version(short major, short minor, short patch, boolean snapshot) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = snapshot;
        this.valid = true;
    }

    public short getMajor() {
        return this.major;
    }

    public short getMinor() {
        return this.minor;
    }

    public short getPatch() {
        return this.patch;
    }

    public boolean isSnapshot() {
        return this.snapshot;
    }

    public boolean isValid() {
        return this.valid;
    }

    @Override
    public int compareTo(Version other) {
        if(this.valid && other.valid) {
            if(this.major != other.major) {
                if(this.major > other.major) {
                    return 1;
                } else {
                    return -1;
                }
            } else if(this.minor != other.minor) {
                if(this.minor > other.minor) {
                    return 1;
                } else {
                    return -1;
                }
            } else if(this.patch != other.patch) {
                if(this.patch > other.patch) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if(!this.snapshot && other.snapshot) {
                    return 1;
                } else if(this.snapshot && !other.snapshot) {
                    return -1;
                }
            }
        } else {
            if(other.valid) {
                return -1;
            } else {
                return 1;
            }
        }

        return 0;
    }
}
