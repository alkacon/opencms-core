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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;

import org.htmlparser.util.ParserException;

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
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /** Variable to hold an GMT timezone object. */
    public static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");

    //start HttpClient
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    //These are included for back compat
    private static final Collection<String> DEFAULT_HTTP_CLIENT_PATTERNS = Arrays.asList(
        PATTERN_ASCTIME,
        PATTERN_RFC1036,
        PATTERN_RFC1123);
    //---------------------------------------------------------------------------------------

    /**
     * A suite of default date formats that can be parsed, and thus transformed to the Solr specific format
     */
    public static final Collection<String> DEFAULT_DATE_FORMATS = new ArrayList<>();

    private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

    static {
        DEFAULT_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DEFAULT_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss");
        DEFAULT_DATE_FORMATS.add("yyyy-MM-dd");
        DEFAULT_DATE_FORMATS.add("yyyy-MM-dd hh:mm:ss");
        DEFAULT_DATE_FORMATS.add("yyyy-MM-dd HH:mm:ss");
        DEFAULT_DATE_FORMATS.add("EEE MMM d hh:mm:ss z yyyy");
        DEFAULT_DATE_FORMATS.addAll(DEFAULT_HTTP_CLIENT_PATTERNS);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ROOT);
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
        DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
    }

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
            result.add(CmsSiteManagerImpl.PATH_SYSTEM_SHARED_FOLDER);
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

    /**
     * Returns a formatter that can be use by the current thread if needed to
     * convert Date objects to the Internal representation.
     *
     * @param d The input date to parse
     * @return The parsed {@link java.util.Date}
     * @throws java.text.ParseException If the input can't be parsed
     */
    public static Date parseDate(String d) throws ParseException {

        return parseDate(d, DEFAULT_DATE_FORMATS);
    }

    public static Date parseDate(String d, Collection<String> fmts) throws ParseException {

        // 2007-04-26T08:05:04Z
        if (d.endsWith("Z") && (d.length() > 20)) {
            return DATEFORMAT_ISO_8601.parse(d);
        }
        return parseDate(d, fmts, null);
    }

    /**
     * Slightly modified from org.apache.commons.httpclient.util.DateUtil.parseDate
     * <p>
     * Parses the date value using the given date formats.
     *
     * @param dateValue   the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate   During parsing, two digit years will be placed in the range
     *                    <code>startDate</code> to <code>startDate + 100 years</code>. This value may
     *                    be <code>null</code>. When <code>null</code> is given as a parameter, year
     *                    <code>2000</code> will be used.
     * @return the parsed date
     * @throws ParseException if none of the dataFormats could parse the dateValue
     */
    public static Date parseDate(String dateValue, Collection<String> dateFormats, Date startDate)
    throws ParseException {

        if (dateValue == null) {
            throw new IllegalArgumentException("dateValue is null");
        }
        if (dateFormats == null) {
            dateFormats = DEFAULT_HTTP_CLIENT_PATTERNS;
        }
        if (startDate == null) {
            startDate = DEFAULT_TWO_DIGIT_YEAR_START;
        }
        // trim single quotes around date if present
        // see issue #5279
        if ((dateValue.length() > 1) && dateValue.startsWith("'") && dateValue.endsWith("'")) {
            dateValue = dateValue.substring(1, dateValue.length() - 1);
        }

        SimpleDateFormat dateParser = null;
        Iterator formatIter = dateFormats.iterator();

        while (formatIter.hasNext()) {
            String format = (String)formatIter.next();
            if (dateParser == null) {
                dateParser = new SimpleDateFormat(format, Locale.ENGLISH);
                dateParser.setTimeZone(TIMEZONE_GMT);
                dateParser.set2DigitYearStart(startDate);
            } else {
                dateParser.applyPattern(format);
            }
            try {
                return dateParser.parse(dateValue);
            } catch (ParseException pe) {
                // ignore this exception, we will try the next format
            }
        }

        // we were unable to parse the date
        throw new ParseException("Unable to parse the date " + dateValue, 0);
    }

    /**
     * Strips of HTML of the value to map, if necessary (depending on the property name).
     * @param propertyName name of the property.
     * @param value the properties value (possibly with HTML)
     * @return the value with HTML stripped of, or the original value, if stripping of the HTML fails.
     */
    public static String stripHtmlFromPropertyIfNecessary(String propertyName, String value) {

        if (propertyName.equals(CmsPropertyDefinition.PROPERTY_DESCRIPTION_HTML)) {
            try {
                return CmsHtmlExtractor.extractText(value, CmsEncoder.ENCODING_UTF_8);
            } catch (ParserException | UnsupportedEncodingException e) {
                LOG.warn("Could not strip HTML from property value. Returning the original value.", e);
            }
        }
        return value;

    }

    /**
     * Take a string and make it an iterable ContentStream
     */
    public static Collection<ContentStream> toContentStreams(final String str, final String contentType) {

        if (str == null) {
            return null;
        }

        ArrayList<ContentStream> streams = new ArrayList<>(1);
        ContentStreamBase ccc = new ContentStreamBase.StringStream(str);
        ccc.setContentType(contentType);
        streams.add(ccc);
        return streams;
    }

    /**
     * @param d SolrInputDocument to convert
     * @return a SolrDocument with the same fields and values as the SolrInputDocument
     * @deprecated This method will be removed in Solr 6.0
     */
    @Deprecated
    public static SolrDocument toSolrDocument(SolrInputDocument d) {

        SolrDocument doc = new SolrDocument();
        for (SolrInputField field : d) {
            doc.setField(field.getName(), field.getValue());
        }
        if (d.getChildDocuments() != null) {
            for (SolrInputDocument in : d.getChildDocuments()) {
                doc.addChildDocument(toSolrDocument(in));
            }

        }
        return doc;
    }

    /**
     * @param d SolrDocument to convert
     * @return a SolrInputDocument with the same fields and values as the
     *   SolrDocument.
     * @deprecated This method will be removed in Solr 6.0
     */
    @Deprecated
    public static SolrInputDocument toSolrInputDocument(SolrDocument d) {

        SolrInputDocument doc = new SolrInputDocument();
        d.getFieldNames().forEach(name -> doc.addField(name, d.getFieldValue(name)));
        return doc;
    }

}
