/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsSearchConfiguration.java,v $
 * Date   : $Date: 2004/07/02 16:05:08 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.configuration;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchAnalyzer;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;


/**
 * Lucene search configuration class.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
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

        return null;
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
