/*
 * Components of this file originate from SpongeAPI, which is licensed under MIT.
 */

package net.impactdev.gts.api.util;

/**
 * Represents a simple tristate.
 */
public enum TriState {
    TRUE(true) {
        @Override
        public TriState and(TriState other) {
            return other == TRUE || other == UNDEFINED ? TRUE : FALSE;
        }

        @Override
        public TriState or(TriState other) {
            return TRUE;
        }
    },
    FALSE(false) {
        @Override
        public TriState and(TriState other) {
            return FALSE;
        }

        @Override
        public TriState or(TriState other) {
            return other == TRUE ? TRUE : FALSE;
        }
    },
    UNDEFINED(false) {
        @Override
        public TriState and(TriState other) {
            return other;
        }

        @Override
        public TriState or(TriState other) {
            return other;
        }
    };

    private final boolean val;

    TriState(boolean val) {
        this.val = val;
    }

    /**
     * Return the appropriate tristate for a given boolean value.
     *
     * @param val The boolean value
     * @return The appropriate tristate
     */
    public static TriState fromBoolean(boolean val) {
        return val ? TRUE : FALSE;
    }

    /**
     * ANDs this tristate with another tristate.
     *
     * @param other The tristate to AND with
     * @return The result
     */
    public abstract TriState and(TriState other);

    /**
     * ORs this tristate with another tristate.
     *
     * @param other The tristate to OR with
     * @return The result
     */
    public abstract TriState or(TriState other);

    /**
     * Returns the boolean representation of this tristate.
     *
     * @return The boolean tristate representation
     */
    public boolean asBoolean() {
        return this.val;
    }

    public TriState invert() {
        if(this == TRUE) {
            return FALSE;
        } else if(this == FALSE) {
            return TRUE;
        }

        return UNDEFINED;
    }
}
