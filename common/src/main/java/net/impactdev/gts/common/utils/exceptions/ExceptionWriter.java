package net.impactdev.gts.common.utils.exceptions;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;

public class ExceptionWriter {

    /**
     * Propagates an expected exception (one expected by the plugin) to the GTS logger for cleaner output.
     *
     * <p>If causes are to be shown, it's expected that the input exception is built with these causes
     * already attached to them.</p>
     *
     * <p>If for some reason an internal error occurs whilst trying to output the exception, we will simply
     * default to sending raw stacktrace.</p>
     *
     * @param exception The exception we wish to write out
     */
    public static void write(Throwable exception) {
        PrettyPrinter printer = new PrettyPrinter(100);
        printer.add("GTS Encountered an Exception!").center();
        printer.hr('*');
        printer.add("Version Information:")
                .add(GTSPlugin.instance().environment())
                .hr('-');
        printer.add(exception);
        printer.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.ERROR);
    }

}
