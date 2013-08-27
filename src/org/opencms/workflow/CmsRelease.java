/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.ade.publish.CmsPublish;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A subclass of CmsPublish which builds resource lists for resources which can be released, rather than published.<p>
 */
public class CmsRelease extends CmsPublish {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsPublish.class);

    /** Computing map which keeps track of which projects are workflow projects. */
    private LoadingCache<CmsUUID, Boolean> m_workflowProjectStatus = CacheBuilder.newBuilder().build(
        CacheLoader.from(new Function<CmsUUID, Boolean>() {

            public Boolean apply(CmsUUID projectId) {

                try {
                    @SuppressWarnings("synthetic-access")
                    CmsProject project = m_cms.readProject(projectId);
                    return new Boolean(project.isWorkflowProject());
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return Boolean.FALSE;
                }
            }

        }));

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the user CMS context
     * @param options the publish options 
     */
    public CmsRelease(CmsObject cms, CmsPublishOptions options) {

        super(cms, options);

    }

    /**
     * Creates a new instance.<p>
     * @param cms the user CMS context
     * @param params the additional publish parameters 
     */
    public CmsRelease(CmsObject cms, Map<String, String> params) {

        super(cms, params);

    }

    /**
     * @see org.opencms.ade.publish.CmsPublish#getPublishResourceBeans(org.opencms.ade.publish.CmsPublish.ResourcesAndRelated)
     */
    @Override
    public List<CmsPublishResource> getPublishResourceBeans(ResourcesAndRelated rawPublishResources) {

        // first look for already published resources
        Set<CmsResource> published = getAlreadyPublishedResources(rawPublishResources);

        // then for resources without permission
        Set<CmsResource> exclude = new HashSet<CmsResource>(published);

        ResourcesAndRelated locked = getBlockingLockedResources(rawPublishResources, exclude);

        exclude.clear();
        exclude.addAll(published);
        exclude.addAll(locked.getResources());

        // update the publish list
        ResourcesAndRelated pubResources = new ResourcesAndRelated();
        pubResources.getResources().addAll(rawPublishResources.getResources());
        pubResources.getResources().removeAll(exclude);
        pubResources.getRelatedResources().addAll(rawPublishResources.getRelatedResources());
        pubResources.getRelatedResources().removeAll(locked.getRelatedResources());
        List<CmsResource> resourcesWithoutTempfiles = new ArrayList<CmsResource>();
        for (CmsResource res : rawPublishResources.getResources()) {
            if (!CmsResource.isTemporaryFileName(res.getRootPath())) {
                resourcesWithoutTempfiles.add(res);
            }
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

        List<CmsPublishResource> publishResources = new ArrayList<CmsPublishResource>();
        for (CmsResource resource : resourcesWithoutTempfiles) {
            CmsPublishResource pubRes = createPublishResource(
                resource,
                pubList,
                allPubRes,
                published,
                new ResourcesAndRelated(),
                locked);
            publishResources.add(pubRes);
        }

        return publishResources;
    }

    /**
     * Gets a message from the message bundle.<p>
     * 
     * @param key the message key 
     * @param args the message parameters
     *  
     * @return the message from the message bundle 
     */
    protected String getMessage(String key, String... args) {

        return Messages.get().getBundle(m_cms.getRequestContext().getLocale()).key(key, args);
    }

    /**
     * @see org.opencms.ade.publish.CmsPublish#getRawPublishResources()
     */
    @Override
    protected List<CmsResource> getRawPublishResources() throws CmsException {

        List<CmsResource> rawResourceList = new ArrayList<CmsResource>();

        if ((m_options.getProjectId() == null) || m_options.getProjectId().isNullUUID()) {
            // get the users publish list
            rawResourceList.addAll(OpenCms.getPublishManager().getUsersPubList(m_cms));
        } else {
            rawResourceList.addAll(m_cms.readProjectView(m_options.getProjectId(), CmsResource.STATE_KEEP));
        }
        return rawResourceList;

    }

    /**
     * @see org.opencms.ade.publish.CmsPublish#getResourceInfo(org.opencms.file.CmsResource, java.util.Set, org.opencms.ade.publish.CmsPublish.ResourcesAndRelated, org.opencms.ade.publish.CmsPublish.ResourcesAndRelated)
     */
    @Override
    protected CmsPublishResourceInfo getResourceInfo(
        CmsResource resource,
        Set<CmsResource> published,
        ResourcesAndRelated permissions,
        ResourcesAndRelated locked) {

        CmsPublishResourceInfo info = super.getResourceInfo(resource, published, permissions, locked);
        if (info == null) {
            CmsUUID projectId = resource.getProjectLastModified();
            if (isWorkflowProject(projectId)) {
                info = new CmsPublishResourceInfo(
                    getMessage(Messages.GUI_ALREADY_IN_WORKFLOW_0),
                    CmsPublishResourceInfo.Type.WORKFLOW);

            }
        }
        return info;
    }

    /**
     * Checks whether the project with the given id is a workflow project.<p>
     * 
     * @param projectId the project id 
     * 
     * @return true if the project with the given id is a workflow project 
     */
    private boolean isWorkflowProject(CmsUUID projectId) {

        try {
            return m_workflowProjectStatus.get(projectId).booleanValue();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

}
