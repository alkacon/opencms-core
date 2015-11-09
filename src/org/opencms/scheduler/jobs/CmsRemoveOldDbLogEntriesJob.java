/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.scheduler.jobs;

import org.opencms.db.log.CmsLogFilter;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A scheduled job which removes entries older than a given amount of time from the CMS_LOG table, to improve
 * database performance.<p>
 *
 * This job has a single parameter named 'max-age', whose value consists of a number, followed by one or more spaces and
 * finall a unit which is either 'hours', 'days', or 'weeks', which controls how old values have to be before they are
 * deleted by the job.<p>
 *
 * To delete the CMS_LOG entries, this scheduled job needs to be executed as a user who has the role WORKPLACE_MANAGER.<p>
 */
public class CmsRemoveOldDbLogEntriesJob implements I_CmsScheduledJob {

    /** The default max age. */
    public static final int MAX_AGE_DEFAULT = 24 * 30 * 4;

    /** The key for the max-age parameter. */
    public static final String PARAM_MAX_AGE = "max-age";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoveOldDbLogEntriesJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        String maxAgeStr = parameters.get(PARAM_MAX_AGE);
        long maxAgeHours = parseMaxAge(maxAgeStr);
        if (maxAgeHours > 0) {
            long maxAgeMillis = maxAgeHours * 3600L * 1000L;
            long now = System.currentTimeMillis();
            CmsLogFilter filter = CmsLogFilter.ALL.filterTo(now - maxAgeMillis);
            LOG.info("Removing all entries from CMS_LOG older than " + maxAgeHours + " hours...");
            cms.deleteLogEntries(filter);
        } else {
            LOG.info("Not deleting any log entries because of a problem with the max-age format.");
        }
        return "remove old db log entries (max age : " + maxAgeStr + ")";
    }

    /**
     * Parses the 'max-age' parameter and returns a value in hours.<p>
     *
     * @param maxAgeStr the value of the 'max-age' parameter
     *
     * @return the maximum age in hours
     */
    public int parseMaxAge(String maxAgeStr) {

        if (maxAgeStr == null) {
            showFormatError(maxAgeStr);
            return -1;
        }

        maxAgeStr = maxAgeStr.toLowerCase().trim();
        String[] tokens = maxAgeStr.split(" +");
        if ((tokens.length != 2)) {
            showFormatError(maxAgeStr);
            return -1;
        }
        int number = 0;
        try {
            number = Integer.parseInt(tokens[0]);
        } catch (NumberFormatException e) {
            showFormatError(maxAgeStr);
            return -1;
        }

        String unit = tokens[1];
        if ("d".equals(unit) || unit.startsWith("day")) {
            return 24 * number;
        } else if ("h".equals(unit) || unit.startsWith("hour")) {
            return number;
        } else if ("w".equals(unit) || unit.startsWith("week")) {
            return 7 * 24 * number;
        } else {
            showFormatError(maxAgeStr);
            return -1;
        }
    }

    /**
     * Shows an error with the format of the 'max-age' parameter value.<p>
     *
     * @param paramValue the parameter value
     */
    private void showFormatError(String paramValue) {

        LOG.error("Invalid value for the max-age parameter: '" + paramValue + "'");
    }
}
