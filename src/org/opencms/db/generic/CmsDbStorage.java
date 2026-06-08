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

package org.opencms.db.generic;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.storage.I_CmsDbStorage;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database-based storage implementation.<p>
 *
 * This implementation stores binary content in a specialized table (CMS_STORAGE),
 * using a unique hash for deduplication. It ensures that identical files are only
 * stored once physically.<p>
 */
public class CmsDbStorage implements I_CmsDbStorage {

    /** The SQL manager to execute database queries. */
    protected final CmsSqlManager m_sqlManager;

    /**
     * Public constructor.<p>
     *
     * @param sqlManager the SQL manager to use
     */
    public CmsDbStorage(CmsSqlManager sqlManager) {

        m_sqlManager = sqlManager;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#deleteContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public void deleteContent(CmsDbContext dbc, String hash) throws Exception {

        if (hash == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_DELETE");
            stmt.setString(1, hash);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#getStorageIdentifier()
     */
    @Override
    public String getStorageIdentifier() {

        return STORAGE_TYPE;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#loadContent(org.opencms.db.CmsDbContext, java.lang.String)
     */
    @Override
    public byte[] loadContent(CmsDbContext dbc, String hash) throws Exception {

        if (hash == null) {
            return null;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        byte[] content = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_FILE_CONTENT");
            stmt.setString(1, hash);
            res = stmt.executeQuery();
            if (res.next()) {
                content = m_sqlManager.getBytes(res, "FILE_CONTENT");
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return content;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#storeContent(org.opencms.db.CmsDbContext, java.lang.String, byte[])
     */
    @Override
    public void storeContent(CmsDbContext dbc, String hash, byte[] content) throws Exception {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            // check for deduplication
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_EXISTS");
            stmt.setString(1, hash);
            res = stmt.executeQuery();
            if (res.next()) {
                // content already in storage, skip insert
                return;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            // insert new storage entry
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STORAGE_WRITE");
            stmt.setString(1, hash);
            if (content.length < 2000) {
                stmt.setBytes(2, content);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(content), content.length);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.storage.I_CmsStorage#validateAvailable(org.opencms.db.CmsDbContext)
     */
    @Override
    public void validateAvailable(CmsDbContext dbc) throws Exception {

        // The database connection is already validated during OpenCms startup.
    }
}
