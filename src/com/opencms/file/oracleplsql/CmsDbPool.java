package com.opencms.file.oracleplsql;

/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsDbPool.java,v $
 * Date   : $Date: 2001/02/01 15:37:21 $
 * Version: $Revision: 1.4 $
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
import oracle.jdbc.driver.*;
import com.opencms.file.genericSql.I_CmsDbPool; 

import com.opencms.core.*;

/**
 * This class is used to create an pool of prepared statements.
 * 
 * @author u.roland
 * @author a.schouten
 */
public class CmsDbPool extends com.opencms.file.genericSql.CmsDbPool {
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
		// create a new DB-Pool
		super(driver, url, user, passwd, maxConn);
/*
		Connection conn = null;
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			
			try {
				// setAutoCommit=false because of using SELECT..FOR UPDATE-Statements and performance
				// all insert- and update-statements must be committed manually with
				// getNextPreparedStatement(C_COMMIT);
				conn.setAutoCommit(false);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
*/
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
	 * Init the CallableStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL callable-statement that may contain one or more '?' IN and OUT parameter placeholders
	 */
	public void initCallableStatement(Integer key, String sql) throws CmsException {
		Connection conn = null;
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			
			try {
				CallableStatement cstmt = conn.prepareCall(sql);
				tmp.put(key, cstmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
	/**
	 * Init the CallableStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL callable-statement that may contain one or more '?' IN and OUT parameter placeholders
	 */
	public void initOracleCallableStatement(Integer key, String sql) throws CmsException {
		Connection conn = null;
		
		for (int i = 0; i < m_maxConn; i++) {
			conn = (Connection) m_connections.elementAt(i);
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			
			try {
				OracleCallableStatement cstmt = (OracleCallableStatement) conn.prepareCall(sql);
				tmp.put(key, cstmt);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
	/**
	 * Init the CallableStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param parameterIndex - the first parameter is 1, the second is 2,... 
	 * @param sqlType - SQL type code defined by java.sql.Types; for parameters of type Numeric or Decimal 
	 * use the version of registerOutParameter that accepts a scale value
	 */
	public void initRegisterOutParameter(Integer key, int parameterIndex, int sqlType) throws CmsException {
				
		for (int i = 0; i < m_maxConn; i++) {
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			CallableStatement cstmt = (CallableStatement) tmp.get(key);
			
			try {
				cstmt.registerOutParameter(parameterIndex, sqlType);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
	/**
	 * Init the CallableStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param parameterIndex - the first parameter is 1, the second is 2,... 
	 * @param sqlType - SQL type code defined by java.sql.Types; for parameters of type Numeric or Decimal 
	 * use the version of registerOutParameter that accepts a scale value
	 * @param scale - a value greater than or equal to zero representing the desired number of digits to the right of the decimal point 
	 */
	public void initRegisterOutParameter(Integer key, int parameterIndex, int sqlType, int scale) throws CmsException {
				
		for (int i = 0; i < m_maxConn; i++) {
			Hashtable tmp = (Hashtable) m_prepStatements.elementAt(i);
			CallableStatement cstmt = (CallableStatement) tmp.get(key);
			
			try {
				cstmt.registerOutParameter(parameterIndex, sqlType, scale);
			}
			catch (SQLException e) {
				throw new CmsException(CmsException.C_SQL_ERROR, e);
			}
		}
	}
}
