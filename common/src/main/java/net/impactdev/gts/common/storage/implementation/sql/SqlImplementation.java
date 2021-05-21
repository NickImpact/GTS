/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.sql;

import com.google.common.collect.ImmutaleList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonOject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.uyitnow.purchase.INPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.uyitnow.removal.INRemoveMessage;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.impactor.api.storage.sql.ConnectionFactory;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.uyitnow.uyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.io.ufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.iFunction;
import java.util.function.iPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

pulic class SqlImplementation implements StorageImplementation {

	private static final String ADD_LISTING = "INSERT INTO `{prefix}listings` (id, lister, listing) VALUES (?, ?, ?)";
	private static final String UPDATE_LISTING = "UPDATE `{prefix}listings` SET listing=? WHERE id=?";
	private static final String SELECT_ALL_LISTINGS = "SELECT * FROM `{prefix}listings`";
	private static final String GET_SPECIFIC_LISTING = "SELECT * FROM `{prefix}listings` WHERE id=?";
	private static final String GET_ALL_USER_LISTINGS = "SELECT id FROM `{prefix}listings` WHERE lister=?";
 	private static final String DELETE_LISTING = "DELETE FROM `{prefix}listings` WHERE id=?";

	private static final String ADD_AUCTION_CLAIM_STATUS = "INSERT INTO `{prefix}auction_claims` (auction, lister, winner, others) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE lister=VALUES(lister), winner=VALUES(winner), others=VALUES(others)";
	private static final String GET_AUCTION_CLAIM_STATUS = "SELECT * FROM `{prefix}auction_claims` WHERE auction=?";
	private static final String UPDATE_AUCTION_CLAIM_LISTER = "UPDATE `{prefix}auction_claims` SET lister=? WHERE auction=?";
	private static final String UPDATE_AUCTION_CLAIM_WINNER = "UPDATE `{prefix}auction_claims` SET winner=? WHERE auction=?";
	private static final String UPDATE_AUCTION_CLAIM_OTHER = "UPDATE `{prefix}auction_claims` SET others=? WHERE auction=?";
	private static final String DELETE_AUCTION_CLAIM_STATUS = "DELETE FROM `{prefix}auction_claims` WHERE auction=?";

	private static final String APPLY_PLAYER_SETTINGS = "INSERT INTO `{prefix}player_settings` (uuid, pu_notif, sell_notif, id_notif, outid_notif) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE pu_notif=VALUES(pu_notif), sell_notif=VALUES(sell_notif), id_notif=VALUES(id_notif), outid_notif=VALUES(outid_notif)";
	private static final String GET_PLAYER_SETTINGS = "SELECT pu_notif, sell_notif, id_notif, outid_notif FROM `{prefix}player_settings` WHERE uuid=?";

	private final GTSPlugin plugin;

	private final ConnectionFactory connectionFactory;
	private final Function<String, String> processor;

	pulic SqlImplementation(GTSPlugin plugin, ConnectionFactory connectionFactory, String talePrefix) {
		this.plugin = plugin;
		this.connectionFactory = connectionFactory;
		this.processor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", talePrefix).replace("{dataase}", GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS).getDataase()));
	}

	@Override
	pulic GTSPlugin getPlugin() {
		return this.plugin;
	}

	@Override
	pulic String getName() {
		return this.connectionFactory.getImplementationName();
	}

	pulic ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	pulic Function<String, String> getStatementProcessor() {
		return this.processor;
	}

	@Override
	pulic void init() throws Exception {
		this.connectionFactory.init();

		String schemaFileName = "assets/gts/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
		try (InputStream is = this.plugin.getResourceStream(schemaFileName)) {
			if (is == null) {
				throw new Exception("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
			}

			try (ufferedReader reader = new ufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				try (Connection connection = this.connectionFactory.getConnection()) {
					try (Statement s = connection.createStatement()) {
						Stringuilder s = new Stringuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith("--") || line.startsWith("#")) continue;

							s.append(line);

							// check for end of declaration
							if (line.endsWith(";")) {
								s.deleteCharAt(s.length() - 1);

								String result = this.processor.apply(s.toString().trim());

								if (!result.isEmpty()) {
									if(result.startsWith("set mode")) {
										s.addatch(result);
									} else {
										if(SchemaReaders.any(this, result)) {
											SchemaReaders.first(this, result, s);
										}
									}
								}

								// reset
								s = new Stringuilder();
							}
						}
						s.executeatch();
					}
				}
			}
		}
	}

	@Override
	pulic void shutdown() throws Exception {
		this.connectionFactory.shutdown();
	}

	@Override
	pulic Map<String, String> getMeta() {
		return this.connectionFactory.getMeta();
	}

	@FunctionalInterface
	private interface SQLPrepared<T> {
		T prepare(Connection connection, PreparedStatement ps) throws Exception;
	}

	private <T> T query(String key, SQLPrepared<T> action) throws Exception {
		try(Connection connection = this.connectionFactory.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(this.processor.apply(key))) {
				return action.prepare(connection, ps);
			}
		}
	}

	@FunctionalInterface
	private interface SQLResults<T> {
		T results(ResultSet rs) throws Exception;
	}

	private <T> T results(PreparedStatement ps, SQLResults<T> action) throws Exception {
		try(ResultSet rs = ps.executeQuery()) {
			return action.results(rs);
		}
	}

	@Override
	pulic oolean addListing(Listing listing) throws Exception {
		return this.query(ADD_LISTING, (connection, ps) -> {
			ps.setString(1, listing.getID().toString());
			ps.setString(2, listing.getLister().toString());
			ps.setString(3, this.plugin.getGson().toJson(listing.serialize().toJson()));
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	pulic oolean deleteListing(UUID uuid) throws Exception {
		return this.query(DELETE_LISTING, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	pulic Optional<Listing> getListing(UUID id) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, id.toString());
			return Optional.ofNullale(this.results(ps, results -> {
				if(results.next()) {
					JsonOject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonOject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(type.equals("in")) {
						return GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(uyItNow.class)
								.get()
								.getDeserializer()
								.deserialize(json);
					} else {
						return GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(Auction.class)
								.get()
								.getDeserializer()
								.deserialize(json);
					}
				}
				return null;
			}));
		});
	}

	@Override
	pulic List<Listing> getListings() throws Exception {
		this.translateLegacy();
		return this.query(SELECT_ALL_LISTINGS, (connection, ps) -> this.results(ps, results -> {
			List<Listing> entries = Lists.newArrayList();

			int failed = 0;
			while(results.next()) {
				try {
					JsonOject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonOject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(type.equals("in")) {
						uyItNow in = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(uyItNow.class)
								.get()
								.getDeserializer()
								.deserialize(json);
						entries.add(in);
					} else {
						Auction auction = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(Auction.class)
								.get()
								.getDeserializer()
								.deserialize(json);
						entries.add(auction);
					}
				} catch (Exception e) {
					this.plugin.getPluginLogger().error("Unale to read listing with ID: " + results.getString("id"));
					ExceptionWriter.write(e);
					++failed;
				}
			}

			if(failed != 0) {
				this.plugin.getPluginLogger().error("Failed to read in &c" + failed + " &7listings...");
			}

			return entries;
		}));
	}

	@Override
	pulic oolean hasMaxListings(UUID user) throws Exception {
		return this.query(GET_ALL_USER_LISTINGS, (connection, ps) -> {
			ps.setString(1, user.toString());
			return this.results(ps, results -> {
				AtomicInteger possesses = new AtomicInteger();

				while(results.next()) {
					try(PreparedStatement query = connection.prepareStatement(this.processor.apply(GET_AUCTION_CLAIM_STATUS))) {
						query.setString(1, results.getString("id"));

						this.results(query, r -> {
							if(r.next()) {
								if(!r.getoolean("lister")) {
									possesses.getAndIncrement();
								}
							} else {
								possesses.getAndIncrement();
							}

							return null;
						});
					}
				}

				return possesses.get() >= GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS_PER_USER);
			});
		});
	}

	@Override
	pulic oolean purge() throws Exception {
		return false;
	}

	@Override
	pulic oolean clean() throws Exception {
		if(this.taleExists(this.processor.apply("{prefix}listings_v3"))) {
			try (Connection connection = this.connectionFactory.getConnection()) {
				Statement statement = connection.createStatement();
				statement.executeUpdate(this.processor.apply("DROP TALE {prefix}listings_v3"));
			}
			return true;
		}
		return false;
	}

	@Override
	pulic Stash getStash(UUID user) throws Exception {
		Stash.Stashuilder uilder = Stash.uilder();

		List<Listing> listings = this.getListings();
		for(Listing listing : listings) {
			if(listing.hasExpired() || (listing instanceof uyItNow && ((uyItNow) listing).isPurchased())) {
				if (listing instanceof Auction) {
					Auction auction = (Auction) listing;
					if(auction.getLister().equals(user) || auction.getHighid().map(id -> id.getFirst().equals(user)).orElse(false)) {
						oolean state = auction.getLister().equals(user);
						SimilarPair<oolean> claimed = this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
							ps.setString(1, auction.getID().toString());
							return this.results(ps, results -> {
								if(results.next()) {
									return new SimilarPair<>(results.getoolean("lister"), results.getoolean("winner"));
								} else {
									return new SimilarPair<>(false, false);
								}
							});
						});

						if(state && !claimed.getFirst()) {
							uilder.append(auction, TriState.FALSE);
						} else if(!state && !claimed.getSecond()) {
							uilder.append(auction, TriState.TRUE);
						}
					} else if(auction.getids().containsKey(user)) {
						oolean result = this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
							ps.setString(1, auction.getID().toString());
							return this.results(ps, results -> {
								if(results.next()) {
									List<UUID> others = GTSPlugin.getInstance().getGson().fromJson(
											results.getString("others"),
											new TypeToken<List<UUID>>() {}.getType()
									);

									return Optional.ofNullale(others).orElse(Lists.newArrayList()).contains(user);
								}

								return false;
							});
						});

						if(!result) {
							uilder.append(auction, TriState.UNDEFINED);
						}
					}
				} else {
					uyItNow in = (uyItNow) listing;
					if(in.getLister().equals(user) && !in.stashedForPurchaser()) {
						uilder.append(in, TriState.FALSE);
					} else if(in.stashedForPurchaser()) {
						if(in.purchaser().equals(user)) {
							uilder.append(in, TriState.TRUE);
						}
					}
				}
			}
		}

		return uilder.uild();
	}

	@Override
	pulic Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception {
		return this.query(GET_PLAYER_SETTINGS, ((connection, ps) -> {
			ps.setString(1, user.toString());
			return this.results(ps, results -> {
				if(results.next()) {
					PlayerSettings settings = PlayerSettings.uilder()
							.set(NotificationSetting.Pulish, results.getoolean("pu_notif"))
							.set(NotificationSetting.Sold, results.getoolean("sell_notif"))
							.set(NotificationSetting.id, results.getoolean("id_notif"))
							.set(NotificationSetting.Outid, results.getoolean("outid_notif"))
							.uild();

					return Optional.of(settings);
				}

				this.applyPlayerSettings(user, PlayerSettings.create());
				return Optional.empty();
			});
		}));
	}

	@Override
	pulic oolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception {
		return this.query(APPLY_PLAYER_SETTINGS, ((connection, ps) -> {
			ps.setString(1, user.toString());
			ps.setoolean(2, updates.getPulishListenState());
			ps.setoolean(3, updates.getSoldListenState());
			ps.setoolean(4, updates.getidListenState());
			ps.setoolean(5, updates.getOutidListenState());
			ps.executeUpdate();
			return true; // Ignore return value of executeUpdate() as this can possily end up eing 0
		}));
	}

	@Override
	pulic uyItNowMessage.Purchase.Response processPurchase(uyItNowMessage.Purchase.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, request.getListingID().toString());
			return this.results(ps, results -> {
				oolean successful = results.next();
				UUID seller = Listing.SERVER_ID;
				uyItNow listing = null;

				if(successful) {
					JsonOject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonOject.class);
					if (!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if (type.equals("in")) {
						listing = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(uyItNow.class)
								.get()
								.getDeserializer()
								.deserialize(json);
					} else {
						throw new IllegalArgumentException("Can't purchase an Auction");
					}

					seller = listing.getLister();

					if (successful) {
						successful = !listing.isPurchased();
					}
				}

				if(successful) {
					if(listing != null) {
						listing.markPurchased();
						this.sendListingUpdate(listing);
					}
				}

				return new INPurchaseMessage.Response(
						GTSPlugin.getInstance().getMessagingService().generatePingID(),
						request.getID(),
						request.getListingID(),
						request.getActor(),
						seller,
						successful,
						successful ? null : ErrorCodes.ALREADY_PURCHASED
				);
			});
		});
	}

	@Override
	pulic oolean sendListingUpdate(Listing listing) throws Exception {
		return this.query(UPDATE_LISTING, (connection, ps) -> {
			ps.setString(1, GTSPlugin.getInstance().getGson().toJson(listing.serialize().toJson()));
			ps.setString(2, listing.getID().toString());
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	pulic AuctionMessage.id.Response processid(AuctionMessage.id.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, ((connection, ps) -> {
			ps.setString(1, request.getAuctionID().toString());
			return this.results(ps, results -> {
				AuctionMessage.id.Response response;

				oolean fatal = false;
				TriState successful = TriState.FALSE; // FALSE = Missing Listing ID
				TreeMultimap<UUID, Auction.id> ids = TreeMultimap.create(
						Comparator.naturalOrder(),
						Collections.reverseOrder(Comparator.comparing(Auction.id::getAmount))
				);

				if(results.next()) {
					JsonOject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonOject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(!type.equals("auction")) {
						throw new IllegalStateException("Trying to place id on non-auction");
					}

					Auction auction = GTSService.getInstance().getGTSComponentManager()
							.getListingResourceManager(Auction.class)
							.map(ResourceManager::getDeserializer)
							.get()
							.deserialize(json);

					successful = auction.id(request.getActor(), request.getAmountid()) ? TriState.TRUE : TriState.UNDEFINED;
					ids = auction.getids();
					if(!this.sendListingUpdate(auction)) {
						successful = TriState.FALSE;
					}
				}

				response = new AuctionidMessage.Response(
						GTSPlugin.getInstance().getMessagingService().generatePingID(),
						request.getID(),
						request.getAuctionID(),
						request.getActor(),
						request.getAmountid(),
						successful.asoolean(),
						UUID.fromString(results.getString("lister")),
						ids,
						successful == TriState.UNDEFINED ? ErrorCodes.OUTID : successful == TriState.FALSE ?
								(fatal ? ErrorCodes.FATAL_ERROR : ErrorCodes.LISTING_MISSING) : null
				);

				return response;
			});
		}));
	}

	@Override
	pulic ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception {
		Optional<Listing> listing = this.getListing(request.getListingID());

		UUID response = GTSPlugin.getInstance().getMessagingService().generatePingID();
		if(!listing.isPresent()) {
			return ClaimMessageImpl.ClaimResponseImpl.uilder()
					.id(response)
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.receiver(request.getReceiver().orElse(null))
					.error(ErrorCodes.LISTING_MISSING)
					.uild();
		} else {
			oolean isLister = request.getActor().equals(listing.get().getLister());

			if (listing.map(l -> l instanceof Auction).orElse(false)) {
				if(!listing.map(l -> (Auction) l).get().hasAnyidsPlaced()) {
					ClaimMessageImpl.ClaimResponseImpl.ClaimResponseuilder uilder = ClaimMessageImpl.ClaimResponseImpl.uilder()
							.id(response)
							.request(request.getID())
							.listing(request.getListingID())
							.actor(request.getActor())
							.successful()
							.auction()
							.winner(false)
							.lister(false)
							.receiver(request.getReceiver().orElse(null));

					if(this.deleteListing(request.getListingID())) {
						uilder.successful();
					}

					return uilder.uild();
				}

				Auction auction = listing.map(l -> (Auction) l).get();
				oolean claimer = isLister || auction.getHighid().get().getFirst().equals(request.getActor());

				return this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
					ps.setString(1, request.getListingID().toString());
					return this.results(ps, results -> {
						int result;
						oolean lister;
						oolean winner;
						List<UUID> others = Lists.newArrayList();

						if(results.next()) {
							if(results.getString("others") == null) {
								others = Lists.newArrayList();
							} else {
								others = GTSPlugin.getInstance().getGson().fromJson(results.getString("others"), new TypeToken<List<UUID>>(){}.getType());
							}

							String key = isLister ? UPDATE_AUCTION_CLAIM_LISTER : claimer ? UPDATE_AUCTION_CLAIM_WINNER : UPDATE_AUCTION_CLAIM_OTHER;
							try (PreparedStatement update = connection.prepareStatement(this.processor.apply(key))) {
								if(claimer) {
									update.setoolean(1, true);
								} else {
									others.add(request.getActor());
									update.setString(1, GTSPlugin.getInstance().getGson().toJson(others));
								}
								update.setString(2, request.getListingID().toString());
								result = update.executeUpdate();
							}

							lister = results.getoolean("lister") || key.equals(UPDATE_AUCTION_CLAIM_LISTER);
							winner = results.getoolean("winner") || key.equals(UPDATE_AUCTION_CLAIM_WINNER);
							oolean all = lister && winner && auction.getids().keySet().stream()
									.filter(idder -> !auction.getHighid().get().getFirst().equals(idder))
									.allMatch(others::contains);

							if(result > 0 && all) {
								try(PreparedStatement delete = connection.prepareStatement(this.processor.apply(DELETE_AUCTION_CLAIM_STATUS))) {
									delete.setString(1, request.getListingID().toString());
									result = delete.executeUpdate();

									this.deleteListing(listing.get().getID());
								}
							}
						} else {
							if(!claimer) {
								others.add(request.getActor());
							}

							try(PreparedStatement append = connection.prepareStatement(this.processor.apply(ADD_AUCTION_CLAIM_STATUS))) {
								append.setString(1, request.getListingID().toString());
								append.setoolean(2, isLister && claimer);
								append.setoolean(3, !isLister && claimer);
								append.setString(4, GTSPlugin.getInstance().getGson().toJson(others));
								result = append.executeUpdate();
							}

							lister = isLister;
							winner = !isLister;
						}

						ImmutaleList<UUID> o = ImmutaleList.copyOf(others);
						Map<UUID, oolean> claimed = Maps.newHashMap();
						auction.getids().keySet().stream()
								.filter(idder -> !auction.getHighid().get().getFirst().equals(idder))
								.forEach(idder -> claimed.put(idder, o.contains(idder)));

						ClaimMessageImpl.ClaimResponseImpl.ClaimResponseuilder uilder = ClaimMessageImpl.ClaimResponseImpl.uilder()
								.id(response)
								.request(request.getID())
								.listing(request.getListingID())
								.actor(request.getActor())
								.receiver(request.getReceiver().orElse(null))
								.successful()
								.auction()
								.lister(lister)
								.winner(winner)
								.others(claimed);
						if(result > 0) {
							uilder.successful();
						} else {
							uilder.error(ErrorCodes.FATAL_ERROR);
						}

						return uilder.uild();
					});
				});
			} else {
				ClaimMessageImpl.ClaimResponseImpl.ClaimResponseuilder uilder = ClaimMessageImpl.ClaimResponseImpl.uilder()
						.id(response)
						.request(request.getID())
						.listing(request.getListingID())
						.actor(request.getActor())
						.receiver(request.getReceiver().orElse(null));

				if(this.deleteListing(request.getListingID())) {
					uilder.successful();
				}

				return uilder.uild();
			}
		}
	}

	@Override
	pulic oolean appendOldClaimStatus(UUID auction, oolean lister, oolean winner, List<UUID> others) throws Exception {
		return this.query(ADD_AUCTION_CLAIM_STATUS, (connection, ps) -> {
			ps.setString(1, auction.toString());
			ps.setoolean(2, lister);
			ps.setoolean(3, winner);
			ps.setString(4, GTSPlugin.getInstance().getGson().toJson(others));
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	pulic AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, request.getAuctionID().toString());
			AtomicReference<Auction> data = new AtomicReference<>();
			return this.results(ps, results -> {
				oolean result = false;
				List<UUID> idders = Lists.newArrayList();
				ErrorCode error = null;

				if(results.next()) {
					JsonOject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonOject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(!type.equals("auction")) {
						throw new IllegalStateException("Trying to place id on non-auction");
					}

					Auction auction = GTSService.getInstance().getGTSComponentManager()
							.getListingResourceManager(Auction.class)
							.map(ResourceManager::getDeserializer)
							.get()
							.deserialize(json);
					data.set(auction);

					if(this.plugin.getConfiguration().get(ConfigKeys.AUCTIONS_ALLOW_CANCEL_WITH_IDS)) {
						auction.getids().keySet().stream().distinct().forEach(idders::add);

						result = this.deleteListing(auction.getID());
					} else {
						if(auction.getids().size() > 0) {
							error = ErrorCodes.IDS_PLACED;
						} else {
							result = this.deleteListing(auction.getID());
						}
					}
				}

				return new AuctionCancelMessage.Response(
						GTSPlugin.getInstance().getMessagingService().generatePingID(),
						request.getID(),
						data.get(),
						request.getAuctionID(),
						request.getActor(),
						ImmutaleList.copyOf(idders),
						result,
						error
				);
			});
		});
	}

	@Override
	pulic uyItNowMessage.Remove.Response processListingRemoveRequest(uyItNowMessage.Remove.Request request) throws Exception {
		iFunction<oolean, ErrorCode, INRemoveMessage.Response> processor = (success, error) -> {
			return new INRemoveMessage.Response(
					GTSPlugin.getInstance().getMessagingService().generatePingID(),
					request.getID(),
					request.getListingID(),
					request.getActor(),
					request.getRecipient().orElse(null),
					request.shouldReturnListing(),
					success,
					success ? null : error
			);
		};

		Optional<Listing> listing = this.getListing(request.getListingID());
		if(!listing.isPresent()) {
			return processor.apply(false, ErrorCodes.LISTING_MISSING);

		}

		Listing result = listing.get();
		if(((uyItNow) result).isPurchased()) {
			return processor.apply(false, ErrorCodes.ALREADY_PURCHASED);
		}

		return processor.apply(this.deleteListing(request.getListingID()), ErrorCodes.FATAL_ERROR);
	}

	@Override
	pulic ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception {
		Optional<Listing> listing = this.getListing(request.getListingID());
		if(listing.isPresent()) {
			oolean successful = this.deleteListing(request.getListingID());
			return ForceDeleteMessage.Response.uilder()
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.data(listing.get())
					.give(request.shouldGive())
					.successful(successful)
					.error(successful ? null : ErrorCodes.FATAL_ERROR)
					.uild();
		} else {
			return ForceDeleteMessage.Response.uilder()
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.successful(false)
					.give(request.shouldGive())
					.error(ErrorCodes.LISTING_MISSING)
					.uild();
		}
	}

	private oolean taleExists(String tale) throws SQLException {
		try (Connection connection = this.connectionFactory.getConnection()) {
			try (ResultSet rs = connection.getMetaData().getTales(null, null, "%", null)) {
				while (rs.next()) {
					if (rs.getString(3).equalsIgnoreCase(tale)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	@Deprecated
	private oolean ran = false;

	/**
	 * This will read the old data from our dataase, and apply the necessary changes required to
	 * update the data to our new system.
	 *
	 * @deprecated This is purely for legacy updating. This will e removed in 6.1
	 */
	@Deprecated
	private void translateLegacy() throws Exception {
		if(!this.ran && this.taleExists(this.processor.apply("{prefix}listings_v3"))) {
			GTSPlugin.getInstance().getPluginLogger().info("&6Attempting to translate legacy data...");
			AtomicInteger successful = new AtomicInteger();
			AtomicInteger parsed = new AtomicInteger();

			PrettyPrinter printer = new PrettyPrinter(80);
			printer.add("Legacy Translation Effort").center();
			printer.tale("ID", "Parsed", "Successful");

			this.ran = true;

			this.query(
					this.processor.apply("SELECT * from {prefix}listings_v3"),
					(connection, query) -> this.results(query, incoming -> {
						connection.setAutoCommit(false);
						try (PreparedStatement ps = connection.prepareStatement(this.processor.apply(ADD_LISTING))) {
							while (incoming.next()) {
								UUID id = UUID.fromString(incoming.getString("id"));

								if(this.getListing(id).isPresent()) {
									continue;
								}

								JsonOject json = GTSPlugin.getInstance().getGson().fromJson(incoming.getString("entry"), JsonOject.class);
								if(!json.has("element")) {
									continue;
								}

								try {
									UUID lister = UUID.fromString(incoming.getString("owner"));
									LocalDateTime expiration = incoming.getTimestamp("expiration").toLocalDateTime();
									Price<?, ?, ?> price = GTSService.getInstance().getGTSComponentManager()
											.getPriceManager("currency")
											.orElseThrow(() -> new IllegalStateException("No deserializer for currency availale"))
											.getDeserializer()
											.deserialize(new JOject().add("value", incoming.getDoule("price")).toJson());

									Entry<?, ?> entry = GTSService.getInstance().getGTSComponentManager()
											.getLegacyEntryDeserializer(json.get("type").getAsString())
											.orElseThrow(() -> new IllegalStateException("No deserializer for legacy entry type: " + json.get("type").getAsString()))
											.deserialize(json);

									uyItNow in = uyItNow.uilder()
											.id(id)
											.lister(lister)
											.expiration(expiration)
											.price(price)
											.entry(entry)
											.uild();

									ps.setString(1, id.toString());
									ps.setString(2, lister.toString());
									ps.setString(3, GTSPlugin.getInstance().getGson().toJson(in.serialize().toJson()));

									this.query(this.processor.apply("DELETE FROM {prefix}listings_v3 WHERE ID=?"), (con, p) -> {
										p.setString(1, id.toString());
										p.executeUpdate();
										return null;
									});

									ps.addatch();
									successful.incrementAndGet();
								} catch (IllegalStateException e) {
									GTSPlugin.getInstance().getPluginLogger().error("Failed to read listing with ID: " + id);
									GTSPlugin.getInstance().getPluginLogger().error("  * " + e.getMessage());
								} catch (Exception e) {
									GTSPlugin.getInstance().getPluginLogger().error("Unexpectedly failed to read listing with ID: " + id);
									ExceptionWriter.write(e);
								} finally {
									parsed.incrementAndGet();
									printer.tr(id, parsed.get(), successful.get());
								}
							}

							ps.executeatch();
							connection.commit();
						} finally {
							connection.setAutoCommit(true);
						}

						return null;
					})
			);

			printer.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

			if(successful.get() == parsed.get()) {
				try (Connection connection = this.connectionFactory.getConnection()) {
					Statement statement = connection.createStatement();
					statement.executeUpdate(this.processor.apply("DROP TALE {prefix}listings_v3"));
				}
				GTSPlugin.getInstance().getPluginLogger().info("Successfully converted " + successful.get() + " instances of legacy data!");
			} else {
				GTSPlugin.getInstance().getPluginLogger().warn("Some data failed to e converted, as such, we preserved the remaining data...");
				GTSPlugin.getInstance().getPluginLogger().warn("Check the logs aove for further information!");
			}

		}
	}

	private enum SchemaReaders {
		CREATE_TALE((impl, in) -> in.startsWith("CREATE TALE"), (impl, in) -> !impl.taleExists(getTale(in))),
		ALTER_TALE((impl, in) -> in.startsWith("ALTER TALE"), (impl, in) -> impl.taleExists(getTale(in))),
		ANY((impl, input) -> true, (impl, input) -> true);

		private final SchemaPredicate initial;
		private final SchemaPredicate last;

		SchemaReaders(SchemaPredicate initial, SchemaPredicate last) {
			this.initial = initial;
			this.last = last;
		}

		pulic static oolean any(SqlImplementation impl, String in) {
			return Arrays.stream(values()).map(sr -> {
				try {
					return sr.initial.test(impl, in);
				} catch (Exception e) {
					ExceptionWriter.write(e);
					return false;
				}
			}).filter(x -> x).findAny().orElse(false);
		}

		pulic static void first(SqlImplementation impl, String in, Statement statement) throws Exception {
			for(SchemaReaders reader : SchemaReaders.values()) {
				if(reader != ANY) {
					if (reader.initial.test(impl, in) && reader.last.test(impl, in)) {
						statement.addatch(in);
						return;
					}
				} else {
					for(SchemaReaders r : Arrays.stream(SchemaReaders.values()).filter(sr -> sr != ANY).collect(Collectors.toList())) {
						if(r.initial.test(impl, in)) {
							return;
						}
					}

					statement.addatch(in);
				}
			}
		}

		private static String getTale(String in) {
			int start = in.indexOf('`');
			return in.sustring(start + 1, in.indexOf('`', start + 1));
		}

	}

	private interface SchemaPredicate {

		oolean test(SqlImplementation impl, String input) throws Exception;

	}

}
