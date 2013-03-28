/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Special document text extraction factory for Solr index.<p>
 * 
 * @since 8.5.0 
 */
public class CmsSolrDocumentXmlContent extends CmsDocumentXmlContent {

    /** The solr document type name for xml-contents. */
    public static final String TYPE_XMLCONTENT_SOLR = "xmlcontent-solr";

    /**
     * Public constructor.<p>
     * 
     * @param name the name for the document type
     */
    public CmsSolrDocumentXmlContent(String name) {

        super(name);
    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            // unmarshall the content
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, readFile(cms, resource));
            Map<String, String> items = new HashMap<String, String>();
            StringBuffer locales = new StringBuffer();
            Locale resLocale = index.getLocaleForResource(cms, resource, xmlContent.getLocales());
            String defaultContent = null;

            // loop over the locales
            for (Locale locale : xmlContent.getLocales()) {

                // store the extracted locales
                locales.append(locale.toString());
                locales.append(' ');
                StringBuffer content = new StringBuffer();

                // loop over the available element paths
                List<String> paths = xmlContent.getNames(locale);
                for (String xpath : paths) {

                    String extracted = extractValue(cms, xmlContent, xpath, locale);
                    I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);

                    // put the extraction to the content and to the items
                    if (value.getContentDefinition().getContentHandler().isSearchable(value)
                        && CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                        // create the content value for the locale by adding all String values in the XML nodes
                        content.append(extracted);
                        content.append('\n');
                    }

                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                        // The key must be the local extended parameter value of the Solr field mapping defined in:
                        // CmsDefaultXmlContentHandler.initSearchSettings(Element, CmsXmlContentDefinition)
                        // later during index process the values are retrieved in:
                        // CmsSearchFieldMapping#getStringValue(CmsObject, CmsResource, I_CmsExtractionResult, List, List)
                        items.put(CmsSearchFieldConfiguration.getLocaleExtendedName(xpath, locale), extracted);
                    }
                }
                if (content.length() > 0) {
                    // add the extracted content with a localized key into the extraction result
                    String contentKey = CmsSearchFieldConfiguration.getLocaleExtendedName(
                        CmsSearchField.FIELD_CONTENT,
                        locale);
                    items.put(contentKey, content.toString());
                    if (resLocale.equals(locale)) {
                        defaultContent = content.toString();
                    }
                }
                // add the locales that have been indexed for this document as item
                items.put(CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString());
            }
            // get all search fields configured in the XSD of this XML content
            Set<CmsSearchField> fields = new HashSet<CmsSearchField>(xmlContent.getHandler().getSearchFields());
            return new CmsExtractionResult(defaultContent, items, fields);
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * Extracts the content of a given XML content value.<p>
     * 
     * @param cms the CMS object
     * @param xmlContent the XML content
     * @param xpath the xPath of the content
     * @param locale the locale
     * 
     * @return the extracted value
     * @throws CmsException if something goes wrong
     */
    private String extractValue(CmsObject cms, A_CmsXmlDocument xmlContent, String xpath, Locale locale)
    throws CmsException {

        I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
        try {
            String extracted = value.getPlainText(cms);
            if (value.isSimpleType()) {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted)) {
                    extracted = value.getStringValue(cms);
                }
                // put the extraction to the content and to the items
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    // create the content value for the locale by adding all String values in the XML nodes
                    return extracted;
                }
            }
        } catch (Exception e) {
            // it can happen that a (runtime) exception is thrown while extracting the content of a value
            // e.g. nested contents don't have a String representation
            throw new CmsException(Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, xmlContent.getFile()), e);
        }
        return null;
    }
}
