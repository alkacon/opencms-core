/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsGenericDocument.java,v $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/11 15:01:00 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsGenericDocument implements I_CmsDocumentFactory {

    /**
     * The cms object
     */
    protected CmsObject m_cms;
    
    /**
     * Name of the documenttype
     */
    protected String m_name;
    
    /**
     * Creates a new instance of a lucene document for CmsResources.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsGenericDocument(CmsObject cms, String name) {
        m_cms = cms;
        m_name = name;
    }

    /**
     * Returns the raw text content of a given resource.<p>
     * 
     * @param resource the resource
     * @param language the language requested
     * @return the raw text content
     * @throws CmsException if something goes wrong
     */
    public String getRawContent(CmsResource resource, String language) throws CmsException {        
        if (resource == null) {
            throw new CmsIndexException("Can not get raw content for language " + language + " from a 'null' resource");
        }
        return "";
    }
    
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(com.opencms.file.CmsResource, java.lang.String)
     */
    public Document newInstance (CmsResource resource, String language) throws CmsException {
        
        Document document = new Document();
        String path = m_cms.getRequestContext().removeSiteRoot(resource.getRootPath());
        String value;

        if ((value = m_cms.readProperty(path, I_CmsConstants.C_PROPERTY_TITLE)) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_TITLE, value));
        }
        if ((value = m_cms.readProperty(path, I_CmsConstants.C_PROPERTY_KEYWORDS)) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_KEYWORDS, value));
        }        
        if ((value = m_cms.readProperty(path, I_CmsConstants.C_PROPERTY_DESCRIPTION)) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_DESCRIPTION, value));
        }                

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, 
            DateField.timeToString(resource.getDateCreated())));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, 
            DateField.timeToString(resource.getDateLastModified())));
    
        document.add(Field.UnIndexed(I_CmsDocumentFactory.DOC_PATH, path));

        return document;
    }
        
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {
        return m_name;
    }
}
