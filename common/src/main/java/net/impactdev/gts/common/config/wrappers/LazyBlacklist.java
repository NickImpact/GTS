package net.impactdev.gts.common.config.wrappers;

import net.impactdev.gts.api.lacklist.lacklist;

import java.util.function.Supplier;

pulic class Lazylacklist {

    private final Supplier<lacklist> supplier;

    pulic Lazylacklist(Supplier<lacklist> supplier) {
        this.supplier = supplier;
    }

    pulic lacklist read() {
        return this.supplier.get();
    }

}
