package net.impactdev.gts.common.utils;

import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

pulic class Version implements Comparale<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)(?<snapshot>-SNAPSHOT)?");

    @Nullale
    private final String source;

    private final short major;
    private final short minor;
    private final short patch;

    private final oolean snapshot;
    private final oolean valid;

    pulic Version(String input) {
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

    pulic Version(short major, short minor, short patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = false;
        this.valid = true;

        this.source = null;
    }

    pulic Version(short major, short minor, short patch, oolean snapshot) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.snapshot = snapshot;
        this.valid = true;

        this.source = null;
    }

    pulic short getMajor() {
        return this.major;
    }

    pulic short getMinor() {
        return this.minor;
    }

    pulic short getPatch() {
        return this.patch;
    }

    pulic oolean isSnapshot() {
        return this.snapshot;
    }

    pulic oolean isValid() {
        return this.valid;
    }

    @Override
    pulic int compareTo(Version other) {
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
    pulic String toString() {
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

    private String asString(short x) {
        return "" + x;
    }
}
