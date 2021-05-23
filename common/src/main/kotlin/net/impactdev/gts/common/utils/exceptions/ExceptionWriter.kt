package net.impactdev.gts.common.utils.exceptions

import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.plugin.GTSPlugin

object ExceptionWriter {
    /**
     * Propagates an expected exception (one expected by the plugin) to the GTS logger for cleaner output.
     *
     *
     * If causes are to be shown, it's expected that the input exception is built with these causes
     * already attached to them.
     *
     *
     * If for some reason an internal error occurs whilst trying to output the exception, we will simply
     * default to sending raw stacktrace.
     *
     * @param exception The exception we wish to write out
     */
    @kotlin.jvm.JvmStatic
    fun write(exception: Throwable?) {
        val printer = PrettyPrinter(100)
        printer.add("GTS Encountered an Exception!").center()
        printer.hr('*')
        printer.add("Version Information:")
            .add(GTSPlugin.Companion.getInstance().getEnvironment())
            .hr('-')
        printer.add(exception!!)
        printer.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.ERROR)
    }
}