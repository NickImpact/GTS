package net.impactdev.gts.api.messaging.message.type

import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.util.PrettyPrinter
import java.util.*
import java.util.concurrent.CompletableFuture

interface MessageType {
    /**
     * Represents a message that is intended to be received only on the proxy server. This message will
     * then attempt to process and construct a Response message.
     */
    interface Request<T : Response?> : MessageType {
        /**
         * When this message is detected, this message will be processed by the proxy and then dispatched
         * to the listening servers. This task is to be executed asynchronously as these tasks will likely
         * involve polling the central database for information.
         *
         * @return A [CompletableFuture] that will process the message and craft a response that
         * will be distributed across the servers.
         */
        fun respond(): CompletableFuture<T>?
    }

    /**
     * Represents a message built in regards to an incoming proxy message. These messages are intended
     * to only be sent to the connected servers, and not the proxy itself. In the case of a multi-layered
     * proxy, these messages should simply be relayed rather than processed.
     */
    interface Response : MessageType {
        /**
         * This call specifies the ID of the message that sent out the request that is now being responded to.
         * We can use this to ensure we received a message back from the system manager for the specific message,
         * rather than attempt to guess on which message was received. Also helps determine timeout procedures as
         * we can now directly map an incoming response to an outgoing request.
         *
         * @return The ID of the request message that generated this response
         */
        val requestID: UUID?
        /**
         * Represents how long it took for a request to create this response. This is handled internally.
         *
         * @return A millisecond value indicating the time between messages
         */
        /**
         * Applies the input rate as the response time for this message. This should realistically only be
         * set by the implementation, and not otherwise altered. You may lose valuable information if you
         * change this value.
         */
        var responseTime: Long

        /**
         * States whether or not the request was successful. This could be caused by a number of things,
         * and is expected to have an accompanying error code to indicate the failure.
         *
         * @return True if the initial request was successful, false otherwise.
         */
        fun wasSuccessful(): Boolean

        /**
         * Indicates a state where this response indicated the request was not successful. In the case where
         * we fail to succeed, this field should be populated with a common error code indicating the reason
         * for the non-successful request.
         *
         * @return An empty Optional should this response be handled successfully, otherwise an Error Code
         * indicating the reason the initial request failed.
         */
        val errorCode: Optional<ErrorCode>
        fun finalizeReport(printer: PrettyPrinter): PrettyPrinter? {
            printer.kv("Successful", wasSuccessful())
            errorCode.ifPresent { error: ErrorCode -> printer.kv("Error", error.key) }
            return printer.kv("Response Time", responseTime.toString() + " ms")
        }
    }
}