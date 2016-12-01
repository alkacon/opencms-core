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

package org.opencms.search.documents;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Lucene document factory class to extract index data from a resource
 * of type <code>CmsResourceTypeContainerPage</code>.<p>
 *
 * @since 8.0
 */
public class CmsDocumentContainerPage extends A_CmsVfsDocument {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentContainerPage.class);

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the document type
     */
    public CmsDocumentContainerPage(String name) {

        super(name);
    }

    /**
     * Generates a new lucene document instance from contents of the given resource for the provided index.<p>
     *
     * For container pages, we must not cache based on the container page content age,
     * since the content of the included elements may change any time.
     */
    @Override
    public I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        // extract the content from the resource
        I_CmsExtractionResult content = null;

        if (index.isExtractingContent()) {
            // do full text content extraction only if required

            try {
                content = extractContent(cms, resource, index);
            } catch (Exception e) {
                // text extraction failed for document - continue indexing meta information only
                LOG.error(Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()), e);
            }
        }

        // create the Lucene document according to the index field configuration
        return index.getFieldConfiguration().createDocument(cms, resource, index, content);
    }

    /**
     * Returns the raw text content of a VFS resource of type <code>CmsResourceTypeContainerPage</code>.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            CmsXmlContainerPage containerPage = CmsXmlContainerPageFactory.unmarshal(cms, file);
            Locale locale = index.getLocaleForResource(cms, resource, null);

            // initialize return values
            StringBuffer content = new StringBuffer();
            LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();

            CmsContainerPageBean containerBean = containerPage.getContainerPage(cms);
            for (CmsContainerElementBean element : containerBean.getElements()) {
                // check all elements in this container

                // get the formatter configuration for this element
                element.initResource(cms);
                CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cms, file.getRootPath());
                CmsFormatterConfiguration formatters = adeConfig.getFormatters(cms, element.getResource());

                if (formatters.isSearchContent(element.getFormatterId())) {
                    // the content of this element must be included for the container page

                    element.initResource(cms);
                    CmsFile elementFile = readFile(cms, element.getResource());
                    A_CmsXmlDocument elementContent = CmsXmlContentFactory.unmarshal(cms, elementFile);
                    List<String> elementNames = elementContent.getNames(locale);
                    for (String xpath : elementNames) {
                        // xpath will have the form "Text[1]" or "Nested[1]/Text[1]"
                        I_CmsXmlContentValue value = elementContent.getValue(xpath, locale);
                        if (value.getContentDefinition().getContentHandler().isSearchable(value)) {
                            // the content value is searchable
                            String extracted = value.getPlainText(cms);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                                items.put(elementFile.getRootPath() + "/" + xpath, extracted);
                                content.append(extracted);
                                content.append('\n');
                            }
                        }
                    }
                }
            }

            return new CmsExtractionResult(content.toString(), items);

        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    public boolean isLocaleDependend() {

        return true;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    public boolean isUsingCache() {

        return true;
    }
}
