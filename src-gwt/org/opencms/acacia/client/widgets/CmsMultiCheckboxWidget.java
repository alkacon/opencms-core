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

import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsMultiCheckBox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides a standard HTML form checkbox widget, for use on a widget dialog.<p>
 *
 *
 * @since 8.5.0
 *
 * */
public class CmsMultiCheckboxWidget extends Composite implements I_CmsEditWidget {

    /** Value of the activation. */
    private boolean m_active = true;
    /** The multi check box panel. */
    private CmsMultiCheckBox m_multicheckbox;
    /** The selected checkboxes. */
    private String m_selected;

    /**
     * Constructs an OptionalTextBox with the given caption on the check.<p>
     *
     * @param config configuration string
     *
     */
    public CmsMultiCheckboxWidget(String config) {

        m_multicheckbox = new CmsMultiCheckBox(parseconfig(config));
        setValue(m_selected);
        // All composites must call initWidget() in their constructors.
        initWidget(m_multicheckbox);
        //panel.add(m_multicheckbox);

        List<CmsCheckBox> checkboxes = m_multicheckbox.getCheckboxes();
        Iterator<CmsCheckBox> it = checkboxes.iterator();
        while (it.hasNext()) {
            it.next().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                public void onValueChange(ValueChangeEvent<Boolean> arg0) {

                    fireChangeEvent();

                }

            });
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
     *  Represents a value change event.<p>
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_multicheckbox.getFormValueAsString());
    }

    /**


    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_multicheckbox.getFormValueAsString();
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
        m_multicheckbox.setEnabled(active);
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

        m_selected = value;
        m_multicheckbox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Helper class for parsing the configuration in to a list of Radiobuttons. <p>
     *
     * @param config the configuration string
     * @return Map of option and value of the checkbox
     * */
    private Map<String, String> parseconfig(String config) {

        Map<String, String> result = new HashMap<String, String>();
        m_selected = "";
        String[] labels = config.split("\\|");
        for (int i = 0; i < labels.length; i++) {

            if (labels[i].indexOf("*") >= 0) {
                labels[i] = labels[i].replace("*", "");
                m_selected += labels[i] + "|";
            }
            result.put(labels[i], labels[i]);
        }
        m_selected = m_selected.substring(0, m_selected.lastIndexOf("|"));
        return result;
    }

}
