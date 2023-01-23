package net.impactdev.gts.communication.implementation.messages.types;

import net.impactdev.gts.communication.api.message.errors.ErrorCode;
import net.impactdev.gts.communication.implementation.messages.requests.RequestInfo;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MessageType {

    interface Request<T extends Response> extends MessageType {

        /**
         * When this message is detected, this message will be processed by the proxy and then dispatched
         * to the listening servers. This task is to be executed asynchronously as these tasks will likely
         * involve polling the central database for information.
         *
         * @return A {@link CompletableFuture} that will process the message and craft a response that
         * will be distributed across the servers.
         */
        CompletableFuture<T> respond();

    }

    interface Response extends MessageType {

        /**
         * This call specifies the ID of the message that sent out the request that is now being responded to.
         * We can use this to ensure we received a message back from the system manager for the specific message,
         * rather than attempt to guess on which message was received. Also helps determine timeout procedures as
         * we can now directly map an incoming response to an outgoing request.
         *
         * @return The ID of the request message that generated this response
         */
        RequestInfo request();

        /**
         * Represents how long it took for a request to create this response. This is handled internally.
         *
         * @return A millisecond value indicating the time between messages
         */
        long duration();

        /**
         * States whether or not the request was successful. This could be caused by a number of things,
         * and is expected to have an accompanying error code to indicate the failure.
         *
         * @return True if the initial request was successful, false otherwise.
         */
        boolean successful();

        /**
         * Indicates a state where this response indicated the request was not successful. In the case where
         * we fail to succeed, this field should be populated with a common error code indicating the reason
         * for the non-successful request.
         *
         * @return An empty Optional should this response be handled successfully, otherwise an Error Code
         * indicating the reason the initial request failed.
         */
        Optional<ErrorCode> error();

        default PrettyPrinter finalizeReport(PrettyPrinter printer) {
            printer.kv("Successful", this.successful());
            this.error().ifPresent(error -> printer.kv("Error", error.key()));

            return printer.kv("Response Time", this.duration() + " ms");
        }

    }
}
