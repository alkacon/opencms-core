/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2004/02/22 13:52:28 $
 * Version: $Revision: 1.11 $
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

import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.CmsHighlightExtractor;
import org.opencms.search.documents.CmsHtmlHighlighter;
import org.opencms.search.documents.I_CmsDocumentFactory;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;

import com.opencms.legacy.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
 * @version $Revision: 1.11 $ $Date: 2004/02/22 13:52:28 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchIndex {
  
    /** Manual rebuild as default value */
    private static String C_DEFAULT_REBUILD = "manual";

    /** The cms object */
    private CmsObject m_cms;

    /** The incremental mode for this index */
    private boolean m_incremental;

    /** The index manager */
    private CmsSearchManager m_indexManager;
    
    /** The language filter of this index */
    private String m_language;
    
    /** The name of this index */
    private String m_name;
                
    /** Path to index data */
    private String m_path;
    
    /** The project of this index */
    private String m_project;

    /** The rebuild mode for this index */
    private String m_rebuild;

    /** The site of this index */    
    private String m_site;
      
    /** The list of vfs paths to index */
    private List m_vfsSources;

    /** The list of cos channels to index */
    private List m_cosSources;
    
    /** Documenttypes of folders/channels */
    private Map m_documenttypes;
    
    /** Displayuri of channel content */
    private Map m_cosDisplayuri;
    
    /** Displayparam of channel content */
    private Map m_cosDisplayparam;
    
    /**
     * Constructor to create a new index instance.<p>
     * 
     * @param indexManager the index manager
     * @param cms the cms object
     * @param path the directory path in the server file system
     * @param configuration the configuration for the index
     * @throws CmsIndexException if mandatory configuration values missing
     */
    protected CmsSearchIndex(CmsSearchManager indexManager, CmsObject cms, String path, Map configuration) throws CmsIndexException {
       
        m_cms = cms;
        m_indexManager = indexManager;
        m_vfsSources = new ArrayList();
        m_cosSources = new ArrayList();
        m_documenttypes = new HashMap();
        m_cosDisplayuri = new HashMap();
        m_cosDisplayparam = new HashMap();

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("[" + this.getClass().getName() + "] " + "Initializing");
        }
                                    
        if ((m_name = (String)configuration.get("name")) == null) {
            throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Name undefined");
        }
        
        if ((m_project = (String)configuration.get("project")) == null) {
            throw new CmsIndexException("[" + this.getClass().getName() +"] " + "Project undefined");
        }

        if ((m_site = (String)configuration.get("site")) == null) {
            throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Site undefined");
        }

        List folders = null;
        try {
            folders = (List)configuration.get("folder");
        } catch (ClassCastException exc) {
            folders = Arrays.asList(new Map[] {(Map)configuration.get("folder")});
        }
        if (folders != null) {
            readFolders(folders);
        }
        
        List channels = null;
        try {
            channels = (List)configuration.get("channel");
        } catch (ClassCastException exc) {
            channels = Arrays.asList(new Map[] {(Map)configuration.get("channel")});
        }
        if (channels != null) {
            readChannels(channels);
        }
        
        if ((m_rebuild = (String)configuration.get("rebuild")) == null) {
            m_rebuild = C_DEFAULT_REBUILD;
        }
            
        if ((m_language = (String)configuration.get("lang")) == null) {
            m_language = null;
        }

        m_incremental = "true".equals(configuration.get("incremental"));

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(path + "/" + m_name);
    }   

    /**
     * Reads the folder configuration.<p>
     * 
     * @param folders the list of folder configurations
     * @throws CmsIndexException if something goes wrong
     */
    private void readFolders(List folders) throws CmsIndexException {
        
        for (Iterator i = folders.iterator(); i.hasNext();) {
            Map folder = (Map)i.next();
            
            String source;
            if ((source = (String)folder.get("source")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Folder source undefined");
            }
            
            Set documenttypes;
            try {
                documenttypes = new HashSet((List)folder.get("documenttype"));
            } catch (ClassCastException exc) {
                documenttypes = new HashSet(Arrays.asList(new String[] {(String)folder.get("documenttype") }));
            }
            
            m_vfsSources.add(source);
            m_documenttypes.put(source, documenttypes);
            
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Configured folder: " + source);
            }            
        }
    }

    /**
     * Reads the channel configuration.<p>
     * 
     * @param channels the list of folder configurations
     * @throws CmsIndexException if something goes wrong
     */
    private void readChannels(List channels) throws CmsIndexException {
        
        for (Iterator i = channels.iterator(); i.hasNext();) {
            Map folder = (Map)i.next();
            
            String source;
            if ((source = (String)folder.get("source")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Channel source undefined");
            }
            
            String documenttype;
            if ((documenttype = (String)folder.get("documenttype")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Channel documenttype undefined");
            }            
            
            String displayuri;
            if ((displayuri = (String)folder.get("displayuri")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Channel displayuri undefined");
            }  
            
            String displayparam;
            if ((displayparam = (String)folder.get("displayparam")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Channel displayparam undefined");
            }              
                        
            HashSet dtSet = new HashSet();
            dtSet.add(documenttype);
            m_documenttypes.put(source, dtSet);

            m_cosSources.add(source);
            m_cosDisplayuri.put(source, displayuri);
            m_cosDisplayparam.put(source, displayparam);
            
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Configured channel: " + source);
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
            analyzer = m_indexManager.getAnalyzer(m_language);
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
        return m_indexManager;
    }
        
    /**
     * Returns a new index writer for this index.<p>
     * 
     * @return a new instance of IndexWriter
     * @throws CmsIndexException if something goes wrong
     */
    protected IndexWriter getIndexWriter() throws CmsIndexException {
        
        IndexWriter indexWriter;
        Analyzer analyzer = m_indexManager.getAnalyzer(m_language);
        
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
    public String getLanguage() {
        return m_language;    
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
     * Gets the site of this index.<p>
     * 
     * @return the site of the index, i.e. "/sites/default"
     */
    public String getSite() {
        return m_site;
    }
        
    /**
     * Gets the vfs path list of this index.<p>
     * The list contains Strings with the startpoints for indexing. 
     * 
     * @return the vfs path list of the index
     */
    public List getFolders() {
        return (m_vfsSources != null) ? m_vfsSources : new ArrayList();
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
            documenttypes = m_indexManager.getDocumenttypes();
        }
        return documenttypes;
    }

    /**
     * Gets the cos channel list of this index.<p>
     * The list contains the cos channels for indexing.
     * 
     * @return the cos channel list of the index
     */
    public List getChannels() {
        return (m_cosSources != null) ? m_cosSources : new ArrayList();
    }
    
    /**
     * Gets the display uri of contents assigned to the given channel.<p>
     * 
     * @param channel the channel name
     * @return the display uri for contents
     */
    public String getChannelDisplayUri(String channel) {
        return (String)m_cosDisplayuri.get(channel);
    }
    
    /**
     * Get the display param of contents assigned to the given channel.<p>
     * 
     * @param channel the channel name
     * @return the display param for contents
     */
    public String getChannelDisplayparam(String channel) {
        return (String)m_cosDisplayparam.get(channel);
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

        Map searchCache = m_indexManager.getResultCache();
        String key = m_cms.getRequestContext().currentUser().getName() + "_" 
            + m_cms.getRequestContext().getRemoteAddress() + "_" + m_name + "_" + searchQuery + "_" + fields;
            
        result = (ArrayList)searchCache.get(key);
        if (result != null) {
            return result;
        }
        
        // change site root and context        
        CmsRequestContext context = m_cms.getRequestContext();
        CmsProject currentProject = context.currentProject();
        context.saveSiteRoot();
        context.setSiteRoot(m_site);
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
                    fieldsQuery.add(QueryParser.parse(searchQuery, fList[i], m_indexManager.getAnalyzer(m_language)), false, false);
                }
                
                query = fieldsQuery;
                 
            } else {
                query = QueryParser.parse(searchQuery, I_CmsDocumentFactory.DOC_CONTENT, m_indexManager.getAnalyzer(m_language));
            }
            
            searcher = new IndexSearcher(m_path);   
            Hits hits = searcher.search(query);
            double maxScore = -1.0;

            result = new ArrayList(hits.length());
            for (int i = 0; i < hits.length(); i++) { 
                try {
                
                    Document doc = hits.doc(i);
                    CmsIndexResource resource = null;
                    String channel = null;
                    String path = null;
                    Field f;
                    
                    if ((f = doc.getField(I_CmsDocumentFactory.DOC_CHANNEL)) != null) {
                        channel = f.stringValue();
                    }
                    
                    if ((f = doc.getField(I_CmsDocumentFactory.DOC_PATH)) != null) {
                        path = f.stringValue();
                    }
                    
                    if (channel != null) {
                        resource = CmsCosIndexer.readResource(m_cms, doc); 
                    } else {
                        CmsResource res = m_cms.readFileHeader(path);
                        if (m_cms.hasPermissions(res, I_CmsConstants.C_READ_ACCESS)) {
                            resource = new CmsVfsIndexResource(res);
                        }
                    }
                    
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
            
            context.restoreSiteRoot();
            context.setCurrentProject(currentProject);
        }
        
        searchCache.put(key, result);
        return result;
    }

    /**
     * Set the rebuild mode for this index.<p>
     * 
     * @param rebuild the rebuild mode (auto/manual)
     */
    public void setRebuildMode(String rebuild) {
        m_rebuild = rebuild;
    }
}
