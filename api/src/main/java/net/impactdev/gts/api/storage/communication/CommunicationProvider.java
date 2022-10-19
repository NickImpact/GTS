package net.impactdev.gts.api.storage.communication;

import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.communication.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.communication.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CommunicationProvider {

    /**
     * Attempts to send a request to the database to process a bid on an auction. Given that the request and response
     * should feature raw json data, the return type of a message response allows for the data to be passed along
     * to the requesting server.
     *
     * @param request The bid request being made by a connected server
     * @return A response indicating the success or failure of the bid request, fit with all necessary information
     */
    CompletableFuture<AuctionMessage.Bid.Response> processBid(AuctionMessage.Bid.Request request);

    /**
     * Indicates a user is attempting to claim something from a listing within their stash. This message
     * is common to both auctions and BIN listings, with auctions receiving an extended response, via
     * {@link ClaimMessage.Response.AuctionResponse}.
     *
     * @param request
     * @return
     */
    CompletableFuture<ClaimMessage.Response> processClaimRequest(ClaimMessage.Request request);

    CompletableFuture<Boolean> appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others);

    CompletableFuture<AuctionMessage.Cancel.Response> processAuctionCancelRequest(AuctionMessage.Cancel.Request request);

    CompletableFuture<BuyItNowMessage.Remove.Response> processListingRemoveRequest(BuyItNowMessage.Remove.Request request);

    CompletableFuture<BuyItNowMessage.Purchase.Response> processPurchase(BuyItNowMessage.Purchase.Request request);

    CompletableFuture<ClaimDelivery.Response> claimDelivery(ClaimDelivery.Request request);

    //------------------------------------------------------------------------------------------------------------------
    //
    //  Admin based message processing
    //
    //------------------------------------------------------------------------------------------------------------------
    CompletableFuture<ForceDeleteMessage.Response> processForcedDeletion(ForceDeleteMessage.Request request);

}
