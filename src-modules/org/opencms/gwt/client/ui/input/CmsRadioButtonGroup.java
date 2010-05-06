/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsRadioButtonGroup.java,v $
 * Date   : $Date: 2010/05/06 13:56:27 $
 * Version: $Revision: 1.8 $
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

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsPair;

import java.util.ArrayList;
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
 * @version $Revision: 1.8 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsRadioButtonGroup extends Composite
implements I_CmsFormWidget, HasValueChangeHandlers<String>, I_CmsHasInit {

    /** The widget type identifier. */
    public static final String WIDGET_TYPE = "radio";

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

        init(items);
    }

    /**
     * Creates a new instance from a map of strings.<p>
     * 
     * The keys of the map are used as the values of the radio buttons, and the values of the map are used as labels 
     * for the radio buttons.
     *  
     * @param items the string map containing the select options 
     */
    public CmsRadioButtonGroup(Map<String, String> items) {

        List<CmsPair<String, String>> pairs = new ArrayList<CmsPair<String, String>>();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            pairs.add(new CmsPair<String, String>(entry.getKey(), entry.getValue()));
        }
        init(pairs);

    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsRadioButtonGroup(widgetParams);
            }
        });
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
    @Override
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String formValue) {

        setFormValue(formValue);
    }

    /**
     * Internal method for initializing the widget with a list of select options.<p>
     * 
     * @param items the list of select options 
     */
    protected void init(List<CmsPair<String, String>> items) {

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
        m_panel.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().radioButtonGroup());
        m_panel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
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
