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

/**
 * Connection pooling is now done by CmsDbPoolV11, and everything except the constants
 * have been removed from this class. Once JPA integration is removed, this class can be deleted.
 *
 * @since 6.0.0
 */
@Deprecated
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
     * Hidden constructor.<p>
     */
    private CmsDbPool() {
        // hidden constructor
    }

}
