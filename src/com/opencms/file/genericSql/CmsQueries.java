/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsQueries.java,v $
 * Date   : $Date: 2003/05/07 11:43:26 $
 * Version: $Revision: 1.46 $
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

package com.opencms.file.genericSql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsProject;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import source.org.apache.java.util.Configurations;

/**
 * A helper object to manage SQL queries. First, it loads key/value encoded SQL queries from a Java
 * properties hash. Second, it has a set of methods to return JDBC connections and statements
 * from different connection pools in the Cms dependent on the CmsProject/project-ID.
 * 
 * <p>
 * 
 * TODO: multiple instances of this class should not load the same property hashes multiple times.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.46 $ $Date: 2003/05/07 11:43:26 $
 */
public class CmsQueries extends Object {

    private static Properties m_queries = null;

    /**
     * The JDBC URL of the offline connection pool.
     */
    private static String m_JdbcUrlOffline;

    /**
     * The JDBC URL of the online connection pool.
     */
    private static String m_JdbcUrlOnline;

    /**
     * The JDBC URL of the backup connection pool.
     */
    private static String m_JdbcUrlBackup;

    /**
     * Internal flag whether the URLs of the JDBC connection pools 
     * are read from the configurations object.
     */
    protected static boolean poolUrlsInitialized = false;

    /**
     * CmsQueries constructor.
     */
    public CmsQueries() {
        if (m_queries == null) {
            m_queries = new Properties();

            try {
                m_queries.load(getClass().getClassLoader().getResourceAsStream("com/opencms/file/genericSql/query.properties"));
            } catch (NullPointerException exc) {
                if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsQueries] cannot get com/opencms/file/genericSql/query.properties");
                }
            } catch (java.io.IOException exc) {
                if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsQueries] cannot get com/opencms/file/genericSql/query.properties");
                }
            }
        }
    }
    
    /**
     * Free any allocated resources when the garbage 
     * collection tries to trash this object.
     */
    protected void finalize() throws Throwable {
        m_queries.clear();
        m_queries = null;
    }

    /**
     * Searches for the SQL query with the specified key.
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String get(String queryKey) {
        if (m_queries == null) {
            m_queries = new Properties();
            try {
                m_queries.load(getClass().getClassLoader().getResourceAsStream("com/opencms/file/genericSql/query.properties"));
            } catch (NullPointerException exc) {
                if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsQueries] cannot get com/opencms/file/genericSql/query.properties");
                }
            } catch (java.io.IOException exc) {
                if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsQueries] cannot get com/opencms/file/genericSql/query.properties");
                }
            }
        }

        String value = null;

        if ((value = m_queries.getProperty(queryKey)) == null) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsQueries] query '" + queryKey + "' not found in query.properties!");
            }
        }

        return value;
    }

    /**
     * Reads the URLs of the JDBC connection pools from the configurations object to
     * choose a connection from the right connection pool depending on the CmsProject
     * or project-ID.
     * 
     * @param config the configurations object
     */
    public void initJdbcPoolUrls(Configurations config) {
        if (!poolUrlsInitialized) {
            // get the broker name from the config.
            String brokerName = (String) config.getString(com.opencms.core.I_CmsConstants.C_CONFIGURATION_RESOURCEBROKER);

            // get the URL of the offline JDBC pool
            m_JdbcUrlOffline = config.getString(com.opencms.core.I_CmsConstants.C_CONFIGURATION_RESOURCEBROKER + "." + brokerName + "." + com.opencms.core.I_CmsConstants.C_CONFIGURATIONS_POOL);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database offline pool: " + m_JdbcUrlOffline);
            }

            // get the URL of the online JDBC pool
            m_JdbcUrlOnline = config.getString(com.opencms.core.I_CmsConstants.C_CONFIGURATION_RESOURCEBROKER + "." + brokerName + ".online." + com.opencms.core.I_CmsConstants.C_CONFIGURATIONS_POOL);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database online pool : " + m_JdbcUrlOnline);
            }

            // get the URL of the backup JDBC pool
            m_JdbcUrlBackup = config.getString(com.opencms.core.I_CmsConstants.C_CONFIGURATION_RESOURCEBROKER + "." + brokerName + ".backup." + com.opencms.core.I_CmsConstants.C_CONFIGURATIONS_POOL);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Database backup pool : " + m_JdbcUrlBackup);
            }

            // set the default JDBC URL for the ID generator
            com.opencms.dbpool.CmsIdGenerator.setDefaultPool(m_JdbcUrlOffline);

            poolUrlsInitialized = true;
        }
    }

    /**
     * Searches for the SQL query with the specified key and project-ID.
     * 
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String get(int projectId, String queryKey) {
        if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            queryKey += "_ONLINE";
        }

        return get(queryKey);
    }

    /**
     * Searches for the SQL query with the specified key and CmsProject.
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public String get(CmsProject project, String queryKey) {
        return get(project.getId(), queryKey);
    }

    /**
     * Receives a JDBC connection from the pool specified by the given project-ID.
     * 
     * @param projectId the ID of the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(int projectId) throws SQLException {
        String jdbcUrl = (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) ? m_JdbcUrlOnline : m_JdbcUrlOffline;
        return DriverManager.getConnection(jdbcUrl);
    }

    /**
     * Receives a JDBC connection from the pool specified by the given CmsProject.
     * 
     * @param project the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(CmsProject project) throws SQLException {
        return getConnection(project.getId());
    }

    /**
     * Receives a JDBC connection from the backup pool.
     * 
     * @return a JDBC connection from the backup pool 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnectionBackup() throws SQLException {
        return DriverManager.getConnection(m_JdbcUrlBackup);
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
        return con.prepareStatement(rawSql);
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
     * Receives a PreparedStatement for a backup JDBC connection specified by the key of a SQL query.
     * 
     * @param con the JDBC connection
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement getPreparedStatementBackup(Connection con, String queryKey) throws SQLException {
        String rawSql = get(Integer.MIN_VALUE, queryKey);
        return con.prepareStatement(rawSql);
    }

    /**
     * Generates a new primary key ID specified by the key of a SQL query and the project-ID.
     * 
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return int a new primary key ID
     * @throws CmsException if a database access error occurs
     */
    public synchronized int nextPkId(int projectId, String queryKey) throws CmsException {
        String jdbcUrl = (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) ? m_JdbcUrlOnline : m_JdbcUrlOffline;
        return nextPkId(jdbcUrl, get(projectId, queryKey));
    }

    /**
     * Generates a new primary key ID specified by the key of a SQL query and the CmsProject.
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return int a new primary key ID
     * @throws CmsException if a database access error occurs
     */
    public synchronized int nextPkId(CmsProject project, String queryKey) throws CmsException {
        return nextPkId(project.getId(), queryKey);
    }

    /**
     * Generates a new primary key ID for a offline table specified by the key of a SQL query.
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return int a new primary key ID
     * @throws CmsException if a database access error occurs
     */
    public synchronized int nextPkId(String queryKey) throws CmsException {
        return nextPkId(m_JdbcUrlOffline, get(Integer.MIN_VALUE, queryKey));
    }

    /**
     * Generates a new primary key ID specified by the URL of the JDBC connection and the SQL query.
     * 
     * @param jdbcUrl
     * @param query
     * @return int a new primary key ID
     * @throws CmsException if a database access error occurs
     */
    protected synchronized int nextPkId(String jdbcUrl, String query) throws CmsException {
        return com.opencms.dbpool.CmsIdGenerator.nextId(jdbcUrl, query);
    }

    /**
     * Attemts to close the connection, statement and result set after a statement has been executed.
     * 
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
     * @see com.opencms.dbpool.CmsConnection#close()
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
        } catch (Exception e) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + getClass().getName() + "] error closing JDBC connection/statement/result: " + e.toString());
            }
        } finally {
            res = null;
            stmnt = null;
            con = null;
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

    public void setBytes(PreparedStatement statement, int posn, byte[] content) throws SQLException {
        if (content.length < 2000) {
            statement.setBytes(posn, content);
        } else {
            statement.setBinaryStream(posn, new ByteArrayInputStream(content), content.length);
        }
    }

    /**
     * Returns a byte array for a given table attribute in the result set. Overwrite this method
     * if another database server requires a different handling of byte attributes in tables.
     * 
     * @param res the result set
     * @param columnName the name of the table attribute
     * @return byte[]
     * @throws SQLException
     */
    public byte[] getBytes(ResultSet res, String columnName) throws SQLException {       
        return res.getBytes(columnName);
    }

}
