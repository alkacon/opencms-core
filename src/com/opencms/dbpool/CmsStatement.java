package com.opencms.dbpool;

/*
 *
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsStatement.java,v $
 * Date   : $Date: 2001/02/06 12:42:38 $
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
import source.org.apache.java.util.*;

/**
 * This class is used to create an connection-pool for opencms.
 *
 * @author a.schouten
 */
public class CmsStatement implements Statement {

	/**
	 * The original statement
	 */
	protected Statement m_originalStatement = null;

	/**
	 * The hook to the connection
	 */
	protected CmsConnection m_con = null;

        /**
         * The default-constructor to create a new statement
         */
         CmsStatement() {
          super();
         }

        /**
         * The constructor to create a new statement
         */
         CmsStatement(Statement originalStatement, CmsConnection con) {
            m_con = con;
            m_originalStatement = originalStatement;
         }

        public ResultSet executeQuery(String sql) throws SQLException {

          return m_originalStatement.executeQuery(sql);
        }

        public int executeUpdate(String sql) throws SQLException {

          return m_originalStatement.executeUpdate(sql);
        }

        public int getMaxFieldSize() throws SQLException {

          return m_originalStatement.getMaxFieldSize();
        }

        public void setMaxFieldSize(int max) throws SQLException {

          m_originalStatement.setMaxFieldSize(max);
        }

        public int getMaxRows() throws SQLException {

          return m_originalStatement.getMaxRows();
        }

        public void setMaxRows(int max) throws SQLException {

          m_originalStatement.setMaxRows(max);
        }

        public void setEscapeProcessing(boolean enable) throws SQLException {

          m_originalStatement.setEscapeProcessing(enable);
        }

        public int getQueryTimeout() throws SQLException {

          return m_originalStatement.getQueryTimeout();
        }

        public void setQueryTimeout(int seconds) throws SQLException {

          m_originalStatement.setQueryTimeout(seconds);
        }

        public void cancel() throws SQLException {

          m_originalStatement.cancel();
        }

        public SQLWarning getWarnings() throws SQLException {

          return m_originalStatement.getWarnings();
        }

        public void clearWarnings() throws SQLException {

          m_originalStatement.clearWarnings();
        }

        public void setCursorName(String name) throws SQLException {

          m_originalStatement.setCursorName(name);
        }

        public boolean execute(String sql) throws SQLException {

          return m_originalStatement.execute(sql);
        }

        public ResultSet getResultSet() throws SQLException {

          return m_originalStatement.getResultSet();
        }

        public int getUpdateCount() throws SQLException {

          return m_originalStatement.getUpdateCount();
        }

        public void addBatch(String sql) throws SQLException {

          m_originalStatement.addBatch(sql);
        }

        public void clearBatch() throws SQLException {

          m_originalStatement.clearBatch();
        }

        public int[] executeBatch() throws SQLException {

          return m_originalStatement.executeBatch();
        }

        public int getResultSetType() throws SQLException {

          return m_originalStatement.getResultSetType();
        }

        public int getResultSetConcurrency() throws SQLException {

          return m_originalStatement.getResultSetConcurrency();
        }

        public int getFetchSize() throws SQLException {

          return m_originalStatement.getFetchSize();
        }

        public void setFetchSize(int rows) throws SQLException {

          m_originalStatement.setFetchSize(rows);
        }

        public int getFetchDirection() throws SQLException {

          return m_originalStatement.getFetchDirection();
        }

        public void setFetchDirection(int direction) throws SQLException {

          m_originalStatement.setFetchDirection(direction);
        }


        public boolean getMoreResults() throws SQLException {

          return m_originalStatement.getMoreResults();
        }

        public Connection getConnection() throws SQLException {

          return m_con;
        }

        public void close() throws SQLException {
          // do nothing, because this statement can be reused
        }

       	/**
	 * This method calls close, to put the connection back to the pool.
	 */
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}


	/**
	 * Try to close this statement without putting it back to the pool.
	 */
	void closeOriginalStatement() {
          try {
                  m_originalStatement.close();
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
		output.append(m_originalStatement);
		return output.toString();
	}

}