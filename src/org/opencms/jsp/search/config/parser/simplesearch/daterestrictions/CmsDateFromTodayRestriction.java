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
 * Date restrictions for a fixed number of time units going backward or forward from the current time.<p>
 */
public class CmsDateFromTodayRestriction implements I_CmsDateRestriction {

    /** The number of time units. */
    private int m_count;

    /** The time unit to use. */
    private TimeUnit m_unit;

    /** The time direction. */
    private TimeDirection m_direction;

    /**
     * Creates a new instance.<p>
     *
     * @param count the number of time units
     * @param unit the time unit
     * @param direction the time direction
     */
    public CmsDateFromTodayRestriction(int count, TimeUnit unit, TimeDirection direction) {

        m_count = count;
        m_unit = unit;
        m_direction = direction;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction#getRange()
     */
    public String getRange() {

        boolean isFuture = m_direction == TimeDirection.future;
        String sign = isFuture ? "+" : "-";
        String time = "NOW/DAY" + sign + m_unit.formatForRange(m_count);
        if (isFuture) {
            return "[NOW TO " + time + "+1DAYS}";
        } else {
            return "[" + time + " TO NOW}";
        }
    }

}
