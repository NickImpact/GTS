package net.impactdev.gts.api.environment

import com.google.common.collect.Maps
import net.impactdev.gts.api.util.PrettyPrinter

class Environment : PrettyPrinter.IPrettyPrintable {
    private val environment: MutableMap<String, String> = Maps.newLinkedHashMap()
    fun append(key: String, version: String) {
        environment[key] = version
    }

    override fun print(printer: PrettyPrinter) {
        for ((key, value) in environment) {
            printer.add("  * $key: $value")
        }
    }
}