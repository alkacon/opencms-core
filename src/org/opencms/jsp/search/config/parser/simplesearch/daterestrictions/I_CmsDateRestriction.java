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

package org.opencms.jsp.search.config.parser.simplesearch.daterestrictions;

/**
 * Interface for date restrictions used in list configurations.<p>
 */
public interface I_CmsDateRestriction {

    /**
     * Enum representing a direction in time (past / future).
     */
    enum TimeDirection {
        /** Going backward in time. */
        past,

        /** Going forward in time. */
        future
    }

    /**
     * Enum representing a time unit.<p>
     */
    enum TimeUnit {
        /** Days. */
        DAYS,

        /** Weeks. */
        WEEKS,

        /** Months. */
        MONTHS,

        /** Years. */
        YEARS;

        /**
         * Formats a positive integer amount of the given unit for use in Solr range queries.<p>
         *
         * @param count the number
         *
         * @return the formatted value
         */
        public String formatForRange(int count) {

            if (this == WEEKS) {
                return (7 * count) + "DAYS";
            } else {
                return "" + count + toString();
            }
        }
    }

    /**
     * Gets the formatted range expression for this restriction, for use in Solr date range queries.<p>
     *
     * @return the date range expression
     */
    String getRange();

}
