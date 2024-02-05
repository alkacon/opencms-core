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
import org.opencms.db.CmsSelectQuery.TableAlias;
import org.opencms.db.CmsSimpleQueryFragment;
import org.opencms.db.I_CmsQueryFragment;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.CmsUserQueryBuilder;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsDataAccessException;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Joiner;

/**
 * Oracle implementation of the user driver methods.<p>
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

        Blob blob = res.getBlob(name);
        blob.truncate(0);
        return blob.setBinaryStream(0L);
    }

    /**
     * @see org.opencms.db.generic.CmsUserDriver#createUserQueryBuilder()
     */
    @Override
    public CmsUserQueryBuilder createUserQueryBuilder() {

        return new CmsUserQueryBuilder() {

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#createFlagCondition(org.opencms.db.CmsSelectQuery.TableAlias, int)
             */
            @Override
            protected I_CmsQueryFragment createFlagCondition(TableAlias users, int flags) {

                return new CmsSimpleQueryFragment(
                    "BITAND(" + users.column("USER_FLAGS") + ", ?) = ? ",
                    Integer.valueOf(flags),
                    Integer.valueOf(flags));
            }

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#generateConcat(java.lang.String[])
             */
            @Override
            protected String generateConcat(String... expressions) {

                return Joiner.on(" || ").join(expressions);
            }

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#getUserFlagExpression(org.opencms.db.CmsSelectQuery.TableAlias, int)
             */
            @Override
            protected String getUserFlagExpression(TableAlias users, int flags) {

                return "BITAND(" + users.column("USER_FLAGS") + ", " + flags + ")";

            }

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#useWindowFunctionsForPaging()
             */
            @Override
            protected boolean useWindowFunctionsForPaging() {

                return true;
            }

        };
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * Updates additional user info.<p>
     * @param dbc the current dbc
     * @param userId the user id to add the user info for
     * @param key the name of the additional user info
     * @param value the value of the additional user info
     * @throws CmsDataAccessException if something goes wrong
     */
    @Override
    protected void internalUpdateUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {

            // get connection
            conn = m_sqlManager.getConnection(dbc);

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERDATA_UPDATE_3");
            stmt.setString(1, value.getClass().getName());
            stmt.setString(2, userId.toString());
            stmt.setString(3, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        internalUpdateUserInfoData(dbc, userId, key, value);
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
    protected void internalUpdateUserInfoData(CmsDbContext dbc, CmsUUID userId, String key, Object value)
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
            res = stmt.executeQuery();
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
            throw new CmsDbSqlException(
                org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)),
                e);
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

    /**
     * Writes a new additional user info.<p>
     * @param dbc the current dbc
     * @param userId the user id to add the user info for
     * @param key the name of the additional user info
     * @param value the value of the additional user info
     * @throws CmsDataAccessException if something goes wrong
     */
    @Override
    protected void internalWriteUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value)
    throws CmsDataAccessException {

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
            throw new CmsDbSqlException(
                org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        internalUpdateUserInfoData(dbc, userId, key, value);
    }

}
