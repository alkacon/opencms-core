/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchResourcesCollector.java,v $
 * Date   : $Date: 2011/03/23 14:52:48 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Collector for receiving CmsResources from a search result set.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.1.0 
 */
public class CmsSearchResourcesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "searchresources";

    /** Meta Parameter name constant. */
    public static final String PARAM_FIELDS = "fields";

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
    protected Map m_srCache = new HashMap();

    /** Cached search bean. */
    private CmsSearch m_searchBean;

    /** Cached search results. */
    private List m_searchResults;

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
     */
    public CmsSearchResourcesCollector(
        A_CmsListExplorerDialog wp,
        String query,
        String sort,
        String fields,
        List searchRoots,
        String minCreationDate,
        String maxCreationDate,
        String minLastModificationDate,
        String maxLastModificationDate) {

        super(wp);
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
    public List getCollectorNames() {

        List names = new ArrayList();
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List getResources(CmsObject cms, Map params) throws CmsException {

        List result = getSearchResults(params);
        int count = getSearchBean(params).getSearchResultCount();
        Object[] objs = new Object[count];
        Arrays.fill(objs, new Object());
        int from = (getSearchBean(params).getSearchPage() - 1) * getSearchBean(params).getMatchesPerPage();
        int siteLen = cms.getRequestContext().getSiteRoot().length();
        Iterator it = result.iterator();
        while (it.hasNext()) {
            CmsSearchResult sr = (CmsSearchResult)it.next();
            CmsResource resource = cms.readResource(sr.getPath().substring(siteLen), CmsResourceFilter.ALL);
            m_resCache.put(resource.getStructureId().toString(), resource);
            m_srCache.put(resource.getStructureId().toString(), sr);
            objs[from] = resource;
            from++;
        }
        return Arrays.asList(objs);
    }

    /**
     * Returns the search result object for the given structure id.<p>
     * 
     * @param structureId the structure id
     * 
     * @return the resource
     */
    public CmsSearchResult getSearchResult(String structureId) {

        return (CmsSearchResult)m_srCache.get(structureId);
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
    protected List getInternalResources(CmsObject cms, Map params) throws CmsException {

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

        item.set(CmsSearchResultsList.LIST_COLUMN_SCORE, new Integer(getSearchResult(item.getId()).getScore()));
    }

    /**
     * Returns the search bean object.<p>
     * 
     * @param params the parameter map
     * 
     * @return the used search bean
     */
    private CmsSearch getSearchBean(Map params) {

        if (m_searchBean == null) {
            m_searchBean = new CmsSearch();
            m_searchBean.init(getWp().getCms());
            m_searchBean.setParameters(getSearchParameters(params));
            m_searchBean.setIndex(getWp().getSettings().getUserSettings().getWorkplaceSearchIndexName());
            m_searchBean.setMatchesPerPage(getWp().getSettings().getUserSettings().getExplorerFileEntries());
            m_searchBean.setSearchPage(Integer.parseInt((String)params.get(I_CmsListResourceCollector.PARAM_PAGE)));
            // set search roots
            List resources = getResourceNamesFromParam(params);
            String[] searchRoots = new String[resources.size()];
            resources.toArray(searchRoots);
            m_searchBean.setSearchRoots(searchRoots);
        } else {
            int page = Integer.parseInt((String)params.get(I_CmsListResourceCollector.PARAM_PAGE));
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
    private CmsSearchParameters getSearchParameters(Map params) {

        CmsSearchParameters searchParams = new CmsSearchParameters();
        searchParams.setQuery((String)params.get(PARAM_QUERY));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)params.get(PARAM_SORT))) {
            searchParams.setSortName((String)params.get(PARAM_SORT));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)params.get(PARAM_MINCREATIONDATE))) {
            searchParams.setMinDateCreated(Long.parseLong((String)params.get(PARAM_MINCREATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)params.get(PARAM_MAXCREATIONDATE))) {
            searchParams.setMaxDateCreated(Long.parseLong((String)params.get(PARAM_MAXCREATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)params.get(PARAM_MINLASTMODIFICATIONDATE))) {
            searchParams.setMinDateLastModified(Long.parseLong((String)params.get(PARAM_MINLASTMODIFICATIONDATE)));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)params.get(PARAM_MAXLASTMODIFICATIONDATE))) {
            searchParams.setMaxDateLastModified(Long.parseLong((String)params.get(PARAM_MAXLASTMODIFICATIONDATE)));
        }
        List fields = CmsStringUtil.splitAsList((String)params.get(PARAM_FIELDS), ',');
        searchParams.setFields(fields);
        searchParams.setSearchPage(Integer.parseInt((String)params.get(I_CmsListResourceCollector.PARAM_PAGE)));
        return searchParams;
    }

    /**
     * Returns the search result list.<p>
     * 
     * @param params the parameter map
     * 
     * @return a list of {@link org.opencms.search.CmsSearchResult} objects
     */
    private List getSearchResults(Map params) {

        if (m_searchResults == null) {
            m_searchResults = getSearchBean(params).getSearchResult();
            m_searchResults = (m_searchResults == null ? Collections.EMPTY_LIST : m_searchResults);
        }
        return m_searchResults;
    }
}
