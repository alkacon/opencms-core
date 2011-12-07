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
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishEventAdapter;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.publish.CmsPublishManager;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

/**
 * The default workflow manager implementation, which supports 2 basic actions, Release and Publish.
 */
public class CmsWorkflowManager implements I_CmsEventListener {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkflowManager.class);

    /** A CMS context with admin privileges. */
    private CmsObject m_adminCms;

    /** The publish workflow action. */
    public static final String ACTION_PUBLISH = "publish";

    /** The forced publish workflow action. */
    public static final String ACTION_FORCE_PUBLISH = "forcepublish";

    /** The release workflow action. */
    public static final String ACTION_RELEASE = "release";

    /**
     * Creates a new workflow manager instance.<p>
     *  
     * @param adminCms a CMS context with admin privileges 
     */
    public CmsWorkflowManager(CmsObject adminCms, CmsPublishManager publishManager) {

        m_adminCms = adminCms;
        publishManager.addPublishListener(new CmsPublishEventAdapter() {

            @Override
            public void onFinish(CmsPublishJobRunning publishJob) {

                CmsWorkflowManager.this.onFinishPublishJob(publishJob);
            }
        });
    }

    public void cmsEvent(CmsEvent event) {

        // TODO: Auto-generated method stub

    }

    /**
     * Executes a workflow action in the context of the current user.<p>
     * 
     * @param userCms the current user's CMS context
     * @param actionKey the key identifying the workflow action 
     * 
     * @param resources the resources to be processed 
     * 
     * @return the workflow response for the executed action 
     * 
     * @throws CmsException if something goes wrong 
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

    /**
     * Gets the group which should be used as the 'manager' group for newly created workflow projects.<p>
     * @return
     */
    public String getWorkflowProjectManagerGroup() {

        return OpenCms.getDefaultUsers().getGroupProjectmanagers();
    }

    /**
     * Gets the group which should be used as the 'user' group for newly created workflow projects.<p>
     * 
     * @return a group name 
     */
    public String getWorkflowProjectUserGroup() {

        return OpenCms.getDefaultUsers().getGroupProjectmanagers();
    }

    public void onFinishPublishJob(CmsPublishJobRunning publishJob) {

        System.out.println("###CmsWorkflowManager: publish job finished!");
        CmsPublishList publishList = publishJob.getPublishList();
        Set<CmsUUID> projectIds = new HashSet<CmsUUID>();
        for (CmsResource resource : publishList.getAllResources()) {
            projectIds.add(resource.getProjectLastModified());
        }
        for (CmsUUID projectId : projectIds) {
            try {
                CmsProject project = m_adminCms.readProject(projectId);
                cleanupProjectIfEmpty(project);
            } catch (CmsException e) {
                LOG.info("Project " + projectId + " doesn't exist anymore.");
            }
        }
    }

    /**
     * Implemenation of the 'forcepublish' workflow action.<p>
     * 
     * @param userCms the user's current CMS context 
     * @param resources the resources to be processed
     *  
     * @return the workflow response for this action
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionForcePublish(CmsObject userCms, List<CmsResource> resources)
    throws CmsException {

        CmsPublish publish = new CmsPublish(userCms);
        List<CmsPublishResource> brokenLinkBeans = new ArrayList<CmsPublishResource>();
        publish.publishResources(resources);
        return getSuccessResponse();
    }

    /**
     * Implementation of the 'publish' workflow action.<p>
     * 
     * @param userCms the user's current CMS context 
     * @param resources the resources to be processed 
     * 
     * @return the workflow response for this action 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionPublish(CmsObject userCms, List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms);
        List<CmsPublishResource> brokenLinkBeans = publish.getBrokenResources(resources);
        if (brokenLinkBeans.size() == 0) {
            publish.publishResources(resources);
            return getSuccessResponse();
        }
        return getPublishBrokenRelationsResponse(brokenLinkBeans);
    }

    /**
     * Implementation of the 'release' workflow action.<p>
     * 
     * @param userCms the current user's CMS context 
     * @param resources the resources which should be released
     *  
     * @return the workflow response for this action 
     * 
     * @throws CmsException if something goes wrong 
     */
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
        sendNotification(userCms, workflowProject, resources);
        return new CmsWorkflowResponse(
            true,
            "ok",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowActionBean>(),
            workflowProject.getUuid());
    }

    /**
     * Removes a project if there are no longer any resources which have been last modified in that project.<p>
     * 
     * @param project the project 
     * @throws CmsException
     */
    protected void cleanupProjectIfEmpty(CmsProject project) throws CmsException {

        if ((project.getType().getMode() == CmsProject.PROJECT_TYPE_WORKFLOW.getMode()) && isProjectEmpty(project)) {
            LOG.info("Removing project " + project.getName() + " because it is an empty workflow project.");
            m_adminCms.deleteProject(project.getUuid());
        }
    }

    /**
     * Generates the description for a new workflow project based on the user for whom it is created.<p>
     * 
     * @param userCms the user's current CMS context 
     * 
     * @return the workflow project description 
     */
    protected String generateProjectDescription(CmsObject userCms) {

        return "Workflow project.";
    }

    /**
     * Generates the name for a new workflow project based on the user for whom it is created.<p>
     * 
     * @param userCms the user's current CMS context 
     * 
     * @return the workflow project name 
     */
    protected String generateProjectName(CmsObject userCms) {

        return userCms.getRequestContext().getCurrentUser().getName() + "_" + (new CmsUUID()).toString();
    }

    /**
     * Helper method for generating the workflow response which should be sent when publishing the resources would break relations.<p>
     * 
     * @param publishResources the resources whose links would be broken
     *  
     * @return the workflow response 
     */
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

    /**
     * Gets the workflow response which should be sent when the resources have successfully been published.<p>
     * 
     * @return the successful workflow response 
     */
    protected CmsWorkflowResponse getSuccessResponse() {

        return new CmsWorkflowResponse(
            true,
            "",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowActionBean>(),
            null);
    }

    protected boolean isProjectEmpty(CmsProject project) throws CmsException {

        CmsPublishManager publishManager = OpenCms.getPublishManager();
        CmsObject projectCms = OpenCms.initCmsObject(m_adminCms);
        projectCms.getRequestContext().setCurrentProject(project);
        CmsPublishList publishList = publishManager.getPublishList(projectCms);
        List resourcesModifiedInProject = publishList.getAllResources();
        return resourcesModifiedInProject.isEmpty();
    }

    /**
     * Sends the notification for released resources.<p>
     * 
     * @param userCms the user's CMS context 
     * @param workflowProject the workflow project which 
     * @param resources
     * @throws CmsException
     */
    protected void sendNotification(CmsObject userCms, CmsProject workflowProject, List<CmsResource> resources)
    throws CmsException {

        try {
            CmsWorkflowNotification notification = new CmsWorkflowNotification(
                userCms.getRequestContext().getCurrentUser(),
                workflowProject,
                resources);
            notification.send();
        } catch (EmailException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
