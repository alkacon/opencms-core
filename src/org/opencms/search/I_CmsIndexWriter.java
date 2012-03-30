/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import java.io.IOException;

import org.apache.lucene.document.Document;

/**
 * Abstracts the index writer implementation for the most important index manipulation operations.
 * 
 * @since 8.0.2 
 */
public interface I_CmsIndexWriter {

    /**
     * Close this IndexWriter.<p>
     * 
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Commit all previous operations.<p>
     * 
     * @throws IOException
     */
    void commit() throws IOException;

    /**
     * Delete a document from the index.<p>
     * 
     * @param rootPath the root path of the document to delete
     * 
     * @throws IOException in case something goes wrong
     */
    void deleteDocuments(String rootPath) throws IOException;

    /**
     * Optimizes the index.<p>
     * 
     * Please note that as of Lucene 3.5, the direct use of optimize is discouraged
     * as Lucene apparently is now able to manage the file structure so efficiently that
     * frequent optimizations are not longer required.<p>
     * 
     * @throws IOException
     */
    void optimize() throws IOException;

    /**
     * Update a document in the index.<p>
     * 
     * @param rootPath the root path of the document to update
     * @param document the document to update
     * 
     * @throws IOException in case something goes wrong
     */
    void updateDocument(String rootPath, Document document) throws IOException;
}