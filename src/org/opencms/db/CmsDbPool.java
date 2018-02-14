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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Various methods to create DBCP pools.<p>
 *
 * Only JDBC Driver based pools are supported currently. JNDI DataSource
 * based pools might be added probably later.<p>
 *
 * <b>Please note:</b> This class is subject to change in later versions.
 * To obtain information about the connections, please use the
 * {@link org.opencms.db.CmsSqlManager}.<p>
 *
 * @since 6.0.0
 */
public final class CmsDbPool {

    /** Key for number of connection attempts. */
    public static final String KEY_CONNECT_ATTEMTS = "connects";

    /** Key for connection waiting. */
    public static final String KEY_CONNECT_WAITS = "wait";

    /** Prefix for database keys. */
    public static final String KEY_DATABASE = "db.";

    /** Key for the database name. */
    public static final String KEY_DATABASE_NAME = KEY_DATABASE + "name";

    /** Key for the pool id. */
    public static final String KEY_DATABASE_POOL = KEY_DATABASE + "pool";

    /** Key for statement pooling. */
    public static final String KEY_DATABASE_STATEMENTS = KEY_DATABASE + "statements";

    /** Key for the entity manager pool size. */
    public static final String KEY_ENTITY_MANAGER_POOL_SIZE = "entityMangerPoolSize";

    /** Key for jdbc driver. */
    public static final String KEY_CONNECTION_PROPERTIES = "connectionProperties";

    /** Key for jdbc driver. */
    public static final String KEY_JDBC_DRIVER = "jdbcDriver";

    /** Key for jdbc url. */
    public static final String KEY_JDBC_URL = "jdbcUrl";

    /** Key for jdbc url params. */
    public static final String KEY_JDBC_URL_PARAMS = KEY_JDBC_URL + ".params";

    /** Key for maximum active connections. */
    public static final String KEY_MAX_ACTIVE = "maxActive";

    /** Key for maximum idle connections. */
    public static final String KEY_MAX_IDLE = "maxIdle";

    /** Key for maximum wait time. */
    public static final String KEY_MAX_WAIT = "maxWait";

    /** Key for minimum idle time before a connection is subject to an eviction test. */
    public static final String KEY_MIN_EVICTABLE_IDLE_TIME = "minEvictableIdleTime";

    /** Key for minimum number of connections kept open. */
    public static final String KEY_MIN_IDLE = "minIdle";

    /** Key for number of tested connections per run. */
    public static final String KEY_NUM_TESTS_PER_EVICTION_RUN = "numTestsPerEvictionRun";

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

    /** Key for pooling flag. */
    public static final String KEY_POOLING = "pooling";

    /** Key for test on borrow flag. */
    public static final String KEY_TEST_ON_BORROW = "testOnBorrow";

    /** Key for test query. */
    public static final String KEY_TEST_QUERY = "testQuery";

    /** Key for test while idle flag. */
    public static final String KEY_TEST_WHILE_IDLE = "testWhileIdle";

    /** Key for time between two eviction runs. */
    public static final String KEY_TIME_BETWEEN_EVICTION_RUNS = "timeBetweenEvictionRuns";

    /** Key for user name. */
    public static final String KEY_USERNAME = "user";

    /** Key for "when pool exhausted" action. */
    public static final String KEY_WHEN_EXHAUSTED_ACTION = "whenExhaustedAction";

    /** The name of the opencms default pool. */
    public static final String OPENCMS_DEFAULT_POOL_NAME = "default";

    /** The default OpenCms JDBC pool URL. */
    public static final String OPENCMS_DEFAULT_POOL_URL = "opencms:default";

    /** The prefix used for opencms JDBC pools. */
    public static final String OPENCMS_URL_PREFIX = "opencms:";

    /**
     * Default constructor.<p>
     *
     * Nobody is allowed to create an instance of this class!
     */
    private CmsDbPool() {

        super();
    }

    /**
     * Creates a JDBC DriverManager based DBCP connection pool.<p>
     *
     * @param config the configuration (opencms.properties)
     * @param key the key of the database pool in the configuration
     * @return String the URL to access the created DBCP pool
     * @throws Exception if the pool could not be initialized
     */
    public static HikariDataSource createDriverManagerConnectionPool(CmsParameterConfiguration config, String key)
    throws Exception {
        
        final String dbPoolName = KEY_DATABASE_POOL + '.' + key + '.';

        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.get(dbPoolName + KEY_JDBC_DRIVER);
        String jdbcUrl = config.get(dbPoolName + KEY_JDBC_URL);
        String jdbcUrlParams = config.get(dbPoolName + KEY_JDBC_URL_PARAMS);
        String username = config.get(dbPoolName + KEY_USERNAME);
        username = OpenCms.getCredentialsResolver().resolveCredential(I_CmsCredentialsResolver.DB_USER, username);
        String password = config.get(dbPoolName + KEY_PASSWORD);
        password = OpenCms.getCredentialsResolver().resolveCredential(I_CmsCredentialsResolver.DB_PASSWORD, password);
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        
        String poolUrl = config.get(dbPoolName + KEY_POOL_URL);
        // initialize a connection factory to make the DriverManager taking connections from the pool
        if (jdbcUrlParams != null) {
            jdbcUrl += jdbcUrlParams;
        }
        int maxActive = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_MAX_ACTIVE, 50);
        
        // create an instance of the JDBC driver
        Class.forName(jdbcDriver).newInstance();


        
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName(jdbcDriver);
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setPoolName(dbPoolName);
        cfg.setMaximumPoolSize(maxActive);
        
        // initialize a new pooling driver using the pool
        HikariDataSource datasource = new HikariDataSource(cfg);

        boolean connect = false;
        int connectionTests = 0;

        // try to connect once to the database to ensure it can be connected to at all
        // if the conection cannot be established, multiple attempts will be done to connect
        // just in cast the database was not fast enough to start before OpenCms was started
        do {
            Connection con = null;
            try {
                // try to connect
                con = datasource.getConnection();
                connect = true;
            } catch (Exception e) {
                // connection failed, increase attempts, sleept for some seconds and log a message
                connectionTests++;
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_WAIT_FOR_DB_4,
                            new Object[] {
                                poolUrl,
                                jdbcUrl,
                                new Integer(connectionTests),
                                new Integer(5000)}));
                }
                Thread.sleep(5000);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } while (!connect && (connectionTests < 10));

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_JDBC_POOL_2, poolUrl, jdbcUrl));
        }
        return datasource;
    }

    /**
     * Returns the database pool name for a given configuration key.<p>
     *
     * @param configuration the configuration
     * @param key a db pool configuration key
     * @return the database pool name
     */
    public static String getDbPoolName(CmsParameterConfiguration configuration, String key) {

        return configuration.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_POOL_URL);
    }

    /**
     * Returns a list of available database pool names.<p>
     *
     * @param configuration the configuration to read the pool names from
     *
     * @return a list of database pool names
     */
    public static List<String> getDbPoolUrls(CmsParameterConfiguration configuration) {

        List<String> dbPoolNames = new ArrayList<String>();
        List<String> driverPoolNames = configuration.getList(CmsDriverManager.CONFIGURATION_DB + ".pools");

        for (String driverPoolName : driverPoolNames) {
            dbPoolNames.add(getDbPoolName(configuration, driverPoolName));
        }
        return dbPoolNames;
    }

    /**
     * Returns the name of the default database connection pool.<p>
     *
     * @return the name of the default database connection pool
     */
    public static String getDefaultDbPoolName() {

        return OPENCMS_DEFAULT_POOL_NAME;
    }
}
