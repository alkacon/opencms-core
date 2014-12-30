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

import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsType;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * The interface for VIE implementations.<p>
 */
public interface I_CmsEntityBackend {

    /**
     * Changes the original entities content to the given new content.<p>
     * 
     * @param original the original entity to change
     * @param newContent the new content entity
     */
    void changeEntityContentValues(CmsEntity original, CmsEntity newContent);

    /**
     * Clears all entities from VIE.<p>
     */
    void clearEntities();

    /**
     * Creates a new entity registering it within VIE.<p>
     * 
     * @param entityId the entity id
     * @param entityType the entity type
     * 
     * @return the new entity
     */
    CmsEntity createEntity(String entityId, String entityType);

    /**
     * Creates a new type.<p>
     * 
     * @param id the type id/name
     * 
     * @return the new type
     */
    CmsType createType(String id);

    /**
     * Returns DOM elements displaying the given attribute's value.<p>
     * 
     * @param entity the entity
     * @param attributeName the attribute name
     * @param context the context to search within
     * 
     * @return the elements
     */
    List<Element> getAttributeElements(CmsEntity entity, String attributeName, Element context);

    /**
     * Returns DOM elements displaying the given attribute's value.<p>
     * 
     * @param entityId the entity id/subject
     * @param attributeName the attribute name
     * @param context the context to search within
     * 
     * @return the elements
     */
    List<Element> getAttributeElements(String entityId, String attributeName, Element context);

    /**
     * Returns the entity with the given id.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the entity
     */
    CmsEntity getEntity(String entityId);

    /**
     * Returns the type with the given id/name.<p>
     * 
     * @param id the type id/name
     * 
     * @return the type
     */
    CmsType getType(String id);

    /**
     * Registers the given entity within the VIE model.<p>
     * 
     * @param entity the entity to register
     * 
     * @return the new registered entity object
     */
    CmsEntity registerEntity(CmsEntity entity);

    /**
     * Registers the given entity within the VIE model.<p>
     * 
     * @param entity the entity to register
     * @param discardIds <code>true</code> to discard the entity ids and generate ids
     * 
     * @return the new registered entity object
     */
    CmsEntity registerEntity(CmsEntity entity, boolean discardIds);

    /**
     * Registers the type and it's sub-types.<p>
     * 
     * @param type the type to register
     * @param types the available types
     */
    void registerTypes(CmsType type, Map<String, CmsType> types);

    /**
     * Removes the given entity from VIE.<p>
     * 
     * @param entityId the entity id
     */
    void removeEntity(String entityId);
}
