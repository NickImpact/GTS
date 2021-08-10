package net.impactdev.gts.test.configurate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.json.factory.JObject;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestListing implements Listing, BuyItNow {

    private UUID id = UUID.randomUUID();
    private final TestEntry entry;
    private final TestPrice price;

    public TestListing(TestEntry entry, TestPrice price) {
        this.entry = entry;
        this.price = price;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        Preconditions.checkArgument(this.getEntry().getClass().isAnnotationPresent(GTSKeyMarker.class), "An Entry type must be annotated with GTSKeyMarker");

        JObject timings = new JObject()
                .add("published", this.getPublishTime().toString())
                .add("expiration", this.getExpiration().toString());

        JObject entry = new JObject()
                .add("key", this.getEntry().getClass().getAnnotation(GTSKeyMarker.class).value()[0])
                .add("content", this.getEntry().serialize());

        JObject price = new JObject()
                .add("key", this.getPrice().getClass().getAnnotation(GTSKeyMarker.class).value()[0])
                .add("content", this.getPrice().serialize());

        return new JObject()
                .add("id", this.getID().toString())
                .add("lister", this.getLister().toString())
                .add("version", this.getVersion())
                .add("timings", timings)
                .add("entry", entry)
                .add("price", price)
                .add("type", "bin");
    }

    public static TestListing deserialize(JsonObject json) {
        JsonObject element = json.getAsJsonObject("entry");
        EntryManager<?, ?> entryManager = GTSService.getInstance().getGTSComponentManager()
                .getEntryManager(element.get("key").getAsString())
                .orElseThrow(() -> new RuntimeException("No Entry Manager found for key: " + element.get("key").getAsString()));

        JsonObject price = json.getAsJsonObject("price");
        Storable.Deserializer<Price<?, ?, ?>> deserializer = GTSService.getInstance().getGTSComponentManager()
                .getPriceManager(price.get("key").getAsString())
                .map(ResourceManager::getDeserializer)
                .orElseThrow(() -> new RuntimeException("JSON Data for price is missing mapping key"));

        TestListing result = new TestListing(
                (TestEntry) entryManager.getDeserializer().deserialize(element.getAsJsonObject("content")),
                (TestPrice) deserializer.deserialize(price.getAsJsonObject("content"))
        );
        result.id = UUID.fromString(json.get("id").getAsString());
        return result;
    }

    @Override
    public UUID getID() {
        return this.id;
    }

    @Override
    public UUID getLister() {
        return UUID.randomUUID();
    }

    @Override
    public Entry<?, ?> getEntry() {
        return this.entry;
    }

    @Override
    public LocalDateTime getPublishTime() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime getExpiration() {
        return LocalDateTime.now();
    }

    @Override
    public void setExpiration(LocalDateTime expiration) {}

    @Override
    public Price<?, ?, ?> getPrice() {
        return this.price;
    }

    @Override
    public UUID purchaser() {
        return null;
    }

    @Override
    public boolean stashedForPurchaser() {
        return false;
    }

    @Override
    public boolean isPurchased() {
        return false;
    }

    @Override
    public void markPurchased() {

    }
}
