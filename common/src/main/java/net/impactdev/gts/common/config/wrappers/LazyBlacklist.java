package net.impactdev.gts.common.config.wrappers;

import net.impactdev.gts.api.blacklist.Blacklist;

import java.util.function.Supplier;

public class LazyBlacklist {

    private final Supplier<Blacklist> supplier;

    public LazyBlacklist(Supplier<Blacklist> supplier) {
        this.supplier = supplier;
    }

    public Blacklist read() {
        return this.supplier.get();
    }

}
