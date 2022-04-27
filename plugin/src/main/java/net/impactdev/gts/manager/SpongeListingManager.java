package net.impactdev.gts.manager;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.event.factory.GTSEventFactory;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Fees;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.storage.GTSStorageImpl;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceControlled;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.discord.DiscordNotifier;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.listings.SpongeAuction;
import net.impactdev.gts.sponge.listings.SpongeBuyItNow;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.impactor.api.utilities.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SpongeListingManager implements ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> {

	public static final DiscordNotifier notifier = new DiscordNotifier(GTSPlugin.instance());

	@Override
	public String getServiceName() {
		return "Sponge Listing Manager";
	}

	@Override
	public CompletableFuture<Boolean> list(UUID lister, SpongeListing listing) {
		final Optional<ServerPlayer> source = Sponge.server().player(lister);
		final Config main = GTSPlugin.instance().configuration().main();
		final Config lang = GTSPlugin.instance().configuration().language();
		final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
		final EconomyService economy = Sponge.server().serviceProvider().economyService().orElseThrow(IllegalStateException::new);

		return CompletableFutureManager.makeFuture(() -> {
			PlaceholderSources sources = PlaceholderSources.empty();
			source.ifPresent(player -> sources.append(ServerPlayer.class, () -> player));
			source.ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST))));

			try {
				listing.serialize();
			} catch (Exception e) {
				source.ifPresent(player -> player.sendMessage(parser.parse("{{gts:error}} There's an issue with your listing, so we cancelled the list attempt!")));
				ExceptionWriter.write(e);
			}

			// Check if the user attempting to list a listing has already hit the max amount allowed for a player
			boolean hasMax = this.hasMaxListings(lister).get(2, TimeUnit.SECONDS);
			if(hasMax) {
				source.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.MAX_LISTINGS), sources).forEach(player::sendMessage));
				return false;
			}

			// Publish our event to indicate the user's desire to publish their listing
			if(Impactor.getInstance().getEventBus().post(GTSEventFactory.createPublishListingEvent(lister, listing))) {
				source.ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.LISTING_EVENT_CANCELLED), sources)));
				return false;
			}

			// Retrieve the Price of the listing
			AtomicReference<Price<?, ?, ?>> price;
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
					AtomicDouble min = new AtomicDouble(main.get(ConfigKeys.LISTINGS_MIN_PRICE));
					AtomicDouble max = new AtomicDouble(main.get(ConfigKeys.LISTINGS_MAX_PRICE));

					if(listing.getEntry() instanceof PriceControlled) {
						PriceControlled controls = (PriceControlled) listing.getEntry();
						min.set(Math.max(min.get(), controls.getMin()));
						max.set(Math.min(max.get(), controls.getMax()));
					}

					double actual = monetary.getPrice().doubleValue();
					if(actual < min.get()) {
						PlaceholderSources minSources = PlaceholderSources.builder()
								.from(sources)
								.append(Double.class, min::get)
								.build();

						source.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.MIN_PRICE_ERROR), minSources).forEach(player::sendMessage));
						return false;
					} else if(actual > max.get()) {
						PlaceholderSources maxSources = PlaceholderSources.builder()
								.from(sources)
								.append(Double.class, max::get)
								.build();

						source.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.MAX_PRICE_ERROR), maxSources).forEach(player::sendMessage));
						return false;
					}
				}
			}

			// Let's begin tax application
			//
			// Of course, let's make sure it's enabled, and that the price is also taxable
			Fees.FeeBuilder feeBuilder = Fees.builder();
			AtomicReference<BigDecimal> fees = new AtomicReference<>(BigDecimal.ZERO);
			if(main.get(ConfigKeys.FEES_ENABLED)) {
				fees.updateAndGet(current -> {
					double amount = price.get().calculateFee(listing instanceof BuyItNow);
					feeBuilder.price(price.get(), listing instanceof BuyItNow);
					return current.add(new BigDecimal(amount));
				});

				Time time = new Time(Duration.between(listing.getPublishTime(), listing.getExpiration()).getSeconds());
				Function function = GTSPlugin.instance().configuration().main().get(ConfigKeys.FEE_TIME_EQUATION);
				SimilarPair<Argument> arguments = Utilities.calculateTimeFee(time);
				Expression expression = new Expression("f(hours,minutes)", function, arguments.getFirst(), arguments.getSecond());
				if(!Double.isNaN(expression.calculate())) {
					fees.updateAndGet(current -> {
						double amount = expression.calculate();
						feeBuilder.time(time, amount);
						return current.add(BigDecimal.valueOf(amount));
					});
				}
			}

			// Take the listing from the lister
			AtomicBoolean check = new AtomicBoolean(true);
			source.ifPresent(player -> {
				// Have user pay their fees if any are present
				if(fees.get().doubleValue() > 0) {
					sources.append(BigDecimal.class, fees::get);
					sources.append(Fees.class, feeBuilder::build);
					player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_FEES_COLLECTION), sources));
					economy.findOrCreateAccount(lister).ifPresent(account -> {
						if(account.balance(economy.defaultCurrency()).doubleValue() < fees.get().doubleValue()) {
							parser.parse(lang.get(MsgConfigKeys.FEE_INVALID), sources).forEach(player::sendMessage);
							check.set(false);
						}

						account.withdraw(economy.defaultCurrency(), fees.get());
					});
				}

				if(check.get()) {
					player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_COLLECT_LISTING), sources));
					AtomicBoolean hold = new AtomicBoolean(false);
					AtomicBoolean result = new AtomicBoolean(false);
					Impactor.getInstance().getScheduler().sync().execute(() -> {
						result.set(listing.getEntry().take(lister));
						hold.set(true);
					});
					while(!hold.get()) {
						try {
							//noinspection BusyWait
							Thread.sleep(50);
						} catch (InterruptedException e) {
							ExceptionWriter.write(e);
						}
					}
					if (!result.get()) {
						player.sendMessage(parser.parse(lang.get(MsgConfigKeys.UNABLE_TO_TAKE_LISTING)));
						if(fees.get().doubleValue() > 0) {
							economy.findOrCreateAccount(lister).ifPresent(account -> {
								if (account.balance(economy.defaultCurrency()).doubleValue() < fees.get().doubleValue()) {
									player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_RETURN_FEES), sources));
								}

								account.deposit(economy.defaultCurrency(), fees.get());
							});
						}
						check.set(false);
					}
				}
			});

			if(!check.get()) {
				return false;
			}

			boolean result = GTSPlugin.instance().storage().publishListing(listing).exceptionally(throwable -> {
				source.ifPresent(player -> player.sendMessage(Component.text("Fatal Error Detected").color(NamedTextColor.RED)));
				ExceptionWriter.write(throwable);
				return false;
			}).get(3, TimeUnit.SECONDS);

			if(result) {
				source.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.ADD_TEMPLATE), sources).forEach(player::sendMessage));

				if(main.get(ConfigKeys.FEES_ENABLED)) {
					sources.appendIfAbsent(Fees.class, feeBuilder::build);
					source.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.FEE_APPLICATION), sources).forEach(player::sendMessage));
				}

				PlayerSettingsManager manager = GTSService.getInstance().getPlayerSettingsManager();
				for(Player player : Sponge.server().onlinePlayers()) {
					if (source.isPresent() && !source.get().uniqueId().equals(player.uniqueId())) {
						manager.retrieve(player.uniqueId()).thenAccept(settings -> {
							if(settings.getPublishListenState()) {
								if(listing instanceof BuyItNow) {
									parser.parse(lang.get(MsgConfigKeys.ADD_BROADCAST_BIN), sources).forEach(player::sendMessage);
								} else {
									parser.parse(lang.get(MsgConfigKeys.ADD_BROADCAST_AUCTION), sources).forEach(player::sendMessage);
								}
							}
						});
					}
				}

				GTSPlugin.instance().messagingService().sendPublishNotice(listing.getID(), lister, listing instanceof Auction);

				if(listing instanceof BuyItNow) {
					notifier.forgeAndSend(
							DiscordOption.fetch(DiscordOption.Options.List_BIN),
							MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE,
							listing,
							sources
					);
				} else {
					notifier.forgeAndSend(
							DiscordOption.fetch(DiscordOption.Options.List_Auction),
							MsgConfigKeys.DISCORD_PUBLISH_AUCTION_TEMPLATE,
							listing,
							sources
					);
				}

				return true;
			}

			return false;
		});
	}

	@Override
	public CompletableFuture<Boolean> bid(UUID bidder, SpongeAuction listing, double amount) {
		final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

		Sponge.server().player(bidder).ifPresent(player -> player.sendMessage(
				service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_PROCESSING_BID))
		));

		return CompletableFutureManager.makeFuture(() -> {
			EconomyService economy = Sponge.server().serviceProvider().economyService().get();
			Optional<UniqueAccount> account = economy.findOrCreateAccount(bidder);
			if (!account.isPresent()) {
				Sponge.server().player(bidder).ifPresent(player -> player.sendMessage(
						Component.text("Failed to locate your bank account, no funds have been taken...")
								.color(NamedTextColor.RED)
				));
				return false;
			}

			AtomicDouble actual = new AtomicDouble(amount);
			listing.getCurrentBid(bidder).ifPresent(prior -> actual.set(actual.get() - prior.getAmount()));

			if (account.get().balance(economy.defaultCurrency()).doubleValue() < actual.get()) {
				Sponge.server().player(bidder).ifPresent(player -> player.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID))));
				return false;
			}

			if (!Impactor.getInstance().getEventBus().post(GTSEventFactory.createBidEvent(bidder, listing, amount))) {
				Sponge.server().player(bidder).ifPresent(player -> player.sendMessage(
						service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_TO_ESCROW))
				));

				TransactionResult result = account.get().withdraw(economy.defaultCurrency(), BigDecimal.valueOf(actual.get()));
				if (result.result().equals(ResultType.ACCOUNT_NO_FUNDS)) {
					Sponge.server().player(bidder).ifPresent(player -> player.sendMessage(
							service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID))
					));
					return false;
				}

				PlaceholderSources sources = PlaceholderSources.builder()
						.append(Double.class, () -> amount)
						.append(UUID.class, () -> bidder)
						.build();
				notifier.forgeAndSend(
						DiscordOption.fetch(DiscordOption.Options.Bid),
						MsgConfigKeys.DISCORD_BID_TEMPLATE,
						listing,
						sources
				);

				return GTSPlugin.instance().messagingService().publishBid(listing.getID(), bidder, amount)
						.thenApply(response -> {
							if(response.wasSuccessful()) {
								Auction.BidContext context = new Auction.BidContext(bidder, new Auction.Bid(amount));
								Sponge.server().player(bidder).ifPresent(player -> {
									ConfigKey<String> key = response.wasSniped() ? MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_BID_PLACEDSNIPED : MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_BID_PLACED;
									sources.append(Auction.BidContext.class, () -> context);
									sources.append(Time.class, () -> GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_SET_TIME));

									player.sendMessage(service.parse(Utilities.readMessageConfigOption(key), sources));
								});
							} else {
								sources.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN));
								// Return funds placed in escrow
								Sponge.server().player(bidder).ifPresent(player -> {
									player.sendMessage(service.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
											sources
									));
									player.sendMessage(service.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)
									));

									account.get().deposit(economy.defaultCurrency(), BigDecimal.valueOf(actual.get()));
								});
							}

							return response.wasSuccessful();
						})
						.get(5, TimeUnit.SECONDS);
			} else {
				Sponge.server().player(bidder).ifPresent(player -> {
					player.sendMessage(service.parse(
							Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
							PlaceholderSources.builder().append(ErrorCode.class, () -> ErrorCodes.THIRD_PARTY_CANCELLED).build())
					);
				});
			}
			return false;
		});

	}

	@Override
	public CompletableFuture<Boolean> purchase(UUID buyer, SpongeBuyItNow listing, Object source) {
		final Config lang = GTSPlugin.instance().configuration().language();
		final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);
		return CompletableFutureManager.makeFuture(() -> {
			Sponge.server().player(buyer).ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST))));
			if (source == null || listing.getPrice().getSourceType().equals(source.getClass())) {
				boolean canPay = CompletableFutureManager.makeFuture(() -> listing.getPrice().canPay(buyer), Impactor.getInstance().getScheduler().sync()).get(2, TimeUnit.SECONDS);

				if (canPay && !Impactor.getInstance().getEventBus().post(GTSEventFactory.createPurchaseListingEvent(buyer, listing))) {
					Sponge.server().player(buyer).ifPresent(player -> player.sendMessage(
							parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_TO_ESCROW))
					));

					final AtomicBoolean marker = new AtomicBoolean(false);
					listing.getPrice().pay(buyer, source, marker);

					return GTSPlugin.instance().messagingService().requestBINPurchase(listing.getID(), buyer, source)
							.thenApply(response -> {
								if(response.wasSuccessful()) {
									while(!marker.get()) {
										try {
											//noinspection BusyWait
											Thread.sleep(50);
										} catch (InterruptedException e) {
											ExceptionWriter.write(e);
										}
									}

									PlaceholderSources sources = PlaceholderSources.builder()
											.append(Listing.class, () -> listing)
											.build();

									Sponge.server().player(buyer)
											.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.PURCHASE_PAY), sources).forEach(player::sendMessage));

									if(!listing.getEntry().give(buyer)) {
										BuyItNow delegate = BuyItNow.builder()
												.from(listing)
												.id(UUID.randomUUID())
												.expiration(LocalDateTime.now())
												.purchased()
												.purchaser(buyer)
												.stashedForPurchaser()
												.build();

										GTSPlugin.instance().storage().publishListing(delegate);
										Sponge.server().player(buyer)
												.ifPresent(player -> parser.parse(lang.get(MsgConfigKeys.PURCHASE_PAY_FAIL_TO_GIVE), sources).forEach(player::sendMessage));
									}

									// We do the following such that we will ensure we populate any potential source
									// the price may require
									listing.markPurchased();
									((GTSStorageImpl) GTSPlugin.instance().storage()).sendListingUpdate(listing)
											.exceptionally(e -> {
												GTSPlugin.instance().logger().error("Fatal error detected while updating listing with price, see error below:");
												ExceptionWriter.write(e);
												return false;
											});

									notifier.forgeAndSend(
											DiscordOption.fetch(DiscordOption.Options.Purchase),
											MsgConfigKeys.DISCORD_PURCHASE_TEMPLATE,
											listing,
											sources.append(UUID.class, () -> buyer)
									);
								} else {
									PlaceholderSources sources = PlaceholderSources.builder()
											.append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
											.build();
									Sponge.server().player(buyer).ifPresent(player -> {
										player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED), sources));
										player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)));

										listing.getPrice().reward(buyer);
									});
								}

								return response.wasSuccessful();
							})
							.exceptionally(throwable -> {
								final ErrorCode error = throwable instanceof TimeoutException ? ErrorCodes.REQUEST_TIMED_OUT : ErrorCodes.FATAL_ERROR;
								Sponge.server().player(buyer).ifPresent(player -> {
									PlaceholderSources sources = PlaceholderSources.builder()
											.append(ErrorCode.class, () -> error)
											.build();

									player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED), sources));
									player.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)));

									listing.getPrice().reward(buyer);
								});
								return false;
							})
							.get(5, TimeUnit.SECONDS);
				}

				return false;
			} else {
				throw new CompletionException(new IllegalArgumentException("Source of price is invalid"));
			}

		});
	}

	@Override
	public CompletableFuture<Boolean> deleteListing(SpongeListing listing) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletableFuture<Boolean> hasMaxListings(UUID lister) {
		if(GTSPlugin.instance().configuration().main().get(ConfigKeys.MAX_LISTINGS_PER_USER) <= 0) {
			return CompletableFuture.supplyAsync(() -> false);
		}
		return GTSPlugin.instance().storage().hasMaxListings(lister);
	}

	@Override
	public CompletableFuture<List<SpongeListing>> fetchListings() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return GTSPlugin.instance().storage().fetchListings().get()
						.stream()
						.map(listing -> (SpongeListing) listing)
						.collect(Collectors.toList());
			} catch (Exception e) {
				ExceptionWriter.write(e);
				return Lists.newArrayList();
			}
		});
	}
}
