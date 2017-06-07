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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Work flow action information.<p>
 */
public class CmsWorkflowAction implements IsSerializable {

    /** The cancel workflow action. */
    public static final String ACTION_CANCEL = "cancel";

    /** The action key. */
    private String m_action;

    /** The action label. */
    private String m_label;

    /** Action enabled flag. */
    private boolean m_enabled;

    /** A flag which indicates whether this workflow action is actually a publish action. */
    private boolean m_isPublish;

    /**
     * Constructor.<p>
     *
     * @param actionKey the action key
     * @param label the action label
     * @param isEnabled <code>true</code> if the action is enabled
     */
    public CmsWorkflowAction(String actionKey, String label, boolean isEnabled) {

        m_action = actionKey;
        m_label = label;
        m_enabled = isEnabled;
    }

    /**
     * Constructor.<p>
     *
     * @param actionKey the action key
     * @param label the action label
     * @param isEnabled <code>true</code> if the action is enabled
     * @param isPublish a flag to distinguish publish actions from other workflow actions
     */
    public CmsWorkflowAction(String actionKey, String label, boolean isEnabled, boolean isPublish) {

        m_action = actionKey;
        m_label = label;
        m_enabled = isEnabled;
        m_isPublish = isPublish;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsWorkflowAction() {

        // nothing to do
    }

    /**
     * Returns the action key.<p>
     *
     * @return the action key
     */
    public String getAction() {

        return m_action;
    }

    /**
     * Returns the action label.<p>
     *
     * @return the action label
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * Returns if the action is enabled.<p>
     *
     * @return <code>true</code> if the action is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Check whether this action is a publish action.<p>
     *
     * @return true if this is a publish action
     */
    public boolean isPublish() {

        return m_isPublish;
    }
}
