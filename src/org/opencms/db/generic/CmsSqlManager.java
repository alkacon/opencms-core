/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) implementation of the SQL manager.<p>
 *
 * @since 6.0.0
 */
public class CmsSqlManager extends org.opencms.db.CmsSqlManager {

    /** A pattern being replaced in SQL queries to generate SQL queries to access online/offline tables. */
    protected static final String QUERY_PROJECT_SEARCH_PATTERN = "_${PROJECT}_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSqlManager.class);

    /** The filename/path of the SQL query properties. */
    private static final String QUERY_PROPERTIES = "org/opencms/db/generic/query.properties";

    /** A map to cache queries with replaced search patterns. */
    protected ConcurrentHashMap<String, String> m_cachedQueries;

    /** The type ID of the driver (vfs, user, project or history) from where this SQL manager is referenced. */
    protected int m_driverType;

    /** The pool URL to get connections from the JDBC driver manager, including DBCP's pool URL prefix. */
    protected String m_poolUrl;

    /** A map holding all SQL queries. */
    protected Map<String, String> m_queries;

    /**
     * Creates a new, empty SQL manager.<p>
     */
    public CmsSqlManager() {

        m_cachedQueries = new ConcurrentHashMap<String, String>();
        m_queries = new HashMap<String, String>();
        loadQueryProperties(QUERY_PROPERTIES);
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
            LOG.error(Messages.get().getBundle().key(Messages.LOG_SQL_MANAGER_INIT_FAILED_1, classname), t);
            sqlManager = null;
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_SQL_MANAGER_1, classname));
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
    protected static String replaceProjectPattern(CmsUUID projectId, String query) {

        // make the statement project dependent
        String replacePattern = ((projectId == null) || projectId.equals(CmsProject.ONLINE_PROJECT_ID))
        ? "_ONLINE_"
        : "_OFFLINE_";
        return CmsStringUtil.substitute(query, QUERY_PROJECT_SEARCH_PATTERN, replacePattern);
    }

    /**
     * Attempts to close the connection, statement and result set after a statement has been executed.<p>
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
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NULL_DB_CONTEXT_0));
        }

        try {
            // first, close the result set
            if (res != null) {
                res.close();
            }
        } catch (SQLException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        } finally {
            res = null;
        }

        try {
            // close the statement
            if (stmnt != null) {
                stmnt.close();
            }
        } catch (SQLException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        } finally {
            stmnt = null;
        }

        try {
            // close the connection
            if ((con != null) && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        } finally {
            con = null;
        }

    }

    /**
     * Retrieves the value of the designated column in the current row of this ResultSet object as
     * a byte array in the Java programming language.<p>
     *
     * The bytes represent the raw values returned by the driver. Overwrite this method if another
     * database server requires a different handling of byte attributes in tables.<p>
     *
     * @param res the result set
     * @param attributeName the name of the table attribute
     *
     * @return byte[] the column value; if the value is SQL NULL, the value returned is null
     *
     * @throws SQLException if a database access error occurs
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {

        return res.getBytes(attributeName);
    }

    /**
     * Returns a JDBC connection from the connection pool.<p>
     *
     * Use this method to get a connection for reading/writing project independent data.<p>
     *
     * @param dbc the current database context
     *
     * @return a JDBC connection
     *
     * @throws SQLException if the project id is not supported
     */
    public Connection getConnection(CmsDbContext dbc) throws SQLException {

        if (dbc == null) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NULL_DB_CONTEXT_0));
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
     *
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement
     *
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey)
    throws SQLException {

        return getPreparedStatement(con, project.getUuid(), queryKey);
    }

    /**
     * Returns a PreparedStatement for a JDBC connection specified by the key of a SQL query
     * and the project-ID.<p>
     *
     * @param con the JDBC connection
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     *
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement
     *
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatement(Connection con, CmsUUID projectId, String queryKey)
    throws SQLException {

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

        String rawSql = readQuery(CmsUUID.getNullUUID(), queryKey);
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
     * Initializes this SQL manager.<p>
     *
     * @param driverType the type ID of the driver (vfs,user,project or history) from where this SQL manager is referenced
     * @param poolUrl the pool URL to get connections from the JDBC driver manager
     */
    public void init(int driverType, String poolUrl) {

        if (!poolUrl.startsWith(CmsDbPool.DBCP_JDBC_URL_PREFIX)) {
            poolUrl = CmsDbPool.DBCP_JDBC_URL_PREFIX + poolUrl;
        }

        m_driverType = driverType;
        m_poolUrl = poolUrl;

    }

    /**
     * Searches for the SQL query with the specified key and CmsProject.<p>
     *
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(CmsProject project, String queryKey) {

        return readQuery(project.getUuid(), queryKey);
    }

    /**
     * Searches for the SQL query with the specified key and project-ID.<p>
     *
     * For projectIds &ne; 0, the pattern {@link #QUERY_PROJECT_SEARCH_PATTERN} in table names of queries is
     * replaced with "_ONLINE_" or "_OFFLINE_" to choose the right database
     * tables for SQL queries that are project dependent!
     *
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String readQuery(CmsUUID projectId, String queryKey) {

        String key;
        if ((projectId != null) && !projectId.isNullUUID()) {
            // id 0 is special, please see below
            StringBuffer buffer = new StringBuffer(128);
            buffer.append(queryKey);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                buffer.append("_ONLINE");
            } else {
                buffer.append("_OFFLINE");
            }
            key = buffer.toString();
        } else {
            key = queryKey;
        }

        // look up the query in the cache
        String query = m_cachedQueries.get(key);

        if (query == null) {
            // the query has not been cached yet
            // get the SQL statement from the properties hash
            query = readQuery(queryKey);

            if (query == null) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_QUERY_NOT_FOUND_1, queryKey));
            }

            // replace control chars.
            query = CmsStringUtil.substitute(query, "\t", " ");
            query = CmsStringUtil.substitute(query, "\n", " ");

            if ((projectId != null) && !projectId.isNullUUID()) {
                // a project ID = 0 is an internal indicator that a project-independent
                // query was requested - further regex operations are not required then
                query = CmsSqlManager.replaceProjectPattern(projectId, query);
            }

            // to minimize costs, all statements with replaced expressions are cached in a map
            m_cachedQueries.put(key, query);
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

        String value = m_queries.get(queryKey);
        if (value == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_QUERY_NOT_FOUND_1, queryKey));
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
     * Loads a Java properties hash containing SQL queries.<p>
     *
     * @param propertyFilename the package/filename of the properties hash
     */
    protected void loadQueryProperties(String propertyFilename) {

        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
            m_queries.putAll(CmsCollectionsGenericWrapper.<String, String> map(properties));
            replaceQuerySearchPatterns();
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_LOAD_QUERY_PROP_FILE_FAILED_1, propertyFilename),
                    t);
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

        Iterator<String> allKeys = m_queries.keySet().iterator();
        while (allKeys.hasNext()) {
            currentKey = allKeys.next();
            currentValue = m_queries.get(currentKey);
            startIndex = 0;
            endIndex = 0;
            lastIndex = 0;

            while ((startIndex = currentValue.indexOf("${", lastIndex)) != -1) {
                endIndex = currentValue.indexOf('}', startIndex);
                if ((endIndex != -1) && !currentValue.startsWith(QUERY_PROJECT_SEARCH_PATTERN, startIndex - 1)) {

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
}
