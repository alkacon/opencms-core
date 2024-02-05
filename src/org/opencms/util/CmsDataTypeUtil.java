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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * Utilities to handle basic data types.<p>
 *
 * @since 6.5.6
 */
public final class CmsDataTypeUtil {

    /**
     * Hides the public constructor.<p>
     */
    private CmsDataTypeUtil() {

        // noop
    }

    /**
     * Returns the deserialized (if needed) object.<p>
     *
     * @param data the data to deserialize
     * @param type the data type
     *
     * @return the deserialized object
     *
     * @throws IOException if the inputstream fails
     * @throws ClassNotFoundException if the serialized object fails
     */
    public static Object dataDeserialize(byte[] data, String type) throws IOException, ClassNotFoundException {

        // check the type of the stored data
        Class<?> clazz = Class.forName(type);

        if (isParseable(clazz)) {
            // this is parseable data
            return parse(new String(data), clazz);
        }

        // this is a serialized object
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return oin.readObject();
    }

    /**
     * Returns a ready to export string representation of the given object.<p>
     *
     * For not parseable objects, base64 encoded string with the serialized object is generated.<p>
     *
     * @param data the object to export
     *
     * @return the string representation
     *
     * @throws IOException  if something goes wrong
     */
    public static String dataExport(Object data) throws IOException {

        if (CmsDataTypeUtil.isParseable(data.getClass())) {
            return CmsDataTypeUtil.format(data);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(data);
        oout.close();
        return new String(Base64.encodeBase64(bout.toByteArray()));
    }

    /**
     * Returns the import data object.<p>
     *
     * @param value the exported value
     * @param type the expected data type
     *
     * @return the import data object
     *
     * @throws ClassNotFoundException if something goes wrong
     * @throws IOException if something goes wrong
     */
    public static Object dataImport(String value, String type) throws ClassNotFoundException, IOException {

        Class<?> clazz = Class.forName(type);
        if (CmsDataTypeUtil.isParseable(clazz)) {
            return CmsDataTypeUtil.parse(value, clazz);
        }
        byte[] data = Base64.decodeBase64(value.getBytes());
        return dataDeserialize(data, type);
    }

    /**
     * Serialize the given data.<p>
     *
     * @param data the data to serialize
     *
     * @return byte[] the serailized data
     *
     * @throws IOException if something goes wrong
     */
    public static byte[] dataSerialize(Object data) throws IOException {

        if (isParseable(data.getClass())) {
            return format(data).getBytes();
        }

        // serialize the data
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        Object obj = data;
        if (data instanceof Map) {
            Hashtable<Object, Object> ht = new Hashtable<Object, Object>();
            @SuppressWarnings("unchecked")
            Iterator<Entry<Object, Object>> it = ((Map<Object, Object>)data).entrySet().iterator();
            while (it.hasNext()) {
                Entry<Object, Object> entry = it.next();
                if ((entry.getKey() != null) && (entry.getValue() != null)) {
                    ht.put(entry.getKey(), entry.getValue());
                }
            }
            obj = ht;
        }
        oout.writeObject(obj);
        oout.close();
        return bout.toByteArray();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(boolean data) {

        return String.valueOf(data);
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(byte data) {

        return Byte.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(char data) {

        return Character.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(Date data) {

        return Long.valueOf(data.getTime()).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(double data) {

        return Double.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(float data) {

        return Float.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(int data) {

        return Integer.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(long data) {

        return Long.valueOf(data).toString();
    }

    /**
     * Formats the given data into a string value depending on the data type.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(Object data) {

        if (data == null) {
            return null;
        }
        Class<?> clazz = data.getClass();
        if (clazz.equals(Date.class)) {
            return format(((Date)data).getTime());
        }
        return data.toString();
    }

    /**
     * Formats the given data into a string value.<p>
     *
     * @param data the data to format
     *
     * @return a string representation of the given data
     */
    public static String format(short data) {

        return Short.valueOf(data).toString();
    }

    /**
     * Checks if the given class is representable as a string.<p>
     *
     * @param clazz the type to test
     *
     * @return if the given class is representable as a string
     */
    public static boolean isParseable(Class<?> clazz) {

        boolean parseable = false;
        parseable = parseable || (clazz.equals(byte.class));
        parseable = parseable || (clazz.equals(Byte.class));
        parseable = parseable || (clazz.equals(short.class));
        parseable = parseable || (clazz.equals(Short.class));
        parseable = parseable || (clazz.equals(int.class));
        parseable = parseable || (clazz.equals(Integer.class));
        parseable = parseable || (clazz.equals(long.class));
        parseable = parseable || (clazz.equals(Long.class));
        parseable = parseable || (clazz.equals(float.class));
        parseable = parseable || (clazz.equals(Float.class));
        parseable = parseable || (clazz.equals(double.class));
        parseable = parseable || (clazz.equals(Double.class));
        parseable = parseable || (clazz.equals(boolean.class));
        parseable = parseable || (clazz.equals(Boolean.class));
        parseable = parseable || (clazz.equals(char.class));
        parseable = parseable || (clazz.equals(Character.class));
        parseable = parseable || (clazz.equals(String.class));
        parseable = parseable || (clazz.equals(Date.class));
        parseable = parseable || (clazz.equals(CmsUUID.class));
        return parseable;
    }

    /**
     * Converts Number to int.<p>
     *
     * @param n the number object
     *
     * @return Number.inValue(), 0 - if the parameter is null
     */
    public static int numberToInt(Number n) {

        return (n == null ? 0 : n.intValue());
    }

    /**
     * Returns an object of the given type (or a wrapper for base types)
     * with the value of the given data.<p>
     *
     * @param data the data to parse
     * @param clazz the data type
     *
     * @return the value of the given data
     */
    public static Object parse(String data, Class<?> clazz) {

        if (data == null) {
            return null;
        }
        if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
            return parseByte(data);
        }
        if (clazz.equals(short.class) || clazz.equals(Short.class)) {
            return parseShort(data);
        }
        if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            return parseLong(data);
        }
        if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            return parseInt(data);
        }
        if (clazz.equals(float.class) || clazz.equals(Float.class)) {
            return parseFloat(data);
        }
        if (clazz.equals(double.class) || clazz.equals(Double.class)) {
            return parseDouble(data);
        }
        if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            return parseBoolean(data);
        }
        if (clazz.equals(char.class) || clazz.equals(Character.class)) {
            return parseChar(data);
        }
        if (clazz.equals(CmsUUID.class)) {
            return parseUUID(data);
        }
        if (clazz.equals(Date.class)) {
            return parseDate(data);
        }
        return data;
    }

    /**
     * Parses the given data as a boolean.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Boolean parseBoolean(String data) {

        return Boolean.valueOf(data);
    }

    /**
     * Parses the given data as a byte.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Byte parseByte(String data) {

        return Byte.valueOf(data);
    }

    /**
     * Parses the given data as a char.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Character parseChar(String data) {

        return Character.valueOf(data.charAt(0));
    }

    /**
     * Parses the given data as a date.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Date parseDate(String data) {

        return new Date(parseLong(data).longValue());
    }

    /**
     * Parses the given data as a double.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Double parseDouble(String data) {

        return Double.valueOf(data);
    }

    /**
     * Parses the given data as a float.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Float parseFloat(String data) {

        return Float.valueOf(data);
    }

    /**
     * Parses the given data as an integer.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Integer parseInt(String data) {

        return Integer.valueOf(data);
    }

    /**
     * Parses the given data as a long.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Long parseLong(String data) {

        return Long.valueOf(data);
    }

    /**
     * Parses the given data as a short.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static Short parseShort(String data) {

        return Short.valueOf(data);
    }

    /**
     * Parses the given data as an uuid.<p>
     *
     * @param data the data to parse
     *
     * @return the converted data value
     */
    public static CmsUUID parseUUID(String data) {

        return new CmsUUID(data);
    }
}
