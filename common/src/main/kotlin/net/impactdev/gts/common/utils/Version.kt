package net.impactdev.gts.common.utils

import java.util.*
import java.util.regex.Pattern

class Version : Comparable<Version> {
    private val source: String?
    val major: Short
    val minor: Short
    val patch: Short
    val isSnapshot: Boolean
    val isValid: Boolean

    constructor(input: String?) {
        source = input
        val matcher = VERSION_PATTERN.matcher(input)
        if (matcher.find()) {
            major = matcher.group("major").toShort()
            minor = matcher.group("minor").toShort()
            patch = matcher.group("patch").toShort()
            isSnapshot = matcher.group("snapshot") != null
            isValid = true
        } else {
            major = 0
            minor = 0
            patch = 0
            isSnapshot = false
            isValid = false
        }
    }

    constructor(major: Short, minor: Short, patch: Short) {
        this.major = major
        this.minor = minor
        this.patch = patch
        isSnapshot = false
        isValid = true
        source = null
    }

    constructor(major: Short, minor: Short, patch: Short, snapshot: Boolean) {
        this.major = major
        this.minor = minor
        this.patch = patch
        isSnapshot = snapshot
        isValid = true
        source = null
    }

    override fun compareTo(other: Version): Int {
        if (isValid && other.isValid) {
            if (major != other.major) {
                return if (major > other.major) {
                    1
                } else {
                    -1
                }
            } else if (minor != other.minor) {
                return if (minor > other.minor) {
                    1
                } else {
                    -1
                }
            } else if (patch != other.patch) {
                return if (patch > other.patch) {
                    1
                } else {
                    -1
                }
            } else {
                if (!isSnapshot && other.isSnapshot) {
                    return 1
                } else if (isSnapshot && !other.isSnapshot) {
                    return -1
                }
            }
        } else {
            return if (other.isValid) {
                -1
            } else {
                1
            }
        }
        return 0
    }

    override fun toString(): String {
        if (source != null) {
            return source
        }
        val joiner = StringJoiner(".")
        joiner.add(asString(major))
        joiner.add(asString(minor))
        joiner.add(asString(patch))
        var result = joiner.toString()
        if (isSnapshot) {
            result += "-SNAPSHOT"
        }
        return result
    }

    private fun asString(x: Short): String {
        return "" + x
    }

    companion object {
        private val VERSION_PATTERN =
            Pattern.compile("(?<major>[0-9]+).(?<minor>[0-9]+).(?<patch>[0-9]+)(?<snapshot>-SNAPSHOT)?")
    }
}