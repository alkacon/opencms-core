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

import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.detailpage.CmsDetailPageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A configuration data class whose parent can be set directly for test purposes.<p>
 */
public class CmsTestConfigData extends CmsADEConfigData {

    /** The parent configuration object. */
    public CmsADEConfigData m_parent;

    /**
     * Creates a new configuration data object.<p>
     * 
     * @param basePath the base path 
     * @param resourceTypeConfig the resource type configuration
     * @param propertyConfig the  property configuration 
     * @param detailPageInfos the detail page configuration 
     * @param modelPages the model page configuration 
     */
    public CmsTestConfigData(
        String basePath,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        List<CmsPropertyConfig> propertyConfig,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages) {

        super(
            basePath,
            resourceTypeConfig,
            false,
            propertyConfig,
            false,
            detailPageInfos,
            modelPages,
            new ArrayList<CmsFunctionReference>(),
            false,
            false,
            new CmsFormatterChangeSet());
    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#parent()
     */
    @Override
    public CmsADEConfigData parent() {

        return m_parent;
    }

    /**
     * Sets the "create contents locally" flag.<p>
     * 
     * @param createContentsLocally the flag to control whether contents from inherited resource types are stored in the local .content folder 
     */
    public void setCreateContentsLocally(boolean createContentsLocally) {

        m_createContentsLocally = createContentsLocally;
    }

    /**
     * Sets the "discard inherited model pages" flag.<p>
     *  
     * @param discardInheritedModelPages the flag to control whether inherited model pages are discarded 
    */
    public void setDiscardInheritedModelPages(boolean discardInheritedModelPages) {

        m_discardInheritedModelPages = discardInheritedModelPages;
    }

    /**
     * Sets the "discard inherited properties" flag.<p>
     * 
     * @param discardInheritedProperties the flag to control whether inherited properties are discarded 
     */
    public void setDiscardInheritedProperties(boolean discardInheritedProperties) {

        m_discardInheritedProperties = discardInheritedProperties;
    }

    /**
     * Sets the "discard inherited types" flag.<p>
     * 
     * @param discardInheritedTypes the flag to control whether inherited types are discarded 
     */
    public void setIsDiscardInheritedTypes(boolean discardInheritedTypes) {

        m_discardInheritedTypes = discardInheritedTypes;
    }

    /**
     * Sets the parent configuration object.<p>
     * 
     * @param parent the parent configuration object 
     */
    public void setParent(CmsADEConfigData parent) {

        m_parent = parent;
    }

}
