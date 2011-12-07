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

package org.opencms.ade.publish.shared;

import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CmsWorkflowResponse implements IsSerializable {

    private List<CmsWorkflowActionBean> m_availableActions;

    private String m_message;

    private List<CmsPublishResource> m_resources;

    private boolean m_success;

    private CmsUUID m_workflowId;

    public CmsWorkflowResponse(
        boolean isSuccess,
        String message,
        List<CmsPublishResource> resources,
        List<CmsWorkflowActionBean> availableActions,
        CmsUUID workflowId) {

        m_success = isSuccess;
        m_message = message;
        m_resources = resources;
        m_availableActions = availableActions;
        m_workflowId = workflowId;
    }

    protected CmsWorkflowResponse() {

    }

    public List<CmsWorkflowActionBean> getAvailableActions() {

        return m_availableActions;
    }

    public String getMessage() {

        return m_message;
    }

    public List<CmsPublishResource> getResources() {

        return m_resources;
    }

    public CmsUUID getWorkflowId() {

        return m_workflowId;
    }

    public boolean isSuccess() {

        return m_success;
    }
}
