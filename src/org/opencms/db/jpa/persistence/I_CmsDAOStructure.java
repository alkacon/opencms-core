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
 * This interface declares the getters and setters for the structure data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.file.CmsResource
 */
public interface I_CmsDAOStructure {

    /**
     * Returns the date expired.<p>
     *
     * @return the date expired
     */
    long getDateExpired();

    /**
     * Returns the date released.<p>
     *
     * @return the date released
     */
    long getDateReleased();

    /**
     * Returns the parent id.<p>
     *
     * @return the parent id
     */
    String getParentId();

    /**
     * Returns the resource id.<p>
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Returns the resource path.<p>
     *
     * @return the resource path
     */
    String getResourcePath();

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    String getStructureId();

    /**
     * Returns the structure state.<p>
     *
     * @return the structure state
     */
    int getStructureState();

    /**
     * Returns the structure version.<p>
     *
     * @return the structure version
     */
    int getStructureVersion();

    /**
     * Sets the date expired.<p>
     *
     * @param dateExpired the date to set
     */
    void setDateExpired(long dateExpired);

    /**
     * Sets the date released.<p>
     *
     * @param dateReleased the date to set
     */
    void setDateReleased(long dateReleased);

    /**
     * Sets the parent id.<p>
     *
     * @param parentId the id to set
     */
    void setParentId(String parentId);

    /**
     * Sets the resource id.<p>
     *
     * @param resourceId the id to set
     */
    void setResourceId(String resourceId);

    /**
     * Sets the resource path.<p>
     *
     * @param resourcePath the path to set
     */
    void setResourcePath(String resourcePath);

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the id to set
     */
    void setStructureId(String structureId);

    /**
     * Sets the structure state.<p>
     *
     * @param structureState the state to set
     */
    void setStructureState(int structureState);

    /**
     * Sets the structure version.<p>
     *
     * @param structureVersion the version to set
     */
    void setStructureVersion(int structureVersion);

}