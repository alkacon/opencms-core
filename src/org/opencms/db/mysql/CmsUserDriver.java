/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsUserDriver.java,v $
 * Date   : $Date: 2004/02/13 13:41:46 $
 * Version: $Revision: 1.16 $
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

package org.opencms.db.mysql;

import org.opencms.util.CmsUUID;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

/**
 * MySQL implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.16 $ $Date: 2004/02/13 13:41:46 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @since 5.1
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser createUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_sqlManager.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // user data is project independent- use a "dummy" project ID to receive
            // a JDBC connection from the offline connection pool
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);

            // crypt the password with MD5
            stmt.setString(3, encryptPassword(password));

            stmt.setString(4, encryptPassword(""));
            stmt.setString(5, description);
            stmt.setString(6, firstname);
            stmt.setString(7, lastname);
            stmt.setString(8, email);
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(0));
            stmt.setInt(11, flags);
            stmt.setBytes(12, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(13, defaultGroup.getId().toString());
            stmt.setString(14, address);
            stmt.setString(15, section);
            stmt.setInt(16, type);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.mysql.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUser(org.opencms.file.CmsUser)
     */
    public void writeUser(CmsUser user) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_WRITE");
            // write data to database
            stmt.setString(1, user.getDescription());
            stmt.setString(2, user.getFirstname());
            stmt.setString(3, user.getLastname());
            stmt.setString(4, user.getEmail());
            stmt.setTimestamp(5, new Timestamp(user.getLastlogin()));
            stmt.setTimestamp(6, new Timestamp(0));
            stmt.setInt(7, user.getFlags());
            stmt.setBytes(8, internalSerializeAdditionalUserInfo(user.getAdditionalInfo()));
            stmt.setString(9, (user.getDefaultGroupId() != null) ? user.getDefaultGroupId().toString() : "");
            stmt.setString(10, user.getAddress());
            stmt.setString(11, user.getSection());
            stmt.setInt(12, user.getType());
            stmt.setString(13, user.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

}
