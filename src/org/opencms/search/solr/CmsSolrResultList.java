
package org.opencms.search.solr;

import org.opencms.search.CmsSearchResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Encapsulates a list of 'OpenCms resource documents' ({@link CmsSearchResource}).<p>
 *
 * This list can be accessed exactly like an {@link ArrayList} which entries are
 * {@link CmsSearchResource} that extend {@link org.opencms.file.CmsResource} and
 * holds the Solr implementation of {@link org.opencms.search.I_CmsSearchDocument}
 * as member. <b>This enables you to deal with the resulting list as you do with
 * well known {@link List} and work on it's entries like you do on
 * {@link org.opencms.file.CmsResource}.</b><p>
 *
 * @since 8.5.0
 */
public class CmsSolrResultList extends ArrayList<CmsSearchResource> {

    /** The serial version UID. */
    private static final long serialVersionUID = 707475894827620542L;

    /** The end index of documents to display (start + rows). */
    private int m_end;

    /** The time in ms when the highlighting is finished. */
    private long m_highlightEndTime;

    /** A map of highlighting. */
    private Map<String, Map<String, List<String>>> m_highlighting;

    /** The current page (start / rows), used to build a pagination. */
    private int m_page;

    /** The original Solr query. */
    private SolrQuery m_query;

    /** The original query response. */
    private QueryResponse m_queryResponse;

    /** The original list of Solr documents. */
    private SolrDocumentList m_resultDocuments;

    /** The row count. */
    private Integer m_rows;

    /** The start time, when the search query was executed. */
    private long m_startTime;

    /** The count of visible documents. */
    private long m_visibleHitCount;

    /**
     * The public constructor.<p>
     *
     * @param query original Solr query
     * @param queryResponse original query response
     * @param resultDocuments original list of Solr documents
     * @param resourceDocumentList the list of resource documents
     * @param start the start (offset)
     * @param rows the rows (hits per page)
     * @param end the end (start + rows)
     * @param page the current page (start / rows)
     * @param visibleHitCount the visible hit count
     * @param maxScore the max score of the best matching doc
     * @param startTime the start time when the query has been executed
     * @param highlightEndTime the time in ms when the highlighting is finished
     */
    public CmsSolrResultList(
        SolrQuery query,
        QueryResponse queryResponse,
        SolrDocumentList resultDocuments,
        List<CmsSearchResource> resourceDocumentList,
        int start,
        Integer rows,
        int end,
        int page,
        long visibleHitCount,
        Float maxScore,
        long startTime,
        long highlightEndTime) {

        super(resourceDocumentList);

        m_query = query;
        m_startTime = startTime;
        m_highlightEndTime = highlightEndTime;
        m_rows = rows;
        m_end = end;
        m_page = page;
        m_visibleHitCount = visibleHitCount;

        m_resultDocuments = resultDocuments;
        m_queryResponse = queryResponse;

        m_highlighting = transformHighlighting();
    }

    /**
     * Returns the last index of documents to display.<p>
     *
     * @return the last index of documents to display
     */
    public int getEnd() {

        return m_end;
    }

    /**
     * Delegator.<p>
     *
     * @param name the name
     *
     * @return the facet field
     */
    public FacetField getFacetDate(String name) {

        return m_queryResponse.getFacetDate(name);
    }

    /**
     * Delegator.<p>
     *
     * @return the list of faceted date fields
     */
    public List<FacetField> getFacetDates() {

        return m_queryResponse.getFacetDates();
    }

    /**
     * Delegator.<p>
     *
     * @param name the name
     *
     * @return the facet field
     */
    public FacetField getFacetField(String name) {

        return m_queryResponse.getFacetField(name);
    }

    /**
     * Delegator.<p>
     *
     * @return the list of faceted fields
     */
    public List<FacetField> getFacetFields() {

        return m_queryResponse.getFacetFields();
    }

    /**
     * Delegator.<p>
     *
     * @return the facet query
     */
    public Map<String, Integer> getFacetQuery() {

        return m_queryResponse.getFacetQuery();
    }

    /**
     * Delegator.<p>
     *
     * @return the list of facet ranges
     */
    @SuppressWarnings("rawtypes")
    public List<RangeFacet> getFacetRanges() {

        return m_queryResponse.getFacetRanges();
    }

    /**
     * Returns the time in ms when the highlighting is finished.<p>
     *
     * @return the time in ms when the highlighting is finished
     */
    public long getHighlightEndTime() {

        return m_highlightEndTime;
    }

    /**
     * Returns the highlighting information.<p>
     *
     * @return the highlighting information
     */
    public Map<String, Map<String, List<String>>> getHighLighting() {

        return m_highlighting;
    }

    /**
     * Delegator.<p>
     *
     * @return the limiting facets
     */
    public List<FacetField> getLimitingFacets() {

        return m_queryResponse.getLimitingFacets();
    }

    /**
     * Returns the score of the best matching document.<p>
     *
     * @return the score of the best matching document
     */
    public Float getMaxScore() {

        return m_resultDocuments.getMaxScore();
    }

    /**
     * Returns the count of docs that have been found.<p>
     *
     * @return the count of docs that have been found
     */
    public long getNumFound() {

        return m_resultDocuments.getNumFound();
    }

    /**
     * Returns the current page.<p>
     *
     * @return the current page
     */
    public int getPage() {

        return m_page;
    }

    /**
     * The original Solr query.<p>
     *
     * @return the query
     */
    public SolrQuery getQuery() {

        return m_query;
    }

    /**
     * Returns the requested row count.<p>
     *
     * @return the rows
     */
    public Integer getRows() {

        return m_rows;
    }

    /**
     * Delegator.<p>
     *
     * @return the spellcheck response
     */
    public SpellCheckResponse getSpellCheckResponse() {

        return m_queryResponse.getSpellCheckResponse();
    }

    /**
     * Returns the start index (offset).<p>
     *
     * @return the start
     */
    public Long getStart() {

        return Long.valueOf(m_resultDocuments.getStart());
    }

    /**
     * Returns the start time.<p>
     *
     * @return the start time
     */
    public long getStartTime() {

        return m_startTime;
    }

    /**
     * Returns the visible hit count.<p>
     *
     * @return the visible count of documents
     */
    public long getVisibleHitCount() {

        return m_visibleHitCount;
    }

    /**
     * Transforms / corrects the highlighting.<p>
     *
     * @return the highlighting
     */
    private Map<String, Map<String, List<String>>> transformHighlighting() {

        Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();
        if (m_queryResponse.getHighlighting() != null) {
            for (String key : m_queryResponse.getHighlighting().keySet()) {
                Map<String, ?> value = m_queryResponse.getHighlighting().get(key);
                Map<String, List<String>> innerResult = new HashMap<String, List<String>>();
                for (String innerKey : value.keySet()) {
                    Object entry = value.get(innerKey);
                    List<String> innerList = new ArrayList<String>();
                    if (entry instanceof String) {
                        innerResult.put(innerKey, Collections.singletonList((String)entry));
                    } else if (entry instanceof String[]) {
                        String[] li = (String[])entry;
                        for (Object lo : li) {
                            String s = (String)lo;
                            innerList.add(s);
                        }
                        innerResult.put(innerKey, innerList);
                    } else if (entry instanceof List<?>) {
                        List<?> li = (List<?>)entry;
                        for (Object lo : li) {
                            String s = (String)lo;
                            innerList.add(s);
                        }
                        innerResult.put(innerKey, innerList);
                    }
                }
                result.put(key, innerResult);
            }
        }
        return result;
    }
}
