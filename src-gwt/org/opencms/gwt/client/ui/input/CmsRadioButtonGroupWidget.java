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

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.util.CmsPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Widget class consisting of a group of radio buttons, of which at most one may be active.<p>
 *
 * This is mostly a 'convenience widget' for creating and handling multiple radio buttons as a single widget.
 * The radio buttons will be layed out vertically. If you need more control about the layout of the radio
 * buttons, use multiple {@link CmsRadioButton} instances and link them with a {@link CmsRadioButtonGroup}.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsRadioButtonGroupWidget extends Composite
implements I_CmsFormWidget, HasValueChangeHandlers<String>, I_CmsHasInit {

    /** The widget type identifier. */
    public static final String WIDGET_TYPE = "radio";

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The error display used by this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The radio button group for this radio button. */
    private CmsRadioButtonGroup m_group = new CmsRadioButtonGroup();

    /** The root panel containing all other components of this widget. */
    private Panel m_panel = new FlowPanel();

    /** A map which stores all radio buttons using their value as keys. */
    private Map<String, CmsRadioButton> m_radioButtons;

    /**
     * Creates a new instance from a list of key/value pairs.<p>
     *
     * The first component of each pair is the value of the radio buttons, the second component is used as the label.
     *
     * @param items a list of pairs of strings
     */
    public CmsRadioButtonGroupWidget(List<CmsPair<String, String>> items) {

        init(CmsPair.pairsToMap(items));
    }

    /**
     * Creates a new instance from a map of strings.<p>
     *
     * The keys of the map are used as the values of the radio buttons, and the values of the map are used as labels
     * for the radio buttons.
     *
     * @param items the string map containing the select options
     */
    public CmsRadioButtonGroupWidget(Map<String, String> items) {

        init(items);

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

                return new CmsRadioButtonGroupWidget(widgetParams);
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        return m_eventBus.addHandlerToSource(ValueChangeEvent.getType(), this, handler);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    @Override
    public void fireEvent(GwtEvent<?> event) {

        m_eventBus.fireEventFromSource(event, this);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
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

        CmsRadioButton button = m_group.getSelectedButton();
        if (button == null) {
            return "";
        } else {
            return button.getName();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        boolean result = true;
        for (Map.Entry<String, CmsRadioButton> entry : m_radioButtons.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_group.deselectButton();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do

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
     * Sets the value of the widget.<p>
     *
     * @param value the new value
     */
    public void setFormValue(Object value) {

        if (value == null) {
            value = "";
        }

        if (value instanceof String) {
            String strValue = (String)value;
            if (strValue.equals("")) {
                // interpret empty string as "no radio button selected"
                reset();
            } else {
                CmsRadioButton button = m_radioButtons.get(value);
                m_group.selectButton(button);
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
    protected void init(Map<String, String> items) {

        initWidget(m_panel);
        m_eventBus = new SimpleEventBus();
        m_radioButtons = new HashMap<String, CmsRadioButton>();
        for (Map.Entry<String, String> entry : items.entrySet()) {

            final CmsRadioButton button = new CmsRadioButton(entry.getKey(), entry.getValue());
            button.setGroup(m_group);
            m_radioButtons.put(entry.getKey(), button);
            FlowPanel wrapper = new FlowPanel();
            wrapper.add(button);
            m_panel.add(wrapper);
        }
        m_panel.add(m_error);
        m_panel.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().radioButtonGroup());
        m_panel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
        m_group.setValueChangeTarget(this);
    }

    /**
     * Fires a ValueChangedEvent on this widget.<p>
     *
     * @param newValue the new value of this widget
     */
    void fireValueChangedEvent(String newValue) {

        ValueChangeEvent.fire(this, newValue);
    }

}
