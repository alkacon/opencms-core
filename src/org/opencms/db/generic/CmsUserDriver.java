/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsUserDriver.java,v $
 * Date   : $Date: 2004/02/16 01:30:36 $
 * Version: $Revision: 1.54 $
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

package org.opencms.db.generic;

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsUUID;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Generic (ANSI-SQL) database server implementation of the user driver methods.<p>
 * 
 * @version $Revision: 1.54 $ $Date: 2004/02/16 01:30:36 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @since 5.1
 */
public class CmsUserDriver extends Object implements I_CmsDriver, I_CmsUserDriver {

    /**
     * A digest to encrypt the passwords.
     */
    protected MessageDigest m_digest = null;

    /**
     * The file.encoding to code passwords after encryption with digest.
     */
    protected String m_digestFileEncoding = null;
    
    /**
     * The driver manager
     */
    protected CmsDriverManager m_driverManager;

    /**
     * The sql manager
     */ 
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsUserDriver#createAccessControlEntry(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, int, int, int)
     */
    public void createAccessControlEntry(CmsProject project, CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_CREATE");

            stmt.setString(1, resource.toString());
            stmt.setString(2, principal.toString());
            stmt.setInt(3, allowed);
            stmt.setInt(4, denied);
            stmt.setInt(5, flags);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createGroup(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, int, java.lang.String, java.lang.Object)
     */
    public CmsGroup createGroup(CmsUUID groupId, String groupName, String description, int flags, String parentGroupName, Object reservedParam) throws CmsException {
        CmsUUID parentId = CmsUUID.getNullUUID();
        CmsGroup group = null;

        Connection conn = null;
        PreparedStatement stmt = null;

        try {

            // get the id of the parent group if necessary
            if ((parentGroupName != null) && (!"".equals(parentGroupName))) {
                parentId = readGroup(parentGroupName).getId();
            }

            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_CREATEGROUP");

            // write new group to the database
            stmt.setString(1, groupId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, groupName);
            stmt.setString(4, m_sqlManager.validateNull(description));
            stmt.setInt(5, flags);
            stmt.executeUpdate();

            // TODO: remove this
            // create the user group by reading it from the database.
            // this is necessary to get the group id which is generated in the
            // database.
            // group = readGroup(groupName);
            group = new CmsGroup(groupId, parentId, groupName, description, flags);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "[CmsGroup]: " + groupName + ", Id=" + groupId.toString(), CmsException.C_SQL_ERROR, e, true);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, java.util.Hashtable, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int)
     */
    public CmsUser createUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
        //int id = m_sqlManager.nextPkId("C_TABLE_USERS");
        CmsUUID id = new CmsUUID();

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write data to database

            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            // crypt the password with MD5
            stmt.setString(3, encryptPassword(password));
            stmt.setString(4, encryptPassword(""));
            stmt.setString(5, m_sqlManager.validateNull(description));
            stmt.setString(6, m_sqlManager.validateNull(firstname));
            stmt.setString(7, m_sqlManager.validateNull(lastname));
            stmt.setString(8, m_sqlManager.validateNull(email));
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(0));
            stmt.setInt(11, flags);
            m_sqlManager.setBytes(stmt, 12, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(13, defaultGroup.getId().toString());
            stmt.setString(14, m_sqlManager.validateNull(address));
            stmt.setString(15, m_sqlManager.validateNull(section));
            stmt.setInt(16, type);
            stmt.executeUpdate();            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // cw 20.06.2003 avoid calling upper level methods
        // return readUser(id);
        return this.readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUserInGroup(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.Object)
     */
    public void createUserInGroup(CmsUUID userid, CmsUUID groupid, Object reservedParam) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        // check if user is already in group
        if (!internalValidateUserInGroup(userid, groupid, reservedParam)) {
            // if not, add this user to the group
            try {
                if (reservedParam == null) {
                    // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                    conn = m_sqlManager.getConnection();
                } else {
                    // get a JDBC connection from the reserved JDBC pools
                    conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
                }
                
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_ADDUSERTOGROUP");

                // write the new assingment to the database
                stmt.setString(1, groupid.toString());
                stmt.setString(2, userid.toString());
                // flag field is not used yet
                stmt.setInt(3, I_CmsConstants.C_UNKNOWN_INT);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteAccessControlEntries(org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void deleteAccessControlEntries(CmsProject project, CmsUUID resource) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_SETFLAGS_ALL");

            stmt.setInt(1, I_CmsConstants.C_ACCESSFLAGS_DELETED);
            stmt.setString(2, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteGroup(java.lang.String)
     */
    public void deleteGroup(String delgroup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_DELETEGROUP");

            stmt.setString(1, delgroup);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_DELETE");
            stmt.setString(1, userName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteUserInGroup(org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void deleteUserInGroup(CmsUUID userId, CmsUUID groupId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_REMOVEUSERFROMGROUP");

            stmt.setString(1, groupId.toString());
            stmt.setString(2, userId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#encryptPassword(java.lang.String)
     */
    public String encryptPassword(String value) {
        // is there a valid digest?
        if (m_digest != null) {
            try {
                byte[] bytesFromDigest = m_digest.digest(value.getBytes(m_digestFileEncoding));
                // to get a String out of the bytearray we translate every byte
                // in a hex value and put them together
                StringBuffer result = new StringBuffer();
                String addZerro;
                for (int i = 0; i < bytesFromDigest.length; i++) {
                    addZerro = Integer.toHexString(128 + bytesFromDigest[i]);
                    if (addZerro.length() < 2) {
                        addZerro = "0" + addZerro;
                    }
                    result.append(addZerro);
                }
                bytesFromDigest = null;
                return result.toString();
            } catch (UnsupportedEncodingException exc) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("File encoding " + m_digestFileEncoding + " for passwords not supported. Using the default.");
                }
                return new String(m_digest.digest(value.getBytes()));
            }
        } else {
            // no digest - use clear passwords
            return value;
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            m_sqlManager = null;
            m_driverManager = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#importUser(org.opencms.util.CmsUUID, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, long, int, java.util.Hashtable, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int, java.lang.Object)
     */
    public CmsUser importUser(CmsUUID id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type, Object reservedParam) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, m_sqlManager.validateNull(password));
            stmt.setString(4, m_sqlManager.validateNull(recoveryPassword));
            stmt.setString(5, m_sqlManager.validateNull(description));
            stmt.setString(6, m_sqlManager.validateNull(firstname));
            stmt.setString(7, m_sqlManager.validateNull(lastname));
            stmt.setString(8, m_sqlManager.validateNull(email));
            stmt.setTimestamp(9, new Timestamp(lastlogin));
            stmt.setTimestamp(10, new Timestamp(lastused));
            stmt.setInt(11, flags);
            m_sqlManager.setBytes(stmt, 12, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(13, defaultGroup.getId().toString());
            stmt.setString(14, m_sqlManager.validateNull(address));
            stmt.setString(15, m_sqlManager.validateNull(section));
            stmt.setInt(16, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "[CmsUser]: " + name + ", Id=" + id.toString(), CmsException.C_SQL_ERROR, e, true);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, "[CmsAccessUserInfoMySql/addUserInformation(id,object)]:", CmsException.C_SERIALIZATION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readUser(id);
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {
        String poolUrl = configuration.getString("db.user.pool");

        m_sqlManager = this.initQueries();
        m_sqlManager.setPoolUrlOffline(poolUrl);
        m_sqlManager.setPoolUrlOnline(poolUrl);
        m_sqlManager.setPoolUrlBackup(poolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assigned pool        : " + poolUrl);
        }

        String digest = configuration.getString(I_CmsConstants.C_CONFIGURATION_DB + ".user.digest.type", "MD5");
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Digest configured    : " + digest);
        }

        m_digestFileEncoding = configuration.getString(I_CmsConstants.C_CONFIGURATION_DB + ".user.digest.encoding", "UTF-8");
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Digest file encoding : " + m_digestFileEncoding);
        }

        // create the digest
        try {
            m_digest = MessageDigest.getInstance(digest);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Using digest encoding: " + m_digest.getAlgorithm() + " from " + m_digest.getProvider().getName() + " version " + m_digest.getProvider().getVersion());
            }
        } catch (NoSuchAlgorithmException e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Error setting digest : using clear passwords - " + e.getMessage());
            }
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(this.getClass().toString() + " does not support successive drivers");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * Internal helper method to create an access control entry from a database record.<p>
     * 
     * @param res resultset of the current query
     * @return a new CmsAccessControlEntry initialized with the values from the current database record
     * @throws SQLException if something goes wrong
     */
    private CmsAccessControlEntry internalCreateAce(ResultSet res) throws SQLException {
        // this method is final to allow the java compiler to inline this code!

        return new CmsAccessControlEntry(new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_RESOURCE_ID"))), new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_PRINCIPAL_ID"))), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_ALLOWED")), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_DENIED")), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_FLAGS")));
    }

    /**
     * Internal helper method to create an access control entry from a database record.<p>
     * 
     * @param res resultset of the current query
     * @param newId the id of the new access control entry
     * @return a new CmsAccessControlEntry initialized with the values from the current database record
     * @throws SQLException if something goes wrong
     */
    private CmsAccessControlEntry internalCreateAce(ResultSet res, CmsUUID newId) throws SQLException {
        // this method is final to allow the java compiler to inline this code!

        return new CmsAccessControlEntry(newId, new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_PRINCIPAL_ID"))), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_ALLOWED")), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_DENIED")), res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_FLAGS")));
    }

    /**
     * Semi-constructor to create a CmsGroup instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param hasGroupIdInResultSet true if the SQL select query includes the GROUP_ID table attribute
     * @return CmsGroup the new CmsGroup object
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    protected final CmsGroup internalCreateGroup(ResultSet res, boolean hasGroupIdInResultSet) throws SQLException {
        // this method is final to allow the java compiler to inline this code!
        CmsUUID groupId = null;

        if (hasGroupIdInResultSet) {
            groupId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_ID")));
        } else {
            groupId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_USERS_USER_DEFAULT_GROUP_ID")));
        }

        return new CmsGroup(groupId, new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_PARENT_GROUP_ID"))), res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_NAME")), res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_DESCRIPTION")), res.getInt(m_sqlManager.readQuery("C_GROUPS_GROUP_FLAGS")));
    }

    /**
     * Semi-constructor to create a CmsUser instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param hasGroupIdInResultSet true, if a default group id is available
     * @return CmsUser the new CmsUser object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws IOException if there is an error in deserializing the user info
     * @throws ClassNotFoundException if there is an error in deserializing the user info
     */
    protected final CmsUser internalCreateUser(ResultSet res, boolean hasGroupIdInResultSet) throws SQLException, IOException, ClassNotFoundException {
        // this method is final to allow the java compiler to inline this code!

        // deserialize the additional userinfo hash
        ByteArrayInputStream bin = new ByteArrayInputStream(m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_USERS_USER_INFO")));
        ObjectInputStream oin = new ObjectInputStream(bin);
        Hashtable info = (Hashtable)oin.readObject();

        return new CmsUser(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_USERS_USER_ID"))),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_NAME")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_PASSWORD")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_RECOVERY_PASSWORD")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_DESCRIPTION")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_FIRSTNAME")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_LASTNAME")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_EMAIL")),
            CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_USERS_USER_LASTLOGIN")).getTime(),
            res.getInt(m_sqlManager.readQuery("C_USERS_USER_FLAGS")),
            info,
            internalCreateGroup(res, hasGroupIdInResultSet),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_ADDRESS")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_SECTION")),
            res.getInt(m_sqlManager.readQuery("C_USERS_USER_TYPE")));
    }

    /**
     * Serialize additional user information to write it as byte array in the database.<p>
     * 
     * @param additionalUserInfo the HashTable with additional information
     * @return byte[] the byte array which is written to the db
     * @throws IOException if something goes wrong
     */
    protected final byte[] internalSerializeAdditionalUserInfo(Hashtable additionalUserInfo) throws IOException {
        // this method is final to allow the java compiler to inline this code!

        // serialize the hashtable
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(additionalUserInfo);
        oout.close();

        return bout.toByteArray();
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#publishAccessControlEntries(org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void publishAccessControlEntries(CmsProject offlineProject, CmsProject onlineProject, CmsUUID offlineId, CmsUUID onlineId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        // at first, we remove all access contries of this resource in the online project
        removeAccessControlEntries(onlineProject, onlineId);

        // then, we copy the access control entries from the offline project into the online project
        try {
            conn = m_sqlManager.getConnection(offlineProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, offlineProject, "C_ACCESS_READ_ENTRIES");

            stmt.setString(1, offlineId.toString());

            res = stmt.executeQuery();

            while (res.next()) {
                CmsAccessControlEntry ace = internalCreateAce(res, onlineId);
                if ((ace.getFlags() & I_CmsConstants.C_ACCESSFLAGS_DELETED) == 0) {
                    writeAccessControlEntry(onlineProject, ace);
                }
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readAccessControlEntries(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, boolean)
     */
    public Vector readAccessControlEntries(CmsProject project, CmsUUID resource, boolean inheritedOnly) throws CmsException {

        Vector aceList = new Vector();
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_READ_ENTRIES");

            String resId = resource.toString();
            stmt.setString(1, resId);

            res = stmt.executeQuery();

            // create new CmsAccessControlEntry and add to list
            while (res.next()) {
                CmsAccessControlEntry ace = internalCreateAce(res);
                if ((ace.getFlags() & I_CmsConstants.C_ACCESSFLAGS_DELETED) > 0) {
                    continue;
                }

                if (inheritedOnly && ((ace.getFlags() & I_CmsConstants.C_ACCESSFLAGS_INHERIT) == 0)) {
                    continue;
                }

                if (inheritedOnly && ((ace.getFlags() & I_CmsConstants.C_ACCESSFLAGS_INHERIT) > 0)) {
                    ace.setFlags(I_CmsConstants.C_ACCESSFLAGS_INHERITED);
                }

                aceList.add(ace);
            }

            return aceList;

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsUserDriver#hasGroupsOfUserLike(org.opencms.util.CmsUUID, String, String)
     */
    public boolean hasGroupsOfUserLike(CmsUUID userId, String paramStr, String groupNamePattern) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        boolean found = false;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_HASGROUPSOFUSER_LIKE");

            stmt.setString(1, userId.toString());
            stmt.setString(2, groupNamePattern);

            res = stmt.executeQuery();
            if (res.next()) {
                found = res.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        
        return found;
    }    

    /**
     * @see org.opencms.db.I_CmsUserDriver#readAccessControlEntry(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsAccessControlEntry readAccessControlEntry(CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsException {

        CmsAccessControlEntry ace = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_READ_ENTRY");

            stmt.setString(1, resource.toString());
            stmt.setString(2, principal.toString());

            res = stmt.executeQuery();

            // create new CmsAccessControlEntry
            if (res.next()) {
                ace = internalCreateAce(res);
            } else {
                res.close();
                res = null;
                throw new CmsException("[" + this.getClass().getName() + "]", CmsException.C_NOT_FOUND);
            }

            return ace;

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, true);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readChildGroups(java.lang.String)
     */
    public Vector readChildGroups(String groupname) throws CmsException {

        Vector childs = new Vector();
        CmsGroup group;
        CmsGroup parent;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // get parent group
            parent = readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
                // create statement
                conn = m_sqlManager.getConnection();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETCHILD");
                stmt.setString(1, parent.getId().toString());
                res = stmt.executeQuery();
                // create new Cms group objects
                while (res.next()) {
                    group = new CmsGroup(new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_ID"))), new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_PARENT_GROUP_ID"))), res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_NAME")), res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_DESCRIPTION")), res.getInt(m_sqlManager.readQuery("C_GROUPS_GROUP_FLAGS")));
                    childs.addElement(group);
                }
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        //check if the child vector has no elements, set it to null.
        if (childs.size() == 0) {
            childs = null;
        }
        return childs;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroup(org.opencms.util.CmsUUID)
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {
        CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_READGROUP2");

            // read the group from the database
            stmt.setString(1, groupId.toString());
            res = stmt.executeQuery();
            // create new Cms group object
            if (res.next()) {
                group = internalCreateGroup(res, true);
            } else {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_NO_GROUP, new Exception("No group found with UUID " + groupId), false);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroup(java.lang.String)
     */
    public CmsGroup readGroup(String groupName) throws CmsException {

        CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_READGROUP");

            // read the group from the database
            stmt.setString(1, groupName);
            res = stmt.executeQuery();

            // create new Cms group object
            if (res.next()) {
                group = internalCreateGroup(res, true);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + groupName, CmsException.C_NO_GROUP);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroups()
     */
    public Vector readGroups() throws CmsException {
        Vector groups = new Vector();
        //CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETGROUPS");

            res = stmt.executeQuery();

            // create new Cms group objects
            while (res.next()) {
                groups.addElement(internalCreateGroup(res, true));
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return groups;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroupsOfUser(org.opencms.util.CmsUUID, java.lang.String)
     */
    public Vector readGroupsOfUser(CmsUUID userId, String paramStr) throws CmsException {
        //CmsGroup group;
        Vector groups = new Vector();

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETGROUPSOFUSER");

            //  get all all groups of the user
            stmt.setString(1, userId.toString());

            res = stmt.executeQuery();

            while (res.next()) {
                groups.addElement(internalCreateGroup(res, true));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return groups;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(org.opencms.util.CmsUUID)
     */
    public CmsUser readUser(CmsUUID id) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READID");

            stmt.setString(1, id.toString());
            res = stmt.executeQuery();

            // create new Cms user object
            if (res.next()) {
                user = internalCreateUser(res, true);
            } else {
                res.close();
                res = null;
                throw new CmsException("[" + this.getClass().getName() + "]" + id, CmsException.C_NO_USER);
            }

            return user;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException e) {
            throw m_sqlManager.getCmsException(this, null, e.getType(), e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(java.lang.String, int)
     */
    public CmsUser readUser(String name, int type) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READ");
            stmt.setString(1, name);
            stmt.setInt(2, type);

            res = stmt.executeQuery();

            if (res.next()) {
                user = internalCreateUser(res, true);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] '" + name + "'", CmsException.C_NO_USER);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException e) {
            throw m_sqlManager.getCmsException(this, null, e.getType(), e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return user;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(java.lang.String, java.lang.String, int)
     */
    public CmsUser readUser(String name, String password, int type) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READPW");
            stmt.setString(1, name);
            stmt.setString(2, encryptPassword(password));
            stmt.setInt(3, type);
            res = stmt.executeQuery();

            // create new Cms user object
            if (res.next()) {
                user = internalCreateUser(res, true);
            } else {
                res.close();
                res = null;
                throw new CmsException("[" + this.getClass().getName() + "]" + name, CmsException.C_NO_USER);
            }

            return user;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException e) {
            throw m_sqlManager.getCmsException(this, null, e.getType(), e, true);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public CmsUser readUser(String name, String password, String remoteAddress, int type) throws CmsException {
        CmsUser user = readUser(name, password, type);
        return user;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsers(int)
     */
    public Vector readUsers(int type) throws CmsException {
        Vector users = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_GETUSERS");
            stmt.setInt(1, type);
            res = stmt.executeQuery();
            // create new Cms user objects
            while (res.next()) {
                users.addElement(internalCreateUser(res, true));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsers(int, java.lang.String)
     */
    public Vector readUsers(int type, String namefilter) throws CmsException {
        Vector users = new Vector();
        Statement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = conn.createStatement();
            res = stmt.executeQuery(m_sqlManager.readQuery("C_USERS_GETUSERS_FILTER1") + type + m_sqlManager.readQuery("C_USERS_GETUSERS_FILTER2") + namefilter + m_sqlManager.readQuery("C_USERS_GETUSERS_FILTER3"));

            // create new Cms user objects
            while (res.next()) {
                users.addElement(internalCreateUser(res, true));
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsers(java.lang.String, int, int, int, int)
     */
    public Vector readUsers(String lastname, int userType, int userStatus, int wasLoggedIn, int nMax) throws CmsException {
        Vector users = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        int i = 0;
        // "" =  return (nearly) all users
        if (lastname == null) {
            lastname = "";
        }

        try {
            conn = m_sqlManager.getConnection();

            if (wasLoggedIn == I_CmsConstants.C_AT_LEAST_ONCE) {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_GETUSERS_BY_LASTNAME_ONCE");
            } else if (wasLoggedIn == I_CmsConstants.C_NEVER) {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_GETUSERS_BY_LASTNAME_NEVER");
            } else { 
                // C_WHATEVER or whatever else
                stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_GETUSERS_BY_LASTNAME_WHATEVER");
            }

            stmt.setString(1, lastname + "%");
            stmt.setInt(2, userType);
            stmt.setInt(3, userStatus);

            res = stmt.executeQuery();
            // create new Cms user objects
            while (res.next() && (i++ < nMax)) {
                users.addElement(internalCreateUser(res, true));
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsersOfGroup(java.lang.String, int)
     */
    public Vector readUsersOfGroup(String name, int type) throws CmsException {
        Vector users = new Vector();

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETUSERSOFGROUP");
            stmt.setString(1, name);
            stmt.setInt(2, type);

            res = stmt.executeQuery();

            while (res.next()) {
                users.addElement(internalCreateUser(res, false));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#removeAccessControlEntries(org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void removeAccessControlEntries(CmsProject project, CmsUUID resource) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE_ALL");

            stmt.setString(1, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#removeAccessControlEntry(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void removeAccessControlEntry(CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE");

            stmt.setString(1, resource.toString());
            stmt.setString(2, principal.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#undeleteAccessControlEntries(org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void undeleteAccessControlEntries(CmsProject project, CmsUUID resource) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_RESETFLAGS_ALL");

            stmt.setInt(1, I_CmsConstants.C_ACCESSFLAGS_DELETED);
            stmt.setString(2, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Checks if a user is member of a group.<P/>
     *
     * @param userId the id of the user to check
     * @param groupId the id of the group to check
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * @return true if user is member of group
     * @throws CmsException if operation was not succesful
     */
    private boolean internalValidateUserInGroup(CmsUUID userId, CmsUUID groupId, Object reservedParam) throws CmsException {
        boolean userInGroup = false;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_USERINGROUP");

            stmt.setString(1, groupId.toString());
            stmt.setString(2, userId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                userInGroup = true;
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return userInGroup;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeAccessControlEntry(org.opencms.file.CmsProject, org.opencms.security.CmsAccessControlEntry)
     */
    public void writeAccessControlEntry(CmsProject project, CmsAccessControlEntry acEntry) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_READ_ENTRY");

            stmt.setString(1, acEntry.getResource().toString());
            stmt.setString(2, acEntry.getPrincipal().toString());

            res = stmt.executeQuery();
            if (!res.next()) {
                createAccessControlEntry(project, acEntry.getResource(), acEntry.getPrincipal(), acEntry.getAllowedPermissions(), acEntry.getDeniedPermissions(), acEntry.getFlags());
                return;
            }

            // otherwise update the already existing entry

            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_UPDATE");

            stmt.setInt(1, acEntry.getAllowedPermissions());
            stmt.setInt(2, acEntry.getDeniedPermissions());
            stmt.setInt(3, acEntry.getFlags());
            stmt.setString(4, acEntry.getResource().toString());
            stmt.setString(5, acEntry.getPrincipal().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeGroup(org.opencms.file.CmsGroup)
     */
    public void writeGroup(CmsGroup group) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        if (group != null) {
            try {

                // create statement
                conn = m_sqlManager.getConnection();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_WRITEGROUP");

                stmt.setString(1, m_sqlManager.validateNull(group.getDescription()));
                stmt.setInt(2, group.getFlags());
                stmt.setString(3, group.getParentId().toString());
                stmt.setString(4, group.getId().toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_NO_GROUP);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writePassword(java.lang.String, java.lang.String)
     */
    public void writePassword(String userName, String password) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_SETPW");
            stmt.setString(1, encryptPassword(password));
            stmt.setString(2, userName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void writePassword(String userName, String recoveryPassword, String password) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        int result;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_RECOVERPW");

            stmt.setString(1, encryptPassword(password));
            stmt.setString(2, userName);
            stmt.setString(3, encryptPassword(recoveryPassword));
            result = stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        if (result != 1) {
            // the update wasn't succesfull -> throw exception
            throw new CmsException("[" + this.getClass().getName() + "] the password couldn't be recovered.");
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeRecoveryPassword(java.lang.String, java.lang.String)
     */
    public void writeRecoveryPassword(String userName, String password) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int result;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_SETRECPW");

            stmt.setString(1, encryptPassword(password));
            stmt.setString(2, userName);
            result = stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        if (result != 1) {
            // the update wasn't succesfull -> throw exception
            throw new CmsException("[" + this.getClass().getName() + "] new password couldn't be set.");
        }
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
            stmt.setString(1, m_sqlManager.validateNull(user.getDescription()));
            stmt.setString(2, m_sqlManager.validateNull(user.getFirstname()));
            stmt.setString(3, m_sqlManager.validateNull(user.getLastname()));
            stmt.setString(4, m_sqlManager.validateNull(user.getEmail()));
            stmt.setTimestamp(5, new Timestamp(user.getLastlogin()));
            stmt.setTimestamp(6, new Timestamp(0));
            stmt.setInt(7, user.getFlags());
            m_sqlManager.setBytes(stmt, 8, internalSerializeAdditionalUserInfo(user.getAdditionalInfo()));
            stmt.setString(9, user.getDefaultGroupId().toString());
            stmt.setString(10, m_sqlManager.validateNull(user.getAddress()));
            stmt.setString(11, m_sqlManager.validateNull(user.getSection()));
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

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUserType(org.opencms.util.CmsUUID, int)
     */
    public void writeUserType(CmsUUID userId, int userType) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {

            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_UPDATE_USERTYPE");

            // write data to database
            stmt.setInt(1, userType);
            stmt.setString(2, userId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsUserDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }
    
    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroupsLike(String)
     */
    public Vector readGroupsLike(String namePattern) throws CmsException {
        Vector groups = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETGROUPS_LIKE");
            stmt.setString(1, namePattern);

            res = stmt.executeQuery();

            // create new Cms group objects
            while (res.next()) {
                groups.addElement(internalCreateGroup(res, true));
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        
        return groups;
    }    
    
}