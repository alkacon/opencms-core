package com.opencms.dbpool;

/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsConnection.java,v $
 * Date   : $Date: 2001/01/31 16:46:19 $
 * Version: $Revision: 1.2 $
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
import source.org.apache.java.util.*;

/**
 * This class is used to create an connection-pool for opencms.
 *
 * @author a.schouten
 */
public class CmsConnection implements java.sql.Connection {

	/**
	 * The original connection to the db
	 */
	private java.sql.Connection m_originalConnection;

	/**
	 * The hook to the pool
	 */
	private CmsPool m_pool;

	/**
	 * Determines, if the connection is closed already
	 */
	private boolean m_isClosed = true;

	/**
	 * The time, when this connection was used the last time.
	 */
	private long m_lastUsed;

	/**
	 * Constructs a new connection.
	 */
	CmsConnection(java.sql.Connection originalConnection, CmsPool pool) {
		m_originalConnection = originalConnection;
		m_pool = pool;
		m_lastUsed = System.currentTimeMillis();
		m_isClosed = false;
	}

	/**
	 * If the connection was closed (and put back into the pool)
	 * this method throws an exception.
	 */
	private void checkIsClosed() throws SQLException {
		if(m_isClosed) {
			throw new SQLException("Connection was closed already.");
		}
	}

	public Statement createStatement() throws SQLException {
		checkIsClosed();
		return m_originalConnection.createStatement();
	}

	public Statement createStatement(int a, int b) throws SQLException {
		checkIsClosed();
		return m_originalConnection.createStatement(a, b);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkIsClosed();
		return m_originalConnection.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int a, int b) throws SQLException {
		checkIsClosed();
		return m_originalConnection.prepareStatement(sql, a, b);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		checkIsClosed();
		return m_originalConnection.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int a, int b) throws SQLException {
		checkIsClosed();
		return m_originalConnection.prepareCall(sql, a, b);
	}

	public String nativeSQL(String sql) throws SQLException {
		checkIsClosed();
		return m_originalConnection.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkIsClosed();
		m_originalConnection.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getAutoCommit();
	}

	public void commit() throws SQLException {
		checkIsClosed();
		m_originalConnection.commit();
	}

	public void rollback() throws SQLException {
		checkIsClosed();
		m_originalConnection.rollback();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		checkIsClosed();
		m_originalConnection.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		checkIsClosed();
		return m_originalConnection.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		checkIsClosed();
		m_originalConnection.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		checkIsClosed();
		m_originalConnection.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		checkIsClosed();
		m_originalConnection.clearWarnings();
	}

	public void setTypeMap(Map map) throws SQLException {
		checkIsClosed();
		m_originalConnection.setTypeMap(map);
	}

	public Map getTypeMap() throws SQLException {
		checkIsClosed();
		return m_originalConnection.getTypeMap();
	}

	/**
	 * Finds out, if this connection was closed.
	 */
	public boolean isClosed() throws SQLException {
		return m_isClosed || m_originalConnection.isClosed();
	}

	/**
	 * This method don't closes this connection. It puts it back to
	 * the pool. Please use it at the end of your database activity.
	 */
	public void close() throws SQLException {
		checkIsClosed();
		// set is closed to true
		m_isClosed = true;

		// put the connection back to the pool
		m_pool.putConnection(new CmsConnection(m_originalConnection, m_pool));
	}

	/**
	 * This method calls close, to put the connection back to the pool.
	 */
	protected void finalize() throws Throwable {
		// close the connection (put it back to the pool
		close();
		super.finalize();
	}

	/**
	 * Try to close this connection without putting it back to the pool.
	 */
	void closeOriginalConnection() {
		try {
			m_originalConnection.close();
		} catch(SQLException exc) {
			// todo: insert logging here
		}
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString() {
		StringBuffer output=new StringBuffer();
		output.append("[" + this.getClass().getName() + "]:");
		output.append(m_originalConnection);
		output.append(", ");
		output.append("isClosed: " + m_isClosed);
		return output.toString();
	}
}
