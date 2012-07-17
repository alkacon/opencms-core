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

package org.opencms.gwt.client.ui.input.colorpicker;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * A helpful class to set transparencies in browsers GWT supports.<p>
 */
class CmsTransparencyImpl {

    /** Caches the original DXImageTransform.Microsoft.AlphaImageLoader settings for IE6. */
    private static Map<Element, String> map = new HashMap<Element, String>();

    // Get IE version (provided by Microsoft)
    /**
     * @return rv
     */
    public static native float getIEVersion() /*-{
                                              var rv = -1;
                                              if (navigator.appName == 'Microsoft Internet Explorer') {
                                              var ua = navigator.userAgent;
                                              var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
                                              if (re.exec(ua) != null)
                                              rv = parseFloat(RegExp.$1);
                                              }
                                              return rv;
                                              }-*/;

    /** Given a DOM element, set the transparency value, with 100 being fully opaque and 0 being fully transparent.<p>
     * @param elem A com.google.gwt.user.client.Element object
     * @param alpha An alpha value
     */
    public static void setTransparency(Element elem, int alpha) {

        float ieVersion = getIEVersion();

        if ((ieVersion >= 5.5) && (ieVersion < 7.0)) {
            elem = DOM.getChild(elem, 0);

            // Cache existing filters on the image, then re-apply everything with our Alpha filter
            // stacked on the end.
            if (map.containsKey(elem)) {
                if (alpha == 100) {
                    DOM.setStyleAttribute(elem, "filter", map.get(elem) + "");
                } else {
                    DOM.setStyleAttribute(elem, "filter", map.get(elem)
                        + ", progid:DXImageTransform.Microsoft.Alpha(opacity="
                        + alpha
                        + ");");
                }
            } else {
                map.put(elem, DOM.getStyleAttribute(elem, "filter"));

                if (alpha == 100) {
                    DOM.setStyleAttribute(elem, "filter", map.get(elem) + "");
                } else {
                    DOM.setStyleAttribute(elem, "filter", map.get(elem)
                        + ", progid:DXImageTransform.Microsoft.Alpha(opacity="
                        + alpha
                        + ");");
                }
            }
        }
        // If IE 7 (or better)
        else if (ieVersion >= 7.0) {
            DOM.setStyleAttribute(elem, "filter", "alpha(opacity=" + alpha + ")");
        } else {
            // Everyone else 
            setMozOpacity(elem, String.valueOf(new Integer(alpha).floatValue() / 100));
            DOM.setStyleAttribute(elem, "opacity", String.valueOf(new Integer(alpha).floatValue() / 100));
        }
    }

    /**
     * @param elem
     * @param opacity
     */
    //workaround for to strict debugger....
    private static native void setMozOpacity(Element elem, String opacity) /*-{
                                                                           elem.style["-moz-opacity"] = opacity;
                                                                           }-*/;
}