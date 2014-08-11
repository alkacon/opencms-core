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

import org.opencms.acacia.shared.Entity;
import org.opencms.acacia.shared.EntityAttribute;
import org.opencms.acacia.shared.Type;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * The editor data back-end.<p>
 */
public final class Vie implements I_Vie {

    /** The registered entities. */
    private Map<String, Entity> m_entities;

    /** The registered types. */
    private Map<String, Type> m_types;

    /** Entity id counter. */
    private int m_count;

    /** The instance. */
    private static Vie INSTANCE;

    /**
     * Constructor.<p>
     */
    public Vie() {

        m_entities = new HashMap<String, Entity>();
        m_types = new HashMap<String, Type>();
    }

    /**
     * Returns the instance.<p>
     * 
     * @return the instance
     */
    public static Vie getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new Vie();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#changeEntityContentValues(org.opencms.acacia.shared.Entity, org.opencms.acacia.shared.Entity)
     */
    public void changeEntityContentValues(Entity original, Entity newContent) {

        clearEntityAttributes(original);
        for (EntityAttribute attribute : newContent.getAttributes()) {
            if (attribute.isSimpleValue()) {
                for (String value : attribute.getSimpleValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                for (Entity value : attribute.getComplexValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), registerEntity(value));
                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#clearEntities()
     */
    public void clearEntities() {

        m_entities.clear();
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#createEntity(java.lang.String, java.lang.String)
     */
    public Entity createEntity(String entityId, String entityType) {

        if (entityId == null) {
            entityId = generateId();
        }
        if (!m_types.containsKey(entityType)) {
            throw new IllegalArgumentException("Type " + entityType + " is not registered yet");
        }
        if (m_entities.containsKey(entityId)) {
            throw new IllegalArgumentException("Entity " + entityId + " is already registered");
        }
        Entity entity = new Entity(entityId, entityType);
        m_entities.put(entityId, entity);
        return entity;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#createType(java.lang.String)
     */
    public Type createType(String id) {

        if (m_types.containsKey(id)) {
            throw new IllegalArgumentException("Type " + id + " is already registered");
        }
        Type type = new Type(id);
        m_types.put(id, type);
        return type;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getAttributeElements(org.opencms.acacia.shared.Entity, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(Entity entity, String attributeName, Element context) {

        return getAttributeElements(entity.getId(), attributeName, context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getAttributeElements(java.lang.String, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(String entityId, String attributeName, Element context) {

        String selector = "[about='"
            + entityId
            + "'][property='"
            + attributeName
            + "'], [about='"
            + entityId
            + "'] [property='"
            + attributeName
            + "']";
        if (context == null) {
            context = Document.get().getDocumentElement();
        }
        return select(selector, context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getEntity(java.lang.String)
     */
    public Entity getEntity(String entityId) {

        return m_entities.get(entityId);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getType(java.lang.String)
     */
    public Type getType(String id) {

        return m_types.get(id);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#registerEntity(org.opencms.acacia.shared.Entity)
     */
    public Entity registerEntity(Entity entity) {

        if (m_entities.containsKey(entity.getId())) {
            throw new IllegalArgumentException("Entity " + entity.getId() + " is already registered");
        }
        if (!m_types.containsKey(entity.getTypeName())) {
            throw new IllegalArgumentException("Type " + entity.getTypeName() + " is not registered yet");
        }
        for (EntityAttribute attr : entity.getAttributes()) {
            if (attr.isComplexValue()) {
                for (Entity child : attr.getComplexValues()) {
                    registerEntity(child);
                }
            }
        }
        entity.ensureChangeHandlers();
        m_entities.put(entity.getId(), entity);
        return entity;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#registerEntity(org.opencms.acacia.shared.Entity, boolean)
     */
    public Entity registerEntity(Entity entity, boolean discardIds) {

        if (!discardIds) {
            return registerEntity(entity);
        } else {
            if (!m_types.containsKey(entity.getTypeName())) {
                throw new IllegalArgumentException("Type " + entity.getTypeName() + " is not registered yet");
            }
            Entity result = createEntity(null, entity.getTypeName());
            for (EntityAttribute attr : entity.getAttributes()) {
                if (attr.isComplexValue()) {
                    for (Entity child : attr.getComplexValues()) {
                        result.addAttributeValue(attr.getAttributeName(), registerEntity(child, discardIds));
                    }
                } else {
                    for (String value : attr.getSimpleValues()) {
                        result.addAttributeValue(attr.getAttributeName(), value);
                    }
                }

            }
            result.ensureChangeHandlers();
            m_entities.put(result.getId(), result);
            return result;
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#registerTypes(org.opencms.acacia.shared.Type, java.util.Map)
     */
    public void registerTypes(Type type, Map<String, Type> types) {

        if (!m_types.containsKey(type.getId())) {
            for (String attrName : type.getAttributeNames()) {
                String subTypeId = type.getAttributeTypeName(attrName);
                Type subType = types.get(subTypeId);
                if (subType == null) {
                    throw new IllegalArgumentException("Type information for " + subTypeId + " is missing");
                }
                registerTypes(subType, types);
            }
            m_types.put(type.getId(), type);
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#removeEntity(java.lang.String)
     */
    public void removeEntity(String entityId) {

        if (m_entities.containsKey(entityId)) {
            Entity entity = m_entities.get(entityId);
            for (EntityAttribute attr : entity.getAttributes()) {
                if (attr.isComplexValue()) {
                    for (Entity child : attr.getComplexValues()) {
                        removeEntity(child.getId());
                    }
                }
            }
            m_entities.remove(entityId);
        }
    }

    /**
     * Returns a list of DOM elements matching the given selector.<p>
     * 
     * @param selector the CSS selector
     * @param context the context element
     * 
     * @return the matching elements
     */
    protected List<Element> select(String selector, Element context) {

        NodeList<Element> elements = CmsDomUtil.querySelectorAll(selector, context);
        List<Element> result = new ArrayList<Element>();
        for (int i = 0; i < elements.getLength(); i++) {
            result.add(elements.getItem(i));
        }
        return result;
    }

    /**
     * Removes all attributes from the given entity.<p>
     * 
     * @param entity the entity
     */
    private void clearEntityAttributes(Entity entity) {

        for (EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isComplexValue()) {
                for (Entity child : attribute.getComplexValues()) {
                    clearEntityAttributes(child);
                    removeEntity(child.getId());
                }
            }
            entity.removeAttributeSilent(attribute.getAttributeName());
        }
    }

    /**
     * Generates a new entity id.<p>
     * 
     * @return the generated id
     */
    private String generateId() {

        m_count++;
        String id = "generic_id" + m_count;
        while (m_entities.containsKey(id)) {
            m_count++;
            id = "generic_id" + m_count;
        }
        return id;
    }
}
