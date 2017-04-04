package com.nickimpact.GTS.Storage;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.GTS.Configuration.MessageConfig;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.Utils.Lot;
import com.nickimpact.GTS.Utils.LotUtils;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.zaxxer.hikari.HikariDataSource;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * ============================================================================*
 * |            Project Name: GTS (Sponge)
 * |                  Author: Nick (NickImpact)
 * |        Package Location: com.nickimpact.GTS.Storage
 * |
 * |   Date of File Creation: 2/7/2017
 * |   Time of File Creation: 1:12 AM
 * =============================================================================
 */
public abstract class SQLDatabase {

    abstract void init() throws Exception;
    abstract HikariDataSource getHikari();
    abstract String getDbName();

    public void shutdown() throws Exception {
        if (getHikari() != null) {
            getHikari().close();
        }
    }

    private Connection getConnection() throws SQLException {
        return getHikari().getConnection();
    }

    public void createTable() {
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + getDbName() + "` (ID INTEGER, Ends MEDIUMTEXT, Expired TINYINT(1), uuid CHAR(36), Lot MEDIUMTEXT);")) {
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

                try (PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + getDbName() + "`")) {
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

                    try (PreparedStatement ps = connection.prepareStatement("SELECT uuid, Lot FROM `" + getDbName() + "` WHERE uuid='" + uuid + "'")) {
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

                try (PreparedStatement ps = connection.prepareStatement("SELECT ID FROM `" + getDbName() + "` ORDER BY ID ASC")) {
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
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + getDbName() + "`(ID, Ends, Expired, uuid, Lot) VALUES (?, ?, ?, ?, ?)")) {
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
                try (PreparedStatement ps = connection.prepareStatement("SELECT Lot FROM `" + getDbName() + "` WHERE ID='" + id + "'")) {
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
     * Deletes a {@link Lot} from the SQLDatabase
     *
     * @param id The proposed Lot ID
     */
    public void deleteLot(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + getDbName() + "` WHERE ID='" + id + "'")){
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

                try (PreparedStatement query = connection.prepareStatement("SELECT Lot FROM `" + getDbName() + "`")) {
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

        return Lists.newArrayList();
    }

    public boolean returnLots() {
        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT uuid, Lot FROM `" + getDbName() + "`")) {
                    ResultSet results = query.executeQuery();
                    while (results.next()) {
                        UUID uuid = UUID.fromString(results.getString("uuid"));
                        Lot lot = (Lot) LotUtils.lotFromJson(results.getString("Lot"));
                        Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), uuid);
                        if(storage.isPresent()){
                            EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(JsonToNBT.getTagFromJson(lot.getNBT()), (World)Sponge.getServer().getPlayer(uuid).get().getWorld());
                            storage.get().addToParty(pokemon);
                            storage.get().sendUpdatedList();
                            Gson gson = new Gson();
                            try(PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + getDbName() + "` WHERE Lot='" + gson.toJson(lot) + "'")){
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

                try(PreparedStatement ps = connection.prepareStatement("SELECT Lot FROM `" + getDbName() + "` WHERE uuid='" + uuid + "'")) {
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

                try (PreparedStatement query = connection.prepareStatement("UPDATE `" + getDbName() + "` SET Expired='" + 1 + "' WHERE ID='" + id + "'")) {
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
                try (PreparedStatement ps = connection.prepareStatement("SELECT Ends FROM `" + getDbName() + "` WHERE ID='" + id + "'")) {
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
                try (PreparedStatement ps = connection.prepareStatement("SELECT Expired FROM `" + getDbName() + "` WHERE ID='" + id + "'")) {
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
