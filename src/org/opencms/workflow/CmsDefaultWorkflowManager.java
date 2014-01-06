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

import org.opencms.ade.publish.CmsCurrentPageProject;
import org.opencms.ade.publish.CmsMyChangesProject;
import org.opencms.ade.publish.CmsPublish;
import org.opencms.ade.publish.CmsRealProjectVirtualWrapper;
import org.opencms.ade.publish.I_CmsVirtualProject;
import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishListToken;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * The default implementation of the workflow manager interface, which offers only publish functionality.<p>
 */
public class CmsDefaultWorkflowManager extends A_CmsWorkflowManager {

    /** The forced publish workflow action. */
    public static final String ACTION_FORCE_PUBLISH = "forcepublish";

    /** The publish workflow action. */
    public static final String ACTION_PUBLISH = "publish";

    /** Default value for the maximum number of resources in the initial publish list. */
    public static int DEFAULT_RESOURCE_LIMIT = 1000;

    /** The parameter name for the resource limit. */
    public static final String PARAM_RESOURCE_LIMIT = "resourceLimit";

    /** The name for the publish action. */
    public static final String WORKFLOW_PUBLISH = "WORKFLOW_PUBLISH";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultWorkflowManager.class);

    /** The map of registered virtual  projects. */
    protected Map<CmsUUID, I_CmsVirtualProject> m_virtualProjects = Maps.newHashMap();

    /** The number of resources in the initial publish list above which the resources are not being displayed to the user. */
    private int m_resourceLimit = DEFAULT_RESOURCE_LIMIT;

    /** 
     * Constructor.<p>
     */
    public CmsDefaultWorkflowManager() {

        m_virtualProjects.put(CmsCurrentPageProject.ID, CmsCurrentPageProject.INSTANCE);
        m_virtualProjects.put(CmsMyChangesProject.ID, CmsMyChangesProject.INSTANCE);
    }

    /**
     * Creates a project bean from a real project.<p>
     * 
     * @param cms the CMS context 
     * @param project the project
     *  
     * @return the bean containing the project information 
     */
    public static CmsProjectBean createProjectBeanFromProject(CmsObject cms, CmsProject project) {

        CmsProjectBean manProj = new CmsProjectBean(
            project.getUuid(),
            project.getType().getMode(),
            org.opencms.ade.publish.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                org.opencms.ade.publish.Messages.GUI_NORMAL_PROJECT_1,
                getOuAwareName(cms, project.getName())),
            project.getDescription());
        return manProj;
    }

    /**
     * Returns the simple name if the ou is the same as the current user's ou.<p>
     * 
     * @param cms the CMS context 
     * @param name the fully qualified name to check
     * 
     * @return the simple name if the ou is the same as the current user's ou
     */
    protected static String getOuAwareName(CmsObject cms, String name) {

        String ou = CmsOrganizationalUnit.getParentFqn(name);
        if (ou.equals(cms.getRequestContext().getCurrentUser().getOuFqn())) {
            return CmsOrganizationalUnit.getSimpleName(name);
        }
        return CmsOrganizationalUnit.SEPARATOR + name;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#createFormatter(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public I_CmsPublishResourceFormatter createFormatter(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options) {

        CmsDefaultPublishResourceFormatter formatter = new CmsDefaultPublishResourceFormatter(cms);
        return formatter;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#executeAction(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflowAction, org.opencms.ade.publish.shared.CmsPublishListToken)
     */
    public CmsWorkflowResponse executeAction(CmsObject cms, CmsWorkflowAction action, CmsPublishListToken token)
    throws CmsException {

        if (action.getAction().equals(CmsWorkflowAction.ACTION_CANCEL)) {
            // Don't need to get the resource list for canceling 
            return new CmsWorkflowResponse(true, action.getAction(), null, null, null);
        }
        List<CmsResource> resources = getWorkflowResources(cms, token.getWorkflow(), token.getOptions());
        return executeAction(cms, action, token.getOptions(), resources);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#executeAction(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflowAction, org.opencms.ade.publish.shared.CmsPublishOptions, java.util.List)
     */
    public CmsWorkflowResponse executeAction(
        CmsObject userCms,
        CmsWorkflowAction action,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        String actionKey = action.getAction();
        if (CmsWorkflowAction.ACTION_CANCEL.equals(actionKey)) {
            return new CmsWorkflowResponse(true, actionKey, null, null, null);
        } else if (ACTION_PUBLISH.equals(actionKey)) {
            return actionPublish(userCms, options, resources);
        } else if (ACTION_FORCE_PUBLISH.equals(actionKey)) {
            return actionForcePublish(userCms, options, resources);
        }
        throw new CmsInvalidActionException(actionKey);
    }

    /**
     * Gets the localized label for a given CMS context and key.<p>
     *  
     * @param cms the CMS context 
     * @param key the localization key 
     * 
     * @return the localized label 
     */
    public String getLabel(CmsObject cms, String key) {

        CmsMessages messages = Messages.get().getBundle(getLocale(cms));
        return messages.key(key);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getManageableProjects(org.opencms.file.CmsObject, java.util.Map)
     */
    public List<CmsProjectBean> getManageableProjects(CmsObject cms, Map<String, String> params) {

        List<CmsProjectBean> manProjs = new ArrayList<CmsProjectBean>();

        List<CmsProject> projects;
        try {
            projects = OpenCms.getOrgUnitManager().getAllManageableProjects(cms, "", true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return manProjs;
        }

        for (CmsProject project : projects) {
            CmsProjectBean manProj = createProjectBeanFromProject(cms, project);
            manProjs.add(manProj);
        }

        for (I_CmsVirtualProject handler : m_virtualProjects.values()) {
            CmsProjectBean projectBean = handler.getProjectBean(cms, params);
            if (projectBean != null) {
                manProjs.add(projectBean);
            }
        }

        return manProjs;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getPublishListToken(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public CmsPublishListToken getPublishListToken(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options) {

        return new CmsPublishListToken(workflow, options);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getRealOrVirtualProject(org.opencms.util.CmsUUID)
     */
    public I_CmsVirtualProject getRealOrVirtualProject(CmsUUID projectId) {

        I_CmsVirtualProject project = m_virtualProjects.get(projectId);
        if (project == null) {
            project = new CmsRealProjectVirtualWrapper(projectId);
        }
        return project;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getResourceLimit()
     */
    public int getResourceLimit() {

        return m_resourceLimit;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowResources(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsResource> getWorkflowResources(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options) {

        try {
            List<CmsResource> rawResourceList = new ArrayList<CmsResource>();
            I_CmsVirtualProject projectHandler = null;
            projectHandler = getRealOrVirtualProject(options.getProjectId());
            if (projectHandler != null) {
                rawResourceList = projectHandler.getResources(cms, options.getParameters(), workflow.getId());
                return rawResourceList;
            }
            return rawResourceList;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflows(org.opencms.file.CmsObject)
     */
    public Map<String, CmsWorkflow> getWorkflows(CmsObject cms) {

        Map<String, CmsWorkflow> result = new LinkedHashMap<String, CmsWorkflow>();
        List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
        String publishLabel = getLabel(cms, Messages.GUI_WORKFLOW_ACTION_PUBLISH_0);
        CmsWorkflowAction publishAction = new CmsWorkflowAction(ACTION_PUBLISH, publishLabel, true, true);
        actions.add(publishAction);
        String workflowLabel = getLabel(cms, Messages.GUI_WORKFLOW_PUBLISH_0);
        CmsWorkflow publishWorkflow = new CmsWorkflow(WORKFLOW_PUBLISH, workflowLabel, actions);
        result.put(WORKFLOW_PUBLISH, publishWorkflow);
        return result;
    }

    /**
     * @see org.opencms.workflow.A_CmsWorkflowManager#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject adminCms) {

        super.initialize(adminCms);
        String resourceLimitStr = getParameter(PARAM_RESOURCE_LIMIT, "invalid").trim();
        try {
            m_resourceLimit = Integer.parseInt(resourceLimitStr);
        } catch (NumberFormatException e) {
            // ignore, resource limit will remain at the default setting  
        }
    }

    /**
     * The implementation of the "forcepublish" workflow action.<p>
     * 
     * @param userCms the user CMS context 
     * @param resources the resources which the action should process 
     * @param options the publish options to use 
     * @return the workflow response
     *  
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionForcePublish(
        CmsObject userCms,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms, options.getParameters());
        publish.publishResources(resources);
        CmsWorkflowResponse response = new CmsWorkflowResponse(
            true,
            "",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowAction>(),
            null);
        return response;
    }

    /**
     * The implementation of the "publish" workflow action.<p>
     * 
     * @param userCms the user CMS context 
     * @param options the publish options 
     * @param resources the resources which the action should process 
     * 
     * @return the workflow response 
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionPublish(
        CmsObject userCms,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms, options);
        List<CmsPublishResource> brokenResources = publish.getBrokenResources(resources);
        if (brokenResources.size() == 0) {
            publish.publishResources(resources);
            CmsWorkflowResponse response = new CmsWorkflowResponse(
                true,
                "",
                new ArrayList<CmsPublishResource>(),
                new ArrayList<CmsWorkflowAction>(),
                null);
            return response;
        } else {
            String brokenResourcesLabel = getLabel(userCms, Messages.GUI_BROKEN_LINKS_0);
            boolean canForcePublish = OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
                || OpenCms.getRoleManager().hasRole(userCms, CmsRole.VFS_MANAGER);
            List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
            if (canForcePublish) {
                String forceLabel = getLabel(userCms, Messages.GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0);
                actions.add(new CmsWorkflowAction(ACTION_FORCE_PUBLISH, forceLabel, true, true));
            }
            CmsWorkflowResponse response = new CmsWorkflowResponse(
                false,
                brokenResourcesLabel,
                brokenResources,
                actions,
                null);
            return response;
        }
    }
}
