/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2004/07/07 11:21:08 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.CmsHighlightExtractor;
import org.opencms.search.documents.CmsHtmlHighlighter;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 *   
 * @version $Revision: 1.20 $ $Date: 2004/07/07 11:21:08 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchIndex {

    /** Manual rebuild as default value. */
    public static final String C_DEFAULT_REBUILD = "manual";

    /** Automatic rebuild. */
    public static final String C_AUTO_REBUILD = "auto";

    /** The incremental mode for this index. */
    private boolean m_incremental;

    /** The language filter of this index. */
    private String m_locale;

    /** The name of this index. */
    private String m_name;

    /** Path to index data. */
    private String m_path;

    /** The project of this index. */
    private String m_project;

    /** The rebuild mode for this index. */
    private String m_rebuild;

    /** Documenttypes of folders/channels. */
    private Map m_documenttypes;

    /** The configures sources for this index. */
    private List m_sourceNames;

    /**
     * Creates a new CmsSearchIndex.<p>
     */
    public CmsSearchIndex() {

        m_sourceNames = new ArrayList();
        m_documenttypes = new HashMap();
    }

    /**
     * Initializes the search index.<p>
     */
    public void initialize() {

        String sourceName = null;
        CmsSearchIndexSource indexSource = null;
        List searchIndexSourceDocumentTypes = null;
        List resourceNames = null;
        String resourceName = null;

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + m_name);

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {

            sourceName = (String)m_sourceNames.get(i);
            indexSource = OpenCms.getSearchManager().getIndexSource(sourceName);
            resourceNames = indexSource.getResourcesNames();
            searchIndexSourceDocumentTypes = indexSource.getDocumentTypes();

            for (int j = 0, m = resourceNames.size(); j < m; j++) {

                resourceName = (String)resourceNames.get(j);
                m_documenttypes.put(resourceName, searchIndexSourceDocumentTypes);
            }
        }
    }

    /**
     * Returns the excerpt of a given resource.<p>
     *  
     * @param result a single search result
     * @return the excerpt
     * 
     * @throws CmsException if something goes wrong
     */
    public String getExcerpt(CmsSearchResult result) throws CmsException {

        String excerpt = null;

        Analyzer analyzer = null;
        Query query = null;
        CmsHighlightExtractor highlighter = null;
        String rawContent = null;

        try {
            analyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);
            query = QueryParser.parse(result.getQuery(), I_CmsDocumentFactory.DOC_CONTENT, analyzer);
            highlighter = new CmsHighlightExtractor(new CmsHtmlHighlighter(), query, analyzer);

            rawContent = result.getRawContent();

            if (rawContent != null) {

                int highlightFragmentSizeInBytes = 60;
                int maxNumFragmentsRequired = 5;
                String fragmentSeparator = ".. ";

                excerpt = highlighter.getBestFragments(
                    rawContent,
                    highlightFragmentSizeInBytes,
                    maxNumFragmentsRequired,
                    fragmentSeparator);
                excerpt = excerpt.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
            }

        } catch (Exception exc) {
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
            throw new CmsException(message, exc);
        }

        return excerpt;
    }

    /**
     * Returns the index manager.<p>
     * 
     * @return the index manager
     */
    protected CmsSearchManager getIndexManager() {

        return OpenCms.getSearchManager();
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
     * Gets the set of documenttypes of a folder or channel.<p>
     * The set contains Strings with the names of the documenttypes.
     * 
     * @param path path of the folder or channel
     * @return the name set of documenttypes of a folder
     */
    public Set getDocumenttypes(String path) {

        Set documenttypes = null;
        if (m_documenttypes != null) {
            documenttypes = (Set)m_documenttypes.get(path);
        }
        if (documenttypes == null) {
            documenttypes = OpenCms.getSearchManager().getDocumentTypes();
        }
        return documenttypes;
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
     * 
     * @return the List of results found or an empty list
     * @throws CmsException if something goes wrong
     */
    public List search(CmsObject cms, String searchRoot, String searchQuery, String fields) throws CmsException {

        List searchResult = null;
        Query query = null;
        Searcher searcher = null;
        IndexReader reader = null;
        Hits hits = null;
        Document doc = null;
        A_CmsIndexResource resource = null;
        List foundDocuments = null;
        double maxScore = -1.0;
        double[] scores = null;

        Map searchCache = OpenCms.getSearchManager().getResultCache();
        String key = cms.getRequestContext().currentUser().getName()
            + "_"
            + cms.getRequestContext().getRemoteAddress()
            + "_"
            + m_name
            + "_"
            + searchQuery
            + "_"
            + searchRoot
            + "_"
            + fields;

        // try to find the search result in the cache if we are in the Online project
        //if (cms.getRequestContext().currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            searchResult = (ArrayList)searchCache.get(key);
            if (searchResult != null) {
                return searchResult;
            }
        //}

        // change the project     
        CmsRequestContext context = cms.getRequestContext();
        CmsProject currentProject = context.currentProject();
        context.setCurrentProject(cms.readProject(m_project));

        // complete the search root
        if (searchRoot != null && !"".equals(searchRoot)) {
            // add the site root to the search root
            searchRoot = cms.getRequestContext().getSiteRoot() + searchRoot;
        } else {
            // just use the site root as the search root
            searchRoot = cms.getRequestContext().getSiteRoot();
        }

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Searching for \"" + searchQuery + "\" in fields \"" + fields + "\" of index " + m_name);
        }

        try {

            if (!"*".equals(searchQuery)) {
                
                // a query search- return the documents in the index matching the query expr.
                if (fields != null) {
    
                    BooleanQuery fieldsQuery = new BooleanQuery();
                    String fList[] = org.opencms.util.CmsStringSubstitution.split(fields, " ");
                    for (int i = 0; i < fList.length; i++) {
                        fieldsQuery.add(QueryParser.parse(searchQuery, fList[i], OpenCms.getSearchManager().getAnalyzer(
                            m_locale)), false, false);
                    }
    
                    query = fieldsQuery;
    
                } else {
                    query = QueryParser.parse(searchQuery, I_CmsDocumentFactory.DOC_CONTENT, OpenCms.getSearchManager()
                        .getAnalyzer(m_locale));
                }
    
                searcher = new IndexSearcher(m_path);
                hits = searcher.search(query);
                
                foundDocuments = new ArrayList(hits.length());
                scores = new double[hits.length()];
                
                for (int i = 0, n = hits.length(); i < n; i++) {
                    
                    doc = hits.doc(i);
                    foundDocuments.add(doc);
                    scores[i] = hits.score(i);
                }
            } else {
                
                // a non-query search- return all documents in the index
                try {
                    reader = IndexReader.open(m_path);
                    foundDocuments = new ArrayList(reader.numDocs());
                    scores = new double[reader.numDocs()];
                    
                    for (int i = 0, n = reader.numDocs(); i < n; i++) {
                        
                        if (!reader.isDeleted(i)) {
                            doc = reader.document(i);
                            foundDocuments.add(doc);
                            scores[i] = 0;
                        }
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

            // filter resource out from the search result where the current user has no read permissions
            // or that are not subresources of the search root folder
            searchResult = new ArrayList(foundDocuments.size());
            for (int i = 0, n = foundDocuments.size(); i < n; i++) {

                try {

                    doc = (Document)foundDocuments.get(i);
                    resource = getIndexResource(cms, searchRoot, doc);

                    if (resource != null) {
                        maxScore = (maxScore < scores[i]) ? scores[i] : maxScore;
                        searchResult.add(new CmsSearchResult(
                            this,
                            searchQuery,
                            resource,
                            doc,
                            (int)((scores[i] / maxScore) * 100.0)));
                    }
                } catch (Exception exc) {
                    // happens if resource was deleted or current user has not the right to view the current resource at least
                }
            }

        } catch (Exception exc) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Search on " + m_path + " failed. ", exc);
        } finally {

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

        searchCache.put(key, searchResult);
        return searchResult;
    }

    /**
     * Returns a A_CmsIndexResource for a specified Lucene search result document.<p>
     * 
     * All index sources of this search index are iterated in order to find a matching
     * indexer to create the index resource.<p>
     * 
     * Permissions and the search root are checked in the implementations of
     * {@link I_CmsIndexer#getIndexResource(CmsObject, String, Document)}.<p>
     * 
     * @param cms the current user's CmsObject
     * @param searchRoot only resource that are sub-resource of the search root are included in the search result
     * @param doc the Lucene search result document
     * 
     * @return a A_CmsIndexResource, or null if no index source had an indexer configured that was able to convert the document
     * @throws Exception if something goes wrong
     * @see I_CmsIndexer#getIndexResource(CmsObject, String, Document)
     */
    protected A_CmsIndexResource getIndexResource(CmsObject cms, String searchRoot, Document doc) throws Exception {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        String indexSourceName = null;
        CmsSearchIndexSource indexSource = null;
        I_CmsIndexer indexer = null;
        A_CmsIndexResource result = null;
        String className = null;

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {
            indexSourceName = (String)m_sourceNames.get(i);
            indexSource = searchManager.getIndexSource(indexSourceName);

            className = indexSource.getIndexerClassName();
            indexer = (I_CmsIndexer)Class.forName(className).newInstance();
            result = indexer.getIndexResource(cms, searchRoot, doc);

            if (result != null) {
                break;
            }
        }

        return result;
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
     * Sets the logical key/name of this search index.<p>
     * 
     * @param name the logical key/name of this search index
     */
    public void setName(String name) {

        m_name = name;
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
     * Sets the name of the project used to index resources.<p>
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProjectName(String projectName) {

        m_project = projectName;
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
     * Returns all configured sources names of this search index.<p>
     * 
     * @return a list with all configured sources names of this search index
     */
    public List getSourceNames() {

        return m_sourceNames;
    }

}