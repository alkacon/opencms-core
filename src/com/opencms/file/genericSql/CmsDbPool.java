/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsDbPool.java,v $
 * Date   : $Date: 2000/07/17 10:20:26 $
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

package com.opencms.file.genericSql;

import java.sql.*;
import java.util.*; 

import com.opencms.core.*;

/**
 * This class is used to create an pool of prepared statements.
 * 
 * @author u.roland
 * @author a.schouten
 */
public class CmsDbPool {
	
	/**
	 * A Key for a simple statement in the hashtable.
	 */
	private static final String C_SIMPLE_STATEMENT = "simple-statement";
	
	/**
	 * A simple statement to keep alive all connections.
	 */
	private static final String C_KEEP_ALIVE_STATEMENT = "select 1 from DUAL;";
	
	/*
	 * maximum of connections to the database
	 */
	private int m_maxConn = 10;

	/*
	 * the driver for the database
	 */
	private String m_driver = null;
	
	/*
	 * url to the database
	 */
	private String m_url = null;

	/*
	 * user for the database
	 */
	private String m_user = null;
	
	/*
	 * passwd for the database
	 */
	private String m_passwd = null;
	
	/*
	 * store the PreparedStatements with an connection
	 */
	private Stack m_prepStatements = null;
	
	/*
	 * store the used-sql statements
	 */
	private Hashtable m_usedStatementsCache = new Hashtable();
	
	/*
	 * store the connections
	 */
	private Vector m_connections;
	
	/**
	 * Init the pool with a specified number of connections.
	 * 
     * @param driver - driver for the database
     * @param url - the URL of the database to which to connect
     * @param user - the username to connect to the db.
     * @param passwd - the passwd of the user to connect to the db.
     * @param maxConn - maximum connections
	 */
	public CmsDbPool(String driver, String url, String user, String passwd, int maxConn) throws CmsException {
		this.m_driver = driver;
		this.m_url = url;
		this.m_user = user;
		this.m_passwd = passwd;
		this.m_maxConn = maxConn;
		
		// register the driver for the database
		try {
			Class.forName(m_driver);
		}
		catch (ClassNotFoundException e) {
           	throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
		}
		// init the hashtables and vector(s)
		m_prepStatements = new Stack();
		m_connections = new Vector(m_maxConn+1);
		
		// init connections
		for (int i = 0; i < m_maxConn; i++) {
			Connection conn = null;
	
			try {
      			conn = DriverManager.getConnection(m_url, m_user, m_passwd);
   				m_connections.addElement(conn);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
			m_prepStatements.push(new Hashtable());
		}
		initStatement();
	}
	
	/**
	 * Init the PreparedStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL statement that may contain one or more '?' IN parameter placeholders
	 */
	public void initPreparedStatement(Integer key, String sql) throws CmsException {
		Connection conn = null;
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				tmp.put(key, pstmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
	
	/**
	 * Init a (Simple)Statement on all connections.
	 * 
	 */
	public void initStatement() throws CmsException {
		Connection conn = null;
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			
			try {
				Statement stmt = conn.createStatement();
				tmp.put(C_SIMPLE_STATEMENT, stmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}

	/**
	 * Gets a PreparedStatement object and remove it from the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */
	public PreparedStatement getPreparedStatement(Integer key) throws CmsException {
		
        PreparedStatement pstmt = null;
		synchronized(m_prepStatements) {
			while(m_prepStatements.size() == 0) {
				try {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbPool] no connections available - have to wait.");
					}
					m_prepStatements.wait(100);					
				} catch(InterruptedException exc) {
				}
			}
			Hashtable pool = (Hashtable)m_prepStatements.pop();
			pstmt = (PreparedStatement)pool.get(key);
			m_usedStatementsCache.put(pstmt, pool);
			m_prepStatements.notify();
		}		
		return pstmt;
	}
	
	/**
	 * Gets a following PreparedStatement object using the same connection as the firstStatement.
	 * This is usefull vor locking tables and other statemnts that have to use the same connection.
	 * This staments don't have to be put back - only the firstone must be given back to the pool.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */
	public PreparedStatement getNextPreparedStatement(PreparedStatement firstStatement, Integer key) throws CmsException {
		
        PreparedStatement pstmt = null;
		Hashtable pool = (Hashtable)m_usedStatementsCache.get(firstStatement);
		pstmt = (PreparedStatement)pool.get(key);
		return pstmt;
	}

	
	/**
	 * Gets a (Simple)Statement object and remove it from the list of available statements.
	 * 
	 * @return a statement to execute queries on.
	 */
	public Statement getStatement() throws CmsException {
		
        Statement stmt = null;
		synchronized(m_prepStatements) {
			while(m_prepStatements.size() == 0) {
				try {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbPool] no connections available - have to wait.");
					}
					m_prepStatements.wait(100);					
				} catch(InterruptedException exc) {
				}
			}
			Hashtable pool = (Hashtable)m_prepStatements.pop();
			stmt = (Statement)pool.get(C_SIMPLE_STATEMENT);
			m_usedStatementsCache.put(stmt, pool);
			m_prepStatements.notify();
		}		
		return stmt;
	}
	
	/**
	 * Add the given statement to the list of available statements.
	 * 
	 * @param pstmt - the statement
	 */
	public void putStatement(Statement stmt) {
		synchronized(m_prepStatements) {
			Hashtable pool = (Hashtable)(m_usedStatementsCache.remove(stmt));
			m_prepStatements.push(pool);
			m_prepStatements.notify();
		}		
	}
	
	/**
	 * Add the given statement to the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @param pstmt - the statement
	 */
	public void putPreparedStatement(Integer key, PreparedStatement pstmt) {
		synchronized(m_prepStatements) {
			Hashtable pool = (Hashtable)(m_usedStatementsCache.remove(pstmt));
			m_prepStatements.push(pool);
			m_prepStatements.notify();
		}		
	}
	
	/**
	 * Destroys this db-pool.
	 */
	public void destroy() {
		
		// close all prep-statements on all connections
		while(m_prepStatements.size() != 0) {
			Hashtable pool = (Hashtable) m_prepStatements.pop();
			Enumeration keys = pool.keys();
			while(keys.hasMoreElements() ) {
				Object key = keys.nextElement();
				Statement statement = (Statement) pool.get(key);
				try {
					statement.close();
				} catch (SQLException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbPool] closing statement failed: " + com.opencms.util.Utils.getStackTrace(exc));
					}
				}
			}
		}
		
		// close all connections
		for(int i = 0 ; i < m_connections.size(); i++) {
			try {
				((Connection)m_connections.elementAt(i)).close();
			} catch (SQLException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbPool] closing connection failed: " + com.opencms.util.Utils.getStackTrace(exc));
				}
			}
		}
	}
	
	/**
	 * This method sends a sql-query tzo each connection, to prvent them from closing by the database.
	 */
	public void keepAlive() {
		synchronized(m_prepStatements) {
			for(int i = 0; i < m_prepStatements.size(); i++) {
				Hashtable pool = (Hashtable) m_prepStatements.elementAt(i);
				Statement statement = (Statement)pool.get(C_SIMPLE_STATEMENT);
				try {
					statement.execute(C_KEEP_ALIVE_STATEMENT);
				} catch(SQLException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsDbPool] keepAlive() failed: " + com.opencms.util.Utils.getStackTrace(exc));
					}
				}
			}
		}		
	}
}
