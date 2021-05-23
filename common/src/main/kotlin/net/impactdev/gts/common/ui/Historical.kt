package net.impactdev.gts.common.ui

import java.util.*
import java.util.function.Supplier

interface Historical<T> {
    /**
     * Represents an interface or other design which is generated via a parent of the same nature.
     * This provides a mapping
     *
     * @return
     */
    val parent: Optional<Supplier<T>?>?
}