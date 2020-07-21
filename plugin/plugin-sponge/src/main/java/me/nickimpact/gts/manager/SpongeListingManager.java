package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.Impactor;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.sponge.listings.SpongeAuction;
import me.nickimpact.gts.sponge.listings.SpongeBuyItNow;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SpongeListingManager implements ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> {

	@Override
	public String getServiceName() {
		return "Sponge Listing Manager";
	}

	@Override
	public List<UUID> getIgnorers() {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addToMarket(UUID lister, SpongeListing listing) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletableFuture<Boolean> bid(UUID bidder, SpongeAuction listing, double amount) {
		return this.schedule(() -> {
			EconomyService economy = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
			Optional<UniqueAccount> account = economy.getOrCreateAccount(bidder);
			if(!account.isPresent()) {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
						Text.of(TextColors.RED, "Failed to locate your bank account, no funds have been taken...")
				));
				return false;
			}

			if(account.get().getBalance(economy.getDefaultCurrency()).doubleValue() < amount) {

			}

			Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
					Text.of(TextColors.GRAY, "Putting funds in escrow...")
			));



			TransactionResult result = account.get().withdraw(economy.getDefaultCurrency(), new BigDecimal(amount), Sponge.getCauseStackManager().getCurrentCause());
			if(result.getResult().equals(ResultType.ACCOUNT_NO_FUNDS)) {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
						Text.of(TextColors.RED, "You don't have enough funds for this bid...")
				));
				return false;
			}

			Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
					Text.of(TextColors.GRAY, "Processing bid...")
			));
			GTSPlugin.getInstance().getMessagingService().publishBid(listing.getID(), bidder, amount);
			return true;
		});

	}

	@Override
	public CompletableFuture<Boolean> purchase(UUID buyer, SpongeBuyItNow listing) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletableFuture<Boolean> deleteListing(SpongeListing listing) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletableFuture<Boolean> hasMaxListings(UUID lister) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletableFuture<List<SpongeListing>> fetchListings() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return Lists.newArrayList();
		});
	}

	private CompletableFuture<Boolean> schedule(Supplier<Boolean> task) {
		return CompletableFuture.supplyAsync(task, Impactor.getInstance().getScheduler().async());
	}
}
