package net.impactdev.gts.api.messaging.message.exceptions

import net.impactdev.gts.api.messaging.message.errors.ErrorCode

class MessagingException(val error: ErrorCode, cause: Throwable?) : RuntimeException(cause)