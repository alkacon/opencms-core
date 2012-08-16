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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.CmsIndexException;
import org.opencms.search.I_CmsSearchDocument;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Special document text extraction factory for Solr index.<p>
 * 
 * @since 8.5.0 
 */
public class CmsSolrDocumentXmlContent extends CmsDocumentXmlContent {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentXmlContent.class);

    /**
     * Public constructor.<p>
     * 
     * @param name the name for the document type
     */
    public CmsSolrDocumentXmlContent(String name) {

        super(name);
    }

    /**
     * @see org.opencms.search.documents.A_CmsVfsDocument#createDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.A_CmsSearchIndex)
     */
    @Override
    public I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, A_CmsSearchIndex index)
    throws CmsException {

        I_CmsExtractionResult content = null;
        try {
            content = extractContent(cms, resource, index);
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()), e);
        }
        return index.getFieldConfiguration().createDocument(cms, resource, index, content);
    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.A_CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, A_CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            // unmarshall the content
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
            Set<I_CmsSearchField> fields = new HashSet<I_CmsSearchField>();
            Map<String, String> items = new HashMap<String, String>();
            StringBuffer locales = new StringBuffer();
            for (Locale locale : xmlContent.getLocales()) {
            	
            	List<Locale> otherLocales = new ArrayList<Locale>(xmlContent.getLocales());
            	otherLocales.remove(locale);
            	
                // loop over the locales
                locales.append(locale.toString());
                locales.append(' ');
                StringBuffer content = new StringBuffer();
                for (String xpath : xmlContent.getNames(locale)) {
                    // loop over the content items
                    I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                    
                    List<Locale> valuesNotAvailableInLocale = new ArrayList<Locale>();
                    for (Locale oLocale : otherLocales) {
                    	I_CmsXmlContentValue valueNA = xmlContent.getValue(xpath, oLocale);
                    	if (valueNA == null) {
                    		valuesNotAvailableInLocale.add(oLocale);
                    	}
                    }
                    
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

                        // if the extraction was empty try to take the String value as fall-back for the items
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted)
                            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(value.getStringValue(cms))) {
                            try {
                                extracted = value.getStringValue(cms);
                                items.put(xpath, extracted);
                            } catch (CmsRuntimeException re) {
                                // ignore: is only thrown for those XML content values that don't have a String representation
                            }
                        }

                        // put those items into the extraction result for that a field mapping done in the XSD
                        CmsSolrField field = xmlContent.getHandler().getSolrField(value);
                        if (field != null) {
	                        // resolve the field mappings configured in the XSD of the content
	                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                                // The key must be the same as the parameter value of the Solr field mapping defined in:
                                // org.opencms.xml.content.CmsDefaultXmlContentHandler.initSearchSettings(Element, CmsXmlContentDefinition)
                                // later during index process the values are retrieved in:
                                // org.opencms.search.fields.I_CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
                                items.put(A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                                    CmsXmlUtils.removeXpath(xpath),
                                    locale), extracted);
                                fields.add(field);
	                        }
	                        for (Locale naLocale : valuesNotAvailableInLocale) {
	                        	items.put(A_CmsSearchFieldConfiguration.getLocaleExtendedName(
	                                    CmsXmlUtils.removeXpath(xpath),
	                                    naLocale), null);
	                        	int ind = field.getTargetField().lastIndexOf("_" + locale.toString());
	                        	String newFieldName = A_CmsSearchFieldConfiguration.getLocaleExtendedName(field.getTargetField().substring(0, ind),naLocale) ;
	                        	CmsSolrField naField = new CmsSolrField(newFieldName, field.getCopyFields(), naLocale, field.getDefaultValue(), field.getBoost());
	                        	fields.add(naField);
	                        }
                        }
                    }
                }
                if (content.length() > 0) {
                    // add the extracted content to the items, needed for multi-language support 
                    String fieldname = A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                        I_CmsSearchField.FIELD_CONTENT,
                        locale);
                    items.put(fieldname, content.toString());
                    items.put(I_CmsSearchField.FIELD_CONTENT, content.toString());
                }
                // store the locales that have been indexed for this document
                items.put(I_CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString());
            }
            return new CmsExtractionResult(null, items, fields);
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }
}