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

import org.opencms.file.CmsResource;

/**
 * This class contains the model page configuration for a sitemap region.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.0 $
 * 
 * @since 8.0.0
 */
public class CmsModelPageConfig implements I_CmsConfigurationObject<CmsModelPageConfig>, Cloneable {

    /** The model page resource. */
    private CmsResource m_resource;

    /** True if this is a default model page. */
    private boolean m_isDefault;

    /** True if this bean disables a model page rather than adding one.*/
    private boolean m_isDisabled;

    /**
     * Creates a new model page configuration bean.<p>
     * 
     * @param res the model page resource 
     * @param isDefault true if this is a default model page 
     * @param isDisabled true if this is a disabled model page 
     */
    public CmsModelPageConfig(CmsResource res, boolean isDefault, boolean isDisabled) {

        m_resource = res;
        m_isDefault = isDefault;
        m_isDisabled = isDisabled;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsModelPageConfig clone() {

        return new CmsModelPageConfig(m_resource, m_isDefault, m_isDisabled);
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#getKey()
     */
    public String getKey() {

        return m_resource.getStructureId().toString();
    }

    /**
     * Gets the model page resource.<p>
     * 
     * @return the model page resource 
     */
    public CmsResource getResource() {

        return m_resource;
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

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#merge(org.opencms.ade.configuration.I_CmsConfigurationObject)
     */
    public CmsModelPageConfig merge(CmsModelPageConfig child) {

        return child.clone();
    }
}
