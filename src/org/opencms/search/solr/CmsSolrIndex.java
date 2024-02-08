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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.CmsSearchResultList;
import org.opencms.search.I_CmsIndexWriter;
import org.opencms.search.I_CmsSearchDocument;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.FastWriter;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.ReplicationHandler;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;

import com.google.common.base.Objects;

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

    /**
     * Constant for additional parameter to set the maximally processed results (start + rows) for searches with this index.
     * It overwrites the global configuration from {@link CmsSolrConfiguration#getMaxProcessedResults()} for this index.
    **/
    public static final String SOLR_SEARCH_MAX_PROCESSED_RESULTS = "search.solr.maxProcessedResults";

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

    /** Constant for additional parameter to configure an external solr server specifically for the index. */
    private static final String SOLR_SERVER_URL = "server.url";

    /** The solr exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_SOLR = "solr";

    /** Indicates the maximum number of documents from the complete result set to return. */
    public static final int ROWS_MAX = 50;

    /** The constant for an unlimited maximum number of results to return in a Solr search. */
    public static final int MAX_RESULTS_UNLIMITED = -1;

    /** The constant for an unlimited maximum number of results to return in a Solr search. */
    public static final int MAX_RESULTS_GALLERY = 10000;

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

    /** The name of the key that is used for the query time. */
    private static final String QUERY_HIGHLIGHTING_NAME = "highlighting";

    /** A constant for UTF-8 charset. */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** The name of the request parameter holding the debug secret. */
    private static final String REQUEST_PARAM_DEBUG_SECRET = "_debug";

    /** The name of the query parameter enabling spell checking. */
    private static final String QUERY_SPELLCHECK_NAME = "spellcheck";

    /** The name of the query parameter sorting. */
    private static final String QUERY_SORT_NAME = "sort";

    /** The name of the query parameter expand. */
    private static final String QUERY_PARAM_EXPAND = "expand";

    /** The embedded Solr client for this index. */
    transient SolrClient m_solr;

    /** The post document manipulator. */
    private transient I_CmsSolrPostSearchProcessor m_postProcessor;

    /** The core name for the index. */
    private transient String m_coreName;

    /** The list of allowed fields to return. */
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

    /** The maximal number of results to process for search queries. */
    int m_maxProcessedResults = -2; // special value for not initialized.

    /** Server URL to use specific for the index. If set, it overwrites all other server settings. */
    private String m_serverUrl;

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
            case SOLR_SEARCH_MAX_PROCESSED_RESULTS:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    try {
                        m_maxProcessedResults = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        LOG.warn(
                            "Could not parse parameter \""
                                + SOLR_SEARCH_MAX_PROCESSED_RESULTS
                                + "\" for index \""
                                + getName()
                                + "\". The global configuration will be used instead.");
                    }
                }
                break;
            case SOLR_SERVER_URL:
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                    m_serverUrl = value.trim();
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
        // If this is the default offline index than it is used for gallery search that needs all resources indexed.
        if (this.getName().equals(DEFAULT_INDEX_NAME_OFFLINE)) {
            return false;
        }

        boolean isOnlineIndex = getProject().equals(CmsProject.ONLINE_PROJECT_NAME);
        if (isOnlineIndex && (resource.getDateExpired() <= System.currentTimeMillis())) {
            return true;
        }

        try {
            // do property lookup with folder search
            String propValue = cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE,
                true).getValue();
            if (propValue != null) {
                if (!("false".equalsIgnoreCase(propValue.trim()))) {
                    return true;
                }
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    org.opencms.search.Messages.get().getBundle().key(
                        org.opencms.search.Messages.LOG_UNABLE_TO_READ_PROPERTY_1,
                        resource.getRootPath()));
            }
        }
        if (!USE_ALL_LOCALE.equalsIgnoreCase(getLocale().getLanguage())) {
            // check if any resource default locale has a match with the index locale, if not skip resource
            List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
            Locale match = OpenCms.getLocaleManager().getFirstMatchingLocale(
                Collections.singletonList(getLocale()),
                locales);
            return (match == null);
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
        if (params.isForceEmptyResult()) {
            return resultList;
        }

        try {
            CmsSolrResultList list = search(
                cms,
                params.getQuery(cms),
                false,
                null,
                true,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED,
                MAX_RESULTS_GALLERY); // ignore the maximally searched number of contents.

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
            // We could have more than one document due to serial dates. We only want one arbitrary document per id/path
            query.setRows(Integer.valueOf(1));
            if (null != fls) {
                query.setFields(fls);
            }
            QueryResponse res = m_solr.query(getCoreName(), query);
            if (res != null) {
                SolrDocumentList sdl = m_solr.query(getCoreName(), query).getResults();
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
     * Returns the maximal number of results (start + rows) that are processed for each search query unless another
     * maximum is explicitly specified in {@link #search(CmsObject, CmsSolrQuery, boolean, ServletResponse, boolean, CmsResourceFilter, int)}.
     *
     * @return the maximal number of results (start + rows) that are processed for a search query.
     */
    public int getMaxProcessedResults() {

        return m_maxProcessedResults;
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
     * Returns the Solr server URL to connect to for this specific index, or <code>null</code> if no specific URL is configured.
     * @return the Solr server URL to connect to for this specific index, or <code>null</code> if no specific URL is configured.
     */
    public String getServerUrl() {

        return m_serverUrl;
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#initialize()
     */
    @Override
    public void initialize() throws CmsSearchException {

        super.initialize();
        if (m_maxProcessedResults == -2) {
            m_maxProcessedResults = OpenCms.getSearchManager().getSolrServerConfiguration().getMaxProcessedResults();
        }
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
     * @param query the OpenCms Solr query
     * @param ignoreMaxRows <code>true</code> to return all all requested rows, <code>false</code> to use max rows
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
    public CmsSolrResultList search(
        CmsObject cms,
        final CmsSolrQuery query,
        boolean ignoreMaxRows,
        ServletResponse response,
        boolean ignoreSearchExclude,
        CmsResourceFilter filter)
    throws CmsSearchException {

        return search(cms, query, ignoreMaxRows, response, ignoreSearchExclude, filter, getMaxProcessedResults());
    }

    /**
     * Performs the actual search.<p>
     *
     * To provide for correct permissions two queries are performed and the response is fused from that queries:
     * <ol>
     *  <li>a query for permission checking, where fl, start and rows is adjusted. From this query result we take for the response:
     *      <ul>
     *          <li>facets</li>
     *          <li>spellcheck</li>
     *          <li>suggester</li>
     *          <li>morelikethis</li>
     *          <li>clusters</li>
     *      </ul>
     *  </li>
     *  <li>a query that collects only the resources determined by the first query and performs highlighting. From this query we take for the response:
     *      <li>result</li>
     *      <li>highlighting</li>
     *  </li>
     *</ol>
     *
     * Currently not or only partly supported Solr features are:
     * <ul>
     *  <li>groups</li>
     *  <li>collapse - representatives of the collapsed group might be filtered by the permission check</li>
     *  <li>expand is disabled</li>
     * </ul>
     *
     * @param cms the current OpenCms context
     * @param query the OpenCms Solr query
     * @param ignoreMaxRows <code>true</code> to return all requested rows, <code>false</code> to use max rows
     * @param response the servlet response to write the query result to, may also be <code>null</code>
     * @param ignoreSearchExclude if set to false, only contents with search_exclude unset or "false" will be found - typical for the the non-gallery case
     * @param filter the resource filter to use
     * @param maxNumResults the maximal number of results to search for
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
        CmsResourceFilter filter,
        int maxNumResults)
    throws CmsSearchException {

        CmsSolrResultList result = null;
        long startTime = System.currentTimeMillis();

        // TODO:
        // - fall back to "last found results" if none are present at the "last page"?
        // - deal with cursorMarks?
        // - deal with groups?
        // - deal with result clustering?
        // - remove max score calculation?

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SOLR_DEBUG_ORIGINAL_QUERY_2, query, getName()));
        }

        // change thread priority in order to reduce search impact on overall system performance
        int previousPriority = Thread.currentThread().getPriority();
        if (getPriority() > 0) {
            Thread.currentThread().setPriority(getPriority());
        }

        // check if the user is allowed to access this index
        checkOfflineAccess(cms);

        if (!ignoreSearchExclude) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_SOLR_INFO_ADDING_SEARCH_EXCLUDE_FILTER_FOR_QUERY_2,
                        query,
                        getName()));
            }
            String fqSearchExclude = CmsSearchField.FIELD_SEARCH_EXCLUDE + ":\"false\"";
            query.removeFilterQuery(fqSearchExclude);
            query.addFilterQuery(fqSearchExclude);
        }

        if (CmsProject.ONLINE_PROJECT_NAME.equals(getProject())) {
            query.addFilterQuery(
                "-"
                    + CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE_ONLINE
                    + CmsSearchField.FIELD_DYNAMIC_PROPERTIES
                    + ":\"true\"");
        }

        // get start parameter from the request
        int start = null == query.getStart() ? 0 : query.getStart().intValue();

        // correct negative start values to 0.
        if (start < 0) {
            query.setStart(Integer.valueOf(0));
            start = 0;
        }

        // Adjust the maximal number of results to process in case it is unlimited.
        if (maxNumResults < 0) {
            maxNumResults = Integer.MAX_VALUE;
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_SOLR_INFO_LIMITING_MAX_PROCESSED_RESULTS_3,
                        query,
                        getName(),
                        Integer.valueOf(maxNumResults)));
            }
        }

        // Correct the rows parameter
        // Set the default rows, if rows are not set in the original query.
        int rows = null == query.getRows() ? CmsSolrQuery.DEFAULT_ROWS.intValue() : query.getRows().intValue();

        // Restrict the rows, such that the maximal number of queryable results is not exceeded.
        if ((((rows + start) > maxNumResults) || ((rows + start) < 0))) {
            rows = maxNumResults - start;
        }
        // Restrict the rows to the maximally allowed number, if they should be restricted.
        if (!ignoreMaxRows && (rows > ROWS_MAX)) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_SOLR_INFO_LIMITING_MAX_ROWS_4,
                        new Object[] {query, getName(), Integer.valueOf(rows), Integer.valueOf(ROWS_MAX)}));
            }
            rows = ROWS_MAX;
        }
        // If start is higher than maxNumResults, the rows could be negative here - correct this.
        if (rows < 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_SOLR_INFO_CORRECTING_ROWS_4,
                        new Object[] {query, getName(), Integer.valueOf(rows), Integer.valueOf(0)}));
            }
            rows = 0;
        }
        // Set the corrected rows for the query.
        query.setRows(Integer.valueOf(rows));

        // remove potentially set expand parameter
        if (null != query.getParams(QUERY_PARAM_EXPAND)) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_SOLR_INFO_REMOVING_EXPAND_2, query, getName()));
            query.remove("expand");
        }

        float maxScore = 0;

        LocalSolrQueryRequest solrQueryRequest = null;
        SolrCore core = null;
        String[] sortParamValues = query.getParams(QUERY_SORT_NAME);
        boolean sortByScoreDesc = (null == sortParamValues)
            || (sortParamValues.length == 0)
            || Objects.equal(sortParamValues[0], "score desc");

        try {

            // initialize the search context
            CmsObject searchCms = OpenCms.initCmsObject(cms);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////// QUERY FOR PERMISSION CHECK, FACETS, SPELLCHECK, SUGGESTIONS ///////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Clone the query and keep the original one
            CmsSolrQuery checkQuery = query.clone();
            // Initialize rows, offset, end and the current page.
            int end = start + rows;
            int itemsToCheck = 0 == end ? 0 : Math.max(10, end + (end / 5)); // request 20 percent more, but at least 10 results if permissions are filtered
            // use a set to prevent double entries if multiple check queries are performed.
            Set<String> resultSolrIds = new HashSet<>(rows); // rows are set before definitely.

            // counter for the documents found and accessible
            int cnt = 0;
            long hitCount = 0;
            long visibleHitCount = 0;
            int processedResults = 0;
            long solrPermissionTime = 0;
            // disable highlighting - it's done in the next query.
            checkQuery.setHighlight(false);
            // adjust rows and start for the permission check.
            checkQuery.setRows(Integer.valueOf(Math.min(maxNumResults - processedResults, itemsToCheck)));
            checkQuery.setStart(Integer.valueOf(processedResults));
            // return only the fields required for the permission check and for scoring
            checkQuery.setFields(CmsSearchField.FIELD_TYPE, CmsSearchField.FIELD_SOLR_ID, CmsSearchField.FIELD_PATH);
            List<String> originalFields = Arrays.asList(query.getFields().split(","));
            if (originalFields.contains(CmsSearchField.FIELD_SCORE)) {
                checkQuery.addField(CmsSearchField.FIELD_SCORE);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SOLR_DEBUG_CHECK_QUERY_2, checkQuery, getName()));
            }
            // perform the permission check Solr query and remember the response and time Solr took.
            long solrCheckTime = System.currentTimeMillis();
            QueryResponse checkQueryResponse = m_solr.query(getCoreName(), checkQuery);
            solrCheckTime = System.currentTimeMillis() - solrCheckTime;
            solrPermissionTime += solrCheckTime;

            // initialize the counts
            hitCount = checkQueryResponse.getResults().getNumFound();
            int maxToProcess = Long.valueOf(Math.min(hitCount, maxNumResults)).intValue();
            visibleHitCount = hitCount;

            // process found documents
            for (SolrDocument doc : checkQueryResponse.getResults()) {
                try {
                    CmsSolrDocument searchDoc = new CmsSolrDocument(doc);
                    if (needsPermissionCheck(searchDoc) && !hasPermissions(searchCms, searchDoc, filter)) {
                        visibleHitCount--;
                    } else {
                        if (cnt >= start) {
                            resultSolrIds.add(searchDoc.getFieldValueAsString(CmsSearchField.FIELD_SOLR_ID));
                        }
                        if (sortByScoreDesc && (searchDoc.getScore() > maxScore)) {
                            maxScore = searchDoc.getScore();
                        }
                        if (++cnt >= end) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    // should not happen, but if it does we want to go on with the next result nevertheless
                    visibleHitCount--;
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_SOLR_ERR_RESULT_ITERATION_FAILED_0), e);
                }
            }
            processedResults += checkQueryResponse.getResults().size();

            if ((resultSolrIds.size() < rows) && (processedResults < maxToProcess)) {
                CmsSolrQuery secondCheckQuery = checkQuery.clone();
                // disable all features not necessary, since results are present from the first check query.
                secondCheckQuery.setFacet(false);
                secondCheckQuery.setMoreLikeThis(false);
                secondCheckQuery.set(QUERY_SPELLCHECK_NAME, false);
                do {
                    // query directly more under certain conditions to reduce number of queries
                    itemsToCheck = itemsToCheck < 3000 ? itemsToCheck * 4 : itemsToCheck;
                    // adjust rows and start for the permission check.
                    secondCheckQuery.setRows(
                        Integer.valueOf(
                            Long.valueOf(Math.min(maxToProcess - processedResults, itemsToCheck)).intValue()));
                    secondCheckQuery.setStart(Integer.valueOf(processedResults));

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_SOLR_DEBUG_SECONDCHECK_QUERY_2,
                                secondCheckQuery,
                                getName()));
                    }

                    long solrSecondCheckTime = System.currentTimeMillis();
                    QueryResponse secondCheckQueryResponse = m_solr.query(getCoreName(), secondCheckQuery);
                    processedResults += secondCheckQueryResponse.getResults().size();
                    solrSecondCheckTime = System.currentTimeMillis() - solrSecondCheckTime;
                    solrPermissionTime += solrCheckTime;

                    // process found documents
                    for (SolrDocument doc : secondCheckQueryResponse.getResults()) {
                        try {
                            CmsSolrDocument searchDoc = new CmsSolrDocument(doc);
                            String docSolrId = searchDoc.getFieldValueAsString(CmsSearchField.FIELD_SOLR_ID);
                            if ((needsPermissionCheck(searchDoc) && !hasPermissions(searchCms, searchDoc, filter))
                                || resultSolrIds.contains(docSolrId)) {
                                visibleHitCount--;
                            } else {
                                if (cnt >= start) {
                                    resultSolrIds.add(docSolrId);
                                }
                                if (sortByScoreDesc && (searchDoc.getScore() > maxScore)) {
                                    maxScore = searchDoc.getScore();
                                }
                                if (++cnt >= end) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // should not happen, but if it does we want to go on with the next result nevertheless
                            visibleHitCount--;
                            LOG.warn(
                                Messages.get().getBundle().key(Messages.LOG_SOLR_ERR_RESULT_ITERATION_FAILED_0),
                                e);
                        }
                    }

                } while ((resultSolrIds.size() < rows) && (processedResults < maxToProcess));
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////// QUERY FOR RESULTS AND HIGHLIGHTING ////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // the lists storing the found documents that will be returned
            List<CmsSearchResource> resourceDocumentList = new ArrayList<CmsSearchResource>(resultSolrIds.size());
            SolrDocumentList solrDocumentList = new SolrDocumentList();

            long solrResultTime = 0;

            // If we're using a post-processor, (re-)initialize it before using it
            if (m_postProcessor != null) {
                m_postProcessor.init();
            }

            // build the query for getting the results
            SolrQuery queryForResults = query.clone();
            // we add an additional filter, such that we can only find the documents we want to retrieve, as we figured out in the check query.
            if (!resultSolrIds.isEmpty()) {
                String queryFilterString = resultSolrIds.stream().collect(Collectors.joining(","));
                queryForResults.addFilterQuery(
                    "{!terms f=" + CmsSearchField.FIELD_SOLR_ID + " separator=\",\"}" + queryFilterString);
            }
            queryForResults.setRows(Integer.valueOf(resultSolrIds.size()));
            queryForResults.setStart(Integer.valueOf(0));

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_SOLR_DEBUG_RESULT_QUERY_2, queryForResults, getName()));
            }
            // perform the result query.
            solrResultTime = System.currentTimeMillis();
            QueryResponse resultQueryResponse = m_solr.query(getCoreName(), queryForResults);
            solrResultTime = System.currentTimeMillis() - solrResultTime;

            // List containing solr ids of filtered contents for which highlighting has to be removed.
            // Since we checked permissions just a few milliseconds ago, this should typically stay empty.
            List<String> filteredResultIds = new ArrayList<>(5);

            for (SolrDocument doc : resultQueryResponse.getResults()) {
                try {
                    CmsSolrDocument searchDoc = new CmsSolrDocument(doc);
                    if (needsPermissionCheck(searchDoc)) {
                        CmsResource resource = filter == null
                        ? getResource(searchCms, searchDoc)
                        : getResource(searchCms, searchDoc, filter);
                        if (null != resource) {
                            if (m_postProcessor != null) {
                                doc = m_postProcessor.process(
                                    searchCms,
                                    resource,
                                    (SolrInputDocument)searchDoc.getDocument());
                            }
                            resourceDocumentList.add(new CmsSearchResource(resource, searchDoc));
                            solrDocumentList.add(doc);
                        } else {
                            filteredResultIds.add(searchDoc.getFieldValueAsString(CmsSearchField.FIELD_SOLR_ID));
                        }
                    } else { // should not happen unless the index has changed since the first query.
                        resourceDocumentList.add(new CmsSearchResource(PSEUDO_RES, searchDoc));
                        solrDocumentList.add(doc);
                        visibleHitCount--;
                    }
                } catch (Exception e) {
                    // should not happen, but if it does we want to go on with the next result nevertheless
                    visibleHitCount--;
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_SOLR_ERR_RESULT_ITERATION_FAILED_0), e);
                }
            }

            long processTime = System.currentTimeMillis() - startTime - solrPermissionTime - solrResultTime;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////// CREATE THE FINAL RESPONSE /////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // we are manipulating the checkQueryResponse to set up the final response, we want to deliver.

            // adjust start, max score and hit count displayed in the result list.
            solrDocumentList.setStart(start);
            Float finalMaxScore = sortByScoreDesc
            ? Float.valueOf(maxScore)
            : checkQueryResponse.getResults().getMaxScore();
            solrDocumentList.setMaxScore(finalMaxScore);
            solrDocumentList.setNumFound(visibleHitCount);

            // Exchange the search parameters in the response header by the ones from the (adjusted) original query.
            NamedList<Object> params = ((NamedList<Object>)(checkQueryResponse.getHeader().get(HEADER_PARAMS_NAME)));
            params.clear();
            for (String paramName : query.getParameterNames()) {
                params.add(paramName, query.get(paramName));
            }

            // Fill in the documents to return.
            checkQueryResponse.getResponse().setVal(
                checkQueryResponse.getResponse().indexOf(QUERY_RESPONSE_NAME, 0),
                solrDocumentList);

            // Fill in the time, the overall query took, including processing and permission check.
            ((NamedList<Object>)checkQueryResponse.getResponseHeader()).setVal(
                checkQueryResponse.getResponseHeader().indexOf(QUERY_TIME_NAME, 0),
                Integer.valueOf(Long.valueOf(System.currentTimeMillis() - startTime).intValue()));

            // Fill in the highlighting information from the result query.
            if (query.getHighlight()) {
                NamedList<Object> highlighting = (NamedList<Object>)resultQueryResponse.getResponse().get(
                    QUERY_HIGHLIGHTING_NAME);
                // filter out highlighting for documents where access is not permitted.
                for (String filteredId : filteredResultIds) {
                    highlighting.remove(filteredId);
                }
                NamedList<Object> completeResponse = new SimpleOrderedMap<Object>(1);
                completeResponse.addAll(checkQueryResponse.getResponse());
                completeResponse.add(QUERY_HIGHLIGHTING_NAME, highlighting);
                checkQueryResponse.setResponse(completeResponse);
            }

            // build the result
            result = new CmsSolrResultList(
                query,
                checkQueryResponse,
                solrDocumentList,
                resourceDocumentList,
                start,
                Integer.valueOf(rows),
                Math.min(end, (start + solrDocumentList.size())),
                rows > 0 ? (start / rows) + 1 : 0, //page - but matches only in case of equally sized pages and is zero for rows=0 (because this was this way before!?!)
                visibleHitCount,
                finalMaxScore,
                startTime,
                System.currentTimeMillis());
            if (LOG.isDebugEnabled()) {
                Object[] logParams = new Object[] {
                    Long.valueOf(System.currentTimeMillis() - startTime),
                    Long.valueOf(result.getNumFound()),
                    Long.valueOf(solrPermissionTime + solrResultTime),
                    Long.valueOf(processTime),
                    Long.valueOf(result.getHighlightEndTime() != 0 ? result.getHighlightEndTime() - startTime : 0)};
                LOG.debug(
                    query.toString()
                        + "\n"
                        + Messages.get().getBundle().key(Messages.LOG_SOLR_SEARCH_EXECUTED_5, logParams));
            }
            // write the response for the handler
            if (response != null) {
                // create and return the result
                core = m_solr instanceof EmbeddedSolrServer
                ? ((EmbeddedSolrServer)m_solr).getCoreContainer().getCore(getCoreName())
                : null;

                solrQueryRequest = new LocalSolrQueryRequest(core, query);
                SolrQueryResponse solrQueryResponse = new SolrQueryResponse();
                solrQueryResponse.setAllValues(checkQueryResponse.getResponse());
                writeResp(response, solrQueryRequest, solrQueryResponse);
            }
        } catch (

        Exception e) {
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
            if (null != core) {
                core.close();
            }
            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);
        }
        return result;
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
            q.setRows(Integer.valueOf(0));

            QueryResponse queryResponse = m_solr.query(getCoreName(), q);

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
     * Check, if the current user has permissions on the document's resource.
     * @param cms the context
     * @param doc the solr document (from the search result)
     * @param filter the resource filter to use for checking permissions
     * @return <code>true</code> iff the resource mirrored by the search result can be read by the current user.
     */
    protected boolean hasPermissions(CmsObject cms, CmsSolrDocument doc, CmsResourceFilter filter) {

        return null != (filter == null ? getResource(cms, doc) : getResource(cms, doc, filter));
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
