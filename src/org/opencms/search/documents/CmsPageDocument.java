/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsPageDocument.java,v $
 * Date   : $Date: 2004/02/13 11:27:46 $
 * Version: $Revision: 1.2 $
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

import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsIndexResource;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.template.CmsXmlTemplateFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.htmlparser.parserapplications.StringExtractor;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/13 11:27:46 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsPageDocument extends CmsVfsDocument {

    /**
     * Creates a new instance of a lucene document for cms html pages.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsPageDocument (CmsObject cms, String name) {
        super(cms, name);
    }
    
    /**
     * @see org.opencms.search.documents.CmsVfsDocument#getRawContent(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public String getRawContent(CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getObject();
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
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsIndexResource resource, String language) throws CmsException {
                   
        Document document = super.newInstance(resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));
        
        return document;
    }
}
