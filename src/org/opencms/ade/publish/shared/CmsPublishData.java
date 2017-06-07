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

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean that contains both publish options and a map of projects.<p>
 *
 * @since 8.0
 */
public class CmsPublishData implements IsSerializable {

    /** Name of the used dictionary. */
    public static final String DICT_NAME = "org_opencms_ade_publish";

    /** The close link. */
    private String m_closeLink;

    /** The publish group list. */
    private CmsPublishGroupList m_groups;

    /** The publish options. */
    private CmsPublishOptions m_options;

    /** The list of projects. */
    private List<CmsProjectBean> m_projects;

    /** The currently selected workflow. */
    private String m_selectedWorkflowId;

    /** Flag which controls whether the confirmation dialog should be shown before returning to the workplace. */
    private boolean m_showConfirmation;

    /** The available work flow actions. */
    private Map<String, CmsWorkflow> m_workflows;

    /**
     * Creates a new instance.<p>
     *
     * @param options the publish options
     * @param projects the map of projects
     * @param groups the publish groups
     * @param workflows the available work flows
     * @param selectedWorkflowId the selected workflow id
     */
    public CmsPublishData(
        CmsPublishOptions options,
        List<CmsProjectBean> projects,
        CmsPublishGroupList groups,
        Map<String, CmsWorkflow> workflows,
        String selectedWorkflowId) {

        m_options = options;
        m_projects = projects;
        m_groups = groups;
        m_workflows = workflows;
        m_selectedWorkflowId = selectedWorkflowId;
    }

    /**
     * For serialization.<p>
     */
    protected CmsPublishData() {

        // for serialization
    }

    /**
     * Gets the close link to open when the dialog is finished.<p>
     *
     * @return the close link
     */
    public String getCloseLink() {

        return m_closeLink;
    }

    /**
     * Returns the publish groups.<p>
     *
     * @return the publish groups
     */
    public CmsPublishGroupList getGroups() {

        return m_groups;
    }

    /**
     * Returns the publish options.<p>
     *
     * @return the publish options
     */
    public CmsPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Returns the list of projects.<p>
     *
     * @return the list of projects
     */
    public List<CmsProjectBean> getProjects() {

        return m_projects;
    }

    /**
     * Returns the selected workflow.<p>
     *
     * @return the selected workflow
     */
    public String getSelectedWorkflowId() {

        return m_selectedWorkflowId;
    }

    /**
     * Returns the available work flow actions.<p>
     *
     * @return the available work flow actions
     */
    public Map<String, CmsWorkflow> getWorkflows() {

        return m_workflows;
    }

    /**
     * Returns true if the confirmation dialog should be shown before returning to the workplace.<p>
     *
     * @return true if the confirmation dialog is enabled
     */
    public boolean isShowConfirmation() {

        return m_showConfirmation;
    }

    /**
     * Sets the close link.<p>
     *
     * @param closeLink the close link
     */
    public void setCloseLink(String closeLink) {

        m_closeLink = closeLink;

    }

    /**
     * Enables or disables showing the confirmation dialog before returning to the workplace.<p>
     *
     * @param confirm true if the confirmation dialog should be shown
     */
    public void setShowConfirmation(boolean confirm) {

        m_showConfirmation = confirm;
    }
}
