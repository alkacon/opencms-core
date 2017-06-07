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

package org.opencms.ade.publish.shared;

import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The result of a workflow action.<p>
 */
public class CmsWorkflowResponse implements IsSerializable {

    /** An internal workflow id. */
    private CmsUUID m_workflowId;

    /** The list of workflow actions which should be available next in case of failure. */
    private List<CmsWorkflowAction> m_availableActions;

    /** A flag indicating whether the workflow action was successful. */
    private boolean m_success;

    /** The message text which should be displayed to the user in case of failure. */
    private String m_message;

    /** A list of resources which should be presented to the user in case of failure. */
    private List<CmsPublishResource> m_resources;

    /**
     * Creates a new workflow response object.<p>
     *
     * @param isSuccess a flag indicating whether the workflow action was successful
     * @param message the message which should be displayed to the user in case of failure
     * @param resources the resources which should be presented to the user in case of  failure
     * @param availableActions the actions which should be possible for the user in case of failure
     * @param workflowId the internal workflow id
     */
    public CmsWorkflowResponse(
        boolean isSuccess,
        String message,
        List<CmsPublishResource> resources,
        List<CmsWorkflowAction> availableActions,
        CmsUUID workflowId) {

        m_success = isSuccess;
        m_message = message;
        m_resources = resources;
        m_availableActions = availableActions;
        m_workflowId = workflowId;
    }

    /**
     * Constructor needed for serialization only.<p>
     */
    protected CmsWorkflowResponse() {

        // nothing to do
    }

    /**
     * Returns the list of actions which are available next.<p>
     *
     * @return a list of beans representing the next possible workflow actions
     */
    public List<CmsWorkflowAction> getAvailableActions() {

        return m_availableActions;
    }

    /**
     * Gets the message which should be displayed to the user in case of failure.<p>
     *
     * @return the message which should be displayed to the user in case of failure
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Gets the list of resources which should be presented to the user in case of failure.<p>
     *
     * @return a list of resources
     */
    public List<CmsPublishResource> getResources() {

        return m_resources;
    }

    /**
     * Gets the internal workflow id.<p>
     *
     * @return the internal workflow id
     */
    public CmsUUID getWorkflowId() {

        return m_workflowId;
    }

    /**
     * Returns true if the action for which this object is the workflow response was successful or not.<p>
     *
     * @return true if the workflow action was successful
     */
    public boolean isSuccess() {

        return m_success;
    }
}
