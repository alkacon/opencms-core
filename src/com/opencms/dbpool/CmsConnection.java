/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsConnection.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.9 $
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * A connection (session) with a specific database.
 *
 * @author Andreas Schouten
 * @see java.sql.Connection
 */
public class CmsConnection implements Connection {

    /**
     * Constant: key for simnple statements
     */
    private static final String C_SIMPLE_STATEMENT_KEY = "SIMPLE_STATEMENT";

    /**
     * The original connection to the db
     */
    private Connection m_originalConnection;

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
     * The time, when this connection was established.
     */
    private long m_establishTime;

        /**
         * A pool with all created different statements during the lifetime of
         * this connection.
         */
         private Hashtable m_statementPool = new Hashtable();

    /**
     * Constructs a new connection.
     */
    CmsConnection(Connection originalConnection, CmsPool pool) {
          this(originalConnection, pool, new Hashtable(), System.currentTimeMillis());
    }

    /**
     * Reconstructs a connection.
     */
    CmsConnection(Connection originalConnection, CmsPool pool, Hashtable statementPool, long establishTime) {
          m_originalConnection = originalConnection;
          m_pool = pool;
          // create an empty statement
          m_lastUsed = System.currentTimeMillis();
          m_establishTime = establishTime;
          m_statementPool = statementPool;
          m_isClosed = false;
    }

        /**
         * Returns the time (ms), when this connection was last used.
         */
        long getLastUsed() {
          return m_lastUsed;
        }

        /**
         * Returns the time (ms), when this connection was established.
         */
        long getEstablishedTime() {
          return m_establishTime;
        }

    /**
     * If the connection was closed (and put back into the pool)
     * this method throws an exception.
     */
    private void checkIsClosed() throws SQLException {
        if(m_isClosed) {
            throw new SQLException("Connection was already closed.");
        }
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
    
    public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        checkIsClosed();
        m_originalConnection.rollback(savepoint);
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
         * Creates a new CmsStatement from the pool.
         */
        public Statement createStatement() throws SQLException {
          checkIsClosed();
          String key = C_SIMPLE_STATEMENT_KEY;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsStatement(m_originalConnection.createStatement(), this));
          }
          // return the simple statement
          return (Statement) m_statementPool.get(key);
    }

        /**
         * Creates a new CmsStatement from the pool.
         */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
                throws SQLException {
          checkIsClosed();
          String key = C_SIMPLE_STATEMENT_KEY + ":" + resultSetType + ":" + resultSetConcurrency;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsStatement(m_originalConnection.createStatement(resultSetType,
              resultSetConcurrency ), this));
          }
          // return the simple statement
          return (Statement) m_statementPool.get(key);
    }

    public java.sql.Statement createStatement(int param, int param1, int param2) throws java.sql.SQLException {
          checkIsClosed();
          String key = C_SIMPLE_STATEMENT_KEY + ":" + param + ":" + param1 + ":" + param2;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsStatement(m_originalConnection.createStatement(param, param1, param2), this));
          }
          // return the simple statement
          return (Statement) m_statementPool.get(key);        
    }
        
        /**
         * Creates a new CmsPreparedStatement from the pool.
         */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
          checkIsClosed();
          String key = sql;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(sql), this));
          }
          return (PreparedStatement) m_statementPool.get(key);
    }

        /**
         * Creates a new CmsPreparedStatement from the pool.
         */
    public PreparedStatement prepareStatement(String sql, int a, int b) throws SQLException {
          checkIsClosed();
          String key = sql + ":" + a + ":" + b;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(sql, a, b), this));
          }
          return (PreparedStatement) m_statementPool.get(key);
    }
    
    public java.sql.PreparedStatement prepareStatement(String str, int param) throws java.sql.SQLException {
          checkIsClosed();
          String key = str + ":" + param;
          if(!m_statementPool.containsKey(key)) {
            // the pool does not contain this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(str, param), this));
          }
          return (PreparedStatement) m_statementPool.get(key);
    }
    
    public java.sql.PreparedStatement prepareStatement(String str, int[] values) throws java.sql.SQLException {
          checkIsClosed();
          String key = str + ":" + values;
          if(!m_statementPool.containsKey(key)) {
            // the pool does not contain this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(str, values), this));
          }
          return (PreparedStatement) m_statementPool.get(key);
    }
    
    public java.sql.PreparedStatement prepareStatement(String str, String[] str1) throws java.sql.SQLException {
          checkIsClosed();
          String key = str + ":" + str1;
          if(!m_statementPool.containsKey(key)) {
            // the pool does not contain this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(str, str1), this));
          }
          return (PreparedStatement) m_statementPool.get(key);
    }
    
    public java.sql.PreparedStatement prepareStatement(String str, int param, int param2, int param3) throws java.sql.SQLException {
          checkIsClosed();
          String key = str + ":" + param + ":" + param2 + ":" + param3;
          if(!m_statementPool.containsKey(key)) {
            // the pool does not contain this statement - create it
            m_statementPool.put(key,
              new CmsPreparedStatement(m_originalConnection.prepareStatement(str, param, param2, param3), this));
          }
          return (PreparedStatement) m_statementPool.get(key);        
    }
  
        /**
         * Creates a new CmsPreparedStatement from the pool.
         */
    public CallableStatement prepareCall(String sql) throws SQLException {
          String key = sql;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsCallableStatement(m_originalConnection.prepareCall(sql), this));
          }
          return (CallableStatement) m_statementPool.get(key);
    }

        /**
         * Creates a new CmsPreparedStatement from the pool.
         */
    public CallableStatement prepareCall(String sql, int a, int b) throws SQLException {
          String key = sql + ":" + a + ":" + b;
          if(!m_statementPool.containsKey(key)) {
            // the pool contains not this statement - create it
            m_statementPool.put(key,
              new CmsCallableStatement(m_originalConnection.prepareCall(sql, a, b), this));
          }
          return (CallableStatement) m_statementPool.get(key);
    }

    public java.sql.CallableStatement prepareCall(String str, int param, int param2, int param3) throws java.sql.SQLException {
         String key = str + ":" + param + ":" + param2 + ":" + param3;
          if(!m_statementPool.containsKey(key)) {
            // the pool does not contains this statement - create it
            m_statementPool.put(key,
              new CmsCallableStatement(m_originalConnection.prepareCall(str, param, param2, param3), this));
          }
          return (CallableStatement) m_statementPool.get(key);
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
        m_pool.putConnection(new CmsConnection(m_originalConnection, m_pool, m_statementPool, m_establishTime));
    }

    /**
     * This method calls close, to put the connection back to the pool.
     */
    protected void finalize() throws Throwable {
        // close the connection (put it back to the pool)
        close();
        super.finalize();
    }

    /**
     * Try to close this connection without putting it back to the pool.
     */
    void closeOriginalConnection() {
          // close all statements
          m_isClosed = true;
          Enumeration keys = m_statementPool.keys();
          while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            CmsStatement statement = (CmsStatement)m_statementPool.get(key);
            statement.closeOriginalStatement();
          }
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
    
    public int getHoldability() throws java.sql.SQLException {
        checkIsClosed();
        return m_originalConnection.getHoldability();        
    }
    
    public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        checkIsClosed();
        m_originalConnection.releaseSavepoint(savepoint);        
    }
    
    public void setHoldability(int param) throws java.sql.SQLException {
        checkIsClosed();
        m_originalConnection.setHoldability(param);
    }
    
    public java.sql.Savepoint setSavepoint() throws java.sql.SQLException {
        checkIsClosed();
        return m_originalConnection.setSavepoint();        
    }
    
    public java.sql.Savepoint setSavepoint(String str) throws java.sql.SQLException {
        checkIsClosed();
        return m_originalConnection.setSavepoint(str);        
    }
    
}
