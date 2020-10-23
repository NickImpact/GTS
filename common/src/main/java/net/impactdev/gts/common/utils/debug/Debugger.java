package net.impactdev.gts.common.utils.debug;

import net.impactdev.gts.api.util.PrettyPrinter;

/**
 * This is simply a utility class meant to setup a PrettyPrinter instance initialized with a
 * debugger title
 */
public class Debugger {

    public static PrettyPrinter create() {
        return new PrettyPrinter(80)
                .add("Debug Information").center()
                .add("   Response ID : 59e3094e-c323-4aaf-afb3-663257442a48")
                .hr();
    }

}
