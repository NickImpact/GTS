package net.impactdev.gts.manager;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.auctions.BidEvent;
import net.impactdev.gts.api.events.buyitnow.PurchaseListingEvent;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.makeup.Fees;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.storage.GTSStorageImpl;
import net.impactdev.gts.sponge.utils.Utilities;
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
import net.impactdev.gts.common.config.ConfigKeys;
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
import net.impactdev.impactor.api.utilities.Time;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeListingManager implements ListingManager<SpongeListing, SpongeAuction, SpongeBuyItNow> {

	public static final DiscordNotifier notifier = new DiscordNotifier(GTSPlugin.getInstance());

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

			source.ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST))));

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
						List<Supplier<Object>> minSources = Lists.newArrayList(sources);
						minSources.add(min::get);

						source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.MIN_PRICE_ERROR), minSources)));
						return false;
					} else if(actual > max.get()) {
						List<Supplier<Object>> maxSources = Lists.newArrayList(sources);
						maxSources.add(max::get);

						source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.MAX_PRICE_ERROR), maxSources)));
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
				Function function = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.FEE_TIME_EQUATION);
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
					sources.add(fees::get);
					sources.add(feeBuilder::build);
					player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_FEES_COLLECTION), sources));
					EconomyService economy = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
					economy.getOrCreateAccount(lister).ifPresent(account -> {
						if(account.getBalance(economy.getDefaultCurrency()).doubleValue() < fees.get().doubleValue()) {
							player.sendMessages(parser.parse(lang.get(MsgConfigKeys.FEE_INVALID), sources));
							check.set(false);
						}

						account.withdraw(economy.getDefaultCurrency(), fees.get(), Cause.builder()
								.append(player)
								.build(EventContext.builder()
										.add(EventContextKeys.PLUGIN, GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer())
										.build()
								)
						);
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

								account.deposit(economy.getDefaultCurrency(), fees.get(), Cause.builder()
										.append(player)
										.build(EventContext.builder()
												.add(EventContextKeys.PLUGIN, GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer())
												.build()
										)
								);
							});
						}
						check.set(false);
					}
				}
			});

			if(!check.get()) {
				return false;
			}

			boolean result = GTSPlugin.getInstance().getStorage().publishListing(listing).exceptionally(throwable -> {
				source.ifPresent(player -> player.sendMessage(Text.of(TextColors.RED, "Fatal Error Detected")));
				ExceptionWriter.write(throwable);
				return false;
			}).get(3, TimeUnit.SECONDS);

			if(result) {
				source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.ADD_TEMPLATE), sources)));

				if(main.get(ConfigKeys.FEES_ENABLED)) {
					sources.add(feeBuilder::build);
					source.ifPresent(player -> player.sendMessages(parser.parse(lang.get(MsgConfigKeys.FEE_APPLICATION), sources)));
				}

				PlayerSettingsManager manager = GTSService.getInstance().getPlayerSettingsManager();
				for(Player player : Sponge.getServer().getOnlinePlayers()) {
					if (source.isPresent() && !source.get().getUniqueId().equals(player.getUniqueId())) {
						manager.retrieve(player.getUniqueId()).thenAccept(settings -> {
							if(settings.getPublishListenState()) {
								if(listing instanceof BuyItNow) {
									player.sendMessages(parser.parse(lang.get(MsgConfigKeys.ADD_BROADCAST_BIN), sources));
								} else {
									player.sendMessages(parser.parse(lang.get(MsgConfigKeys.ADD_BROADCAST_AUCTION), sources));
								}
							}
						});
					}
				}

				GTSPlugin.getInstance().getMessagingService().sendPublishNotice(listing.getID(), lister, listing instanceof Auction);

				if(listing instanceof BuyItNow) {
					Message message = notifier.forgeMessage(
							DiscordOption.fetch(DiscordOption.Options.List_BIN),
							MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE,
							listing
					);
					notifier.sendMessage(message);
				} else {
					notifier.sendMessage(notifier.forgeMessage(
							DiscordOption.fetch(DiscordOption.Options.List_Auction),
							MsgConfigKeys.DISCORD_PUBLISH_AUCTION_TEMPLATE,
							listing
					));
				}

				return true;
			}

			return false;
		});
	}

	@Override
	public CompletableFuture<Boolean> bid(UUID bidder, SpongeAuction listing, double amount) {
		final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

		Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
				service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_PROCESSING_BID))
		));

		return CompletableFutureManager.makeFuture(() -> {
			EconomyService economy = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
			Optional<UniqueAccount> account = economy.getOrCreateAccount(bidder);
			if (!account.isPresent()) {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
						Text.of(TextColors.RED, "Failed to locate your bank account, no funds have been taken...")
				));
				return false;
			}

			AtomicDouble actual = new AtomicDouble(amount);
			listing.getCurrentBid(bidder).ifPresent(prior -> actual.set(actual.get() - prior.getAmount()));

			if (account.get().getBalance(economy.getDefaultCurrency()).doubleValue() < actual.get()) {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID))));
				return false;
			}

			if (!Impactor.getInstance().getEventBus().post(BidEvent.class, bidder, listing, amount)) {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
						service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_TO_ESCROW))
				));

				TransactionResult result = account.get().withdraw(
						economy.getDefaultCurrency(),
						BigDecimal.valueOf(actual.get()),
						Cause.builder()
								.append(bidder)
								.build(EventContext.builder()
									.add(EventContextKeys.PLUGIN, GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer())
									.build()
								)
				);
				if (result.getResult().equals(ResultType.ACCOUNT_NO_FUNDS)) {
					Sponge.getServer().getPlayer(bidder).ifPresent(player -> player.sendMessage(
							service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID))
					));
					return false;
				}

				Message message = notifier.forgeMessage(
						DiscordOption.fetch(DiscordOption.Options.Bid),
						MsgConfigKeys.DISCORD_BID_TEMPLATE,
						listing, amount, bidder
				);
				notifier.sendMessage(message);

				return GTSPlugin.getInstance().getMessagingService().publishBid(listing.getID(), bidder, amount)
						.thenApply(response -> {
							if(response.wasSuccessful()) {
								Auction.BidContext context = new Auction.BidContext(bidder, new Auction.Bid(amount));
								Sponge.getServer().getPlayer(bidder).ifPresent(player -> {
									player.sendMessage(service.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_BID_PLACED),
											Lists.newArrayList(() -> context)
									));
								});
							} else {
								// Return funds placed in escrow
								Sponge.getServer().getPlayer(bidder).ifPresent(player -> {
									player.sendMessage(service.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
											Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
									));
									player.sendMessage(service.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)
									));

									account.get().deposit(
											economy.getDefaultCurrency(),
											BigDecimal.valueOf(actual.get()),
											Cause.builder()
													.append(bidder)
													.build(EventContext.builder()
															.add(EventContextKeys.PLUGIN, GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer())
															.build()
													)
									);
								});
							}

							return response.wasSuccessful();
						})
						.get(5, TimeUnit.SECONDS);
			} else {
				Sponge.getServer().getPlayer(bidder).ifPresent(player -> {
					player.sendMessage(service.parse(
							Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
							Lists.newArrayList(() -> ErrorCodes.THIRD_PARTY_CANCELLED)
					));
				});
			}
			return false;
		});

	}

	@Override
	public CompletableFuture<Boolean> purchase(UUID buyer, SpongeBuyItNow listing, Object source) {
		final Config lang = GTSPlugin.getInstance().getMsgConfig();
		final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
		return CompletableFutureManager.makeFuture(() -> {
			Sponge.getServer().getPlayer(buyer).ifPresent(player -> player.sendMessage(parser.parse(lang.get(MsgConfigKeys.GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST))));
			if (source == null || listing.getPrice().getSourceType().equals(source.getClass())) {
				boolean canPay = CompletableFutureManager.makeFuture(() -> listing.getPrice().canPay(buyer), Impactor.getInstance().getScheduler().sync()).get(2, TimeUnit.SECONDS);

				if (canPay && !Impactor.getInstance().getEventBus().post(PurchaseListingEvent.class, buyer, listing)) {
					Sponge.getServer().getPlayer(buyer).ifPresent(player -> player.sendMessage(
							parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_TO_ESCROW))
					));

					final AtomicBoolean marker = new AtomicBoolean(false);
					listing.getPrice().pay(buyer, source, marker);

					return GTSPlugin.getInstance().getMessagingService().requestBINPurchase(listing.getID(), buyer, source)
							.thenApply(response -> {
								if(response.wasSuccessful()) {
									while(!marker.get()) {}

									Sponge.getServer().getPlayer(buyer).ifPresent(player -> player.sendMessages(
											parser.parse(lang.get(MsgConfigKeys.PURCHASE_PAY), Lists.newArrayList(
													() -> listing
											))
									));

									if(!listing.getEntry().give(buyer)) {
										BuyItNow delegate = BuyItNow.builder()
												.from(listing)
												.id(UUID.randomUUID())
												.expiration(LocalDateTime.now())
												.purchased()
												.purchaser(buyer)
												.stashedForPurchaser()
												.build();

										GTSPlugin.getInstance().getStorage().publishListing(delegate);
										Sponge.getServer().getPlayer(buyer).ifPresent(player -> player.sendMessages(
												parser.parse(lang.get(MsgConfigKeys.PURCHASE_PAY_FAIL_TO_GIVE),
														Lists.newArrayList(() -> listing)
												)
										));
									}

									// We do the following such that we will ensure we populate any potential source
									// the price may require
									listing.markPurchased();
									((GTSStorageImpl) GTSPlugin.getInstance().getStorage()).sendListingUpdate(listing)
											.exceptionally(e -> {
												GTSPlugin.getInstance().getPluginLogger().error("Fatal error detected while updating listing with price, see error below:");
												ExceptionWriter.write(e);
												return false;
											});

									Message message = notifier.forgeMessage(
											DiscordOption.fetch(DiscordOption.Options.Purchase),
											MsgConfigKeys.DISCORD_PURCHASE_TEMPLATE,
											listing, buyer
									);
									notifier.sendMessage(message);
								} else {
									Sponge.getServer().getPlayer(buyer).ifPresent(player -> {
										player.sendMessage(parser.parse(
												Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
												Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
										));

										player.sendMessage(parser.parse(
												Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)
										));

										listing.getPrice().reward(buyer);
									});
								}

								return response.wasSuccessful();
							})
							.exceptionally(throwable -> {
								final ErrorCode error = throwable instanceof TimeoutException ? ErrorCodes.REQUEST_TIMED_OUT : ErrorCodes.FATAL_ERROR;
								Sponge.getServer().getPlayer(buyer).ifPresent(player -> {
									player.sendMessage(parser.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.REQUEST_FAILED),
											Lists.newArrayList(() -> error)
									));

									player.sendMessage(parser.parse(
											Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_FUNDS_FROM_ESCROW)
									));

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
		if(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS_PER_USER) <= 0) {
			return CompletableFuture.supplyAsync(() -> false);
		}
		return GTSPlugin.getInstance().getStorage().hasMaxListings(lister);
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
}
