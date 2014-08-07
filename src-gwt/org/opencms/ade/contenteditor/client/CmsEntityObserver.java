/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client;

import org.opencms.acacia.client.entity.Entity;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDebugLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Observer for content entities, used to notify listeners of entity changes.<p>
 */
public class CmsEntityObserver implements ValueChangeHandler<Entity> {

    /** The registered change listeners. */
    Map<String, List<I_CmsEntityChangeListener>> m_changeListeners;

    /** The scope values. */
    Map<String, String> m_scopeValues;

    /** The change handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /** The observed entity. */
    private Entity m_observerdEntity;

    /**
     * Constructor.<p>
     * 
     * @param entity the entity to observe
     */
    public CmsEntityObserver(Entity entity) {

        m_observerdEntity = entity;
        m_handlerRegistration = entity.addValueChangeHandler(this);
        m_changeListeners = new HashMap<String, List<I_CmsEntityChangeListener>>();
        m_scopeValues = new HashMap<String, String>();
    }

    /**
     * Adds an entity change listener for the given scope.<p>
     * 
     * @param changeListener the change listener
     * @param changeScope the change scope
     */
    public void addEntityChangeListener(I_CmsEntityChangeListener changeListener, String changeScope) {

        if (m_observerdEntity == null) {
            throw new RuntimeException("The Observer has been cleared, no listener registration possible.");
        }
        if (!m_changeListeners.containsKey(changeScope)) {
            m_changeListeners.put(changeScope, new ArrayList<I_CmsEntityChangeListener>());
            // save the current change scope value
            m_scopeValues.put(changeScope, CmsContentDefinition.getValueForPath(m_observerdEntity, changeScope));
        }
        m_changeListeners.get(changeScope).add(changeListener);
    }

    /**
     * Removes this observer from the entities change handler registration and clears registered listeners.<p>
     */
    public void clear() {

        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
            m_handlerRegistration = null;
        }
        m_changeListeners.clear();
        m_scopeValues.clear();
        m_observerdEntity = null;
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<Entity> event) {

        Entity entity = event.getValue();
        for (String scope : m_scopeValues.keySet()) {
            String scopeValue = CmsContentDefinition.getValueForPath(entity, scope);
            String previousValue = m_scopeValues.get(scope);
            if (((scopeValue != null) && !scopeValue.equals(previousValue))
                || ((scopeValue == null) && (previousValue != null))) {
                // the value within this scope has changed, notify all listeners
                if (m_changeListeners.containsKey(scope)) {
                    for (I_CmsEntityChangeListener changeListener : m_changeListeners.get(scope)) {
                        try {
                            changeListener.onEntityChange(entity);
                        } catch (Exception e) {
                            String stack = CmsClientStringUtil.getStackTrace(e, "<br />");
                            CmsDebugLog.getInstance().printLine("<br />" + e.getMessage() + "<br />" + stack);
                        }
                    }
                }

                m_scopeValues.put(scope, scopeValue);
            }
        }
    }
}
