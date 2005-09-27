/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsDbUtil.java,v $
 * Date   : $Date: 2005/09/27 11:18:35 $
 * Version: $Revision: 1.22.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsDataAccessException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import org.apache.commons.logging.Log;

/**
 * This class is used to create primary keys as integers for Cms database tables that
 * don't have a UUID primary key.<p>
 * 
 * 
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.22.2.1 $
 * 
 * @since 6.0.0
 */
public final class CmsDbUtil {

    /** Indicates an unknown (or not set) id. */
    public static final int UNKNOWN_ID = -1;

    /** Hashtable with border id's. */
    private static Hashtable c_borderId;

    /** Hashtable with next available id's. */
    private static Hashtable c_currentId;

    /** The name of the default pool. */
    private static String c_dbPoolUrl;

    /** Grow value. */
    private static final int GROW_VALUE = 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDbUtil.class);

    /**
     * Default constructor.<p>
     * 
     * Nobody is allowed to create an instance of this class!
     */
    private CmsDbUtil() {

        super();
    }

    /**
     * This method tries to get the timestamp several times, because there
     * is a timing problem in the mysql driver.<p>
     *
     * @param result the resultset to get the stamp from
     * @param column the column to read the timestamp from
     * @return the timestamp
     * @throws SQLException if something goes wrong
     */
    public static Timestamp getTimestamp(ResultSet result, String column) throws SQLException {

        int i = 0;
        for (;;) {
            try {
                return (result.getTimestamp(column));
            } catch (SQLException exc) {
                i++;
                if (i >= 10) {
                    throw exc;
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().key(Messages.LOG_GET_TIMESTAMP_2, column, new Integer(i)));
                    }
                }
            }
        }
    }

    /**
     * Initilizes this DB utils.<p>
     */
    public static void init() {

        c_currentId = new Hashtable();
        c_borderId = new Hashtable();
        c_dbPoolUrl = "";
    }

    /**
     * Creates a new primary key ID for a given table.<p>
     * 
     * @param tableName the name of the table to create a new primary key ID
     * @return a new primary key ID for the given table
     * @throws CmsException if something goes wrong
     */
    public static synchronized int nextId(String tableName) throws CmsException {

        return nextId(c_dbPoolUrl, tableName);
    }

    /**
     * Creates a new primary key ID for a given table using JDBC connection specified by a pool URL.<p>
     * 
     * @param dbPoolUrl the URL to access the connection pool
     * @param tableName the name of the table to create a new primary key ID
     * @return a new primary key ID for the given table
     * @throws CmsDataAccessException if something goes wrong
     */
    public static synchronized int nextId(String dbPoolUrl, String tableName) throws CmsDataAccessException {

        String cacheKey = dbPoolUrl + '.' + tableName;

        // generated primary keys are cached!
        if (c_currentId.containsKey(cacheKey)) {
            int id = ((Integer)c_currentId.get(cacheKey)).intValue();
            int borderId = ((Integer)c_borderId.get(cacheKey)).intValue();
            if (id < borderId) {
                int nextId = id + 1;
                c_currentId.put(cacheKey, new Integer(nextId));
                return id;
            }
        }

        // there is no primary key ID for the given table yet in the cache.
        // we generate a new primary key ID based on the last primary key
        // entry in the CMS_SYSTEMID table instead
        generateNextId(dbPoolUrl, tableName, cacheKey);

        // afterwards, return back to this method to take the new primary key 
        // ID out of the cache...
        return nextId(dbPoolUrl, tableName);
    }

    /**
     * Sets the URL of the connection pool.<p>
     * 
     * @param dbPoolUrl the URL to access the connection pool
     */
    public static void setDefaultPool(String dbPoolUrl) {

        c_dbPoolUrl = dbPoolUrl;
    }

    /**
     * Creates a new primary key ID for a given table in the CMS_SYSTEMID table.<p>
     * 
     * @param conn the connection to access the database
     * @param tableName the name of the table to read the primary key ID
     * @param newId the new primary key ID
     * 
     * @throws CmsDbSqlException if something gows wrong
     */
    private static void createId(Connection conn, String tableName, int newId) throws CmsDbSqlException {

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement("INSERT INTO CMS_SYSTEMID (TABLE_KEY,ID) VALUES (?,?)");
            stmt.setString(1, tableName);
            stmt.setInt(2, newId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
        }
    }

    /**
     * Creates a new primary key ID for a given table based on the last primary key ID for this
     * table in the CMS_SYSTEMID table.<p>
     * 
     * @param dbPoolUrl the URL to access the connection pool
     * @param tableName the name of the table to create a new primary key ID
     * @param cacheKey the key to store the new primary key ID in the cache
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    private static void generateNextId(String dbPoolUrl, String tableName, String cacheKey) throws CmsDbSqlException {

        Connection con = null;
        int id;
        int borderId;

        try {
            if (!dbPoolUrl.startsWith(CmsDbPool.DBCP_JDBC_URL_PREFIX)) {
                dbPoolUrl = CmsDbPool.DBCP_JDBC_URL_PREFIX + dbPoolUrl;
            }

            con = DriverManager.getConnection(dbPoolUrl);
            // repeat this operation, until the nextId is valid and can be saved
            // (this is for clustering of several OpenCms)
            do {
                id = readId(con, tableName);

                if (id == CmsDbUtil.UNKNOWN_ID) {
                    // there was no entry - set it to 0
                    // EF: set id to 1 because the table contains
                    // the next available id
                    id = 1;
                    createId(con, tableName, id);
                }
                borderId = id + GROW_VALUE;
                // save the next id for future requests
            } while (!writeId(con, tableName, id, borderId));
            // store the generated values in the cache
            c_currentId.put(cacheKey, new Integer(id));
            c_borderId.put(cacheKey, new Integer(borderId));
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_0), e);
        } finally {
            // close all db-resources
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
        }
    }

    /**
     * Reads the last primary key ID for a given table.<p>
     * 
     * @param conn the connection to access the database
     * @param tableName the name of the table to read the primary key ID
     * 
     * @return the primary key ID or UNKNOWN_ID if there is no entry for the given table
     * 
     * @throws CmsDbSqlException if something gows wrong
     */
    private static int readId(Connection conn, String tableName) throws CmsDbSqlException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = conn.prepareStatement("SELECT CMS_SYSTEMID.ID FROM CMS_SYSTEMID WHERE CMS_SYSTEMID.TABLE_KEY=?");
            stmt.setString(1, tableName);

            res = stmt.executeQuery();
            if (res.next()) {
                return res.getInt(1);
            } else {
                return CmsDbUtil.UNKNOWN_ID;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            // close all db-resources
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
        }
    }

    /**
     * Updates the CMS_SYSTEMID table with a new primary key ID for a given table.<p>
     * 
     * @param conn the connection to access the database
     * @param tableName the name of the table to read the primary key ID
     * @param oldId the last primary key ID
     * @param newId the new primary key ID
     * 
     * @return true if the number of affected rows is 1
     * 
     * @throws CmsDbSqlException if something gows wrong
     */
    private static boolean writeId(Connection conn, String tableName, int oldId, int newId) throws CmsDbSqlException {

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement("UPDATE CMS_SYSTEMID SET ID=? WHERE CMS_SYSTEMID.TABLE_KEY=? AND CMS_SYSTEMID.ID=?");
            stmt.setInt(1, newId);
            stmt.setString(2, tableName);
            stmt.setInt(3, oldId);
            int amount = stmt.executeUpdate();
            // return, if the update had succeeded
            return (amount == 1);
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
        }
    }
}