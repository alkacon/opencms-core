/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/generic/Attic/CmsSqlManager.java,v $
 * Date   : $Date: 2003/05/23 16:26:47 $
 * Version: $Revision: 1.3 $
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
 
package com.opencms.db.generic;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.exceptions.CmsResourceNotFoundException;
import com.opencms.db.CmsDbPool;
import com.opencms.file.CmsProject;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Reads SQL queries from query.properties of the generic (ANSI-SQL) driver package.<p>
 * 
 * This is our swiss knife for JDBC operations: it loads key/value encoded SQL queries from a Java
 * properties hash. Second, it has a set of methods to return JDBC connections and prepared statements
 * from different connection pools in the Cms depending on the CmsProject/project-ID and database pool.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2003/05/23 16:26:47 $
 * @since 5.1.2
 */
public class CmsSqlManager extends Object implements Serializable, Cloneable {
    
    /**
     * The filename/path of the SQL query properties file.
     */
    private static final String C_PROPERTY_FILENAME = "com/opencms/db/generic/query.properties";
    
    /**
     * The properties hash holding the SQL queries.
     */
    private static Properties c_queries = null;
    
    /**
     * The URL to access the correct connection pool.
     */
    protected String m_dbPoolUrl;

    /**
     * CmsSqlManager constructor.
     * 
     * @param dbPoolUrl the URL to access the correct connection pool
     */
    public CmsSqlManager(String dbPoolUrl) {
        m_dbPoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + dbPoolUrl;
        
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);            
        }
    }
    
    /**
     * CmsSqlManager constructor.
     * 
     * @param dbPoolUrl the URL to access the correct connection pool
     * @param loadQueries flag indicating whether the query.properties should be loaded during initialization
     */
    protected CmsSqlManager(String dbPoolUrl, boolean loadQueries) {
        m_dbPoolUrl = CmsDbPool.C_DBCP_JDBC_URL_PREFIX + dbPoolUrl;
    
        if (loadQueries && c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);            
        }
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
        } catch (SQLException e) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + getClass().getName() + "] error closing JDBC connection/statement/result: " + e.toString());
            }
        } finally {
            res = null;
            stmnt = null;
            con = null;
        }
    }
    
    /**
     * Free any allocated resources when the garbage 
     * collection tries to trash this object.
     */
    protected void finalize() throws Throwable {
        if (c_queries != null) {
            c_queries.clear();
        }

        c_queries = null;
        m_dbPoolUrl = null;
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
     * Searches for the SQL query with the specified key.
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public String get(String queryKey) {              
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
        }      

        String value = null;
        if ((value = c_queries.getProperty(queryKey)) == null) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + getClass().getName() + "] query '" + queryKey + "' not found in " + C_PROPERTY_FILENAME);
            }
        }

        return value;
    }

    /**
     * Returns a byte array for a given table attribute in the result set. Overwrite this method
     * if another database server requires a different handling of byte attributes in tables.
     * 
     * @param res the result set
     * @param attributeName the name of the table attribute
     * @return byte[]
     * @throws SQLException
     */
    public byte[] getBytes(ResultSet res, String attributeName) throws SQLException {       
        return res.getBytes(attributeName);
    }
    
    /**
     * Wraps an exception in a new CmsException object. Optionally, a log message is
     * written to the "critical" OpenCms logging channel.
     * 
     * @param o the object caused the exception
     * @param message a message that is written to the log
     * @param type the type of the exception
     * @param rootCause the exception that was thrown
     * @return CmsException
     */
    public CmsException getCmsException(Object o, String message, int type, Throwable rootCause) {
        if (A_OpenCms.isLogging() && I_CmsLogChannels.C_LOGGING) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + o.getClass().getName() + "] " + ((message==null)?"":message) + ((rootCause==null)?"":rootCause.toString()) );
        }
        
        switch (type) {
            case CmsException.C_NOT_FOUND: return new CmsResourceNotFoundException( "[" + o.getClass().getName() + "] " + ((message==null)?"":message) );
        }
                
        return new CmsException("[" + o.getClass().getName() + "] " + ((message==null)?"":message), type, rootCause);
    }
    
    /**
     * Receives a JDBC connection from the (offline) pool. Use this method with caution! 
     * Using this method to makes only sense to read/write project independent data such 
     * as user data!
     * 
     * @return a JDBC connection from the (offline) pool 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        // To receive a JDBC connection from the offline pool, 
        // a non-existent dummy project-ID is used
        return getConnection(Integer.MIN_VALUE);
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
     * Receives a JDBC connection from the pool specified by the given project-ID.
     * 
     * @param projectId the ID of the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(int projectId) throws SQLException {
        return DriverManager.getConnection(m_dbPoolUrl);
    }

    /**
     * Receives a JDBC connection from the backup pool. Use this method with caution! 
     * Using this method to makes only sense to read/write data to backup data. 
     * 
     * @return a JDBC connection from the backup pool 
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnectionForBackup() throws SQLException {
        return DriverManager.getConnection(m_dbPoolUrl);
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
        return con.prepareStatement(query);
    }    
    
    /**
     * Loads a Java properties hash.
     * 
     * @param propertyFilename the package/filename of the properties hash
     * @return Properties the new properties instance.
     */
    protected Properties loadProperties(String propertyFilename) {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
        } catch (NullPointerException exc) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + getClass().getName() + "] error loading " + propertyFilename);
            }

            properties = null;
        } catch (java.io.IOException exc) {
            if (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + getClass().getName() + "] error loading " + propertyFilename);
            }

            properties = null;
        }

        return properties;
    }
    
    public synchronized int nextId(String key) throws CmsException {
        return com.opencms.db.CmsIdGenerator.nextId(m_dbPoolUrl, key);
    }

    public void setBytes(PreparedStatement statement, int posn, byte[] content) throws SQLException {
        if (content.length < 2000) {
            statement.setBytes(posn, content);
        } else {
            statement.setBinaryStream(posn, new ByteArrayInputStream(content), content.length);
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