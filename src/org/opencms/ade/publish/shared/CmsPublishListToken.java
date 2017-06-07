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
 * A bean which can be used to reconstruct a publish list.<p>
 *
 * This is used instead of sending the whole resource list to the client when it becomes too big.<p>
 */
public class CmsPublishListToken implements IsSerializable {

    /** The publish options. */
    private CmsPublishOptions m_options;

    /** The selected workflow. */
    private CmsWorkflow m_workflow;

    /**
     * Creates a new instance.<p>
     *
     * @param workflow the selected workflow
     * @param options the publish options
     */
    public CmsPublishListToken(CmsWorkflow workflow, CmsPublishOptions options) {

        m_workflow = workflow;
        m_options = options;
    }

    /**
     * Default constructor used for serialization.<p>
     */
    protected CmsPublishListToken() {

        // do nothing
    }

    /**
     * Gets the publish options.<p>
     *
     * @return the publish options
     */
    public CmsPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Gets the selected workflow.<p>
     *
     * @return the selected workflow
     */
    public CmsWorkflow getWorkflow() {

        return m_workflow;
    }

}
