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
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishEventAdapter;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.publish.CmsPublishManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

/**
 * The default workflow manager implementation, which supports 2 basic actions, Release and Publish.
 */
public class CmsWorkflowManager {

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

    /** The key for the configurable workflow project manager group. */
    public static final String PARAM_WORKFLOW_PROJECT_MANAGER_GROUP = "workflowProjectManagerGroup";

    /** The key for the configurable workflow project user group. */
    public static final String PARAM_WORKFLOW_PROJECT_USER_GROUP = "workflowProjectUserGroup";

    /** The project counter, used for generating project names. */
    private int m_projectCounter;

    /** The map of configuration parameters. */
    private Map<String, String> m_parameters;

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
    public synchronized CmsWorkflowResponse executeAction(
        CmsObject userCms,
        String actionKey,
        List<CmsResource> resources) throws CmsException {

        if (LOG.isInfoEnabled()) {
            LOG.info("workflow action: " + userCms.getRequestContext().getCurrentUser().getName() + " " + actionKey);
            List<String> resourceNames = new ArrayList<String>();
            for (CmsResource resource : resources) {
                resourceNames.add(resource.getRootPath());
            }
            LOG.info("Resources: " + CmsStringUtil.listAsString(resourceNames, ","));
        }
        try {
            if (actionKey.equals(ACTION_PUBLISH)) {
                return actionPublish(userCms, resources);
            } else if (actionKey.equals(ACTION_RELEASE)) {
                return actionRelease(userCms, resources);
            } else if (actionKey.equals(ACTION_FORCE_PUBLISH)) {
                return actionForcePublish(userCms, resources);
            }
        } catch (CmsException e) {
            LOG.info("workflow action failed");
            LOG.info(e.getLocalizedMessage(), e);
            throw e;
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
        Locale locale = getLocale(userCms);
        String publishLabel = Messages.get().getBundle(locale).key(Messages.GUI_WORKFLOW_ACTION_PUBLISH_0);
        String releaseLabel = Messages.get().getBundle(locale).key(Messages.GUI_WORKFLOW_ACTION_RELEASE_0);
        CmsWorkflowActionBean publish = new CmsWorkflowActionBean(ACTION_PUBLISH, publishLabel, true);
        CmsWorkflowActionBean release = new CmsWorkflowActionBean(ACTION_RELEASE, releaseLabel, true);
        actions.add(publish);
        actions.add(release);
        return actions;
    }

    /**
     * Gets the parameters of the workflow manager.<p>
     * 
     * @return the configuration parameters of the workflow manager 
     */
    public Map<String, String> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * Gets the name of the group which should be used as the 'manager' group for newly created workflow projects.<p>
     * 
     * @return a group name 
     */
    public String getWorkflowProjectManagerGroup() {

        return getParameter(PARAM_WORKFLOW_PROJECT_MANAGER_GROUP, OpenCms.getDefaultUsers().getGroupProjectmanagers());
    }

    /**
     * Gets the name of the group which should be used as the 'user' group for newly created workflow projects.<p>
     * 
     * @return a group name 
     */
    public String getWorkflowProjectUserGroup() {

        return getParameter(PARAM_WORKFLOW_PROJECT_USER_GROUP, OpenCms.getDefaultUsers().getGroupProjectmanagers());
    }

    /**
     * Initializes this workflow manager instance.<p>
     * 
     * @param adminCms the CMS context with admin privileges 
     * @param publishManager the publish manager instance 
     */
    public void initialize(CmsObject adminCms, CmsPublishManager publishManager) {

        if (m_adminCms != null) {
            throw new IllegalStateException();
        }
        m_adminCms = adminCms;
        publishManager.addPublishListener(new CmsPublishEventAdapter() {

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onFinish(org.opencms.publish.CmsPublishJobRunning)
             */
            @Override
            public void onFinish(CmsPublishJobRunning publishJob) {

                CmsWorkflowManager.this.onFinishPublishJob(publishJob);
            }

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onStart(org.opencms.publish.CmsPublishJobEnqueued)
             */
            @Override
            public void onStart(CmsPublishJobEnqueued publishJob) {

                //CmsWorkflowManager.this.onStartPublishJob(publishJob);
            }
        });
    }

    /**
     * Handles finished publish jobs by removing projects of resources in the publish job if they are empty workflow projects.<p>
     * 
     * @param publishJob the finished published job 
     */
    public void onFinishPublishJob(CmsPublishJobRunning publishJob) {

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
     * Sets the configuration parameters of the workflow manager.<p>
     * 
     * @param parameters the map of configuration parameters 
     */
    public void setParameters(Map<String, String> parameters) {

        if (m_parameters != null) {
            throw new IllegalStateException();
        }
        m_parameters = parameters;
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
        return getPublishBrokenRelationsResponse(userCms, brokenLinkBeans);
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

        checkNewParentsInList(userCms, resources);
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
        CmsObject newProjectCms = OpenCms.initCmsObject(offlineAdminCms);
        newProjectCms.getRequestContext().setCurrentProject(workflowProject);
        newProjectCms.getRequestContext().setSiteRoot("");
        newProjectCms.copyResourceToProject("/");
        CmsUser admin = offlineAdminCms.getRequestContext().getCurrentUser();
        clearLocks(userCms.getRequestContext().getCurrentProject(), resources);
        for (CmsResource resource : resources) {
            CmsLock lock = offlineAdminCms.getLock(resource);
            if (lock.isUnlocked()) {
                offlineAdminCms.lockResource(resource);
            } else if (!lock.isOwnedBy(admin)) {
                offlineAdminCms.changeLock(resource);
            }
            offlineAdminCms.writeProjectLastModified(resource, workflowProject);
            offlineAdminCms.unlockResource(resource);
        }
        for (CmsUser user : getNotificationMailRecipients()) {
            sendNotification(userCms, user, workflowProject, resources);
        }
        return new CmsWorkflowResponse(
            true,
            "",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowActionBean>(),
            workflowProject.getUuid());
    }

    /**
     * Checks that the parent folders of new resources which are released are either not new or are also released.<p>
     *   
     * @param userCms the user CMS context 
     * @param resources the resources to check 
     * 
     * @throws CmsException if the check fails 
     */
    protected void checkNewParentsInList(CmsObject userCms, List<CmsResource> resources) throws CmsException {

        Map<String, CmsResource> resourcesByPath = new HashMap<String, CmsResource>();
        CmsObject rootCms = OpenCms.initCmsObject(m_adminCms);
        rootCms.getRequestContext().setCurrentProject(userCms.getRequestContext().getCurrentProject());
        rootCms.getRequestContext().setSiteRoot("");
        for (CmsResource resource : resources) {
            resourcesByPath.put(resource.getRootPath(), resource);
        }
        for (CmsResource resource : resources) {
            if (resource.getState().isNew()) {
                String parentPath = CmsResource.getParentFolder(resource.getRootPath());
                CmsResource parent = resourcesByPath.get(parentPath);
                if (parent == null) {
                    parent = rootCms.readResource(parentPath);
                    if (parent.getState().isNew()) {
                        throw new CmsNewParentNotInWorkflowException(Messages.get().container(
                            Messages.ERR_NEW_PARENT_NOT_IN_WORKFLOW_1,
                            resource.getRootPath()));
                    }
                }
            }
        }
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
     * Ensures that the resources to be released are unlocked.<p>
     * 
     * @param project the project in which to operate 
     * @param resources the resources for which the locks should be removed
     *  
     * @throws CmsException if something goes wrong 
     */
    protected void clearLocks(CmsProject project, List<CmsResource> resources) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(m_adminCms);
        rootCms.getRequestContext().setCurrentProject(project);
        rootCms.getRequestContext().setSiteRoot("");
        for (CmsResource resource : resources) {
            CmsLock lock = rootCms.getLock(resource);
            if (lock.isUnlocked()) {
                continue;
            }
            String currentPath = resource.getRootPath();
            while (lock.isInherited()) {
                currentPath = CmsResource.getParentFolder(currentPath);
                lock = rootCms.getLock(currentPath);
            }
            rootCms.changeLock(currentPath);
            rootCms.unlockResource(currentPath);
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

        CmsUser user = userCms.getRequestContext().getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd k:m:s");
        String dateString = format.format(date);
        return "Workflow project created by " + user.getName() + " at " + dateString;
    }

    /**
     * Generates the name for a new workflow project based on the user for whom it is created.<p>
     * 
     * @param userCms the user's current CMS context 
     * 
     * @return the workflow project name 
     */
    protected String generateProjectName(CmsObject userCms) {

        CmsUser user = userCms.getRequestContext().getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_k_m_s");
        String dateStr = format.format(date);
        dateStr = dateStr + "_" + time;
        return "WF_" + user.getName() + "_" + dateStr;
    }

    /**
     * Gets the locale to use for a given CMS context.<p>
     * 
     * @param userCms the CMS context 
     * 
     * @return the locale to use 
     */
    protected Locale getLocale(CmsObject userCms) {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(userCms);
    }

    /**
     * Gets the list of recipients for the notifications.<p>
     * 
     * @return the list of users which should be notified when resources are released
     */
    protected List<CmsUser> getNotificationMailRecipients() {

        String group = getWorkflowProjectManagerGroup();
        CmsObject cms = m_adminCms;
        try {
            List<CmsUser> users = cms.getUsersOfGroup(group);
            return users;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new ArrayList<CmsUser>();
        }
    }

    /**
     * Gets the configuration parameter for a given key, and if it doesn't find one, returns a default value.<p>
     * 
     * @param key the configuration key 
     * @param defaultValue the default value to use when the configuration entry isn't found 
     * 
     * @return the configuration value 
     */
    protected String getParameter(String key, String defaultValue) {

        String result = m_parameters.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Helper method for generating the workflow response which should be sent when publishing the resources would break relations.<p>
     *
     * @param userCms the user's CMS context 
     * @param publishResources the resources whose links would be broken
     *  
     * @return the workflow response 
     */
    protected CmsWorkflowResponse getPublishBrokenRelationsResponse(
        CmsObject userCms,
        List<CmsPublishResource> publishResources) {

        List<CmsWorkflowActionBean> actions = new ArrayList<CmsWorkflowActionBean>();
        String forcePublishLabel = Messages.get().getBundle(getLocale(userCms)).key(
            Messages.GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0);

        CmsWorkflowActionBean forcePublish = new CmsWorkflowActionBean(ACTION_FORCE_PUBLISH, forcePublishLabel, true);
        actions.add(forcePublish);
        return new CmsWorkflowResponse(false, Messages.get().getBundle(getLocale(userCms)).key(
            Messages.GUI_BROKEN_LINKS_0), publishResources, actions, null);
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

    /**
     * Checks whether there are resources which have last been modified in a given project.<p>
     * 
     * @param project the project which should be checked 
     * @return true if there are no resources which have been last modified inside the project 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected boolean isProjectEmpty(CmsProject project) throws CmsException {

        CmsPublishManager publishManager = OpenCms.getPublishManager();
        CmsObject projectCms = OpenCms.initCmsObject(m_adminCms);
        projectCms.getRequestContext().setCurrentProject(project);
        CmsPublishList publishList = publishManager.getPublishList(projectCms);
        List<CmsResource> resourcesModifiedInProject = publishList.getAllResources();
        return resourcesModifiedInProject.isEmpty();
    }

    /**
     * Sends the notification for released resources.<p>
     * 
     * @param userCms the user's CMS context 
     * @param recipient the OpenCms user to whom the notification should be sent 
     * @param workflowProject the workflow project which 
     * @param resources the resources which have been affected by a workflow action 
     */
    protected void sendNotification(
        CmsObject userCms,
        CmsUser recipient,
        CmsProject workflowProject,
        List<CmsResource> resources) {

        try {
            CmsWorkflowNotification notification = new CmsWorkflowNotification(
                m_adminCms,
                recipient,
                userCms.getRequestContext().getCurrentUser(),
                workflowProject,
                resources);
            notification.send();
        } catch (EmailException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
