/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsPool.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.18 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;

/**
 * This class is used to create an connection-pool for opencms.
 *
 * @author Andreas Schouten
 */
public class CmsPool extends Thread {

    /**
     * The parameters for this pool.
     */
    private String m_driver;
    private String m_url;
    private String m_user;
    private String m_password;
    private String m_conTestQuery = null;
    private int m_minConn;
    private int m_maxConn;
    private int m_increaseRate;
    private long m_timeout;
    private long m_maxage;
    private String m_poolname;
    private Driver m_originalDriver;

    /**
     * The current amount of connections in this pool.
     */
    private int m_connectionAmount = 0;

    /**
     * The Stack to store the connections in.
     */
    private Stack m_availableConnections = new Stack();

    /**
     * Creates a new Pool.
     * @param poolname - the name of this pool.
     * @param driver - the classname of the driver.
     * @param url - the url to connect to the database.
     * @param user - the user to access the db.
     * @param password - the password to connect to the db.
     * @param minConn - the minimum amount Connections maintained in the pool.
     * @param maxConn - the maximum amount Connections maintained in the pool.
     * @param increaseRate - the rate to increase the the amount of
     * connections in the pool.
     * @param timeout - the timout after a unused connection has to be closed.
     * @throws SQLException - if a SQL-Error occurs.
     */
    public CmsPool(String poolname, String driver, String url, String user,
                String password, int minConn, int maxConn, int increasRate, int timeout, int maxage)
        throws SQLException {
        super(poolname);
        // store the parameters
        m_poolname = poolname;
        m_driver = driver;
        m_url = url;
        m_user = user;
        if(m_user == null) m_user = "";
        m_password = password;
        if(m_password == null) m_password = "";
        m_minConn = minConn;
        m_maxConn = maxConn;
        m_increaseRate = increasRate;
        m_timeout = timeout  * 60 * 1000;
        m_maxage = maxage  * 60 * 1000;

        // register the driver to the driver-manager
        try {
            Class.forName(driver);
        } catch(ClassNotFoundException exc) {
            throw new SQLException("Driver not found: " + exc.getMessage());
        }
        m_originalDriver = DriverManager.getDriver(m_url);

        // create the initial amount of connections
        createConnections(m_minConn);

        // set this a deamon-thread
        setDaemon(true);
        // start the connection-guard for this pool
        start();
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": created");
        }
    }

    /**
     * Creates a new Pool.
     * @param poolname - the name of this pool.
     * @param driver - the classname of the driver.
     * @param url - the url to connect to the database.
     * @param user - the user to access the db.
     * @param password - the password to connect to the db.
     * @param minConn - the minimum amount Connections maintained in the pool.
     * @param maxConn - the maximum amount Connections maintained in the pool.
     * @param increaseRate - the rate to increase the the amount of
     * connections in the pool.
     * @param timeout - the timout after a unused connection has to be closed.
     * @param conTestQuery - the test query to test a connection before
     * delivering. If this is set to null, no test will be performed.
     * @throws SQLException - if a SQL-Error occurs.
     */
    public CmsPool(String poolname, String driver, String url, String user,
                String password, int minConn, int maxConn, int increasRate,
                int timeout, int maxage, String conTestQuery)
        throws SQLException {
        this(poolname, driver, url, user, password, minConn, maxConn,
            increasRate, timeout, maxage);
        m_conTestQuery = conTestQuery;
    }
    /**
     * The run-method for the connection-guard
     */
    public void run() {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
           A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": starting connection-guard");
        }
        // never stop
        for(;;) {
            // sleep, before checking the timeouts of the connections
            try {
                sleep(m_timeout);
            } catch(InterruptedException exc) {
                // ignore this exception
            }
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
               A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": checking for outtimed connections");
            }
            synchronized(m_availableConnections) {
                Enumeration elements = m_availableConnections.elements();
                while(elements.hasMoreElements()) {
                    CmsConnection con = (CmsConnection) elements.nextElement();
                    if((con.getLastUsed() + (m_timeout)) < System.currentTimeMillis()) {
                        // this connection is to old... destroy it
                        m_availableConnections.removeElement(con);
                        m_connectionAmount--;
                        con.closeOriginalConnection();
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                           A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": closing one outtimed connection");
                        }
                    }
                }
                // create missing minimum connection-amount
                if(m_connectionAmount < m_minConn) {
                    try {
                        createConnections(m_minConn - m_connectionAmount);
                    } catch(SQLException exc) {
                        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": unable to create new connection for broken one");
                        }
                    }
                }
            }
        }
    }


    /**
     * Try to make a database connection to the given database.
     * @return a Connection to the database
     * @throws SQLException if a database-access error occurs.
     */
    public Connection connect() throws SQLException {
        return getConnection();
    }

    /**
     * Gets a connection.
     * @throws SQLException if a database-access error occurs.
     */
    private Connection getConnection()
        throws SQLException {
        Connection con = null;
        while(con == null) {
            synchronized(m_availableConnections) {
                if( (m_availableConnections.size() <= 0) && (m_connectionAmount < m_maxConn) ) {
                    // create new connections
                    createConnections(m_increaseRate);
                } else if(m_availableConnections.size() > 0) {
                    // return the available connection
                    con = (Connection) m_availableConnections.pop();
                } else {
                    // no connection available - have to wait
                    // wait until there are available connections
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                          A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": no connections available - have to wait");
                    }
                    try {
                          m_availableConnections.wait();
                    } catch(InterruptedException iExc) {
                          // ignore the exception
                    }
                }
            }
        }
        // done it - we have a connection
        if(testConnection(con)) {
            return con;
        } else {
            synchronized(m_availableConnections) {
                // the connection is invalid - destroy it
                ((CmsConnection)con).closeOriginalConnection();
                m_connectionAmount --;

                // create a new one
                try {
                    createConnections(1);
                    m_availableConnections.notify();
                } catch(SQLException exc) {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": unable to create new connection for broken one");
                    }
                }
            }
            // now return a new one
            return getConnection();
        }
    }

    /**
     * Puts a connection back to the pool.
     */
    public void putConnection(CmsConnection con) {
        boolean alive = false;
        try {
            // check, if the connection is available
            if(!con.isClosed()) {
                con.clearWarnings();
                // this connection is alive
                alive = true;
            }
        } catch(SQLException exc) {
            // ignore the exception, alive is false
        }

        if((con.getEstablishedTime() + (m_maxage)) < System.currentTimeMillis()) {
            // this connection is to old. destroy it and create a new-one!
            alive = false;
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": connection is to old, destroy it.");
            }
        }

        synchronized(m_availableConnections) {
            if(alive) {
                // put the connection to the available connections
                m_availableConnections.push(con);
                m_availableConnections.notify();
            } else {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": connection was broken");
                }
                // no, the connection is dead -> trhow it away and close it
                con.closeOriginalConnection();
                m_connectionAmount --;

                // create a new one
                try {
                    createConnections(1);
                    m_availableConnections.notify();
                } catch(SQLException exc) {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": unable to create new connection for broken one");
                    }
                }
            }
        }
    }

    /**
     * Creates the needed connections, if possible.
     * @param amount - the amount of connections to create.
     * @throws SQLException if a database-access error occurs.
     */
    private void createConnections(int amount) throws SQLException {
        for(int i = 0; (i < amount) && (m_connectionAmount < m_maxConn); i++) {
            // create another connection
            m_availableConnections.push(createConnection());
            m_connectionAmount++;
        }
    }

    /**
     * Creates one connection.
     * @return the new created connection.
     * @throws SQLException if a database-access error occurs.
     */
    private Connection createConnection() throws SQLException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": creating new connection. Current Amount is:" + m_connectionAmount);
        }
        Connection con = null;
        Properties props = new Properties();
        props.setProperty("user", m_user);
        props.setProperty("password", m_password);
        con = m_originalDriver.connect(m_url, props);
        CmsConnection retValue = new CmsConnection(con, this);
        return retValue;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[" + this.getClass().getName() + "]:");
        output.append(m_driver);
        output.append(", ");
        output.append(m_url);
        output.append(", ");
        output.append(m_user);
        output.append(", ");
        output.append(m_minConn);
        output.append(", ");
        output.append(m_maxConn);
        output.append(", ");
        output.append(m_increaseRate);
        output.append(".");
        return output.toString();
    }

    /**
     * Destroys this pool.
     */
    public void destroy() {
        synchronized(m_availableConnections) {
            while(m_availableConnections.size() > 0) {
                ((CmsConnection) m_availableConnections.pop()).closeOriginalConnection();
                m_connectionAmount--;
            }
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": destroyed");
        }
    }

    /**
     * Test the connection by executing an select statement. This is done
     * only if the statement is defined in opencms.properties. Normally there
     * is no need for this testing - but in difficult environments (like firewalls)
     * you can use it to find out whats going wrong.
     * @param con The connection to test.
     * @return true if the connection could be tested without an SQLException.
     * Returns true if there is no test-statement defined in opencms.properties.
     * Returns false if there was an SQLException by executing the statement.
     */
    protected boolean testConnection(java.sql.Connection con) {
        if((m_conTestQuery == null) || (m_conTestQuery == "")) {
            // no test should be performed - return true - con ok!
            return true;
        }
        ResultSet res = null;
        PreparedStatement stmnt = null;
        boolean retValue = true;
        try {
            stmnt = con.prepareStatement(m_conTestQuery);
            res = stmnt.executeQuery();
            res.next();
        } catch(SQLException exc) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": testConnection failed:\n" + com.opencms.util.Utils.getStackTrace(exc) + "\n\n" + com.opencms.util.Utils.getStackTrace(new Exception()));
            }
            retValue = false;
        } finally {
            try {
                res.close();
            } catch(Exception exc) {
                // ignore
            }
            try {
                stmnt.close();
            } catch(Exception exc) {
                // ignore
            }
        }
        return retValue;
    }
}
