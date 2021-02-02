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

import org.opencms.acacia.shared.CmsEntityChangeEvent.ChangeType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Serializable entity implementation.<p>
 */
public class CmsEntity implements HasValueChangeHandlers<CmsEntity>, Serializable {

    /**
     * Handles child entity changes.<p>
     */
    protected class EntityChangeHandler implements ValueChangeHandler<CmsEntity> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<CmsEntity> event) {

            ChangeType type = ((CmsEntityChangeEvent)event).getChangeType();
            fireChange(type);
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -6933931178070025267L;

    /** The entity attribute values. */
    private Map<String, List<CmsEntity>> m_entityAttributes;

    /** The entity id. */
    private String m_id;

    /** The simple attribute values. */
    private Map<String, List<String>> m_simpleAttributes;

    /** The type name. */
    private String m_typeName;

    /** The event bus. */
    private transient SimpleEventBus m_eventBus;

    /** The child entites change handler. */
    private transient EntityChangeHandler m_childChangeHandler = new EntityChangeHandler();

    /** The handler registrations. */
    private transient Map<String, HandlerRegistration> m_changeHandlerRegistry;

    /**
     * Constructor.<p>
     *
     * @param id the entity id/URI
     * @param typeName the entity type name
     */
    public CmsEntity(String id, String typeName) {

        this();
        m_id = id;
        m_typeName = typeName;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsEntity() {

        m_simpleAttributes = new HashMap<String, List<String>>();
        m_entityAttributes = new HashMap<String, List<CmsEntity>>();
        m_changeHandlerRegistry = new HashMap<String, HandlerRegistration>();
    }

    /**
     * Returns the value of a simple attribute for the given path or <code>null</code>, if the value does not exist.<p>
     *
     * @param entity the entity to get the value from
     * @param pathElements the path elements
     *
     * @return the value
     */
    public static String getValueForPath(CmsEntity entity, String[] pathElements) {

        String result = null;
        if ((pathElements != null) && (pathElements.length >= 1)) {
            String attributeName = pathElements[0];
            int index = CmsContentDefinition.extractIndex(attributeName);
            if (index > 0) {
                index--;
            }
            attributeName = entity.getTypeName() + "/" + CmsContentDefinition.removeIndex(attributeName);
            CmsEntityAttribute attribute = entity.getAttribute(attributeName);
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
                    List<CmsEntity> values = attribute.getComplexValues();
                    result = getValueForPath(values.get(index), childPathElements);
                }
            }
        }
        return result;
    }

    /**
     * Gets the list of values reachable from the given base object with the given path.<p>
     *
     * @param baseObject the base object (a CmsEntity or a string)
     * @param pathComponents the path components
     * @return the list of values for the given path (either of type String or CmsEntity)
     */
    public static List<Object> getValuesForPath(Object baseObject, String[] pathComponents) {

        List<Object> currentList = Lists.newArrayList();
        currentList.add(baseObject);
        for (String pathComponent : pathComponents) {
            List<Object> newList = Lists.newArrayList();
            for (Object element : currentList) {
                newList.addAll(getValuesForPathComponent(element, pathComponent));
            }
            currentList = newList;
        }
        return currentList;
    }

    /**
     * Gets the values reachable from a given object (an entity or a string) with a single XPath component.<p>
     *
     * If entityOrString is a string, and pathComponent is "VALUE", a list containing only entityOrString is returned.
     * Otherwise, entityOrString is assumed to be an entity, and the pathComponent is interpreted as a field of the entity
     * (possibly with an index).
     *
     * @param entityOrString the entity or string from which to get the values for the given path component
     * @param pathComponent the path component
     * @return the list of reachable values
     */
    public static List<Object> getValuesForPathComponent(Object entityOrString, String pathComponent) {

        List<Object> result = Lists.newArrayList();
        if (pathComponent.equals("VALUE")) {
            result.add(entityOrString);
        } else {
            if (entityOrString instanceof CmsEntity) {
                CmsEntity entity = (CmsEntity)entityOrString;
                boolean hasIndex = CmsContentDefinition.hasIndex(pathComponent);
                int index = CmsContentDefinition.extractIndex(pathComponent);
                if (index > 0) {
                    index--;
                }
                String attributeName = entity.getTypeName() + "/" + CmsContentDefinition.removeIndex(pathComponent);
                CmsEntityAttribute attribute = entity.getAttribute(attributeName);

                if (attribute != null) {
                    if (hasIndex) {
                        if (index < attribute.getValueCount()) {
                            if (attribute.isSimpleValue()) {
                                result.add(attribute.getSimpleValues().get(index));
                            } else {
                                result.add(attribute.getComplexValues().get(index));
                            }
                        }
                    } else {
                        if (attribute.isSimpleValue()) {
                            result.addAll(attribute.getSimpleValues());
                        } else {
                            result.addAll(attribute.getComplexValues());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds the given attribute value.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    public void addAttributeValue(String attributeName, CmsEntity value) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (m_entityAttributes.containsKey(attributeName)) {
            m_entityAttributes.get(attributeName).add(value);
        } else {
            List<CmsEntity> values = new ArrayList<CmsEntity>();
            values.add(value);
            m_entityAttributes.put(attributeName, values);
        }
        registerChangeHandler(value);
        fireChange(ChangeType.add);
    }

    /**
     * Adds the given attribute value.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
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
        fireChange(ChangeType.add);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsEntity> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Clones the given entity keeping all entity ids.<p>
     *
     * @return returns the cloned instance
     */
    public CmsEntity cloneEntity() {

        CmsEntity clone = new CmsEntity(getId(), getTypeName());
        for (CmsEntityAttribute attribute : getAttributes()) {
            if (attribute.isSimpleValue()) {
                List<String> values = attribute.getSimpleValues();
                for (String value : values) {
                    clone.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                List<CmsEntity> values = attribute.getComplexValues();
                for (CmsEntity value : values) {
                    clone.addAttributeValue(attribute.getAttributeName(), value.cloneEntity());
                }
            }
        }
        return clone;
    }

    /**
     * Creates a deep copy of this entity.<p>
     *
     * @param entityId the id of the new entity, if <code>null</code> a generic id will be used
     *
     * @return the entity copy
     */
    public CmsEntity createDeepCopy(String entityId) {

        CmsEntity result = new CmsEntity(entityId, getTypeName());
        for (CmsEntityAttribute attribute : getAttributes()) {
            if (attribute.isSimpleValue()) {
                List<String> values = attribute.getSimpleValues();
                for (String value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                List<CmsEntity> values = attribute.getComplexValues();
                for (CmsEntity value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value.createDeepCopy(null));
                }
            }
        }
        return result;
    }

    /**
     * Ensures that the change event is also fired on child entity change.<p>
     */
    public void ensureChangeHandlers() {

        if (!m_changeHandlerRegistry.isEmpty()) {
            for (HandlerRegistration reg : m_changeHandlerRegistry.values()) {
                reg.removeHandler();
            }
            m_changeHandlerRegistry.clear();
        }
        for (List<CmsEntity> attr : m_entityAttributes.values()) {
            for (CmsEntity child : attr) {
                registerChangeHandler(child);
                child.ensureChangeHandlers();
            }
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean result = false;
        if (obj instanceof CmsEntity) {
            CmsEntity test = (CmsEntity)obj;
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
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        ensureHandlers().fireEventFromSource(event, this);
    }

    /**
     * Returns an attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the attribute value
     */
    public CmsEntityAttribute getAttribute(String attributeName) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            return CmsEntityAttribute.createSimpleAttribute(attributeName, m_simpleAttributes.get(attributeName));
        }
        if (m_entityAttributes.containsKey(attributeName)) {
            return CmsEntityAttribute.createEntityAttribute(attributeName, m_entityAttributes.get(attributeName));
        }
        return null;
    }

    /**
     * Returns all entity attributes.<p>
     *
     * @return the entity attributes
     */
    public List<CmsEntityAttribute> getAttributes() {

        List<CmsEntityAttribute> result = new ArrayList<CmsEntityAttribute>();
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
    public CmsEntity getEntityById(String entityId) {

        CmsEntity result = null;
        if (m_id.equals(entityId)) {
            result = this;
        } else {
            for (List<CmsEntity> children : m_entityAttributes.values()) {
                for (CmsEntity child : children) {
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
     * Returns the entity id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the entity type name.<p>
     *
     * @return the entity type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Returns if the entity has the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return <code>true</code> if the entity has the given attribute
     */
    public boolean hasAttribute(String attributeName) {

        return m_simpleAttributes.containsKey(attributeName) || m_entityAttributes.containsKey(attributeName);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return super.hashCode();
    }

    /**
     * Inserts a new attribute value at the given index.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    public void insertAttributeValue(String attributeName, CmsEntity value, int index) {

        if (m_entityAttributes.containsKey(attributeName)) {
            m_entityAttributes.get(attributeName).add(index, value);
        } else {
            setAttributeValue(attributeName, value);
        }
        registerChangeHandler(value);
        fireChange(ChangeType.add);
    }

    /**
     * Inserts a new attribute value at the given index.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    public void insertAttributeValue(String attributeName, String value, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            m_simpleAttributes.get(attributeName).add(index, value);
        } else {
            setAttributeValue(attributeName, value);
        }
        fireChange(ChangeType.add);
    }

    /**
     * Removes the given attribute.<p>
     *
     * @param attributeName the attribute name
     */
    public void removeAttribute(String attributeName) {

        removeAttributeSilent(attributeName);
        fireChange(ChangeType.remove);
    }

    /**
     * Removes the attribute without triggering any change events.<p>
     *
     * @param attributeName the attribute name
     */
    public void removeAttributeSilent(String attributeName) {

        CmsEntityAttribute attr = getAttribute(attributeName);
        if (attr != null) {
            if (attr.isSimpleValue()) {
                m_simpleAttributes.remove(attributeName);
            } else {
                for (CmsEntity child : attr.getComplexValues()) {
                    removeChildChangeHandler(child);
                }
                m_entityAttributes.remove(attributeName);
            }
        }
    }

    /**
     * Removes a specific attribute value.<p>
     *
     * @param attributeName the attribute name
     * @param index the value index
     */
    public void removeAttributeValue(String attributeName, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            List<String> values = m_simpleAttributes.get(attributeName);
            if ((values.size() == 1) && (index == 0)) {
                removeAttributeSilent(attributeName);
            } else {
                values.remove(index);
            }
        } else if (m_entityAttributes.containsKey(attributeName)) {
            List<CmsEntity> values = m_entityAttributes.get(attributeName);
            if ((values.size() == 1) && (index == 0)) {
                removeAttributeSilent(attributeName);
            } else {
                CmsEntity child = values.remove(index);
                removeChildChangeHandler(child);
            }
        }
        fireChange(ChangeType.remove);
    }

    /**
     * Sets the given attribute value. Will remove all previous attribute values.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    public void setAttributeValue(String attributeName, CmsEntity value) {

        // make sure there is no attribute value set
        removeAttributeSilent(attributeName);
        addAttributeValue(attributeName, value);
    }

    /**
     * Sets the given attribute value at the given index.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    public void setAttributeValue(String attributeName, CmsEntity value, int index) {

        if (m_simpleAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (!m_entityAttributes.containsKey(attributeName)) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            } else {
                addAttributeValue(attributeName, value);
            }
        } else {
            if (m_entityAttributes.get(attributeName).size() > index) {
                CmsEntity child = m_entityAttributes.get(attributeName).remove(index);
                removeChildChangeHandler(child);
            }
            m_entityAttributes.get(attributeName).add(index, value);
            fireChange(ChangeType.change);
        }
    }

    /**
     * Sets the given attribute value. Will remove all previous attribute values.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    public void setAttributeValue(String attributeName, String value) {

        m_entityAttributes.remove(attributeName);
        List<String> values = new ArrayList<String>();
        values.add(value);
        m_simpleAttributes.put(attributeName, values);
        fireChange(ChangeType.change);
    }

    /**
     * Sets the given attribute value at the given index.<p>
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     * @param index the value index
     */
    public void setAttributeValue(String attributeName, String value, int index) {

        if (m_entityAttributes.containsKey(attributeName)) {
            throw new RuntimeException("Attribute already exists with a simple type value.");
        }
        if (!m_simpleAttributes.containsKey(attributeName)) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            } else {
                addAttributeValue(attributeName, value);
            }
        } else {
            if (m_simpleAttributes.get(attributeName).size() > index) {
                m_simpleAttributes.get(attributeName).remove(index);
            }
            m_simpleAttributes.get(attributeName).add(index, value);
            fireChange(ChangeType.change);
        }
    }

    /**
     * Returns the JSON string representation of this entity.<p>
     *
     * @return the JSON string representation of this entity
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
        for (Entry<String, List<CmsEntity>> entityEntry : m_entityAttributes.entrySet()) {
            result.append("\"").append(entityEntry.getKey()).append("\"").append(": [\n");
            boolean firstValue = true;
            for (CmsEntity value : entityEntry.getValue()) {
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

    /**
     * Adds this handler to the widget.
     *
     * @param <H> the type of handler to add
     * @param type the event type
     * @param handler the handler
     * @return {@link HandlerRegistration} used to remove the handler
     */
    protected final <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {

        return ensureHandlers().addHandlerToSource(type, this, handler);
    }

    /**
     * Fires the change event for this entity.<p>
     * 
     * @param type the change type 
     */
    void fireChange(ChangeType type) {

        CmsEntityChangeEvent event = new CmsEntityChangeEvent(this, type);
        fireEvent(event);
    }

    /**
     * Lazy initializing the handler manager.<p>
     *
     * @return the handler manager
     */
    private SimpleEventBus ensureHandlers() {

        if (m_eventBus == null) {
            m_eventBus = new SimpleEventBus();
        }
        return m_eventBus;
    }

    /**
     * Adds the value change handler to the given entity.<p>
     *
     * @param child the child entity
     */
    private void registerChangeHandler(CmsEntity child) {

        HandlerRegistration reg = child.addValueChangeHandler(m_childChangeHandler);
        m_changeHandlerRegistry.put(child.getId(), reg);
    }

    /**
     * Removes the child entity change handler.<p>
     *
     * @param child the child entity
     */
    private void removeChildChangeHandler(CmsEntity child) {

        HandlerRegistration reg = m_changeHandlerRegistry.remove(child.getId());
        if (reg != null) {
            reg.removeHandler();
        }
    }
}
