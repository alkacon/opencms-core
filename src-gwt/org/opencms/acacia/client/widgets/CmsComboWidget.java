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
import org.opencms.gwt.client.ui.input.CmsComboBox;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Map.Entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * A combo box widget.<p>
 *
 * Regarding widget configuration, see <code>{@link org.opencms.acacia.client.widgets.CmsSelectConfigurationParser}</code>.<p>
 */
public class CmsComboWidget extends Composite implements I_CmsEditWidget {

    /** Value of the activation. */
    private boolean m_active = true;

    /** The combo box. */
    private CmsComboBox m_comboBox = new CmsComboBox();

    /** String to control new key press. */
    private String m_oldtext = "";

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     *
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsComboWidget(String config) {

        parseConfiguration(config);

        m_comboBox.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        // add some styles to parts of the combobox.
        m_comboBox.getOpener().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_comboBox.getSelectorPopup().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPopup());
        m_comboBox.getTextBox().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().comboBoxInput());
        m_comboBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent arg0) {

                onkeyupevent();
            }
        });

        m_comboBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });
        initWidget(m_comboBox);
        m_comboBox.getTextBox().addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsComboWidget.this);
            }
        });
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

        ValueChangeEvent.fire(this, m_comboBox.getFormValueAsString());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_comboBox.getFormValueAsString();
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

        if (active == m_active) {
            return;
        }
        m_active = active;
        m_comboBox.setEnabled(active);
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

        //m_selectBox.selectValue(value);
        m_comboBox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     *  Helper function to handle the popup of the combobox. <p>
     */
    protected void onkeyupevent() {

        String newText = m_comboBox.getFormValueAsString();

        if (!newText.equals(m_oldtext)) {
            fireChangeEvent();
            if (!newText.equals("")) {
                m_comboBox.closeSelector();
            } else {
                m_comboBox.openSelector();
            }
        }

        m_oldtext = newText;
    }

    /**
     * Helper function for parsing the configuration of the combo-box.<p>
     *
     * @param config the configuration string.
     * */
    private void parseConfiguration(String config) {

        CmsSelectConfigurationParser parser = new CmsSelectConfigurationParser(config);
        // set the help info first!!
        for (Entry<String, String> helpEntry : parser.getHelpTexts().entrySet()) {
            m_comboBox.setTitle(helpEntry.getKey(), helpEntry.getValue());
        }
        //set value and option to the combo box.
        m_comboBox.setItems(parser.getOptions());
        //if one entrance is declared for default.
        if (parser.getDefaultValue() != null) {
            //set the declared value selected.
            m_comboBox.selectValue(parser.getDefaultValue());
        }
        fireChangeEvent();
    }
}
