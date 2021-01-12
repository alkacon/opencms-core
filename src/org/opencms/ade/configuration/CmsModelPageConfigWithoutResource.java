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
 * This class contains the model page configuration for a sitemap region, without the actual resource.
 */
public class CmsModelPageConfigWithoutResource {

    /** The structure id. */
    private CmsUUID m_structureId;

    /** True if this is a default model page. */
    private boolean m_isDefault;

    /** True if this bean disables a model page rather than adding one.*/
    private boolean m_isDisabled;

    /**
     * Creates a new model page configuration bean.<p>
     *
     * @param structureId the model page structure id
     * @param isDefault true if this is a default model page
     * @param isDisabled true if this is a disabled model page
     */
    public CmsModelPageConfigWithoutResource(CmsUUID structureId, boolean isDefault, boolean isDisabled) {

        m_structureId = structureId;
        m_isDefault = isDefault;
        m_isDisabled = isDisabled;
    }

    /**
     * Gets the structure id.
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns true if this is a default model page.<p>
     *
     * @return true if this is a default model page
     */
    public boolean isDefault() {

        return m_isDefault;
    }

    /**
     * Returns true if this entry disables the model page.<p>
     *
     * @return true if this entry disables the model page
     */
    public boolean isDisabled() {

        return m_isDisabled;
    }

}
