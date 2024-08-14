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

package org.opencms.acacia.client.widgets;

import org.opencms.gwt.shared.CmsGwtLog;

import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * Helpers / utilities related to the 'typograf' typography library.
 */
public final class CmsTypografUtil {

    /**
     * Native type for the Typograf class provided by the library of the same name.
     */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class Typograf {

        @JsConstructor
        public Typograf(JsPropertyMap<Object> options) { /* must be empty */ }

        public static native boolean hasLocale(String locale);

        public native void disableRule(String rule);

        public native void enableRule(String rule);

        public native String execute(String input);
    }

    /**
     * Hidden default constructor.
     */
    private CmsTypografUtil() {
        // empty
    }

    /**
     * Creates a 'live' typograf instance to be used for typography while typing in an editor widget.
     *
     * @param typografLocale the typograf locale (not the same format as OpenCms locales!)
     * @return the typograf instance for the locale, or null if it couldn't be created
     */
    public static Typograf createLiveInstance(String typografLocale) {

        Typograf typograf = null;
        if (Typograf.hasLocale(typografLocale)) {
            try {
                JsPropertyMap<Object> options = Js.cast(new JsObject());
                options.set("locale", new JsArray<>(typografLocale, "en-US"));
                options.set("live", Boolean.TRUE);
                typograf = new Typograf(options);
                typograf.disableRule("*");
                typograf.enableRule("common/punctuation/quote");
            } catch (Exception e) {
                CmsGwtLog.log(e.getLocalizedMessage());
            }
        }
        return typograf;
    }

}
