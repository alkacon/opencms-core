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

package org.opencms.search;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.CmsDocumentDependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A VFS indexer that resolves locale dependent documents.<p>
 *
 * @since 8.5.0
 */
public class CmsDependencyIndexer extends CmsVfsIndexer {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDependencyIndexer.class);

    /**
     * @see org.opencms.search.CmsVfsIndexer#getUpdateData(org.opencms.search.CmsSearchIndexSource, java.util.List)
     */
    @Override
    public CmsSearchIndexUpdateData getUpdateData(
        CmsSearchIndexSource source,
        List<CmsPublishedResource> publishedResources) {

        try {
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
                        CmsPublishedResource pub = pubRes;
                        // grab the dependencies of this resource
                        CmsDocumentDependency dep = CmsDocumentDependency.load(m_cms, pub);
                        List<CmsDocumentDependency> depsAvailable = dep.getDependencies();
                        Iterator<CmsDocumentDependency> depIt = depsAvailable.iterator();
                        do {
                            // add the document to the search index update data
                            addResourceToUpdateData(pub, result);
                            if (!pub.getState().isDeleted()) {
                                // don't need to store dependency info for deleted resources
                                dep.storeInContext(m_cms);
                            }
                            if (depIt.hasNext()) {
                                // add all dependent documents that are not already included in the original publish list
                                pub = depIt.next().getResource();
                            } else {
                                dep = null;
                            }
                        } while (dep != null);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * @see org.opencms.search.CmsVfsIndexer#isLocaleDependenciesEnable()
     */
    @Override
    public boolean isLocaleDependenciesEnable() {

        return true;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#rebuildIndex(org.opencms.search.I_CmsIndexWriter, org.opencms.search.CmsIndexingThreadManager, org.opencms.search.CmsSearchIndexSource)
     */
    @Override
    public void rebuildIndex(
        I_CmsIndexWriter writer,
        CmsIndexingThreadManager threadManager,
        CmsSearchIndexSource source) {

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
                Map<String, List<CmsResource>> folderLookupMap = createFolderLookupMap(resources);
                // iterate all resources found in the folder
                for (CmsResource resource : resources) {
                    List<CmsResource> folderContent = folderLookupMap.get(
                        CmsResource.getFolderPath(resource.getRootPath()));
                    CmsDocumentDependency dep = CmsDocumentDependency.load(m_cms, resource, folderContent);
                    dep.storeInContext(m_cms);
                    // now update all the resources individually
                    updateResource(writer, threadManager, resource);
                }
            }
        }
    }

    /**
     * Creates a folder based lookup map for the given resource list.<p>
     *
     * @param resources the list of resource to build the lookup map for
     *
     * @return a folder based lookup map for the given resource list
     */
    protected Map<String, List<CmsResource>> createFolderLookupMap(List<CmsResource> resources) {

        Map<String, List<CmsResource>> result = new HashMap<String, List<CmsResource>>(128);
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
            String folderPath = CmsResource.getFolderPath(res.getRootPath());
            List<CmsResource> folderContent = result.get(folderPath);
            if (folderContent == null) {
                folderContent = new ArrayList<CmsResource>(32);
                result.put(folderPath, folderContent);
            }
            folderContent.add(res);
        }

        return result;
    }
}
