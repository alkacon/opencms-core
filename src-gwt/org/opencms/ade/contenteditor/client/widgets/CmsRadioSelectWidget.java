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

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsRadioButton;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

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
public class CmsRadioSelectWidget extends Composite implements I_EditWidget {

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
    CmsScrollPanel m_scrollPanel = GWT.create(CmsScrollPanel.class);

    /** Value of the activation. */
    private boolean m_active = true;

    /** The default radio button set in xsd. */
    private CmsRadioButton m_defaultRadioButton;

    /** Value of the radio group. */
    private CmsRadioButtonGroup m_group;

    /** List of all radio button. */
    private List<CmsRadioButton> m_radioButtons;

    /** The parameter set from configuration.*/
    private int m_rowsToShow = DEFAULT_ROWS_SHOWN;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     * @param config the configuration string.
     */
    public CmsRadioSelectWidget(String config) {

        // generate a list of all radio button.
        m_group = new CmsRadioButtonGroup();
        // move the list to the array of all radio button.
        m_radioButtons = parseconfig(config);
        // add separate style to the panel.
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonPanel());
        FocusHandler focusHandler = new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsRadioSelectWidget.this);
            }
        };
        // iterate about all radio button.
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // add a separate style each radio button.
            radiobutton.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().radioButtonlabel());
            radiobutton.getRadioButton().addFocusHandler(focusHandler);
            // add the radio button to the panel.
            m_panel.add(radiobutton);
        }
        m_scrollPanel.add(m_panel);
        m_scrollPanel.setResizable(false);
        int height = (m_rowsToShow * 17);
        if (m_radioButtons.size() < m_rowsToShow) {
            height = (m_radioButtons.size() * 17);
        }
        m_scrollPanel.setDefaultHeight(height);
        m_scrollPanel.setHeight(height + "px");
        initWidget(m_scrollPanel);
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

        String result = "";
        // check if there is a radio button selected.
        if (m_group.getSelectedButton() != null) {
            // set the name of the selected radio button.
            result = m_group.getSelectedButton().getName();
        }

        ValueChangeEvent.fire(this, result);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_group.getSelectedButton().getName();
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
            // if the value is initial activated set the default value.
            if (active) {
                fireChangeEvent();
            }
            return;
        }
        // set the new value.
        m_active = active;
        // Iterate about all radio button.
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // set the radio button active / inactive.
            radiobutton.setEnabled(active);
            // if this widget is set inactive.
            if (!active) {
                // deselect all radio button.
                radiobutton.setChecked(active);
            } else {
                // select the default value if set.
                if (m_defaultRadioButton != null) {
                    m_defaultRadioButton.setChecked(active);
                    fireChangeEvent();
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

        // iterate about all the radio button.
        for (CmsRadioButton radiobutton : m_radioButtons) {
            // if the value is the name of a radio button active it.
            if (radiobutton.getName().equals(value)) {
                m_group.selectButton(radiobutton);
            }
            // fire change event.
            if (fireEvents) {
                fireChangeEvent();
            }
        }

    }

    /**
     * Helper class for parsing the configuration in to a list of Radiobuttons. <p>
     * 
     * @param config the configuration string.
     * @return List of CmsRadioButtons
     * */
    private List<CmsRadioButton> parseconfig(String config) {

        // generate an empty list off radio button.
        List<CmsRadioButton> result = new LinkedList<CmsRadioButton>();

        //split the configuration in single strings to handle every string single. 
        String[] labels = config.split("\\" + INPUT_DELIMITER);

        int selected = -1;

        //declare some string arrays with the same size of labels.
        String[] value = new String[labels.length];
        String[] options = new String[labels.length];
        String[] help = new String[labels.length];

        //declare a Map to handle the single values of the configuration. 
        HashMap<String, String> values = new LinkedHashMap<String, String>();

        for (int i = 0; i < labels.length; i++) {
            //check if there are one or more parameters set in this substring.
            boolean test_default = (labels[i].indexOf(KEY_DEFAULT) >= 0);
            boolean test_value = labels[i].indexOf(KEY_VALUE) >= 0;
            boolean test_option = labels[i].indexOf(KEY_OPTION) >= 0;
            boolean test_short_option = labels[i].indexOf(KEY_SHORT_OPTION) >= 0;
            boolean test_help = labels[i].indexOf(KEY_HELP) >= 0;
            try {
                //check if there is a default value set.
                if ((labels[i].indexOf(DEFAULT_MARKER) >= 0) || test_default) {
                    //remember the position in the array.
                    selected = i;
                    //remove the declaration parameters.
                    labels[i] = labels[i].replace(DEFAULT_MARKER, KEY_EMPTY);
                    labels[i] = labels[i].replace(KEY_DEFAULT, KEY_EMPTY);
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
                values.put(value[i], options[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        int j = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            // create a new radio button with the given name and label.
            CmsRadioButton radiobutton = new CmsRadioButton(entry.getKey(), entry.getValue());
            // add click handler.
            radiobutton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    fireChangeEvent();
                }
            });
            // add this radio button to the group
            radiobutton.setGroup(m_group);
            // check if this value is default set.
            if (j == selected) {
                radiobutton.setChecked(true);
                m_defaultRadioButton = radiobutton;
            }
            // add this radio button to the list.
            result.add(radiobutton);
            j++;
        }
        return result;
    }
}
