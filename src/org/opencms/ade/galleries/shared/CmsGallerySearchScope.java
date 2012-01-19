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

package org.opencms.ade.galleries.shared;


/**
 * An enum that represents the possible search scope choices in the ADE gallery search tab.<p>
 */
public enum CmsGallerySearchScope {
    /** Search only in the shared folder. */
    shared(false, false, true, "GUI_SCOPE_SHARED_0"),

    /** Search in the current site. */
    site(true, false, false, "GUI_SCOPE_SITE_0"),

    /** Search in the current site and the shared folder. */
    siteShared(true, false, true, "GUI_SCOPE_SITESHARED_0"),

    /** Search in the current subsite. */
    subSite(false, true, false, "GUI_SCOPE_SUBSITE_0"),

    /** Search in the current subsite and the shared folder. */
    subSiteShared(false, true, true, "GUI_SCOPE_SUBSITESHARED_0");

    /** The localization key.*/
    private final String m_key;

    /** If true, search in the shared folder. */
    private final boolean m_shared;

    /** If true, search in the current site. */
    private final boolean m_site;

    /** If true, search in the current sub-site. */
    private final boolean m_subSite;

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
     * Returns if this search scope includes the shared folder.<p>
     * 
     * @return <code>true</code> if this search scope includes the shared folder
     */
    public boolean isIncludeShared() {

        return m_shared;
    }

    /**
     * Returns if this search scope includes the site folder.<p>
     * 
     * @return <code>true</code> if this search scope includes the site folder
     */
    public boolean isIncludeSite() {

        return m_site;
    }

    /**
     * Returns if this search scope includes the sub site folder.<p>
     * 
     * @return <code>true</code> if this search scope includes the sub site folder
     */
    public boolean isIncludeSubSite() {

        return m_subSite;
    }
}
