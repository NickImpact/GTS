package net.impactdev.gts.api.logs

interface LogOutputStream {
    @kotlin.Throws(Exception::class)
    fun write(log: Log?)
}