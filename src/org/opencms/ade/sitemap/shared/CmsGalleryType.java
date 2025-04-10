/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.List;

/**
 * The gallery type bean.<p>
 */
public class CmsGalleryType extends CmsListInfoBean {

    /** The gallery content type names. */
    private List<String> m_contentTypeNames;

    /** The resource type id. */
    private int m_typeId;

    /**
     * Returns the gallery content type names.<p>
     *
     * @return the gallery content type names
     */
    public List<String> getContentTypeNames() {

        return m_contentTypeNames;
    }

    /**
     * Returns the type id.<p>
     *
     * @return the type id
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Sets the gallery content type names.<p>
     *
     * @param contentTypeNames the gallery content type names to set
     */
    public void setContentTypeNames(List<String> contentTypeNames) {

        m_contentTypeNames = contentTypeNames;
    }

    /**
     * Sets the type id.<p>
     *
     * @param typeId the type id to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }
}
