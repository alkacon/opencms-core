/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/sitemap/Attic/CmsSitemapSessionCache.java,v $
 * Date   : $Date: 2009/11/04 13:54:40 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;

/**
 * ADE's session cache.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.9.2
 */
public final class CmsSitemapSessionCache {

    /** The sitemap recent list. */
    private JSONArray m_recentList;

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_SITEMAP_CACHE = "__OCMS_SITEMAP_CACHE__";

    /**
     * Initializes the session cache.<p>
     * 
     * @param cms the cms context
     */
    public CmsSitemapSessionCache(CmsObject cms) {

        // sitemap recent lists
        m_recentList = new JSONArray();
    }

    /**
     * Returns the cached recent list.<p>
     * 
     * @return the cached recent list
     */
    public JSONArray getRecentList() {

        return m_recentList;
    }

    /**
     * Caches the given recent list.<p>
     * 
     * @param list the recent list to cache
     */
    public void setRecentList(JSONArray list) {

        m_recentList = list;
    }
}
