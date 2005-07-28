/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/I_CmsIndexer.java,v $
 * Date   : $Date: 2005/07/28 15:53:10 $
 * Version: $Revision: 1.13 $
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

/**
 * Interface for an indexer indexing Cms resources.<p>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsIndexer {

    /**
     * Incremental index update - delete the index entry for all resources in the given list.<p> 
     * 
     * @param reader the index reader to delete the entries from
     * @param resourcesToDelete a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be deleted
     * 
     * @throws CmsIndexException if something goes wrong
     */
    void deleteResources(IndexReader reader, List resourcesToDelete) throws CmsIndexException;

    /**
     * Returns an index resource for a specified Lucene search result document.<p>
     * 
     * Implementations of this method have to check if the current user has read permissions
     * on the Cms resource represented by the Lucene document.<p>
     * 
     * If this check fails, the implementation must return null as a result.<p>
     * 
     * @param cms the OpenCms context to use when reading resources from the VFS
     * @param doc the Lucene search result document
     * @return a new index resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see A_CmsIndexResource
     */
    A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws CmsException;

    /**
     * Calculates the data for an incremental search index update.<p>
     * 
     * @param source the search index source to update
     * @param publishedResources a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects that are to be updated
     * 
     * @return a container with the information about the resources to delete and / or update
     * 
     * @throws CmsIndexException if something goes wrong
     */
    CmsSearchIndexUpdateData getUpdateData(CmsSearchIndexSource source, List publishedResources)
    throws CmsIndexException;

    /**
     * Creates and initializes a new instance of this indexer implementation.<p>
     * 
     * @param cms the OpenCms user context to use when reading resources from the VFS during indexing
     * @param report the report to write the indexing output to
     * @param index the search index to update
     * 
     * @return a new instance of this indexer implementation
     */
    I_CmsIndexer newInstance(CmsObject cms, I_CmsReport report, CmsSearchIndex index);

    /**
     * Rebuilds the index for the given configured index source.<p>
     * 
     * This is used when the index is fully rebuild, not for updating only some parts
     * of an existing index.<p>
     * 
     * @param writer the index writer to write the update to
     * @param source the search index source to update
     * @param threadManager the thread manager to use when extracting the document text
     * 
     * @throws CmsIndexException if something goes wrong
     */
    void rebuildIndex(IndexWriter writer, CmsIndexingThreadManager threadManager, CmsSearchIndexSource source)
    throws CmsIndexException;

    /**
     * Incremental index update - create a new index entry for all resources in the given list.<p> 
     * 
     * @param writer the index writer to write the update to
     * @param resourcesToUpdate a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be updated
     * @param threadManager the thread manager to use when extracting the document text
     * 
     * @throws CmsIndexException if something goes wrong
     */
    void updateResources(IndexWriter writer, CmsIndexingThreadManager threadManager, List resourcesToUpdate)
    throws CmsIndexException;
}