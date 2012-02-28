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
 * This data access object represents a history properties definition entry 
 * inside the table "cms_history_propertydef".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_PROPERTYDEF")
public class CmsDAOHistoryPropertyDef {

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
    public CmsDAOHistoryPropertyDef() {

        // noop
    }

    /**
     * * A public constructor for generating a new history property object with an unique id.<p>
     * 
     * @param propertydefId the property definition id
     */
    public CmsDAOHistoryPropertyDef(String propertydefId) {

        m_propertyDefId = propertydefId;
    }

    /**
     * Returns the propertyDefId.<p>
     *
     * @return the propertyDefId
     */
    public String getPropertyDefId() {

        return m_propertyDefId;
    }

    /**
     * Returns the propertyDefName.<p>
     *
     * @return the propertyDefName
     */
    public String getPropertyDefName() {

        return m_propertyDefName;
    }

    /**
     * Returns the propertyDefType.<p>
     *
     * @return the propertyDefType
     */
    public int getPropertyDefType() {

        return m_propertyDefType;
    }

    /**
     * Sets the propertyDefId.<p>
     *
     * @param propertyDefId the propertyDefId to set
     */
    public void setPropertyDefId(String propertyDefId) {

        m_propertyDefId = propertyDefId;
    }

    /**
     * Sets the propertyDefName.<p>
     *
     * @param propertyDefName the propertyDefName to set
     */
    public void setPropertyDefName(String propertyDefName) {

        m_propertyDefName = propertyDefName;
    }

    /**
     * Sets the propertyDefType.<p>
     *
     * @param propertyDefType the propertyDefType to set
     */
    public void setPropertyDefType(int propertyDefType) {

        m_propertyDefType = propertyDefType;
    }

}