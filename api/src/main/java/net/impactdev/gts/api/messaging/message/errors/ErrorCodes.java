package net.impactdev.gts.api.messaging.message.errors;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ErrorCodes {

    public static final ErrorCode LISTING_MISSING = create("UNKNOWN_LISTING", "Listing could not be found");
    public static final ErrorCode ALREADY_PURCHASED = create("PURCHASED", "Listing already purchased");
    public static final ErrorCode REQUEST_TIMED_OUT = create("TIMEOUT", "Failed to receive a response within 5 seconds");
    public static final ErrorCode FATAL_ERROR = create("FATAL", "A fatal error occurred...");

    private static final List<ErrorCode> KEYS;

    static {
        List<ErrorCode> codes = new LinkedList<>();
        Field[] values = ErrorCodes.class.getFields();
        int i = 0;

        for (Field f : values) {
            // ignore non-static fields
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            // ignore fields that aren't error codes
            if (!ErrorCode.class.isAssignableFrom(f.getType())) {
                continue;
            }

            try {
                // get the key instance
                ErrorCodeBackend key = (ErrorCodeBackend) f.get(null);
                // set the ordinal value of the key.
                key.ordinal = i++;
                // add the key to the return map
                codes.add(key);
            } catch (Exception e) {
                throw new RuntimeException("Exception processing field: " + f, e);
            }
        }

        KEYS = ImmutableList.copyOf(codes);
    }

    public static ErrorCode get(int ordinal) {
        return KEYS.get(ordinal);
    }

    private static ErrorCode create(String key, String description) {
        return new ErrorCodeBackend(key, description);
    }

    public static class ErrorCodeBackend implements ErrorCode {

        private int ordinal = -1;

        private final String key;
        private final String description;

        ErrorCodeBackend(String key, String description) {
            this.key = key;
            this.description = description;
        }

        @Override
        public int ordinal() {
            return this.ordinal;
        }

        void setOrdinal(int ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }

}
