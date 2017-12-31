package com.nickimpact.gts.storage.dao.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.api.utils.MessageUtils;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.dao.AbstractDao;
import com.nickimpact.gts.storage.dao.sql.connection.AbstractConnectionFactory;
import lombok.Getter;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class SqlDao extends AbstractDao {

	private static final String SELECT_ALL_LISTINGS = "SELECT ID, LISTING FROM `{prefix}listings`";
	private static final String SELECT_ALL_LOGS = "SELECT * FROM `{prefix}logs` ORDER BY ID ASC";
	private static final String TRUNCATE_LISTINGS = "TRUNCATE TABLE `{prefix}listings`";
	private static final String TRUNCATE_LOGS = "TRUNCATE TABLE `{prefix}logs`";
	private static final String ADD_LISTING = "INSERT INTO `{prefix}listings` VALUES (%d, '%s', '%s')";
	private static final String ADD_LOG = "INSERT INTO `{prefix}logs` VALUES (%d, '%s', '%s')";
	private static final String REMOVE_LISTING = "DELETE FROM `{prefix}listings` WHERE ID=%d";
	private static final String REMOVE_LOG = "DELETE FROM `{prefix}logs` WHERE ID=%d";
	private static final String ADD_HELD_ENTRY = "INSERT INTO `{prefix}held_entries` VALUES (%d, '%s')";
	private static final String REMOVE_HELD_ENTRY = "DELETE FROM `{prefix}held_entries` WHERE ID=%d";
	private static final String GET_HELD_ENTRIES = "SELECT * FROM `{prefix}held_entries` ORDER BY ID ASC";
	private static final String ADD_HELD_PRICE = "INSERT INTO `{prefix}held_prices` VALUES (%d, '%s')";
	private static final String REMOVE_HELD_PRICE = "DELETE FROM `{prefix}held_prices` WHERE ID=%d";
	private static final String GET_HELD_PRICES = "SELECT * FROM `{prefix}held_prices` ORDER BY ID ASC";
	private static final String ADD_IGNORER = "INSERT INTO `{prefix}ignorers` VALUES ('%s')";
	private static final String REMOVE_IGNORER = "DELETE FROM `{prefix}ignorers` WHERE UUID='%s'";
	private static final String GET_IGNORERS = "SELECT * FROM `{prefix}ignorers`";

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

	private Connection findConnection() throws SQLException {
		try (Connection connection = provider.getConnection()) {
			if (connection.isClosed()) {
				throw new IllegalStateException("Fetched connection for operation is closed...");
			}

			return connection;
		}
	}

	private void runRemoval(String key, int id) throws Exception {
		try (Connection connection = this.findConnection()) {
			String stmt = prefix.apply(key);
			stmt = String.format(stmt, id);
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
				ps.close();
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
			if(!tableExists(prefix.apply("{prefix}listings"))) {
				String schemaFileName = "schema/" + provider.getName().toLowerCase() + ".sql";
				try (InputStream is = plugin.getResourceStream(schemaFileName)) {
					if(is == null) {
						throw new Exception("Couldn't locate schema file for " + provider.getName());
					}

					try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
						try (Connection connection = provider.getConnection()) {
							try (Statement s = connection.createStatement()) {
								StringBuilder sb = new StringBuilder();
								String line;
								while ((line = reader.readLine()) != null) {
									if (line.startsWith("--") || line.startsWith("#")) continue;

									sb.append(line);

									// check for end of declaration
									if (line.endsWith(";")) {
										sb.deleteCharAt(sb.length() - 1);

										String result = prefix.apply(sb.toString().trim());
										if (!result.isEmpty())
											s.addBatch(result);

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
		} catch (Exception e) {
			plugin.getConsole().ifPresent(console -> console.sendMessage(Text.of(
					GTSInfo.ERROR_PREFIX, "An error occurred whilst initializing the database..."
			)));
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
		try (Connection connection = this.findConnection()) {
			String stmt = prefix.apply(ADD_LISTING);
			stmt = String.format(stmt, listing.getID(), listing.getOwnerUUID(), GTS.prettyGson.toJson(listing));
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
				ps.close();
			}
		} catch (Exception e) {
			GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(
					Text.of(GTSInfo.ERROR_PREFIX, "Something happened during the writing process")
			));
			e.printStackTrace();
		}
	}

	@Override
	public void removeListing(int id) throws Exception {
		this.runRemoval(REMOVE_LISTING, id);
	}

	@Override
	public List<Listing> getListings() throws Exception {
		List<Listing> entries = Lists.newArrayList();
		try (Connection connection = this.findConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(SELECT_ALL_LISTINGS))) {
				ResultSet results = query.executeQuery();
				while(results.next()) {
					try {
						entries.add(GTS.prettyGson.fromJson(results.getString("listing"), Listing.class));
					} catch (JsonSyntaxException e) {
						MessageUtils.genAndSendErrorMessage(
								"JSON Syntax Error",
								"Invalid listing JSON detected",
								"Listing ID: " + results.getInt("id")
						);
					}
				}
				results.close();
				query.close();
			}
		}

		return entries;
	}

	@Override
	public void addLog(Log log) throws Exception {
		try (Connection connection = this.findConnection()) {
			String stmt = prefix.apply(ADD_LOG);
			stmt = String.format(stmt, log.getID(), log.getSource(), GTS.prettyGson.toJson(log));
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
				ps.close();
			}
		}
	}

	@Override
	public void removeLog(int id) throws Exception {
		this.runRemoval(REMOVE_LOG, id);
	}

	@Override
	public List<Log> getLogs() throws Exception {
		List<Log> logs = Lists.newArrayList();
		try (Connection connection = this.findConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(SELECT_ALL_LOGS))) {
				ResultSet results = query.executeQuery();
				while(results.next()) {
					try {
						logs.add(GTS.prettyGson.fromJson(results.getString("log"), Log.class));
					} catch (JsonSyntaxException e) {
						MessageUtils.genAndSendErrorMessage(
								"JSON Syntax Error",
								"Invalid Log JSON detected",
								"Log ID: " + results.getInt("id")
						);
					}
				}
				results.close();
				query.close();
			}
		}

		return logs;
	}

	@Override
	public void addHeldElement(EntryHolder holder) {

	}

	@Override
	public void removeHeldElement(EntryHolder holder) {

	}

	@Override
	public List<EntryHolder> getHeldElements() {
		return null;
	}

	@Override
	public void addHeldPrice(PriceHolder holder) {

	}

	@Override
	public void removeHeldPrice(PriceHolder holder) {

	}

	@Override
	public List<PriceHolder> getHeldPrices() {
		return null;
	}

	@Override
	public void addIgnorer(UUID uuid) {

	}

	@Override
	public void removeIgnorer(UUID uuid) {

	}

	@Override
	public List<UUID> getIgnorers() {
		return null;
	}

	@Override
	public void purge(boolean logs) throws Exception {
		try (Connection connection = this.findConnection()) {
			try (PreparedStatement stmt = connection.prepareStatement(prefix.apply(TRUNCATE_LISTINGS))) {
				stmt.executeUpdate();
				stmt.close();
			}

			try (PreparedStatement stmt = connection.prepareStatement(prefix.apply(TRUNCATE_LOGS))) {
				stmt.executeUpdate();
				stmt.close();
			}

			// Clear the cache
			GTS.getInstance().getListingsCache().clear();
			GTS.getInstance().getLogCache().clear();
		}
	}

	@Override
	public void save() throws Exception {}
}
