package net.impactdev.gts.sponge.deliveries;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.json.factory.JObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SpongeDelivery implements Delivery {

    private final UUID id;
    private final UUID source;
    private final UUID recipient;
    private final SpongeEntry<?> content;
    private final LocalDateTime expiration;

    public SpongeDelivery(SpongeDeliveryBuilder builder) {
        this.id = builder.id;
        this.source = builder.source;
        this.recipient = builder.recipient;
        this.content = builder.content;
        this.expiration = builder.expiration;
    }

    @Override
    public UUID getID() {
        return this.id;
    }

    @Override
    public UUID getSource() {
        return this.source;
    }

    @Override
    public UUID getRecipient() {
        return this.recipient;
    }

    @Override
    public SpongeEntry<?> getContent() {
        return this.content;
    }

    @Override
    public Optional<LocalDateTime> getExpiration() {
        return Optional.ofNullable(this.expiration);
    }

    @Override
    public void deliver() {

    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        JObject result = new JObject();
        result.add("version", this.getVersion());
        result.add("data", new JObject()
                .add("id", this.id.toString())
                .add("source", this.source.toString())
                .add("recipient", this.recipient.toString())
                .add("content", this.content.serialize())
                .consume(o -> {
                    this.getExpiration().ifPresent(e -> o.add("expiration", e.toString()));
                }));

        return result;
    }

    public static SpongeDelivery deserialize(JsonObject json) {
        JsonObject focus = json.getAsJsonObject("data");
        JsonObject element = focus.getAsJsonObject("entry");
        EntryManager<?> em = GTSService.getInstance().getGTSComponentManager()
                .getEntryManager(element.get("key").getAsString())
                .orElseThrow(() -> new RuntimeException("No Entry Manager found for key: " + element.get("key").getAsString()));

        return (SpongeDelivery) Delivery.builder()
                .id(UUID.fromString(focus.get("id").getAsString()))
                .source(UUID.fromString(focus.get("source").getAsString()))
                .recipient(UUID.fromString(focus.get("recipient").getAsString()))
                .content((SpongeEntry<?>) em.getDeserializer().deserialize(element))
                .expiration(LocalDateTime.parse(focus.get("expiration").getAsString()))
                .build();
    }

    public static class SpongeDeliveryBuilder implements DeliveryBuilder {

        private UUID id = UUID.randomUUID();
        private UUID source;
        private UUID recipient;
        private SpongeEntry<?> content;
        private LocalDateTime expiration;

        @Override
        public DeliveryBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public DeliveryBuilder source(UUID source) {
            this.source = source;
            return this;
        }

        @Override
        public DeliveryBuilder recipient(UUID recipient) {
            this.recipient = recipient;
            return this;
        }

        @Override
        public DeliveryBuilder content(Entry<?, ?> content) {
            this.content = (SpongeEntry<?>) content;
            return this;
        }

        @Override
        public DeliveryBuilder expiration(LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        @Override
        public Delivery build() {
            return new SpongeDelivery(this);
        }
    }
}
