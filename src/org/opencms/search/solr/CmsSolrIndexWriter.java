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

import org.opencms.search.I_CmsIndexWriter;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.fields.I_CmsSearchField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;

/**
 * Implements the index writer for the Solr server used by OpenCms.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrIndexWriter implements I_CmsIndexWriter {

    /** The Solr server. */
    private SolrServer m_server;

    /**
     * Constructor to create a Solr index writer.<p>
     * 
     * @param server the server to use
     */
    public CmsSolrIndexWriter(SolrServer server) {

        m_server = server;
    }

    /**
     * 
     * @see org.opencms.search.I_CmsIndexWriter#close()
     */
    public void close() {

        // nothing to do here
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#commit()
     */
    public void commit() throws IOException {

        try {
            m_server.commit();
        } catch (SolrServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#deleteDocuments(java.lang.String)
     */
    public void deleteDocuments(String rootPath) throws IOException {

        try {
            m_server.deleteByQuery(I_CmsSearchField.FIELD_PATH + ":" + rootPath + "*");
        } catch (SolrServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#optimize()
     */
    public void optimize() throws IOException {

        try {
            m_server.optimize();
        } catch (SolrServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexWriter#updateDocument(java.lang.String, org.opencms.search.I_CmsSearchDocument)
     */
    public void updateDocument(String rootPath, I_CmsSearchDocument document) throws IOException {

        if (document.getDocument() != null) {
            SolrInputDocument doc = (SolrInputDocument)document.getDocument();
            List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>(1);
            docs.add(doc);
            updateSolrDocuments(docs);
        }
    }

    /**
     * Deletes the the whole index.<p>
     * 
     * @throws IOException if something goes wrong
     */
    protected void deleteSolrIndex() throws IOException {

        try {
            m_server.deleteByQuery(CmsSolrQuery.DEFAULT_QUERY);
        } catch (SolrServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the list of given documents.<p>
     * 
     * @param docs the documents to update
     * 
     * @throws IOException if something goes wrong
     */
    protected void updateSolrDocuments(List<SolrInputDocument> docs) throws IOException {

        UpdateRequest req = new UpdateRequest();
        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, false);
        req.add(docs);
        try {
            req.process(m_server);
        } catch (SolrServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
