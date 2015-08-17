/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.util.CmsUUID;

/**
 * Bean for representing a named dynamic function reference from the configuration.<p>
 */
public class CmsFunctionReference implements I_CmsConfigurationObject<CmsFunctionReference> {

    /** The function reference name. */
    private String m_name;

    /** A number used for sorting the function references.<p>*/
    private int m_order;

    /** The function resource structure id. */
    private CmsUUID m_structureId;

    /**
     * Creates a new function reference.<p>
     *
     * @param name the name of the function reference
     * @param structureId the structure id of the function
     * @param order the number used for sorting the function references
     */
    public CmsFunctionReference(String name, CmsUUID structureId, int order) {

        m_name = name;
        m_structureId = structureId;
        m_order = order;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#getKey()
     */
    public String getKey() {

        return m_name;
    }

    /**
     * Returns the name of the function reference.<p>
     *
     * @return the name of the function reference
     */
    public String getName() {

        return m_name;
    }

    /**
     * The order information for sorting the function references.<p>
     *
     * @return the order information
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * Returns the structure id of the dynamic function resource.<p>
     *
     * @return the structure id of the function
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#isDisabled()
     */
    public boolean isDisabled() {

        return false;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#merge(org.opencms.ade.configuration.I_CmsConfigurationObject)
     */
    public CmsFunctionReference merge(CmsFunctionReference child) {

        return child;
    }
}
