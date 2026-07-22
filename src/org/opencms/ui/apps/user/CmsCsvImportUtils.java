/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.user;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsXsltUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

/**
 * Utility methods for CSV user imports.<p>
 */
final class CmsCsvImportUtils {

    /** UTF-8 byte order mark. */
    private static final String BOM = "\ufeff";

    /** Hidden constructor. */
    private CmsCsvImportUtils() {

        // utility class
    }

    /**
     * Normalizes a CSV field name or value by removing an optional UTF-8 BOM and matching surrounding quotes.<p>
     *
     * @param value the raw CSV token
     * @return the normalized token
     */
    static String normalizeToken(String value) {

        if (value == null) {
            return null;
        }
        String result = value;
        if (result.startsWith(BOM)) {
            result = result.substring(BOM.length());
        }
        if ((result.length() >= 2) && result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    /**
     * Parses CSV user import data.<p>
     *
     * @param data the UTF-8 encoded CSV data
     * @param defaultPassword the password to use if the CSV password should not be used
     * @param keepPasswordIfPossible true if a CSV password value should be imported
     *
     * @return the parsed users
     */
    static List<CmsUser> readUsers(byte[] data, String defaultPassword, boolean keepPasswordIfPossible) {

        List<CmsUser> users = new ArrayList<CmsUser>();
        List<String> columns = null;
        String separator = null;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if ((lineNumber == 1) && CmsStringUtil.isEmptyOrWhitespaceOnly(line)) {
                    throw new IllegalArgumentException("The CSV header is empty.");
                }
                if (separator == null) {
                    separator = CmsXsltUtil.getPreferredDelimiter(line);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(separator)) {
                        throw new IllegalArgumentException("Unable to detect the CSV separator.");
                    }
                }
                List<String> lineValues = Splitter.on(separator).splitToList(line);
                if (columns == null) {
                    columns = normalizeHeader(lineValues);
                    validateHeader(columns);
                    continue;
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(line)) {
                    continue;
                }
                users.add(parseUser(columns, lineValues, lineNumber, defaultPassword, keepPasswordIfPossible));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read the CSV user import file.", e);
        }

        if (columns == null) {
            throw new IllegalArgumentException("The CSV file does not contain a header.");
        }
        return users;
    }

    /**
     * Gets a normalized value by column name.<p>
     *
     * @param columns the normalized columns
     * @param rawValues the raw values
     * @param columnName the column name
     *
     * @return the value
     */
    private static String getValue(List<String> columns, List<String> rawValues, String columnName) {

        int index = columns.indexOf(columnName);
        String value = ((index >= 0) && (index < rawValues.size())) ? normalizeToken(rawValues.get(index)) : "";
        return value == null ? "" : value;
    }

    /**
     * Normalizes the CSV header.<p>
     *
     * @param rawHeader the raw header fields
     *
     * @return the normalized header fields
     */
    private static List<String> normalizeHeader(List<String> rawHeader) {

        List<String> result = new ArrayList<String>();
        for (int i = 0; i < rawHeader.size(); i++) {
            String column = normalizeToken(rawHeader.get(i));
            if (column != null) {
                column = column.trim();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(column)) {
                throw new IllegalArgumentException("CSV column " + (i + 1) + " has an empty name.");
            }
            if (result.contains(column)) {
                throw new IllegalArgumentException("Duplicate CSV column '" + column + "'.");
            }
            result.add(column);
        }
        return result;
    }

    /**
     * Parses a single CSV user line.<p>
     *
     * @param columns the normalized columns
     * @param rawValues the raw values
     * @param lineNumber the CSV line number
     * @param defaultPassword the default password
     * @param keepPasswordIfPossible true if CSV passwords should be imported
     *
     * @return the parsed user
     */
    private static CmsUser parseUser(
        List<String> columns,
        List<String> rawValues,
        int lineNumber,
        String defaultPassword,
        boolean keepPasswordIfPossible) {

        CmsUser user = new CmsUser();
        String userName = getValue(columns, rawValues, "name");
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            throw new IllegalArgumentException("CSV line " + lineNumber + " does not contain a user name.");
        }
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String value = i < rawValues.size() ? normalizeToken(rawValues.get(i)) : "";
            if (value == null) {
                value = "";
            }
            if ("password".equals(column)
                && (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || !keepPasswordIfPossible)) {
                value = defaultPassword;
            }
            setUserValue(user, column, value, lineNumber);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(user.getName())) {
            throw new IllegalArgumentException("CSV line " + lineNumber + " does not contain a user name.");
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(user.getPassword())) {
            user.setPassword(defaultPassword);
        }
        return user;
    }

    /**
     * Sets a field value on a user by reflection or stores it as additional information.<p>
     *
     * @param user the user
     * @param column the CSV column
     * @param value the CSV value
     * @param lineNumber the CSV line number
     */
    private static void setUserValue(CmsUser user, String column, String value, int lineNumber) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || "null".equals(value)) {
            return;
        }
        try {
            Method method = CmsUser.class.getMethod(
                "set" + column.substring(0, 1).toUpperCase() + column.substring(1),
                new Class[] {String.class});
            method.invoke(user, new Object[] {value});
        } catch (NoSuchMethodException e) {
            user.setAdditionalInfo(column, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                "CSV line " + lineNumber + ": field '" + column + "' is not accessible.",
                e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalArgumentException(
                "CSV line " + lineNumber + ": invalid value for field '" + column + "'.",
                cause);
        } catch (CmsRuntimeException e) {
            throw new IllegalArgumentException("CSV line " + lineNumber + ": unable to set field '" + column + "'.", e);
        }
    }

    /**
     * Validates the CSV header.<p>
     *
     * @param columns the normalized columns
     */
    private static void validateHeader(List<String> columns) {

        if (!columns.contains("name")) {
            throw new IllegalArgumentException("The mandatory CSV column 'name' is missing.");
        }
    }
}
