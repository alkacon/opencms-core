/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsDriver.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.8 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.dbpool;

import java.sql.*;
import java.util.*;
import source.org.apache.java.util.*;

/**
 * The OpenCms database driver.
 *
 * @author Andreas Schouten
 * @see java.sql.Driver
 */
public class CmsDriver implements java.sql.Driver {

    //
    // Register ourselves with the DriverManager
    //
    static {
        try {
            java.sql.DriverManager.registerDriver(new CmsDriver());
        } catch (java.sql.SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    /**
     * The start of the connection urls for this Driver
     */
    private static final String C_CONNECTION_URL = "jdbc:opencmspool:";

    /**
     * The configurations to read properties from.
     */
    private static Configurations c_config = null;

    /**
     * A hashtable to store all pools, identified by poolName.
     */
    private Hashtable m_pools = new Hashtable();

    /**
     * Sets the confitgurations to read pool-properties from.
     * @param conf - the configurations to read from.
     */
    public static void setConfigurations(Configurations conf) {
        c_config = conf;
    }

    /**
     * Default-Constructor
     */
    private CmsDriver() {
        super();
    }

    /**
     * Try to make a database connection to the given URL. The driver should
     * return "null" if it realizes it is the wrong kind of driver to connect
     * to the given URL. This will be common, as when the JDBC driver manager
     * is asked to connect to a given URL it passes the URL to each loaded
     * driver in turn.
     *
     * The driver should raise a SQLException if it is the right driver to
     * connect to the given URL, but has trouble connecting to the database.
     *
     * The java.util.Properties argument can be used to passed arbitrary string
     *  tag/value pairs as connection arguments. Normally at least "user" and
     * "password" properties should be included in the Properties.
     * @param url - The URL of the database to connect to
     * @param info - a list of arbitrary string tag/value pairs as connection
     * arguments; normally at least a "user" and "password" property should be included
     * @return a Connection to the URL
     * @throws SQLException if a database-access error occurs.
     */
    public java.sql.Connection connect(String url, Properties info)
        throws SQLException {
        if(acceptsURL(url)) {
            String poolName = url.substring(C_CONNECTION_URL.length());
            return getPool(poolName).connect();
        } else {
            // this driver don't accepts this url - return null
            return null;
        }
    }

    /**
     * Returns true if the driver thinks that it can open a connection
     * to the given URL. Typically drivers will return true if they
     * understand the subprotocol specified in the URL and false if
     * they don't.
     * @param url - The URL of the database.
     * @return True if this driver can connect to the given URL.
     * @throws SQLException if a database-access error occurs.
     */
    public boolean acceptsURL(String url)
        throws SQLException {
        return url.startsWith(C_CONNECTION_URL);
    }

    /**
     * The getPropertyInfo method is intended to allow a generic GUI tool
     * to discover what properties it should prompt a human for in order to
     *  get enough information to connect to a database. Note that depending
     *  on the values the human has supplied so far, additional values may
     * become necessary, so it may be necessary to iterate though several
     *  calls to getPropertyInfo.
     *
     * @param url - The URL of the database to connect to.
     * @param info - A proposed list of tag/value pairs that will be sent
     * on connect open
     * @return An array of DriverPropertyInfo objects describing possible properties.
     *  This array may be an empty array if no properties are required.
     * @throws SQLException if a database-access error occurs.
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
        throws SQLException {
        return new DriverPropertyInfo[0];
    }

    /**
     * Get the driver's major version number. Initially this should be 1.
     */
    public int getMajorVersion() {
        return 1;
    }

    /**
     * Get the driver's minor version number. Initially this should be 0.
     */
    public int getMinorVersion() {
        return 0;
    }

    /**
     * Report whether the Driver is a genuine JDBC COMPLIANT (tm) driver.
     * A driver may only report "true" here if it passes the JDBC compliance tests,
     * otherwise it is required to return false. JDBC compliance requires full
     * support for the JDBC API and full support for SQL 92 Entry Level. It is
     * expected that JDBC compliant drivers will be available for all the major
     * commercial databases. This method is not intended to encourage the
     * development of non-JDBC compliant drivers, but is a recognition of the fact
     * that some vendors are interested in using the JDBC API and framework for
     * lightweight databases that do not support full database functionality, or
     * for special databases such as document information retrieval where a SQL
     * implementation may not be feasible.
     */
    public boolean jdbcCompliant() {
        return false;
    }

    /**
     * Returns the specified pool-object.
     * @param poolName - the name of the pool to return
     * @return Pool - the pool for this name
     * @throws SQLException - if a SQL-Error occures.
     */
    private CmsPool getPool(String poolName) throws SQLException {
        if(!m_pools.containsKey(poolName)) {
            // the pool doesen't exist - try to create it
            m_pools.put(poolName, createPool(poolName));
        }
        // return the existing or new created pool
        return (CmsPool) m_pools.get(poolName);
    }

    /**
     * Creates a new pool with the name poolName. Reads informations
     * for the pool from configurations.
     * @param poolName - the name of the pool to create.
     * @return Pool - the created pool.
     * @throws SQLException - is a SQL-Error occurs.
     */
    private CmsPool createPool(String poolName) throws SQLException {
        String driver = c_config.getString("pool." + poolName + ".driver");
        String url = c_config.getString("pool." + poolName + ".url");
        String user = c_config.getString("pool." + poolName + ".user");
        String password = c_config.getString("pool." + poolName + ".password");
        int minConn = c_config.getInteger("pool." + poolName + ".minConn", 1);
        int maxConn = c_config.getInteger("pool." + poolName + ".maxConn", 1);
        int increaseRate = c_config.getInteger("pool." + poolName + ".increaseRate", 1);
        int timeout = c_config.getInteger("pool." + poolName + ".timeout", 120);
                int maxage = c_config.getInteger("pool." + poolName + ".maxage", 360);
        String conTestQuery = c_config.getString("pool." + poolName + ".testQuery", null);

        // create the pool and return it
        return new CmsPool(poolName, driver, url, user, password, minConn,
                        maxConn, increaseRate, timeout, maxage, conTestQuery);
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[" + this.getClass().getName() + "]:");
        output.append(" available pools: \n");

        Enumeration keys = m_pools.keys();

        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            output.append("\t"+key + " :" + m_pools.get(key).toString()+"\n");
        }
        return output.toString();
    }

    /**
     * Destroys this driver and all its connections.
     */
    public void destroy() {
        Enumeration pools = m_pools.elements();
        m_pools = new Hashtable();
        while(pools.hasMoreElements() ){
            CmsPool pool = (CmsPool) pools.nextElement();
            pool.destroy();
        }
    }
}
