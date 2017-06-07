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

package org.opencms.setup.xml;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.CmsVfsIndexer;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;

import org.dom4j.Document;

/**
 * Skeleton for handling opencms-search.xml.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsXmlSearch extends A_CmsSetupXmlUpdate {

    /**
     * Creates a new fieldconfiguration node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the fieldconfiguration, ie <code>opencms/search/fieldconfigurations/fieldconfiguration[name='...']</code>
     * @param fieldConf the field configuration
     * @param clazz the optional class attribute value
     */
    public void createFieldConfig(
        Document document,
        String xpath,
        CmsSearchFieldConfiguration fieldConf,
        Class<?> clazz) {

        if (clazz != null) {
            CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_CLASS, clazz.getName());
        }
        CmsSetupXmlHelper.setValue(document, xpath + "/" + I_CmsXmlConfiguration.N_NAME, fieldConf.getName());
        CmsSetupXmlHelper.setValue(
            document,
            xpath + "/" + CmsSearchConfiguration.N_DESCRIPTION,
            fieldConf.getDescription());
        for (CmsSearchField sField : fieldConf.getFields()) {
            CmsLuceneField field = (CmsLuceneField)sField;
            String fieldPath = xpath
                + "/"
                + CmsSearchConfiguration.N_FIELDS
                + "/"
                + CmsSearchConfiguration.N_FIELD
                + "[@"
                + I_CmsXmlConfiguration.A_NAME
                + "='"
                + field.getName()
                + "']";
            createField(document, fieldPath, field);
        }
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsSearchConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * Creates a new document type node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the index source, ie <code>/opencms/search/documenttypes/documenttype[name='...']</code>
     * @param name the name attribute value
     * @param clazz the class attribute value
     * @param mimetypes the list of mimetypes
     * @param restypes the list of resource types
     */
    protected void createDocType(
        Document document,
        String xpath,
        String name,
        Class<?> clazz,
        String[] mimetypes,
        String[] restypes) {

        CmsSetupXmlHelper.setValue(document, xpath + "/" + I_CmsXmlConfiguration.N_NAME, name);
        CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_CLASS, clazz.getName());
        CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_MIMETYPES, "");
        for (String mimetype : mimetypes) {
            String mimetypePath = xpath
                + "/"
                + CmsSearchConfiguration.N_MIMETYPES
                + "/"
                + CmsSearchConfiguration.N_MIMETYPE
                + "[text()='"
                + mimetype
                + "']";
            CmsSetupXmlHelper.setValue(document, mimetypePath, mimetype);
        }
        for (String restype : restypes) {
            String resTypePath = xpath
                + "/"
                + CmsSearchConfiguration.N_RESOURCETYPES
                + "/"
                + CmsSearchConfiguration.N_RESOURCETYPE
                + "[text()='"
                + restype
                + "']";
            CmsSetupXmlHelper.setValue(document, resTypePath, restype);
        }
    }

    /**
     * Creates a new field node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the field, ie <code>opencms/search/fieldconfigurations/fieldconfiguration[name='...']/fields/field[@name="..."]</code>
     * @param field the field
     */
    protected void createField(Document document, String xpath, CmsLuceneField field) {

        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_NAME, field.getName());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDisplayNameForConfiguration())) {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + CmsSearchConfiguration.A_DISPLAY,
                field.getDisplayNameForConfiguration());
        }
        if (field.isCompressed()) {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + CmsSearchConfiguration.A_STORE,
                CmsLuceneField.STR_COMPRESS);
        } else {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + CmsSearchConfiguration.A_STORE,
                String.valueOf(field.isStored()));
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
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + CmsSearchConfiguration.A_INDEX, index);
        if (field.getBoost() != CmsSearchField.BOOST_DEFAULT) {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + CmsSearchConfiguration.A_BOOST,
                String.valueOf(field.getBoost()));
        }
        if (field.isInExcerptAndStored()) {
            CmsSetupXmlHelper.setValue(document, xpath + "/@" + CmsSearchConfiguration.A_EXCERPT, String.valueOf(true));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(field.getDefaultValue())) {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + I_CmsXmlConfiguration.A_DEFAULT,
                field.getDefaultValue());
        }
        if (field.getAnalyzer() != null) {
            String className = field.getAnalyzer().getClass().getName();
            if (className.startsWith(CmsSearchManager.LUCENE_ANALYZER)) {
                className = className.substring(CmsSearchManager.LUCENE_ANALYZER.length());
            }
            CmsSetupXmlHelper.setValue(document, xpath + "/@" + CmsSearchConfiguration.A_ANALYZER, className);
        }

        // field mappings
        Iterator<I_CmsSearchFieldMapping> mappings = field.getMappings().iterator();
        while (mappings.hasNext()) {
            CmsSearchFieldMapping mapping = (CmsSearchFieldMapping)mappings.next();
            String mappingPath = xpath
                + "/"
                + CmsSearchConfiguration.N_MAPPING
                + "[@"
                + I_CmsXmlConfiguration.A_TYPE
                + "='"
                + mapping.getType().toString()
                + "']";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getParam())) {
                mappingPath += "[text()='" + mapping.getParam() + "']";
            }
            createFieldMapping(document, mappingPath, mapping);
        }
    }

    /**
     * Creates a new mapping node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the field mapping, ie <code>opencms/search/fieldconfigurations/fieldconfiguration[name='...']/fields/field[@name="..."]/mappings/mapping[@type='...']</code>
     * @param mapping the field mapping
     */
    protected void createFieldMapping(Document document, String xpath, CmsSearchFieldMapping mapping) {

        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_TYPE, mapping.getType().toString());
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_DEFAULT, mapping.getDefaultValue());
        // add class attribute (if required)
        if (!mapping.getClass().equals(CmsSearchFieldMapping.class)
            || (mapping.getType() == CmsSearchFieldMappingType.DYNAMIC)) {
            CmsSetupXmlHelper.setValue(
                document,
                xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                mapping.getClass().getName());
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mapping.getParam())) {
            CmsSetupXmlHelper.setValue(document, xpath, mapping.getParam());
        }
    }

    /**
     * Creates a new index node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the non-existing index (ie. <code>/opencms/search/indexes/index[name='...']</code>)
     * @param clazz the optional class attribute
     * @param name the name attribute value
     * @param rebuild the rebuild attribute value
     * @param project the project attribute value
     * @param locale the locale attribute value
     * @param configuration the optional configuration attribute value
     * @param sources the list of referenced sources
     */
    protected void createIndex(
        Document document,
        String xpath,
        Class<?> clazz,
        String name,
        String rebuild,
        String project,
        String locale,
        String configuration,
        String[] sources) {

        if (clazz != null) {
            CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_CLASS, clazz.getName());
        }
        CmsSetupXmlHelper.setValue(document, xpath + "/" + I_CmsXmlConfiguration.N_NAME, name);
        CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_REBUILD, rebuild);
        CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_PROJECT, project);
        CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_LOCALE, locale);
        if (configuration != null) {
            CmsSetupXmlHelper.setValue(document, xpath + "/" + CmsSearchConfiguration.N_CONFIGURATION, configuration);
        }
        for (String source : sources) {
            String sourcePath = xpath
                + "/"
                + CmsSearchConfiguration.N_SOURCES
                + "/"
                + CmsSearchConfiguration.N_SOURCE
                + "[text()='"
                + source
                + "']";
            CmsSetupXmlHelper.setValue(document, sourcePath, source);
        }
    }

    /**
     * Creates a new indexsource node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the index source, ie <code>/opencms/search/indexsources/indexsource[name='...']</code>
     * @param name the name attribute value
     * @param clazz the class attribute value
     * @param resources the list of resources
     * @param doctypes the list of document types
     */
    protected void createIndexSource(
        Document document,
        String xpath,
        String name,
        Class<CmsVfsIndexer> clazz,
        String[] resources,
        String[] doctypes) {

        CmsSetupXmlHelper.setValue(document, xpath + "/" + I_CmsXmlConfiguration.N_NAME, name);
        CmsSetupXmlHelper.setValue(
            document,
            xpath + "/" + CmsSearchConfiguration.N_INDEXER + "/@" + I_CmsXmlConfiguration.A_CLASS,
            clazz.getName());
        for (String resource : resources) {
            String resourcePath = xpath
                + "/"
                + CmsSearchConfiguration.N_RESOURCES
                + "/"
                + I_CmsXmlConfiguration.N_RESOURCE
                + "[text()='"
                + resource
                + "']";
            CmsSetupXmlHelper.setValue(document, resourcePath, resource);
        }
        for (String docType : doctypes) {
            String doctypePath = xpath
                + "/"
                + CmsSearchConfiguration.N_DOCUMENTTYPES_INDEXED
                + "/"
                + I_CmsXmlConfiguration.N_NAME
                + "[text()='"
                + docType
                + "']";
            CmsSetupXmlHelper.setValue(document, doctypePath, docType);
        }
    }
}