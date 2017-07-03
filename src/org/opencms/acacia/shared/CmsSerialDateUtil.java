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

package org.opencms.acacia.shared;

/** Utility methods for the serial date widget. */
public final class CmsSerialDateUtil {

    /**
     * Returns the maximally allowed number of events in a series.
     * @return the maximally allowed number of events in a series.
     */
    public static int getMaxEvents() {

        return 100;
    }

    /**
     * Parses int value and returns the provided default if the value can't be parsed.
     * @param value the int to parse.
     * @param defaultValue the default value.
     * @return the parsed int, or the default value if parsing fails.
     */
    public static int toIntWithDefault(String value, int defaultValue) {

        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (@SuppressWarnings("unused") Exception e) {
            // Do nothing, return default.
        }
        return result;
    }
}
