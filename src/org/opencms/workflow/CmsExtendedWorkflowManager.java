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

import org.opencms.ade.publish.CmsPublishService;
import org.opencms.ade.publish.I_CmsVirtualProject;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishEventAdapter;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * The default workflow manager implementation, which supports 2 basic actions, Release and Publish.
 */
public class CmsExtendedWorkflowManager extends CmsDefaultWorkflowManager {

    /** The release workflow action. */
    public static final String ACTION_RELEASE = "release";

    /** The parameter which points to the XML content used for notifications. */
    public static final String PARAM_NOTIFICATION_CONTENT = "notificationContent";

    /** The key for the configurable workflow project manager group. */
    public static final String PARAM_WORKFLOW_PROJECT_MANAGER_GROUP = "workflowProjectManagerGroup";

    /** The key for the configurable workflow project user group. */
    public static final String PARAM_WORKFLOW_PROJECT_USER_GROUP = "workflowProjectUserGroup";

    /** The key for the 'release' workflow. */
    public static final String WORKFLOW_RELEASE = "WORKFLOW_RELEASE";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtendedWorkflowManager.class);

    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#createFormatter(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    @Override
    public I_CmsPublishResourceFormatter createFormatter(
        CmsObject cms,
        CmsWorkflow workflow,
        CmsPublishOptions options) {

        String workflowKey = workflow.getId();
        boolean release = WORKFLOW_RELEASE.equals(workflowKey);
        CmsExtendedPublishResourceFormatter formatter = new CmsExtendedPublishResourceFormatter(cms);
        formatter.setRelease(release);
        return formatter;
    }

    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#executeAction(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflowAction, org.opencms.ade.publish.shared.CmsPublishOptions, java.util.List)
     */
    @Override
    public CmsWorkflowResponse executeAction(
        CmsObject userCms,
        CmsWorkflowAction action,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        if (LOG.isInfoEnabled()) {
            LOG.info(
                "workflow action: "
                    + userCms.getRequestContext().getCurrentUser().getName()
                    + " "
                    + action.getAction());
            List<String> resourceNames = new ArrayList<String>();
            for (CmsResource resource : resources) {
                resourceNames.add(resource.getRootPath());
            }
            LOG.info("Resources: " + CmsStringUtil.listAsString(resourceNames, ","));
        }
        try {

            String actionKey = action.getAction();
            if (ACTION_RELEASE.equals(actionKey)) {
                return actionRelease(userCms, resources);
            } else {
                return super.executeAction(userCms, action, options, resources);
            }
        } catch (CmsException e) {
            LOG.error("workflow action failed");
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#getRealOrVirtualProject(org.opencms.util.CmsUUID)
     */
    @Override
    public I_CmsVirtualProject getRealOrVirtualProject(CmsUUID projectId) {

        I_CmsVirtualProject result = m_virtualProjects.get(projectId);
        if (result == null) {
            result = new CmsExtendedRealProjectWrapper(projectId);
        }
        return result;
    }

    /**
     * Gets the name of the group which should be used as the 'manager' group for newly created workflow projects.<p>
     *
     * @return a group name
     */
    public String getWorkflowProjectManagerGroup() {

        return getParameter(PARAM_WORKFLOW_PROJECT_MANAGER_GROUP, OpenCms.getDefaultUsers().getGroupAdministrators());
    }

    /**
     * Gets the name of the group which should be used as the 'user' group for newly created workflow projects.<p>
     *
     * @return a group name
     */
    public String getWorkflowProjectUserGroup() {

        return getParameter(PARAM_WORKFLOW_PROJECT_USER_GROUP, OpenCms.getDefaultUsers().getGroupAdministrators());
    }

    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#getWorkflowResources(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions, boolean)
     */
    @Override
    public CmsWorkflowResources getWorkflowResources(
        CmsObject cms,
        CmsWorkflow workflow,
        CmsPublishOptions options,
        boolean canOverrideWorkflow) {

        String workflowKey = workflow.getId();
        String overrideId = null;
        if (WORKFLOW_RELEASE.equals(workflowKey)) {
            List<CmsResource> result = super.getWorkflowResources(
                cms,
                workflow,
                options,
                canOverrideWorkflow).getWorkflowResources();
            if (canOverrideWorkflow) {
                boolean override = false;

                for (CmsResource permCheckResource : result) {
                    try {
                        boolean canPublish = cms.hasPermissions(
                            permCheckResource,
                            CmsPermissionSet.ACCESS_DIRECT_PUBLISH);
                        if (canPublish) {
                            override = true;
                        }
                    } catch (Exception e) {
                        LOG.error(
                            "Can't check permissions for "
                                + permCheckResource.getRootPath()
                                + ":"
                                + e.getLocalizedMessage(),
                            e);
                    }
                    if (override) {
                        List<CmsResource> resources = getWorkflowResources(
                            cms,
                            getWorkflows(cms).get(CmsDefaultWorkflowManager.WORKFLOW_PUBLISH),
                            options,
                            false).getWorkflowResources();
                        result = resources;
                        overrideId = WORKFLOW_PUBLISH;
                    }
                }
            }
            CmsWorkflowResources realResult = new CmsWorkflowResources(result, getWorkflows(cms).get(overrideId));
            return realResult;
        } else {
            CmsWorkflowResources realResult = super.getWorkflowResources(cms, workflow, options, canOverrideWorkflow);
            return realResult;
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflows(org.opencms.file.CmsObject)
     */
    @Override
    public Map<String, CmsWorkflow> getWorkflows(CmsObject cms) {

        Map<String, CmsWorkflow> parentWorkflows = super.getWorkflows(cms);
        Map<String, CmsWorkflow> result = new LinkedHashMap<String, CmsWorkflow>();
        String releaseLabel = getLabel(cms, Messages.GUI_WORKFLOW_ACTION_RELEASE_0);
        CmsWorkflowAction release = new CmsWorkflowAction(ACTION_RELEASE, releaseLabel, true);
        List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
        actions.add(release);
        CmsWorkflow releaseWorkflow = new CmsWorkflow(WORKFLOW_RELEASE, releaseLabel, actions);
        try {
            boolean isProjectManager = isProjectManager(cms);
            // make release action always available, but make it the default if the user
            // isn't a project manager.
            if (isProjectManager) {
                result.putAll(parentWorkflows);
                result.put(WORKFLOW_RELEASE, releaseWorkflow);
            } else {
                result.put(WORKFLOW_RELEASE, releaseWorkflow);
                result.putAll(parentWorkflows);
            }
        } catch (CmsException e) {
            result = parentWorkflows;
        }
        return result;
    }

    /**
     * @see org.opencms.workflow.A_CmsWorkflowManager#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject adminCms) {

        super.initialize(adminCms);
        OpenCms.getPublishManager().addPublishListener(new CmsPublishEventAdapter() {

            @Override
            public void onFinish(CmsPublishJobRunning publishJob) {

                CmsExtendedWorkflowManager.this.onFinishPublishJob(publishJob);
            }

            /**
             * @see org.opencms.publish.CmsPublishEventAdapter#onStart(org.opencms.publish.CmsPublishJobEnqueued)
             */
            @Override
            public void onStart(CmsPublishJobEnqueued publishJob) {

                CmsExtendedWorkflowManager.this.onStartPublishJob(publishJob);
            }
        });
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
            new ArrayList<CmsWorkflowAction>(),
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
                        throw new CmsNewParentNotInWorkflowException(
                            Messages.get().container(
                                Messages.ERR_NEW_PARENT_NOT_IN_WORKFLOW_1,
                                resource.getRootPath()));
                    }
                }
            }
        }
    }

    /**
     * Cleans up empty workflow projects.<p>
     *
     * @param projects the workflow projects to clean up
     *
     * @throws CmsException if something goes wrong
     */
    protected void cleanupEmptyWorkflowProjects(List<CmsProject> projects) throws CmsException {

        if (projects == null) {
            projects = OpenCms.getOrgUnitManager().getAllManageableProjects(m_adminCms, "", true);
        }
        for (CmsProject project : projects) {
            if (project.isWorkflowProject()) {
                if (isProjectEmpty(project)) {
                    m_adminCms.deleteProject(project.getUuid());
                }
            }
        }
    }

    /**
     * Removes a project if there are no longer any resources which have been last modified in that project.<p>
     *
     * @param project the project
     * @throws CmsException if something goes wrong
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
     * Helper method to check whether a project exists.<p>
     *
     * @param projectName the project name
     *
     * @return true if the project exists
     */
    protected boolean existsProject(String projectName) {

        try {
            m_adminCms.readProject(projectName);
            return true;
        } catch (CmsException e) {
            return false;
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
        OpenCms.getLocaleManager();
        Locale locale = CmsLocaleManager.getDefaultLocale();
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
        String dateString = format.format(date);
        String result = Messages.get().getBundle(locale).key(
            Messages.GUI_WORKFLOW_PROJECT_DESCRIPTION_2,
            user.getName(),
            dateString);
        return result;
    }

    /**
     * Generates the name for a new workflow project based on the user for whom it is created.<p>
     *
     * @param userCms the user's current CMS context
     *
     * @return the workflow project name
     */
    protected String generateProjectName(CmsObject userCms) {

        String projectName = generateProjectName(userCms, true);
        if (existsProject(projectName)) {
            projectName = generateProjectName(userCms, false);
        }
        return projectName;
    }

    /**
     * Generates the name for a new workflow project based on the user for whom it is created.<p>
     *
     * @param userCms the user's current CMS context
     * @param shortTime if true, the short time format will be used, else the medium time format
     *
     * @return the workflow project name
     */
    protected String generateProjectName(CmsObject userCms, boolean shortTime) {

        CmsUser user = userCms.getRequestContext().getCurrentUser();
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        OpenCms.getLocaleManager();
        Locale locale = CmsLocaleManager.getDefaultLocale();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        DateFormat timeFormat = DateFormat.getTimeInstance(shortTime ? DateFormat.SHORT : DateFormat.MEDIUM, locale);
        String dateStr = dateFormat.format(date) + " " + timeFormat.format(date);
        String username = user.getName();
        String result = Messages.get().getBundle(locale).key(Messages.GUI_WORKFLOW_PROJECT_NAME_2, username, dateStr);
        result = result.replaceAll("/", "|");

        return result;
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
     * Gets the resource  notification content path.<p>
     *
     * @return the resource notification content path
     */
    protected String getNotificationResource() {

        String result = getParameter(
            PARAM_NOTIFICATION_CONTENT,
            "/system/workplace/admin/notification/workflow-notification");
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

        List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
        String forcePublishLabel = Messages.get().getBundle(getLocale(userCms)).key(
            Messages.GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0);

        CmsWorkflowAction forcePublish = new CmsWorkflowAction(ACTION_FORCE_PUBLISH, forcePublishLabel, true, true);
        actions.add(forcePublish);
        return new CmsWorkflowResponse(
            false,
            Messages.get().getBundle(getLocale(userCms)).key(Messages.GUI_BROKEN_LINKS_0),
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
            new ArrayList<CmsWorkflowAction>(),
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

        List<CmsResource> resources = m_adminCms.readProjectView(project.getUuid(), CmsResourceState.STATE_KEEP);
        return resources.isEmpty();
    }

    /**
     * Checks whether the user for a given CMS context can manage workflow projects.<p>
     *
     * @param userCms the user CMS Context
     * @return true if this user can manage workflow projects
     *
     * @throws CmsException if something goes wrong
     */
    protected boolean isProjectManager(CmsObject userCms) throws CmsException {

        CmsGroup managerGroup = m_adminCms.readGroup(getWorkflowProjectManagerGroup());
        List<CmsGroup> groups = m_adminCms.getGroupsOfUser(
            userCms.getRequestContext().getCurrentUser().getName(),
            false);
        return groups.contains(managerGroup);
    }

    /**
     * Handles finished publish jobs by removing projects of resources in the publish job if they are empty workflow projects.<p>
     *
     * @param publishJob the finished published job
     */
    protected void onFinishPublishJob(CmsPublishJobRunning publishJob) {

        try {
            cleanupEmptyWorkflowProjects(null);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * This is called when a publish job is started.<p>
     *
     * @param publishJob the publish job being started
     */
    protected void onStartPublishJob(CmsPublishJobEnqueued publishJob) {

        // do nothing
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
            String linkHref = OpenCms.getLinkManager().getServerLink(
                userCms,
                "/system/workplace/commons/publish.jsp?"
                    + CmsPublishService.PARAM_PUBLISH_PROJECT_ID
                    + "="
                    + workflowProject.getUuid()
                    + "&"
                    + CmsPublishService.PARAM_CONFIRM
                    + "=true");
            CmsWorkflowNotification notification = new CmsWorkflowNotification(
                m_adminCms,
                userCms,
                recipient,
                getNotificationResource(),
                workflowProject,
                resources,
                linkHref);
            notification.send();
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
