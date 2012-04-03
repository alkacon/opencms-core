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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;

/**
 * An indexer indexing {@link CmsResource} based content from the OpenCms VFS.<p>
 * 
 * @since 6.0.0 
 */
public class CmsVfsIndexer implements I_CmsIndexer {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsIndexer.class);

    // Note: The following member variables must all be "protected" (not "private") since
    // in case the indexer is extended, the factory method "newInstance()" needs to set them. 

    /** The OpenCms user context to use when reading resources from the VFS during indexing. */
    protected CmsObject m_cms;

    /** The index. */
    protected CmsSearchIndex m_index;

    /** The report. */
    protected I_CmsReport m_report;

    /**
     * @see org.opencms.search.I_CmsIndexer#deleteResources(org.opencms.search.I_CmsIndexWriter, java.util.List)
     */
    public void deleteResources(I_CmsIndexWriter indexWriter, List<CmsPublishedResource> resourcesToDelete) {

        if ((resourcesToDelete == null) || resourcesToDelete.isEmpty()) {
            // nothing to delete
            return;
        }

        // contains all resources already deleted to avoid multiple deleting in case of siblings
        List<String> resourcesAlreadyDeleted = new ArrayList<String>(resourcesToDelete.size());

        Iterator<CmsPublishedResource> i = resourcesToDelete.iterator();
        while (i.hasNext()) {
            // iterate all resources in the given list of resources to delete
            CmsPublishedResource res = i.next();
            String rootPath = res.getRootPath();
            if (!resourcesAlreadyDeleted.contains(rootPath)) {
                // ensure siblings are only deleted once per update
                resourcesAlreadyDeleted.add(rootPath);
                if (!res.isFolder()) {
                    // now delete the resource from the index
                    deleteResource(indexWriter, rootPath);
                }
            }
        }
    }

    /**
     * Returns the OpenCms user context used by this indexer.<p>
     *     
     * @return the OpenCms user context used by this indexer
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the OpenCms search index updated by this indexer.<p>
     *     
     * @return the OpenCms search index updated by this indexer 
     */
    public CmsSearchIndex getIndex() {

        return m_index;
    }

    /**
     * Returns the report used by this indexer.<p>
     *     
     * @return the report used by this indexer
     */
    public I_CmsReport getReport() {

        return m_report;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#getUpdateData(org.opencms.search.CmsSearchIndexSource, java.util.List)
     */
    public CmsSearchIndexUpdateData getUpdateData(
        CmsSearchIndexSource source,
        List<CmsPublishedResource> publishedResources) {

        // create a new update collection from this indexer and the given index source
        CmsSearchIndexUpdateData result = new CmsSearchIndexUpdateData(source, this);

        Iterator<CmsPublishedResource> i = publishedResources.iterator();
        while (i.hasNext()) {
            // check all published resources if they match this indexer / source
            CmsPublishedResource pubRes = i.next();
            // VFS resources will always have a structure id
            if (!pubRes.getStructureId().isNullUUID()) {
                // use utility method from CmsProject to check if published resource is "inside" this index source
                if (CmsProject.isInsideProject(source.getResourcesNames(), pubRes.getRootPath())) {
                    // the resource is "inside" this index source
                    addResourceToUpdateData(pubRes, result);
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
     * @see org.opencms.search.I_CmsIndexer#rebuildIndex(org.opencms.search.I_CmsIndexWriter, org.opencms.search.CmsIndexingThreadManager, org.opencms.search.CmsSearchIndexSource)
     */
    public void rebuildIndex(
        I_CmsIndexWriter writer,
        CmsIndexingThreadManager threadManager,
        CmsSearchIndexSource source) throws CmsIndexException {

        List<String> resourceNames = source.getResourcesNames();
        Iterator<String> i = resourceNames.iterator();
        while (i.hasNext()) {
            // read the resources from all configured source folders
            String resourceName = i.next();
            List<CmsResource> resources = null;
            try {
                // read all resources (only files) below the given path
                resources = m_cms.readResources(resourceName, CmsResourceFilter.IGNORE_EXPIRATION.addRequireFile());
            } catch (CmsException e) {
                if (m_report != null) {
                    m_report.println(
                        Messages.get().container(
                            Messages.RPT_UNABLE_TO_READ_SOURCE_2,
                            resourceName,
                            e.getLocalizedMessage()),
                        I_CmsReport.FORMAT_WARNING);
                }
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_UNABLE_TO_READ_SOURCE_2,
                            resourceName,
                            m_index.getName()),
                        e);
                }
            }
            if (resources != null) {
                // iterate all resources found in the folder
                Iterator<CmsResource> j = resources.iterator();
                while (j.hasNext()) {
                    // now update all the resources individually
                    CmsResource resource = j.next();
                    updateResource(writer, threadManager, resource);
                }
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#updateResources(org.opencms.search.I_CmsIndexWriter, org.opencms.search.CmsIndexingThreadManager, java.util.List)
     */
    public void updateResources(
        I_CmsIndexWriter writer,
        CmsIndexingThreadManager threadManager,
        List<CmsPublishedResource> resourcesToUpdate) throws CmsIndexException {

        if ((resourcesToUpdate == null) || resourcesToUpdate.isEmpty()) {
            // nothing to update
            return;
        }

        // contains all resources already updated to avoid multiple updates in case of siblings
        List<String> resourcesAlreadyUpdated = new ArrayList<String>(resourcesToUpdate.size());

        // index all resources that are in the given list
        Iterator<CmsPublishedResource> i = resourcesToUpdate.iterator();
        while (i.hasNext()) {
            CmsPublishedResource res = i.next();
            CmsResource resource = null;
            try {
                resource = m_cms.readResource(res.getRootPath(), CmsResourceFilter.IGNORE_EXPIRATION);
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_UNABLE_TO_READ_RESOURCE_2,
                            res.getRootPath(),
                            m_index.getName()),
                        e);
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
     * Adds a given published resource to the provided search index update data.<p>
     * 
     * This method decides if the resource has to be included in the "update" or "delete" list.<p>
     * 
     * @param pubRes the published resource to add
     * @param updateData the search index update data to add the resource to
     */
    protected void addResourceToUpdateData(CmsPublishedResource pubRes, CmsSearchIndexUpdateData updateData) {

        if (pubRes.getState().isDeleted()) {
            // deleted resource just needs to be removed
            updateData.addResourceToDelete(pubRes);
        } else if (pubRes.getState().isNew() || pubRes.getState().isChanged() || pubRes.getState().isUnchanged()) {
            updateData.addResourceToUpdate(pubRes);
        }
    }

    /**
     * Deletes a resource with the given index writer.<p>
     * 
     * @param indexWriter the index writer to resource the resource with
     * @param rootPath the root path of the resource to delete
     */
    protected void deleteResource(I_CmsIndexWriter indexWriter, String rootPath) {

        try {
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_DELETING_FROM_INDEX_1, rootPath));
            }
            // delete all documents with this term from the index
            indexWriter.deleteDocuments(rootPath);
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(Messages.LOG_IO_INDEX_DOCUMENT_DELETE_2, rootPath, m_index.getName()),
                    e);
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
    protected void updateResource(I_CmsIndexWriter writer, CmsIndexingThreadManager threadManager, CmsResource resource)
    throws CmsIndexException {

        if (resource.isFolder() || resource.isTemporaryFile()) {
            // don't ever index folders or temporary files
            return;
        }
        try {
            // create the index thread for the resource
            threadManager.createIndexingThread(this, writer, resource);
        } catch (Exception e) {

            if (m_report != null) {
                m_report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FAILED_0),
                    I_CmsReport.FORMAT_WARNING);
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        resource.getRootPath(),
                        m_index.getName()),
                    e);
            }
            throw new CmsIndexException(Messages.get().container(
                Messages.ERR_INDEX_RESOURCE_FAILED_2,
                resource.getRootPath(),
                m_index.getName()));
        }
    }

    /**
     * Updates a resource with the given index writer and the new document provided.<p>
     * 
     * @param indexWriter the index writer to update the resource with
     * @param rootPath the root path of the resource to update
     * @param doc the new document for the resource
     */
    protected void updateResource(I_CmsIndexWriter indexWriter, String rootPath, Document doc) {

        try {
            indexWriter.updateDocument(rootPath, doc);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(Messages.LOG_IO_INDEX_DOCUMENT_UPDATE_2, rootPath, m_index.getName()),
                    e);
            }
        }
    }
}