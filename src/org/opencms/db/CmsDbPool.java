/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbPool.java,v $
 * Date   : $Date: 2003/08/25 09:10:43 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import source.org.apache.java.util.Configurations;

/**
 * Various methods to create DBCP pools.<p>
 * 
 * Only JDBC Driver based pools are supported currently. JNDI DataSource 
 * based pools might be added probably later.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.8 $ $Date: 2003/08/25 09:10:43 $
 * @since 5.1
 */
public final class CmsDbPool extends Object {

    /**
     * This prefix is required to make the JDBC DriverManager return pooled DBCP connections.
     */
    public static final String C_DBCP_JDBC_URL_PREFIX = "jdbc:apache:commons:dbcp:";

    public static final String C_KEY_DATABASE = "db.";
    public static final String C_KEY_DATABASE_NAME = C_KEY_DATABASE + "name";
    public static final String C_KEY_DATABASE_POOL = C_KEY_DATABASE + "pool";
    public static final String C_KEY_DATABASE_STATEMENTS = C_KEY_DATABASE + "statements";
    public static final String C_KEY_POOL_DEFAULT = "default";
    public static final String C_KEY_POOL_USER = "user";
    public static final String C_KEY_POOL_VFS = "vfs";

    protected static final String C_KEY_JDBC_DRIVER = "jdbcDriver";
    protected static final String C_KEY_JDBC_URL = "jdbcUrl";
    protected static final String C_KEY_MAX_ACTIVE = "maxActive";
    protected static final String C_KEY_MAX_WAIT = "maxWait";
    protected static final String C_KEY_MAX_IDLE = "maxIdle";
    protected static final String C_KEY_TEST_QUERY = "testQuery";
    protected static final String C_KEY_USERNAME = "user";
    protected static final String C_KEY_PASSWORD = "password";
    protected static final String C_KEY_POOL_URL = "poolUrl";
    protected static final String C_KEY_LOG_ABANDONED = "logAbandoned";
    protected static final String C_KEY_REMOVE_ABANDONED = "removeAbandoned";
    protected static final String C_KEY_REMOVE_ABANDONED_TIMEOUT = "removeAbandonedTimeout";
    protected static final String C_KEY_WHEN_EXHAUSTED_ACTION = "whenExhaustedAction";
    protected static final String C_KEY_TEST_ON_BORROW = "testOnBorrow";
    protected static final String C_KEY_POOLING = "pooling";

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
    public static final String createDriverManagerConnectionPool(Configurations config, String key) throws Exception {
        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_JDBC_DRIVER);
        String jdbcUrl = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_JDBC_URL);
        int maxActive = config.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_ACTIVE, 10);
        int maxWait = config.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_WAIT, 2000);
        int maxIdle = config.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_IDLE, 5);
        String testQuery = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_TEST_QUERY);
        String username = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_USERNAME);
        String password = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_PASSWORD);
        String poolUrl = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_POOL_URL);
        String dbName = config.getString(C_KEY_DATABASE_NAME).trim();
        String whenExhaustedActionValue = config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_WHEN_EXHAUSTED_ACTION).trim();
        byte whenExhaustedAction = 0;
        boolean testOnBorrow = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_TEST_ON_BORROW).trim());

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
        boolean poolingStmts = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_POOLING, "true").trim());
        int maxActiveStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_ACTIVE, 25);
        int maxWaitStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_WAIT, 250);
        int maxIdleStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_IDLE, 15);
        String whenStmtsExhaustedActionValue = config.getString(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_WHEN_EXHAUSTED_ACTION);
        byte whenStmtsExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        if (whenStmtsExhaustedActionValue != null) {
            whenStmtsExhaustedActionValue = whenStmtsExhaustedActionValue.trim();
            whenStmtsExhaustedAction = 
            ("block".equalsIgnoreCase(whenStmtsExhaustedActionValue)) ? GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK
            : ("fail".equalsIgnoreCase(whenStmtsExhaustedActionValue))? GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL
            : GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        }  
            
        // create an instance of the JDBC driver
        Class.forName(jdbcDriver).newInstance();

        // initialize a keyed object pool to store connections
        GenericObjectPool connectionPool = null;

        if ("mysql".equalsIgnoreCase(dbName.trim())) {
            // read the additional config values
            boolean logAbandoned = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_LOG_ABANDONED).trim());
            boolean removeAbandoned = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_REMOVE_ABANDONED).trim());
            int removeAbandonedTimeout = config.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_REMOVE_ABANDONED_TIMEOUT, 300);

            // settings to handle abandoned db connections in case of a MySQL server
            AbandonedConfig abandonedConfig = new AbandonedConfig();
            abandonedConfig.setLogAbandoned(logAbandoned);
            abandonedConfig.setRemoveAbandoned(removeAbandoned);
            abandonedConfig.setRemoveAbandonedTimeout(removeAbandonedTimeout);

            connectionPool = new AbandonedObjectPool(null, abandonedConfig);
        } else {
            // all other servers use a generic pool
            connectionPool = new GenericObjectPool(null);
        }

        // initialize an object pool to store connections
        connectionPool.setMaxActive(maxActive);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMaxWait(maxWait);
        connectionPool.setWhenExhaustedAction(whenExhaustedAction);

        connectionPool.setTestOnBorrow(testOnBorrow && (testQuery != null));
        connectionPool.setTestWhileIdle(true);

        // initialize a connection factory to make the DriverManager taking connections from the pool
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password);

        // initialize a keyed object pool to store PreparedStatements
        // i still have to rtfm how pooling of PreparedStatements with DBCP works
        //KeyedObjectPoolFactory statementFactory = new StackKeyedObjectPoolFactory();
        //KeyedObjectPoolFactory statementFactory = new StackKeyedObjectPoolFactory(null, 5000, 0);

        // Set up statement pool, if desired
        GenericKeyedObjectPoolFactory statementFactory = null;
        if (poolingStmts) {
            statementFactory = new GenericKeyedObjectPoolFactory(null, maxActiveStmts, whenStmtsExhaustedAction, maxWaitStmts, maxIdleStmts);
        }
        
        // initialize a factory to obtain pooled connections and prepared statements
        new PoolableConnectionFactory(connectionFactory, connectionPool, statementFactory, testQuery, false, true);

        // initialize a new pooling driver using the pool
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(poolUrl, connectionPool);

        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Init. JDBC pool      : " + poolUrl + " (" + jdbcUrl + ")");
        }

        return poolUrl;
    } 

}
