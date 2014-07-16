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
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.CmsIndexNoContentException;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Special document text extraction factory for Solr index.<p>
 * 
 * @since 8.5.0 
 */
public class CmsSolrDocumentXmlContent extends CmsDocumentXmlContent {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentXmlContent.class);

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
     * Extracts the content of a single XML content resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource
     * @param index the used index
     * 
     * @return the extraction result
     * 
     * @throws CmsException in case reading or unmarshalling the content fails
     */
    public static CmsExtractionResult extractXmlContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        // un-marshal the content
        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexNoContentException(Messages.get().container(
                Messages.ERR_NO_CONTENT_1,
                resource.getRootPath()));
        }
        A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

        // initialize some variables
        Map<String, String> items = new HashMap<String, String>();
        StringBuffer locales = new StringBuffer();
        Locale resourceLocale = index.getLocaleForResource(cms, resource, xmlContent.getLocales());
        String defaultContent = null;

        // loop over the locales of the content 
        for (Locale locale : xmlContent.getLocales()) {
            StringBuffer textContent = new StringBuffer();
            // store the locales of the content as space separated field
            locales.append(locale.toString());
            locales.append(' ');

            // loop over the available element paths of the current content locale
            List<String> paths = xmlContent.getNames(locale);
            for (String xpath : paths) {

                // try to get the value extraction for the current element path
                String extracted = null;
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                try {
                    extracted = value.getPlainText(cms);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted) && value.isSimpleType()) {
                        // no text value for simple type, so take the string value as item
                        extracted = value.getStringValue(cms);
                    }
                } catch (Exception e) {
                    // it can happen that a exception is thrown while extracting a single value
                    LOG.warn(Messages.get().container(Messages.LOG_EXTRACT_VALUE_2, xpath, resource), e);
                }

                // put the extraction to the items and to the textual content
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    // add the extracted value to the items and use the "/locale/xpath" as key
                    items.put(CmsXmlUtils.concatXpath(locale.toString(), xpath), extracted);
                }
                if (value.getContentDefinition().getContentHandler().isSearchable(value)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    // value is search-able and the extraction is not empty, so added to the textual content
                    textContent.append(extracted);
                    textContent.append('\n');
                }
            }

            // handle the textual content
            if (textContent.length() > 0) {
                // add the textual content with a localized key to the items
                String key = CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale);
                items.put(key, textContent.toString());
                // use the default locale of this resource as general text content for the extraction result
                if (resourceLocale.equals(locale)) {
                    defaultContent = textContent.toString();
                }
            }
        }

        // add the locales that have been indexed for this document as item and return the extraction result
        items.put(CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString().trim());
        return new CmsExtractionResult(defaultContent, items);
    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);

        try {
            I_CmsExtractionResult result = null;
            if (hasDetailContainers(cms, resource)) {
                String detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(cms.getSitePath(resource));
                CmsResource detailContainers = cms.readResource(detailContainerPage);
                List<I_CmsExtractionResult> ex = new ArrayList<I_CmsExtractionResult>();
                CmsSolrDocumentContainerPage containerpageExtractor = new CmsSolrDocumentContainerPage("");
                I_CmsExtractionResult containersExtractionResult = containerpageExtractor.extractContent(
                    cms,
                    detailContainers,
                    index);
                // only use the locales of the resource itself, not the ones of the detail containers page
                containersExtractionResult.getContentItems().remove(CmsSearchField.FIELD_RESOURCE_LOCALES);
                ex.add(containersExtractionResult);
                ex.add(extractXmlContent(cms, resource, index));
                result = CmsSolrDocumentContainerPage.merge(
                    cms,
                    resource,
                    ex,
                    (CmsSolrFieldConfiguration)index.getFieldConfiguration());
            } else {
                result = extractXmlContent(cms, resource, index);
            }
            return result;

        } catch (Throwable t) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource), t);
        }
    }

    /**
     * Checks whether the resource has associated detail containers.<p>
     * 
     * @param cms the cms context
     * @param resource the resource
     * 
     * @return <code>true</code> if the resource has associated detail containers
     */
    public boolean hasDetailContainers(CmsObject cms, CmsResource resource) {

        String detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(cms.getSitePath(resource));
        return cms.existsResource(detailContainerPage);
    }
}
