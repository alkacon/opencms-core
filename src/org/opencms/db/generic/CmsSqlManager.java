/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsSqlManager.java,v $
 * Date   : $Date: 2003/08/20 13:14:52 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.db.generic;

import org.opencms.db.CmsDbPool;
import org.opencms.db.I_CmsSqlManager;
import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.exceptions.CmsResourceNotFoundException;
import com.opencms.file.CmsProject;
import com.opencms.flex.util.CmsStringSubstitution;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Handles SQL queries from query.properties of the generic (ANSI-SQL) driver package.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.10 $ $Date: 2003/08/20 13:14:52 $
 * @since 5.1
 */
public class CmsSqlManager extends Object implements Serializable, Cloneable, I_CmsSqlManager {
    
    /**
     * The shared instance of this SqlManager.
     */
    private static I_CmsSqlManager sharedInstance = null;
    
    /**
     * The filename/path of the SQL query properties file.
     */
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/generic/query.properties";
    
    /**
     * The properties hash holding the SQL queries.
     */
    private static Properties c_queries = null;
    
    /**
     * The URL to access the correct connection pool.
     */
    protected String m_dbPoolUrl;
    
    /**
     * This map caches all queries with replaced expressions to minimize costs of regex/matching operations.
     */
    protected Map m_cachedQueries;
    
    /**
     * Table pattern being replaced in SQL queries to generate SQL queries to access online/offline tables.
     */
    protected static final String C_TABLE_KEY_SEARCH_PATTERN = "_T_";

    /**
     * CmsSqlManager constructor.<p>
     * 
     * Never invoke this constructor! Use {@link org.opencms.db.generic.CmsSqlManager#getInstance(String)} instead.
     * 
     * @param dbPoolUrl the URL to access the connection pool
     */
    protected CmsSqlManager(String dbPoolUrl) {
        m_dbPoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + dbPoolUrl;

        if (c_queries == null) {
            c_queries = loadQueryProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        m_cachedQueries = (Map) new HashMap();
    }
    
    /**
     * Returns the shared instance of the generic SQL manager.<p>
     * 
     * @param dbPoolUrl the URL to access the connection pool
     * @return the shared instance of the generic SQL manager
     */
    public static synchronized I_CmsSqlManager getInstance(String dbPoolUrl) {
        if (sharedInstance == null) {
            sharedInstance = (I_CmsSqlManager) new org.opencms.db.generic.CmsSqlManager(dbPoolUrl);
        }

        return sharedInstance;        
    }
    
    /**
     * CmsSqlManager constructor.<p>
     * 
     * @param dbPoolUrl the URL to access the correct connection pool
     * @param loadQueries flag indicating whether the query.properties should be loaded during initialization
     */
    protected CmsSqlManager(String dbPoolUrl, boolean loadQueries) {
        m_dbPoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + dbPoolUrl;
    
        if (loadQueries && c_queries == null) {
            c_queries = loadQueryProperties(C_PROPERTY_FILENAME);   
            precalculateQueries(c_queries);         
        }
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#closeAll(java.sql.Connection, java.sql.Statement, java.sql.ResultSet)
     */
    public void closeAll(Connection con, Statement stmnt, ResultSet res) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }

            if (stmnt != null) {
                stmnt.close();
            }

            if (res != null) {
                res.close();
            }
        } catch (SQLException e) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] error closing JDBC connection/statement/result: " + e.toString());
            }
        } finally {
            res = null;
            stmnt = null;
            con = null;
        }
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if (c_queries != null) {
            c_queries.clear();
        }

        if (m_cachedQueries != null) {
            m_cachedQueries.clear();
        }

        c_queries = null;
        m_cachedQueries = null;
        m_dbPoolUrl = null;

        super.finalize();
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#get(com.opencms.file.CmsProject, java.lang.String)
     */
    public String get(CmsProject project, String queryKey) {
        return get(project.getId(), queryKey);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#get(int, java.lang.String)
     */
    public String get(int projectId, String queryKey) {       
        // get the SQL statement from the properties hash
        String query = get(queryKey);        
        
        // replace control chars.
        query = CmsStringSubstitution.substitute(query,"\t"," ");
        query = CmsStringSubstitution.substitute(query,"\n"," ");         

        if (projectId < 0) {
            // a project ID < 0 is an internal indicator that a project-independent 
            // query was requested- further regex operations are not required then!
            return query;
        }
        
        if (!m_cachedQueries.containsKey(queryKey)) {            
            // make the statement project dependent
            query = CmsSqlManager.replaceTableKey(projectId, query);
            
            // to minimize costs, all statements with replaced expressions are cached in a map
            queryKey += (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) ? "_ONLINE" : "_OFFLINE";
            m_cachedQueries.put(queryKey, query);
        } else {
            // use the statement where the pattern is already replaced
            query = (String) m_cachedQueries.get(queryKey);            
        }

        return query;
    }
    
    /**
     * Replaces the search pattern _T_ in SQL queries by the pattern _ONLINE_ or _OFFLINE_ 
     * depending on the ID of the current project.<p> 
     * 
     * @param projectId the ID of the current project
     * @param query the SQL query
     * @return String the SQL query with the table key search pattern replaced
     */
    public static String replaceTableKey(int projectId, String query) {
        // make the statement project dependent
        String replacePattern = (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) ? "_ONLINE_" : "_OFFLINE_";
        query = CmsStringSubstitution.substitute(query, C_TABLE_KEY_SEARCH_PATTERN, replacePattern);

        return query;
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#get(java.lang.String)
     */
    public String get(String queryKey) {
        if (c_queries == null) {
            /*
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
            */
            throw new RuntimeException(this.getClass().getName() + " is not initialized!");
        }

        String value = null;
        if ((value = c_queries.getProperty(queryKey)) == null) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] query '" + queryKey + "' not found in " + C_PROPERTY_FILENAME);
            }
        }

        return value;
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getBytes(java.sql.ResultSet, java.lang.String)
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {       
        return res.getBytes(attributeName);
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#getCmsException(java.lang.Object, java.lang.String, int, java.lang.Throwable, boolean)
     */
    public CmsException getCmsException(Object o, String message, int exceptionType, Throwable rootCause, boolean logSilent) {
        String className = "";
        
        if (o!=null) {            
            className = "[" + o.getClass().getName() + "] ";
        }
        
        if (rootCause != null) {            
            StackTraceElement[] stackTraceElements = rootCause.getStackTrace();
            String stackTraceElement = "";
            
            // i want to see only the first stack trace element of 
            // my own OpenCms classes in the log message...
            for (int i=0;i<stackTraceElements.length;i++) {
                String currentStackTraceElement = stackTraceElements[i].toString();
                if (currentStackTraceElement.indexOf(".opencms.")!=-1) {
                    stackTraceElement = currentStackTraceElement;
                    break;
                }
            }
            
            if (message != null)
            	message = "[" + message + "] ";
            else
            	message = "";
            	
            // where did we crash?
            message += "where: " + stackTraceElement + ", ";
            // why did we crash?
            message += "why: " + rootCause.toString();
        }
        
        message = className + message;

        if (!logSilent && OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, message);
        }

        switch (exceptionType) {
            case CmsException.C_NOT_FOUND :
                return new CmsResourceNotFoundException(message);
        }

        return new CmsException(message, exceptionType, rootCause);
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#getConnection()
     */
    public Connection getConnection() throws SQLException {
        // To receive a JDBC connection from the offline pool, 
        // a non-existent dummy project-ID is used
        return getConnection(Integer.MIN_VALUE);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getConnection(com.opencms.file.CmsProject)
     */
    public Connection getConnection(CmsProject project) throws SQLException {
        return getConnection(project.getId());
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getConnection(int)
     */
    public Connection getConnection(int projectId) throws SQLException {
        return DriverManager.getConnection(m_dbPoolUrl);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getConnectionForBackup()
     */
    public Connection getConnectionForBackup() throws SQLException {
        return DriverManager.getConnection(m_dbPoolUrl);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getPreparedStatement(java.sql.Connection, com.opencms.file.CmsProject, java.lang.String)
     */
    public PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey) throws SQLException {
        return getPreparedStatement(con, project.getId(), queryKey);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getPreparedStatement(java.sql.Connection, int, java.lang.String)
     */
    public PreparedStatement getPreparedStatement(Connection con, int projectId, String queryKey) throws SQLException {
        String rawSql = get(projectId, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#getPreparedStatement(java.sql.Connection, java.lang.String)
     */
    public PreparedStatement getPreparedStatement(Connection con, String queryKey) throws SQLException {
        String rawSql = get(Integer.MIN_VALUE, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#getPreparedStatementForSql(java.sql.Connection, java.lang.String)
     */
    public PreparedStatement getPreparedStatementForSql(Connection con, String query) throws SQLException {
        // unfortunately, this wrapper is essential, because some JDBC driver 
        // implementations don't accept the delegated objects of DBCP's connection pool. 
        return con.prepareStatement(query);
    }    
    
    /**
     * Loads a Java properties hash.
     * 
     * @param propertyFilename the package/filename of the properties hash
     * @return Properties the new properties instance.
     */
    protected synchronized Properties loadQueryProperties(String propertyFilename) {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        } catch (NullPointerException exc) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] error loading " + propertyFilename);
            }
            properties = null;
        } catch (java.io.IOException exc) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] error loading " + propertyFilename);
            }
            properties = null;
        }

        return properties;
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#nextId(java.lang.String)
     */
    public synchronized int nextId(String tableName) throws CmsException {
        return org.opencms.db.CmsIdGenerator.nextId(m_dbPoolUrl, tableName);
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#setBytes(java.sql.PreparedStatement, int, byte[])
     */
    public void setBytes(PreparedStatement statement, int posn, byte[] content) throws SQLException {
        if (content.length < 2000) {
            statement.setBytes(posn, content);
        } else {
            statement.setBinaryStream(posn, new ByteArrayInputStream(content), content.length);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSqlManager#validateNull(java.lang.String)
     */
    public String validateNull(String value) {
        if (value != null && value.length() != 0) {
            return value;
        }

        return " ";
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#commit(java.sql.Connection)
     */
    public void commit(Connection conn) {
        try {
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            getCmsException(this, e.getMessage(), CmsException.C_SQL_ERROR, e, false);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#rollback(java.sql.Connection, java.sql.Savepoint)
     */
    public void rollback(Connection conn, Savepoint savepoint) {
        try {
            if (!conn.getAutoCommit()) {
                if (savepoint!=null) {
                    conn.rollback(savepoint);
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            getCmsException(this, e.getMessage(), CmsException.C_SQL_ERROR, e, false);
        }        
    } 
    
    /**
     * @see org.opencms.db.I_CmsSqlManager#releaseSavepoint(java.sql.Connection, java.sql.Savepoint)
     */
    public void releaseSavepoint(Connection conn, Savepoint savepoint) {
        try {
            if (!conn.getAutoCommit()) {
                conn.releaseSavepoint(savepoint);
            }
        } catch (SQLException e) {
            getCmsException(this, e.getMessage(), CmsException.C_SQL_ERROR, e, false);
        }        
    }
    
    /**
     * Replaces patterns ${XXX} by another property value, if XXX is a property key with a value.
     * 
     * @param properties a hash containt key/value coded SQL statements
     */
    protected synchronized void precalculateQueries(Properties properties) {
        String currentKey = null;
        String currentValue = null;
        int startIndex = 0;
        int endIndex = 0;
        int lastIndex = 0;

        Iterator allKeys = properties.keySet().iterator();
        while (allKeys.hasNext()) {
            currentKey = (String) allKeys.next();
            currentValue = (String) properties.get(currentKey);
            startIndex = endIndex = lastIndex = 0;

            while ((startIndex = currentValue.indexOf("${", lastIndex)) != -1) {
                if ((endIndex = currentValue.indexOf('}', startIndex)) != -1) {
                    String replaceKey = currentValue.substring(startIndex + 2, endIndex);
                    String searchPattern = currentValue.substring(startIndex, endIndex + 1);
                    String replacePattern = this.get(replaceKey);

                    if (replacePattern != null) {
                        currentValue = CmsStringSubstitution.substitute(currentValue, searchPattern, replacePattern);
                    }

                    lastIndex = endIndex + 2;
                }
            }

            properties.put(currentKey, currentValue);
        }
    }  
    
}