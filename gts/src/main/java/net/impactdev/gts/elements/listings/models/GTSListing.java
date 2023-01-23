package net.impactdev.gts.elements.listings.models;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.elements.content.Content;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JObject;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public abstract class GTSListing implements Listing {

    private final UUID id;
    private final @Nullable UUID lister;
    private final Content<?> content;
    private final LocalDateTime published;
    private final @Nullable LocalDateTime expiration;

    protected GTSListing(GTSListingBuilder<?> builder) {
        this.id = builder.id;
        this.lister = builder.lister;
        this.content = builder.content;
        this.published = builder.published;
        this.expiration = builder.expiration;
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public Optional<UUID> lister() {
        return Optional.ofNullable(this.lister);
    }

    @Override
    public @NotNull Component asComponent() {
        return this.content.asComponent();
    }

    @Override
    public Content<?> content() {
        return this.content;
    }

    @Override
    public LocalDateTime published() {
        return this.published;
    }

    @Override
    public Optional<LocalDateTime> expiration() {
        return Optional.ofNullable(this.expiration);
    }

    @Override
    public JsonObject serialize() {
        JObject json = new JObject()
                .add("version", this.version())
                .add("uuid", this.id.toString())
                .consume(o -> Optional.ofNullable(this.lister).ifPresent(id -> o.add("lister", id.toString())))
                .add("published", this.published.toString())
                .consume(o -> Optional.ofNullable(this.expiration).ifPresent(id -> o.add("expiration", id.toString())))
                .add("content", this.content.serialize());

        this.serialize$child(json);
        return json.toJson();
    }

    protected abstract void serialize$child(JObject json);

    @Override
    public void print(PrettyPrinter printer) {
        printer.kv("Version", this.version());
        printer.kv("ID", this.id());
        printer.kv("Lister", this.lister().map(UUID::toString).orElse("Server"));
        printer.kv("Published", this.published());
        printer.kv("Expiration", this.expiration().map(LocalDateTime::toString).orElse("N/A"));
        printer.newline();
        printer.add("Content:");
        printer.add(this.content);
    }

    public abstract static class GTSListingBuilder<B extends ListingBuilder<B>> implements ListingBuilder<B> {

        private UUID id;
        private UUID lister;
        private Content<?> content;
        private LocalDateTime published;
        private LocalDateTime expiration;

        @Override
        public B id(UUID uuid) {
            this.id = uuid;
            return (B) this;
        }

        @Override
        public B lister(UUID uuid) {
            this.lister = uuid;
            return (B) this;
        }

        @Override
        public B content(Content<?> content) {
            this.content = content;
            return (B) this;
        }

        @Override
        public B published(LocalDateTime timestamp) {
            this.published = timestamp;
            return (B) this;
        }

        @Override
        public B expiration(LocalDateTime timestamp) {
            this.expiration = timestamp;
            return (B) this;
        }

    }
}
