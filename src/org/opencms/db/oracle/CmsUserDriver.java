/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsUserDriver.java,v $
 * Date   : $Date: 2004/12/15 12:29:46 $
 * Version: $Revision: 1.36 $
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

import org.opencms.db.CmsDbContext;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
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

/**
 * Oracle implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.36 $ $Date: 2004/12/15 12:29:46 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUser(org.opencms.db.CmsDbContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, java.lang.String, int)
     */
    public CmsUser createUser(CmsDbContext dbc, String name, String password, String description, String firstname, String lastname, String email, long lastlogin, int flags, Map additionalInfos, String address, int type) throws CmsException {

        CmsUUID id = new CmsUUID();
        PreparedStatement stmt = null;
        Connection conn = null;
    
        if (existsUser(dbc, name, type, null)) {
            throw new CmsException("User " + name + " name already exists", CmsException.C_USER_ALREADY_EXISTS);
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
            throw m_sqlManager.getCmsException(this, "createUser name=" + name + " id=" + id.toString(), CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#importUser(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, java.lang.String, int, java.lang.Object)
     */
    public CmsUser importUser(CmsDbContext dbc, CmsUUID id, String name, String password, String description, String firstname, String lastname, String email, long lastlogin, int flags, Map additionalInfos, String address, int type, Object reservedParam) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
     
        if (existsUser(dbc, name, type, reservedParam)) {
            throw new CmsException("User " + name + " name already exists", CmsException.C_USER_ALREADY_EXISTS);
        }
        
        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer) reservedParam).intValue());
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
            throw m_sqlManager.getCmsException(this, "importUser name=" + name + " id=" + id.toString(), CmsException.C_SQL_ERROR, e, true);
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
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsException {

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
            throw m_sqlManager.getCmsException(this, "writeUser name=" + user.getName() + " id=" + user.getId().toString(), CmsException.C_SQL_ERROR, e, false);
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
    private void internalWriteUserInfo(CmsDbContext dbc, CmsUUID userId, Map additionalInfo, Object reservedParam) throws CmsException {

        PreparedStatement stmt = null;
        PreparedStatement commit = null;
        PreparedStatement rollback = null;
        ResultSet res = null;
        Connection conn = null;
            
        try {

            // serialize the user info
            byte[] value = internalSerializeAdditionalUserInfo(additionalInfo);
            
            // get connection
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer) reservedParam).intValue());
            }      
                        
            if (dbc.isDefaultDbContext()) {
                conn.setAutoCommit(false);
            }
                        
            // update user_info in this special way because of using blob
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ORACLE_USERS_UPDATEINFO");
            stmt.setString(1, userId.toString());
            res = ((DelegatingResultSet)stmt.executeQuery()).getInnermostDelegate();
            if (!res.next()) {
                throw new CmsException("internalWriteUserInfo id=" + userId.toString() + " user info not found", CmsException.C_NOT_FOUND);
            }
            
            // write serialized user info 
            OutputStream output = getOutputStreamFromBlob(res, "USER_INFO");
            output.write(value);
            output.close();
            value = null;
                         
            if (dbc.isDefaultDbContext()) {
                commit = m_sqlManager.getPreparedStatement(conn, "C_COMMIT");
                commit.execute();
                m_sqlManager.closeAll(dbc, null, commit, null);      
            }
            
            m_sqlManager.closeAll(dbc, null, stmt, res);      

            commit = null;              
            stmt = null;
            res = null;
                          
            if (dbc.isDefaultDbContext()) {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteUserInfo id=" + userId.toString(), CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "internalWriteUserInfo id=" + userId.toString(), CmsException.C_SERIALIZATION, e, false);
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
            
            if (dbc.isDefaultDbContext()) {
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
        
        // TODO: figure out how to implement this without being Oracle JDBC driver version dependent        
        // this is the code for Oracle 10 (dosen't work with Oracle 9)                
        //        ((oracle.sql.BLOB)blob).truncate(0);
        //        return blob.setBinaryStream(0L);
        
        ((oracle.sql.BLOB)blob).trim(0);
        return ((oracle.sql.BLOB)blob).getBinaryOutputStream();
    }
}