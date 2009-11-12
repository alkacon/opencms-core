/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsGalleryItemBean.java,v $
 * Date   : $Date: 2009/11/12 12:47:21 $
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

package org.opencms.workplace.galleries;

import org.opencms.search.CmsSearchResult;

public class CmsGalleryItemBean {

    /** The item title. */
    private String m_title;

    /** The item sub-title. */
    private String m_subtitle;

    /** The item icon path. */
    private String m_icon;

    /** The search-result. */
    private CmsSearchResult m_searchResult;

    /**
     * Returns the search-result if available.<p>
     *
     * @return the search-result
     */
    public CmsSearchResult getSearchResult() {

        return m_searchResult;
    }

    /**
     * Sets the searchResult.<p>
     *
     * @param searchResult the searchResult to set
     */
    public void setSearchResult(CmsSearchResult searchResult) {

        m_searchResult = searchResult;
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title
     * @param subtitle the subtitle
     * @param icon the icon
     */
    public CmsGalleryItemBean(String title, String subtitle, String icon) {

        m_title = title;
        m_subtitle = subtitle;
        m_icon = icon;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Returns the subtitle.<p>
     *
     * @return the subtitle
     */
    public String getSubtitle() {

        return m_subtitle;
    }

    /**
     * Sets the subtitle.<p>
     *
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(String subtitle) {

        m_subtitle = subtitle;
    }

    /**
     * Returns the icon.<p>
     *
     * @return the icon
     */
    public String getIcon() {

        return m_icon;
    }

    /**
     * Sets the icon.<p>
     *
     * @param icon the icon to set
     */
    public void setIcon(String icon) {

        m_icon = icon;
    }

}
