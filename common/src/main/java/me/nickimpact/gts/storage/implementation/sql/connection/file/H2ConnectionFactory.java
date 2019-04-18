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

package me.nickimpact.gts.storage.implementation.sql.connection.file;

import me.nickimpact.gts.api.dependencies.Dependency;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.dependencies.classloader.IsolatedClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.function.Function;

public class H2ConnectionFactory extends FlatfileConnectionFactory {

    private final Driver driver;
    private NonClosableConnection connection;

    public H2ConnectionFactory(IGTSPlugin plugin, Path file) {
        super(file);

        IsolatedClassLoader classLoader = plugin.getDependencyManager().obtainClassLoaderWith(EnumSet.of(Dependency.H2_DRIVER));
        try {
            Class<?> driverClass = classLoader.loadClass("org.h2.Driver");
            Method loadMethod = driverClass.getMethod("load");
            this.driver = (Driver) loadMethod.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getImplementationName() {
        return "H2";
    }

    @Override
    public void shutdown() throws Exception {
        if(this.connection != null) {
            this.connection.shutdown();
        }
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace("'", "");
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            Connection connection = this.driver.connect("jdbc:h2:" + file.toAbsolutePath(), new Properties());
            if (connection != null) {
                this.connection = NonClosableConnection.wrap(connection);
            }
        }

        if (this.connection == null) {
            throw new SQLException("Unable to get a connection.");
        }

        return this.connection;
    }

    @Override
    protected Path getWriteFile() {
        // h2 appends this to the end of the database file
        return super.file.getParent().resolve(super.file.getFileName().toString() + ".mv.db");
    }
}
