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

package org.opencms.search.galleries;

import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsLuceneDocument;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.Messages;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.FilterClause;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

/**
 * Implements the search within a the gallery index.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchIndex extends CmsSearchIndex {

    /** The system folder. */
    public static final String FOLDER_SYSTEM = "/system/";

    /** The system galleries path. */
    public static final String FOLDER_SYSTEM_GALLERIES = "/system/galleries/";

    /** The system modules folder path. */
    public static final String FOLDER_SYTEM_MODULES = "/system/modules/";

    /** The advanced gallery index name. */
    public static final String GALLERY_INDEX_NAME = "Gallery Index";

    /** The gallery document type name for xml-contents. */
    public static final String TYPE_XMLCONTENT_GALLERIES = "xmlcontent-galleries";

    /** The gallery document type name for xml-pages. */
    public static final String TYPE_XMLPAGE_GALLERIES = "xmlpage-galleries";

    /** The search.exclude property values considered when searching for page editor gallery. */
    private static final List<String> EXCLUDE_PROPERTY_VALUES = Arrays.asList(new String[] {"all", "gallery"});

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGallerySearchIndex.class);

    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     * 
     * It is recommended to use the constructor <code>{@link #CmsGallerySearchIndex(String)}</code> 
     * as it enforces the mandatory name argument. <p>
     * 
     */
    public CmsGallerySearchIndex() {

        super();
        setRequireViewPermission(true);
    }

    /**
     * Creates a new gallery search index with the given name.<p>
     * 
     * @param name the system-wide unique name for the search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     * 
     */
    public CmsGallerySearchIndex(String name)
    throws CmsIllegalArgumentException {

        super();
        setName(name);
        setRequireViewPermission(true);
    }

    /**
     * Computes the search root folders for the given search parameters based on the search scope.<p>
     * 
     * @param cms the current CMS context 
     * @param params the current search parameters 
     * 
     * @return the search root folders based on the search scope 
     */
    public List<String> computeScopeFolders(CmsObject cms, CmsGallerySearchParameters params) {

        String subsite = null;
        if (params.getReferencePath() != null) {
            subsite = OpenCms.getADEManager().getSubSiteRoot(
                cms,
                cms.getRequestContext().addSiteRoot(params.getReferencePath()));
            if (subsite != null) {
                subsite = cms.getRequestContext().removeSiteRoot(subsite);
            } else if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_GALLERIES_COULD_NOT_EVALUATE_SUBSITE_1,
                    params.getReferencePath()));
            }
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_GALLERIES_NO_REFERENCE_PATH_PROVIDED_0));
        }
        List<String> scopeFolders = getSearchRootsForScope(
            params.getScope(),
            cms.getRequestContext().getSiteRoot(),
            subsite);
        return scopeFolders;
    }

    /**
     * Returns the Lucene document with the given structure id from the index.<p>
     * 
     * @param structureId the structure id of the document to retrieve  
     * 
     * @return the Lucene document with the given root path from the index
     * 
     * @deprecated Use {@link #getDocument(String, String)} instead and provide {@link CmsGallerySearchFieldMapping#FIELD_RESOURCE_STRUCTURE_ID} as field to search in
     */
    @Deprecated
    public I_CmsSearchDocument getDocument(CmsUUID structureId) {

        return getDocument(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STRUCTURE_ID, structureId.toString());
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#getDocumentFactory(org.opencms.file.CmsResource)
     */
    @Override
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if ((res != null) && (getSources() != null)) {
            // the result can only be null or the type configured for the resource
            if (CmsResourceTypeXmlContent.isXmlContent(res) || CmsResourceTypeXmlContainerPage.isContainerPage(res)) {
                return OpenCms.getSearchManager().getDocumentFactory(TYPE_XMLCONTENT_GALLERIES, null);
            } else if (CmsResourceTypeXmlPage.isXmlPage(res)) {
                return OpenCms.getSearchManager().getDocumentFactory(TYPE_XMLPAGE_GALLERIES, null);
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

        Locale result;
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
        if ((availableLocales != null) && (availableLocales.size() > 0)) {
            result = OpenCms.getLocaleManager().getBestMatchingLocale(
                defaultLocales.get(0),
                defaultLocales,
                availableLocales);
        } else {
            result = defaultLocales.get(0);
        }
        return result;
    }

    /**
     * Gets the search roots to use for the given site/subsite parameters.<p>
     *  
     * @param scope the search scope
     * @param siteParam the current site 
     * @param subSiteParam the current subsite
     *  
     * @return the list of search roots for that option 
     */
    public List<String> getSearchRootsForScope(CmsGallerySearchScope scope, String siteParam, String subSiteParam) {

        List<String> result = new ArrayList<String>();
        if (scope == CmsGallerySearchScope.everything) {
            result.add("/");
            return result;
        }
        if (scope.isIncludeSite()) {
            result.add(siteParam);
        }
        if (scope.isIncludeSubSite()) {
            if (subSiteParam == null) {
                result.add(siteParam);
            } else {
                result.add(CmsStringUtil.joinPaths(siteParam, subSiteParam));
            }
        }
        if (scope.isIncludeShared()) {
            String sharedFolder = OpenCms.getSiteManager().getSharedFolder();
            if (sharedFolder != null) {
                result.add(sharedFolder);
            }
        }
        return result;
    }

    /**
     * Performs a search on the gallery index.<p>
     * 
     * @param cms the current users OpenCms context
     * @param params the parameters to use for the search
     * 
     * @return the List of results found
     * 
     * @throws CmsSearchException if something goes wrong
     */
    public synchronized CmsGallerySearchResultList searchGallery(CmsObject cms, CmsGallerySearchParameters params)
    throws CmsSearchException {

        // the hits found during the search
        TopDocs hits;

        // storage for the results found
        CmsGallerySearchResultList searchResults = new CmsGallerySearchResultList();

        try {
            // copy the user OpenCms context
            CmsObject searchCms = OpenCms.initCmsObject(cms);
            // make sure to keep the request time when evaluating resource expiration
            searchCms.getRequestContext().setRequestTime(cms.getRequestContext().getRequestTime());

            // change the project     
            searchCms.getRequestContext().setCurrentProject(searchCms.readProject(getProject()));

            // several search options are searched using filters
            BooleanFilter filter = new BooleanFilter();
            // append root path filter
            List<String> folders = new ArrayList<String>();

            if (params.getFolders() != null) {
                folders.addAll(params.getFolders());
            }
            if (params.getGalleries() != null) {
                folders.addAll(params.getGalleries());
            }

            if (!folders.isEmpty()) {
                // appendPathFilter has some annoying default behavior for empty folder lists which conflicts with 
                // the scope filter logic below
                filter = appendPathFilter(searchCms, filter, folders);
            }

            // append category filter
            filter = appendCategoryFilter(searchCms, filter, params.getCategories());
            // append container type filter
            filter = appendContainerTypeFilter(searchCms, filter, params.getContainerTypes());
            // append resource type filter
            filter = appendResourceTypeFilter(searchCms, filter, params.getResourceTypes());

            // only append scope filter if no no folders or galleries given
            if (folders.isEmpty()) {
                if ((params.getResourceTypes() != null)
                    && params.getResourceTypes().contains(CmsXmlDynamicFunctionHandler.TYPE_FUNCTION)) {
                    Filter functionTypeFilter = getTermQueryFilter(
                        CmsSearchField.FIELD_TYPE,
                        CmsXmlDynamicFunctionHandler.TYPE_FUNCTION);
                    List<String> searchRootsForOtherTypes = computeScopeFolders(cms, params);
                    List<String> searchRootsForFunctions = new ArrayList<String>(searchRootsForOtherTypes);
                    searchRootsForFunctions.add(CmsGallerySearchIndex.FOLDER_SYTEM_MODULES);

                    // build a filter with two cases joined by OR:
                    // CASE 1: document is a dynamic function => use search roots together with /system/modules
                    // CASE 2: document is not a dynamic  function => use search roots as-is 

                    BooleanFilter scopeFilter = filterOr(
                        filterAnd(functionTypeFilter, createPathFilter(searchRootsForFunctions)),
                        filterAnd(filterNot(functionTypeFilter), createPathFilter(searchRootsForOtherTypes)));
                    filter.add(scopeFilter, Occur.MUST);
                } else {
                    List<String> scopeFolders = computeScopeFolders(cms, params);
                    filter = appendPathFilter(searchCms, filter, scopeFolders);
                }
            }

            // append locale filter
            filter = appendLocaleFilter(searchCms, filter, params.getLocale());
            // append date last modified filter            
            filter = appendDateLastModifiedFilter(
                filter,
                params.getDateLastModifiedRange().getStartTime(),
                params.getDateLastModifiedRange().getEndTime());
            // append date created filter
            filter = appendDateCreatedFilter(
                filter,
                params.getDateCreatedRange().getStartTime(),
                params.getDateCreatedRange().getEndTime());
            // append ignore search exclude filter
            filter = appendIgnoreSearchExclude(filter, params.isIgnoreSearchExclude());

            // the search query to use, will be constructed in the next lines 
            Query query = null;
            // store separate fields query for excerpt highlighting  
            Query fieldsQuery = null;

            // get an index searcher that is certainly up to date
            indexSearcherUpdate();
            IndexSearcher searcher = getSearcher();

            Locale locale = params.getLocale() == null ? null : CmsLocaleManager.getLocale(params.getLocale());
            if (params.getSearchWords() != null) {
                // this search contains a full text search component
                BooleanQuery booleanFieldsQuery = new BooleanQuery();
                OpenCms.getLocaleManager();
                // extend the field names with the locale information
                List<String> fields = params.getFields();
                fields = getLocaleExtendedFields(params.getFields(), locale);
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.                
                for (String field : fields) {
                    QueryParser p = new QueryParser(CmsSearchIndex.LUCENE_VERSION, field, getAnalyzer());
                    booleanFieldsQuery.add(p.parse(params.getSearchWords()), BooleanClause.Occur.SHOULD);
                }
                fieldsQuery = searcher.rewrite(booleanFieldsQuery);
            }

            // finally set the main query to the fields query
            // please note that we still need both variables in case the query is a MatchAllDocsQuery - see below
            query = fieldsQuery;

            if (query == null) {
                // if no text query is set, then we match all documents 
                query = new MatchAllDocsQuery();
            }

            // perform the search operation          
            hits = searcher.search(query, filter, getMaxHits(), params.getSort(), true, true);

            if (hits != null) {
                int hitCount = hits.totalHits > hits.scoreDocs.length ? hits.scoreDocs.length : hits.totalHits;
                int page = params.getResultPage();
                int start = -1, end = -1;
                if ((params.getMatchesPerPage() > 0) && (page > 0) && (hitCount > 0)) {
                    // calculate the final size of the search result
                    start = params.getMatchesPerPage() * (page - 1);
                    end = start + params.getMatchesPerPage();
                    // ensure that both i and n are inside the range of foundDocuments.size()
                    start = (start > hitCount) ? hitCount : start;
                    end = (end > hitCount) ? hitCount : end;
                } else {
                    // return all found documents in the search result
                    start = 0;
                    end = hitCount;
                }

                Document doc;
                CmsGallerySearchResult searchResult;
                CmsSearchParameters searchParams = params.getCmsSearchParams();

                int visibleHitCount = hitCount;
                for (int i = 0, cnt = 0; (i < hitCount) && (cnt < end); i++) {
                    try {
                        doc = getSearcher().doc(hits.scoreDocs[i].doc);
                        I_CmsSearchDocument searchDoc = new CmsLuceneDocument(doc);
                        if (hasReadPermission(searchCms, searchDoc)) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document!
                                String excerpt = null;
                                if (isCreatingExcerpt() && (fieldsQuery != null)) {
                                    I_CmsTermHighlighter highlighter = OpenCms.getSearchManager().getHighlighter();
                                    excerpt = highlighter.getExcerpt(
                                        doc,
                                        this,
                                        searchParams,
                                        fieldsQuery,
                                        getAnalyzer());
                                }
                                searchResult = new CmsGallerySearchResult(
                                    searchCms,
                                    Math.round((hits.scoreDocs[i].score / hits.getMaxScore()) * 100f),
                                    doc,
                                    excerpt,
                                    locale);
                                searchResults.add(searchResult);
                            }
                            cnt++;
                        } else {
                            visibleHitCount--;
                        }
                    } catch (Exception e) {
                        // should not happen, but if it does we want to go on with the next result nevertheless                        
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
                        }
                    }
                }

                // save the total count of search results
                searchResults.setHitCount(visibleHitCount);
            } else {
                searchResults.setHitCount(0);
            }

        } catch (RuntimeException e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        } catch (Exception e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        }

        return searchResults;
    }

    /**
     * Appends a container type filter to the given filter clause that matches all given container types.<p>
     * 
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param containers the containers that will compose the filter
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendContainerTypeFilter(CmsObject cms, BooleanFilter filter, List<String> containers) {

        if ((containers != null) && (containers.size() > 0)) {
            // add query categories (if required)
            filter.add(new FilterClause(getMultiTermQueryFilter(
                CmsGallerySearchFieldMapping.FIELD_CONTAINER_TYPES,
                containers), BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Appends the ignore search exclude property filter.<p>
     * 
     * @param filter the filter to extend
     * @param ignoreSearchExclude <code>true</code> if the search exclude property should be ignored
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendIgnoreSearchExclude(BooleanFilter filter, boolean ignoreSearchExclude) {

        if (!ignoreSearchExclude) {
            filter.add(new FilterClause(getMultiTermQueryFilter(
                CmsSearchField.FIELD_SEARCH_EXCLUDE,
                EXCLUDE_PROPERTY_VALUES), BooleanClause.Occur.MUST_NOT));
        }
        return filter;
    }

    /**
     * Appends the locale filter to the given filter clause that matches the given locale.<p>
     * 
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param locale the locale that will compose the filter
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendLocaleFilter(CmsObject cms, BooleanFilter filter, String locale) {

        if (locale != null) {
            // add query categories (if required)
            filter.add(new FilterClause(
                getTermQueryFilter(CmsSearchField.FIELD_RESOURCE_LOCALES, locale),
                BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Appends the a VFS path filter to the given filter clause that matches all given root paths.<p>
     * 
     * In case the provided List is null or empty, the current request context site root is appended.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param roots the VFS root paths that will compose the filter
     * 
     * @return the extended filter clause
     */
    @Override
    protected BooleanFilter appendPathFilter(CmsObject cms, BooleanFilter filter, List<String> roots) {

        // complete the search root
        List<Term> terms = new ArrayList<Term>();
        if ((roots != null) && (roots.size() > 0)) {
            // add the all configured search roots with will request context
            for (int i = 0; i < roots.size(); i++) {
                extendPathFilter(terms, roots.get(i));
            }
        } else {
            // use the current site root as the search root
            extendPathFilter(terms, cms.getRequestContext().getSiteRoot());
            // also add the shared folder (v 8.0)
            extendPathFilter(terms, OpenCms.getSiteManager().getSharedFolder());
            extendPathFilter(terms, FOLDER_SYTEM_MODULES);
            extendPathFilter(terms, FOLDER_SYSTEM_GALLERIES);
        }

        // add the calculated path filter for the root path
        filter.add(new FilterClause(new TermsFilter(terms), BooleanClause.Occur.MUST));
        return filter;
    }

    /**
     * Creates a search filter for the given search root paths.<p>
     * 
     * @param roots the search root paths
     *  
     * @return the filter which filters for the given search roots 
     */
    protected TermsFilter createPathFilter(Collection<String> roots) {

        // complete the search root
        List<Term> terms = new ArrayList<Term>();
        for (String root : roots) {
            extendPathFilter(terms, root);
        }
        return new TermsFilter(terms);
    }

    /**
     * Checks if the provided resource should be excluded from this search index.<p> 
     *
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource to index
     * 
     * @return true if the resource should be excluded, false if it should be included in this index
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
     * Returns a list of locale extended field names.<p>
     * 
     * @param fields the field name to extend
     * @param locale the locale to extend the field names with
     * 
     * @return a list of locale extended field names
     */
    protected List<String> getLocaleExtendedFields(List<String> fields, Locale locale) {

        List<String> result = new ArrayList<String>(fields.size() * 2);
        for (String fieldName : fields) {
            result.add(fieldName);
            if (locale != null) {
                result.add(CmsSearchFieldConfiguration.getLocaleExtendedName(fieldName, locale));
            } else {
                for (Locale l : OpenCms.getLocaleManager().getAvailableLocales()) {
                    result.add(CmsSearchFieldConfiguration.getLocaleExtendedName(fieldName, l));
                }
            }
        }
        return result;
    }

    /**
     * We are overriding getResource since the default implementation uses the path to read the resource,
     * which doesn't work for resources in a different site.<p>
     * 
     * @see org.opencms.search.CmsSearchIndex#getResource(org.opencms.file.CmsObject, org.opencms.search.I_CmsSearchDocument)
     */
    @Override
    protected CmsResource getResource(CmsObject cms, I_CmsSearchDocument doc) {

        String fieldStructureId = doc.getFieldValueAsString(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STRUCTURE_ID);
        CmsUUID structureId = new CmsUUID(fieldStructureId);
        // check if the resource exits in the VFS, 
        // this will implicitly check read permission and if the resource was deleted
        //String contextPath = cms.getRequestContext().removeSiteRoot(doc.getPath());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
        if (isRequireViewPermission()) {
            filter = CmsResourceFilter.DEFAULT_ONLY_VISIBLE;
        }
        try {
            return cms.readResource(structureId, filter);
        } catch (CmsException e) {
            // Do nothing 
        }
        return null;
    }

    /**
     * Creates a filter which represents the "AND" operation on two other filters.<p>
     * 
     * @param f1 the first filter 
     * @param f2 the second filter 
     * 
     * @return the "AND" operation on the two filters 
     */
    private BooleanFilter filterAnd(Filter f1, Filter f2) {

        BooleanFilter filter = new BooleanFilter();
        filter.add(f1, Occur.MUST);
        filter.add(f2, Occur.MUST);
        return filter;
    }

    /**
     * Creates a boolean filter for the negation of another filter.<p>
     * 
     * @param f1 the filter to negate
     *  
     * @return the negated filter 
     */
    private BooleanFilter filterNot(Filter f1) {

        BooleanFilter filter = new BooleanFilter();
        filter.add(f1, Occur.MUST_NOT);
        return filter;
    }

    /** 
     * Creates a boolean filter for the "OR" operation on two other filters.<p>
     * 
     * @param f1 the first filter 
     * @param f2 the second filter
     *  
     * @return the composite filter 
     */
    private BooleanFilter filterOr(Filter f1, Filter f2) {

        BooleanFilter filter = new BooleanFilter();
        filter.add(f1, Occur.SHOULD);
        filter.add(f2, Occur.SHOULD);
        return filter;
    }

}
