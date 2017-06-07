/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.report.I_CmsReport;

import java.util.List;

/**
 * Indexes resources for the OpenCms search.<p>
 *
 * This is a high level interface that abstracts the index generation process from the search index itself.
 * Implement this in case special handling of the index generation process is required.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsIndexer {

    /**
     * Incremental index update - delete the index entry for all resources in the given list.<p>
     *
     * @param indexWriter the writer to the index to delete the entries from
     * @param resourcesToDelete a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> instances that must be deleted
     *
     * @throws CmsIndexException if something goes wrong
     */
    void deleteResources(I_CmsIndexWriter indexWriter, List<CmsPublishedResource> resourcesToDelete)
    throws CmsIndexException;

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
    CmsSearchIndexUpdateData getUpdateData(CmsSearchIndexSource source, List<CmsPublishedResource> publishedResources)
    throws CmsIndexException;

    /**
     * Returns <code>true</code> if this VFS indexer is able to resolve locale dependencies between documents.<p>
     *
     * @return <code>true</code> if this VFS indexer is able to resolve locale dependencies between documents
     */
    boolean isLocaleDependenciesEnable();

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
    void rebuildIndex(I_CmsIndexWriter writer, CmsIndexingThreadManager threadManager, CmsSearchIndexSource source)
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
    void updateResources(
        I_CmsIndexWriter writer,
        CmsIndexingThreadManager threadManager,
        List<CmsPublishedResource> resourcesToUpdate) throws CmsIndexException;
}