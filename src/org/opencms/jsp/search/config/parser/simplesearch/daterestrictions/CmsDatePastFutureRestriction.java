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

package org.opencms.jsp.search.config.parser.simplesearch.daterestrictions;

/**
 * A restriction which selects either all entries in the past (from the current time) or all entries in the future.<p>
 */
public class CmsDatePastFutureRestriction implements I_CmsDateRestriction {

    /** The time direction. */
    private TimeDirection m_direction;

    /**
     * Creates a new instance.<p>
     *
     * @param direction the time direction
     */
    public CmsDatePastFutureRestriction(TimeDirection direction) {
        m_direction = direction;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction#getRange()
     */
    public String getRange() {

        switch (m_direction) {
            case future:
                return "[NOW TO *]";
            case past:
                return "[* TO NOW ]";
            default:
                return null;
        }
    }

}
