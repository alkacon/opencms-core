/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsExcelDocument.java,v $
 * Date   : $Date: 2004/02/11 15:01:00 $
 * Version: $Revision: 1.1 $
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
import org.opencms.search.util.ExcelExtractor;

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/11 15:01:00 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsExcelDocument extends CmsGenericDocument {

    /**
     * Creates a new instance of a lucene document for Excel files.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsExcelDocument(CmsObject cms, String name) {
        super(cms, name);
    }
    
    /**
     * @see org.opencms.search.documents.CmsGenericDocument#getRawContent(com.opencms.file.CmsResource, java.lang.String)
     */
    public String getRawContent(CmsResource resource, String language) throws CmsException {
        
        String rawContent = null;
        
        try {
            CmsFile file = m_cms.readFile(m_cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
            if (!(file.getLength() > 0)) {
                throw new CmsIndexException("Resource " + resource.getRootPath() + " has no content");
            }    
            ExcelExtractor extractor = new ExcelExtractor();
            rawContent = extractor.extractText(new ByteArrayInputStream(file.getContents()));
   
        } catch (FileNotFoundException exc) {
            // special case: catch Excel95 format error
            if (exc.getMessage() != null && exc.getMessage().indexOf("Workbook") > 0) {
                throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed, not in Excel97 format", exc);
            } else {
                throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed", exc);
            }
        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed", exc);
        }
        
        return rawContent;
    }
    
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(com.opencms.file.CmsResource, java.lang.String)
     */
    public Document newInstance (CmsResource resource, String language) throws CmsException {

        Document document = super.newInstance(resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));
        
        return document;
    }
}
