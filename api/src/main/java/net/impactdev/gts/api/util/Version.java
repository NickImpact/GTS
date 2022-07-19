package net.impactdev.gts.api.util;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)(?<snapshot>-SNAPSHOT)?");

    @Nullable
    private final String source;

    private final short major;
    private final short minor;
    private final short patch;

    private final boolean snapshot;
    private final boolean valid;

    public static Version of(@Nonnull @org.intellij.lang.annotations.Pattern("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)(?<snapshot>-SNAPSHOT)?") String input) {
        return new Version(input);
    }

    public Version(@Nonnull String input) {
        this.source = input;

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

        this.source = null;
    }

    public Version(short major, short minor, short patch, boolean snapshot) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = snapshot;
        this.valid = true;

        this.source = null;
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

    @Override
    public String toString() {
        if(this.source != null) {
            return this.source;
        }

        StringJoiner joiner = new StringJoiner(".");

        joiner.add(this.asString(this.getMajor()));
        joiner.add(this.asString(this.getMinor()));
        joiner.add(this.asString(this.getPatch()));

        String result = joiner.toString();
        if(this.isSnapshot()) {
            result += "-SNAPSHOT";
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Version) {
            return this.compareTo((Version) obj) == 0;
        }

        return false;
    }

    private String asString(short x) {
        return "" + x;
    }

    public static class Minecraft {

        public static final Version v1_12_2 = Version.of("1.12.2");
        public static final Version v1_16_5 = Version.of("1.16.5");

    }

}
