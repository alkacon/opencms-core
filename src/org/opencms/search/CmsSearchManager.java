/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchManager.java,v $
 * Date   : $Date: 2004/02/22 13:52:28 $
 * Version: $Revision: 1.10 $
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
import org.opencms.file.CmsRegistry;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.LRUMap;
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
 * @version $Revision: 1.10 $ $Date: 2004/02/22 13:52:28 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchManager implements I_CmsCronJob, I_CmsEventListener {

    /** Configured analyzers for languages using <analyzer> */
    private HashMap m_analyzer;

    /** The cms object */
    private CmsObject m_cms;
    
    /** Configuration of the index manager */
    private Map m_config;
    
    /** Configured documenttypes for indexing using <documenttype> */
    private Map m_documenttypes;
    
    /** Configured resourcetype lists per documenttype */
    private Map m_resourcetypes;
    
    /** Configured mimetype lists per documenttype */
    private Map m_mimetypes;
           
    /** Configured indexes using <index> */
    private List m_indexes;

    /** Path to index files */
    private String m_path;

    /** Timeout for abandoning indexing thread */
    private String m_timeout;

    /** The cache for storing search results */
    private static Map m_resultCache = null;

    /** The vfs indexer */
    private I_CmsIndexer m_vfsIndexer;
    
    /** The cos indexer */
    private I_CmsIndexer m_cosIndexer;
    
    /*
     * The merge factor 
     * @see lucene documentation 
     */
    // private String m_mergeFactor;
    
    /**
     * Returns a new instance of the search manager.<p>
     * 
     * @param configuration configuration properties
     * @param cms the cms object
     * @return a new instance of the index manager
     */
    public static CmsSearchManager initialize(ExtendedProperties configuration, CmsObject cms) {
        // configuration currently not used
        if (false) {
            configuration.get(null);
        }
        
        return new CmsSearchManager(cms);
    }

    /**
     * Default constructer when called as cron job.<p>
     */
    public CmsSearchManager() {
        // must be initialized with cms object        
    }
    
    /**
     * Constructor to create a new instance of CmsSearchManager.<p>
     * The new manager instance is initialized based on the registry configuration.
     * 
     * @param cms the cms object
     */
    public CmsSearchManager(CmsObject cms) {
        
        List documenttypes;
        List analyzers;
        
        m_cms = cms;
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Initializing CmsSearchManager");
        }

        try {
            CmsRegistry registry = m_cms.getRegistry();
            m_config = registry.getSubNodeValues(registry.getSystemElement(), "search");
            
            if ((documenttypes = (List)m_config.get("documenttype")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No documenttypes defined");
            } else {
                readDocumenttypes(documenttypes);
            }
            
            if ((analyzers = (List)m_config.get("analyzer")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No analyzer defined");
            } else {
                readAnalyzer(analyzers);
            }
            
            if ((m_path = (String)m_config.get("directory")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Directory for storing index data not defined");
            }
            
            String vfsIndexer;
            if ((vfsIndexer = (String)m_config.get("vfsindexer")) == null) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Vfs indexer not defined");
            }
            try {
                m_vfsIndexer = (I_CmsIndexer)Class.forName(vfsIndexer).newInstance();
            } catch (Exception exc) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Cant instanciate vfs indexer", exc);
            }
            
            String cosIndexer;
            if ((cosIndexer = (String)m_config.get("cosindexer")) != null) {
                try {
                    m_cosIndexer = (I_CmsIndexer)Class.forName(cosIndexer).newInstance();
                } catch (Exception exc) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Cant instanciate cos indexer", exc);
                }
            }
            
            if ((m_timeout = (String)m_config.get("timeout")) == null) {
                m_timeout = "20000";
            }
            
            // currently not used    
            // m_mergeFactor = (String)m_config.get("mergefactor");
            
            String cacheSize = null;
            if ((cacheSize = (String)m_config.get("cache")) == null) {
                cacheSize="8";
            }
            
            if (m_resultCache == null) {
                LRUMap hashMap = new LRUMap(Integer.parseInt(cacheSize));
                m_resultCache = Collections.synchronizedMap(hashMap);
                if (OpenCms.getMemoryMonitor().enabled()) {
                    OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_resultCache", hashMap);
                }
            }
            
            try {
                m_indexes = (List)m_config.get("index");
            } catch (ClassCastException exc) {
                m_indexes = Arrays.asList(new Map[] {(Map) m_config.get("index")});
            }
        } catch (CmsException exc) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Index configuration failed", exc);
            }
        }
        
        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES
        });         

        if (OpenCms.getLog(this).isDebugEnabled()) {
            for (Iterator i = m_indexes.iterator(); i.hasNext();) {
                OpenCms.getLog(this).debug("Configured index: " + (((Map)i.next()).get("name")));
            }
        }        
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
     * @param language a language id, i.e. de, en, it
     * @return the appropriate lucene analyzer
     * @throws CmsIndexException if something goes wrong
     */
    protected Analyzer getAnalyzer (String language) throws CmsIndexException {
        
        Analyzer analyzer = null;
        
        Map analyzerConf = (Map)m_analyzer.get(language);
        if (analyzerConf == null) {
            analyzerConf = (Map)m_analyzer.get("*");
        }
        
        if (analyzerConf == null) {
            throw new CmsIndexException("No analyzer found for language " + language);
        }
            
        try {
            
            String className = (String)analyzerConf.get("class");
            Class analyzerClass = Class.forName(className);
            
            // added param for snowball analyzer
            String param = (String)analyzerConf.get("param");
            if (param != null) {
                analyzer = (Analyzer)analyzerClass.getDeclaredConstructor(new Class[] {String.class}).newInstance(new Object[] {param});
            } else {
                analyzer = (Analyzer)analyzerClass.newInstance();
            }
                
        } catch (Exception exc) {
            throw new CmsIndexException("Can't load analyzer " + (String)analyzerConf.get("class"), exc);    
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
    protected I_CmsDocumentFactory getDocumentFactory (CmsIndexResource resource) {

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
     * @throws CmsException if something goes wrong
     */
    public CmsSearchIndex getIndex(String indexName) throws CmsException {

        for (Iterator i = m_indexes.iterator(); i.hasNext();) {
            Map indexConfig = (Map)i.next();
            if (indexName.equals(indexConfig.get("name"))) {
                return new CmsSearchIndex (this, m_cms, m_path, indexConfig);
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
        for (Iterator i = m_indexes.iterator(); i.hasNext();) {
            indexNames.add(((Map)i.next()).get("name"));
        }
            
        return indexNames;
    }
    
    /**
     * Reads the analyzer configuration.<p>
     * 
     * @param analyzers the list of analyzer configurations
     */
    private void readAnalyzer(List analyzers) {
        
        m_analyzer = new HashMap();
        
        for (Iterator i = analyzers.iterator(); i.hasNext();) {
            Map analyzer = (Map)i.next();
            List languages;
            try {
                languages = (List)analyzer.get("lang");
            } catch (ClassCastException exc) {
                languages = Arrays.asList(new String[] {(String)analyzer.get("lang") });
            }

            if (languages != null) {
                for (Iterator l = languages.iterator(); l.hasNext();) {
                    m_analyzer.put(l.next(), analyzer);
                }
            } else {
                m_analyzer.put("*", analyzer);
            }
                
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Configured analyzer: " + (String)analyzer.get("class"));
            }
        }
    }
    
    /**
     * Reads the documenttype configuration.<p>
     * 
     * @param documenttypes the list of documenttype configurations
     */
    private void readDocumenttypes(List documenttypes) {
        
        m_documenttypes = new HashMap();
        m_resourcetypes = new HashMap();
        m_mimetypes = new HashMap();
        
        for (Iterator i = documenttypes.iterator(); i.hasNext();) {
            Map documenttype = (Map)i.next();
            String name = (String)documenttype.get("name");
            
            try {    
                String className = (String)documenttype.get("class");
                I_CmsDocumentFactory documentFactory = null;
                List resourceTypes;
                List mimeTypes;
                
                try {
                    resourceTypes = (List)documenttype.get("resourcetype");
                } catch (ClassCastException exc) {
                    resourceTypes = Arrays.asList(new String[] {(String)documenttype.get("resourcetype")});   
                }
                
                try {
                    mimeTypes = (List)documenttype.get("mimetype");
                } catch (ClassCastException exc) {
                    mimeTypes = Arrays.asList(new String[] {(String)documenttype.get("mimetype")});
                }
                
                if (name == null) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No name defined for documenttype");
                }
                if (className == null) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No class defined for documenttype");
                }
                    
                if (resourceTypes == null) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No resourcetype/moduletype defined for documenttype");
                }
    
                try {
                    Class c = Class.forName(className);
                    documentFactory = (I_CmsDocumentFactory)c.getConstructor(new Class[]{m_cms.getClass(), String.class}).newInstance(new Object[]{m_cms, name});
                } catch (ClassNotFoundException exc) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Documentclass " + className + " not found", exc);
                } catch (Exception exc) {
                    throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Instanciation of documentclass " + className + " failed", exc);
                }
    
                m_resourcetypes.put(name, resourceTypes);
                m_mimetypes.put(name, mimeTypes);
                
                for (Iterator r = resourceTypes.iterator(); r.hasNext();) {
               
                    String resourceType = (String)r.next();
                    String resourceTypeId;
                    
                    try {
                        resourceTypeId = documentFactory.getDocumentKey(resourceType);
                    } catch (Exception exc) {
                        throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Instanciation of resource type" + resourceType + " failed", exc);    
                    }
                    
                    if (mimeTypes != null) {
                        for (Iterator m = mimeTypes.iterator(); m.hasNext();) {
                            
                            String mimeType = (String)m.next();
        
                            if (OpenCms.getLog(this).isDebugEnabled()) {
                                OpenCms.getLog(this).debug("Configured document class: " + className + " for " + resourceType + ":" + mimeType);
                            }
                                    
                            m_documenttypes.put(resourceTypeId + ":" + mimeType, documentFactory);
                        }
                    } else {
                        m_documenttypes.put(resourceTypeId + "", documentFactory);
                    }
                }
            } catch (CmsException exc) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Configuration of documenttype " + name  + " failed", exc);
                }
            }
        }                       
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
        
        for (Iterator i = getIndexNames().iterator(); i.hasNext();) {
            
            CmsSearchIndex index = getIndex((String)i.next());
            
            if ("auto".equals(index.getRebuildMode())) {            
                updateIndex(index.getName(), report, wait);
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
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(String indexName, I_CmsReport report, boolean wait) throws CmsException {
                        
        CmsSearchIndex index = null;
        IndexWriter writer = null;
        
        if (report == null) {
            report = (I_CmsReport) new CmsLogReport();
        }        

        if (report != null) {
            report.print(report.key("search.indexing_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            report.print(indexName, I_CmsReport.C_FORMAT_HEADLINE);
            report.println(report.key("search.dots"), I_CmsReport.C_FORMAT_HEADLINE);
        }    
        index = getIndex(indexName);
        
        CmsRequestContext context = m_cms.getRequestContext();
        CmsProject currentProject = context.currentProject();
        
        String currentSiteRoot = context.getSiteRoot();
        context.setSiteRoot(index.getSite());
        context.setCurrentProject(m_cms.readProject(index.getProject()));
        if (report != null) {
            report.println(report.key("search.indexing_context") + context.getSiteRoot() + ", " + context.currentProject().getName(), I_CmsReport.C_FORMAT_NOTE);
        }
                        
        try {
            
            CmsIndexingThreadManager threadManager = new CmsIndexingThreadManager(this, report, Long.parseLong(m_timeout), indexName);
            writer = index.getIndexWriter();
            
            List folders = index.getFolders();
            for (Iterator i = folders.iterator(); i.hasNext();) {
                String vfsPath = (String)i.next();
                m_vfsIndexer.init(m_cms, null, writer, index, report, threadManager);
                m_vfsIndexer.updateIndex(vfsPath);
            }
            
            List channels = index.getChannels();
            for (Iterator i = channels.iterator(); i.hasNext();) {
                String cosChannel = (String)i.next();
                String documenttype = (String)(index.getDocumenttypes(cosChannel).toArray())[0];
                String resourcetype = (String)((List)m_resourcetypes.get(documenttype)).get(0);
                m_cosIndexer.init(m_cms, resourcetype, writer, index, report, threadManager);
                m_cosIndexer.updateIndex(cosChannel);
            }
            
            // wait for indexing threads
            while (wait && threadManager.isRunning()) {
                Thread.sleep(1000);
            }
            threadManager.reportStatistics();
                        
        } catch (Exception exc) {
            
            if (report != null) {
                report.println(report.key("search.indexing_failed"), I_CmsReport.C_FORMAT_WARNING);
            }
                
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Rebuilding index " + index.getName() + " failed.", exc);
            }
                
        } finally {
            if (writer != null) { 
                try {
                    writer.close();
                } catch (IOException exc) {
                    //
                }
            }
            context.setSiteRoot(currentSiteRoot);
            context.setCurrentProject(currentProject);
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
}
