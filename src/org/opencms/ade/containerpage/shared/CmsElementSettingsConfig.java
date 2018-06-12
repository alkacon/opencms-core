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

package org.opencms.ade.containerpage.shared;

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents the setting configuration for a container element.<p>
 */
public class CmsElementSettingsConfig implements IsSerializable {

    /** The additional infos to display. */
    private ArrayList<CmsAdditionalInfoBean> m_additionalInfo;

    /** The data for the container element. */
    private CmsContainerElementData m_elementData;

    /** Schema path for element. **/
    private String m_schema;

    /** The resource state. */
    private CmsResourceState m_state;

    /**
     * Creates a new instance.<p>
     *
     * @param elementData the element data
     * @param state the resource state
     * @param additionalInfo the additional infos
     */
    public CmsElementSettingsConfig(
        CmsContainerElementData elementData,
        CmsResourceState state,
        ArrayList<CmsAdditionalInfoBean> additionalInfo,
        String schema) {

        m_elementData = elementData;
        m_additionalInfo = additionalInfo;
        m_state = state;
        m_schema = schema;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsElementSettingsConfig() {
        // do nothing

    }

    /**
     * Gets the additional info items.<p>
     *
     * @return the additional info items
     */
    public ArrayList<CmsAdditionalInfoBean> getAdditionalInfo() {

        return m_additionalInfo;
    }

    /**
     * Gets the element data.<p>
     *
     * @return the element data
     */
    public CmsContainerElementData getElementData() {

        return m_elementData;
    }

    /**
     * Gets the schema path.
     *
     * @return the schema path
     */
    public String getSchema() {

        return m_schema;
    }

    /**
     * The state.<p>
     *
     * @return the resource state
     */
    public CmsResourceState getState() {

        return m_state;
    }

}
