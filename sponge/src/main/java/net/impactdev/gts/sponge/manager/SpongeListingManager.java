package net.impactdev.gts.sponge.manager;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.gts.api.events.PublishListingEvent;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.discord.DiscordNotifier;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.gts.common.discord.Message;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeListingManager implements ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> {

	private final DiscordNotifier notifier = new DiscordNotifier(GTSPlugin.getInstance());

	@Override
	public String getServiceName() {
		return "Sponge Listing Manager";
	}

	@Override
	public CompletableFuture<Boolean> list(UUID lister, SpongeListing listing) {
		final Optional<Player> source = Sponge.getServer().getPlayer(lister);
		final Config main = GTSPlugin.getInstance().getConfiguration();
		final Config lang = GTSPlugin.getInstance().getMsgConfig();
		final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

		return CompletableFutureManager.makeFuture(() -> {
			List<Supplier<Object>> sources = Lists.newArrayList(() -> listing);
			source.ifPresent(player -> sources.add(() -> player));

			source.ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PUBLISH_REQUEST))));

			// Check if the user attempting to list a listing has already hit the max amount allowed for a player
			boolean hasMax = this.hasMaxListings(lister).get(2, TimeUnit.SECONDS);
			if(hasMax) {
				source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.MAX_LISTINGS), sources)));
				return false;
			}

			// Publish our event to indicate the user's desire to publish their listing
			if(Impactor.getInstance().getEventBus().post(PublishListingEvent.class, lister, listing)) {
				source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.LISTING_EVENT_CANCELLED), sources)));
				return false;
			}

			// Retrieve the Price of the listing
			AtomicReference<Price<?, ?>> price;
			if(listing instanceof Auction) {
				price = new AtomicReference<>(new MonetaryPrice(((Auction) listing).getStartingPrice()));
			} else {
				price = new AtomicReference<>(((BuyItNow) listing).getPrice());
			}

			// Check if the entry is price controlled
			//
			// If controlled, and our configuration states we should apply controls,
			// take our price and ensure it's a MoneyPrice. If so, apply price controls
			// as necessary.
			if(main.get(ConfigKeys.PRICE_CONTROL_ENABLED)) {
				if (price.get() instanceof MonetaryPrice) {
					MonetaryPrice monetary = (MonetaryPrice) price.get();
					double min = main.get(ConfigKeys.LISTINGS_MIN_PRICE);
					double max = main.get(ConfigKeys.LISTINGS_MAX_PRICE);

					if(listing.getEntry() instanceof PriceControlled) {
						PriceControlled controls = (PriceControlled) listing.getEntry();
						min = Math.max(min, controls.getMin());
						max = Math.min(max, controls.getMax());
					}

					double actual = monetary.getPrice().doubleValue();
					if(actual < min || actual > max) {
						source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.MIN_PRICE_ERROR), sources)));
						return false;
					}
				}
			}

			// Let's begin tax application
			//
			// Of course, let's make sure it's enabled, and that the price is also taxable
			AtomicReference<BigDecimal> fees = new AtomicReference<>(BigDecimal.ZERO);
			if(main.get(ConfigKeys.TAXES_ENABLED)) {
				if(price.get() instanceof MonetaryPrice) {
					MonetaryPrice monetary = (MonetaryPrice) price.get();
					float rate = main.get(ConfigKeys.TAXES_RATE);

					fees.set(monetary.getPrice().multiply(new BigDecimal(rate)));
				}
			}

			// Take the listing from the lister
			AtomicBoolean check = new AtomicBoolean(true);
			source.ifPresent(player -> {
				// Have user pay their fees if any are present
				if(fees.get().doubleValue() > 0) {
					sources.add(fees::get);
					player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_FEES_COLLECTION), sources));
					EconomyService economy = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
					economy.getOrCreateAccount(lister).ifPresent(account -> {
						if(account.getBalance(economy.getDefaultCurrency()).doubleValue() < fees.get().doubleValue()) {
							player.sendMessages(parser.parse(lang.get(MsgConfigKeys.TAX_INVALID), sources));
							check.set(false);
						}

						account.withdraw(economy.getDefaultCurrency(), fees.get(), Sponge.getCauseStackManager().getCurrentCause());
					});
				}

				if(check.get()) {
					player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_COLLECT_LISTING), sources));
					if (!listing.getEntry().take(lister)) {
						player.sendMessage(parser.parse(lang.get(MsgConfigKeys.UNABLE_TO_TAKE_LISTING)));
						if(fees.get().doubleValue() > 0) {
							EconomyService economy = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
							economy.getOrCreateAccount(lister).ifPresent(account -> {
								if (account.getBalance(economy.getDefaultCurrency()).doubleValue() < fees.get().doubleValue()) {
									player.sendMessages(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_RETURN_FEES), sources));
								}

								account.deposit(economy.getDefaultCurrency(), fees.get(), Sponge.getCauseStackManager().getCurrentCause());
							});
						}
						check.set(false);
					}
				}
			});

			if(!check.get()) {
				return false;
			}

			GTSPlugin.getInstance().getStorage().publishListing(listing).exceptionally(throwable -> {
				source.ifPresent(player -> player.sendMessage(Text.of(TextColors.RED, "Fatal Error Detected")));
				ExceptionWriter.write(throwable);
				return false;
			}).get(3, TimeUnit.SECONDS);

			source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.ADD_TEMPLATE), sources)));

			List<UUID> ignoring = this.getIgnorers().get(2, TimeUnit.SECONDS);
			for(Player player : Sponge.getServer().getOnlinePlayers()) {
				if(!ignoring.contains(player.getUniqueId())) {
					if (source.isPresent() && !source.get().getUniqueId().equals(player.getUniqueId())) {
						player.sendMessages(parser.parse(lang.get(MsgConfigKeys.ADD_BROADCAST), sources));
					}
				}
			}

			Message message = this.notifier.forgeMessage(
					DiscordOption.fetch(DiscordOption.Options.List),
					MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE,
					listing
			);
			this.notifier.sendMessage(message);

			return true;
		}, Impactor.getInstance().getScheduler().async());
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
				return GTSPlugin.getInstance().getStorage().fetchListings().get()
						.stream()
						.map(listing -> (SpongeListing) listing)
						.collect(Collectors.toList());
			} catch (Exception e) {
				ExceptionWriter.write(e);
				return Lists.newArrayList();
			}
		});
	}

	@Override
	public CompletableFuture<List<UUID>> getIgnorers() {
		return CompletableFuture.supplyAsync(Lists::newArrayList);
	}

	private CompletableFuture<Boolean> schedule(Supplier<Boolean> task) {
		return CompletableFuture.supplyAsync(task, Impactor.getInstance().getScheduler().async());
	}
}
