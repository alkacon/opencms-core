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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * List info bean with an additional structure id field.
 *
 * @since 8.0.3
 */
public class CmsResourceListInfo extends CmsListInfoBean {

    /** The  structure id. */
    private CmsUUID m_structureId;

    /**
     * Default constructor.<p>
     */
    public CmsResourceListInfo() {

        super();
    }

    /**
     * Constructor.<p>
     *
     * @param title the title
     * @param subtitle the subtitle
     * @param additionalInfo the additional info
     */
    public CmsResourceListInfo(String title, String subtitle, List<CmsAdditionalInfoBean> additionalInfo) {

        super(title, subtitle, additionalInfo);
    }

    /**
     * Returns the  structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the structure id to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

}
