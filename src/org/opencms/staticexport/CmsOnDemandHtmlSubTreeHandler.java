/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsOnDemandHtmlSubTreeHandler.java,v $
 * Date   : $Date: 2005/01/07 08:47:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * The <code>CmsOnDemandStaticExportHandler</code> is the default implementation
 * for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 * 
 * This handler is most suitable for dynamic sites that use the static export 
 * as optimization for non-dynamic content.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 * @see I_CmsStaticExportHandler
 */
public class CmsOnDemandHtmlSubTreeHandler implements I_CmsStaticExportHandler {

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    public void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report) {
        
        final CmsUUID id = publishHistoryId;
        
        Thread t = new Thread(new Runnable() {

            public void run() {

                scrubExportFolders(id);        
            }
        });
        t.start();
        
    }

    /**
     * Scrubs all files from the export folder that might have been changed,
     * so that the export is newly created after the next request to the resource.<p>
     * 
     * @param publishHistoryId id of the last published project
     */
    public void scrubExportFolders(CmsUUID publishHistoryId) {
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Static export manager scrubbing export folders for project ID " + publishHistoryId);
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
                OpenCms.getLog(this).error("Static export manager could not read list of changes resources for project ID " + publishHistoryId);
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

            // ensure all siblings are scrubbed if the resource has one
            List siblings = getSiblingsList(cms, res.getRootPath());

            Iterator sibIt = siblings.iterator();
            while (sibIt.hasNext()) {
                String vfsName = (String)sibIt.next();
                
                // get the link name for the published file 
                String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Static export checking for deletion vfsName='" + vfsName + "' rfsName='" + rfsName + "'");
                }
                if (rfsName.startsWith(OpenCms.getStaticExportManager().getRfsPrefix())
                    && (!scrubedFiles.contains(rfsName))
                    && (!scrubedFolders.contains(CmsResource.getFolderPath(rfsName)))) {
                    
                    if (res.isFolder()) {
                        if (res.isDeleted()) {
                            String exportFolderName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath()
                                + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix().length() + 1));
                            try {
                                File exportFolder = new File(exportFolderName);
                                // check if export folder exists, if so delete it
                                if (exportFolder.exists() && exportFolder.canWrite()) {
                                    CmsFileUtil.purgeDirectory(exportFolder);
                                    // write log message
                                    if (OpenCms.getLog(this).isInfoEnabled()) {
                                        OpenCms.getLog(this).info("Static export deleted export folder '" + exportFolderName + "'");
                                    }
                                    scrubedFolders.add(rfsName);
                                    continue;
                                }
                            } catch (Throwable t) {
                                // ignore, nothing to do about this
                                if (OpenCms.getLog(this).isWarnEnabled()) {
                                    OpenCms.getLog(this).warn("Error deleting static export folder vfsName='" + vfsName + "' rfsName='" + exportFolderName + "'", t);
                                }
                            }
                        }
                        // add index.html to folder name
                        rfsName += CmsStaticExportManager.C_EXPORT_DEFAULT_FILE;
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug("Static export folder index file rfsName='" + rfsName + "'");
                        }
                    }
                    
                    String exportFileName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath()
                        + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix().length() + 1));
                    
                    purgeFile(exportFileName);
                    scrubedFiles.add(rfsName);
                    
                    if (!res.isFolder()) {
                        FileFilter htmlFilter = new FileFilter() {
                            
                            /**
                             * Accepts only html files
                             */
                            public boolean accept(File file) {
                                
                                return file.isFile() && (file.getName().endsWith(".html") || file.getName().endsWith(".htm"));
                            }
                            
                        };
                        
                        List fileList = CmsFileUtil.getSubtree(exportFileName, htmlFilter);
                        Iterator iter = fileList.iterator();
                        while (iter.hasNext()) {
                            File file = (File)iter.next();
                            purgeFile(file.getAbsolutePath());
                            rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getRfsPrefix() + "/" + file.getAbsolutePath().substring(OpenCms.getStaticExportManager().getExportPath().length()));
                            rfsName = CmsStringUtil.substitute(rfsName, new String(new char[] {File.separatorChar}), "/");
                            scrubedFiles.add(rfsName);
                            
                        }
                    }
                }
            }
        }
    }
    
    private void purgeFile(String exportFileName) {

        String rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getRfsPrefix() + "/" + exportFileName.substring(OpenCms.getStaticExportManager().getExportPath().length()));
        rfsName = CmsStringUtil.substitute(rfsName, new String(new char[] {File.separatorChar}), "/");
        
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
                OpenCms.getLog(this).warn("Error deleting static export file rfsName='" + rfsName + "'", t);
            }
        }
        
    }

    private List getSiblingsList(CmsObject cms, String resRootPath) {
        List siblings = new ArrayList();

        try {
            List li = cms.readSiblings(resRootPath, CmsResourceFilter.ALL);
            for (int i = 0, l = li.size(); i < l; i++) {
                String vfsName = ((CmsResource)li.get(i)).getRootPath();
                siblings.add(vfsName);
            }
        } catch (CmsException e) {
            // ignore, nothing to do about this
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Error while getting the siblings for resource vfsName='" + resRootPath + "'", e);
            }
        }
        return siblings;
    }
}
