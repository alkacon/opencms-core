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

import org.opencms.acacia.client.CmsUndoRedoHandler.UndoRedoState;
import org.opencms.acacia.shared.CmsEntity;

import java.util.Stack;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;

/**
 * Handler for the undo redo function.<p>
 */
public final class CmsUndoRedoHandler implements HasValueChangeHandlers<UndoRedoState> {

    /** The change types. */
    public enum ChangeType {
        /** New value added change. */
        add,

        /** A choice change. */
        choice,

        /** Value removed change. */
        remove,

        /** Value sort change. */
        sort,

        /** A simple value change. */
        value
    }

    /** Representing the undo/redo state. */
    public class UndoRedoState {

        /** Indicating if there are changes to be re done. */
        private boolean m_hasRedo;

        /** Indicating if there are changes to be undone. */
        private boolean m_hasUndo;

        /**
         * Constructor.<p>
         * 
         * @param hasUndo if there are changes to be undone
         * @param hasRedo if there are changes to be re done
         */
        UndoRedoState(boolean hasUndo, boolean hasRedo) {

            m_hasUndo = hasUndo;
            m_hasRedo = hasRedo;
        }

        /**
         * Returns if there are changes to be re done.
         * 
         * @return <code>true</code> if there are changes to be re done.
         */
        public boolean hasRedo() {

            return m_hasRedo;
        }

        /**
         * Returns if there are changes to be undone.
         * 
         * @return <code>true</code> if there are changes to be undone.
         */
        public boolean hasUndo() {

            return m_hasUndo;
        }
    }

    /**
     * A timer to delay the addition of a change.<p> 
     */
    protected class ChangeTimer extends Timer {

        /** The attribute name. */
        private String m_attributeName;

        /** The change type. */
        private ChangeType m_changeType;

        /** The value index. */
        private int m_valueIndex;

        /** The value path. */
        private String m_valuePath;

        /**
         * Constructor.<p>
         * 
         * @param valuePath the entity value path
         * @param attributeName the attribute name
         * @param valueIndex the value index
         * @param changeType the change type
         */
        protected ChangeTimer(String valuePath, String attributeName, int valueIndex, ChangeType changeType) {

            m_valuePath = valuePath;
            m_attributeName = attributeName;
            m_valueIndex = valueIndex;
            m_changeType = changeType;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            internalAddChange(m_valuePath, m_attributeName, m_valueIndex, m_changeType);
        }

        /**
         * Checks whether the timer change properties match the given ones.<p>
         * 
         * @param valuePath the entity value path
         * @param attributeName the attribute name
         * @param valueIndex the value index
         * 
         * @return <code>true</code> if the timer change properties match the given ones
         */
        protected boolean matches(String valuePath, String attributeName, int valueIndex) {

            return m_valuePath.equals(valuePath)
                && m_attributeName.equals(attributeName)
                && (m_valueIndex == valueIndex);
        }
    }

    /**
     * Representing a change stack entry.<p>
     */
    private class Change {

        /** The attribute name. */
        private String m_attributeName;

        /** The entity data. */
        private CmsEntity m_entityData;

        /** The entity id. */
        private String m_entityId;

        /** The change type. */
        private ChangeType m_type;

        /** The value index. */
        private int m_valueIndex;

        /**
         * Constructor.<p>
         * 
         * @param entity the chane entity data
         * @param entityId the entity id
         * @param attributeName the attribute name
         * @param valueIndex the value index
         * @param type the change type
         */
        Change(CmsEntity entity, String entityId, String attributeName, int valueIndex, ChangeType type) {

            m_entityId = entityId;
            m_attributeName = attributeName;
            m_valueIndex = valueIndex;
            m_type = type;
            m_entityData = entity;
        }

        /**
         * Returns the attribute name.<p>
         * 
         * @return the attribute name
         */
        public String getAttributeName() {

            return m_attributeName;
        }

        /**
         * Returns the change entity data.<p>
         * 
         * @return the change entity data
         */
        public CmsEntity getEntityData() {

            return m_entityData;
        }

        /**
         * Returns the change entity id.<p>
         * 
         * @return the entity id
         */
        public String getEntityId() {

            return m_entityId;
        }

        /**
         * The change type.<p>
         * 
         * @return the change type
         */
        public ChangeType getType() {

            return m_type;
        }

        /**
         * Returns the value index.<p>
         * 
         * @return the value index
         */
        public int getValueIndex() {

            return m_valueIndex;
        }
    }

    /** The change timer delay. */
    private static final int CHANGE_TIMER_DELAY = 500;

    /** The static instance. */
    private static CmsUndoRedoHandler INSTANCE;

    /** The ad change timer. */
    private ChangeTimer m_changeTimer;

    /** The current data state. */
    private Change m_current;

    /** The editor instance. */
    private CmsEditorBase m_editor;

    /** The edited entity. */
    private CmsEntity m_entity;

    /** The event bus. */
    private SimpleEventBus m_eventBus;

    /** The redo stack. */
    private Stack<Change> m_redo;

    /** The root attribute handler. */
    private CmsRootHandler m_rootHandler;

    /** The undo stack. */
    private Stack<Change> m_undo;

    /**
     * Constructor.<p>
     */
    private CmsUndoRedoHandler() {

        m_undo = new Stack<Change>();
        m_redo = new Stack<Change>();
    }

    /**
     * Returns the undo redo handler instance.<p>
     * 
     * @return the handler instance
     */
    public static CmsUndoRedoHandler getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsUndoRedoHandler();
        }
        return INSTANCE;
    }

    /**
     * Adds a change to the undo stack.<p>
     * 
     * @param valuePath the entity value path
     * @param attributeName the attribute name
     * @param valueIndex the value index
     * @param changeType the change type
     */
    public void addChange(String valuePath, String attributeName, int valueIndex, ChangeType changeType) {

        if (ChangeType.value.equals(changeType)) {
            if (m_changeTimer != null) {
                if (!m_changeTimer.matches(valuePath, attributeName, valueIndex)) {
                    // only in case the change properties of the timer do not match the current change, 
                    // add the last change and start a new timer
                    m_changeTimer.cancel();
                    m_changeTimer.run();
                    m_changeTimer = new ChangeTimer(valuePath, attributeName, valueIndex, changeType);
                    m_changeTimer.schedule(CHANGE_TIMER_DELAY);
                }
            } else {
                m_changeTimer = new ChangeTimer(valuePath, attributeName, valueIndex, changeType);
                m_changeTimer.schedule(CHANGE_TIMER_DELAY);
            }
        } else {
            if (m_changeTimer != null) {
                m_changeTimer.cancel();
                m_changeTimer.run();
            }
            internalAddChange(valuePath, attributeName, valueIndex, changeType);
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<UndoRedoState> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Clears the undo/redo stacks and all references.<p>
     */
    public void clear() {

        m_undo.clear();
        m_redo.clear();
        m_entity = null;
        m_editor = null;
        m_rootHandler = null;
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        ensureHandlers().fireEventFromSource(event, this);
    }

    /**
     * Indicates if there are changes to be undone.<p>
     * 
     * @return <code>true</code> if there are changes to be undone
     */
    public boolean hasRedo() {

        return !m_redo.isEmpty();
    }

    /**
     * Indicates if there are changes to be undone.<p>
     * 
     * @return <code>true</code> if there are changes to be undone
     */
    public boolean hasUndo() {

        return !m_undo.isEmpty();
    }

    /**
     * Initializes the handler to be used for the given entity.<p>
     * 
     * @param entity the edited entity
     * @param editor the editor instance
     * @param rootHandler the root attribute handler
     */
    public void initialize(CmsEntity entity, CmsEditorBase editor, CmsRootHandler rootHandler) {

        m_undo.clear();
        m_redo.clear();
        m_entity = entity;
        m_editor = editor;
        m_rootHandler = rootHandler;
        m_current = new Change(m_entity.cloneEntity(), null, null, 0, null);
        fireStateChange();
    }

    /**
     * Indicates if the handler has been initialized.<p>
     * 
     * @return <code>true</code> if the handler has been initialized
     */
    public boolean isIntitalized() {

        return m_entity != null;
    }

    /**
     * Re-applies the latest state in the redo stack.<p>
     */
    public void redo() {

        if (!m_redo.isEmpty()) {
            m_undo.push(m_current);
            m_current = m_redo.pop();
            changeEntityContentValues(
                m_current.getEntityData(),
                m_current.getEntityId(),
                m_current.getAttributeName(),
                m_current.getValueIndex(),
                m_current.getType());
            fireStateChange();
        }
    }

    /**
     * Reverts to the latest state in the undo stack.<p>
     */
    public void undo() {

        if (hasUndo()) {
            ChangeType type = m_current.getType();
            String entityId = m_current.getEntityId();
            String attributeName = m_current.getAttributeName();
            int valueIndex = m_current.getValueIndex();
            m_redo.push(m_current);
            m_current = m_undo.pop();
            changeEntityContentValues(m_current.getEntityData(), entityId, attributeName, valueIndex, type);
            fireStateChange();
        }
    }

    /**
     * Adds this handler to the widget.
     * 
     * @param <H> the type of handler to add
     * @param type the event type
     * @param handler the handler
     * @return {@link HandlerRegistration} used to remove the handler
     */
    protected <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {

        return ensureHandlers().addHandlerToSource(type, this, handler);
    }

    /**
     * Internally adds a change to the undo stack.<p>
     * 
     * @param valuePath the entity value path
     * @param attributeName the attribute name
     * @param valueIndex the value index
     * @param changeType the change type
     */
    void internalAddChange(String valuePath, String attributeName, int valueIndex, ChangeType changeType) {

        m_changeTimer = null;
        //TODO: keep the IDs, otherwise redo will not work
        CmsEntity currentData = m_entity.cloneEntity();
        if (!currentData.equals(m_current.getEntityData())) {
            m_undo.push(m_current);
            m_current = new Change(currentData, valuePath, attributeName, valueIndex, changeType);
            m_redo.clear();
            fireStateChange();
        }
    }

    /**
     * Sets the editor to the given state.<p>
     * 
     * @param newContent the state content
     * @param entityId the value path elements
     * @param attributeName the attribute name
     * @param valueIndex the value index
     * @param type the change type
     */
    private void changeEntityContentValues(
        CmsEntity newContent,
        String entityId,
        String attributeName,
        int valueIndex,
        ChangeType type) {

        switch (type) {
            case value:
                CmsAttributeHandler handler = m_rootHandler.getHandlerById(entityId, attributeName);
                CmsEntity entity = newContent.getEntityById(entityId);
                if ((entity != null) && (entity.getAttribute(attributeName) != null)) {
                    String value = entity.getAttribute(attributeName).getSimpleValues().get(valueIndex);
                    if ((handler != null) && handler.hasValueView(valueIndex) && (value != null)) {
                        handler.changeValue(value, valueIndex);
                        break;
                    }
                }
                //$FALL-THROUGH$
            default:
                m_editor.rerenderForm(newContent);
        }
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
     * Fires a value change event to indicate the undo/redo state has changed.<p>
     */
    private void fireStateChange() {

        ValueChangeEvent.fire(this, new UndoRedoState(hasUndo(), hasRedo()));
    }
}
