/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsCredentialsResolver;
import org.opencms.util.CmsStringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database connection pool class using HikariCP.
 */
public final class CmsDbPoolV11 {

    /** Prefix for database keys. */
    public static final String KEY_DATABASE = "db.";

    /** Key for the database name. */
    public static final String KEY_DATABASE_NAME = KEY_DATABASE + "name";

    /** Key for the pool id. */
    public static final String KEY_DATABASE_POOL = KEY_DATABASE + "pool";

    /** Key for number of connection attempts. */
    public static final String KEY_CONNECT_ATTEMTS = "connects";

    /** Key for connection waiting. */
    public static final String KEY_CONNECT_WAITS = "wait";

    /** Key for the entity manager pool size. */
    public static final String KEY_ENTITY_MANAGER_POOL_SIZE = "entityMangerPoolSize";

    /** Key for jdbc driver. */
    public static final String KEY_JDBC_DRIVER = "jdbcDriver";

    /** Key for jdbc url. */
    public static final String KEY_JDBC_URL = "jdbcUrl";

    /** Key for jdbc url params. */
    public static final String KEY_JDBC_URL_PARAMS = KEY_JDBC_URL + ".params";

    /** Key for database password. */
    public static final String KEY_PASSWORD = "password";

    /** Key for default. */
    public static final String KEY_POOL_DEFAULT = "default";

    /** Key for pool url. */
    public static final String KEY_POOL_URL = "poolUrl";

    /** Key for pool user. */
    public static final String KEY_POOL_USER = "user";

    /** Key for vfs pool. */
    public static final String KEY_POOL_VFS = "vfs";

    /** Key for user name. */
    public static final String KEY_USERNAME = "user";

    /** The name of the opencms default pool. */
    public static final String OPENCMS_DEFAULT_POOL_NAME = "default";

    /** The default OpenCms JDBC pool URL. */
    public static final String OPENCMS_DEFAULT_POOL_URL = "opencms:default";

    /** The prefix used for opencms JDBC pools. */
    public static final String OPENCMS_URL_PREFIX = "opencms:";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDbPoolV11.class);

    /** Map of default test queries. */
    private static Map<String, String> testQueries = Maps.newHashMap();

    static {
        testQueries.put("com.ibm.db2.jcc.DB2Driver", "SELECT 1 FROM SYSIBM.SYSDUMMY1");
        testQueries.put("net.sourceforge.jtds.jdbc.Driver", "SELECT 1");
        testQueries.put("oracle.jdbc.driver.OracleDriver", "SELECT 1 FROM DUAL");
        testQueries.put("com.ibm.as400.access.AS400JDBCDriver", "SELECT NOW()");
    }

    /** The opencms pool url. */
    private String m_poolUrl;

    /** The HikariCP data source. */
    private HikariDataSource m_dataSource;

    /**
     * Default constructor.<p>
     *
     * @param config the OpenCms configuration (opencms.properties)
     * @param key the name of the pool (without the opencms: prefix)
     *
     * @throws Exception if something goes wrong
     */
    public CmsDbPoolV11(CmsParameterConfiguration config, String key)
    throws Exception {

        HikariConfig hikariConf = createHikariConfig(config, key);
        m_poolUrl = OPENCMS_URL_PREFIX + key;
        m_dataSource = new HikariDataSource(hikariConf);
        Connection con = null;
        boolean connect = false;
        int connectionTests = 0;
        int connectionAttempts = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_CONNECT_ATTEMTS, 10);
        int connectionsWait = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_CONNECT_WAITS, 5000);

        // try to connect once to the database to ensure it can be connected to at all
        // if the conection cannot be established, multiple attempts will be done to connect
        // just in cast the database was not fast enough to start before OpenCms was started

        do {
            try {
                // try to connect
                con = m_dataSource.getConnection();
                connect = true;
            } catch (Exception e) {
                // connection failed, increase attempts, sleept for some seconds and log a message
                connectionTests++;
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_WAIT_FOR_DB_4,
                            new Object[] {
                                getPoolUrl(),
                                m_dataSource.getJdbcUrl(),
                                Integer.valueOf(connectionTests),
                                Integer.valueOf(connectionsWait)}));
                }
                Thread.sleep(connectionsWait);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } while (!connect && (connectionTests < connectionAttempts));

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_JDBC_POOL_2, getPoolUrl(), m_dataSource.getJdbcUrl()));
        }
    }

    /**
     * Creates the HikariCP configuration based on the configuration of a pool defined in opencms.properties.
     *
     * @param config the configuration object with the properties
     * @param key the pool name (without the opencms prefix)
     *
     * @return the HikariCP configuration for the pool
     */
    public static HikariConfig createHikariConfig(CmsParameterConfiguration config, String key) {

        Map<String, String> poolMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String suffix = getPropertyRelativeSuffix(KEY_DATABASE_POOL + "." + key, entry.getKey());
            if ((suffix != null) && !CmsStringUtil.isEmptyOrWhitespaceOnly(entry.getValue())) {
                String value = entry.getValue().trim();
                poolMap.put(suffix, value);
            }
        }

        // these are for backwards compatibility , all other properties not of the form db.pool.poolname.v11..... are ignored
        String jdbcUrl = poolMap.get(KEY_JDBC_URL);
        String params = poolMap.get(KEY_JDBC_URL_PARAMS);
        String driver = poolMap.get(KEY_JDBC_DRIVER);
        String user = poolMap.get(KEY_USERNAME);
        String password = poolMap.get(KEY_PASSWORD);
        String poolName = OPENCMS_URL_PREFIX + key;

        if ((params != null) && (jdbcUrl != null)) {
            jdbcUrl += params;
        }

        Properties hikariProps = new Properties();

        if (jdbcUrl != null) {
            hikariProps.put("jdbcUrl", jdbcUrl);
        }
        if (driver != null) {
            hikariProps.put("driverClassName", driver);
        }
        if (user != null) {
            user = OpenCms.getCredentialsResolver().resolveCredential(I_CmsCredentialsResolver.DB_USER, user);
            hikariProps.put("username", user);
        }
        if (password != null) {
            password = OpenCms.getCredentialsResolver().resolveCredential(
                I_CmsCredentialsResolver.DB_PASSWORD,
                password);
            hikariProps.put("password", password);
        }

        hikariProps.put("maximumPoolSize", "30");

        // Properties of the form db.pool.poolname.v11.<foo> are directly passed to HikariCP as <foo>
        for (Map.Entry<String, String> entry : poolMap.entrySet()) {
            String suffix = getPropertyRelativeSuffix("v11", entry.getKey());
            if (suffix != null) {
                hikariProps.put(suffix, entry.getValue());
            }
        }

        String configuredTestQuery = (String)(hikariProps.get("connectionTestQuery"));
        String testQueryForDriver = testQueries.get(driver);
        if ((testQueryForDriver != null) && CmsStringUtil.isEmptyOrWhitespaceOnly(configuredTestQuery)) {
            hikariProps.put("connectionTestQuery", testQueryForDriver);
        }
        hikariProps.put("registerMbeans", "true");
        HikariConfig result = new HikariConfig(hikariProps);

        result.setPoolName(poolName.replace(":", "_"));
        return result;
    }

    /**
     * Returns the name of the default database connection pool.<p>
     *
     * @return the name of the default database connection pool
     */
    public static String getDefaultDbPoolName() {

        return OPENCMS_DEFAULT_POOL_NAME;
    }

    /**
     * If str starts with prefix + '.', return the remaining part, otherwise return null.<p>
     *
     * @param prefix the prefix
     * @param str the string to remove the prefix from
     * @return str with the prefix removed, or null if it didn't start with prefix + '.'
     */
    public static String getPropertyRelativeSuffix(String prefix, String str) {

        String realPrefix = prefix + ".";
        if (str.startsWith(realPrefix)) {
            return str.substring(realPrefix.length());
        }
        return null;
    }

    /**
     * Closes the pool.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void close() throws Exception {

        m_dataSource.close();
    }

    /**
     * Returns the number of active connections.<p>
     *
     * @return the number of active connections
     */
    public int getActiveConnections() {

        return m_dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    /**
     * Gets a database connection from the pool.<p>
     *
     * @return the database connection
     * @throws SQLException if something goes wrong
     */
    public Connection getConnection() throws SQLException {

        try {
            return m_dataSource.getConnection();
        } catch (SQLTransientConnectionException | SQLTimeoutException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the number of idle connections.<p>
     *
     * @return the number of idle connections
     */
    public int getIdleConnections() {

        return m_dataSource.getHikariPoolMXBean().getIdleConnections();
    }

    /**
     * Gets the pool url.<p>
     *
     * @return the pool url
     */
    public String getPoolUrl() {

        return m_poolUrl;
    }
}
