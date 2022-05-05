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

package org.opencms.acacia.client.widgets.code;

import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Simple toggle button for the CodeMirror toolbar.
 */
public class CmsCodeMirrorToggleButton extends CmsCodeMirrorToolbarButton implements HasValueChangeHandlers<Boolean> {

    /** The value. */
    private boolean m_value;

    /** Style variable for the toggle status. */
    private CmsStyleVariable m_style;

    /**
     * Creates a new instance.
     *
     * @param icon the icon
     */
    public CmsCodeMirrorToggleButton(FontOpenCms icon) {

        super(icon);
        m_style = new CmsStyleVariable(this);
        updateStyle();
        addClickHandler(event -> {
            m_value = !m_value;
            updateStyle();
            ValueChangeEvent.fire(CmsCodeMirrorToggleButton.this, Boolean.valueOf(m_value));
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {

        return addHandler(handler, ValueChangeEvent.getType());

    }

    /**
     * Returns the toggle state.
     *
     * @return the toggle state
     */
    public boolean getValue() {

        return m_value;
    }

    /**
     *
     * Sets the value.
     *
     * @param value the new value
     * @param fireEvents true if a change event should be fired
     */
    public void setValue(boolean value, boolean fireEvents) {

        m_value = value;
        updateStyle();
        if (fireEvents) {
            ValueChangeEvent.fire(this, Boolean.valueOf(m_value));
        }
    }

    /**
     * Updates the style based on the current toggle state.
     */
    private void updateStyle() {

        m_style.setValue(m_value ? "cmsState-down" : "cmsState-up");
    }

}
