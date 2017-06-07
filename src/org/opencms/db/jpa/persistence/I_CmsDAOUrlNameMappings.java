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
 * This interface declares the getters and setters for the URL mapping data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.file.CmsResource
 */
public interface I_CmsDAOUrlNameMappings {

    /**
     * Returns the date changed.<p>
     *
     * @return the changed date
     */
    long getDateChanged();

    /**
     * Sets the date changed.<p>
     *
     * @param dateChanged the date to set
     */
    void setDateChanged(long dateChanged);

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name.<p>
     * @param name the name to set
     */
    void setName(String name);

    /**
     * Returns the state.<p>
     *
     * @return the state
     */
    int getState();

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    void setLocale(String locale);

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    String getLocale();

    /**
     * Sets the state.<p>
     *
     * @param state the state to set
     */
    void setState(int state);

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    String getStructureId();

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the id to set
     */
    void setStructureId(String structureId);

}