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

package me.nickimpact.gts.storage.dao.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.events.DataReceivedEvent;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.storage.dao.AbstractDao;
import me.nickimpact.gts.storage.dao.sql.connection.AbstractConnectionFactory;
import me.nickimpact.gts.storage.dao.sql.connection.hikari.MySqlConnectionFactory;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;


public class SqlDao extends AbstractDao {

	private static final String SELECT_ALL_LISTINGS = "SELECT * FROM `{prefix}listings_v2`";
	private static final String TRUNCATE_LISTINGS = "TRUNCATE TABLE `{prefix}listings_v2`";
	private static final String ADD_LISTING = "INSERT INTO `{prefix}listings_v2` VALUES ('%s', '%s', '%s')";
	private static final String UPDATE_LISTING = "UPDATE `{prefix}listings_v2` SET LISTING='%s' WHERE UUID='%s'";
	private static final String REMOVE_LISTING = "DELETE FROM `{prefix}listings_v2` WHERE UUID='%s'";
	private static final String ADD_IGNORER = "INSERT INTO `{prefix}ignorers` VALUES ('%s')";
	private static final String REMOVE_IGNORER = "DELETE FROM `{prefix}ignorers` WHERE UUID='%s'";
	private static final String GET_IGNORERS = "SELECT * FROM `{prefix}ignorers`";

	private static final String TEMP_DROP = "DROP TABLE `%s`";

	@Getter
	private final AbstractConnectionFactory provider;

	@Getter
	private final Function<String, String> prefix;

	public SqlDao(GTS plugin, AbstractConnectionFactory provider, String prefix) {
		super(plugin, provider.getName());
		this.provider = provider;
		this.prefix = s -> s.replace("{prefix}", prefix);
	}

	private boolean tableExists(String table) throws SQLException {
		try(Connection connection = provider.getConnection()) {
			try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
				while(rs.next()) {
					if(rs.getString(3).equalsIgnoreCase(table)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	private void runRemoval(String key, UUID uuid) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(key);
			stmt = String.format(stmt, uuid);
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		try {
			provider.init();

			// Init tables
			if(!tableExists(prefix.apply("{prefix}listings_v2"))) {
				String schemaFileName = "me/nickimpact/gts/schema/" + provider.getName().toLowerCase() + ".sql";
				try (InputStream is = plugin.getResourceStream(schemaFileName)) {
					if(is == null) {
						throw new Exception("Couldn't locate schema file for " + provider.getName());
					}

					try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
						try (Connection connection = provider.getConnection()) {
							try (Statement s = connection.createStatement()) {
								StringBuilder sb = new StringBuilder();
								String line;

								boolean available = true;
								while ((line = reader.readLine()) != null) {
									if (line.startsWith("--") || line.startsWith("#")) continue;
									if (line.startsWith("CREATE TABLE") && tableExists(prefix.apply(line.substring(14, line.length() - 3)))) {
										available = false;
									}

									if(available) {
										sb.append(line);
									}

									// check for end of declaration
									if (line.endsWith(";")) {
										if(!available) {
											available = true;
										} else {
											sb.deleteCharAt(sb.length() - 1);

											String result = prefix.apply(sb.toString().trim());
											if (!result.isEmpty())
												s.addBatch(result);

											// reset
											sb = new StringBuilder();
										}
									}
								}
								s.executeBatch();
							}
						}
					}
				}
			}

			for(String deprecated : Lists.newArrayList("logs_v2", "held_entries_v2", "held_prices_v2")) {
				String table = prefix.apply("{prefix}" + deprecated);
				if (tableExists(table)) {
					this.dropTable(table);
				}
			}
		} catch (Exception e) {
			plugin.getConsole().ifPresent(console -> console.sendMessage(Text.of(
					GTSInfo.ERROR, "An error occurred whilst initializing the database..."
			)));
			e.printStackTrace();
		}
	}

	private void dropTable(String table) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(String.format(TEMP_DROP, table));
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			provider.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addListing(Listing listing) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(ADD_LISTING);
			stmt = String.format(stmt, listing.getUuid(), listing.getOwnerUUID(), GTS.prettyGson.toJson(listing));
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		} catch (Exception e) {
			GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(
					Text.of(GTSInfo.ERROR, "Something happened during the writing process")
			));
			e.printStackTrace();
		}
	}

	@Override
	public void updateListing(Listing listing) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(UPDATE_LISTING);
			stmt = String.format(stmt, GTS.prettyGson.toJson(listing), listing.getUuid());
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		} catch (Exception e) {
			GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(
					Text.of(GTSInfo.ERROR, "Something happened during the writing process")
			));
			e.printStackTrace();
		}
	}

	@Override
	public void removeListing(UUID uuid) throws Exception {
		this.runRemoval(REMOVE_LISTING, uuid);
	}

	@Override
	public List<Listing> getListings() throws Exception {
		return getListings(SELECT_ALL_LISTINGS);
	}

	public List<Listing> getListings(String key) throws Exception {
		List<Listing> entries = Lists.newArrayList();
		try (Connection connection = provider.getConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(key))) {
				ResultSet results = query.executeQuery();
				while(results.next()) {
					String json = results.getString("listing");

					GTS.getInstance().getLogger().debug(json);
					if(this.provider instanceof MySqlConnectionFactory) {
						String before = json.substring(0, json.indexOf("\"{") + 2);
						String toConvert = json.substring(before.length(), json.indexOf("\"price\"", before.length()) - (json.contains("}\"\n    }") ? 13 : 11));
						String after = json.substring(before.length() + toConvert.length());
						String reformatted = before;
						reformatted += Pattern.compile("\"").matcher(toConvert).replaceAll("\\\\\"");
						reformatted += after;

						json = reformatted;
					}

					try {
						entries.add(GTS.prettyGson.fromJson(json, Listing.class));
					} catch (JsonSyntaxException e) {
						GTS.getInstance().getLogger().error("Unable to read listing data for listing with ID: " + results.getString("uuid"));
						GTS.getInstance().getLogger().error("Listing JSON: \n" + json);
					}
				}
				results.close();
			}
		}

		return entries;
	}

	@Override
	public void addIgnorer(UUID uuid) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(ADD_IGNORER);
			stmt = String.format(stmt, uuid);
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		}
	}

	@Override
	public void removeIgnorer(UUID uuid) throws Exception{
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(REMOVE_IGNORER);
			stmt = String.format(stmt, uuid);
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		}
	}

	@Override
	public List<UUID> getIgnorers() throws Exception {
		List<UUID> ignorers = Lists.newArrayList();
		try (Connection connection = provider.getConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(GET_IGNORERS))) {
				ResultSet results = query.executeQuery();
				while(results.next())
					ignorers.add(UUID.fromString(results.getString("uuid")));

				results.close();
			}
		}

		return ignorers;
	}

	@Override
	public void purge(boolean logs) throws Exception {
		try (Connection connection = provider.getConnection()) {
			try (PreparedStatement stmt = connection.prepareStatement(prefix.apply(TRUNCATE_LISTINGS))) {
				stmt.executeUpdate();
			}

			// Clear the cache
			GTS.getInstance().getListingsCache().clear();
		}
	}

	@Override
	public void save() throws Exception {}
}
