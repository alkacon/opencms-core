
package org.opencms.search.solr;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.QParser;

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

    /** The name for the parameters key of the response header. */
    private static final String HEADER_PARAMS_NAME = "params";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrResultList.class);

    /** The name of the key that is used for the result documents inside the Solr query response. */
    private static final String QUERY_RESPONSE_NAME = "response";

    /** The name of the key that is used for the query time. */
    private static final String QUERY_TIME_NAME = "QTime";

    /** The serial version UID. */
    private static final long serialVersionUID = 707475894827620542L;

    /** The end index of documents to display (start + rows). */
    private int m_end;

    /** The time in ms when the highlighting is finished. */
    private long m_highlightEndTime;

    /** The current page (start / rows), used to build a pagination. */
    private int m_page;

    /** The original Solr query. */
    private SolrQuery m_query;

    /** The original query response. */
    private QueryResponse m_queryResponse;

    /** The original list of Solr documents. */
    private SolrDocumentList m_resultDocuments = new SolrDocumentList();

    /** The row count. */
    private Integer m_rows;

    /** The Solr query request. */
    private SolrQueryRequest m_solrQueryRequest;

    /** The Solr query response. */
    private SolrQueryResponse m_solrQueryResponse;

    /** The start time, when the search query was executed. */
    private long m_startTime;

    /** The count of visible documents. */
    private long m_visibleHitCount;

    /**
     * The public constructor.<p>
     * 
     * @param core the Solr core 
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
     */
    @SuppressWarnings("unchecked")
    public CmsSolrResultList(
        final SolrCore core,
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
        long startTime) {

        super(resourceDocumentList);

        m_query = query;
        m_startTime = startTime;
        m_rows = rows;
        m_end = end;
        m_page = page;
        m_visibleHitCount = visibleHitCount;

        m_resultDocuments = resultDocuments;
        m_resultDocuments.setStart(start);
        m_resultDocuments.setMaxScore(maxScore);
        m_resultDocuments.setNumFound(m_visibleHitCount);

        m_queryResponse = queryResponse;
        m_queryResponse.getResponse().setVal(
            m_queryResponse.getResponse().indexOf(QUERY_RESPONSE_NAME, 0),
            m_resultDocuments);

        if (core != null) {
            initSolrReqRes(core);
        }
        m_queryResponse.getResponseHeader().setVal(
            m_queryResponse.getResponseHeader().indexOf(QUERY_TIME_NAME, 0),
            new Integer(new Long(System.currentTimeMillis() - m_startTime).intValue()));
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
     * Returns the start index (offset).<p>
     * 
     * @return the start
     */
    public Long getStart() {

        return new Long(m_resultDocuments.getStart());
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
     * Returns the solrQueryRequest.<p>
     *
     * @return the solrQueryRequest
     */
    protected SolrQueryRequest getSolrQueryRequest() {

        return m_solrQueryRequest;
    }

    /**
     * Returns the solrQueryResponse.<p>
     *
     * @return the solrQueryResponse
     */
    protected SolrQueryResponse getSolrQueryResponse() {

        return m_solrQueryResponse;
    }

    /**
     * Initializes the Solr query response.<p>
     * 
     * @param core the core to use
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initSolrReqRes(final SolrCore core) {

        // create and initialize the solr response
        m_solrQueryResponse = new SolrQueryResponse();
        m_solrQueryResponse.setAllValues(m_queryResponse.getResponse());
        m_solrQueryResponse.setEndTime(m_queryResponse.getQTime());
        int paramsIndex = m_queryResponse.getResponseHeader().indexOf(HEADER_PARAMS_NAME, 0);
        NamedList header = null;
        Object o = m_queryResponse.getResponseHeader().getVal(paramsIndex);
        if (o instanceof NamedList) {
            header = (NamedList)o;
            header.setVal(header.indexOf(CommonParams.ROWS, 0), m_rows);
            header.setVal(header.indexOf(CommonParams.START, 0), getStart());
        }

        // create and initialize the solr request
        m_solrQueryRequest = new LocalSolrQueryRequest(core, m_queryResponse.getResponseHeader());
        // set the OpenCms Solr query as parameters to the request
        m_solrQueryRequest.setParams(m_query);

        // perform the highlighting
        SearchComponent highlightComponenet = core.getSearchComponent("highlight");
        if ((header != null) && (m_query.getHighlight()) && (highlightComponenet != null)) {
            header.add(HighlightParams.HIGHLIGHT, "on");
            if ((m_query.getHighlightFields() != null) && (m_query.getHighlightFields().length > 0)) {
                header.add(HighlightParams.FIELDS, CmsStringUtil.arrayAsString(m_query.getHighlightFields(), ","));
            }
            String formatter = m_query.getParams(HighlightParams.FORMATTER) != null
            ? m_query.getParams(HighlightParams.FORMATTER)[0]
            : null;
            if (formatter != null) {
                header.add(HighlightParams.FORMATTER, formatter);
            }
            if (m_query.getHighlightFragsize() != 100) {
                header.add(HighlightParams.FRAGSIZE, new Integer(m_query.getHighlightFragsize()));
            }
            if (m_query.getHighlightRequireFieldMatch()) {
                header.add(HighlightParams.FIELD_MATCH, new Boolean(m_query.getHighlightRequireFieldMatch()));
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_query.getHighlightSimplePost())) {
                header.add(HighlightParams.SIMPLE_POST, m_query.getHighlightSimplePost());
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_query.getHighlightSimplePre())) {
                header.add(HighlightParams.SIMPLE_PRE, m_query.getHighlightSimplePre());
            }
            if (m_query.getHighlightSnippets() != 1) {
                header.add(HighlightParams.SNIPPETS, new Integer(m_query.getHighlightSnippets()));
            }
            ResponseBuilder rb = new ResponseBuilder(
                m_solrQueryRequest,
                m_solrQueryResponse,
                Collections.singletonList(highlightComponenet));
            try {
                rb.doHighlights = true;
                DocListAndSet res = new DocListAndSet();
                res.docList = solrDocumentListToDocList(m_resultDocuments);
                rb.setResults(res);
                rb.setQuery(QParser.getParser(getQuery().getQuery(), null, m_solrQueryRequest).getQuery());
                rb.setQueryString(getQuery().getQuery());
                highlightComponenet.prepare(rb);
                highlightComponenet.process(rb);
            } catch (Exception e) {
                LOG.error(e);
            }
            m_highlightEndTime = System.currentTimeMillis();
        }
    }

    /**
     * Converts a List of Solr documents into a DocList based on lucene internal ids.<p>
     * 
     * @param list the list of Solr documents to convert
     * 
     * @return the doc list with Lucene ids
     * 
     * @throws IOException if something goes wrong
     */
    private DocList solrDocumentListToDocList(SolrDocumentList list) throws IOException {

        SchemaField idField = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema().getUniqueKeyField();

        int[] luceneIds = new int[m_rows.intValue()];
        int docs = 0;
        for (SolrDocument doc : list) {
            String idString = (String)doc.getFirstValue(I_CmsSearchField.FIELD_ID);
            int id = m_solrQueryRequest.getSearcher().getFirstMatch(
                new Term(idField.getName(), idField.getType().toInternal(idString)));
            luceneIds[docs++] = id;
        }
        return new DocSlice(0, docs, luceneIds, null, docs, 0);
    }
}
