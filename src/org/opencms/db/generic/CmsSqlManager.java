/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsSqlManager.java,v $
 * Date   : $Date: 2004/07/18 16:32:08 $
 * Version: $Revision: 1.36 $
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
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsVfsResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Handles SQL queries from query.properties of the generic (ANSI-SQL) driver package.<p>
 * 
 * The following table gives an overview about how a project-ID (provided by a
 * method call in a Cms driver) is matched to a JDBC pool URL, and whether the 
 * table key is replaced in SQL queries or not:<p>
 * 
 * <table border="1">
 * <tr>
 * <th>DB table</th>
 * <th>project ID</th>
 * <th>table ID</th>
 * <th>replace table key in query</th>
 * </tr>
 * <tr>
 * <td>offline</td>
 * <td>> 1</td>
 * <td>2</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td>online</td>
 * <td>< 1</td>
 * <td>1</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td>backup</td>
 * <td>= 0</td>
 * <td>0</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>reserved</td>
 * <td>-1,..,-n</td>
 * <td>1,...,n</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.36 $ $Date: 2004/07/18 16:32:08 $
 * @since 5.1
 */
public class CmsSqlManager extends Object implements Serializable, Cloneable {

    /** 
     * The filename/path of the SQL query properties file.<p> 
     */
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/generic/query.properties";

    /** 
     * The properties hash holding the SQL queries.<p> 
     */
    private static Properties c_queries;
    
    /** 
     * The backup table ID.<p>
     * 
     * The project-ID provided by a Cms driver method call is matched to this internal ID to 
     * get the JDBC pool URL of the offline pool.<p> 
     */    
    protected static final int C_TABLE_ID_BACKUP = 0;
    
    /** 
     * The offline table ID.<p>
     * 
     * The project-ID provided by a Cms driver method call is matched to this internal ID to 
     * get the JDBC pool URL of the offline pool.<p> 
     */
    protected static final int C_TABLE_ID_OFFLINE = 2;
    
    /** 
     * The online table ID.<p>
     * 
     * The project-ID provided by a Cms driver method call is matched to this internal ID to 
     * get the JDBC pool URL of the offline pool.<p> 
     */    
    protected static final int C_TABLE_ID_ONLINE = 1;

    /** 
     * Table key being replaced in SQL queries to generate SQL queries to access online/offline tables.<p> 
     */
    protected static final String C_TABLE_KEY_SEARCH_PATTERN = "_T_";

    /** 
     * Caches all queries with replaced expressions to minimize costs of regex/matching operations.<p> 
     */
    protected Map m_cachedQueries;
    
    /** 
     * Stores the "regular" OpenCms JDBC pool URLs {online|offline|backup}.<p> 
     */
    protected List m_poolUrls;
    
    /** 
     * Stores the "reserved" OpenCms JDBC pool URLs for special purposes.<p> 
     */
    protected List m_reservedPoolUrls;   

    /**
     * Initializes the SQL manager.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be set.
     * 
     */
    public CmsSqlManager() {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        m_poolUrls = new ArrayList(3);
        for (int i = 0; i < 3; i++) {
            m_poolUrls.add(i, null);
        }
        
        m_reservedPoolUrls = new ArrayList(64);
        for (int i = 0; i < 64; i++) {
            m_reservedPoolUrls.add(i, null);
        }
                
        m_cachedQueries = new HashMap();
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
     */
    protected CmsSqlManager(String poolUrl, boolean loadQueries) {
        m_poolUrls = new ArrayList(3);
        m_reservedPoolUrls = new ArrayList(64);
    
        m_poolUrls = new ArrayList(3);
        for (int i = 0; i < 3; i++) {
            m_poolUrls.add(i, null);
        }
        
        m_reservedPoolUrls = new ArrayList(64);
        for (int i = 0; i < 64; i++) {
            m_reservedPoolUrls.add(i, null);
        }
                    
        setPoolUrlOffline(poolUrl);
        setPoolUrlOnline(poolUrl);
        setPoolUrlBackup(poolUrl);

        if (loadQueries && c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }
    }

    /**
     * Replaces the search pattern "_T_" in SQL queries by the pattern _ONLINE_ or _OFFLINE_ 
     * depending on the ID of the current project.<p> 
     * 
     * @param projectId the ID of the current project
     * @param query the SQL query
     * @return String the SQL query with the table key search pattern replaced
     */
    public static String replaceTableKey(int projectId, String query) {
        // make the statement project dependent
        String replacePattern = (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID || projectId < 0) ? "_ONLINE_" : "_OFFLINE_";
        query = CmsStringUtil.substitute(query, C_TABLE_KEY_SEARCH_PATTERN, replacePattern);

        return query;
    }

    /**
     * Attemts to close the connection, statement and result set after a statement has been executed.<p>
     * 
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
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
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (c_queries != null) {
                c_queries.clear();
            }
            
            if (m_cachedQueries != null) {
                m_cachedQueries.clear();
            }
            
            if (m_poolUrls != null) {
                m_poolUrls.clear();
            }
            
            if (m_reservedPoolUrls != null) {
                m_reservedPoolUrls.clear();
            }
            
            c_queries = null;
            m_cachedQueries = null;
            m_poolUrls = null;
            m_reservedPoolUrls = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
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

            if (message != null) {
                message = "[" + message + "] ";
            } else {
                message = "";
            }

            // where did we crash?
            message += "where: " + stackTraceElement + ", ";
            // why did we crash?
            message += "why: " + rootCause.toString();
        }

        message = className + message;

        if (!logSilent && OpenCms.getLog(this).isErrorEnabled()) {
            OpenCms.getLog(this).error(message);
        }

        switch (exceptionType) {
            case CmsException.C_NOT_FOUND :
                return new CmsVfsResourceNotFoundException(message);
            default:
        }

        return new CmsException(message, exceptionType, rootCause);
    }

    /**
     * Receives a JDBC connection from the (offline) pool.<p>
     * 
     * Using this method makes only sense to read/write project 
     * independent data such as user data.
     * 
     * @return a JDBC connection from the (offline) pool 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return getConnection(C_TABLE_ID_OFFLINE);
    }

    /**
     * Receives a JDBC connection from the pool specified by the given CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
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
     * <li>Offline: ID > 1</li>
     * <li>Online: ID = 1</li>
     * <li>Backup: ID = 0</li>
     * <li>Reserved for special purposes: ID &lt; 0</li>
     * </ul>
     * 
     * @param id is matched to the correct JDBC pool URL to obtain a connection from the DriverManager
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(int id) throws SQLException {
        Connection conn = null;
        
        if (id >= 0) {
            // match the ID to a JDBC pool URL of the OpenCms JDBC pools {online|offline|backup}
            conn = DriverManager.getConnection(getPoolUrl(id));
        } else {
            // match the ID to a JDBC pool URL of the OpenCms "reserved" JDBC pools
            conn = DriverManager.getConnection(getReservedPoolUrl(id));
        }
        
        return conn;
    }

    /**
     * Receives a JDBC connection from the backup pool.<p>
     * 
     * Using this method makes only sense to read/write 
     * data to backup data. 
     * 
     * @return a JDBC connection from the backup pool 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnectionForBackup() throws SQLException {
        return getConnection(C_TABLE_ID_BACKUP);
    }
    
    /**
     * Returns the pool JDBC URL for a specified table ID.<p>
     * 
     * @param id the table ID
     * @return the pool JDBC URL for the specified table ID including DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    protected String getPoolUrl(int id) {
        if (id > 2) {
            // it is sufficient to use the offline table ID for project-IDs >2,
            // anything else will result in an ArrayOutOfBoundsException
            id = C_TABLE_ID_OFFLINE;
        }
                
        return (String) m_poolUrls.get(id);
    }   

    /**
     * Returns the backup JDBC pool URL of the OpenCms standard JDBC pools.<p>
     * 
     * @return backup JDBC pool URL including DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public String getPoolUrlBackup() {
        return getPoolUrl(C_TABLE_ID_BACKUP);
    }

    /**
     * Returns the offline JDBC pool URL of the OpenCms standard JDBC pools.<p>
     * 
     * @return offline JDBC pool URL including DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public String getPoolUrlOffline() {
        return getPoolUrl(C_TABLE_ID_OFFLINE);
    }

    /**
     * Returns the online JDBC pool URL of the OpenCms standard JDBC pools.<p>
     * 
     * @return online JDBC pool URL including DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public String getPoolUrlOnline() {
        return getPoolUrl(C_TABLE_ID_ONLINE);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query
     * and the CmsProject.<p>
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
     * and the project-ID.<p>
     * 
     * @param con the JDBC connection
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, int projectId, String queryKey) throws SQLException {
        String rawSql = readQuery(projectId, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query.<p>
     * 
     * @param con the JDBC connection
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, String queryKey) throws SQLException {
        String rawSql = readQuery(0, queryKey);
        return getPreparedStatementForSql(con, rawSql);
    }

    /**
     * Receives a PreparedStatement for a JDBC connection specified by the SQL query.<p>
     * 
     * @param con the JDBC connection
     * @param query the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatementForSql(Connection con, String query) throws SQLException {
        // unfortunately, this wrapper is essential, because some JDBC driver 
        // implementations don't accept the delegated objects of DBCP's connection pool. 
        return con.prepareStatement(query);
    }
    
    /**
     * Returns the JDBC pool URL for a specified reserved table ID.<p>
     * 
     * Reserved JDBC pool URLs should be used to have more than
     * the default OpenCms JDBC connection pools to read/write data 
     * in online tables for special purposes.<p>
     * 
     * @param id the reserved table ID, which has to be <=-1
     * @return poolUrl the JDBC pool URL for this table ID
     * @see #getConnection(int)
     */
    public String getReservedPoolUrl(int id) {
        if (id < 0) {
            id *= -1;
        }
                
        return (String) m_reservedPoolUrls.get(id);
    }     

    /**
     * Loads a Java properties hash containing SQL queries.<p>
     * 
     * @param propertyFilename the package/filename of the properties hash
     * @return Properties the new properties instance.
     */
    protected synchronized Properties loadProperties(String propertyFilename) {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        } catch (Throwable t) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error loading " + propertyFilename, t);
            }
            
            properties = null;
        }

        return properties;
    }

    /**
     * Generates a new primary key for a given database table name.<p>
     * 
     * This method makes only sense for old-style tables where the primary key is NOT a CmsUUID!
     * 
     * @param tableName the table for which a new primary key should be generated.
     * @return int the new primary key
     * @throws CmsException if an error occurs
     */
    public synchronized int nextId(String tableName) throws CmsException {
        return org.opencms.db.CmsDbUtil.nextId(getPoolUrlOffline(), tableName);
    }
    
    /**
     * Generates a new primary key for a given database table ID and table name.<p>
     * 
     * This method makes only sense for old-style tables where the primary key is NOT a CmsUUID!<p>
     * 
     * @param id is matched to the a JDBC pool URL to obtain a connection from the DriverManager
     * @param tableName the table for which a new primary key should be generated.
     * @return int the new primary key
     * @throws CmsException if an error occurs
     */
    public synchronized int nextId(int id, String tableName) throws CmsException {
        String poolUrl = null;
        
        if (id >= 0) {
            // match the ID to the JDBC pool URL of the OpenCms JDBC offline pool
            poolUrl = getPoolUrlOffline();
        } else {
            // match the ID to a JDBC pool URL of the OpenCms "reserved" JDBC pools
            poolUrl = getReservedPoolUrl(id);
        }        
        
        return org.opencms.db.CmsDbUtil.nextId(poolUrl, tableName);
    }   

    /**
     * Replaces patterns ${XXX} by another property value, if XXX is a property key with a value.<p>
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
                    String replacePattern = this.readQuery(replaceKey);

                    if (replacePattern != null) {
                        currentValue = CmsStringUtil.substitute(currentValue, searchPattern, replacePattern);
                    }

                    lastIndex = endIndex + 2;
                }
            }

            properties.put(currentKey, currentValue);
        }
    }

    /**
     * Searches for the SQL query with the specified key and CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(CmsProject project, String queryKey) {
        return readQuery(project.getId(), queryKey);
    }

    /**
     * Searches for the SQL query with the specified key and project-ID.<p>
     * 
     * For projectIds &ne; 0, the pattern "_T_" in table names of queries is 
     * replaced with "_ONLINE_" or "_OFFLINE_" to choose the right database 
     * tables for SQL queries that are project dependent!
     * 
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(int projectId, String queryKey) {
        // get the SQL statement from the properties hash
        String query = readQuery(queryKey);

        // replace control chars.
        query = CmsStringUtil.substitute(query, "\t", " ");
        query = CmsStringUtil.substitute(query, "\n", " ");

        if (projectId == 0) {
            // a project ID = 0 is an internal indicator that a project-independent 
            // query was requested- further regex operations are not required then!
            return query;
        }

        // calculate the key for the map of all cached pre-calculated queries
        queryKey += (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID || projectId < 0) ? "_ONLINE" : "_OFFLINE";

        if (!m_cachedQueries.containsKey(queryKey)) {
            // make the statement project dependent
            query = CmsSqlManager.replaceTableKey(projectId, query);

            // to minimize costs, all statements with replaced expressions are cached in a map
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
    public String readQuery(String queryKey) {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }

        String value = null;
        if ((value = c_queries.getProperty(queryKey)) == null) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Query '" + queryKey + "' not found in " + C_PROPERTY_FILENAME);
            }
        }

        return value;
    }

    /**
     * Sets the designated parameter to the given Java array of bytes.<p>
     * 
     * The driver converts this to an SQL VARBINARY or LONGVARBINARY (depending on the argument's 
     * size relative to the driver's limits on VARBINARY values) when it sends it to the database. 
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
     * Sets the JDBC pool URL for a specified table ID.<p>
     * 
     * @param id the table ID
     * @param poolUrl the JDBC pool URL excluding DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    protected void setPoolUrl(int id, String poolUrl) {
        m_poolUrls.set(id, CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl);
    }

    /**
     * Sets the backup JDBC pool url.<p>
     * 
     * @param poolUrl backup JDBC pool url excluding DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public void setPoolUrlBackup(String poolUrl) {
        if (poolUrl != null) {
            setPoolUrl(C_TABLE_ID_BACKUP, poolUrl);
        }
    }

    /**
     * Sets the offline JDBC pool url.<p>
     * 
     * @param poolUrl offline JDBC pool url excluding DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public void setPoolUrlOffline(String poolUrl) {
        if (poolUrl != null) {
            setPoolUrl(C_TABLE_ID_OFFLINE, poolUrl);
        }
    }

    /**
     * Sets the online JDBC pool url.<p>
     * 
     * @param poolUrl online JDBC pool url excluding DBCP's pool URL prefix
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public void setPoolUrlOnline(String poolUrl) {
        if (poolUrl != null) {
            setPoolUrl(C_TABLE_ID_ONLINE, poolUrl);
        }
    }
    
    /**
     * Sets a reserved JDBC pool URL for a specified table ID.<p>
     * 
     * Reserved JDBC pool URLs should be used to have more than
     * the default OpenCms JDBC connection pools to read/write data 
     * in online tables for special purposes.<p>
     * 
     * To obtain a JDBC connection from a DriverManager pool
     * identified by the specified poolUrl, the specified ID 
     * has to be passed to getConnection.
     * 
     * @param id the reserved table ID, which has to be <=-1
     * @param poolUrl the JDBC pool URL for this table ID excluding DBCP's pool URL prefix
     * @see #getConnection(int)
     * @see CmsDbPool#C_DBCP_JDBC_URL_PREFIX
     */
    public void setReservedPoolUrl(int id, String poolUrl) {
        if (id < 0) {
            id *= -1;
        }

        ((ArrayList) m_reservedPoolUrls).ensureCapacity(id);
        m_reservedPoolUrls.set(id, CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl);
    }

    /**
     * Replaces null Strings by an empty string.<p>
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