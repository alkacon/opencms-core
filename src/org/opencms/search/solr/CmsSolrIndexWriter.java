/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.db.CmsPublishedResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsIndexWriter;
import org.opencms.search.I_CmsSearchDocument;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;

/**
 * Implements the index writer for the Solr server used by OpenCms.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrIndexWriter implements I_CmsIndexWriter {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsSolrIndexWriter.class);

    /** The time to wait before a commit is sent to the Solr index.  */
    private int m_commitMs = new Long(OpenCms.getSearchManager().getSolrServerConfiguration().getSolrCommitMs()).intValue();

    /** The Solr index. */
    private CmsSolrIndex m_index;

    /** The Solr server. */
    private SolrServer m_server;

    /**
     * Constructor to create a Solr index writer.<p>
     * 
     * @param server the server to use
     */
    public CmsSolrIndexWriter(SolrServer server) {

        this(server, null);
    }

    /**
     * Creates a new index writer based on the provided standard Lucene IndexWriter for the 
     * provided OpenCms search index instance.<p>
     * 
     * The OpenCms search instance is currently used only for improved logging of the 
     * index operations.<p>
     * 
     * @param server the standard Lucene IndexWriter to use as delegate
     * @param index the OpenCms search index instance this writer to supposed to write to
     */
    public CmsSolrIndexWriter(SolrServer server, CmsSolrIndex index) {

        m_index = index;
        m_server = server;
        if (m_index != null) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_SOLR_WRITER_CREATE_2,
                m_index.getName(),
                m_index.getPath()));
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#close()
     */
    public void close() {

        // nothing to do here
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#commit()
     */
    public void commit() throws IOException {

        if ((m_server != null) && (m_index != null)) {
            try {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_SOLR_WRITER_COMMIT_2,
                    m_index.getName(),
                    m_index.getPath()));
                m_server.commit();
            } catch (SolrServerException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Deletes all documents of the index belonging to this index writer.<p>
     * 
     * @throws IOException if something goes wrong
     */
    public void deleteAllDocuments() throws IOException {

        if ((m_server != null) && (m_index != null)) {
            try {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_SOLR_WRITER_DELETE_ALL_2,
                    m_index.getName(),
                    m_index.getPath()));
                m_server.deleteByQuery("*:*", m_commitMs);
            } catch (SolrServerException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#deleteDocument(org.opencms.db.CmsPublishedResource)
     */
    public void deleteDocument(CmsPublishedResource resource) throws IOException {

        if ((m_server != null) && (m_index != null)) {
            try {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_SOLR_WRITER_DOC_DELETE_3,
                    resource.getRootPath(),
                    m_index.getName(),
                    m_index.getPath()));
                m_server.deleteById(resource.getStructureId().toString(), m_commitMs);
            } catch (SolrServerException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            } catch (SolrException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#optimize()
     */
    public void optimize() {

        // optimization is not recommended
        // should be configured within solrconfig.xml
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#updateDocument(java.lang.String, org.opencms.search.I_CmsSearchDocument)
     */
    public void updateDocument(String rootPath, I_CmsSearchDocument document) throws IOException {

        if ((m_server != null) && (m_index != null)) {
            if (document.getDocument() != null) {
                try {
                    LOG.info(Messages.get().getBundle().key(
                        Messages.LOG_SOLR_WRITER_DOC_UPDATE_3,
                        rootPath,
                        m_index.getName(),
                        m_index.getPath()));
                    m_server.add((SolrInputDocument)document.getDocument(), m_commitMs);
                } catch (SolrServerException e) {
                    throw new IOException(e.getLocalizedMessage(), e);
                }
            }
        }
    }
}
