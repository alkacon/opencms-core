/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsOnDemandStaticExportHandler.java,v $
 * Date   : $Date: 2010/09/30 10:09:14 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.sitemap.CmsInternalSitemapEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Concrete OnDemandExportHandler.<p>
 * 
 * Just the published files and folders are purged.<p>
 * 
 * @author Michael Moossen
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 * 
 * @see I_CmsStaticExportHandler
 */
public class CmsOnDemandStaticExportHandler extends A_CmsOnDemandStaticExportHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsOnDemandStaticExportHandler.class);

    /**
     * @see org.opencms.staticexport.A_CmsStaticExportHandler#getRelatedFilesToPurge(java.lang.String, java.lang.String)
     */
    @Override
    protected List<File> getRelatedFilesToPurge(String exportFileName, String vfsName) {

        List<File> filesToPurge = new ArrayList<File>();
        try {
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            addFileToPurge(cms, filesToPurge, vfsName);
            // get the related files of the given resource (vfsPath)
            List<CmsRelation> relations = cms.getRelationsForResource(vfsName, CmsRelationFilter.SOURCES);
            for (CmsRelation relation : relations) {
                // handle the relation sources
                CmsResource source = null;
                try {
                    source = relation.getSource(cms, CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // error reading a resource, should usually never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    continue;
                }
                if (source != null) {
                    // if source is container page
                    // and if a sitemap contains this page
                    // purge all the sitemap entry pages
                    if (CmsResourceTypeXmlContainerPage.isContainerPage(source)) {
                        List<CmsInternalSitemapEntry> entries = OpenCms.getSitemapManager().getEntriesForStructureId(
                            cms,
                            source.getStructureId());
                        for (CmsInternalSitemapEntry entry : entries) {
                            addFileToPurge(cms, filesToPurge, entry.getRootPath());
                        }
                    }
                    addFileToPurge(cms, filesToPurge, source.getRootPath());
                }
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return filesToPurge;
    }

    /**
     * Adds a given path to the list of rfs files that should be purged.<p>
     * 
     * @param cms the export cms object
     * @param filesToPurge the list to add the rfs file to
     * @param path the path information for getting the rfs name from
     */
    private void addFileToPurge(CmsObject cms, List<File> filesToPurge, String path) {

        String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, path);
        String exportName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(path)
            + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix(path).length()));
        File f;
        if (exportName.endsWith(File.separator)) {
            f = new File(exportName + File.separator + CmsStaticExportManager.EXPORT_DEFAULT_FILE);
        } else {
            f = new File(exportName);
        }
        if (!filesToPurge.contains(f)) {
            filesToPurge.add(f);
        }
    }
}