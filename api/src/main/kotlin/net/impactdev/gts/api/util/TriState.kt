/*
 * Components of this file originate from SpongeAPI, which is licensed under MIT.
 */
package net.impactdev.gts.api.util

/**
 * Represents a simple tristate.
 */
enum class TriState(private val `val`: Boolean) {
    TRUE(true) {
        override fun and(other: TriState): TriState? {
            return if (other === TRUE || other === UNDEFINED) TRUE else FALSE
        }

        override fun or(other: TriState): TriState? {
            return TRUE
        }
    },
    FALSE(false) {
        override fun and(other: TriState): TriState? {
            return FALSE
        }

        override fun or(other: TriState): TriState? {
            return if (other === TRUE) TRUE else FALSE
        }
    },
    UNDEFINED(false) {
        override fun and(other: TriState): TriState? {
            return other
        }

        override fun or(other: TriState): TriState? {
            return other
        }
    };

    /**
     * ANDs this tristate with another tristate.
     *
     * @param other The tristate to AND with
     * @return The result
     */
    abstract fun and(other: TriState): TriState?

    /**
     * ORs this tristate with another tristate.
     *
     * @param other The tristate to OR with
     * @return The result
     */
    abstract fun or(other: TriState): TriState?

    /**
     * Returns the boolean representation of this tristate.
     *
     * @return The boolean tristate representation
     */
    fun asBoolean(): Boolean {
        return `val`
    }

    fun invert(): TriState {
        if (this === TRUE) {
            return FALSE
        } else if (this === FALSE) {
            return TRUE
        }
        return UNDEFINED
    }

    companion object {
        /**
         * Return the appropriate tristate for a given boolean value.
         *
         * @param val The boolean value
         * @return The appropriate tristate
         */
        @kotlin.jvm.JvmStatic
        fun fromBoolean(`val`: Boolean): TriState {
            return if (`val`) TRUE else FALSE
        }
    }
}