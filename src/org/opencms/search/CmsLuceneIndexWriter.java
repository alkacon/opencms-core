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

import org.opencms.main.CmsLog;
import org.opencms.search.fields.CmsSearchField;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

/**
 * Delegates indexing to a standard Lucene IndexWriter.<p>
 * 
 * @since 8.0.2
 */
public class CmsLuceneIndexWriter implements I_CmsIndexWriter {

    /** The threshold for the commits until optimize is called. */
    public static final int COMMIT_OPTIMIZE_THRESHOLD = 1000;

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsLuceneIndexWriter.class);

    /** The OpenCms search index instance this writer to supposed to write to. */
    private CmsSearchIndex m_index;

    /** The Lucene index writer to use. */
    private final IndexWriter m_indexWriter;

    /** A counter for the commits until optimize is called. */
    private int m_optimizeCounter;

    /**
     * Creates a new index writer based on the provided standard Lucene IndexWriter.<p>
     * 
     * @param indexWriter the standard Lucene IndexWriter to use as delegate
     */
    public CmsLuceneIndexWriter(IndexWriter indexWriter) {

        this(indexWriter, null);
    }

    /**
     * Creates a new index writer based on the provided standard Lucene IndexWriter for the 
     * provided OpenCms search index instance.<p>
     * 
     * The OpenCms search instance is currently used only for improved logging of the 
     * index operations.<p>
     * 
     * @param indexWriter the standard Lucene IndexWriter to use as delegate
     * @param index the OpenCms search index instance this writer to supposed to write to
     */
    public CmsLuceneIndexWriter(IndexWriter indexWriter, CmsSearchIndex index) {

        m_indexWriter = indexWriter;
        m_optimizeCounter = 0;
        m_index = index;
        if ((m_index != null) && LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_INDEX_WRITER_MSG_CREATE_2,
                m_index.getName(),
                m_index.getPath()));
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#close()
     */
    public void close() throws IOException {

        // make sure directory is unlocked when it is closed
        Directory dir = m_indexWriter.getDirectory();
        try {
            if ((m_index != null) && LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_INDEX_WRITER_MSG_CLOSE_2,
                    m_index.getName(),
                    m_index.getPath()));
            }
            m_indexWriter.close();
        } finally {
            if ((dir != null) && IndexWriter.isLocked(dir)) {
                IndexWriter.unlock(dir);
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#commit()
     */
    public void commit() throws IOException {

        if ((m_index != null) && LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_INDEX_WRITER_MSG_COMMIT_2,
                m_index.getName(),
                m_index.getPath()));
        }
        m_indexWriter.commit();
        m_optimizeCounter++;
        if (m_optimizeCounter >= COMMIT_OPTIMIZE_THRESHOLD) {
            // optimize the search index when the threshold is reached
            optimize();
            m_indexWriter.commit();
            m_optimizeCounter = 0;
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#deleteDocuments(java.lang.String)
     */
    public void deleteDocuments(String rootPath) throws IOException {

        // search for an exact match on the document root path
        Term term = new Term(CmsSearchField.FIELD_PATH, rootPath);
        if ((m_index != null) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_INDEX_WRITER_MSG_DOC_DELETE_3,
                rootPath,
                m_index.getName(),
                m_index.getPath()));
        }
        m_indexWriter.deleteDocuments(term);
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#optimize()
     */
    public void optimize() throws IOException {

        if ((m_index != null) && LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_INDEX_WRITER_MSG_OPTIMIZE_2,
                m_index.getName(),
                m_index.getPath()));
        }
        int oldPriority = Thread.currentThread().getPriority();
        // we don't want the priority too low as the process should complete as fast as possible
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY / 2);
        m_indexWriter.optimize();
        Thread.currentThread().setPriority(oldPriority);
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#updateDocument(java.lang.String, org.apache.lucene.document.Document)
     */
    public void updateDocument(String rootPath, Document document) throws IOException {

        Term pathTerm = new Term(CmsSearchField.FIELD_PATH, rootPath);
        if ((m_index != null) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_INDEX_WRITER_MSG_DOC_UPDATE_3,
                rootPath,
                m_index.getName(),
                m_index.getPath()));
        }
        m_indexWriter.updateDocument(pathTerm, document);
    }
}