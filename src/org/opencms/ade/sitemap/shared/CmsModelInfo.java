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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Wraps the model page and model group info into one object.<p>
 */
public class CmsModelInfo implements IsSerializable {

    /** The model group info. */
    private List<CmsModelPageEntry> m_modelGroups;

    /** The model page info. */
    private List<CmsModelPageEntry> m_modelPages;

    /** The parent model pages. */
    private List<CmsModelPageEntry> m_parentModelPages;

    /**
     * Constructor.<p>
     *
     * @param modelPages the model pages
     * @param parentModelPages the global model pages
     * @param modelGroups the model groups
     */
    public CmsModelInfo(
        List<CmsModelPageEntry> modelPages,
        List<CmsModelPageEntry> parentModelPages,
        List<CmsModelPageEntry> modelGroups) {

        m_modelPages = modelPages;
        m_parentModelPages = parentModelPages;
        m_modelGroups = modelGroups;
    }

    /**
     * Constructor required for serialization.<p>
     */
    protected CmsModelInfo() {

        // nothing to do
    }

    /**
     * Returns the model group info.<p>
     *
     * @return the model group info
     */
    public List<CmsModelPageEntry> getModelGroups() {

        return m_modelGroups;
    }

    /**
     * Returns the model page info.<p>
     *
     * @return the model page info
     */
    public List<CmsModelPageEntry> getModelPages() {

        return m_modelPages;
    }

    /**
     * Returns the parent model pages.<p>
     *
     * @return the parent model pages
     */
    public List<CmsModelPageEntry> getParentModelPages() {

        return m_parentModelPages;
    }

}
