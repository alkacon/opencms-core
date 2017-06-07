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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class to create native JSON maps.<p>
 *
 * @since 8.0.0
 */
public final class CmsJSONMap extends JavaScriptObject {

    /**
     * Not directly instantiable. All subclasses must also define a protected, empty, no-arg constructor.<p>
     */
    protected CmsJSONMap() {

        // empty
    }

    /**
     * Creates a native javascript object to be used as a JSON map.<p>
     *
     * @return the JSON map
     */
    public static native CmsJSONMap createJSONMap() /*-{
                                                    return {};
                                                    }-*/;

    /**
     * Returns if the given key is present in the map.<p>
     *
     * @param key the key
     *
     * @return <code>true</code> if the map contains the key
     */
    public native boolean containsKey(String key) /*-{
                                                  for (var _key in this){
                                                  if (_key==key){
                                                  return true;
                                                  }
                                                  }
                                                  return false;
                                                  }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native boolean getBoolean(String key) /*-{
                                                 return this[key];
                                                 }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native boolean[] getBooleanArray(String key) /*-{
                                                        return this[key];
                                                        }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native double getDouble(String key) /*-{
                                               return this[key];
                                               }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native double[] getDoubleArray(String key) /*-{
                                                      return this[key];
                                                      }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native int getInt(String key) /*-{
                                         return this[key];
                                         }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native int[] getIntArray(String key) /*-{
                                                return this[key];
                                                }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native JavaScriptObject getJavaScriptObject(String key) /*-{
                                                                   return this[key];
                                                                   }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native JavaScriptObject[] getJavaScriptObjectArray(String key) /*-{
                                                                          return this[key];
                                                                          }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native String getString(String key) /*-{
                                               return this[key];
                                               }-*/;

    /**
     * Returns the value to the given key.<p>
     *
     * @param key the key
     *
     * @return the value
     */
    public native String[] getStringArray(String key) /*-{
                                                      return this[key];
                                                      }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, boolean value) /*-{
                                                      this[key]=value;
                                                      }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, boolean[] value) /*-{
                                                        this[key]=value;
                                                        }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, double value) /*-{
                                                     this[key]=value;
                                                     }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, double[] value) /*-{
                                                       this[key]=value;
                                                       }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, int value) /*-{
                                                  this[key]=value;
                                                  }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, int[] value) /*-{
                                                    this[key]=value;
                                                    }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, JavaScriptObject value) /*-{
                                                               this[key]=value;
                                                               }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, JavaScriptObject[] value) /*-{
                                                                 this[key]=value;
                                                                 }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, String value) /*-{
                                                     this[key]=value;
                                                     }-*/;

    /**
     * Puts the value into the map.<p>
     *
     * @param key the key
     * @param value the value
     */
    public native void put(String key, String[] value) /*-{
                                                       this[key]=value;
                                                       }-*/;
}
