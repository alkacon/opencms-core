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

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean which represents a possible workflow, containing an identifier, a user-readable name
 * and a list of workflow actions.<p>
 */
public class CmsWorkflow implements IsSerializable {

    /** The list of actions which are possible in this workflow. */
    private List<CmsWorkflowAction> m_actions;

    /** An identifier for the workflow type. */
    private String m_id;

    /** A user-readable nice name for the workflow. */
    private String m_niceName;

    /**
     * Creates a new workflow bean instance.<p>
     *
     * @param id the identifier for the workflow type
     * @param niceName the nice name for the workflow which is displayed to the user
     * @param actions the actions which are possible in this workflow
     */
    public CmsWorkflow(String id, String niceName, List<CmsWorkflowAction> actions) {

        m_id = id;
        m_niceName = niceName;
        m_actions = actions;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsWorkflow() {

        // nothing to do
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CmsWorkflow) {
            return getId().equals(((CmsWorkflow)obj).getId());
        }
        return false;
    }

    /**
     * Returns the list of actions which are possible in this workflow.<p>
     *
     * @return the actions possible in this workflow
     */
    public List<CmsWorkflowAction> getActions() {

        return Collections.unmodifiableList(m_actions);
    }

    /**
     * Gets the workflow identifier.<p>
     *
     * @return the workflow identifier
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the user-readable nice name for the workflow.<p>
     *
     * @return the nice name for the workflow
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getId().hashCode();
    }

}
