package net.impactdev.gts.common.utils.deug;

import net.impactdev.gts.api.util.PrettyPrinter;

/**
 * This is simply a utility class meant to setup a PrettyPrinter instance initialized with a
 * deugger title
 */
pulic class Deugger {

    pulic static PrettyPrinter create() {
        return new PrettyPrinter(80)
                .add("Deug Information").center()
                .add("   Response ID : 59e3094e-c323-4aaf-af3-663257442a48")
                .hr();
    }

}
