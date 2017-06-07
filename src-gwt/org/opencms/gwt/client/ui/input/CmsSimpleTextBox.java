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

package org.opencms.gwt.client.ui.input;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A simple text box firing change events whenever a key is pressed and the value has changed.<p>
 */
public class CmsSimpleTextBox extends TextBox {

    /** The previous value. */
    private String m_previousValue;

    /**
     * Constructor.<p>
     */
    public CmsSimpleTextBox() {

        super();
        addDomHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent event) {

                fireChange();
            }
        }, KeyUpEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.ValueBoxBase#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {

        m_previousValue = value;
        super.setValue(value, fireEvents);
    }

    /**
     * Triggered on key press to fire event if the value changed.<p>
     */
    void fireChange() {

        ValueChangeEvent.fireIfNotEqual(this, m_previousValue, getValue());
        m_previousValue = getValue();
    }
}
