/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/I_CmsIndexer.java,v $
 * Date   : $Date: 2005/06/22 14:19:40 $
 * Version: $Revision: 1.10 $
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * Interface for an indexer indexing Cms resources.<p>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsIndexer {

    /**
     * Initializes the indexer.<p>
     * 
     * @param report the report
     * @param index the search index
     * @param indexSource the search index source
     * @param writer writer to write the search index in the physical file system
     * @param threadManager the tread manager
     * 
     * @throws CmsIndexException if something goes wrong
     */
    void init(
        I_CmsReport report,
        CmsSearchIndex index,
        CmsSearchIndexSource indexSource,
        IndexWriter writer,
        CmsIndexingThreadManager threadManager) throws CmsIndexException;

    /**
     * Creates new index entries for all resources below the given path.<p>
     * 
     * @param cms the current user's CmsObject
     * @param source the source of the resources
     * @param path the path to the root of the subtree to index
     * 
     * @throws CmsIndexException if something goes wrong
     */
    void updateIndex(CmsObject cms, String source, String path) throws CmsIndexException;

    /**
     * Returns an index resource for a specified Lucene search result document.<p>
     * 
     * Implementations of this method have to check if the current user has read permissions
     * on the Cms resource represented by the Lucene document.<p>
     * 
     * If this check fails, the implementation must return null as a result.<p>
     * 
     * @param cms the current user's CmsObject
     * @param doc the Lucene search result document
     * @return a new index resource
     * @throws CmsException if something goes wrong
     * @see A_CmsIndexResource
     */
    A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws CmsException;

}