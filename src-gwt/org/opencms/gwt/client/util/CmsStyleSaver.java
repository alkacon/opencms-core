/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.util;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * This is a utility class for saving and restoring CSS style properties of a DOM element.<p>
 * 
 * @since 8.0.0
 */
public class CmsStyleSaver {

    /** The element for which the CSS style properties are saved. */
    private Element m_element;

    /** The values of the saved style properties. */
    private Map<String, String> m_propertyValues = new HashMap<String, String>();

    /** 
     * Creates a new instance for an element and saves some of its style properties.<p>
     * 
     * @param elem the DOM element 
     * @param properties the style properties to save 
     */
    public CmsStyleSaver(Element elem, String... properties) {

        m_element = elem;
        Style style = elem.getStyle();
        for (String prop : properties) {
            String val = style.getProperty(prop);
            m_propertyValues.put(prop, val);
        }
    }

    /**
     * Restores all saved style properties on this instance's DOM element.<p>
     */
    public void restore() {

        Style style = m_element.getStyle();
        for (Map.Entry<String, String> entry : m_propertyValues.entrySet()) {
            style.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
