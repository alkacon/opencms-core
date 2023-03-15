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

package org.opencms.util;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

import java.util.function.Function;

/**
 * Utility class for JSON-related functions.
 */
public class CmsJsonUtil {

    /**
     * Recursively walks through a JSON structure and returns a copy of it, but transforms primitive
     * values using the given function for the copy.
     *
     * @param obj a JSONObject or JSONArray
     * @param func the function to apply to primitive values
     * @return the copy with the replaced values
     * @throws JSONException if JSON operations fail
     */
    public static Object mapJson(Object obj, Function<Object, Object> func) throws JSONException {

        if (obj instanceof JSONObject) {
            return mapJsonObject((JSONObject)obj, func);
        } else if (obj instanceof JSONArray) {
            return mapJsonArray((JSONArray)obj, func);
        } else {
            return func.apply(obj);
        }
    }

    /**
     * Recursively walks through a JSON object and returns a copy of it, but transforms primitive
     * values using the given function for the copy.
     *
     * @param obj a JSONObject
     * @param func the function to apply to primitive values
     * @return the copy with the replaced values
     * @throws JSONException if JSON operations fail
     */
    public static JSONObject mapJsonObject(JSONObject obj, Function<Object, Object> func) throws JSONException {

        JSONObject result = new JSONObject();
        for (String key : obj.keySet()) {
            Object val = obj.opt(key);
            Object val2 = mapJson(val, func);
            result.put(key, val2);
        }
        return result;
    }

    /**
     * Recursively walks through a JSON array and returns a copy of it, but transforms primitive
     * values using the given function for the copy.
     *
     * @param array a JSON array
     * @param func the function to apply to primitive values
     * @return the copy with the replaced values
     * @throws JSONException if JSON operations fail
     */

    private static JSONArray mapJsonArray(JSONArray array, Function<Object, Object> func) throws JSONException {

        JSONArray result = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            result.put(mapJson(array.get(i), func));
        }
        return result;
    }

}
