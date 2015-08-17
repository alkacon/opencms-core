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

package org.opencms.db;

import org.opencms.util.CmsUUID;

/**
 * Filter class used for selecting rewrite aliases from the database.<p>
 */
public class CmsRewriteAliasFilter {

    /** The id to use for filtering. */
    private CmsUUID m_id;

    /** The site root to filter with. */
    private String m_siteRoot;

    /**
     * Gets the id to filter with.<p>
     *
     * @return the id to filter with
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Gets the site root used for filtering.<p>
     *
     * @return the site root used for filtering
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Sets the id of the rewrite alias we want.<p>
     *
     * @param id a rewrite alias id
     *
     * @return this filter
     */
    public CmsRewriteAliasFilter setId(CmsUUID id) {

        m_id = id;
        return this;
    }

    /**
     * Sets the site root to use for filtering.<p>
     *
     * @param siteRoot the site root
     *
     * @return this filter
     */
    public CmsRewriteAliasFilter setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
        return this;
    }
}
