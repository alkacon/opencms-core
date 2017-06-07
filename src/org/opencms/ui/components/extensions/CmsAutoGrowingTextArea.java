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

package org.opencms.ui.components.extensions;

import org.opencms.ui.shared.components.CmsAutoGrowingTextAreaState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.TextArea;

/**
 * Extension to add a CSS class to any component depending on it's scroll position.<p>
 */
public class CmsAutoGrowingTextArea extends AbstractExtension {

    /** The serial version id. */
    private static final long serialVersionUID = 8321661587169935234L;

    /**
     * Constructor.<p>
     *
     * @param textArea the text area to extend
     * @param maxRows the maximal number of rows (<1 for unlimited)
     */
    public CmsAutoGrowingTextArea(TextArea textArea, int maxRows) {
        super.extend(textArea);
        getState().setMaxRows(maxRows);
        getState().setMinRows(textArea.getRows());
    }

    /**
     * Adds the text area auto grow extension to the given component.
     *
     * @param textArea the text area to extend
     * @param maxRows the maximal number of rows (<1 for unlimited)
     */
    @SuppressWarnings("unused")
    public static void addTo(TextArea textArea, int maxRows) {

        new CmsAutoGrowingTextArea(textArea, maxRows);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsAutoGrowingTextAreaState getState() {

        return (CmsAutoGrowingTextAreaState)super.getState();
    }

}
