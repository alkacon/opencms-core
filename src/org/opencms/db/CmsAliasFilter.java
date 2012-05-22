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
 * This class is used for filtering aliases in database operations.<p>
 */
public class CmsAliasFilter {

    /** The alias path. */
    private String m_path;

    /** The alias site root. */
    private String m_siteRoot;

    /** The alias structure id. */
    private CmsUUID m_structureId;

    /**
     * Creates a new alias filter.<p>
     *
     * Any parameter which is null will not be used for filtering.<p>
     *
     * @param siteRoot the site root to filter
     * @param aliasPath the alias path to filter
     * @param structureId the structure id to filter
     */
    public CmsAliasFilter(String siteRoot, String aliasPath, CmsUUID structureId) {

        m_path = aliasPath;
        m_structureId = structureId;
        m_siteRoot = siteRoot;
    }

    /**
     * Gets the alias path to filter.<p>
     *
     * @return the alias path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Gets the site root to filter.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the structure id to filter.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Checks whether this filter is trivial, i.e. would match all aliases.<p>
     *
     * @return true if the filter is trivial
     */
    public boolean isNullFilter() {

        return (m_path == null) && (m_siteRoot == null) && (m_structureId == null);
    }

}
