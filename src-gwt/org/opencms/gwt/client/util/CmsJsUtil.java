/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A class containing generic Javascript utility methods.<p>
 */
public final class CmsJsUtil {

    /**
     * Prevent instantiation.<p>
     */
    private CmsJsUtil() {

        // do nothing
    }

    /**
     * Calls a named global function (i.e. a function-valued attribute of window) with a string argument.<p>
     *
     * @param name the function name
     * @param param the function parameter
     */
    public static native void callNamedFunctionWithString(String name, String param) /*-{
        var w = $wnd;
        w[name](param);
    }-*/;

    /**
     * Calls a named global function (i.e. a function-valued attribute of window) with a string argument.<p>
     *
     * @param name the function name
     * @param param1 a function parameter
     * @param param2 a function parameter
     */
    public static native void callNamedFunctionWithString2(String name, String param1, String param2) /*-{
        var w = $wnd;
        w[name](param1, param2);
    }-*/;

    /**
     * Calls a JS function with a string parameter.<p>
     *
     * @param func the Javascript function
     * @param param the string parameter
     */
    public static native void callWithString(JavaScriptObject func, String param) /*-{
        func(param);
    }-*/;

    /**
     * Closes the browser window.
     */
    public static native void closeWindow() /*-{
        $wnd.close();
    }-*/;

    /**
     * Reads an attribute from a Javascript object.<p>
     *
     * @param jso the Javascript object
     * @param attr the name of the attribute
     * @return the value of the attribute
     */
    public static native JavaScriptObject getAttribute(JavaScriptObject jso, String attr) /*-{
        return jso[attr];
    }-*/;

    /**
     * Reads a string-valued attribute from a Javascript object.<p>
     *
     * @param jso the Javascript object
     * @param attr the name of the attribute
     * @return the value of the attribute
     */
    public static native String getAttributeString(JavaScriptObject jso, String attr) /*-{
        return jso[attr];
    }-*/;

    /**
     * Gets the current window as a Javascript object.<p>
     *
     * @return the current window
     */
    public static native JavaScriptObject getWindow() /*-{
        var result = $wnd;
        return result;
    }-*/;

    /**
     * Sets an attribute of the given Javascript object to a new value.<p>
     *
     * @param jso the object to modify
     * @param attr the attribute to set
     * @param newValue the new attribute value
     */
    public static native void setAttribute(JavaScriptObject jso, String attr, JavaScriptObject newValue) /*-{
        jso[attr] = newValue;
    }-*/;

    /**
     * Wraps a native JavaScript callback taking a string argument into an AsyncCallback so that it can easily be called from GWT Java code.<p>
     *
     * @param func the function to wrap
     * @return the AsyncCallback wrapper
     */
    public static AsyncCallback<String> wrapCallback(final JavaScriptObject func) {

        return new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {

                // TODO Auto-generated method stub

            }

            public void onSuccess(String result) {

                callWithString(func, result);
            }

        };
    }

}
