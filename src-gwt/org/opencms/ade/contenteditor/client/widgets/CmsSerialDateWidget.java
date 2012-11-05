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

import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSerialDate;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides a DHTML calendar widget, for use on a widget dialog.<p>
 * 
 * */
public class CmsSerialDateWidget extends Composite implements I_EditWidget {

    /** Value of the activation. */
    private boolean m_active = true;

    /** The global select box. */
    private CmsSerialDate m_serialDate = new CmsSerialDate();

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsSerialDateWidget(String config) {

        // All composites must call initWidget() in their constructors.
        initWidget(m_serialDate);

        ValueChangeHandler<String> handler = new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> arg0) {

                fireChangeEvent();

            }

        };
        m_serialDate.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_serialDate.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_serialDate.getElement().getStyle().setBorderColor("#7788AA");
        m_serialDate.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_serialDate.addValueChangeHandler(handler);
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

        ValueChangeEvent.fire(this, m_serialDate.getFormValueAsString());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_serialDate.getFormValueAsString();
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

        if (active == m_active) {
            return;
        }
        m_active = active;
        if (m_active) {
            getElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
            getElement().focus();
        } else {
            getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
            m_serialDate.clearFealds();
        }
        if (active) {
            fireChangeEvent();
        }
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        //m_serialDate.setName(name);
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

        m_serialDate.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }
}
