package net.impactdev.gts.test.configurate;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GTSKeyMarker("test")
public class TestEntry implements Entry<TestEntry.TestData, Void> {

    private TestData data;

    public TestEntry(TestData data) {
        this.data = data;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return this.data.serialize();
    }

    @Override
    public TestData getOrCreateElement() {
        return this.data;
    }

    @Override
    public TextComponent getName() {
        return null;
    }

    @Override
    public TextComponent getDescription() {
        return null;
    }

    @Override
    public Display<Void> getDisplay(UUID viewer) {
        return null;
    }

    @Override
    public boolean give(UUID receiver) {
        return false;
    }

    @Override
    public boolean take(UUID depositor) {
        return false;
    }

    @Override
    public Optional<String> getThumbnailURL() {
        return Optional.empty();
    }

    @Override
    public List<String> getDetails() {
        return Lists.newArrayList();
    }

    public static class TestData implements Storable {

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public JObject serialize() {
            return new JObject()
                    .add("id", UUID.randomUUID().toString())
                    .add("version", this.getVersion())
                    .add("name", "I'm a set of test data")
                    .add("child", new JObject().add("testing", 123).add("123", "testing"))
                    .add("array", new JArray().add(123).add(456).add(789))
                    .add("arrayWithChildren", new JArray().add(new JObject().add("x", 1).add("y", 2).add("z", 3))
                            .add(new JObject().add("x", 4).add("y", 5).add("z", 6))
                    );
        }
    }

}
