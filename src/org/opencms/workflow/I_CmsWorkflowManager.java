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

import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;
import java.util.Map;

/**
 * Workflow manager interface.<p>
 */
public interface I_CmsWorkflowManager {

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
     * Gets the parameters of the workflow manager.<p>
     * 
     * @return the configuration parameters of the workflow manager 
     */
    Map<String, String> getParameters();

    /**
     * Returns the publish resource beans for the given workflow and project.<p>
     * 
     * @param cms the user cms context
     * @param workflow the workflow
     * @param options the resource options
     * 
     * @return the workflow publish resource beans
     */
    List<CmsPublishResource> getWorkflowPublishResources(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options);

    /**
     * Returns the resources for the given workflow and project.<p>
     * 
     * @param cms the user cms context
     * @param workflow the workflow
     * @param options the resource options
     * 
     * @return the workflow resources
     */
    List<CmsResource> getWorkflowResources(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options);

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
