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

package org.opencms.module;

import org.opencms.file.CmsObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Module data read from a module zip file.<p>
 */
public class CmsModuleImportData {

    /** The CMS context. */
    private CmsObject m_cms;

    /** The module metadata. */
    private CmsModule m_module;

    /** The list of resource data for each entry in the manifest. */
    private List<CmsResourceImportData> m_resources = new ArrayList<>();

    /**
     * Adds the information for a single resource.<p>
     *
     * @param resourceData the information for a single resource
     */
    public void addResource(CmsResourceImportData resourceData) {

        m_resources.add(resourceData);
    }

    /**
     * Gets the CMS object.<p>
     *
     * @return the CMS object
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Gets the module metadata from the import zip.<p>
     *
     * @return the module metadata
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Gets the list of resource data objects for the manifest entries.<p>
     *
     * @return the resource data objects
     */
    public List<CmsResourceImportData> getResourceData() {

        return Collections.unmodifiableList(m_resources);
    }

    /**
     * Sets the CMS object.<p>
     *
     * @param cms the CMS object to set
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Sets the module metadata.<p>
     *
     * @param module the module metadata
     */
    public void setModule(CmsModule module) {

        m_module = module;
    }

}
