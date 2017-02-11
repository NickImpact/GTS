package com.nickimpact.GTS.Storage;

import com.nickimpact.GTS.GTS;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
public class MySQLProvider extends SQLDatabase {

    private HikariDataSource hikari;
    private String dbName;

    public MySQLProvider() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init() throws Exception {
        HikariConfig config = new HikariConfig();

        String address = GTS.getInstance().getConfig().getHost();
        int port = GTS.getInstance().getConfig().getPort();

        this.dbName = GTS.getInstance().getConfig().getDatabase();
        String username = GTS.getInstance().getConfig().getUser();
        String password = GTS.getInstance().getConfig().getPassword();

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
    }

    @Override
    HikariDataSource getHikari() {
        return this.hikari;
    }

    @Override
    String getDbName() {
        return this.dbName;
    }
}