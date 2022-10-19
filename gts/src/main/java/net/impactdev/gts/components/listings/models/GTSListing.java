package net.impactdev.gts.components.listings.models;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.components.content.Content;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
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
    public int compareTo(@NotNull Listing o) {
        return 0;
    }

    @Override
    public JsonObject serialize() {
        // TODO - Create a JSON module for Impactor that Impactor itself inherits, so other projects can also use it
        JsonObject json = new JsonObject();

        return null;
    }

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
