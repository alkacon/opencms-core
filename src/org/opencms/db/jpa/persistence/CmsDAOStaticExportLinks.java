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

package org.opencms.db.jpa.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This data access object represents a static export link entry inside the table "cms_staticexport_links".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_STATICEXPORT_LINKS")
public class CmsDAOStaticExportLinks {

    /** The link id. */
    @Id
    @Column(name = "LINK_ID", length = 36)
    private String m_linkId;

    /** The link parameter. */
    @Basic
    @Column(name = "LINK_PARAMETER", length = 1024)
    private String m_linkParameter;

    /** The rfs path. */
    @Basic
    @Column(name = "LINK_RFS_PATH", length = 1024)
    private String m_linkRfsPath;

    /** The link timestamp. */
    @Basic
    @Column(name = "LINK_TIMESTAMP")
    private long m_linkTimestamp;

    /** The link type. */
    @Basic
    @Column(name = "LINK_TYPE")
    private int m_linkType;

    /**
     * The default constructor.<p>
     */
    public CmsDAOStaticExportLinks() {

        // noop
    }

    /**
     * A public constructor for generating a new export link object with an unique id.<p>
     * 
     * @param linkId the link id
     */
    public CmsDAOStaticExportLinks(String linkId) {

        m_linkId = linkId;
    }

    /**
     * Returns the linkId.<p>
     *
     * @return the linkId
     */
    public String getLinkId() {

        return m_linkId;
    }

    /**
     * Returns the linkParameter.<p>
     *
     * @return the linkParameter
     */
    public String getLinkParameter() {

        return m_linkParameter;
    }

    /**
     * Returns the linkRfsPath.<p>
     *
     * @return the linkRfsPath
     */
    public String getLinkRfsPath() {

        return m_linkRfsPath;
    }

    /**
     * Returns the linkTimestamp.<p>
     *
     * @return the linkTimestamp
     */
    public long getLinkTimestamp() {

        return m_linkTimestamp;
    }

    /**
     * Returns the linkType.<p>
     *
     * @return the linkType
     */
    public int getLinkType() {

        return m_linkType;
    }

    /**
     * Sets the linkId.<p>
     *
     * @param linkId the linkId to set
     */
    public void setLinkId(String linkId) {

        m_linkId = linkId;
    }

    /**
     * Sets the linkParameter.<p>
     *
     * @param linkParameter the linkParameter to set
     */
    public void setLinkParameter(String linkParameter) {

        m_linkParameter = linkParameter;
    }

    /**
     * Sets the linkRfsPath.<p>
     *
     * @param linkRfsPath the linkRfsPath to set
     */
    public void setLinkRfsPath(String linkRfsPath) {

        m_linkRfsPath = linkRfsPath;
    }

    /**
     * Sets the linkTimestamp.<p>
     *
     * @param linkTimestamp the linkTimestamp to set
     */
    public void setLinkTimestamp(long linkTimestamp) {

        m_linkTimestamp = linkTimestamp;
    }

    /**
     * Sets the linkType.<p>
     *
     * @param linkType the linkType to set
     */
    public void setLinkType(int linkType) {

        m_linkType = linkType;
    }

}