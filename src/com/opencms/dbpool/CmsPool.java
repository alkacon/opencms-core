package com.opencms.dbpool;

/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsPool.java,v $
 * Date   : $Date: 2001/01/29 15:13:25 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a  of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.sql.*;
import java.util.*;
import com.opencms.core.*;
import source.org.apache.java.util.*;

/**
 * This class is used to create an connection-pool for opencms.
 * 
 * @author a.schouten
 */
public class CmsPool {
	
	/**
	 * The parameters for this pool.
	 */
	private String m_driver;
	private String m_url;
	private String m_user;
	private String m_password;
	private int m_minConn;
	private int m_maxConn;
	private int m_increaseRate;
	private int m_timeout;
	private String m_poolname;
	
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
	 * @exception SQLException - if a SQL-Error occurs.
	 */
	public CmsPool(String poolname, String driver, String url, String user, 
				String password, int minConn, int maxConn, int increasRate, int timeout) 
		throws SQLException {
		// store the parameters
		m_poolname = poolname;
		m_driver = driver;
		m_url = url;
		m_user = user;
		m_password = password;
		m_minConn = minConn;
		m_maxConn = maxConn;
		m_increaseRate = increasRate;
		m_timeout = timeout;
		
		// register the driver to the driver-manager
		try {
			Class.forName(driver);
		} catch(ClassNotFoundException exc) {
			throw new SQLException("Driver not found: " + exc.getMessage());
		}
		
		// create the initial amount of connections
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": created");
		}
		createConnections(m_minConn);
	}

	/**
	 * Try to make a database connection to the given database. 
	 * @return a Connection to the database
	 * @exception SQLException if a database-access error occurs. 
	 */
	public Connection connect() 
		throws SQLException {
		return getConnection();
	}
	
	/**
	 * Gets a connection.
	 * @exception SQLException if a database-access error occurs. 
	 */
	public Connection getConnection() 
		throws SQLException {
		try {
			return (Connection) m_availableConnections.pop();
		} catch(EmptyStackException exc) {
			if(m_connectionAmount < m_maxConn) {
				// create new connections
				createConnections(m_increaseRate);
			} else {
				// no connections available - have to wait
				synchronized(m_availableConnections) {
					// wait until there are available connections
					if(A_OpenCms.isLogging()) {
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
		// try again to get a connection
		return getConnection();
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
		
		if(alive) {
			// put the connection to the available connections
			m_availableConnections.push(con);
		} else {
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": connection was broken");
			}
			// no, the connection is dead -> trhow it away and close it
			con.closeOriginalConnection();
			m_connectionAmount --;

			// create a new one
			try {
				createConnections(1);
			} catch(SQLException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": unable to create new connection for broken one");
				}
			}
		}
			
		// wake up eventually waiting threads		
		synchronized(m_availableConnections) {
			m_availableConnections.notifyAll();
		}
	}
	
	/**
	 * Creates the needed connections, if possible.
	 * @param amount - the amount of connections to create.
	 * @exception SQLException if a database-access error occurs. 
	 */
	private synchronized void createConnections(int amount) 
		throws SQLException {
		for(int i = 0; (i < amount) && (m_connectionAmount < m_maxConn); i++) {
			// create another connection
			m_availableConnections.push(createConnection());
			m_connectionAmount++;
		}
	}
	
	/**
	 * Creates one connection.
	 * @return the new created connection.
	 * @exception SQLException if a database-access error occurs. 
	 */
	private Connection createConnection() throws SQLException {
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_POOL, "["+ getClass().getName() +"] " + m_poolname + ": creating new connection number " + m_connectionAmount);
		}
		return new CmsConnection(DriverManager.getConnection(m_url, m_user, m_password), this);
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
}
