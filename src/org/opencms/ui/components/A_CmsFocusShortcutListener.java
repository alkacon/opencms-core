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

package org.opencms.ui.components;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.TextField;

/**
 * Shortcut listener that will only be active, while the given text field has the keyboard focus.<p>
 */
public abstract class A_CmsFocusShortcutListener extends ShortcutListener implements BlurListener, FocusListener {

    /** The serial version id. */
    private static final long serialVersionUID = -4768641299003694650L;

    /** The text field. */
    private TextField m_field;

    /**
     * Constructor.<p>
     *
     * @see com.vaadin.event.ShortcutAction#ShortcutAction(String, int, int...)
     */
    public A_CmsFocusShortcutListener(String caption, int keyCode, int[] modifierKeys) {
        super(caption, keyCode, modifierKeys);
    }

    /**
     * @see com.vaadin.event.FieldEvents.BlurListener#blur(com.vaadin.event.FieldEvents.BlurEvent)
     */
    public void blur(BlurEvent event) {

        m_field.removeShortcutListener(this);
    }

    /**
     * @see com.vaadin.event.FieldEvents.FocusListener#focus(com.vaadin.event.FieldEvents.FocusEvent)
     */
    public void focus(FocusEvent event) {

        m_field.addShortcutListener(this);
    }

    /**
     * Install the listener on the given text field
     *
     * @param field the txt field
     */
    public void installOn(TextField field) {

        m_field = field;
        m_field.addFocusListener(this);
        m_field.addBlurListener(this);
    }

}
