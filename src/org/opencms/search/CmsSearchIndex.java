/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2004/07/02 16:05:08 $
 * Version: $Revision: 1.15 $
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
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
 * A search index is configured in the registry using the following tags:
 * <pre>
 * &lt;index&gt;
 *     &lt;name&gt;Default (Online)&lt;/name&gt;
 *     &lt;rebuild&gt;auto&lt;/rebuild&gt;
 *     &lt;project&gt;online&lt;/project&gt;
 *     &lt;site&gt;/sites/default/&lt;/site&gt;
 *     &lt;lang&gt;en&lt;/lang&gt;
 *     &lt;folder&gt;
 *         &lt;source&gt;/&lt;/source&gt;
 *         &lt;documenttype&gt;xmlpage&lt;/documenttype&gt;
 *         ...
 *     &lt;/folder&gt;
 *     &lt;channel&gt;
 *         &lt;source&gt;/jobs/&lt;/source&gt;
 *         &lt;documenttype&gt;jobs&lt;/documenttype&gt;
 *         &lt;displayuri&gt;/showjob.html&lt;/displayuri&gt;
 *         &lt;displayparam&gt;id&lt;/displayparam&gt;
 *     &lt;/channel&gt;
 * &lt;/index&gt;
 * </pre>
 * <p>In this example, an index with display name "Default (Online)" is configured.
 * The index is automatically updated when the CmsSearchManager is started as cron job
 * (<code>manual</code> here means that the index is not automatically updated).</p>
 * 
 * <p>The index contains published resources within the site with the root <code>/sites/default</code>.
 * Only resource data for the language "en" will be indexed using the appropriate analyzer.</p>
 * 
 * <p>Within the site, only resource data below the folder "/" will be indexed and only if
 * the resource has the document type "xmlpage" (Typically you will have to specify more 
 * documenttypes here or to leave it out completely in order to index all available documenttypes).</p>
 * 
 * <p>Additionally, the cos data of type "jobs" of the channel "jobs" will be indexed.
 * Note: For a channel specification, only one documenttype is allowed.
 * To access a cos data item in a search result, a uri will be formed using the
 * displayuri and displayid, i.e. <code>/showjob.html?id=....</code></p>
 * 
 * <p>Certainly, you can specify more than one folder or channel to index.</p>
 *   
 * @version $Revision: 1.15 $ $Date: 2004/07/02 16:05:08 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchIndex {
  
    /** Manual rebuild as default value. */
    public static String C_DEFAULT_REBUILD = "manual";
    
    /** Automatic rebuild. */
    public static String C_AUTO_REBUILD = "auto";    

    /** The cms object. */
    private CmsObject m_cms;

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
    
    /** The site of this index. */    
    //private String m_site;
      
    int warning = 0;
    
    private List m_sourceNames;
    
    /**
     * Creates a new CmsSearchIndex.<p>
     */
    public CmsSearchIndex() {
        
        m_sourceNames = new ArrayList();
    }
    
    /**
     * Initializes the search index.<p>
     * 
     * @param cms the Cms object
     */
    public void initialize(CmsObject cms) {
        
        m_cms = cms;
        
        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(OpenCms.getSearchManager().getDirectory() + "/" + m_name);
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
            highlighter= new CmsHighlightExtractor(new CmsHtmlHighlighter(), query, analyzer);
            
            rawContent = result.getRawContent();
         
            if (rawContent!= null) {
            
                int highlightFragmentSizeInBytes=60;
                int maxNumFragmentsRequired=5;
                String fragmentSeparator=".. ";
            
                excerpt =
                    highlighter.getBestFragments(
                        rawContent,
                        highlightFragmentSizeInBytes,
                        maxNumFragmentsRequired,
                        fragmentSeparator);
                excerpt = excerpt.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
            }
           
        } catch (Exception exc) {
            String message="[Analyzer: "+analyzer+"][Query: "+query+"][CmsHighlightExtractor: "+highlighter+"][RawContent: ";
            if (rawContent!=null) {
                message += rawContent.length()+"]";
            } else {
                message += rawContent+"]";
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
            documenttypes = OpenCms.getSearchManager().getDocumenttypes();
        }
        return documenttypes;
    }
    
    /**
     * Performs a search on the index.<p>
     * The result is returned as List with entries of type I_CmsSearchResult
     * 
     * @param searchQuery the search term to search the index
     * @return the List of results found or an empty list
     * @throws CmsException if something goes wrong
     */
    public List search(String searchQuery) throws CmsException {

        return search(searchQuery, null);        
    }      
    
    /**
     * Performs a search on the index within the given fields.<p>
     * The result is returned as List with entries of type I_CmsSearchResult
     * 
     * @param searchQuery the search term to search the index
     * @param fields the list of fields to search
     * @return the List of results found or an empty list
     * @throws CmsException if something goes wrong
     */
    public List search(String searchQuery, String fields) throws CmsException {

        ArrayList result = null;

        Map searchCache = OpenCms.getSearchManager().getResultCache();
        String key = m_cms.getRequestContext().currentUser().getName() + "_" 
            + m_cms.getRequestContext().getRemoteAddress() + "_" + m_name + "_" + searchQuery + "_" + fields;
            
        result = (ArrayList)searchCache.get(key);
        if (result != null) {
            return result;
        }
        
        // change the project     
        CmsRequestContext context = m_cms.getRequestContext();
        CmsProject currentProject = context.currentProject();
        context.setCurrentProject(m_cms.readProject(m_project));
        
        Searcher searcher = null;

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Searching for \"" + searchQuery + "\" in fields \"" + fields + "\" of index " + m_name);
        }
        
        try {
             
            Query query;
            
            if (fields != null) {
                
                BooleanQuery fieldsQuery = new BooleanQuery();
                String fList[] = org.opencms.util.CmsStringSubstitution.split(fields, " ");
                for (int i = 0; i < fList.length; i++) {
                    fieldsQuery.add(QueryParser.parse(searchQuery, fList[i], OpenCms.getSearchManager().getAnalyzer(m_locale)), false, false);
                }
                
                query = fieldsQuery;
                 
            } else {
                query = QueryParser.parse(searchQuery, I_CmsDocumentFactory.DOC_CONTENT, OpenCms.getSearchManager().getAnalyzer(m_locale));
            }
            
            searcher = new IndexSearcher(m_path);   
            Hits hits = searcher.search(query);
            double maxScore = -1.0;

            result = new ArrayList(hits.length());
            for (int i = 0, n = hits.length(); i < n; i++) { 
                try {
                
                    Document doc = hits.doc(i);                                       
                    CmsIndexResource resource = getIndexResource(doc);
                    
                    if (resource != null) {
                        maxScore = (maxScore < hits.score(i)) ? hits.score(i) : maxScore;
                        result.add(new CmsSearchResult(this, searchQuery, resource, doc, (int)((hits.score(i) / maxScore) * 100.0)));
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
        
        searchCache.put(key, result);
        return result;
    }
    
    /**
     * Returns a CmsIndexResource for a specified Lucene search result document.<p>
     * 
     * All index sources of this search index are iterated. The indexer of an 
     * index source then tries to convert the Lucene document into an index 
     * resource.<p>
     * 
     * @param doc the Lucene search result document
     * @return a CmsIndexResource, or null if no index source had an indexer configured that was able to convert the document
     * @throws Exception if something goes wrong
     * @see I_CmsIndexer#getIndexResource(Document)
     */
    protected CmsIndexResource getIndexResource(Document doc) throws Exception {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        String indexSourceName = null;
        CmsSearchIndexSource indexSource = null;
        I_CmsIndexer indexer = null;
        CmsIndexResource result = null;
        String className = null;

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {
            indexSourceName = (String)m_sourceNames.get(i);
            indexSource = searchManager.getIndexSource(indexSourceName);

            className = indexSource.getIndexerClassName();
            indexer = (I_CmsIndexer)Class.forName(className).newInstance();
            indexer.init(m_cms, null, null, null, null, null);
            result = indexer.getIndexResource(doc);

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
