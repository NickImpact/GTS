package com.nickimpact.GTS.Utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.nbthandler.NBTHandler;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import scala.collection.mutable.MultiMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================*
 * |            Project Name: GTS (Sponge)
 * |                  Author: Nick (NickImpact)
 * |        Package Location: com.nickimpact.GTS.Utils
 * |
 * |   Date of File Creation: 1/1/2017
 * |   Time of File Creation: 4:45 PM
 * =============================================================================
 */
public class MySQLProvider {

    private HikariDataSource hikari;
    private String dbName;

    public MySQLProvider() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws Exception {
        HikariConfig config = new HikariConfig();

        String address = GTS.getInstance().getConfig().getMysqlHost();
        int port = GTS.getInstance().getConfig().getMysqlPort();

        this.dbName = GTS.getInstance().getConfig().getMysqlDatabase();
        String username = GTS.getInstance().getConfig().getMysqlUser();
        String password = GTS.getInstance().getConfig().getMysqlPassword();

        config.setMaximumPoolSize(10);

        config.setPoolName("GTS");
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", dbName);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("cacheCallableStmts", true);
        config.addDataSourceProperty("alwaysSendSetIsolation", false);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10)); // 10000
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(5)); // 5000
        config.setValidationTimeout(TimeUnit.SECONDS.toMillis(3)); // 3000
        config.setInitializationFailFast(true);
        config.setConnectionTestQuery("/* GTS ping */ SELECT 1");

        hikari = new HikariDataSource(config);
        createTable();
    }

    public void shutdown() throws Exception {
        if (hikari != null) {
            hikari.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    private void createTable() {
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + dbName + "` (ID INTEGER, Ends MEDIUMTEXT, Expired TINYINT(1), uuid CHAR(36), Lot MEDIUMTEXT);")) {
                    statement.executeUpdate();
                    statement.close();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void clearTable(){
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + dbName + "`")) {
                    statement.executeUpdate();
                    statement.close();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean hasTooMany(UUID uuid){
        if(GTS.getInstance().getConfig().getMaxPokemon() != -1) {
            try {
                try(Connection connection = this.getConnection()) {
                    if (connection == null || connection.isClosed()) {
                        throw new IllegalStateException("SQL connection is null");
                    }

                    try (PreparedStatement ps = connection.prepareStatement("SELECT uuid, Lot FROM `" + dbName + "` WHERE uuid='" + uuid + "'")) {
                        ResultSet result = ps.executeQuery();
                        int total = 0;
                        while (result.next()) {
                            total++;
                        }

                        result.close();
                        ps.close();

                        if (total < GTS.getInstance().getConfig().getMaxPokemon()) {
                            return false;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public int getPlacement(){
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement ps = connection.prepareStatement("SELECT ID FROM `" + dbName + "` ORDER BY ID ASC")) {
                    ResultSet result = ps.executeQuery();

                    int placement = 1;
                    while (result.next()) {
                        if (placement == result.getInt("ID")) {
                            placement++;
                        } else {
                            break;
                        }
                    }
                    result.close();
                    ps.close();

                    return placement;
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * This function adds a new lot to the database for the specified uuid.
     *
     * @param uuid A uuid of the user we will deposit the pokemon into
     * @param lot A representation of the market listing
     */

    public void addLot(UUID uuid, String lot) {
        try {
            try(Connection connection = getConnection()){
                if(connection == null || connection.isClosed()){
                    throw new IllegalStateException("SQL connection is null");
                }
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + dbName + "`(ID, Ends, Expired, uuid, Lot) VALUES (?, ?, ?, ?, ?)")) {
                    statement.setInt(1, getPlacement());
                    statement.setString(2, Instant.now().plusSeconds(GTS.getInstance().getConfig().getLotTime() * 60).toString());
                    statement.setBoolean(3, false);
                    statement.setString(4, uuid.toString());
                    statement.setString(5, lot);
                    statement.executeUpdate();
                    statement.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a {@link Lot} from the database
     *
     * @param id The proposed Lot ID
     * @return A representation of the Lot in the database. If we return null, we didn't find the lot at all.
     */
    public Lot getLot(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT Lot FROM `" + dbName + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        return (Lot) LotUtils.lotFromJson(result.getString("Lot"));
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a {@link Lot} from the Database
     *
     * @param id The proposed Lot ID
     */
    public void deleteLot(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + dbName + "` WHERE ID='" + id + "'")){
                    query.executeUpdate();
                    query.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Lot> getAllLots() {
        List<Lot> lots = new ArrayList<>();

        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT Lot FROM `" + dbName + "`")) {
                    ResultSet results = query.executeQuery();
                    while (results.next()) {
                        Lot lot = (Lot) LotUtils.lotFromJson(results.getString("Lot"));
                        if (!isExpired(lot.getLotID())) {
                            lots.add(lot);
                        }
                    }
                    results.close();
                    query.close();
                }
            }

            return lots;
        } catch(SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    public boolean returnLots() {
        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT uuid, Lot FROM `" + dbName + "`")) {
                    ResultSet results = query.executeQuery();
                    while (results.next()) {
                        UUID uuid = UUID.fromString(results.getString("uuid"));
                        Lot lot = (Lot) LotUtils.lotFromJson(results.getString("Lot"));
                        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), uuid);
                        if(storage.isPresent()){
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(JsonToNBT.getTagFromJson(lot.getNBT()), NBTHandler.getWorld());
                            storage.get().addToParty(pokemon);
                            storage.get().sendUpdatedList();
                            Gson gson = new Gson();
                            try(PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + dbName + "` WHERE Lot='" + gson.toJson(lot) + "'")){
                                ps.executeUpdate();
                                ps.close();
                            }
                        }
                    }
                    results.close();
                    query.close();
                    Sponge.getServer().getBroadcastChannel().send(MessageConfig.getMessage("Admin.Clear"));
                } catch (NBTException e) {
                    e.printStackTrace();
                }
            }

            return true;
        } catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public List<Lot> getPlayerLots(UUID uuid){
        List<Lot> lots = new ArrayList<>();

        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try(PreparedStatement ps = connection.prepareStatement("SELECT Lot FROM `" + dbName + "` WHERE uuid='" + uuid + "'")) {
                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        lots.add((Lot) LotUtils.lotFromJson(results.getString("Lot")));
                    }
                    results.close();
                    ps.close();
                }
                return lots;
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    public void updateEntry(int id) {
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("UPDATE `" + dbName + "` SET Expired='" + 1 + "' WHERE ID='" + id + "'")) {
                    query.executeUpdate();
                    query.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getEnd(int id){
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                Date date = null;
                try (PreparedStatement ps = connection.prepareStatement("SELECT Ends FROM `" + dbName + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        date = Date.from(Instant.parse(result.getString("Ends")));
                    }
                    result.close();
                    ps.close();
                    return date;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isExpired(int id) {
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                boolean expired = false;
                try (PreparedStatement ps = connection.prepareStatement("SELECT Expired FROM `" + dbName + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        expired = result.getBoolean("Expired");
                    }
                    result.close();
                    ps.close();

                    return expired;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}