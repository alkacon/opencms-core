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

import org.opencms.gwt.client.ui.input.CmsLinkSelector;

import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides an OpenCms Principal selection widget, for use on a widget dialog.<p>
 * 
 * */
public class CmsPrincipalWidget extends Composite implements I_EditWidget {

    /** Configuration parameter to set the flags of the principals to display, optional. */
    public static final String CONFIGURATION_FLAGS = "flags";

    /** Value of the activation. */
    private boolean m_active = true;

    /** The global select box. */
    private CmsLinkSelector m_principalPanel = new CmsLinkSelector();

    /**
     * Constructs an CmsComboWidget with the in XSD schema declared configuration.<p>
     * @param config The configuration string given from OpenCms XSD.
     */
    public CmsPrincipalWidget(String config) {

        // All composites must call initWidget() in their constructors.
        initWidget(m_principalPanel);
        /*m_principalPanel.setBasePath("system/workplace/commons/principal_selection.jsp");
        m_principalPanel.setPathAttributes("&type=principalwidget&flags=null");
        m_principalPanel.setPopupHeight(305);
        m_principalPanel.setPopupWidth(500);
        m_principalPanel.setPopupActiveByFocus(true);
        m_principalPanel.setAutoHide(false);
        m_principalPanel.setModal(false);
        m_principalPanel.getTextBox().setReadOnly(false);
        m_principalPanel.getCheckBox().removeFromParent();*/
        m_principalPanel.getTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> arg0) {

                fireChangeEvent();

            }

        });

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

        ValueChangeEvent.fire(this, m_principalPanel.getFormValueAsString());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_principalPanel.getFormValueAsString();
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
        m_principalPanel.setEnabled(active);
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

        setValue(value, true);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        m_principalPanel.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }
}
