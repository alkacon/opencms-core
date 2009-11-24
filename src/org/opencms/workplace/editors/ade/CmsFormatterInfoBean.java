/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsFormatterInfoBean.java,v $
 * Date   : $Date: 2009/11/24 07:36:27 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Info bean for elements displayed by default list formatter.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
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
     * @param isType is this a resource-type info
     */
    public CmsFormatterInfoBean(I_CmsResourceType resourceType, boolean isType) {

        m_resourceType = resourceType;
        m_isType = isType;
        m_additionalInfo = new ArrayList<CmsFieldInfoBean>();
    }

    /**
     * Adds an additional info.<p>
     * 
     * @param info the additional info field
     */
    public void addAdditionalInfo(CmsFieldInfoBean info) {

        m_additionalInfo.add(info);
    }

    /**
     * Adds an additional info.<p>
     * 
     * @param fieldName the field name
     * @param fieldTitle the field title
     * @param fieldValue the field value
     */
    public void addAdditionalInfo(String fieldName, String fieldTitle, String fieldValue) {

        m_additionalInfo.add(new CmsFieldInfoBean(fieldName, fieldTitle, fieldValue));
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
     * Returns the resource-id.<p>
     *
     * @return the resource-id
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the resource-type.<p>
     *
     * @return the resource-type
     */
    public I_CmsResourceType getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the sub-title info.<p>
     *
     * @return the sub-title info
     */
    public CmsFieldInfoBean getSubTitleInfo() {

        return m_subTitleInfo;
    }

    /**
     * Returns the title info.<p>
     *
     * @return the title info
     */
    public CmsFieldInfoBean getTitleInfo() {

        return m_titleInfo;
    }

    /**
     * Returns the type-id.<p>
     *
     * @return the type-id
     */
    public int getTypeId() {

        return m_resourceType.getTypeId();
    }

    /**
     * Returns this is a resource-type info element and not a resource info.<p>
     *
     * @return this is a resource-type info
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
     * Sets the site-path.<p>
     *
     * @param sitePath the site-path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the sub-title info.<p>
     *
     * @param subTitleInfo the sub-title info to set
     */
    public void setSubTitleInfo(CmsFieldInfoBean subTitleInfo) {

        m_subTitleInfo = subTitleInfo;
    }

    /**
     * Sets the sub-title info.<p>
     * 
     * @param fieldName the field name
     * @param fieldTitle the field title
     * @param fieldValue the field value
     */
    public void setSubTitleInfo(String fieldName, String fieldTitle, String fieldValue) {

        m_subTitleInfo = new CmsFieldInfoBean(fieldName, fieldTitle, fieldValue);
    }

    /**
     * Sets the title info.<p>
     *
     * @param titleInfo the title info to set
     */
    public void setTitleInfo(CmsFieldInfoBean titleInfo) {

        m_titleInfo = titleInfo;
    }

    /**
     * Sets the title info.<p>
     * 
     * @param fieldName the field name
     * @param fieldTitle the field title
     * @param fieldValue the field value
     */
    public void setTitleInfo(String fieldName, String fieldTitle, String fieldValue) {

        m_titleInfo = new CmsFieldInfoBean(fieldName, fieldTitle, fieldValue);
    }

}
