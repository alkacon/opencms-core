/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflowActionBean;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

public class CmsWorkflowManager {

    /** A CMS context with admin privileges. */
    private CmsObject m_adminCms;

    /** The publish workflow action. */
    public static final String ACTION_PUBLISH = "publish";

    /** The forced publish workflow action. */
    public static final String ACTION_FORCE_PUBLISH = "forcepublish";

    /** The release workflow action. */
    public static final String ACTION_RELEASE = "release";

    /**
     * Creates a new workflow manager instance 
     * @param adminCms
     */
    public CmsWorkflowManager(CmsObject adminCms) {

        m_adminCms = adminCms;
    }

    /**
     * Executes a workflow action in the context of the current user.<p>
     * 
     * @param userCms the current user's CMS context
     * @param actionKey the key identifying the workflow action 
     * 
     * @param resources
     * @return
     * @throws CmsException
     */
    public CmsWorkflowResponse executeAction(CmsObject userCms, String actionKey, List<CmsResource> resources)
    throws CmsException {

        if (actionKey.equals(ACTION_PUBLISH)) {
            return actionPublish(userCms, resources);
        } else if (actionKey.equals(ACTION_RELEASE)) {
            return actionRelease(userCms, resources);
        } else if (actionKey.equals(ACTION_FORCE_PUBLISH)) {
            return actionForcePublish(userCms, resources);
        }
        throw new CmsInvalidActionException(actionKey);
    }

    /**
     * Returns the workflow actions which should be available to the user.<p>
     * 
     * @param userCms the user's CMS object 
     * 
     * @return the list of workflow actions which should be available to the user 
     */
    public List<CmsWorkflowActionBean> getAvailableActions(CmsObject userCms) {

        List<CmsWorkflowActionBean> actions = new ArrayList<CmsWorkflowActionBean>();
        CmsWorkflowActionBean publish = new CmsWorkflowActionBean(ACTION_PUBLISH, "Publish", true);
        CmsWorkflowActionBean release = new CmsWorkflowActionBean(ACTION_RELEASE, "Release", true);
        actions.add(publish);
        actions.add(release);
        return actions;
    }

    public String getWorkflowProjectManagerGroup() {

        return "Projectmanagers";
    }

    public String getWorkflowProjectUserGroup() {

        return "Projectmanagers";
    }

    protected CmsWorkflowResponse actionForcePublish(CmsObject userCms, List<CmsResource> resources)
    throws CmsException {

        CmsPublish publish = new CmsPublish(userCms);
        List<CmsPublishResource> brokenLinkBeans = new ArrayList<CmsPublishResource>();
        publish.publishResources(resources);
        return getSuccessResponse();
    }

    protected CmsWorkflowResponse actionPublish(CmsObject userCms, List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms);
        List<CmsPublishResource> brokenLinkBeans = publish.getBrokenResources(resources);
        if (brokenLinkBeans.size() == 0) {
            publish.publishResources(resources);
            return getSuccessResponse();
        }
        return getPublishBrokenRelationsResponse(brokenLinkBeans);
    }

    protected CmsWorkflowResponse actionRelease(CmsObject userCms, List<CmsResource> resources) throws CmsException {

        String projectName = generateProjectName(userCms);
        String projectDescription = generateProjectDescription(userCms);
        CmsObject offlineAdminCms = OpenCms.initCmsObject(m_adminCms);
        offlineAdminCms.getRequestContext().setCurrentProject(userCms.getRequestContext().getCurrentProject());
        String managerGroup = getWorkflowProjectManagerGroup();
        String userGroup = getWorkflowProjectUserGroup();
        CmsProject workflowProject = m_adminCms.createProject(
            projectName,
            projectDescription,
            userGroup,
            managerGroup,
            CmsProject.PROJECT_TYPE_WORKFLOW);
        for (CmsResource resource : resources) {
            offlineAdminCms.writeProjectLastModified(resource, workflowProject);
            System.out.println("Releasing resource " + resource.getRootPath());
        }
        return new CmsWorkflowResponse(
            true,
            "ok",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowActionBean>(),
            workflowProject.getUuid());
    }

    protected String generateProjectDescription(CmsObject userCms) {

        return "Workflow project.";
    }

    protected String generateProjectName(CmsObject userCms) {

        return userCms.getRequestContext().getCurrentUser().getName() + "_" + (new CmsUUID()).toString();
    }

    protected CmsWorkflowResponse getPublishBrokenRelationsResponse(List<CmsPublishResource> publishResources) {

        List<CmsWorkflowActionBean> actions = new ArrayList<CmsWorkflowActionBean>();
        CmsWorkflowActionBean forcePublish = new CmsWorkflowActionBean(ACTION_FORCE_PUBLISH, "Publish", true);
        return new CmsWorkflowResponse(
            false,
            "$ broken relations - use message bundle here $",
            publishResources,
            actions,
            null);

    }

    protected CmsWorkflowResponse getSuccessResponse() {

        return new CmsWorkflowResponse(
            true,
            "",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowActionBean>(),
            null);
    }

}
