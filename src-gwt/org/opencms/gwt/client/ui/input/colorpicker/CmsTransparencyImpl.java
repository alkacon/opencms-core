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

package org.opencms.gwt.client.ui.input.colorpicker;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * A helpful class to set transparencies in browsers GWT supports.<p>
 */
final class CmsTransparencyImpl {

    // TODO: check if this is really necessary. Try Element.getStyle().setOpacity(...) instead.

    /**
     * Hiding constructor.<p>
     */
    private CmsTransparencyImpl() {

        // nothing to do
    }

    /** Caches the original DXImageTransform.Microsoft.AlphaImageLoader settings for IE6. */
    private static Map<Element, String> map = new HashMap<Element, String>();

    /**
     * Get IE version (provided by Microsoft).<p>
     *
     * @return browser version
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
     *
     * @param elem A com.google.gwt.user.client.Element object
     * @param alpha An alpha value
     */
    public static void setTransparency(Element elem, int alpha) {

        float ieVersion = getIEVersion();
        // only IE 8 requires special treatment, older IE versions are no longer supported
        if (ieVersion < 9.0) {
            elem.getStyle().setProperty(
                "-ms-filter",
                "\"progid:DXImageTransform.Microsoft.Alpha(opacity=" + alpha + ")\"");
        } else {
            // Everyone else
            elem.getStyle().setOpacity((1.0 * alpha) / 100);
        }
    }
}