/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEPublish.java,v $
 * Date   : $Date: 2009/11/17 12:29:58 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationPublishValidator;
import org.opencms.relations.CmsRelationValidatorInfoEntry;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * ADE publishing features.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 7.9.3
 */
public class CmsADEPublish {

    /**
     * Just for passing around resources and their related together but not mixed up.<p>
     */
    private class ResourcesAndRelated {

        /** The related resources. */
        private Set<CmsResource> m_relatedResources = new HashSet<CmsResource>();

        /** The resources. */
        private Set<CmsResource> m_resources = new HashSet<CmsResource>();

        /**
         * Constructor.<p>
         */
        public ResourcesAndRelated() {

            // empty
        }

        /**
         * Checks if the given resource is present in at least one of the sets.<p>
         * 
         * @param resource the resource to test
         * 
         * @return <code>true</code> if the given resource is present in at least one of the sets
         */
        public boolean contains(CmsResource resource) {

            return m_resources.contains(resource) || m_relatedResources.contains(resource);
        }

        /**
         * Returns the related resources.<p>
         *
         * @return the related resources
         */
        public Set<CmsResource> getRelatedResources() {

            return m_relatedResources;
        }

        /**
         * Returns the resources.<p>
         *
         * @return the resources
         */
        public Set<CmsResource> getResources() {

            return m_resources;
        }
    }

    /** The number of day groups. */
    protected static final int GROUP_DAYS_NUMBER = 3;

    /** The gap between session groups. */
    protected static final int GROUP_SESSIONS_GAP = 8 * 60 * 60 * 1000;

    /** The number of session groups. */
    protected static final int GROUP_SESSIONS_NUMBER = 2;

    /** Formatted path length. */
    protected static final int PATH_LENGTH = 50;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEPublish.class);

    /** The current cms context. */
    private final CmsObject m_cms;

    /** The current locale. */
    private final Locale m_locale;

    /** The options. */
    private final CmsPublishOptions m_options;

    /** The user's resource publish list. */
    private ResourcesAndRelated m_resourceList;

    /**
     * Constructor.<p>
     * 
     * @param cms the current cms context
     */
    public CmsADEPublish(CmsObject cms) {

        m_cms = cms;
        m_locale = m_cms.getRequestContext().getLocale();
        m_options = new CmsPublishOptions();
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     * 
     * @param pubResources list of resources to be published
     * 
     * @return a list of resources that would produce broken links when published 
     */
    public List<CmsPublishResourceBean> getBrokenResources(List<CmsResource> pubResources) {

        List<CmsPublishResourceBean> resources = new ArrayList<CmsPublishResourceBean>();

        CmsPublishList publishList;
        try {
            publishList = OpenCms.getPublishManager().getPublishList(
                m_cms,
                pubResources,
                m_options.isIncludeSiblings(),
                true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return resources;
        }

        CmsRelationPublishValidator validator = new CmsRelationPublishValidator(m_cms, publishList);
        for (String resourceName : validator.keySet()) {
            CmsRelationValidatorInfoEntry infoEntry = validator.getInfoEntry(resourceName);
            try {
                CmsResource resource = m_cms.readResource(
                    m_cms.getRequestContext().removeSiteRoot(resourceName),
                    CmsResourceFilter.ALL);
                if (resource.getState().isDeleted()) {
                    for (CmsRelation relation : infoEntry.getRelations()) {
                        try {
                            CmsResource theResource = relation.getSource(m_cms, CmsResourceFilter.ALL);
                            CmsPublishResourceInfoBean info = new CmsPublishResourceInfoBean(
                                Messages.get().getBundle(m_locale).key(Messages.GUI_BROKEN_LINK_ONLINE_0),
                                CmsPublishResourceInfoBean.Type.BROKENLINK);
                            CmsPublishResourceBean pubRes = resourceToBean(
                                theResource,
                                null,
                                false,
                                Collections.singletonList(resourceToBean(resource, info, false, null)));
                            resources.add(pubRes);
                        } catch (CmsException e) {
                            // should never happen
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else {
                    try {
                        List<CmsPublishResourceBean> related = new ArrayList<CmsPublishResourceBean>();
                        for (CmsRelation relation : infoEntry.getRelations()) {
                            try {
                                CmsResource theResource = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                                CmsPublishResourceBean pubRes = resourceToBean(theResource, null, false, null);
                                related.add(pubRes);
                            } catch (CmsException e) {
                                // should never happen
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                        CmsPublishResourceInfoBean info = new CmsPublishResourceInfoBean(
                            Messages.get().getBundle(m_locale).key(Messages.GUI_RESOURCE_MISSING_ONLINE_0),
                            CmsPublishResourceInfoBean.Type.MISSING);
                        CmsPublishResourceBean pubRes = resourceToBean(resource, info, false, related);
                        resources.add(pubRes);
                    } catch (Exception e) {
                        // should never happen
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        return resources;
    }

    /**
     * Returns the current user's manageable projects.<p>
     * 
     * @return the current user's manageable projects
     */
    public List<CmsProjectBean> getManageableProjects() {

        List<CmsProjectBean> manProjs = new ArrayList<CmsProjectBean>();

        List<CmsProject> projects;
        try {
            projects = OpenCms.getOrgUnitManager().getAllManageableProjects(m_cms, "", true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return manProjs;
        }

        for (CmsProject project : projects) {
            CmsProjectBean manProj = new CmsProjectBean(project.getUuid(), getOuAwareName(project.getName()));
            manProjs.add(manProj);
        }

        return manProjs;
    }

    /**
     * Returns the options.<p>
     *
     * @return the options
     */
    public CmsPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Returns the list of publish groups with resources that can be published.<p>
     * 
     * @return the list of publish groups with resources that can be published
     */
    public List<CmsPublishGroupBean> getPublishGroups() {

        // first look for already published resources
        Set<CmsResource> published = getAlreadyPublishedResources();

        // then for resources without permission
        Set<CmsResource> exclude = new HashSet<CmsResource>(published);

        ResourcesAndRelated permissions = getResourcesWithoutPermissions(exclude);

        // and finally for locked resources
        exclude.addAll(permissions.getResources());
        exclude.addAll(permissions.getRelatedResources());

        ResourcesAndRelated locked = getBlockingLockedResources(exclude);

        // all direct resources that can not be published
        exclude.clear();
        exclude.addAll(published);
        exclude.addAll(permissions.getResources());
        exclude.addAll(locked.getResources());

        // update the publish list
        ResourcesAndRelated pubResources = new ResourcesAndRelated();
        pubResources.getResources().addAll(getPublishResources().getResources());
        pubResources.getResources().removeAll(exclude);
        pubResources.getRelatedResources().addAll(getPublishResources().getRelatedResources());
        pubResources.getRelatedResources().removeAll(permissions.getRelatedResources());
        pubResources.getRelatedResources().removeAll(locked.getRelatedResources());

        List<CmsResource> sortedResources = new ArrayList<CmsResource>(getPublishResources().getResources());
        Collections.sort(sortedResources, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);

        if (sortedResources.isEmpty()) {
            // nothing to do
            return new ArrayList<CmsPublishGroupBean>();
        }

        // the resources the user can really publish
        Set<CmsResource> allPubRes = new HashSet<CmsResource>(pubResources.getRelatedResources());
        allPubRes.addAll(pubResources.getResources());

        List<CmsResource> pubList = new ArrayList<CmsResource>();
        try {
            pubList = OpenCms.getPublishManager().getUsersPubList(m_cms);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }

        int sessions = GROUP_SESSIONS_NUMBER;
        int days = GROUP_DAYS_NUMBER;
        List<CmsPublishGroupBean> groups = new ArrayList<CmsPublishGroupBean>();
        List<CmsPublishResourceBean> resources = new ArrayList<CmsPublishResourceBean>();
        long groupDate = sortedResources.get(0).getDateLastModified(); // we checked earlier that there is at least one resource
        ListIterator<CmsResource> itResources = sortedResources.listIterator();
        while (itResources.hasNext()) {
            CmsResource resource = itResources.next();
            try {
                List<CmsPublishResourceBean> related = getRelatedResources(
                    resource,
                    allPubRes,
                    published,
                    permissions,
                    locked);
                CmsPublishResourceInfoBean info = getResourceInfo(resource, published, permissions, locked);
                CmsPublishResourceBean pubResource = resourceToBean(resource, info, pubList.contains(resource), related);
                resources.add(pubResource);
            } catch (Exception e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
            boolean newGroup = !itResources.hasNext();
            if (!newGroup) {
                CmsResource nextRes = itResources.next();
                itResources.previous(); // go back to continue in the right order
                if (sessions > 0) {
                    // check the difference is not greater than x hours
                    newGroup = (resource.getDateLastModified() - nextRes.getDateLastModified() > GROUP_SESSIONS_GAP);
                } else if (days > 0) {
                    // check they are not in the same day
                    Calendar oldCalendar = Calendar.getInstance();
                    oldCalendar.setTimeInMillis(resource.getDateLastModified());
                    Calendar newCalendar = Calendar.getInstance();
                    newCalendar.setTimeInMillis(nextRes.getDateLastModified());

                    newGroup = (oldCalendar.get(Calendar.DAY_OF_MONTH) == newCalendar.get(Calendar.DAY_OF_MONTH));
                    newGroup &= (oldCalendar.get(Calendar.MONTH) == newCalendar.get(Calendar.MONTH));
                    newGroup &= (oldCalendar.get(Calendar.YEAR) == newCalendar.get(Calendar.YEAR));
                }
            }
            if (newGroup) {
                try {
                    String groupName;
                    if (sessions > 0) {
                        if (resources.size() > 1) {
                            groupName = Messages.get().getBundle(m_locale).key(
                                Messages.GUI_GROUPNAME_SESSION_2,
                                new Date(resource.getDateLastModified()),
                                new Date(groupDate));
                        } else {
                            groupName = Messages.get().getBundle(m_locale).key(
                                Messages.GUI_GROUPNAME_SESSION_1,
                                new Date(groupDate));
                        }
                        sessions--;
                    } else if (days > 0) {
                        groupName = Messages.get().getBundle(m_locale).key(
                            Messages.GUI_GROUPNAME_DAY_1,
                            new Date(groupDate));
                        days--;
                    } else {
                        groupName = Messages.get().getBundle(m_locale).key(Messages.GUI_GROUPNAME_EVERYTHING_ELSE_0);
                    }
                    if (itResources.hasNext()) {
                        CmsResource nextRes = itResources.next();
                        itResources.previous(); // go back to continue in the right order
                        groupDate = nextRes.getDateLastModified();
                    }
                    CmsPublishGroupBean group = new CmsPublishGroupBean(groupName, resources);
                    groups.add(group);
                    resources = new ArrayList<CmsPublishResourceBean>();
                } catch (Exception e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return groups;
    }

    /**
     * Publishes the given list of resources.<p>
     * 
     * @param resources list of resources to publish
     * 
     * @throws CmsException if something goes wrong
     */
    public void publishResources(List<CmsResource> resources) throws CmsException {

        I_CmsReport report = new CmsShellReport(m_locale);
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(
            m_cms,
            resources,
            m_options.isIncludeSiblings(),
            false);
        OpenCms.getPublishManager().publishProject(m_cms, report, publishList);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param idsToRemove list of structure ids identifying the resources to be removed
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourcesFromPublishList(Collection<CmsUUID> idsToRemove) throws CmsException {

        OpenCms.getPublishManager().removeResourceFromUsersPubList(m_cms, idsToRemove);
    }

    /**
     * Returns already published resources.<p>
     * 
     * @return already published resources
     */
    protected Set<CmsResource> getAlreadyPublishedResources() {

        Set<CmsResource> resources = new HashSet<CmsResource>();
        for (CmsResource resource : getPublishResources().getResources()) {
            // we are interested just in changed resources
            if (!resource.getState().isUnchanged()) {
                continue;
            }
            resources.add(resource);
        }
        return resources;
    }

    /**
     * Returns locked resources that do not belong to the current user.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the locked and related resources
     * 
     * @see org.opencms.workplace.commons.CmsLock#getBlockingLockedResources
     */
    protected ResourcesAndRelated getBlockingLockedResources(Set<CmsResource> exclude) {

        CmsUser user = m_cms.getRequestContext().currentUser();
        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
        blockingFilter = blockingFilter.filterNotLockableByUser(user);

        ResourcesAndRelated result = new ResourcesAndRelated();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getResources().addAll(m_cms.getLockedResources(resource, blockingFilter));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                result.getRelatedResources().addAll(m_cms.getLockedResources(resource, blockingFilter));
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     * 
     * @param name the fully qualified name to check
     * 
     * @return the simple name if the ou is the same as the current user's ou
     */
    protected String getOuAwareName(String name) {

        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(m_cms.getRequestContext().currentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * Returns the resources stored in the user's publish list.<p>
     * 
     * @return the resources stored in the user's publish list
     */
    protected ResourcesAndRelated getPublishResources() {

        if (m_resourceList != null) {
            return m_resourceList;
        }
        m_resourceList = new ResourcesAndRelated();
        try {
            if (m_options.getProjectId() == null) {
                // get the users publish list
                m_resourceList.getResources().addAll(OpenCms.getPublishManager().getUsersPubList(m_cms));
            } else {
                CmsProject project = m_cms.getRequestContext().currentProject();
                try {
                    project = m_cms.readProject(m_options.getProjectId());
                } catch (Exception e) {
                    // can happen if the cached project was deleted
                    // so ignore and use current project
                }
                // get the project publish list
                CmsProject originalProject = m_cms.getRequestContext().currentProject();
                try {
                    m_cms.getRequestContext().setCurrentProject(project);
                    m_resourceList.getResources().addAll(
                        OpenCms.getPublishManager().getPublishList(m_cms).getAllResources());
                } finally {
                    m_cms.getRequestContext().setCurrentProject(originalProject);
                }
            }
        } catch (CmsException e) {
            // error reading the publish list, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return m_resourceList;
        }
        if (m_options.isIncludeSiblings()) {
            for (CmsResource resource : new HashSet<CmsResource>(m_resourceList.getResources())) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    m_resourceList.getResources().addAll(
                        m_cms.readSiblings(m_cms.getSitePath(resource), CmsResourceFilter.ALL_MODIFIED));
                } catch (CmsException e) {
                    // error reading resource siblings, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        if (m_options.isIncludeRelated()) {
            for (CmsResource resource : m_resourceList.getResources()) {
                // we are interested just in changed resources
                if (resource.getState().isUnchanged()) {
                    continue;
                }
                try {
                    // get and iterate over all related resources
                    for (CmsRelation relation : m_cms.getRelationsForResource(
                        resource,
                        CmsRelationFilter.TARGETS.filterStrong().filterIncludeChildren())) {

                        CmsResource target = null;
                        try {
                            target = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                        } catch (CmsException e) {
                            // error reading a resource, should usually never happen
                            if (LOG.isErrorEnabled()) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                            continue;
                        }
                        // we are interested just in changed resources
                        if (target.getState().isUnchanged()) {
                            continue;
                        }
                        // if already selected
                        if (m_resourceList.contains(target)) {
                            continue;
                        }
                        m_resourceList.getRelatedResources().add(target);
                    }
                } catch (CmsException e) {
                    // error reading a resource relations, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
            }
        }
        return m_resourceList;
    }

    /**
     * Returns a string with a list of related resources in the publish list.<p>
     * 
     * @param resource the resource to use
     * @param resources the resources the user can really publish
     * @param published the already published resources
     * @param permissions the resource the current user does not have publish permissions for
     * @param locked the locked resources
     * 
     * @return a string with a list of related resources in the publish list, or <code>null</code> if none
     */
    protected List<CmsPublishResourceBean> getRelatedResources(
        CmsResource resource,
        Set<CmsResource> resources,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        List<CmsPublishResourceBean> relatedResources = new ArrayList<CmsPublishResourceBean>();
        try {
            // get and iterate over all related resources
            for (CmsRelation relation : m_cms.getRelationsForResource(
                resource,
                CmsRelationFilter.TARGETS.filterStrong().filterIncludeChildren())) {

                CmsResource target = null;
                try {
                    target = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                // see if the source is a resource to be published
                CmsPublishResourceInfoBean info;
                if (resources.contains(target)) {
                    info = getResourceInfo(resource, published, permissions, locked);
                } else if (!target.getState().isUnchanged()) {
                    // a modified related resource can not be published
                    info = new CmsPublishResourceInfoBean(Messages.get().getBundle(m_locale).key(
                        Messages.GUI_RELATED_RESOURCE_CAN_NOT_BE_PUBLISHED_0), CmsPublishResourceInfoBean.Type.RELATED);
                } else {
                    continue;
                }
                CmsPublishResourceBean relatedResource = resourceToBean(target, info, false, null);
                relatedResources.add(relatedResource);
            }
        } catch (CmsException e) {
            // error reading a resource relations, should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return relatedResources;
    }

    /**
     * Returns the additional info for the given resource.<p>
     * 
     * @param resource the resource to use
     * @param published the already published resources
     * @param permissions the resource the current user does not have publish permissions for
     * @param locked the locked resources
     * 
     * @return the additional info for the given resource
     */
    protected CmsPublishResourceInfoBean getResourceInfo(
        CmsResource resource,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        String info = null;
        CmsPublishResourceInfoBean.Type infoType = null;
        try {
            if (published.contains(resource)) {
                // TODO: get the real publish data
                String publishUser = getOuAwareName(m_cms.readUser(resource.getUserLastModified()).getName());
                Date publishDate = new Date(resource.getDateLastModified());
                info = Messages.get().getBundle(m_locale).key(
                    Messages.GUI_RESOURCE_PUBLISHED_BY_2,
                    publishUser,
                    publishDate);
                infoType = CmsPublishResourceInfoBean.Type.PUBLISHED;
            } else if (permissions.contains(resource)) {
                info = Messages.get().getBundle(m_locale).key(Messages.GUI_RESOURCE_NOT_ENOUGH_PERMISSIONS_0);
                infoType = CmsPublishResourceInfoBean.Type.PERMISSIONS;
            } else if (locked.contains(resource)) {
                CmsLock lock = m_cms.getLock(resource);
                info = Messages.get().getBundle(m_locale).key(
                    Messages.GUI_RESOURCE_LOCKED_BY_2,
                    getOuAwareName(m_cms.readUser(lock.getUserId()).getName()),
                    getOuAwareName(lock.getProject().getName()));
                infoType = CmsPublishResourceInfoBean.Type.LOCKED;
            }
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        return infoType == null ? null : new CmsPublishResourceInfoBean(info, infoType);
    }

    /**
     * Formats the given resource path depending on the site root.<p>
     * 
     * @param rootPath the resource path to format
     * @param siteRoot the site root
     * 
     * @return the formatted resource path
     */
    protected String getResourceName(String rootPath, String siteRoot) {

        if (rootPath.startsWith(siteRoot)) {
            // same site
            rootPath = rootPath.substring(siteRoot.length());
            rootPath = CmsStringUtil.formatResourceName(rootPath, PATH_LENGTH);
        } else {
            // other site
            String site = OpenCms.getSiteManager().getSiteRoot(rootPath);
            String siteName = site;
            if (site != null) {
                rootPath = rootPath.substring(site.length());
                siteName = OpenCms.getSiteManager().getSiteForSiteRoot(site).getTitle();
            } else {
                siteName = "/";
            }
            rootPath = CmsStringUtil.formatResourceName(rootPath, PATH_LENGTH);
            rootPath = org.opencms.workplace.commons.Messages.get().getBundle(m_locale).key(
                org.opencms.workplace.commons.Messages.GUI_PUBLISH_SITE_RELATION_2,
                new Object[] {siteName, rootPath});
        }
        return rootPath;
    }

    /**
     * Returns the sublist of the publish list with resources without publish permissions.<p>
     * 
     * @param exclude the resources to exclude
     * 
     * @return the list with resources without publish permissions
     */
    protected ResourcesAndRelated getResourcesWithoutPermissions(Set<CmsResource> exclude) {

        Set<CmsUUID> projectIds = new HashSet<CmsUUID>();
        try {
            for (CmsProject project : OpenCms.getOrgUnitManager().getAllManageableProjects(m_cms, "", true)) {
                projectIds.add(project.getUuid());
            }
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }

        ResourcesAndRelated result = new ResourcesAndRelated();
        for (CmsResource resource : getPublishResources().getResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!projectIds.contains(resource.getProjectLastModified())
                    && !m_cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the permissions, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        for (CmsResource resource : getPublishResources().getRelatedResources()) {
            // skip already blocking resources
            if (exclude.contains(resource)) {
                continue;
            }
            try {
                if (!m_cms.hasPermissions(resource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    result.getRelatedResources().add(resource);
                }
            } catch (Exception e) {
                // error reading the resource list, should usually never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Creates a publish resource bean instance from the given parameters.<p>
     * 
     * @param resource the resource
     * @param info the publish information, if any
     * @param removable if removable
     * @param related the list of related resources
     * 
     * @return the publish resource bean
     */
    protected CmsPublishResourceBean resourceToBean(
        CmsResource resource,
        CmsPublishResourceInfoBean info,
        boolean removable,
        List<CmsPublishResourceBean> related) {

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        CmsPublishResourceBean pubResource = new CmsPublishResourceBean(
            resource.getStructureId(),
            CmsStringUtil.formatResourceName(resUtil.getFullPath(), PATH_LENGTH),
            resUtil.getTitle(),
            CmsWorkplace.getResourceUri(resUtil.getIconPathExplorer()),
            "" + resUtil.getStateAbbreviation(),
            removable,
            info,
            related);
        return pubResource;
    }
}
