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
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
  * An option of a select type widget.<p>
 * 
 * If options are passed from XML content schema definitions as widget configuration options,
 * the following syntax is used for defining the option values:<p>
 * 
 * <code>value='{text}' default='{true|false}' option='{text}' help='{text}|{more option definitions}</code><p>
 * 
 * For example:<p>  
 * 
 * <code>value='value1' default='true' option='option1' help='help1'|value='value2' option='option2' help='help2'</code><p>
 * 
 * The elements <code>default</code>, <code>option</code> and <code>help</code> are all optional, only a 
 * <code>value</code> must be present in the input. 
 * There should be only one <code>default</code> set to <code>true</code>
 * in the input, if more than one is detected, only the first <code>default</code> found is actually used. 
 * If no <code>option</code> is given, the value of <code>option</code> defaults to the value of the given <code>value</code>. 
 * If no <code>help</code> is given, the default is <code>null</code>.<p> 
 * 
 * Shortcut syntax options:<p>
 * 
 * If you don't specify the <code>value</code> key, the value is assumed to start at the first position of an
 * option definition. In this case the value must not be surrounded by the <code>'</code> chars. 
 * Example: <code>value='some value' default='true'</code> can also be written as <code>some value default='true'</code>.<p>
 * 
 * Only if you use the short value definition as described above, a default value can be marked with a <code>*</code>
 * at the end of the value definition.
 * Example: <code>value='some value' default='true'</code> can also be written as <code>some value*</code>.<p>
 * 
 * Only if you use the short value definition as described above, you can also append the <code>option</code>
 * to the <code>value</code> using a <code>:</code>. In this case no <code>'</code> must surround the <code>option</code>.
 * Please keep in mind that in this case the value 
 * itself can not longer contain a <code>:</code> char, since it would then be interpreted as a delimiter.
 * Example: <code>value='some value' option='some option'</code> can also be written as <code>some value:some option</code>.<p>
 * 
 * Any combinations of the above described shortcuts are allowed in the configuration option String.
 * Here are some more examples of valid configuration option Strings:<p>
 * 
 * <code>1*|2|3|4|5|6|7</code><br>
 * <code>1 default='true'|2|3|4|5|6|7</code><br>
 * <code>value='1' default='true'|value='2'|value='3'</code><br>
 * <code>value='1'|2*|value='3'</code><br>
 * <code>1*:option text|2|3|4</code><br>
 * <code>1* option='option text' help='some'|2|3|4</code><p>
 * 
 * Please note: If an entry in the configuration String is malformed, this error is silently ignored (but written 
 * to the log channel of this class at <code>INFO</code>level.<p>
 * */
public class CmsSelectWidget extends Composite implements I_EditWidget {

    /** Optional shortcut default marker. */
    private static final String DEFAULT_MARKER = "*";

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

    /** Value of the activation. */
    private boolean m_active = true;

    /** The last value set through the setValue method. This is not necessarily the current widget value. */
    private String m_externalValue;

    /** The global select box. */
    protected CmsSelectBox m_selectBox = new CmsSelectBox();

    /** THe default value. */
    private String m_defaultValue;

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsSelectWidget(String config) {

        parseconfig(config);

        // Place the check above the box using a vertical panel.
        m_selectBox.addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        m_selectBox.setPopupResize(false);
        // add some styles to parts of the selectbox.
        m_selectBox.getOpener().addStyleName(
            org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_selectBox.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_selectBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });

        // All composites must call initWidget() in their constructors.
        initWidget(m_selectBox);

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

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     * 
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_selectBox.getFormValueAsString());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_selectBox.getFormValueAsString();
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

        // check if value change. If not do nothing.
        if (m_active == active) {
            if (active && !m_selectBox.getFormValueAsString().equals(m_externalValue)) {
                // in case the previously set external value does not match the internal select box value fire change event
                // this is needed in case the external value didn't match any select option, 
                // in this case the select box will display the first available value so fire the change to reflect that
                fireChangeEvent();
            }
            return;
        }
        // set new value.
        m_active = active;
        // set the new value to the selectbox.
        m_selectBox.setEnabled(active);
        // fire change event if necessary.
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

        Map<String, String> items = m_selectBox.getItems();
        if (items.containsKey(value)) {
            m_selectBox.selectValue(value);
            m_externalValue = value;
        } else {
            m_selectBox.selectValue(m_defaultValue);
        }
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Helper class for parsing the configuration in to a list for the combobox. <p>
     * 
     * @param config the configuration string
     * */
    private void parseconfig(String config) {

        //split the configuration in single strings to handle every string single. 
        String[] labels = config.split("\\" + INPUT_DELIMITER);

        int selected = -1;

        //declare some string arrays with the same size of labels.
        String[] value = new String[labels.length];
        String[] options = new String[labels.length];
        String[] help = new String[labels.length];

        //declare a Map to handle the single values of the configuration. 
        HashMap<String, String> values = new HashMap<String, String>();

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
        //set value and option to the combo box.
        m_selectBox.setItems(values);
        // TODO: transfer the help values to the select box.
        //if one entrance is declared for default.
        if (selected >= 0) {
            //set the declared value selected. 
            m_selectBox.selectValue(value[selected]);
            m_defaultValue = value[selected];
        }
        fireChangeEvent();
    }

}
