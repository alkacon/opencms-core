/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsHistoryDriver.java,v $
 * Date   : $Date: 2007/05/31 12:43:43 $
 * Version: $Revision: 1.1.2.14 $
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
import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.CmsHistoryFolder;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the hitory driver methods.<p>
 * 
 * @author Thomas Weckert 
 * @author Michael Emmerich 
 * @author Carsten Weinholz  
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.14 $
 * 
 * @since 6.9.1
 */
public class CmsHistoryDriver implements I_CmsDriver, I_CmsHistoryDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsHistoryDriver.class);

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsPropertyDefinition.CmsPropertyType)
     */
    public CmsPropertyDefinition createPropertyDefinition(
        CmsDbContext dbc,
        String name,
        CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_CREATE_HISTORY");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.setInt(3, type.getMode());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);

        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readPropertyDefinition(dbc, name);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#deleteEntries(CmsDbContext, I_CmsHistoryResource, int, long)
     */
    public int deleteEntries(CmsDbContext dbc, I_CmsHistoryResource resource, int versionsToKeep, long time)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            int maxVersion = -1;
            // get the maximal version number for this resource
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_MAXVER");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                maxVersion = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (time >= 0) {
                int maxVersionByTime = -1;
                // get the maximal version to keep for this resource based on the time parameter
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_MAXVER_BYTIME");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setLong(2, time);
                res = stmt.executeQuery();
                if (res.next()) {
                    maxVersionByTime = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
                if (maxVersionByTime > 0) {
                    if (versionsToKeep < 0) {
                        versionsToKeep = (maxVersion - maxVersionByTime);
                    } else {
                        versionsToKeep = Math.min(versionsToKeep, (maxVersion - maxVersionByTime));
                    }
                }
            }

            if (maxVersion - versionsToKeep <= 0) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }

            // get the minimal structure publish tag to keep for this sibling
            int minStrPublishTagToKeep = -1;
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_MAXTAG_FOR_VERSION");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setInt(2, 1 + maxVersion - versionsToKeep);
            res = stmt.executeQuery();
            if (res.next()) {
                minStrPublishTagToKeep = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);
            if (minStrPublishTagToKeep < 1) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }
            minStrPublishTagToKeep++;

            // delete the properties
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_DELETE");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setInt(2, minStrPublishTagToKeep);
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // delete the structure entries
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_DELETE");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setInt(2, minStrPublishTagToKeep);
            int structureVersions = stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // get the minimal resource publish tag to keep, 
            // all entries with publish tag less than this will be deleted
            int minResPublishTagToKeep = -1;
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_MIN_USED_TAG");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                minResPublishTagToKeep = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                // nothing to delete
                internalCleanup(dbc, resource);
                return structureVersions;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            // delete the resource entries
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_DELETE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setInt(2, minResPublishTagToKeep);
            int resourceVersions = stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // delete the content entries
            stmt = m_sqlManager.getPreparedStatement(conn, "C_CONTENT_HISTORY_DELETE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setInt(2, minResPublishTagToKeep);
            stmt.executeUpdate();

            internalCleanup(dbc, resource);
            return Math.max(structureVersions, resourceVersions);
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if ((internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0)
                || (internalCountProperties(dbc, metadef, CmsUUID.getOpenCmsUUID()) != 0)) { // HACK: to get an offline project

                throw new CmsDbConsistencyException(Messages.get().container(
                    Messages.ERR_ERROR_DELETING_PROPERTYDEF_1,
                    metadef.getName()));
            }

            // delete the historical property definition
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_HISTORY");
            stmt.setString(1, metadef.getId().toString());
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
     * @see org.opencms.db.I_CmsHistoryDriver#destroy()
     */
    public void destroy() throws Throwable {

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getAllDeletedEntries(CmsDbContext, CmsUUID)
     */
    public List getAllDeletedEntries(CmsDbContext dbc, CmsUUID parentId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        List entries = new ArrayList();
        try {
            conn = m_sqlManager.getConnection(dbc);

            // get all not-deleted historical entries that may come in question
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_DELETED");
            res = stmt.executeQuery();
            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString(1));
                int version = res.getInt(2);
                entries.add(readResource(dbc, structureId, version));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return internalFilterParentId(parentId, entries);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getAllNotDeletedEntries(CmsDbContext, CmsUUID)
     */
    public List getAllNotDeletedEntries(CmsDbContext dbc, CmsUUID parentId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        List entries = new ArrayList();
        try {
            conn = m_sqlManager.getConnection(dbc);

            // get all not-deleted historical entries that may come in question
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_NOTDELETED");
            res = stmt.executeQuery();
            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString(1));
                int version = res.getInt(2);
                entries.add(readResource(dbc, structureId, version));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return internalFilterParentId(parentId, entries);

    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
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

        String poolUrl;
        if (configuration.get("db.history.pool") != null) {
            poolUrl = configuration.get("db.history.pool").toString();
        } else {
            int todoDeprecated; // deprecated, remove as soon as possible
            poolUrl = configuration.get("db.backup.pool").toString();
        }

        String classname;
        if (configuration.get("db.history.sqlmanager") != null) {
            classname = configuration.get("db.history.sqlmanager").toString();
        } else {
            int todoDeprecated; // deprecated, remove as soon as possible
            classname = configuration.get("db.backup.sqlmanager").toString();
        }

        m_sqlManager = initSqlManager(classname);
        m_sqlManager.init(I_CmsHistoryDriver.DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if ((successiveDrivers != null) && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1,
                    getClass().getName()));
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readAllAvailableVersions(CmsDbContext, CmsUUID)
     */
    public List readAllAvailableVersions(CmsDbContext dbc, CmsUUID structureId) throws CmsDataAccessException {

        ResultSet res = null;
        List result = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            List historyResources = new ArrayList();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_ALL_VERSIONS");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                historyResources.add(internalCreateResource(res));
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (!historyResources.isEmpty()) {
                // look for newer versions
                I_CmsHistoryResource histRes = (I_CmsHistoryResource)historyResources.get(0);

                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_NEW_VERSIONS");
                stmt.setString(1, histRes.getResourceId().toString());
                stmt.setInt(2, histRes.getPublishTag());
                res = stmt.executeQuery();

                I_CmsHistoryResource lastHistRes = histRes;
                while (res.next()) {
                    int pubTag = res.getInt(1);
                    I_CmsHistoryResource newHistRes = internalReadMergedResource(dbc, histRes, pubTag);
                    if (newHistRes.getVersion() == lastHistRes.getVersion()) {
                        lastHistRes = newHistRes;
                        continue;
                    }
                    lastHistRes = newHistRes;
                    result.add(0, lastHistRes); // put them in the right order
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }
            for (int i = 0; i < historyResources.size(); i++) {
                I_CmsHistoryResource histRes = (I_CmsHistoryResource)historyResources.get(i);
                result.add(histRes);
                if (i < historyResources.size() - 1) {
                    // look in between
                    I_CmsHistoryResource histRes2 = (I_CmsHistoryResource)historyResources.get(i + 1);

                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_BTW_VERSIONS");
                    stmt.setString(1, histRes.getResourceId().toString());
                    stmt.setInt(2, histRes.getPublishTag());
                    stmt.setInt(3, histRes2.getPublishTag());
                    res = stmt.executeQuery();

                    I_CmsHistoryResource lastHistRes = histRes;
                    while (res.next()) {
                        int pubTag = res.getInt(1);
                        I_CmsHistoryResource newHistRes = internalReadMergedResource(dbc, histRes, pubTag);
                        if (newHistRes.getVersion() == lastHistRes.getVersion()) {
                            lastHistRes = newHistRes;
                            continue;
                        }
                        lastHistRes = newHistRes;
                        result.add(lastHistRes);
                    }
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                }
            }
            if (!result.isEmpty()) {
                // look for older versions
                I_CmsHistoryResource histRes = (I_CmsHistoryResource)result.get(result.size() - 1);

                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_OLD_VERSIONS");
                stmt.setString(1, histRes.getResourceId().toString());
                stmt.setInt(2, histRes.getPublishTag());
                res = stmt.executeQuery();

                I_CmsHistoryResource lastHistRes = histRes;
                while (res.next()) {
                    int pubTag = res.getInt(1);
                    I_CmsHistoryResource newHistRes = internalReadMergedResource(dbc, histRes, pubTag);
                    if (newHistRes.getVersion() == lastHistRes.getVersion()) {
                        lastHistRes = newHistRes;
                        continue;
                    }
                    lastHistRes = newHistRes;
                    result.add(lastHistRes);
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
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
     * @see org.opencms.db.I_CmsHistoryDriver#readContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, int)
     */
    public byte[] readContent(CmsDbContext dbc, CmsUUID resourceId, int publishTag) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        byte[] content = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_CONTENT");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, publishTag);
            stmt.setInt(3, publishTag);
            res = stmt.executeQuery();

            if (res.next()) {
                content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return content;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readDeletedResources(CmsDbContext, CmsUUID, CmsUUID)
     */
    public List readDeletedResources(CmsDbContext dbc, CmsUUID structureId, CmsUUID userId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List result = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            if (userId == null) {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_DELETED");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_DELETED_RESTRICTED");
            }
            stmt.setString(1, structureId.toString());
            if (userId != null) {
                stmt.setString(2, userId.toString());
            }
            res = stmt.executeQuery();
            while (res.next()) {
                I_CmsHistoryResource histRes = internalCreateResource(res);
                if (m_driverManager.getVfsDriver().validateStructureIdExists(
                    dbc,
                    dbc.currentProject().getUuid(),
                    histRes.getStructureId())) {
                    // only add resources that are really deleted
                    continue;
                }
                result.add(histRes);
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
     * @see org.opencms.db.I_CmsHistoryDriver#readFile(CmsDbContext, CmsUUID, int)
     * @deprecated
     */
    public I_CmsHistoryResource readFile(CmsDbContext dbc, CmsUUID structureId, int tagId)
    throws CmsDataAccessException {

        I_CmsHistoryResource file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_READ_HISTORY_BYID");
            stmt.setString(1, structureId.toString());
            stmt.setInt(2, tagId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = internalCreateResource(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_HISTORY_FILE_NOT_FOUND_1,
                    structureId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        ((CmsFile)file).setContents(readContent(dbc, file.getResourceId(), file.getPublishTag()));
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readLastVersion(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public int readLastVersion(CmsDbContext dbc, CmsUUID structureId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int lastVersion = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_MAXVER");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                lastVersion = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                lastVersion = 0;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return lastVersion;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readMaxPublishTag(CmsDbContext, CmsUUID)
     */
    public int readMaxPublishTag(CmsDbContext dbc, CmsUUID resourceId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int result = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_MAX_PUBLISH_TAG");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                result = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
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
     * @see org.opencms.db.I_CmsHistoryDriver#readNextPublishTag(org.opencms.db.CmsDbContext)
     */
    public int readNextPublishTag(CmsDbContext dbc) {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int projectPublishTag = 1;
        int resourcePublishTag = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_MAXTAG");
            res = stmt.executeQuery();

            if (res.next()) {
                projectPublishTag = res.getInt(1) + 1;
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }

            m_sqlManager.closeAll(dbc, null, stmt, res);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_MAXTAG");
            res = stmt.executeQuery();

            if (res.next()) {
                resourcePublishTag = res.getInt(1) + 1;
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }

            if (resourcePublishTag > projectPublishTag) {
                projectPublishTag = resourcePublishTag;
            }
        } catch (SQLException exc) {
            return 1;
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return projectPublishTag;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readPrincipal(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsHistoryPrincipal readPrincipal(CmsDbContext dbc, CmsUUID principalId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsHistoryPrincipal historyPrincipal = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_PRINCIPAL_READ");
            stmt.setString(1, principalId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                String userName = res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_NAME"));
                String ou = CmsOrganizationalUnit.removeLeadingSeparator(res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_OU")));
                historyPrincipal = new CmsHistoryPrincipal(
                    principalId,
                    ou + userName,
                    res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_DESCRIPTION")),
                    res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_EMAIL")),
                    res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_TYPE")),
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_USERDELETED"))),
                    res.getLong(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_DATEDELETED")));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_HISTORY_PRINCIPAL_NOT_FOUND_1,
                    principalId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return historyPrincipal;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProject(org.opencms.db.CmsDbContext, int)
     */
    public CmsHistoryProject readProject(CmsDbContext dbc, int publishTag) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsHistoryProject project = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_READ");

            stmt.setInt(1, publishTag);
            res = stmt.executeQuery();

            if (res.next()) {
                List projectresources = readProjectResources(dbc, publishTag);
                project = internalCreateProject(res, projectresources);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_HISTORY_PROJECT_WITH_TAG_ID_1,
                    new Integer(publishTag)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjectResources(org.opencms.db.CmsDbContext, int)
     */
    public List readProjectResources(CmsDbContext dbc, int publishTag) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List projectResources = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_HISTORY_READ");
            stmt.setInt(1, publishTag);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return projectResources;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjects(org.opencms.db.CmsDbContext)
     */
    public List readProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_READ_ALL");
            res = stmt.executeQuery();

            // this is not really efficient
            // but it is overriden in all db specific implementations, including mysql
            int i = 0;
            int max = 300;

            while (res.next() && (i < max)) {
                List resources = readProjectResources(dbc, res.getInt("PUBLISH_TAG"));
                projects.add(internalCreateProject(res, resources));
                i++;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /** 
     * @see org.opencms.db.I_CmsHistoryDriver#readProperties(org.opencms.db.CmsDbContext, org.opencms.file.history.I_CmsHistoryResource)
     */
    public List readProperties(CmsDbContext dbc, I_CmsHistoryResource resource) throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        Map propertyMap = new HashMap();

        try {
            conn = m_sqlManager.getConnection(dbc);

            // get the latest properties for this sibling
            int pubTag = -1;
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_READ_PUBTAG");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setInt(2, resource.getPublishTag());
            res = stmt.executeQuery();
            if (res.next()) {
                pubTag = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (pubTag > 0) {
                // add the siblings props
                stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_READALL_STR");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, pubTag);
                res = stmt.executeQuery();

                while (res.next()) {
                    String propertyKey = res.getString(1);
                    String propertyValue = res.getString(2);
                    int mappingType = res.getInt(3);

                    internalAddToPropMap(propertyMap, resource, propertyKey, propertyValue, mappingType);
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }

            if (pubTag != resource.getPublishTag()) {
                // check if there were newer shared properties modifications
                stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_READALL_RES");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, resource.getPublishTag());
                res = stmt.executeQuery();

                while (res.next()) {
                    String propertyKey = res.getString(1);
                    String propertyValue = res.getString(2);
                    int mappingType = res.getInt(3);

                    internalAddToPropMap(propertyMap, resource, propertyKey, propertyValue, mappingType);
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException {

        CmsPropertyDefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_READ_HISTORY");
            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))),
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_TYPE"))));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    name));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readPublishTag(org.opencms.db.CmsDbContext, long)
     */
    public int readPublishTag(CmsDbContext dbc, long maxdate) throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int maxVersion = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_READ_TAG_FOR_DATE");
            stmt.setLong(1, maxdate);
            res = stmt.executeQuery();
            if (res.next()) {
                maxVersion = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return maxVersion;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readResource(CmsDbContext, CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsDbContext dbc, CmsUUID structureId, int version)
    throws CmsDataAccessException {

        I_CmsHistoryResource resource = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_VERSION");
            stmt.setString(1, structureId.toString());
            stmt.setInt(2, version);
            res = stmt.executeQuery();
            if (res.next()) {
                resource = internalCreateResource(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_HISTORY_FILE_NOT_FOUND_1,
                    structureId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writePrincipal(CmsDbContext, org.opencms.security.I_CmsPrincipal)
     */
    public void writePrincipal(CmsDbContext dbc, I_CmsPrincipal principal) throws CmsDbSqlException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_PRINCIPAL_CREATE");
            stmt.setString(1, principal.getId().toString());
            stmt.setString(2, principal.getSimpleName());
            String desc = principal.getDescription();
            desc = CmsStringUtil.isEmptyOrWhitespaceOnly(desc) ? "-" : desc;
            stmt.setString(3, desc);
            stmt.setString(4, CmsOrganizationalUnit.SEPARATOR + principal.getOuFqn());
            if (principal instanceof CmsUser) {
                String email = ((CmsUser)principal).getEmail();
                email = CmsStringUtil.isEmptyOrWhitespaceOnly(email) ? "-" : email;
                stmt.setString(5, email);
                stmt.setString(6, I_CmsPrincipal.PRINCIPAL_USER);
            } else {
                stmt.setString(5, "-");
                stmt.setString(6, I_CmsPrincipal.PRINCIPAL_GROUP);
            }
            stmt.setString(7, dbc.currentUser().getId().toString());
            stmt.setLong(8, System.currentTimeMillis());

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
     * @see org.opencms.db.I_CmsHistoryDriver#writeProject(org.opencms.db.CmsDbContext, int, long)
     */
    public void writeProject(CmsDbContext dbc, int publishTag, long publishDate) throws CmsDataAccessException {

        CmsProject currentProject = dbc.currentProject();
        CmsUser currentUser = dbc.currentUser();

        List projectresources = m_driverManager.getProjectDriver().readProjectResources(dbc, currentProject);

        // write historical project to the database
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_CREATE");
            // first write the project
            stmt.setInt(1, publishTag);
            stmt.setString(2, currentProject.getUuid().toString());
            stmt.setString(3, currentProject.getSimpleName());
            stmt.setLong(4, publishDate);
            stmt.setString(5, currentUser.getId().toString());
            stmt.setString(6, currentProject.getOwnerId().toString());
            stmt.setString(7, currentProject.getGroupId().toString());
            stmt.setString(8, currentProject.getManagerGroupId().toString());
            stmt.setString(9, currentProject.getDescription());
            stmt.setLong(10, currentProject.getDateCreated());
            stmt.setInt(11, currentProject.getType().getMode());
            stmt.setString(12, CmsOrganizationalUnit.SEPARATOR + currentProject.getOuFqn());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // now write the projectresources
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_HISTORY_CREATE");
            Iterator i = projectresources.iterator();
            while (i.hasNext()) {
                stmt.setInt(1, publishTag);
                stmt.setString(2, currentProject.getUuid().toString());
                stmt.setString(3, (String)i.next());
                stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeProperties(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeProperties(CmsDbContext dbc, CmsResource resource, List properties, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Iterator dummy = properties.iterator();
            while (dummy.hasNext()) {
                CmsProperty property = (CmsProperty)dummy.next();
                CmsPropertyDefinition propDef = readPropertyDefinition(dbc, property.getName());

                for (int i = 0; i < 2; i++) {
                    int mappingType;
                    String value;
                    CmsUUID id;
                    if (i == 0) {
                        // write the structure value on the first cycle
                        value = property.getStructureValue();
                        mappingType = CmsProperty.STRUCTURE_RECORD_MAPPING;
                        id = resource.getStructureId();
                        if (CmsStringUtil.isEmpty(value)) {
                            continue;
                        }
                    } else {
                        // write the resource value on the second cycle
                        value = property.getResourceValue();
                        mappingType = CmsProperty.RESOURCE_RECORD_MAPPING;
                        id = resource.getResourceId();
                        if (CmsStringUtil.isEmpty(value)) {
                            break;
                        }
                    }

                    conn = m_sqlManager.getConnection(dbc);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_CREATE");

                    stmt.setString(1, resource.getStructureId().toString());
                    stmt.setString(2, propDef.getId().toString());
                    stmt.setString(3, id.toString());
                    stmt.setInt(4, mappingType);
                    stmt.setString(5, m_sqlManager.validateEmpty(value));
                    stmt.setInt(6, publishTag);

                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, conn, stmt, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeResource(CmsDbContext dbc, CmsResource resource, List properties, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        int resourceVersion = -1;
        int structureVersion = -1;
        try {
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_RESOURCES_READ_VERSIONS");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                resourceVersion = res.getInt(1);
                structureVersion = res.getInt(2);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (!internalValidateResource(dbc, resource, publishTag)) {
                // write the resource
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_WRITE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, resource.getTypeId());
                stmt.setInt(3, resource.getFlags());
                stmt.setLong(4, resource.getDateCreated());
                stmt.setString(5, resource.getUserCreated().toString());
                stmt.setLong(6, resource.getDateLastModified());
                stmt.setString(7, resource.getUserLastModified().toString());
                stmt.setInt(8, resource.getState().getState());
                stmt.setInt(9, resource.getLength());
                stmt.setLong(10, resource.getDateContent());
                stmt.setString(11, dbc.currentProject().getUuid().toString());
                stmt.setInt(12, resource.getSiblingCount());
                stmt.setInt(13, resourceVersion);
                stmt.setInt(14, publishTag);
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // if it is a file
                if (resource instanceof CmsFile) {
                    if (resource.getState().isDeleted()) {
                        // if deleted, first copy content from offline to online content tables
                        m_driverManager.getVfsDriver().createOnlineContent(
                            dbc,
                            resource.getResourceId(),
                            ((CmsFile)resource).getContents(),
                            publishTag,
                            false,
                            true);
                    }
                }
            }

            CmsUUID parentId = CmsUUID.getNullUUID();
            CmsFolder parent = m_driverManager.getVfsDriver().readParentFolder(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getStructureId());
            if (parent != null) {
                parentId = parent.getStructureId();
            }
            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_WRITE");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, resource.getState().getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setInt(7, structureVersion);
            stmt.setString(8, parentId.toString());
            stmt.setInt(9, publishTag);
            stmt.setInt(10, resource.getVersion());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        writeProperties(dbc, resource, properties, publishTag);
    }

    /**
     * Updates the property map for the given resource with the given property data.<p>
     * 
     * @param propertyMap the map to update
     * @param resource the resource the properties belong to
     * @param propertyKey the property key
     * @param propertyValue the property value
     * @param mappingType the mapping type
     * 
     * @throws CmsDbConsistencyException if the mapping type is wrong
     */
    protected void internalAddToPropMap(
        Map propertyMap,
        I_CmsHistoryResource resource,
        String propertyKey,
        String propertyValue,
        int mappingType) throws CmsDbConsistencyException {

        CmsProperty property = (CmsProperty)propertyMap.get(propertyKey);
        if (property != null) {
            // there exists already a property for this key in the result
            switch (mappingType) {
                case CmsProperty.STRUCTURE_RECORD_MAPPING:
                    // this property value is mapped to a structure record
                    property.setStructureValue(propertyValue);
                    break;
                case CmsProperty.RESOURCE_RECORD_MAPPING:
                    // this property value is mapped to a resource record
                    property.setResourceValue(propertyValue);
                    break;
                default:
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        propertyKey));
            }
        } else {
            // there doesn't exist a property for this key yet
            property = new CmsProperty();
            property.setName(propertyKey);

            switch (mappingType) {
                case CmsProperty.STRUCTURE_RECORD_MAPPING:
                    // this property value is mapped to a structure record
                    property.setStructureValue(propertyValue);
                    property.setResourceValue(null);
                    break;
                case CmsProperty.RESOURCE_RECORD_MAPPING:
                    // this property value is mapped to a resource record
                    property.setStructureValue(null);
                    property.setResourceValue(propertyValue);
                    break;
                default:
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        propertyKey));
            }
            propertyMap.put(propertyKey, property);
        }
    }

    /**
     * Deletes all historical entries of subresources of a folder without any historical netry left.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to check
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalCleanup(CmsDbContext dbc, I_CmsHistoryResource resource) throws CmsDataAccessException {

        boolean isFolder = resource.getRootPath().endsWith("/");
        List subResources = new ArrayList();

        // if the resource is a folder
        if (isFolder) {
            // and if no versions left
            if (readLastVersion(dbc, resource.getStructureId()) == 0) {
                // get all direct subresources                    
                Connection conn = null;
                PreparedStatement stmt = null;
                ResultSet res = null;

                try {
                    conn = m_sqlManager.getConnection(dbc);

                    stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_SUBRESOURCES");
                    stmt.setString(1, resource.getStructureId().toString());
                    res = stmt.executeQuery();
                    while (res.next()) {
                        CmsUUID structureId = new CmsUUID(res.getString(1));
                        int version = res.getInt(2);
                        subResources.add(readResource(dbc, structureId, version));
                    }
                } catch (SQLException e) {
                    throw new CmsDbSqlException(Messages.get().container(
                        Messages.ERR_GENERIC_SQL_1,
                        CmsDbSqlException.getErrorQuery(stmt)), e);
                } finally {
                    m_sqlManager.closeAll(dbc, conn, stmt, res);
                }
            }
        }

        // delete all subresource versions
        Iterator it = subResources.iterator();
        while (it.hasNext()) {
            I_CmsHistoryResource histResource = (I_CmsHistoryResource)it.next();
            deleteEntries(dbc, histResource, 0, -1);
        }
    }

    /**
     * Returns the amount of properties for a propertydefinition.<p>
     * 
     * @param dbc the current database context
     * @param metadef the propertydefinition to test
     * @param projectId the ID of the current project
     * 
     * @return the amount of properties for a propertydefinition
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertyDefinition metadef, CmsUUID projectId)
    throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        int returnValue;
        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL_COUNT");
            stmt.setString(1, metadef.getId().toString());
            res = stmt.executeQuery();

            if (res.next()) {
                returnValue = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbConsistencyException(Messages.get().container(
                    Messages.ERR_NO_PROPERTIES_FOR_PROPERTYDEF_1,
                    metadef.getName()));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Creates a historical project from the given result set and resources.<p>
     * @param res the resource set
     * @param resources the hsitorical resources
     * 
     * @return the historical project
     *  
     * @throws SQLException if something goes wrong
     */
    protected CmsHistoryProject internalCreateProject(ResultSet res, List resources) throws SQLException {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_OU_0")));
        CmsUUID publishedById = new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECT_PUBLISHED_BY_0")));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID_0")));
        return new CmsHistoryProject(
            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PUBLISH_TAG_0")),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID_0"))),
            ou + res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME_0")),
            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION_0")),
            userId,
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID_0"))),
            res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED_0")),
            CmsProject.CmsProjectType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE_0"))),
            res.getLong(m_sqlManager.readQuery("C_PROJECT_PUBLISHDATE_0")),
            publishedById,
            resources);
    }

    /**
     * Creates a valid {@link I_CmsHistoryResource} instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC result set
     * 
     * @return the new historical resource instance
     * 
     * @throws SQLException if a requested attribute was not found in the result set
     */
    protected I_CmsHistoryResource internalCreateResource(ResultSet res) throws SQLException {

        int version = res.getInt(m_sqlManager.readQuery("C_RESOURCES_HISTORY_VERSION"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
        int tagId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PUBLISH_TAG"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID projectLastModified = new CmsUUID(
            res.getString(m_sqlManager.readQuery("C_RESOURCES_PROJECT_LASTMODIFIED")));
        int state = Math.max(
            res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE")),
            res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE")));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_HISTORY_PARENTID")));
        long dateContent = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));

        boolean isFolder = resourcePath.endsWith("/");
        if (isFolder) {
            return new CmsHistoryFolder(
                tagId,
                structureId,
                resourceId,
                resourcePath,
                resourceType,
                resourceFlags,
                projectLastModified,
                CmsResourceState.valueOf(state),
                dateCreated,
                userCreated,
                dateLastModified,
                userLastModified,
                dateReleased,
                dateExpired,
                version,
                parentId,
                resourceVersion,
                structureVersion);
        } else {
            return new CmsHistoryFile(
                tagId,
                structureId,
                resourceId,
                resourcePath,
                resourceType,
                resourceFlags,
                projectLastModified,
                CmsResourceState.valueOf(state),
                dateCreated,
                userCreated,
                dateLastModified,
                userLastModified,
                dateReleased,
                dateExpired,
                resourceSize,
                dateContent,
                version,
                parentId,
                null,
                resourceVersion,
                structureVersion);
        }
    }

    /**
     * Filters all resources in the subtree of the folder identified by the given id.<p>
     * 
     * @param parentId the id of the folder to filter
     * @param resources the resources to filter
     * 
     * @return the filtered list of resource
     */
    protected List internalFilterParentId(CmsUUID parentId, List resources) {

        List result = new ArrayList();

        // filter the parent id
        Set ids = new HashSet();
        ids.add(parentId);

        boolean modified = true;
        while (modified) {
            modified = false;
            Iterator it = resources.iterator();
            while (it.hasNext()) {
                I_CmsHistoryResource resource = (I_CmsHistoryResource)it.next();
                if (ids.contains(resource.getParentId())) {
                    ids.add(resource.getStructureId());
                    result.add(resource);
                    modified = true;
                    it.remove();
                }
            }
        }
        return result;
    }

    /**
     * Reads an historical entry for a sibling, based on the structure entry for the given sibling
     * and a publish tag for a resource entry made by a modification of other sibling.<p>
     * 
     * @param dbc the current database context 
     * @param histRes the original historical entry
     * @param pubTag the publish tag to get the resource entry from
     * 
     * @return a merged historical entry for the sibling
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected I_CmsHistoryResource internalReadMergedResource(CmsDbContext dbc, I_CmsHistoryResource histRes, int pubTag)
    throws CmsDataAccessException {

        Connection conn = null;
        I_CmsHistoryResource result = null;
        ResultSet res = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_MERGED_VERSION");
            stmt.setInt(1, pubTag < histRes.getPublishTag() ? 1 : 0);
            stmt.setInt(2, pubTag < histRes.getPublishTag() ? 1 : 0);
            stmt.setString(3, histRes.getStructureId().toString());
            stmt.setInt(4, histRes.getPublishTag());
            stmt.setInt(5, pubTag);
            res = stmt.executeQuery();
            if (res.next()) {
                result = internalCreateResource(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
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
     * Tests if a history resource does exist.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to test
     * @param publishTag the publish tag of the resource to test
     * 
     * @return <code>true</code> if the resource already exists, <code>false</code> otherweise
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected boolean internalValidateResource(CmsDbContext dbc, CmsResource resource, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_EXISTS_RESOURCE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setInt(2, publishTag);
            res = stmt.executeQuery();

            exists = res.next();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return exists;
    }
}
