/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Oracle database storage implementation.<p>
 */
public class CmsDbStorage extends org.opencms.db.generic.CmsDbStorage {

    /**
     * Public constructor.<p>
     *
     * @param sqlManager the SQL manager to use
     */
    public CmsDbStorage(CmsSqlManager sqlManager) {

        super(sqlManager);
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#storeContent(org.opencms.db.CmsDbContext, java.lang.String, byte[])
     */
    @Override
    public void storeContent(CmsDbContext dbc, String hash, byte[] content) throws Exception {

        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        ResultSet res = null;
        boolean wasInTransaction = false;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_EXISTS");
            stmt.setString(1, hash);
            res = stmt.executeQuery();
            if (res.next()) {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
                conn = null;
                stmt = null;
                res = null;
                return;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);
            stmt = null;
            res = null;

            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_STORAGE_WRITE");
            stmt.setString(1, hash);
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);
            stmt = null;

            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_STORAGE_UPDATECONTENT");

            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            stmt.setString(1, hash);
            res = stmt.executeQuery();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_GENERIC_SQL_1));
            }

            OutputStream output = CmsUserDriver.getOutputStreamFromBlob(res, "FILE_CONTENT");
            output.write(content, 0, content.length);
            output.close();

            if (!wasInTransaction) {
                commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                commit.execute();
                m_sqlManager.closeAll(dbc, null, commit, null);
            }

            m_sqlManager.closeAll(dbc, null, stmt, res);
            commit = null;
            stmt = null;
            res = null;

            if (!wasInTransaction) {
                conn.setAutoCommit(true);
            }
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_WRITING_TO_OUTPUT_STREAM_1, hash), e);
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
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
