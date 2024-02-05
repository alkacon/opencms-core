/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsVfsDriver;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the history driver methods.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryDriver implements I_CmsDriver, I_CmsHistoryDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsHistoryDriver.class);

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected CmsSqlManager m_sqlManager;

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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);

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
            boolean noHistoryStructure = false;
            if (res.next()) {
                maxVersion = res.getInt(1);
                noHistoryStructure |= res.wasNull();
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                // make sure the connection is closed
                m_sqlManager.closeAll(dbc, conn, stmt, res);
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }
            m_sqlManager.closeAll(dbc, conn, stmt, res);

            if (time >= 0) {
                int maxVersionByTime = -1;
                conn = m_sqlManager.getConnection(dbc);
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
                m_sqlManager.closeAll(dbc, conn, stmt, res);
                if (maxVersionByTime > 0) {
                    if (versionsToKeep < 0) {
                        versionsToKeep = (maxVersion - maxVersionByTime);
                    } else {
                        versionsToKeep = Math.min(versionsToKeep, (maxVersion - maxVersionByTime));
                    }
                }
            }
            int structureVersions = 0;
            // get the minimal structure publish tag to keep for this sibling
            conn = m_sqlManager.getConnection(dbc);
            if (!noHistoryStructure) {
                if ((versionsToKeep == -1) || ((maxVersion - versionsToKeep) <= 0)) {
                    // nothing to delete
                    internalCleanup(dbc, resource);
                    return 0;
                }

                // get the minimal structure publish tag to keep for this sibling
                int minStrPublishTagToKeep = -1;

                stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_MAXTAG_FOR_VERSION");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, (1 + maxVersion) - versionsToKeep);
                res = stmt.executeQuery();
                if (res.next()) {
                    minStrPublishTagToKeep = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                } else {
                    // make sure the statement and the result is closed
                    m_sqlManager.closeAll(dbc, conn, stmt, res);
                    // nothing to delete
                    internalCleanup(dbc, resource);
                    return 0;
                }
                m_sqlManager.closeAll(dbc, conn, stmt, res);
                if (minStrPublishTagToKeep < 1) {
                    // nothing to delete
                    internalCleanup(dbc, resource);
                    return 0;
                }
                minStrPublishTagToKeep++;

                // delete the properties
                conn = m_sqlManager.getConnection(dbc);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_DELETE");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, minStrPublishTagToKeep);
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);

                // delete the structure entries
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_DELETE");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, minStrPublishTagToKeep);
                structureVersions = stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            // get the minimal resource publish tag to keep,
            // all entries with publish tag less than this will be deleted
            int minResPublishTagToKeep = -1;
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_READ_MIN_USED_TAG");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                minResPublishTagToKeep = res.getInt(1);
                if (res.wasNull()) {
                    // the database will return a row with a single NULL column if there are no rows at all for the given
                    // resource id. This means that we want to clean up all resource history and content history entries
                    // for this resource id, and we achieve this by comparing their publish tag with the maximum integer.
                    minResPublishTagToKeep = Integer.MAX_VALUE;
                }
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
            m_sqlManager.closeAll(dbc, conn, stmt, res);

            // delete the resource entries
            conn = m_sqlManager.getConnection(dbc);
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

            // make sure the statement and the result is closed
            m_sqlManager.closeAll(dbc, conn, stmt, res);
            internalCleanup(dbc, resource);
            return Math.max(structureVersions, resourceVersions);
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if ((internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0)
                || (internalCountProperties(dbc, metadef, CmsUUID.getOpenCmsUUID()) != 0)) { // HACK: to get an offline project

                throw new CmsDbConsistencyException(
                    Messages.get().container(Messages.ERR_ERROR_DELETING_PROPERTYDEF_1, metadef.getName()));
            }

            // delete the historical property definition
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_HISTORY");
            stmt.setString(1, metadef.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
     * @see org.opencms.db.I_CmsHistoryDriver#getAllDeletedEntries(org.opencms.db.CmsDbContext)
     */
    public List<I_CmsHistoryResource> getAllDeletedEntries(CmsDbContext dbc) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        Map<CmsUUID, Integer> tmpEntrieis = new HashMap<CmsUUID, Integer>();
        List<I_CmsHistoryResource> entries = new ArrayList<I_CmsHistoryResource>();
        try {
            conn = m_sqlManager.getConnection(dbc);
            // get all not-deleted historical entries that may come in question
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_DELETED");
            res = stmt.executeQuery();
            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString(1));
                int version = res.getInt(2);
                tmpEntrieis.put(structureId, Integer.valueOf(version));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        for (Map.Entry<CmsUUID, Integer> entry : tmpEntrieis.entrySet()) {
            entries.add(readResource(dbc, entry.getKey(), entry.getValue().intValue()));
        }
        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getAllNotDeletedEntries(org.opencms.db.CmsDbContext)
     */
    public List<I_CmsHistoryResource> getAllNotDeletedEntries(CmsDbContext dbc) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        Map<CmsUUID, Integer> tmpEntrieis = new HashMap<CmsUUID, Integer>();

        List<I_CmsHistoryResource> entries = new ArrayList<I_CmsHistoryResource>();
        try {
            conn = m_sqlManager.getConnection(dbc);

            // get all not-deleted historical entries that may come in question
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_NOTDELETED");
            res = stmt.executeQuery();
            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString(1));
                int version = res.getInt(2);
                tmpEntrieis.put(structureId, Integer.valueOf(version));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        for (Map.Entry<CmsUUID, Integer> entry : tmpEntrieis.entrySet()) {
            entries.add(readResource(dbc, entry.getKey(), entry.getValue().intValue()));
        }
        return entries;
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
        List<String> successiveDrivers,
        CmsDriverManager driverManager) {

        CmsParameterConfiguration configuration = configurationManager.getConfiguration();

        String poolUrl;
        if (configuration.get("db.history.pool") != null) {
            poolUrl = configuration.get("db.history.pool").toString();
        } else {
            // TODO: deprecated, remove as soon as possible
            poolUrl = configuration.get("db.backup.pool").toString();
        }

        String classname;
        if (configuration.get("db.history.sqlmanager") != null) {
            classname = configuration.get("db.history.sqlmanager").toString();
        } else {
            // TODO: deprecated, remove as soon as possible
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
                LOG.warn(
                    Messages.get().getBundle().key(
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
    public List<I_CmsHistoryResource> readAllAvailableVersions(CmsDbContext dbc, CmsUUID structureId)
    throws CmsDataAccessException {

        ResultSet res = null;
        List<I_CmsHistoryResource> result = new ArrayList<I_CmsHistoryResource>();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // get all direct versions (where the structure entry has been written)
            // sorted from the NEWEST to the OLDEST version (publish tag descendant)
            List<I_CmsHistoryResource> historyResources = new ArrayList<I_CmsHistoryResource>();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_ALL_VERSIONS");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                historyResources.add(internalCreateResource(res));
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (!historyResources.isEmpty()) {
                // look for newer versions
                // this is the NEWEST version, with the HIGHEST publish tag
                I_CmsHistoryResource histRes = historyResources.get(0);

                // look for later resource entries
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_NEW_VERSIONS");
                stmt.setString(1, histRes.getResourceId().toString());
                stmt.setInt(2, histRes.getPublishTag());
                res = stmt.executeQuery();

                I_CmsHistoryResource lastHistRes = histRes;
                // these are sorted from the oldest to the newest version (publish tag ascendent)
                while (res.next()) {
                    int resVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                    if (resVersion == lastHistRes.getResourceVersion()) {
                        // skip not interesting versions
                        continue;
                    }
                    I_CmsHistoryResource newHistRes = internalMergeResource(histRes, res, 0);
                    // add interesting versions, in the right order
                    result.add(0, newHistRes);
                    lastHistRes = newHistRes;
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }
            // iterate from the NEWEST to the OLDEST versions (publish tag descendant)
            for (int i = 0; i < historyResources.size(); i++) {
                I_CmsHistoryResource histRes = historyResources.get(i);
                result.add(histRes);
                if (i < (historyResources.size() - 1)) {
                    // this is one older direct version than histRes (histRes.getPublishTag() > histRes2.getPublishTag())
                    I_CmsHistoryResource histRes2 = historyResources.get(i + 1);

                    // look for resource changes in between of the direct versions in ascendent order
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_BTW_VERSIONS");
                    stmt.setString(1, histRes.getResourceId().toString());
                    stmt.setInt(2, histRes2.getPublishTag()); // lower limit
                    stmt.setInt(3, histRes.getPublishTag()); // upper limit
                    res = stmt.executeQuery();

                    int pos = result.size();
                    I_CmsHistoryResource lastHistRes = histRes2;
                    while (res.next()) {
                        int resVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                        if (resVersion == lastHistRes.getResourceVersion()) {
                            // skip not interesting versions
                            continue;
                        }
                        I_CmsHistoryResource newHistRes = internalMergeResource(histRes2, res, 0);
                        // add interesting versions, in the right order
                        result.add(pos, newHistRes);
                        lastHistRes = newHistRes;
                    }
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                }
            }
            if (!result.isEmpty()) {
                // get the oldest version
                I_CmsHistoryResource histRes = result.get(result.size() - 1);

                if (histRes.getVersion() > 1) {
                    // look for older resource versions, in descendant order
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_OLD_VERSIONS");
                    stmt.setString(1, histRes.getResourceId().toString());
                    stmt.setInt(2, histRes.getPublishTag());
                    res = stmt.executeQuery();

                    int offset = (histRes.getStructureVersion() > 0 ? 1 : 0);

                    I_CmsHistoryResource lastHistRes = histRes;
                    while (res.next()) {
                        I_CmsHistoryResource newHistRes = internalMergeResource(histRes, res, offset);
                        if (newHistRes.getResourceVersion() != lastHistRes.getResourceVersion()) {
                            // only add interesting versions
                            if (offset == 1) {
                                if (histRes != lastHistRes) {
                                    result.add(lastHistRes);
                                }
                            } else {
                                result.add(newHistRes);
                            }
                        }
                        lastHistRes = newHistRes;
                    }
                    // add the last one if there is one
                    if ((offset == 1) && (lastHistRes != histRes)) {
                        result.add(lastHistRes);
                    }
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return content;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readDeletedResources(CmsDbContext, CmsUUID, CmsUUID)
     */
    public List<I_CmsHistoryResource> readDeletedResources(CmsDbContext dbc, CmsUUID structureId, CmsUUID userId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<I_CmsHistoryResource> result = new ArrayList<I_CmsHistoryResource>();
        List<I_CmsHistoryResource> tmpHistRes = new ArrayList<I_CmsHistoryResource>();

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
                // store the result into a temporary list
                tmpHistRes.add(internalCreateResource(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
        for (I_CmsHistoryResource histRes : tmpHistRes) {
            if (vfsDriver.validateStructureIdExists(dbc, dbc.currentProject().getUuid(), histRes.getStructureId())) {
                // only add resources that are really deleted
                continue;
            }
            result.add(histRes);
        }

        if (!result.isEmpty()
            || (dbc.getRequestContext() == null)
            || (dbc.getRequestContext().getAttribute("ATTR_RESOURCE_NAME") == null)) {
            return result;
        }
        try {
            conn = m_sqlManager.getConnection(dbc);
            if (userId == null) {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_DELETED_NAME");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_READ_DELETED_NAME_RESTRICTED");
            }
            String path = dbc.getRequestContext().getAttribute("ATTR_RESOURCE_NAME").toString();
            stmt.setString(1, path + '%');
            stmt.setString(2, path);
            if (userId != null) {
                stmt.setString(3, userId.toString());
            }
            res = stmt.executeQuery();
            // clear the temporary list
            tmpHistRes.clear();
            while (res.next()) {
                // store the result into a temporary list
                tmpHistRes.add(internalCreateResource(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        for (I_CmsHistoryResource histRes : tmpHistRes) {
            if (vfsDriver.validateStructureIdExists(dbc, dbc.currentProject().getUuid(), histRes.getStructureId())) {
                // only add resources that are really deleted
                continue;
            }
            result.add(histRes);
        }
        return result;
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
            // get the max publish tag from project history
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_MAXTAG");
            res = stmt.executeQuery();

            if (res.next()) {
                projectPublishTag = res.getInt(1) + 1;
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException exc) {
            LOG.error(Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)), exc);
        } finally {
            m_sqlManager.closeAll(dbc, null, stmt, res);
        }

        try {
            // get the max publish tag from resource history
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_HISTORY_MAXTAG");
            res = stmt.executeQuery();

            if (res.next()) {
                resourcePublishTag = res.getInt(1) + 1;
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException exc) {
            LOG.error(Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)), exc);
        } finally {
            m_sqlManager.closeAll(dbc, null, stmt, res);
        }

        // keep the biggest
        if (resourcePublishTag > projectPublishTag) {
            projectPublishTag = resourcePublishTag;
        }

        try {
            // get the max publish tag from contents
            stmt = m_sqlManager.getPreparedStatement(conn, "C_CONTENT_PUBLISH_MAXTAG");
            res = stmt.executeQuery();

            if (res.next()) {
                resourcePublishTag = res.getInt(1) + 1;
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException exc) {
            LOG.error(Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)), exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // return the biggest
        if (resourcePublishTag > projectPublishTag) {
            projectPublishTag = resourcePublishTag;
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
                String ou = CmsOrganizationalUnit.removeLeadingSeparator(
                    res.getString(m_sqlManager.readQuery("C_PRINCIPALS_HISTORY_OU")));
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
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_HISTORY_PRINCIPAL_NOT_FOUND_1, principalId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return historyPrincipal;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProject(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public CmsHistoryProject readProject(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsHistoryProject project = null;
        ResultSet res = null;
        Connection conn = null;

        int tmpTag;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_HISTORY_READ_BYID");

            stmt.setString(1, projectId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                tmpTag = res.getInt(m_sqlManager.readQuery("C_PROJECTS_PUBLISH_TAG_0"));
                project = internalCreateProject(res, null);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_HISTORY_PROJECT_WITH_ID_1, projectId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        List<String> projectresources = readProjectResources(dbc, tmpTag);
        project.setProjectResources(projectresources);

        return project;
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
                project = internalCreateProject(res, null);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_HISTORY_PROJECT_WITH_TAG_ID_1, Integer.valueOf(publishTag)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        List<String> projectresources = readProjectResources(dbc, publishTag);
        project.setProjectResources(projectresources);

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjectResources(org.opencms.db.CmsDbContext, int)
     */
    public List<String> readProjectResources(CmsDbContext dbc, int publishTag) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List<String> projectResources = new ArrayList<String>();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_HISTORY_READ");
            stmt.setInt(1, publishTag);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return projectResources;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjects(org.opencms.db.CmsDbContext)
     */
    public List<CmsHistoryProject> readProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List<CmsHistoryProject> projects = new ArrayList<CmsHistoryProject>();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        Map<Integer, CmsHistoryProject> tmpProjects = new HashMap<Integer, CmsHistoryProject>();

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
                tmpProjects.put(Integer.valueOf(res.getInt("PUBLISH_TAG")), internalCreateProject(res, null));
                i++;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        for (Map.Entry<Integer, CmsHistoryProject> entry : tmpProjects.entrySet()) {
            List<String> resources = readProjectResources(dbc, entry.getKey().intValue());
            entry.getValue().setProjectResources(resources);
            projects.add(entry.getValue());
        }

        return projects;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProperties(org.opencms.db.CmsDbContext, org.opencms.file.history.I_CmsHistoryResource)
     */
    public List<CmsProperty> readProperties(CmsDbContext dbc, I_CmsHistoryResource resource)
    throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        Map<String, CmsProperty> propertyMap = new HashMap<String, CmsProperty>();

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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return new ArrayList<CmsProperty>(propertyMap.values());
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
                    CmsPropertyDefinition.CmsPropertyType.valueOf(
                        res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_TYPE"))));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1, name));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
                throw new CmsVfsResourceNotFoundException(
                    Messages.get().container(Messages.ERR_HISTORY_FILE_NOT_FOUND_1, structureId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#setDriverManager(org.opencms.db.CmsDriverManager)
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#setSqlManager(org.opencms.db.CmsSqlManager)
     */
    public void setSqlManager(org.opencms.db.CmsSqlManager sqlManager) {

        m_sqlManager = (CmsSqlManager)sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writePrincipal(CmsDbContext, org.opencms.security.I_CmsPrincipal)
     */
    public void writePrincipal(CmsDbContext dbc, I_CmsPrincipal principal) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // check if the principal was already saved
            readPrincipal(dbc, principal.getId());
            return;
        } catch (CmsDbEntryNotFoundException e) {
            // ok
        }
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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

        List<String> projectresources = m_driverManager.getProjectDriver(dbc).readProjectResources(dbc, currentProject);

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
            Iterator<String> i = projectresources.iterator();
            while (i.hasNext()) {
                stmt.setInt(1, publishTag);
                stmt.setString(2, currentProject.getUuid().toString());
                stmt.setString(3, i.next());
                stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeProperties(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeProperties(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        Map<CmsProperty, CmsPropertyDefinition> propDefs = new HashMap<CmsProperty, CmsPropertyDefinition>();

        try {
            for (CmsProperty property : properties) {
                CmsPropertyDefinition propDef = null;
                try {
                    propDef = readPropertyDefinition(dbc, property.getName());
                } catch (CmsDbEntryNotFoundException e) {
                    // create if missing
                    propDef = createPropertyDefinition(dbc, property.getName(), CmsPropertyDefinition.TYPE_NORMAL);
                }
                propDefs.put(property, propDef);
            }
        } catch (CmsDataAccessException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        try {
            conn = m_sqlManager.getConnection(dbc);
            for (Map.Entry<CmsProperty, CmsPropertyDefinition> entry : propDefs.entrySet()) {

                for (int i = 0; i < 2; i++) {
                    int mappingType;
                    String value;
                    CmsUUID id;
                    if (i == 0) {
                        // write the structure value on the first cycle
                        value = entry.getKey().getStructureValue();
                        mappingType = CmsProperty.STRUCTURE_RECORD_MAPPING;
                        id = resource.getStructureId();
                        if (CmsStringUtil.isEmpty(value)) {
                            continue;
                        }
                    } else {
                        // write the resource value on the second cycle
                        value = entry.getKey().getResourceValue();
                        mappingType = CmsProperty.RESOURCE_RECORD_MAPPING;
                        id = resource.getResourceId();
                        if (CmsStringUtil.isEmpty(value)) {
                            break;
                        }
                    }

                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_HISTORY_CREATE");

                    stmt.setString(1, resource.getStructureId().toString());
                    stmt.setString(2, entry.getValue().getId().toString());
                    stmt.setString(3, id.toString());
                    stmt.setInt(4, mappingType);
                    stmt.setString(5, m_sqlManager.validateEmpty(value));
                    stmt.setInt(6, publishTag);

                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeResource(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            boolean valResource = internalValidateResource(dbc, resource, publishTag);
            int sibCount = resource.getSiblingCount();

            // if deleted
            if (resource.getState().isDeleted()) {
                // if it is a file
                if (resource instanceof CmsFile) {
                    if (!valResource) {
                        if (sibCount < 2) {
                            // copy from offline content to content tables
                            // so that the history contains the last state of the file
                            m_driverManager.getVfsDriver(dbc).createOnlineContent(
                                dbc,
                                resource.getResourceId(),
                                ((CmsFile)resource).getContents(),
                                publishTag,
                                false,
                                true);
                        } else {
                            @SuppressWarnings("unchecked")
                            Set<CmsUUID> changedAndDeleted = (Set<CmsUUID>)dbc.getAttribute(
                                CmsDriverManager.KEY_CHANGED_AND_DELETED);
                            if ((changedAndDeleted == null) || !changedAndDeleted.contains(resource.getResourceId())) {
                                // put the content definitively in the history if no sibling is left
                                // (unless another sibling with status "changed" or "new" is published)
                                m_driverManager.getVfsDriver(dbc).createOnlineContent(
                                    dbc,
                                    resource.getResourceId(),
                                    ((CmsFile)resource).getContents(),
                                    publishTag,
                                    true,
                                    false);
                            }
                        }
                    }
                }

                // update version numbers
                m_driverManager.getVfsDriver(dbc).publishVersions(dbc, resource, !valResource);
            }

            // read the version numbers
            Map<String, Integer> versions = m_driverManager.getVfsDriver(dbc).readVersions(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getResourceId(),
                resource.getStructureId());
            int structureVersion = (versions.get("structure")).intValue();
            int resourceVersion = (versions.get("resource")).intValue();

            CmsUUID parentId = CmsUUID.getNullUUID();
            CmsFolder parent = m_driverManager.getVfsDriver(dbc).readParentFolder(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getStructureId());
            if (parent != null) {
                parentId = parent.getStructureId();
            }

            conn = m_sqlManager.getConnection(dbc);
            if (!valResource) {
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
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
        Map<String, CmsProperty> propertyMap,
        I_CmsHistoryResource resource,
        String propertyKey,
        String propertyValue,
        int mappingType) throws CmsDbConsistencyException {

        CmsProperty property = propertyMap.get(propertyKey);
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
                    throw new CmsDbConsistencyException(
                        Messages.get().container(
                            Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                            resource.getRootPath(),
                            Integer.valueOf(mappingType),
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
                    throw new CmsDbConsistencyException(
                        Messages.get().container(
                            Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                            resource.getRootPath(),
                            Integer.valueOf(mappingType),
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

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Map<CmsUUID, Integer> tmpSubResources = new HashMap<CmsUUID, Integer>();

        // if is folder and if no versions left
        boolean isFolderAndNoVersionLeft = resource.getRootPath().endsWith("/")
            && (readLastVersion(dbc, resource.getStructureId()) == 0);

        // if the resource is a folder
        if (isFolderAndNoVersionLeft) {
            try {
                conn = m_sqlManager.getConnection(dbc);
                // get all direct subresources
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_HISTORY_READ_SUBRESOURCES");
                stmt.setString(1, resource.getStructureId().toString());
                res = stmt.executeQuery();
                while (res.next()) {
                    CmsUUID structureId = new CmsUUID(res.getString(1));
                    int version = res.getInt(2);
                    tmpSubResources.put(structureId, Integer.valueOf(version));
                }
            } catch (SQLException e) {
                throw new CmsDbSqlException(
                    Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                    e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
            }
        }
        // delete all subresource versions
        for (Map.Entry<CmsUUID, Integer> entry : tmpSubResources.entrySet()) {
            I_CmsHistoryResource histResource = readResource(dbc, entry.getKey(), entry.getValue().intValue());
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
                throw new CmsDbConsistencyException(
                    Messages.get().container(Messages.ERR_NO_PROPERTIES_FOR_PROPERTYDEF_1, metadef.getName()));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Creates a historical project from the given result set and resources.<p>
     * @param res the resource set
     * @param resources the historical resources
     *
     * @return the historical project
     *
     * @throws SQLException if something goes wrong
     */
    protected CmsHistoryProject internalCreateProject(ResultSet res, List<String> resources) throws SQLException {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(
            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_OU_0")));
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
                resourceVersion + structureVersion,
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
                resourceVersion + structureVersion,
                parentId,
                null,
                resourceVersion,
                structureVersion);
        }
    }

    /**
     * Merges an historical entry for a sibling, based on the structure data from the given historical resource
     * and result set for the resource entry.<p>
     *
     * @param histRes the original historical entry
     * @param res the result set of the resource entry
     * @param versionOffset the offset for the structure version
     *
     * @return a merged historical entry for the sibling
     *
     * @throws SQLException if something goes wrong
     */
    protected I_CmsHistoryResource internalMergeResource(I_CmsHistoryResource histRes, ResultSet res, int versionOffset)
    throws SQLException {

        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = histRes.getStructureVersion() - versionOffset;
        int tagId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PUBLISH_TAG"));
        CmsUUID structureId = histRes.getStructureId();
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID projectLastModified = new CmsUUID(
            res.getString(m_sqlManager.readQuery("C_RESOURCES_PROJECT_LASTMODIFIED")));
        int state = histRes.getState().getState(); // may be we have to compute something here?
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = histRes.getDateReleased();
        long dateExpired = histRes.getDateExpired();
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        // here we could use the path/parent id for the sibling where the modification really occurred
        String resourcePath = histRes.getRootPath();
        CmsUUID parentId = histRes.getParentId();
        long dateContent = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));

        if (histRes.isFolder()) {
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
                resourceVersion + structureVersion,
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
                resourceVersion + structureVersion,
                parentId,
                null,
                resourceVersion,
                structureVersion);
        }
    }

    /**
     * Tests if a history resource does exist.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to test
     * @param publishTag the publish tag of the resource to test
     *
     * @return <code>true</code> if the resource already exists, <code>false</code> otherwise
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
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return exists;
    }

}
