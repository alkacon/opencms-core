/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.I_CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A collector to fetch XML contents in a folder or the current site filtered by one or more given category types.<p>
 * 
 * The return list will also be filtered by given key value pairs which are given as a collector parameter.<p>
 * 
 * Usage:
 * <code>
 * &lt;cms:contentload collector=&quot;allKeyValuePairFiltered&quot; param=&quot;resource=[filename]|resourceType=[resource type]|categoryTypes=[category1,category2,...]|subTree=[boolean]|sortBy=[category|date|property:[property_name]]|sortAsc=[boolean]&quot;&gt;
 * </code>
 * 
 * @since 7.0.0
 */
public class CmsCategoryResourceCollector extends A_CmsResourceCollector {

    /**
     * Data structure for the collector, parsed from the collector parameters.<p>
     *
     * In addition to the superclass this implementation accepts parameters that build key value pairs separated by
     * pipes '|', which allows arbitrary order of parameters and free numbers of parameters.<p>
     * 
     * Usage:
     * <code>
     * &quot;resource=[filename]|resourceType=[resource type]|categoryTypes=[category1,category2,...]|subTree=[boolean]|sortBy=[category|date|property:[property_name]]|sortAsc=[boolean]&quot;
     * </code>
     */
    private static final class CmsCategoryCollectorData extends CmsCollectorData {

        /** The collector parameter key for the resource type. */
        public static final String PARAM_KEY_CATEGORY_TYPES = "categoryTypes";

        /** The collector parameter key for the count. */
        public static final String PARAM_KEY_COUNT = "count";

        /** The collector parameter key for the resource (folder / file). */
        public static final String PARAM_KEY_RESOURCE = "resource";

        /** The collector parameter key for the resource type. */
        public static final String PARAM_KEY_RESOURCE_TYPE = "resourceType";

        /** The collector parameter key for sort ascending. */
        public static final String PARAM_KEY_SORT_ASC = "sortAsc";

        /** The collector parameter key for sort by. */
        public static final String PARAM_KEY_SORT_BY = "sortBy";

        /** The collector parameter key for the sub tree. */
        public static final String PARAM_KEY_SUB_TREE = "subTree";

        /** The list of category types. */
        private List<String> m_categoryTypes;

        /** Indicates if the returned list will be sorted ascending or not (descending). */
        private boolean m_sortAsc;

        /** The returned list will be sort by this ('category', 'date' or 'property' are excepted). */
        private String m_sortBy;

        /** The returned list will be sort by this property value  */
        private String m_sortByPropertyName;

        /** Indicates if the sub tree of the given resource will be searched for appropriate resources too. */
        private boolean m_subTree;

        /**
         * Creates a new collector data set.<p>
         *
         * @param data the data to parse.
         *
         * @throws CmsLoaderException if the given configuration is not valid.
         */
        public CmsCategoryCollectorData(String data)
        throws CmsLoaderException {

            parseExtendedData(data);
        }

        /**
         * Returns the list of requested categories.<p>
         * 
         * @return the list of requested categories
         */
        public List<String> getCategoryTypes() {

            return m_categoryTypes;
        }

        /**
         * Returns the sort by string (only 'date', 'category' or 'property' excepted).<p>
         *
         * @return the sort by string
         */
        public String getSortBy() {

            return m_sortBy;
        }

        /**
         * Returns the sort by this property value.<p>
         *
         * @return the sort by string
         */
        public String getSortByPropertyName() {

            return m_sortByPropertyName;
        }

        /**
         * Returns the sort order. <code>true=asc</code> or <code>false=desc</code>  <p>
         *
         * @return the sort order. <code>true=asc</code> or <code>false=desc</code> 
         */
        public boolean getSortOrder() {

            return m_sortAsc;
        }

        /**
         * Returns <code>true</code> if the list has to be sorted in ascending order.<p>
         *
         * @return <code>true</code> if the list has to be sorted in ascending order
         */
        public boolean isSortAsc() {

            return m_sortAsc;
        }

        /**
         * Returns <code>true</code> if the sub tree of the given resource will be searched too.<p>
         * 
         * @return <code>true</code> if the sub tree of the given resource will be searched too.
         */
        public boolean isSubTree() {

            return m_subTree;
        }

        /**
         * Parses the additional configuration data from the collector param.<p>
         *
         * @param data the configuration data.
         *
         * @throws CmsLoaderException if something goes wrong
         */
        private void parseExtendedData(String data) throws CmsLoaderException {

            String[] keyValueTokens = CmsStringUtil.splitAsArray(data, '|');
            setType(-1);
            for (int i = keyValueTokens.length - 1; i >= 0; i--) {
                String relation = keyValueTokens[i];
                String[] keyValuePair = CmsStringUtil.splitAsArray(relation, '=');

                String key = keyValuePair[0];
                String value = keyValuePair[1];

                if (PARAM_KEY_CATEGORY_TYPES.equals(key)) {
                    m_categoryTypes = CmsStringUtil.splitAsList(value, ',');
                } else if (PARAM_KEY_RESOURCE.equals(key)) {
                    setFileName(value);
                } else if (PARAM_KEY_RESOURCE_TYPE.equals(key)) {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(value);
                    if (type != null) {
                        setType(type.getTypeId());
                    }
                } else if (PARAM_KEY_SORT_ASC.equals(key)) {
                    m_sortAsc = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_SORT_BY.equals(key)) {
                    if (value.contains(":")) {
                        String[] keyValuePairProp = CmsStringUtil.splitAsArray(value, ':');
                        String keyProp = keyValuePairProp[0];
                        String valueProp = keyValuePairProp[1];
                        m_sortBy = keyProp;
                        m_sortByPropertyName = valueProp;
                    } else {
                        m_sortBy = value;
                        m_sortByPropertyName = null;
                    }
                } else if (PARAM_KEY_SUB_TREE.equals(key)) {
                    m_subTree = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_COUNT.equals(key)) {
                    int count = 0;
                    try {
                        count = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    setCount(count);
                } else {
                    LOG.error("Unknow key found in collector parameters.");
                }
            }
        }
    }

    /** Compares the release date of resources in descending order. */
    public static final Comparator<CmsResource> COMPARE_DATE_RELEASED_DESC = new Comparator<CmsResource>() {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsResource r1, CmsResource r2) {

            if (r1 == r2) {
                return 0;
            }

            long date1 = r1.getDateReleased();
            if (date1 == CmsResource.DATE_RELEASED_DEFAULT) {
                // use creation date if release date is not set
                date1 = r1.getDateLastModified();
            }

            long date2 = r2.getDateReleased();
            if (date2 == CmsResource.DATE_RELEASED_DEFAULT) {
                // use creation date if release date is not set
                date2 = r2.getDateLastModified();
            }

            return (date1 > date2) ? 1 : (date1 < date2) ? -1 : 0;
        }
    };

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsCategoryResourceCollector.class);

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {"allKeyValuePairFiltered"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param)
    throws CmsException, CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allKeyValuePairFiltered"
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allKeyValuePairFiltered"
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {

            case 0:
                // "allKeyValuePairFiltered"
                return allKeyValuePairFiltered(cms, param);
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * Collects all resources for the given categories filtered and sorted by the given collector parameter.<p>
     * 
     * @param cms the current OpenCms user context
     * @param param value parameter to filter the resources
     * 
     * @return a list of resources filtered and sorted by the given collector parameter
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allKeyValuePairFiltered(CmsObject cms, String param) throws CmsException {

        CmsCategoryCollectorData data = new CmsCategoryCollectorData(param);

        if ((data.getCategoryTypes() != null) && (data.getCategoryTypes().size() > 0)) {
            List<CmsResource> result = new ArrayList<CmsResource>();
            Map<String, List<CmsResource>> sortCategories = new HashMap<String, List<CmsResource>>();
            String foldername = null;
            boolean includeSubTree = false;
            if (data.getFileName() != null) {
                foldername = CmsResource.getFolderPath(data.getFileName());
                includeSubTree = data.isSubTree();
            } else {
                foldername = "/";
                includeSubTree = true;
            }

            CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addExcludeFlags(CmsResource.FLAG_TEMPFILE);
            if (data.getType() != -1) {
                filter = filter.addRequireType(data.getType());
            }

            List<CmsResource> resources = cms.readResources(foldername, filter, includeSubTree);
            List<String> categoryTypes = data.getCategoryTypes();
            Iterator<CmsResource> itResources = resources.iterator();
            CmsResource resource;
            CmsCategoryService service = CmsCategoryService.getInstance();
            while (itResources.hasNext()) {
                resource = itResources.next();
                Iterator<CmsCategory> itCategories = service.readResourceCategories(cms, cms.getSitePath(resource)).iterator();
                while (itCategories.hasNext()) {
                    CmsCategory category = itCategories.next();
                    if (categoryTypes.contains(category.getPath())) {
                        if ((data.getSortBy() != null) && data.getSortBy().equals("category")) {
                            if (sortCategories.containsKey(category.getPath())) {
                                (sortCategories.get(category.getPath())).add(resource);
                            } else {
                                List<CmsResource> sortResources = new ArrayList<CmsResource>();
                                sortResources.add(resource);
                                sortCategories.put(category.getPath(), sortResources);
                            }
                        } else {
                            if (!result.contains(resource)) {
                                result.add(resource);
                            }
                        }
                    }
                }
            }

            if ((data.getSortBy() != null) && data.getSortBy().equals("date")) {
                if (!data.isSortAsc()) {
                    Collections.sort(result, COMPARE_DATE_RELEASED_DESC);
                } else {
                    Collections.sort(result, I_CmsResource.COMPARE_DATE_RELEASED);
                }
            } else if ((data.getSortBy() != null) && data.getSortBy().equals("category")) {
                // categories are sort by their paths
                Iterator<String> itCategoryTypes = categoryTypes.iterator();
                while (itCategoryTypes.hasNext()) {
                    List<CmsResource> categoryListToAdd = sortCategories.get(itCategoryTypes.next());
                    if (categoryListToAdd != null) {
                        result.addAll(categoryListToAdd);
                    }
                }
            } else if ((data.getSortBy() != null) && data.getSortBy().equals("property")) {
                Comparator<CmsResource> comp = new CmsPropertyResourceComparator(
                    cms,
                    data.getSortByPropertyName(),
                    data.getSortOrder());
                Collections.sort(result, comp);
            }

            return shrinkToFit(result, data.getCount());
        }
        return null;
    }
}