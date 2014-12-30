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

package org.opencms.acacia.client;

import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The root attribute handler.<p>
 */
public class CmsRootHandler implements I_CmsAttributeHandler {

    /** The sub handlers. */
    protected List<Map<String, CmsAttributeHandler>> m_handlers;

    /** The attribute handler by id. */
    private Map<String, CmsAttributeHandler> m_handlerById;

    /**
     * Constructor.<p>
     */
    public CmsRootHandler() {

        m_handlers = new ArrayList<Map<String, CmsAttributeHandler>>();
        m_handlers.add(new HashMap<String, CmsAttributeHandler>());
        m_handlerById = new HashMap<String, CmsAttributeHandler>();
    }

    /**
     * Clears the handler hierarchy.
     */
    public void clearHandlers() {

        for (Map<String, CmsAttributeHandler> handlers : m_handlers) {
            for (CmsAttributeHandler handler : handlers.values()) {
                handler.clearHandlers();
            }
            handlers.clear();
        }
        m_handlers.clear();
        m_handlers.add(new HashMap<String, CmsAttributeHandler>());
        m_handlerById.clear();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#collectSimplePath(org.opencms.acacia.client.I_CmsAttributeHandler)
     */
    public String collectSimplePath(I_CmsAttributeHandler childHandler) {

        return "";
    }

    /**
     * Ensures attribute handler maps are available up to the specified index.<p>
     * This is required during inline editing, where only a fragment of the handlers data structure is build and used.<p>
     * 
     * @param index the index of the currently edited attribute 
     */
    public void ensureHandlers(int index) {

        if (m_handlers.size() <= index) {
            // make sure the handler maps are available
            // this may be necessary
            for (int i = m_handlers.size(); i <= index; i++) {
                insertHandlers(i);
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#getAttributeName()
     */
    public String getAttributeName() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#getChildHandler(java.lang.String, int)
     */
    public CmsAttributeHandler getChildHandler(String attributeName, int index) {

        if (m_handlers.size() > index) {
            return m_handlers.get(index).get(attributeName);
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#getChildHandlerBySimpleName(java.lang.String, int)
     */
    public CmsAttributeHandler getChildHandlerBySimpleName(String name, int index) {

        if (m_handlers.size() > index) {
            for (String attributeName : m_handlers.get(index).keySet()) {
                if (attributeName.equals(name) || attributeName.endsWith(name)) {
                    return m_handlers.get(index).get(attributeName);
                }
            }
        }
        return null;
    }

    /**
     * Returns the handler for the given id.<p>
     * 
     * @param entityId the entity id
     * @param attributeName the attribute name
     * 
     * @return the handler
     */
    public CmsAttributeHandler getHandlerById(String entityId, String attributeName) {

        return m_handlerById.get(entityId + "/" + attributeName);
    }

    /**
     * Returns the attribute handler for the given path.<p>
     * 
     * @param pathNames the path names
     * 
     * @return the attribute handler
     */
    public CmsAttributeHandler getHandlerByPath(String[] pathNames) {

        I_CmsAttributeHandler handler = this;
        int index = 0;
        for (int i = 0; i < pathNames.length; i++) {
            String attributeName = pathNames[i];
            int nextIndex = CmsContentDefinition.extractIndex(attributeName);
            attributeName = CmsContentDefinition.removeIndex(attributeName);
            if ((handler instanceof CmsAttributeHandler) && ((CmsAttributeHandler)handler).getAttributeType().isChoice()) {
                // in case of a choice attribute, skip to the next level
                attributeName = CmsType.CHOICE_ATTRIBUTE_NAME;
            }
            handler = handler.getChildHandler(attributeName, index);
            index = nextIndex;
        }
        return (CmsAttributeHandler)handler;
    }

    /**
     * Returns the attribute handler to the given simple path.<p>
     * 
     * @param pathNames the simple path elements
     * 
     * @return the attribute handler
     */
    public CmsAttributeHandler getHandlersBySimplePath(String[] pathNames) {

        I_CmsAttributeHandler handler = this;
        int index = 0;
        for (int i = 0; i < pathNames.length; i++) {
            String attributeName = pathNames[i];
            int nextIndex = CmsContentDefinition.extractIndex(attributeName);
            if (nextIndex > 0) {
                nextIndex--;
            }
            attributeName = CmsContentDefinition.removeIndex(attributeName);
            if ((handler instanceof CmsAttributeHandler) && ((CmsAttributeHandler)handler).getAttributeType().isChoice()) {
                // in case of a choice attribute, skip to the next level
                attributeName = CmsType.CHOICE_ATTRIBUTE_NAME;
            }
            handler = handler.getChildHandlerBySimpleName(attributeName, index);
            index = nextIndex;
        }
        return (CmsAttributeHandler)handler;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#insertHandlers(int)
     */
    public void insertHandlers(int index) {

        if (index <= m_handlers.size()) {
            m_handlers.add(index, new HashMap<String, CmsAttributeHandler>());
        } else {
            throw new IndexOutOfBoundsException("index of " + index + " too big, current size: " + m_handlers.size());
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#removeHandlers(int)
     */
    public void removeHandlers(int index) {

        m_handlers.remove(index);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#setHandler(int, java.lang.String, org.opencms.acacia.client.CmsAttributeHandler)
     */
    public void setHandler(int index, String attributeName, CmsAttributeHandler handler) {

        m_handlers.get(index).put(attributeName, handler);
        handler.setParentHandler(this);
        setHandlerById(attributeName, handler);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsAttributeHandler#setHandlerById(java.lang.String, org.opencms.acacia.client.CmsAttributeHandler)
     */
    public void setHandlerById(String attributeName, CmsAttributeHandler handler) {

        m_handlerById.put(handler.getEntityId() + "/" + attributeName, handler);
    }

    /**
     * Initializes the sub handlers maps for the given value count.<p>
     * 
     * @param count the value count
     */
    protected void initHandlers(int count) {

        if (count == 0) {
            m_handlers.clear();
        } else {
            while (m_handlers.size() < count) {
                m_handlers.add(new HashMap<String, CmsAttributeHandler>());
            }
        }
        m_handlerById.clear();
    }
}
