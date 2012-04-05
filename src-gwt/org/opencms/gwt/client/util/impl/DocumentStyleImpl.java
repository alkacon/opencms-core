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

package org.opencms.gwt.client.util.impl;

import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;

/**
 * Helper class to retrieve the computed style of an element.<p>
 * 
 * This implementation is used for all none MSIE browsers.<p>
 * 
 * @since 8.0.0
 */
public class DocumentStyleImpl {

    /**
     * Transforms a CSS property name to its javascript property name (font-size >> fontSize).<p>
     * 
     * @param s the property name
     * 
     * @return the javascript property name
     */
    protected static native String camelize(String s)/*-{
        return s.replace(/\-(\w)/g, function(all, letter) {
            return letter.toUpperCase();
        });
    }-*/;

    /**
     * Removes the opacity attribute from the element's inline-style.<p>
     * 
     * @param element the DOM element to manipulate
     */
    public native void clearOpacity(Element element) /*-{
        element.style.removeProperty("opacity");
    }-*/;

    /**
     * Returns the computed style of the given element.<p>
     * 
     * @param elem the element
     * @param name the name of the CSS property 
     * 
     * @return the currently computed style
     */
    public String getCurrentStyle(Element elem, String name) {

        name = hyphenize(name);
        String propVal = getComputedStyle(elem, name);
        if (CmsDomUtil.Style.opacity.name().equals(name) && ((propVal == null) || (propVal.trim().length() == 0))) {
            propVal = "1";
        }
        return propVal;
    }

    /**
     * Transforms the CSS style name to the name of the javascript style property.<p>
     * 
     * @param name the name of the CSS property
     * 
     * @return the javascript property name
     */
    public String getPropertyName(String name) {

        if ("float".equals(name)) {
            return "cssFloat";
        } else if ("class".equals(name)) {
            return "className";
        } else if ("for".equals(name)) {
            return "htmlFor";
        }
        return camelize(name);
    }

    /**
     * Returns the computed style from the DOM object.<p>
     * 
     * @param elem the element object
     * @param name name of the CSS property
     * 
     * @return the property value
     */
    protected native String getComputedStyle(Element elem, String name) /*-{
        var cStyle = $doc.defaultView.getComputedStyle(elem, null);
        if (cStyle == null) {
            return null;
        }
        var value = cStyle.getPropertyValue(name);
        if (value == "auto" && (name == "width" || name == "height")) {
            var which = name === "width" ? [ "Left", "Right" ] : [ "Top",
                    "Bottom" ];
            function getWH() {
                var val;
                val = name === "width" ? elem.offsetWidth : elem.offsetHeight;
                for ( var i = 0; i < which.length; i++) {
                    val -= parseFloat(getComputedStyle(elem, "padding"
                            + which[i])) || 0;
                    val -= parseFloat(getComputedStyle(elem, "border"
                            + which[i] + "Width")) || 0;

                }
                return Math.max(0, Math.round(val));
            }
            value = getWH() + "px";
        }

        return value;
    }-*/;

    /**
     * Hyphenizes the given string.<p>
     * 
     * @param name the string to hyphenize
     * 
     * @return the result
     */
    protected native String hyphenize(String name) /*-{
        return name.replace(/([A-Z])/g, "-$1").toLowerCase();
    }-*/;
}
