/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsXmlPageDocument.java,v $
 * Date   : $Date: 2004/06/28 07:47:32 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsIndexResource;
import org.opencms.xml.page.CmsXmlPage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * of type <code>CmsResourceTypeXmlPage</code>.<p>
 * 
 * @version $Revision: 1.10 $ $Date: 2004/06/28 07:47:32 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsXmlPageDocument extends CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsXmlPageDocument (CmsObject cms, String name) {
        super(cms, name);
    }
    
    /**
     * Returns the raw text content of a given vfs resource of type <code>CmsResourceTypeXmlPage</code>.<p>
     * 
     * @see org.opencms.search.documents.CmsVfsDocument#getRawContent(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public String getRawContent(CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        String rawContent = null;
        
        try {
            CmsFile file = CmsFile.upgrade(resource, m_cms);
            String absolutePath = m_cms.getSitePath(file);
            CmsXmlPage page = CmsXmlPage.unmarshal(m_cms, file);
            
            List pageLocales = page.getLocales();
            if (pageLocales.size() == 0) {
                pageLocales = OpenCms.getLocaleManager().getDefaultLocales(m_cms, absolutePath);
            }
            Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                    CmsLocaleManager.getLocale(language), 
                    OpenCms.getLocaleManager().getDefaultLocales(m_cms, absolutePath), 
                    pageLocales);
            
            List elements = page.getNames(locale);
            StringBuffer content = new StringBuffer();
            for (Iterator i = elements.iterator(); i.hasNext();) {
                content.append(page.getRawContent((String)i.next(), locale));
            }
            
            CmsHtmlExtractor extractor = new CmsHtmlExtractor();
            rawContent = extractor.extractText(content.toString());
            
        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed", exc);
        }
        
        return rawContent;
    }
    
    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsIndexResource resource, String language) throws CmsException {
                   
        Document document = super.newInstance(resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));
        
        return document;
    }
}
