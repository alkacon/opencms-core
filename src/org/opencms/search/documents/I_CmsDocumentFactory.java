/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsDocumentFactory.java,v $
 * Date   : $Date: 2005/06/22 14:19:40 $
 * Version: $Revision: 1.22 $
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
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsIndexResource;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * Implementation interface for lucene document factories used in OpenCms.<p>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsDocumentFactory extends I_CmsSearchExtractor {

    /** Contains the (optional) category of the document. */
    String DOC_CATEGORY = "category";

    /** Search field for document content. */
    String DOC_CONTENT = "content";

    /** Search field for document creation date. */
    String DOC_DATE_CREATED = "created";

    /** Search field for document last update. */
    String DOC_DATE_LASTMODIFIED = "lastmodified";

    /** Search field for document description. */
    String DOC_DESCRIPTION = "description";

    /** Search field for document keywords. */
    String DOC_KEYWORDS = "keywords";

    /** Combines all document "meta" information, that is "title", "keywords" and "description". */
    String DOC_META = "meta";

    /** Contains the document root path in the VFS. */
    String DOC_PATH = "path";

    /** Contains the (optional) document priority, which can be used to boost the document in the result list. */
    String DOC_PRIORITY = "priority";

    /** Contains a special format of the document root path in the VFS for optimized searches. */
    String DOC_ROOT = "root";

    /** Contains the document title in an analyzed form used for searching in the title. */
    String DOC_TITLE_INDEXED = "title";

    /** Contains the document title as a keyword used for sorting and also for retrieving the title text. */
    String DOC_TITLE_KEY = "title-key";

    /** Contains the type of the document. */
    String DOC_TYPE = "type";

    /** Value for "high" search priority. */
    String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** Value for "low" search priority. */
    String SEARCH_PRIORITY_LOW_VALUE = "low";

    /** Value for "maximum" search priority. */
    String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** Value for "normal" search priority. */
    String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /**
     * Returns the document key for the search manager.<p>
     * 
     * @param resourceType the resource type to get the document key for
     * @return the document key for the search manager
     * @throws CmsException if something goes wrong
     */
    String getDocumentKey(String resourceType) throws CmsException;

    /**
     * Returns a list of document keys for the documenttype.<p>
     * 
     * The list of accepted resource types may contain a catch-all entry "*";
     * in this case, a list for all possible resource types is returned,
     * calculated by a logic depending on the document handler class.<p>
     * 
     * @param resourceTypes list of accepted resource types
     * @param mimeTypes list of accepted mime types
     * @return a list of document keys for this document factory
     * @throws CmsException if something goes wrong
     */
    List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException;

    /**
     * Returns the name of the documenttype.<p>
     * 
     * @return the name of the documenttype
     */
    String getName();

    /**
     * Creates a new instance of a lucene document type for the concrete file type.<p>
     * 
     * @param cms the cms object
     * @param resource a cms resource
     * @param language the requested language
     * @return a lucene document for the given resource
     * @throws CmsException if something goes wrong
     */
    Document newInstance(CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException;
}