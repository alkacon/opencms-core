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
 * This data access object represents a offline property definition entry inside the table "cms_offline_propertydef".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_OFFLINE_PROPERTYDEF", uniqueConstraints = @UniqueConstraint(columnNames = {"PROPERTYDEF_NAME"}))
public class CmsDAOOfflinePropertyDef implements I_CmsDAOPropertyDef {

    /** The property definition id. */
    @Id
    @Column(name = "PROPERTYDEF_ID", length = 36)
    private String m_propertyDefId;

    /** The property definition name. */
    @Basic
    @Column(name = "PROPERTYDEF_NAME", nullable = false, length = 128)
    private String m_propertyDefName;

    /** The property definition type. */
    @Basic
    @Column(name = "PROPERTYDEF_TYPE")
    private int m_propertyDefType;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOfflinePropertyDef() {

        // noop
    }

    /**
     * A public constructor for generating a new offline property definition object with an unique id.<p>
     * 
     * @param propertydefId the id
     */
    public CmsDAOOfflinePropertyDef(String propertydefId) {

        m_propertyDefId = propertydefId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#getPropertyDefId()
     */
    public String getPropertyDefId() {

        return m_propertyDefId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#getPropertyDefName()
     */
    public String getPropertyDefName() {

        return m_propertyDefName;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#getPropertyDefType()
     */
    public int getPropertyDefType() {

        return m_propertyDefType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#setPropertyDefId(java.lang.String)
     */
    public void setPropertyDefId(String propertydefId) {

        m_propertyDefId = propertydefId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#setPropertyDefName(java.lang.String)
     */
    public void setPropertyDefName(String propertydefName) {

        m_propertyDefName = propertydefName;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef#setPropertyDefType(int)
     */
    public void setPropertyDefType(int propertydefType) {

        m_propertyDefType = propertydefType;
    }
}