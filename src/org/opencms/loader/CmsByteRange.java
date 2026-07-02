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

package org.opencms.loader;

/**
 * Parsed HTTP byte range.<p>
 */
class CmsByteRange {

    /** Range unit for byte ranges. */
    static final String RANGE_UNIT_BYTES = "bytes";

    /** Range unit and delimiter. */
    private static final String RANGE_UNIT_BYTES_PREFIX = RANGE_UNIT_BYTES + "=";

    /** If the range header should be ignored. */
    private boolean m_ignore;

    /** If the range is invalid. */
    private boolean m_invalid;

    /** The range length. */
    long m_length;

    /** The range start. */
    long m_start;

    /**
     * Parses a Range header.<p>
     *
     * @param rangeHeader the range header
     * @param contentLength the content length
     *
     * @return the parsed byte range
     */
    static CmsByteRange parse(String rangeHeader, long contentLength) {

        if ((rangeHeader == null)
            || !rangeHeader.startsWith(RANGE_UNIT_BYTES_PREFIX)
            || (rangeHeader.indexOf(',') >= 0)) {
            return ignored();
        }
        String rangeSpec = rangeHeader.substring(RANGE_UNIT_BYTES_PREFIX.length()).trim();
        int dashIndex = rangeSpec.indexOf('-');
        if ((dashIndex < 0) || (rangeSpec.indexOf('-', dashIndex + 1) >= 0)) {
            return invalid();
        }
        try {
            String startValue = rangeSpec.substring(0, dashIndex).trim();
            String endValue = rangeSpec.substring(dashIndex + 1).trim();
            if (startValue.length() == 0) {
                long suffixLength = Long.parseLong(endValue);
                if (suffixLength <= 0) {
                    return invalid();
                }
                long rangeLength = Math.min(suffixLength, contentLength);
                return valid(contentLength - rangeLength, rangeLength);
            }
            long start = Long.parseLong(startValue);
            long end = (endValue.length() == 0) ? (contentLength - 1) : Long.parseLong(endValue);
            if ((start < 0) || (end < start) || (start >= contentLength)) {
                return invalid();
            }
            end = Math.min(end, contentLength - 1);
            return valid(start, end - start + 1);
        } catch (NumberFormatException e) {
            return invalid();
        }
    }

    /**
     * Creates an ignored range.<p>
     *
     * @return the range
     */
    private static CmsByteRange ignored() {

        CmsByteRange result = new CmsByteRange();
        result.m_ignore = true;
        return result;
    }

    /**
     * Creates an invalid range.<p>
     *
     * @return the range
     */
    private static CmsByteRange invalid() {

        CmsByteRange result = new CmsByteRange();
        result.m_invalid = true;
        return result;
    }

    /**
     * Creates a valid range.<p>
     *
     * @param start the start offset
     * @param length the range length
     *
     * @return the range
     */
    private static CmsByteRange valid(long start, long length) {

        CmsByteRange result = new CmsByteRange();
        result.m_start = start;
        result.m_length = length;
        return result;
    }

    /**
     * Returns if the range header should be ignored.<p>
     *
     * @return if the range header should be ignored
     */
    boolean isIgnored() {

        return m_ignore;
    }

    /**
     * Returns if the range is invalid.<p>
     *
     * @return if the range is invalid
     */
    boolean isInvalid() {

        return m_invalid;
    }
}
