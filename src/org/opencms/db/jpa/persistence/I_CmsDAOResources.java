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
 * This interface declares the getters and setters for the resource data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.file.CmsResource
 */
public interface I_CmsDAOResources {

    /**
     * Returns the date content.<p>
     *
     * @return the date content
     */
    long getDateContent();

    /**
     * Sets the date content.<p>
     *
     * @param dateContent the date to set
     */
    void setDateContent(long dateContent);

    /**
     * Returns the date created.<p>
     *
     * @return the date created
     */
    long getDateCreated();

    /**
     * Sets the date created.<p>
     *
     * @param dateCreated the date created to set
     */
    void setDateCreated(long dateCreated);

    /**
     * Returns the date last modified.<p>
     *
     * @return the date last modified
     */
    long getDateLastModified();

    /**
     * Sets the date last modified.<p>
     *
     * @param dateLastmodified the date to set
     */
    void setDateLastModified(long dateLastmodified);

    /**
     * Returns the project last modified.<p>
     *
     * @return the project last modified
     */
    String getProjectLastModified();

    /**
     * Sets the project last modified.<p>
     *
     * @param projectLastmodified the project to set
     */
    void setProjectLastModified(String projectLastmodified);

    /**
     * Returns the resource flags.<p>
     *
     * @return the resource flags
     */
    int getResourceFlags();

    /**
     * Sets the resource flags.<p>
     *
     * @param resourceFlags the resource flags to set
     */
    void setResourceFlags(int resourceFlags);

    /**
     * Returns the resource id.<p>
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Sets the resource id.<p>
     *
     * @param resourceId the id to set
     */
    void setResourceId(String resourceId);

    /**
     * Returns the resource size.<p>
     *
     * @return the resource size
     */
    int getResourceSize();

    /**
     * Sets the resource size.<p>
     *
     * @param resourceSize the resource size to set
     */
    void setResourceSize(int resourceSize);

    /**
     * Returns the resource state.<p>
     *
     * @return the resource state
     */
    int getResourceState();

    /**
     * Sets the resource state.<p>
     *
     * @param resourceState the state to set
     */
    void setResourceState(int resourceState);

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    int getResourceType();

    /**
     * Sets the resource type.<p>
     *
     * @param resourceType the type to set
     */
    void setResourceType(int resourceType);

    /**
     * Returns the resource version.<p>
     *
     * @return the resource version
     */
    int getResourceVersion();

    /**
     * Sets the resource version.<p>
     *
     * @param resourceVersion the version to set
     */
    void setResourceVersion(int resourceVersion);

    /**
     * Returns the count of siblings.<p>
     *
     * @return the count of siblings
     */
    int getSiblingCount();

    /**
     * Sets the count of siblings.<p>
     *
     * @param siblingCount the count to set
     */
    void setSiblingCount(int siblingCount);

    /**
     * Returns the user created.<p>
     *
     * @return the user created
     */
    String getUserCreated();

    /**
     * Sets the user created.<p>
     *
     * @param userCreated the user to set
     */
    void setUserCreated(String userCreated);

    /**
     * Returns the user last modified.<p>
     *
     * @return the user last modified
     */
    String getUserLastModified();

    /**
     * Sets the user last modified.<p>
     *
     * @param userLastmodified the user to set
     */
    void setUserLastModified(String userLastmodified);

}