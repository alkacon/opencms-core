/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2005/03/08 06:21:01 $
 * Version: $Revision: 1.36 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.CmsHighlightExtractor;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_TermHighlighter;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 *   
 * @version $Revision: 1.36 $ $Date: 2005/03/08 06:21:01 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchIndex implements I_CmsConfigurationParameterHandler {

    /** Automatic rebuild. */
    public static final String C_AUTO_REBUILD = "auto";

    /** Manual rebuild as default value. */
    public static final String C_DEFAULT_REBUILD = "manual";

    /** Constant for additional param to enable excerpt creation (default: true). */
    public static final String C_EXCERPT = CmsSearchIndex.class.getName() + ".createExcerpt";

    /** Constant for additional param to enable permission checks (default: true). */
    public static final String C_PERMISSIONS = CmsSearchIndex.class.getName() + ".checkPermissions";

    /** Contsnat for additional param to set the thread priority during search. */
    public static final String C_PRIORITY = CmsSearchIndex.class.getName() + ".priority";

    /** Special root path start token for optimized path queries. */
    public static final String C_ROOT_PATH_TOKEN = "root";

    /** A search query to return all documents in the index. */
    public static final String C_SEARCH_QUERY_RETURN_ALL = "*";

    /** Constant for a field list that cointains only the "meta" field. */
    private static final String[] C_DOC_META_FIELDS = new String[] {
        I_CmsDocumentFactory.DOC_META,
        I_CmsDocumentFactory.DOC_CONTENT};

    /** The permission check mode for this index. */
    private boolean m_checkPermissions;

    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Documenttypes of folders/channels. */
    private Map m_documenttypes;

    /** The incremental mode for this index. */
    private boolean m_incremental;

    /** The language filter of this index. */
    private String m_locale;

    /** The name of this index. */
    private String m_name;

    /** Path to index data. */
    private String m_path;

    /** The thread priority for a search. */
    private int m_priority;

    /** The project of this index. */
    private String m_project;

    /** The rebuild mode for this index. */
    private String m_rebuild;

    /** The configured sources for this index. */
    private List m_sourceNames;

    /**
     * Creates a new CmsSearchIndex.<p>
     */
    public CmsSearchIndex() {

        m_sourceNames = new ArrayList();
        m_documenttypes = new HashMap();
        m_createExcerpt = true;
        m_checkPermissions = true;
        m_priority = -1;
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     */
    public void addConfigurationParameter(String key, String value) {

        if (C_PERMISSIONS.equals(key)) {
            m_checkPermissions = Boolean.valueOf(value).booleanValue();
        } else if (C_EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();
        } else if (C_PRIORITY.equals(key)) {
            m_priority = Integer.parseInt(value);
            if (m_priority < Thread.MIN_PRIORITY) {
                m_priority = Thread.MIN_PRIORITY;
                OpenCms.getLog(this).error(
                    "Value '"
                        + value
                        + "' given for search thread priority is to low, setting to "
                        + Thread.MIN_PRIORITY);
            } else if (m_priority > Thread.MAX_PRIORITY) {
                m_priority = Thread.MAX_PRIORITY;
                OpenCms.getLog(this).error(
                    "Value '"
                        + value
                        + "' given for search thread priority is to high, setting to "
                        + Thread.MAX_PRIORITY);
            }
        }
    }

    /**
     * Adds a source name to this search index.<p>
     * 
     * @param sourceName a source name
     */
    public void addSourceName(String sourceName) {

        m_sourceNames.add(sourceName);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        ExtendedProperties result = new ExtendedProperties();
        result.put(C_PRIORITY, new Integer(m_priority));
        result.put(C_EXCERPT, new Boolean(m_createExcerpt));
        result.put(C_PERMISSIONS, new Boolean(m_checkPermissions));
        return result;
    }

    /**
     * Gets the set of documenttypes of a folder or channel.<p>
     * The set contains Strings with the names of the documenttypes.
     * 
     * @param path path of the folder or channel
     * @return the name set of documenttypes of a folder
     */
    public List getDocumenttypes(String path) {

        List documenttypes = null;
        if (m_documenttypes != null) {
            for (Iterator i = m_documenttypes.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                // NOTE: assumed that configured resource paths do not overlap, otherwise result is undefined
                if (path.startsWith(key)) {
                    documenttypes = (List)m_documenttypes.get(key);
                    break;
                }
            }
        }
        if (documenttypes == null) {
            documenttypes = OpenCms.getSearchManager().getDocumentTypes();
        }
        return documenttypes;
    }

    /**
     * Returns a new index writer for this index.<p>
     * 
     * @return a new instance of IndexWriter
     * @throws CmsIndexException if something goes wrong
     */
    public IndexWriter getIndexWriter() throws CmsIndexException {

        IndexWriter indexWriter;
        Analyzer analyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);

        try {

            File f = new File(m_path);

            if (f.exists()) {
                indexWriter = new IndexWriter(m_path, analyzer, !m_incremental);
            } else {
                f = f.getParentFile();
                if (f != null && !f.exists()) {
                    f.mkdir();
                }

                indexWriter = new IndexWriter(m_path, analyzer, true);
            }

        } catch (Exception exc) {
            throw new CmsIndexException("Can't create IndexWriter for " + m_name, exc);
        }

        return indexWriter;
    }

    /**
     * Gets the langauge of this index.<p>
     * 
     * @return the language of the index, i.e. de
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Gets the name of this index.<p>
     * 
     * @return the name of the index
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the project of this index.<p>
     * 
     * @return the project of the index, i.e. "online"
     */
    public String getProject() {

        return m_project;
    }

    /**
     * Get the rebuild mode of this index.<p>
     * 
     * @return the current rebuild mode
     */
    public String getRebuildMode() {

        return m_rebuild;
    }

    /**
     * Returns all configured sources names of this search index.<p>
     * 
     * @return a list with all configured sources names of this search index
     */
    public List getSourceNames() {

        return m_sourceNames;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // noting to do here
    }

    /**
     * Initializes the search index.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void initialize() throws CmsException {

        String sourceName = null;
        CmsSearchIndexSource indexSource = null;
        List searchIndexSourceDocumentTypes = null;
        List resourceNames = null;
        String resourceName = null;

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + m_name);

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {

            try {
                sourceName = (String)m_sourceNames.get(i);
                indexSource = OpenCms.getSearchManager().getIndexSource(sourceName);

                resourceNames = indexSource.getResourcesNames();
                searchIndexSourceDocumentTypes = indexSource.getDocumentTypes();
                for (int j = 0, m = resourceNames.size(); j < m; j++) {

                    resourceName = (String)resourceNames.get(j);
                    m_documenttypes.put(resourceName, searchIndexSourceDocumentTypes);
                }
            } catch (Exception exc) {
                throw new CmsException("Index source association for \"" + sourceName + "\" failed", exc);
            }
        }
    }

    /**
     * Performs a search on the index within the fields "content" and "meta".<p>
     * 
     * The result is returned as List with entries of type I_CmsSearchResult.<p>
     * 
     * @param cms the current user's Cms object
     * @param searchRoot only resource that are sub-resource of the search root are included in the search result
     * @param searchQuery the search term to search the index
     * @param page the page to calculate the search result list, or -1 to return all found documents in the search result
     * @param matchesPerPage the number of search results per page, or -1 to return all found documents in the search result
     * @return the List of results found or an empty list
     * @throws CmsException if something goes wrong
     */
    public synchronized List search(CmsObject cms, String searchRoot, String searchQuery, int page, int matchesPerPage)
    throws CmsException {

        return search(cms, searchRoot, searchQuery, C_DOC_META_FIELDS, page, matchesPerPage);
    }

    /**
     * Performs a search on the index within the given fields.<p>
     * 
     * The result is returned as List with entries of type I_CmsSearchResult.<p>
     * 
     * @param cms the current user's Cms object
     * @param searchRoot only resource that are sub-resource of the search root are included in the search result
     * @param searchQuery the search term to search the index
     * @param fields the list of fields to search
     * @param page the page to calculate the search result list, or -1 to return all found documents in the search result
     * @param matchesPerPage the number of search results per page, or -1 to return all found documents in the search result
     * @return the List of results found or an empty list
     * @throws CmsException if something goes wrong
     */
    public synchronized List search(
        CmsObject cms,
        String searchRoot,
        String searchQuery,
        String[] fields,
        int page,
        int matchesPerPage) throws CmsException {

        Document luceneDocument = null;
        double maxScore = -1.0;
        CmsSearchResult searchResult = null;
        double score = -1;
        String excerpt = null;

        long totalSearchDuration = -System.currentTimeMillis();
        long luceneSearchDuration = 0;

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Searching for \"" + searchQuery + "\" in fields \"" + fields + "\" of index " + m_name);
        }

        CmsRequestContext context = cms.getRequestContext();
        CmsProject currentProject = context.currentProject();

        // the searcher to perform the operation in
        Searcher searcher = null;

        // the hits found during the search
        Hits hits;

        // storage for the results found
        List searchResults = new ArrayList();

        int previousPriority = Thread.currentThread().getPriority();

        try {

            if (m_priority > 0) {
                // change thread priority in order to reduce search impact on overall system performance
                Thread.currentThread().setPriority(m_priority);
            }

            // change the project     
            context.setCurrentProject(cms.readProject(m_project));

            // complete the search root
            if (searchRoot != null && !"".equals(searchRoot)) {
                // add the site root to the search root
                searchRoot = cms.getRequestContext().getSiteRoot() + searchRoot;
            } else {
                // just use the site root as the search root
                searchRoot = cms.getRequestContext().getSiteRoot();
            }

            luceneSearchDuration = -System.currentTimeMillis();

            // the language analyzer to use for creating the queries
            Analyzer languageAnalyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);

            // increase the clause count - just a precaution even though should not really be required
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            // the main query to use, will be constructed in the next lines 
            BooleanQuery query = new BooleanQuery();

            // add query to search only in the selected site root
            if (CmsStringUtil.isNotEmpty(searchRoot)) {
                // implementation note: 
                // initially this was a simple PrefixQuery based on the DOC_PATH
                // however, internally Lucene rewrote that to literally hundreds of BooleanQuery parts
                // the following implementation will lead to just one Lucene query and is thus much better
                String[] path = CmsStringUtil.splitAsArray(searchRoot, '/');
                if (path.length > 0) {
                    PhraseQuery pathQuery = new PhraseQuery();
                    // add root term
                    pathQuery.add(new Term(I_CmsDocumentFactory.DOC_ROOT, C_ROOT_PATH_TOKEN));
                    for (int i = 0; i < path.length; i++) {
                        if (CmsStringUtil.isEmpty(path[i])) {
                            continue;
                        }
                        // add term for the folder
                        pathQuery.add(new Term(I_CmsDocumentFactory.DOC_ROOT, path[i]));
                    }
                    query.add(pathQuery, true, false);
                }
            }

            if (!C_SEARCH_QUERY_RETURN_ALL.equals(searchQuery) && (fields != null) && (fields.length > 0)) {
                // this is a "regular" query over one or more fields
                BooleanQuery fieldsQuery = new BooleanQuery();
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                for (int i = 0; i < fields.length; i++) {
                    fieldsQuery.add(QueryParser.parse(searchQuery, fields[i], languageAnalyzer), false, false);
                }
                // finally add the field queries to the main query
                query.add(fieldsQuery, true, false);
            } else {
                // if no fields are provided, just use the "content" field by default
                query.add(
                    QueryParser.parse(searchQuery, I_CmsDocumentFactory.DOC_CONTENT, languageAnalyzer),
                    true,
                    false);
            }

            // create the index searcher
            searcher = new IndexSearcher(m_path);

            // uncomment the next to lines for debugging query generation
            // IndexReader reader = IndexReader.open(m_path);            
            // Query finalQuery = query.rewrite(reader);

            // perform the search operation
            hits = searcher.search(query);

            luceneSearchDuration += System.currentTimeMillis();

            if (hits != null) {
                maxScore = (hits.length() > 0) ? hits.score(0) : 0.0;

                int start = -1, end = -1;
                if (matchesPerPage > 0 && page > 0 && hits.length() > 0) {
                    // calculate the final size of the search result
                    start = matchesPerPage * (page - 1);
                    end = start + matchesPerPage;
                    // ensure that both i and n are inside the range of foundDocuments.size()
                    start = (start > hits.length()) ? hits.length() : start;
                    end = (end > hits.length()) ? hits.length() : end;
                } else {
                    // return all found documents in the search result
                    start = 0;
                    end = hits.length();
                }

                if (m_checkPermissions) {
                    // filter out resources from the search result where the current user has no read permissions
                    for (int i = 0, cnt = 0; i < hits.length() && cnt < end; i++) {
                        try {
                            luceneDocument = hits.doc(i);
                            score = (hits.score(i) / maxScore) * 100.0;

                            if (getIndexResource(cms, luceneDocument) != null) {
                                // user has read permission
                                if (cnt >= start) {
                                    // do not use the resource to obtain the raw content, read it from the lucene document !
                                    if (m_createExcerpt) {
                                        excerpt = getExcerpt(luceneDocument.getField(I_CmsDocumentFactory.DOC_CONTENT)
                                            .stringValue(), searchQuery);
                                    }
                                    searchResult = new CmsSearchResult((int)score, luceneDocument, excerpt);
                                    searchResults.add(searchResult);
                                }
                                cnt++;
                            }
                        } catch (Exception exc) {
                            // happens if resource was deleted or current user has not the permission to view the current resource at least
                        }
                    }
                } else {
                    // without permission check
                    for (int i = start; i < end; i++) {
                        try {
                            luceneDocument = hits.doc(i);
                            score = (hits.score(i) / maxScore) * 100.0;

                            if (m_createExcerpt) {
                                excerpt = getExcerpt(luceneDocument.getField(I_CmsDocumentFactory.DOC_CONTENT)
                                    .stringValue(), searchQuery);
                            }
                            searchResult = new CmsSearchResult((int)score, luceneDocument, excerpt);
                            searchResults.add(searchResult);
                        } catch (Exception exc) {
                            // happens if resource was deleted or current user has not the permission to view the current resource at least
                        }
                    }
                }

                // save the total count of search results at the last index of the search result 
                searchResults.add(new Integer(hits.length()));
            } else {
                searchResults.add(new Integer(0));
            }

        } catch (Exception exc) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Search on " + m_path + " failed. ", exc);
        } finally {

            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);

            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException exc) {
                    // noop
                }
            }

            // switch back to the original project
            context.setCurrentProject(currentProject);
        }

        totalSearchDuration += System.currentTimeMillis();

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                hits.length()
                    + " results found in "
                    + totalSearchDuration
                    + " ms"
                    + " (Lucene: "
                    + luceneSearchDuration
                    + " ms)");
        }

        return searchResults;
    }

    /**
     * Sets the locale to index resources.<p>
     * 
     * @param locale the locale to index resources
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the logical key/name of this search index.<p>
     * 
     * @param name the logical key/name of this search index
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the name of the project used to index resources.<p>
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProjectName(String projectName) {

        m_project = projectName;
    }

    /**
     * Sets the rebuild mode of this search index.<p>
     * 
     * @param rebuildMode the rebuild mode of this search index {auto|manual}
     */
    public void setRebuildMode(String rebuildMode) {

        m_rebuild = rebuildMode;
    }

    /**
     * Returns an excerpt of the content of a specified index resource.<p>
     * 
     * @param cms the cms object
     * @param indexResource an index resource
     * @param searchQuery the search query
     * @return an excerpt of the content of the specified index resource
     * @throws CmsException if something goes wrong
     */
    protected String getExcerpt(CmsObject cms, A_CmsIndexResource indexResource, String searchQuery)
    throws CmsException {

        return getExcerpt(getRawContent(cms, indexResource), searchQuery);
    }

    /**
     * Returns an excerpt of the given content related to the given search query.<p>
     * 
     * @param rawContent the content
     * @param searchQuery the search query
     * @return an excerpt of the content
     * @throws CmsException if something goes wrong
     */
    protected String getExcerpt(String rawContent, String searchQuery) throws CmsException {

        int highlightFragmentSizeInBytes = 60;
        int maxNumFragmentsRequired = 5;
        String fragmentSeparator = ".. ";

        String excerpt = null;
        Analyzer analyzer = null;
        Query query = null;
        CmsHighlightExtractor highlighter = null;
        I_TermHighlighter termHighlighter = null;
        int maxExcerptLength = OpenCms.getSearchManager().getMaxExcerptLength();

        // the index reader, required for excerpt generation with wildcards
        IndexReader reader = null;

        try {

            if (rawContent != null) {

                // there are no search terms to highlight if the query 
                // was a search query to return all documents from the index                 
                if (!C_SEARCH_QUERY_RETURN_ALL.equalsIgnoreCase(searchQuery)) {

                    // open the index reader
                    reader = IndexReader.open(m_path);

                    analyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);
                    // create the query 
                    query = QueryParser.parse(searchQuery, I_CmsDocumentFactory.DOC_CONTENT, analyzer);
                    // rewrite the query, this will remove wildcards and replace them with full terms
                    query = query.rewrite(reader);
                    termHighlighter = OpenCms.getSearchManager().getHighlighter();
                    highlighter = new CmsHighlightExtractor(termHighlighter, query, analyzer);

                    excerpt = highlighter.getBestFragments(
                        rawContent,
                        highlightFragmentSizeInBytes,
                        maxNumFragmentsRequired,
                        fragmentSeparator);
                    excerpt = excerpt.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

                } else {

                    excerpt = rawContent.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
                }

                if (excerpt != null && excerpt.length() > maxExcerptLength) {
                    excerpt = excerpt.substring(0, maxExcerptLength);
                }
            }

        } catch (Exception e) {

            String message = "[Analyzer: "
                + analyzer
                + "][Query: "
                + query
                + "][CmsHighlightExtractor: "
                + highlighter
                + "][RawContent: ";

            if (rawContent != null) {
                message += rawContent.length() + "]";
            } else {
                message += rawContent + "]";
            }

            throw new CmsException(message, e);
        } finally {
            // close the reader            
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exc) {
                    // noop
                }
            }
        }

        return excerpt;
    }

    /**
     * Returns a A_CmsIndexResource for a specified Lucene search result document.<p>
     * 
     * All index sources of this search index are iterated in order to find a matching
     * indexer to create the index resource.<p>
     * 
     * Permissions and the search root are checked in the implementations of
     * {@link I_CmsIndexer#getIndexResource(CmsObject, Document)}.<p>
     * @param cms the current user's CmsObject
     * @param doc the Lucene search result document
     * 
     * @return a A_CmsIndexResource, or null if no index source had an indexer configured that was able to convert the document
     * @throws Exception if something goes wrong
     * @see I_CmsIndexer#getIndexResource(CmsObject, Document)
     */
    protected A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws Exception {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        String indexSourceName = doc.getField(I_CmsDocumentFactory.DOC_SOURCE).stringValue();
        CmsSearchIndexSource indexSource = searchManager.getIndexSource(indexSourceName);
        I_CmsIndexer indexer = indexSource.getIndexer();

        return indexer.getIndexResource(cms, doc);
    }

    /**
     * Returns the raw content of an index resource.<p>
     * 
     * The content is read using the document factory matching the index resource's
     * Cms resource types and mimetypes.<p>
     * 
     * @param cms the cms object
     * @param indexResource an index resource
     * @return the raw content
     */
    protected String getRawContent(CmsObject cms, A_CmsIndexResource indexResource) {

        String rawContent = null;

        try {
            rawContent = OpenCms.getSearchManager().getDocumentFactory(indexResource).getRawContent(
                cms,
                indexResource,
                m_locale);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Could not read raw content of " + indexResource.getRootPath(), e);
            }
        }

        return rawContent;
    }

}