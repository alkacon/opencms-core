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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean class which represents a resource which is related to another resource.<p>
 */
public class CmsResourceStatusRelationBean implements IsSerializable {

    /** The info bean for the resource. */
    private CmsListInfoBean m_infoBean;

    /** True if the resource is an xml content. */
    private boolean m_isXmlContent;

    /** A link to the resource. */
    private String m_link;

    /** The permission info. */
    private CmsPermissionInfo m_permissionInfo;

    /** The site path of the resource. */
    private String m_sitePath;

    /** The site root. */
    private String m_siteRoot;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /**
     * Creates a new instance.<p>
     *
     * @param infoBean the list info bean
     * @param link the link to the resource
     * @param structureId the structure id of the resource
     * @param permissionInfo the permission info
     */
    public CmsResourceStatusRelationBean(
        CmsListInfoBean infoBean,
        String link,
        CmsUUID structureId,
        CmsPermissionInfo permissionInfo) {

        m_infoBean = infoBean;
        m_link = link;
        m_structureId = structureId;
        m_permissionInfo = permissionInfo;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsResourceStatusRelationBean() {

        // do nothing
    }

    /**
     * Gets the list info bean.<p>
     *
     * @return the list info bean
     */
    public CmsListInfoBean getInfoBean() {

        return m_infoBean;
    }

    /**
     * Gets the link to the resource.<p>
     *
     * @return the link to the resource
     */
    public String getLink() {

        return m_link;
    }

    /**
     * Returns the permission info.<p>
     *
     * @return the permission info
     */
    public CmsPermissionInfo getPermissionInfo() {

        return m_permissionInfo;
    }

    /**
     * Gets the site path of the resource.<p>
     *
     * @return the site path of the resource
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the siteRoot.<p>
     *
     * @return the siteRoot
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the structure id of the resource.<p>
     *
     * @return the structure id of the resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns true if the resource is an XML content.<p>
     *
     * @return true if the resource is an XML content
     */
    public boolean isXmlContent() {

        return m_isXmlContent;
    }

    /**
     * Sets the list info bean.<p>
     *
     * @param infoBean the new list info bean
     */
    public void setInfoBean(CmsListInfoBean infoBean) {

        m_infoBean = infoBean;
    }

    /**
     * Marks this bean as belonging to an XML content resource.<p>
     *
     * @param isXmlContent if the resource is an XML content
     */
    public void setIsXmlContent(boolean isXmlContent) {

        m_isXmlContent = isXmlContent;

    }

    /**
     * Sets the link for the resource.<p>
     *
     * @param link the link for the resource
     */
    public void setLink(String link) {

        m_link = link;
    }

    /**
     * Sets the site path for the resource.<p>
     *
     * @param path the new site path
     */
    public void setSitePath(String path) {

        m_sitePath = path;
    }

    /**
     * Sets the siteRoot.<p>
     *
     * @param siteRoot the siteRoot to set
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

    /**
     * Sets the structure id for the resource.<p>
     *
     * @param structureId the new structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

}
