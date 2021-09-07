package net.impactdev.gts.sponge.listings;

import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.json.factory.JObject;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpongeDelivery extends SpongeListing implements Delivery {

    private UUID recipient;

    public SpongeDelivery(SpongeDeliveryBuilder builder) {
        super(builder.id, builder.lister, builder.entry, builder.expiration);
    }

    @Override
    public UUID getRecipient() {
        return this.recipient;
    }

    @Override
    public void send() {

    }

    @Override
    public JObject serialize() {
        JObject result = super.serialize();
        result.add("recipient", this.recipient.toString());

        return result;
    }

    public static class SpongeDeliveryBuilder implements DeliveryBuilder {

        private UUID id = UUID.randomUUID();
        private UUID lister;
        private SpongeEntry<?> entry;
        private LocalDateTime expiration;

        private UUID recipient;

        @Override
        public DeliveryBuilder recipient(UUID recipient) {
            this.recipient = recipient;
            return this;
        }

        @Override
        public DeliveryBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public DeliveryBuilder lister(UUID lister) {
            this.lister = lister;
            return this;
        }

        @Override
        public DeliveryBuilder entry(Entry<?, ?> entry) {
            return this;
        }

        @Override
        public DeliveryBuilder price(Price<?, ?, ?> price) {
            throw new UnsupportedOperationException("Deliveries will never have a price!");
        }

        @Override
        public DeliveryBuilder expiration(LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        @Override
        public DeliveryBuilder from(Delivery delivery) {
            return this;
        }

        @Override
        public Delivery build() {
            return new SpongeDelivery(this);
        }
    }
}
