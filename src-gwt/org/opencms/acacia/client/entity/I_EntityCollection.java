/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.I_Entity;

/**
 * Interface describing an entity collection.<p>
 */
public interface I_EntityCollection {

    /**
     * Adds a new entity to the collection or updates the entity with the same URI.<p>
     * 
     * @param entity the entity to add
     */
    void addOrUpdate(I_Entity entity);

    /**
     * Returns the entity for the given index.<p>
     * 
     * @param index the index to get the entity for
     * 
     * @return the entity
     */
    I_Entity getEntity(int index);

    /**
     * Returns the entity with the given id/URI.<p>
     * 
     * @param uri the entity id/URI
     * 
     * @return the entity
     */
    I_Entity getEntityById(String uri);

    /**
     * Returns the size of the collection.<p>
     * 
     * @return the size
     */
    int size();

}