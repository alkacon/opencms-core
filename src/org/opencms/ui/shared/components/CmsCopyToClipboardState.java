/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.shared.components;

import com.vaadin.shared.ui.button.ButtonState;

/**
 * The copy to clip-board/select text button shared state.<p>
 */
public class CmsCopyToClipboardState extends ButtonState {

    /** The serial version id. */
    private static final long serialVersionUID = 5756156529345274883L;

    /** The element selector. */
    private String m_selector;

    /**
     * Returns the element selector.<p>
     *
     * @return the element selector
     */
    public String getSelector() {

        return m_selector;
    }

    /**
     * Sets the element selector.<p>
     *
     * @param selector the element selector
     */
    public void setSelector(String selector) {

        m_selector = selector;
    }
}
