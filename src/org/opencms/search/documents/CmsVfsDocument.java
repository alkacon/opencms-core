/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsVfsDocument.java,v $
 * Date   : $Date: 2005/03/04 13:42:45 $
 * Version: $Revision: 1.13 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.A_CmsIndexResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Lucene document factory class to extract index data from a vfs resource 
 * of any type derived from <code>CmsResource</code>.<p>
 * 
 * @version $Revision: 1.13 $ $Date: 2005/03/04 13:42:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsVfsDocument implements I_CmsDocumentFactory {

    /** The vfs prefix for document keys. */
    public static final String C_DOCUMENT_KEY_PREFIX = "VFS";
    
    /**
     * Name of the documenttype.
     */
    protected String m_name;
    
    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsVfsDocument(String name) {
        m_name = name;
    }

    /**
     * Returns the raw text content of a vfs resource.<p>
     * NOT IMPLEMENTED.
     * 
     * @param cms the cms object 
     * @param resource the resource
     * @param language the language requested
     * @return the raw text content
     * @throws CmsException if something goes wrong
     */
    public String getRawContent(CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException {        
        if (resource == null) {
            throw new CmsIndexException("Can not get raw content for language " + language + " from a 'null' resource");
        }
        return "";
    }
    
    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException {
        
        Document document = new Document();
        CmsResource res = (CmsResource)resource.getData();
        String path = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
        String value;

        if ((value = cms.readPropertyObject(path, I_CmsConstants.C_PROPERTY_TITLE, false).getValue()) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_TITLE, value));
        }
        if ((value = cms.readPropertyObject(path, I_CmsConstants.C_PROPERTY_KEYWORDS, false).getValue()) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_KEYWORDS, value));
        }        
        if ((value = cms.readPropertyObject(path, I_CmsConstants.C_PROPERTY_DESCRIPTION, false).getValue()) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_DESCRIPTION, value));
        }                

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, 
            DateField.timeToString(res.getDateCreated())));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, 
            DateField.timeToString(res.getDateLastModified())));
    
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_PATH, resource.getRootPath()));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_SOURCE, resource.getSource()));
        
        return document;
    }
        
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {
        
        return m_name;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKey(java.lang.String)
     */
    public String getDocumentKey(String resourceType) throws CmsException {
        
        try {
            return C_DOCUMENT_KEY_PREFIX + ((I_CmsResourceType)Class.forName(resourceType).newInstance()).getTypeId();
        } catch (Exception exc) {
            throw new CmsException("Instanciation of resource class " + resourceType + " failed", exc);
        }
    }
    
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {
     
        ArrayList keys = new ArrayList();
        
        if (resourceTypes.contains("*")) {
            ArrayList allTypes = new ArrayList();
            for (Iterator i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
                allTypes.add(resourceType.getTypeName());
            }
            resourceTypes = allTypes;
        }
        
        try {
            for (Iterator i = resourceTypes.iterator(); i.hasNext();) {
                
                int id = OpenCms.getResourceManager().getResourceType((String)i.next()).getTypeId();                
                for (Iterator j = mimeTypes.iterator(); j.hasNext();) {
                    keys.add(C_DOCUMENT_KEY_PREFIX + id + ":" + (String)j.next());
                }
                if (mimeTypes.isEmpty()) {
                    keys.add(C_DOCUMENT_KEY_PREFIX + id);
                }
            }
        } catch (Exception exc) {
            throw new CmsException ("Creation of document keys failed.", exc);
        }
        
        return keys;
    }
}
