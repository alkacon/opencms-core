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

package org.opencms.gwt.client.ui.input.location;

import org.opencms.gwt.client.ui.CmsPushButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A google maps based location picker widget.<p>
 */
public class CmsLocationPicker extends Composite implements HasValueChangeHandlers<String> {

    /** The location picker controller. */
    CmsLocationController m_controller;

    /** The popup opener button. */
    private CmsPushButton m_openerButton;

    /** The value display. */
    private Label m_valueDisplay;

    /** Constructor. */
    public CmsLocationPicker() {

        FlowPanel main = new FlowPanel();
        m_valueDisplay = new Label();
        main.add(m_valueDisplay);
        m_openerButton = new CmsPushButton();
        m_openerButton.setText("Pick");
        main.add(m_openerButton);
        initWidget(main);
        m_controller = new CmsLocationController(this);

        m_openerButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_controller.openPopup();

            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns the location value.<p>
     * 
     * @return the location value
     */
    public CmsLocationValue getLocationValue() {

        return m_controller.getLocationValue();
    }

    /**
     * Returns the JSON string representation of the value.<p>
     * 
     * @return the JSON string representation
     */
    public String getStringValue() {

        return m_controller.getStringValue();
    }

    /**
     * Sets the picker enabled.<p>
     * 
     * @param enabled <code>true</code> to enable the picker
     */
    public void setEnabled(boolean enabled) {

        m_openerButton.setEnabled(enabled);
    }

    /**
     * Sets the widget value.<p>
     * 
     * @param value the value
     */
    public void setValue(String value) {

        m_controller.setStringValue(value);
    }

    /**
     * Displays the given value.<p>
     * 
     * @param location the location value to display
     */
    protected void displayValue(CmsLocationValue location) {

        m_valueDisplay.setText(location.toJSONString());
    }
}
