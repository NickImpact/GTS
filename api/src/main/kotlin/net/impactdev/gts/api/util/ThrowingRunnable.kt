package net.impactdev.gts.api.util

@FunctionalInterface
interface ThrowingRunnable {
    @kotlin.Throws(Exception::class)
    fun run()
}