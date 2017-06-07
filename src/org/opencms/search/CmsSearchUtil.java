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

package org.opencms.search;

import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;

/**
 * Provides common functions regarding searching.<p>
 *
 * @since 9.0.0
 */
public final class CmsSearchUtil {

    /** Date format object that obeys ISO 8601 which is used by Solr. */
    private static final DateFormat DATEFORMAT_ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchUtil.class);

    /** Variable to hold an UTC timezone object. */
    private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /**
     * Private constructor of utitlity class.<p>
     */
    private CmsSearchUtil() {

        // noop
    }

    /**
     * Computes the search root folders for the given search parameters based on the search scope.<p>
     *
     * @param cms the current CMS context
     * @param params the current search parameters
     *
     * @return the search root folders based on the search scope
     */
    public static List<String> computeScopeFolders(CmsObject cms, CmsGallerySearchParameters params) {

        String subsite = null;
        if (params.getReferencePath() != null) {
            subsite = OpenCms.getADEManager().getSubSiteRoot(
                cms,
                cms.getRequestContext().addSiteRoot(params.getReferencePath()));
            if (subsite != null) {
                subsite = cms.getRequestContext().removeSiteRoot(subsite);
            } else if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_GALLERIES_COULD_NOT_EVALUATE_SUBSITE_1,
                        params.getReferencePath()));
            }
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_GALLERIES_NO_REFERENCE_PATH_PROVIDED_0));
        }

        List<String> scopeFolders = getSearchRootsForScope(
            params.getScope(),
            cms.getRequestContext().getSiteRoot(),
            subsite);
        return scopeFolders;
    }

    /**
     * Returns a given date object in the ISO 8601 format.
     *
     * @param date that should be converted.
     * @return string that represents the given date in the ISO 8601 format.
     */
    public static String getDateAsIso8601(Date date) {

        synchronized (CmsSearchUtil.class) {
            if (DATEFORMAT_ISO_8601.getTimeZone() != TIMEZONE_UTC) {
                DATEFORMAT_ISO_8601.setTimeZone(TIMEZONE_UTC);
            }
            return DATEFORMAT_ISO_8601.format(date);
        }
    }

    /**
     * Returns a given date object in the ISO 8601 format.
     *
     * @param date that should be converted.
     * @return string that represents the given date in the ISO 8601 format.
     */
    public static String getDateAsIso8601(long date) {

        // Check if date is set
        if ((date > Long.MIN_VALUE) && (date < Long.MAX_VALUE)) {
            final Date d_date = new Date(date);
            return getDateAsIso8601(d_date);
        }
        return null;
    }

    /**
     * Returns a time interval as Solr compatible query string.
     * @param searchField the field to search for.
     * @param startTime the lower limit of the interval.
     * @param endTime the upper limit of the interval.
     * @return Solr compatible query string.
     */
    public static String getDateCreatedTimeRangeFilterQuery(String searchField, long startTime, long endTime) {

        String sStartTime = null;
        String sEndTime = null;

        // Convert startTime to ISO 8601 format
        if ((startTime > Long.MIN_VALUE) && (startTime < Long.MAX_VALUE)) {
            sStartTime = CmsSearchUtil.getDateAsIso8601(new Date(startTime));
        }

        // Convert endTime to ISO 8601 format
        if ((endTime > Long.MIN_VALUE) && (endTime < Long.MAX_VALUE)) {
            sEndTime = CmsSearchUtil.getDateAsIso8601(new Date(endTime));
        }

        // Build Solr range string
        final String rangeString = CmsSearchUtil.getSolrRangeString(sStartTime, sEndTime);

        // Build Solr filter string
        return String.format("%s:%s", searchField, rangeString);
    }

    /**
     * Gets the search roots to use for the given site/subsite parameters.<p>
     *
     * @param scope the search scope
     * @param siteParam the current site
     * @param subSiteParam the current subsite
     *
     * @return the list of search roots for that option
     */
    public static List<String> getSearchRootsForScope(
        CmsGallerySearchScope scope,
        String siteParam,
        String subSiteParam) {

        List<String> result = new ArrayList<String>();
        if (scope == CmsGallerySearchScope.everything) {
            result.add("/");
            return result;
        }
        if (scope.isIncludeSite()) {
            result.add(siteParam);
        }
        if (scope.isIncludeSubSite()) {
            if (subSiteParam == null) {
                result.add(siteParam);
            } else {
                result.add(CmsStringUtil.joinPaths(siteParam, subSiteParam));
            }
        }
        if (scope.isIncludeShared()) {
            String sharedFolder = OpenCms.getSiteManager().getSharedFolder();
            if (sharedFolder != null) {
                result.add(sharedFolder);
            }
        }
        return result;
    }

    /**
     * Returns a string that represents a valid Solr query range.
     *
     * @param from Lower bound of the query range.
     * @param to Upper bound of the query range.
     * @return String that represents a Solr query range.
     */
    public static String getSolrRangeString(String from, String to) {

        // If a parameter is not initialized, use the asterisk '*' operator
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(from)) {
            from = "*";
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(to)) {
            to = "*";
        }

        return String.format("[%s TO %s]", from, to);
    }
}
