/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Lucene document factory class to extract index data from a resource 
 * of type <code>CmsResourceTypeContainerPage</code>.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrDocumentContainerPage extends CmsSolrDocumentXmlContent {

    /** The solr document type name for xml-contents. */
    public static final String TYPE_CONTAINERPAGE_SOLR = "containerpage-solr";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentContainerPage.class);

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the document type
     */
    public CmsSolrDocumentContainerPage(String name) {

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
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            CmsXmlContainerPage containerPage = CmsXmlContainerPageFactory.unmarshal(cms, file);
            Locale locale = index.getLocaleForResource(cms, resource, containerPage.getLocales());

            List<I_CmsExtractionResult> all = new ArrayList<I_CmsExtractionResult>();
            CmsContainerPageBean containerBean = containerPage.getContainerPage(cms, locale);
            if (containerBean != null) {
                for (CmsContainerElementBean element : containerBean.getElements()) {
                    // check all elements in this container
                    // get the formatter configuration for this element
                    element.initResource(cms);
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(element.getResource());
                    CmsFormatterConfiguration formatters = type.getFormattersForResource(cms, element.getResource());

                    if (formatters.isSearchContent(element.getFormatterId())) {
                        // the content of this element must be included for the container page
                        element.initResource(cms);
                        all.add(super.extractContent(cms, element.getResource(), index));
                    }
                }
            }
            return merge(cms, resource, all);
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    @Override
    public boolean isLocaleDependend() {

        return true;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    @Override
    public boolean isUsingCache() {

        return true;
    }

    /**
     * Merges the given list of extraction results into a single one.<p>
     * 
     * @param all the extraction result objects to merge
     * 
     * @return the merged result
     */
    private I_CmsExtractionResult merge(CmsObject cms, CmsResource resource, List<I_CmsExtractionResult> all) {

        StringBuffer content = new StringBuffer();
        Map<String, String> items = new HashMap<String, String>();
        Set<CmsSearchField> fields = new HashSet<CmsSearchField>();

        for (I_CmsExtractionResult ex : all) {
            if (ex.getContent() != null) {
                content.append(ex.getContent());
            }
            if (ex.getContentItems() != null) {
                items.putAll(ex.getContentItems());
            }
            Set<CmsSearchField> mappedFields = CmsSolrFieldConfiguration.getXSDMappings(cms, resource);
            if (mappedFields != null) {
                fields.addAll(mappedFields);
            }
        }
        return new CmsExtractionResult(content.toString(), items, fields);
    }
}
