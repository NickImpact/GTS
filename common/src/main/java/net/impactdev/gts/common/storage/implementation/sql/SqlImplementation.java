/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.storage.sql.ConnectionFactory;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.io.BufferedReader;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SqlImplementation implements StorageImplementation {

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

	private static final String APPLY_PLAYER_SETTINGS = "INSERT INTO `{prefix}player_settings` (uuid, pub_notif, sell_notif, bid_notif, outbid_notif) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE pub_notif=VALUES(pub_notif), sell_notif=VALUES(sell_notif), bid_notif=VALUES(bid_notif), outbid_notif=VALUES(outbid_notif)";
	private static final String GET_PLAYER_SETTINGS = "SELECT pub_notif, sell_notif, bid_notif, outbid_notif FROM `{prefix}player_settings` WHERE uuid=?";

	private final GTSPlugin plugin;

	private final ConnectionFactory connectionFactory;
	private final Function<String, String> processor;

	public SqlImplementation(GTSPlugin plugin, ConnectionFactory connectionFactory, String tablePrefix) {
		this.plugin = plugin;
		this.connectionFactory = connectionFactory;
		this.processor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix).replace("{database}", GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS).getDatabase()));
	}

	@Override
	public GTSPlugin getPlugin() {
		return this.plugin;
	}

	@Override
	public String getName() {
		return this.connectionFactory.getImplementationName();
	}

	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	public Function<String, String> getStatementProcessor() {
		return this.processor;
	}

	@Override
	public void init() throws Exception {
		this.connectionFactory.init();

		String schemaFileName = "assets/gts/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
		try (InputStream is = this.plugin.getResourceStream(schemaFileName)) {
			if (is == null) {
				throw new Exception("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				try (Connection connection = this.connectionFactory.getConnection()) {
					try (Statement s = connection.createStatement()) {
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith("--") || line.startsWith("#")) continue;

							sb.append(line);

							// check for end of declaration
							if (line.endsWith(";")) {
								sb.deleteCharAt(sb.length() - 1);

								String result = this.processor.apply(sb.toString().trim());

								if (!result.isEmpty()) {
									if(result.startsWith("set mode")) {
										s.addBatch(result);
									} else {
										if(SchemaReaders.any(this, result)) {
											SchemaReaders.first(this, result, s);
										}
									}
								}

								// reset
								sb = new StringBuilder();
							}
						}
						s.executeBatch();
					}
				}
			}
		}
	}

	@Override
	public void shutdown() throws Exception {
		this.connectionFactory.shutdown();
	}

	@Override
	public Map<String, String> getMeta() {
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
	public boolean addListing(Listing listing) throws Exception {
		return this.query(ADD_LISTING, (connection, ps) -> {
			ps.setString(1, listing.getID().toString());
			ps.setString(2, listing.getLister().toString());
			ps.setString(3, this.plugin.getGson().toJson(listing.serialize().toJson()));
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public boolean deleteListing(UUID uuid) throws Exception {
		return this.query(DELETE_LISTING, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	public Optional<Listing> getListing(UUID id) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, id.toString());
			return Optional.ofNullable(this.results(ps, results -> {
				if(results.next()) {
					JsonObject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonObject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(type.equals("bin")) {
						return GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(BuyItNow.class)
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
	public List<Listing> getListings() throws Exception {
		this.translateLegacy();
		return this.query(SELECT_ALL_LISTINGS, (connection, ps) -> this.results(ps, results -> {
			List<Listing> entries = Lists.newArrayList();

			int failed = 0;
			while(results.next()) {
				try {
					JsonObject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonObject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(type.equals("bin")) {
						BuyItNow bin = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(BuyItNow.class)
								.get()
								.getDeserializer()
								.deserialize(json);
						entries.add(bin);
					} else {
						Auction auction = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(Auction.class)
								.get()
								.getDeserializer()
								.deserialize(json);
						entries.add(auction);
					}
				} catch (Exception e) {
					this.plugin.getPluginLogger().error("Unable to read listing with ID: " + results.getString("id"));
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
	public boolean hasMaxListings(UUID user) throws Exception {
		return this.query(GET_ALL_USER_LISTINGS, (connection, ps) -> {
			ps.setString(1, user.toString());
			return this.results(ps, results -> {
				AtomicInteger possesses = new AtomicInteger();

				while(results.next()) {
					try(PreparedStatement query = connection.prepareStatement(this.processor.apply(GET_AUCTION_CLAIM_STATUS))) {
						query.setString(1, results.getString("id"));

						this.results(query, r -> {
							if(r.next()) {
								if(!r.getBoolean("lister")) {
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
	public boolean purge() throws Exception {
		return false;
	}

	@Override
	public Stash getStash(UUID user) throws Exception {
		Stash.StashBuilder builder = Stash.builder();

		List<Listing> listings = this.getListings();
		for(Listing listing : listings) {
			if(listing.hasExpired() || (listing instanceof BuyItNow && ((BuyItNow) listing).isPurchased())) {
				if (listing instanceof Auction) {
					Auction auction = (Auction) listing;
					if(auction.getLister().equals(user) || auction.getHighBid().map(bid -> bid.getFirst().equals(user)).orElse(false)) {
						boolean state = auction.getLister().equals(user);
						SimilarPair<Boolean> claimed = this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
							ps.setString(1, auction.getID().toString());
							return this.results(ps, results -> {
								if(results.next()) {
									return new SimilarPair<>(results.getBoolean("lister"), results.getBoolean("winner"));
								} else {
									return new SimilarPair<>(false, false);
								}
							});
						});

						if(state && !claimed.getFirst()) {
							builder.append(auction, TriState.FALSE);
						} else if(!state && !claimed.getSecond()) {
							builder.append(auction, TriState.TRUE);
						}
					} else if(auction.getBids().containsKey(user)) {
						boolean result = this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
							ps.setString(1, auction.getID().toString());
							return this.results(ps, results -> {
								if(results.next()) {
									List<UUID> others = GTSPlugin.getInstance().getGson().fromJson(
											results.getString("others"),
											new TypeToken<List<UUID>>() {}.getType()
									);

									return Optional.ofNullable(others).orElse(Lists.newArrayList()).contains(user);
								}

								return false;
							});
						});

						if(!result) {
							builder.append(auction, TriState.UNDEFINED);
						}
					}
				} else {
					BuyItNow bin = (BuyItNow) listing;
					if(bin.getLister().equals(user)) {
						builder.append(bin, TriState.FALSE);
					}
				}
			}
		}

		return builder.build();
	}

	@Override
	public Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception {
		return this.query(GET_PLAYER_SETTINGS, ((connection, ps) -> {
			ps.setString(1, user.toString());
			return this.results(ps, results -> {
				if(results.next()) {
					PlayerSettings settings = PlayerSettings.builder()
							.set(NotificationSetting.Publish, results.getBoolean("pub_notif"))
							.set(NotificationSetting.Sold, results.getBoolean("sell_notif"))
							.set(NotificationSetting.Bid, results.getBoolean("bid_notif"))
							.set(NotificationSetting.Outbid, results.getBoolean("outbid_notif"))
							.build();

					return Optional.of(settings);
				}

				this.applyPlayerSettings(user, PlayerSettings.create());
				return Optional.empty();
			});
		}));
	}

	@Override
	public boolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception {
		return this.query(APPLY_PLAYER_SETTINGS, ((connection, ps) -> {
			ps.setString(1, user.toString());
			ps.setBoolean(2, updates.getPublishListenState());
			ps.setBoolean(3, updates.getSoldListenState());
			ps.setBoolean(4, updates.getBidListenState());
			ps.setBoolean(5, updates.getOutbidListenState());
			ps.executeUpdate();
			return true; // Ignore return value of executeUpdate() as this can possibly end up being 0
		}));
	}

	@Override
	public BuyItNowMessage.Purchase.Response processPurchase(BuyItNowMessage.Purchase.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, request.getListingID().toString());
			return this.results(ps, results -> {
				boolean successful = results.next();
				UUID seller = Listing.SERVER_ID;
				if(successful) {
					BuyItNow listing = null;

					JsonObject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonObject.class);
					if (!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if (type.equals("bin")) {
						listing = GTSService.getInstance().getGTSComponentManager()
								.getListingResourceManager(BuyItNow.class)
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

				return new BINPurchaseMessage.Response(
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
	public boolean sendListingUpdate(Listing listing) throws Exception {
		return this.query(UPDATE_LISTING, (connection, ps) -> {
			ps.setString(1, GTSPlugin.getInstance().getGson().toJson(listing.serialize().toJson()));
			ps.setString(2, listing.getID().toString());
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	public AuctionMessage.Bid.Response processBid(AuctionMessage.Bid.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, ((connection, ps) -> {
			ps.setString(1, request.getAuctionID().toString());
			return this.results(ps, results -> {
				AuctionMessage.Bid.Response response;

				boolean fatal = false;
				TriState successful = TriState.FALSE; // FALSE = Missing Listing ID
				TreeMultimap<UUID, Auction.Bid> bids = TreeMultimap.create(
						Comparator.naturalOrder(),
						Collections.reverseOrder(Comparator.comparing(Auction.Bid::getAmount))
				);

				if(results.next()) {
					JsonObject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonObject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(!type.equals("auction")) {
						throw new IllegalStateException("Trying to place bid on non-auction");
					}

					Auction auction = GTSService.getInstance().getGTSComponentManager()
							.getListingResourceManager(Auction.class)
							.map(ResourceManager::getDeserializer)
							.get()
							.deserialize(json);

					successful = auction.bid(request.getActor(), request.getAmountBid()) ? TriState.TRUE : TriState.UNDEFINED;
					bids = auction.getBids();
					if(!this.sendListingUpdate(auction)) {
						successful = TriState.FALSE;
					}
				}

				response = new AuctionBidMessage.Response(
						GTSPlugin.getInstance().getMessagingService().generatePingID(),
						request.getID(),
						request.getAuctionID(),
						request.getActor(),
						request.getAmountBid(),
						successful.asBoolean(),
						UUID.fromString(results.getString("lister")),
						bids,
						successful == TriState.UNDEFINED ? ErrorCodes.OUTBID : successful == TriState.FALSE ?
								(fatal ? ErrorCodes.FATAL_ERROR : ErrorCodes.LISTING_MISSING) : null
				);

				return response;
			});
		}));
	}

	@Override
	public ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception {
		Optional<Listing> listing = this.getListing(request.getListingID());

		UUID response = GTSPlugin.getInstance().getMessagingService().generatePingID();
		if(!listing.isPresent()) {
			return ClaimMessageImpl.ClaimResponseImpl.builder()
					.id(response)
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.receiver(request.getReceiver().orElse(null))
					.error(ErrorCodes.LISTING_MISSING)
					.build();
		} else {
			boolean isLister = request.getActor().equals(listing.get().getLister());

			if (listing.map(l -> l instanceof Auction).orElse(false)) {
				if(!listing.map(l -> (Auction) l).get().hasAnyBidsPlaced()) {
					ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder builder = ClaimMessageImpl.ClaimResponseImpl.builder()
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
						builder.successful();
					}

					return builder.build();
				}

				Auction auction = listing.map(l -> (Auction) l).get();
				boolean claimer = isLister || auction.getHighBid().get().getFirst().equals(request.getActor());

				return this.query(GET_AUCTION_CLAIM_STATUS, (connection, ps) -> {
					ps.setString(1, request.getListingID().toString());
					return this.results(ps, results -> {
						int result;
						boolean lister;
						boolean winner;
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
									update.setBoolean(1, true);
								} else {
									others.add(request.getActor());
									update.setString(1, GTSPlugin.getInstance().getGson().toJson(others));
								}
								update.setString(2, request.getListingID().toString());
								result = update.executeUpdate();
							}

							lister = results.getBoolean("lister") || key.equals(UPDATE_AUCTION_CLAIM_LISTER);
							winner = results.getBoolean("winner") || key.equals(UPDATE_AUCTION_CLAIM_WINNER);
							boolean all = lister && winner && auction.getBids().keySet().stream()
									.filter(bidder -> !auction.getHighBid().get().getFirst().equals(bidder))
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
								append.setBoolean(2, isLister && claimer);
								append.setBoolean(3, !isLister && claimer);
								append.setString(4, GTSPlugin.getInstance().getGson().toJson(others));
								result = append.executeUpdate();
							}

							lister = isLister;
							winner = !isLister;
						}

						ImmutableList<UUID> o = ImmutableList.copyOf(others);
						Map<UUID, Boolean> claimed = Maps.newHashMap();
						auction.getBids().keySet().stream()
								.filter(bidder -> !auction.getHighBid().get().getFirst().equals(bidder))
								.forEach(bidder -> claimed.put(bidder, o.contains(bidder)));

						ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder builder = ClaimMessageImpl.ClaimResponseImpl.builder()
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
							builder.successful();
						} else {
							builder.error(ErrorCodes.FATAL_ERROR);
						}

						return builder.build();
					});
				});
			} else {
				ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder builder = ClaimMessageImpl.ClaimResponseImpl.builder()
						.id(response)
						.request(request.getID())
						.listing(request.getListingID())
						.actor(request.getActor())
						.receiver(request.getReceiver().orElse(null));

				if(this.deleteListing(request.getListingID())) {
					builder.successful();
				}

				return builder.build();
			}
		}
	}

	@Override
	public boolean appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others) throws Exception {
		return this.query(ADD_AUCTION_CLAIM_STATUS, (connection, ps) -> {
			ps.setString(1, auction.toString());
			ps.setBoolean(2, lister);
			ps.setBoolean(3, winner);
			ps.setString(4, GTSPlugin.getInstance().getGson().toJson(others));
			return ps.executeUpdate() != 0;
		});
	}

	@Override
	public AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception {
		return this.query(GET_SPECIFIC_LISTING, (connection, ps) -> {
			ps.setString(1, request.getAuctionID().toString());
			AtomicReference<Auction> data = new AtomicReference<>();
			return this.results(ps, results -> {
				boolean result = false;
				List<UUID> bidders = Lists.newArrayList();
				ErrorCode error = null;

				if(results.next()) {
					JsonObject json = GTSPlugin.getInstance().getGson().fromJson(results.getString("listing"), JsonObject.class);
					if(!json.has("type")) {
						throw new JsonParseException("Invalid Listing: Missing type");
					}

					String type = json.get("type").getAsString();
					if(!type.equals("auction")) {
						throw new IllegalStateException("Trying to place bid on non-auction");
					}

					Auction auction = GTSService.getInstance().getGTSComponentManager()
							.getListingResourceManager(Auction.class)
							.map(ResourceManager::getDeserializer)
							.get()
							.deserialize(json);
					data.set(auction);

					if(this.plugin.getConfiguration().get(ConfigKeys.AUCTIONS_ALLOW_CANCEL_WITH_BIDS)) {
						auction.getBids().keySet().stream().distinct().forEach(bidders::add);

						result = this.deleteListing(auction.getID());
					} else {
						if(auction.getBids().size() > 0) {
							error = ErrorCodes.BIDS_PLACED;
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
						ImmutableList.copyOf(bidders),
						result,
						error
				);
			});
		});
	}

	@Override
	public BuyItNowMessage.Remove.Response processListingRemoveRequest(BuyItNowMessage.Remove.Request request) throws Exception {
		BiFunction<Boolean, ErrorCode, BINRemoveMessage.Response> processor = (success, error) -> {
			return new BINRemoveMessage.Response(
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
		if(((BuyItNow) result).isPurchased()) {
			return processor.apply(false, ErrorCodes.ALREADY_PURCHASED);
		}

		return processor.apply(this.deleteListing(request.getListingID()), ErrorCodes.FATAL_ERROR);
	}

	@Override
	public ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception {
		Optional<Listing> listing = this.getListing(request.getListingID());
		if(listing.isPresent()) {
			boolean successful = this.deleteListing(request.getListingID());
			return ForceDeleteMessage.Response.builder()
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.data(listing.get())
					.give(request.shouldGive())
					.successful(successful)
					.error(successful ? null : ErrorCodes.FATAL_ERROR)
					.build();
		} else {
			return ForceDeleteMessage.Response.builder()
					.request(request.getID())
					.listing(request.getListingID())
					.actor(request.getActor())
					.successful(false)
					.give(request.shouldGive())
					.error(ErrorCodes.LISTING_MISSING)
					.build();
		}
	}

	private boolean tableExists(String table) throws SQLException {
		try (Connection connection = this.connectionFactory.getConnection()) {
			try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
				while (rs.next()) {
					if (rs.getString(3).equalsIgnoreCase(table)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	@Deprecated
	private boolean ran = false;

	/**
	 * This will read the old data from our database, and apply the necessary changes required to
	 * update the data to our new system.
	 *
	 * @deprecated This is purely for legacy updating. This will be removed in 6.1
	 */
	@Deprecated
	private void translateLegacy() throws Exception {
		if(!this.ran && this.tableExists(this.processor.apply("{prefix}listings_v3"))) {
			GTSPlugin.getInstance().getPluginLogger().info("&6Attempting to translate legacy data...");
			AtomicInteger successful = new AtomicInteger();
			AtomicInteger parsed = new AtomicInteger();

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

								try {
									UUID lister = UUID.fromString(incoming.getString("owner"));
									LocalDateTime expiration = incoming.getTimestamp("expiration").toLocalDateTime();
									Price<?, ?, ?> price = GTSService.getInstance().getGTSComponentManager()
											.getPriceManager("currency")
											.orElseThrow(() -> new IllegalStateException("No deserializer for currency available"))
											.getDeserializer()
											.deserialize(new JObject().add("value", incoming.getDouble("price")).toJson());

									JsonObject json = GTSPlugin.getInstance().getGson().fromJson(incoming.getString("entry"), JsonObject.class);
									if(!json.has("element")) {
										continue;
									}

									Entry<?, ?> entry = GTSService.getInstance().getGTSComponentManager()
											.getLegacyEntryDeserializer(json.get("type").getAsString())
											.orElseThrow(() -> new IllegalStateException("No deserializer for legacy entry type: " + json.get("type").getAsString()))
											.deserialize(json);

									BuyItNow bin = BuyItNow.builder()
											.id(id)
											.lister(lister)
											.expiration(expiration)
											.price(price)
											.entry(entry)
											.build();

									ps.setString(1, id.toString());
									ps.setString(2, lister.toString());
									ps.setString(3, GTSPlugin.getInstance().getGson().toJson(bin.serialize().toJson()));

									ps.addBatch();
									successful.incrementAndGet();
								} catch (IllegalStateException e) {
									GTSPlugin.getInstance().getPluginLogger().error("Failed to read listing with ID: " + id.toString());
									GTSPlugin.getInstance().getPluginLogger().error("  * " + e.getMessage());
								} catch (Exception e) {
									GTSPlugin.getInstance().getPluginLogger().error("Unexpectedly failed to read listing with ID: " + id.toString());
									ExceptionWriter.write(e);
								} finally {
									parsed.incrementAndGet();
								}
							}

							ps.executeBatch();
							connection.commit();
						} finally {
							connection.setAutoCommit(true);
						}

						return null;
					})
			);

			if(successful.get() == parsed.get()) {
				try (Connection connection = this.connectionFactory.getConnection()) {
					Statement statement = connection.createStatement();
					statement.executeUpdate(this.processor.apply("DROP TABLE {prefix}listings_v3"));
				}
				GTSPlugin.getInstance().getPluginLogger().info("Successfully converted " + successful.get() + " instances of legacy data!");
			} else {
				GTSPlugin.getInstance().getPluginLogger().warn("Some data failed to be converted, as such, we preserved the remaining data...");
				GTSPlugin.getInstance().getPluginLogger().warn("Check the logs above for further information!");
			}

		}
	}

	private enum SchemaReaders {
		CREATE_TABLE((impl, in) -> in.startsWith("CREATE TABLE"), (impl, in) -> !impl.tableExists(getTable(in))),
		ALTER_TABLE((impl, in) -> in.startsWith("ALTER TABLE"), (impl, in) -> impl.tableExists(getTable(in))),
		ANY((impl, input) -> true, (impl, input) -> true);

		private final SchemaPredicate initial;
		private final SchemaPredicate last;

		SchemaReaders(SchemaPredicate initial, SchemaPredicate last) {
			this.initial = initial;
			this.last = last;
		}

		public static boolean any(SqlImplementation impl, String in) {
			return Arrays.stream(values()).map(sr -> {
				try {
					return sr.initial.test(impl, in);
				} catch (Exception e) {
					ExceptionWriter.write(e);
					return false;
				}
			}).filter(x -> x).findAny().orElse(false);
		}

		public static void first(SqlImplementation impl, String in, Statement statement) throws Exception {
			for(SchemaReaders reader : SchemaReaders.values()) {
				if(reader != ANY) {
					if (reader.initial.test(impl, in) && reader.last.test(impl, in)) {
						statement.addBatch(in);
						return;
					}
				} else {
					for(SchemaReaders r : Arrays.stream(SchemaReaders.values()).filter(sr -> sr != ANY).collect(Collectors.toList())) {
						if(r.initial.test(impl, in)) {
							return;
						}
					}

					statement.addBatch(in);
				}
			}
		}

		private static String getTable(String in) {
			int start = in.indexOf('`');
			return in.substring(start + 1, in.indexOf('`', start + 1));
		}

	}

	private interface SchemaPredicate {

		boolean test(SqlImplementation impl, String input) throws Exception;

	}

}
