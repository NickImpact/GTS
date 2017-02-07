package com.nickimpact.GTS.Storage;

import com.nickimpact.GTS.GTS;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

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
public class H2Provider extends SQLDatabase {

    private static Method ADD_URL_METHOD;
    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private HikariDataSource hikari;
    private String dbName;

    public H2Provider(){
        try {
            loadJar(downloadDependencies(), Dependency.H2_DRIVER.getTestClass());
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File downloadDependencies() throws Exception {
        String name = Dependency.H2_DRIVER.name().toLowerCase() + "-" + Dependency.H2_DRIVER.getVersion() + ".jar";

        File dir = new File(GTS.getInstance().getConfigDir() + "/storage/");
        dir.mkdirs();
        File file = new File(GTS.getInstance().getConfigDir() + "/storage/", name);
        if(file.exists()){
            return file;
        }

        // H2 Driver Jar doesn't exist, let's download one
        URL url = new URL(Dependency.H2_DRIVER.getUrl());

        GTS.getInstance().getLogger().info("H2 Dependency '" + name + "' not found, attempting to download it..");
        try(InputStream in = url.openStream()){
            Files.copy(in, file.toPath());
        }

        if(!file.exists()){
            throw new IllegalStateException("File not present. - " + file.toString());
        }

        GTS.getInstance().getLogger().info("H2 Dependency successfully downloaded!");
        return file;
    }

    private static void loadJar(File file, String baseClass) throws Exception{
        URLClassLoader classLoader = (URLClassLoader) GTS.getInstance().getClass().getClassLoader();

        ADD_URL_METHOD.invoke(classLoader, file.toURI().toURL());
        classLoader.loadClass(baseClass).newInstance();
    }

    @Override
    void init() throws Exception {
        HikariConfig config = new HikariConfig();

        this.dbName = GTS.getInstance().getConfig().getDatabase();

        config.setMaximumPoolSize(10);
        config.setPoolName("GTS");
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("URL", "jdbc:h2:file:" + GTS.getInstance().getConfigDir() + "/storage/GTS");
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10)); // 10000
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(5)); // 5000
        config.setValidationTimeout(TimeUnit.SECONDS.toMillis(3)); // 3000
        config.setInitializationFailFast(true);
        config.setConnectionTestQuery("/* GTS ping */ SELECT 1");

        this.hikari = new HikariDataSource(config);
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
