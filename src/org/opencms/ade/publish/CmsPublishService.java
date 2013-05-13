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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The implementation of the publish service.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** The publish project id parameter name. */
    public static final String PARAM_PUBLISH_PROJECT_ID = "publishProjectId";

    /** The workflow id parameter name. */
    public static final String PARAM_WORKFLOW_ID = "workflowId";

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
        try {
            result = srv.getInitData();
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#executeAction(java.util.List, java.util.List, org.opencms.ade.publish.shared.CmsWorkflowAction)
     */
    public CmsWorkflowResponse executeAction(List<CmsUUID> toPublish, List<CmsUUID> toRemove, CmsWorkflowAction action)
    throws CmsRpcException {

        CmsWorkflowResponse response = null;
        try {
            CmsObject cms = getCmsObject();
            CmsPublishOptions options = getCachedOptions();
            CmsPublish pub = new CmsPublish(cms, options);
            List<CmsResource> publishResources = idsToResources(cms, toPublish);
            pub.removeResourcesFromPublishList(toRemove);
            response = OpenCms.getWorkflowManager().executeAction(cms, action, options, publishResources);

        } catch (Throwable e) {
            error(e);
        }
        return response;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getInitData()
     */
    public CmsPublishData getInitData() throws CmsRpcException {

        CmsPublishData result = null;
        CmsObject cms = getCmsObject();
        try {
            Map<String, CmsWorkflow> workflows = OpenCms.getWorkflowManager().getWorkflows(cms);
            if (workflows.isEmpty()) {
                throw new Exception("No workflow available for the current user");
            }
            String workflowId = getRequest().getParameter(PARAM_WORKFLOW_ID);

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(workflowId) || !workflows.containsKey(workflowId)) {
                workflowId = getLastWorklowForUser();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(workflowId) || !workflows.containsKey(workflowId)) {
                    workflowId = workflows.values().iterator().next().getId();
                }
            }
            setLastWorkflowForUser(workflowId);
            String projectParam = getRequest().getParameter(PARAM_PUBLISH_PROJECT_ID);
            CmsPublishOptions options = getCachedOptions();
            List<CmsProjectBean> projects = getProjects();
            boolean foundProject = false;
            CmsUUID selectedProject = null;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(projectParam) && CmsUUID.isValidUUID(projectParam)) {
                selectedProject = new CmsUUID(projectParam);
                // check if the selected project is a manageable project
                for (CmsProjectBean project : projects) {
                    if (selectedProject.equals(project.getId())) {
                        foundProject = true;
                        break;
                    }
                }
            }
            if (!foundProject) {
                selectedProject = options.getProjectId();
                // check if the selected project is a manageable project
                for (CmsProjectBean project : projects) {
                    if (selectedProject.equals(project.getId())) {
                        foundProject = true;
                        break;
                    }
                }
            }
            if (foundProject) {
                options.setProjectId(selectedProject);
            } else {
                options.setProjectId(CmsUUID.getNullUUID());
            }
            result = new CmsPublishData(
                options,
                projects,
                getResourceGroups(workflows.get(workflowId), options),
                workflows,
                workflowId);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getProjects()
     */
    public List<CmsProjectBean> getProjects() throws CmsRpcException {

        List<CmsProjectBean> result = null;
        try {
            result = new CmsPublish(getCmsObject()).getManageableProjects();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getResourceGroups(org.opencms.ade.publish.shared.CmsWorkflow,org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsPublishGroup> getResourceGroups(CmsWorkflow workflow, CmsPublishOptions options)
    throws CmsRpcException {

        List<CmsPublishGroup> results = null;
        CmsObject cms = getCmsObject();
        try {
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            List<CmsPublishResource> publishResources = OpenCms.getWorkflowManager().getWorkflowPublishResources(
                cms,
                workflow,
                options);
            for (CmsPublishResource publishResource : publishResources) {
                checkPreview(publishResource);
            }
            A_CmsPublishGroupHelper<CmsPublishResource, CmsPublishGroup> groupHelper = new CmsDefaultPublishGroupHelper(
                locale);
            results = groupHelper.getGroups(publishResources);
            setCachedOptions(options);
        } catch (Throwable e) {
            error(e);
        }
        return results;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getResourceOptions()
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
     * Checks if there is any reason to deactivate the preview function.<p>
     * 
     * @param publishResource the publish resource to check
     */
    private void checkPreview(CmsPublishResource publishResource) {

        CmsObject cms = getCmsObject();
        String noPreviewReason = null;
        try {
            CmsResource resource = cms.readResource(publishResource.getId(), CmsResourceFilter.ONLY_VISIBLE);
            noPreviewReason = CmsVfsService.getNoPreviewReason(cms, resource);
        } catch (CmsException e) {
            noPreviewReason = e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        }
        if (noPreviewReason != null) {
            if (publishResource.getInfo() == null) {
                publishResource.setInfo(new CmsPublishResourceInfo(null, null));
            }
            publishResource.getInfo().setNoPreviewReason(noPreviewReason);
        }
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
            cache = new CmsPublishOptions();
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, cache);
        }
        return cache;

    }

    /**
     * Returns the id of the last used workflow for the current user.<p>
     * 
     * @return the workflow id
     */
    private String getLastWorklowForUser() {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        return (String)user.getAdditionalInfo(PARAM_WORKFLOW_ID);
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
