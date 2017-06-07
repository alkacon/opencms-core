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

import org.opencms.ui.shared.components.CmsCopyToClipboardState;

import com.vaadin.ui.Button;

/**
 * The copy to clip-board/select text button.<p>
 */
public class CmsCopyToClipboardButton extends Button {

    /** The serial version id. */
    private static final long serialVersionUID = -5962296601420539691L;

    /**
     * Hiding the constructor.<p>
     */
    public CmsCopyToClipboardButton() {
        super();
    }

    /**
     * Constructor.<p>
     *
     * @param text the button text
     * @param selector the selector string to the element to select
     */
    public CmsCopyToClipboardButton(String text, String selector) {
        super(text);
        getState().setSelector(selector);
    }

    /**
     * Sets the element selector.<p>
     *
     * @param selector the element selector
     */
    public void setSelector(String selector) {

        getState().setSelector(selector);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsCopyToClipboardState getState() {

        return (CmsCopyToClipboardState)super.getState();
    }
}
