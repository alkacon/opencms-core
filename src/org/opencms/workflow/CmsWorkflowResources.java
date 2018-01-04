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
import org.opencms.main.OpenCms;

import java.util.List;

/**
 * Set of workflow resources, and an optional workflow.<p>
 */
public class CmsWorkflowResources {

    /** Override workflow. */
    private CmsWorkflow m_overrideWorkflow;

    /** The workflow resources. */
    private List<CmsResource> m_workflowResources;

    /** If set, there are too many resources, and the value contains the approximate amount of resources. */
    private Integer m_tooManyCount;

    /**
     * Creates new instance.<p>
     *
     * @param workflowResources the workflow resources
     * @param overrideWorkflow the workflow to override the selected workflow
     * @param tooManyCount null if there are not too many resources, otherwise the approximate resource count
     */
    public CmsWorkflowResources(
        List<CmsResource> workflowResources,
        CmsWorkflow overrideWorkflow,
        Integer tooManyCount) {

        m_workflowResources = workflowResources;
        m_overrideWorkflow = overrideWorkflow;
        m_tooManyCount = tooManyCount;
    }

    /**
     * Gets a number that can be used as a lower bound for the number of publish resources if the list is too big (bigger than the workflow manager's resource limit).
     *
     * @return a lower bound for the number of publish resources
     */
    public int getLowerBoundForSize() {

        if (m_tooManyCount != null) {
            return m_tooManyCount.intValue();
        } else {
            return m_workflowResources.size();
        }
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
     * Gets the approximate amount of resources if there are too many resources.<p>
     *
     * @return the approximate amount of resources if there are too many
     */
    public Integer getTooManyCount() {

        return m_tooManyCount;
    }

    /**
     * Returns the workflowResources.<p>
     *
     * Note that if the isTooMany() method returns true, this method may return an empty list.
     *
     * @return the workflowResources
     */
    public List<CmsResource> getWorkflowResources() {

        return m_workflowResources;
    }

    /**
     * Returns true if there are too many resources.<p>
     *
     * @return true if there are too many resources
     */
    public boolean isTooMany() {

        return (m_tooManyCount != null)
            || ((m_workflowResources != null)
                && (m_workflowResources.size() > OpenCms.getWorkflowManager().getResourceLimit()));
    }

}
