/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsDocumentFactory.java,v $
 * Date   : $Date: 2006/10/14 08:44:57 $
 * Version: $Revision: 1.24.8.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.search.CmsSearchIndex;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * Used to create index Lucene Documents for OpenCms resources,
 * controls the text extraction algorithm used for a specific OpenCms resource type / MIME type combination.<p>
 * 
 * The configuration of the search index is defined in <code>opencms-search.xml</code>.
 * There you can associate a combintion of OpenCms resource types and MIME types to an instance
 * of this factory. This rather complex configuration is required because only the combination of
 * OpenCms resource type and MIME type can decide what to use for search indexing. 
 * For example, if the OpenCms resource type is <code>plain</code>,
 * the extraction algorithm for MIME types <code>.html</code> and <code>.txt</code> must be different.
 * On the other hand, the MIME type <code>.html</code> in OpenCms can be almost any resource type,
 * like <code>xmlpage</code>, <code>xmlcontent</code> or even <code>jsp</code>.<p> 
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.24.8.1 $ 
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
     * Creates the Lucene Document for the given index resource and the given search index.<p>
     * 
     * This triggers the indexing process for the given index resource accoring to the configuration 
     * of the provided index.<p>
     * 
     * The provided index resource contains the basic contents to index.
     * The provided search index contains the configuration what to index, such as the locale and 
     * possible special field mappings.<p>
     * 
     * @param cms the cms object used to access the OpenCms VFS
     * @param resource the search index resource to create the Lucene document from
     * @param index the search index to create the Document for
     * 
     * @return the Lucene Document for the given index resource and the given search index
     * 
     * @throws CmsException if something goes wrong
     */
    Document createDocument(CmsObject cms, A_CmsIndexResource resource, CmsSearchIndex index) throws CmsException;

    /**
     * Returns the list of accepted keys for the resource types that can be indexed using this document factory.<p>
     * 
     * The result List contains String objects. 
     * This String is later matched against {@link A_CmsIndexResource#getDocumentKey(boolean)} to find
     * the corrospondig {@link I_CmsDocumentFactory} for a resource to index.<p> 
     * 
     * The list of accepted resource types may contain a catch-all entry "*";
     * in this case, a list for all possible resource types is returned,
     * calculated by a logic depending on the document handler class.<p>
     * 
     * @param resourceTypes list of accepted resource types
     * @param mimeTypes list of accepted mime types
     * 
     * @return the list of accepted keys for the resource types that can be indexed using this document factory (String objects)
     * 
     * @throws CmsException if something goes wrong
     */
    List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException;

    /**
     * Returns the name of this document type factory.<p>
     * 
     * @return the name of this document type factory
     */
    String getName();
}