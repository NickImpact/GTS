package net.impactdev.gts.common.messaging.messages.utility

import com.google.gson.JsonElement
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.messaging.GTSMessagingService
import net.impactdev.gts.common.messaging.messages.AbstractMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.utils.future.CompletableFutureManager
import net.impactdev.impactor.api.json.factory.JObject
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class PingPongMessage(id: UUID) : AbstractMessage(id), PingMessage {
    class Ping(id: UUID) : PingPongMessage(id), PingMessage.Ping {
        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject().toJson()
            )
        }

        override fun respond(): CompletableFuture<PingMessage.Pong?>? {
            return CompletableFutureManager.makeFuture<PingMessage.Pong> {
                Pong(
                    GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                    id,
                    true,
                    null
                )
            }
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("ID", id)
        }

        companion object {
            const val TYPE = "PING"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): Ping {
                return Ping(id)
            }
        }
    }

    class Pong(id: UUID, override val requestID: UUID, private val successful: Boolean, private val error: ErrorCode?) :
        PingPongMessage(id), PingMessage.Pong {
        override var responseTime: Long = 0
        override fun wasSuccessful(): Boolean {
            return successful
        }

        override val errorCode: Optional<ErrorCode>
            get() = Optional.ofNullable(error)

        override fun asEncodedString(): String {
            return GTSMessagingService.Companion.encodeMessageAsString(
                TYPE,
                id,
                JObject()
                    .add("request", requestID.toString())
                    .add("successful", successful)
                    .consume { o: JObject ->
                        if (errorCode.isPresent) {
                            o.add("error", error!!.ordinal())
                        }
                    }
                    .toJson()
            )
        }

        override fun print(printer: PrettyPrinter) {
            printer.kv("ID", id)
                .kv("Ping ID", requestID)
        }

        companion object {
            const val TYPE = "PONG"
            @kotlin.jvm.JvmStatic
            fun decode(content: JsonElement?, id: UUID): Pong {
                checkNotNull(content) { "Raw JSON data was null" }
                val raw = content.asJsonObject
                val requestID = Optional.ofNullable(raw["request"])
                    .map { x: JsonElement -> UUID.fromString(x.asString) }
                    .orElseThrow { IllegalStateException("Unable to locate or parse request ID") }
                val successful = Optional.ofNullable(raw["successful"])
                    .map { obj: JsonElement -> obj.asBoolean }
                    .orElseThrow { IllegalStateException("Unable to locate success state") }
                val error = Optional.ofNullable(raw["error"])
                    .map { x: JsonElement -> ErrorCodes[x.asInt] }
                    .orElse(null)
                return Pong(id, requestID, successful, error)
            }
        }
    }
}