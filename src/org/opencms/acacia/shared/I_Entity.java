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

package org.opencms.acacia.shared;

import java.util.List;

/**
 * Interface describing an entity.<p>
 */
public interface I_Entity {

    /**
     * Adds the given attribute value.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    void addAttributeValue(String attributeName, I_Entity value);

    /**
     * Adds the given attribute value.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    void addAttributeValue(String attributeName, String value);

    /**
     * Creates a deep copy of this entity.<p>
     * 
     * @param entityId the id of the new entity, if <code>null</code> a generic id will be used
     * 
     * @return the entity copy
     */
    I_Entity createDeepCopy(String entityId);

    /**
     * Returns an attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the attribute value
     */
    I_EntityAttribute getAttribute(String attributeName);

    /**
     * Returns all entity attributes.<p>
     * 
     * @return the entity attributes
     */
    List<I_EntityAttribute> getAttributes();

    /**
     * Returns the entity id/URI.<p>
     *
     * @return the id/URI
     */
    String getId();

    /**
     * Returns the entity type name.<p>
     * 
     * @return the entity type name
     */
    String getTypeName();

    /**
     * Returns if the entity has the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return <code>true</code> if the entity has the given attribute
     */
    boolean hasAttribute(String attributeName);

    /**
     * Inserts a new attribute value at the given index.<p>
     * 
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    void insertAttributeValue(String attributeName, I_Entity value, int index);

    /**
     * Inserts a new attribute value at the given index.<p>
     * 
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    void insertAttributeValue(String attributeName, String value, int index);

    /**
     * Removes the given attribute.<p>
     *
     * @param attributeName the attribute name
     */
    void removeAttribute(String attributeName);

    /**
     * Removes the attribute without triggering any change events.<p>
     *
     * @param attributeName the attribute name
     */
    void removeAttributeSilent(String attributeName);

    /**
     * Removes a specific attribute value.<p>
     * 
     * @param attributeName the attribute name
     * @param index the value index
     */
    void removeAttributeValue(String attributeName, int index);

    /**
     * Sets the given attribute value. Will remove all previous attribute values.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    void setAttributeValue(String attributeName, I_Entity value);

    /**
     * Sets the given attribute value at the given index.<p>
     * 
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    void setAttributeValue(String attributeName, I_Entity value, int index);

    /**
     * Sets the given attribute value. Will remove all previous attribute values.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    void setAttributeValue(String attributeName, String value);

    /**
     * Sets the given attribute value at the given index.<p>
     * 
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    void setAttributeValue(String attributeName, String value, int index);

    /**
     * Returns the JSON string representation of this entity.<p>
     * 
     * @return the JSON string representation of this entity
     */
    String toJSON();
}