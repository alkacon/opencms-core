/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsDocumentFactory.java,v $
 * Date   : $Date: 2005/03/24 10:25:26 $
 * Version: $Revision: 1.17 $
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
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * Implementation interface for lucene document factories used in OpenCms.<p>
 * 
 * @version $Revision: 1.17 $ $Date: 2005/03/24 10:25:26 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public interface I_CmsDocumentFactory {

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

    /** Combined search field for all document "meta" information, that is title, keywords, description and root path. */
    String DOC_META = "meta";

    /** Search field for document path within a site. */
    String DOC_PATH = "path";

    /** Search field for document priority. */
    String DOC_PRIORITY = "priority";

    /** Search field for optimized path searches. */
    String DOC_ROOT = "root";

    /** (Internal used) Search field for the document source. */
    String DOC_SOURCE = "source";

    /** Search field for document title. */
    String DOC_TITLE = "title";

    /** Value for "high" search priority. */
    String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** Value for "low" search priority. */
    String SEARCH_PRIORITY_LOW_VALUE = "low";

    /** Value for "maximum" search priority. */
    String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** Value for "normal" search priority. */
    String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /**
     * Extractes the content of a given resource according to the resource file type.<p>
     * 
     * @param cms the cms object
     * @param resource a cms resource
     * @param language the requested language
     * @return the extracted content of the resource
     * @throws CmsException if somethin goes wrong
     */
    I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource resource, String language)
    throws CmsException;

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