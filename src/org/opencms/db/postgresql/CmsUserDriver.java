/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/postgresql/CmsUserDriver.java,v $
 * Date   : $Date: 2005/05/13 09:07:23 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db.postgresql;

import org.opencms.db.CmsDataAccessException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsObjectAlreadyExistsException;
import org.opencms.db.CmsObjectNotFoundException;
import org.opencms.db.CmsSerializationException;
import org.opencms.db.CmsSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * PostgreSql implementation of the user driver methods.<p>
 * 
 * @author Antonio Core (antonio@starsolutions.it)
 * 
 * @version $Revision: 1.7 $
 * @since 6.0
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDriver.class); 

    /**
     * @see org.opencms.db.I_CmsUserDriver#createGroup(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, int, java.lang.String, java.lang.Object)
     */
    public CmsGroup createGroup(
        CmsDbContext dbc,
        CmsUUID groupId,
        String groupName,
        String description,
        int flags,
        String parentGroupName,
        Object reservedParam) throws CmsDataAccessException {

        CmsUUID parentId = CmsUUID.getNullUUID();
        CmsGroup group = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        if (existsGroup(dbc, groupName, reservedParam)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_GROUP_WITH_NAME_ALREADY_EXISTS_1, groupName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message);
            }
            throw new CmsObjectAlreadyExistsException(message);
        }

        try {
            // get the id of the parent group if necessary
            if ((parentGroupName != null) && (!"".equals(parentGroupName))) {
                parentId = readGroup(dbc, parentGroupName).getId();
            }

            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_CREATEGROUP");

            // write new group to the database
            stmt.setString(1, groupId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, groupName);
            stmt.setString(4, description);
            stmt.setInt(5, flags);
            stmt.executeUpdate();

            group = new CmsGroup(groupId, parentId, groupName, description, flags);
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUser(org.opencms.db.CmsDbContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, java.lang.String, int)
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
                LOG.error(message);
            }
            throw new CmsObjectAlreadyExistsException(message);
        }

        try {
            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, OpenCms.getPasswordHandler().digest(password));
            stmt.setString(4, description);
            stmt.setString(5, firstname);
            stmt.setString(6, lastname);
            stmt.setString(7, email);
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            stmt.setBytes(10, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(11, address);
            stmt.setInt(12, type);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } catch (IOException e) {
            throw new CmsSerializationException("creating user:", e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#importUser(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, java.lang.String, int, java.lang.Object)
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

        Connection conn = null;
        PreparedStatement stmt = null;

        if (existsUser(dbc, name, type, reservedParam)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_USER_WITH_NAME_ALREADY_EXISTS_1, name);
            if (LOG.isErrorEnabled()) {
                LOG.error(message);
            }
            throw new CmsObjectAlreadyExistsException(message);
        }

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, password);
            stmt.setString(4, description);
            stmt.setString(5, firstname);
            stmt.setString(6, lastname);
            stmt.setString(7, email);
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            stmt.setBytes(10, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(11, address);
            stmt.setInt(12, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } catch (IOException e) {
            throw new CmsSerializationException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:", e);
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
     * @see org.opencms.db.I_CmsUserDriver#writeGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        if (group != null) {
            try {

                // create statement
                conn = m_sqlManager.getConnection(dbc);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_WRITEGROUP");

                stmt.setString(1, group.getDescription());
                stmt.setInt(2, group.getFlags());
                stmt.setString(3, group.getParentId().toString());
                stmt.setString(4, group.getId().toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new CmsSqlException(this, stmt, e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }
        } else {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_GROUP_WITH_NAME_1, group.getName());
            throw new CmsObjectNotFoundException(message);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUser(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_WRITE");
            // write data to database
            stmt.setString(1, user.getDescription());
            stmt.setString(2, user.getFirstname());
            stmt.setString(3, user.getLastname());
            stmt.setString(4, user.getEmail());
            stmt.setLong(5, user.getLastlogin());
            stmt.setInt(6, user.getFlags());
            stmt.setBytes(7, internalSerializeAdditionalUserInfo(user.getAdditionalInfo()));
            stmt.setString(8, user.getAddress());
            stmt.setInt(9, user.getType());
            stmt.setString(10, user.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } catch (IOException e) {
            throw new CmsSerializationException("writing user:", e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

}