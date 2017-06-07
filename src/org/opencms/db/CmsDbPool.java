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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

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

    /** This prefix is required to make the JDBC DriverManager return pooled DBCP connections. */
    public static final String DBCP_JDBC_URL_PREFIX = "jdbc:apache:commons:dbcp:";

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
    public static PoolingDriver createDriverManagerConnectionPool(CmsParameterConfiguration config, String key)
    throws Exception {

        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_JDBC_DRIVER);
        String jdbcUrl = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_JDBC_URL);
        String jdbcUrlParams = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_JDBC_URL_PARAMS);
        int maxActive = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_MAX_ACTIVE, 10);
        int maxWait = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_MAX_WAIT, 2000);
        int maxIdle = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_MAX_IDLE, 5);
        int minEvictableIdleTime = config.getInteger(
            KEY_DATABASE_POOL + '.' + key + '.' + KEY_MIN_EVICTABLE_IDLE_TIME,
            1800000);
        int minIdle = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_MIN_IDLE, 0);
        int numTestsPerEvictionRun = config.getInteger(
            KEY_DATABASE_POOL + '.' + key + '.' + KEY_NUM_TESTS_PER_EVICTION_RUN,
            3);
        int timeBetweenEvictionRuns = config.getInteger(
            KEY_DATABASE_POOL + '.' + key + '.' + KEY_TIME_BETWEEN_EVICTION_RUNS,
            3600000);
        String testQuery = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_TEST_QUERY);
        String username = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_USERNAME);
        username = OpenCms.getCredentialsResolver().resolveCredential(I_CmsCredentialsResolver.DB_USER, username);
        String password = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_PASSWORD);
        password = OpenCms.getCredentialsResolver().resolveCredential(I_CmsCredentialsResolver.DB_PASSWORD, password);
        String poolUrl = config.get(KEY_DATABASE_POOL + '.' + key + '.' + KEY_POOL_URL);
        String whenExhaustedActionValue = config.get(
            KEY_DATABASE_POOL + '.' + key + '.' + KEY_WHEN_EXHAUSTED_ACTION).trim();
        byte whenExhaustedAction = 0;
        boolean testOnBorrow = Boolean.valueOf(
            config.getString(KEY_DATABASE_POOL + '.' + key + '.' + KEY_TEST_ON_BORROW, "false").trim()).booleanValue();
        boolean testWhileIdle = Boolean.valueOf(
            config.getString(KEY_DATABASE_POOL + '.' + key + '.' + KEY_TEST_WHILE_IDLE, "false").trim()).booleanValue();

        if ("block".equalsIgnoreCase(whenExhaustedActionValue)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        } else if ("fail".equalsIgnoreCase(whenExhaustedActionValue)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
        } else if ("grow".equalsIgnoreCase(whenExhaustedActionValue)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        } else {
            whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
        }

        if ("".equals(testQuery)) {
            testQuery = null;
        }

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        // read the values of the statement pool configuration specified by the given key
        boolean poolingStmts = Boolean.valueOf(
            config.getString(
                KEY_DATABASE_STATEMENTS + '.' + key + '.' + KEY_POOLING,
                CmsStringUtil.TRUE).trim()).booleanValue();
        int maxActiveStmts = config.getInteger(KEY_DATABASE_STATEMENTS + '.' + key + '.' + KEY_MAX_ACTIVE, 25);
        int maxWaitStmts = config.getInteger(KEY_DATABASE_STATEMENTS + '.' + key + '.' + KEY_MAX_WAIT, 250);
        int maxIdleStmts = config.getInteger(KEY_DATABASE_STATEMENTS + '.' + key + '.' + KEY_MAX_IDLE, 15);
        String whenStmtsExhaustedActionValue = config.get(
            KEY_DATABASE_STATEMENTS + '.' + key + '.' + KEY_WHEN_EXHAUSTED_ACTION);
        byte whenStmtsExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        if (whenStmtsExhaustedActionValue != null) {
            whenStmtsExhaustedActionValue = whenStmtsExhaustedActionValue.trim();
            whenStmtsExhaustedAction = ("block".equalsIgnoreCase(whenStmtsExhaustedActionValue))
            ? GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK
            : ("fail".equalsIgnoreCase(whenStmtsExhaustedActionValue))
            ? GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL
            : GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        }

        int connectionAttempts = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_CONNECT_ATTEMTS, 10);
        int connetionsWait = config.getInteger(KEY_DATABASE_POOL + '.' + key + '.' + KEY_CONNECT_WAITS, 5000);

        // create an instance of the JDBC driver
        Class.forName(jdbcDriver).newInstance();

        // initialize a keyed object pool to store connections
        GenericObjectPool connectionPool = new GenericObjectPool(null);

        /* Abandoned pool configuration:
         *
         * In case the systems encounters "pool exhaustion" (runs out of connections),
         * comment the above line with "new GenericObjectPool(null)" and uncomment the
         * 5 lines below. This will generate an "abandoned pool" configuration that logs
         * abandoned connections to the System.out. Unfortunatly this code is deprecated,
         * so to avoid code warnings it's also disabled here.
         * Tested with commons-pool v 1.2.
         */

        //        AbandonedConfig abandonedConfig = new AbandonedConfig();
        //        abandonedConfig.setLogAbandoned(true);
        //        abandonedConfig.setRemoveAbandoned(true);
        //        abandonedConfig.setRemoveAbandonedTimeout(5);
        //        GenericObjectPool connectionPool = new AbandonedObjectPool(null, abandonedConfig);
        // initialize an object pool to store connections
        connectionPool.setMaxActive(maxActive);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMinIdle(minIdle);
        connectionPool.setMaxWait(maxWait);
        connectionPool.setWhenExhaustedAction(whenExhaustedAction);

        if (testQuery != null) {
            connectionPool.setTestOnBorrow(testOnBorrow);
            connectionPool.setTestWhileIdle(testWhileIdle);
            connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRuns);
            connectionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
            connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTime);
        }

        // initialize a connection factory to make the DriverManager taking connections from the pool
        if (jdbcUrlParams != null) {
            jdbcUrl += jdbcUrlParams;
        }

        Properties connectionProperties = config.getPrefixedProperties(
            KEY_DATABASE_POOL + '.' + key + '.' + KEY_CONNECTION_PROPERTIES);
        connectionProperties.put(KEY_USERNAME, username);
        connectionProperties.put(KEY_PASSWORD, password);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, connectionProperties);

        // Set up statement pool, if desired
        GenericKeyedObjectPoolFactory statementFactory = null;
        if (poolingStmts) {
            statementFactory = new GenericKeyedObjectPoolFactory(
                null,
                maxActiveStmts,
                whenStmtsExhaustedAction,
                maxWaitStmts,
                maxIdleStmts);
        }

        // initialize a factory to obtain pooled connections and prepared statements
        new PoolableConnectionFactory(connectionFactory, connectionPool, statementFactory, testQuery, false, true);

        // initialize a new pooling driver using the pool
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(poolUrl, connectionPool);

        Connection con = null;
        boolean connect = false;
        int connectionTests = 0;

        // try to connect once to the database to ensure it can be connected to at all
        // if the conection cannot be established, multiple attempts will be done to connect
        // just in cast the database was not fast enough to start before OpenCms was started

        do {
            try {
                // try to connect
                con = connectionFactory.createConnection();
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
                                new Integer(connetionsWait)}));
                }
                Thread.sleep(connetionsWait);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } while (!connect && (connectionTests < connectionAttempts));

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_JDBC_POOL_2, poolUrl, jdbcUrl));
        }
        return driver;
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
