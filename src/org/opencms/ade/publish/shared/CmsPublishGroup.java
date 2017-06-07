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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A publish group.<p>
 *
 * @since 7.6
 */
public class CmsPublishGroup implements IsSerializable {

    /** Flag which indicates whether the resource group is auto-selectable. */
    private boolean m_autoSelectable = true;

    /** The group name.*/
    private String m_name;

    /** The group resources.*/
    private List<CmsPublishResource> m_resources;

    /**
     * Creates a new publish group bean.<p>
     *
     * @param name the group name
     * @param resources the resources
     **/
    public CmsPublishGroup(String name, List<CmsPublishResource> resources) {

        m_name = name;
        m_resources = ((resources == null)
        ? new ArrayList<CmsPublishResource>()
        : new ArrayList<CmsPublishResource>(resources));
    }

    /**
     * For serialization.<p>
     */
    protected CmsPublishGroup() {

        // for serialization
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the resources.<p>
     *
     * @return the resources
     */
    public List<CmsPublishResource> getResources() {

        return m_resources;
    }

    /**
     * Returns true if the GUI should be able to automatically select this group.<p>
     *
     * @return the value of the auto-selectable flag
     */
    public boolean isAutoSelectable() {

        return m_autoSelectable;
    }

    /**
     * Sets the auto-selectable flag.<p>
     *
     * @param autoSelectable the new flag value
     */
    public void setAutoSelectable(boolean autoSelectable) {

        m_autoSelectable = autoSelectable;
    }
}
