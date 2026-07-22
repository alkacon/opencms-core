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
 */

package org.opencms.ui.apps.user;

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
}
