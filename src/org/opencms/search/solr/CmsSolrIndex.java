/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.CmsSearchResultList;
import org.opencms.search.I_CmsIndexWriter;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.search.galleries.CmsGallerySearchResultList;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.ReplicationHandler;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.QParser;
import org.apache.solr.util.FastWriter;

/**
 * Implements the search within an Solr index.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrIndex extends CmsSearchIndex {

    /** The name of the default Solr Offline index. */
    public static final String DEFAULT_INDEX_NAME_OFFLINE = "Solr Offline";

    /** The name of the default Solr Online index. */
    public static final String DEFAULT_INDEX_NAME_ONLINE = "Solr Online";

    /** Constant for additional parameter to set the post processor class name. */
    public static final String POST_PROCESSOR = "search.solr.postProcessor";

    /** The solr exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_SOLR = "solr";

    /** Indicates the maximum number of documents from the complete result set to return. */
    public static final int ROWS_MAX = 50;

    /** A constant for debug formatting output. */
    protected static final int DEBUG_PADDING_RIGHT = 50;

    /** The name for the parameters key of the response header. */
    private static final String HEADER_PARAMS_NAME = "params";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrIndex.class);

    /** Pseudo resource used for not permission checked indexes. */
    private static final CmsResource PSEUDO_RES = new CmsResource(
        null,
        null,
        null,
        0,
        false,
        0,
        null,
        null,
        0L,
        null,
        0L,
        null,
        0L,
        0L,
        0,
        0,
        0L,
        0);

    /** The name of the key that is used for the result documents inside the Solr query response. */
    private static final String QUERY_RESPONSE_NAME = "response";

    /** The name of the key that is used for the query time. */
    private static final String QUERY_TIME_NAME = "QTime";

    /** A constant for UTF-8 charset. */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** The embedded Solr client for this index. */
    SolrClient m_solr;

    /** The post document manipulator. */
    private I_CmsSolrPostSearchProcessor m_postProcessor;

    /** The core name for the index. */
    private String m_coreName;

    /**
     * Default constructor.<p>
     */
    public CmsSolrIndex() {

        super();
    }

    /**
     * Public constructor to create a Solr index.<p>
     *
     * @param name the name for this index.<p>
     *
     * @throws CmsIllegalArgumentException if something goes wrong
     */
    public CmsSolrIndex(String name)
    throws CmsIllegalArgumentException {

        super(name);
    }

    /**
     * Returns the resource type for the given root path.<p>
     *
     * @param cms the current CMS context
     * @param rootPath the root path of the resource to get the type for
     *
     * @return the resource type for the given root path
     */
    public static final String getType(CmsObject cms, String rootPath) {

        String type = null;
        CmsSolrIndex index = CmsSearchManager.getIndexSolr(cms, null);
        if (index != null) {
            I_CmsSearchDocument doc = index.getDocument(CmsSearchField.FIELD_PATH, rootPath);
            if (doc != null) {
                type = doc.getFieldValueAsString(CmsSearchField.FIELD_TYPE);
            }
        }
        return type;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String key, String value) {

        if (POST_PROCESSOR.equals(key)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                try {
                    setPostProcessor((I_CmsSolrPostSearchProcessor)Class.forName(value).newInstance());
                } catch (Exception e) {
                    CmsException ex = new CmsException(
                        Messages.get().container(Messages.LOG_SOLR_ERR_POST_PROCESSOR_NOT_EXIST_1, value),
                        e);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        super.addConfigurationParameter(key, value);
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#createEmptyDocument(org.opencms.file.CmsResource)
     */
    @Override
    public I_CmsSearchDocument createEmptyDocument(CmsResource resource) {

        CmsSolrDocument doc = new CmsSolrDocument(new SolrInputDocument());
        doc.setId(resource.getStructureId());
        return doc;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#createIndexWriter(boolean, org.opencms.report.I_CmsReport)
     */
    @Override
    public I_CmsIndexWriter createIndexWriter(boolean create, I_CmsReport report) {

        return new CmsSolrIndexWriter(m_solr, this);
    }

    /**
     * Performs a search with according to the gallery search parameters.<p>
     *
     * @param cms the cms context
     * @param params the search parameters
     *
     * @return the search result
     */
    public CmsGallerySearchResultList gallerySearch(CmsObject cms, CmsGallerySearchParameters params) {

        CmsGallerySearchResultList resultList = new CmsGallerySearchResultList();

        try {
            CmsSolrResultList list = search(
                cms,
                params.getQuery(cms),
                false,
                null,
                true,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);

            if (null == list) {
                return null;
            }

            resultList.setHitCount(Long.valueOf(list.getNumFound()).intValue());
            for (CmsSearchResource resource : list) {
                I_CmsSearchDocument document = resource.getDocument();
                Locale locale = CmsLocaleManager.getLocale(params.getLocale());

                CmsGallerySearchResult result = new CmsGallerySearchResult(
                    document,
                    cms,
                    (int)document.getScore(),
                    locale);

                resultList.add(result);
            }
        } catch (CmsSearchException e) {
            LOG.error(e.getMessage(), e);
        }
        return resultList;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = super.getConfiguration();
        if (getPostProcessor() != null) {
            result.put(POST_PROCESSOR, getPostProcessor().getClass().getName());
        }
        return result;
    }

    /**
     * Returns the name of the core of the index.
     * NOTE: Index and core name differ since OpenCms 10.5 due to new naming rules for cores in SOLR.
     *
     * @return the name of the core of the index.
     */
    public String getCoreName() {

        return m_coreName;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#getDocument(java.lang.String, java.lang.String)
     */
    @Override
    public synchronized I_CmsSearchDocument getDocument(String fieldname, String term) {

        try {
            SolrQuery query = new SolrQuery();
            if (CmsSearchField.FIELD_PATH.equals(fieldname)) {
                query.setQuery(fieldname + ":\"" + term + "\"");
            } else {
                query.setQuery(fieldname + ":" + term);
            }
            QueryResponse res = m_solr.query(query);
            if (res != null) {
                SolrDocumentList sdl = m_solr.query(query).getResults();
                if ((sdl.getNumFound() > 0L) && (sdl.get(0) != null)) {
                    return new CmsSolrDocument(sdl.get(0));
                }
            }
        } catch (Exception e) {
            // ignore and assume that the document could not be found
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#getDocumentFactory(org.opencms.file.CmsResource)
     */
    @Override
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if (isIndexing(res)) {
            if (OpenCms.getResourceManager().getResourceType(res) instanceof CmsResourceTypeXmlContainerPage) {
                return OpenCms.getSearchManager().getDocumentFactory(
                    CmsSolrDocumentContainerPage.TYPE_CONTAINERPAGE_SOLR,
                    "text/html");
            }
            if (CmsResourceTypeXmlContent.isXmlContent(res)) {
                return OpenCms.getSearchManager().getDocumentFactory(
                    CmsSolrDocumentXmlContent.TYPE_XMLCONTENT_SOLR,
                    "text/html");
            } else {
                return super.getDocumentFactory(res);
            }
        }
        return null;
    }

    /**
     * Returns the language locale for the given resource in this index.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the resource to check
     * @param availableLocales a list of locales supported by the resource
     *
     * @return the language locale for the given resource in this index
     */
    @Override
    public Locale getLocaleForResource(CmsObject cms, CmsResource resource, List<Locale> availableLocales) {

        Locale result = null;
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
        if ((availableLocales != null) && (availableLocales.size() > 0)) {
            result = OpenCms.getLocaleManager().getBestMatchingLocale(
                defaultLocales.get(0),
                defaultLocales,
                availableLocales);
        }
        if (result == null) {
            result = ((availableLocales != null) && availableLocales.isEmpty())
            ? availableLocales.get(0)
            : defaultLocales.get(0);
        }
        return result;
    }

    /**
     * Returns the search post processor.<p>
     *
     * @return the post processor to use
     */
    public I_CmsSolrPostSearchProcessor getPostProcessor() {

        return m_postProcessor;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#initialize()
     */
    @Override
    public void initialize() throws CmsSearchException {

        super.initialize();
        getFieldConfiguration().init();
        try {
            OpenCms.getSearchManager().registerSolrIndex(this);
        } catch (CmsConfigurationException ex) {
            LOG.error(ex.getMessage(), ex);
            setEnabled(false);
        }
    }

    /** Returns a flag, indicating if the Solr server is not yet set.
     * @return a flag, indicating if the Solr server is not yet set.
     */
    public boolean isNoSolrServerSet() {

        return null == m_solr;
    }

    /**
     * Not yet implemented for Solr.<p>
     *
     * <code>
     * #################<br>
     * ### DON'T USE ###<br>
     * #################<br>
     * </code>
     *
     * @Deprecated Use {@link #search(CmsObject, SolrQuery)} or {@link #search(CmsObject, String)} instead
     */
    @Override
    @Deprecated
    public synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) {

        throw new UnsupportedOperationException();
    }

    /**
     * Default search method.<p>
     *
     * @param cms the current CMS object
     * @param query the query
     *
     * @return the results
     *
     * @throws CmsSearchException if something goes wrong
     *
     * @see #search(CmsObject, String)
     */
    public CmsSolrResultList search(CmsObject cms, CmsSolrQuery query) throws CmsSearchException {

        return search(cms, query, false);
    }

    /**
     * Performs a search.<p>
     *
     * Returns a list of 'OpenCms resource documents'
     * ({@link CmsSearchResource}) encapsulated within the class  {@link CmsSolrResultList}.
     * This list can be accessed exactly like an {@link List} which entries are
     * {@link CmsSearchResource} that extend {@link CmsResource} and holds the Solr
     * implementation of {@link I_CmsSearchDocument} as member. <b>This enables you to deal
     * with the resulting list as you do with well known {@link List} and work on it's entries
     * like you do on {@link CmsResource}.</b>
     *
     * <h4>What will be done with the Solr search result?</h4>
     * <ul>
     * <li>Although it can happen, that there are less results returned than rows were requested
     * (imagine an index containing less documents than requested rows) we try to guarantee
     * the requested amount of search results and to provide a working pagination with
     * security check.</li>
     *
     * <li>To be sure we get enough documents left even the permission check reduces the amount
     * of found documents, the rows are multiplied by <code>'5'</code> and the current page
     * additionally the offset is added. The count of documents we don't have enough
     * permissions for grows with increasing page number, that's why we also multiply
     * the rows by the current page count.</li>
     *
     * <li>Also make sure we perform the permission check for all found documents, so start with
     * the first found doc.</li>
     * </ul>
     *
     * <b>NOTE:</b> If latter pages than the current one are containing protected documents the
     * total hit count will be incorrect, because the permission check ends if we have
     * enough results found for the page to display. With other words latter pages than
     * the current can contain documents that will first be checked if those pages are
     * requested to be displayed, what causes a incorrect hit count.<p>
     *
     * @param cms the current OpenCms context
     * @param ignoreMaxRows <code>true</code> to return all all requested rows, <code>false</code> to use max rows
     * @param query the OpenCms Solr query
     *
     * @return the list of found documents
     *
     * @throws CmsSearchException if something goes wrong
     *
     * @see org.opencms.search.solr.CmsSolrResultList
     * @see org.opencms.search.CmsSearchResource
     * @see org.opencms.search.I_CmsSearchDocument
     * @see org.opencms.search.solr.CmsSolrQuery
     */
    public CmsSolrResultList search(CmsObject cms, final CmsSolrQuery query, boolean ignoreMaxRows)
    throws CmsSearchException {

        return search(cms, query, ignoreMaxRows, null, false, null);
    }

    /**
     * Like {@link #search(CmsObject, CmsSolrQuery, boolean)}, but additionally a resource filter can be specified.
     * By default, the filter depends on the index.
     *
     * @param cms the current OpenCms context
     * @param ignoreMaxRows <code>true</code> to return all all requested rows, <code>false</code> to use max rows
     * @param query the OpenCms Solr query
     * @param filter the resource filter to use for post-processing.
     *
     * @return the list of documents found.
     *
     * @throws CmsSearchException if something goes wrong
     */
    public CmsSolrResultList search(
        CmsObject cms,
        final CmsSolrQuery query,
        boolean ignoreMaxRows,
        final CmsResourceFilter filter)
    throws CmsSearchException {

        return search(cms, query, ignoreMaxRows, null, false, filter);
    }

    /**
     * Performs the actual search.<p>
     *
     * @param cms the current OpenCms context
     * @param ignoreMaxRows <code>true</code> to return all all requested rows, <code>false</code> to use max rows
     * @param query the OpenCms Solr query
     * @param response the servlet response to write the query result to, may also be <code>null</code>
     * @param ignoreSearchExclude if set to false, only contents with search_exclude unset or "false" will be found - typical for the the non-gallery case
     * @param filter the resource filter to use
     *
     * @return the found documents
     *
     * @throws CmsSearchException if something goes wrong
     *
     * @see #search(CmsObject, CmsSolrQuery, boolean)
     */
    @SuppressWarnings("unchecked")
    public CmsSolrResultList search(
        CmsObject cms,
        final CmsSolrQuery query,
        boolean ignoreMaxRows,
        ServletResponse response,
        boolean ignoreSearchExclude,
        CmsResourceFilter filter)
    throws CmsSearchException {

        // check if the user is allowed to access this index
        checkOfflineAccess(cms);
        if (!ignoreSearchExclude) {
            query.addFilterQuery(CmsSearchField.FIELD_SEARCH_EXCLUDE + ":\"false\"");
        }

        int previousPriority = Thread.currentThread().getPriority();
        long startTime = System.currentTimeMillis();

        // remember the initial query
        SolrQuery initQuery = query.clone();

        query.setHighlight(false);
        LocalSolrQueryRequest solrQueryRequest = null;
        try {

            // initialize the search context
            CmsObject searchCms = OpenCms.initCmsObject(cms);

            // change thread priority in order to reduce search impact on overall system performance
            if (getPriority() > 0) {
                Thread.currentThread().setPriority(getPriority());
            }

            // the lists storing the found documents that will be returned
            List<CmsSearchResource> resourceDocumentList = new ArrayList<CmsSearchResource>();
            SolrDocumentList solrDocumentList = new SolrDocumentList();

            // Initialize rows, offset, end and the current page.
            int rows = query.getRows() != null ? query.getRows().intValue() : CmsSolrQuery.DEFAULT_ROWS.intValue();
            if (!ignoreMaxRows && (rows > ROWS_MAX)) {
                rows = ROWS_MAX;
            }
            int start = query.getStart() != null ? query.getStart().intValue() : 0;
            int end = start + rows;
            int page = 0;
            if (rows > 0) {
                page = Math.round(start / rows) + 1;
            }

            // set the start to '0' and expand the rows before performing the query
            query.setStart(new Integer(0));
            query.setRows(new Integer((5 * rows * page) + start));

            // perform the Solr query and remember the original Solr response
            QueryResponse queryResponse = m_solr.query(query);
            long solrTime = System.currentTimeMillis() - startTime;

            // initialize the counts
            long hitCount = queryResponse.getResults().getNumFound();
            start = -1;
            end = -1;
            if ((rows > 0) && (page > 0) && (hitCount > 0)) {
                // calculate the final size of the search result
                start = rows * (page - 1);
                end = start + rows;
                // ensure that both i and n are inside the range of foundDocuments.size()
                start = new Long((start > hitCount) ? hitCount : start).intValue();
                end = new Long((end > hitCount) ? hitCount : end).intValue();
            } else {
                // return all found documents in the search result
                start = 0;
                end = new Long(hitCount).intValue();
            }
            long visibleHitCount = hitCount;
            float maxScore = 0;

            // If we're using a postprocessor, (re-)initialize it before using it
            if (m_postProcessor != null) {
                m_postProcessor.init();
            }

            // process found documents
            List<CmsSearchResource> allDocs = new ArrayList<CmsSearchResource>();
            int cnt = 0;
            for (int i = 0; (i < queryResponse.getResults().size()) && (cnt < end); i++) {
                try {
                    SolrDocument doc = queryResponse.getResults().get(i);
                    CmsSolrDocument searchDoc = new CmsSolrDocument(doc);
                    if (needsPermissionCheck(searchDoc)) {
                        // only if the document is an OpenCms internal resource perform the permission check
                        CmsResource resource = filter == null
                        ? getResource(searchCms, searchDoc)
                        : getResource(searchCms, searchDoc, filter);
                        if (resource != null) {
                            // permission check performed successfully: the user has read permissions!
                            if (cnt >= start) {
                                if (m_postProcessor != null) {
                                    doc = m_postProcessor.process(
                                        searchCms,
                                        resource,
                                        (SolrInputDocument)searchDoc.getDocument());
                                }
                                resourceDocumentList.add(new CmsSearchResource(resource, searchDoc));
                                if (null != doc) {
                                    solrDocumentList.add(doc);
                                }
                                maxScore = maxScore < searchDoc.getScore() ? searchDoc.getScore() : maxScore;
                            }
                            allDocs.add(new CmsSearchResource(resource, searchDoc));
                            cnt++;
                        } else {
                            visibleHitCount--;
                        }
                    } else {
                        // if permission check is not required for this index,
                        // add a pseudo resource together with document to the results
                        resourceDocumentList.add(new CmsSearchResource(PSEUDO_RES, searchDoc));
                        solrDocumentList.add(doc);
                        maxScore = maxScore < searchDoc.getScore() ? searchDoc.getScore() : maxScore;
                        cnt++;
                    }
                } catch (Exception e) {
                    // should not happen, but if it does we want to go on with the next result nevertheless
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_SOLR_ERR_RESULT_ITERATION_FAILED_0), e);
                }
            }
            // the last documents were all secret so let's take the last found docs
            if (resourceDocumentList.isEmpty() && (allDocs.size() > 0)) {
                page = Math.round(allDocs.size() / rows) + 1;
                int showCount = allDocs.size() % rows;
                showCount = showCount == 0 ? rows : showCount;
                start = allDocs.size() - new Long(showCount).intValue();
                end = allDocs.size();
                if (allDocs.size() > start) {
                    resourceDocumentList = allDocs.subList(start, end);
                    for (CmsSearchResource r : resourceDocumentList) {
                        maxScore = maxScore < r.getDocument().getScore() ? r.getDocument().getScore() : maxScore;
                        solrDocumentList.add(((CmsSolrDocument)r.getDocument()).getSolrDocument());
                    }
                }
            }
            long processTime = System.currentTimeMillis() - startTime - solrTime;

            // create and return the result
            solrDocumentList.setStart(start);
            solrDocumentList.setMaxScore(new Float(maxScore));
            solrDocumentList.setNumFound(visibleHitCount);

            queryResponse.getResponse().setVal(
                queryResponse.getResponse().indexOf(QUERY_RESPONSE_NAME, 0),
                solrDocumentList);

            queryResponse.getResponseHeader().setVal(
                queryResponse.getResponseHeader().indexOf(QUERY_TIME_NAME, 0),
                new Integer(new Long(System.currentTimeMillis() - startTime).intValue()));
            long highlightEndTime = System.currentTimeMillis();
            SolrCore core = m_solr instanceof EmbeddedSolrServer
            ? ((EmbeddedSolrServer)m_solr).getCoreContainer().getCore(getCoreName())
            : null;
            CmsSolrResultList result = null;
            try {
                SearchComponent highlightComponenet = null;
                if (core != null) {
                    highlightComponenet = core.getSearchComponent("highlight");
                    solrQueryRequest = new LocalSolrQueryRequest(core, queryResponse.getResponseHeader());
                }
                SolrQueryResponse solrQueryResponse = null;
                if (solrQueryRequest != null) {
                    // create and initialize the solr response
                    solrQueryResponse = new SolrQueryResponse();
                    solrQueryResponse.setAllValues(queryResponse.getResponse());
                    int paramsIndex = queryResponse.getResponseHeader().indexOf(HEADER_PARAMS_NAME, 0);
                    NamedList<Object> header = null;
                    Object o = queryResponse.getResponseHeader().getVal(paramsIndex);
                    if (o instanceof NamedList) {
                        header = (NamedList<Object>)o;
                        header.setVal(header.indexOf(CommonParams.ROWS, 0), new Integer(rows));
                        header.setVal(header.indexOf(CommonParams.START, 0), new Long(start));
                    }

                    // set the OpenCms Solr query as parameters to the request
                    solrQueryRequest.setParams(initQuery);

                    // perform the highlighting
                    if ((header != null) && (initQuery.getHighlight()) && (highlightComponenet != null)) {
                        header.add(HighlightParams.HIGHLIGHT, "on");
                        if ((initQuery.getHighlightFields() != null) && (initQuery.getHighlightFields().length > 0)) {
                            header.add(
                                HighlightParams.FIELDS,
                                CmsStringUtil.arrayAsString(initQuery.getHighlightFields(), ","));
                        }
                        String formatter = initQuery.getParams(HighlightParams.FORMATTER) != null
                        ? initQuery.getParams(HighlightParams.FORMATTER)[0]
                        : null;
                        if (formatter != null) {
                            header.add(HighlightParams.FORMATTER, formatter);
                        }
                        if (initQuery.getHighlightFragsize() != 100) {
                            header.add(HighlightParams.FRAGSIZE, new Integer(initQuery.getHighlightFragsize()));
                        }
                        if (initQuery.getHighlightRequireFieldMatch()) {
                            header.add(
                                HighlightParams.FIELD_MATCH,
                                new Boolean(initQuery.getHighlightRequireFieldMatch()));
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(initQuery.getHighlightSimplePost())) {
                            header.add(HighlightParams.SIMPLE_POST, initQuery.getHighlightSimplePost());
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(initQuery.getHighlightSimplePre())) {
                            header.add(HighlightParams.SIMPLE_PRE, initQuery.getHighlightSimplePre());
                        }
                        if (initQuery.getHighlightSnippets() != 1) {
                            header.add(HighlightParams.SNIPPETS, new Integer(initQuery.getHighlightSnippets()));
                        }
                        ResponseBuilder rb = new ResponseBuilder(
                            solrQueryRequest,
                            solrQueryResponse,
                            Collections.singletonList(highlightComponenet));
                        try {
                            rb.doHighlights = true;
                            DocListAndSet res = new DocListAndSet();
                            SchemaField idField = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema().getUniqueKeyField();

                            int[] luceneIds = new int[rows];
                            int docs = 0;
                            for (SolrDocument doc : solrDocumentList) {
                                String idString = (String)doc.getFirstValue(CmsSearchField.FIELD_ID);
                                int id = solrQueryRequest.getSearcher().getFirstMatch(
                                    new Term(idField.getName(), idField.getType().toInternal(idString)));
                                luceneIds[docs++] = id;
                            }
                            res.docList = new DocSlice(0, docs, luceneIds, null, docs, 0);
                            rb.setResults(res);
                            rb.setQuery(QParser.getParser(initQuery.getQuery(), null, solrQueryRequest).getQuery());
                            rb.setQueryString(initQuery.getQuery());
                            highlightComponenet.prepare(rb);
                            highlightComponenet.process(rb);
                            highlightComponenet.finishStage(rb);
                        } catch (Exception e) {
                            LOG.error(e.getMessage() + " in query: " + initQuery, new Exception(e));
                        }

                        // Make highlighting also available via the CmsSolrResultList
                        queryResponse.setResponse(solrQueryResponse.getValues());

                        highlightEndTime = System.currentTimeMillis();
                    }
                }

                result = new CmsSolrResultList(
                    initQuery,
                    queryResponse,
                    solrDocumentList,
                    resourceDocumentList,
                    start,
                    new Integer(rows),
                    end,
                    page,
                    visibleHitCount,
                    new Float(maxScore),
                    startTime,
                    highlightEndTime);
                if (LOG.isDebugEnabled()) {
                    Object[] logParams = new Object[] {
                        new Long(System.currentTimeMillis() - startTime),
                        new Long(result.getNumFound()),
                        new Long(solrTime),
                        new Long(processTime),
                        new Long(result.getHighlightEndTime() != 0 ? result.getHighlightEndTime() - startTime : 0)};
                    LOG.debug(
                        query.toString()
                            + "\n"
                            + Messages.get().getBundle().key(Messages.LOG_SOLR_SEARCH_EXECUTED_5, logParams));
                }
                if (response != null) {
                    writeResp(response, solrQueryRequest, solrQueryResponse);
                }
            } finally {
                if (solrQueryRequest != null) {
                    solrQueryRequest.close();
                }
                if (core != null) {
                    core.close();
                }
            }
            return result;
        } catch (Exception e) {
            throw new CmsSearchException(
                Messages.get().container(
                    Messages.LOG_SOLR_ERR_SEARCH_EXECUTION_FAILD_1,
                    CmsEncoder.decode(query.toString()),
                    e),
                e);
        } finally {
            if (solrQueryRequest != null) {
                solrQueryRequest.close();
            }
            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);
        }

    }

    /**
     * Default search method.<p>
     *
     * @param cms the current CMS object
     * @param query the query
     *
     * @return the results
     *
     * @throws CmsSearchException if something goes wrong
     *
     * @see #search(CmsObject, String)
     */
    public CmsSolrResultList search(CmsObject cms, SolrQuery query) throws CmsSearchException {

        return search(cms, CmsEncoder.decode(query.toString()));
    }

    /**
     * Performs a search.<p>
     *
     * @param cms the cms object
     * @param solrQuery the Solr query
     *
     * @return a list of documents
     *
     * @throws CmsSearchException if something goes wrong
     *
     * @see #search(CmsObject, CmsSolrQuery, boolean)
     */
    public CmsSolrResultList search(CmsObject cms, String solrQuery) throws CmsSearchException {

        return search(cms, new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(solrQuery)), false);
    }

    /**
     * Writes the response into the writer.<p>
     *
     * NOTE: Currently not available for HTTP server.<p>
     *
     * @param response the servlet response
     * @param cms the CMS object to use for search
     * @param query the Solr query
     * @param ignoreMaxRows if to return unlimited results
     *
     * @throws Exception if there is no embedded server
     */
    public void select(ServletResponse response, CmsObject cms, CmsSolrQuery query, boolean ignoreMaxRows)
    throws Exception {

        boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
        CmsResourceFilter filter = isOnline ? null : CmsResourceFilter.IGNORE_EXPIRATION;

        search(cms, query, ignoreMaxRows, response, false, filter);
    }

    /**
     * Sets the logical key/name of this search index.<p>
     *
     * @param name the logical key/name of this search index
     *
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index
     */
    @Override
    public void setName(String name) throws CmsIllegalArgumentException {

        super.setName(name);
        updateCoreName();
    }

    /**
     * Sets the search post processor.<p>
     *
     * @param postProcessor the search post processor to set
     */
    public void setPostProcessor(I_CmsSolrPostSearchProcessor postProcessor) {

        m_postProcessor = postProcessor;
    }

    /**
     * Sets the Solr server used by this index.<p>
     *
     * @param client the server to set
     */
    public void setSolrServer(SolrClient client) {

        m_solr = client;
    }

    /**
     * Executes a spell checking Solr query and returns the Solr query response.<p>
     *
     * @param res the servlet response
     * @param cms the CMS object
     * @param q the query
     *
     * @throws CmsSearchException if something goes wrong
     */
    public void spellCheck(ServletResponse res, CmsObject cms, CmsSolrQuery q) throws CmsSearchException {

        SolrCore core = null;
        LocalSolrQueryRequest solrQueryRequest = null;
        try {
            q.setRequestHandler("/spell");

            QueryResponse queryResponse = m_solr.query(q);

            List<CmsSearchResource> resourceDocumentList = new ArrayList<CmsSearchResource>();
            SolrDocumentList solrDocumentList = new SolrDocumentList();
            if (m_postProcessor != null) {
                for (int i = 0; (i < queryResponse.getResults().size()); i++) {
                    try {
                        SolrDocument doc = queryResponse.getResults().get(i);
                        CmsSolrDocument searchDoc = new CmsSolrDocument(doc);
                        if (needsPermissionCheck(searchDoc)) {
                            // only if the document is an OpenCms internal resource perform the permission check
                            CmsResource resource = getResource(cms, searchDoc);
                            if (resource != null) {
                                // permission check performed successfully: the user has read permissions!
                                if (m_postProcessor != null) {
                                    doc = m_postProcessor.process(
                                        cms,
                                        resource,
                                        (SolrInputDocument)searchDoc.getDocument());
                                }
                                resourceDocumentList.add(new CmsSearchResource(resource, searchDoc));
                                solrDocumentList.add(doc);
                            }
                        }
                    } catch (Exception e) {
                        // should not happen, but if it does we want to go on with the next result nevertheless
                        LOG.warn(Messages.get().getBundle().key(Messages.LOG_SOLR_ERR_RESULT_ITERATION_FAILED_0), e);
                    }
                }
                queryResponse.getResponse().setVal(
                    queryResponse.getResponse().indexOf(QUERY_RESPONSE_NAME, 0),
                    solrDocumentList);
            }

            // create and return the result
            core = m_solr instanceof EmbeddedSolrServer
            ? ((EmbeddedSolrServer)m_solr).getCoreContainer().getCore(getCoreName())
            : null;

            SolrQueryResponse solrQueryResponse = new SolrQueryResponse();
            solrQueryResponse.setAllValues(queryResponse.getResponse());

            // create and initialize the solr request
            solrQueryRequest = new LocalSolrQueryRequest(core, solrQueryResponse.getResponseHeader());
            // set the OpenCms Solr query as parameters to the request
            solrQueryRequest.setParams(q);

            writeResp(res, solrQueryRequest, solrQueryResponse);

        } catch (Exception e) {
            throw new CmsSearchException(
                Messages.get().container(Messages.LOG_SOLR_ERR_SEARCH_EXECUTION_FAILD_1, q),
                e);
        } finally {
            if (solrQueryRequest != null) {
                solrQueryRequest.close();
            }
            if (core != null) {
                core.close();
            }
        }
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#createIndexBackup()
     */
    @Override
    protected String createIndexBackup() {

        if (!isBackupReindexing()) {
            // if no backup is generated we don't need to do anything
            return null;
        }
        if (m_solr instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer ser = (EmbeddedSolrServer)m_solr;
            CoreContainer con = ser.getCoreContainer();
            SolrCore core = con.getCore(getCoreName());
            if (core != null) {
                try {
                    SolrRequestHandler h = core.getRequestHandler("/replication");
                    if (h instanceof ReplicationHandler) {
                        h.handleRequest(
                            new LocalSolrQueryRequest(core, CmsRequestUtil.createParameterMap("?command=backup")),
                            new SolrQueryResponse());
                    }
                } finally {
                    core.close();
                }
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#excludeFromIndex(CmsObject, CmsResource)
     */
    @Override
    protected boolean excludeFromIndex(CmsObject cms, CmsResource resource) {

        if (resource.isFolder() || resource.isTemporaryFile()) {
            // don't index  folders or temporary files for galleries, but pretty much everything else
            return true;
        }
        return false;

    }

    /**
     * @see org.opencms.search.CmsSearchIndex#indexSearcherClose()
     */
    @SuppressWarnings("sync-override")
    @Override
    protected void indexSearcherClose() {

        // nothing to do here
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#indexSearcherOpen(java.lang.String)
     */
    @SuppressWarnings("sync-override")
    @Override
    protected void indexSearcherOpen(final String path) {

        // nothing to do here
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#indexSearcherUpdate()
     */
    @SuppressWarnings("sync-override")
    @Override
    protected void indexSearcherUpdate() {

        // nothing to do here
    }

    /**
     * Checks if the given resource should be indexed by this index or not.<p>
     *
     * @param res the resource candidate
     *
     * @return <code>true</code> if the given resource should be indexed or <code>false</code> if not
     */
    protected boolean isIndexing(CmsResource res) {

        if ((res != null) && (getSources() != null)) {
            I_CmsDocumentFactory result = OpenCms.getSearchManager().getDocumentFactory(res);
            for (CmsSearchIndexSource source : getSources()) {
                if (source.isIndexing(res.getRootPath(), CmsSolrDocumentContainerPage.TYPE_CONTAINERPAGE_SOLR)
                    || source.isIndexing(res.getRootPath(), CmsSolrDocumentXmlContent.TYPE_XMLCONTENT_SOLR)
                    || source.isIndexing(res.getRootPath(), result.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the current user is allowed to access non-online indexes.<p>
     *
     * To access non-online indexes the current user must be a workplace user at least.<p>
     *
     * @param cms the CMS object initialized with the current request context / user
     *
     * @throws CmsSearchException thrown if the access is not permitted
     */
    private void checkOfflineAccess(CmsObject cms) throws CmsSearchException {

        // If an offline index is being selected, check permissions
        if (!CmsProject.ONLINE_PROJECT_NAME.equals(getProject())) {
            // only if the user has the role Workplace user, he is allowed to access the Offline index
            try {
                OpenCms.getRoleManager().checkRole(cms, CmsRole.ELEMENT_AUTHOR);
            } catch (CmsRoleViolationException e) {
                throw new CmsSearchException(
                    Messages.get().container(
                        Messages.LOG_SOLR_ERR_SEARCH_PERMISSION_VIOLATION_2,
                        getName(),
                        cms.getRequestContext().getCurrentUser()),
                    e);
            }
        }
    }

    /**
     * Generates a valid core name from the provided name (the index name).
     * @param name the index name.
     * @return the core name
     */
    private String generateCoreName(final String name) {

        if (name != null) {
            //TODO: Add more name manipulations to guarantee a valid core name
            return name.replace(" ", "-");
        }
        return null;
    }

    /**
     * Updates the core name to be in sync with the index name.
     */
    private void updateCoreName() {

        m_coreName = generateCoreName(getName());

    }

    /**
     * Writes the Solr response.<p>
     *
     * @param response the servlet response
     * @param queryRequest the Solr request
     * @param queryResponse the Solr response to write
     *
     * @throws IOException if sth. goes wrong
     * @throws UnsupportedEncodingException if sth. goes wrong
     */
    private void writeResp(ServletResponse response, SolrQueryRequest queryRequest, SolrQueryResponse queryResponse)
    throws IOException, UnsupportedEncodingException {

        if (m_solr instanceof EmbeddedSolrServer) {
            SolrCore core = ((EmbeddedSolrServer)m_solr).getCoreContainer().getCore(getCoreName());
            Writer out = null;
            try {
                QueryResponseWriter responseWriter = core.getQueryResponseWriter(queryRequest);

                final String ct = responseWriter.getContentType(queryRequest, queryResponse);
                if (null != ct) {
                    response.setContentType(ct);
                }

                if (responseWriter instanceof BinaryQueryResponseWriter) {
                    BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter)responseWriter;
                    binWriter.write(response.getOutputStream(), queryRequest, queryResponse);
                } else {
                    String charset = ContentStreamBase.getCharsetFromContentType(ct);
                    out = ((charset == null) || charset.equalsIgnoreCase(UTF8.toString()))
                    ? new OutputStreamWriter(response.getOutputStream(), UTF8)
                    : new OutputStreamWriter(response.getOutputStream(), charset);
                    out = new FastWriter(out);
                    responseWriter.write(out, queryRequest, queryResponse);
                    out.flush();
                }
            } finally {
                core.close();
                if (out != null) {
                    out.close();
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
