/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsDocumentFactory.java,v $
 * Date   : $Date: 2004/02/20 13:35:45 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexResource;


import org.apache.lucene.document.Document;

/**
 * Implementation interface for lucene document factories used in OpenCms.<p>
 * 
 * @version $Revision: 1.5 $ $Date: 2004/02/20 13:35:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public interface I_CmsDocumentFactory {

    /** Channel of cos document */
    String DOC_CHANNEL = "channel";

    /** Search field for document content */
    String DOC_CONTENT = "content";
    
    /** Content definition of cos document */
    String DOC_CONTENT_DEFINITION = "contentdefinition";
    
    /** Content id of cos document */
    String DOC_CONTENT_ID = "contentid";

    /** Search field for document creation date */
    String DOC_DATE_CREATED = "created";

    /** Search field for document last update */
    String DOC_DATE_LASTMODIFIED = "lastmodified";
    
    /** Search field for document description */
    String DOC_DESCRIPTION = "description";
    
    /** Search field for document keywords */
    String DOC_KEYWORDS = "keywords";
    
    /** Search field for document path within a site */
    String DOC_PATH = "path";

    /** Search field for document title */
    String DOC_TITLE = "title";
    
    /**
     * Returns the document key for the search manager.<p>
     * 
     * @param resourceType the resource type to get the document key for
     * @return the document key for the search manager
     * @throws InstantiationException if something goes wrong
     * @throws IllegalAccessException if something goes wrong
     * @throws ClassNotFoundException if something goes wrong
     */
    String getDocumentKey(String resourceType) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
    
    /**
     * Returns the name of the documenttype
     * 
     * @return the name of the documenttype
     */
    String getName();
    
    /**
     * Returns the raw content of a given resource according to the concrete file type.<p>
     * 
     * @param resource a cms resource
     * @param language the requested language
     * @return the raw textual content of the resource
     * @throws CmsException if somethin goes wrong
     */
    String getRawContent(CmsIndexResource resource, String language) throws CmsException;
    
    /**
     * Creates a new instance of a lucene document type for the concrete file type.<p>
     * 
     * @param resource a cms resource
     * @param language the requested language
     * @return a lucene document for the given resource
     * @throws CmsException if something goes wrong
     */
    Document newInstance(CmsIndexResource resource, String language) throws CmsException;
}