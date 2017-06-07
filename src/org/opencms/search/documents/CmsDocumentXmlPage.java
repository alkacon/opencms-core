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
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Lucene document factory class to extract index data from a cms resource
 * of type <code>CmsResourceTypeXmlPage</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsDocumentXmlPage extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the documenttype
     */
    public CmsDocumentXmlPage(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource of type <code>CmsResourceTypeXmlPage</code>.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
            Locale locale = index.getLocaleForResource(cms, resource, page.getLocales());

            List<String> elements = page.getNames(locale);
            StringBuffer content = new StringBuffer();
            LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();
            for (Iterator<String> i = elements.iterator(); i.hasNext();) {
                String elementName = i.next();
                String value = page.getStringValue(cms, elementName, locale);
                String extracted = CmsHtmlExtractor.extractText(value, page.getEncoding());
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    items.put(elementName, extracted);
                    content.append(extracted);
                    content.append('\n');
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