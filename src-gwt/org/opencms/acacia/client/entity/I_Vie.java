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
import org.opencms.acacia.shared.I_Type;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * The interface for VIE implementations.<p>
 */
public interface I_Vie {

    /**
     * Binds a given callback to the entities of vie.<p>
     * 
     * @param functionName the name of the function
     * @param callback the function that should be executed
     */
    void bindFunctionToEntities(String functionName, I_EntityCallback callback);

    /**
     * Changes the original entities content to the given new content.<p>
     * 
     * @param original the original entity to change
     * @param newContent the new content entity
     */
    void changeEntityContentValues(Entity original, I_Entity newContent);

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
    I_Entity createEntity(String entityId, String entityType);

    /**
     * Creates a new type.<p>
     * 
     * @param id the type id/name
     * 
     * @return the new type
     */
    I_Type createType(String id);

    /**
     * Returns all attribute elements within a given context.<p>
     * 
     * @param context the context to search within
     * 
     * @return the elements
     */
    List<Element> getAttributeElements(Element context);

    /**
     * Returns DOM elements displaying the given attribute's value.<p>
     * 
     * @param entity the entity
     * @param attributeName the attribute name
     * @param context the context to search within
     * 
     * @return the elements
     */
    List<Element> getAttributeElements(I_Entity entity, String attributeName, Element context);

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
     * Returns the element subject.<p>
     * 
     * @param element the DOM element
     * 
     * @return the elements subject
     */
    String getElementPredicate(Element element);

    /**
     * Returns the element subject.<p>
     * 
     * @param element the DOM element
     * 
     * @return the elements subject
     */
    String getElementSubject(Element element);

    /**
     * Returns the entities of vie.<p>
     * 
     * @return the entities
     */
    I_EntityCollection getEntities();

    /**
     * Returns the entity with the given id.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the entity
     */
    I_Entity getEntity(String entityId);

    /**
     * Returns the type with the given id/name.<p>
     * 
     * @param id the type id/name
     * 
     * @return the type
     */
    I_Type getType(String id);

    /**
     * Executes the load function on the VIE instance.<p>
     * 
     * @param service the name of the service to use
     * @param selector the jQuery selector to specify the HTML-Elements inside the DOM to search for entities
     * @param callback the callback that is executed on success 
     */
    void load(String service, String selector, I_EntityArrayCallback callback);

    /**
     * Registers the given entity within the VIE model.<p>
     * 
     * @param entity the entity to register
     * 
     * @return the new registered entity object
     */
    I_Entity registerEntity(I_Entity entity);

    /**
     * Registers the given entity within the VIE model.<p>
     * 
     * @param entity the entity to register
     * @param discardIds <code>true</code> to discard the entity ids and generate ids
     * 
     * @return the new registered entity object
     */
    I_Entity registerEntity(I_Entity entity, boolean discardIds);

    /**
     * Registers the type and it's sub-types.<p>
     * 
     * @param type the type to register
     * @param types the available types
     */
    void registerTypes(I_Type type, Map<String, I_Type> types);

    /**
     * Removes the given entity from VIE.<p>
     * 
     * @param entityId the entity id
     */
    void removeEntity(String entityId);
}
