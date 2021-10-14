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

package org.opencms.gwt.client.util;

import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT wrapper for  programmatic media query checking.
 *
 *<p>An instance of this class can be created from the media query rule text usign the parse() method.
 * Checking if the media query applies can be either done using the matches() method, or by adding a listener
 * with the addListener method if you need to keep track of changes.
 */
public final class CmsMediaQuery extends JavaScriptObject {

    /**
     * Hidden constructor.
     */
    protected CmsMediaQuery() {}

    /**
     * Creates a new instance using the given media query rule.
     *
     * @param text the rule text
     * @return the new media query
     */
    public static native CmsMediaQuery parse(String text) /*-{
        return $wnd.matchMedia(text);
    }-*/;

    /**
     * Adds a listener to detect when the matching state of the media query changes.
     *
     * @param callback the callback to call with the matching state as a parameter
     */
    public final native void addListener(Consumer<Boolean> callback) /*-{
        var jsCallback = function(state) {
            var match;
            if (state.matches) {
                match = @java.lang.Boolean::TRUE;
            } else {
                match = @java.lang.Boolean::FALSE;
            }
            callback.@java.util.function.Consumer::accept(Ljava/lang/Object;)(match);
        };

        try {
            this.addEventListener('change', jsCallback);
        } catch (e) {
            // for older browsers which don't have the addEventListener method
            this.addListener(jsCallback);
        }
    }-*/;

    /**
     * Checks if the media query matches.
     *
     * @return true if the media query matches
     */
    public final native boolean matches() /*-{
        return this.matches;
    }-*/;

}
