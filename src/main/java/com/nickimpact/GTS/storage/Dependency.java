package com.nickimpact.GTS.storage;

/**
 * Created by Nick on 2/7/2017.
 */
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Dependency {

    MYSQL_DRIVER("https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar", "5.1.6", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource"),
    POSTGRESQL_DRIVER("https://repo1.maven.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar", "9.4.1212", "org.postgresql.ds.PGSimpleDataSource"),
    H2_DRIVER("https://repo1.maven.org/maven2/com/h2database/h2/1.4.194/h2-1.4.194.jar", "1.4.194", "org.h2.Driver"),
    SQLITE_DRIVER("https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.15.1/sqlite-jdbc-3.15.1.jar", "3.15.1", "org.sqlite.JDBC"),
    HIKARI("https://repo1.maven.org/maven2/com/zaxxer/HikariCP/2.5.1/HikariCP-2.5.1.jar", "2.5.1", "com.zaxxer.hikari.HikariDataSource"),
    SLF4J_SIMPLE("https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.9/slf4j-simple-1.7.9.jar", "1.7.9", "org.slf4j.impl.SimpleLoggerFactory"),
    SLF4J_API("https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.9/slf4j-api-1.7.9.jar", "1.7.9", "org.slf4j.helpers.BasicMarkerFactory"),
    MONGODB_DRIVER("https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.4.1/mongo-java-driver-3.4.1.jar", "3.4.1", "com.mongodb.Mongo"),
    JEDIS("https://github.com/lucko/jedis/releases/download/jedis-2.9.1-shaded/jedis-2.9.1-shaded.jar", "2.9.1-shaded", "redis.clients.jedis.shaded.Jedis");

    private final String url;
    private final String version;
    private final String testClass;

}