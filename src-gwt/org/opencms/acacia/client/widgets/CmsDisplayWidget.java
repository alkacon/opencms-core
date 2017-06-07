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
import org.opencms.gwt.client.ui.input.CmsTextBox;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides a display only widget, for use on a widget dialog.<br>
 * If there is no value in the content xml, the value<br>
 * set in the configuration string of the xsd is shown.<p>
 *
 * */
public class CmsDisplayWidget extends Composite implements I_CmsEditWidget {

    /** Value of the activation. */
    private boolean m_active = true;

    /** Default value set in XSD. */
    private String m_default = "";

    /** The disabled textbox to show the value. */
    private CmsTextBox m_textbox = new CmsTextBox();

    /**
     * Creates a new display widget.<p>
     *
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsDisplayWidget(String config) {

        m_default = config;
        // All composites must call initWidget() in their constructors.
        initWidget(m_textbox);

        m_textbox.getTextBoxContainer().addStyleName(
            I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().displayTextBoxPanel());
        m_textbox.getTextBox().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().displayTextBox());
        m_textbox.setReadOnly(true);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
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
        if (m_textbox.getText() != null) {
            if (!m_textbox.getText().equals(m_default)) {
                result = m_textbox.getText();
            }
        }

        ValueChangeEvent.fire(this, result);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_textbox.getText();
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
        m_textbox.setEnabled(active);
        if (!active) {
            m_textbox.setFormValueAsString("");
        }
        m_active = active;
        if (active) {
            fireChangeEvent();
        }
    }

    /**
     * Sets the color for the input box.<p>
     *
     * @param color the color that should be set
     * */
    public void setColor(String color) {

        m_textbox.getTextBox().getElement().getStyle().setColor(color);
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

        // add the saved value to the display field
        if (value.equals("")) {
            m_textbox.setFormValueAsString(m_default);
        } else {
            m_textbox.setFormValueAsString(value);
        }
        if (fireEvents) {
            fireChangeEvent();
        }
    }

}
