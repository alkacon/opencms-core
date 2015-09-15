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

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.controller.I_CmsSearchControllerDidYouMean;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
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
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

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
    /** Map from facet names to the facet entries checked, but not part of the result. */
    private Map<String, List<String>> m_missingFacetEntryMap;
    /** Map with the facet items of the query facet and their counts. */
    private Map<String, Integer> m_facetQuery;
    /** CmsObject. */
    private final CmsObject m_cmsObject;

    /** Constructor taking the main search form controller and the result list as normally returned.
     * @param controller The main search form controller.
     * @param resultList The result list as returned from OpenCms' embedded Solr server.
     * @param cms The Cms object used to access XML contents, if wanted.
     */
    public CmsSearchResultWrapper(
        final I_CmsSearchControllerMain controller,
        final CmsSolrResultList resultList,
        final CmsObject cms) {

        m_controller = controller;
        m_solrResultList = resultList;
        m_cmsObject = cms;
        convertSearchResults(resultList);
        final long l = resultList.getStart() == null ? 1 : resultList.getStart().longValue() + 1;
        m_start = Long.valueOf(l);
        m_end = resultList.getEnd();
        m_numFound = resultList.getNumFound();
        m_maxScore = resultList.getMaxScore();
        if (resultList.getFacetQuery() != null) {
            Map<String, Integer> originalMap = resultList.getFacetQuery();
            m_facetQuery = new HashMap<String, Integer>(originalMap.size());
            for (String q : resultList.getFacetQuery().keySet()) {
                m_facetQuery.put(removeLocalParamPrefix(q), originalMap.get(q));
            }
        }
    }

    /** Converts the search results from CmsSearchResource to CmsSearchResourceBean.
     * @param searchResults The collection of search results to transform.
     */
    protected void convertSearchResults(final Collection<CmsSearchResource> searchResults) {

        m_foundResources = new ArrayList<I_CmsSearchResourceBean>();
        for (final CmsSearchResource searchResult : searchResults) {
            m_foundResources.add(new CmsSearchResourceBean(searchResult, m_cmsObject));
        }
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getController()
     */
    @Override
    public I_CmsSearchControllerMain getController() {

        return m_controller;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getDidYouMeanCollated()
     */
    public String getDidYouMeanCollated() {

        String suggestion = null;
        I_CmsSearchControllerDidYouMean didYouMeanController = getController().getDidYouMean();
        if ((null != didYouMeanController) && didYouMeanController.getConfig().getCollate()) {
            if (m_solrResultList.getSpellCheckResponse() != null) {
                suggestion = m_solrResultList.getSpellCheckResponse().getCollatedResult();
            }
        }
        return suggestion;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getDidYouMeanSuggestion()
     */
    public Suggestion getDidYouMeanSuggestion() {

        I_CmsSearchControllerDidYouMean didYouMeanController = getController().getDidYouMean();
        Suggestion usedSuggestion = null;
        if ((null != didYouMeanController) && (m_solrResultList.getSpellCheckResponse() != null)) {
            // find most suitable suggestion
            List<Suggestion> suggestionList = m_solrResultList.getSpellCheckResponse().getSuggestions();
            int queryLength = m_controller.getDidYouMean().getState().getQuery().length();
            int minDistance = queryLength + 1;
            for (Suggestion suggestion : suggestionList) {
                int currentDistance = Math.abs(queryLength - suggestion.getToken().length());
                if (currentDistance < minDistance) {
                    usedSuggestion = suggestion;
                    minDistance = currentDistance;
                }
            }
        }
        return usedSuggestion;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getEnd()
     */
    @Override
    public int getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getFacetQuery()
     */
    @Override
    public Map<String, Integer> getFacetQuery() {

        return m_facetQuery;
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
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getMissingSelectedFieldFacetEntries()
     */
    @Override
    public Map<String, List<String>> getMissingSelectedFieldFacetEntries() {

        if (m_missingFacetEntryMap == null) {
            m_missingFacetEntryMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    FacetField facetResult = m_solrResultList.getFacetField(fieldName.toString());
                    I_CmsSearchControllerFacetField facetController = m_controller.getFieldFacets().getFieldFacetController().get(
                        fieldName.toString());
                    List<String> result = new ArrayList<String>();

                    if (null != facetController) {

                        List<String> checkedEntries = facetController.getState().getCheckedEntries();
                        List<String> returnedValues = new ArrayList<String>(facetResult.getValues().size());
                        for (Count value : facetResult.getValues()) {
                            returnedValues.add(value.getName());
                        }
                        for (String checked : checkedEntries) {
                            if (!returnedValues.contains(checked)) {
                                result.add(checked);
                            }
                        }
                    }
                    return result;
                }
            });
        }
        return m_missingFacetEntryMap;
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

        return (int)((m_solrResultList.getNumFound() - 1) / m_controller.getPagination().getConfig().getPageSize()) + 1;
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
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getSearchResults()
     */
    @Override
    public Collection<I_CmsSearchResourceBean> getSearchResults() {

        return m_foundResources;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getStart()
     */
    @Override
    public Long getStart() {

        return m_start;
    }

    /**
     * @see org.opencms.jsp.search.result.I_SearchResultWrapper#getStateParameters()
     */
    public CmsSearchStateParameters getStateParameters() {

        Map<String, String[]> parameters = new HashMap<String, String[]>();
        m_controller.addParametersForCurrentState(parameters);
        return new CmsSearchStateParameters(this, parameters);
    }

    /** Removes the !{ex=...} prefix from the query.
     * @param q the original query
     * @return the query with the prefix !{ex=...} removed.
     */
    private String removeLocalParamPrefix(final String q) {

        int index = q.indexOf('}');
        if (index >= 0) {
            return q.substring(index + 1);
        }
        return q;
    }

}
