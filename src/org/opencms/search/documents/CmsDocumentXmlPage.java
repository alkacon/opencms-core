/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/CmsDocumentXmlPage.java,v $
 * Date   : $Date: 2005/06/22 14:19:40 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * of type <code>CmsResourceTypeXmlPage</code>.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.8 $ 
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
     * @see org.opencms.search.documents.A_CmsVfsDocument#extractContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, String language)
    throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        String result = null;

        try {
            String path = cms.getRequestContext().removeSiteRoot(resource.getRootPath());

            CmsFile file = CmsFile.upgrade(resource, cms);
            String absolutePath = cms.getSitePath(file);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);

            List pageLocales = page.getLocales();
            if (pageLocales.size() == 0) {
                pageLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
            }
            Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                CmsLocaleManager.getLocale(language),
                OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),
                pageLocales);

            List elements = page.getNames(locale);
            StringBuffer content = new StringBuffer();
            for (Iterator i = elements.iterator(); i.hasNext();) {
                String value = page.getStringValue(cms, (String)i.next(), locale);
                if (value != null) {
                    content.append(value);
                }
            }

            result = CmsHtmlExtractor.extractText(content.toString(), page.getEncoding());

            CmsProperty extractionClass = cms.readPropertyObject(
                path,
                CmsPropertyDefinition.PROPERTY_SEARCH_EXTRACTIONCLASS,
                true);
            if (extractionClass != CmsProperty.getNullProperty()) {
                Object ext = Class.forName(extractionClass.getValue()).newInstance();

                if (ext instanceof I_CmsSearchExtractor) {
                    I_CmsSearchExtractor extra = (I_CmsSearchExtractor)ext;
                    result = result + "\n" + extra.extractContent(cms, indexResource, language).getContent();
                } else {
                    throw new CmsIndexException(Messages.get().container(
                        Messages.ERR_EXTRACTION_CLASS_2,
                        resource.getRootPath(),
                        ext.getClass().getName()));
                }
            }

            return new CmsExtractionResult(result);

        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }
}