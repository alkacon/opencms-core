/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchManager.java,v $
 * Date   : $Date: 2004/09/22 12:08:53 $
 * Version: $Revision: 1.24 $
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
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
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

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the general management and configuration of the search and 
 * indexing facilities in OpenCms.<p>
 * 
 * @version $Revision: 1.24 $ $Date: 2004/09/22 12:08:53 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchManager implements I_CmsScheduledJob, I_CmsEventListener {
    /** Configured analyzers for languages using &lt;analyzer&gt;. */
    private HashMap m_analyzers;

    /** The Admin cms object to index Cms resources. */
    private transient CmsObject m_cms;

    /** A map of document factory configurations. */
    private Map m_documentTypeConfigs;

    /** A map of document factories keyed by their matching Cms resource types and/or mimetypes. */
    private Map m_documentTypes;
    
    /** 
     * The package/class name of the class to highlight the search terms in the excerpt of a search result.
     * A highlighter is a class implementing org.opencms.search.documents.I_TermHighlighter.
     */
    private String m_highlighter;

    /** A list of search indexes. */
    private List m_indexes;

    /** Configured index sources. */
    private Map m_indexSources;
    
    /** The max. char. length of the excerpt in the search result. */
    private int m_maxExcerptLength;

    /** Path to index files below WEB-INF/. */
    private String m_path;

    /** The cache for storing search results. */
    private Map m_resultCache;

    /** The result cache size. */
    private String m_resultCacheSize;

    /** Timeout for abandoning indexing thread. */
    private String m_timeout;

    /**
     * Default constructer when called as cron job.<p>
     */
    public CmsSearchManager() {

        m_documentTypes = new HashMap();
        m_documentTypeConfigs = new HashMap();
        m_analyzers = new HashMap();
        m_indexes = new ArrayList();
        m_indexSources = new HashMap();

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
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
     * Adds a document type.<p>
     * 
     * @param documentType a document type
     */
    public void addDocumentTypeConfig(CmsSearchDocumentType documentType) {

        m_documentTypeConfigs.put(documentType.getName(), documentType);
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

    /**
     * Implements the event listener of this class.<p>
     * 
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                if (m_resultCache != null) {
                    m_resultCache.clear();
                }
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Lucene index manager catched event EVENT_CLEAR_CACHES");
                }
                break;

            default:
                // no operation
        }
    }  
    
    /**
     * Returns the CmsSearchAnalyzer Object.<p>
     * @param locale unique locale key to specify the CmsSearchAnalyzer in HashMap
     * @return the CmsSearchAnalyzer Object
     */
    public CmsSearchAnalyzer getCmsSearchAnalyzer(String locale) {

        return (CmsSearchAnalyzer)m_analyzers.get(locale);
    }
    
    /**
     * Returns an unmodifiable view (read-only) of the Analyzers Map.<p>
     *
     * @return an unmodifiable view (read-only) of the Analyzers Map
     */
    public Map getAnalyzers() {

        return Collections.unmodifiableMap(m_analyzers);
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
     * Returns a document type config.<p>
     * 
     * @param name the name of the document type config
     * @return the document type config.
     */
    public CmsSearchDocumentType getDocumentTypeConfig(String name) {

        return (CmsSearchDocumentType)m_documentTypeConfigs.get(name);
    }
    
    /**
     * Returns an unmodifiable view (read-only) of the DocumentTypeConfigs Map.<p>
     *
     * @return an unmodifiable view (read-only) of the DocumentTypeConfigs Map
     */
    public Map getDocumentTypeConfigs() {
        
        return Collections.unmodifiableMap(m_documentTypeConfigs);
    }
    
    /**
     * Returns the package/class name of the highlighter.<p>
     * 
     * A highlighter is a class implementing org.opencms.search.documents.I_TermHighlighter.<p>
     * 
     * @return the package/class name of the highlighter
     */
    public String getHighlighter() {

        return m_highlighter;
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
     * Returns a search index source for a specified source name.<p>
     * 
     * @param sourceName the name of the index source
     * @return a search index source
     */
    public CmsSearchIndexSource getIndexSource(String sourceName) {

        return (CmsSearchIndexSource)m_indexSources.get(sourceName);
    }
    
    /**
     * Returns the max. excerpt length.<p>
     *
     * @return the max excerpt length
     */
    public int getMaxExcerptLength() {

        return m_maxExcerptLength;
    }

    /**
     * Returns the result cache size.<p>
     *
     * @return the result cache size
     */
    public String getResultCacheSize() {

        return m_resultCacheSize;
    }
    
    /**
     * Returns an unmodifiable list of all configured indexes.<p>
     * 
     * @return unmodifiable list of configured indexes
     */
    public List getSearchIndexs() {

        return Collections.unmodifiableList(m_indexes);
    }
    
    /**
     * Returns an unmodifiable view (read-only) of the SearchIndexSources Map.<p>
     * 
     * @return an unmodifiable view (read-only) of the SearchIndexSources Map
     */
    public Map getSearchIndexSources() {
        
        return Collections.unmodifiableMap(m_indexSources);
    }
    
    /**
     * Returns the timeout to abandon threads indexing a resource.<p>
     *
     * @return the timeout to abandon threads indexing a resource
     */
    public String getTimeout() {

        return m_timeout;
    }

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
     * Method for automatically rebuilding indexes configured with <rebuild>auto</rebuild>.<p>
     * 
     * @param cms the cms object
     * @param parameters the parameters for the scheduled job
     * @return the finish message
     * @throws Exception if something goes wrong
     */
    public final String launch(CmsObject cms, ExtendedProperties parameters) throws Exception {

        // Note: launch is normally called with uninitialized object data,
        // so we have to create our own instance
        CmsSearchManager manager = OpenCms.getSearchManager();

        I_CmsReport report = null;
        boolean writeLog = parameters.getBoolean("writeLog", false);
        
        if (writeLog) {
            report = new CmsLogReport(I_CmsReport.C_BUNDLE_NAME, cms.getRequestContext().getLocale(), getClass());
        }

        manager.updateIndex(report);

        String finishMessage = "[" + this.getClass().getName() + "] " + "Reindexing finished.";
        if (OpenCms.getLog(this).isWarnEnabled()) {
            OpenCms.getLog(this).warn(finishMessage);
        }
        return finishMessage;
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
     * Sets the package/class name of the highlighter.<p>
     *
     * A highlighter is a class implementing org.opencms.search.documents.I_TermHighlighter.<p>
     *
     * @param highlighter the package/class name of the highlighter
     */
    public void setHighlighter(String highlighter) {

        m_highlighter = highlighter;
    }
    
    /**
     * Sets the max. excerpt length.<p>
     *
     * @param maxExcerptLength the max. excerpt length to set
     */
    public void setMaxExcerptLength(String maxExcerptLength) {

        try {
            m_maxExcerptLength = Integer.parseInt(maxExcerptLength);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error parsing max. excerpt length " + maxExcerptLength, e);
            }
            
            m_maxExcerptLength = 1024;
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
     * Sets the timeout to abandon threads indexing a resource.<p>
     * 
     * @param value the timeout in milliseconds
     */
    public void setTimeout(String value) {

        m_timeout = value;
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

                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Rebuilding of search index " + index.getName() + " failed!", e);
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
     * Returns an analyzer for the given language.<p>
     * The analyzer is selected according to the analyzer configuration.
     * 
     * @param locale a language id, i.e. de, en, it
     * @return the appropriate lucene analyzer
     * @throws CmsIndexException if something goes wrong
     */
    protected Analyzer getAnalyzer(String locale) throws CmsIndexException {

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
                analyzer = (Analyzer)analyzerClass.getDeclaredConstructor(new Class[] {String.class}).newInstance(
                    new Object[] {stemmerAlgorithm});
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
    protected I_CmsDocumentFactory getDocumentFactory(A_CmsIndexResource resource) {

        String documentTypeKey = resource.getDocumentKey();

        I_CmsDocumentFactory factory = (I_CmsDocumentFactory)m_documentTypes.get(documentTypeKey);
        if (factory == null) {
            factory = (I_CmsDocumentFactory)m_documentTypes.get(resource.getType() + "");
        }

        return factory;
    }

    /**
     * Returns the set of names of all configured documenttypes.<p>
     * 
     * @return the set of names of all configured documenttypes
     */
    protected Set getDocumentTypes() {

        Set names = new HashSet();
        for (Iterator i = m_documentTypes.values().iterator(); i.hasNext();) {
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

        m_documentTypes = new HashMap();

        List keys = new ArrayList(m_documentTypeConfigs.keySet());
        for (int i = 0, n = keys.size(); i < n; i++) {

            documenttype = (CmsSearchDocumentType)(m_documentTypeConfigs.get(keys.get(i)));
            name = documenttype.getName();

            try {
                className = documenttype.getClassName();
                resourceTypes = documenttype.getResourceTypes();
                mimeTypes = documenttype.getMimeTypes();

                if (name == null) {
                    throw new CmsIndexException("["
                        + this.getClass().getName()
                        + "] "
                        + "No name defined for documenttype");
                }
                if (className == null) {
                    throw new CmsIndexException("["
                        + this.getClass().getName()
                        + "] "
                        + "No class defined for documenttype");
                }

                if (resourceTypes.size() == 0) {
                    throw new CmsIndexException("["
                        + this.getClass().getName()
                        + "] "
                        + "No resourcetype/moduletype defined for documenttype");
                }

                try {
                    c = Class.forName(className);
                    documentFactory = (I_CmsDocumentFactory)c.getConstructor(
                        new Class[] {m_cms.getClass(), String.class}).newInstance(new Object[] {m_cms, name});
                } catch (ClassNotFoundException exc) {
                    throw new CmsIndexException("["
                        + this.getClass().getName()
                        + "] "
                        + "Documentclass "
                        + className
                        + " not found", exc);
                } catch (Exception exc) {
                    throw new CmsIndexException("["
                        + this.getClass().getName()
                        + "] "
                        + "Instanciation of documentclass "
                        + className
                        + " failed", exc);
                }

                for (int j = 0, m = resourceTypes.size(); j < m; j++) {

                    resourceType = (String)resourceTypes.get(j);
                    resourceTypeId = null;

                    try {
                        resourceTypeId = documentFactory.getDocumentKey(resourceType);
                    } catch (Exception exc) {
                        throw new CmsIndexException("["
                            + this.getClass().getName()
                            + "] "
                            + "Instanciation of resource type '"
                            + resourceType
                            + "' failed", exc);
                    }

                    if (mimeTypes.size() > 0) {
                        for (int k = 0, l = mimeTypes.size(); k < l; k++) {

                            String mimeType = (String)mimeTypes.get(k);

                            if (OpenCms.getLog(this).isDebugEnabled()) {
                                OpenCms.getLog(this)
                                    .debug(
                                        "Configured document class: "
                                            + className
                                            + " for "
                                            + resourceType
                                            + ":"
                                            + mimeType);
                            }

                            m_documentTypes.put(resourceTypeId + ":" + mimeType, documentFactory);
                        }
                    } else {
                        m_documentTypes.put(resourceTypeId + "", documentFactory);
                    }
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Configuration of documenttype " + name + " failed", e);
                }
            }
        }
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
}