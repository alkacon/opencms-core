/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsPageDocument.java,v $
 * Date   : $Date: 2004/07/08 15:21:13 $
 * Version: $Revision: 1.3 $
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
package com.opencms.legacy;

import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.documents.CmsVfsDocument;
import org.opencms.search.documents.I_CmsDocumentFactory;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import com.opencms.template.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.htmlparser.parserapplications.StringExtractor;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * of type <code>CmsResourceTypePage</code>.<p>
 * 
 * @version $Revision: 1.3 $ $Date: 2004/07/08 15:21:13 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsPageDocument extends CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsPageDocument (CmsObject cms, String name) {
        super(cms, name);
    }
    
    /**
     * Gets the raw text content of a cms resource.<p>
     * 
     * @see org.opencms.search.documents.CmsVfsDocument#getRawContent(org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public String getRawContent(A_CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        String rawContent = null;
        
        try {
            CmsXmlTemplateFile file = new CmsXmlTemplateFile(m_cms, m_cms.getRequestContext().removeSiteRoot(resource.getRootPath()));        
            String content = file.getProcessedTemplateContent(null, null);
            
            StringExtractor extractor = new StringExtractor(content);
            rawContent = extractor.extractStrings(true);
        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed", exc);
        }
        
        return rawContent;
    }
    
    /**
     * Creates a new lucene document instance for a resource of type <code>CmsResourceTypePage</code>.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance (A_CmsIndexResource resource, String language) throws CmsException {
                   
        Document document = super.newInstance(resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));
        
        return document;
    }
}
