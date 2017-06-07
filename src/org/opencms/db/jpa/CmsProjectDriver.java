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
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsVisitEntryFilter;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.generic.Messages;
import org.opencms.db.jpa.persistence.CmsDAOLog;
import org.opencms.db.jpa.persistence.CmsDAOOfflineResources;
import org.opencms.db.jpa.persistence.CmsDAOOfflineStructure;
import org.opencms.db.jpa.persistence.CmsDAOProjectResources;
import org.opencms.db.jpa.persistence.CmsDAOProjectResources.CmsDAOProjectResourcesPK;
import org.opencms.db.jpa.persistence.CmsDAOProjects;
import org.opencms.db.jpa.persistence.CmsDAOPublishHistory;
import org.opencms.db.jpa.persistence.CmsDAOPublishJobs;
import org.opencms.db.jpa.persistence.CmsDAOResourceLocks;
import org.opencms.db.jpa.persistence.CmsDAOStaticExportLinks;
import org.opencms.db.jpa.persistence.CmsDAOUserPublishListEntry;
import org.opencms.db.jpa.utils.CmsQueryIntParameter;
import org.opencms.db.jpa.utils.CmsQueryLongParameter;
import org.opencms.db.jpa.utils.CmsQueryStringParameter;
import org.opencms.db.jpa.utils.I_CmsQueryParameter;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogEntryType;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.db.userpublishlist.CmsUserPublishListEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * JPA database server implementation of the project driver methods.<p>
 *
 * @since 8.0.0
 */
public class CmsProjectDriver implements I_CmsDriver, I_CmsProjectDriver {

    /** Attribute name for reading the project of a resource. */
    public static final String DBC_ATTR_READ_PROJECT_FOR_RESOURCE = "DBC_ATTR_READ_PROJECT_FOR_RESOURCE";

    /** Query key. */
    private static final String C_DELETE_PUBLISH_HISTORY = "C_DELETE_PUBLISH_HISTORY";

    /** Query key. */
    private static final String C_LOG_DELETE_ENTRIES = "C_LOG_DELETE_ENTRIES";

    /** Query key. */
    private static final String C_LOG_FILTER_DATE_FROM = "C_LOG_FILTER_DATE_FROM";

    /** Query key. */
    private static final String C_LOG_FILTER_DATE_TO = "C_LOG_FILTER_DATE_TO";

    /** Query key. */
    private static final String C_LOG_FILTER_EXCLUDE_TYPE = "C_LOG_FILTER_EXCLUDE_TYPE";

    /** Query key. */
    private static final String C_LOG_FILTER_INCLUDE_TYPE = "C_LOG_FILTER_INCLUDE_TYPE";

    /** Query key. */
    private static final String C_LOG_FILTER_RESOURCE_ID = "C_LOG_FILTER_RESOURCE_ID";

    /** Query key. */
    private static final String C_LOG_FILTER_USER_ID = "C_LOG_FILTER_USER_ID";

    /** Query key. */
    private static final String C_LOG_READ_ENTRIES = "C_LOG_READ_ENTRIES";

    /** Query key. */
    private static final String C_PROJECTRESOURCES_DELETEALL_1 = "C_PROJECTRESOURCES_DELETEALL_1";

    /** Query key. */
    private static final String C_PROJECTRESOURCES_READ_2 = "C_PROJECTRESOURCES_READ_2";

    /** Query key. */
    private static final String C_PROJECTRESOURCES_READ_BY_ID_1 = "C_PROJECTRESOURCES_READ_BY_ID_1";

    /** Query key. */
    private static final String C_PROJECTS_READ_1 = "C_PROJECTS_READ_1";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYGROUP_2 = "C_PROJECTS_READ_BYGROUP_2";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYMANAGER_1 = "C_PROJECTS_READ_BYMANAGER_1";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYNAME_2 = "C_PROJECTS_READ_BYNAME_2";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYOU_1 = "C_PROJECTS_READ_BYOU_1";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYRESOURCE_1 = "C_PROJECTS_READ_BYRESOURCE_1";

    /** Query key. */
    private static final String C_PROJECTS_READ_BYUSER_1 = "C_PROJECTS_READ_BYUSER_1";

    /** Query key. */
    private static final String C_PROJECTS_WRITE_6 = "C_PROJECTS_WRITE_6";

    /** Query key. */
    private static final String C_PUBLISHJOB_DELETE_PUBLISHLIST = "C_PUBLISHJOB_DELETE_PUBLISHLIST";

    /** Query key. */
    private static final String C_PUBLISHJOB_READ_JOB = "C_PUBLISHJOB_READ_JOB";

    /** Query key. */
    private static final String C_PUBLISHJOB_READ_JOBS_IN_TIMERANGE = "C_PUBLISHJOB_READ_JOBS_IN_TIMERANGE";

    /** Query key. */
    private static final String C_PUBLISHJOB_READ_PUBLISHLIST = "C_PUBLISHJOB_READ_PUBLISHLIST";

    /** Query key. */
    private static final String C_PUBLISHJOB_READ_REPORT = "C_PUBLISHJOB_READ_REPORT";

    /** Query key. */
    private static final String C_RESOURCE_LOCKS_DELETEALL = "C_RESOURCE_LOCKS_DELETEALL";

    /** Query key. */
    private static final String C_RESOURCE_LOCKS_READALL = "C_RESOURCE_LOCKS_READALL";

    /** Query key. */
    private static final String C_RESOURCES_DELETE_PUBLISH_HISTORY_ENTRY = "C_RESOURCES_DELETE_PUBLISH_HISTORY_ENTRY";

    /** Query key. */
    private static final String C_RESOURCES_UNMARK = "C_RESOURCES_UNMARK";

    /** Query key. */
    private static final String C_SELECT_PUBLISHED_RESOURCES = "C_SELECT_PUBLISHED_RESOURCES";

    /** Query key. */
    private static final String C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS = "C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS";

    /** Query key. */
    private static final String C_STATICEXPORT_DELETE_PUBLISHED_LINKS = "C_STATICEXPORT_DELETE_PUBLISHED_LINKS";

    /** Query key. */
    private static final String C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS = "C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS";

    /** Query key. */
    private static final String C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS = "C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS";

    /** Query key. */
    private static final String C_STATICEXPORT_READ_PUBLISHED_RESOURCES = "C_STATICEXPORT_READ_PUBLISHED_RESOURCES";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.jpa.CmsProjectDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager. */
    protected CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int, CmsProject.CmsProjectType)
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        CmsUUID id,
        CmsUser owner,
        CmsGroup group,
        CmsGroup managergroup,
        String projectFqn,
        String description,
        int flags,
        CmsProject.CmsProjectType type) throws CmsDataAccessException {

        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        try {
            CmsDAOProjects p = new CmsDAOProjects();

            p.setProjectId(id.toString());
            p.setUserId(owner.getId().toString());
            p.setGroupId(group.getId().toString());
            p.setManagerGroupId(managergroup.getId().toString());
            p.setProjectName(CmsOrganizationalUnit.getSimpleName(projectFqn));
            p.setProjectDescription(description);
            p.setProjectFlags(flags);
            p.setProjectType(type.getMode());
            p.setProjectOu(CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));

            synchronized (this) {
                long createTime = System.currentTimeMillis();
                p.setDateCreated(createTime);
                m_sqlManager.persist(dbc, p);
                try {
                    // this is an ugly hack, but for MySQL (and maybe other DBs as well)
                    // there is a UNIQUE INDEX constraint on the project name+createTime
                    // so theoretically if 2 projects with the same name are created very fast, this
                    // SQL restraint would be violated if we don't wait here
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // continue
                }
                project = new CmsProject(
                    id,
                    projectFqn,
                    description,
                    owner.getId(),
                    group.getId(),
                    managergroup.getId(),
                    flags,
                    createTime,
                    type);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void createProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        // do not create entries for online-project
        boolean projectResourceExists = false;
        try {
            readProjectResource(dbc, projectId, resourcePath);
            projectResourceExists = true;
        } catch (CmsVfsResourceNotFoundException e) {
            // resource does not exist yet, everything is okay
            projectResourceExists = false;
        }

        if (projectResourceExists) {
            throw new CmsVfsResourceAlreadyExistsException(
                Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resourcePath)));
        }

        try {
            CmsDAOProjectResources pr = new CmsDAOProjectResources();

            pr.setProjectId(projectId.toString());
            pr.setResourcePath(resourcePath);

            m_sqlManager.persist(dbc, pr);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createPublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void createPublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsDataAccessException {

        try {
            CmsPublishJobInfoBean currentJob = readPublishJob(dbc, publishJob.getPublishHistoryId());
            LOG.error("wanted to write: " + publishJob);
            LOG.error("already on db: " + currentJob);
            return;
        } catch (CmsDbEntryNotFoundException e) {
            // ok, this is the expected behavior
        }
        try {
            CmsDAOPublishJobs pj = new CmsDAOPublishJobs();

            pj.setHistoryId(publishJob.getPublishHistoryId().toString());
            pj.setProjectId(publishJob.getProjectId().toString());
            pj.setProjectName(publishJob.getProjectName());
            pj.setUserId(publishJob.getUserId().toString());
            pj.setPublishLocale(publishJob.getLocale().toString());
            pj.setPublishFlags(publishJob.getFlags());
            pj.setResourceCount(publishJob.getSize());
            pj.setEnqueueTime(publishJob.getEnqueueTime());
            pj.setStartTime(publishJob.getStartTime());
            pj.setFinishTime(publishJob.getFinishTime());
            byte[] publishList = internalSerializePublishList(publishJob.getPublishList());
            pj.setPublishList(publishList);

            m_sqlManager.persist(dbc, pj);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(
                    Messages.ERR_SERIALIZING_PUBLISHLIST_1,
                    publishJob.getPublishHistoryId().toString()),
                e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteAllStaticExportPublishedResources(org.opencms.db.CmsDbContext, int)
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, int linkType) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS);
            q.setParameter(1, Integer.valueOf(linkType));
            List<CmsDAOStaticExportLinks> res = q.getResultList();
            for (CmsDAOStaticExportLinks sel : res) {
                m_sqlManager.remove(dbc, sel);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteLog(org.opencms.db.CmsDbContext, org.opencms.db.log.CmsLogFilter)
     */
    public void deleteLog(CmsDbContext dbc, CmsLogFilter filter) throws CmsDataAccessException {

        try {
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(C_LOG_DELETE_ENTRIES));

            CmsPair<String, List<I_CmsQueryParameter>> conditionsAndParams = prepareLogConditions(filter);
            queryBuf.append(conditionsAndParams.getFirst());
            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
            List<I_CmsQueryParameter> params = conditionsAndParams.getSecond();
            for (int i = 0; i < params.size(); i++) {
                I_CmsQueryParameter param = conditionsAndParams.getSecond().get(i);
                param.insertIntoQuery(q, i + 1);
            }

            // execute
            q.executeUpdate();
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // delete the resources from project_resources
        deleteProjectResources(dbc, project);

        // remove the project id form all resources within their project
        unmarkProjectResources(dbc, project);

        // finally delete the project
        try {
            CmsDAOProjects p = m_sqlManager.find(dbc, CmsDAOProjects.class, project.getUuid().toString());
            if (p != null) {
                m_sqlManager.remove(dbc, p);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void deleteProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourceName)
    throws CmsDataAccessException {

        try {
            CmsDAOProjectResourcesPK pk = new CmsDAOProjectResourcesPK();
            pk.setProjectId(projectId.toString());
            pk.setResourcePath(resourceName);
            CmsDAOProjectResources pr = m_sqlManager.find(dbc, CmsDAOProjectResources.class, pk);
            if (pr != null) {
                m_sqlManager.remove(dbc, pr);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTRESOURCES_DELETEALL_1);
            q.setParameter(1, project.getUuid().toString());
            List<CmsDAOProjectResources> res = q.getResultList();
            for (CmsDAOProjectResources pr : res) {
                m_sqlManager.remove(dbc, pr);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistory(org.opencms.db.CmsDbContext, CmsUUID, int)
     */
    public void deletePublishHistory(CmsDbContext dbc, CmsUUID projectId, int maxpublishTag)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_DELETE_PUBLISH_HISTORY);
            q.setParameter(1, Integer.valueOf(maxpublishTag));
            List<CmsDAOPublishHistory> res = q.getResultList();
            for (CmsDAOPublishHistory ph : res) {
                m_sqlManager.remove(dbc, ph);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistoryEntry(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void deletePublishHistoryEntry(
        CmsDbContext dbc,
        CmsUUID publishHistoryId,
        CmsPublishedResource publishedResource) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCES_DELETE_PUBLISH_HISTORY_ENTRY);
            q.setParameter(1, publishHistoryId.toString());
            q.setParameter(2, Integer.valueOf(publishedResource.getPublishTag()));
            q.setParameter(3, publishedResource.getStructureId().toString());
            q.setParameter(4, publishedResource.getRootPath());
            List<CmsDAOPublishHistory> res = q.getResultList();
            for (CmsDAOPublishHistory ph : res) {
                m_sqlManager.remove(dbc, ph);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishJob(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        try {
            CmsDAOPublishJobs pj = m_sqlManager.find(dbc, CmsDAOPublishJobs.class, publishHistoryId.toString());
            if (pj != null) {
                m_sqlManager.remove(dbc, pj);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PUBLISHJOB_DELETE_PUBLISHLIST);
            q.setParameter(1, publishHistoryId.toString());
            List<CmsDAOPublishJobs> res = q.getResultList();

            for (CmsDAOPublishJobs pj : res) {
                pj.setPublishList(null);
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteStaticExportPublishedResource(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.String)
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_STATICEXPORT_DELETE_PUBLISHED_LINKS);
            q.setParameter(1, resourceName);
            q.setParameter(2, Integer.valueOf(linkType));
            q.setParameter(3, linkParameter);
            List<CmsDAOStaticExportLinks> res = q.getResultList();
            for (CmsDAOStaticExportLinks sel : res) {
                m_sqlManager.remove(dbc, sel);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteUserPublishListEntries(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void deleteUserPublishListEntries(CmsDbContext dbc, List<CmsUserPublishListEntry> publishListDeletions)
    throws CmsDataAccessException {

        try {

            for (CmsUserPublishListEntry entry : publishListDeletions) {
                Query q = m_sqlManager.createQuery(dbc, "C_USER_PUBLISH_LIST_DELETE_2");
                q.setParameter(1, entry.getUserId().toString());
                q.setParameter(2, entry.getStructureId().toString());
                q.executeUpdate();
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#destroy()
     */
    public void destroy() throws Throwable {

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#fillDefaults(org.opencms.db.CmsDbContext)
     */
    public void fillDefaults(CmsDbContext dbc) throws CmsDataAccessException {

        try {
            if (readProject(dbc, CmsProject.ONLINE_PROJECT_ID) != null) {
                // online-project exists - no need of filling defaults
                return;
            }
        } catch (CmsDataAccessException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FILL_DEFAULTS_0));
        }

        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.readUser(dbc, adminUser);

        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.readGroup(dbc, administratorsGroup);

        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.readGroup(dbc, usersGroup);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // create the online project
        CmsProject onlineProject = createProject(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            admin,
            users,
            administrators,
            CmsProject.ONLINE_PROJECT_NAME,
            "The Online project",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        CmsFolder rootFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_CHANGED,
            0,
            admin.getId(),
            0,
            admin.getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            0);

        m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), rootFolder, null);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver(dbc).createProjectResource(
            dbc,
            onlineProject.getUuid(),
            rootFolder.getRootPath());

        // create the system-folder for the online project
        CmsFolder systemFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/system",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_CHANGED,
            0,
            admin.getId(),
            0,
            admin.getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            0);

        m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), systemFolder, null);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // important: must access through driver manager to ensure proper cascading
        CmsProject setupProject = m_driverManager.getProjectDriver(dbc).createProject(
            dbc,
            CmsUUID.getConstantUUID(SETUP_PROJECT_NAME),
            admin,
            administrators,
            administrators,
            SETUP_PROJECT_NAME,
            "The Project for the initial import",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_TEMPORARY);

        rootFolder.setState(CmsResource.STATE_CHANGED);
        // create the root-folder for the offline project
        CmsResource offlineRootFolder = m_driverManager.getVfsDriver(dbc).createResource(
            dbc,
            setupProject.getUuid(),
            rootFolder,
            null);

        offlineRootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver(dbc).writeResource(
            dbc,
            setupProject.getUuid(),
            offlineRootFolder,
            CmsDriverManager.NOTHING_CHANGED);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver(dbc).createProjectResource(
            dbc,
            setupProject.getUuid(),
            offlineRootFolder.getRootPath());

        systemFolder.setState(CmsResource.STATE_CHANGED);
        // create the system-folder for the offline project
        CmsResource offlineSystemFolder = m_driverManager.getVfsDriver(dbc).createResource(
            dbc,
            setupProject.getUuid(),
            systemFolder,
            null);

        offlineSystemFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver(dbc).writeResource(
            dbc,
            setupProject.getUuid(),
            offlineSystemFolder,
            CmsDriverManager.NOTHING_CHANGED);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#getUsersPubList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public List<CmsResource> getUsersPubList(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        try {
            Query q = m_sqlManager.createQuery(dbc, "C_USER_PUBLISH_LIST_READ_1");
            q.setParameter(1, userId.toString());
            List<Object[]> queryResults = q.getResultList();
            for (Object[] queryResult : queryResults) {
                CmsDAOOfflineStructure structure = (CmsDAOOfflineStructure)queryResult[1];
                CmsDAOOfflineResources resource = (CmsDAOOfflineResources)queryResult[2];
                CmsDAOUserPublishListEntry entry = (CmsDAOUserPublishListEntry)queryResult[0];
                Object[] resourceEntry = new Object[] {resource, structure, resource.getProjectLastModified()};
                CmsResource resourceObj = ((CmsVfsDriver)m_driverManager.getVfsDriver(dbc)).createResource(
                    resourceEntry,
                    dbc.currentProject().getUuid());
                resourceObj.setDateLastModified(entry.getDateChanged());
                result.add(resourceObj);
            }
            return result;
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return result;
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

        String poolUrl = config.get("db.project.pool");
        String classname = config.get("db.project.sqlmanager");

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
     * @see org.opencms.db.I_CmsProjectDriver#initSqlManager(String)
     */
    public CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#log(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void log(CmsDbContext dbc, List<CmsLogEntry> logEntries) throws CmsDbSqlException {

        try {

            EntityManager em = m_sqlManager.getEntityManager(dbc);
            em.getTransaction().commit();
            CmsDAOLog daoLog;
            for (CmsLogEntry logEntry : logEntries) {
                em.getTransaction().begin();
                daoLog = new CmsDAOLog();
                daoLog.setUserId(logEntry.getUserId().toString());
                daoLog.setLogDate(logEntry.getDate());
                daoLog.setStructureId(logEntry.getStructureId() == null ? null : logEntry.getStructureId().toString());
                daoLog.setLogType(logEntry.getType().getId());
                daoLog.setLogData(CmsStringUtil.arrayAsString(logEntry.getData(), "|"));
                em.persist(daoLog);
                // here commits on each record,
                // because there may be a duplicate records
                // and just ignore them
                try {
                    if ((em.getTransaction() != null) && em.getTransaction().isActive()) {
                        em.getTransaction().commit();
                    }
                } catch (RuntimeException e) {
                    if ((em.getTransaction() != null) && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                } finally {
                    em.clear();
                }
            }
            em.getTransaction().begin();
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, ""), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishDeletedFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, org.opencms.util.CmsUUID, int)
     */
    public void publishDeletedFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder currentFolder,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_DELETE_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(currentFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            CmsResourceState folderState = fixMovedResource(
                dbc,
                onlineProject,
                currentFolder,
                publishHistoryId,
                publishTag);

            // read the folder online
            CmsFolder onlineFolder = m_driverManager.readFolder(
                dbc,
                currentFolder.getRootPath(),
                CmsResourceFilter.ALL);

            // if the folder in the online-project contains any files, these need to be removed.
            // this can occur if these files were moved in the offline-project
            List<CmsResource> movedFiles = null;
            I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
            movedFiles = vfsDriver.readResourceTree(
                dbc,
                onlineProject.getUuid(),
                currentFolder.getRootPath(),
                CmsDriverManager.READ_IGNORE_TYPE,
                null,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_ONLY_FILES);

            for (CmsResource delFile : movedFiles) {
                try {
                    CmsResource offlineResource = vfsDriver.readResource(
                        dbc,
                        dbc.currentProject().getUuid(),
                        delFile.getStructureId(),
                        true);
                    CmsFile offlineFile = new CmsFile(offlineResource);
                    offlineFile.setContents(
                        vfsDriver.readContent(dbc, dbc.currentProject().getUuid(), offlineFile.getResourceId()));
                    internalWriteHistory(
                        dbc,
                        offlineFile,
                        CmsResource.STATE_DELETED,
                        null,
                        publishHistoryId,
                        publishTag);
                    vfsDriver.deletePropertyObjects(
                        dbc,
                        onlineProject.getUuid(),
                        delFile,
                        CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    vfsDriver.removeFile(dbc, onlineProject.getUuid(), delFile);
                    m_sqlManager.getEntityManager(dbc).getTransaction().commit();
                    m_sqlManager.getEntityManager(dbc).getTransaction().begin();
                } catch (CmsDataAccessException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, delFile.getName()),
                            e);
                    }
                }
            }

            // write history before deleting
            internalWriteHistory(dbc, currentFolder, folderState, null, publishHistoryId, publishTag);

            try {
                // delete the properties online and offline
                vfsDriver.deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            try {
                // remove the folder online and offline
                vfsDriver.removeFolder(dbc, dbc.currentProject(), currentFolder);
                vfsDriver.removeFolder(dbc, onlineProject, currentFolder);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            try {
                // remove the ACL online and offline
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    onlineProject,
                    onlineFolder.getResourceId());
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    currentFolder.getResourceId());
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            // remove relations
            try {
                vfsDriver.deleteRelations(dbc, onlineProject.getUuid(), onlineFolder, CmsRelationFilter.TARGETS);
                vfsDriver.deleteRelations(
                    dbc,
                    dbc.currentProject().getUuid(),
                    currentFolder,
                    CmsRelationFilter.TARGETS);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_RELATIONS_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            // remove project resources
            String deletedResourceRootPath = currentFolder.getRootPath();
            Iterator<CmsProject> itProjects = readProjectsForResource(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = itProjects.next();
                deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
            }

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEL_FOLDER_3,
                            currentFolder.getRootPath(),
                            String.valueOf(m),
                            String.valueOf(n)));
                }
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, currentFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFile(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set, org.opencms.util.CmsUUID, int)
     */
    public void publishFile(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedContentIds,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        /*
         * Never use onlineResource.getState() here!
         * Only use offlineResource.getState() to determine the state of an offline resource!
         *
         * In case a resource has siblings, after a sibling was published the structure
         * and resource states are reset to UNCHANGED -> the state of the corresponding
         * onlineResource is still NEW, DELETED or CHANGED.
         * Thus, using onlineResource.getState() will inevitably result in unpublished resources!
         */

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);

            if (offlineResource.getState().isDeleted()) {
                report.print(Messages.get().container(Messages.RPT_DELETE_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishDeletedFile(dbc, onlineProject, offlineResource, publishHistoryId, publishTag);

                dbc.pop();
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEL_FILE_3,
                            String.valueOf(m),
                            String.valueOf(n),
                            offlineResource.getRootPath()));
                }

            } else if (offlineResource.getState().isChanged()) {
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishChangedFile(
                    dbc,
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    publishHistoryId,
                    publishTag);

                dbc.pop();
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    OpenCms.getSystemInfo().getHistoryVersions(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_FILE_3,
                            offlineResource.getRootPath(),
                            String.valueOf(m),
                            String.valueOf(n)));
                }
            } else if (offlineResource.getState().isNew()) {
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishNewFile(dbc, onlineProject, offlineResource, publishedContentIds, publishHistoryId, publishTag);

                dbc.pop();
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    OpenCms.getSystemInfo().getHistoryVersions(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_FILE_3,
                                offlineResource.getRootPath(),
                                String.valueOf(m),
                                String.valueOf(n)));
                    }
                }
            } else {
                // state == unchanged !!?? something went really wrong
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);

                if (LOG.isErrorEnabled()) {
                    // the whole resource is printed out here
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_FILE_3,
                            String.valueOf(m),
                            String.valueOf(n),
                            offlineResource));
                }
            }
        } catch (CmsException e) {
            throw new CmsDataAccessException(e.getMessageContainer(), e);
        } finally {
            // notify the app. that the published file and it's properties have been modified offline
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, offlineResource)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(CmsDbContext, CmsProject, CmsProject, CmsResource, Set, boolean, int)
     */
    public CmsFile publishFileContent(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedResourceIds,
        boolean needToUpdateContent,
        int publishTag) throws CmsDataAccessException {

        CmsFile newFile = null;
        try {
            // read the file content offline
            CmsUUID projectId = dbc.getProjectId();
            boolean dbcHasProjectId = (projectId != null) && !projectId.isNullUUID();
            CmsUUID projectIdForReading = (!dbcHasProjectId ? offlineProject.getUuid() : CmsProject.ONLINE_PROJECT_ID);
            dbc.setProjectId(offlineProject.getUuid());
            byte[] offlineContent = m_driverManager.getVfsDriver(dbc).readContent(
                dbc,
                projectIdForReading,
                offlineResource.getResourceId());
            CmsFile offlineFile = new CmsFile(offlineResource);
            offlineFile.setContents(offlineContent);
            dbc.setProjectId(projectId);

            // create the file online
            newFile = (CmsFile)offlineFile.clone();
            newFile.setState(CmsResource.STATE_UNCHANGED);

            boolean createSibling = true;
            // check if we are facing with a create new sibling operation
            if (!offlineFile.getState().isNew()) {
                createSibling = false;
            } else {
                // check if the resource entry already exists
                if (!m_driverManager.getVfsDriver(dbc).validateResourceIdExists(
                    dbc,
                    onlineProject.getUuid(),
                    offlineFile.getResourceId())) {
                    // we are creating a normal resource and not a sibling
                    createSibling = false;
                }
            }

            // only update the content if it was not updated before
            boolean alreadyPublished = publishedResourceIds.contains(offlineResource.getResourceId());
            needToUpdateContent &= !alreadyPublished;

            if (createSibling) {
                if (!alreadyPublished) {
                    // create the file online, the first time a sibling is published also the resource entry has to be actualized
                    m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), newFile, null);
                } else {
                    // create the sibling online
                    m_driverManager.getVfsDriver(dbc).createSibling(dbc, onlineProject, offlineResource);
                }
                newFile = new CmsFile(offlineResource);
                newFile.setContents(offlineContent);
            } else {
                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, newFile, offlineFile);
            }
            // update version numbers
            m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineResource, !alreadyPublished);

            // create/update the content
            m_driverManager.getVfsDriver(dbc).createOnlineContent(
                dbc,
                offlineFile.getResourceId(),
                offlineFile.getContents(),
                publishTag,
                true,
                needToUpdateContent);

            // mark the resource as written to avoid that the same content is written for each sibling instance
            publishedResourceIds.add(offlineResource.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_FILE_CONTENT_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }
        return newFile;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, org.opencms.util.CmsUUID, int)
     */
    public void publishFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder offlineFolder,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_PUBLISH_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            CmsResourceState resourceState = fixMovedResource(
                dbc,
                onlineProject,
                offlineFolder,
                publishHistoryId,
                publishTag);

            CmsResource onlineFolder = null;
            if (offlineFolder.getState().isNew()) {
                try {
                    // create the folder online
                    CmsResource newFolder = (CmsFolder)offlineFolder.clone();
                    newFolder.setState(CmsResource.STATE_UNCHANGED);

                    onlineFolder = m_driverManager.getVfsDriver(dbc).createResource(
                        dbc,
                        onlineProject.getUuid(),
                        newFolder,
                        null);
                    m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, onlineFolder, offlineFolder);
                    // update version numbers
                    m_driverManager.getVfsDriver().publishVersions(dbc, offlineFolder, true);
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    if (!offlineFolder.getRootPath().equals("/")
                        && !offlineFolder.getRootPath().equals("/system/")
                        && LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_WARN_FOLDER_WRONG_STATE_CN_1,
                                offlineFolder.getRootPath()));
                    }
                    try {
                        onlineFolder = m_driverManager.getVfsDriver(dbc).readFolder(
                            dbc,
                            onlineProject.getUuid(),
                            offlineFolder.getRootPath());
                        m_driverManager.getVfsDriver(dbc).publishResource(
                            dbc,
                            onlineProject,
                            onlineFolder,
                            offlineFolder);
                        // update version numbers
                        m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineFolder, true);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_READING_RESOURCE_1,
                                    offlineFolder.getRootPath()),
                                e);
                        }
                        throw e1;
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            } else if (offlineFolder.getState().isChanged()) {
                try {
                    // read the folder online
                    onlineFolder = m_driverManager.getVfsDriver(dbc).readFolder(
                        dbc,
                        onlineProject.getUuid(),
                        offlineFolder.getStructureId());
                } catch (CmsVfsResourceNotFoundException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_WARN_FOLDER_WRONG_STATE_NC_1,
                                offlineFolder.getRootPath()));
                    }
                    try {
                        onlineFolder = m_driverManager.getVfsDriver(dbc).createResource(
                            dbc,
                            onlineProject.getUuid(),
                            offlineFolder,
                            null);
                        internalResetResourceState(dbc, onlineFolder);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_PUBLISHING_RESOURCE_1,
                                    offlineFolder.getRootPath()),
                                e);
                        }
                        throw e1;
                    }
                }

                try {
                    // update the folder online
                    m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, onlineFolder, offlineFolder);
                    // update version numbers
                    m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineFolder, true);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            }

            if (onlineFolder != null) {
                try {
                    // write the ACL online
                    m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        offlineFolder.getResourceId(),
                        onlineFolder.getResourceId());
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            }

            List<CmsProperty> offlineProperties = null;
            try {
                // write the properties online
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                    dbc,
                    dbc.currentProject(),
                    offlineFolder);
                CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                m_driverManager.getVfsDriver(dbc).writePropertyObjects(
                    dbc,
                    onlineProject,
                    onlineFolder,
                    offlineProperties);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_PROPERTIES_1,
                            offlineFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            internalWriteHistory(dbc, offlineFolder, resourceState, offlineProperties, publishHistoryId, publishTag);

            m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineFolder);

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_PUBLISHING_FOLDER_3,
                        String.valueOf(m),
                        String.valueOf(n),
                        offlineFolder.getRootPath()));
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, offlineFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishProject(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, org.opencms.file.CmsProject, org.opencms.db.CmsPublishList, int)
     */
    public void publishProject(
        CmsDbContext dbc,
        I_CmsReport report,
        CmsProject onlineProject,
        CmsPublishList publishList,
        int publishTag) throws CmsException {

        int publishedFolderCount = 0;
        int deletedFolderCount = 0;
        int publishedFileCount = 0;
        Set<CmsUUID> publishedContentIds = new HashSet<CmsUUID>();
        Set<CmsUUID> publishedIds = new HashSet<CmsUUID>();

        try {

            ////////////////////////////////////////////////////////////////////////////////////////
            // write the historical project entry

            if (OpenCms.getSystemInfo().isHistoryEnabled()) {
                try {
                    // write an entry in the publish project log
                    m_driverManager.getHistoryDriver(dbc).writeProject(dbc, publishTag, System.currentTimeMillis());
                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(
                            Messages.ERR_WRITING_HISTORY_OF_PROJECT_1,
                            dbc.currentProject().getName()),
                        t);
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // publish new/changed folders

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_START_PUBLISHING_PROJECT_2,
                        dbc.currentProject().getName(),
                        dbc.currentUser().getName()));
            }

            publishedFolderCount = 0;
            int foldersSize = publishList.getFolderList().size();
            if (foldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Iterator<CmsResource> itFolders = publishList.getFolderList().iterator();
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            I_CmsHistoryDriver historyDriver = m_driverManager.getHistoryDriver(dbc);
            while (itFolders.hasNext()) {
                CmsResource currentFolder = itFolders.next();
                try {
                    if (currentFolder.getState().isNew() || currentFolder.getState().isChanged()) {
                        // bounce the current publish task through all project drivers
                        projectDriver.publishFolder(
                            dbc,
                            report,
                            ++publishedFolderCount,
                            foldersSize,
                            onlineProject,
                            new CmsFolder(currentFolder),
                            publishList.getPublishHistoryId(),
                            publishTag);

                        dbc.pop();

                        publishedIds.add(currentFolder.getStructureId());
                        // log it
                        CmsLogEntryType type = currentFolder.getState().isNew()
                        ? CmsLogEntryType.RESOURCE_PUBLISHED_NEW
                        : CmsLogEntryType.RESOURCE_PUBLISHED_MODIFIED;
                        m_driverManager.log(
                            dbc,
                            new CmsLogEntry(
                                dbc,
                                currentFolder.getStructureId(),
                                type,
                                new String[] {currentFolder.getRootPath()}),
                            true);

                        // delete old historical entries
                        historyDriver.deleteEntries(
                            dbc,
                            new CmsHistoryFile(currentFolder),
                            OpenCms.getSystemInfo().getHistoryVersions(),
                            -1);

                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentFolder);

                        m_driverManager.unlockResource(dbc, currentFolder, true, true);
                    } else {
                        // state == unchanged !!?? something went really wrong
                        report.print(Messages.get().container(Messages.RPT_PUBLISH_FOLDER_0), I_CmsReport.FORMAT_NOTE);
                        report.print(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_ARGUMENT_1,
                                dbc.removeSiteRoot(currentFolder.getRootPath())));
                        report.print(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                            I_CmsReport.FORMAT_ERROR);

                        if (LOG.isErrorEnabled()) {
                            // the whole resource is printed out here
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_PUBLISHING_FILE_3,
                                    String.valueOf(++publishedFolderCount),
                                    String.valueOf(foldersSize),
                                    currentFolder));
                        }
                    }

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(Messages.ERR_ERROR_PUBLISHING_FOLDER_1, currentFolder.getRootPath()),
                        t);
                }
            }

            if (foldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // publish changed/new/deleted files

            publishedFileCount = 0;
            int filesSize = publishList.getFileList().size();

            if (filesSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FILES_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Set<CmsUUID> deletedResourceIds = new HashSet<CmsUUID>();
            Set<CmsUUID> changedResourceIds = new HashSet<CmsUUID>();
            for (CmsResource res : publishList.getFileList()) {
                if (res.getState().isDeleted()) {
                    deletedResourceIds.add(res.getResourceId());
                } else {
                    changedResourceIds.add(res.getResourceId());
                }
            }
            Set<CmsUUID> changedAndDeletedResourceIds = Sets.intersection(deletedResourceIds, changedResourceIds);
            dbc.setAttribute(CmsDriverManager.KEY_CHANGED_AND_DELETED, changedAndDeletedResourceIds);

            Iterator<CmsResource> itFiles = publishList.getFileList().iterator();
            while (itFiles.hasNext()) {
                CmsResource currentResource = itFiles.next();
                try {
                    // bounce the current publish task through all project drivers
                    projectDriver.publishFile(
                        dbc,
                        report,
                        ++publishedFileCount,
                        filesSize,
                        onlineProject,
                        currentResource,
                        publishedContentIds,
                        publishList.getPublishHistoryId(),
                        publishTag);

                    CmsResourceState state = currentResource.getState();
                    if (!state.isDeleted()) {
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentResource);
                    }

                    // unlock it
                    m_driverManager.unlockResource(dbc, currentResource, true, true);
                    // log it
                    CmsLogEntryType type = state.isNew()
                    ? CmsLogEntryType.RESOURCE_PUBLISHED_NEW
                    : (state.isDeleted()
                    ? CmsLogEntryType.RESOURCE_PUBLISHED_DELETED
                    : CmsLogEntryType.RESOURCE_PUBLISHED_MODIFIED);
                    m_driverManager.log(
                        dbc,
                        new CmsLogEntry(
                            dbc,
                            currentResource.getStructureId(),
                            type,
                            new String[] {currentResource.getRootPath()}),
                        true);

                    publishedIds.add(currentResource.getStructureId());
                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(Messages.ERR_ERROR_PUBLISHING_FILE_1, currentResource.getRootPath()),
                        t);
                }
            }

            if (filesSize > 0) {
                report.println(Messages.get().container(Messages.RPT_PUBLISH_FILES_END_0), I_CmsReport.FORMAT_HEADLINE);
            }

            ////////////////////////////////////////////////////////////////////////////////////////

            // publish deleted folders
            List<CmsResource> deletedFolders = publishList.getDeletedFolderList();
            if (deletedFolders.isEmpty()) {
                return;
            }

            deletedFolderCount = 0;
            int deletedFoldersSize = deletedFolders.size();
            if (deletedFoldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_DELETE_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Iterator<CmsResource> itDeletedFolders = deletedFolders.iterator();
            while (itDeletedFolders.hasNext()) {
                CmsResource currentFolder = itDeletedFolders.next();

                try {
                    // bounce the current publish task through all project drivers
                    projectDriver.publishDeletedFolder(
                        dbc,
                        report,
                        ++deletedFolderCount,
                        deletedFoldersSize,
                        onlineProject,
                        new CmsFolder(currentFolder),
                        publishList.getPublishHistoryId(),
                        publishTag);

                    dbc.pop();
                    // delete old historical entries
                    m_driverManager.getHistoryDriver(dbc).deleteEntries(
                        dbc,
                        new CmsHistoryFile(currentFolder),
                        OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion(),
                        -1);

                    publishedIds.add(currentFolder.getStructureId());
                    // unlock it
                    m_driverManager.unlockResource(dbc, currentFolder, true, true);
                    // log it
                    m_driverManager.log(
                        dbc,
                        new CmsLogEntry(
                            dbc,
                            currentFolder.getStructureId(),
                            CmsLogEntryType.RESOURCE_PUBLISHED_DELETED,
                            new String[] {currentFolder.getRootPath()}),
                        true);

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(
                            Messages.ERR_ERROR_PUBLISHING_DELETED_FOLDER_1,
                            currentFolder.getRootPath()),
                        t);
                }
            }

            if (deletedFoldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_DELETE_FOLDERS_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        } catch (OutOfMemoryError o) {
            // clear all caches to reclaim memory
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));

            CmsMessageContainer message = Messages.get().container(Messages.ERR_OUT_OF_MEMORY_0);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), o);
            }
            throw new CmsDataAccessException(message, o);
        } finally {
            // reset vfs driver internal info after publishing
            m_driverManager.getVfsDriver(dbc).publishVersions(dbc, null, false);
            Object[] msgArgs = new Object[] {
                String.valueOf(publishedFileCount),
                String.valueOf(publishedFolderCount),
                String.valueOf(deletedFolderCount),
                report.formatRuntime()};

            CmsMessageContainer message = Messages.get().container(Messages.RPT_PUBLISH_STAT_4, msgArgs);
            if (LOG.isInfoEnabled()) {
                LOG.info(message.key());
            }
            report.println(message);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readLocks(org.opencms.db.CmsDbContext)
     */
    public List<CmsLock> readLocks(CmsDbContext dbc) throws CmsDataAccessException {

        List<CmsLock> locks = new ArrayList<CmsLock>(256);
        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCE_LOCKS_READALL);
            List<CmsDAOResourceLocks> res = q.getResultList();
            for (CmsDAOResourceLocks rl : res) {
                String resourcePath = rl.getResourcePath();
                CmsUUID userId = new CmsUUID(rl.getUserId());
                CmsUUID projectId = new CmsUUID(rl.getProjectId());
                int lockType = rl.getLockType();
                CmsProject project;
                try {
                    project = readProject(dbc, projectId);
                } catch (CmsDataAccessException dae) {
                    // the project does not longer exist, ignore this lock (should usually not happen)
                    project = null;
                }
                if (project != null) {
                    CmsLock lock = new CmsLock(resourcePath, userId, project, CmsLockType.valueOf(lockType));
                    locks.add(lock);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_READ_LOCKS_1, new Integer(locks.size())));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return locks;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readLog(org.opencms.db.CmsDbContext, org.opencms.db.log.CmsLogFilter)
     */
    public List<CmsLogEntry> readLog(CmsDbContext dbc, CmsLogFilter filter) throws CmsDataAccessException {

        List<CmsLogEntry> entries = new ArrayList<CmsLogEntry>();

        try {
            // compose statement
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(C_LOG_READ_ENTRIES));
            CmsPair<String, List<I_CmsQueryParameter>> conditionsAndParameters = prepareLogConditions(filter);
            List<I_CmsQueryParameter> params = conditionsAndParameters.getSecond();
            queryBuf.append(conditionsAndParameters.getFirst());

            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
            for (int i = 0; i < params.size(); i++) {
                I_CmsQueryParameter param = params.get(i);
                param.insertIntoQuery(q, i + 1);
            }

            // execute
            List<CmsDAOLog> res = q.getResultList();
            for (CmsDAOLog log : res) {
                // get results
                entries.add(internalReadLogEntry(log));
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_LOG_READ_ENTRIES), e);
        }
        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public CmsProject readProject(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

        CmsProject project = null;
        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_1);
            q.setParameter(1, id.toString());
            try {
                CmsDAOProjects p = (CmsDAOProjects)q.getSingleResult();
                project = internalCreateProject(p);
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECT_WITH_ID_1, String.valueOf(id)));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsProject readProject(CmsDbContext dbc, String projectFqn) throws CmsDataAccessException {

        CmsProject project = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYNAME_2);

            q.setParameter(1, CmsOrganizationalUnit.getSimpleName(projectFqn));
            q.setParameter(2, CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));

            try {
                CmsDAOProjects p = (CmsDAOProjects)q.getSingleResult();
                project = internalCreateProject(p);
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECT_WITH_NAME_1, projectFqn));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public String readProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        String resName = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTRESOURCES_READ_2);

            // select resource from the database
            q.setParameter(1, projectId.toString());
            q.setParameter(2, resourcePath);

            try {
                resName = (String)q.getSingleResult();
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECTRESOURCE_1, resourcePath));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return resName;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public List<String> readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        List<String> result = new ArrayList<String>();

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTRESOURCES_READ_BY_ID_1);
            q.setParameter(1, project.getUuid().toString());
            List<String> res = q.getResultList();

            for (String r : res) {
                result.add(r);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjects(org.opencms.db.CmsDbContext, String)
     */
    public List<CmsProject> readProjects(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(DBC_ATTR_READ_PROJECT_FOR_RESOURCE) != null)) {
            dbc.getRequestContext().removeAttribute(DBC_ATTR_READ_PROJECT_FOR_RESOURCE);
            // TODO: this should get its own method in the interface
            return readProjectsForResource(dbc, ouFqn);
        }

        List<CmsProject> projects = new ArrayList<CmsProject>();

        try {
            // create the statement
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYOU_1);

            q.setParameter(1, CmsOrganizationalUnit.SEPARATOR + ouFqn + "%");
            List<CmsDAOProjects> res = q.getResultList();

            for (CmsDAOProjects p : res) {
                projects.add(internalCreateProject(p));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List<CmsProject> readProjectsForGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();

        try {
            // create the statement
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYGROUP_2);

            q.setParameter(1, group.getId().toString());
            q.setParameter(2, group.getId().toString());
            List<CmsDAOProjects> res = q.getResultList();

            for (CmsDAOProjects p : res) {
                projects.add(internalCreateProject(p));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForManagerGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List<CmsProject> readProjectsForManagerGroup(CmsDbContext dbc, CmsGroup group)
    throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();

        try {
            // create the statement
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYMANAGER_1);

            q.setParameter(1, group.getId().toString());
            List<CmsDAOProjects> res = q.getResultList();

            for (CmsDAOProjects p : res) {
                projects.add(internalCreateProject(p));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return (projects);
    }

    /**
     * Returns the projects of a given resource.<p>
     *
     * @param dbc the database context
     * @param rootPath the resource root path
     *
     * @return the projects of the resource, as a list of projects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    public List<CmsProject> readProjectsForResource(CmsDbContext dbc, String rootPath) throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYRESOURCE_1);
            q.setParameter(1, CmsVfsDriver.escapeDbWildcard(rootPath + "%"));
            List<CmsDAOProjects> res = q.getResultList();

            if (res.size() > 0) {
                projects.add(internalCreateProject(res.get(0)));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return projects;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForUser(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public List<CmsProject> readProjectsForUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();

        try {
            // create the statement
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_READ_BYUSER_1);

            q.setParameter(1, user.getId().toString());
            List<CmsDAOProjects> res = q.getResultList();

            for (CmsDAOProjects p : res) {
                projects.add(internalCreateProject(p));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public List<CmsPublishedResource> readPublishedResources(CmsDbContext dbc, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        List<CmsPublishedResource> publishedResources = new ArrayList<CmsPublishedResource>();

        try {
            Query q = m_sqlManager.createQuery(dbc, C_SELECT_PUBLISHED_RESOURCES);
            q.setParameter(1, publishHistoryId.toString());
            List<CmsDAOPublishHistory> res = q.getResultList();

            for (CmsDAOPublishHistory ph : res) {
                CmsUUID structureId = new CmsUUID(ph.getStructureId());
                CmsUUID resourceId = new CmsUUID(ph.getResourceId());
                String rootPath = ph.getResourcePath();
                int resourceState = ph.getResourceState();
                int resourceType = ph.getResourceType();
                int siblingCount = ph.getSiblingCount();
                int publishTag = ph.getPublishTag();

                // compose the resource state
                CmsResourceState state;
                if (resourceState == CmsPublishedResource.STATE_MOVED_SOURCE.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_SOURCE;
                } else if (resourceState == CmsPublishedResource.STATE_MOVED_DESTINATION.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_DESTINATION;
                } else {
                    state = CmsResourceState.valueOf(resourceState);
                }

                publishedResources.add(
                    new CmsPublishedResource(
                        structureId,
                        resourceId,
                        publishTag,
                        rootPath,
                        resourceType,
                        structureId.isNullUUID() ? false : CmsFolder.isFolderType(resourceType),
                        state,
                        siblingCount));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return publishedResources;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishJobInfoBean readPublishJob(CmsDbContext dbc, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        CmsPublishJobInfoBean result = null;
        try {
            Query q = m_sqlManager.createQuery(dbc, C_PUBLISHJOB_READ_JOB);
            q.setParameter(1, publishHistoryId.toString());
            try {
                CmsDAOPublishJobs pj = (CmsDAOPublishJobs)q.getSingleResult();
                result = createPublishJobInfoBean(pj);
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJobs(org.opencms.db.CmsDbContext, long, long)
     */
    public List<CmsPublishJobInfoBean> readPublishJobs(CmsDbContext dbc, long startTime, long endTime)
    throws CmsDataAccessException {

        List<CmsPublishJobInfoBean> result = null;
        try {
            Query q = m_sqlManager.createQuery(dbc, C_PUBLISHJOB_READ_JOBS_IN_TIMERANGE);
            q.setParameter(1, Long.valueOf(startTime));
            q.setParameter(2, Long.valueOf(endTime));
            List<CmsDAOPublishJobs> res = q.getResultList();

            result = new ArrayList<CmsPublishJobInfoBean>();
            for (CmsDAOPublishJobs pj : res) {
                result.add(createPublishJobInfoBean(pj));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishList readPublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        CmsPublishList publishList = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PUBLISHJOB_READ_PUBLISHLIST);
            q.setParameter(1, publishHistoryId.toString());
            try {
                CmsDAOPublishJobs pj = (CmsDAOPublishJobs)q.getSingleResult();
                publishList = internalDeserializePublishList(pj.getPublishList());
            } catch (NoResultException e) {
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        } catch (Exception e) {
            throw new CmsDataAccessException(
                Messages.get().container(Messages.ERR_PUBLISHLIST_DESERIALIZATION_FAILED_1, publishHistoryId),
                e);
        }

        return publishList;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishReportContents(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public byte[] readPublishReportContents(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        byte[] bytes = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PUBLISHJOB_READ_REPORT);
            q.setParameter(1, publishHistoryId.toString());
            try {
                CmsDAOPublishJobs pj = (CmsDAOPublishJobs)q.getSingleResult();
                bytes = pj.getPublishReport();
            } catch (NoResultException e) {
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }

        } catch (PersistenceException e) {
            LOG.error(C_PUBLISHJOB_READ_REPORT, e);
            bytes = Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_PUBLISHJOB_READ_REPORT).key().getBytes();
        }

        return bytes;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportPublishedResourceParameters(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public String readStaticExportPublishedResourceParameters(CmsDbContext dbc, String rfsName)
    throws CmsDataAccessException {

        String returnValue = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS);
            q.setParameter(1, rfsName);
            try {
                returnValue = (String)q.getSingleResult();
            } catch (NoResultException e) {
                // do nothing
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportResources(org.opencms.db.CmsDbContext, int, long)
     */
    public List<String> readStaticExportResources(CmsDbContext dbc, int parameterResources, long timestamp)
    throws CmsDataAccessException {

        List<String> returnValue = new ArrayList<String>();

        if (parameterResources == CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER) {
            timestamp = 0;
        }
        try {
            Query q = m_sqlManager.createQuery(dbc, C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS);
            q.setParameter(1, Integer.valueOf(parameterResources));
            q.setParameter(2, Long.valueOf(timestamp));
            List<String> res = q.getResultList();
            // add all resource names to the list of return values
            for (String path : res) {
                returnValue.add(path);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#setDriverManager(org.opencms.db.CmsDriverManager)
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#setSqlManager(org.opencms.db.CmsSqlManager)
     */
    public void setSqlManager(org.opencms.db.CmsSqlManager manager) {

        m_sqlManager = (CmsSqlManager)manager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#unmarkProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void unmarkProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // finally remove the project id form all resources
        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCES_UNMARK);
            // create the statement
            q.setParameter(1, project.getUuid().toString());
            List<CmsDAOOfflineResources> res = q.getResultList();

            for (CmsDAOOfflineResources r : res) {
                r.setProjectLastModified("00000000-0000-0000-0000-000000000000");
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeLocks(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void writeLocks(CmsDbContext dbc, List<CmsLock> locks) throws CmsDataAccessException {

        CmsDAOResourceLocks rl;

        try {
            Query q = m_sqlManager.createQuery(dbc, C_RESOURCE_LOCKS_DELETEALL);
            List<CmsDAOResourceLocks> res = q.getResultList();
            int deleted = 0;
            for (CmsDAOResourceLocks r : res) {
                m_sqlManager.remove(dbc, r);
                deleted++;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_CLEAR_LOCKS_1, new Integer(deleted)));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL :" + "Inserting rows in the CMS_RESOURCE_LOCKS table.");
            }
            Iterator<CmsLock> i = locks.iterator();
            int count = 0;
            while (i.hasNext()) {
                CmsLock lock = i.next();
                // only persist locks that should be written to the DB
                CmsLock sysLock = lock.getSystemLock();
                if (sysLock.isPersistent()) {
                    // persist system lock

                    rl = new CmsDAOResourceLocks();

                    rl.setResourcePath(sysLock.getResourceName());
                    rl.setUserId(sysLock.getUserId().toString());
                    rl.setProjectId(sysLock.getProjectId().toString());
                    rl.setLockType(sysLock.getType().hashCode());

                    m_sqlManager.persist(dbc, rl);
                    count++;
                }
                CmsLock editLock = lock.getEditionLock();
                if (editLock.isPersistent()) {
                    // persist edition lock
                    rl = new CmsDAOResourceLocks();

                    rl.setResourcePath(editLock.getResourceName());
                    rl.setUserId(editLock.getUserId().toString());
                    rl.setProjectId(editLock.getProjectId().toString());
                    rl.setLockType(editLock.getType().hashCode());

                    m_sqlManager.persist(dbc, rl);
                    count++;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_WRITE_LOCKS_1, new Integer(count)));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void writeProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(project.getDescription())) {
            project.setDescription(" ");
        }

        try {
            Query q = m_sqlManager.createQuery(dbc, C_PROJECTS_WRITE_6);
            q.setParameter(1, project.getUuid().toString());
            List<CmsDAOProjects> res = q.getResultList();

            for (CmsDAOProjects pr : res) {
                pr.setProjectDescription(project.getDescription());
                pr.setGroupId(project.getGroupId().toString());
                pr.setManagerGroupId(project.getManagerGroupId().toString());
                pr.setProjectFlags(project.getFlags());
                pr.setProjectType(project.getType().getMode());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void writePublishHistory(CmsDbContext dbc, CmsUUID publishId, CmsPublishedResource resource)
    throws CmsDataAccessException {

        try {
            CmsDAOPublishHistory ph = new CmsDAOPublishHistory();
            ph.setPublishTag(resource.getPublishTag());
            ph.setStructureId(resource.getStructureId().toString());
            ph.setResourceId(resource.getResourceId().toString());
            ph.setResourcePath(resource.getRootPath());
            ph.setResourceState(resource.getMovedState().getState());
            ph.setResourceType(resource.getType());
            ph.setHistoryId(publishId.toString());
            ph.setSiblingCount(resource.getSiblingCount());

            m_sqlManager.persist(dbc, ph);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void writePublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsDataAccessException {

        try {
            CmsDAOPublishJobs pj = m_sqlManager.find(
                dbc,
                CmsDAOPublishJobs.class,
                publishJob.getPublishHistoryId().toString());
            if (pj != null) {
                pj.setProjectId(publishJob.getProjectId().toString());
                pj.setProjectName(publishJob.getProjectName());
                pj.setUserId(publishJob.getUserId().toString());
                pj.setPublishLocale(publishJob.getLocale().toString());
                pj.setPublishFlags(publishJob.getFlags());
                pj.setResourceCount(publishJob.getSize());
                pj.setEnqueueTime(publishJob.getEnqueueTime());
                pj.setStartTime(publishJob.getStartTime());
                pj.setFinishTime(publishJob.getFinishTime());
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishReport(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[])
     */
    public void writePublishReport(CmsDbContext dbc, CmsUUID publishId, byte[] content) throws CmsDataAccessException {

        try {
            CmsDAOPublishJobs pj = m_sqlManager.find(dbc, CmsDAOPublishJobs.class, publishId.toString());
            if (pj != null) {
                pj.setPublishReport(content);
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeStaticExportPublishedResource(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.String, long)
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsDataAccessException {

        int returnValue = 0;
        // first check if a record with this resource name does already exist
        try {
            Query q = m_sqlManager.createQuery(dbc, C_STATICEXPORT_READ_PUBLISHED_RESOURCES);
            q.setParameter(1, resourceName);
            try {
                returnValue = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }

        // there was no entry found, so add it to the database
        if (returnValue == 0) {
            try {
                CmsDAOStaticExportLinks sel = new CmsDAOStaticExportLinks();

                sel.setLinkId(new CmsUUID().toString());
                sel.setLinkRfsPath(resourceName);
                sel.setLinkType(linkType);
                sel.setLinkParameter(linkParameter);
                sel.setLinkTimestamp(timestamp);

                m_sqlManager.persist(dbc, sel);
            } catch (PersistenceException e) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeUserPublishListEntries(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void writeUserPublishListEntries(CmsDbContext dbc, List<CmsUserPublishListEntry> publishListAdditions)
    throws CmsDataAccessException {

        try {
            for (CmsUserPublishListEntry entry : publishListAdditions) {
                Query delete = m_sqlManager.createQuery(dbc, "C_USER_PUBLISH_LIST_DELETE_2");
                delete.setParameter(1, entry.getUserId().toString());
                delete.setParameter(2, entry.getStructureId().toString());
                delete.executeUpdate();

                CmsDAOUserPublishListEntry newEntry = new CmsDAOUserPublishListEntry(
                    entry.getUserId(),
                    entry.getStructureId(),
                    entry.getDateChanged());
                m_sqlManager.getEntityManager(dbc).persist(newEntry);
            }
            m_sqlManager.getEntityManager(dbc).flush();

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE_1, e), e);
        }
    }

    /**
     * Creates a <code>CmsPublishJobInfoBean</code> from a result set.<p>
     *
     * @param pj the result set
     *
     * @return an initialized <code>CmsPublishJobInfoBean</code>
     */
    protected CmsPublishJobInfoBean createPublishJobInfoBean(CmsDAOPublishJobs pj) {

        return new CmsPublishJobInfoBean(
            new CmsUUID(pj.getHistoryId()),
            new CmsUUID(pj.getProjectId()),
            pj.getProjectName(),
            new CmsUUID(pj.getUserId()),
            pj.getPublishLocale(),
            pj.getPublishFlags(),
            pj.getResourceCount(),
            pj.getEnqueueTime(),
            pj.getStartTime(),
            pj.getFinishTime());
    }

    /**
     * Checks if the given resource (by id) is available in the online project,
     * if there exists a resource with a different path (a moved file), then the
     * online entry is moved to the right (new) location before publishing.<p>
     *
     * @param dbc the db context
     * @param onlineProject the online project
     * @param offlineResource the offline resource to check
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @return <code>true</code> if the resource has actually been moved
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected CmsResourceState fixMovedResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        CmsResource onlineResource;
        // check if the resource has been moved since last publishing
        try {
            onlineResource = m_driverManager.getVfsDriver(dbc).readResource(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getStructureId(),
                true);
            if (onlineResource.getRootPath().equals(offlineResource.getRootPath())) {
                // resource changed, not moved
                return offlineResource.getState();
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, resource new, not moved
            return offlineResource.getState();
        }

        // move the online resource to the new position
        m_driverManager.getVfsDriver(dbc).moveResource(
            dbc,
            onlineProject.getUuid(),
            onlineResource,
            offlineResource.getRootPath());

        try {
            // write the resource to the publish history
            m_driverManager.getProjectDriver(dbc).writePublishHistory(
                dbc,
                publishHistoryId,
                new CmsPublishedResource(onlineResource, publishTag, CmsPublishedResource.STATE_MOVED_SOURCE));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                        onlineResource.getRootPath()),
                    e);
            }
            throw e;
        }
        return offlineResource.getState().isDeleted()
        ? CmsResource.STATE_DELETED
        : CmsPublishedResource.STATE_MOVED_DESTINATION;
    }

    /**
     * Returns a SQL parameter string for the given data.<p>
     *
     * @param data the data
     *
     * @return the SQL parameter
     */
    protected String getParameterString(Collection<?> data) {

        StringBuffer conditions = new StringBuffer();
        conditions.append(BEGIN_CONDITION);
        Iterator<?> it = data.iterator();
        while (it.hasNext()) {
            it.next();
            conditions.append("?");
            if (it.hasNext()) {
                conditions.append(", ");
            }
        }
        conditions.append(END_CONDITION);
        return conditions.toString();
    }

    /**
     * Creates a new project from the current row of the given result set.<p>
     *
     * @param p the result set
     *
     * @return the new project
     */
    protected CmsProject internalCreateProject(CmsDAOProjects p) {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(p.getProjectOu());
        return new CmsProject(
            new CmsUUID(p.getProjectId()),
            ou + p.getProjectName(),
            p.getProjectDescription(),
            new CmsUUID(p.getUserId()),
            new CmsUUID(p.getGroupId()),
            new CmsUUID(p.getManagerGroupId()),
            p.getProjectFlags(),
            p.getDateCreated(),
            CmsProject.CmsProjectType.valueOf(p.getProjectType()));
    }

    /**
     * Builds a publish list from serialized data.<p>
     *
     * @param bytes the byte array containing the serailized data for the publish list
     * @return the initialized publish list
     *
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if deserialization fails
     */
    protected CmsPublishList internalDeserializePublishList(byte[] bytes) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return (CmsPublishList)oin.readObject();
    }

    /**
     * Creates a new {@link CmsLogEntry} object from the given result set entry.<p>
     *
     * @param daoLog the result set
     *
     * @return the new {@link CmsLogEntry} object
     */
    protected CmsLogEntry internalReadLogEntry(CmsDAOLog daoLog) {

        CmsUUID userId = new CmsUUID(daoLog.getUserId());
        long date = daoLog.getLogDate();
        CmsUUID structureId = new CmsUUID(daoLog.getStructureId());
        CmsLogEntryType type = CmsLogEntryType.valueOf(daoLog.getLogType());
        String[] data = CmsStringUtil.splitAsArray(daoLog.getLogData(), '|');
        return new CmsLogEntry(userId, date, structureId, type, data);
    }

    /**
     * Resets the state to UNCHANGED for a specified resource.<p>
     *
     * @param dbc the current database context
     * @param resource the Cms resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalResetResourceState(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        try {
            // reset the resource state
            resource.setState(CmsResource.STATE_UNCHANGED);
            m_driverManager.getVfsDriver(dbc).writeResourceState(
                dbc,
                dbc.currentProject(),
                resource,
                CmsDriverManager.UPDATE_ALL,
                true);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_ERROR_RESETTING_RESOURCE_STATE_1,
                        resource.getRootPath()),
                    e);
            }
            throw e;
        }
    }

    /**
     * Serialize publish list to write it as byte array to the database.<p>
     *
     * @param publishList the publish list
     * @return byte array containing the publish list data
     * @throws IOException if something goes wrong
     */
    protected byte[] internalSerializePublishList(CmsPublishList publishList) throws IOException {

        // serialize the publish list
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(publishList);
        oout.close();
        return bout.toByteArray();
    }

    /**
     * Writes the needed history entries.<p>
     *
     * @param dbc the current database context
     * @param resource the offline resource
     * @param state the state to store in the publish history entry
     * @param properties the offline properties
     * @param publishHistoryId the current publish process id
     * @param publishTag the current publish process tag
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteHistory(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceState state,
        List<CmsProperty> properties,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        try {
            if (OpenCms.getSystemInfo().isHistoryEnabled()) {
                // write the resource to the historical archive
                if (properties == null) {
                    properties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        resource);
                }
                m_driverManager.getHistoryDriver(dbc).writeResource(dbc, resource, properties, publishTag);
            }
            // write the resource to the publish history
            m_driverManager.getProjectDriver().writePublishHistory(
                dbc,
                publishHistoryId,
                new CmsPublishedResource(resource, publishTag, state));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, resource.getRootPath()),
                    e);
            }
            throw e;
        }
    }

    /**
     * Build the whole WHERE SQL statement part for the given log entry filter.<p>
     *
     * @param filter the filter
     *
     * @return a pair containing both the SQL and the parameters for it
     */
    protected CmsPair<String, List<I_CmsQueryParameter>> prepareLogConditions(CmsLogFilter filter) {

        List<I_CmsQueryParameter> params = new ArrayList<I_CmsQueryParameter>();
        StringBuffer conditions = new StringBuffer();

        // user id filter
        if (filter.getUserId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_USER_ID));
            params.add(new CmsQueryStringParameter(filter.getUserId().toString()));
            conditions.append(END_CONDITION);
        }

        // resource id filter
        if (filter.getStructureId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_RESOURCE_ID));
            params.add(new CmsQueryStringParameter(filter.getStructureId().toString()));
            conditions.append(END_CONDITION);
        }

        // date from filter
        if (filter.getDateFrom() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_DATE_FROM));
            params.add(new CmsQueryLongParameter(filter.getDateFrom()));
            conditions.append(END_CONDITION);
        }

        // date to filter
        if (filter.getDateTo() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_DATE_TO));
            params.add(new CmsQueryLongParameter(filter.getDateTo()));
            conditions.append(END_CONDITION);
        }

        // include type filter
        Set<CmsLogEntryType> includeTypes = filter.getIncludeTypes();
        if (!includeTypes.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_INCLUDE_TYPE));
            conditions.append(BEGIN_CONDITION);
            Iterator<CmsLogEntryType> it = includeTypes.iterator();
            while (it.hasNext()) {
                CmsLogEntryType type = it.next();
                conditions.append("?");
                params.add(new CmsQueryIntParameter(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }

        // exclude type filter
        Set<CmsLogEntryType> excludeTypes = filter.getExcludeTypes();
        if (!excludeTypes.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_LOG_FILTER_EXCLUDE_TYPE));
            conditions.append(BEGIN_CONDITION);
            Iterator<CmsLogEntryType> it = excludeTypes.iterator();
            while (it.hasNext()) {
                CmsLogEntryType type = it.next();
                conditions.append("?");
                params.add(new CmsQueryIntParameter(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }
        return CmsPair.create(conditions.toString(), params);
    }

    /**
     * Publishes a changed file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedResourceIds contains the UUIDs of already published content records
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishChangedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedResourceIds,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        CmsResource onlineResource = null;
        boolean needToUpdateContent = true;
        boolean existsOnline = m_driverManager.getVfsDriver(dbc).validateStructureIdExists(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            offlineResource.getStructureId());
        CmsResourceState resourceState = existsOnline
        ? fixMovedResource(dbc, onlineProject, offlineResource, publishHistoryId, publishTag)
        : offlineResource.getState();
        try {
            // reset the labeled link flag before writing the online file
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);

            if (existsOnline) {
                // read the file header online
                onlineResource = m_driverManager.getVfsDriver(dbc).readResource(
                    dbc,
                    onlineProject.getUuid(),
                    offlineResource.getStructureId(),
                    false);
                needToUpdateContent = (onlineResource.getDateContent() < offlineResource.getDateContent());
                // delete the properties online
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // if the offline file has a resource ID different from the online file
                // (probably because a deleted file was replaced by a new file with the
                // same name), the properties mapped to the "old" resource ID have to be
                // deleted also offline. if this is the case, the online and offline structure
                // ID's do match, but the resource ID's are different. structure IDs are reused
                // to prevent orphan structure records in the online project.
                if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                    List<CmsProperty> offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        onlineResource);
                    if (offlineProperties.size() > 0) {
                        for (int i = 0; i < offlineProperties.size(); i++) {
                            CmsProperty property = offlineProperties.get(i);
                            property.setStructureValue(null);
                            property.setResourceValue(CmsProperty.DELETE_VALUE);
                        }
                        m_driverManager.getVfsDriver(dbc).writePropertyObjects(
                            dbc,
                            dbc.currentProject(),
                            onlineResource,
                            offlineProperties);
                    }
                }
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }

        CmsFile newFile;
        try {
            // publish the file content
            newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource,
                publishedResourceIds,
                needToUpdateContent,
                publishTag);

        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        List<CmsProperty> offlineProperties;
        try {
            // write the properties online
            offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                dbc,
                dbc.currentProject(),
                offlineResource);
            CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
            m_driverManager.getVfsDriver(dbc).writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
            }
            throw e;
        }

        try {
            // write the ACL online
            m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                dbc,
                dbc.currentProject(),
                onlineProject,
                newFile.getResourceId(),
                offlineResource.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
            }
            throw e;
        }

        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(newFile.getContents());
        internalWriteHistory(dbc, offlineFile, resourceState, offlineProperties, publishHistoryId, publishTag);

        m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineResource);
    }

    /**
     * Publishes a deleted file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishDeletedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        CmsResourceState resourceState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            publishTag);

        boolean existsOnline = m_driverManager.getVfsDriver(dbc).validateStructureIdExists(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            offlineResource.getStructureId());
        CmsResource onlineResource = null;
        if (existsOnline) {
            try {
                // read the file header online
                onlineResource = m_driverManager.getVfsDriver(dbc).readResource(
                    dbc,
                    onlineProject.getUuid(),
                    offlineResource.getStructureId(),
                    true);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_READING_RESOURCE_1, offlineResource.getRootPath()),
                        e);
                }
                throw e;
            }
        }
        if (offlineResource.isLabeled() && !m_driverManager.labelResource(dbc, offlineResource, null, 2)) {
            // update the resource flags to "unlabeled" of the siblings of the offline resource
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);
        }

        // write history before deleting
        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(
            m_driverManager.getVfsDriver(dbc).readContent(
                dbc,
                dbc.currentProject().getUuid(),
                offlineFile.getResourceId()));
        internalWriteHistory(dbc, offlineFile, resourceState, null, publishHistoryId, publishTag);

        int propertyDeleteOption = -1;
        try {
            // delete the properties online and offline
            if (offlineResource.getSiblingCount() > 1) {
                // there are other siblings- delete only structure property values and keep the resource property values
                propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES;
            } else {
                // there are no other siblings- delete both the structure and resource property values
                propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
            }

            if (existsOnline) {
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineResource,
                    propertyDeleteOption);
            }
            m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                dbc,
                dbc.currentProject().getUuid(),
                offlineResource,
                propertyDeleteOption);

            // if the offline file has a resource ID different from the online file
            // (probably because a (deleted) file was replaced by a new file with the
            // same name), the properties with the "old" resource ID have to be
            // deleted also offline
            if (existsOnline
                && (onlineResource != null)
                && !onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        try {
            // remove the file online and offline
            m_driverManager.getVfsDriver(dbc).removeFile(dbc, dbc.currentProject().getUuid(), offlineResource);
            if (existsOnline && (onlineResource != null)) {
                m_driverManager.getVfsDriver(dbc).removeFile(dbc, onlineProject.getUuid(), onlineResource);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        // delete the ACL online and offline
        try {
            if (existsOnline && (onlineResource != null) && (onlineResource.getSiblingCount() == 1)) {
                // only if no siblings left
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    onlineProject,
                    onlineResource.getResourceId());
            }
            if (offlineResource.getSiblingCount() == 1) {
                // only if no siblings left
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    offlineResource.getResourceId());
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, offlineResource.toString()), e);
            }
            throw e;
        }

        try {
            // delete relations online and offline
            m_driverManager.getVfsDriver(dbc).deleteRelations(
                dbc,
                onlineProject.getUuid(),
                offlineResource,
                CmsRelationFilter.TARGETS);
            m_driverManager.getVfsDriver(dbc).deleteRelations(
                dbc,
                dbc.currentProject().getUuid(),
                offlineResource,
                CmsRelationFilter.TARGETS);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_REMOVING_RELATIONS_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }

        if (OpenCms.getSubscriptionManager().isEnabled()) {
            try {
                // delete visited information for resource from log
                CmsVisitEntryFilter filter = CmsVisitEntryFilter.ALL.filterResource(offlineResource.getStructureId());
                m_driverManager.getSubscriptionDriver().deleteVisits(
                    dbc,
                    OpenCms.getSubscriptionManager().getPoolName(),
                    filter);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_VISITEDLOG_1, offlineResource.toString()),
                        e);
                }
                throw e;
            }

            try {
                // mark the subscribed resource as deleted
                /* changed to subscription driver */
                //                m_driverManager.getUserDriver(dbc).setSubscribedResourceAsDeleted(
                //                    dbc,
                //                    OpenCms.getSubscriptionManager().getPoolName(),
                //                    offlineResource);
                m_driverManager.getSubscriptionDriver().setSubscribedResourceAsDeleted(
                    dbc,
                    OpenCms.getSubscriptionManager().getPoolName(),
                    offlineResource);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_REMOVING_SUBSCRIPTIONS_1,
                            offlineResource.toString()),
                        e);
                }
                throw e;
            }
        }
    }

    /**
     * Publishes a new file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedContentIds contains the UUIDs of already published content records
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishNewFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedContentIds,
        CmsUUID publishHistoryId,
        int publishTag) throws CmsDataAccessException {

        CmsResourceState resourceState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            publishTag);

        CmsFile newFile;
        try {
            // reset the labeled link flag before writing the online file
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);

            // publish the file content
            newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource,
                publishedContentIds,
                true,
                publishTag);

        } catch (CmsVfsResourceAlreadyExistsException e) {
            try {
                // remove the existing file and ensure that it's content is written
                // in any case by removing it's resource ID from the set of published resource IDs
                m_driverManager.getVfsDriver(dbc).removeFile(dbc, onlineProject.getUuid(), offlineResource);
                publishedContentIds.remove(offlineResource.getResourceId());
                newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                    dbc,
                    dbc.currentProject(),
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    true,
                    publishTag);
            } catch (CmsDataAccessException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_RESOURCE_1,
                            offlineResource.getRootPath()),
                        e);
                }
                throw e1;
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        List<CmsProperty> offlineProperties;
        try {
            // write the properties online
            offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                dbc,
                dbc.currentProject(),
                offlineResource);
            CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
            m_driverManager.getVfsDriver(dbc).writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
            }

            throw e;
        }

        try {
            // write the ACL online
            m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource.getResourceId(),
                newFile.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
            }
            throw e;
        }

        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(newFile.getContents());
        internalWriteHistory(dbc, offlineFile, resourceState, offlineProperties, publishHistoryId, publishTag);

        m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineResource);
    }
}
