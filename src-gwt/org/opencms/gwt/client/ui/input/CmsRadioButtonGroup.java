/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * This class coordinates multiple radio buttons and makes sure that when a radio button of a group is
 * selected, no other radio button of the same group is selected.<p>
 *
 * @since 8.0.0
 */
public class CmsRadioButtonGroup implements HasValueChangeHandlers<String> {

    /** The event bus. */
    private transient SimpleEventBus m_eventBus;

    /** The currently selected radio button (null if none is selected). */
    private CmsRadioButton m_selectedButton;

    /** The object to which value change events should be fired. */
    private HasValueChangeHandlers<String> m_target;

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Deselects a selected radio button (if one is selected).<p>
     */
    public void deselectButton() {

        if (m_selectedButton != null) {
            if (m_selectedButton.isChecked()) {
                m_selectedButton.setChecked(false);
            }
            m_selectedButton = null;
            ValueChangeEvent.fire(this, null);
        }
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        ensureHandlers().fireEventFromSource(event, this);
    }

    /**
     * Returns the currently selected button, or null if none is selected.<p>
     *
     * @return the selected button or null
     */
    public CmsRadioButton getSelectedButton() {

        return m_selectedButton;
    }

    /**
     * Selects a new button and deselects the previously selected one.<p>
     *
     * @param button the button which should be selected
     */
    public void selectButton(CmsRadioButton button) {

        if (m_selectedButton != button) {
            if (m_selectedButton != null) {
                m_selectedButton.setChecked(false);
            }
            if (!button.isChecked()) {
                button.setChecked(true);
            }
            m_selectedButton = button;
            if (m_target != null) {
                ValueChangeEvent.fire(m_target, button.getName());
            }
            ValueChangeEvent.fire(this, button.getName());
        }
    }

    /**
     * Sets the new value change event target for this button group.<p>
     *
     * @param target the value change event target
     */
    public void setValueChangeTarget(HasValueChangeHandlers<String> target) {

        m_target = target;
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

}
