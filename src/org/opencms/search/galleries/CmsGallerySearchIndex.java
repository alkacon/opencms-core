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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.Messages;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;

/**
 * Implements the search within a the gallery index.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchIndex extends CmsSearchIndex {

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
     * Returns the Lucene document with the given structure id from the index.<p>
     * 
     * @param structureId the structure id of the document to retrieve  
     * 
     * @return the Lucene document with the given root path from the index
     * 
     * @deprecated Use {@link #getDocument(String, String)} instead and provide {@link CmsGallerySearchFieldMapping#FIELD_RESOURCE_STRUCTURE_ID} as field to search in
     */
    @Deprecated
    public Document getDocument(CmsUUID structureId) {

        return getDocument(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STRUCTURE_ID, structureId.toString());
    }

    /**
     * @see org.opencms.search.CmsSearchIndex#getDocumentFactory(org.opencms.file.CmsResource)
     */
    @Override
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if ((res != null) && (m_sources != null)) {
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
     * @param subSiteParam the current subsite
     *  
     * @return the list of search roots for that option 
     */
    public List<String> getSearchRootsForScope(CmsGallerySearchScope scope, String subSiteParam) {

        List<String> result = new ArrayList<String>();
        if (scope.isIncludeSite()) {
            result.add("/");
        }
        if (scope.isIncludeSubSite() && (subSiteParam != null)) {
            result.add(subSiteParam);
        }
        if (scope.isIncludeShared()) {
            String sharedFolder = OpenCms.getSiteManager().getSharedFolder();
            if (sharedFolder != null) {
                result.add(sharedFolder);
            }
        }
        if (scope == CmsGallerySearchScope.siteShared) {
            result.add(FOLDER_SYTEM_MODULES);
            result.add(FOLDER_SYSTEM_GALLERIES);
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
            filter = appendPathFilter(searchCms, filter, folders);

            String subsite = null;
            if (params.getReferencePath() != null) {
                subsite = OpenCms.getADEManager().getSubSiteRoot(
                    cms,
                    cms.getRequestContext().addSiteRoot(params.getReferencePath()));
                if (subsite != null) {
                    subsite = cms.getRequestContext().removeSiteRoot(subsite);
                }
            }
            List<String> scopeFolders = getSearchRootsForScope(params.getScope(), subsite);
            filter = appendPathFilter(searchCms, filter, scopeFolders);

            // append category filter
            filter = appendCategoryFilter(searchCms, filter, params.getCategories());
            // append container type filter
            filter = appendContainerTypeFilter(searchCms, filter, params.getContainerTypes());
            // append resource type filter
            filter = appendResourceTypeFilter(searchCms, filter, params.getResourceTypes());
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
            searcher.setDefaultFieldSortScoring(true, true);
            hits = searcher.search(query, filter, getMaxHits(), params.getSort());

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
                        doc = searcher.doc(hits.scoreDocs[i].doc);
                        if (hasReadPermission(searchCms, doc)) {
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
                getTermQueryFilter(CmsGallerySearchFieldMapping.FIELD_RESOURCE_LOCALES, locale),
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
        TermsFilter pathFilter = new TermsFilter();
        String sharedFolder = OpenCms.getSiteManager().getSharedFolder();
        if ((roots != null) && (roots.size() > 0)) {
            // add the all configured search roots with will request context
            for (int i = 0; i < roots.size(); i++) {
                String searchRoot = roots.get(i);
                if (!searchRoot.startsWith(FOLDER_SYTEM_MODULES)
                    && !searchRoot.startsWith(FOLDER_SYSTEM_GALLERIES)
                    && ((sharedFolder == null) || !searchRoot.startsWith(OpenCms.getSiteManager().getSharedFolder()))) {
                    searchRoot = cms.getRequestContext().addSiteRoot(roots.get(i));
                }
                extendPathFilter(pathFilter, searchRoot);
            }
        } else {
            // use the current site root as the search root
            extendPathFilter(pathFilter, cms.getRequestContext().getSiteRoot());
            // also add the shared folder (v 8.0)
            extendPathFilter(pathFilter, OpenCms.getSiteManager().getSharedFolder());
            extendPathFilter(pathFilter, FOLDER_SYTEM_MODULES);
            extendPathFilter(pathFilter, FOLDER_SYSTEM_GALLERIES);
        }

        // add the calculated path filter for the root path
        filter.add(new FilterClause(pathFilter, BooleanClause.Occur.MUST));
        return filter;
    }

    /**
     * Checks if the provided resource should be excluded from this search index.<p> 
     *
     * With the introduction of the gallery search index in OpenCms 8, the meaning 
     * of the VFS property <code>search.exclude</code> that controls
     * if a resource is included in a search index has been extended.<p>
     *
     * The following uses cases can be covered with the property:<p>
     *
     * <dl>
     * <dt>Case A: Exclude from all indexes</dt>
     *      <dd>Applies at least to ADE resource type copy templates.<br>
     *      Set <code>search.exclude=all</code>
     *      </dd>
     *      
     * <dt>Case B: Include in all indexes</dt>
     *      <dd>Applies to most resources e.g. news articles etc.<br>
     *      Set <code>search.exclude=false</code> - or anything else but <code>all|true|gallery</code>.
     *      This is also the default in case the property is not set at all.
     *      </dd>
     *      
     * <dt>Case D: Include in gallery index, but exclude in standard index</dt>
     *      <dd>Applies to content like articles that are displayed only in container pages,
     *          also applies to "list generating" resource types like those that contain settings for a collector.<br>
     *      Set <code>search.exclude=true</code> - This is the behavior before OpenCms v8.
     *      </dd>
     *       
     * <dt>Case C: Exclude from gallery index, but include in standard index</dt>
     *      <dd>Use case so far unknown, but implemented anyway.<br>
     *      Set <code>search.exclude=gallery</code>.
     *      </dd> 
     * </dl>
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
        boolean excludeFromIndex = false;
        try {
            // do property lookup with folder search
            String propValue = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE, true).getValue();
            if (propValue != null) {
                propValue = propValue.trim();
                // property value was neither "true" nor null, must check for "all"
                excludeFromIndex = PROPERTY_SEARCH_EXCLUDE_VALUE_ALL.equalsIgnoreCase(propValue)
                    || PROPERTY_SEARCH_EXCLUDE_VALUE_GALLERY.equalsIgnoreCase(propValue);
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNABLE_TO_READ_PROPERTY_1, resource.getRootPath()));
            }
        }
        return excludeFromIndex;
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
                result.add(CmsGallerySearchFieldConfiguration.getLocaleExtendedName(fieldName, locale));
            } else {
                for (Locale l : OpenCms.getLocaleManager().getAvailableLocales()) {
                    result.add(CmsGallerySearchFieldConfiguration.getLocaleExtendedName(fieldName, l));
                }
            }
        }
        return result;
    }

}
