package net.impactdev.gts.api.messaging.message

@FunctionalInterface
interface MessageConsumer<V : Message?> {
    fun consume(message: V)
}