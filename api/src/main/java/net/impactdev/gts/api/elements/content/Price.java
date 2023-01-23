package net.impactdev.gts.api.elements.content;

import net.impactdev.gts.api.storage.StorableContent;
import net.kyori.adventure.text.ComponentLike;

import java.math.BigDecimal;

/**
 * A sense of value that a user must pay for a listing in order to obtain it.
 */
public interface Price extends StorableContent, ComponentLike {

    /**
     * A mutable price is one that can be adjusted via a number. These prices are used
     * for listing types that should be based on a price that changes through its lifetime,
     * such as auctions. Adjustments to this type of price will result in a new instance as
     * to not directly affect the original price, so each price can maintain its immutable status.
     */
    interface Incremental extends Price, Comparable<Incremental> {

        BigDecimal asNumber();

        Incremental add(BigDecimal amount);

    }

}
