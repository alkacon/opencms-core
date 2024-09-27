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

package org.opencms.ade.publish;

import org.opencms.ade.publish.CmsPublishRelationFinder.ResourceMap;
import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishGroupList;
import org.opencms.ade.publish.shared.CmsPublishListToken;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowActionParams;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsWorkflowResources;
import org.opencms.workflow.I_CmsPublishResourceFormatter;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The implementation of the publish service. <p>
 *
 * @since 8.0.0
 *
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** Name for the request parameter to control display of the confirmation dialog. */
    public static final String PARAM_CONFIRM = "confirm";

    /** The publish project id parameter name. */
    public static final String PARAM_PUBLISH_PROJECT_ID = "publishProjectId";

    /** The workflow id parameter name. */
    public static final String PARAM_WORKFLOW_ID = "workflowId";

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishService.class);

    /** The version id for serialization. */
    private static final long serialVersionUID = 3852074177607037076L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_PUB_OPTS_CACHE = "__OCMS_ADE_PUB_OPTS_CACHE__";

    /**
     * Fetches the publish data.<p>
     *
     * @param request the servlet request
     *
     * @return the publish data
     *
     * @throws CmsRpcException if something goes wrong
     */
    public static CmsPublishData prefetch(HttpServletRequest request) throws CmsRpcException {

        CmsPublishService srv = new CmsPublishService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsPublishData result = null;
        HashMap<String, String> params = Maps.newHashMap();
        params.put("prefetch", "true");
        try {
            result = srv.getInitData(params);
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * Wraps the project name in a message string.<p>
     *
     * @param cms the CMS context
     * @param name the project name
     *
     * @return the message for the given project name
     */
    public static String wrapProjectName(CmsObject cms, String name) {

        return Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
            Messages.GUI_NORMAL_PROJECT_1,
            name);
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#executeAction(org.opencms.ade.publish.shared.CmsWorkflowAction, org.opencms.ade.publish.shared.CmsWorkflowActionParams)
     */
    public CmsWorkflowResponse executeAction(CmsWorkflowAction action, CmsWorkflowActionParams params)
    throws CmsRpcException {

        CmsWorkflowResponse response = null;
        try {
            CmsObject cms = getCmsObject();
            if (params.getToken() == null) {
                CmsPublishOptions options = getCachedOptions();
                CmsPublish pub = new CmsPublish(cms, options);
                List<CmsResource> publishResources = idsToResources(cms, params.getPublishIds());
                Set<CmsUUID> toRemove = new HashSet<CmsUUID>(params.getRemoveIds());
                pub.removeResourcesFromPublishList(toRemove);
                response = OpenCms.getWorkflowManager().executeAction(cms, action, options, publishResources);
            } else {
                response = OpenCms.getWorkflowManager().executeAction(cms, action, params.getToken());
            }
        } catch (Throwable e) {
            error(e);
        }
        return response;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getInitData(java.util.HashMap)
     */
    public CmsPublishData getInitData(HashMap<String, String> params) throws CmsRpcException {

        CmsPublishData result = null;
        CmsObject cms = getCmsObject();
        String closeLink = getRequest().getParameter(CmsDialog.PARAM_CLOSELINK);
        if ((closeLink == null) && "true".equals(params.get("prefetch"))) {
            closeLink = CmsVaadinUtils.getWorkplaceLink();
        }
        String confirmStr = getRequest().getParameter(PARAM_CONFIRM);
        boolean confirm = Boolean.parseBoolean(confirmStr);
        String workflowId = getRequest().getParameter(PARAM_WORKFLOW_ID);
        String projectParam = getRequest().getParameter(PARAM_PUBLISH_PROJECT_ID);
        String filesParam = getRequest().getParameter(CmsWorkplace.PARAM_RESOURCELIST);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(filesParam)) {
            filesParam = getRequest().getParameter(CmsDialog.PARAM_RESOURCE);
        }
        List<String> pathList = Lists.newArrayList();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(filesParam)) {
            pathList = CmsStringUtil.splitAsList(filesParam, "|");
        }
        try {
            result = getPublishData(cms, params, workflowId, projectParam, pathList, closeLink, confirm);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Gets the publish data for the given parameters.<p>
     *
     * @param cms the CMS context
     * @param params other publish parameters
     * @param workflowId the workflow id
     * @param projectParam the project
     * @param pathList the list of direct publish resource site paths
     * @param closeLink the close link
     * @param confirm true if confirmation dialog should be displayed after closing the dialog
     *
     * @return the publish data
     *
     * @throws Exception if something goes wrong
     */
    public CmsPublishData getPublishData(
        CmsObject cms,
        HashMap<String, String> params,
        String workflowId,
        String projectParam,
        List<String> pathList,
        String closeLink,
        boolean confirm)
    throws Exception {

        CmsPublishData result;
        boolean canOverrideWorkflow = true;
        Map<String, CmsWorkflow> workflows = OpenCms.getWorkflowManager().getWorkflows(cms);
        if (workflows.isEmpty()) {
            throw new Exception("No workflow available for the current user");
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(workflowId) || !workflows.containsKey(workflowId)) {
            workflowId = getLastWorkflowForUser();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(workflowId) || !workflows.containsKey(workflowId)) {
                workflowId = workflows.values().iterator().next().getId();
            }
        } else {
            canOverrideWorkflow = false;
        }
        setLastWorkflowForUser(workflowId);

        // need to put this into params here so that the virtual project for direct publishing is included in the result of getManageableProjects()
        if (!pathList.isEmpty()) {
            params.put(CmsPublishOptions.PARAM_FILES, CmsStringUtil.listAsString(pathList, "|"));
        }

        boolean useCurrentPageAsDefault = params.containsKey(CmsPublishOptions.PARAM_START_WITH_CURRENT_PAGE);
        CmsPublishOptions options = getCachedOptions();
        List<CmsProjectBean> projects = OpenCms.getWorkflowManager().getManageableProjects(cms, params);
        Set<CmsUUID> availableProjectIds = Sets.newHashSet();
        for (CmsProjectBean projectBean : projects) {
            availableProjectIds.add(projectBean.getId());
        }
        CmsUUID defaultProjectId = CmsUUID.getNullUUID();
        if (useCurrentPageAsDefault && availableProjectIds.contains(CmsCurrentPageProject.ID)) {
            defaultProjectId = CmsCurrentPageProject.ID;
        }

        boolean foundProject = false;
        CmsUUID selectedProject = null;
        if (!pathList.isEmpty()) {
            int enableIncludeContents = 0;
            if (pathList.size() > 1) {
                enableIncludeContents = 2;
            } else {
                try {
                    if (cms.readResource(pathList.get(0), CmsResourceFilter.ALL).isFolder()) {
                        enableIncludeContents = 1;
                    }
                } catch (CmsException e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            params.put(CmsPublishOptions.PARAM_ENABLE_INCLUDE_CONTENTS, "" + enableIncludeContents);
            params.put(CmsPublishOptions.PARAM_INCLUDE_CONTENTS, Boolean.TRUE.toString());
            selectedProject = CmsDirectPublishProject.ID;
            foundProject = true;
        } else {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(projectParam) && CmsUUID.isValidUUID(projectParam)) {
                selectedProject = new CmsUUID(projectParam);
                // check if the selected project is a manageable project
                for (CmsProjectBean project : projects) {
                    if (selectedProject.equals(project.getId())) {
                        foundProject = true;
                        if (project.isWorkflowProject()) {
                            canOverrideWorkflow = false;
                            workflowId = OpenCms.getWorkflowManager().getWorkflowForWorkflowProject(selectedProject);
                        }
                        break;
                    }
                }
            }
            if (!foundProject) {
                selectedProject = options.getProjectId();
                if (selectedProject == null) {
                    selectedProject = defaultProjectId;
                    foundProject = true;
                } else {
                    // check if the selected project is a manageable project
                    for (CmsProjectBean project : projects) {
                        if (selectedProject.equals(project.getId())) {
                            foundProject = true;
                            if (project.isWorkflowProject()) {
                                canOverrideWorkflow = false;
                                workflowId = OpenCms.getWorkflowManager().getWorkflowForWorkflowProject(
                                    selectedProject);
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (foundProject) {
            options.setProjectId(selectedProject);
        } else {
            options.setProjectId(CmsUUID.getNullUUID());
        }

        options.setParameters(params);
        result = new CmsPublishData(
            options,
            projects,
            getResourceGroups(workflows.get(workflowId), options, canOverrideWorkflow),
            workflows,
            workflowId);
        result.setCloseLink(closeLink);
        result.setShowConfirmation(confirm);
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getResourceGroups(org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions, boolean)
     */
    public CmsPublishGroupList getResourceGroups(
        CmsWorkflow workflow,
        CmsPublishOptions options,
        boolean projectChanged)
    throws CmsRpcException {

        List<CmsPublishGroup> results = null;
        CmsObject cms = getCmsObject();
        String overrideWorkflowId = null;
        try {
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            I_CmsWorkflowManager workflowManager = OpenCms.getWorkflowManager();
            I_CmsPublishResourceFormatter formatter = workflowManager.createFormatter(cms, workflow, options);
            CmsWorkflowResources workflowResources = null;
            workflowResources = workflowManager.getWorkflowResources(cms, workflow, options, projectChanged, false);
            if (workflowResources.getOverrideWorkflow() != null) {
                overrideWorkflowId = workflowResources.getOverrideWorkflow().getId();
                formatter = workflowManager.createFormatter(cms, workflowResources.getOverrideWorkflow(), options);
            }
            if (workflowResources.isTooMany()) {
                // too many resources, send a publish list token to the client which can be used later to restore the resource list
                CmsPublishListToken token = workflowManager.getPublishListToken(cms, workflow, options);
                CmsPublishGroupList result = new CmsPublishGroupList(token);
                result.setTooManyResourcesMessage(
                    Messages.get().getBundle(locale).key(
                        Messages.GUI_TOO_MANY_RESOURCES_2,
                        "" + workflowResources.getLowerBoundForSize(),
                        "" + OpenCms.getWorkflowManager().getResourceLimit()));
                return result;
            }
            I_CmsVirtualProject virtualProject = workflowManager.getRealOrVirtualProject(options.getProjectId());
            I_CmsPublishRelatedResourceProvider relProvider = virtualProject.getRelatedResourceProvider(
                getCmsObject(),
                options);
            ResourceMap resourcesAndRelated = getResourcesAndRelated(
                workflowResources.getWorkflowResources(),
                options.isIncludeRelated(),
                options.isIncludeSiblings(),
                (options.getProjectId() == null) || options.getProjectId().isNullUUID(),
                relProvider);
            formatter.initialize(options, resourcesAndRelated);
            List<CmsPublishResource> publishResources = formatter.getPublishResources();
            A_CmsPublishGroupHelper<CmsPublishResource, CmsPublishGroup> groupHelper;
            boolean autoSelectable = true;
            if ((options.getProjectId() == null) || options.getProjectId().isNullUUID()) {
                groupHelper = new CmsDefaultPublishGroupHelper(locale);
            } else {
                String title = "";
                CmsProjectBean projectBean = virtualProject.getProjectBean(cms, options.getParameters());
                title = projectBean.getDefaultGroupName();
                if (title == null) {
                    title = "";
                }
                autoSelectable = virtualProject.isAutoSelectable();
                groupHelper = new CmsSinglePublishGroupHelper(locale, title);
            }
            results = groupHelper.getGroups(publishResources);
            for (CmsPublishGroup group : results) {
                group.setAutoSelectable(autoSelectable);
            }
            setCachedOptions(options);
            setLastWorkflowForUser(workflow.getId());
        } catch (Throwable e) {
            error(e);
        }
        CmsPublishGroupList wrapper = new CmsPublishGroupList();
        wrapper.setOverrideWorkflowId(overrideWorkflowId);
        wrapper.setGroups(results);
        return wrapper;
    }

    /**
     * Retrieves the publish options.<p>
     *
     * @return the publish options last used
     *
     * @throws CmsRpcException if something goes wrong
     */
    public CmsPublishOptions getResourceOptions() throws CmsRpcException {

        CmsPublishOptions result = null;
        try {
            result = getCachedOptions();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Adds siblings to a set of publish resources.<p>
     *
     * @param publishResources the set to which siblings should be added
     */
    protected void addSiblings(Set<CmsResource> publishResources) {

        List<CmsResource> toAdd = Lists.newArrayList();
        for (CmsResource resource : publishResources) {
            if (!resource.getState().isUnchanged()) {
                try {
                    List<CmsResource> changedSiblings = getCmsObject().readSiblings(
                        resource,
                        CmsResourceFilter.ALL_MODIFIED);
                    toAdd.addAll(changedSiblings);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        publishResources.addAll(toAdd);
    }

    /**
     * Returns the cached publish options, creating it if it doesn't already exist.<p>
     *
     * @return the cached publish options
     */
    private CmsPublishOptions getCachedOptions() {

        CmsPublishOptions cache = (CmsPublishOptions)getRequest().getSession().getAttribute(
            SESSION_ATTR_ADE_PUB_OPTS_CACHE);
        if (cache == null) {
            CmsUserSettings settings = new CmsUserSettings(getCmsObject());
            cache = new CmsPublishOptions();
            cache.setIncludeSiblings(settings.getDialogPublishSiblings());
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, cache);
        }
        return cache;

    }

    /**
     * Returns the id of the last used workflow for the current user.<p>
     *
     * @return the workflow id
     */
    private String getLastWorkflowForUser() {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        return (String)user.getAdditionalInfo(PARAM_WORKFLOW_ID);
    }

    /**
     * Gets the resource map containing the publish resources together with their related resources.<p>
     *
     * @param resources the base list of publish resources
     * @param includeRelated flag to control whether related resources should be included
     * @param includeSiblings flag to control whether siblings should be included
     * @param keepOriginalUnchangedResources flag which determines whether unchanged resources in the original resource list should be kept or removed
     * @param relProvider the provider for additional related resources
     *
     * @return the resources together with their related resources
     */
    private ResourceMap getResourcesAndRelated(
        List<CmsResource> resources,
        boolean includeRelated,
        boolean includeSiblings,
        boolean keepOriginalUnchangedResources,
        I_CmsPublishRelatedResourceProvider relProvider) {

        Set<CmsResource> publishResources = new HashSet<CmsResource>();
        publishResources.addAll(resources);
        if (includeSiblings) {
            addSiblings(publishResources);
        }
        Set<CmsResource> visibleResources = publishResources.stream().filter(
            resource -> getCmsObject().existsResource(
                resource.getStructureId(),
                CmsResourceFilter.ALL.addRequireVisible())

        ).collect(Collectors.toSet());
        ResourceMap result;
        if (includeRelated) {
            CmsPublishRelationFinder relationFinder = new CmsPublishRelationFinder(
                getCmsObject(),
                visibleResources,
                keepOriginalUnchangedResources,
                relProvider);

            result = relationFinder.getPublishRelatedResources();
        } else {
            result = new ResourceMap();
            for (CmsResource resource : visibleResources) {
                if (keepOriginalUnchangedResources || !resource.getState().isUnchanged()) {
                    result.put(resource, new HashSet<CmsResource>());
                }
            }
        }
        return result;
    }

    /**
     * Converts a list of IDs to resources.<p>
     *
     * @param cms the CmObject used for reading the resources
     * @param ids the list of IDs
     *
     * @return a list of resources
     */
    private List<CmsResource> idsToResources(CmsObject cms, List<CmsUUID> ids) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsUUID id : ids) {
            try {
                CmsResource resource = cms.readResource(id, CmsResourceFilter.ALL);
                result.add(resource);
            } catch (CmsException e) {
                // should never happen
                logError(e);
            }
        }
        return result;
    }

    /**
     * Saves the given options to the session.<p>
     *
     * @param options the options to save
     */
    private void setCachedOptions(CmsPublishOptions options) {

        getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, options);
    }

    /**
     * Writes the id of the last used workflow to the current user.<p>
     *
     * @param workflowId the workflow id
     *
     * @throws CmsException if something goes wrong writing the user object
     */
    private void setLastWorkflowForUser(String workflowId) throws CmsException {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        user.setAdditionalInfo(PARAM_WORKFLOW_ID, workflowId);
        getCmsObject().writeUser(user);
    }
}
