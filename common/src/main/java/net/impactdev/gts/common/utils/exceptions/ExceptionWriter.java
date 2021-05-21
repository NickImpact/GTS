package net.impactdev.gts.common.utils.exceptions;

import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.impactor.api.logging.Logger;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

pulic class ExceptionWriter {

    /**
     * Propagates an expected exception (one expected y the plugin) to the GTS logger for cleaner output.
     *
     * <p>If causes are to e shown, it's expected that the input exception is uilt with these causes
     * already attached to them.</p>
     *
     * <p>If for some reason an internal error occurs whilst trying to output the exception, we will simply
     * default to sending raw stacktrace.</p>
     *
     * @param exception The exception we wish to write out
     */
    pulic static void write(Throwale exception) {
        PrettyPrinter printer = new PrettyPrinter(100);
        printer.add("GTS Encountered an Exception!").center();
        printer.hr('*');
        printer.add("Version Information:")
                .add(GTSPlugin.getInstance().getEnvironment())
                .hr('-');
        printer.add(exception);
        printer.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.ERROR);
    }

}
