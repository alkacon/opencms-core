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
import javax.persistence.UniqueConstraint;

/**
 * This data access object represents a offline property entry inside the table "cms_offline_properties".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_OFFLINE_PROPERTIES", uniqueConstraints = @UniqueConstraint(columnNames = {
    "PROPERTYDEF_ID",
    "PROPERTY_MAPPING_ID"}))
public class CmsDAOOfflineProperties implements I_CmsDAOProperties {

    /** The property defenition id. */
    @Basic
    @Column(name = "PROPERTYDEF_ID", nullable = false, length = 36)
    private String m_propertyDefId;

    /** The property id. */
    @Id
    @Column(name = "PROPERTY_ID", length = 36)
    private String m_propertyId;

    /** The property mapping id. */
    @Basic
    @Column(name = "PROPERTY_MAPPING_ID", nullable = false, length = 36)
    private String m_propertyMappingId;

    /** The property mapping type. */
    @Basic
    @Column(name = "PROPERTY_MAPPING_TYPE")
    private int m_propertyMappingType;

    /** The property value. */
    @Basic
    @Column(name = "PROPERTY_VALUE", nullable = false, length = 2048)
    private String m_propertyValue;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOfflineProperties() {

        // noop
    }

    /**
     * A public constructor for generating a new log object with an unique id.<p>
     * 
     * @param propertyId the property id
     */
    public CmsDAOOfflineProperties(String propertyId) {

        m_propertyId = propertyId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#getPropertyDefId()
     */
    public String getPropertyDefId() {

        return m_propertyDefId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#getPropertyId()
     */
    public String getPropertyId() {

        return m_propertyId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#getPropertyMappingId()
     */
    public String getPropertyMappingId() {

        return m_propertyMappingId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#getPropertyMappingType()
     */
    public int getPropertyMappingType() {

        return m_propertyMappingType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#getPropertyValue()
     */
    public String getPropertyValue() {

        return m_propertyValue;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#setPropertyDefId(java.lang.String)
     */
    public void setPropertyDefId(String propertydefId) {

        m_propertyDefId = propertydefId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#setPropertyId(java.lang.String)
     */
    public void setPropertyId(String propertyId) {

        m_propertyId = propertyId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#setPropertyMappingId(java.lang.String)
     */
    public void setPropertyMappingId(String propertyMappingId) {

        m_propertyMappingId = propertyMappingId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#setPropertyMappingType(int)
     */
    public void setPropertyMappingType(int propertyMappingType) {

        m_propertyMappingType = propertyMappingType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOProperties#setPropertyValue(java.lang.String)
     */
    public void setPropertyValue(String propertyValue) {

        m_propertyValue = propertyValue;
    }
}