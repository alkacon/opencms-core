/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsFormatterInfoBean.java,v $
 * Date   : $Date: 2009/11/19 13:22:03 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Info bean for elements displayed by default list formatter.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.6
 * 
 */
public class CmsFormatterInfoBean {

    /** List of additional info. */
    private List<CmsFieldInfoBean> m_additionalInfo;

    /** The item icon path. */
    private String m_icon;

    /** Flag to indicate if item is a resource or a resource-type. */
    private boolean m_isType;

    /** The resource. */
    private CmsResource m_resource;

    /** The resource-id. */
    private CmsUUID m_resourceId;

    /** The resource type. */
    private I_CmsResourceType m_resourceType;

    /** The resource site-path. */
    private String m_sitePath;

    /** The item sub-title to display. */
    private CmsFieldInfoBean m_subTitleInfo;

    /** The item title to display. */
    private CmsFieldInfoBean m_titleInfo;

    /**
     * Constructor.<p>
     * 
     * @param resourceType the resource type
     * @param isType is this a resource-type item
     */
    public CmsFormatterInfoBean(I_CmsResourceType resourceType, boolean isType) {

        m_resourceType = resourceType;
        m_isType = isType;
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public List<CmsFieldInfoBean> getAdditionalInfo() {

        return m_additionalInfo;
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
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the resourceId.<p>
     *
     * @return the resourceId
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the resourceType.<p>
     *
     * @return the resourceType
     */
    public I_CmsResourceType getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the sitePath.<p>
     *
     * @return the sitePath
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the subTitleInfo.<p>
     *
     * @return the subTitleInfo
     */
    public CmsFieldInfoBean getSubTitleInfo() {

        return m_subTitleInfo;
    }

    /**
     * Returns the titleInfo.<p>
     *
     * @return the titleInfo
     */
    public CmsFieldInfoBean getTitleInfo() {

        return m_titleInfo;
    }

    /**
     * Returns the typeId.<p>
     *
     * @return the typeId
     */
    public int getTypeId() {

        return m_resourceType.getTypeId();
    }

    /**
     * Returns the isType.<p>
     *
     * @return the isType
     */
    public boolean isType() {

        return m_isType;
    }

    /**
     * Sets the additional info.<p>
     *
     * @param additionalInfo the additional info to set
     */
    public void setAdditionalInfo(List<CmsFieldInfoBean> additionalInfo) {

        m_additionalInfo = additionalInfo;
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
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(CmsResource resource) {

        m_resource = resource;
    }

    /**
     * Sets the resourceId.<p>
     *
     * @param resourceId the resourceId to set
     */
    public void setResourceId(CmsUUID resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * Sets the sitePath.<p>
     *
     * @param sitePath the sitePath to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the subTitleInfo.<p>
     *
     * @param subTitleInfo the subTitleInfo to set
     */
    public void setSubTitleInfo(CmsFieldInfoBean subTitleInfo) {

        m_subTitleInfo = subTitleInfo;
    }

    /**
     * Sets the titleInfo.<p>
     *
     * @param titleInfo the titleInfo to set
     */
    public void setTitleInfo(CmsFieldInfoBean titleInfo) {

        m_titleInfo = titleInfo;
    }

}
