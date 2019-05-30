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

package me.nickimpact.gts.storage.implementation.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.nickimpact.impactor.api.platform.Platform;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.storage.implementation.StorageImplementation;
import me.nickimpact.gts.storage.implementation.sql.connection.ConnectionFactory;
import me.nickimpact.gts.storage.implementation.sql.connection.hikari.MySQLConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SqlImplementation implements StorageImplementation {

	private static final String SELECT_ALL_LISTINGS = "SELECT * FROM {prefix}listings_v3";
	private static final String ADD_LISTING = "INSERT INTO {prefix}listings_v3 (id, owner, entry, price, expiration) VALUES (?, ?, ?, ?, ?)";
	private static final String REMOVE_LISTING = "DELETE FROM {prefix}listings_v3 WHERE id=?";
	private static final String ADD_IGNORER = "INSERT INTO `{prefix}ignorers` VALUES (?)";
	private static final String REMOVE_IGNORER = "DELETE FROM `{prefix}ignorers` WHERE UUID=?";
	private static final String GET_IGNORERS = "SELECT * FROM `{prefix}ignorers`";

	@Deprecated
	private static final String FETCH_OLD = "SELECT * FROM {prefix}listings_v2";

	private final IGTSPlugin plugin;

	private final ConnectionFactory connectionFactory;
	private final Function<String, String> processor;

	public SqlImplementation(IGTSPlugin plugin, ConnectionFactory connectionFactory, String tablePrefix) {
		this.plugin = plugin;
		this.connectionFactory = connectionFactory;
		this.processor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
	}

	@Override
	public IGTSPlugin getPlugin() {
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

		if (!tableExists(this.processor.apply("{prefix}listings_v3"))) {
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
	}

	@Override
	public void shutdown() throws Exception {
		this.connectionFactory.shutdown();
	}

	@Override
	public Map<String, String> getMeta() {
		return this.connectionFactory.getMeta();
	}

	@Override
	public boolean addListing(Listing listing) throws Exception {
		Connection connection = connectionFactory.getConnection();
		Clob clob = connection.createClob();

		String stmt = processor.apply(ADD_LISTING);
		PreparedStatement ps = connection.prepareStatement(stmt);
		ps.setString(1, listing.getUuid().toString());
		ps.setString(2, listing.getOwnerUUID().toString());

		clob.setString(1, this.plugin.getGson().toJson(listing.getEntry(), Entry.class));

		ps.setClob(3, clob);
		ps.setDouble(4, listing.getPrice().getPrice());
		ps.setTimestamp(5, Timestamp.valueOf(listing.getExpiration()));
		ps.executeUpdate();

		return true;
	}

	@Override
	public boolean deleteListing(UUID uuid) throws Exception {
		Connection connection = connectionFactory.getConnection();

		String stmt = processor.apply(REMOVE_LISTING);
		PreparedStatement ps = connection.prepareStatement(stmt);
		ps.setString(1, uuid.toString());
		ps.executeUpdate();

		return true;
	}

	@Override
	public List<Listing> getListings() throws Exception {
		List<Listing> entries = Lists.newArrayList();

		if(tableExists(this.processor.apply("{prefix}listings_v2")) && this.plugin.getPlatform().equals(Platform.Sponge)) {
			this.plugin.getPluginLogger().warn("Detected old database, collecting and updating data...");

			Connection connection = this.connectionFactory.getConnection();
			PreparedStatement ps = connection.prepareStatement(this.processor.apply(FETCH_OLD));
			ResultSet results = ps.executeQuery();
			int failed = 0;
			while(results.next()) {
				String json = results.getString("listing");

				if(this.connectionFactory instanceof MySQLConnectionFactory) {
					String before = json.substring(0, json.indexOf("\"{") + 2);
					String toConvert = json.substring(before.length(), json.indexOf("\"price\"", before.length()) - 8);
					String after = json.substring(before.length() + toConvert.length());
					String reformatted = before;
					reformatted += Pattern.compile("\"").matcher(toConvert).replaceAll("\\\\\"");
					reformatted += after;

					json = reformatted;
				}

				try {
					me.nickimpact.gts.deprecated.Listing old = this.plugin.getAPIService().getDeprecatedGson().fromJson(json, me.nickimpact.gts.deprecated.Listing.class);
					Listing updated = Listing.builder(this.plugin)
							.entry(this.convert(old.getEntry()))
							.price(old.getEntry().getPrice().getPrice().doubleValue())
							.id(old.getUuid())
							.owner(old.getOwnerUUID())
							.expiration(LocalDateTime.ofInstant(old.getExpiration().toInstant(), ZoneId.systemDefault()))
							.build();
					entries.add(updated);
				} catch (Exception e) {
					this.plugin.getPluginLogger().error("Unable to read listing data for listing with ID: " + results.getString("uuid"));
					this.plugin.getPluginLogger().error("Listing JSON: \n" + json);
					e.printStackTrace();
					++failed;
				}
			}
			results.close();

			this.plugin.getPluginLogger().warn(String.format("Read in %d listings, attempting to convert...", entries.size()));
			if(this.transfer(Lists.newArrayList(entries))) {
				if (failed == 0) {
					this.plugin.getPluginLogger().warn("Purging old table...");
					PreparedStatement p = connection.prepareStatement(processor.apply("DROP TABLE {prefix}listings_v2"));
					p.executeUpdate();
				} else {
					if(entries.size() != 0) {
						this.plugin.getPluginLogger().warn(String.format("Failed to read %d listings, will preserve database for now...", failed));
					}
				}
			}

			return entries;
		}

		Connection connection = this.connectionFactory.getConnection();
		PreparedStatement query = connection.prepareStatement(this.processor.apply(SELECT_ALL_LISTINGS));
		ResultSet results = query.executeQuery();

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
						.price(price)
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
	}

	@Override
	public boolean addIgnorer(UUID uuid) throws Exception {
		Connection connection = connectionFactory.getConnection();

		String stmt = processor.apply(ADD_IGNORER);
		PreparedStatement ps = connection.prepareStatement(stmt);
		ps.setString(1, uuid.toString());
		ps.executeUpdate();

		return true;
	}

	@Override
	public boolean removeIgnorer(UUID uuid) throws Exception {
		Connection connection = connectionFactory.getConnection();

		String stmt = processor.apply(REMOVE_IGNORER);
		PreparedStatement ps = connection.prepareStatement(stmt);
		ps.setString(1, uuid.toString());
		ps.executeUpdate();

		return true;
	}

	@Override
	public List<UUID> getAllIgnorers() throws Exception {
		List<UUID> ignorers = Lists.newArrayList();
		Connection connection = this.connectionFactory.getConnection();
		PreparedStatement query = connection.prepareStatement(this.processor.apply(GET_IGNORERS));
		ResultSet results = query.executeQuery();

		while(results.next()) {
			ignorers.add(UUID.fromString(results.getString("uuid")));
		}

		return ignorers;
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

	@Deprecated
	private boolean transfer(List<Listing> listings) {
		AtomicBoolean result = new AtomicBoolean(true);
		if(listings.size() > 0) {
			Listing focus = listings.remove(0);
			this.plugin.getAPIService().getStorage().addListing(focus).thenAccept(x -> this.transfer(listings)).exceptionally(x -> {
				x.printStackTrace();
				result.set(false);
				return null;
			});
		}

		return result.get();
	}

	@Deprecated
	private Entry convert(me.nickimpact.gts.api.deprecated.Entry old) throws Exception {
		EntryClassification classification = this.plugin.getAPIService().getEntryRegistry().getForIdentifier(old.getClass().getAnnotation(JsonTyping.class).value()).get();
		Entry entry = (Entry) classification.getClassification().newInstance();
		return entry.setEntry(old.getEntry());
	}
}
