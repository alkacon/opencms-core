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

package org.opencms.ade.postupload.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Allows using an 'a' element as a GWT panel.
 */
public class CmsPreviewLink extends FlowPanel {

    /**
     * Creates a new instance.
     */
    public CmsPreviewLink() {

        super("a");
    }

    /**
     * Sets the link URL.
     *
     * @param href the link URL
     */
    public void setHref(String href) {

        setAttr("href", href);
    }

    /**
     * Sets the 'target' attribute of the link.
     *
     * @param target the new value for the 'target' attribute
     */
    public void setTarget(String target) {

        setAttr("target", target);
    }

    /**
     * Helper method for setting/clearing an element attribute.
     *
     * <p>If the given value is null, the attribute is cleared, otherwise it is set to that value.
     *
     * @param name the attribute name
     * @param value the attribute value
     */
    private void setAttr(String name, String value) {

        if (value != null) {
            getElement().setAttribute(name, value);
        } else {
            getElement().removeAttribute(name);
        }
    }

}
