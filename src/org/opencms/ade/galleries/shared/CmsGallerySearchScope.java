/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * An enum that represents the possible search scope choices in the ADE gallery search tab.<p>
 */
public enum CmsGallerySearchScope {
    /** Search in the current site. */
    site(true, false, false, "GUI_SCOPE_SITE_0"),

    /** Search in the current subsite. */
    subSite(false, true, false, "GUI_SCOPE_SUBSITE_0"),

    /** Search in the current site and the shared folder. */
    siteShared(true, false, true, "GUI_SCOPE_SITESHARED_0"),

    /** Search in the current subsite and the shared folder. */
    subSiteShared(false, true, true, "GUI_SCOPE_SUBSITESHARED_0"),

    /** Search only in the shared folder. */
    shared(false, false, true, "GUI_SCOPE_SHARED_0");

    /** If true, search in the current site. */
    private final boolean m_site;

    /** If true, search in the current sub-site. */
    private final boolean m_subSite;

    /** If true, search in the shared folder. */
    private final boolean m_shared;

    /** The localization key.*/
    private final String m_key;

    /**
     * Default constructor needed for serialization.<p>
     */
    CmsGallerySearchScope() {

        m_subSite = false;
        m_shared = false;
        m_key = null;
        m_site = false;
    }

    /**
     * Creates a new search scope choice.<p>
     * 
     * @param siteParam true if the current site should be searched 
     * @param subSiteParam true if the current subsite should be searched 
     * @param sharedParam true if the shared folder should be searched 
     * @param key the localization key for the choice 
     */
    CmsGallerySearchScope(boolean siteParam, boolean subSiteParam, boolean sharedParam, String key) {

        m_site = siteParam;
        m_subSite = subSiteParam;
        m_shared = sharedParam;
        m_key = key;
    }

    /**
     * Returns the localization key for the choice.<p>
     * 
     * @return the localization key for the choice 
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Gets the search roots to use for the given site/subsite parameters.<p>
     *  
     * @param siteParam the current site 
     * @param subSiteParam the current subsite 
     * @param sharedParam the shared folder path 
     * @return the list of search roots for that option 
     */
    public List<String> getSearchRoots(String siteParam, String subSiteParam, String sharedParam) {

        List<String> result = new ArrayList<String>();
        if (m_site && (siteParam != null)) {
            result.add(siteParam);
        }
        if (m_subSite && (subSiteParam != null)) {
            result.add(subSiteParam);
        }
        if (m_shared && (sharedParam != null)) {
            result.add(sharedParam);
        }
        if (this == CmsGallerySearchScope.siteShared) {
            result.add("/system/modules/");
        }
        return result;
    }
}
