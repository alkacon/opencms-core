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

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
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
 * Provides a standard HTML form check box widget, for use on a widget dialog.<p>
 *
 * */
public class CmsCheckboxWidget extends Composite implements I_CmsEditWidget {

    /** The check box of this widget. */
    protected CmsCheckBox m_checkbox = new CmsCheckBox();

    /** The token to control activation. */
    private boolean m_active = true;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     */
    public CmsCheckboxWidget() {

        SimplePanel panel = new SimplePanel();
        // adds the checkbox to the panel.
        panel.add(m_checkbox);

        // Set the check box's caption, and check it by default.
        m_checkbox.setChecked(true);
        m_checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (Boolean.parseBoolean(m_checkbox.getFormValueAsString())) {
                    getParent().getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
                } else {
                    getParent().getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
                }
                fireChangeEvent();

            }

        });
        m_checkbox.getButton().addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsDomUtil.fireFocusEvent(CmsCheckboxWidget.this);
            }
        });
        // All composites must call initWidget() in their constructors.
        initWidget(panel);

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
     *  Represents a value change event.<p>
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_checkbox.getFormValueAsString());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_checkbox.getFormValueAsString();
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

        // fix button bar positioning issue
        Element parent = CmsDomUtil.getAncestor(getElement(), I_CmsLayoutBundle.INSTANCE.form().attributeValue());
        if (parent != null) {
            parent.addClassName(I_CmsLayoutBundle.INSTANCE.form().shallowWidget());
        }
        // control if the value has not change do nothing.
        if (m_active == active) {
            return;
        }
        // set the new value.
        m_active = active;
        // fire change event.
        if (active) {
            fireChangeEvent();
        }
        // activate the checkbox.
        m_checkbox.setEnabled(active);
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

        m_checkbox.setFormValueAsString(value);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        if (Boolean.parseBoolean(value)) {
            getParent().getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
        } else {
            getParent().getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
        }
        m_checkbox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

}
