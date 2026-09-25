/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.tools.storage;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Shared infrastructure for standalone storage command line tools.<p>
 */
final class CmsStorageToolSupport {

    /**
     * Database connection configuration.<p>
     */
    static class DatabaseConfiguration {

        /** JDBC driver class. */
        String m_driverClass;

        /** JDBC driver class loader. */
        ClassLoader m_driverClassLoader;

        /** JDBC URL. */
        String m_jdbcUrl;

        /** Password. */
        String m_password;

        /** User name. */
        String m_user;
    }

    /**
     * SQL manager wrapper which uses direct JDBC connections instead of OpenCms pools.<p>
     */
    static class ToolSqlManager extends org.opencms.db.generic.CmsSqlManager {

        /** Connection supplier. */
        private Supplier<Connection> m_connectionSupplier;

        /** Current transaction connection. */
        private ThreadLocal<Connection> m_currentConnection = new ThreadLocal<>();

        /** SQL manager delegate for configured query texts. */
        private org.opencms.db.generic.CmsSqlManager m_delegate;

        /**
         * Creates a tool SQL manager.<p>
         *
         * @param delegate the configured SQL manager
         * @param connectionSupplier the connection supplier
         */
        ToolSqlManager(org.opencms.db.generic.CmsSqlManager delegate, Supplier<Connection> connectionSupplier) {

            super(Arrays.asList(STORAGE_QUERY_PROPERTIES));
            m_delegate = delegate;
            m_connectionSupplier = connectionSupplier;
        }

        /**
         * @see org.opencms.db.generic.CmsSqlManager#closeAll(org.opencms.db.CmsDbContext, java.sql.Connection, java.sql.Statement, java.sql.ResultSet)
         */
        @Override
        public void closeAll(CmsDbContext dbc, Connection conn, Statement stmt, ResultSet res) {

            Connection currentConnection = m_currentConnection.get();
            m_delegate.closeAll(dbc, (conn == currentConnection) ? null : conn, stmt, res);
        }

        /**
         * @see org.opencms.db.generic.CmsSqlManager#getConnection(org.opencms.db.CmsDbContext)
         */
        @Override
        public Connection getConnection(CmsDbContext dbc) throws SQLException {

            Connection currentConnection = m_currentConnection.get();
            if (currentConnection != null) {
                return currentConnection;
            }
            return m_connectionSupplier.get();
        }

        /**
         * @see org.opencms.db.generic.CmsSqlManager#getPreparedStatement(java.sql.Connection, java.lang.String)
         */
        @Override
        public PreparedStatement getPreparedStatement(Connection conn, String queryKey) throws SQLException {

            return m_delegate.getPreparedStatement(conn, queryKey);
        }

        /**
         * Clears the current transaction connection.<p>
         */
        void clearCurrentConnection() {

            m_currentConnection.remove();
        }

        /**
         * Sets the current transaction connection.<p>
         *
         * @param connection the current connection
         */
        void setCurrentConnection(Connection connection) {

            m_currentConnection.set(connection);
        }
    }

    /** Additional SQL query properties needed by storage code. */
    static final String STORAGE_QUERY_PROPERTIES = "org/opencms/db/generic/storage.properties";

    /**
     * Hidden constructor.<p>
     */
    private CmsStorageToolSupport() {

        // utility class
    }

    /**
     * Adds all jar files from a driver directory if it exists.<p>
     *
     * @param driverJars the driver jar list to update
     * @param directory the directory
     */
    static void addDriverDirectory(List<Path> driverJars, Path directory) {

        File file = directory.toFile();
        File[] jars = file.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) {
            return;
        }
        for (File jar : jars) {
            driverJars.add(jar.toPath());
        }
    }

    /**
     * Creates the configured SQL manager and loads storage query definitions.<p>
     *
     * @param properties the OpenCms properties
     *
     * @return the SQL manager
     */
    static org.opencms.db.generic.CmsSqlManager createSqlManager(CmsParameterConfiguration properties) {

        String sqlManagerClass = properties.getString("db.vfs.sqlmanager", null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(sqlManagerClass)) {
            throw new IllegalArgumentException("Missing db.vfs.sqlmanager in opencms.properties.");
        }
        try {
            Class<?> clazz = Class.forName(sqlManagerClass);
            Object result;
            try {
                result = clazz.getConstructor(List.class).newInstance(Arrays.asList(STORAGE_QUERY_PROPERTIES));
            } catch (NoSuchMethodException e) {
                result = clazz.newInstance();
            }
            return (org.opencms.db.generic.CmsSqlManager)result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not create SQL manager " + sqlManagerClass + ".", e);
        }
    }

    /**
     * Initializes OpenCms logging used by storage classes.<p>
     */
    static void initializeOpenCmsLogging() {

        if (org.opencms.main.CmsLog.INIT == null) {
            org.opencms.main.CmsLog.INIT = org.apache.commons.logging.LogFactory.getLog("org.opencms.init");
        }
    }

    /**
     * Opens a JDBC connection.<p>
     *
     * @param configuration the database configuration
     *
     * @return the JDBC connection
     */
    static Connection openConnection(DatabaseConfiguration configuration) {

        try {
            try {
                return DriverManager.getConnection(
                    configuration.m_jdbcUrl,
                    configuration.m_user,
                    configuration.m_password);
            } catch (SQLException e) {
                java.sql.Driver driver = (java.sql.Driver)Class.forName(
                    configuration.m_driverClass,
                    true,
                    configuration.m_driverClassLoader).newInstance();
                Properties properties = new Properties();
                properties.setProperty("user", configuration.m_user);
                properties.setProperty("password", configuration.m_password);
                Connection result = driver.connect(configuration.m_jdbcUrl, properties);
                if (result == null) {
                    throw e;
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads database configuration from opencms.properties.<p>
     *
     * @param properties the OpenCms properties
     * @param driverJars additional driver jars
     *
     * @return the database configuration
     *
     * @throws Exception if loading the JDBC driver fails
     */
    static DatabaseConfiguration readDatabaseConfiguration(CmsParameterConfiguration properties, List<Path> driverJars)
    throws Exception {

        String poolUrl = properties.getString("db.vfs.pool", "opencms:default");
        String poolName = poolUrl.startsWith("opencms:") ? poolUrl.substring("opencms:".length()) : poolUrl;
        String prefix = "db.pool." + poolName + ".";
        DatabaseConfiguration result = new DatabaseConfiguration();
        result.m_driverClass = requireProperty(properties, prefix + "jdbcDriver");
        result.m_driverClassLoader = createDriverClassLoader(driverJars);
        Class.forName(result.m_driverClass, true, result.m_driverClassLoader);
        result.m_jdbcUrl = requireProperty(properties, prefix + "jdbcUrl")
            + properties.getString(prefix + "jdbcUrl.params", "");
        result.m_user = properties.getString(prefix + "user", "");
        result.m_password = properties.getString(prefix + "password", "");
        return result;
    }

    /**
     * Requires a property value.<p>
     *
     * @param properties the properties
     * @param key the key
     *
     * @return the value
     */
    static String requireProperty(CmsParameterConfiguration properties, String key) {

        String result = properties.getString(key, null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return result;
    }

    /**
     * Creates the class loader for JDBC drivers.<p>
     *
     * @param driverJars additional driver jars
     *
     * @return the class loader
     *
     * @throws Exception if a jar URL can not be created
     */
    private static ClassLoader createDriverClassLoader(List<Path> driverJars) throws Exception {

        if (driverJars.isEmpty()) {
            return Thread.currentThread().getContextClassLoader();
        }
        List<URL> urls = new ArrayList<>();
        for (Path driverJar : driverJars) {
            urls.add(driverJar.toUri().toURL());
        }
        URLClassLoader result = new URLClassLoader(
            urls.toArray(new URL[urls.size()]),
            Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(result);
        return result;
    }
}
