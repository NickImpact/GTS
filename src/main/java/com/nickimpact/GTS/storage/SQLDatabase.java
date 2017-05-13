package com.nickimpact.GTS.storage;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import com.nickimpact.GTS.utils.Lot;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;

/**
 * ============================================================================*
 * |            Project Name: GTS (Sponge)
 * |                  Author: Nick (NickImpact)
 * |        Package Location: com.nickimpact.GTS.storage
 * |
 * |   Date of File Creation: 2/7/2017
 * |   Time of File Creation: 1:12 AM
 * =============================================================================
 */
public abstract class SQLDatabase {

    private String mainTable;
    private String logTable;

    public SQLDatabase(String mainTable, String logTable){
        this.mainTable = mainTable;
        this.logTable = logTable;
    }

    abstract void init() throws Exception;
    abstract HikariDataSource getHikari();

    public void shutdown() throws Exception {
        if (getHikari() != null) {
            getHikari().close();
        }
    }

    private Connection getConnection() throws SQLException {
        return getHikari().getConnection();
    }

    public PreparedStatement prepareStatement(String sql){
        try {
            try (Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                return connection.prepareStatement(sql);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public void createTables() {
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + mainTable + "` (ID INTEGER, Ends MEDIUMTEXT, Expired TINYINT(1), uuid CHAR(36), Lot MEDIUMTEXT, DoesExpire TINYINT(1));")) {
                    statement.executeUpdate();
                    statement.close();
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + logTable + "` (ID INTEGER, Date MEDIUMTEXT, uuid CHAR(36), Action MEDIUMTEXT, Log MEDIUMTEXT);")) {
                    statement.executeUpdate();
                    statement.close();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updateTables(){
        try {
            try (Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                DatabaseMetaData dbmd = connection.getMetaData();
                ResultSet rs = dbmd.getColumns(null, null, this.mainTable.toUpperCase(), "DoesExpire".toUpperCase());
                if(!rs.next()){
                    try(PreparedStatement ps = connection.prepareStatement("ALTER TABLE `" + mainTable + "` ADD DoesExpire MEDIUMTEXT NOT NULL DEFAULT(1);")) {
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void clearTable(){
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + mainTable + "`")) {
                    statement.executeUpdate();
                    statement.close();
                }

                GTS.getInstance().getLots().clear();
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

                    try (PreparedStatement ps = connection.prepareStatement("SELECT uuid, Lot FROM `" + mainTable + "` WHERE uuid='" + uuid + "'")) {
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

    public boolean canExpire(int id){
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                boolean canExpire = false;
                try (PreparedStatement ps = connection.prepareStatement("SELECT DoesExpire FROM `" + mainTable + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        canExpire = result.getBoolean("Does_Expire");
                    }
                    result.close();
                    ps.close();

                    return canExpire;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    /**
     * This function adds a new lot to the database for the specified uuid.
     *
     * @param uuid A uuid of the user we will deposit the pokemon into
     * @param lot A representation of the market listing
     */
    /*public synchronized void addLot(UUID uuid, String lot, boolean canExpire, long expires) {
        try {
            try(Connection connection = getConnection()){
                if(connection == null || connection.isClosed()){
                    throw new IllegalStateException("SQL connection is null");
                }
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + mainTable + "`(ID, Ends, Expired, uuid, Lot, DoesExpire) VALUES (?, ?, ?, ?, ?, ?)")) {
                    statement.setInt(1, getPlacement(1));
                    statement.setString(2, Instant.now().plusSeconds(expires).toString());
                    statement.setBoolean(3, false);
                    statement.setString(4, uuid.toString());
                    statement.setString(5, lot);
                    statement.setBoolean(6, canExpire);
                    statement.executeUpdate();
                    statement.close();


                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Retrieves a {@link Lot} from the database
     *
     * @param id The proposed Lot ID
     * @return A representation of the Lot in the database. If we return null, we didn't find the lot at all.
     */
    public synchronized LotCache getLot(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT Lot, Ends, Expired FROM `" + mainTable + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        Lot lot = LotUtils.lotFromJson(result.getString("Lot"));
                        boolean expired = result.getBoolean("Expired");
                        Date date = Date.from(Instant.parse(result.getString("Ends")));

                        return new LotCache(lot, expired, date);
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
    public synchronized void deleteLot(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + mainTable + "` WHERE ID='" + id + "'")){
                    query.executeUpdate();
                    query.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    public synchronized void execute(String sql, String... options){
        try{
            PreparedStatement ps = prepareStatement(sql);
            for(int i = 1; i <= options.length; i++){
                ps.setString(i, options[i]);
            }
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }*/

    /**
     * Using a list of commands, we will edit the entire database on the fly
     * with just this one call. We will add, replace, and delete anything
     * we need to here.
     *
     * @param sqlCmds The representative commands to be processed
     */
    public synchronized void updateLots(List<String> sqlCmds){
        try {
            try(Connection connection = getConnection()){
                if(connection == null || connection.isClosed())
                    throw new IllegalStateException("SQL connection is null");

                Statement statement = connection.createStatement();
                for(String cmd : sqlCmds) {
                    statement.addBatch(cmd);
                }

                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<LotCache> getAllLots() {
        List<LotCache> lots = new ArrayList<>();

        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT Lot, Ends, Expired FROM `" + mainTable + "`")) {
                    ResultSet results = query.executeQuery();
                    while (results.next()) {
                        Lot lot = LotUtils.lotFromJson(results.getString("Lot"));
                        boolean expired = results.getBoolean("Expired");
                        Date date = Date.from(Instant.parse(results.getString("Ends")));

                        lots.add(new LotCache(lot, expired, date));
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

    public synchronized int clearLots() {
        int slots = 0;
        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT * FROM `" + mainTable + "`")) {
                    ResultSet results = query.executeQuery();
                    while(results.next())
                        ++slots;
                    clearTable();

                    results.close();
                    query.close();
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        return slots;
    }

    public synchronized List<LotCache> getPlayerLots(UUID uuid){
        List<LotCache> lots = new ArrayList<>();

        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try(PreparedStatement ps = connection.prepareStatement("SELECT Lot, Ends, Expired FROM `" + mainTable + "` WHERE uuid='" + uuid + "'")) {
                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        Lot lot = LotUtils.lotFromJson(results.getString("Lot"));
                        boolean expired = results.getBoolean("Expired");
                        Date date = Date.from(Instant.parse(results.getString("Ends")));
                        lots.add(new LotCache(lot, expired, date));
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

    public void updateEntry(Lot lot){
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("UPDATE `" + mainTable + "` SET Lot='" + new Gson().toJson(lot) + "' WHERE ID='" + lot.getLotID() + "'")) {
                    query.executeUpdate();
                    query.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setExpired(int id) {
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("UPDATE `" + mainTable + "` SET Expired='" + 1 + "' WHERE ID='" + id + "'")) {
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
                try (PreparedStatement ps = connection.prepareStatement("SELECT Ends FROM `" + mainTable + "` WHERE ID='" + id + "'")) {
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
        return Date.from(Instant.now());
    }

    public boolean isExpired(int id) {
        try {
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                boolean expired = false;
                try (PreparedStatement ps = connection.prepareStatement("SELECT Expired FROM `" + mainTable + "` WHERE ID='" + id + "'")) {
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

    public void appendLog(Log log){
        try {
            try(Connection connection = getConnection()){
                if(connection == null || connection.isClosed()){
                    throw new IllegalStateException("SQL connection is null");
                }
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + logTable + "`(ID, Date, uuid, Action, Log) VALUES (?, ?, ?, ?, ?)")) {
                    statement.setInt(1, log.getId());
                    statement.setString(2, log.getDate().toInstant().toString());
                    statement.setString(3, log.getActor().toString());
                    statement.setString(4, log.getAction());
                    statement.setString(5, log.getLog());
                    statement.executeUpdate();
                    statement.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Log> getLogs(UUID uuid){
        List<Log> logs = Lists.newArrayList();
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + logTable + "` WHERE uuid='" + uuid + "' ORDER BY ID ASC")) {
                    ResultSet result = ps.executeQuery();
                    while (result.next()) {
                        int lotID = result.getInt("ID");
                        Date date = Date.from(Instant.parse(result.getString("Date")));
                        String action = result.getString("Action");
                        String logInfo = result.getString("Log");

                        Log log = new Log(lotID, date, uuid, action);
                        log.setLog(logInfo);
                        logs.add(log);
                    }
                    return logs;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public Log getMostRecentLog(UUID uuid){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + logTable + "` WHERE uuid='" + uuid + "' ORDER BY ID DEC")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        int lotID = result.getInt("ID");
                        Date date = Date.from(Instant.parse(result.getString("Date")));
                        String action = result.getString("Action");
                        String logInfo = result.getString("Log");

                        Log log = new Log(lotID, date, uuid, action);
                        log.setLog(logInfo);
                        return log;
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Log getLog(int id){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + logTable + "` WHERE ID='" + id + "'")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        Date date = Date.from(Instant.parse(result.getString("Date")));
                        UUID uuid = UUID.fromString(result.getString("uuid"));
                        String action = result.getString("Action");
                        String logInfo = result.getString("Log");

                        Log log = new Log(id, date, uuid, action);
                        log.setLog(logInfo);
                        return log;
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void purgeLogs(UUID uuid){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + logTable + "` WHERE uuid='" + uuid + "'")) {
                    ps.executeUpdate();
                    ps.close();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Log getEarliestLog(UUID uuid){
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + logTable + "` WHERE uuid='" + uuid + "' ORDER BY ID ASC")) {
                    ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        int lotID = result.getInt("ID");
                        Date date = Date.from(Instant.parse(result.getString("Date")));
                        String action = result.getString("Action");
                        String logInfo = result.getString("Log");

                        Log log = new Log(lotID, date, uuid, action);
                        log.setLog(logInfo);
                        return log;
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
