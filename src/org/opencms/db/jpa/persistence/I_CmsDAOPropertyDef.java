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
 * This interface declares the getters and setters for the property definitions data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.file.CmsPropertyDefinition
 */
public interface I_CmsDAOPropertyDef {

    /**
     * Returns the id of this property definition entry.<p>
     *
     * @return the id
     */
    String getPropertyDefId();

    /**
     * Sets the property definition id of this entry.<p>
     *
     * @param propertydefId the id to set
     */
    void setPropertyDefId(String propertydefId);

    /**
     * Returns the property definition name.<p>
     *
     * @return the name
     */
    String getPropertyDefName();

    /**
     * Sets the property definitions name.<p>
     *
     * @param propertydefName the name to set
     */
    void setPropertyDefName(String propertydefName);

    /**
     * Returns the property definition type.<p>
     *
     * @return the type
     */
    int getPropertyDefType();

    /**
     * Sets the property definition type.<p>
     *
     * @param propertydefType the type to set
     */
    void setPropertyDefType(int propertydefType);

}