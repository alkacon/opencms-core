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

import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.file.CmsResource;

import java.util.List;

/**
 * Set of workflow resources, and an optional workflow
 */
public class CmsWorkflowResources {

    /** Override workflow. */
    private CmsWorkflow m_overrideWorkflow;

    /** The workflow resources. */
    private List<CmsResource> m_workflowResources;

    /**
     * Creates new instance.<p>
     *
     * @param workflowResources the workflow resources
     */
    public CmsWorkflowResources(List<CmsResource> workflowResources) {

        this(workflowResources, null);
    }

    /**
     * Creates new instance.<p>
     *
     * @param workflowResources the workflow resources
     * @param overrideWorkflow the
     */
    public CmsWorkflowResources(List<CmsResource> workflowResources, CmsWorkflow overrideWorkflow) {

        m_workflowResources = workflowResources;
        m_overrideWorkflow = overrideWorkflow;
    }

    /**
     * Gets the override workflow, or null.<p>
     *
     * @return the override workflow, or null
     */
    public CmsWorkflow getOverrideWorkflow() {

        return m_overrideWorkflow;
    }

    /**
     * Returns the workflowResources.<p>
     *
     * @return the workflowResources
     */
    public List<CmsResource> getWorkflowResources() {

        return m_workflowResources;
    }

}
