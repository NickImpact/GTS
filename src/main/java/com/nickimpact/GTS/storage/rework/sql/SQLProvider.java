package com.nickimpact.GTS.storage.rework.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.logging.Log;
import com.nickimpact.GTS.storage.rework.Storage;
import com.nickimpact.GTS.utils.Lot;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class SQLProvider implements Storage
{
    /** The table name for the main place of storage */
    private String mt;

    /** The table name for any generated logs */
    private String lt;

    /** The hikari instance being used by our storage provider */
    protected HikariDataSource hikari;

    public SQLProvider(String mt, String lt)
    {
        this.mt = mt;
        this.lt = lt;
    }

    private HikariDataSource getHikari()
    {
        return this.hikari;
    }

    private Connection getConnection() throws SQLException
    {
        return getHikari().getConnection();
    }

    @Override
    public void shutdown() throws Exception
    {
        if (getHikari() != null)
        {
            getHikari().close();
        }
    }

    public void createTables() {
        try{
            try(Connection connection = this.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + mt + "` (ID INTEGER, Ends MEDIUMTEXT, Expired TINYINT(1), uuid CHAR(36), Lot MEDIUMTEXT, DoesExpire TINYINT(1));")) {
                    statement.executeUpdate();
                    statement.close();
                }

                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + lt + "` (ID INTEGER, Date MEDIUMTEXT, uuid CHAR(36), Action MEDIUMTEXT, Log MEDIUMTEXT);")) {
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
                ResultSet rs = dbmd.getColumns(null, null, this.mt.toUpperCase(), "DoesExpire".toUpperCase());
                if(!rs.next()){
                    try(PreparedStatement ps = connection.prepareStatement("ALTER TABLE `" + mt + "` ADD DoesExpire MEDIUMTEXT NOT NULL DEFAULT(1);")) {
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getLots()
    {
        try{
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement query = connection.prepareStatement("SELECT ID, Lot, Ends, Expired FROM `" + mt + "`")) {
                    ResultSet results = query.executeQuery();
                    while (results.next()) {
                        Lot lot;
                        try {
                            lot = LotUtils.lotFromJson(results.getString("Lot"));
                        } catch (JsonSyntaxException e){
                            GTS.getInstance().getConsole().sendMessage(Text.of(
                                    GTSInfo.ERROR_PREFIX, TextColors.DARK_RED, "=== JSON Syntax Error ==="
                            ));
                            GTS.getInstance().getConsole().sendMessage(Text.of(
                                    GTSInfo.ERROR_PREFIX, TextColors.RED, "Invalid lot JSON detected"
                            ));
                            GTS.getInstance().getConsole().sendMessage(Text.of(
                                    GTSInfo.ERROR_PREFIX, TextColors.RED, "Lot ID: " + results.getInt("ID")
                            ));

                            continue;
                        }
                        boolean expired = results.getBoolean("Expired");
                        java.util.Date date = java.util.Date.from(Instant.parse(results.getString("Ends")));

                        if(!expired) {
                            GTS.getInstance().getLots().add(new LotCache(lot, false, date));
                        } else {
                            GTS.getInstance().getExpiredLots().add(new LotCache(lot, true, date));
                        }
                    }
                    results.close();
                    query.close();
                }
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void getLogs()
    {
        try {
            try(Connection connection = getConnection()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("SQL connection is null");
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + lt + "` ORDER BY ID ASC")) {
                    ResultSet result = ps.executeQuery();
                    while (result.next()) {
                        int lotID = result.getInt("ID");
                        java.util.Date date = java.util.Date.from(Instant.parse(result.getString("Date")));
                        UUID uuid = UUID.fromString(result.getString("uuid"));
                        String action = result.getString("Action");
                        String logInfo = result.getString("Log");

                        Log log = new Log(lotID, date, uuid, action);
                        log.setLog(logInfo);

                        GTS.getInstance().getLogs().put(uuid, log);
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean purge(boolean logs)
    {
        try
        {
            try(Connection connection = this.getConnection())
            {
                if (connection == null || connection.isClosed())
                {
                    throw new IllegalStateException("SQL connection is null");
                }

                try (PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + mt + "`"))
                {
                    statement.executeUpdate();
                    statement.close();
                }

                if(logs)
                {
                    try(PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + lt + "`"))
                    {
                        statement.executeUpdate();
                        statement.close();
                    }
                }

                GTS.getInstance().getLots().clear();
            }
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save()
    {
        try {
            try(Connection connection = getConnection()){
                if(connection == null || connection.isClosed())
                    throw new IllegalStateException("SQL connection is null");

                Statement statement = connection.createStatement();
                for(String cmd : LotUtils.getSqlCmds()) {
                    statement.addBatch(cmd);
                }

                statement.executeBatch();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
