/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsSqlManager.java,v $
 * Date   : $Date: 2005/03/04 15:11:32 $
 * Version: $Revision: 1.51 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsDataAccessException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * Generic (ANSI-SQL) implementation of the SQL manager.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.51 $ $Date: 2005/03/04 15:11:32 $
 * @since 5.1
 */
public class CmsSqlManager extends org.opencms.db.CmsSqlManager implements Serializable, Cloneable {

    /** A pattern being replaced in SQL queries to generate SQL queries to access online/offline tables. */
    protected static final String C_QUERY_PROJECT_SEARCH_PATTERN = "_${PROJECT}_";

    /** The filename/path of the SQL query properties. */
    private static final String C_QUERY_PROPERTIES = "org/opencms/db/generic/query.properties";

    /** A map holding all SQL queries. */
    protected Map m_queries;

    /** A map to cache queries with replaced search patterns. */
    protected Map m_cachedQueries;

    /** The pool URL to get connections from the JDBC driver manager, including DBCP's pool URL prefix. */
    protected String m_poolUrl;
    
    /** The type ID of the driver (vfs, user, project, workflow or backup) from where this SQL manager is referenced. */
    protected int m_driverType;

    /**
     * Creates a new, empty SQL manager.<p>
     */
    public CmsSqlManager() {

        m_cachedQueries = new HashMap();
        m_queries = new HashMap();
        loadQueryProperties(C_QUERY_PROPERTIES);
    }
    
    /**
     * Initializes this SQL manager.<p>
     * 
     * @param driverType the type ID of the driver (vfs,user,project,workflow or backup) from where this SQL manager is referenced
     * @param poolUrl the pool URL to get connections from the JDBC driver manager
     */
    public void init(int driverType, String poolUrl) {
        
        if (!poolUrl.startsWith(CmsDbPool.C_DBCP_JDBC_URL_PREFIX)) {
            poolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + poolUrl;
        }        
        
        m_driverType = driverType;
        m_poolUrl = poolUrl;

    }
    
    /**
     * Creates a new instance of a SQL manager.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return a new instance of the SQL manager
     */
    public static org.opencms.db.generic.CmsSqlManager getInstance(String classname) {
        
        org.opencms.db.generic.CmsSqlManager sqlManager;
        
        try {
           Object objectInstance = Class.forName(classname).newInstance();
           sqlManager = (org.opencms.db.generic.CmsSqlManager)objectInstance;
        } catch (Throwable t) {
            OpenCms.getLog(org.opencms.db.generic.CmsSqlManager.class.getName()).error(". SQL manager class '" + classname  + "' could not be instanciated", t);
            sqlManager = null;
        }
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver SQL manager   : " + classname);
        }
        
        return sqlManager;
        
    }

    /**
     * Replaces the project search pattern in SQL queries by the pattern _ONLINE_ or _OFFLINE_ depending on the 
     * specified project ID.<p> 
     * 
     * @param projectId the ID of the current project
     * @param query the SQL query
     * @return String the SQL query with the table key search pattern replaced
     */
    protected static String replaceProjectPattern(int projectId, String query) {

        // make the statement project dependent
        String replacePattern = (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID || projectId < 0) ? "_ONLINE_"
        : "_OFFLINE_";
        query = CmsStringUtil.substitute(query, C_QUERY_PROJECT_SEARCH_PATTERN, replacePattern);

        return query;
    }
    
    /**
     * Attemts to close the connection, statement and result set after a statement has been executed.<p>
     * 
     * @param dbc the current database context
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
     */
    public void closeAll(CmsDbContext dbc, Connection con, Statement stmnt, ResultSet res) {

        // NOTE: we have to close Connections/Statements that way, because a dbcp PoolablePreparedStatement
        // is not a DelegatedStatement; for that reason its not removed from the trace of the connection when it is closed.
        // So, the connection tries to close it again when the connection is closed itself; 
        // as a result there is an error that forces the connection to be destroyed and not pooled

        if (dbc == null) {            
            OpenCms.getLog(this).error("Database context is null!");
        }
        
        try {
            // first, close the connection and (eventually) implicitly all assigned statements and result sets
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        } finally {
            con = null;
        }

        try {
            // close the statement and (normally) implicitly all assigned result sets
            if (stmnt != null) {
                stmnt.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        } finally {
            stmnt = null;
        }

        try {
            // close the result set          
            if (res != null) {
                res.close();
            }
        } catch (SQLException e) {
            // intentionally left blank
        } finally {
            res = null;
        }
        
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
     * Optionally, a log message is written to the OpenCms error logging channel.
     * 
     * @param o the object caused the exception
     * @param message a message that is written to the log
     * @param exceptionType the type of the exception
     * @param rootCause the exception that was thrown
     * @param logSilent if TRUE, no entry to the log is written
     * @return CmsException
     * 
     * @deprecated use any <code>{@link CmsDataAccessException}</code> instead.
     */
    public CmsException getCmsException(
        Object o,
        String message,
        int exceptionType,
        Throwable rootCause,
        boolean logSilent) {

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
            if (rootCause != null) {
                OpenCms.getLog(this).error(message, rootCause);
            } else {
                OpenCms.getLog(this).error(message);
            }
        }

        switch (exceptionType) {
            case CmsException.C_NOT_FOUND:
                return new CmsVfsResourceNotFoundException(message);
            default:
        }

        return new CmsException(message, exceptionType, rootCause);
    }
    
    /**
     * Returns a JDBC connection from the connection pool.<p>
     * 
     * Use this method to get a connection for reading/writing project independent data.<p>
     * 
     * @param dbc the current database context
     * 
     * @return a JDBC connection
     * @throws SQLException if a database access error occurs
     */    
    public Connection getConnection(CmsDbContext dbc) throws SQLException {
        
        return getConnection(dbc, 0);
    }
    
    /**
     * Returns a JDBC connection from the connection pool specified by the given CmsProject id.<p>
     * 
     * Use this method to get a connection for reading/writing data either in online or offline projects
     * such as files, folders.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of a project (to distinguish between online / offline tables)
     * 
     * @return a JDBC connection
     * @throws SQLException if a database access error occurs
     */       
    public Connection getConnection(CmsDbContext dbc, int projectId) throws SQLException {
        
        if (dbc == null) {
            OpenCms.getLog(this).error("Null database context used");
        }
        return getConnection(projectId);
    } 

    /**
     * Returns a JDBC connection from the connection pool specified by the given project ID.<p>
     * 
     * The project ID is (usually) the ID of the current project.<p>
     * 
     * Use this method to get a connection for reading/writing data either in online or offline projects
     * such as files, folders.<p>
     * 
     * @param projectId the ID of a Cms project (e.g. the current project from the request context)
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    protected Connection getConnection(int projectId) throws SQLException {
        
        // the specified project ID is not evaluated in this implementation.
        // extensions of this object might evaluate the project ID to return
        // different connections...        

        if (projectId < 0) {
            throw new SQLException("Unsupported project ID " + projectId + " to return a JDBC connection!");
        }
        
        // match the ID to a JDBC pool URL of the OpenCms JDBC pools {online|offline|backup}
        return getConnectionByUrl(m_poolUrl);
    }

    /**
     * Returns a PreparedStatement for a JDBC connection specified by the key of a SQL query
     * and the CmsProject.<p>
     * 
     * @param con the JDBC connection
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey)
    throws SQLException {

        return getPreparedStatement(con, project.getId(), queryKey);
    }

    /**
     * Returns a PreparedStatement for a JDBC connection specified by the key of a SQL query
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
     * Returns a PreparedStatement for a JDBC connection specified by the key of a SQL query.<p>
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
     * Returns a PreparedStatement for a JDBC connection specified by the SQL query.<p>
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
     * Generates a new primary key for a given database table name.<p>
     * 
     * This method makes only sense for old-style tables where the primary key is NOT a CmsUUID!
     * 
     * @param tableName the table for which a new primary key should be generated.
     * @return int the new primary key
     * @throws CmsDataAccessException if an error occurs
     */
    public synchronized int nextId(String tableName) throws CmsDataAccessException {

        return org.opencms.db.CmsDbUtil.nextId(m_poolUrl, tableName);
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
     * For projectIds &ne; 0, the pattern {@link #C_QUERY_PROJECT_SEARCH_PATTERN} in table names of queries is 
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
            query = CmsSqlManager.replaceProjectPattern(projectId, query);

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
        
        String value = null;
        if ((value = (String)m_queries.get(queryKey)) == null) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Query '" + queryKey + "' not found!");
            }
        }

        return value;
    }

    /**
     * Replaces null or empty Strings with a String with one space character <code>" "</code>.<p>
     * 
     * @param value the string to validate
     * @return the validate string or a String with one space character if the validated string is null or empty
     */
    public String validateEmpty(String value) {

        if (CmsStringUtil.isNotEmpty(value)) {
            return value;
        }

        return " ";
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_cachedQueries != null) {
                m_cachedQueries.clear();
            }

            if (m_queries != null) {
                m_queries.clear();
            }
        } catch (Throwable t) {
            // intentionally left blank
        } finally {
            m_cachedQueries = null;
            m_queries = null;
            m_poolUrl = null;            
        }

        super.finalize();
    }

    /**
     * Loads a Java properties hash containing SQL queries.<p>
     * 
     * @param propertyFilename the package/filename of the properties hash
     */
    protected void loadQueryProperties(String propertyFilename) {

        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
            m_queries.putAll(properties);
            replaceQuerySearchPatterns();
        } catch (Throwable t) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error loading " + propertyFilename, t);
            }

            properties = null;
        }
    }

    /**
     * Replaces patterns ${XXX} by another property value, if XXX is a property key with a value.<p>
     */
    protected synchronized void replaceQuerySearchPatterns() {

        String currentKey = null;
        String currentValue = null;
        int startIndex = 0;
        int endIndex = 0;
        int lastIndex = 0;

        Iterator allKeys = m_queries.keySet().iterator();
        while (allKeys.hasNext()) {
            currentKey = (String)allKeys.next();
            currentValue = (String)m_queries.get(currentKey);
            startIndex = 0;
            endIndex = 0;
            lastIndex = 0;

            while ((startIndex = currentValue.indexOf("${", lastIndex)) != -1) {
                if ((endIndex = currentValue.indexOf('}', startIndex)) != -1
                    && !currentValue.startsWith(C_QUERY_PROJECT_SEARCH_PATTERN, startIndex - 1)) {

                    String replaceKey = currentValue.substring(startIndex + 2, endIndex);
                    String searchPattern = currentValue.substring(startIndex, endIndex + 1);
                    String replacePattern = this.readQuery(replaceKey);

                    if (replacePattern != null) {
                        currentValue = CmsStringUtil.substitute(currentValue, searchPattern, replacePattern);
                    }
                }

                lastIndex = endIndex + 2;
            }

            m_queries.put(currentKey, currentValue);
        }
    }
    
    /**
     * Sets the designated parameter to the given Java array of bytes.<p>
     * 
     * The driver converts this to an SQL VARBINARY or LONGVARBINARY (depending on the argument's 
     * size relative to the driver's limits on VARBINARY values) when it sends it to the database. 
     * 
     * @param statement the PreparedStatement where the content is set
     * @param pos the first parameter is 1, the second is 2, ...
     * @param content the parameter value 
     * @throws SQLException if a database access error occurs
     */
    public void setBytes(PreparedStatement statement, int pos, byte[] content) throws SQLException {

        if (content.length < 2000) {
            statement.setBytes(pos, content);
        } else {
            statement.setBinaryStream(pos, new ByteArrayInputStream(content), content.length);
        }
    }
    
    /**
     * Returns the pool URL to get connections from the JDBC driver manager.<p>
     * 
     * @return the pool URL to get connections from the JDBC driver manager
     */
//    public String getPoolUrl() {
//        
//        return m_poolUrl;
//    }    

}