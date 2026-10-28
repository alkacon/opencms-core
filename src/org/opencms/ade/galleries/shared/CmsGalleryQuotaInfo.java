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

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsQuotaStatus;

import java.io.Serializable;

/**
 * Named budget snapshot for the permanent galleries tab warning.<p>
 */
public class CmsGalleryQuotaInfo implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** The localized label of the actual budget scope. */
    private String m_title;

    /** The budget snapshot. */
    private CmsQuotaStatus m_status;

    /**
     * Constructor.<p>
     *
     * @param title the localized budget scope label
     * @param status the budget snapshot
     */
    public CmsGalleryQuotaInfo(String title, CmsQuotaStatus status) {

        m_title = title;
        m_status = status;
    }

    /**
     * Constructor for serialization.<p>
     */
    protected CmsGalleryQuotaInfo() {}

    /**
     * Gets the budget snapshot.<p>
     *
     * @return the snapshot
     */
    public CmsQuotaStatus getStatus() {

        return m_status;
    }

    /**
     * Gets the localized budget scope label.<p>
     *
     * @return the label
     */
    public String getTitle() {

        return m_title;
    }
}
