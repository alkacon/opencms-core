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

package org.opencms.ade.sitemap.shared;

import org.opencms.db.CmsResourceState;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean for additional site-map entry information.<p>
 *
 * @since 8.0.0
 */
public class CmsAdditionalEntryInfo implements IsSerializable {

    /** Other info, label / value mapping. */
    private Map<String, String> m_additional;

    /** The resource state. */
    private CmsResourceState m_resourceState;

    /**
     * Returns the additional info map.<p>
     *
     * @return the additional info map
     */
    public Map<String, String> getAdditional() {

        return m_additional;
    }

    /**
     * Returns the resource state.<p>
     *
     * @return the resource state
     */
    public CmsResourceState getResourceState() {

        return m_resourceState;
    }

    /**
     * Sets the additional info map.<p>
     *
     * @param additional the additional info map to set
     */
    public void setAdditional(Map<String, String> additional) {

        m_additional = additional;
    }

    /**
     * Sets the resource state.<p>
     *
     * @param resourceState the resource state to set
     */
    public void setResourceState(CmsResourceState resourceState) {

        m_resourceState = resourceState;
    }

}
