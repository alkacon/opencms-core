/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsDbcp.java,v $
 * Date   : $Date: 2003/05/20 11:47:36 $
 * Version: $Revision: 1.2 $
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
package com.opencms.dbpool;

import java.sql.Driver;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;

import source.org.apache.java.util.Configurations;

/**
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/05/20 11:47:36 $
 */
public class CmsDbcp extends Object {

    public static final String C_DBCP_JDBC_URL_PREFIX = "jdbc:apache:commons:dbcp:";

    public static final String C_KEY_DATABASE_POOL = "database.pool";
    public static final String C_KEY_DEFAULT_POOL_KEY = "default";
    public static final String C_KEY_USER_POOL_KEY = "user";
    public static final String C_KEY_VFS_POOL_KEY = "vfs";

    protected static final String C_KEY_JDBC_DRIVER = "jdbcDriver";
    protected static final String C_KEY_JDBC_URL = "jdbcUrl";
    protected static final String C_KEY_MAX_ACTIVE = "maxActive";
    protected static final String C_KEY_MAX_WAIT = "maxWait";
    protected static final String C_KEY_MAX_IDLE = "maxIdle";
    protected static final String C_KEY_TEST_QUERY = "testQuery";
    protected static final String C_KEY_USERNAME = "user";
    protected static final String C_KEY_PASSWORD = "password";
    protected static final String C_KEY_POOL_URL = "poolUrl";

    /**
     * Default constructor.
     */
    private CmsDbcp() {
        super();
    }

    public static final String createConnectionPool(Configurations config, String key) throws Exception {
        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.getString(key + "." + C_KEY_JDBC_DRIVER);
        String jdbcUrl = config.getString(key + "." + C_KEY_JDBC_URL);
        int maxActive = config.getInteger(key + "." + C_KEY_MAX_ACTIVE);
        int maxWait = config.getInteger(key + "." + C_KEY_MAX_WAIT);
        int maxIdle = config.getInteger(key + "." + C_KEY_MAX_IDLE);
        String testQuery = config.getString(key + "." + C_KEY_TEST_QUERY);
        String username = config.getString(key + "." + C_KEY_USERNAME);
        String password = config.getString(key + "." + C_KEY_PASSWORD);
        String poolUrl = config.getString(key + "." + C_KEY_POOL_URL);

        Class.forName(jdbcDriver).newInstance();
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        connectionPool.setMaxActive(maxActive);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMaxWait(maxWait);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(poolUrl, connectionPool);

        return poolUrl;
    }

}
