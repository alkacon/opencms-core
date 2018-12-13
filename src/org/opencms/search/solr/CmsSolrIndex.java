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
import org.opencms.file.CmsFile;
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
import org.opencms.util.CmsFileUtil;
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
import java.util.stream.Stream;

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

    /** The serial version id. */
    private static final long serialVersionUID = -1570077792574476721L;

    /** The name of the default Solr Offline index. */
    public static final String DEFAULT_INDEX_NAME_OFFLINE = "Solr Offline";

    /** The name of the default Solr Online index. */
    public static final String DEFAULT_INDEX_NAME_ONLINE = "Solr Online";

    /** Constant for additional parameter to set the post processor class name. */
    public static final String POST_PROCESSOR = "search.solr.postProcessor";

    /** Constant for additional parameter to set the fields the select handler should return at maximum. */
    public static final String SOLR_HANDLER_ALLOWED_FIELDS = "handle.solr.allowedFields";

    /** Constant for additional parameter to set the number results the select handler should return at maxium per request. */
    public static final String SOLR_HANDLER_MAX_ALLOWED_RESULTS_PER_PAGE = "handle.solr.maxAllowedResultsPerPage";

    /** Constant for additional parameter to set the maximal number of a result, the select handler should return. */
    public static final String SOLR_HANDLER_MAX_ALLOWED_RESULTS_AT_ALL = "handle.solr.maxAllowedResultsAtAll";

    /** Constant for additional parameter to disable the select handler (except for debug mode). */
    private static final String SOLR_HANDLER_DISABLE_SELECT = "handle.solr.disableSelectHandler";

    /** Constant for additional parameter to set the VFS path to the file holding the debug secret. */
    private static final String SOLR_HANDLER_DEBUG_SECRET_FILE = "handle.solr.debugSecretFile";

    /** Constant for additional parameter to disable the spell handler (except for debug mode). */
    private static final String SOLR_HANDLER_DISABLE_SPELL = "handle.solr.disableSpellHandler";
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

    /** The name of the request parameter holding the debug secret. */
    private static final String REQUEST_PARAM_DEBUG_SECRET = "_debug";

    /** The embedded Solr client for this index. */
    transient SolrClient m_solr;

    /** The post document manipulator. */
    private transient I_CmsSolrPostSearchProcessor m_postProcessor;

    /** The core name for the index. */
    private transient String m_coreName;

    /** The list of allowed fields to return */
    private String[] m_handlerAllowedFields;

    /** The number of maximally allowed results per page when using the handler. */
    private int m_handlerMaxAllowedResultsPerPage = -1;

    /** The number of maximally allowed results at all when using the handler. */
    private int m_handlerMaxAllowedResultsAtAll = -1;

    /** Flag, indicating if the handler only works in debug mode. */
    private boolean m_handlerSelectDisabled;

    /** Path to the secret file. Must be under /system/.../ or /shared/.../ and readable by all users that should be able to debug. */
    private String m_handlerDebugSecretFile;

    /** Flag, indicating if the spellcheck handler is disabled for the index. */
    private boolean m_handlerSpellDisabled;

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

        switch (key) {
            case POST_PROCESSOR:
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
                break;
            case SOLR_HANDLER_ALLOWED_FIELDS:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    m_handlerAllowedFields = Stream.of(value.split(",")).map(v -> v.trim()).toArray(String[]::new);
                }
                break;
            case SOLR_HANDLER_MAX_ALLOWED_RESULTS_PER_PAGE:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    try {
                        m_handlerMaxAllowedResultsPerPage = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        LOG.warn(
                            "Could not parse parameter \""
                                + SOLR_HANDLER_MAX_ALLOWED_RESULTS_PER_PAGE
                                + "\" for index \""
                                + getName()
                                + "\". Results per page will not be restricted.");
                    }
                }
                break;
            case SOLR_HANDLER_MAX_ALLOWED_RESULTS_AT_ALL:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    try {
                        m_handlerMaxAllowedResultsAtAll = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        LOG.warn(
                            "Could not parse parameter \""
                                + SOLR_HANDLER_MAX_ALLOWED_RESULTS_AT_ALL
                                + "\" for index \""
                                + getName()
                                + "\". Results per page will not be restricted.");
                    }
                }
                break;
            case SOLR_HANDLER_DISABLE_SELECT:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    m_handlerSelectDisabled = value.trim().toLowerCase().equals("true");
                }
                break;
            case SOLR_HANDLER_DEBUG_SECRET_FILE:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    m_handlerDebugSecretFile = value.trim();
                }
                break;
            case SOLR_HANDLER_DISABLE_SPELL:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    m_handlerSpellDisabled = value.trim().toLowerCase().equals("true");
                }
                break;
            default:
                super.addConfigurationParameter(key, value);
                break;
        }
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
     * @see org.opencms.search.CmsSearchIndex#excludeFromIndex(CmsObject, CmsResource)
     */
    @Override
    public boolean excludeFromIndex(CmsObject cms, CmsResource resource) {

        if (resource.isFolder() || resource.isTemporaryFile()) {
            // don't index  folders or temporary files for galleries, but pretty much everything else
            return true;
        }
        return false;

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

        return getDocument(fieldname, term, null);
    }

    /**
     * Version of {@link org.opencms.search.CmsSearchIndex#getDocument(java.lang.String, java.lang.String)} where
     * the returned fields can be restricted.
     *
     * @param fieldname the field to query in
     * @param term the query
     * @param fls the returned fields.
     * @return the document.
     */
    public synchronized I_CmsSearchDocument getDocument(String fieldname, String term, String[] fls) {

        try {
            SolrQuery query = new SolrQuery();
            if (CmsSearchField.FIELD_PATH.equals(fieldname)) {
                query.setQuery(fieldname + ":\"" + term + "\"");
            } else {
                query.setQuery(fieldname + ":" + term);
            }
            query.addFilterQuery("{!collapse field=" + fieldname + "}");
            if (null != fls) {
                query.setFields(fls);
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
            I_CmsDocumentFactory defaultFactory = super.getDocumentFactory(res);
            if (null == defaultFactory) {

                if (OpenCms.getResourceManager().getResourceType(res) instanceof CmsResourceTypeXmlContainerPage) {
                    return OpenCms.getSearchManager().getDocumentFactory(
                        CmsSolrDocumentContainerPage.TYPE_CONTAINERPAGE_SOLR,
                        "text/html");
                }
                if (CmsResourceTypeXmlContent.isXmlContent(res)) {
                    return OpenCms.getSearchManager().getDocumentFactory(
                        CmsSolrDocumentXmlContent.TYPE_XMLCONTENT_SOLR,
                        "text/html");
                }
            }
            return defaultFactory;
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
     * @deprecated Use {@link #search(CmsObject, SolrQuery)} or {@link #search(CmsObject, String)} instead
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

            // set the start to '0' and expand the rows before performing the query
            query.setStart(new Integer(0));
            query.setRows(new Integer(5 * (rows + start)));

            // perform the Solr query and remember the original Solr response
            QueryResponse queryResponse = m_solr.query(query);
            long solrTime = System.currentTimeMillis() - startTime;

            // initialize the counts
            long hitCount = queryResponse.getResults().getNumFound();
            if ((rows > 0) && (hitCount > 0)) {
                // ensure that both start and end are inside the range of foundDocuments.size()
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
            // TODO: Is this useful? For the last page?
            // Better way to determine which resources to show in case of page sizes changing?
            if (resourceDocumentList.isEmpty() && (allDocs.size() > 0)) {
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
                                String idString = (String)doc.getFirstValue(CmsSearchField.FIELD_SOLR_ID);
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
                    rows > 0 ? (allDocs.size() / rows) + 1 : 0, //page - but matches only in case of equally sized pages and is zero for rows=0 (because this was this way before!?!)
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

        throwExceptionIfSafetyRestrictionsAreViolated(cms, query, false);
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

        throwExceptionIfSafetyRestrictionsAreViolated(cms, q, true);
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
    @Override
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
     * Checks if the query should be executed using the debug mode where the security restrictions do not apply.
     * @param cms the current context.
     * @param query the query to execute.
     * @return a flag, indicating, if the query should be performed in debug mode.
     */
    private boolean isDebug(CmsObject cms, CmsSolrQuery query) {

        String[] debugSecretValues = query.remove(REQUEST_PARAM_DEBUG_SECRET);
        String debugSecret = (debugSecretValues == null) || (debugSecretValues.length < 1)
        ? null
        : debugSecretValues[0];
        if ((null != debugSecret) && !debugSecret.trim().isEmpty() && (null != m_handlerDebugSecretFile)) {
            try {
                CmsFile secretFile = cms.readFile(m_handlerDebugSecretFile);
                String secret = new String(secretFile.getContents(), CmsFileUtil.getEncoding(cms, secretFile));
                return secret.trim().equals(debugSecret.trim());
            } catch (Exception e) {
                LOG.info(
                    "Failed to read secret file for index \""
                        + getName()
                        + "\" at path \""
                        + m_handlerDebugSecretFile
                        + "\".");
            }
        }
        return false;
    }

    /**
     * Throws an exception if the request can for security reasons not be performed.
     * Security restrictions can be set via parameters of the index.
     *
     * @param cms the current context.
     * @param query the query.
     * @param isSpell flag, indicating if the spellcheck handler is requested.
     * @throws CmsSearchException thrown if the query cannot be executed due to security reasons.
     */
    private void throwExceptionIfSafetyRestrictionsAreViolated(CmsObject cms, CmsSolrQuery query, boolean isSpell)
    throws CmsSearchException {

        if (!isDebug(cms, query)) {
            if (isSpell) {
                if (m_handlerSpellDisabled) {
                    throw new CmsSearchException(Messages.get().container(Messages.GUI_HANDLER_REQUEST_NOT_ALLOWED_0));
                }
            } else {
                if (m_handlerSelectDisabled) {
                    throw new CmsSearchException(Messages.get().container(Messages.GUI_HANDLER_REQUEST_NOT_ALLOWED_0));
                }
                int start = null != query.getStart() ? query.getStart().intValue() : 0;
                int rows = null != query.getRows() ? query.getRows().intValue() : CmsSolrQuery.DEFAULT_ROWS.intValue();
                if ((m_handlerMaxAllowedResultsAtAll >= 0) && ((rows + start) > m_handlerMaxAllowedResultsAtAll)) {
                    throw new CmsSearchException(
                        Messages.get().container(
                            Messages.GUI_HANDLER_TOO_MANY_RESULTS_REQUESTED_AT_ALL_2,
                            Integer.valueOf(m_handlerMaxAllowedResultsAtAll),
                            Integer.valueOf(rows + start)));
                }
                if ((m_handlerMaxAllowedResultsPerPage >= 0) && (rows > m_handlerMaxAllowedResultsPerPage)) {
                    throw new CmsSearchException(
                        Messages.get().container(
                            Messages.GUI_HANDLER_TOO_MANY_RESULTS_REQUESTED_PER_PAGE_2,
                            Integer.valueOf(m_handlerMaxAllowedResultsPerPage),
                            Integer.valueOf(rows)));
                }
                if ((null != m_handlerAllowedFields) && (Stream.of(m_handlerAllowedFields).anyMatch(x -> true))) {
                    if (query.getFields().equals(CmsSolrQuery.ALL_RETURN_FIELDS)) {
                        query.setFields(m_handlerAllowedFields);
                    } else {
                        for (String requestedField : query.getFields().split(",")) {
                            if (Stream.of(m_handlerAllowedFields).noneMatch(
                                allowedField -> allowedField.equals(requestedField))) {
                                throw new CmsSearchException(
                                    Messages.get().container(
                                        Messages.GUI_HANDLER_REQUESTED_FIELD_NOT_ALLOWED_2,
                                        requestedField,
                                        Stream.of(m_handlerAllowedFields).reduce("", (a, b) -> a + "," + b)));
                            }
                        }
                    }
                }
            }
        }
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
