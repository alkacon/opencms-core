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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A data class which is used to transfer information about sub-sitemaps which have
 * been created.<p>
 *
 * @since 8.0.0
 */
public class CmsSubSitemapInfo implements IsSerializable {

    /** The path of the newly created sitemap. */
    private CmsClientSitemapEntry m_entry;

    /** The 'last modified' time of the parent sitemap. */
    private long m_timestamp;

    /**
     * Constructor.<p>
     *
     * @param entry the entry of the newly created sub sitemap
     * @param timestamp the 'last modified' time of the parent sitemap
     */
    public CmsSubSitemapInfo(CmsClientSitemapEntry entry, long timestamp) {

        m_timestamp = timestamp;
        m_entry = entry;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsSubSitemapInfo() {

        // hidden default constructor
    }

    /**
     * Returns the entry of the newly created sitemap.<p>
     *
     * @return the entry of the newly created sitemap
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Returns the last modification time of the parent sitemap.<p>
     *
     * @return the last modification time of the parent sitemap
     */
    public long getParentTimestamp() {

        return m_timestamp;
    }

}
