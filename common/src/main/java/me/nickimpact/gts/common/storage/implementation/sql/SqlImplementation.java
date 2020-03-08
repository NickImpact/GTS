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

package me.nickimpact.gts.common.storage.implementation.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import com.nickimpact.impactor.api.storage.sql.ConnectionFactory;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.SoldListing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.common.config.ConfigKeys;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.common.storage.implementation.StorageImplementation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class SqlImplementation implements StorageImplementation {

	private static final String SELECT_ALL_LISTINGS = "SELECT * FROM {prefix}listings_v3";
	private static final String ADD_LISTING = "INSERT INTO {prefix}listings_v3 (id, owner, entry, price, expiration) VALUES (?, ?, ?, ?, ?)";
	private static final String REMOVE_LISTING = "DELETE FROM {prefix}listings_v3 WHERE id=?";
	private static final String ADD_IGNORER = "INSERT INTO `{prefix}ignorers` VALUES (?)";
	private static final String REMOVE_IGNORER = "DELETE FROM `{prefix}ignorers` WHERE UUID=?";
	private static final String GET_IGNORERS = "SELECT * FROM `{prefix}ignorers`";

	private static final String ADD_SOLD_LISTING = "INSERT INTO `{prefix}sold` VALUES (?, ?, ?, ?)";
	private static final String GET_SOLD_LISTINGS = "SELECT name, price FROM `{prefix}sold` WHERE owner = ?";
	private static final String REMOVE_SOLD_LISTING = "DELETE FROM `{prefix}sold` WHERE id = ? AND owner = ?";

	@Deprecated
	private static final String FETCH_OLD = "SELECT * FROM {prefix}listings_v2";

	private final GTSPlugin plugin;

	private final ConnectionFactory connectionFactory;
	private final Function<String, String> processor;

	public SqlImplementation(GTSPlugin plugin, ConnectionFactory connectionFactory, String tablePrefix) {
		this.plugin = plugin;
		this.connectionFactory = connectionFactory;
		this.processor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
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

		String schemaFileName = "me/nickimpact/gts/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
		try (InputStream is = plugin.getResourceStream(schemaFileName)) {
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
									int start = result.indexOf('`');
									if(!tableExists(result.substring(start + 1, result.indexOf('`', start + 1)))) {
										s.addBatch(result);
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

			Clob clob = connection.createClob();
			clob.setString(1, this.plugin.getGson().toJson(listing.getEntry(), Entry.class));

			ps.setClob(3, clob);
			ps.setDouble(4, listing.getPrice().getPrice());
			ps.setTimestamp(5, Timestamp.valueOf(listing.getExpiration()));
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public boolean deleteListing(UUID uuid) throws Exception {
		return this.query(REMOVE_LISTING, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			ps.executeUpdate();
			return true;
		});
	}

	@Override
	public List<Listing> getListings() throws Exception {
		List<Listing> entries = Lists.newArrayList();

		return this.query(SELECT_ALL_LISTINGS, (connection, ps) -> this.results(ps, results -> {
			int failed = 0;
			while(results.next()) {
				try {
					UUID id = UUID.fromString(results.getString("id"));
					UUID owner = UUID.fromString(results.getString("owner"));
					String entry = results.getString("entry");
					double price = results.getDouble("price");
					LocalDateTime date = results.getTimestamp("expiration").toLocalDateTime();

					Listing listing = Listing.builder(this.plugin)
							.id(id)
							.owner(owner)
							.entry(this.plugin.getGson().fromJson(entry, Entry.class))
							.price(Math.min(this.plugin.getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), price))
							.expiration(date)
							.build();
					entries.add(listing);
				} catch (JsonParseException e) {
					++failed;
				}
			}

			if(failed != 0) {
				plugin.getPluginLogger().error("Failed to read in &c" + failed + " &7listings...");
			}

			return entries;
		}));
	}

	@Override
	public boolean addIgnorer(UUID uuid) throws Exception {
		return this.query(ADD_IGNORER, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public boolean removeIgnorer(UUID uuid) throws Exception {
		return this.query(REMOVE_IGNORER, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public List<UUID> getAllIgnorers() throws Exception {
		return this.query(GET_IGNORERS, (connection, ps) -> this.results(ps, results -> {
			List<UUID> ignorers = Lists.newArrayList();
			while(results.next()) {
				ignorers.add(UUID.fromString(results.getString("uuid")));
			}
			return ignorers;
		}));
	}

	@Override
	public boolean addToSoldListings(UUID owner, SoldListing listing) throws Exception {
		return this.query(ADD_SOLD_LISTING, (connection, ps) -> {
			ps.setString(1, listing.getId().toString());
			ps.setString(2, owner.toString());
			ps.setString(3, listing.getNameOfEntry());
			ps.setDouble(4, listing.getMoneyReceived());
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public List<SoldListing> getAllSoldListingsForPlayer(UUID uuid) throws Exception {
		return this.query(GET_SOLD_LISTINGS, (connection, ps) -> {
			ps.setString(1, uuid.toString());
			return this.results(ps, rs -> {
				List<SoldListing> sold = Lists.newArrayList();
				while(rs.next()) {
					sold.add(SoldListing.builder().id(UUID.fromString(rs.getString("id"))).name(rs.getString("name")).money(rs.getDouble("price")).build());
				}

				return sold;
			});
		});
	}

	@Override
	public boolean deleteSoldListing(UUID id, UUID owner) throws Exception {
		return this.query(REMOVE_SOLD_LISTING, (connection, ps) -> {
			ps.setString(1, id.toString());
			ps.setString(2, owner.toString());
			ps.executeUpdate();

			return true;
		});
	}

	@Override
	public boolean purge() throws Exception {
		return false;
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

}
