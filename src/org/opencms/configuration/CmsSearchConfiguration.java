/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsSearchConfiguration.java,v $
 * Date   : $Date: 2005/03/15 18:05:54 $
 * Version: $Revision: 1.8 $
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
 
package org.opencms.configuration;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchAnalyzer;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;


/**
 * Lucene search configuration class.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.8 $
 * @since 5.3.5
 */
public class CmsSearchConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** The name of the DTD for this configuration. */
    private static final String C_CONFIGURATION_DTD_NAME = "opencms-search.dtd";
    
    /** The name of the default XML file for this configuration. */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms-search.xml";
    
    /** The configured search manager. */
    private CmsSearchManager m_searchManager;    
    
    private static final String N_SEARCH = "search";
    private static final String N_CACHE = "cache";
    private static final String N_DIRECTORY = "directory";
    private static final String N_TIMEOUT = "timeout";
    private static final String N_DOCUMENTTYPE = "documenttype";
    private static final String N_DOCUMENTTYPES = "documenttypes";
    private static final String N_DOCUMENTTYPES_INDEXED = "documenttypes-indexed";    
    private static final String N_NAME = "name";
    private static final String N_CLASS = "class";
    private static final String N_LOCALE = "locale";
    private static final String N_RESOURCETYPES = "resourcetypes";
    private static final String N_RESOURCETYPE = "resourcetype";
    private static final String N_MIMETYPES = "mimetypes";
    private static final String N_MIMETYPE = "mimetype";
    private static final String N_ANALYZERS = "analyzers";
    private static final String N_ANALYZER = "analyzer";
    private static final String N_STEMMER = "stemmer";
    private static final String N_INDEXES = "indexes";
    private static final String N_INDEX = "index";
    private static final String N_REBUILD = "rebuild";
    private static final String N_PROJECT = "project";
    private static final String N_SOURCES = "sources";
    private static final String N_SOURCE = "source";
    private static final String N_INDEXSOURCES = "indexsources";
    private static final String N_INDEXSOURCE = "indexsource";
    private static final String N_INDEXER = "indexer";
    private static final String N_RESOURCES = "resources";   
    private static final String C_XPATH_SEARCH = "*/" + N_SEARCH;
    private static final String N_EXCERPT = "excerpt";
    private static final String N_HIGHLIGHTER = "highlighter";
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsSearchConfiguration() {
        
        setXmlFileName(C_DEFAULT_XML_FILE_NAME);        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Search configuration : initialized");
        }
    }    

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {
        
        String xPath = null;
        
        // add finish rule
        digester.addCallMethod(C_XPATH_SEARCH, "initializeFinished"); 
        
        // creation of the search manager        
        digester.addObjectCreate(C_XPATH_SEARCH, CmsSearchManager.class); 
        
        // search manager finished
        digester.addSetNext(C_XPATH_SEARCH, "setSearchManager");
        
        // result cache size rule
        digester.addCallMethod(C_XPATH_SEARCH + "/" + N_CACHE, "setResultCacheSize", 0);
        
        // directory rule
        digester.addCallMethod(C_XPATH_SEARCH + "/" + N_DIRECTORY, "setDirectory", 0);
        
        // timeout rule
        digester.addCallMethod(C_XPATH_SEARCH + "/" + N_TIMEOUT, "setTimeout", 0);
        
        // rule for the max. char. lenght of the search result excerpt
        digester.addCallMethod(C_XPATH_SEARCH + "/" + N_EXCERPT, "setMaxExcerptLength", 0);
        
        // rule for the highlighter to highlight the search terms in the excerpt of the search result
        digester.addCallMethod(C_XPATH_SEARCH + "/" + N_HIGHLIGHTER, "setHighlighter", 0);        
        
        // document type rule
        xPath = C_XPATH_SEARCH + "/" + N_DOCUMENTTYPES + "/" + N_DOCUMENTTYPE;
        digester.addObjectCreate(xPath, CmsSearchDocumentType.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_MIMETYPES + "/" + N_MIMETYPE, "addMimeType", 0);
        digester.addCallMethod(xPath + "/" + N_RESOURCETYPES + "/" + N_RESOURCETYPE, "addResourceType", 0);
        digester.addSetNext(xPath, "addDocumentTypeConfig");
        
        // analyzer rule
        xPath = C_XPATH_SEARCH + "/" + N_ANALYZERS + "/" + N_ANALYZER;
        digester.addObjectCreate(xPath, CmsSearchAnalyzer.class);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_STEMMER, "setStemmerAlgorithm", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocale", 0);
        digester.addSetNext(xPath, "addAnalyzer"); 
        
        // search index rule
        xPath = C_XPATH_SEARCH + "/" + N_INDEXES + "/" + N_INDEX;
        digester.addObjectCreate(xPath, CmsSearchIndex.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_REBUILD, "setRebuildMode", 0);
        digester.addCallMethod(xPath + "/" + N_PROJECT, "setProjectName", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocale", 0);
        digester.addCallMethod(xPath + "/" + N_SOURCES + "/" + N_SOURCE, "addSourceName", 0);
        digester.addSetNext(xPath, "addSearchIndex");
        
        // search index source rule
        xPath = C_XPATH_SEARCH + "/" + N_INDEXSOURCES + "/" + N_INDEXSOURCE;
        digester.addObjectCreate(xPath, CmsSearchIndexSource.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_INDEXER, "setIndexerClassName", 1); 
        digester.addCallParam(xPath + "/" + N_INDEXER, 0, N_CLASS);
        digester.addCallMethod(xPath + "/" + N_RESOURCES + "/" + N_RESOURCE, "addResourceName", 0);
        digester.addCallMethod(xPath + "/" + N_DOCUMENTTYPES_INDEXED + "/" + N_NAME, "addDocumentType", 0);
        digester.addSetNext(xPath, "addSearchIndexSource");
        
        // generic <param> parameter rules
        digester.addCallMethod("*/" + I_CmsXmlConfiguration.N_PARAM, I_CmsConfigurationParameterHandler.C_ADD_PARAMETER_METHOD, 2);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 1);         
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // add <search> node
        Element searchElement = parent.addElement(N_SEARCH);
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // initialized OpenCms instance is available, use latest values
            m_searchManager = OpenCms.getSearchManager();            
        }
        
        // add <cache> element
        searchElement.addElement(N_CACHE).addText(m_searchManager.getResultCacheSize());
        // add <directory> element
        searchElement.addElement(N_DIRECTORY).addText(m_searchManager.getDirectory());
        // add <timeout> element
        searchElement.addElement(N_TIMEOUT).addText(m_searchManager.getTimeout());
        // add <exerpt> element
        searchElement.addElement(N_EXCERPT).addText(String.valueOf(m_searchManager.getMaxExcerptLength()));
        // add <highlighter> element
        searchElement.addElement(N_HIGHLIGHTER).addText(m_searchManager.getHighlighter().getClass().getName());
        
        // <documenttypes> 
        Element documenttypesElement = searchElement.addElement(N_DOCUMENTTYPES);
        List docTypeKeyList = new ArrayList(m_searchManager.getDocumentTypeConfigs().keySet());
        Collections.sort(docTypeKeyList);
        List sortedDocTypeList = new ArrayList();
        Iterator i = docTypeKeyList.iterator();
        while (i.hasNext()) {
            CmsSearchDocumentType currDocType = m_searchManager.getDocumentTypeConfig((String)i.next());
            sortedDocTypeList.add(currDocType);
        }
        Iterator docTypeIterator = sortedDocTypeList.iterator();        
        while (docTypeIterator.hasNext()) {
            CmsSearchDocumentType currSearchDocType = (CmsSearchDocumentType) docTypeIterator.next();
            // add the next <documenttype> element
            Element documenttypeElement = documenttypesElement.addElement(N_DOCUMENTTYPE);
            // add <name> element
            documenttypeElement.addElement(N_NAME).addText(currSearchDocType.getName());
            // add <class> element
            documenttypeElement.addElement(N_CLASS).addText(currSearchDocType.getClassName());
            // add <mimetypes> element
            Element mimetypesElement = documenttypeElement.addElement(N_MIMETYPES);
            // get the list of mimetypes to trigger the document factory class 
            Iterator mimeTypesIterator = currSearchDocType.getMimeTypes().iterator();
            while (mimeTypesIterator.hasNext()) {
                // add <mimetype> element(s)
                mimetypesElement.addElement(N_MIMETYPE).addText((String)mimeTypesIterator.next());
            }  
            // add <resourcetypes> element
            Element restypesElement = documenttypeElement.addElement(N_RESOURCETYPES);
            // get the list of Cms resource types to trigger the document factory
            Iterator resTypesIterator = currSearchDocType.getResourceTypes().iterator();
            while (resTypesIterator.hasNext()) {
                // add <resourcetype> element(s)
                restypesElement.addElement(N_RESOURCETYPE).addText((String)resTypesIterator.next());
            }  
        }
        // </documenttypes> 
        
        // <analyzers> 
        Element analyzersElement = searchElement.addElement(N_ANALYZERS);
        List analyzerList = new ArrayList(m_searchManager.getAnalyzers().keySet());
        // sort Analyzers in ascending order
        Collections.sort(analyzerList);        
        Iterator analyzersIterator = analyzerList.iterator();
        while (analyzersIterator.hasNext()) {
            CmsSearchAnalyzer searchAnalyzer = m_searchManager.getCmsSearchAnalyzer((String)analyzersIterator.next());
            // add the next <analyzer> element
            Element analyzerElement = analyzersElement.addElement(N_ANALYZER);
            // add <class> element
            analyzerElement.addElement(N_CLASS).addText(searchAnalyzer.getClassName());
            if (searchAnalyzer.getStemmerAlgorithm()!=null) {
                // add <stemmer> element
                analyzerElement.addElement(N_STEMMER).addText(searchAnalyzer.getStemmerAlgorithm());
            }
            // add <locale> element
            analyzerElement.addElement(N_LOCALE).addText(searchAnalyzer.getLocale());
        }
        // </analyzers>

        // <indexes>
        Element indexesElement = searchElement.addElement(N_INDEXES);
        Iterator indexIterator  = m_searchManager.getSearchIndexs().iterator();
        while (indexIterator.hasNext()) {
            CmsSearchIndex searchIndex = (CmsSearchIndex) indexIterator.next();
            // add the next <index> element
            Element indexElement = indexesElement.addElement(N_INDEX);
            // add <name> element
            indexElement.addElement(N_NAME).addText(searchIndex.getName());
            // add <rebuild> element
            indexElement.addElement(N_REBUILD).addText(searchIndex.getRebuildMode());
            // add <project> element
            indexElement.addElement(N_PROJECT).addText(searchIndex.getProject());
            // add <locale> element
            indexElement.addElement(N_LOCALE).addText(searchIndex.getLocale());
            // add <sources> element
            Element sourcesElement = indexElement.addElement(N_SOURCES);
            // iterate above sourcenames
            Iterator sourcesIterator = searchIndex.getSourceNames().iterator();
            while (sourcesIterator.hasNext()) {
                // add <source> element
                sourcesElement.addElement(N_SOURCE).addText((String)sourcesIterator.next());
            }
            // iterate additional params
            Map indexConfiguration = searchIndex.getConfiguration();
            if (indexConfiguration != null) {
                Iterator it = indexConfiguration.keySet().iterator();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    String value = indexConfiguration.get(name).toString();
                    Element paramNode = indexElement.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }
        // </indexes>
        
        // <indexsources>
        Element indexsourcesElement = searchElement.addElement(N_INDEXSOURCES);
        List indexSources = new ArrayList(m_searchManager.getSearchIndexSources().values());
        Iterator indexsourceIterator = indexSources.iterator();
        while (indexsourceIterator.hasNext()) {
            CmsSearchIndexSource searchIndexSource = (CmsSearchIndexSource)indexsourceIterator.next();
            // add <indexsource> element(s)
            Element indexsourceElement = indexsourcesElement.addElement(N_INDEXSOURCE);
            // add <name> element
            indexsourceElement.addElement(N_NAME).addText(searchIndexSource.getName());
            // add <indexer class=""> element
            Element indexerElement = indexsourceElement.addElement(N_INDEXER).addAttribute(N_CLASS, searchIndexSource.getIndexerClassName());
            Map params = searchIndexSource.getParams();
            Iterator paramIterator = params.keySet().iterator();
            while (paramIterator.hasNext()) {
                String paramKey = (String) paramIterator.next();
                // add <param name=""> element(s)                
                indexerElement.addElement(I_CmsXmlConfiguration.N_PARAM).addAttribute(I_CmsXmlConfiguration.A_NAME, paramKey).addText((String)params.get(paramKey));
            }
            // add <resources> element
            Element resourcesElement = indexsourceElement.addElement(N_RESOURCES);
            Iterator resourceIterator = searchIndexSource.getResourcesNames().iterator();
            while (resourceIterator.hasNext()) {
                // add <resource> element(s)
                resourcesElement.addElement(N_RESOURCE).addText((String)resourceIterator.next());                
            }
            // add <documenttypes-indexed> element
            Element doctypes_indexedElement = indexsourceElement.addElement(N_DOCUMENTTYPES_INDEXED);
            Iterator doctypesIterator = searchIndexSource.getDocumentTypes().iterator();
            while (doctypesIterator.hasNext()) {
                // add <name> element(s)
                doctypes_indexedElement.addElement(N_NAME).addText((String)doctypesIterator.next());
            }              
        }
        // </indexsources>
        
        return searchElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {
        
        return C_CONFIGURATION_DTD_NAME;
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Search configuration : finished");
        }            
    }    
    
    /**
     * Sets the generated search manager.<p>
     * 
     * @param manager the search manager to set
     */
    public void setSearchManager(CmsSearchManager manager) {
        
        m_searchManager = manager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Search manager init  : finished");
        }
    }    
    
    /**
     * Returns the generated search manager.<p>
     * 
     * @return the generated search manager
     */
    public CmsSearchManager getSearchManager() {
        
        return m_searchManager;
    }    

}
