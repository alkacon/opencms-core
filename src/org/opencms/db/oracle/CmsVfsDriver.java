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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle implementation of the VFS driver methods.<p>
 *
 * @since 6.0.0
 */
public class CmsVfsDriver extends org.opencms.db.generic.CmsVfsDriver {

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsDbContext, CmsUUID, CmsUUID, byte[])
     */
    @Override
    public void createContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ORACLE_OFFLINE_CONTENTS_WRITE");

            // first insert new file without file_content, then update the file_content
            // these two steps are necessary because of using BLOBs in the Oracle DB
            stmt.setString(1, resourceId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        // now update the file content
        internalWriteContent(dbc, projectId, resourceId, content, -1);
    }

    /**
     * @see org.opencms.db.generic.CmsVfsDriver#createOnlineContent(CmsDbContext, CmsUUID, byte[], int, boolean, boolean)
     */
    @Override
    public void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline,
        boolean needToUpdateContent) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();
            if (needToUpdateContent || dbcHasProjectId) {
                if (dbcHasProjectId || !OpenCms.getSystemInfo().isHistoryEnabled()) {
                    // remove the online content for this resource id
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_DELETE");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                } else {
                    // put the online content in the history
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }

                // create new empty online content entry
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_ONLINE_CONTENTS_WRITE");
                stmt.setString(1, resourceId.toString());
                stmt.setInt(2, publishTag);
                stmt.setInt(3, publishTag);
                stmt.setInt(4, keepOnline ? 1 : 0);
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, conn, stmt, null);

                // now update the file content
                internalWriteContent(dbc, CmsProject.ONLINE_PROJECT_ID, resourceId, contents, publishTag);
            } else {
                // update old content entry
                stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_CONTENTS_UPDATE");
                stmt.setInt(1, publishTag);
                stmt.setString(2, resourceId.toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);

                if (!keepOnline) {
                    // put the online content in the history
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(CmsDbContext, CmsUUID, byte[])
     */
    @Override
    public void writeContent(CmsDbContext dbc, CmsUUID resourceId, byte[] content) throws CmsDataAccessException {

        internalWriteContent(dbc, dbc.currentProject().getUuid(), resourceId, content, -1);
    }

    /**
     * Writes the resource content with the specified resource id.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the current project
     * @param resourceId the id of the resource used to identify the content to update
     * @param contents the new content of the file
     * @param publishTag the publish tag if to be written to the online content
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteContent(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        Connection conn = null;
        ResultSet res = null;

        boolean wasInTransaction = false;
        try {
            conn = m_sqlManager.getConnection(dbc);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ORACLE_ONLINE_CONTENTS_UPDATECONTENT");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ORACLE_OFFLINE_CONTENTS_UPDATECONTENT");
            }

            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            // update the file content in the contents table
            stmt.setString(1, resourceId.toString());
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                stmt.setInt(2, publishTag);
                stmt.setInt(3, publishTag);
            }
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.LOG_READING_RESOURCE_1, resourceId));
            }
            // write file content
            OutputStream output = CmsUserDriver.getOutputStreamFromBlob(res, "FILE_CONTENT");
            output.write(contents, 0, contents.length);
            output.close();

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
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(Messages.ERR_WRITING_TO_OUTPUT_STREAM_1, resourceId),
                e);
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
