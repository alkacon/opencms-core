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
import org.opencms.gwt.client.ui.input.CmsColorPicker;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Provides a DHTML calendar widget, for use on a widget dialog.<p>
 * 
 * */
public class CmsColorpickerWidget extends Composite implements I_EditWidget {

    /** Value of the activation. */
    private boolean m_active = true;

    /** The global select box. */
    private CmsColorPicker m_colorPicker = new CmsColorPicker();

    /**The main panel. */
    private SimplePanel m_panel = new SimplePanel();

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsColorpickerWidget(String config) {

        // All composites must call initWidget() in their constructors.
        m_panel.add(m_colorPicker);
        initWidget(m_panel);
        m_colorPicker.getColorfield().addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().colorPicker());
        m_colorPicker.getTextboxPanel().addStyleName(I_CmsLayoutBundle.INSTANCE.widgetCss().colorPickerValue());
        m_colorPicker.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });
        m_colorPicker.getColorValueBox().addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsColorpickerWidget.this);
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

        if (m_colorPicker.getFormValueAsString() != null) {
            ValueChangeEvent.fire(this, m_colorPicker.getFormValueAsString());
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_colorPicker.getFormValueAsString();
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
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // TODO implement this in case we want the delete behavior for optional fields
        return false;

    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (m_active == active) {
            return;
        }

        m_active = active;
        if (m_active) {
            m_colorPicker.getElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
        } else {
            m_colorPicker.getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
            m_colorPicker.getColorfield().getElement().getStyle().setBackgroundColor("#FFFFFF");
        }
        if (!active) {
            m_colorPicker.getColorValueBox().setText("");
        }
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        m_colorPicker.setName(name);

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

        m_colorPicker.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }
}
