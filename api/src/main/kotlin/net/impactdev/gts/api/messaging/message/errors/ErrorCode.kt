package net.impactdev.gts.api.messaging.message.errors

/**
 *
 */
interface ErrorCode {
    /**
     * Represents the ordinal value of this error code. This is calculated during the setup of the entire
     * error codes file. The purpose of such is to provide a proper fallback option for messages featuring
     * an error code in their response. Rather than saving description, we can save an index that'll be universal
     * across all setups (assuming people are up to date and no new error codes exist across versions).
     *
     * @return The ordinal value of this error code
     */
    fun ordinal(): Int

    /**
     * The front of the error code reported to a user when an error occurs. For information on what the key means,
     * a user should be able to hover over the key to receive what is specified by [.getDescription].
     *
     * @return The key of this error code
     */
    val key: String

    /**
     * Represents the error code description for this error. This is meant to help a user identify what exactly
     * happened in response to a request failing.
     *
     * @return The error's description
     */
    val description: String
}