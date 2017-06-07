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
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @since 6.0.0
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createPublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    @Override
    public void createPublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_PUBLISHJOB_CREATE");
            stmt.setString(1, publishJob.getPublishHistoryId().toString());
            stmt.setString(2, publishJob.getProjectId().toString());
            stmt.setString(3, publishJob.getProjectName());
            stmt.setString(4, publishJob.getUserId().toString());
            stmt.setString(5, publishJob.getLocale().toString());
            stmt.setInt(6, publishJob.getFlags());
            stmt.setInt(7, publishJob.getSize());
            stmt.setLong(8, publishJob.getEnqueueTime());
            stmt.setLong(9, publishJob.getStartTime());
            stmt.setLong(10, publishJob.getFinishTime());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        try {
            // now write the publish list
            internalWritePublishJobData(
                dbc,
                publishJob.getPublishHistoryId(),
                "C_ORACLE_PUBLISHJOB_UPDATE_PUBLISHLIST",
                "PUBLISH_LIST",
                internalSerializePublishList(publishJob.getPublishList()));
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(
                    Messages.ERR_SERIALIZING_PUBLISHLIST_1,
                    publishJob.getPublishHistoryId().toString()),
                e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishReport(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[])
     */
    @Override
    public void writePublishReport(CmsDbContext dbc, CmsUUID publishId, byte[] content) throws CmsDataAccessException {

        internalWritePublishJobData(
            dbc,
            publishId,
            "C_ORACLE_PUBLISHJOB_UPDATE_PUBLISHREPORT",
            "PUBLISH_REPORT",
            content);
    }

    /**
     * Writes data for a publish job.<p>
     *
     * @param dbc the database context
     * @param publishJobHistoryId the publish job id
     * @param queryKey the query to use
     * @param fieldName the fiueld to use
     * @param data the data to write
     * @throws CmsDataAccessException if something goes wrong
     */
    private void internalWritePublishJobData(
        CmsDbContext dbc,
        CmsUUID publishJobHistoryId,
        String queryKey,
        String fieldName,
        byte[] data) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        ResultSet res = null;
        boolean wasInTransaction = false;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, queryKey);

            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            // update the file content in the contents table
            stmt.setString(1, publishJobHistoryId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishJobHistoryId));
            }

            // write file content
            OutputStream output = CmsUserDriver.getOutputStreamFromBlob(res, fieldName);
            output.write(data);
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
                Messages.get().container(Messages.ERR_WRITING_TO_OUTPUT_STREAM_1, publishJobHistoryId),
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

    /**
     * @see org.opencms.db.I_CmsProjectDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }
}
