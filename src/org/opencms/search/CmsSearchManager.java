/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchManager.java,v $
 * Date   : $Date: 2004/07/05 11:58:21 $
 * Version: $Revision: 1.18 $
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

import org.opencms.cron.I_CmsCronJob;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the general management and configuration of the search and 
 * indexing facilities in OpenCms.<p>
 * 
 * <p>The configuration is specified in the cms registry <code>registry.xml</code>
 * using the following tags:</p>
 * 
 * <pre>
 * &lt;search&gt;
 *     &lt;directory&gt;index&lt;/directory&gt;
 *     &lt;timeout&gt;60000&lt;/timeout&gt;
 *     &lt;cosindexer&gt;...&lt;/cosindexer&gt;
 *     &lt;vfsindexer&gt;...&lt;/vfsindexer&gt;
 *     &lt;documenttype&gt;
 *         (see below)
 *     &lt;/documenttype&gt;
 *     ...
 *     &lt;analyzer&gt;
 *         (see below)
 *     &lt;/analyzer&gt;
 *     ...
 *     &lt;index&gt;
 *         (see CmsSearchIndex)
 *     &lt;/index&gt;
 *     ...
 * &lt;/search&gt;
 * </pre>
 *
 * <p>The general search configuration specifies the folder in the server filesystem used to store
 * the lucene index files below the <code>WEB-INF</code> folder. The timeout value is used to abort
 * the indexing of a single resource if it exceeds the specified value.</p>
 *
 * <p>The documenttype entries are used to specify which document factory for lucene index documents
 * will be used for which OpenCms resource type and/or mimetype:</p>
 *
 * <pre>
 * &lt;documenttype&gt;
 *     &lt;name&gt;pdf&lt;/name&gt;
 *     &lt;class&gt;org.opencms.search.documents.CmsPdfDocument&lt;/class&gt;
 *     &lt;resourcetype&gt;org.opencms.file.CmsResourceTypeBinary&lt;/resourcetype&gt;
 *     &lt;resourcetype&gt;org.opencms.file.CmsResourceTypePlain&lt;/resourcetype&gt;
 *     &lt;mimetype&gt;application/pdf&lt;/mimetype&gt;
 * &lt;/documenttype&gt;
 * </pre>
 *
 * <p>In this example, the factory class <code>org.opencms.search.documents.CmsPdfDocument</code>
 * is used for cms resources of resourcetype <code>CmsResourceTypeBinary</code> or
 * <code>CmsResourceTypePlain</code>, both with the mime type <code>application/pdf</code>
 * (derived from the extension <code>.pdf</code> in the name of the resource).</p>
 * 
 * <p>For cos resources, the following form is used:</p>
 * 
 * <pre>
 * &lt;documenttype&gt;
 *     &lt;name&gt;jobs&lt;/name&gt;
 *     &lt;class&gt;org.opencms.search.documents.CmsCosDocument&lt;/class&gt;
 *     &lt;resourcetype&gt;de.alkacon.opencms.modules.lgt.jobs.CmsJobsContent&lt;/resourcetype&gt;
 * &lt;/documenttype&gt;
 * </pre>
 *
 * <p>In this case, the resourcetype specifies the content definition class 
 * (here: <code>CmsJobsContent</code>) used to access the cos data within a channel.</p>
 * 
 * <p>The analyzer entries are used to specify which lucene analyzer will be used for the language
 * specfied in the index configuration:</p>
 * 
 * <pre>
 * &lt;analyzer&gt;
 *     &lt;class&gt;org.apache.lucene.analysis.de.GermanAnalyzer&lt;/class&gt;
 *     &lt;lang&gt;de&lt;/lang&gt;
 * &lt;/analyzer&gt;
 * </pre>
 *
 * <p>The <code>GermanAnalyzer</code> will be used for analyzing the contents of resources
 * when building an index with "de" as specified language.</p>
 * 
 * @version $Revision: 1.18 $ $Date: 2004/07/05 11:58:21 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchManager implements I_CmsCronJob, I_CmsEventListener {

    /** Configured analyzers for languages using &lt;analyzer&gt;. */
    private HashMap m_analyzers;

    /** The Admin cms object to index Cms resources. */
    private CmsObject m_cms;
    
    /** Configured documenttypes for indexing using &lt;documenttype&gt;. */
    private Map m_documenttypes;
    
    private List m_documentTypeConfigs;
           
    /** Configured indexes using &lt;index&gt;. */
    private List m_indexes;

    /** Path to index files. */
    private String m_path;
    
    /** The result cache size. */
    private String m_resultCacheSize;

    /** Timeout for abandoning indexing thread. */
    private String m_timeout;

    /** The cache for storing search results. */
    private Map m_resultCache;

    /** Configured index sources. */
    private Map m_indexSources;    
    
    /**
     * Initializes the search manager.<p>
     * 
     * @param cms the cms object
     */
    public void initialize(CmsObject cms) {

        // store the Admin cms to index Cms resources
        m_cms = cms;

        // init. the search result cache
        LRUMap hashMap = new LRUMap(Integer.parseInt(m_resultCacheSize));
        m_resultCache = Collections.synchronizedMap(hashMap);

        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_resultCache", hashMap);
        }

        initAvailableDocumentTypes();
        initSearchIndexes();
    }
    
    /**
     * Initializes the configured search indexes.<p>
     * 
     * This initializes also the list of Cms resources types
     * to be indexed by an index source.<p>
     */
    protected void initSearchIndexes() {

        CmsSearchIndex index = null;

        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            index = (CmsSearchIndex)m_indexes.get(i);
            index.initialize();
        }
    }
    
    /**
     * Initializes the available Cms resource types to be indexed.<p>
     * 
     * A map stores document factories keyed by a string representing
     * a colon separated list of Cms resource types and/or mimetypes.<p>
     * 
     * The keys of this map are used to trigger a document factory to convert 
     * a Cms resource into a Lucene index document.<p>
     * 
     * A document factory is a class implementing the interface
     * {@link org.opencms.search.documents.I_CmsDocumentFactory}.<p>
     */
    protected void initAvailableDocumentTypes() {
                
        CmsSearchDocumentType documenttype = null;
        String className = null;
        String name = null;
        I_CmsDocumentFactory documentFactory = null;
        List resourceTypes = null;
        List mimeTypes = null; 
        Class c = null;
        String resourceType = null;
        String resourceTypeId = null;        
        
        m_documenttypes = new HashMap();
        
        for (int i = 0, n = m_documentTypeConfigs.size(); i < n; i++) {
            
            documenttype = (CmsSearchDocumentType)m_documentTypeConfigs.get(i);
            name = documenttype.getName();
            
            try {    
                className = documenttype.getClassName();
                resourceTypes = documenttype.getResourceTypes();
                mimeTypes = documenttype.getMimeTypes();
                
                if (name == null) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No name defined for documenttype");
                }
                if (className == null) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No class defined for documenttype");
                }
                    
                if (resourceTypes.size() == 0) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No resourcetype/moduletype defined for documenttype");
                }
    
                try {
                    c = Class.forName(className);
                    documentFactory = (I_CmsDocumentFactory)c.getConstructor(new Class[]{m_cms.getClass(), String.class}).newInstance(new Object[]{m_cms, name});
                } catch (ClassNotFoundException exc) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Documentclass " + className + " not found", exc);
                } catch (Exception exc) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Instanciation of documentclass " + className + " failed", exc);
                }
                
                for (int j = 0, m = resourceTypes.size(); j < m; j++) {
                               
                    resourceType = (String)resourceTypes.get(j);
                    resourceTypeId = null;
                    
                    try {
                        resourceTypeId = documentFactory.getDocumentKey(resourceType);
                    } catch (Exception exc) {
                        throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Instanciation of resource type '" + resourceType + "' failed", exc);    
                    }
                    
                    if (mimeTypes.size() > 0) {
                        for (int k = 0, l = mimeTypes.size(); k < l; k++) {
                            
                            String mimeType = (String)mimeTypes.get(k);
        
                            if (OpenCms.getLog(this).isDebugEnabled()) {
                                OpenCms.getLog(this).debug("Configured document class: " + className + " for " + resourceType + ":" + mimeType);
                            }
                                    
                            m_documenttypes.put(resourceTypeId + ":" + mimeType, documentFactory);
                        }
                    } else {
                        m_documenttypes.put(resourceTypeId + "", documentFactory);
                    }
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Configuration of documenttype " + name  + " failed", e);
                }
            }
        }                       
    }     

    /**
     * Default constructer when called as cron job.<p>
     */
    public CmsSearchManager() {

        m_documenttypes = new HashMap();
        m_documentTypeConfigs = new ArrayList();
        m_analyzers = new HashMap();
        m_indexes = new ArrayList();
        m_indexSources = new HashMap();

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_CLEAR_CACHES
        });
    }
    
    /**
     * Method for automatically rebuilding indexes configured with <rebuild>auto</rebuild>.<p>
     * 
     * @param cms the cms object
     * @param parameter a parameter string for the cron job (currently unused)
     * @return the finish message
     * @throws Exception if something goes wrong
     */
    public final String launch(CmsObject cms, String parameter) throws Exception {
        
        // Note: launch is normally called with uninitialized object data,
        // so we have to create our own instance
        CmsSearchManager manager = OpenCms.getSearchManager();
        
        I_CmsReport report = null;
        if (parameter != null && parameter.indexOf("log=on") >= 0) {
            report = new CmsLogReport("com.alkacon.search.workplace", cms.getRequestContext().getLocale(), getClass());
        } 
        
        manager.updateIndex(report);
        
        String finishMessage =  "[" + this.getClass().getName() + "] " + "Reindexing finished.";
        if (OpenCms.getLog(this).isWarnEnabled()) {
            OpenCms.getLog(this).warn(finishMessage);
        }    
        return finishMessage;
    }
             
    /**
     * Returns an analyzer for the given language.<p>
     * The analyzer is selected according to the analyzer configuration.
     * 
     * @param locale a language id, i.e. de, en, it
     * @return the appropriate lucene analyzer
     * @throws CmsIndexException if something goes wrong
     */
    protected Analyzer getAnalyzer (String locale) throws CmsIndexException {
        
        Analyzer analyzer = null;
        String className = null;
        
        CmsSearchAnalyzer analyzerConf = (CmsSearchAnalyzer)m_analyzers.get(locale);       
        if (analyzerConf == null) {
            throw new CmsIndexException("No analyzer found for language " + locale);
        }
            
        try {            
            className = analyzerConf.getClassName();
            Class analyzerClass = Class.forName(className);
            
            // added param for snowball analyzer
            String stemmerAlgorithm = analyzerConf.getStemmerAlgorithm();
            if (stemmerAlgorithm != null) {
                analyzer = (Analyzer)analyzerClass.getDeclaredConstructor(new Class[] {String.class}).newInstance(new Object[] {stemmerAlgorithm});
            } else {
                analyzer = (Analyzer)analyzerClass.newInstance();
            }
                
        } catch (Exception e) {
            throw new CmsIndexException("Can't load analyzer " + className, e);    
        }

        return analyzer;        
    }   

    /**
     * Returns a lucene document factory for given resource.<p>
     * The type of the document factory is selected by the type of the resource
     * and the mimetype of the resource content according to the documenttype configuration.
     * 
     * @param resource a cms resource
     * @return a lucene document factory or null
     */
    protected I_CmsDocumentFactory getDocumentFactory (A_CmsIndexResource resource) {

        String documentTypeKey = resource.getDocumentKey();
        
        I_CmsDocumentFactory factory = (I_CmsDocumentFactory)m_documenttypes.get(documentTypeKey);                           
        if (factory == null) {
            factory = (I_CmsDocumentFactory)m_documenttypes.get(resource.getType() + "");
        }
        
        return factory;
    }

    /**
     * Returns the set of names of all configured documenttypes.<p>
     * 
     * @return the set of names of all configured documenttypes
     */
    public Set getDocumenttypes () {
    
        Set names = new HashSet();
        for (Iterator i = m_documenttypes.values().iterator(); i.hasNext();) {
            I_CmsDocumentFactory factory = (I_CmsDocumentFactory)i.next();
            names.add(factory.getName());
        }
        
        return names;
    }
    
    /**
     * Returns the common cache for buffering search results.<p>
     * 
     * @return the cache
     */
    protected Map getResultCache() {        
        return m_resultCache;    
    }   
     
    /**
     * Returns the index belonging to the passed name.<p>
     * The index must exist already.
     * 
     * @param indexName then name of the index
     * @return an object representing the desired index
     */
    public CmsSearchIndex getIndex(String indexName) {

        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            CmsSearchIndex searchIndex = (CmsSearchIndex)m_indexes.get(i);
            
            if (indexName.equalsIgnoreCase(searchIndex.getName())) {
                return searchIndex;
            }
        }

        return null;
    }
    
    /**
     * Returns a search index source for a specified source name.<p>
     * 
     * @param sourceName the name of the index source
     * @return a search index source
     */
    public CmsSearchIndexSource getIndexSource(String sourceName) {
        
        return (CmsSearchIndexSource)m_indexSources.get(sourceName);
    }
    
    /**
     * Returns the names of all configured indexes.<p>
     * 
     * @return list of names
     */
    public List getIndexNames() {

        List indexNames = new ArrayList();
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            indexNames.add(((CmsSearchIndex)m_indexes.get(i)).getName());
        }

        return indexNames;
    }       

    /**
     * Updates all indexes that are configured in the registry.<p>
     * An index will be updated only if rebuild mode is set to auto.
     * 
     * @param report the report object to write messages or null
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(I_CmsReport report) throws CmsException {
        
        updateIndex(report, false);
    }

    /**
     * Updates all indexes that are configured in the registry.<p>
     * An index will be updated only if rebuild mode is set to auto.
     * 
     * @param report the report object to write messages or null
     * @param wait flag signals to wait until the indexing threads are finished
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(I_CmsReport report, boolean wait) throws CmsException {
        
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            CmsSearchIndex searchIndex = (CmsSearchIndex)m_indexes.get(i);
            
            if (CmsSearchIndex.C_AUTO_REBUILD.equals(searchIndex.getRebuildMode())) {            
                updateIndex(searchIndex.getName(), report, wait);
            }
        }    
    } 
    
    /**
     * Updates the index belonging to the passed name.<p>
     * If the index is not already created, it will be created, too.
     * If the rebuild flag is set to true, it will be rebuild ignoring 
     * the value of the rebuild configuration entry in the registry
     * Further configuration information about this index must be available 
     * in the registry.
     * 
     * @param indexName the name of the index
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(String indexName) throws CmsException {
        
        updateIndex(indexName, null);
    }
    
    /**
     * Updates the index belonging to the passed name.<p>
     * If the index is not already created, it will be created, too.
     * If the rebuild flag is set to true, it will be rebuild ignoring 
     * the value of the rebuild configuration entry in the registry
     * Further configuration information about this index must be available 
     * in the registry.
     * 
     * @param indexName the name of the index
     * @param report the report object to write messages or null
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(String indexName, I_CmsReport report) throws CmsException {
        
        updateIndex(indexName, report, false);
    }

    /**
     * Updates the index belonging to the passed name.<p>
     * If the index is not already created, it will be created, too.
     * If the rebuild flag is set to true, it will be rebuild ignoring 
     * the value of the rebuild configuration entry in the registry
     * Further configuration information about this index must be available 
     * in the registry.
     * 
     * @param indexName the name of the index
     * @param report the report object to write messages or null
     * @param wait flag signals to wait until the indexing threads are finished
     * @throws CmsException is something goes wrong
     */
    public void updateIndex(String indexName, I_CmsReport report, boolean wait) throws CmsException {
                        
        CmsSearchIndex index = null;
        CmsSearchIndexSource indexSource = null;
        List sourceNames = null;
        String sourceName = null;
        CmsIndexingThreadManager threadManager = null;
        I_CmsIndexer indexer = null;
        List resourceNames = null;
        String resourceName = null;
        IndexWriter writer = null;      
        String currentSiteRoot = null;
        CmsProject currentProject = null;
        CmsRequestContext context = m_cms.getRequestContext();      
        
        if (report == null) {
            report = new CmsLogReport();
        }        

        if (report != null) {
            report.print(report.key("search.indexing_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            report.print(indexName, I_CmsReport.C_FORMAT_HEADLINE);
            report.println(report.key("search.dots"), I_CmsReport.C_FORMAT_HEADLINE);
        }    
        
        // get the search index by name
        index = getIndex(indexName);
        
        // create a new index writer
        writer = index.getIndexWriter();
        
        // iterate over all search index sources of this search index
        sourceNames = index.getSourceNames();
        for (int i = 0, n = sourceNames.size(); i < n; i++) {
            try {             
                // get the search index source
                sourceName = (String)sourceNames.get(i);
                indexSource = (CmsSearchIndexSource)m_indexSources.get(sourceName);
                
                // save the current site root
                currentSiteRoot = context.getSiteRoot();
                // switch to the "/" root site
                context.setSiteRoot("/");
                
                // save the current project
                currentProject = context.currentProject();                               
                // switch to the configured project
                context.setCurrentProject(m_cms.readProject(index.getProject()));

                // create a new thread manager
                threadManager = new CmsIndexingThreadManager(report, Long.parseLong(m_timeout), indexName);
                
                // create an instance of the configured indexer class
                indexer = (I_CmsIndexer)Class.forName(indexSource.getIndexerClassName()).newInstance();                
                
                resourceNames = indexSource.getResourcesNames();
                for (int j = 0, m = resourceNames.size(); j < m; j++) {
                    resourceName = (String)resourceNames.get(j);
                    
                    // update the index
                    indexer.init(report, index, indexSource, writer, threadManager);
                    indexer.updateIndex(m_cms, resourceName);                
                }
                
                // wait for indexing threads to finish
                while (wait && threadManager.isRunning()) {
                    Thread.sleep(1000);
                }
                
                threadManager.reportStatistics();                
            } catch (Exception e) {
                if (report != null) {
                    report.println(report.key("search.indexing_failed"), I_CmsReport.C_FORMAT_WARNING);
                }
                    
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Rebuilding of search index " + index.getName() + " failed!", e);
                }                
            } finally {
                if (writer != null) { 
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // noop
                    }
                }    
                
                // switch back to the original project
                context.setCurrentProject(currentProject);
                if (currentSiteRoot != null) {
                    // switch back to the original site root
                    context.setSiteRoot(currentSiteRoot);
                }
            }
        }
        
        // clear the cache for search results
        m_resultCache.clear();           
    }

    /**
     * Implements the event listener of this class.<p>
     * 
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                m_resultCache.clear();
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Lucene index manager catched event EVENT_CLEAR_CACHES");
                }
                break;
                
            default:
                // no operation
        }
    }
    
    /**
     * Sets the result cache size.<p>
     * 
     * @param value the result cache size
     */
    public void setResultCacheSize(String value) {
        
        m_resultCacheSize = value;
    }
    
    /**
     * Sets the name of the directory below WEB-INF/ where the search indexes are stored.<p>
     * 
     * @param value the name of the directory below WEB-INF/ where the search indexes are stored
     */
    public void setDirectory(String value) {
        
        m_path = value;
    }
    
    /**
     * Returns the name of the directory below WEB-INF/ where the search indexes are stored.<p>
     * 
     * @return the name of the directory below WEB-INF/ where the search indexes are stored
     */
    public String getDirectory() {
        
        return m_path;
    }    
    
    /**
     * Sets the timeout to abandon threads indexing a resource.<p>
     * 
     * @param value the timeout in milliseconds
     */
    public void setTimeout (String value) {
        
        m_timeout = value;
    }
    
    /**
     * Adds a document type.<p>
     * 
     * @param documentType a document type
     */
    public void addDocumentTypeConfig(CmsSearchDocumentType documentType) {    
        
        m_documentTypeConfigs.add(documentType);
    }
    
    /**
     * Adds an analyzer.<p>
     * 
     * @param analyzer an analyzer
     */
    public void addAnalyzer(CmsSearchAnalyzer analyzer) {
        
        m_analyzers.put(analyzer.getLocale(), analyzer);
    }
    
    /**
     * Adds a search index configuration.<p>
     * 
     * @param searchIndex a search index configuration
     */
    public void addSearchIndex(CmsSearchIndex searchIndex) {
        
        m_indexes.add(searchIndex);
    }
    
    /**
     * Adds a search index source configuration.<p>
     * 
     * @param searchIndexSource a search index source configuration
     */
    public void addSearchIndexSource(CmsSearchIndexSource searchIndexSource) {
        
        m_indexSources.put(searchIndexSource.getName(), searchIndexSource);
    }   
    
}
