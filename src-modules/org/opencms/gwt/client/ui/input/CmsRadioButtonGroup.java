/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsRadioButtonGroup.java,v $
 * Date   : $Date: 2010/03/10 12:51:58 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.util.CmsPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Widget class consisting of a group of radio buttons, of which at most one may be active.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsRadioButtonGroup extends Composite implements I_CmsFormWidget, HasValueChangeHandlers<String> {

    /** CSS bundle for this widget. */
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** A collection of event handlers for this widget. */
    HandlerManager m_handlers = new HandlerManager(null);

    /** The error display used by this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing all other components of this widget. */
    private Panel m_panel = new FlowPanel();

    /** A map which stores all radio buttons using their value as keys. */
    private Map<String, CmsRadioButton> m_radioButtons;

    /** The value of the selected radio button, or null. */
    private String m_selected;

    /** The table containing the radio buttons and labels. */
    private FlexTable m_table = new FlexTable();

    /**
     * Creates a new instance from a list of key/value pairs.<p>
     * 
     * The first component of each pair is the value of the radio buttons, the second component is used as the label.
     * 
     * @param items a list of pairs of strings 
     */
    public CmsRadioButtonGroup(List<CmsPair<String, String>> items) {

        m_panel.add(m_table);
        m_panel.add(m_error);
        m_radioButtons = new HashMap<String, CmsRadioButton>();
        int i = 0;

        for (CmsPair<String, String> pair : items) {
            final CmsRadioButton button = new CmsRadioButton(pair.getFirst());
            m_radioButtons.put(pair.getFirst(), button);
            button.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent e) {

                    changeSelectedItem(button.getName());
                }
            });
            m_table.setWidget(i, 0, button);
            m_table.setText(i, 1, pair.getSecond());
            i += 1;
        }
        initWidget(m_panel);
        m_panel.setStyleName(CSS.radioButtonGroup());
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        m_handlers.addHandler(ValueChangeEvent.getType(), handler);
        return new HandlerRegistration() {

            public void removeHandler() {

                m_handlers.removeHandler(ValueChangeEvent.getType(), handler);
            }
        };
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        m_handlers.fireEvent(event);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_selected == null) {
            return "";
        } else {
            return m_selected;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        if (m_selected != null) {
            CmsRadioButton button = m_radioButtons.get(m_selected);
            button.setDown(false);
            m_selected = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        for (Map.Entry<String, CmsRadioButton> entry : m_radioButtons.entrySet()) {
            entry.getValue().setEnabled(enabled);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValue(java.lang.Object)
     */
    public void setFormValue(Object value) {

        if (value instanceof String) {
            String strValue = (String)value;
            if (strValue.equals("")) {
                // interpret empty string as "no radio button selected"
                reset();
            } else {
                changeSelectedItem(strValue);
            }
        }
    }

    /**
     * Changes the selected radio button to the one with a given key.<p>
     * 
     * @param key the key of the radio button
     */
    void changeSelectedItem(String key) {

        if (m_selected != null) {
            CmsRadioButton selectedButton = m_radioButtons.get(m_selected);
            selectedButton.setDown(false);
        }
        m_selected = key;
        CmsRadioButton button = m_radioButtons.get(m_selected);
        if (button != null) {
            button.setDown(true);
        }
        fireValueChangedEvent(getSelected());
    }

    /**
     * Fires a ValueChangedEvent on this widget.<p>
     *  
     * @param newValue the new value of this widget
     */
    void fireValueChangedEvent(String newValue) {

        ValueChangeEvent.fire(this, newValue);

    }

    /**
     * Returns the selected value, or null if no value is selected.<p>
     * 
     * @return the selected value or null
     */
    String getSelected() {

        return m_selected;
    }
}
