/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsStatement.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.5 $
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * The object used for executing a static SQL statement and returning the results it produces.
 *
 * @author Andreas Schouten
 * @see java.sql.Statement
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
        
        public boolean getMoreResults(int param) throws java.sql.SQLException {
            
            return m_originalStatement.getMoreResults(param);
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
    
    public boolean execute(String str, int param) throws java.sql.SQLException {
        return m_originalStatement.execute(str, param);
    }
    
    public boolean execute(String str, String[] str1) throws java.sql.SQLException {
         return m_originalStatement.execute(str, str1);
    }
    
    public boolean execute(String str, int[] values) throws java.sql.SQLException {
         return m_originalStatement.execute(str, values);
    }
    
    public int executeUpdate(String str, String[] str1) throws java.sql.SQLException {
         return m_originalStatement.executeUpdate(str, str1);
    }
    
    public int executeUpdate(String str, int[] values) throws java.sql.SQLException {
         return m_originalStatement.executeUpdate(str, values);
    }
    
    public int executeUpdate(String str, int param) throws java.sql.SQLException {
         return m_originalStatement.executeUpdate(str, param);
    }
    
    public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException {
         return m_originalStatement.getGeneratedKeys();
    }
    
    public int getResultSetHoldability() throws java.sql.SQLException {
         return m_originalStatement.getResultSetHoldability();
    }
    
}