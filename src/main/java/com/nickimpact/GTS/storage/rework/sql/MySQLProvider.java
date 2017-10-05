package com.nickimpact.GTS.storage.rework.sql;

import com.zaxxer.hikari.HikariDataSource;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MySQLProvider extends SQLProvider {

    private HikariDataSource hikari;

    public MySQLProvider(String mt, String lt) {
        super(mt, lt);

        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {

    }
}
