/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/I_CmsSqlManager.java,v $
 * Date   : $Date: 2003/08/20 13:14:52 $
 * Version: $Revision: 1.1 $
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

package org.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.CmsProject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Definitions of all required SQL manager methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/08/20 13:14:52 $
 * @since 5.1.8
 */
public interface I_CmsSqlManager {
    
    /**
     * Attemts to close the connection, statement and result set after a statement has been executed.<p>
     * 
     * @param con the JDBC connection
     * @param stmnt the statement
     * @param res the result set
     * @see com.opencms.dbpool.CmsConnection#close()
     */
    public abstract void closeAll(Connection con, Statement stmnt, ResultSet res);
    
    /**
     * Searches for the SQL query with the specified key and CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the SQL query
     * @return the the SQL query in this property list with the specified key
     */
    public abstract String get(CmsProject project, String queryKey);
    
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
    public abstract String get(int projectId, String queryKey);
    
    /**
     * Searches for the SQL query with the specified key.<p>
     * 
     * @param queryKey the SQL query key
     * @return the the SQL query in this property list with the specified key
     */
    public abstract String get(String queryKey);
    
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
    public abstract byte[] getBytes(ResultSet res, String attributeName) throws SQLException;
    
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
    public abstract CmsException getCmsException(Object o, String message, int exceptionType, Throwable rootCause, boolean logSilent);
    
    /**
     * Receives a JDBC connection from the (offline) pool.<p> 
     * 
     * Use this method with caution! Using this method to makes only sense to read/write project 
     * independent data such as user data!
     * 
     * @return a JDBC connection from the (offline) pool 
     * @throws SQLException if a database access error occurs
     */
    public abstract Connection getConnection() throws SQLException;
    
    /**
     * Receives a JDBC connection from the pool specified by the given CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public abstract Connection getConnection(CmsProject project) throws SQLException;
    
    /**
     * Receives a JDBC connection from the pool specified by the given project-ID.<p>
     * 
     * @param projectId the ID of the specified CmsProject
     * @return a JDBC connection from the pool specified by the project-ID 
     * @throws SQLException if a database access error occurs
     */
    public abstract Connection getConnection(int projectId) throws SQLException;
    
    /**
     * Receives a JDBC connection from the backup pool.<p> 
     * 
     * Use this method with caution! Using this method to makes only sense to read/write data 
     * to backup data. 
     * 
     * @return a JDBC connection from the backup pool 
     * @throws SQLException if a database access error occurs
     */
    public abstract Connection getConnectionForBackup() throws SQLException;
    
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
    public abstract PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey) throws SQLException;
    
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
    public abstract PreparedStatement getPreparedStatement(Connection con, int projectId, String queryKey) throws SQLException;
    
    /**
     * Receives a PreparedStatement for a JDBC connection specified by the key of a SQL query.<p>
     * 
     * @param con the JDBC connection
     * @param queryKey the key of the SQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public abstract PreparedStatement getPreparedStatement(Connection con, String queryKey) throws SQLException;
    
    /**
     * Receives a PreparedStatement for a JDBC connection specified by the SQL query.<p>
     * 
     * @param con the JDBC connection
     * @param query the kSQL query
     * @return PreparedStatement a new PreparedStatement containing the pre-compiled SQL statement 
     * @throws SQLException if a database access error occurs
     */
    public abstract PreparedStatement getPreparedStatementForSql(Connection con, String query) throws SQLException;
    
    /**
     * Generates a new primary key for a given database table.<p>
     * 
     * This method makes only sense for old-style tables where the primary key is NOT a CmsUUID!
     * 
     * @param tableName the table for which a new primary key should be generated.
     * @return int the new primary key
     * @throws CmsException if an error occurs
     */
    public abstract int nextId(String tableName) throws CmsException;
    
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
    public abstract void setBytes(PreparedStatement statement, int posn, byte[] content) throws SQLException;
    
    /**
     * Replaces null Strings by an empty string.<p>
     * 
     * @param value the string to validate
     * @return String the validate string or an empty string if the validated string is null
     */
    public abstract String validateNull(String value);
    
    /**
     * Makes all changes permanent since the previous commit/rollback if auto-commit is turned off.<p>
     * 
     * @param conn the connection to commit
     */
    public abstract void commit(Connection conn);
    
    /**
     * Undoes all changes made in the current transaction, optionally after the given Savepoint object was set.<p>
     * 
     * @param conn the connection to roll back
     * @param savepoint an optional savepoint after which all changes are rolled back
     */
    public abstract void rollback(Connection conn, Savepoint savepoint);
    
    /**
     * Removes the given Savepoint object from the current transaction.<p>
     * 
     * @param conn the connection from which the savepoint object is removed
     * @param savepoint the Savepoint object to be removed 
     */
    public abstract void releaseSavepoint(Connection conn, Savepoint savepoint);
    
}