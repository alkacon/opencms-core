
package org.opencms.search.solr;

import org.opencms.file.CmsResource;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.I_CmsSearchDocument;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Encapsulates a list of 'OpenCms resource documents' ({@link CmsSearchResource}).<p>
 * 
 * This list can be accessed exactly like an {@link ArrayList} which entries are 
 * {@link CmsSearchResource} that extend {@link CmsResource} and holds the Solr 
 * implementation of {@link I_CmsSearchDocument} as member. <b>This enables you to deal 
 * with the resulting list as you do with well known {@link List} and work on it's entries
 * like you do on {@link CmsResource}.</b><p>
 * 
 * @since 8.5.0
 */
public class CmsSolrResultList extends ArrayList<CmsSearchResource> {

    /** The name of the key that is used for the result documents inside the Solr query response. */
    private static final String QUERY_RESPONSE_NAME = "response";

    /** The name of the key that is used for the query time. */
    private static final String QUERY_TIME_NAME = "QTime";

    /** The serial version UID. */
    private static final long serialVersionUID = 707475894827620542L;

    /** The end index of documents to display (start + rows). */
    private int m_end;

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
     */
    @SuppressWarnings("unchecked")
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
        long startTime) {

        super();

        Integer queryTime = new Integer(new Long(System.currentTimeMillis() - startTime).intValue());

        m_rows = rows;
        m_end = end;
        m_page = page;
        m_visibleHitCount = visibleHitCount;

        m_query = query;

        m_resultDocuments = resultDocuments;
        m_resultDocuments.setStart(start);
        m_resultDocuments.setMaxScore(maxScore);
        m_resultDocuments.setNumFound(queryResponse.getResults().getNumFound());

        m_queryResponse = queryResponse;
        m_queryResponse.getResponse().setVal(
            m_queryResponse.getResponse().indexOf(QUERY_RESPONSE_NAME, 0),
            m_resultDocuments);
        m_queryResponse.getResponseHeader().setVal(
            m_queryResponse.getResponseHeader().indexOf(QUERY_TIME_NAME, 0),
            queryTime);

        addAll(resourceDocumentList);
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
     * Returns the query response.<p>
     *  
     * @return the query response
     */
    public final QueryResponse getQueryResponse() {

        return m_queryResponse;
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

        return m_resultDocuments.getStart();
    }

    /**
     * Returns the visible hit count.<p>
     * 
     * @return the visible count of documents
     */
    public long getVisibleHitCount() {

        return m_visibleHitCount;
    }
}
