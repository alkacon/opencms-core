/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsUserAccess.java,v $
 * Date   : $Date: 2003/05/20 11:30:51 $
 * Version: $Revision: 1.6 $
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
package com.opencms.file.mySql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.file.genericSql.I_CmsUserAccess;
import com.opencms.flex.util.CmsUUID;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import source.org.apache.java.util.Configurations;

/**
 * MySQL implementation of the user access methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.6 $ $Date: 2003/05/20 11:30:51 $
 * 
 * @see com.opencms.file.genericSql.CmsUserAccess
 * @see com.opencms.file.genericSql.I_CmsUserAccess
 */
public class CmsUserAccess extends com.opencms.file.genericSql.CmsUserAccess implements I_CmsConstants, I_CmsLogChannels, I_CmsUserAccess {

    /**
     * Default constructor.
     * 
     * @param config the configurations objects (-> opencms.properties)
     * @param theResourceBroker the instance of the resource broker
     */
    public CmsUserAccess(Configurations config, String dbPoolUrl, I_CmsResourceBroker theResourceBroker) {
        super(config, dbPoolUrl, theResourceBroker);
    }

    /**
     * Adds a user to the database.
     *
     * @param name username
     * @param password user-password
     * @param description user-description
     * @param firstname user-firstname
     * @param lastname user-lastname
     * @param email user-email
     * @param lastlogin user-lastlogin
     * @param lastused user-lastused
     * @param flags user-flags
     * @param additionalInfos user-additional-infos
     * @param defaultGroup user-defaultGroup
     * @param address user-defauladdress
     * @param section user-section
     * @param type user-type
     *
     * @return the created user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_SqlQueries.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();
        byte[] value = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            value = serializeAdditionalUserInfo( additionalInfos );

            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            conn = m_SqlQueries.getConnection();
            stmt = m_SqlQueries.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // crypt the password with MD5
            stmt.setString(3, digest(password));

            stmt.setString(4, digest(""));
            stmt.setString(5, description);
            stmt.setString(6, firstname);
            stmt.setString(7, lastname);
            stmt.setString(8, email);
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(lastused));
            stmt.setInt(11, flags);
            stmt.setBytes(12, value);
            stmt.setString(13, defaultGroup.getId().toString());
            stmt.setString(14, address);
            stmt.setString(15, section);
            stmt.setInt(16, type);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, null);
        }

        return readUser(id);
    }

    /**
     * Add a new group to the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param name The name of the new group.
     * @param description The description for the new group.
     * @param flags The flags for the new group.
     * @param name The name of the parent group (or null).
     *
     * @return Group
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsGroup createGroup(String name, String description, int flags, String parent) throws CmsException {
        CmsUUID parentId = CmsUUID.getNullUUID();
        CmsGroup newGroup = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID newGroupId = new CmsUUID();
        
        try {
            // get the id of the parent group if nescessary            
            if ((parent != null) && (!"".equals(parent))) {
                parentId = readGroup(parent).getId();
            }
            
            // create statement
            conn = m_SqlQueries.getConnection();
            stmt = m_SqlQueries.getPreparedStatement(conn, "C_GROUPS_CREATEGROUP");
            
            // write new group to the database
            stmt.setString(1, newGroupId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setInt(5, flags);
            stmt.executeUpdate();

            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            newGroup = readGroup(name);
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, null);
        }

        return newGroup;
    }

    public com.opencms.file.genericSql.CmsQueries initQueries(String dbPoolUrl) {
        com.opencms.file.mySql.CmsQueries queries = new com.opencms.file.mySql.CmsQueries();
        queries.initJdbcPoolUrls(dbPoolUrl);

        return queries;
    }

    /**
     * Writes a user to the database.
     *
     * @param user the user to write
     * @throws thorws CmsException if something goes wrong.
     */
    public void writeUser(CmsUser user) throws CmsException {
        byte[] value = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_SqlQueries.getConnection();
            stmt = m_SqlQueries.getPreparedStatement(conn, "C_USERS_WRITE");
            
            value = serializeAdditionalUserInfo( user.getAdditionalInfo() );

            // write data to database
            stmt.setString(1, user.getDescription());
            stmt.setString(2, user.getFirstname());
            stmt.setString(3, user.getLastname());
            stmt.setString(4, user.getEmail());
            stmt.setTimestamp(5, new Timestamp(user.getLastlogin()));
            stmt.setTimestamp(6, new Timestamp(user.getLastUsed()));
            stmt.setInt(7, user.getFlags());
            stmt.setBytes(8, value);
            stmt.setString(9, user.getDefaultGroupId().toString());
            stmt.setString(10, user.getAddress());
            stmt.setString(11, user.getSection());
            stmt.setInt(12, user.getType());
            stmt.setString(13, user.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_SqlQueries.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_SqlQueries.closeAll(conn, stmt, null);
        }
    }

}
