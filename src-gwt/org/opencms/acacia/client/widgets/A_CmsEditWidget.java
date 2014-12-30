/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * Abstract editing widget class.<p>
 */
public abstract class A_CmsEditWidget extends FocusWidget implements I_CmsEditWidget {

    /** The previous value. */
    private String m_previousValue;

    /**
     * Constructor wrapping a specific DOM element.<p>
     * 
     * @param element the element to wrap
     */
    protected A_CmsEditWidget(Element element) {

        super(element);
        m_previousValue = element.getInnerHTML();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public abstract HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return getElement().getInnerText().trim();
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
    public boolean owns(com.google.gwt.dom.client.Element element) {

        return false;
    }

    /**
     * Fires the value change event, if the value has changed.<p>
     * 
     * @param force <code>true</code> to force firing the event, not regarding an actually changed value 
     */
    protected void fireValueChange(boolean force) {

        String currentValue = getValue();
        if (force || !currentValue.equals(m_previousValue)) {
            m_previousValue = currentValue;
            ValueChangeEvent.fire(this, currentValue);
        }
    }

    /**
     * Returns the previous value.<p>
     *
     * @return the previous value
     */
    protected String getPreviousValue() {

        return m_previousValue;
    }

    /**
     * Sets the previous value.<p>
     *
     * @param previousValue the previous value to set
     */
    protected void setPreviousValue(String previousValue) {

        m_previousValue = previousValue;
    }
}
