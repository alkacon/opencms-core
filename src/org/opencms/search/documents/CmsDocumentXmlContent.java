/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/CmsDocumentXmlContent.java,v $
 * Date   : $Date: 2005/03/25 18:35:09 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * of type <code>CmsResourceTypeXmlContent</code>.<p>
 * 
 * @version $Revision: 1.2 $ $Date: 2005/03/25 18:35:09 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsDocumentXmlContent extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsDocumentXmlContent(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource of type <code>CmsResourceTypeXmlContent</code>.<p>
     * 
     * @see org.opencms.search.documents.A_CmsVfsDocument#extractContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, String language)
    throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        String result = null;

        try {
            CmsFile file = CmsFile.upgrade(resource, cms);
            String absolutePath = cms.getSitePath(file);
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

            List locales = xmlContent.getLocales();
            if (locales.size() == 0) {
                locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
            }
            Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                CmsLocaleManager.getLocale(language),
                OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),
                locales);

            List elements = xmlContent.getNames(locale);
            StringBuffer content = new StringBuffer();
            for (Iterator i = elements.iterator(); i.hasNext();) {
                I_CmsXmlContentValue value = xmlContent.getValue((String)i.next(), locale);
                String plainText = value.getPlainText(cms);
                if (plainText != null) {
                    content.append(plainText);
                    content.append('\n');
                }
            }

            result = content.toString();
            // CmsHtmlExtractor extractor = new CmsHtmlExtractor();
            //rawContent = extractor.extractText(content.toString(), page.getEncoding());

        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed", exc);
        }

        return new CmsExtractionResult(result);
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {

        if (resourceTypes.contains("*")) {
            ArrayList allTypes = new ArrayList();
            for (Iterator i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
                if (resourceType instanceof CmsResourceTypeXmlContent
                    && ((CmsResourceTypeXmlContent)resourceType).getConfiguration().containsKey(
                        CmsResourceTypeXmlContent.C_CONFIGURATION_SCHEMA)) {
                    allTypes.add(resourceType.getTypeName());
                }
            }
            resourceTypes = allTypes;
        }

        return super.getDocumentKeys(resourceTypes, mimeTypes);
    }
}