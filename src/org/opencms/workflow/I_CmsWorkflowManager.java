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

import org.opencms.ade.publish.I_CmsVirtualProject;
import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishListToken;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

/**
 * Workflow manager interface.<p>
 */
public interface I_CmsWorkflowManager {

    /**
     * Creates the formatter for formatting the resources to be displayed to the user.<p>
     * @param cms the CMS context to use
     * @param workflow the current workflow
     * @param options the publish options
     *
     * @return the publish resource formatter to use
     */
    I_CmsPublishResourceFormatter createFormatter(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options);

    /**
     * Executes a workflow action for a publish list token instead of a resource list.<p>
     *
     * @param cms the CMS context to use
     * @param action the action to perform
     * @param token the publish list token to use
     *
     * @return the workflow response
     * @throws CmsException if something goes wrong

     */
    CmsWorkflowResponse executeAction(CmsObject cms, CmsWorkflowAction action, CmsPublishListToken token)
    throws CmsException;

    /**
     * Executes a workflow action in the context of the current user.<p>
     *
     * @param userCms the current user's CMS context
     * @param action the workflow action
     * @param options the publish options
     * @param resources the resources to be processed
     *
     * @return the workflow response for the executed action
     *
     * @throws CmsException if something goes wrong
     */
    CmsWorkflowResponse executeAction(
        CmsObject userCms,
        CmsWorkflowAction action,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException;

    /**
     * Returns the current user's manageable projects.<p>
     *
     * @param cms the CMS context to use
     * @param params the publish parameters
     *
     * @return the current user's manageable projects
     */
    List<CmsProjectBean> getManageableProjects(CmsObject cms, Map<String, String> params);

    /**
     * Gets the parameters of the workflow manager.<p>
     *
     * @return the configuration parameters of the workflow manager
     */
    Map<String, String> getParameters();

    /**
     * Gets a publish list token for the given parameters which can be used later to reconstruct the publish list.<p>
     *
     * @param cms the CMS context to use
     * @param workflow the workflow
     * @param options the publish options
     *
     * @return the publish list token
     */
    CmsPublishListToken getPublishListToken(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options);

    /**
     * Gets the virtual project object identified by the given id.<p>
     *
     * @param projectId the virtual project id
     * @return the virtual project object
     */
    I_CmsVirtualProject getRealOrVirtualProject(CmsUUID projectId);

    /**
     * Gets the resource limit.<p>
     *
     * Publish lists which exceed this limit (counted before adding any related resources, siblings etc.) are not displayed to the user.<p>
     *
     * @return the resource limit
     */
    int getResourceLimit();

    /**
     * Gets the workflow id which should be used for a given workflow project.<p>
     *
     * @param projectId the project id
     *
     * @return the workflow id for the project
     */
    String getWorkflowForWorkflowProject(CmsUUID projectId);

    /**
     * Returns the resources for the given workflow and project.<p>
     *
     * @param cms the user cms context
     * @param workflow the workflow
     * @param options the resource options
     * @param canOverride flag to indicate whether the workflow manager should be able to override the selected workflow
     *
     * @return the workflow resources
     */
    CmsWorkflowResources getWorkflowResources(
        CmsObject cms,
        CmsWorkflow workflow,
        CmsPublishOptions options,
        boolean canOverride);

    /**
     * Returns the available workflows for the current user.<p>
     *
     * @param cms  the user cms context
     *
     * @return the available workflows
     */
    Map<String, CmsWorkflow> getWorkflows(CmsObject cms);

    /**
     * Initializes this workflow manager instance.<p>
     *
     * @param adminCms the CMS context with admin privileges
     */
    void initialize(CmsObject adminCms);

    /**
     * Sets the configuration parameters of the workflow manager.<p>
     *
     * @param parameters the map of configuration parameters
     */
    void setParameters(Map<String, String> parameters);

}
