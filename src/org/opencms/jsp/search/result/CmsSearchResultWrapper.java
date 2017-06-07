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

package org.opencms.jsp.search.result;

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.controller.I_CmsSearchControllerDidYouMean;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetQuery;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetRange;
import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

/** Wrapper for the whole search result. Also allowing to access the search form controller. */
public class CmsSearchResultWrapper implements I_CmsSearchResultWrapper {

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
    /** Map from range facet names to the facets as given by the search result. */
    @SuppressWarnings("rawtypes")
    private Map<String, RangeFacet> m_rangeFacetMap;
    /** Map from facet names to the facet entries checked, but not part of the result. */
    private Map<String, List<String>> m_missingFieldFacetEntryMap;
    /** Map from facet names to the facet entries checked, but not part of the result. */
    private Map<String, List<String>> m_missingRangeFacetEntryMap;
    /** Query facet items that are checked, but not part of the result. */
    private List<String> m_missingQueryFacetEntries;
    /** Map with the facet items of the query facet and their counts. */
    private Map<String, Integer> m_facetQuery;
    /** CmsObject. */
    private final CmsObject m_cmsObject;
    /** Search exception, if one occurs. */
    private final CmsSearchException m_exception;
    /** The search query sent to Solr. */
    private final CmsSolrQuery m_query;

    /** Constructor taking the main search form controller and the result list as normally returned.
     * @param controller The main search form controller.
     * @param resultList The result list as returned from OpenCms' embedded Solr server.
     * @param query The complete query send to Solr.
     * @param cms The Cms object used to access XML contents, if wanted.
     * @param exception Search exception, or <code>null</code> if no exception occurs.
     */
    @SuppressWarnings("rawtypes")
    public CmsSearchResultWrapper(
        final I_CmsSearchControllerMain controller,
        final CmsSolrResultList resultList,
        final CmsSolrQuery query,
        final CmsObject cms,
        final CmsSearchException exception) {

        m_controller = controller;
        m_solrResultList = resultList;
        m_cmsObject = cms;
        m_exception = exception;
        m_query = query;
        if (resultList != null) {
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
            List<RangeFacet> rangeFacets = resultList.getFacetRanges();
            if (null != rangeFacets) {
                m_rangeFacetMap = new HashMap<String, RangeFacet>(rangeFacets.size());
                for (RangeFacet facet : rangeFacets) {
                    m_rangeFacetMap.put(facet.getName(), facet);
                }
            }
        } else {
            m_start = null;
            m_end = 0;
            m_numFound = 0;
            m_maxScore = null;
        }
        if (null == m_rangeFacetMap) {
            m_rangeFacetMap = new HashMap<String, RangeFacet>();
        }
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getController()
     */
    @Override
    public I_CmsSearchControllerMain getController() {

        return m_controller;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getDidYouMeanCollated()
     */
    public String getDidYouMeanCollated() {

        String suggestion = null;
        I_CmsSearchControllerDidYouMean didYouMeanController = getController().getDidYouMean();
        if ((null != didYouMeanController) && didYouMeanController.getConfig().getCollate()) {
            if ((m_solrResultList != null) && (m_solrResultList.getSpellCheckResponse() != null)) {
                suggestion = m_solrResultList.getSpellCheckResponse().getCollatedResult();
            }
        }
        return suggestion;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getDidYouMeanSuggestion()
     */
    public Suggestion getDidYouMeanSuggestion() {

        I_CmsSearchControllerDidYouMean didYouMeanController = getController().getDidYouMean();
        Suggestion usedSuggestion = null;
        if ((null != didYouMeanController)
            && ((m_solrResultList != null) && (m_solrResultList.getSpellCheckResponse() != null))) {
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
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getEnd()
     */
    @Override
    public int getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getException()
     */
    public CmsSearchException getException() {

        return m_exception;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getFacetQuery()
     */
    @Override
    public Map<String, Integer> getFacetQuery() {

        return m_facetQuery;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getFieldFacet()
     */
    @Override
    public Map<String, FacetField> getFieldFacet() {

        if (m_fieldFacetMap == null) {
            m_fieldFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    return m_solrResultList == null ? null : m_solrResultList.getFacetField(fieldName.toString());
                }
            });
        }
        return m_fieldFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getFieldFacets()
     */
    @Override
    public Collection<FacetField> getFieldFacets() {

        return m_solrResultList == null ? null : m_solrResultList.getFacetFields();
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getFinalQuery()
     */
    public CmsSolrQuery getFinalQuery() {

        return m_query;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getHighlighting()
     */
    @Override
    public Map<String, Map<String, List<String>>> getHighlighting() {

        return m_solrResultList == null ? null : m_solrResultList.getHighLighting();
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getMaxScore()
     */
    @Override
    public Float getMaxScore() {

        return m_maxScore;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getMissingSelectedFieldFacetEntries()
     */
    @Override
    public Map<String, List<String>> getMissingSelectedFieldFacetEntries() {

        if (m_missingFieldFacetEntryMap == null) {
            m_missingFieldFacetEntryMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    FacetField facetResult = m_solrResultList == null
                    ? null
                    : m_solrResultList.getFacetField(fieldName.toString());
                    I_CmsSearchControllerFacetField facetController = m_controller.getFieldFacets().getFieldFacetController().get(
                        fieldName.toString());
                    List<String> result = new ArrayList<String>();

                    if (null != facetController) {

                        List<String> checkedEntries = facetController.getState().getCheckedEntries();
                        if (null != facetResult) {
                            List<String> returnedValues = new ArrayList<String>(facetResult.getValues().size());
                            for (FacetField.Count value : facetResult.getValues()) {
                                returnedValues.add(value.getName());
                            }
                            for (String checked : checkedEntries) {
                                if (!returnedValues.contains(checked)) {
                                    result.add(checked);
                                }
                            }
                        } else {
                            result = checkedEntries;
                        }
                    }
                    return result;
                }
            });
        }
        return m_missingFieldFacetEntryMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getMissingSelectedQueryFacetEntries()
     */
    public List<String> getMissingSelectedQueryFacetEntries() {

        if (null == m_missingQueryFacetEntries) {

            Collection<String> returnedValues = getFacetQuery().keySet();

            I_CmsSearchControllerFacetQuery facetController = m_controller.getQueryFacet();

            m_missingQueryFacetEntries = new ArrayList<String>();

            if (null != facetController) {

                List<String> checkedEntries = facetController.getState().getCheckedEntries();
                if (null != returnedValues) {
                    for (String checked : checkedEntries) {
                        if (!returnedValues.contains(checked)) {
                            m_missingQueryFacetEntries.add(checked);
                        }
                    }
                } else {
                    m_missingQueryFacetEntries = checkedEntries;
                }
            }
        }
        return m_missingQueryFacetEntries;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getMissingSelectedRangeFacetEntries()
     */
    public Map<String, List<String>> getMissingSelectedRangeFacetEntries() {

        if (m_missingRangeFacetEntryMap == null) {
            m_missingRangeFacetEntryMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    @SuppressWarnings("rawtypes")
                    RangeFacet facetResult = m_rangeFacetMap.get(fieldName);
                    I_CmsSearchControllerFacetRange facetController = m_controller.getRangeFacets().getRangeFacetController().get(
                        fieldName.toString());
                    List<String> result = new ArrayList<String>();

                    if (null != facetController) {

                        List<String> checkedEntries = facetController.getState().getCheckedEntries();
                        if (null != facetResult) {
                            List<String> returnedValues = new ArrayList<String>(facetResult.getCounts().size());
                            for (Object value : facetResult.getCounts()) {
                                //TODO: Should yield RangeFacet.Count - but somehow does not!?!?
                                // Hence, the cast should not be necessary at all.
                                returnedValues.add(((RangeFacet.Count)value).getValue());
                            }
                            for (String checked : checkedEntries) {
                                if (!returnedValues.contains(checked)) {
                                    result.add(checked);
                                }
                            }
                        } else {
                            result = checkedEntries;
                        }
                    }
                    return result;
                }
            });
        }
        return m_missingRangeFacetEntryMap;

    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getNumFound()
     */
    @Override
    public long getNumFound() {

        return m_numFound;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getNumPages()
     */
    @Override
    public int getNumPages() {

        return m_solrResultList == null
        ? 1
        : (int)((m_solrResultList.getNumFound() - 1) / m_controller.getPagination().getConfig().getPageSize()) + 1;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getPageNavFirst()
     */
    @Override
    public int getPageNavFirst() {

        final int page = m_controller.getPagination().getState().getCurrentPage()
            - ((m_controller.getPagination().getConfig().getPageNavLength() - 1) / 2);
        return page < 1 ? 1 : page;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getPageNavLast()
     */
    @Override
    public int getPageNavLast() {

        final int page = m_controller.getPagination().getState().getCurrentPage()
            + ((m_controller.getPagination().getConfig().getPageNavLength()) / 2);
        return page > getNumPages() ? getNumPages() : page;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getRangeFacet()
     */
    @SuppressWarnings("rawtypes")
    public Map<String, RangeFacet> getRangeFacet() {

        return m_rangeFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getRangeFacets()
     */
    @SuppressWarnings("rawtypes")
    public Collection<RangeFacet> getRangeFacets() {

        return m_rangeFacetMap.values();
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getSearchResults()
     */
    @Override
    public Collection<I_CmsSearchResourceBean> getSearchResults() {

        return m_foundResources;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getStart()
     */
    @Override
    public Long getStart() {

        return m_start;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResultWrapper#getStateParameters()
     */
    public CmsSearchStateParameters getStateParameters() {

        Map<String, String[]> parameters = new HashMap<String, String[]>();
        m_controller.addParametersForCurrentState(parameters);
        return new CmsSearchStateParameters(this, parameters);
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
