/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbPool.java,v $
 * Date   : $Date: 2004/06/07 12:59:51 $
 * Version: $Revision: 1.20 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import org.apache.commons.collections.ExtendedProperties;
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
 * based pools might be added probably later.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.20 $ $Date: 2004/06/07 12:59:51 $
 * @since 5.1
 */
public final class CmsDbPool extends Object {

    /** This prefix is required to make the JDBC DriverManager return pooled DBCP connections.<p> */
    public static final String C_DBCP_JDBC_URL_PREFIX = "jdbc:apache:commons:dbcp:";
    
    /** The default OpenCms JDBC pool URL.<p> */
    public static final String C_OPENCMS_DEFAULT_POOL_URL = "opencms:default";

    /**
     * Prefix for database keys
     */
    public static final String C_KEY_DATABASE = "db.";
    
    /**
     * Key for database name
     */
    public static final String C_KEY_DATABASE_NAME = C_KEY_DATABASE + "name";
    
    /**
     * Key for pool id
     */
    public static final String C_KEY_DATABASE_POOL = C_KEY_DATABASE + "pool";
    
    /**
     * Key for statement pooling
     */
    public static final String C_KEY_DATABASE_STATEMENTS = C_KEY_DATABASE + "statements";

    /**
     * Key for jdbc driver
     */
    public static final String C_KEY_JDBC_DRIVER = "jdbcDriver";
    
    /**
     * Key for jdbc url
     */
    public static final String C_KEY_JDBC_URL = "jdbcUrl";
    
    /**
     * Key for jdbc url params
     */
    public static final String C_KEY_JDBC_URL_PARAMS = C_KEY_JDBC_URL + ".params";    
    
    /**
     * Key for max active connections
     */
    public static final String C_KEY_MAX_ACTIVE = "maxActive";
    
    /**
     * Key for max idle connections
     */
    public static final String C_KEY_MAX_IDLE = "maxIdle";
    
    /**
     * Key for max wait time
     */
    public static final String C_KEY_MAX_WAIT = "maxWait";
    
    /**
     * Key for database password
     */
    public static final String C_KEY_PASSWORD = "password";
    
    /**
     * Key for default
     */
    public static final String C_KEY_POOL_DEFAULT = "default";
    
    /**
     * Key for pool url
     */
    public static final String C_KEY_POOL_URL = "poolUrl";
    
    /**
     * Key for pool user
     */
    public static final String C_KEY_POOL_USER = "user";
    
    /**
     * Key for vfs pool
     */
    public static final String C_KEY_POOL_VFS = "vfs";
    
    /**
     * Key for pooling flag
     */
    public static final String C_KEY_POOLING = "pooling";

    /**
     * Key for test on borrow flag
     */
    public static final String C_KEY_TEST_ON_BORROW = "testOnBorrow";
    
    /**
     * Key for test query
     */
    public static final String C_KEY_TEST_QUERY = "testQuery";
    
    /**
     * Comment for <code>C_KEY_USERNAME</code>
     */
    public static final String C_KEY_USERNAME = "user";
    
    /**
     * Comment for <code>C_KEY_WHEN_EXHAUSTED_ACTION</code>
     */
    public static final String C_KEY_WHEN_EXHAUSTED_ACTION = "whenExhaustedAction";

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
     * @param configuration the configuration (opencms.properties)
     * @param key the key of the database pool in the configuration
     * @return String the URL to access the created DBCP pool
     * @throws Exception if the pool could not be initialized
     */
    public static final String createDriverManagerConnectionPool(ExtendedProperties configuration, String key) throws Exception {
        // read the values of the pool configuration specified by the given key
        String jdbcDriver = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_JDBC_DRIVER);
        String jdbcUrl = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_JDBC_URL);
        String jdbcUrlParams = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_JDBC_URL_PARAMS);
        int maxActive = configuration.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_ACTIVE, 10);
        int maxWait = configuration.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_WAIT, 2000);
        int maxIdle = configuration.getInteger(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_MAX_IDLE, 5);
        String testQuery = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_TEST_QUERY);
        String username = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_USERNAME);
        String password = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_PASSWORD);
        String poolUrl = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_POOL_URL);
        String whenExhaustedActionValue = configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_WHEN_EXHAUSTED_ACTION).trim();
        byte whenExhaustedAction = 0;
        boolean testOnBorrow = "true".equalsIgnoreCase(configuration.getString(C_KEY_DATABASE_POOL + "." + key + "." + C_KEY_TEST_ON_BORROW).trim());

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
        boolean poolingStmts = "true".equalsIgnoreCase(configuration.getString(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_POOLING, "true").trim());
        int maxActiveStmts = configuration.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_ACTIVE, 25);
        int maxWaitStmts = configuration.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_WAIT, 250);
        int maxIdleStmts = configuration.getInteger(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_MAX_IDLE, 15);
        String whenStmtsExhaustedActionValue = configuration.getString(C_KEY_DATABASE_STATEMENTS + "." + key + "." + C_KEY_WHEN_EXHAUSTED_ACTION);
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

        // use the generic pool
        connectionPool = new GenericObjectPool(null);

        // initialize an object pool to store connections
        connectionPool.setMaxActive(maxActive);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMaxWait(maxWait);
        connectionPool.setWhenExhaustedAction(whenExhaustedAction);

        connectionPool.setTestOnBorrow(testOnBorrow && (testQuery != null));
        connectionPool.setTestWhileIdle(true);

        // initialize a connection factory to make the DriverManager taking connections from the pool
        if (jdbcUrlParams != null) {
            jdbcUrl += jdbcUrlParams;
        }
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password);

        // initialize a keyed object pool to store PreparedStatements
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

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Init. JDBC pool      : " + poolUrl + " (" + jdbcUrl + ")");
        }

        return poolUrl;
    } 

}
