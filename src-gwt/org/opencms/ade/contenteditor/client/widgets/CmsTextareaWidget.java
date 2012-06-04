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

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.widgets.I_EditWidget;

import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsTextArea;

import java.util.Iterator;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *  
 * */
public class CmsTextareaWidget extends Composite implements I_EditWidget {

    /** The token to control activation*/
    private boolean m_active = true;

    /** The master panel for all added input fields. */
    private Panel m_panel;

    /** The input test area.*/
    private CmsTextArea m_textarea = new CmsTextArea();

    /** The value changed handler initialized flag. */
    private boolean m_valueChangeHandlerInitialized;

    /**
     * Creates a new display widget.<p>
     */
    public CmsTextareaWidget() {

        // Place the check above the text box using a vertical panel.
        m_panel = new CmsScrollPanel();
        m_panel.setHeight("90px");
        // All composites must call initWidget() in their constructors.
        initWidget(m_panel);
        m_panel.add(m_textarea);

    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return null;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        // Initialization code
        if (!m_valueChangeHandlerInitialized) {
            m_valueChangeHandlerInitialized = true;
            addDomHandler(new KeyPressHandler() {

                public void onKeyPress(KeyPressEvent event) {

                    // schedule the change event, so the key press can take effect
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            fireChangeEvent();
                        }
                    });
                }
            }, KeyPressEvent.getType());
            addDomHandler(new ChangeHandler() {

                public void onChange(ChangeEvent event) {

                    fireChangeEvent();

                }
            }, ChangeEvent.getType());
            addDomHandler(new BlurHandler() {

                public void onBlur(BlurEvent event) {

                    fireChangeEvent();
                }
            }, BlurEvent.getType());
        }
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     * 
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_textarea.getFormValueAsString());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_textarea.getFormValueAsString();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_active = active;
        // set all parameters of the panel visible or invisible
        Iterator<Widget> it = m_panel.iterator();
        while (it.hasNext()) {
            CmsTextArea ta = (CmsTextArea)it.next();
            ta.setVisible(active);
            ta.setFormValueAsString("");
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        // set the value and start changeEvent
        setValue(value, true);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        // set the saved value to the textArea
        m_textarea.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }
    }

}
