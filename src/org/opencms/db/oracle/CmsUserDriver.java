/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsUserDriver.java,v $
 * Date   : $Date: 2003/08/01 10:39:58 $
 * Version: $Revision: 1.2 $
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

package org.opencms.db.oracle;

import oracle.jdbc.driver.OracleResultSet;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

/**
 * Oracle/OCI implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.2 $ $Date: 2003/08/01 10:39:58 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /**
     * @see org.opencms.db.I_CmsUserDriver#addImportUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, long, int, java.util.Hashtable, com.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser addImportUser(CmsUUID id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {

        byte[] value = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            value = serializeAdditionalUserInfo(additionalInfos);

            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSFORINSERT");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // crypt the password with MD5
            stmt.setString(3, m_sqlManager.validateNull(password));

            stmt.setString(4, m_sqlManager.validateNull(recoveryPassword));
            stmt.setString(5, m_sqlManager.validateNull(description));
            stmt.setString(6, m_sqlManager.validateNull(firstname));
            stmt.setString(7, m_sqlManager.validateNull(lastname));
            stmt.setString(8, m_sqlManager.validateNull(email));
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(lastused));
            stmt.setInt(11, flags);
            stmt.setString(12, defaultGroup.getId().toString());
            stmt.setString(13, m_sqlManager.validateNull(address));
            stmt.setString(14, m_sqlManager.validateNull(section));
            stmt.setInt(15, type);

            stmt.executeUpdate();
            stmt.close();

            // now update user_info of the new user
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSFORUPDATE");
            stmt2.setString(1, id.toString());
            conn.setAutoCommit(false);

            res = stmt2.executeQuery();
            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }

            stmt2.close();
            res.close();

            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();

            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "[CmsAccessUserInfoMySql/addUserInformation(id,object)]:", CmsException.C_SERIALIZATION, e, false);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException se) {
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#addUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, long, int, java.util.Hashtable, com.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_sqlManager.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();
        byte[] value = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            value = serializeAdditionalUserInfo(additionalInfos);

            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSFORINSERT");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // crypt the password with MD5
            stmt.setString(3, digest(password));

            stmt.setString(4, digest(""));
            stmt.setString(5, m_sqlManager.validateNull(description));
            stmt.setString(6, m_sqlManager.validateNull(firstname));
            stmt.setString(7, m_sqlManager.validateNull(lastname));
            stmt.setString(8, m_sqlManager.validateNull(email));
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(lastused));
            stmt.setInt(11, flags);
            stmt.setString(12, defaultGroup.getId().toString());
            stmt.setString(13, m_sqlManager.validateNull(address));
            stmt.setString(14, m_sqlManager.validateNull(section));
            stmt.setInt(15, type);
            stmt.executeUpdate();
            stmt.close();

            // now update user_info of the new user
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSFORUPDATE");
            stmt2.setString(1, id.toString());
            conn.setAutoCommit(false);
            res = stmt2.executeQuery();

            while (res.next()) {
                oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
                ByteArrayInputStream instream = new ByteArrayInputStream(value);
                OutputStream outstream = blob.getBinaryOutputStream();
                byte[] chunk = new byte[blob.getChunkSize()];
                int i = -1;
                while ((i = instream.read(chunk)) != -1) {
                    outstream.write(chunk, 0, i);
                }
                instream.close();
                outstream.close();
            }

            stmt2.close();
            res.close();
            // for the oracle-driver commit or rollback must be executed manually
            // because setAutoCommit = false in CmsDbPool.CmsDbPool
            nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            nextStmt.execute();

            nextStmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "[CmsAccessUserInfoMySql/addUserInformation(id,object)]:", CmsException.C_SERIALIZATION, e, false);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException se) {
                }
            }
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new org.opencms.db.oracle.CmsSqlManager(dbPoolUrl);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUser(com.opencms.file.CmsUser)
     */
    public void writeUser(CmsUser user) throws CmsException {
        byte[] value = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement nextStmt = null;
        PreparedStatement trimStmt = null;

        ResultSet res = null;
        Connection conn = null;
        try {
            value = serializeAdditionalUserInfo(user.getAdditionalInfo());

            // write data to database
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSWRITE");
            stmt.setString(1, m_sqlManager.validateNull(user.getDescription()));
            stmt.setString(2, m_sqlManager.validateNull(user.getFirstname()));
            stmt.setString(3, m_sqlManager.validateNull(user.getLastname()));
            stmt.setString(4, m_sqlManager.validateNull(user.getEmail()));
            stmt.setTimestamp(5, new Timestamp(user.getLastlogin()));
            stmt.setTimestamp(6, new Timestamp(user.getLastUsed()));
            stmt.setInt(7, user.getFlags());
            stmt.setString(8, user.getDefaultGroupId().toString());
            stmt.setString(9, m_sqlManager.validateNull(user.getAddress()));
            stmt.setString(10, m_sqlManager.validateNull(user.getSection()));
            stmt.setInt(11, user.getType());
            stmt.setString(12, user.getId().toString());
            stmt.executeUpdate();
            stmt.close();
            // update user_info in this special way because of using blob
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERSFORUPDATE");
            stmt2.setString(1, user.getId().toString());
            conn.setAutoCommit(false);
            res = stmt2.executeQuery();
            try {
                while (res.next()) {
                    oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("USER_INFO");
                    // first trim the blob to 0 bytes, otherwise ther could be left some bytes
                    // of the old content
                    trimStmt = m_sqlManager.getPreparedStatement(conn, "C_TRIMBLOB");
                    trimStmt.setBlob(1, blobnew);
                    trimStmt.setInt(2, 0);
                    trimStmt.execute();
                    trimStmt.close();
                    ByteArrayInputStream instream = new ByteArrayInputStream(value);
                    OutputStream outstream = blobnew.getBinaryOutputStream();
                    byte[] chunk = new byte[blobnew.getChunkSize()];
                    int i = -1;
                    while ((i = instream.read(chunk)) != -1) {
                        outstream.write(chunk, 0, i);
                    }
                    instream.close();
                    outstream.close();
                }
                // for the oracle-driver commit or rollback must be executed manually
                // because setAutoCommit = false in CmsDbPool.CmsDbPool
                nextStmt = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                nextStmt.execute();
                nextStmt.close();
                conn.setAutoCommit(true);
            } catch (IOException e) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
            }
            stmt2.close();
            res.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "[CmsAccessUserInfoMySql/addUserInformation(id,object)]:", CmsException.C_SERIALIZATION, e, false);
        } finally {
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException exc) {
                }
                try {
                    nextStmt = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    nextStmt.execute();
                } catch (SQLException se) {
                }
            }
            m_sqlManager.closeAll(null, trimStmt, null);
            m_sqlManager.closeAll(null, nextStmt, null);
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

}