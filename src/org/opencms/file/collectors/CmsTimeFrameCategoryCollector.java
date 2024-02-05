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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.I_CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A collector that allows to collect resources within a time range based upon
 * a configurable property that contains a time stamp.<p>
 *
 * Additionally a property may be specified that contains a comma separated
 * list of category Strings that have to match the specified list of categories
 * to allow. <p>
 *
 * <b>Demo usage:</b><br/>
 * <pre>
 * &lt;cms:contentload collector="timeFrameAndCategories"
 *   param="
 *     resource=/de/events/|
 *     resourceType=xmlcontent|
 *     resultLimit=10|
 *     sortDescending=true|
 *     excludeTimerange=true|
 *     timeStart=2007-08-01 14:22:12|
 *     timeEnd=2007-08-01 14:22:12|
 *     propertyTime=collector.time|
 *     propertyCategories=collector.categories|
 *     categories=sports,action,lifestyle"
 *   &gt;
 * </pre>
 * <p>
 *
 * <b>The param attribute</b>
 *
 * supports a key - value syntax for collector params.<p>
 *
 * All parameters are specified as follows:
 * <pre>
 * key=value
 * </pre>
 * <p>
 * Many key - value pairs may exist:
 * <pre>
 * key=value|key2=value2|key3=value3
 * </pre>
 * <p>
 * The following keys are reserved:
 * <ul>
 * <li>
 * <b>resource</b><br/>
 * The value defines the folder / single file for collection of results.
 * </li>
 * <li>
 * <b>resourceType</b><br/>
 * The value defines the name of the type of resource that is required for the result as
 * defined in opencms-modules.xml, opencms-workplace.xml.
 * </li>
 * <li>
 * <b>resultLimit</b><br/>
 * The value defines the maximum amount of results to return.
 * </li>
 * <li>
 * <b>sortDescending</b><br/>
 * The value defines if the result is sorted in descending ("true") or ascending
 * (anything else than "true") order.
 * </li>
 * <li>
 * <b>excludeTimeRange</b><br/>
 * The value defines if the result should exclude the time range in an offline project.
 * </li>
 * <li>
 * <b>timeStart</b><br/>
 * The value defines the start time in the format <code>yyyy-MM-dd HH:mm:ss</code> as
 * known by the description of <code>{@link SimpleDateFormat}</code>
 * that will be used for the validity time frame of result candidates.
 * </li>
 * <li>
 * <b>timeEnd</b><br/>
 * The value defines the end time in the format <code>yyyy-MM-dd HH:mm:ss</code> as
 * known by the description of <code>{@link SimpleDateFormat}</code>
 * that will be used for the validity time frame of result candidates.
 * </li>
 * <li>
 * <b>propertyTime</b><br/>
 * The value defines the name of the property that is inspected for a time stamp
 * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity time frame
 * check.
 * </li>
 * <li>
 * <b>propertyCategories</b><br/>
 * The value defines the name of the property that is inspected for a pipe separated
 * list of category strings.
 * </li>
 * <li>
 * <b>categories</b><br/>
 * The value defines a list of comma separated category Strings used to filter
 * result candidates by. If this parameter is missing completely no category
 * filtering will be done and also resources with empty category property will
 * be accepted.
 * </li>
 * </ul>
 * <p>
 *
 * All other key - value pairs are ignored.<p>
 *
 * @since 7.0.3
 *
 */
public class CmsTimeFrameCategoryCollector extends A_CmsResourceCollector {

    /**
     * Supports a key - value syntax for collector params.<p>
     *
     * All parameters are specified as follows:
     * <pre>
     * key=value
     * </pre>
     * <p>
     * Many key - value pairs may exist:
     * <pre>
     * key=value|key2=value2|key3=value3
     * </pre>
     * <p>
     * The following keys are reserved:
     * <ul>
     * <li>
     * <b>resource</b><br/>
     * The value defines the folder / single file for collection of results.
     * </li>
     * <li>
     * <b>resourceType</b><br/>
     * The value defines the name of the type of resource that is required for the result as
     * defined in opencms-modules.xml, opencms-workplace.xml.
     * </li>
     * <li>
     * <b>resultLimit</b><br/>
     * The value defines the maximum amount of results to return.
     * </li>
     * <li>
     * <b>sortDescending</b><br/>
     * The value defines if the result is sorted in descending ("true") or ascending
     * (anything else than "true") order.
     * </li>
     * <li>
     * <b>excludeTimeRange</b><br/>
     * The value defines if the result should exclude the time range in an offline project.
     * </li>
     * <li>
     * <b>timeStart</b><br/>
     * The value defines the start time in the format <code>yyyy-MM-dd HH:mm:ss</code> as
     * known by the description of <code>{@link SimpleDateFormat}</code>
     * that will be used for the validity time frame of result candidates.
     * </li>
     * <li>
     * <b>timeEnd</b><br/>
     * The value defines the end time in the format <code>yyyy-MM-dd HH:mm:ss</code> as
     * known by the description of <code>{@link SimpleDateFormat}</code>
     * that will be used for the validity time frame of result candidates.
     * </li>
     * <li>
     * <b>propertyTime</b><br/>
     * The value defines the name of the property that is inspected for a time stamp
     * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity time frame
     * check.
     * </li>
     * <li>
     * <b>propertyCategories</b><br/>
     * The value defines the name of the property that is inspected for a pipe separated
     * list of category strings.
     * </li>
     * <li>
     * <b>categories</b><br/>
     * The value defines a list of comma separated category Strings used to filter
     * result candidates by. If this parameter is missing completely no category
     * filtering will be done and also resources with empty category property will
     * be accepted.
     * </li>
     * </ul>
     * <p>
     */
    private class CollectorDataPropertyBased extends CmsCollectorData {

        /** The collector parameter key for the categories: value is a list of comma - separated Strings. */
        public static final String PARAM_KEY_CATEGORIES = "categories";

        /** The collector parameter key for the name of the categoires property used to filter resources by. */
        public static final String PARAM_KEY_PPROPERTY_CATEGORIES = "propertyCategories";

        /** The collector parameter key for the name of the property to use for the validity time frame check. */
        public static final String PARAM_KEY_PPROPERTY_TIME = "propertyTime";

        /** The collector parameter key for the resource (folder / file). */
        public static final String PARAM_KEY_RESOURCE = "resource";

        /** The collector parameter key for a result limit. */
        public static final String PARAM_KEY_RESOURCE_TYPE = "resourceType";

        /** The collector parameter key for a result limit. */
        public static final String PARAM_KEY_RESULT_LIMIT = "resultLimit";

        /** The collector parameter key for a result limit. */
        public static final String PARAM_KEY_SORT_DESCENDING = "sortDescending";

        /** The collector parameter key for the start time of the validity time frame. */
        public static final String PARAM_KEY_TIMEFRAME_END = "timeEnd";

        /** The collector parameter key for the start time of the validity time frame. */
        public static final String PARAM_KEY_TIMEFRAME_START = "timeStart";

        /** The List &lt;String&gt; containing the categories to allow. */
        private List<String> m_categories = Collections.emptyList();

        /** The display count. */
        private int m_count;

        /** The resource path (folder / file). */
        private String m_fileName;

        /** The property to look for a pipe separated list of category strings in.*/
        private CmsProperty m_propertyCategories = new CmsProperty();

        /** The property to look up for a time stamp on result candidates for validity time frame check.*/
        private CmsProperty m_propertyTime = new CmsProperty();

        /** If true results should be sorted in descending order.*/
        private boolean m_sortDescending;

        /** The end of the validity time frame.*/
        private long m_timeFrameEnd = Long.MAX_VALUE;

        /** The start of the validity time frame.*/
        private long m_timeFrameStart;

        /** The resource type to require. */
        private I_CmsResourceType m_type;

        /**
         * Constructor with the collector param of the tag.<p>
         *
         * @param data the param attribute value of the contentload tag.
         *
         * @throws CmsLoaderException if the collector param specifies an illegal resource type.
         *
         */
        public CollectorDataPropertyBased(String data)
        throws CmsLoaderException {

            try {
                parseParam(data);
            } catch (ParseException pe) {
                CmsRuntimeException ex = new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_COLLECTOR_PARAM_DATE_FORMAT_SYNTAX_0));
                ex.initCause(pe);
                throw ex;
            }

        }

        /**
         * Returns The List &lt;String&gt; containing the categories to allow.<p>
         *
         * @return The List &lt;String&gt; containing the categories to allow.
         */
        public List<String> getCategories() {

            return m_categories;
        }

        /**
         * Returns the count.
         * <p>
         *
         * @return the count
         */
        @Override
        public int getCount() {

            return m_count;
        }

        /**
         * Returns the file name.<p>
         *
         * @return the file name
         */
        @Override
        public String getFileName() {

            return m_fileName;
        }

        /**
         * Returns the property to look for a pipe separated list of category strings in.<p>
         *
         * Never write this property to VFS as it is "invented in RAM" and not
         * read from VFS!<p>
         *
         * @return the property to look for a pipe separated list of category strings in.
         */
        public CmsProperty getPropertyCategories() {

            return m_propertyCategories;
        }

        /**
         * Returns The property to look up for a time stamp
         * on result candidates for validity time frame check.<p>
         *
         * Never write this property to VFS as it is "invented in RAM" and not
         * read from VFS!<p>
         *
         * @return The property to look up for a time stamp on result candidates for validity time frame check.
         */
        public CmsProperty getPropertyTime() {

            return m_propertyTime;
        }

        /**
         * Returns the timeFrameEnd.<p>
         *
         * @return the timeFrameEnd
         *
         * @see #getPropertyTime()
         */
        public long getTimeFrameEnd() {

            return m_timeFrameEnd;
        }

        /**
         * Returns the timeFrameStart.<p>
         *
         * @return the timeFrameStart
         */
        public long getTimeFrameStart() {

            return m_timeFrameStart;
        }

        /**
         * Returns the type.
         * <p>
         *
         * @return the type
         */
        @Override
        public int getType() {

            return m_type.getTypeId();
        }

        /**
         * If true results should be sorted in descending order.<p>
         *
         * Defaults to true.<p>
         *
         * @return true if results should be sorted in descending order, false
         *      if results should be sorted in ascending order.
         */
        public boolean isSortDescending() {

            return m_sortDescending;
        }

        /**
         * Internally parses the constructor-given param into the data model
         * of this instance.<p>
         *
         * @param param the constructor-given param.
         *
         * @throws CmsLoaderException if the collector param specifies an illegal resource type.
         *
         * @throws ParseException if date parsing in scope of the param attribute fails.
         */
        private void parseParam(final String param) throws CmsLoaderException, ParseException {

            List<String> keyValuePairs = CmsStringUtil.splitAsList(param, '|');
            String[] keyValuePair;
            Iterator<String> itKeyValuePairs = keyValuePairs.iterator();
            String keyValuePairStr;
            String key;
            String value;
            while (itKeyValuePairs.hasNext()) {
                keyValuePairStr = itKeyValuePairs.next();
                keyValuePair = CmsStringUtil.splitAsArray(keyValuePairStr, '=');
                if (keyValuePair.length != 2) {
                    throw new CmsIllegalArgumentException(
                        Messages.get().container(
                            Messages.ERR_COLLECTOR_PARAM_KEY_VALUE_SYNTAX_1,
                            new Object[] {keyValuePairStr}));
                }
                key = String.valueOf(keyValuePair[0]).trim();
                value = String.valueOf(keyValuePair[1]).trim();

                if (PARAM_KEY_RESOURCE.equals(key)) {
                    m_fileName = value;
                } else if (PARAM_KEY_RESOURCE_TYPE.equals(key)) {
                    m_type = OpenCms.getResourceManager().getResourceType(value);
                } else if (PARAM_KEY_RESULT_LIMIT.equals(key)) {
                    m_count = Integer.parseInt(value);
                } else if (PARAM_KEY_SORT_DESCENDING.equals(key)) {
                    m_sortDescending = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_TIMEFRAME_START.equals(key)) {
                    m_timeFrameStart = DATEFORMAT_SQL.parse(value).getTime();
                } else if (PARAM_KEY_TIMEFRAME_END.equals(key)) {
                    m_timeFrameEnd = DATEFORMAT_SQL.parse(value).getTime();
                } else if (PARAM_KEY_PPROPERTY_TIME.equals(key)) {
                    m_propertyTime.setName(value);
                } else if (PARAM_KEY_CATEGORIES.equals(key)) {
                    m_categories = CmsStringUtil.splitAsList(value, ',');
                } else if (PARAM_KEY_PPROPERTY_CATEGORIES.equals(key)) {
                    m_propertyCategories.setName(value);
                } else if (PARAM_EXCLUDETIMERANGE.equalsIgnoreCase(key)) {
                    setExcludeTimerange(Boolean.valueOf(value).booleanValue());
                } else {
                    // nop, one could accept additional filter properties here...
                }

            }

        }

    }

    /** SQL Standard date format: "yyyy-MM-dd HH:mm:ss".*/
    public static final DateFormat DATEFORMAT_SQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** Static array of the collectors implemented by this class. */
    private static final String COLLECTOR_NAME = "timeFrameAndCategories";

    /** Sorted set for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(
        Arrays.asList(new String[] {COLLECTOR_NAME}));

    /**
     * Public constructor.<p>
     */
    public CmsTimeFrameCategoryCollector() {

        // NOOP
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return new ArrayList<String>(COLLECTORS_LIST);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param)
    throws CmsException, CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }
        if (COLLECTOR_NAME.equals(collectorName)) {
            return getCreateInFolder(cms, new CollectorDataPropertyBased(param));
        } else {
            throw new CmsDataAccessException(
                org.opencms.file.collectors.Messages.get().container(
                    org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.A_CmsResourceCollector#getCreateTypeId(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public int getCreateTypeId(CmsObject cms, String collectorName, String param) throws CmsException {

        int result = -1;
        if (param != null) {
            result = new CollectorDataPropertyBased(param).getType();
        }
        return result;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        return getResults(cms, collectorName, param, -1);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param, int numResults)
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }

        if (COLLECTOR_NAME.equals(collectorName)) {
            // "singleFile"
            return getTimeFrameAndCategories(cms, param, numResults);
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }

    /**
     * Returns a list of resources according to the given parameter.<p>
     *
     * @param cms the current cms context
     * @param param the parameter
     * @param numResults the number of results
     *
     * @return the resulting list of resources
     *
     * @throws CmsException if something goes wrong reading the resources
     */
    private List<CmsResource> getTimeFrameAndCategories(CmsObject cms, String param, int numResults)
    throws CmsException {

        List<CmsResource> result = null;
        CollectorDataPropertyBased data = new CollectorDataPropertyBased(param);

        // Step 1: Read from DB, expiration is respected.
        String foldername = CmsResource.getFolderPath(data.getFileName());
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        result = cms.readResources(foldername, filter, true);

        // Step 2: Time range filtering
        String timeProperty = data.getPropertyTime().getName();
        long start = data.getTimeFrameStart();
        long end = data.getTimeFrameEnd();
        long resTime;
        Iterator<CmsResource> itResults = result.iterator();
        CmsProperty prop;
        CmsResource res;
        while (itResults.hasNext()) {
            res = itResults.next();
            prop = cms.readPropertyObject(res, timeProperty, true);
            if (!prop.isNullProperty()) {
                resTime = Long.parseLong(prop.getValue());
                if ((resTime < start) || (resTime > end)) {
                    itResults.remove();
                }
            }
        }

        // Step 3: Category filtering
        List<String> categories = data.getCategories();
        if ((categories != null) && !categories.isEmpty()) {
            itResults = result.iterator();
            String categoriesProperty = data.getPropertyCategories().getName();
            List<String> categoriesFound;
            while (itResults.hasNext()) {
                res = itResults.next();
                prop = cms.readPropertyObject(res, categoriesProperty, true);
                if (prop.isNullProperty()) {
                    // disallow contents with empty category property:
                    itResults.remove();
                    // accept contents with empty category property:
                    // continue;
                } else {
                    categoriesFound = CmsStringUtil.splitAsList(prop.getValue(), '|');

                    // filter: resource has to be at least in one category
                    Iterator<String> itCategories = categories.iterator();
                    String category;
                    boolean contained = false;
                    while (itCategories.hasNext()) {
                        category = itCategories.next();
                        if (categoriesFound.contains(category)) {
                            contained = true;
                            break;
                        }
                    }
                    if (!contained) {
                        itResults.remove();
                    }
                }
            }
        }

        // Step 4: Sorting
        if (data.isSortDescending()) {
            Collections.sort(result, I_CmsResource.COMPARE_DATE_RELEASED);
        } else {
            Collections.sort(result, new ComparatorInverter(I_CmsResource.COMPARE_DATE_RELEASED));
        }

        // Step 5: result limit
        return shrinkToFit(result, data.getCount(), numResults);
    }
}