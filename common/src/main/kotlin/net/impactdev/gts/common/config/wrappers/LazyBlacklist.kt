package net.impactdev.gts.common.config.wrappers

import net.impactdev.gts.api.blacklist.Blacklist
import java.util.function.Supplier

class LazyBlacklist(private val supplier: Supplier<Blacklist>) {
    fun read(): Blacklist {
        return supplier.get()
    }
}