/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsSqlManager.java,v $
 * Date   : $Date: 2003/09/17 13:01:30 $
 * Version: $Revision: 1.20 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.exceptions.CmsResourceNotFoundException;
import com.opencms.file.CmsProject;

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
 * @version $Revision: 1.20 $ $Date: 2003/09/17 13:01:30 $
 * @since 5.1
 */
public class CmsSqlManager extends Object implements Serializable, Cloneable {

    /**
     * The filename/path of the SQL query properties file.
     */
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/generic/query.properties";

    /**
     * The properties hash holding the SQL queries.
     */
    private static Properties c_queries = null;

    /**
     * Table pattern being replaced in SQL queries to generate SQL queries to access online/offline tables.
     */
    protected static final String C_TABLE_KEY_SEARCH_PATTERN = "_T_";

    protected String m_backupPoolUrl;

    /**
     * This map caches all queries with replaced expressions to minimize costs of regex/matching operations.
     */
    protected Map m_cachedQueries;

    /**
     * The URL to access the correct connection pool.
     */
    protected String m_offlinePoolUrl;

    protected String m_onlinePoolUrl;

    /**
     * Initializes the SQL manager.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @see #setOfflinePoolUrl(String)
     * @see #setOnlinePoolUrl(String)
     * @see #setBackupPoolUrl(String)
     */
    public CmsSqlManager() {
        m_offlinePoolUrl = null;
        m_onlinePoolUrl = null;
        m_backupPoolUrl = null;

        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        m_cachedQueries = (Map)new HashMap();
    }

    /**
     * Initializes the SQL manager.<p>
     * 
     * Per default, the same JDBC pool URL is used to obtain JDBC 
     * connections from one pool.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @param poolUrl the default connection pool URL
     * @param loadQueries flag indicating whether the query.properties should be loaded during initialization
     * @see setOfflinePoolUrl(String)
     * @see setOnlinePoolUrl(String)
     * @see setBackupPoolUrl(String)
     */
    protected CmsSqlManager(String poolUrl, boolean loadQueries) {
        m_offlinePoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl;

        if (loadQueries && c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }
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
     * Attemts to close the connection, statement and result set after a statement has been executed.<p>
     * 
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
     * @see com.opencms.dbpool.CmsConnection#close()
     */
    public void closeAll(Connection con, Statement stmnt, ResultSet res) {

        // NOTE: we have to close Connections/Statements that way, because a dbcp PoolablePreparedStatement
        // is not a DelegatedStatement; for that reason its not removed from the trace of the connection when it is closed.
        // So, the connection tries to close it again when the connection is closed itself; 
        // as a result there is an error that forces the connection to be destroyed and not pooled

        try {
            // first, close the connection and (eventually) implicitly all assigned statements and result sets
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        }

        try {
            // close the statement and (normally) implicitly all assigned result sets
            if (stmnt != null) {
                stmnt.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        }

        try {
            // close the result set          
            if (res != null) {
                res.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        }

        res = null;
        stmnt = null;
        con = null;
    }

    /**
     * Makes all changes permanent since the previous commit/rollback if auto-commit is turned off.
     * 
     * @param conn the connection to commit
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
        m_offlinePoolUrl = null;

        super.finalize();
    }

    /**
     * Searches for the SQL query with the specified key and CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String get(CmsProject project, String queryKey) {
        return get(project.getId(), queryKey);
    }

    /**
     * Searches for the SQL query with the specified key and project-ID.<p>
     * 
     * The pattern "_T_" in table names is replaced with "_ONLINE_" or 
     * "_OFFLINE_" to choose the right database tables for SQL queries 
     * that are project dependent!
     * 
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String get(int projectId, String queryKey) {
        // get the SQL statement from the properties hash
        String query = get(queryKey);

        // replace control chars.
        query = CmsStringSubstitution.substitute(query, "\t", " ");
        query = CmsStringSubstitution.substitute(query, "\n", " ");

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
            query = (String)m_cachedQueries.get(queryKey);
        }

        return query;
    }

    /**
     * Searches for the SQL query with the specified key.<p>
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String get(String queryKey) {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        String value = null;
        if ((value = c_queries.getProperty(queryKey)) == null) {
            if (OpenCms.getLog(CmsLog.CHANNEL_MAIN).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_MAIN).error("[" + this.getClass().getName() + "] query '" + queryKey + "' not found in " + C_PROPERTY_FILENAME);
            }
        }

        return value;
    }

    /**
     * Gets the backup pool url.<p>
     * 
     * @return backup pool url
     */
    public String getBackupPoolUrl() {
        return m_backupPoolUrl;
    }

    /**
     * Retrieves the value of the designated column in the current row of this ResultSet object as 
     * a byte array in the Java programming language.<p>
     * 
     * The bytes represent the raw values returned by the driver. Overwrite this method if another 
     * database server requires a different handling of byte attributes in tables.
     * 
     * @param res the result set
     * @param attributeName the name of the table attribute
     * @return byte[] the column value; if the value is SQL NULL, the value returned is null 
     * @throws SQLException if a database access error occurs
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {
        return res.getBytes(attributeName);
    }

    /**
     * Wraps an exception in a new CmsException object.<p>
     * 
     * Optionally, a log message is written to the "critical" OpenCms logging channel.
     * 
     * @param o the object caused the exception
     * @param message a message that is written to the log
     * @param exceptionType the type of the exception
     * @param rootCause the exception that was thrown
     * @param logSilent if TRUE, no entry to the log is written
     * @return CmsException
     */
    public CmsException getCmsException(Object o, String message, int exceptionType, Throwable rootCause, boolean logSilent) {
        String className = "";

        if (o != null) {
            className = "[" + o.getClass().getName() + "] ";
        }

        if (rootCause != null) {
            StackTraceElement[] stackTraceElements = rootCause.getStackTrace();
            String stackTraceElement = "";

            // i want to see only the first stack trace element of 
            // my own OpenCms classes in the log message...
            for (int i = 0; i < stackTraceElements.length; i++) {
                String currentStackTraceElement = stackTraceElements[i].toString();
                if (currentStackTraceElement.indexOf(".opencms.") != -1) {
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

        if (!logSilent && OpenCms.getLog(CmsLog.CHANNEL_MAIN).isErrorEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_MAIN).error(message);
        }

        switch (exceptionType) {
            case CmsException.C_NOT_FOUND :
                return new CmsResourceNotFoundException(message);
            default:
        }

        return new CmsException(message, exceptionType, rootCause);
    }

    /**
     * Receives a JDBC connection from the (offline) pool.<p>
     * 
     * To do so, Integer.MAX_VALUE is used as a non-existent dummy project-ID.<p>
     * 
     * Use this method with caution! Using this method makes only sense to read/write project 
     * independent data such as user data!
     * 
     * @return a JDBC connection from the (offline) pool 
     * @throws SQLException if a database access error occurs
     * @see getConnection(int)
     */
    public Connection getConnection() throws SQLException {
        // To receive a JDBC connection from the offline pool, a non-existent dummy project-ID is used
        return getConnection(Integer.MAX_VALUE);
    }

    /**
     * Receives a JDBC connection from the pool specified by the given CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     * @see getConnection(int)
     */
    public Connection getConnection(CmsProject project) throws SQLException {
        return getConnection(project.getId());
    }

    /**
     * Receives a JDBC connection from the pool specified by the given ID.<p>
     * 
     * The ID is (usually) the ID of the current project.<p>
     * 
     * <ul>
     * <li>Offline: ID &gt; 1</li>
     * <li>Online: ID = 1</li>
     * <li>Backup: ID &lt; 0</li>
     * </ul>
     * 
     * @param id is matched to the correct JDBC pool URL to obtain a connection from the DriverManager
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(int id) throws SQLException {
        Connection conn = null;

        if (id > 1) {
            conn = DriverManager.getConnection(m_offlinePoolUrl);
            //conn = DriverManager.getConnection(CmsDbPool.getOfflinePoolUrl());
        } else if (id == 1) {
            conn = DriverManager.getConnection(m_onlinePoolUrl);
            //conn = DriverManager.getConnection(CmsDbPool.getOnlinePoolUrl());
        } else if (id < 0) {
            conn = DriverManager.getConnection(m_backupPoolUrl);
            //conn = DriverManager.getConnection(CmsDbPool.getBackupPoolUrl());
        } else {
            conn = DriverManager.getConnection(m_offlinePoolUrl);
            //conn = DriverManager.getConnection(CmsDbPool.getOfflinePoolUrl());
        }
        /*
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_DEBUG)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "getConnection/" + id + " returning " + conn.toString());
        }    
        */
        return conn;
    }

    /**
     * Receives a JDBC connection from the backup pool.<p>
     * 
     * To do so, -1 is used as a non-existent dummy project-ID.<p>
     * 
     * Use this method with caution! Using this method makes only sense to read/write 
     * data to backup data. 
     * 
     * @return a JDBC connection from the backup pool 
     * @throws SQLException if a database access error occurs
     * @see getConnection(int)
     */
    public Connection getConnectionForBackup() throws SQLException {
        return getConnection(-1);
    }

    /**
     * Gets the offline pool url.<p>
     * 
     * @return offline pool url
     */
    public String getOfflinePoolUrl() {
        return m_offlinePoolUrl;
    }

    /**
     * Gets the online pool url.<p>
     * 
     * @return online pool url
     */
    public String getOnlinePoolUrl() {
        return m_onlinePoolUrl;
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query
     * and the CmsProject.
     * 
     * @param con the JDBC connection
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey) throws SQLException {
        return getPreparedStatement(con, project.getId(), queryKey);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query
     * and the project-ID.
     * 
     * @param con the JDBC connection
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, int projectId, String queryKey) throws SQLException {
        String rawSql = get(projectId, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query.
     * 
     * @param con the JDBC connection
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, String queryKey) throws SQLException {
        String rawSql = get(Integer.MIN_VALUE, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the SQL query.
     * 
     * @param con the JDBC connection
     * @param query the kSQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatementForSql(Connection con, String query) throws SQLException {
        // unfortunately, this wrapper is essential, because some JDBC driver 
        // implementations don't accept the delegated objects of DBCP's connection pool. 
        PreparedStatement stmt = con.prepareStatement(query);
        /*
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_DEBUG)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "getPreparedStatementForSql/" + con.toString() + " (" + query + ") returning " + stmt.toString());
        }
        */
        return stmt;
    }

    /**
     * Loads a Java properties hash.
     * 
     * @param propertyFilename the package/filename of the properties hash
     * @return Properties the new properties instance.
     */
    protected synchronized Properties loadProperties(String propertyFilename) {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        } catch (NullPointerException exc) {
            if (OpenCms.getLog(CmsLog.CHANNEL_MAIN).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_MAIN).error("[" + this.getClass().getName() + "] error loading " + propertyFilename);
            }
            properties = null;
        } catch (java.io.IOException exc) {
            if (OpenCms.getLog(CmsLog.CHANNEL_MAIN).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_MAIN).error("[" + this.getClass().getName() + "] error loading " + propertyFilename);
            }
            properties = null;
        }

        return properties;
    }

    /**
     * Generates a new primary key for a given database table. IMPORTANT: this method makes only
     * sense for old-style tables where the primary key is NOT a CmsUUID!
     * 
     * @param tableName the table for which a new primary key should be generated.
     * @return int the new primary key
     * @throws CmsException if an error occurs
     */
    public synchronized int nextId(String tableName) throws CmsException {
        return org.opencms.db.CmsDbUtil.nextId(m_offlinePoolUrl, tableName);
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
            currentKey = (String)allKeys.next();
            currentValue = (String)properties.get(currentKey);
            startIndex = 0;
            endIndex = 0;
            lastIndex = 0;

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

    /**
     * Removes the given Savepoint object from the current transaction.
     * 
     * @param conn the connection from which the savepoint object is removed
     * @param savepoint the Savepoint object to be removed 
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
     * Undoes all changes made in the current transaction, optionally after the given Savepoint object was set.
     * 
     * @param conn the connection to roll back
     * @param savepoint an optional savepoint after which all changes are rolled back
     */
    public void rollback(Connection conn, Savepoint savepoint) {
        try {
            if (!conn.getAutoCommit()) {
                if (savepoint != null) {
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
     * Sets the backup pool url.<p>
     * 
     * @param poolUrl backup pool url
     */
    public void setBackupPoolUrl(String poolUrl) {
        if (poolUrl != null) {
            m_backupPoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl;
        }
    }

    /**
     * Sets the designated parameter to the given Java array of bytes. The driver converts this 
     * to an SQL VARBINARY or LONGVARBINARY (depending on the argument's size relative to the 
     * driver's limits on VARBINARY values) when it sends it to the database. 
     * 
     * @param statement the PreparedStatement where the content is set
     * @param posn the first parameter is 1, the second is 2, ...
     * @param content the parameter value 
     * @throws SQLException if a database access error occurs
     */
    public void setBytes(PreparedStatement statement, int posn, byte[] content) throws SQLException {
        if (content.length < 2000) {
            statement.setBytes(posn, content);
        } else {
            statement.setBinaryStream(posn, new ByteArrayInputStream(content), content.length);
        }
    }

    /**
     * Sets the offline pool url.<p>
     * 
     * @param poolUrl offline pool url
     */
    public void setOfflinePoolUrl(String poolUrl) {
        if (poolUrl != null) {
            m_offlinePoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl;
        }
    }

    /**
     * Sets the online pool url.<p>
     * 
     * @param poolUrl online pool url
     */
    public void setOnlinePoolUrl(String poolUrl) {
        if (poolUrl != null) {
            m_onlinePoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl;
        }
    }

    /**
     * Replaces null Strings by an empty string.
     * 
     * @param value the string to validate
     * @return String the validate string or an empty string if the validated string is null
     */
    public String validateNull(String value) {
        if (value != null && value.length() != 0) {
            return value;
        }

        return " ";
    }

}