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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Serializable entity implementation.<p>
 */
public class Entity implements I_Entity, Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = -6933931178070025267L;

    /** The entity attribute values. */
    private Map<String, List<Entity>> m_entityAttributes;

    /** The entity id. */
    private String m_id;

    /** The simple attribute values. */
    private Map<String, List<String>> m_simpleAttributes;

    /** The type name. */
    private String m_typeName;

    /**
     * Constructor.<p>
     * 
     * @param id the entity id/URI
     * @param typeName the entity type name
     */
    public Entity(String id, String typeName) {

        this();
        m_id = id;
        m_typeName = typeName;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected Entity() {

        m_simpleAttributes = new HashMap<String, List<String>>();
        m_entityAttributes = new HashMap<String, List<Entity>>();
    }

    /**
     * Returns the value of a simple attribute for the given path or <code>null</code>, if the value does not exist.<p>
     * 
     * @param entity the entity to get the value from
     * @param pathElements the path elements
     * 
     * @return the value
     */
    public static String getValueForPath(I_Entity entity, String[] pathElements) {

        String result = null;
        if ((pathElements != null) && (pathElements.length >= 1)) {
            String attributeName = pathElements[0];
            int index = ContentDefinition.extractIndex(attributeName);
            if (index > 0) {
                index--;
            }
            attributeName = entity.getTypeName() + "/" + ContentDefinition.removeIndex(attributeName);
            I_EntityAttribute attribute = entity.getAttribute(attributeName);
            if (!((attribute == null) || (attribute.isComplexValue() && (pathElements.length == 1)))) {
                if (attribute.isSimpleValue()) {
                    if ((pathElements.length == 1) && (attribute.getValueCount() > 0)) {
                        List<String> values = attribute.getSimpleValues();
                        result = values.get(index);
                    }
                } else if (attribute.getValueCount() > (index)) {
                    String[] childPathElements = new String[pathElements.length - 1];
                    for (int i = 1; i < pathElements.length; i++) {
                        childPathElements[i - 1] = pathElements[i];
                    }
                    List<I_Entity> values = attribute.getComplexValues();
                    result = getValueForPath(values.get(index), childPathElements);
                }
            }
        }
        return result;
    }

    /**
     * Returns a serializable version of the given entity.<p>
     * 
     * @param entity the entity
     * 
     * @return the serializable version
     */
    public static Entity serializeEntity(I_Entity entity) {

        Entity result = new Entity(entity.getId(), entity.getTypeName());
        for (I_EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isSimpleValue()) {
                List<String> values = attribute.getSimpleValues();
                for (String value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                List<I_Entity> values = attribute.getComplexValues();
                for (I_Entity value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), serializeEntity(value));
                }
            }
        }
        return result;
    }

    /**
     * Transforms into a serializable entity instance.<p>
     * 
     * @param entity the entity to transform
     * 
     * @return the new entity
     */
    public static Entity transformToSerializableEntity(I_Entity entity) {

        if (entity instanceof Entity) {
            return (Entity)entity;
        }
        Entity result = new Entity(entity.getId(), entity.getTypeName());
        for (I_EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isSimpleValue()) {
                for (String value : attribute.getSimpleValues()) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                for (I_Entity value : attribute.getComplexValues()) {
                    result.addAttributeValue(attribute.getAttributeName(), transformToSerializableEntity(value));
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#addAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity)
     */
    public void addAttributeValue(String attributeName, I_Entity value) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (!(value instanceof Entity)) {
            value = transformToSerializableEntity(value);
        }
        if (m_entityAttributes.containsKey(attributeName)) {
            m_entityAttributes.get(attributeName).add((Entity)value);
        } else {
            List<Entity> values = new ArrayList<Entity>();
            values.add((Entity)value);
            m_entityAttributes.put(attributeName, values);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#addAttributeValue(java.lang.String, java.lang.String)
     */
    public void addAttributeValue(String attributeName, String value) {

        if (m_entityAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a entity type value.");
        }
        if (m_simpleAttributes.containsKey(attributeName)) {
            m_simpleAttributes.get(attributeName).add(value);
        } else {
            List<String> values = new ArrayList<String>();
            values.add(value);
            m_simpleAttributes.put(attributeName, values);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#createDeepCopy(java.lang.String)
     */
    public Entity createDeepCopy(String entityId) {

        Entity result = new Entity(entityId, getTypeName());
        for (I_EntityAttribute attribute : getAttributes()) {
            if (attribute.isSimpleValue()) {
                List<String> values = attribute.getSimpleValues();
                for (String value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                List<I_Entity> values = attribute.getComplexValues();
                for (I_Entity value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value.createDeepCopy(null));
                }
            }
        }
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean result = false;
        if (obj instanceof Entity) {
            Entity test = (Entity)obj;
            if (m_simpleAttributes.keySet().equals(test.m_simpleAttributes.keySet())
                && m_entityAttributes.keySet().equals(test.m_entityAttributes.keySet())) {
                result = true;
                for (String attributeName : m_simpleAttributes.keySet()) {
                    if (!m_simpleAttributes.get(attributeName).equals(test.m_simpleAttributes.get(attributeName))) {
                        result = false;
                        break;
                    }
                }
                if (result) {
                    for (String attributeName : m_entityAttributes.keySet()) {
                        if (!m_entityAttributes.get(attributeName).equals(test.m_entityAttributes.get(attributeName))) {
                            result = false;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getAttribute(java.lang.String)
     */
    public I_EntityAttribute getAttribute(String attributeName) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            return EntityAttribute.createSimpleAttribute(attributeName, m_simpleAttributes.get(attributeName));
        }
        if (m_entityAttributes.containsKey(attributeName)) {
            return EntityAttribute.createEntityAttribute(attributeName, m_entityAttributes.get(attributeName));
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getAttributes()
     */
    public List<I_EntityAttribute> getAttributes() {

        List<I_EntityAttribute> result = new ArrayList<I_EntityAttribute>();
        for (String name : m_simpleAttributes.keySet()) {
            result.add(getAttribute(name));
        }
        for (String name : m_entityAttributes.keySet()) {
            result.add(getAttribute(name));
        }
        return result;
    }

    /**
     * Returns this or a child entity with the given id.<p>
     * Will return <code>null</code> if no entity with the given id is present.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the entity
     */
    public Entity getEntityById(String entityId) {

        Entity result = null;
        if (m_id.equals(entityId)) {
            result = this;
        } else {
            for (List<Entity> children : m_entityAttributes.values()) {
                for (Entity child : children) {
                    result = child.getEntityById(entityId);
                    if (result != null) {
                        break;
                    }
                }
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getTypeName()
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String attributeName) {

        return m_simpleAttributes.containsKey(attributeName) || m_entityAttributes.containsKey(attributeName);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#insertAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity, int)
     */
    public void insertAttributeValue(String attributeName, I_Entity value, int index) {

        if (m_entityAttributes.containsKey(attributeName)) {
            m_entityAttributes.get(attributeName).add(index, (Entity)value);
        } else {
            setAttributeValue(attributeName, value);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#insertAttributeValue(java.lang.String, java.lang.String, int)
     */
    public void insertAttributeValue(String attributeName, String value, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            m_simpleAttributes.get(attributeName).add(index, value);
        } else {
            setAttributeValue(attributeName, value);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String attributeName) {

        removeAttributeSilent(attributeName);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#removeAttributeSilent(java.lang.String)
     */
    public void removeAttributeSilent(String attributeName) {

        m_simpleAttributes.remove(attributeName);
        m_entityAttributes.remove(attributeName);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#removeAttributeValue(java.lang.String, int)
     */
    public void removeAttributeValue(String attributeName, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            List<String> values = m_simpleAttributes.get(attributeName);
            if ((values.size() == 1) && (index == 0)) {
                removeAttribute(attributeName);
            } else {
                values.remove(index);
            }
        } else if (m_entityAttributes.containsKey(attributeName)) {
            List<Entity> values = m_entityAttributes.get(attributeName);
            if ((values.size() == 1) && (index == 0)) {
                removeAttribute(attributeName);
            } else {
                values.remove(index);
            }
        }

    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity)
     */
    public void setAttributeValue(String attributeName, I_Entity value) {

        // make sure there is no simple attribute value set
        m_simpleAttributes.remove(attributeName);
        if (!(value instanceof Entity)) {
            value = transformToSerializableEntity(value);
        }
        List<Entity> values = new ArrayList<Entity>();
        values.add((Entity)value);
        m_entityAttributes.put(attributeName, values);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity, int)
     */
    public void setAttributeValue(String attributeName, I_Entity value, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (!(value instanceof Entity)) {
            // ensure serializable entity
            value = transformToSerializableEntity(value);
        }
        if (!m_entityAttributes.containsKey(attributeName)) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            } else {
                List<Entity> values = new ArrayList<Entity>();
                values.add((Entity)value);
                m_entityAttributes.put(attributeName, values);
            }
        } else {
            m_entityAttributes.get(attributeName).add(index, (Entity)value);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String)
     */
    public void setAttributeValue(String attributeName, String value) {

        m_entityAttributes.remove(attributeName);
        List<String> values = new ArrayList<String>();
        values.add(value);
        m_simpleAttributes.put(attributeName, values);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String, int)
     */
    public void setAttributeValue(String attributeName, String value, int index) {

        if (m_entityAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (!m_simpleAttributes.containsKey(attributeName)) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            } else {
                List<String> values = new ArrayList<String>();
                values.add(value);
                m_simpleAttributes.put(attributeName, values);
            }
        } else {
            m_simpleAttributes.get(attributeName).add(index, value);
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#toJSON()
     */
    public String toJSON() {

        StringBuffer result = new StringBuffer();
        result.append("{\n");
        for (Entry<String, List<String>> simpleEntry : m_simpleAttributes.entrySet()) {
            result.append("\"").append(simpleEntry.getKey()).append("\"").append(": [\n");
            boolean firstValue = true;
            for (String value : simpleEntry.getValue()) {
                if (firstValue) {
                    firstValue = false;
                } else {
                    result.append(",\n");
                }
                result.append("\"").append(value).append("\"");
            }
            result.append("],\n");
        }
        for (Entry<String, List<Entity>> entityEntry : m_entityAttributes.entrySet()) {
            result.append("\"").append(entityEntry.getKey()).append("\"").append(": [\n");
            boolean firstValue = true;
            for (Entity value : entityEntry.getValue()) {
                if (firstValue) {
                    firstValue = false;
                } else {
                    result.append(",\n");
                }
                result.append(value.toJSON());
            }
            result.append("],\n");
        }
        result.append("\"id\": \"").append(m_id).append("\"");
        result.append("}");
        return result.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return toJSON();
    }
}
