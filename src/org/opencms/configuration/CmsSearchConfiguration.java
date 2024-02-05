/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchAnalyzer;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.search.fields.I_CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.search.solr.CmsSolrConfiguration;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.digester3.Digester;

import org.dom4j.Element;

/**
 * Lucene search configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchConfiguration extends A_CmsXmlConfiguration {

    /** The "analyzer" attribute. */
    public static final String A_ANALYZER = "analyzer";

    /** The "boost" attribute. */
    public static final String A_BOOST = "boost";

    /** The "displayName" attribute. */
    public static final String A_DISPLAY = "display";

    /** The "excerpt" attribute. */
    public static final String A_EXCERPT = "excerpt";

    /** The "index" attribute. */
    public static final String A_INDEX = "index";

    /** The Solr server URL attribute, set if embedded = false. */
    public static final String A_SERVER_URL = "serverUrl";

    /** The "store" attribute. */
    public static final String A_STORE = "store";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-search.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-search.xml";

    /** Node name constant. */
    public static final String N_ANALYZER = "analyzer";

    /** Node name constant. */
    public static final String N_ANALYZERS = "analyzers";

    /** Node name constant. */
    public static final String N_CLASS = "class";

    /** Node name constant. */
    public static final String N_COMMIT_MS = "commitWithinMs";

    /** Node name constant. */
    public static final String N_CONFIG_FILE = "configfile";

    /** Node name constant. */
    public static final String N_CONFIGURATION = "configuration";

    /** Node name constant. */
    public static final String N_DESCRIPTION = "description";

    /** Node name constant. */
    public static final String N_DIRECTORY = "directory";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPE = "documenttype";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPES = "documenttypes";

    /** Node name constant. */
    public static final String N_DOCUMENTTYPES_INDEXED = "documenttypes-indexed";

    /** Node name constant. */
    public static final String N_EXCERPT = "excerpt";

    /** Node name constant. */
    public static final String N_EXTRACTION_CACHE_MAX_AGE = "extractionCacheMaxAge";

    /** Node name constant. */
    public static final String N_FIELD = "field";

    /** Node name constant. */
    public static final String N_FIELDCONFIGURATION = "fieldconfiguration";

    /** Node name constant. */
    public static final String N_FIELDCONFIGURATIONS = "fieldconfigurations";

    /** Node name constant. */
    public static final String N_FIELDS = "fields";

    /** Node name constant. */
    public static final String N_FORCEUNLOCK = "forceunlock";

    /** Node name constant. */
    public static final String N_HIGHLIGHTER = "highlighter";

    /** Node name constant. */
    public static final String N_HOME = "home";

    /** Node name constant. */
    public static final String N_INDEX = "index";

    /** Node name constant. */
    public static final String N_INDEXER = "indexer";

    /** Node name constant. */
    public static final String N_INDEXES = "indexes";

    /** Node name constant. */
    public static final String N_INDEXSOURCE = "indexsource";

    /** Node name constant. */
    public static final String N_INDEXSOURCES = "indexsources";

    /** Node name constant. */

    /** Node name constant. */
    public static final String N_LOCALE = "locale";

    /** Node name constant. */
    public static final String N_MAPPING = "mapping";

    /** Node name constant. */
    public static final String N_MAX_MODIFICATIONS_BEFORE_COMMIT = "maxModificationsBeforeCommit";

    /** Node name constant. */
    public static final String N_MIMETYPE = "mimetype";

    /** Node name constant. */
    public static final String N_MIMETYPES = "mimetypes";

    /** Node name constant. */
    public static final String N_OFFLINE_UPDATE_FREQUENCY = "offlineUpdateFrequency";

    /** Node name constant. */
    public static final String N_MAX_INDEX_WAIT_TIME = "maxIndexWaitTime";

    /** Node name constant. */
    public static final String N_PROJECT = "project";

    /** Node name constant. */
    public static final String N_REBUILD = "rebuild";

    /** Node name constant. */
    public static final String N_RESOURCES = "resources";

    /** Node name constant. */
    public static final String N_RESOURCETYPE = "resourcetype";

    /** Node name constant. */
    public static final String N_RESOURCETYPES = "resourcetypes";

    /** Node name constant. */
    public static final String N_SEARCH = "search";

    /** Node name constant. */
    public static final String N_SOLR = "solr";

    /** Node name constant. */
    public static final String N_SOURCE = "source";

    /** Node name constant. */
    public static final String N_SOURCES = "sources";

    /** Node name constant. */
    public static final String N_STEMMER = "stemmer";

    /** Node name constant. */
    public static final String N_TIMEOUT = "timeout";

    /** Node name constant. */
    private static final String XPATH_SEARCH = "*/" + N_SEARCH;

    /** Node name constant. */
    private static final String N_MAX_PROCESSED_RESULTS = "maxProcessedResults";

    /** The configured search manager. */
    private CmsSearchManager m_searchManager;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        String xPath = null;

        // add finish rule
        digester.addCallMethod(XPATH_SEARCH, "initializeFinished");

        // creation of the search manager
        digester.addObjectCreate(XPATH_SEARCH, CmsSearchManager.class.getName(), A_CLASS);

        // search manager finished
        digester.addSetNext(XPATH_SEARCH, "setSearchManager");

        // directory rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_DIRECTORY, "setDirectory", 0);

        // timeout rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_TIMEOUT, "setTimeout", 0);

        // offline update rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_OFFLINE_UPDATE_FREQUENCY, "setOfflineUpdateFrequency", 0);

        // offline update rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_MAX_INDEX_WAIT_TIME, "setMaxIndexWaitTime", 0);

        // forceunlock rule
        digester.addCallMethod(XPATH_SEARCH + "/" + N_FORCEUNLOCK, "setForceunlock", 0);

        // rule for the max. char. lenght of the search result excerpt
        digester.addCallMethod(XPATH_SEARCH + "/" + N_EXCERPT, "setMaxExcerptLength", 0);

        // rule for the max. age of entries in the extraction cache
        digester.addCallMethod(XPATH_SEARCH + "/" + N_EXTRACTION_CACHE_MAX_AGE, "setExtractionCacheMaxAge", 0);

        // rule for max. number of modifications before commit
        digester.addCallMethod(
            XPATH_SEARCH + "/" + N_MAX_MODIFICATIONS_BEFORE_COMMIT,
            "setMaxModificationsBeforeCommit",
            0);

        // rule for the highlighter to highlight the search terms in the excerpt of the search result
        digester.addCallMethod(XPATH_SEARCH + "/" + N_HIGHLIGHTER, "setHighlighter", 0);

        xPath = XPATH_SEARCH + "/" + N_SOLR;
        digester.addObjectCreate(xPath, CmsSolrConfiguration.class);
        digester.addCallMethod(xPath, "setEnabled", 1);
        digester.addCallParam(xPath, 0, A_ENABLED);
        digester.addCallMethod(xPath, "setServerUrl", 1);
        digester.addCallParam(xPath, 0, A_SERVER_URL);
        digester.addCallMethod(xPath + "/" + N_HOME, "setHomeFolderPath", 0);
        digester.addCallMethod(xPath + "/" + N_CONFIG_FILE, "setSolrFileName", 0);
        digester.addCallMethod(xPath + "/" + N_COMMIT_MS, "setSolrCommitMs", 0);
        digester.addCallMethod(xPath + "/" + N_MAX_PROCESSED_RESULTS, "setMaxProcessedResults", 0);
        digester.addSetNext(xPath, "setSolrServerConfiguration");

        // document type rule
        xPath = XPATH_SEARCH + "/" + N_DOCUMENTTYPES + "/" + N_DOCUMENTTYPE;
        digester.addObjectCreate(xPath, CmsSearchDocumentType.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_MIMETYPES + "/" + N_MIMETYPE, "addMimeType", 0);
        digester.addCallMethod(xPath + "/" + N_RESOURCETYPES + "/" + N_RESOURCETYPE, "addResourceType", 0);
        digester.addSetNext(xPath, "addDocumentTypeConfig");

        // analyzer rule
        xPath = XPATH_SEARCH + "/" + N_ANALYZERS + "/" + N_ANALYZER;
        digester.addObjectCreate(xPath, CmsSearchAnalyzer.class);
        digester.addCallMethod(xPath + "/" + N_CLASS, "setClassName", 0);
        digester.addCallMethod(xPath + "/" + N_STEMMER, "setStemmerAlgorithm", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocaleString", 0);
        digester.addSetNext(xPath, "addAnalyzer");

        // search index rule
        xPath = XPATH_SEARCH + "/" + N_INDEXES + "/" + N_INDEX;
        digester.addObjectCreate(xPath, CmsSearchIndex.class.getName(), A_CLASS);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_REBUILD, "setRebuildMode", 0);
        digester.addCallMethod(xPath + "/" + N_PROJECT, "setProject", 0);
        digester.addCallMethod(xPath + "/" + N_LOCALE, "setLocaleString", 0);
        digester.addCallMethod(xPath + "/" + N_CONFIGURATION, "setFieldConfigurationName", 0);
        digester.addCallMethod(xPath + "/" + N_SOURCES + "/" + N_SOURCE, "addSourceName", 0);
        digester.addSetNext(xPath, "addSearchIndex");

        // search index source rule
        xPath = XPATH_SEARCH + "/" + N_INDEXSOURCES + "/" + N_INDEXSOURCE;
        digester.addObjectCreate(xPath, CmsSearchIndexSource.class);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_INDEXER, "setIndexerClassName", 1);
        digester.addCallParam(xPath + "/" + N_INDEXER, 0, N_CLASS);
        digester.addCallMethod(xPath + "/" + N_RESOURCES + "/" + N_RESOURCE, "addResourceName", 0);
        digester.addCallMethod(xPath + "/" + N_DOCUMENTTYPES_INDEXED + "/" + N_NAME, "addDocumentType", 0);
        digester.addSetNext(xPath, "addSearchIndexSource");

        // field configuration rules
        xPath = XPATH_SEARCH + "/" + N_FIELDCONFIGURATIONS + "/" + N_FIELDCONFIGURATION;
        digester.addObjectCreate(xPath, CmsLuceneFieldConfiguration.class.getName(), A_CLASS);
        digester.addCallMethod(xPath + "/" + N_NAME, "setName", 0);
        digester.addCallMethod(xPath + "/" + N_DESCRIPTION, "setDescription", 0);
        digester.addSetNext(xPath, "addFieldConfiguration");

        xPath = xPath + "/" + N_FIELDS + "/" + N_FIELD;
        digester.addObjectCreate(xPath, CmsLuceneField.class);
        // Node: It is important to have that rule first, since then it is called last
        // at the closing part of the XML node where rules are executed in reverse order.
        // Thus, use this order to have the field already completely initialized when added
        // to the configuration
        digester.addSetNext(xPath, "addField");
        digester.addCallMethod(xPath, "setName", 1);
        digester.addCallParam(xPath, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallMethod(xPath, "setDisplayNameForConfiguration", 1);
        digester.addCallParam(xPath, 0, A_DISPLAY);
        digester.addCallMethod(xPath, "setStored", 1);
        digester.addCallParam(xPath, 0, A_STORE);
        digester.addCallMethod(xPath, "setIndexed", 1);
        digester.addCallParam(xPath, 0, A_INDEX);
        digester.addCallMethod(xPath, "setInExcerpt", 1);
        digester.addCallParam(xPath, 0, A_EXCERPT);
        digester.addCallMethod(xPath, "setAnalyzer", 1);
        digester.addCallParam(xPath, 0, A_ANALYZER);
        digester.addCallMethod(xPath, "setBoost", 1);
        digester.addCallParam(xPath, 0, A_BOOST);
        digester.addCallMethod(xPath, "setDefaultValue", 1);
        digester.addCallParam(xPath, 0, A_DEFAULT);
        digester.addCallMethod(xPath, "setType", 1);
        digester.addCallParam(xPath, 0, A_TYPE);

        xPath = xPath + "/" + N_MAPPING;
        digester.addObjectCreate(xPath, CmsSearchFieldMapping.class.getName(), A_CLASS);
        digester.addCallMethod(xPath, "setDefaultValue", 1);
        digester.addCallParam(xPath, 0, A_DEFAULT);
        digester.addCallMethod(xPath, "setType", 1);
        digester.addCallParam(xPath, 0, A_TYPE);
        digester.addCallMethod(xPath, "setParam", 0);
        digester.addSetNext(xPath, "addMapping");

        // generic <param> parameter rules
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);
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

        // add class attribute (if required)
        if (!m_searchManager.getClass().equals(CmsSearchManager.class)) {
            searchElement.addAttribute(A_CLASS, m_searchManager.getClass().getName());
        }

        // add the Solr node
        if (m_searchManager.getSolrServerConfiguration() != null) {
            Element solr = searchElement.addElement(N_SOLR);
            CmsSolrConfiguration conf = m_searchManager.getSolrServerConfiguration();
            solr.addAttribute(A_ENABLED, Boolean.valueOf(conf.isEnabled()).toString());
            if (conf.getServerUrl() != null) {
                solr.addAttribute(A_SERVER_URL, conf.getServerUrl().toString());
            }
            if (conf.getHomeFolderPath() != null) {
                solr.addElement(N_HOME).addText(conf.getHomeFolderPath());
            }
            if (conf.getSolrFileName() != null) {
                solr.addElement(N_CONFIG_FILE).addText(conf.getSolrFileName());
            }
            solr.addElement(N_COMMIT_MS).addText(String.valueOf(conf.getSolrCommitMs()));
        }

        // add <directory> element
        searchElement.addElement(N_DIRECTORY).addText(m_searchManager.getDirectory());
        // add <timeout> element
        searchElement.addElement(N_TIMEOUT).addText(String.valueOf(m_searchManager.getTimeout()));
        //add <offlineUpdateFrequency> element
        searchElement.addElement(N_OFFLINE_UPDATE_FREQUENCY).addText(
            String.valueOf(m_searchManager.getOfflineUpdateFrequency()));
        //add <maxIndexWaitTime> element
        searchElement.addElement(N_MAX_INDEX_WAIT_TIME).addText(String.valueOf(m_searchManager.getMaxIndexWaitTime()));
        // add <forceunlock> element
        if (m_searchManager.getForceunlock() != null) {
            searchElement.addElement(N_FORCEUNLOCK).addText(m_searchManager.getForceunlock().toString());
        }
        // add <exerpt> element
        searchElement.addElement(N_EXCERPT).addText(String.valueOf(m_searchManager.getMaxExcerptLength()));
        // add <extractionCacheMaxAge> element
        searchElement.addElement(N_EXTRACTION_CACHE_MAX_AGE).addText(
            String.valueOf(m_searchManager.getExtractionCacheMaxAge()));
        // add <maxModificationsBeforeCommit> element
        searchElement.addElement(N_MAX_MODIFICATIONS_BEFORE_COMMIT).addText(
            String.valueOf(m_searchManager.getMaxModificationsBeforeCommit()));
        // add <highlighter> element
        searchElement.addElement(N_HIGHLIGHTER).addText(m_searchManager.getHighlighter().getClass().getName());

        // <documenttypes>
        Element documenttypesElement = searchElement.addElement(N_DOCUMENTTYPES);
        for (CmsSearchDocumentType currSearchDocType : m_searchManager.getDocumentTypeConfigs()) {
            // add the next <documenttype> element
            Element documenttypeElement = documenttypesElement.addElement(N_DOCUMENTTYPE);
            // add <name> element
            documenttypeElement.addElement(N_NAME).addText(currSearchDocType.getName());
            // add <class> element
            documenttypeElement.addElement(N_CLASS).addText(currSearchDocType.getClassName());
            // add <mimetypes> element
            Element mimetypesElement = documenttypeElement.addElement(N_MIMETYPES);
            // get the list of mimetypes to trigger the document factory class
            Iterator<String> mimeTypesIterator = currSearchDocType.getMimeTypes().iterator();
            while (mimeTypesIterator.hasNext()) {
                // add <mimetype> element(s)
                mimetypesElement.addElement(N_MIMETYPE).addText(mimeTypesIterator.next());
            }
            // add <resourcetypes> element
            Element restypesElement = documenttypeElement.addElement(N_RESOURCETYPES);
            // get the list of Cms resource types to trigger the document factory
            Iterator<String> resTypesIterator = currSearchDocType.getResourceTypes().iterator();
            while (resTypesIterator.hasNext()) {
                // add <resourcetype> element(s)
                restypesElement.addElement(N_RESOURCETYPE).addText(resTypesIterator.next());
            }
        }
        // </documenttypes>

        // <analyzers>
        Element analyzersElement = searchElement.addElement(N_ANALYZERS);
        ArrayList<Locale> analyzerLocaleList = new ArrayList<Locale>(m_searchManager.getAnalyzers().keySet());
        // sort Analyzers in ascending order
        Collections.sort(analyzerLocaleList, CmsLocaleComparator.getComparator());
        Iterator<Locale> analyzersLocaleInterator = analyzerLocaleList.iterator();
        while (analyzersLocaleInterator.hasNext()) {
            CmsSearchAnalyzer searchAnalyzer = m_searchManager.getCmsSearchAnalyzer(analyzersLocaleInterator.next());
            // add the next <analyzer> element
            Element analyzerElement = analyzersElement.addElement(N_ANALYZER);
            // add <class> element
            analyzerElement.addElement(N_CLASS).addText(searchAnalyzer.getClassName());
            // add <locale> element
            analyzerElement.addElement(N_LOCALE).addText(searchAnalyzer.getLocale().toString());
        }
        // </analyzers>

        // <indexes>
        Element indexesElement = searchElement.addElement(N_INDEXES);
        for (I_CmsSearchIndex searchIndex : m_searchManager.getSearchIndexesAll()) {
            // add the next <index> element
            Element indexElement = indexesElement.addElement(N_INDEX);
            // add class attribute (if required)
            if (!searchIndex.getClass().equals(CmsSearchIndex.class)) {
                indexElement.addAttribute(A_CLASS, searchIndex.getClass().getName());
            }
            // add <name> element
            indexElement.addElement(N_NAME).addText(searchIndex.getName());
            // add <rebuild> element
            indexElement.addElement(N_REBUILD).addText(searchIndex.getRebuildMode());
            // add <project> element
            indexElement.addElement(N_PROJECT).addText(searchIndex.getProject());
            // add <locale> element
            indexElement.addElement(N_LOCALE).addText(searchIndex.getLocale().toString());
            // add <configuration> element
            String fieldConfigurationName = searchIndex.getFieldConfigurationName();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(fieldConfigurationName)) {
                indexElement.addElement(N_CONFIGURATION).addText(fieldConfigurationName);
            }
            // add <sources> element
            Element sourcesElement = indexElement.addElement(N_SOURCES);
            // iterate above sourcenames
            Iterator<String> sourcesIterator = searchIndex.getSourceNames().iterator();
            while (sourcesIterator.hasNext()) {
                // add <source> element
                sourcesElement.addElement(N_SOURCE).addText(sourcesIterator.next());
            }
            // iterate additional params
            CmsParameterConfiguration indexConfiguration = searchIndex.getConfiguration();
            if (indexConfiguration != null) {
                indexConfiguration.appendToXml(indexElement);
            }
        }
        // </indexes>

        // <indexsources>
        Element indexsourcesElement = searchElement.addElement(N_INDEXSOURCES);
        for (CmsSearchIndexSource searchIndexSource : m_searchManager.getSearchIndexSources().values()) {
            // add <indexsource> element(s)
            Element indexsourceElement = indexsourcesElement.addElement(N_INDEXSOURCE);
            // add <name> element
            indexsourceElement.addElement(N_NAME).addText(searchIndexSource.getName());
            // add <indexer class=""> element
            Element indexerElement = indexsourceElement.addElement(N_INDEXER).addAttribute(
                N_CLASS,
                searchIndexSource.getIndexerClassName());
            for (Entry<String, String> entry : searchIndexSource.getParams().entrySet()) {
                // add <param name=""> element(s)
                indexerElement.addElement(I_CmsXmlConfiguration.N_PARAM).addAttribute(
                    I_CmsXmlConfiguration.A_NAME,
                    entry.getKey()).addText(entry.getValue());
            }
            // add <resources> element
            Element resourcesElement = indexsourceElement.addElement(N_RESOURCES);
            Iterator<String> resourceIterator = searchIndexSource.getResourcesNames().iterator();
            while (resourceIterator.hasNext()) {
                // add <resource> element(s)
                resourcesElement.addElement(N_RESOURCE).addText(resourceIterator.next());
            }
            // add <documenttypes-indexed> element
            Element doctypes_indexedElement = indexsourceElement.addElement(N_DOCUMENTTYPES_INDEXED);
            Iterator<String> doctypesIterator = searchIndexSource.getDocumentTypes().iterator();
            while (doctypesIterator.hasNext()) {
                // add <name> element(s)
                doctypes_indexedElement.addElement(N_NAME).addText(doctypesIterator.next());
            }
        }
        // </indexsources>

        // <fieldconfigurations>
        Element fieldConfigurationsElement = searchElement.addElement(N_FIELDCONFIGURATIONS);
        for (I_CmsSearchFieldConfiguration fieldConfiguration : m_searchManager.getFieldConfigurations()) {
            Element fieldConfigurationElement = fieldConfigurationsElement.addElement(N_FIELDCONFIGURATION);
            // add class attribute (if required)
            if (!fieldConfiguration.getClass().equals(CmsLuceneFieldConfiguration.class)) {
                fieldConfigurationElement.addAttribute(A_CLASS, fieldConfiguration.getClass().getName());
            }
            fieldConfigurationElement.addElement(N_NAME).setText(fieldConfiguration.getName());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(fieldConfiguration.getDescription())) {
                fieldConfigurationElement.addElement(N_DESCRIPTION).setText(fieldConfiguration.getDescription());
            }
            // search fields
            Element fieldsElement = fieldConfigurationElement.addElement(N_FIELDS);
            for (CmsSearchField sfield : fieldConfiguration.getFields()) {
                if (sfield instanceof CmsLuceneField) {
                    CmsLuceneField field = (CmsLuceneField)sfield;
                    Element fieldElement = fieldsElement.addElement(N_FIELD);
                    fieldElement.addAttribute(A_NAME, field.getName());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDisplayNameForConfiguration())) {
                        fieldElement.addAttribute(A_DISPLAY, field.getDisplayNameForConfiguration());
                    }
                    if (field.isCompressed()) {
                        fieldElement.addAttribute(A_STORE, CmsLuceneField.STR_COMPRESS);
                    } else {
                        fieldElement.addAttribute(A_STORE, String.valueOf(field.isStored()));
                    }
                    String index;
                    if (field.isIndexed()) {
                        if (field.isTokenizedAndIndexed()) {
                            // index and tokenized
                            index = CmsStringUtil.TRUE;
                        } else {
                            // indexed but not tokenized
                            index = CmsLuceneField.STR_UN_TOKENIZED;
                        }
                    } else {
                        // not indexed at all
                        index = CmsStringUtil.FALSE;
                    }
                    fieldElement.addAttribute(A_INDEX, index);
                    if (field.isInExcerptAndStored()) {
                        fieldElement.addAttribute(A_EXCERPT, String.valueOf(true));
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDefaultValue())) {
                        fieldElement.addAttribute(A_DEFAULT, field.getDefaultValue());
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getType())) {
                        fieldElement.addAttribute(A_TYPE, field.getType());
                    }
                    if (field.getAnalyzer() != null) {
                        String className = field.getAnalyzer().getClass().getName();
                        if (className.startsWith(CmsSearchManager.LUCENE_ANALYZER)) {
                            className = className.substring(CmsSearchManager.LUCENE_ANALYZER.length());
                        }
                        fieldElement.addAttribute(A_ANALYZER, className);
                    }
                    // field mappings
                    for (I_CmsSearchFieldMapping mapping : field.getMappings()) {
                        Element mappingElement = fieldElement.addElement(N_MAPPING);
                        mappingElement.addAttribute(A_TYPE, mapping.getType().toString());
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getDefaultValue())) {
                            mappingElement.addAttribute(A_DEFAULT, mapping.getDefaultValue());
                        }
                        // add class attribute (if required)
                        if (!mapping.getClass().equals(CmsSearchFieldMapping.class)
                            || (mapping.getType() == CmsSearchFieldMappingType.DYNAMIC)) {
                            mappingElement.addAttribute(A_CLASS, mapping.getClass().getName());
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getParam())) {
                            mappingElement.setText(mapping.getParam());
                        }
                    }
                }
            }
        }
        // </fieldconfigurations>

        return searchElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the generated search manager.<p>
     *
     * @return the generated search manager
     */
    public CmsSearchManager getSearchManager() {

        return m_searchManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p>
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_CONFIG_FINISHED_0));
        }
    }

    /**
     * Sets the generated search manager.<p>
     *
     * @param manager the search manager to set
     */
    public void setSearchManager(CmsSearchManager manager) {

        m_searchManager = manager;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_MANAGER_FINISHED_0));
        }
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SEARCH_CONFIG_INIT_0));
        }
    }
}
