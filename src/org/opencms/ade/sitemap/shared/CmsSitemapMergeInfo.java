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
 * This class represents the result of a 'merge sub-sitemap' operation.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapMergeInfo implements IsSerializable {

    /** The entries to be merged into the parent sitemap. */
    private CmsClientSitemapEntry m_mergedEntry;

    /** The timestamp of the last modification to the parent sitemap. */
    private long m_timestamp;

    /**
     * Constructor.<p>
     *
     * @param mergedEntry the entries which have been merged into the parent sitemap
     * @param timestamp the modification time of the parent sitemap
     */
    public CmsSitemapMergeInfo(CmsClientSitemapEntry mergedEntry, long timestamp) {

        m_mergedEntry = mergedEntry;
        m_timestamp = timestamp;
    }

    /**
     * Default constructor.<p>
     */
    protected CmsSitemapMergeInfo() {

        // do nothing
    }

    /**
     * Returns the entry to be merged back into the parent sitemap.<p>
     *
     * @return the entry to be merged
     */
    public CmsClientSitemapEntry getMergedEntry() {

        return m_mergedEntry;
    }

    /**
     * The timestamp of the last modification of the parent sitemap.<p>
     *
     * @return the timestamp of the last modification of the parent sitemap
     */
    public long getTimestamp() {

        return m_timestamp;
    }

}
