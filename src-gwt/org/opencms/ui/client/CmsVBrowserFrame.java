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

package org.opencms.ui.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.vaadin.client.ui.VBrowserFrame;

/**
 * Extension of the standard browser frame widget which supports setting the name correctly in Chrome.
 *
 * <p>The difference from the standard implementation is that here the name attribute is set on the iframe
 * element before it is inserted into the DOM, which is necessary in Chrome because setting this attribute
 * in Chrome does not change the corresponding frame window object's 'name' attribute if it already exists.
 */
public class CmsVBrowserFrame extends VBrowserFrame {

    /** The name to set on the iframe element. */
    protected String m_savedName;

    /**
     * @see com.vaadin.client.ui.VBrowserFrame#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {

        super.setName(name);
        m_savedName = name;
    }

    /**
     * Always creates new iframe inside widget. Will replace previous iframe.
     *
     * @return the iframe element
     */
    @Override
    protected IFrameElement createIFrameElement(String src) {

        String name = m_savedName;

        // Remove alt text
        if (altElement != null) {
            getElement().removeChild(altElement);
            altElement = null;
        }

        // Remove old iframe
        if (iframe != null) {
            name = iframe.getAttribute("name");
            getElement().removeChild(iframe);
            iframe = null;
        }

        iframe = Document.get().createIFrameElement();
        iframe.setSrc(src);
        iframe.setFrameBorder(0);
        iframe.setAttribute("width", "100%");
        iframe.setAttribute("height", "100%");

        iframe.setAttribute("allowTransparency", "true");
        if (name != null) {
            iframe.setName(name);
        }

        getElement().appendChild(iframe);
        return iframe;
    }

}
