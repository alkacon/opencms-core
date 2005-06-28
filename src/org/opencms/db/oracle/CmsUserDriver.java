/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsUserDriver.java,v $
 * Date   : $Date: 2005/06/28 08:05:44 $
 * Version: $Revision: 1.52 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryAlreadyExistsException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbcp.DelegatingResultSet;
import org.apache.commons.logging.Log;

/**
 * Oracle implementation of the user driver methods.<p>
 * 
 * @author Thomas Weckert  
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.52 $
 * 
 * @since 6.0.0 
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDriver.class);

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

        int todo = 0;
        // TODO: perform blob check only once and store Oracle version in a static privae member 
        // TODO: best do this during system startup / db init phase once

        Blob blob = res.getBlob(name);
        try {
            // jdbc standard
            blob.truncate(0);
            return blob.setBinaryStream(0L);
        } catch (SQLException e) {
            // oracle 9 & 8
            ((oracle.sql.BLOB)blob).trim(0);
            return ((oracle.sql.BLOB)blob).getBinaryOutputStream();
        }

        // this is the code for Oracle 10 (doesn't work with Oracle 9)                
        //((oracle.sql.BLOB)blob).truncate(0);
        //return blob.setBinaryStream(0L);

        // this is the code for Oracle 9 (& 8 if using the same jdbc driver as provided by oracle9: ojdbc14.jar)
        //((oracle.sql.BLOB)blob).trim(0);
        //return ((oracle.sql.BLOB)blob).getBinaryOutputStream();
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUser(org.opencms.db.CmsDbContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Map, java.lang.String, int)
     */
    public CmsUser createUser(
        CmsDbContext dbc,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        Map additionalInfos,
        String address,
        int type) throws CmsDataAccessException, CmsPasswordEncryptionException {

        CmsUUID id = new CmsUUID();
        PreparedStatement stmt = null;
        Connection conn = null;

        if (existsUser(dbc, name, type, null)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_USER_WITH_NAME_ALREADY_EXISTS_1, name);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key());
            }
            throw new CmsDbEntryAlreadyExistsException(message);
        }

        try {
            conn = m_sqlManager.getConnection(dbc);

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_ADD");
            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, OpenCms.getPasswordHandler().digest(password));
            stmt.setString(4, m_sqlManager.validateEmpty(description));
            stmt.setString(5, m_sqlManager.validateEmpty(firstname));
            stmt.setString(6, m_sqlManager.validateEmpty(lastname));
            stmt.setString(7, m_sqlManager.validateEmpty(email));
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            stmt.setString(10, m_sqlManager.validateEmpty(address));
            stmt.setInt(11, type);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            internalWriteUserInfo(dbc, id, additionalInfos, null);

        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#importUser(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Map, java.lang.String, int, java.lang.Object)
     */
    public CmsUser importUser(
        CmsDbContext dbc,
        CmsUUID id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        Map additionalInfos,
        String address,
        int type,
        Object reservedParam) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        if (existsUser(dbc, name, type, reservedParam)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_USER_WITH_NAME_ALREADY_EXISTS_1, name);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key());
            }
            throw new CmsDbEntryAlreadyExistsException(message);
        }

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_ADD");
            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, m_sqlManager.validateEmpty(password)); // imported passwords are already encrypted
            stmt.setString(4, m_sqlManager.validateEmpty(description));
            stmt.setString(5, m_sqlManager.validateEmpty(firstname));
            stmt.setString(6, m_sqlManager.validateEmpty(lastname));
            stmt.setString(7, m_sqlManager.validateEmpty(email));
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            stmt.setString(10, m_sqlManager.validateEmpty(address));
            stmt.setInt(11, type);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            internalWriteUserInfo(dbc, id, additionalInfos, reservedParam);

        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUser(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {

            // get connection
            conn = m_sqlManager.getConnection(dbc);

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_WRITE");
            stmt.setString(1, m_sqlManager.validateEmpty(user.getDescription()));
            stmt.setString(2, m_sqlManager.validateEmpty(user.getFirstname()));
            stmt.setString(3, m_sqlManager.validateEmpty(user.getLastname()));
            stmt.setString(4, m_sqlManager.validateEmpty(user.getEmail()));
            stmt.setLong(5, user.getLastlogin());
            stmt.setInt(6, user.getFlags());
            stmt.setString(7, m_sqlManager.validateEmpty(user.getAddress()));
            stmt.setInt(8, user.getType());
            stmt.setString(9, user.getId().toString());
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            internalWriteUserInfo(dbc, user.getId(), user.getAdditionalInfo(), null);

        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Writes the user info as blob.<p>
     * 
     * @param runtimeInfo the current runtime info
     * @param userId the user id
     * @param additionalInfo the additional user info
     * @param reservedParam for future use
     * 
     * @throws CmsException if something goes wrong
     */
    private void internalWriteUserInfo(CmsDbContext dbc, CmsUUID userId, Map additionalInfo, Object reservedParam)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        ResultSet res = null;
        Connection conn = null;

        boolean wasInTransaction = false;

        try {

            // serialize the user info
            byte[] value = internalSerializeAdditionalUserInfo(additionalInfo);

            // get connection
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            wasInTransaction = !conn.getAutoCommit();
            if (!wasInTransaction) {
                conn.setAutoCommit(false);
            }

            // update user_info in this special way because of using blob
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_UPDATEINFO");
            stmt.setString(1, userId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_NO_USER_WITH_ID_1, userId));
            }

            // write serialized user info 
            OutputStream output = getOutputStreamFromBlob(res, "USER_INFO");
            output.write(value);
            output.close();
            value = null;

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

        } catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_SERIALIZING_USER_DATA_1, userId), e);
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                    // ignore
                }
            }
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                    // ignore
                }
            }

            if (!wasInTransaction) {
                if (stmt != null) {
                    try {
                        rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                        rollback.execute();
                        rollback.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                    try {
                        stmt.close();
                    } catch (SQLException exc) {
                        // ignore
                    }
                }
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException se) {
                        // ignore
                    }
                }
            }
        }
    }
}