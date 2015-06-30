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

package org.opencms.ade.galleries;

import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.Set;

/**
 * The tree open state of a gallery tree tab.<p>
 */
public class CmsTreeOpenState implements Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The set of structure ids belonging to opened tree items. */
    private Set<CmsUUID> m_openItems;

    /** The site root. */
    private String m_siteRoot;

    /** The time stamp. */
    private long m_timestamp;

    /** The tree name. */
    private String m_treeName;

    /**
     * Creates a new tree open state instance.<p>
     *
     * @param treeName the tree name
     * @param siteRoot the site root
     * @param openItems the ids of the open tree entries
     */
    public CmsTreeOpenState(String treeName, String siteRoot, Set<CmsUUID> openItems) {

        m_treeName = treeName;
        m_siteRoot = siteRoot;
        m_openItems = openItems;
        m_timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the set of structure ids of resources corresponding to opened tree entries.<p>
     *
     * @return the set of structure ids of open tree entries
     */
    public Set<CmsUUID> getOpenItems() {

        return m_openItems;
    }

    /**
     * Gets the site root.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the time stamp.<p>
     *
     * @return the time stamps
     */
    public long getTimestamp() {

        return m_timestamp;
    }

    /**
     * Gets the tree name.<p>
     *
     * @return the tree name
     */
    public String getTreeName() {

        return m_treeName;
    }

}
