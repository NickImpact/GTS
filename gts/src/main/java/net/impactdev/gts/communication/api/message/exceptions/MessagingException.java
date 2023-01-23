package net.impactdev.gts.communication.api.message.exceptions;

import net.impactdev.gts.communication.api.message.errors.ErrorCode;

public class MessagingException extends RuntimeException {

    private ErrorCode error;

    public MessagingException(ErrorCode error) {
        this.error = error;
    }

    public MessagingException(ErrorCode error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    public ErrorCode getError() {
        return this.error;
    }

}
