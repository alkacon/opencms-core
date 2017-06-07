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

package org.opencms.db.jpa;

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
import org.opencms.db.generic.Messages;
import org.opencms.db.jpa.persistence.CmsDAOContents;
import org.opencms.db.jpa.persistence.CmsDAOHistoryPrincipals;
import org.opencms.db.jpa.persistence.CmsDAOHistoryProjectResources;
import org.opencms.db.jpa.persistence.CmsDAOHistoryProjects;
import org.opencms.db.jpa.persistence.CmsDAOHistoryProperties;
import org.opencms.db.jpa.persistence.CmsDAOHistoryPropertyDef;
import org.opencms.db.jpa.persistence.CmsDAOHistoryResources;
import org.opencms.db.jpa.persistence.CmsDAOHistoryStructure;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsPropertyDefinition.CmsPropertyType;
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
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;

/**
 * JPA database server implementation of the history driver methods.<p>
 *
 * @since 8.0.0
 */
public class CmsHistoryDriver implements I_CmsDriver, I_CmsHistoryDriver {

    /** Query key. */
    private static final String C_CONTENT_HISTORY_DELETE = "C_CONTENT_HISTORY_DELETE";

    /** Query key. */
    private static final String C_CONTENT_PUBLISH_MAXTAG = "C_CONTENT_PUBLISH_MAXTAG";

    /** Query key. */
    private static final String C_FILES_READ_HISTORY_BYID = "C_FILES_READ_HISTORY_BYID";

    /** Query key. */
    private static final String C_HISTORY_EXISTS_RESOURCE = "C_HISTORY_EXISTS_RESOURCE";

    /** Query key. */
    private static final String C_HISTORY_PRINCIPAL_READ = "C_HISTORY_PRINCIPAL_READ";

    /** Query key. */
    private static final String C_HISTORY_READ_CONTENT = "C_HISTORY_READ_CONTENT";

    /** Query key. */
    private static final String C_HISTORY_READ_MAXTAG_FOR_VERSION = "C_HISTORY_READ_MAXTAG_FOR_VERSION";

    /** Query key. */
    private static final String C_HISTORY_READ_MIN_USED_TAG = "C_HISTORY_READ_MIN_USED_TAG";

    /** Query key. */
    private static final String C_PROJECTRESOURCES_HISTORY_READ = "C_PROJECTRESOURCES_HISTORY_READ";

    /** Query key. */
    private static final String C_PROJECTS_HISTORY_MAXTAG = "C_PROJECTS_HISTORY_MAXTAG";

    /** Query key. */
    private static final String C_PROJECTS_HISTORY_READ = "C_PROJECTS_HISTORY_READ";

    /** Query key. */
    private static final String C_PROJECTS_HISTORY_READ_ALL = "C_PROJECTS_HISTORY_READ_ALL";

    /** Query key. */
    private static final String C_PROJECTS_HISTORY_READ_BYID = "C_PROJECTS_HISTORY_READ_BYID";

    /** Query key. */
    private static final String C_PROJECTS_HISTORY_READ_TAG_FOR_DATE = "C_PROJECTS_HISTORY_READ_TAG_FOR_DATE";

    /** Query key. */
    private static final String C_PROPERTIES_HISTORY_DELETE = "C_PROPERTIES_HISTORY_DELETE";

    /** Query key. */
    private static final String C_PROPERTIES_HISTORY_READ_PUBTAG = "C_PROPERTIES_HISTORY_READ_PUBTAG";

    /** Query key. */
    private static final String C_PROPERTIES_HISTORY_READALL_RES = "C_PROPERTIES_HISTORY_READALL_RES";

    /** Query key. */
    private static final String C_PROPERTIES_HISTORY_READALL_STR = "C_PROPERTIES_HISTORY_READALL_STR";

    /** Query key. */
    private static final String C_PROPERTIES_READALL_COUNT = "C_PROPERTIES_READALL_COUNT";

    /** Query key. */
    private static final String C_PROPERTYDEF_DELETE_HISTORY = "C_PROPERTYDEF_DELETE_HISTORY";

    /** Query key. */
    private static final String C_PROPERTYDEF_READ_HISTORY = "C_PROPERTYDEF_READ_HISTORY";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_DELETE = "C_RESOURCES_HISTORY_DELETE";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_MAXTAG = "C_RESOURCES_HISTORY_MAXTAG";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_ALL_VERSIONS = "C_RESOURCES_HISTORY_READ_ALL_VERSIONS";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_BTW_VERSIONS = "C_RESOURCES_HISTORY_READ_BTW_VERSIONS";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_DELETED = "C_RESOURCES_HISTORY_READ_DELETED";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_DELETED_NAME = "C_RESOURCES_HISTORY_READ_DELETED_NAME";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_DELETED_NAME_RESTRICTED = "C_RESOURCES_HISTORY_READ_DELETED_NAME_RESTRICTED";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_DELETED_RESTRICTED = "C_RESOURCES_HISTORY_READ_DELETED_RESTRICTED";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_NEW_VERSIONS = "C_RESOURCES_HISTORY_READ_NEW_VERSIONS";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_OLD_VERSIONS = "C_RESOURCES_HISTORY_READ_OLD_VERSIONS";

    /** Query key. */
    private static final String C_RESOURCES_HISTORY_READ_VERSION = "C_RESOURCES_HISTORY_READ_VERSION";

    /** Query key. */
    private static final String C_RESOURCES_READ_MAX_PUBLISH_TAG = "C_RESOURCES_READ_MAX_PUBLISH_TAG";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_DELETE = "C_STRUCTURE_HISTORY_DELETE";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_MAXVER = "C_STRUCTURE_HISTORY_MAXVER";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_MAXVER_BYTIME = "C_STRUCTURE_HISTORY_MAXVER_BYTIME";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_READ_DELETED = "C_STRUCTURE_HISTORY_READ_DELETED";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_READ_NOTDELETED = "C_STRUCTURE_HISTORY_READ_NOTDELETED";

    /** Query key. */
    private static final String C_STRUCTURE_HISTORY_READ_SUBRESOURCES = "C_STRUCTURE_HISTORY_READ_SUBRESOURCES";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.jpa.CmsHistoryDriver.class);

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsPropertyDefinition.CmsPropertyType)
     */
    public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, String name, CmsPropertyType type)
    throws CmsDataAccessException {

        try {
            CmsDAOHistoryPropertyDef chpd = new CmsDAOHistoryPropertyDef();
            chpd.setPropertyDefId(new CmsUUID().toString());
            chpd.setPropertyDefName(name);
            chpd.setPropertyDefType(type.getMode());
            m_sqlManager.persist(dbc, chpd);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1), e);
        }
        return readPropertyDefinition(dbc, name);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#deleteEntries(org.opencms.db.CmsDbContext, org.opencms.file.history.I_CmsHistoryResource, int, long)
     */
    public int deleteEntries(CmsDbContext dbc, I_CmsHistoryResource resource, int versionsToKeep, long time)
    throws CmsDataAccessException {

        try {
            int maxVersion = -1;
            // get the maximal version number for this resource
            Query q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_MAXVER);
            q.setParameter(1, resource.getStructureId().toString());
            try {
                maxVersion = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }

            if (time >= 0) {
                int maxVersionByTime = -1;
                // get the maximal version to keep for this resource based on the time parameter
                q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_MAXVER_BYTIME);
                q.setParameter(1, resource.getStructureId().toString());
                q.setParameter(2, Long.valueOf(time));

                try {
                    maxVersionByTime = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
                } catch (NoResultException e) {
                    // do nothing
                }

                if (maxVersionByTime > 0) {
                    if (versionsToKeep < 0) {
                        versionsToKeep = (maxVersion - maxVersionByTime);
                    } else {
                        versionsToKeep = Math.min(versionsToKeep, (maxVersion - maxVersionByTime));
                    }
                }
            }

            if ((maxVersion - versionsToKeep) <= 0) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }

            // get the minimal structure publish tag to keep for this sibling
            int minStrPublishTagToKeep = -1;
            q = m_sqlManager.createQuery(dbc, C_HISTORY_READ_MAXTAG_FOR_VERSION);
            q.setParameter(1, resource.getStructureId().toString());
            q.setParameter(2, Integer.valueOf((1 + maxVersion) - versionsToKeep));
            try {
                minStrPublishTagToKeep = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }

            if (minStrPublishTagToKeep < 1) {
                // nothing to delete
                internalCleanup(dbc, resource);
                return 0;
            }
            minStrPublishTagToKeep++;

            // delete the properties
            q = m_sqlManager.createQuery(dbc, C_PROPERTIES_HISTORY_DELETE);
            q.setParameter(1, resource.getStructureId().toString());
            q.setParameter(2, Integer.valueOf(minStrPublishTagToKeep));
            @SuppressWarnings("unchecked")
            List<CmsDAOHistoryProperties> hisProps = q.getResultList();
            for (CmsDAOHistoryProperties hp : hisProps) {
                m_sqlManager.remove(dbc, hp);
            }

            // delete the structure entries
            q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_DELETE);
            q.setParameter(1, resource.getStructureId().toString());
            q.setParameter(2, Integer.valueOf(minStrPublishTagToKeep));
            @SuppressWarnings("unchecked")
            List<CmsDAOHistoryStructure> structureEntries = q.getResultList();
            int structureVersions = 0;
            for (CmsDAOHistoryStructure hs : structureEntries) {
                m_sqlManager.remove(dbc, hs);
                structureVersions++;
            }

            // get the minimal resource publish tag to keep,
            // all entries with publish tag less than this will be deleted
            int minResPublishTagToKeep = -1;
            q = m_sqlManager.createQuery(dbc, C_HISTORY_READ_MIN_USED_TAG);
            q.setParameter(1, resource.getResourceId().toString());

            try {
                Object numObj = q.getSingleResult();
                if (numObj == null) {
                    minResPublishTagToKeep = Integer.MAX_VALUE;
                } else {
                    minResPublishTagToKeep = CmsDataTypeUtil.numberToInt((Number)numObj);
                }
            } catch (NoResultException e) {
                internalCleanup(dbc, resource);
                return structureVersions;
            }

            // delete the resource entries
            q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_DELETE);
            q.setParameter(1, resource.getResourceId().toString());
            q.setParameter(2, Integer.valueOf(minResPublishTagToKeep));
            int resourceVersions = 0;
            @SuppressWarnings("unchecked")
            List<CmsDAOHistoryResources> resourceEntries = q.getResultList();
            for (CmsDAOHistoryResources hr : resourceEntries) {
                m_sqlManager.remove(dbc, hr);
                resourceVersions++;
            }

            // delete the content entries
            q = m_sqlManager.createQuery(dbc, C_CONTENT_HISTORY_DELETE);
            q.setParameter(1, resource.getResourceId().toString());
            q.setParameter(2, Integer.valueOf(minResPublishTagToKeep));
            @SuppressWarnings("unchecked")
            List<CmsDAOContents> contentEntries = q.getResultList();
            for (CmsDAOContents c : contentEntries) {
                m_sqlManager.remove(dbc, c);
            }

            internalCleanup(dbc, resource);

            return Math.max(structureVersions, resourceVersions);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef)
    throws CmsDataAccessException {

        try {
            if ((internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0)
                || (internalCountProperties(dbc, metadef, CmsUUID.getOpenCmsUUID()) != 0)) { // HACK: to get an offline project

                throw new CmsDbConsistencyException(
                    Messages.get().container(Messages.ERR_ERROR_DELETING_PROPERTYDEF_1, metadef.getName()));
            }

            // delete the historical property definition
            Query q = m_sqlManager.createQuery(dbc, C_PROPERTYDEF_DELETE_HISTORY);
            q.setParameter(1, metadef.getId().toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOHistoryPropertyDef> res = q.getResultList();
            for (CmsDAOHistoryPropertyDef hpd : res) {
                m_sqlManager.remove(dbc, hpd);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, Messages.ERR_JPA_PERSITENCE_1),
                e);
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

        Query q = null;
        List<I_CmsHistoryResource> entries = new ArrayList<I_CmsHistoryResource>();
        try {
            // get all not-deleted historical entries that may come in question
            q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_READ_DELETED);
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] obj : res) {
                CmsUUID structureId = new CmsUUID((String)obj[0]);
                int version = CmsDataTypeUtil.numberToInt((Integer)obj[1]);
                entries.add(readResource(dbc, structureId, version));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getAllNotDeletedEntries(CmsDbContext)
     */
    public List<I_CmsHistoryResource> getAllNotDeletedEntries(CmsDbContext dbc) throws CmsDataAccessException {

        List<I_CmsHistoryResource> entries = new ArrayList<I_CmsHistoryResource>();
        try {
            // get all not-deleted historical entries that may come in question
            Query q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_READ_NOTDELETED);
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                CmsUUID structureId = new CmsUUID((String)o[0]);
                int version = CmsDataTypeUtil.numberToInt((Number)o[1]);
                entries.add(readResource(dbc, structureId, version));
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#getSqlManager()
     */
    public org.opencms.db.CmsSqlManager getSqlManager() {

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

        CmsParameterConfiguration config = configurationManager.getConfiguration();

        String poolUrl = config.get("db.history.pool");
        String classname = config.get("db.history.sqlmanager");

        m_sqlManager = initSqlManager(classname);

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
     * @see org.opencms.db.I_CmsHistoryDriver#initSqlManager(java.lang.String)
     */
    public CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readAllAvailableVersions(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */

    public List<I_CmsHistoryResource> readAllAvailableVersions(CmsDbContext dbc, CmsUUID structureId)
    throws CmsDataAccessException {

        List<I_CmsHistoryResource> result = new ArrayList<I_CmsHistoryResource>();
        Query q = null;
        try {
            // get all direct versions (where the structure entry has been written)
            // sorted from the NEWEST to the OLDEST version (publish tag descendant)
            List<I_CmsHistoryResource> historyResources = new ArrayList<I_CmsHistoryResource>();
            q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_ALL_VERSIONS);
            q.setParameter(1, structureId.toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                historyResources.add(internalCreateResource(o));
            }
            res = null;

            if (!historyResources.isEmpty()) {
                // look for newer versions
                // this is the NEWEST version, with the HIGHEST publish tag
                I_CmsHistoryResource histRes = historyResources.get(0);

                // look for later resource entries

                q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_NEW_VERSIONS);
                q.setParameter(1, histRes.getResourceId().toString());
                q.setParameter(2, Integer.valueOf(histRes.getPublishTag()));
                @SuppressWarnings("unchecked")
                List<CmsDAOHistoryResources> lResources = q.getResultList();

                I_CmsHistoryResource lastHistRes = histRes;
                // these are sorted from the oldest to the newest version (publish tag ascendent)
                for (CmsDAOHistoryResources hr : lResources) {
                    int resVersion = hr.getResourceVersion();
                    if (resVersion == lastHistRes.getResourceVersion()) {
                        // skip not interesting versions
                        continue;
                    }
                    I_CmsHistoryResource newHistRes = internalMergeResource(histRes, hr, 0);
                    // add interesting versions, in the right order
                    result.add(0, newHistRes);
                    lastHistRes = newHistRes;
                }
            }
            // iterate from the NEWEST to the OLDEST versions (publish tag descendant)
            for (int i = 0; i < historyResources.size(); i++) {
                I_CmsHistoryResource histRes = historyResources.get(i);
                result.add(histRes);
                if (i < (historyResources.size() - 1)) {
                    // this is one older direct version than histRes (histRes.getPublishTag() > histRes2.getPublishTag())
                    I_CmsHistoryResource histRes2 = historyResources.get(i + 1);

                    // look for resource changes in between of the direct versions in ascendent order
                    q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_BTW_VERSIONS);
                    q.setParameter(1, histRes.getResourceId().toString());
                    q.setParameter(2, Integer.valueOf(histRes2.getPublishTag())); // lower limit
                    q.setParameter(3, Integer.valueOf(histRes.getPublishTag())); // upper limit
                    @SuppressWarnings("unchecked")
                    List<CmsDAOHistoryResources> lResources = q.getResultList();

                    int pos = result.size();
                    I_CmsHistoryResource lastHistRes = histRes2;
                    for (CmsDAOHistoryResources hr : lResources) {
                        int resVersion = hr.getResourceVersion();
                        if (resVersion == lastHistRes.getResourceVersion()) {
                            // skip not interesting versions
                            continue;
                        }
                        I_CmsHistoryResource newHistRes = internalMergeResource(histRes2, hr, 0);
                        // add interesting versions, in the right order
                        result.add(pos, newHistRes);
                        lastHistRes = newHistRes;
                    }
                    lResources = null;
                }
            }
            if (!result.isEmpty()) {
                // get the oldest version
                I_CmsHistoryResource histRes = result.get(result.size() - 1);

                if (histRes.getVersion() > 1) {
                    // look for older resource versions, in descendant order
                    q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_OLD_VERSIONS);
                    q.setParameter(1, String.valueOf(histRes.getResourceId()));
                    q.setParameter(2, Integer.valueOf(histRes.getPublishTag()));
                    @SuppressWarnings("unchecked")
                    List<CmsDAOHistoryResources> lResources = q.getResultList();

                    int offset = (histRes.getStructureVersion() > 0 ? 1 : 0);

                    I_CmsHistoryResource lastHistRes = histRes;
                    for (CmsDAOHistoryResources hr : lResources) {
                        I_CmsHistoryResource newHistRes = internalMergeResource(histRes, hr, offset);
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
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, int)
     */
    public byte[] readContent(CmsDbContext dbc, CmsUUID resourceId, int publishTag) throws CmsDataAccessException {

        byte[] content = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_HISTORY_READ_CONTENT);
            q.setParameter(1, resourceId.toString());
            q.setParameter(2, Integer.valueOf(publishTag));
            q.setParameter(3, Integer.valueOf(publishTag));

            try {
                content = ((CmsDAOContents)q.getSingleResult()).getFileContent();
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return content;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readDeletedResources(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public List<I_CmsHistoryResource> readDeletedResources(CmsDbContext dbc, CmsUUID structureId, CmsUUID userId)
    throws CmsDataAccessException {

        List<I_CmsHistoryResource> result = new ArrayList<I_CmsHistoryResource>();
        Query q = null;
        I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);

        try {
            if (userId == null) {
                q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_DELETED);
            } else {
                q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_DELETED_RESTRICTED);
            }
            q.setParameter(1, structureId.toString());
            if (userId != null) {
                q.setParameter(2, userId.toString());
            }
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                I_CmsHistoryResource histRes = internalCreateResource(o);
                if (vfsDriver.validateStructureIdExists(
                    dbc,
                    dbc.currentProject().getUuid(),
                    histRes.getStructureId())) {
                    // only add resources that are really deleted
                    continue;
                }
                result.add(histRes);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        if (!result.isEmpty()
            || (dbc.getRequestContext() == null)
            || (dbc.getRequestContext().getAttribute("ATTR_RESOURCE_NAME") == null)) {
            return result;
        }
        try {
            if (userId == null) {
                q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_DELETED_NAME);
            } else {
                q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_DELETED_NAME_RESTRICTED);
            }
            String path = dbc.getRequestContext().getAttribute("ATTR_RESOURCE_NAME").toString();
            q.setParameter(1, CmsVfsDriver.escapeDbWildcard(path + '%'));
            q.setParameter(2, path);
            if (userId != null) {
                q.setParameter(3, userId.toString());
            }
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                I_CmsHistoryResource histRes = internalCreateResource(o);
                if (vfsDriver.validateStructureIdExists(
                    dbc,
                    dbc.currentProject().getUuid(),
                    histRes.getStructureId())) {
                    // only add resources that are really deleted
                    continue;
                }
                result.add(histRes);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return result;
    }

    /**
     * Possibly there is no need for this method.<p>
     *
     * TODO: check if this method is used somewhere
     * TODO: remove this method
     *
     * @param dbc the db context
     * @param structureId the structure id
     * @param tagId the tag id
     *
     * @return the historical resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    @Deprecated
    public I_CmsHistoryResource readFile(CmsDbContext dbc, CmsUUID structureId, int tagId)
    throws CmsDataAccessException {

        I_CmsHistoryResource file = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_FILES_READ_HISTORY_BYID);
            q.setParameter(1, structureId.toString());
            q.setParameter(2, Integer.valueOf(tagId));
            Object[] res = (Object[])q.getSingleResult();
            file = internalCreateResource(res);
        } catch (NoResultException e) {
            throw new CmsVfsResourceNotFoundException(
                Messages.get().container(Messages.ERR_HISTORY_FILE_NOT_FOUND_1, structureId));
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        ((CmsFile)file).setContents(readContent(dbc, file.getResourceId(), file.getPublishTag()));
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readLastVersion(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public int readLastVersion(CmsDbContext dbc, CmsUUID structureId) throws CmsDataAccessException {

        int lastVersion = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_MAXVER);
            q.setParameter(1, structureId.toString());
            try {
                lastVersion = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            } catch (NullPointerException e) {
                lastVersion = 0;
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return lastVersion;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readMaxPublishTag(CmsDbContext, CmsUUID)
     */
    public int readMaxPublishTag(CmsDbContext dbc, CmsUUID resourceId) throws CmsDataAccessException {

        int result = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCES_READ_MAX_PUBLISH_TAG);
            q.setParameter(1, resourceId.toString());
            try {
                result = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readNextPublishTag(org.opencms.db.CmsDbContext)
     */
    public int readNextPublishTag(CmsDbContext dbc) {

        int projectPublishTag = 1;
        int resourcePublishTag = 1;
        Query q;

        try {
            // get the max publish tag from project history
            q = m_sqlManager.createQuery(dbc, C_PROJECTS_HISTORY_MAXTAG);
            try {
                projectPublishTag = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult()) + 1;
            } catch (NoResultException e) {
                // do nothing
            }

        } catch (PersistenceException e) {
            LOG.error(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        try {
            // get the max publish tag from resource history
            q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_MAXTAG);
            try {
                resourcePublishTag = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult()) + 1;
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            LOG.error(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        // keep the biggest
        if (resourcePublishTag > projectPublishTag) {
            projectPublishTag = resourcePublishTag;
        }

        try {
            // get the max publish tag from contents
            q = m_sqlManager.createQuery(dbc, C_CONTENT_PUBLISH_MAXTAG);
            try {
                resourcePublishTag = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult()) + 1;
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            // do nothing
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

        CmsHistoryPrincipal historyPrincipal = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_HISTORY_PRINCIPAL_READ);
            q.setParameter(1, principalId.toString());
            try {
                CmsDAOHistoryPrincipals hp = (CmsDAOHistoryPrincipals)q.getSingleResult();
                String userName = hp.getPrincipalName();
                String ou = CmsOrganizationalUnit.removeLeadingSeparator(hp.getPrincipalOu());
                historyPrincipal = new CmsHistoryPrincipal(
                    principalId,
                    ou + userName,
                    hp.getPrincipalDescription(),
                    hp.getPrincipalEmail(),
                    hp.getPrincipalType(),
                    new CmsUUID(hp.getPrincipalUserDeleted()),
                    hp.getPrincipalDateDeleted());
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_HISTORY_PRINCIPAL_NOT_FOUND_1, principalId));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return historyPrincipal;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProject(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public CmsHistoryProject readProject(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException {

        CmsHistoryProject project = null;
        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_HISTORY_READ_BYID);

            q.setParameter(1, projectId.toString());
            try {
                CmsDAOHistoryProjects hp = (CmsDAOHistoryProjects)q.getSingleResult();
                int tag = hp.getPublishTag();
                List<String> projectresources = readProjectResources(dbc, tag);
                project = internalCreateProject(hp, projectresources);
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_HISTORY_PROJECT_WITH_ID_1, projectId));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProject(org.opencms.db.CmsDbContext, int)
     */
    public CmsHistoryProject readProject(CmsDbContext dbc, int publishTag) throws CmsDataAccessException {

        CmsHistoryProject project = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_HISTORY_READ);

            q.setParameter(1, Integer.valueOf(publishTag));
            try {
                CmsDAOHistoryProjects hp = (CmsDAOHistoryProjects)q.getSingleResult();
                List<String> projectresources = readProjectResources(dbc, publishTag);
                project = internalCreateProject(hp, projectresources);
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_HISTORY_PROJECT_WITH_TAG_ID_1, new Integer(publishTag)));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjectResources(org.opencms.db.CmsDbContext, int)
     */
    public List<String> readProjectResources(CmsDbContext dbc, int publishTag) throws CmsDataAccessException {

        List<String> projectResources = new ArrayList<String>();
        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTRESOURCES_HISTORY_READ);
            q.setParameter(1, Integer.valueOf(publishTag));
            @SuppressWarnings("unchecked")
            List<String> res = q.getResultList();
            for (String s : res) {
                projectResources.add(s);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return projectResources;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProjects(org.opencms.db.CmsDbContext)
     */
    public List<CmsHistoryProject> readProjects(CmsDbContext dbc) throws CmsDataAccessException {

        List<CmsHistoryProject> projects = new ArrayList<CmsHistoryProject>();

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_HISTORY_READ_ALL);
            q.setMaxResults(300);
            @SuppressWarnings("unchecked")
            List<CmsDAOHistoryProjects> res = q.getResultList();

            for (CmsDAOHistoryProjects hp : res) {
                List<String> resources = readProjectResources(dbc, hp.getPublishTag());
                projects.add(internalCreateProject(hp, resources));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readProperties(org.opencms.db.CmsDbContext, org.opencms.file.history.I_CmsHistoryResource)
     */
    public List<CmsProperty> readProperties(CmsDbContext dbc, I_CmsHistoryResource resource)
    throws CmsDataAccessException {

        Map<String, CmsProperty> propertyMap = new HashMap<String, CmsProperty>();

        try {
            // get the latest properties for this sibling
            int pubTag = -1;
            Query q = m_sqlManager.createQuery(dbc, C_PROPERTIES_HISTORY_READ_PUBTAG);
            q.setParameter(1, String.valueOf(resource.getStructureId()));
            q.setParameter(2, Integer.valueOf(resource.getPublishTag()));
            try {
                pubTag = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }

            if (pubTag > 0) {
                // add the siblings props
                q = m_sqlManager.createQuery(dbc, C_PROPERTIES_HISTORY_READALL_STR);
                q.setParameter(1, resource.getStructureId().toString());
                q.setParameter(2, Integer.valueOf(pubTag));

                @SuppressWarnings("unchecked")
                List<Object[]> res = q.getResultList();
                for (Object[] o : res) {
                    String propertyKey = (String)o[0];
                    String propertyValue = (String)o[1];
                    int mappingType = CmsDataTypeUtil.numberToInt((Number)o[2]);

                    internalAddToPropMap(propertyMap, resource, propertyKey, propertyValue, mappingType);
                }
            }

            if (pubTag != resource.getPublishTag()) {
                // check if there were newer shared properties modifications
                q = m_sqlManager.createQuery(dbc, C_PROPERTIES_HISTORY_READALL_RES);
                q.setParameter(1, resource.getStructureId().toString());
                q.setParameter(2, Integer.valueOf(resource.getPublishTag()));
                @SuppressWarnings("unchecked")
                List<Object[]> res = q.getResultList();

                for (Object[] o : res) {
                    String propertyKey = (String)o[0];
                    String propertyValue = (String)o[1];
                    int mappingType = CmsDataTypeUtil.numberToInt((Number)o[2]);

                    internalAddToPropMap(propertyMap, resource, propertyKey, propertyValue, mappingType);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return new ArrayList<CmsProperty>(propertyMap.values());
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException {

        CmsPropertyDefinition propDef = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROPERTYDEF_READ_HISTORY);
            q.setParameter(1, name);
            try {
                CmsDAOHistoryPropertyDef hpd = (CmsDAOHistoryPropertyDef)q.getSingleResult();
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(hpd.getPropertyDefId()),
                    hpd.getPropertyDefName(),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(hpd.getPropertyDefType()));
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1, name));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readPublishTag(org.opencms.db.CmsDbContext, long)
     */
    public int readPublishTag(CmsDbContext dbc, long maxdate) throws CmsDataAccessException {

        int maxVersion = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_HISTORY_READ_TAG_FOR_DATE);
            q.setParameter(1, Long.valueOf(maxdate));
            try {
                maxVersion = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return maxVersion;
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#readResource(CmsDbContext, CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsDbContext dbc, CmsUUID structureId, int version)
    throws CmsDataAccessException {

        I_CmsHistoryResource resource = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCES_HISTORY_READ_VERSION);
            q.setParameter(1, structureId.toString());
            q.setParameter(2, Integer.valueOf(version));
            try {
                resource = internalCreateResource((Object[])q.getSingleResult());
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(
                    Messages.get().container(Messages.ERR_HISTORY_FILE_NOT_FOUND_1, structureId));
            }

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
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

        try {
            // check if the principal was already saved
            readPrincipal(dbc, principal.getId());
            return;
        } catch (CmsDbEntryNotFoundException e) {
            // ok
        }
        try {
            CmsDAOHistoryPrincipals hp = new CmsDAOHistoryPrincipals();
            hp.setPrincipalId(principal.getId().toString());
            hp.setPrincipalName(principal.getSimpleName());
            String desc = principal.getDescription();
            desc = CmsStringUtil.isEmptyOrWhitespaceOnly(desc) ? "-" : desc;
            hp.setPrincipalDescription(desc);
            hp.setPrincipalOu(CmsOrganizationalUnit.SEPARATOR + principal.getOuFqn());
            if (principal instanceof CmsUser) {
                String email = ((CmsUser)principal).getEmail();
                email = CmsStringUtil.isEmptyOrWhitespaceOnly(email) ? "-" : email;
                hp.setPrincipalEmail(email);
                hp.setPrincipalType(I_CmsPrincipal.PRINCIPAL_USER);
            } else {
                hp.setPrincipalEmail("-");
                hp.setPrincipalType(I_CmsPrincipal.PRINCIPAL_GROUP);
            }
            hp.setPrincipalUserDeleted(dbc.currentUser().getId().toString());
            hp.setPrincipalDateDeleted(System.currentTimeMillis());
            m_sqlManager.persist(dbc, hp);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
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
        try {
            CmsDAOHistoryProjects hp = new CmsDAOHistoryProjects();

            hp.setPublishTag(publishTag);
            hp.setProjectId(currentProject.getUuid().toString());
            hp.setProjectName(currentProject.getSimpleName());
            hp.setProjectPublishDate(publishDate);
            hp.setProjectPublishedBy(currentUser.getId().toString());
            hp.setUserId(currentProject.getOwnerId().toString());
            hp.setGroupId(currentProject.getGroupId().toString());
            hp.setManagerGroupId(currentProject.getManagerGroupId().toString());
            hp.setProjectDescription(currentProject.getDescription());
            hp.setDateCreated(currentProject.getDateCreated());
            hp.setProjectType(currentProject.getType().getMode());
            hp.setProjectOu(CmsOrganizationalUnit.SEPARATOR + currentProject.getOuFqn());

            m_sqlManager.persist(dbc, hp);

            // now write the projectresources
            CmsDAOHistoryProjectResources hpr;
            for (String projectResource : projectresources) {
                hpr = new CmsDAOHistoryProjectResources();
                hpr.setPublishTag(publishTag);
                hpr.setProjectId(currentProject.getUuid().toString());
                hpr.setResourcePath(projectResource);
                m_sqlManager.persist(dbc, hpr);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeProperties(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeProperties(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException {

        CmsDAOHistoryProperties hp;

        try {
            Iterator<CmsProperty> dummy = properties.iterator();
            while (dummy.hasNext()) {
                CmsProperty property = dummy.next();
                CmsPropertyDefinition propDef = null;
                try {
                    propDef = readPropertyDefinition(dbc, property.getName());
                } catch (CmsDbEntryNotFoundException e) {
                    // create if missing
                    propDef = createPropertyDefinition(dbc, property.getName(), CmsPropertyDefinition.TYPE_NORMAL);
                }

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

                    hp = new CmsDAOHistoryProperties();

                    hp.setStructureId(resource.getStructureId().toString());
                    hp.setPropertyDefId(propDef.getId().toString());
                    hp.setPropertyMappingId(id.toString());
                    hp.setPropertyMappingType(mappingType);
                    hp.setPropertyValue(m_sqlManager.validateEmpty(value));
                    hp.setPublishTag(publishTag);

                    m_sqlManager.persist(dbc, hp);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsHistoryDriver#writeResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int)
     */
    public void writeResource(CmsDbContext dbc, CmsResource resource, List<CmsProperty> properties, int publishTag)
    throws CmsDataAccessException {

        try {
            int sibCount = resource.getSiblingCount();

            boolean valResource = internalValidateResource(dbc, resource, publishTag);

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
            int structureVersion = CmsDataTypeUtil.numberToInt(versions.get("structure"));
            int resourceVersion = CmsDataTypeUtil.numberToInt(versions.get("resource"));

            if (!valResource) {
                // write the resource
                CmsDAOHistoryResources hs = new CmsDAOHistoryResources();

                hs.setResourceId(resource.getResourceId().toString());
                hs.setResourceType(resource.getTypeId());
                hs.setResourceFlags(resource.getFlags());
                hs.setDateCreated(resource.getDateCreated());
                hs.setUserCreated(resource.getUserCreated().toString());
                hs.setDateLastModified(resource.getDateLastModified());
                hs.setUserLastModified(resource.getUserLastModified().toString());
                hs.setResourceState(resource.getState().getState());
                hs.setResourceSize(resource.getLength());
                hs.setDateContent(resource.getDateContent());
                hs.setProjectLastModified(dbc.currentProject().getUuid().toString());
                hs.setSiblingCount(resource.getSiblingCount());
                hs.setResourceVersion(resourceVersion);
                hs.setPublishTag(publishTag);

                m_sqlManager.persist(dbc, hs);
            }
            CmsUUID parentId = CmsUUID.getNullUUID();
            CmsFolder parent = m_driverManager.getVfsDriver(dbc).readParentFolder(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getStructureId());
            if (parent != null) {
                parentId = parent.getStructureId();
            }
            // write the structure
            CmsDAOHistoryStructure hstr = new CmsDAOHistoryStructure();

            hstr.setStructureId(resource.getStructureId().toString());
            hstr.setResourceId(resource.getResourceId().toString());
            hstr.setResourcePath(resource.getRootPath());
            hstr.setStructureState(resource.getState().getState());
            hstr.setDateReleased(resource.getDateReleased());
            hstr.setDateExpired(resource.getDateExpired());
            hstr.setStructureVersion(structureVersion);
            hstr.setParentId(parentId.toString());
            hstr.setPublishTag(publishTag);
            hstr.setVersion(resource.getVersion());

            m_sqlManager.persist(dbc, hstr);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
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
                    throw new CmsDbConsistencyException(
                        Messages.get().container(
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
        List<I_CmsHistoryResource> subResources = new ArrayList<I_CmsHistoryResource>();
        // if the resource is a folder
        if (isFolder) {
            // and if no versions left
            if (readLastVersion(dbc, resource.getStructureId()) == 0) {
                // get all direct subresources

                try {
                    Query q = m_sqlManager.createQuery(dbc, C_STRUCTURE_HISTORY_READ_SUBRESOURCES);
                    q.setParameter(1, resource.getStructureId().toString());
                    @SuppressWarnings("unchecked")
                    List<Object[]> res = q.getResultList();
                    for (Object[] obj : res) {
                        CmsUUID structureId = new CmsUUID((String)obj[0]);
                        int version = CmsDataTypeUtil.numberToInt((Integer)obj[1]);
                        subResources.add(readResource(dbc, structureId, version));
                    }
                } catch (PersistenceException e) {
                    throw new CmsDbSqlException(
                        Messages.get().container(Messages.ERR_GENERIC_SQL_1, Messages.ERR_JPA_PERSITENCE_1),
                        e);
                }
            }
        }

        // delete all sub resource versions
        for (I_CmsHistoryResource histResource : subResources) {
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

        int returnValue;
        try {
            // create statement
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_READALL_COUNT);
            q.setParameter(1, metadef.getId().toString());
            try {
                returnValue = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                throw new CmsDbConsistencyException(
                    Messages.get().container(Messages.ERR_NO_PROPERTIES_FOR_PROPERTYDEF_1, metadef.getName()));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return returnValue;
    }

    /**
     * Creates a historical project from the given result set and resources.<p>
     * @param hp the CmsDAOHistoryProjects instance
     * @param resources the historical resources
     *
     * @return the historical project
     *
     * @throws PersistenceException if something goes wrong
     */
    protected CmsHistoryProject internalCreateProject(CmsDAOHistoryProjects hp, List<String> resources)
    throws PersistenceException {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(hp.getProjectOu());
        CmsUUID publishedById = new CmsUUID(hp.getProjectPublishedBy());
        CmsUUID userId = new CmsUUID(hp.getUserId());
        return new CmsHistoryProject(
            hp.getPublishTag(),
            new CmsUUID(hp.getProjectId()),
            ou + hp.getProjectName(),
            hp.getProjectDescription(),
            userId,
            new CmsUUID(hp.getGroupId()),
            new CmsUUID(hp.getManagerGroupId()),
            hp.getDateCreated(),
            CmsProject.CmsProjectType.valueOf(hp.getProjectType()),
            hp.getProjectPublishDate(),
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
     */
    protected I_CmsHistoryResource internalCreateResource(Object[] res) {

        CmsDAOHistoryStructure hs = (CmsDAOHistoryStructure)res[0];
        CmsDAOHistoryResources hr = (CmsDAOHistoryResources)res[1];

        int resourceVersion = hr.getResourceVersion();
        int structureVersion = hs.getStructureVersion();
        int tagId = hr.getPublishTag();
        CmsUUID structureId = new CmsUUID(hs.getStructureId());
        CmsUUID resourceId = new CmsUUID(hr.getResourceId());
        String resourcePath = hs.getResourcePath();
        int resourceType = hr.getResourceType();
        int resourceFlags = hr.getResourceFlags();
        CmsUUID projectLastModified = new CmsUUID(hr.getProjectLastModified());
        int state = Math.max(hr.getResourceState(), hs.getStructureState());
        long dateCreated = hr.getDateCreated();
        long dateLastModified = hr.getDateLastModified();
        long dateReleased = hs.getDateReleased();
        long dateExpired = hs.getDateExpired();
        int resourceSize = hr.getResourceSize();
        CmsUUID userLastModified = new CmsUUID(hr.getUserLastModified());
        CmsUUID userCreated = new CmsUUID(hr.getUserCreated());
        CmsUUID parentId = new CmsUUID(hs.getParentId());
        long dateContent = hr.getDateContent();

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
     * @param hr the CmsDAOHistoryResources instance of the resource entry
     * @param versionOffset the offset for the structure version
     *
     * @return a merged historical entry for the sibling
     *
     */
    protected I_CmsHistoryResource internalMergeResource(
        I_CmsHistoryResource histRes,
        CmsDAOHistoryResources hr,
        int versionOffset) {

        int resourceVersion = hr.getResourceVersion();
        int structureVersion = histRes.getStructureVersion() - versionOffset;
        int tagId = hr.getPublishTag();
        CmsUUID structureId = histRes.getStructureId();
        CmsUUID resourceId = new CmsUUID(hr.getResourceId());
        int resourceType = hr.getResourceType();
        int resourceFlags = hr.getResourceFlags();
        CmsUUID projectLastModified = new CmsUUID(hr.getProjectLastModified());
        int state = histRes.getState().getState(); // may be we have to compute something here?
        long dateCreated = hr.getDateCreated();
        long dateLastModified = hr.getDateLastModified();
        long dateReleased = histRes.getDateReleased();
        long dateExpired = histRes.getDateExpired();
        int resourceSize = hr.getResourceSize();
        CmsUUID userLastModified = new CmsUUID(hr.getUserLastModified());
        CmsUUID userCreated = new CmsUUID(hr.getUserCreated());
        // here we could use the path/parent id for the sibling where the modification really occurred
        String resourcePath = histRes.getRootPath();
        CmsUUID parentId = histRes.getParentId();
        long dateContent = hr.getDateContent();

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

        boolean exists = false;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_HISTORY_EXISTS_RESOURCE);
            q.setParameter(1, resource.getResourceId().toString());
            q.setParameter(2, Integer.valueOf(publishTag));
            try {
                q.getSingleResult();
                exists = true;
            } catch (NoResultException e) {
                //do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return exists;
    }
}
