package net.impactdev.gts.bungee.listings;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.common.listings.JsonStoredEntry;
import net.impactdev.impactor.api.json.factory.JObject;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BungeeListing implements Listing {

    private final UUID id;
    private final UUID lister;
    private final JsonStoredEntry entry;
    private final LocalDateTime published;
    private final LocalDateTime expiration;

    public BungeeListing(UUID id, UUID lister, JsonStoredEntry entry, LocalDateTime expiration) {
        this(id, lister, entry, LocalDateTime.now(), expiration);
    }

    public BungeeListing(UUID id, UUID lister, JsonStoredEntry entry, LocalDateTime published, LocalDateTime expiration) {
        this.id = id;
        this.lister = lister;
        this.entry = entry;
        this.published = published;
        this.expiration = expiration;
    }

    @Override
    public UUID getID() {
        return this.id;
    }

    @Override
    public UUID getLister() {
        return this.lister;
    }

    @Override
    public Entry<?, ?> getEntry() {
        return this.entry;
    }

    @Override
    public LocalDateTime getPublishTime() {
        return this.published;
    }

    @Override
    public LocalDateTime getExpiration() {
        return this.expiration;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        JObject timings = new JObject()
                .add("published", this.getPublishTime().toString())
                .add("expiration", this.getExpiration().toString());

        return new JObject()
                .add("id", this.getID().toString())
                .add("lister", this.getLister().toString())
                .add("version", this.getVersion())
                .add("timings", timings)
                .add("entry", this.entry.getOrCreateElement());
    }
}
