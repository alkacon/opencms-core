/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A bean representing a resource type for use in the detail page creation menu.<p>
 * 
 * @since 8.0.0
 */
public class CmsNewResourceInfo implements Serializable {

    /** ID for serialization. */
    private static final long serialVersionUID = -4731814848380350682L;

    /** The navigation level create parameter. */
    public static final String NAVIGATION_LEVEL_PARAMETER = "new_navigation_level";

    /** The structure id of the copy resource. */
    private CmsUUID m_copyResourceId;

    /** The additional parameter used for creating new resources. */
    private String m_createParameter;

    /** Date. */
    private String m_date;

    /** The description. */
    private String m_description;

    /** True if the user can edit the resource. */
    private boolean m_editable;

    /** The id. */
    private int m_id;

    /** The flag which determines whether this bean is for a function page or for a normal detail page. */
    private boolean m_isFunction;

    /** Navigation position, used for ordering. */
    private Float m_navPos;

    /** Subtitle. */
    private String m_subtitle;

    /** The title. */
    private String m_title;

    /** The type name. */
    private String m_typeName;

    /** VFS path. */
    private String m_vfsPath;

    /**
     * Instantiates a new resource type information bean.
     *
     * @param id the id
     * @param typeName the type name
     * @param title the title
     * @param description the description
     * @param copyResourceId the structure id of the copy resource
     * @param editable true if the model resource is editable 
     * @param subTitle the subtitle to display
     */
    public CmsNewResourceInfo(
        int id,
        String typeName,
        String title,
        String description,
        CmsUUID copyResourceId,
        boolean editable,
        String subTitle) {

        m_id = id;
        m_typeName = typeName;
        m_title = title;
        m_copyResourceId = copyResourceId;
        m_description = description;
        m_subtitle = subTitle;
        m_editable = editable;
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsNewResourceInfo() {

        // do nothing
    }

    /**
     * Returns the structure id of the copy resource.<p>
     *
     * @return the structure id of the copy resource
     */
    public CmsUUID getCopyResourceId() {

        return m_copyResourceId;
    }

    /**
     * Gets the additional parameter for creating new resources.<p>
     * 
     * @return the additional parameter for creating new resources 
     */
    public String getCreateParameter() {

        return m_createParameter;
    }

    /**
     * Returns the date to display.<p>
     * 
     * @return the date to display
     */
    public String getDate() {

        return m_date;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {

        return m_id;
    }

    /**
     * Gets the navigation position (used for ordering).<p>
     * 
     * @return the navigation position 
     */
    public Float getNavPos() {

        return m_navPos;
    }

    /**
     * Gets the subtitle.<p>
     * 
     * @return the subtitle 
     */
    public String getSubTitle() {

        return m_subtitle;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Gets the VFS path.<p>
     * 
     * @return the VFS path  
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /** 
     * Returns true if the model resource is editable.<p>
     * 
     * @return true if the model resource is editable 
     */
    public boolean isEditable() {

        return m_editable;
    }

    /**
     * Returns true if this is a resource info bean for a function page.<p>
     * 
     * @return true if this is a resource info bean for a function page 
     */
    public boolean isFunction() {

        return m_isFunction;
    }

    /**
     * Sets the create parameter used for new pages.<p>
     * 
     * @param createParameter the create parameter used for new pages 
     */
    public void setCreateParameter(String createParameter) {

        m_createParameter = createParameter;
    }

    /** 
     * Sets the modification date string.<p>
     * 
     * @param date the modification date string 
     */
    public void setDate(String date) {

        m_date = date;
    }

    /**
     * Sets the "function page" flag.<p>
     * 
     * @param isFunction the new value for the function page flag 
     */
    public void setIsFunction(boolean isFunction) {

        m_isFunction = isFunction;
    }

    /**
     * Sets the navigation position.<p>
     * 
     * @param navPos the navigation position 
     */
    public void setNavPos(Float navPos) {

        m_navPos = navPos;
    }

    /**
     * Sets the subtitle.<p>
     * 
     * @param subtitle the subtitle 
     */
    public void setSubTitle(String subtitle) {

        m_subtitle = subtitle;
    }

    /**
     * Sets the VFS path.<p>
     * 
     * @param vfsPath the VFS path
     */
    public void setVfsPath(String vfsPath) {

        m_vfsPath = vfsPath;
    }

}
