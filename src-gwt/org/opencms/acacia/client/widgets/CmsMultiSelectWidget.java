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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Provides a widget for a standard HTML form for a group of radio buttons.<p>
 *
 * Regarding widget configuration, see <code>{@link org.opencms.acacia.client.widgets.CmsSelectConfigurationParser}</code>.<p>
 *
 * */
public class CmsMultiSelectWidget extends Composite implements I_CmsEditWidget {

    /** Configuration parameter to indicate the multi-select needs to be activated by a check box. */
    public static final String CONFIGURATION_REQUIRES_ACTIVATION = "|requiresactivation";

    /** Default value of rows to be shown. */
    private static final int DEFAULT_ROWS_SHOWN = 10;

    /** The main panel of this widget. */
    FlowPanel m_panel = new FlowPanel();

    /** The scroll panel around the multi-selections. */
    CmsScrollPanel m_scrollPanel;

    /** Activation button.*/
    private CmsCheckBox m_activation;

    /** Value of the activation. */
    private boolean m_active = true;

    /** List of all check boxes button. */
    private List<CmsCheckBox> m_checkboxes;

    /** The default check boxes set in xsd. */
    private List<CmsCheckBox> m_defaultCheckBox;

    /** Value of the requiresactivation. */
    private boolean m_requiresactivation;

    /** The parameter set from configuration.*/
    private int m_rowsToShow = DEFAULT_ROWS_SHOWN;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     * @param config the configuration string.
     */
    public CmsMultiSelectWidget(String config) {

        FlowPanel main = new FlowPanel();
        m_scrollPanel = GWT.create(CmsScrollPanel.class);
        // add separate style to the panel.
        m_scrollPanel.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());

        if (config.contains(CONFIGURATION_REQUIRES_ACTIVATION)) {
            config = config.replace(CONFIGURATION_REQUIRES_ACTIVATION, "");
            m_requiresactivation = true;
        }
        CmsSelectConfigurationParser parser = new CmsSelectConfigurationParser(config);
        // generate a list of all radio button.
        m_defaultCheckBox = new LinkedList<CmsCheckBox>();
        m_checkboxes = new LinkedList<CmsCheckBox>();
        if (m_requiresactivation) {

            buildActivationButton();
            SimplePanel activation = new SimplePanel(m_activation);
            activation.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
            activation.getElement().getStyle().setMarginBottom(5, Unit.PX);
            main.add(activation);
        }
        FocusHandler focusHandler = new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsMultiSelectWidget.this);
            }
        };

        for (Map.Entry<String, String> entry : parser.getOptions().entrySet()) {
            CmsCheckBox checkbox = new CmsCheckBox(entry.getValue());
            checkbox.setInternalValue(entry.getKey());
            if (parser.getDefaultValues().contains(entry.getKey())) {
                m_defaultCheckBox.add(checkbox);
            }
            checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                public void onValueChange(ValueChangeEvent<Boolean> event) {

                    fireChangeEvent();

                }

            });
            // add a separate style each checkbox .
            checkbox.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().checkboxlabel());
            checkbox.getButton().addFocusHandler(focusHandler);
            m_checkboxes.add(checkbox);
            // add the checkbox to the panel.
            m_panel.add(checkbox);

        }
        // All composites must call initWidget() in their constructors.
        m_scrollPanel.add(m_panel);
        m_scrollPanel.setResizable(false);
        int height = (m_rowsToShow * 17);
        if (m_checkboxes.size() < m_rowsToShow) {
            height = (m_checkboxes.size() * 17);
        }
        m_scrollPanel.setDefaultHeight(height);
        m_scrollPanel.setHeight(height + "px");
        main.add(m_scrollPanel);
        initWidget(main);
        if (m_requiresactivation) {
            setAllCheckboxEnabled(false);
        }

    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     *
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, generateValue());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return generateValue();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // TODO implement this in case we want the delete behavior for optional fields
        return false;

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        // check if the value has changed. If there is no change do nothing.
        if (m_active == active) {
            return;
        }
        // set the new value.
        m_active = active;
        // Iterate over all checkboxes.
        for (CmsCheckBox checkbox : m_checkboxes) {
            // set the checkbox active / inactive.
            checkbox.setEnabled(active);
            // if this widget is set inactive.
            if (!active) {
                // deselect all checkboxes.
                checkbox.setChecked(active);
            } else {
                // select the default value if set.
                if (m_defaultCheckBox != null) {
                    Iterator<CmsCheckBox> it = m_defaultCheckBox.iterator();
                    while (it.hasNext()) {
                        it.next().setChecked(active);
                    }
                }
            }
        }
        // fire value change event.
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        String[] values;
        if ((value != null) && (value != "")) {
            if (value.contains(",")) {
                values = value.split(",");
            } else {
                values = new String[] {value};
            }
            for (CmsCheckBox checkbox : m_checkboxes) {
                checkbox.setChecked(false);
                for (int j = 0; j < values.length; j++) {
                    if (checkbox.getInternalValue().equals(values[j])) {
                        checkbox.setChecked(true);
                    }
                }
            }

        }

        // fire change event.
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Sets the checkboxes enabled or disabled.<p>
     *
     * @param value if it should be enabled or disabled
     */
    protected void setAllCheckboxEnabled(boolean value) {

        for (CmsCheckBox checkbox : m_checkboxes) {
            // set the checkbox active / inactive.
            checkbox.setEnabled(value);
        }
    }

    /**
     * Adds a button to activate or deactivate the selection.<p>
     */
    private void buildActivationButton() {

        m_activation = new CmsCheckBox(Messages.get().key(Messages.GUI_MULTISELECT_ACTIVATE_0));

        m_activation.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().checkboxlabel());
        m_activation.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> event) {

                setAllCheckboxEnabled(event.getValue().booleanValue());

            }
        });
    }

    /**
     * Generate a string with all selected checkboxes separated with ','.
     *
     * @return a string with all selected checkboxes
     * */
    private String generateValue() {

        String result = "";
        for (CmsCheckBox checkbox : m_checkboxes) {
            if (checkbox.isChecked()) {
                result += checkbox.getInternalValue() + ",";
            }
        }
        if (result.contains(",")) {
            result = result.substring(0, result.lastIndexOf(","));
        }
        return result;
    }
}
