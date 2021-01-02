import com.google.common.collect.TreeMultimap;
import net.impactdev.gts.api.listings.auctions.Auction;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MappingTests {

    @Test
    public void verifyBidOrder() {
        final TreeMultimap<UUID, Auction.Bid> bids = TreeMultimap.create(
                Comparator.naturalOrder(),
                Collections.reverseOrder(Comparator.comparing(Auction.Bid::getAmount))
        );

        bids.put(UUID.randomUUID(), new Auction.Bid(500));
        bids.put(UUID.randomUUID(), new Auction.Bid(550));

        UUID marker = UUID.randomUUID();
        bids.put(marker, new Auction.Bid(525));
        bids.put(marker, new Auction.Bid(575));

        assertEquals(575, bids.get(marker).first().getAmount(), 0.0);
    }
}
