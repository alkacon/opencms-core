/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsUserDriver.java,v $
 * Date   : $Date: 2005/02/17 12:43:50 $
 * Version: $Revision: 1.24 $
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

package org.opencms.db.mysql;

import org.opencms.db.CmsDataAccessException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsSerializationException;
import org.opencms.db.CmsSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;


/**
 * MySQL implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.24 $ $Date: 2005/02/17 12:43:50 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
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
