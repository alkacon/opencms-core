/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsAfterPublishStaticExportHandler.java,v $
 * Date   : $Date: 2005/02/20 18:33:03 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

/**
 * The <code>CmsAfterPublishStaticExportHandler</code> is a implementation
 * for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 * 
 * This handler exports all changes immediately after something is published.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.5 $
 * @since 6.0
 * @see I_CmsStaticExportHandler
 */
public class CmsAfterPublishStaticExportHandler implements I_CmsStaticExportHandler {

    /** Indicates if this content handler is busy. */
    protected boolean m_busy;

    /**
     * Does the actual static export.<p>
     *  
     * @param resources a list of CmsPublishedREsources to start the static export with
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file      
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public void doExportAfterPublish(List resources, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        boolean templatesFound;

        // export must be done in the context of the export user 
        CmsObject cmsExportObject = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        // first export all non-template resources,
        templatesFound = OpenCms.getStaticExportManager()
            .exportNonTemplateResources(cmsExportObject, resources, report);

        // export template resourses (check "plainoptimization" setting)
        if ((templatesFound) || (!OpenCms.getStaticExportManager().getQuickPlainExport())) {

            long timestamp = 0;
            List publishedTemplateResources;
            boolean newTemplateLinksFound;
            int linkMode = CmsStaticExportManager.C_EXPORT_LINK_WITHOUT_PARAMETER;

            do {
                // get all template resources which are potential candidates for a static export
                publishedTemplateResources = cmsExportObject.readStaticExportResources(linkMode, timestamp);
                newTemplateLinksFound = publishedTemplateResources.size() > 0;
                if (newTemplateLinksFound) {
                    if (linkMode == CmsStaticExportManager.C_EXPORT_LINK_WITHOUT_PARAMETER) {
                        // first loop, switch mode to parameter links, leave the timestamp unchanged
                        linkMode = CmsStaticExportManager.C_EXPORT_LINK_WITH_PARAMETER;
                    } else {
                        // second and subsequent loops, only look for links not already exported
                        // this can only be the case for a link with parameters 
                        // that was present on a page also generated with parameters
                        timestamp = System.currentTimeMillis();
                    }
                    OpenCms.getStaticExportManager().exportTemplateResources(publishedTemplateResources, report);
                }
                // if no new template links where found we are finished
            } while (newTemplateLinksFound);
        }
    }
    
    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#isBusy()
     */
    public boolean isBusy() {

        return m_busy;
    }
    
    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    public void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report) {

        try {
            m_busy = true;
            exportAfterPublish(publishHistoryId, report);
        } catch (Throwable t) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error during static export:", t);
            }
        } finally {
            m_busy = false;
        }
    }

    /**
     * Starts the static export on publish.<p>
     * 
     * Exports all modified resources after a publish process into the real FS.<p>
     * 
     * @param publishHistoryId the publichHistoryId of the published project
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     *  
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of erros writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    private void exportAfterPublish(CmsUUID publishHistoryId, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // first check if the test resource was published already
        // if not, we must do a complete export of all static resources
        String rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath()
            + OpenCms.getStaticExportManager().getTestResource());

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export, checking test resource " + rfsName);
        }

        File file = new File(rfsName);
        if (!file.exists()) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Test resource does not exist -> do export 'full static render'");
            }
            // the file is not there, so export everything
            OpenCms.getStaticExportManager().exportFullStaticRender(true, report);
        } else {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Test resource exists -> do static export 'after publish'");
            }

            // delete all resources deleted during the publish process
            scrubExportFolders(publishHistoryId);

            // get the list of published resources from the publish history table
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            List publishedResources = cms.readPublishedResources(publishHistoryId);

            // do the export
            doExportAfterPublish(publishedResources, report);
        }

    }

    /**
     * Scrubs all files from the export folder that might have been changed,
     * so that the export is newly created after the next request to the resource.<p>
     * 
     * @param publishHistoryId id of the last published project
     */
    private void scrubExportFolders(CmsUUID publishHistoryId) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Static export manager scrubbing export folders for project ID " + publishHistoryId);
        }

        Set scrubedFolders = new HashSet();
        Set scrubedFiles = new HashSet();
        // get a export user cms context        
        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        } catch (CmsException e) {
            // this should never happen
            OpenCms.getLog(this).error("Could not init CmsObject with default export user");
            return;
        }
        List publishedResources;
        try {
            publishedResources = cms.readPublishedResources(publishHistoryId);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this)
                    .error(
                        "Static export manager could not read list of changes resources for project ID "
                            + publishHistoryId);
            }
            return;
        }
        Iterator it = publishedResources.iterator();
        while (it.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)it.next();
            if (res.isUnChanged() || !res.isVfsResource()) {
                // unchanged resources and non vfs resources don't need to be deleted
                continue;
            }
            if (!res.isDeleted()) {
                // do not delete resources which are not 
                // marked as deleted
                continue;
            }

            List siblings = Collections.singletonList(res.getRootPath());
            if (res.getSiblingCount() > 1) {
                // ensure all siblings are scrubbed if the resource has one 
                try {
                    List li = cms.readSiblings(res.getRootPath(), CmsResourceFilter.ALL);
                    siblings = new ArrayList();
                    for (int i = 0, l = li.size(); i < l; i++) {
                        siblings.add(((CmsResource)li.get(i)).getRootPath());
                    }
                } catch (CmsException e) {
                    siblings = Collections.singletonList(res.getRootPath());
                }
            }

            for (int i = 0, l = siblings.size(); i < l; i++) {
                String vfsName = (String)siblings.get(i);
                // get the link name for the published file 
                String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug(
                        "Static export checking for deletion vfsName='" + vfsName + "' rfsName='" + rfsName + "'");
                }
                if (rfsName.startsWith(OpenCms.getStaticExportManager().getRfsPrefix())
                    && (!scrubedFiles.contains(vfsName))
                    && (!scrubedFolders.contains(CmsResource.getFolderPath(vfsName)))) {
                    scrubedFiles.add(vfsName);
                    // this file could have been exported
                    String exportFileName;
                    if (res.isFolder()) {
                        if (res.isDeleted()) {
                            String exportFolderName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager()
                                .getExportPath()
                                + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix().length() + 1));
                            try {
                                File exportFolder = new File(exportFolderName);
                                // check if export file exists, if so delete it
                                if (exportFolder.exists() && exportFolder.canWrite()) {
                                    CmsFileUtil.purgeDirectory(exportFolder);
                                    // write log message
                                    if (OpenCms.getLog(this).isInfoEnabled()) {
                                        OpenCms.getLog(this).info(
                                            "Static export deleted export folder '" + exportFolderName + "'");
                                    }
                                    scrubedFolders.add(vfsName);
                                    continue;
                                }
                            } catch (Throwable t) {
                                // ignore, nothing to do about this
                                if (OpenCms.getLog(this).isWarnEnabled()) {
                                    OpenCms.getLog(this).warn(
                                        "Error deleting static export folder vfsName='"
                                            + vfsName
                                            + "' rfsName='"
                                            + exportFolderName
                                            + "'",
                                        t);
                                }
                            }
                        }
                        // add index.html to folder name
                        rfsName += CmsStaticExportManager.C_EXPORT_DEFAULT_FILE;
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug("Static export folder index file rfsName='" + rfsName + "'");
                        }
                    }
                    exportFileName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath()
                        + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix().length() + 1));
                    try {
                        File exportFile = new File(exportFileName);
                        // check if export file exists, if so delete it
                        if (exportFile.exists() && exportFile.canWrite()) {
                            exportFile.delete();
                            // write log message
                            if (OpenCms.getLog(this).isInfoEnabled()) {
                                OpenCms.getLog(this).info("Static export deleted exported rfs file '" + rfsName + "'");
                            }
                        }
                    } catch (Throwable t) {
                        // ignore, nothing to do about this
                        if (OpenCms.getLog(this).isWarnEnabled()) {
                            OpenCms.getLog(this).warn(
                                "Error deleting static export file vfsName='"
                                    + vfsName
                                    + "' rfsName='"
                                    + exportFileName
                                    + "'",
                                t);
                        }
                    }
                }
            }
        }
    }

}