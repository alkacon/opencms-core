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
 * This interface declares the getters and setters for the relation data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.relations.CmsRelation
 */
public interface I_CmsDAOResourceRelations {

    /**
     * Returns the source id of this relation.<p>
     *
     * @return the source id
     */
    String getRelationSourceId();

    /**
     * Sets the source id for this relation.<p>
     *
     * @param relationSourceId the id to set
     */
    void setRelationSourceId(String relationSourceId);

    /**
     * Returns the source path of this relation.<p>
     *
     * @return the source path
     */
    String getRelationSourcePath();

    /**
     * Sets the source path for this resource relation.<p>
     *
     * @param relationSourcePath the source path to set
     */
    void setRelationSourcePath(String relationSourcePath);

    /**
     * Returns the target id of this relation.<p>
     *
     * @return the target id
     */
    String getRelationTargetId();

    /**
     * Sets the target id for this relation.<p>
     *
     * @param relationTargetId the id to set
     */
    void setRelationTargetId(String relationTargetId);

    /**
     * Returns the target path of this relation.<p>
     *
     * @return the target path
     */
    String getRelationTargetPath();

    /**
     * Sets the target path for this resource relation.<p>
     *
     * @param relationTargetPath the target path to set
     */
    void setRelationTargetPath(String relationTargetPath);

    /**
     * Returns the relation type of this resource relation.<p>
     *
     * @return the type
     */
    int getRelationType();

    /**
     * Sets the relation type for this resource relation.<p>
     *
     * @param relationType the type to set
     */
    void setRelationType(int relationType);

}