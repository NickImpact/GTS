package net.impactdev.gts.api.util

@FunctionalInterface
interface TriConsumer<A, B, C> {
    fun accept(a: A, b: B, c: C)
}