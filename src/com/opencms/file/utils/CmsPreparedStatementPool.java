/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/utils/Attic/CmsPreparedStatementPool.java,v $
 * Date   : $Date: 2000/07/06 13:35:45 $
 * Version: $Revision: 1.14 $
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

package com.opencms.file.utils;

import java.sql.*;
import java.util.*;

import com.opencms.core.*;

/**
 * This class is used to create an pool of prepared statements.
 * 
 * @author u.roland
 * @author a.schouten
 */
public class CmsPreparedStatementPool {
	
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
	private Hashtable m_prepStatements;
	
	/*
	 * store the sql statements
	 */
	private Hashtable m_prepStatementsCache = null;
	
	/*
	 * store the connections
	 */
	private Vector m_connections;
	
	/*
	 * the connection for generating an Id.
	 */
	private Connection m_idConnection = null;
		
	/*
	 * counter
	 */
	private int count = 0;
	
	/**
	 * Init the pool with a specified number of connections.
	 * 
     * @param driver - driver for the database
     * @param url - the URL of the database to which to connect
     * @param user - the username to connect to the db.
     * @param passwd - the passwd of the user to connect to the db.
     * @param maxConn - maximum connections
	 */
	public CmsPreparedStatementPool(String driver, String url, String user, String passwd, int maxConn) throws CmsException {
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
		m_prepStatements = new Hashtable();
		m_prepStatementsCache  = new Hashtable();
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
		}
		// init IdConnection
		try {
			m_idConnection = DriverManager.getConnection(m_url, m_user, m_passwd);
            m_connections.addElement(m_idConnection);
		}catch (SQLException e) {	
			throw new CmsException(CmsException.C_SQL_ERROR, e);
		}
	}
	
	/**
	 * Init the PreparedStatement on all connections and store the sql statement in an hashtable.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL statement that may contain one or more '?' IN parameter placeholders
	 */
	public void initPreparedStatement(Integer key, String sql) throws CmsException {
		Vector temp = new Vector(m_maxConn);
		Connection conn = null;

		m_prepStatementsCache.put(key, sql);
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				temp.addElement(pstmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
		
		m_prepStatements.put(key, temp);
	}

	/**
	 * Init the IdPreparedStatement on the IdConnections and store the sql statement in an hashtable.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL statement that may contain one or more '?' IN parameter placeholders
	 */
	public void initIdStatement(Integer key, String sql) throws CmsException {

		m_prepStatementsCache.put(key, sql);
		
		try {
			PreparedStatement pstmt = m_idConnection.prepareStatement(sql);
			m_prepStatements.put(key, pstmt);
			
		}catch (SQLException e) {
			throw new CmsException(CmsException.C_SQL_ERROR, e);
		}
		
	}

	/**
	 * Gets a PreparedStatement object for Id access.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */
	public PreparedStatement getIdStatement(Integer key){
		return (PreparedStatement)m_prepStatements.get(key);
	}
	
	/**
	 * Gets a PreparedStatement object and remove it from the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */
	public PreparedStatement getPreparedStatement(Integer key) throws CmsException {
		PreparedStatement pstmt = null;
		int num;
		Vector temp = (Vector) m_prepStatements.get(key);
		
		synchronized (temp) {
			if (temp.size() > 0) {
				pstmt =(PreparedStatement) temp.firstElement();
				temp.removeElementAt(0);
			}
			else {
				String sql =(String) m_prepStatementsCache.get(key);

				if (count > (m_maxConn - 1)) {
					count = 0;
				}
				Connection conn = (Connection) m_connections.elementAt(count);
				count++;
				
				try {
					pstmt = conn.prepareStatement(sql);
                    pstmt.clearParameters();
				}
				catch (SQLException e) {
					throw new CmsException(CmsException.C_SQL_ERROR, e);
				}
			}
			temp.notify();
		}
		   
        //System.err.println("**** --> key: "+key+" *** "+pstmt);
		return pstmt;
	}
	
	/**
	 * Add the given statement to the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @param pstmt - the statement
	 */
	public void putPreparedStatement(Integer key, PreparedStatement pstmt) {
		/* TODO: 
		 * 
		 * Bei dem Versuch, ein Statement in den Vector zu schreiben, muss
		 * kontrolliert werden, ob das Statement ein neu geschaffenes Statement ist.
		 * Wenn ja, dann Statement verwerfen, ansonsten eintragen ...
		 */
        //System.err.println("**** <-- key: "+key+" *** "+pstmt);
		Vector temp = (Vector) m_prepStatements.get(key);
		
		synchronized (temp) {
			temp.addElement(pstmt);
			temp.notify();
		}
	}
	
	/**
	 * Returns a vector with all connections.
	 * 
	 * @return a vector with all connections
	 */
	public Vector getAllConnections() {
		
		return m_connections;
	}
	
	/**
	 * Returns all statements matching the key.
	 * 
	 * @param key - the hashtable key
	 * @return all prepared statements matching the key
	 */
	public Vector getAllPreparedStatement(String key) {
		Vector temp = (Vector) m_prepStatements.get(key);
		
		return temp;
	}

	/**
	 * Returns all statements.
	 * 
	 * @return all prepared statements
	 */
	public Hashtable getAllPreparedStatement() {
		return m_prepStatements;
	}
}
