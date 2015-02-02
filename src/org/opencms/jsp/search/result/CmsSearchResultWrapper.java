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

package org.opencms.jsp.search.result;

import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.solr.client.solrj.response.FacetField;

/** Wrapper for the whole search result. Also allowing to access the search form controller. */
public class CmsSearchResultWrapper implements I_SearchResultWrapper {

    /** The result list as returned normally. */
    final CmsSolrResultList m_solrResultList;
    /** The collection of found resources/documents, already wrapped as {@code I_CmsSearchResourceBean}. */
    private Collection<I_CmsSearchResourceBean> m_foundResources;
    /** The first index of the documents displayed. */
    private final Long m_start;
    /** The last index of the documents displayed. */
    private final int m_end;
    /** The number of found results. */
    private final long m_numFound;
    /** The maximal score of the results. */
    private final Float m_maxScore;
    /** The main controller for the search form. */
    final I_CmsSearchControllerMain m_controller;
    /** Map from field facet names to the facets as given by the search result. */
    private Map<String, FacetField> m_fieldFacetMap;
    /** Map from page numbers (as String) to the links that should be used for the pagination. */
    private Map<String, String> m_paginationLinks;
    /** Map from the sort options (by name) to the links that should be used for choosing the sort option (if the options are not included in the search form. */
    private Map<String, String> m_sortLinks;

    /** Constructor taking the main search form controller and the result list as normally returned.
     * @param controller The main search form controller.
     * @param resultList The result list as returned from OpenCms' embedded Solr server.
     */
    public CmsSearchResultWrapper(final I_CmsSearchControllerMain controller, final CmsSolrResultList resultList) {

        m_controller = controller;
        m_solrResultList = resultList;
        convertSearchResults(resultList);
        final long l = resultList.getStart() == null ? 1 : resultList.getStart().longValue() + 1;
        m_start = Long.valueOf(l);
        m_end = resultList.getEnd();
        m_numFound = resultList.getNumFound();
        m_maxScore = resultList.getMaxScore();
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getController()
     */
    @Override
    public I_CmsSearchControllerMain getController() {

        return m_controller;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getEnd()
     */
    @Override
    public int getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getFieldFacet()
     */
    @Override
    public Map<String, FacetField> getFieldFacet() {

        if (m_fieldFacetMap == null) {
            m_fieldFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    return m_solrResultList.getFacetField(fieldName.toString());
                }
            });
        }
        return m_fieldFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getFieldFacets()
     */
    @Override
    public Collection<FacetField> getFieldFacets() {

        return m_solrResultList.getFacetFields();
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getHighlighting()
     */
    @Override
    public Map<String, Map<String, List<String>>> getHighlighting() {

        return m_solrResultList.getHighLighting();
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getMaxScore()
     */
    @Override
    public Float getMaxScore() {

        return m_maxScore;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getNumFound()
     */
    @Override
    public long getNumFound() {

        return m_numFound;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getNumPages()
     */
    @Override
    public int getNumPages() {

        return (int)(m_solrResultList.getNumFound() / m_controller.getPagination().getConfig().getPageSize()) + 1;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getPageNavFirst()
     */
    @Override
    public int getPageNavFirst() {

        final int page = m_controller.getPagination().getState().getCurrentPage()
            - ((m_controller.getPagination().getConfig().getPageNavLength() - 1) / 2);
        return page < 1 ? 1 : page;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getPageNavLast()
     */
    @Override
    public int getPageNavLast() {

        final int page = m_controller.getPagination().getState().getCurrentPage()
            + ((m_controller.getPagination().getConfig().getPageNavLength()) / 2);
        return page > getNumPages() ? getNumPages() : page;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getPaginationLinkParameters()
     */
    @Override
    public Map<String, String> getPaginationLinkParameters() {

        if (m_paginationLinks == null) {
            m_paginationLinks = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object page) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>();
                    m_controller.addParametersForCurrentState(parameters);
                    parameters.put(m_controller.getPagination().getConfig().getPageParam(), new String[] {(String)page});
                    return paramListToString(parameters);
                }

                private String paramListToString(final Map<String, String[]> parameters) {

                    final StringBuffer result = new StringBuffer();
                    for (final String key : parameters.keySet()) {
                        for (final String value : parameters.get(key)) {
                            result.append(key).append('=').append(value).append('&');
                        }
                    }
                    // remove last '&'
                    if (result.length() > 0) {
                        result.setLength(result.length() - 1);
                    }
                    return result.toString();
                }
            });
        }
        return m_paginationLinks;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getSearchResults()
     */
    @Override
    public Collection<I_CmsSearchResourceBean> getSearchResults() {

        return m_foundResources;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getSortLinkParameters()
     */
    @Override
    public Map<String, String> getSortLinkParameters() {

        if (m_sortLinks == null) {
            m_sortLinks = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object sortOption) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>();
                    m_controller.addParametersForCurrentState(parameters);
                    parameters.put(
                        m_controller.getSorting().getConfig().getSortParam(),
                        new String[] {(String)sortOption});
                    return paramListToString(parameters);
                }

                private String paramListToString(final Map<String, String[]> parameters) {

                    final StringBuffer result = new StringBuffer();
                    for (final String key : parameters.keySet()) {
                        for (final String value : parameters.get(key)) {
                            result.append(key).append('=').append(value).append('&');
                        }
                    }
                    // remove last '&'
                    if (result.length() > 0) {
                        result.setLength(result.length() - 1);
                    }
                    return result.toString();
                }
            });
        }
        return m_sortLinks;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getStart()
     */
    @Override
    public Long getStart() {

        return m_start;
    }

    /** Converts the search results from CmsSearchResource to CmsSearchResourceBean.
     * @param searchResults The collection of search results to transform.
     */
    protected void convertSearchResults(final Collection<CmsSearchResource> searchResults) {

        m_foundResources = new ArrayList<I_CmsSearchResourceBean>();
        for (final CmsSearchResource searchResult : searchResults) {
            m_foundResources.add(new CmsSearchResourceBean(searchResult));
        }
    }
}
