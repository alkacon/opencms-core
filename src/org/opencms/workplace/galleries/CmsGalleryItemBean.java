/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsGalleryItemBean.java,v $
 * Date   : $Date: 2009/11/24 16:32:40 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsResource;
import org.opencms.search.CmsSearchResult;

/**
 * Bean to provide info for preview rendering.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 7.6
 * 
 */
public class CmsGalleryItemBean {

    /** The item icon path. */
    private String m_icon;

    /** The vfs path. */
    private String m_path;

    /** The resource. */
    private CmsResource m_resource;

    /** The search-result. */
    private CmsSearchResult m_searchResult;

    /** The item sub-title. */
    private String m_subtitle;

    /** The item title. */
    private String m_title;

    /** The resource type id. */
    private int m_typeId = -5;

    /** The resource type name. */
    private String m_typeName;

    /**
     * Constructor.<p>
     * 
     * @param resource the element resource
     */
    public CmsGalleryItemBean(CmsResource resource) {

        m_resource = resource;
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
     * Returns the icon.<p>
     *
     * @return the icon
     */
    public String getIcon() {

        return m_icon;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the search-result if available.<p>
     *
     * @return the search-result
     */
    public CmsSearchResult getSearchResult() {

        return m_searchResult;
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
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the typeId.<p>
     *
     * @return the typeId
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Returns the type name.<p>
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Sets the icon.<p>
     *
     * @param icon the icon to set
     */
    public void setIcon(String icon) {

        m_icon = icon;
    }

    /**
     * Sets the path.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
    }

    /**
     * Sets the search result.<p>
     *
     * @param searchResult the search result to set
     */
    public void setSearchResult(CmsSearchResult searchResult) {

        m_searchResult = searchResult;
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
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the type id.<p>
     *
     * @param typeId the type id to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }

    /**
     * Sets the type name.<p>
     *
     * @param typeName the type name to set
     */
    public void setTypeName(String typeName) {

        m_typeName = typeName;
    }

}
