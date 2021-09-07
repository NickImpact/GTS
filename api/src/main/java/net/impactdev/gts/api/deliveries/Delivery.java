package net.impactdev.gts.api.deliveries;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.impactor.api.Impactor;

import java.util.UUID;

/**
 * A Delivery is a special listing in that it doesn't actually appear on the market. Rather, this
 * item will be directly available in a user's stash only.
 *
 * <p>A delivery can also expire. Essentially, if desired, an administrator can set a delivery for
 * a user to expire, such that they must claim it within X amount of time before it is cleaned
 * and deleted by the background system. Otherwise, all deliveries will be set to never expire.</p>
 */
public interface Delivery extends Listing {

    /**
     * Details the ID of the user that will be receiving this delivery.
     *
     * @return The ID of the user receiving the delivery
     */
    UUID getRecipient();

    /**
     * Sends the delivery out to the recipient
     */
    void send();

    static DeliveryBuilder builder() {
        return Impactor.getInstance().getRegistry().createBuilder(DeliveryBuilder.class);
    }

    interface DeliveryBuilder extends ListingBuilder<Delivery, DeliveryBuilder> {

        DeliveryBuilder recipient(UUID recipient);



    }

}
