/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsUserDriver.java,v $
 * Date   : $Date: 2005/06/27 23:22:15 $
 * Version: $Revision: 1.104 $
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

package org.opencms.db.generic;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryAlreadyExistsException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the user driver methods.<p>
 * 
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.104 $
 * 
 * @since 6.0.0 
 */
public class CmsUserDriver implements I_CmsDriver, I_CmsUserDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsUserDriver.class);

    /** The name of the admin user. */
    protected String m_adminUserName;

    /** A digest to encrypt the passwords. */
    protected MessageDigest m_digest;

    /** The algorithm used to encode passwords. */
    protected String m_digestAlgorithm;

    /** The file.encoding to code passwords after encryption with digest. */
    protected String m_digestFileEncoding;

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsUserDriver#createAccessControlEntry(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, int, int, int)
     */
    public void createAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal,
        int allowed,
        int denied,
        int flags) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_CREATE");

            stmt.setString(1, resource.toString());
            stmt.setString(2, principal.toString());
            stmt.setInt(3, allowed);
            stmt.setInt(4, denied);
            stmt.setInt(5, flags);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

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
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_GROUP_WITH_NAME_ALREADY_EXISTS_1,
                groupName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key());
            }
            throw new CmsDbEntryAlreadyExistsException(message);
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
            stmt.setString(4, m_sqlManager.validateEmpty(description));
            stmt.setInt(5, flags);
            stmt.executeUpdate();

            group = new CmsGroup(groupId, parentId, groupName, description, flags);
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
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
        Connection conn = null;
        PreparedStatement stmt = null;

        if (existsUser(dbc, name, type, null)) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_USER_WITH_NAME_ALREADY_EXISTS_1, name);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key());
            }
            throw new CmsDbEntryAlreadyExistsException(message);
        }

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, OpenCms.getPasswordHandler().digest(password));
            stmt.setString(4, m_sqlManager.validateEmpty(description));
            stmt.setString(5, m_sqlManager.validateEmpty(firstname));
            stmt.setString(6, m_sqlManager.validateEmpty(lastname));
            stmt.setString(7, m_sqlManager.validateEmpty(email));
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            m_sqlManager.setBytes(stmt, 10, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(11, m_sqlManager.validateEmpty(address));
            stmt.setInt(12, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_SERIALIZING_USER_DATA_1, name), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return this.readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#createUserInGroup(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.Object)
     */
    public void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid, Object reservedParam)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        // check if user is already in group
        if (!internalValidateUserInGroup(dbc, userid, groupid, reservedParam)) {
            // if not, add this user to the group
            try {
                if (reservedParam == null) {
                    // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                    conn = m_sqlManager.getConnection(dbc);
                } else {
                    // get a JDBC connection from the reserved JDBC pools
                    conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
                }

                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_ADDUSERTOGROUP");

                // write the new assingment to the database
                stmt.setString(1, groupid.toString());
                stmt.setString(2, userid.toString());
                // flag field is not used yet
                stmt.setInt(3, CmsDbUtil.UNKNOWN_ID);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(
                    Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteAccessControlEntries(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_SETFLAGS_ALL");

            stmt.setInt(1, CmsAccessControlEntry.ACCESS_FLAGS_DELETED);
            stmt.setString(2, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteGroup(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public void deleteGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_DELETEGROUP");

            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteUser(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public void deleteUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_DELETE");

            stmt.setString(1, userName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#deleteUserInGroup(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_REMOVEUSERFROMGROUP");

            stmt.setString(1, groupId.toString());
            stmt.setString(2, userId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#destroy()
     */
    public void destroy() throws Throwable {

        finalize();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#existsGroup(org.opencms.db.CmsDbContext, java.lang.String, java.lang.Object)
     */
    public boolean existsGroup(CmsDbContext dbc, String groupName, Object reservedParam) throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        boolean result = false;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_READGROUP");

            stmt.setString(1, groupName);
            res = stmt.executeQuery();

            // create new Cms group object
            if (res.next()) {
                result = true;
            } else {
                result = false;
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#existsUser(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.Object)
     */
    public boolean existsUser(CmsDbContext dbc, String username, int usertype, Object reservedParam)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        boolean result = false;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READ");
            stmt.setString(1, username);
            stmt.setInt(2, usertype);

            res = stmt.executeQuery();

            if (res.next()) {
                result = true;
            } else {
                result = false;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
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

            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_ADD");

            stmt.setString(1, id.toString());
            stmt.setString(2, name);
            stmt.setString(3, m_sqlManager.validateEmpty(password));
            stmt.setString(4, m_sqlManager.validateEmpty(description));
            stmt.setString(5, m_sqlManager.validateEmpty(firstname));
            stmt.setString(6, m_sqlManager.validateEmpty(lastname));
            stmt.setString(7, m_sqlManager.validateEmpty(email));
            stmt.setLong(8, lastlogin);
            stmt.setInt(9, flags);
            m_sqlManager.setBytes(stmt, 10, internalSerializeAdditionalUserInfo(additionalInfos));
            stmt.setString(11, m_sqlManager.validateEmpty(address));
            stmt.setInt(12, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_SERIALIZING_USER_DATA_1, name), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readUser(dbc, id);
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List successiveDrivers,
        CmsDriverManager driverManager) {

        Map configuration = configurationManager.getConfiguration();

        ExtendedProperties config;
        if (configuration instanceof ExtendedProperties) {
            config = (ExtendedProperties)configuration;
        } else {
            config = new ExtendedProperties();
            config.putAll(configuration);
        }

        String poolUrl = config.get("db.user.pool").toString();
        String classname = config.get("db.user.sqlmanager").toString();
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsUserDriver.DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        m_digestAlgorithm = config.getString(CmsDriverManager.CONFIGURATION_DB + ".user.digest.type", "MD5");
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DIGEST_ALGORITHM_1, m_digestAlgorithm));
        }

        m_digestFileEncoding = config.getString(
            CmsDriverManager.CONFIGURATION_DB + ".user.digest.encoding",
            CmsEncoder.ENCODING_UTF_8);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DIGEST_ENCODING_1, m_digestFileEncoding));
        }

        // create the digest
        try {
            m_digest = MessageDigest.getInstance(m_digestAlgorithm);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_DIGEST_ENC_3,
                    m_digest.getAlgorithm(),
                    m_digest.getProvider().getName(),
                    String.valueOf(m_digest.getProvider().getVersion())));
            }
        } catch (NoSuchAlgorithmException e) {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_SET_DIGEST_ERROR_0), e);
            }
        }

        m_adminUserName = OpenCms.getDefaultUsers().getUserAdmin();

        try {
            if (!existsGroup(dbc, OpenCms.getDefaultUsers().getGroupAdministrators(), null)) {
                fillDefaults(dbc);
            }
        } catch (CmsException e) {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().key(Messages.INIT_USER_GROUP_INITIALIZATION_FAILED_0), e);
            }
            throw new CmsInitException(Messages.get().container(Messages.ERR_INITIALIZING_USER_DRIVER_0), e);
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1, getClass().getName()));
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#publishAccessControlEntries(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void publishAccessControlEntries(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsUUID offlineId,
        CmsUUID onlineId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        // at first, we remove all access contries of this resource in the online project
        removeAccessControlEntries(dbc, onlineProject, onlineId);

        // then, we copy the access control entries from the offline project into the online project
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, offlineProject, "C_ACCESS_READ_ENTRIES");

            stmt.setString(1, offlineId.toString());

            res = stmt.executeQuery();

            while (res.next()) {
                CmsAccessControlEntry ace = internalCreateAce(res, onlineId);
                if ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_DELETED) == 0) {
                    writeAccessControlEntry(dbc, onlineProject, ace);
                }
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readAccessControlEntries(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, boolean)
     */
    public List readAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource, boolean inheritedOnly)
    throws CmsDataAccessException {

        List aceList = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_READ_ENTRIES");

            String resId = resource.toString();
            stmt.setString(1, resId);

            res = stmt.executeQuery();

            // create new CmsAccessControlEntry and add to list
            while (res.next()) {
                CmsAccessControlEntry ace = internalCreateAce(res);
                if ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_DELETED) > 0) {
                    continue;
                }

                if (inheritedOnly && ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_INHERIT) == 0)) {
                    continue;
                }

                if (inheritedOnly && ((ace.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_INHERIT) > 0)) {
                    ace.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
                }

                aceList.add(ace);
            }

            return aceList;

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readAccessControlEntry(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsAccessControlEntry readAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal) throws CmsDataAccessException {

        CmsAccessControlEntry ace = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
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
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_ACE_FOUND_2,
                    resource,
                    principal));
            }

            return ace;

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readChildGroups(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public List readChildGroups(CmsDbContext dbc, String groupname) throws CmsDataAccessException {

        List childs = new ArrayList();
        CmsGroup group;
        CmsGroup parent;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // get parent group
            parent = readGroup(dbc, groupname);
            // parent group exists, so get all childs
            if (parent != null) {
                // create statement
                conn = m_sqlManager.getConnection(dbc);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETCHILD");
                stmt.setString(1, parent.getId().toString());
                res = stmt.executeQuery();
                // create new Cms group objects
                while (res.next()) {
                    group = new CmsGroup(
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_PARENT_GROUP_ID"))),
                        res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_NAME")),
                        res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_DESCRIPTION")),
                        res.getInt(m_sqlManager.readQuery("C_GROUPS_GROUP_FLAGS")));
                    childs.add(group);
                }
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return childs;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroup(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException {

        CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_READGROUP2");

            // read the group from the database
            stmt.setString(1, groupId.toString());
            res = stmt.executeQuery();
            // create new Cms group object
            if (res.next()) {
                group = internalCreateGroup(res);
            } else {
                CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_GROUP_WITH_ID_1, groupId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroup(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsGroup readGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {

        CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_READGROUP");

            // read the group from the database
            stmt.setString(1, groupName);
            res = stmt.executeQuery();

            // create new Cms group object
            if (res.next()) {
                group = internalCreateGroup(res);
            } else {
                CmsMessageContainer message = org.opencms.db.Messages.get().container(
                    org.opencms.db.Messages.ERR_UNKNOWN_GROUP_1,
                    groupName);
                if (LOG.isWarnEnabled()) {
                    LOG.warn(message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return group;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroups(org.opencms.db.CmsDbContext)
     */
    public List readGroups(CmsDbContext dbc) throws CmsDataAccessException {

        List groups = new ArrayList();
        //CmsGroup group = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETGROUPS");

            res = stmt.executeQuery();

            // create new Cms group objects
            while (res.next()) {
                groups.add(internalCreateGroup(res));
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return groups;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readGroupsOfUser(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String)
     */
    public List readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String paramStr) throws CmsDataAccessException {

        //CmsGroup group;
        List groups = new ArrayList();

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETGROUPSOFUSER");

            //  get all all groups of the user
            stmt.setString(1, userId.toString());

            res = stmt.executeQuery();

            while (res.next()) {
                groups.add(internalCreateGroup(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return groups;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READID");

            stmt.setString(1, id.toString());
            res = stmt.executeQuery();

            // create new Cms user object
            if (res.next()) {
                user = internalCreateUser(res);
            } else {
                CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_USER_WITH_ID_1, id);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }

            return user;
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } catch (ClassNotFoundException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(org.opencms.db.CmsDbContext, java.lang.String, int)
     */
    public CmsUser readUser(CmsDbContext dbc, String name, int type) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READ");
            stmt.setString(1, name);
            stmt.setInt(2, type);

            res = stmt.executeQuery();

            if (res.next()) {
                user = internalCreateUser(res);
            } else {
                CmsMessageContainer message = org.opencms.db.Messages.get().container(
                    org.opencms.db.Messages.ERR_UNKNOWN_USER_1,
                    name);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } catch (ClassNotFoundException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return user;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(org.opencms.db.CmsDbContext, java.lang.String, java.lang.String, int)
     */
    public CmsUser readUser(CmsDbContext dbc, String name, String password, int type)
    throws CmsDataAccessException, CmsPasswordEncryptionException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUser user = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_READPW");
            stmt.setString(1, name);
            stmt.setString(2, OpenCms.getPasswordHandler().digest(password));
            stmt.setInt(3, type);
            res = stmt.executeQuery();

            // create new Cms user object
            if (res.next()) {
                user = internalCreateUser(res);
            } else {
                res.close();
                res = null;
                CmsMessageContainer message = org.opencms.db.Messages.get().container(
                    org.opencms.db.Messages.ERR_UNKNOWN_USER_1,
                    name);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }

            return user;
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } catch (ClassNotFoundException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_READING_USER_0), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUser(org.opencms.db.CmsDbContext, java.lang.String, java.lang.String, java.lang.String, int)
     */
    public CmsUser readUser(CmsDbContext dbc, String name, String password, String remoteAddress, int type)
    throws CmsDataAccessException, CmsPasswordEncryptionException {

        CmsUser user = readUser(dbc, name, password, type);
        return user;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsers(org.opencms.db.CmsDbContext, int)
     */
    public List readUsers(CmsDbContext dbc, int type) throws CmsDataAccessException {

        List users = new ArrayList();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_GETUSERS");
            stmt.setInt(1, type);
            res = stmt.executeQuery();
            // create new Cms user objects
            while (res.next()) {
                users.add(internalCreateUser(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_READING_USERS_0), e);
        } catch (ClassNotFoundException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_READING_USERS_0), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#readUsersOfGroup(org.opencms.db.CmsDbContext, java.lang.String, int)
     */
    public List readUsersOfGroup(CmsDbContext dbc, String name, int type) throws CmsDataAccessException {

        List users = new ArrayList();

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_GETUSERSOFGROUP");
            stmt.setString(1, name);
            stmt.setInt(2, type);

            res = stmt.executeQuery();

            while (res.next()) {
                users.add(internalCreateUser(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_USERS_OF_GROUP_1,
                name), e);
        } catch (ClassNotFoundException e) {
            throw new CmsDataAccessException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_USERS_OF_GROUP_1,
                name), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return users;
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#removeAccessControlEntries(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void removeAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE_ALL");

            stmt.setString(1, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#removeAccessControlEntriesForPrincipal(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void removeAccessControlEntriesForPrincipal(
        CmsDbContext dbc,
        CmsProject project,
        CmsProject onlineProject,
        CmsUUID principal) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {

            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE_ALL_FOR_PRINCIPAL");

            stmt.setString(1, principal.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE_ALL_FOR_PRINCIPAL_ONLINE");

            stmt.setString(1, principal.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, stmt), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#removeAccessControlEntry(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void removeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_REMOVE");

            stmt.setString(1, resource.toString());
            stmt.setString(2, principal.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#undeleteAccessControlEntries(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public void undeleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_RESETFLAGS_ALL");

            stmt.setInt(1, CmsAccessControlEntry.ACCESS_FLAGS_DELETED);
            stmt.setString(2, resource.toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeAccessControlEntry(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.security.CmsAccessControlEntry)
     */
    public void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_ACCESS_READ_ENTRY");

            stmt.setString(1, acEntry.getResource().toString());
            stmt.setString(2, acEntry.getPrincipal().toString());

            res = stmt.executeQuery();
            if (!res.next()) {
                createAccessControlEntry(
                    dbc,
                    project,
                    acEntry.getResource(),
                    acEntry.getPrincipal(),
                    acEntry.getAllowedPermissions(),
                    acEntry.getDeniedPermissions(),
                    acEntry.getFlags());
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
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
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

                stmt.setString(1, m_sqlManager.validateEmpty(group.getDescription()));
                stmt.setInt(2, group.getFlags());
                stmt.setString(3, group.getParentId().toString());
                stmt.setString(4, group.getId().toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(
                    Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }
        } else {
            throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_UNKNOWN_GROUP_1,
                group.getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writePassword(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.String, java.lang.String)
     */
    public void writePassword(CmsDbContext dbc, String userName, int type, String oldPassword, String newPassword)
    throws CmsDataAccessException, CmsPasswordEncryptionException {

        PreparedStatement stmt = null;
        Connection conn = null;

        // TODO: if old password is not null, check if it is valid
        // TODO: use type in user selection
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_SETPW");
            stmt.setString(1, OpenCms.getPasswordHandler().digest(newPassword));
            stmt.setString(2, userName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
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
            stmt.setString(1, m_sqlManager.validateEmpty(user.getDescription()));
            stmt.setString(2, m_sqlManager.validateEmpty(user.getFirstname()));
            stmt.setString(3, m_sqlManager.validateEmpty(user.getLastname()));
            stmt.setString(4, m_sqlManager.validateEmpty(user.getEmail()));
            stmt.setLong(5, user.getLastlogin());
            stmt.setInt(6, user.getFlags());
            m_sqlManager.setBytes(stmt, 7, internalSerializeAdditionalUserInfo(user.getAdditionalInfo()));
            stmt.setString(8, m_sqlManager.validateEmpty(user.getAddress()));
            stmt.setInt(9, user.getType());
            stmt.setString(10, user.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(Messages.ERR_SERIALIZING_USER_DATA_1, user.getName()),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#writeUserType(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, int)
     */
    public void writeUserType(CmsDbContext dbc, CmsUUID userId, int userType) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_USERS_UPDATE_USERTYPE");

            // write data to database
            stmt.setInt(1, userType);
            stmt.setString(2, userId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
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
     * Semi-constructor to create a CmsGroup instance from a JDBC result set.
     * @param res the JDBC ResultSet
     * 
     * @return CmsGroup the new CmsGroup object
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    protected CmsGroup internalCreateGroup(ResultSet res) throws SQLException {

        return new CmsGroup(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_ID"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_GROUPS_PARENT_GROUP_ID"))),
            res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_NAME")),
            res.getString(m_sqlManager.readQuery("C_GROUPS_GROUP_DESCRIPTION")),
            res.getInt(m_sqlManager.readQuery("C_GROUPS_GROUP_FLAGS")));
    }

    /**
     * Semi-constructor to create a CmsUser instance from a JDBC result set.
     * @param res the JDBC ResultSet
     * 
     * @return CmsUser the new CmsUser object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws IOException if there is an error in deserializing the user info
     * @throws ClassNotFoundException if there is an error in deserializing the user info
     */
    protected CmsUser internalCreateUser(ResultSet res) throws SQLException, IOException, ClassNotFoundException {

        String userName = res.getString(m_sqlManager.readQuery("C_USERS_USER_NAME"));

        // deserialize the additional userinfo hash
        ByteArrayInputStream bin = new ByteArrayInputStream(m_sqlManager.getBytes(
            res,
            m_sqlManager.readQuery("C_USERS_USER_INFO")));
        ObjectInputStream oin = new ObjectInputStream(bin);

        Map info;
        if (m_adminUserName.equals(userName)) {
            // ensure the admin user can be read even if it's additional infos are defect
            try {
                info = (Map)oin.readObject();
            } catch (IOException e) {
                info = new HashMap();
            }
        } else {
            // no exception handling for normal users
            info = (Map)oin.readObject();
        }

        return new CmsUser(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_USERS_USER_ID"))),
            userName,
            res.getString(m_sqlManager.readQuery("C_USERS_USER_PASSWORD")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_DESCRIPTION")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_FIRSTNAME")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_LASTNAME")),
            res.getString(m_sqlManager.readQuery("C_USERS_USER_EMAIL")),
            res.getLong(m_sqlManager.readQuery("C_USERS_USER_LASTLOGIN")),
            res.getInt(m_sqlManager.readQuery("C_USERS_USER_FLAGS")),
            info,
            res.getString(m_sqlManager.readQuery("C_USERS_USER_ADDRESS")),
            res.getInt(m_sqlManager.readQuery("C_USERS_USER_TYPE")));
    }

    /**
     * Serialize additional user information to write it as byte array in the database.<p>
     * 
     * @param additionalUserInfo the HashTable with additional information
     * @return byte[] the byte array which is written to the db
     * @throws IOException if something goes wrong
     */
    protected byte[] internalSerializeAdditionalUserInfo(Map additionalUserInfo) throws IOException {

        // serialize the hashtable
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(additionalUserInfo != null ? new Hashtable(additionalUserInfo) : null);
        oout.close();

        return bout.toByteArray();
    }

    /**
     * Initializes the default users and groups.<p>
     * 
     * @param dbc the current database context
     * 
     * @throws CmsDataAccessException if something goes wrong
     * @throws CmsPasswordEncryptionException if a password of a default user could not be encrypted
     */
    private void fillDefaults(CmsDbContext dbc) throws CmsDataAccessException, CmsPasswordEncryptionException {

        String guestGroup = OpenCms.getDefaultUsers().getGroupGuests();
        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        String projectmanagersGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        String guestUser = OpenCms.getDefaultUsers().getUserGuest();
        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        String exportUser = OpenCms.getDefaultUsers().getUserExport();

        CmsGroup guests, administrators, users, projectmanager;
        CmsUser guest, admin, export;

        guests = createGroup(
            dbc,
            CmsUUID.getConstantUUID(guestGroup),
            guestGroup,
            "The guest group",
            I_CmsPrincipal.FLAG_ENABLED,
            null,
            null);
        administrators = createGroup(
            dbc,
            CmsUUID.getConstantUUID(administratorsGroup),
            administratorsGroup,
            "The administrators group",
            I_CmsPrincipal.FLAG_ENABLED | I_CmsPrincipal.FLAG_GROUP_PROJECT_MANAGER,
            null,
            null);
        users = createGroup(
            dbc,
            CmsUUID.getConstantUUID(usersGroup),
            usersGroup,
            "The users group",
            I_CmsPrincipal.FLAG_ENABLED
                | I_CmsPrincipal.FLAG_GROUP_WORKFLOW_ROLE
                | I_CmsPrincipal.FLAG_GROUP_PROJECT_USER,
            null,
            null);
        projectmanager = createGroup(
            dbc,
            CmsUUID.getConstantUUID(projectmanagersGroup),
            projectmanagersGroup,
            "The projectmanager group",
            I_CmsPrincipal.FLAG_ENABLED
                | I_CmsPrincipal.FLAG_GROUP_PROJECT_MANAGER
                | I_CmsPrincipal.FLAG_GROUP_PROJECT_USER
                | I_CmsPrincipal.FLAG_GROUP_WORKFLOW_ROLE,
            users.getName(),
            null);

        guest = importUser(
            dbc,
            CmsUUID.getConstantUUID(guestUser),
            guestUser,
            OpenCms.getPasswordHandler().digest(""),
            "The guest user",
            " ",
            " ",
            " ",
            0,
            I_CmsPrincipal.FLAG_ENABLED,
            new Hashtable(),
            " ",
            CmsUser.USER_TYPE_SYSTEMUSER,
            null);
        admin = importUser(
            dbc,
            CmsUUID.getConstantUUID(adminUser),
            adminUser,
            OpenCms.getPasswordHandler().digest("admin"),
            "The admin user",
            " ",
            " ",
            " ",
            0,
            I_CmsPrincipal.FLAG_ENABLED,
            new Hashtable(),
            " ",
            CmsUser.USER_TYPE_SYSTEMUSER,
            null);

        createUserInGroup(dbc, guest.getId(), guests.getId(), null);
        createUserInGroup(dbc, admin.getId(), administrators.getId(), null);

        if (!exportUser.equals(OpenCms.getDefaultUsers().getUserAdmin())
            && !exportUser.equals(OpenCms.getDefaultUsers().getUserGuest())) {

            export = importUser(
                dbc,
                CmsUUID.getConstantUUID(exportUser),
                exportUser,
                OpenCms.getPasswordHandler().digest((new CmsUUID()).toString()),
                "The static export user",
                " ",
                " ",
                " ",
                0,
                I_CmsPrincipal.FLAG_ENABLED,
                Collections.EMPTY_MAP,
                " ",
                CmsUser.USER_TYPE_SYSTEMUSER,
                null);
            createUserInGroup(dbc, export.getId(), guests.getId(), null);
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_USER_GROUP_DEFAULTS_INITIALIZED_0));
        }

        // avoid warning
        projectmanager.setEnabled();
    }

    /**
     * Internal helper method to create an access control entry from a database record.<p>
     * 
     * @param res resultset of the current query
     * @return a new CmsAccessControlEntry initialized with the values from the current database record
     * @throws SQLException if something goes wrong
     */
    private CmsAccessControlEntry internalCreateAce(ResultSet res) throws SQLException {

        return new CmsAccessControlEntry(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_RESOURCE_ID"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_PRINCIPAL_ID"))),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_ALLOWED")),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_DENIED")),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_FLAGS")));
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

        return new CmsAccessControlEntry(
            newId,
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_ACCESS_PRINCIPAL_ID"))),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_ALLOWED")),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_DENIED")),
            res.getInt(m_sqlManager.readQuery("C_ACCESS_ACCESS_FLAGS")));
    }

    /**
     * Checks if a user is member of a group.<P/>
     * 
     * @param runtimeInfo the current runtime info
     * @param userId the id of the user to check
     * @param groupId the id of the group to check
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     *
     * @return true if user is member of group
     * @throws CmsException if operation was not succesful
     */
    private boolean internalValidateUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId, Object reservedParam)
    throws CmsDataAccessException {

        boolean userInGroup = false;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_GROUPS_USERINGROUP");

            stmt.setString(1, groupId.toString());
            stmt.setString(2, userId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                userInGroup = true;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return userInGroup;
    }

}