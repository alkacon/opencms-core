/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchManager.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.CmsCosDocument;
import org.opencms.search.documents.I_CmsDocumentFactory;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRegistry;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.I_CmsResourceType;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.LRUMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/13 13:41:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsSearchManager implements I_CmsCronJob, I_CmsEventListener {

    /*
     * Configured analyzers for languages using <analyzer> 
     */
    private HashMap m_analyzer;

    /*
     * The cms object
     */
    private CmsObject m_cms;
    
    /*
     * Configuration of the index manager
     */
    private Map m_config;
    
    /*
     * Configured documenttypes for indexing using <documenttype>
     */
    private HashMap m_documenttypes;
           
    /*
     * Configured indexes using <index>
     */
    private List m_indexes;

    /*
     * Path to index files
     */
    private String m_path;

    /*
     * Timeout for abandoning indexing thread
     */
    private String m_timeout;

    /*
     * The cache for storing search results
     */
    private static Map m_resultCache = null;

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
     * 
     * @throws CmsException if something goes wrong
     */
    public static CmsSearchManager initialize(ExtendedProperties configuration, CmsObject cms) throws CmsException {
        // configuration currently not used
        if (false) {
            configuration.get(null);
        }
        
        return new CmsSearchManager(cms);
    }

    /**
     * Default constructer when called for cron job.<p>
     */
    public CmsSearchManager() {
        // must be initialized with cms object        
    }
    
    /**
     * Constructor to create a new instance of CmsSearchManager.<p>
     * The new manager is initialized based on the registry configuration.
     * 
     * @param cms the cms object
     * @throws CmsException if the configuration is invalid
     */
    public CmsSearchManager(CmsObject cms) throws CmsException {
        
        List documenttypes;
        List analyzers;
        
        m_cms = cms;
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Initializing CmsSearchManager");
        }
        
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
     * Updates all indexes that are configured in the registry.<p>
     * An index will be updated only if rebuild mode is set to auto.
     * 
     * @param report the report object to write messages or null
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(I_CmsReport report) throws CmsException {
        
        for (Iterator i = getIndexNames().iterator(); i.hasNext();) {
            
            CmsSearchIndex index = getIndex((String)i.next());
            
            if ("auto".equals(index.getRebuildMode())) {            
                updateIndex(index.getName(), report);
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
     * Creates new index entries for all vfs resources below the given path.<p>
     * 
     * @param writer the write used to write to the index
     * @param path the path to the root of the subtree to index
     * @param index the current index
     * @param report report for writi9ng out information or null
     * @param threadManager the thread manager for controlling the indexing threads
     * @throws CmsIndexException if something goes wrong
     */
    private void createVfsSubtreeIndex(IndexWriter writer, String path, CmsSearchIndex index, I_CmsReport report, CmsIndexingThreadManager threadManager) throws CmsIndexException {
        
        boolean folderReported = false;
        
        try {
            Vector resources = m_cms.getResourcesInFolder(path);
            CmsResource res;
            
            // process resources
            for (int i = 0; i < resources.size(); i++) {
                
                res = (CmsResource)resources.get(i);
                if (res instanceof CmsFolder) {                    
                    createVfsSubtreeIndex(writer, m_cms.getRequestContext().removeSiteRoot(res.getRootPath()), index, report, threadManager);
                    continue;
                } 

                if (report != null && !folderReported) {
                    report.print(report.key("search.indexing_folder"), I_CmsReport.C_FORMAT_NOTE);
                    report.println(path, I_CmsReport.C_FORMAT_DEFAULT);
                    folderReported = true;
                }

                if (report != null) {
                    report.print("( " + (threadManager.getCounter()+1) + " )", I_CmsReport.C_FORMAT_NOTE);
                    report.print(report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
                    report.print(res.getName(), I_CmsReport.C_FORMAT_DEFAULT);
                    report.print(report.key("search.dots"), I_CmsReport.C_FORMAT_DEFAULT);
                }
                
                CmsIndexResource ires = new CmsIndexResource(res); 
                threadManager.createIndexingThread(writer, ires, index);
            }
                                
        } catch (CmsIndexException exc) {
                                
            if (report != null) {
                report.println();
                report.println(report.key("search.indexing_file_failed") + " : " + exc.getMessage(), 
                    I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

        } catch (CmsException exc) {
                                
            if (report != null) {
                report.println(report.key("search.indexing_folder") + path + report.key("search.indexing_folder_failed") + " : " + exc.getMessage(), 
                    I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

        } catch (Exception exc) {
            
            if (report != null) {
                report.println(report.key("search.indexing.folder.failed"), I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }
                            
            throw new CmsIndexException("Indexing contents of " + path + " failed.", exc);
        }
    }
    
    /**
     * Writes the index for a single file.<p>
     * 
     * @param writer the writer to write out index data
     * @param res the resource
     * @param fileCounter array of progress counters
     * @param report the report for progress reporting
     *//*
    private void indexFile(IndexWriter writer, CmsResource res, I_CmsReport report, int fileCounter[]) {
    
        CmsIndexingThread thread = new CmsIndexingThread(this, writer, res, report);

        try {
            thread.start();
            thread.join(Long.parseLong(m_timeout));
            
            if (thread.isAlive()) {
                
                if (OpenCms.getLog(this).isDebugEnabled())
                    OpenCms.getLog(this).debug("Timeout while indexing file " + res.getRootPath() + ", abandoning thread");
                   
                report.println();
                report.println(report.key("search.indexing_file_failed") + " : " + "Timeout while indexing file " + res.getRootPath() + ", abandoning thread",
                    I_CmsReport.C_FORMAT_WARNING);
                    
                thread.interrupt();
                fileCounter[1]++;
            }
            
            fileCounter[0]++;
                      
        } catch (InterruptedException exc) {
            // noop
        }
    }
    */
/*    
    private void indexFile(IndexWriter writer, CmsResource res, I_CmsReport report) {

        I_CmsDocumentFactory documentFactory = getDocumentFactory(res);
                
        if (report != null) {
            report.print(report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
            report.print(res.getName(), I_CmsReport.C_FORMAT_DEFAULT);
            report.print(report.key("search.dots"), I_CmsReport.C_FORMAT_NOTE);
        }
                        
        if (OpenCms.getLog(this).isDebugEnabled())
            OpenCms.getLog(this).debug("Indexing " + res.getRootPath());
                    
        if (documentFactory != null) {
            try {
                        
                writer.addDocument(documentFactory.newInstance(res));
                if (report != null)
                    report.println(report.key("search.indexing_file_end"), I_CmsReport.C_FORMAT_OK);
                        
            } catch (Exception exc) {
                        
                if (report != null) {
                    report.println(report.key("search.indexing_file_failed") + " : " + exc.getLocalizedMessage(), 
                        I_CmsReport.C_FORMAT_WARNING);
                }    
                if (OpenCms.getLog(this).isWarnEnabled())
                    OpenCms.getLog(this).warn("Failed to index " + res.getRootPath(), exc);
            }
        } else {
    
            if (report != null)
                report.println(report.key("search.indexing_file_skipped"), I_CmsReport.C_FORMAT_NOTE);
                        
            if (OpenCms.getLog(this).isWarnEnabled())
                OpenCms.getLog(this).warn("Skipped " + res.getRootPath() + ", no matching documenttype");
        }          
    }
*/    
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

        String documentTypeKey;
        if (resource.getObject() instanceof CmsMasterDataSet) {
            documentTypeKey = "COS" + resource.getType(); 
        } else {
            documentTypeKey = "VFS" + resource.getType() + ":" + resource.getMimetype();
        }
        
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
     * @throws CmsIndexException if a documenttype configuration is invalid
     */
    private void readDocumenttypes(List documenttypes) throws CmsIndexException {
        
        m_documenttypes = new HashMap();
        
        for (Iterator i = documenttypes.iterator(); i.hasNext();) {
            Map documenttype = (Map)i.next();
            String name = (String)documenttype.get("name");
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
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "No resourcetype defined for documenttype");
            }

            try {
                Class c = Class.forName(className);
                documentFactory = (I_CmsDocumentFactory)c.getConstructor(new Class[]{m_cms.getClass(), String.class}).newInstance(new Object[]{m_cms, name});
            } catch (ClassNotFoundException exc) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Documentclass " + className + " not found", exc);
            } catch (Exception exc) {
                throw new CmsIndexException("[" + this.getClass().getName() + "] " + "Instanciation of documentclass " + className + " failed", exc);
            }
            
            for (Iterator r = resourceTypes.iterator(); r.hasNext();) {
           
                String resourceType = (String)r.next();
                String resourceTypeId;
                
                try {
                    if (documentFactory instanceof CmsCosDocument) {
                        resourceTypeId = "COS" + ((CmsMasterContent)Class.forName(resourceType).newInstance()).getSubId();
                    } else {
                        resourceTypeId = "VFS" + ((I_CmsResourceType)Class.forName(resourceType).newInstance()).getResourceType();
                    }
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
     * @param report the report object to write messages or null
     * @throws CmsException if something goes wrong
     */
    public void updateIndex(String indexName, I_CmsReport report) throws CmsException {
                        
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
        int currentProject = context.currentProject().getId();
        
        String currentSiteRoot = context.getSiteRoot();
        context.setSiteRoot(index.getSite());
        context.setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        if (report != null) {
            report.println(report.key("search.indexing_context") + context.getSiteRoot() + ", " + context.currentProject().getName(), I_CmsReport.C_FORMAT_NOTE);
        }
                        
        try {
            
            CmsIndexingThreadManager threadManager = new CmsIndexingThreadManager(this, report, Long.parseLong(m_timeout));
            List sources = index.getSources();
            writer = index.getIndexWriter();
            
            for (Iterator i = sources.iterator(); i.hasNext();) {
                String vfsPath = (String)i.next();
                createVfsSubtreeIndex(writer, vfsPath, index, report, threadManager);
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
