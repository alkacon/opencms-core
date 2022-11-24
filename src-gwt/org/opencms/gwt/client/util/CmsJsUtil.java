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

import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Command;
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
     * Creates a Command object which calls the given native JS function.
     *
     * @param func a Javascript function
     * @return a Command object which calls the native function
     */
    public static Command convertCallbackToCommand(final JavaScriptObject func) {

        return new Command() {

            public void execute() {

                callWithString(func, null);
            }
        };

    }

    /**
     * Iterates over attributes of a Javascript object and copies them to a string map.
     *
     * <p>Converts all values to strings.
     *
     *
     * @param jso the Javascript object
     * @param map the map to fill
     */
    public static native void fillStringMapFromJsObject(JavaScriptObject jso, Map<String, String> map) /*-{
        var k;
        for (k in jso) {
            var v = jso[k];
            map.@java.util.Map::put(Ljava/lang/Object;Ljava/lang/Object;)(k, String(v));
        }
    }-*/;

    /**
     * Opens the given URI in the current browser window, ensuring that a request to the server is triggered.
     *
     * @param uri the URI to open
     */
    public static native void forceLoadUri(String uri) /*-{
        try {
            var target = new URL(uri, $wnd.location.href);
            var source = $wnd.location;
            if (target.hostname === source.hostname
                    && target.port === source.port
                    && target.pathname === source.pathname
                    && target.search === source.search) {
                $wnd.location.hash = target.hash;
                $wnd.location.reload();
            } else {
                $wnd.location.href = uri;
            }
        } catch (e) {
            $wnd.location.href = uri;
        }
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

    public static native String getLocalStorage(String key) /*-{
        return $wnd.localStorage[key];
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
     * Creates an empty Javascript string array.
     *
     * @return the new array
     */
    public static native JsArrayString newArray() /*-{
        return [];
    }-*/;

    /**
     * Creates a JavaScript object from a JSON string.
     *
     * @param json the JSON string
     * @return the JavaScript object parsed from the JSON string
     */
    public static native JavaScriptObject parseJSON(String json) /*-{
        return JSON.parse(json);
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

    public static native JavaScriptObject toJavaScriptObject(Object o) /*-{
        return o;
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
