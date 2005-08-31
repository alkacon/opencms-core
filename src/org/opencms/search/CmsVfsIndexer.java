/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsVfsIndexer.java,v $
 * Date   : $Date: 2005/08/31 16:20:24 $
 * Version: $Revision: 1.33 $
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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 * Implementation for an indexer indexing VFS Cms resources.<p>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.33 $ 
 * 
 * @since 6.0.0 
 */
public class CmsVfsIndexer implements I_CmsIndexer {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsIndexer.class);

    /** The OpenCms user context to use when reading resources from the VFS during indexing. */
    private CmsObject m_cms;

    /** The index. */
    private CmsSearchIndex m_index;

    /** The report. */
    private I_CmsReport m_report;

    /**
     * @see org.opencms.search.I_CmsIndexer#deleteResources(org.apache.lucene.index.IndexReader, java.util.List)
     */
    public void deleteResources(IndexReader reader, List resourcesToDelete) {

        if ((resourcesToDelete == null) || resourcesToDelete.isEmpty()) {
            // nothing to délete
            return;
        }

        // contains all resources already deleted to avoid multiple deleting in case of siblings
        List resourcesAlreadyDeleted = new ArrayList(resourcesToDelete.size());

        Iterator i = resourcesToDelete.iterator();
        while (i.hasNext()) {
            // iterate all resources in the given list of resources to delete
            CmsPublishedResource res = (CmsPublishedResource)i.next();
            String rootPath = res.getRootPath();
            if (!resourcesAlreadyDeleted.contains(rootPath)) {
                // ensure siblings are only deleted once per update
                resourcesAlreadyDeleted.add(rootPath);
                // search for an exact match on the document root path
                Term term = new Term(I_CmsDocumentFactory.DOC_PATH, rootPath);
                try {
                    // delete all documents with this term from the index
                    reader.delete(term);
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().key(
                            Messages.LOG_IO_INDEX_DOCUMENT_DELETE_2,
                            rootPath,
                            m_index.getName()), e);
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#getIndexResource(org.opencms.file.CmsObject, org.apache.lucene.document.Document)
     */
    public A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws CmsException {

        Field f;
        A_CmsIndexResource result = null;

        if ((f = doc.getField(I_CmsDocumentFactory.DOC_PATH)) != null) {

            String path = cms.getRequestContext().removeSiteRoot(f.stringValue());
            CmsResource resource = cms.readResource(path);
            // an exception would have been thrown if the user has no read persmissions
            result = new CmsVfsIndexResource(resource);
        }

        return result;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#getUpdateData(org.opencms.search.CmsSearchIndexSource, java.util.List)
     */
    public CmsSearchIndexUpdateData getUpdateData(CmsSearchIndexSource source, List publishedResources) {

        // create a new update collection from this indexer and the given index source
        CmsSearchIndexUpdateData result = new CmsSearchIndexUpdateData(source, this);

        Iterator i = publishedResources.iterator();
        while (i.hasNext()) {
            // check all published resources if they match this indexer / source
            CmsPublishedResource resource = (CmsPublishedResource)i.next();
            // VFS resources will always have a structure id
            if (!resource.getStructureId().isNullUUID()) {
                // use utility method from CmsProject to check if published resource is "inside" this index source
                if (CmsProject.isInsideProject(source.getResourcesNames(), resource.getRootPath())) {
                    // the resource is "inside" this index source
                    if (resource.isNew()) {
                        // new resource just needs to be updated
                        if (isResourceInTimeWindow(resource)) {
                            // update only if resource is in time window
                            result.addResourceToUpdate(resource);
                        }
                    } else if (resource.isDeleted()) {
                        // deleted resource just needs to be removed
                        result.addResourceToDelete(resource);
                    } else if (resource.isChanged() || resource.isUnChanged()) {
                        // changed (or unchaged) resource must be removed first, and then updated
                        // note: unchanged resources can be siblings that have been added from the online project,
                        //       these must be treated as if the resource had changed
                        result.addResourceToDelete(resource);
                        if (isResourceInTimeWindow(resource)) {
                            // update only if resource is in time window
                            result.addResourceToUpdate(resource);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#newInstance(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport, org.opencms.search.CmsSearchIndex)
     */
    public I_CmsIndexer newInstance(CmsObject cms, I_CmsReport report, CmsSearchIndex index) {

        CmsVfsIndexer indexer = new CmsVfsIndexer();

        indexer.m_cms = cms;
        indexer.m_report = report;
        indexer.m_index = index;

        return indexer;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#rebuildIndex(org.apache.lucene.index.IndexWriter, org.opencms.search.CmsIndexingThreadManager, org.opencms.search.CmsSearchIndexSource)
     */
    public void rebuildIndex(IndexWriter writer, CmsIndexingThreadManager threadManager, CmsSearchIndexSource source)
    throws CmsIndexException {

        List resourceNames = source.getResourcesNames();
        Iterator i = resourceNames.iterator();
        while (i.hasNext()) {
            // read the resources from all configured source folders
            String resourceName = (String)i.next();
            List resources = null;
            try {
                // read all resources (only files) below the given path
                resources = m_cms.readResources(resourceName, CmsResourceFilter.DEFAULT.addRequireFile());
            } catch (CmsException e) {
                if (m_report != null) {
                    m_report.println(Messages.get().container(
                        Messages.RPT_UNABLE_TO_READ_SOURCE_2,
                        resourceName,
                        e.getLocalizedMessage()), I_CmsReport.FORMAT_WARNING);
                }
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().key(Messages.LOG_UNABLE_TO_READ_SOURCE_2, resourceName, m_index.getName()),
                        e);
                }
            }
            if (resources != null) {
                // iterate all resources found in the folder
                Iterator j = resources.iterator();
                while (j.hasNext()) {
                    // now update all the resources individually
                    CmsResource resource = (CmsResource)j.next();
                    updateResource(writer, threadManager, resource);
                }
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#updateResources(org.apache.lucene.index.IndexWriter, org.opencms.search.CmsIndexingThreadManager, java.util.List)
     */
    public void updateResources(IndexWriter writer, CmsIndexingThreadManager threadManager, List resourcesToUpdate)
    throws CmsIndexException {

        if ((resourcesToUpdate == null) || resourcesToUpdate.isEmpty()) {
            // nothing to update
            return;
        }

        // contains all resources already updated to avoid multiple updates in case of siblings
        List resourcesAlreadyUpdated = new ArrayList(resourcesToUpdate.size());

        // index all resources that in the given list
        Iterator i = resourcesToUpdate.iterator();
        while (i.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)i.next();
            CmsResource resource = null;
            try {
                resource = m_cms.readResource(res.getRootPath());
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(
                        Messages.LOG_UNABLE_TO_READ_RESOURCE_2,
                        resource.getRootPath(),
                        m_index.getName()), e);
                }
            }
            if (resource != null) {
                if (!resourcesAlreadyUpdated.contains(resource.getRootPath())) {
                    // ensure resources are only indexed once per update
                    resourcesAlreadyUpdated.add(resource.getRootPath());
                    updateResource(writer, threadManager, resource);
                }
            }
        }
    }

    /**
     * Checks if the published resource is inside the time window set with release and expiration date.<p>
     * 
     * @param resource the published resource to check
     * @return true if the published resource is inside the time window, otherwise false
     */
    protected boolean isResourceInTimeWindow(CmsPublishedResource resource) {

        return m_cms.existsResource(
            m_cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
            CmsResourceFilter.DEFAULT);
    }

    /**
     * Updates (writes) a single resource in the index.<p>
     * 
     * @param writer the index writer to use
     * @param threadManager the thread manager to use when extracting the document text
     * @param resource the resource to update
     * 
     * @throws CmsIndexException if something goes wrong
     */
    protected void updateResource(IndexWriter writer, CmsIndexingThreadManager threadManager, CmsResource resource)
    throws CmsIndexException {

        if (resource.isInternal()) {
            // don't index internal resources
            return;
        }
        // no check for folder resources, this must be taken care of before calling this method

        try {

            if (m_report != null) {
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    String.valueOf(threadManager.getCounter() + 1)), I_CmsReport.FORMAT_NOTE);
                m_report.print(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FILE_BEGIN_0),
                    I_CmsReport.FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    m_report.removeSiteRoot(resource.getRootPath())));
                m_report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                    I_CmsReport.FORMAT_DEFAULT);
            }

            A_CmsIndexResource indexResource = new CmsVfsIndexResource(resource);
            threadManager.createIndexingThread(m_cms, writer, indexResource, m_index);

        } catch (Exception e) {

            if (m_report != null) {
                m_report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FAILED_0),
                    I_CmsReport.FORMAT_WARNING);
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(
                    Messages.ERR_INDEX_RESOURCE_FAILED_2,
                    resource.getRootPath(),
                    m_index.getName()), e);
            }
            throw new CmsIndexException(Messages.get().container(
                Messages.ERR_INDEX_RESOURCE_FAILED_2,
                resource.getRootPath(),
                m_index.getName()));
        }
    }
}