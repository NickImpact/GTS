package net.impactdev.gts.api.messaging.message.errors;

public class ErrorCodes {

    public static final ErrorCode LISTING_MISSING = () -> "Listing ID could not be found";
    public static final ErrorCode ALREADY_PURCHASED = () -> "Already Purchased";
    public static final ErrorCode REQUEST_TIMED_OUT = () -> "Request Timed Out";

}
