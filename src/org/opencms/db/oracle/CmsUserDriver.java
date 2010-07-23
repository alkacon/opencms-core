/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsUserDriver.java,v $
 * Date   : $Date: 2010/07/23 08:29:34 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogEntryType;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle implementation of the user driver methods.<p>
 * 
 * @author Thomas Weckert  
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 6.0.0 
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /**
     * Generates an Output stream that writes to a blob, also truncating the existing blob if required.<p>
     * 
     * Apparently Oracle requires some non-standard handling here.<p>
     * 
     * @param res the result set where the blob is located in 
     * @param name the name of the database column where the blob is located
     * @return an Output stream from a blob
     * @throws SQLException if something goes wring
     */
    public static OutputStream getOutputStreamFromBlob(ResultSet res, String name) throws SQLException {

        // TODO: perform blob check only once and store Oracle version in a static private member 
        // TODO: best do this during system startup / db init phase once
        Blob blob = res.getBlob(name);
        try {
            // jdbc standard
            blob.truncate(0);
            return blob.setBinaryStream(0L);
        } catch (SQLException e) {
            // oracle 9 & 8 (if using the same jdbc driver as provided by oracle9: ojdbc14.jar)
            ((oracle.sql.BLOB)blob).trim(0);
            return ((oracle.sql.BLOB)blob).getBinaryOutputStream();
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#markResourceAsVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource, org.opencms.file.CmsUser)
     */
    public void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    throws CmsDataAccessException {

        boolean entryExists = false;
        CmsLogFilter filter = CmsLogFilter.ALL.includeType(CmsLogEntryType.USER_RESOURCE_VISITED).filterResource(
            resource.getStructureId()).filterUser(user.getId());
        // delete existing visited entry for the resource
        if (m_driverManager.getProjectDriver(dbc).readLog(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter).size() > 0) {
            entryExists = true;
            m_driverManager.getProjectDriver(dbc).deleteLog(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
        }

        // create new entry
        List<CmsLogEntry> newEntries = new ArrayList<CmsLogEntry>(1);
        CmsLogEntry entry = new CmsLogEntry(
            user.getId(),
            System.currentTimeMillis(),
            resource.getStructureId(),
            CmsLogEntryType.USER_RESOURCE_VISITED,
            new String[] {user.getName(), resource.getRootPath()});
        newEntries.add(entry);
        m_driverManager.getProjectDriver(dbc).log(dbc, poolName, newEntries);

        if (!entryExists) {
            // new entry, check if maximum number of stored visited resources is exceeded
            PreparedStatement stmt = null;
            Connection conn = null;
            ResultSet res = null;
            int count = 0;

            try {
                conn = m_sqlManager.getConnection(poolName);
                stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_VISITED_USER_COUNT_2");

                stmt.setString(1, user.getId().toString());
                stmt.setInt(2, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                res = stmt.executeQuery();

                if (res.next()) {
                    count = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_COUNTING_VISITED_RESOURCES_1,
                        user.getName()));
                }

                int maxCount = OpenCms.getSubscriptionManager().getMaxVisitedCount();
                if (count > maxCount) {
                    // delete old visited log entries
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                    stmt = m_sqlManager.getPreparedStatement(
                        conn,
                        dbc.currentProject(),
                        "C_ORACLE_VISITED_USER_DELETE_5");

                    stmt.setString(1, user.getId().toString());
                    stmt.setInt(2, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                    stmt.setString(3, user.getId().toString());
                    stmt.setInt(4, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                    stmt.setInt(5, count - maxCount);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(
                    Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUserInfo(CmsDbContext, CmsUUID, String, Object)
     */
    public void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {

            // get connection
            conn = m_sqlManager.getConnection(dbc);

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERDATA_WRITE_3");
            stmt.setString(1, userId.toString());
            stmt.setString(2, key);
            stmt.setString(3, value.getClass().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        internalUpdateUserInfo(dbc, userId, key, value);
    }

    /**
     * Updates the given user information entry.<p>
     * 
     * @param dbc the current database context
     * @param userId the id of the user to update
     * @param key the user info entry key
     * @param value the user info entry value
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalUpdateUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        ResultSet res = null;
        Connection conn = null;

        boolean wasInTransaction = false;

        try {
            // get connection
            conn = m_sqlManager.getConnection(dbc);
            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            // update user_info in this special way because of using blob
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERDATA_UPDATE_2");
            stmt.setString(1, userId.toString());
            stmt.setString(2, key);
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_NO_USER_WITH_ID_1, userId));
            }
            // write serialized user info 
            OutputStream output = getOutputStreamFromBlob(res, "DATA_VALUE");
            output.write(CmsDataTypeUtil.dataSerialize(value));
            output.close();
            value = null;

            if (!wasInTransaction) {
                commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                commit.execute();
                m_sqlManager.closeAll(dbc, null, commit, null);
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            // this is needed so the finally block works correctly
            commit = null;
            stmt = null;
            res = null;

            if (!wasInTransaction) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_SERIALIZING_USER_DATA_1, userId), e);
        } finally {
            org.opencms.db.oracle.CmsSqlManager.closeAllInTransaction(
                m_sqlManager,
                dbc,
                conn,
                stmt,
                res,
                commit,
                wasInTransaction);
        }
    }
}
