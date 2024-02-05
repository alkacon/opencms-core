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

package org.opencms.workflow;

import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

/**
 * Publish resource formatter for the extended workflow manager.<p>
 *
 * This class handles warnings differently from the default implementation, since we don't need publish permissions for the release
 * workflow, but need to check whether resources are already in a workflow.<p>
 */
public class CmsExtendedPublishResourceFormatter extends CmsDefaultPublishResourceFormatter {

    /**
     * Rule which checks whether a resource is already in a workflow.<p>
     */
    class ExcludeAlreadyInWorkflow implements I_PublishResourceValidator {

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#findInvalidResources(java.util.Set)
         */
        public Set<CmsResource> findInvalidResources(Set<CmsResource> input) {

            Set<CmsResource> result = Sets.newHashSet();
            CmsUUID optionsProject = m_options.getProjectId();
            for (CmsResource resource : input) {
                CmsUUID projectId = resource.getProjectLastModified();
                if (isWorkflowProject(projectId) && ((optionsProject == null) || !projectId.equals(optionsProject))) {
                    result.add(resource);
                }
            }
            return result;
        }

        /**
         * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter.I_PublishResourceValidator#getInfoForResource(org.opencms.file.CmsResource)
         */
        public CmsPublishResourceInfo getInfoForResource(CmsResource resource) {

            return new CmsPublishResourceInfo(
                getMessage(Messages.GUI_ALREADY_IN_WORKFLOW_0),
                CmsPublishResourceInfo.Type.WORKFLOW);
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtendedPublishResourceFormatter.class);

    /** Computing map which keeps track of which projects are workflow projects. */
    protected LoadingCache<CmsUUID, Boolean> m_workflowProjectStatus = CacheBuilder.newBuilder().build(
        CacheLoader.from(new Function<CmsUUID, Boolean>() {

            public Boolean apply(CmsUUID projectId) {

                try {
                    CmsProject project = m_cms.readProject(projectId);
                    return Boolean.valueOf(project.isWorkflowProject());
                } catch (CmsException e) {
                    getLog().warn(e.getLocalizedMessage(), e);
                    return Boolean.FALSE;
                }
            }

        }));

    /** Flag which determines whether the resources should be formatted for the 'release' workflow. */
    private boolean m_isRelease;

    /**
     * Constructor.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsExtendedPublishResourceFormatter(CmsObject cms) {

        super(cms);

    }

    /**
     * Gets the logger for this class.<p>
     *
     * @return the logger for this class
     */
    public static Log getLog() {

        return LOG;
    }

    /**
     * Sets the 'release' mode.<p>
     *
     * @param release true if the resources should be formatted for the 'release' workflow
     */
    public void setRelease(boolean release) {

        m_isRelease = release;
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
     * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter#getResourceMapFilter()
     */
    @Override
    protected Predicate<CmsResource> getResourceMapFilter() {

        // in 'release' mode, we don't want to filter, otherwise we use the filter from the parent class
        if (m_isRelease) {
            return null;
        } else {
            return super.getResourceMapFilter();
        }
    }

    /**
     * @see org.opencms.workflow.CmsDefaultPublishResourceFormatter#getValidators()
     */
    @Override
    protected List<I_PublishResourceValidator> getValidators() {

        if (m_isRelease) {
            return Arrays.asList(
                new CmsDefaultPublishResourceFormatter.AlreadyPublishedValidator(),
                new CmsDefaultPublishResourceFormatter.BlockingLockedValidator(),
                new ExcludeAlreadyInWorkflow());
        } else {
            return Arrays.asList(
                new CmsDefaultPublishResourceFormatter.AlreadyPublishedValidator(),
                new CmsDefaultPublishResourceFormatter.NoPermissionsValidator(),
                new CmsDefaultPublishResourceFormatter.BlockingLockedValidator(),
                new ExcludeAlreadyInWorkflow());
        }
    }

    /**
     * Checks whether the project with the given id is a workflow project.<p>
     *
     * @param projectId the project id
     *
     * @return true if the project with the given id is a workflow project
     */
    protected boolean isWorkflowProject(CmsUUID projectId) {

        try {
            return m_workflowProjectStatus.get(projectId).booleanValue();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

}
