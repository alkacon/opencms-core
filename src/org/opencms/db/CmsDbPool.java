/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbPool.java,v $
 * Date   : $Date: 2005/05/31 07:49:05 $
 * Version: $Revision: 1.35 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.I_CmsConstants;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * based pools might be added probably later.<p>
 * 
 * <b>Please note:</b> This class is subject to change in later versions.
 * To obtain information about the connections, please use the
 * {@link org.opencms.db.CmsSqlManager}.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.35 $ $Date: 2005/05/31 07:49:05 $
 * @since 5.1
 */
public final class CmsDbPool {

    /** This prefix is required to make the JDBC DriverManager return pooled DBCP connections. */
    public static final String C_DBCP_JDBC_URL_PREFIX = "jdbc:apache:commons:dbcp:";
    
    /** The prefix used for opencms JDBC pools. */
    public static final String C_OPENCMS_URL_PREFIX = "opencms:";
    
    /** The name of the opencms default pool. */
    public static final String C_OPENCMS_DEFAULT_POOL_NAME = "default";
    
    /** The default OpenCms JDBC pool URL. */
    public static final String C_OPENCMS_DEFAULT_POOL_URL = "opencms:default";

    /**
     * Prefix for database keys.
     */
    public static final String C_KEY_DATABASE = "db.";
    
    /**
     * Key for the database name.
     */
    public static final String C_KEY_DATABASE_NAME = C_KEY_DATABASE + "name";
    
    /**
     * Key for the pool id.
     */
    public static final String C_KEY_DATABASE_POOL = C_KEY_DATABASE + "pool";
    
    /**
     * Key for statement pooling.
     */
    public static final String C_KEY_DATABASE_STATEMENTS = C_KEY_DATABASE + "statements";

    /**
     * Key for jdbc driver.
     */
    public static final String C_KEY_JDBC_DRIVER = "jdbcDriver";
    
    /**
     * Key for jdbc url.
     */
    public static final String C_KEY_JDBC_URL = "jdbcUrl";
    
    /**
     * Key for jdbc url params.
     */
    public static final String C_KEY_JDBC_URL_PARAMS = C_KEY_JDBC_URL + ".params";    
    
    /**
     * Key for maximum active connections.
     */
    public static final String C_KEY_MAX_ACTIVE = "maxActive";
    
    /**
     * Key for maximum idle connections.
     */
    public static final String C_KEY_MAX_IDLE = "maxIdle";
    
    /**
     * Key for maximum wait time.
     */
    public static final String C_KEY_MAX_WAIT = "maxWait";
    
    /**
     * Key for database password.
     */
    public static final String C_KEY_PASSWORD = "password";
    
    /**
     * Key for default.
     */
    public static final String C_KEY_POOL_DEFAULT = "default";
    
    /**
     * Key for pool url.
     */
    public static final String C_KEY_POOL_URL = "poolUrl";
    
    /**
     * Key for pool user.
     */
    public static final String C_KEY_POOL_USER = "user";
    
    /**
     * Key for vfs pool.
     */
    public static final String C_KEY_POOL_VFS = "vfs";
    
    /**
     * Key for pooling flag.
     */
    public static final String C_KEY_POOLING = "pooling";

    /**
     * Key for test on borrow flag.
     */
    public static final String C_KEY_TEST_ON_BORROW = "testOnBorrow";
    
    /**
     * Key for test query.
     */
    public static final String C_KEY_TEST_QUERY = "testQuery";
    
    /**
     * Comment for <code>C_KEY_USERNAME</code>.
     */
    public static final String C_KEY_USERNAME = "user";
    
    /**
     * Comment for <code>C_KEY_WHEN_EXHAUSTED_ACTION</code>.
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
     * Returns a list of available database pool names.<p>
     * 
     * @param configuration the configuration
     * @return a list of database pool names
     */
    public static List getDbPoolNames(Map configuration) {
        
        ExtendedProperties config;
        if (configuration instanceof ExtendedProperties) {
            config = (ExtendedProperties)configuration;
        } else {
            config = new ExtendedProperties();
            config.putAll(configuration);            
        }
        
        List dbPoolNames = new ArrayList();
        String[] driverPoolNames = config.getStringArray(I_CmsConstants.C_CONFIGURATION_DB + ".pools");

        for (int i = 0; i < driverPoolNames.length; i++) { 
            dbPoolNames.add(getDbPoolName(configuration, driverPoolNames[i]));
        }
        
        return dbPoolNames;
    }
    
    /**
     * Returns the database pool name for a given configuration key.<p>
     * 
     * @param configuration the configuration 
     * @param key a db pool configuration key
     * @return the database pool name
     */
    public static String getDbPoolName(Map configuration, String key) {
    
        String jdbcUrl = configuration.get(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_JDBC_URL).toString();
        if (jdbcUrl.startsWith(C_OPENCMS_URL_PREFIX)) {
            return jdbcUrl.substring(jdbcUrl.indexOf(':'));
        } else {
            return jdbcUrl;
        }
    }
    
    /**
     * Returns the name of the default database connection pool.<p>
     * 
     * @return the name of the default database connection pool
     */
    public static String getDefaultDbPoolName() {

        return C_OPENCMS_DEFAULT_POOL_NAME;
    }
    
    /**
     * Creates a JDBC DriverManager based DBCP connection pool.<p>
     * 
     * @param configuration the configuration (opencms.properties)
     * @param key the key of the database pool in the configuration
     * @return String the URL to access the created DBCP pool
     * @throws Exception if the pool could not be initialized
     */
    public static PoolingDriver createDriverManagerConnectionPool(Map configuration, String key) throws Exception {
        
        ExtendedProperties config;
        if (configuration instanceof ExtendedProperties) {
            config = (ExtendedProperties)configuration;
        } else {
            config = new ExtendedProperties();
            config.putAll(configuration);            
        }
        
        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_JDBC_DRIVER);
        String jdbcUrl = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_JDBC_URL);
        String jdbcUrlParams = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_JDBC_URL_PARAMS);
        int maxActive = config.getInteger(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_MAX_ACTIVE, 10);
        int maxWait = config.getInteger(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_MAX_WAIT, 2000);
        int maxIdle = config.getInteger(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_MAX_IDLE, 5);
        String testQuery = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_TEST_QUERY);
        String username = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_USERNAME);
        String password = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_PASSWORD);
        String poolUrl = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_POOL_URL);
        String whenExhaustedActionValue = config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_WHEN_EXHAUSTED_ACTION).trim();
        byte whenExhaustedAction = 0;
        boolean testOnBorrow = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_POOL + '.' + key + '.' + C_KEY_TEST_ON_BORROW).trim());

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
        boolean poolingStmts = "true".equalsIgnoreCase(config.getString(C_KEY_DATABASE_STATEMENTS + '.' + key + '.' + C_KEY_POOLING, "true").trim());
        int maxActiveStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + '.' + key + '.' + C_KEY_MAX_ACTIVE, 25);
        int maxWaitStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + '.' + key + '.' + C_KEY_MAX_WAIT, 250);
        int maxIdleStmts = config.getInteger(C_KEY_DATABASE_STATEMENTS + '.' + key + '.' + C_KEY_MAX_IDLE, 15);
        String whenStmtsExhaustedActionValue = config.getString(C_KEY_DATABASE_STATEMENTS + '.' + key + '.' + C_KEY_WHEN_EXHAUSTED_ACTION);
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
        connectionPool.setMaxWait(maxWait);
        connectionPool.setWhenExhaustedAction(whenExhaustedAction);

        connectionPool.setTestOnBorrow(testOnBorrow && (testQuery != null));
        connectionPool.setTestWhileIdle(true);

        // initialize a connection factory to make the DriverManager taking connections from the pool
        if (jdbcUrlParams != null) {
            jdbcUrl += jdbcUrlParams;
        }
        
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password);
        
        // Set up statement pool, if desired
        GenericKeyedObjectPoolFactory statementFactory = null;
        if (poolingStmts) {
            statementFactory = new GenericKeyedObjectPoolFactory(null, maxActiveStmts, whenStmtsExhaustedAction, maxWaitStmts, maxIdleStmts);
        }       
        
        // initialize a factory to obtain pooled connections and prepared statements
        new PoolableConnectionFactory(
            connectionFactory,
            connectionPool,
            statementFactory,
            testQuery,
            false,
            true);
        
        // initialize a new pooling driver using the pool
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(poolUrl, connectionPool);        

        // try to connect once to the database to ensure it can be connected to at all
        Connection con = connectionFactory.createConnection();
        con.close();
        
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(
                Messages.get().key(Messages.INIT_JDBC_POOL_2, poolUrl, jdbcUrl));
        }
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_JDBC_POOL_2, poolUrl, jdbcUrl));
        }
             
        return driver;
    } 

}
