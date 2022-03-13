package net.impactdev.gts.api.deliveries;

import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * A Delivery is an instance which contains some form of contents, and is delivered straight to the recipient's
 * stash.
 *
 * <p>A delivery can also expire. Essentially, if desired, an administrator can set a delivery for
 * a user to expire, such that they must claim it within X amount of time before it is cleaned
 * and deleted by the background system. Otherwise, all deliveries will be set to never expire.</p>
 */
public interface Delivery extends Storable {

    /**
     * Represents the ID of the delivery. Primarily useful just for easy lookups and references for
     * a particular delivery.
     *
     * @return The ID of the delivery.
     */
    UUID getID();

    /**
     * Specifies the source of the delivery. This indicates who actually gave the delivery to the recipient.
     *
     * @return The uuid of the source of the delivery
     */
    UUID getSource();

    /**
     * Specifies the recipient of the delivery.
     *
     * @return The uuid of the recipient of the delivery
     */
    UUID getRecipient();

    /**
     * Specifies the contents held by this delivery. This is what the recipient will actually be able to
     * redeem when they open the delivery.
     *
     * @return The item provided by the delivery
     */
    Entry<?, ?> getContent();

    /**
     * Marks the date this delivery can expire. If the delivery is not claimed by its expiration date,
     * it will no longer be claimable by the recipient.
     *
     * @return The date and time of expiration for the delivery, or empty to represent no expiration
     */
    Optional<LocalDateTime> getExpiration();

    /**
     * Sends the delivery out to the recipient, informing them if they are online. Otherwise,
     * this will be saved directly to their relative stash.
     */
    void deliver();

    static DeliveryBuilder builder() {
        return Impactor.getInstance().getRegistry().createBuilder(DeliveryBuilder.class);
    }

    interface DeliveryBuilder extends Builder<Delivery, DeliveryBuilder> {

        DeliveryBuilder id(UUID id);

        DeliveryBuilder source(UUID source);

        DeliveryBuilder recipient(UUID recipient);

        DeliveryBuilder content(Entry<?, ?> content);

        DeliveryBuilder expiration(LocalDateTime time);

    }

}
