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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Restriction to a date between a fixed start and end date, but only one of them has to be given.<p>
 */
public class CmsDateRangeRestriction implements I_CmsDateRestriction {

    /** A constant for the Solr date format. */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /** The start date. */
    private Date m_from;

    /** The end date. */
    private Date m_to;

    /**
     * Creates a new instance.<p>
     *
     * @param fromDate the start date
     * @param toDate the end date
     */
    public CmsDateRangeRestriction(Date fromDate, Date toDate) {

        m_from = fromDate;
        m_to = toDate;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction#getRange()
     */
    public String getRange() {

        return "[" + formatDate(m_from) + " TO " + formatDate(m_to) + "]";
    }

    /**
     * Formats the date for use in Solr range queries.<p>
     *
     * If null is passed as the date, "*" will be returned.
     *
     * @param date the date to format
     * @return the formatted date
     */
    private String formatDate(Date date) {

        if (date == null) {
            return "*";
        }
        return DATE_FORMAT.format(date);
    }

}
