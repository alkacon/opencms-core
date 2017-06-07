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

package org.opencms.gwt.client.util.impl;

import com.google.gwt.dom.client.Element;

/**
 * Helper class to retrieve the computed style of an element.<p>
 *
 * This implementation is used for MSIE 9 browsers.<p>
 *
 * @since 8.0.0
 */
public class DocumentStyleImplIE9 extends DocumentStyleImpl {

    /**
     * Transforms the CSS style name to the name of the javascript style property.<p>
     *
     * @param name the name of the CSS property
     * @return the javascript property name
     */
    @Override
    public String getPropertyName(String name) {

        if ("float".equals(name)) {
            return "styleFloat";
        } else if ("class".equals(name)) {
            return "className";
        } else if ("for".equals(name)) {
            return "htmlFor";
        }
        return name;
    }

    /**
     * @see org.opencms.gwt.client.util.impl.DocumentStyleImpl#getComputedStyle(com.google.gwt.dom.client.Element, java.lang.String)
     */
    @Override
    protected native String getComputedStyle(Element elem, String name) /*-{
                                                                        function getComputed(elem, name) {
                                                                        var style = elem.style;
                                                                        var camelCase = name.replace(/\-(\w)/g, function(all, letter) {
                                                                        return letter.toUpperCase();
                                                                        });
                                                                        var ret = "";
                                                                        if (elem.currentStyle != null) {
                                                                        ret = elem.currentStyle[name] || elem.currentStyle[camelCase];
                                                                        // From the awesome hack by Dean Edwards
                                                                        // http://erik.eae.net/archives/2007/07/27/18.54.15/#comment-102291
                                                                        // If we're not dealing with a regular pixel number
                                                                        // but a number that has a weird ending, we need to convert it to pixels
                                                                        if (!/^\d+(px)?$/i.test(ret) && /^\d/.test(ret)) {
                                                                        // Remember the original values
                                                                        var left = style.left, rsLeft = elem.runtimeStyle.left;
                                                                        // Put in the new values to get a computed value out
                                                                        elem.runtimeStyle.left = elem.currentStyle.left;
                                                                        style.left = ret || 0;
                                                                        ret = style.pixelLeft + "px";
                                                                        // Revert the changed values
                                                                        style.left = left;
                                                                        elem.runtimeStyle.left = rsLeft;
                                                                        }
                                                                        }
                                                                        return ret;
                                                                        }

                                                                        if (name === "width" || name === "height") {

                                                                        var which = name === "width" ? [ "Left", "Right" ] : [ "Top",
                                                                        "Bottom" ];
                                                                        function getWH() {
                                                                        var val;
                                                                        val = name === "width" ? elem.offsetWidth : elem.offsetHeight;
                                                                        for ( var i = 0; i < which.length; i++) {
                                                                        val -= parseFloat(getComputed(elem, "padding" + which[i])) || 0;
                                                                        val -= parseFloat(getComputed(elem, "border" + which[i]
                                                                        + "Width")) || 0;

                                                                        }
                                                                        return Math.max(0, Math.round(val));
                                                                        }
                                                                        return getWH() + "px";
                                                                        }

                                                                        return "" + getComputed(elem, name);
                                                                        }-*/;
}
