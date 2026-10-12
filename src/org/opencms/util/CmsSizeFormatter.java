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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Shared size formatting for tenant sizes, storage usage and storage budgets.<p>
 */
public final class CmsSizeFormatter {

    /**
     * No instances.<p>
     */
    private CmsSizeFormatter() {}

    /**
     * Formats decimal byte units consistently: whole B/KB, at most one decimal place for MB/GB.
     * The input remains unchanged; rounding is for display only.<p>
     *
     * @param bytes the byte count, or a negative value if unavailable
     * @param locale the display locale
     * @return the localized size, or a dash if unavailable
     */
    public static String format(long bytes, Locale locale) {

        if (bytes < 0) {
            return "—";
        }
        long unit;
        String suffix;
        int decimals;
        if (bytes >= 1000L * 1000 * 1000) {
            unit = 1000L * 1000 * 1000;
            suffix = " GB";
            decimals = 1;
        } else if (bytes >= 1000L * 1000) {
            unit = 1000L * 1000;
            suffix = " MB";
            decimals = 1;
        } else if (bytes >= 1000) {
            unit = 1000;
            suffix = " KB";
            decimals = 0;
        } else {
            unit = 1;
            suffix = " B";
            decimals = 0;
        }
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(decimals);
        return format.format(BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(unit), decimals, RoundingMode.HALF_UP))
            + suffix;
    }
}
