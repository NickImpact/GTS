package net.impactdev.gts.api.messaging.message.exceptions;

import net.impactdev.gts.api.messaging.message.errors.ErrorCode;

public class MessagingException extends RuntimeException {

    private ErrorCode error;

    public MessagingException(ErrorCode error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    public ErrorCode getError() {
        return this.error;
    }

}
