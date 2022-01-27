package net.impactdev.gts.test.configurate;

import com.google.gson.GsonBuilder;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.api.listings.ui.EntryUI;

import java.util.function.Supplier;

public class TestEntryDataManager implements EntryManager<TestEntry, Void> {

    @Override
    public String getName() {
        return "Test Manager";
    }

    @Override
    public String getItemID() {
        return "";
    }

    @Override
    public Storable.Deserializer<TestEntry> getDeserializer() {
        return json -> new GsonBuilder().setPrettyPrinting().create().fromJson(json, TestEntry.class);
    }

    @Override
    public Class<?> getBlacklistType() {
        return null;
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Void player) {
        return () -> null;
    }

    @Override
    public void supplyDeserializers() {}

    @Override
    public CommandGenerator.EntryGenerator<? extends EntrySelection<? extends Entry<?, ?>>> getEntryCommandCreator() {
        return null;
    }

}
