/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsUserDriver.java,v $
 * Date   : $Date: 2003/09/22 12:34:33 $
 * Version: $Revision: 1.17 $
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

import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import org.apache.commons.dbcp.DelegatingResultSet;

/**
 * Oracle implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.17 $ $Date: 2003/09/22 12:34:33 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /**
     * @see org.opencms.db.I_CmsUserDriver#addUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, com.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser createUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {

        CmsUUID id = new CmsUUID();
        PreparedStatement stmt = null;
        Connection conn = null;
    
        try {

            conn = m_sqlManager.getConnection();
            
            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_ADD");
            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // crypt the password with MD5
            stmt.setString(3, encryptPassword(password));
            stmt.setString(4, encryptPassword(""));
            stmt.setString(3, encryptPassword(password));

            stmt.setString(4, encryptPassword(""));
            stmt.setString(5, m_sqlManager.validateNull(description));
            stmt.setString(6, m_sqlManager.validateNull(firstname));
            stmt.setString(7, m_sqlManager.validateNull(lastname));
            stmt.setString(8, m_sqlManager.validateNull(email));
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(0));
            stmt.setInt(11, flags);
            stmt.setString(12, defaultGroup.getId().toString());
            stmt.setString(13, m_sqlManager.validateNull(address));
            stmt.setString(14, m_sqlManager.validateNull(section));
            stmt.setInt(15, type);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            internalWriteUserInfo(id, additionalInfos);
             
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "createUser name=" + name + " id=" + id.toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#addImportUser(com.opencms.flex.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, long, int, java.util.Hashtable, com.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser importUser(CmsUUID id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
     
        try {

            conn = m_sqlManager.getConnection();

            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_ADD");
            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // don't encrypt passwords since imported passwords are already encrypted
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
            stmt = null;

            internalWriteUserInfo(id, additionalInfos);
                        
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "importUser name=" + name + " id=" + id.toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUser(com.opencms.file.CmsUser)
     */
    public void writeUser(CmsUser user) throws CmsException {

        PreparedStatement stmt = null;

        Connection conn = null;
        
        try {

            // get connection
            conn = m_sqlManager.getConnection();
            
            // write data to database
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_WRITE");
            stmt.setString(1, m_sqlManager.validateNull(user.getDescription()));
            stmt.setString(2, m_sqlManager.validateNull(user.getFirstname()));
            stmt.setString(3, m_sqlManager.validateNull(user.getLastname()));
            stmt.setString(4, m_sqlManager.validateNull(user.getEmail()));
            stmt.setTimestamp(5, new Timestamp(user.getLastlogin()));
            stmt.setTimestamp(6, new Timestamp(0));
            stmt.setInt(7, user.getFlags());
            stmt.setString(8, user.getDefaultGroupId().toString());
            stmt.setString(9, m_sqlManager.validateNull(user.getAddress()));
            stmt.setString(10, m_sqlManager.validateNull(user.getSection()));
            stmt.setInt(11, user.getType());
            stmt.setString(12, user.getId().toString());
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            
            internalWriteUserInfo(user.getId(), user.getAdditionalInfo());
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "writeUser name=" + user.getName() + " id=" + user.getId().toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * Writes the user info as blob.<p>
     * 
     * @param userId the user id
     * @param additionalInfo the additional user info
     * @throws CmsException if something goes wrong
     */
    private void internalWriteUserInfo (CmsUUID userId, Hashtable additionalInfo) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        ResultSet res = null;
        Connection conn = null;
                
        try {

            // serialize the user info
            byte[] value = internalSerializeAdditionalUserInfo(additionalInfo);
            
            // get connection
            conn = m_sqlManager.getConnection();
            conn.setAutoCommit(false);
                        
            // update user_info in this special way because of using blob
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_UPDATEINFO");
            stmt.setString(1, userId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) 
                throw new CmsException("internalWriteUserInfo id=" + userId.toString() + " user info not found", CmsException.C_NOT_FOUND);
            
            // write serialized user info 
            Blob userInfo = res.getBlob("USER_INFO");
            ((oracle.sql.BLOB)userInfo).trim(0);
            OutputStream output = ((oracle.sql.BLOB)userInfo).getBinaryOutputStream();
            output.write(value);
            output.close();
                         
            commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
            commit.execute();
            commit.close();
            commit = null;
               
            stmt.close();
            stmt = null;
            res = null;
                          
            conn.setAutoCommit(true);
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteUserInfo id=" + userId.toString(), CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteUserInfo id=" + userId.toString(), CmsException.C_SERIALIZATION, e, false);
        } finally {

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException exc) {
                }                
            } 
            if (commit != null) {
                try {
                    commit.close();
                } catch (SQLException exc) {
                }
            } 
            if (stmt != null) {
                try {
                    rollback = m_sqlManager.getPreparedStatement(conn, "C_ROLLBACK");
                    rollback.execute();
                    rollback.close();
                } catch (SQLException se) {
                }
                try {
                    stmt.close();
                } catch (SQLException exc) {
                }                
            }                
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException se) {
                }                   
            }
        }
    }    
}