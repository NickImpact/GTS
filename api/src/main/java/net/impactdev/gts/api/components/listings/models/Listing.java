package net.impactdev.gts.api.components.listings.models;

import net.impactdev.gts.api.storage.DataWritable;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * A listing represents the top-level layer to a component that can be featured on the GTS.
 * This layer consists of metadata which helps identify this particular instance from another.
 */
public interface Listing extends Comparable<Listing>, DataWritable, PrettyPrinter.IPrettyPrintable {

    /**
     * Represents the ID of the actual listing. This identifier should remain unique, and no
     * listing presently in storage should share the same identifier.
     *
     * @return The unique identifier of the listing
     */
    UUID id();

    /**
     * Specifies the unique identifier of the source that listed this listing, if set at all. A
     * lister identifier might not be set only in the event this given listing is created by the
     * server and is a server-provided listing.
     *
     * @return The identifier of the lister, or empty to signify the lister is the server itself.
     */
    Optional<UUID> lister();

    /**
     * Specifies the time when the listing was published to the market.
     *
     * @return The timestamp of when the listing was published
     */
    LocalDateTime published();

    /**
     * Specifies the expiration timestamp for this particular listing, should it be set to
     * expire. A listing will never expire if this timestamp is left empty.
     *
     * @return The expiration timestamp, if set
     */
    Optional<LocalDateTime> expiration();

}
