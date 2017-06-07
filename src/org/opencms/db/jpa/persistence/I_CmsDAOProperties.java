/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

/**
 * This interface declares the getters and setters for the properties data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.file.CmsProperty
 */
public interface I_CmsDAOProperties {

    /**
     * Returns the id of this property entry.<p>
     *
     * @return the id
     */
    String getPropertyId();

    /**
     * Sets the id of this property entry.<p>
     *
     * @param propertyId the id to set
     */
    void setPropertyId(String propertyId);

    /**
     * Returns the mapping id of this property entry.<p>
     *
     * @return the mapping id
     */
    String getPropertyMappingId();

    /**
     * Sets the mapping id for this property entry.<p>
     *
     * @param propertyMappingId the mapping id to set
     */
    void setPropertyMappingId(String propertyMappingId);

    /**
     * Returns the mapping type.<p>
     *
     * @return the mapping type
     */
    int getPropertyMappingType();

    /**
     * Sets the mapping type.<p>
     *
     * @param propertyMappingType the mapping type to set
     */
    void setPropertyMappingType(int propertyMappingType);

    /**
     * Returns the value of this property entry.<p>
     *
     * @return the value
     */
    String getPropertyValue();

    /**
     * Sets the value for this property.<p>
     *
     * @param propertyValue the value to set
     */
    void setPropertyValue(String propertyValue);

    /**
     * Returns the property definition id.<p>
     *
     * @return the definition id
     */
    String getPropertyDefId();

    /**
     * Sets the property definition id.<p>
     *
     * @param propertydefId the definition id to set
     */
    void setPropertyDefId(String propertydefId);

}