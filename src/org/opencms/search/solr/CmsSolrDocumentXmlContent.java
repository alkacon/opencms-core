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
import org.opencms.main.CmsRuntimeException;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.CmsIndexException;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Special document text extraction factory for Solr index.<p>
 * 
 * @since 8.5.0 
 */
public class CmsSolrDocumentXmlContent extends CmsDocumentXmlContent {

    /**
     * Public constructor.<p>
     * 
     * @param name the name for the document type
     */
    public CmsSolrDocumentXmlContent(String name) {

        super(name);
    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.A_CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, A_CmsSearchIndex index)
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
                for (String xpath : xmlContent.getNames(locale)) {

                    // get the value for the element path
                    I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);

                    // first try to receive the text value 
                    String extracted = value.getPlainText(cms);
                    if (value.isSimpleType()) {

                        // put the extraction to the content and to the items
                        if (value.getContentDefinition().getContentHandler().isSearchable(value)
                            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                            // create the content value for the locale by adding all String values in the XML nodes
                            content.append(extracted);
                            content.append('\n');
                            items.put(xpath, extracted);
                        }

                        // if the extraction was empty try to take the String value as fall-back
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted)
                            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(value.getStringValue(cms))) {
                            try {
                                extracted = value.getStringValue(cms);
                                items.put(xpath, extracted);
                            } catch (CmsRuntimeException re) {
                                // ignore: is only thrown for those XML content values 
                                // that don't have a String representation for those we 
                                // will store a 'null'
                            }
                        }

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                            // The key must be the local extended parameter value of the Solr field mapping defined in:
                            // CmsDefaultXmlContentHandler.initSearchSettings(Element, CmsXmlContentDefinition)
                            // later during index process the values are retrieved in:
                            // I_CmsSearchFieldMapping#getStringValue(CmsObject, CmsResource, I_CmsExtractionResult, List, List)
                            items.put(A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                                CmsXmlUtils.removeXpath(xpath),
                                locale), extracted);
                        }
                    }
                }
                if (content.length() > 0) {
                    // add the extracted content with a localized key into the extraction result
                    String contentKey = A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                        I_CmsSearchField.FIELD_CONTENT,
                        locale);
                    items.put(contentKey, content.toString());
                    if (resLocale.equals(locale)) {
                        defaultContent = content.toString();
                    }
                }
                // add the locales that have been indexed for this document as item
                items.put(I_CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString());
            }
            // get all search fields configured in the XSD of this XML content
            Set<I_CmsSearchField> fields = new HashSet<I_CmsSearchField>(xmlContent.getHandler().getSearchFields());
            return new CmsExtractionResult(defaultContent, items, fields);
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }
}