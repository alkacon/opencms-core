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

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction.TimeDirection;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction.TimeUnit;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentValueLocation;

import java.util.Date;

import org.apache.commons.logging.Log;

/**
 * Parses date restrictions in a list configuration.<p>
 *
 */
public class CmsDateRestrictionParser {

    /** XML node name. */
    public static final String N_RANGE = "Range";

    /** XML node name. */
    public static final String N_PAST_FUTURE = "PastFuture";

    /** XML node name. */
    public static final String N_FROM_TODAY = "FromToday";

    /** XML node name. */
    public static final String N_TO = "To";

    /** XML node name. */
    public static final String N_FROM = "From";

    /** XML node name. */
    public static final String N_DIRECTION = "Direction";

    /** XML node name. */
    public static final String N_UNIT = "Unit";

    /** XML node name. */
    public static final String N_COUNT = "Count";

    /** Logger instance for this class.*/
    public static final Log LOG = CmsLog.getLog(CmsDateRestrictionParser.class);

    /** The CMS context used. */
    public CmsObject m_cms;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsDateRestrictionParser(CmsObject cms) {
        m_cms = cms;
    }

    /**
     * Parses a date restriction.<p>
     *
     * @param dateRestriction the location of the date restriction
     *
     * @return the date restriction
     */
    public I_CmsDateRestriction parse(CmsXmlContentValueLocation dateRestriction) {

        I_CmsDateRestriction result = null;
        result = parseRange(dateRestriction);
        if (result != null) {
            return result;
        }
        result = parseFromToday(dateRestriction);
        if (result != null) {
            return result;
        }
        result = parsePastFuture(dateRestriction);
        return result;
    }

    /**
     * Parses a date.<p>
     *
     * @param dateLoc the location of the date
     * @return the date, or null if it could not be parsed
     */
    private Date parseDate(CmsXmlContentValueLocation dateLoc) {

        if (dateLoc == null) {
            return null;
        }
        String dateStr = dateLoc.getValue().getStringValue(m_cms);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(dateStr)) {
            return null;
        }
        try {
            return new Date(Long.parseLong(dateStr));
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage() + " date = " + dateStr, e);
            return null;
        }
    }

    /**
     * Parses a time direction.<p>
     *
     * @param location the location of the time direction
     *
     * @return the time direction, or null if it could not be parsed
     */
    private I_CmsDateRestriction.TimeDirection parseDirection(CmsXmlContentValueLocation location) {

        try {
            return TimeDirection.valueOf(location.getValue().getStringValue(m_cms));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a date restriction of type 'FromToday'.<p>
     *
     * @param dateRestriction the location of the date restriction
     *
     * @return the date restriction
     */
    private I_CmsDateRestriction parseFromToday(CmsXmlContentValueLocation dateRestriction) {

        CmsXmlContentValueLocation location = dateRestriction.getSubValue(N_FROM_TODAY);
        if (location == null) {
            return null;
        }
        CmsXmlContentValueLocation countLoc = location.getSubValue(N_COUNT);
        CmsXmlContentValueLocation unitLoc = location.getSubValue(N_UNIT);
        CmsXmlContentValueLocation directionLoc = location.getSubValue(N_DIRECTION);

        TimeDirection direction = parseDirection(directionLoc);
        Integer count = parsePositiveNumber(countLoc);
        TimeUnit unit = parseUnit(unitLoc);
        if (count == null) {
            return null;
        }
        return new CmsDateFromTodayRestriction(count.intValue(), unit, direction);
    }

    /**
     * Parses a date restriction of type 'Past/Future'.<p>
     *
     * @param dateRestriction the location of the date restriction
     *
     * @return the date restriction
     */
    private I_CmsDateRestriction parsePastFuture(CmsXmlContentValueLocation dateRestriction) {

        CmsXmlContentValueLocation location = dateRestriction.getSubValue(N_PAST_FUTURE);
        if (location == null) {
            return null;
        }
        CmsXmlContentValueLocation directionLoc = location.getSubValue(N_DIRECTION);
        TimeDirection direction = parseDirection(directionLoc);
        return new CmsDatePastFutureRestriction(direction);
    }

    /**
     * Parses a positive integer.<p>
     *
     * @param loc the location of the positive number
     * @return the number, or null if it could not be parsed
     */
    private Integer parsePositiveNumber(CmsXmlContentValueLocation loc) {

        if (loc == null) {
            return null;
        }
        try {
            Integer result = Integer.valueOf(loc.getValue().getStringValue(m_cms).trim());
            if (result.intValue() < 0) {
                return null;
            } else {
                return result;
            }
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Parses a date restriction of 'range' type.<p>
     *
     * @param dateRestriction the location of the date restriction
     *
     * @return the date restriction or null if it could not be parsed
     */
    private I_CmsDateRestriction parseRange(CmsXmlContentValueLocation dateRestriction) {

        CmsXmlContentValueLocation location = dateRestriction.getSubValue(N_RANGE);
        if (location == null) {
            return null;
        }
        CmsXmlContentValueLocation fromLoc = location.getSubValue(N_FROM);
        CmsXmlContentValueLocation toLoc = location.getSubValue(N_TO);
        Date fromDate = parseDate(fromLoc);
        Date toDate = parseDate(toLoc);
        if ((fromDate != null) || (toDate != null)) {
            return new CmsDateRangeRestriction(fromDate, toDate);
        } else {
            return null;
        }
    }

    /**
     * Parses a time unit.<p>
     *
     * @param location the location containing the time unit
     *
     * @return the time unit
     */
    private TimeUnit parseUnit(CmsXmlContentValueLocation location) {

        if (location == null) {
            return null;
        }
        return TimeUnit.valueOf(location.getValue().getStringValue(m_cms));
    }

}
