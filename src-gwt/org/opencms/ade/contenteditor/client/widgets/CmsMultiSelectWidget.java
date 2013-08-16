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

import org.opencms.ade.contenteditor.client.Messages;
import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsPair;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
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
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description 
 * about the configuration String syntax for the select options.<p>
 *
 * The multi select widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the value of the option
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> for pre-selecting a specific value 
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getOption()}</code> for the display name of the option
 * </ul>
 * <p>
 * 
 * */
public class CmsMultiSelectWidget extends Composite implements I_EditWidget {

    /** Configuration parameter to indicate the multi-select needs to be activated by a check box. */
    public static final String CONFIGURATION_REQUIRES_ACTIVATION = "|requiresactivation";

    /** Optional shortcut default marker. */
    private static final String DEFAULT_MARKER = "*";

    /** Default value of rows to be shown. */
    private static final int DEFAULT_ROWS_SHOWN = 10;

    /** Delimiter between option sets. */
    private static final String INPUT_DELIMITER = "|";

    /** Key prefix for the 'default'. */
    private static final String KEY_DEFAULT = "default='true'";

    /** Empty String to replaces unnecessary keys. */
    private static final String KEY_EMPTY = "";

    /** Key prefix for the 'help' text. */
    private static final String KEY_HELP = "help='";

    /** Key prefix for the 'rows' text. */
    private static final String KEY_LENGTH = "rows=";

    /** Key prefix for the 'option' text. */
    private static final String KEY_OPTION = "option='";

    /** Short key prefix for the 'option' text. */
    private static final String KEY_SHORT_OPTION = ":";

    /** Key suffix for the 'default' , 'help', 'option' text with following entrances.*/
    private static final String KEY_SUFFIX = "' ";

    /** Key suffix for the 'default' , 'help', 'option' text without following entrances.*/
    private static final String KEY_SUFFIX_SHORT = "'";

    /** Key prefix for the 'value'. */
    private static final String KEY_VALUE = "value='";

    /** The main panel of this widget. */
    FlowPanel m_panel = new FlowPanel();

    /** The scroll panel around the multiselections. */
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
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
        // generate a list of all radio button.
        Map<String, CmsPair<String, Boolean>> list = parse(config);
        m_defaultCheckBox = new LinkedList<CmsCheckBox>();
        m_checkboxes = new LinkedList<CmsCheckBox>();
        if (m_requiresactivation) {

            buildActivationButton();
            SimplePanel activation = new SimplePanel(m_activation);
            activation.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
            activation.getElement().getStyle().setMarginBottom(5, Unit.PX);
            main.add(activation);
        }
        FocusHandler focusHandler = new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsMultiSelectWidget.this);
            }
        };

        for (Map.Entry<String, CmsPair<String, Boolean>> entry : list.entrySet()) {
            CmsCheckBox checkbox = new CmsCheckBox(entry.getKey());
            checkbox.setInternalValue(entry.getValue().getFirst());
            if ((entry.getValue().getSecond()).booleanValue()) {
                m_defaultCheckBox.add(checkbox);
            }
            checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                public void onValueChange(ValueChangeEvent<Boolean> event) {

                    fireChangeEvent();

                }

            });
            // add a separate style each checkbox .
            checkbox.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().checkboxlabel());
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
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
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
     * Helper class for parsing the configuration in to a list for the combobox. <p>
     * @param config the configuration string given from xsd
     * @return the pares values from the configuration in a Map
     * */
    Map<String, CmsPair<String, Boolean>> parse(String config) {

        // key = option
        // Pair = 1. label
        // Pair = 2. selected
        if (config.contains(CONFIGURATION_REQUIRES_ACTIVATION)) {
            config = config.replace(CONFIGURATION_REQUIRES_ACTIVATION, "");
            m_requiresactivation = true;
        }
        Map<String, CmsPair<String, Boolean>> result = new LinkedHashMap<String, CmsPair<String, Boolean>>();
        CmsPair<String, Boolean> pair = new CmsPair<String, Boolean>();
        //split the configuration in single strings to handle every string single. 
        String[] labels = config.split("\\" + INPUT_DELIMITER);

        boolean selected = false;

        //declare some string arrays with the same size of labels.
        String[] value = new String[labels.length];
        String[] options = new String[labels.length];
        String[] help = new String[labels.length];

        for (int i = 0; i < labels.length; i++) {
            //check if there are one or more parameters set in this substring.
            boolean parameter = false;
            boolean test_default = (labels[i].indexOf(KEY_DEFAULT) >= 0);
            boolean test_value = labels[i].indexOf(KEY_VALUE) >= 0;
            boolean test_option = labels[i].indexOf(KEY_OPTION) >= 0;
            boolean test_short_option = labels[i].indexOf(KEY_SHORT_OPTION) >= 0;
            boolean test_help = labels[i].indexOf(KEY_HELP) >= 0;
            boolean test_length = labels[i].indexOf(KEY_LENGTH) >= 0;
            try {

                if (test_length) {
                    String sub = KEY_EMPTY;
                    sub = labels[i].substring(labels[i].indexOf(KEY_LENGTH), labels[i].length());
                    try {
                        m_rowsToShow = Integer.parseInt(sub.replace(KEY_LENGTH, KEY_EMPTY));
                    } catch (Exception e) {
                        m_rowsToShow = 5;
                    }
                    labels[i] = labels[i].replace(KEY_LENGTH + m_rowsToShow, KEY_EMPTY);
                    parameter = true;
                }
                selected = false;
                //check if there is a default value set.
                if ((labels[i].indexOf(DEFAULT_MARKER) >= 0) || test_default) {
                    //remove the declaration parameters.
                    labels[i] = labels[i].replace(DEFAULT_MARKER, KEY_EMPTY);
                    labels[i] = labels[i].replace(KEY_DEFAULT, KEY_EMPTY);
                    //remember the value of the selected.                    
                    selected = true;
                }
                //check for values (e.g.:"value='XvalueX' ") set in configuration.
                if (test_value) {
                    String sub = KEY_EMPTY;
                    //check there are more parameters set, separated with space.
                    if (labels[i].indexOf(KEY_SUFFIX) >= 0) {
                        //create substring e.g.:"value='XvalueX".
                        sub = labels[i].substring(labels[i].indexOf(KEY_VALUE), labels[i].indexOf(KEY_SUFFIX));
                    }
                    //if there are no more parameters set.
                    else {
                        //create substring e.g.:"value='XvalueX".
                        sub = labels[i].substring(labels[i].indexOf(KEY_VALUE), labels[i].length() - 1);
                    }
                    //transfer the extracted value to the value array.
                    value[i] = sub.replace(KEY_VALUE, KEY_EMPTY);
                    //remove the parameter within the value.
                    labels[i] = labels[i].replace(KEY_VALUE + value[i] + KEY_SUFFIX_SHORT, KEY_EMPTY);
                }
                //no value parameter is set.
                else {
                    //check there are more parameters set.
                    if (test_short_option) {
                        //transfer the separated value to the value array. 
                        value[i] = labels[i].substring(0, labels[i].indexOf(KEY_SHORT_OPTION));
                    }
                    //no parameters set.
                    else {
                        //transfer the value set in configuration untreated to the value array.
                        value[i] = labels[i];
                    }
                }
                //check for options(e.g.:"option='XvalueX' ") set in configuration.
                if (test_option) {
                    String sub = KEY_EMPTY;
                    //check there are more parameters set, separated with space.
                    if (labels[i].indexOf(KEY_SUFFIX) >= 0) {
                        //create substring e.g.:"option='XvalueX".
                        sub = labels[i].substring(labels[i].indexOf(KEY_OPTION), labels[i].indexOf(KEY_SUFFIX));
                    }
                    //if there are no more parameters set.
                    else {
                        //create substring e.g.:"option='XvalueX".
                        sub = labels[i].substring(
                            labels[i].indexOf(KEY_OPTION),
                            labels[i].lastIndexOf(KEY_SUFFIX_SHORT));
                    }
                    //transfer the extracted value to the option array.
                    options[i] = sub.replace(KEY_OPTION, KEY_EMPTY);
                    //remove the parameter within the value.
                    labels[i] = labels[i].replace(KEY_OPTION + options[i] + KEY_SUFFIX_SHORT, KEY_EMPTY);
                }
                //check if there is a short form (e.g.:":XvalueX") of the option set in configuration.
                else if (test_short_option) {
                    //transfer the extracted value to the option array.
                    options[i] = labels[i].substring(labels[i].indexOf(KEY_SHORT_OPTION) + 1);
                }
                //there are no options set in configuration. 
                else {
                    //option value is the same like the name value so the name value is transfered to the option array.
                    options[i] = value[i];
                }
                //check for help set in configuration.
                if (test_help) {
                    String sub = KEY_EMPTY;
                    //check there are more parameters set, separated with space.
                    if (labels[i].indexOf(KEY_SUFFIX) >= 0) {
                        sub = labels[i].substring(labels[i].indexOf(KEY_HELP), labels[i].indexOf(KEY_SUFFIX));
                    }
                    //if there are no more parameters set.
                    else {
                        sub = labels[i].substring(labels[i].indexOf(KEY_HELP), labels[i].indexOf(KEY_SUFFIX_SHORT));
                    }
                    //transfer the extracted value to the help array.
                    help[i] = sub.replace(KEY_HELP, KEY_EMPTY);
                    //remove the parameter within the value.
                    labels[i] = labels[i].replace(KEY_HELP + help[i] + KEY_SUFFIX_SHORT, KEY_EMPTY);

                }

                //copy value and option to the Map.
                if (!parameter) {
                    pair = new CmsPair<String, Boolean>(value[i], Boolean.valueOf(selected));
                    result.put(options[i], pair);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Adds a button to activate or deactivate the selection.<p>
     */
    private void buildActivationButton() {

        m_activation = new CmsCheckBox(Messages.get().key(Messages.GUI_MULTISELECT_ACTIVATE_0));

        m_activation.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().checkboxlabel());
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
