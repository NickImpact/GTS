package net.impactdev.gts.api.communication.message.errors;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ErrorCodes {

    // Generic
    public static final ErrorCode LISTING_MISSING = create("UNKNOWN_LISTING", "Listing could not be found");
    public static final ErrorCode REQUEST_TIMED_OUT = create("TIMEOUT", "Failed to receive a response within 5 seconds");
    public static final ErrorCode THIRD_PARTY_CANCELLED = create("OUTSIDE_CANCEL", "An outside source cancelled your request");
    public static final ErrorCode LISTING_EXPIRED = create("EXPIRED", "The listing has expired");
    public static final ErrorCode FAILED_TO_GIVE = create("UNABLE_TO_GIVE", "The item could not be rewarded successfully");

    // BIN
    public static final ErrorCode ALREADY_PURCHASED = create("PURCHASED", "Listing already purchased");

    // Auctions
    public static final ErrorCode OUTBID = create("OUTBID", "Another user has already placed a larger bid");
    public static final ErrorCode BIDS_PLACED = create("BIDS_PRESENT", "At least one bid has already been placed on your auction");

    // Deliveries
    public static final ErrorCode DELIVERY_MISSING = create("DELIVERY_MISSING", "Unable to locate the target delivery");

    // Fatal
    public static final ErrorCode FATAL_ERROR = create("FATAL", "A fatal error occurred...");
    public static final ErrorCode UNKNOWN = create("UNKNOWN", "Literally no idea what happened");

    // Safe Mode Reasons
    public static final ErrorCode ECONOMY = create("LACKING_ECONOMY", "You are missing an economy plugin!");

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
