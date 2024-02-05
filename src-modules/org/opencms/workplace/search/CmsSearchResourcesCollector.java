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

package org.opencms.workplace.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Collector for receiving CmsResources from a search result set.<p>
 *
 * @since 6.1.0
 */
public class CmsSearchResourcesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "searchresources";

    /** Meta Parameter name constant. */
    public static final String PARAM_FIELDS = "fields";

    /** Meta Parameter index name constant. */
    public static final String PARAM_INDEXNAME = "indexName";

    /** Maximum creation date parameter name constant. */
    public static final String PARAM_MAXCREATIONDATE = "maxCreationDate";

    /** Maximum last modification date parameter name constant. */
    public static final String PARAM_MAXLASTMODIFICATIONDATE = "maxLastModificationDate";

    /** Minimum creation date parameter name constant. */
    public static final String PARAM_MINCREATIONDATE = "minCreationDate";

    /** Minimum last modification date parameter name constant. */
    public static final String PARAM_MINLASTMODIFICATIONDATE = "minLastModificationDate";

    /** Query Parameter name constant. */
    public static final String PARAM_QUERY = "query";

    /** Sort Parameter name constant. */
    public static final String PARAM_SORT = "sort";

    /** Resource cache. */
    protected Map<String, CmsSearchResult> m_srCache = new HashMap<String, CmsSearchResult>();

    /** Cached search bean. */
    private CmsSearch m_searchBean;

    /** Cached search results. */
    private List<CmsSearchResult> m_searchResults;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchResourcesCollector.class);

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     * @param query the search query
     * @param sort the sort by parameter
     * @param fields the comma separated list of fields to search
     * @param searchRoots a list of search roots
     * @param minCreationDate the minimum creation date of the resources to be searched
     * @param maxCreationDate the maximum creation date of the resources to be searched
     * @param minLastModificationDate the minimum creation date of the resources to be searched
     * @param maxLastModificationDate the maximum creation date of the resources to be searched
     * @param indexName the index name to search in
     */
    public CmsSearchResourcesCollector(
        A_CmsListExplorerDialog wp,
        String query,
        String sort,
        String fields,
        List<String> searchRoots,
        String minCreationDate,
        String maxCreationDate,
        String minLastModificationDate,
        String maxLastModificationDate,
        String indexName) {

        super(wp);
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + PARAM_INDEXNAME
            + I_CmsListResourceCollector.SEP_KEYVAL
            + indexName;
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + PARAM_QUERY
            + I_CmsListResourceCollector.SEP_KEYVAL
            + query;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sort)) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
                + PARAM_SORT
                + I_CmsListResourceCollector.SEP_KEYVAL
                + sort;
        }
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + PARAM_FIELDS
            + I_CmsListResourceCollector.SEP_KEYVAL
            + fields;
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(minCreationDate)) && (!minCreationDate.equals("0"))) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
                + PARAM_MINCREATIONDATE
                + I_CmsListResourceCollector.SEP_KEYVAL
                + minCreationDate;
        }
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxCreationDate)) && (!maxCreationDate.equals("0"))) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
                + PARAM_MAXCREATIONDATE
                + I_CmsListResourceCollector.SEP_KEYVAL
                + maxCreationDate;
        }
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(minLastModificationDate))
            && (!minLastModificationDate.equals("0"))) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
                + PARAM_MINLASTMODIFICATIONDATE
                + I_CmsListResourceCollector.SEP_KEYVAL
                + minLastModificationDate;
        }
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxLastModificationDate))
            && (!maxLastModificationDate.equals("0"))) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
                + PARAM_MAXLASTMODIFICATIONDATE
                + I_CmsListResourceCollector.SEP_KEYVAL
                + maxLastModificationDate;
        }

        setResourcesParam(searchRoots);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return Arrays.asList(COLLECTOR_NAME);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) {

        List<CmsSearchResult> result = getSearchResults(params);
        List<CmsResource> resources = new ArrayList<CmsResource>();
        String siteRoot = cms.getRequestContext().getSiteRoot();
        int siteLen = siteRoot.length();
        for (CmsSearchResult sr : result) {
            try {
                String resultPath = sr.getPath();
                if (resultPath.startsWith(siteRoot)) {
                    resultPath = sr.getPath().substring(siteLen);
                }
                CmsResource resource = cms.readResource(resultPath, CmsResourceFilter.ALL);
                m_resCache.put(resource.getStructureId().toString(), resource);
                m_srCache.put(resource.getStructureId().toString(), sr);
                resources.add(resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return resources;
    }

    /**
     * Returns the search result object for the given structure id.<p>
     *
     * @param structureId the structure id
     *
     * @return the resource
     */
    public CmsSearchResult getSearchResult(String structureId) {

        return m_srCache.get(structureId);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setPage(int)
     */
    @Override
    public void setPage(int page) {

        synchronized (this) {
            super.setPage(page);
            m_searchBean = null;
            m_searchResults = null;
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getInternalResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    protected List<CmsResource> getInternalResources(CmsObject cms, Map<String, String> params) {

        synchronized (this) {
            if (m_resources == null) {
                m_resources = getResources(cms, params);
            }
        }
        return m_resources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        item.set(CmsSearchResultsList.LIST_COLUMN_SCORE, Integer.valueOf(getSearchResult(item.getId()).getScore()));
    }

    /**
     * Returns the search bean object.<p>
     *
     * @param params the parameter map
     *
     * @return the used search bean
     */
    private CmsSearch getSearchBean(Map<String, String> params) {

        if (m_searchBean == null) {
            m_searchBean = new CmsSearch();
            m_searchBean.init(getWp().getCms());
            m_searchBean.setParameters(getSearchParameters(params));
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_searchBean.getIndex())) {
                m_searchBean.setIndex(getWp().getSettings().getUserSettings().getWorkplaceSearchIndexName());
            }
            m_searchBean.setMatchesPerPage(getWp().getSettings().getUserSettings().getExplorerFileEntries());
            m_searchBean.setSearchPage(Integer.parseInt(params.get(I_CmsListResourceCollector.PARAM_PAGE)));
            // set search roots
            List<String> resources = getResourceNamesFromParam(params);
            String[] searchRoots = new String[resources.size()];
            resources.toArray(searchRoots);
            for (int i = 0; i < searchRoots.length; i++) {
                searchRoots[i] = getWp().getCms().addSiteRoot(searchRoots[i]);
            }
            m_searchBean.setSearchRoots(searchRoots);
        } else {
            int page = Integer.parseInt(params.get(I_CmsListResourceCollector.PARAM_PAGE));
            if (m_searchBean.getSearchPage() != page) {
                m_searchBean.setSearchPage(page);
                m_searchResults = null;
            }
        }
        return m_searchBean;
    }

    /**
     * Returns a new search parameters object from the request parameters.<p>
     *
     * @param params the parameter map
     *
     * @return a search parameters object
     */
    private CmsSearchParameters getSearchParameters(Map<String, String> params) {

        CmsSearchParameters searchParams = new CmsSearchParameters();
        searchParams.setQuery(params.get(PARAM_QUERY));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_SORT))) {
            searchParams.setSortName(params.get(PARAM_SORT));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_MINCREATIONDATE))) {
            searchParams.setMinDateCreated(Long.parseLong(params.get(PARAM_MINCREATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_MAXCREATIONDATE))) {
            searchParams.setMaxDateCreated(Long.parseLong(params.get(PARAM_MAXCREATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_MINLASTMODIFICATIONDATE))) {
            searchParams.setMinDateLastModified(Long.parseLong(params.get(PARAM_MINLASTMODIFICATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_MAXLASTMODIFICATIONDATE))) {
            searchParams.setMaxDateLastModified(Long.parseLong(params.get(PARAM_MAXLASTMODIFICATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.get(PARAM_INDEXNAME))) {
            searchParams.setIndex(params.get(PARAM_INDEXNAME));
        }

        List<String> fields = CmsStringUtil.splitAsList(params.get(PARAM_FIELDS), ',');
        searchParams.setFields(fields);
        searchParams.setSearchPage(Integer.parseInt(params.get(I_CmsListResourceCollector.PARAM_PAGE)));
        return searchParams;
    }

    /**
     * Returns the search result list.<p>
     *
     * @param params the parameter map
     *
     * @return a list of {@link org.opencms.search.CmsSearchResult} objects
     */
    private List<CmsSearchResult> getSearchResults(Map<String, String> params) {

        if (m_searchResults == null) {
            m_searchResults = getSearchBean(params).getSearchResult();
        }
        return m_searchResults;
    }
}
