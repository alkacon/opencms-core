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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Lucene document factory class to extract index data from an OpenCms VFS resource
 * of type <code>CmsResourceTypeXmlContent</code>.<p>
 *
 * All XML nodes from the content for all locales will be stored separately in the item map
 * which you can access using {@link CmsExtractionResult#getContentItems()}. The XML elements will be
 * accessible using their xpath. The xpath will have the form like for example
 * <code>Text[1]</code> or <code>Nested[1]/Text[1]</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsDocumentXmlContent extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the document type
     */
    public CmsDocumentXmlContent(String name) {

        super(name);
    }

    /**
     *
     * @see org.opencms.search.documents.A_CmsVfsDocument#createDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.I_CmsSearchIndex)
     */
    @Override
    public I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        CmsXmlContentDefinition def = CmsXmlContentDefinition.getContentDefinitionForResource(cms, resource);
        if (def.getContentHandler().isContainerPageOnly()) {
            return null;
        }
        return super.createDocument(cms, resource, index);
    }

    /**
     * Returns the raw text content of a given VFS resource of type <code>CmsResourceTypeXmlContent</code>.<p>
     *
     * All XML nodes from the content for all locales will be stored separately in the item map
     * which you can access using {@link CmsExtractionResult#getContentItems()}. The XML elements will be
     * accessible using their xpath. The xpath will have the form like for example
     * <code>Text[1]</code> or <code>Nested[1]/Text[1]</code>.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, I_CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
            I_CmsXmlContentHandler handler = xmlContent.getHandler();
            Locale locale = index.getLocaleForResource(cms, resource, xmlContent.getLocales());
            List<String> elements = xmlContent.getNames(locale);
            StringBuffer content = new StringBuffer();
            LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();
            for (Iterator<String> i = elements.iterator(); i.hasNext();) {
                String xpath = i.next();
                // xpath will have the form "Text[1]" or "Nested[1]/Text[1]"
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                if (handler.isSearchable(value)) {
                    // the content value is searchable
                    String extracted = value.getPlainText(cms);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                        items.put(xpath, extracted);
                        content.append(extracted);
                        content.append('\n');
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