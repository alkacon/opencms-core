/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchIndex.java,v $
 * Date   : $Date: 2010/01/14 15:30:14 $
 * Version: $Revision: 1.1 $
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

package org.opencms.search.galleries;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.Messages;
import org.opencms.search.documents.I_CmsTermHighlighter;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

/**
 * Implements the search within a the gallery index.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchIndex extends CmsSearchIndex {

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

            // change the project     
            searchCms.getRequestContext().setCurrentProject(searchCms.readProject(getProject()));

            // several search options are searched using filters
            BooleanFilter filter = new BooleanFilter();
            // append root path filter
            filter = appendPathFilter(searchCms, filter, params.getGalleries());
            // append category filter
            filter = appendCategoryFilter(searchCms, filter, params.getCategories());
            // append resource type filter
            filter = appendResourceTypeFilter(searchCms, filter, params.getResourceTypes());
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

            if (params.getSearchWords() != null) {
                // this search contains a full text search component
                BooleanQuery booleanFieldsQuery = new BooleanQuery();
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                for (int i = 0; i < params.getFields().size(); i++) {
                    QueryParser p = new QueryParser(Version.LUCENE_CURRENT, params.getFields().get(i), getAnalyzer());
                    booleanFieldsQuery.add(p.parse(params.getSearchWords()), BooleanClause.Occur.SHOULD);
                }
                fieldsQuery = getSearcher().rewrite(booleanFieldsQuery);
            }

            // finally set the main query to the fields query
            // please note that we still need both variables in case the query is a MatchAllDocsQuery - see below
            query = fieldsQuery;

            if (query == null) {
                // if no text query is set, then we match all documents 
                query = new MatchAllDocsQuery();
            }

            // perform the search operation          
            hits = getSearcher().search(query, filter, getMaxHits(), params.getSort());

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
                                    excerpt);
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
}
