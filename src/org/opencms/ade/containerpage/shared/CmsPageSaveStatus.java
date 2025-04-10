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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents the status of a container page save operation.
 */
public class CmsPageSaveStatus implements IsSerializable {

    /** The id of the saved page. */
    private CmsUUID m_pageId;

    /** The time directly after the page has been saved. */
    private long m_timestamp;

    /**
     * Creates a new instance.
     *
     * @param pageId the id of the saved page
     * @param timestamp the time after the page has been saved
     */
    public CmsPageSaveStatus(CmsUUID pageId, long timestamp) {

        super();
        m_pageId = pageId;
        m_timestamp = timestamp;
    }

    /**
     * Hidden default constructor for serialization.
     */
    protected CmsPageSaveStatus() {}

    /**
     * Gets the time at which the page was saved.
     *
     * @return the time at which the page was saved
     */
    public long getTimestamp() {

        return m_timestamp;
    }

    /**
     * Gets the id of the saved page.
     *
     * @return the id of the saved page
     */
    public CmsUUID getPageId() {

        return m_pageId;
    }

}
