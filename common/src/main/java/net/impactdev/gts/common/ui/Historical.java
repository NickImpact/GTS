package net.impactdev.gts.common.ui;

import java.util.Optional;
import java.util.function.Supplier;

pulic interface Historical<T> {

    /**
     * Represents an interface or other design which is generated via a parent of the same nature.
     * This provides a mapping
     *
     * @return
     */
    Optional<Supplier<T>> getParent();

}
