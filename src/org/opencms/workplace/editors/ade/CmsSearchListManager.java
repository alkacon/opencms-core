/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsSearchListManager.java,v $
 * Date   : $Date: 2009/09/01 08:44:20 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Maintains ADE search result lists.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 7.6
 */
public final class CmsSearchListManager {

    /** User additional info key constant. */
    private static final String ADDINFO_ADE_SEARCHPAGE_SIZE = "ADE_SEARCHPAGE_SIZE";

    /** default search page size constant. */
    private static final int DEFAULT_SEARCHPAGE_SIZE = 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchListManager.class);

    /** Singleton instance. */
    private static CmsSearchListManager m_instance;

    /** Search options cache. */
    private Map<CmsUUID, CmsSearchOptions> m_searchOptionsCache;

    /**
     * Creates a new instance.<p>
     */
    private CmsSearchListManager() {

        m_searchOptionsCache = new HashMap<CmsUUID, CmsSearchOptions>();
    }

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the singleton instance
     */
    public static CmsSearchListManager getInstance() {

        if (m_instance == null) {
            m_instance = new CmsSearchListManager();
        }
        return m_instance;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cms the cms context
     * @param options the search options
     * @param types the supported container types
     * @param req the http request
     * @param res the http response
     * 
     * @return JSON object with 2 properties, {@link CmsADEServer#P_ELEMENTS} and {@link CmsADEServer#P_HASMORE}
     * 
     * @throws JSONException if something goes wrong
     */
    public JSONObject getLastSearchResult(
        CmsObject cms,
        CmsSearchOptions options,
        Set<String> types,
        HttpServletRequest req,
        HttpServletResponse res) throws JSONException {

        CmsSearchOptions lastOptions = getSearchOptionsFromCache(cms);
        if (compareSearchOptions(lastOptions, options)) {
            return new JSONObject();
        }
        return getSearchResult(cms, lastOptions, types, req, res);
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cms the cms context
     * @param options the search options
     * @param types the supported container types
     * @param req the http request
     * @param res the http response
     * 
     * @return JSON object with 2 properties, {@link CmsADEServer#P_ELEMENTS} and {@link CmsADEServer#P_HASMORE}
     * 
     * @throws JSONException if something goes wrong
     */
    public JSONObject getSearchResult(
        CmsObject cms,
        CmsSearchOptions options,
        Set<String> types,
        HttpServletRequest req,
        HttpServletResponse res) throws JSONException {

        JSONObject result = new JSONObject();
        JSONArray elements = new JSONArray();
        result.put(CmsADEServer.P_ELEMENTS, elements);

        // get the configured search index 
        CmsUser user = cms.getRequestContext().currentUser();
        String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

        // get the page size
        Integer pageSize = (Integer)user.getAdditionalInfo(ADDINFO_ADE_SEARCHPAGE_SIZE);
        if (pageSize == null) {
            pageSize = new Integer(DEFAULT_SEARCHPAGE_SIZE);
        }

        // set the search parameters
        CmsSearchParameters params = new CmsSearchParameters(options.getText());
        params.setIndex(indexName);
        params.setMatchesPerPage(pageSize.intValue());
        params.setSearchPage(options.getPage() + 1);
        params.setResourceTypes(CmsStringUtil.splitAsList(options.getType(), ","));

        // search
        CmsSearch searchBean = new CmsSearch();
        searchBean.init(cms);
        searchBean.setParameters(params);
        searchBean.setSearchRoot(options.getLocation());
        List<CmsSearchResult> searchResults = searchBean.getSearchResult();

        // helper
        CmsElementUtil elemUtil = new CmsElementUtil(cms, req, res);

        // iterate result list and generate the elements
        Iterator<CmsSearchResult> it = searchResults.iterator();
        while (it.hasNext()) {
            CmsSearchResult sr = it.next();
            // get the element data
            String uri = cms.getRequestContext().removeSiteRoot(sr.getPath());
            try {
                JSONObject resElement = elemUtil.getElementData(uri, types);
                // store element data
                elements.put(resElement);
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        // check if there are more search pages
        int results = searchBean.getSearchPage() * searchBean.getMatchesPerPage();
        boolean hasMore = (searchBean.getSearchResultCount() > results);
        result.put(CmsADEServer.P_HASMORE, hasMore);

        // cache the search options
        m_searchOptionsCache.put(user.getId(), options);

        return result;
    }

    /**
     * Compares two search option objects.<p>
     * 
     * Better than to implement the {@link CmsSearchOptions#equals(Object)} method,
     * since the page number is not considered in this comparison.<p>
     * 
     * @param o1 the first search option object
     * @param o2 the first search option object
     * 
     * @return <code>true</code> if they are equal
     */
    protected boolean compareSearchOptions(CmsSearchOptions o1, CmsSearchOptions o2) {

        if (!o1.getLocation().equals(o2.getLocation())) {
            return false;
        }
        if (!o1.getText().equals(o2.getText())) {
            return false;
        }
        if (!o1.getType().equals(o2.getType())) {
            return false;
        }
        return true;

    }

    /**
     * Returns the cached search options.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the cached search options
     */
    protected CmsSearchOptions getSearchOptionsFromCache(CmsObject cms) {

        CmsUser user = cms.getRequestContext().currentUser();
        CmsSearchOptions searchOptions = m_searchOptionsCache.get(user.getId());
        return searchOptions;
    }
}
