package net.impactdev.gts.test;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.data.ResourceManagerImpl;
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage;

import net.impactdev.gts.test.configurate.TestEntry;
import net.impactdev.gts.test.configurate.TestEntryDataManager;
import net.impactdev.gts.test.configurate.TestListing;
import net.impactdev.gts.test.configurate.TestPrice;
import net.impactdev.gts.test.configurate.TestPriceDataManager;
import net.impactdev.gts.test.configurate.TestableConfigurateStorage;
import net.impactdev.impactor.api.storage.file.loaders.JsonLoader;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JsonConfigurateTest {

    @Test
    public void run() throws Exception {
        ApiRegistrationUtil.register(new GTSAPIProvider());
        net.impactdev.gts.common.api.ApiRegistrationUtil.register(new GTSAPIProvider());

        GTSService.getInstance().getGTSComponentManager().registerListingResourceManager(BuyItNow.class, new ResourceManagerImpl<>("BIN", "minecraft:emerald", TestListing::deserialize));
        GTSService.getInstance().getGTSComponentManager().registerEntryManager(TestEntry.class, new TestEntryDataManager());
        GTSService.getInstance().getGTSComponentManager().registerPriceManager(TestPrice.class, new TestPriceDataManager());

        ConfigurateStorage storage = new TestableConfigurateStorage(null, "JSON", new JsonLoader(), ".json", "json");
        storage.init();

        TestListing listing = new TestListing(new TestEntry(new TestEntry.TestData()), new TestPrice("123"));
        UUID id = listing.getID();
        String price = ((TestPrice) listing.getPrice()).getPrice();
        storage.addListing(listing);

        Listing parsed = storage.getListing(listing.getID()).get();
        assertEquals(id, parsed.getID());
        assertEquals(parsed.getEntry().getVersion(), 1);
        assertEquals(((TestPrice) ((TestListing) parsed).getPrice()).getPrice(), price);
    }
}
