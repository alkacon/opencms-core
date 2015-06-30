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
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsType;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * The editor data back-end.<p>
 */
public final class CmsEntityBackend implements I_CmsEntityBackend {

    /** The instance. */
    private static CmsEntityBackend INSTANCE;

    /** CmsEntity id counter. */
    private int m_count;

    /** The registered entities. */
    private Map<String, CmsEntity> m_entities;

    /** The registered types. */
    private Map<String, CmsType> m_types;

    /**
     * Constructor.<p>
     */
    public CmsEntityBackend() {

        m_entities = new HashMap<String, CmsEntity>();
        m_types = new HashMap<String, CmsType>();
    }

    /**
     * Method to create an entity object from a wrapped instance.<p>
     *
     * @param entityWrapper the wrappe entity
     *
     * @return the entity
     */
    public static CmsEntity createFromNativeWrapper(JavaScriptObject entityWrapper) {

        CmsEntity result = new CmsEntity(null, getEntityType(entityWrapper));
        String[] simpleAttr = getSimpleAttributeNames(entityWrapper);
        for (int i = 0; i < simpleAttr.length; i++) {
            String[] simpleAttrValues = getSimpleAttributeValues(entityWrapper, simpleAttr[i]);
            for (int j = 0; j < simpleAttrValues.length; j++) {
                result.addAttributeValue(simpleAttr[i], simpleAttrValues[j]);
            }
        }
        String[] complexAttr = getComplexAttributeNames(entityWrapper);
        for (int i = 0; i < complexAttr.length; i++) {
            JavaScriptObject[] complexAttrValues = getComplexAttributeValues(entityWrapper, complexAttr[i]);
            for (int j = 0; j < complexAttrValues.length; j++) {
                result.addAttributeValue(complexAttr[i], createFromNativeWrapper(complexAttrValues[j]));
            }
        }
        return result;
    }

    /**
     * Returns the instance.<p>
     *
     * @return the instance
     */
    public static CmsEntityBackend getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsEntityBackend();
        }
        return INSTANCE;
    }

    /**
     * Returns the complex attribute names of the given entity.<p>
     *
     * @param entityWrapper the wrapped entity
     *
     * @return the complex attribute names
     */
    private static native String[] getComplexAttributeNames(JavaScriptObject entityWrapper)/*-{
                                                                                           var attr = entityWrapper.getAttributes();
                                                                                           var result = [];
                                                                                           for (i = 0; i < attr.length; i++) {
                                                                                           if (!attr[i].isSimpleValue()) {
                                                                                           result.push(attr[i].getAttributeName());
                                                                                           }
                                                                                           }
                                                                                           return result;
                                                                                           }-*/;

    /**
     * Returns the complex attribute values of the given entity.<p>
     *
     * @param entityWrapper the wrapped entity
     * @param attributeName the attribute name
     *
     * @return the complex attribute values
     */
    private static native JavaScriptObject[] getComplexAttributeValues(
        JavaScriptObject entityWrapper,
        String attributeName)/*-{
                             return entityWrapper.getAttribute(attributeName).getComplexValues();
                             }-*/;

    /**
     * Returns the entity type.<p>
     *
     * @param entityWrapper the wrapped entity
     *
     * @return the entity type name
     */
    private static native String getEntityType(JavaScriptObject entityWrapper)/*-{
                                                                              return entityWrapper.getTypeName();
                                                                              }-*/;

    /**
     * Returns the simple attribute names of the given entity.<p>
     *
     * @param entityWrapper the wrapped entity
     *
     * @return the simple attribute names
     */
    private static native String[] getSimpleAttributeNames(JavaScriptObject entityWrapper)/*-{
                                                                                          var attr = entityWrapper.getAttributes();
                                                                                          var result = [];
                                                                                          for (i = 0; i < attr.length; i++) {
                                                                                          if (attr[i].isSimpleValue()) {
                                                                                          result.push(attr[i].getAttributeName());
                                                                                          }
                                                                                          }
                                                                                          return result;
                                                                                          }-*/;

    /**
     * Returns the simple attribute values of the given entity.<p>
     *
     * @param entityWrapper the wrapped entity
     * @param attributeName the attribute name
     *
     * @return the simple attribute values
     */
    private static native String[] getSimpleAttributeValues(JavaScriptObject entityWrapper, String attributeName)/*-{
                                                                                                                 return entityWrapper.getAttribute(attributeName).getSimpleValues();
                                                                                                                 }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#changeEntityContentValues(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.shared.CmsEntity)
     */
    public void changeEntityContentValues(CmsEntity original, CmsEntity newContent) {

        clearEntityAttributes(original);
        for (CmsEntityAttribute attribute : newContent.getAttributes()) {
            if (attribute.isSimpleValue()) {
                for (String value : attribute.getSimpleValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                for (CmsEntity value : attribute.getComplexValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), registerEntity(value));
                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#clearEntities()
     */
    public void clearEntities() {

        m_entities.clear();
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#createEntity(java.lang.String, java.lang.String)
     */
    public CmsEntity createEntity(String entityId, String entityType) {

        if (entityId == null) {
            entityId = generateId();
        }
        if (!m_types.containsKey(entityType)) {
            throw new IllegalArgumentException("Type " + entityType + " is not registered yet");
        }
        if (m_entities.containsKey(entityId)) {
            throw new IllegalArgumentException("CmsEntity " + entityId + " is already registered");
        }
        CmsEntity entity = new CmsEntity(entityId, entityType);
        m_entities.put(entityId, entity);
        return entity;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#createType(java.lang.String)
     */
    public CmsType createType(String id) {

        if (m_types.containsKey(id)) {
            throw new IllegalArgumentException("Type " + id + " is already registered");
        }
        CmsType type = new CmsType(id);
        m_types.put(id, type);
        return type;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#getAttributeElements(org.opencms.acacia.shared.CmsEntity, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(CmsEntity entity, String attributeName, Element context) {

        return getAttributeElements(entity.getId(), attributeName, context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#getAttributeElements(java.lang.String, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(String entityId, String attributeName, Element context) {

        String selector = "[about='"
            + entityId
            + "'][property*='"
            + attributeName
            + "'], [about='"
            + entityId
            + "'] [property*='"
            + attributeName
            + "']";
        if (context == null) {
            context = Document.get().getDocumentElement();
        }
        return select(selector, context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#getEntity(java.lang.String)
     */
    public CmsEntity getEntity(String entityId) {

        return m_entities.get(entityId);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#getType(java.lang.String)
     */
    public CmsType getType(String id) {

        return m_types.get(id);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#registerEntity(org.opencms.acacia.shared.CmsEntity)
     */
    public CmsEntity registerEntity(CmsEntity entity) {

        if (m_entities.containsKey(entity.getId())) {
            throw new IllegalArgumentException("CmsEntity " + entity.getId() + " is already registered");
        }
        if (!m_types.containsKey(entity.getTypeName())) {
            throw new IllegalArgumentException("Type " + entity.getTypeName() + " is not registered yet");
        }
        for (CmsEntityAttribute attr : entity.getAttributes()) {
            if (attr.isComplexValue()) {
                for (CmsEntity child : attr.getComplexValues()) {
                    registerEntity(child);
                }
            }
        }
        entity.ensureChangeHandlers();
        m_entities.put(entity.getId(), entity);
        return entity;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#registerEntity(org.opencms.acacia.shared.CmsEntity, boolean)
     */
    public CmsEntity registerEntity(CmsEntity entity, boolean discardIds) {

        if (!discardIds) {
            return registerEntity(entity);
        } else {
            if (!m_types.containsKey(entity.getTypeName())) {
                throw new IllegalArgumentException("Type " + entity.getTypeName() + " is not registered yet");
            }
            CmsEntity result = createEntity(null, entity.getTypeName());
            for (CmsEntityAttribute attr : entity.getAttributes()) {
                if (attr.isComplexValue()) {
                    for (CmsEntity child : attr.getComplexValues()) {
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
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#registerTypes(org.opencms.acacia.shared.CmsType, java.util.Map)
     */
    public void registerTypes(CmsType type, Map<String, CmsType> types) {

        if (!m_types.containsKey(type.getId())) {
            for (String attrName : type.getAttributeNames()) {
                String subTypeId = type.getAttributeTypeName(attrName);
                CmsType subType = types.get(subTypeId);
                if (subType == null) {
                    throw new IllegalArgumentException("Type information for " + subTypeId + " is missing");
                }
                registerTypes(subType, types);
            }
            m_types.put(type.getId(), type);
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_CmsEntityBackend#removeEntity(java.lang.String)
     */
    public void removeEntity(String entityId) {

        if (m_entities.containsKey(entityId)) {
            CmsEntity entity = m_entities.get(entityId);
            for (CmsEntityAttribute attr : entity.getAttributes()) {
                if (attr.isComplexValue()) {
                    for (CmsEntity child : attr.getComplexValues()) {
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
    private void clearEntityAttributes(CmsEntity entity) {

        for (CmsEntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isComplexValue()) {
                for (CmsEntity child : attribute.getComplexValues()) {
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
