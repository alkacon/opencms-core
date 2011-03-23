/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/A_CmsStaticExportHandler.java,v $
 * Date   : $Date: 2011/03/23 14:52:52 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Abstract base implementation for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 * 
 * This class provides several util methods to be used by static export handlers.
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.16 $ 
 * 
 * @since 6.1.7 
 * 
 * @see I_CmsStaticExportHandler
 *
 */
public abstract class A_CmsStaticExportHandler implements I_CmsStaticExportHandler {

    /**
     * Implements the file filter used to remove variants with parameters of a base file.<p>
     */
    private static class PrefixFileFilter implements FileFilter {

        /** The extension. */
        private String m_baseExtension;

        /** The base file. */
        private String m_baseName;

        /**
         * Creates a new instance of PrefixFileFilter.<p>
         * 
         * @param baseFile the base file to compare with.
         */
        public PrefixFileFilter(File baseFile) {

            String fileName = baseFile.getName();
            m_baseExtension = CmsFileUtil.getExtension(fileName);
            m_baseName = fileName + "_";
        }

        /**
         * Accepts the given file if its name starts with the name of of the base file (without extension) 
         * and ends with the extension.<p>
         * 
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {

            return f.getName().startsWith(m_baseName) && f.getName().endsWith(m_baseExtension);
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsStaticExportHandler.class);

    /** Indicates if this content handler is busy. */
    protected boolean m_busy;

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#isBusy()
     */
    public boolean isBusy() {

        return m_busy;
    }

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    public abstract void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report);

    /**
     * Scrubs all files from the export folder that might have been changed,
     * so that the export is newly created after the next request to the resource.<p>
     * 
     * @param publishHistoryId id of the last published project
     * 
     * @return the list of {@link CmsPublishedResource} objects to export
     */
    public List scrubExportFolders(CmsUUID publishHistoryId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SCRUBBING_EXPORT_FOLDERS_1, publishHistoryId));
        }

        Set scrubbedFolders = new HashSet();
        Set scrubbedFiles = new HashSet();

        // get a export user cms context        
        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        } catch (CmsException e) {
            // this should never happen
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INIT_FAILED_0), e);
            return Collections.EMPTY_LIST;
        }

        List publishedResources;
        try {
            publishedResources = cms.readPublishedResources(publishHistoryId);
        } catch (CmsException e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_READING_CHANGED_RESOURCES_FAILED_1, publishHistoryId),
                e);
            return Collections.EMPTY_LIST;
        }

        publishedResources = addMovedLinkSources(cms, publishedResources);

        // now iterate the actual resources to be exported
        Iterator itPubRes = publishedResources.iterator();
        while (itPubRes.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)itPubRes.next();
            if (res.getState().isUnchanged()) {
                // unchanged resources don't need to be deleted
                continue;
            }

            scrubResource(cms, res, scrubbedFolders, scrubbedFiles);
        }
        return publishedResources;
    }

    /**
     * Add the link sources of moved resources to the list of published resources.<p>
     * 
     * @param cms the cms context
     * @param publishedResources the published resources
     * 
     * @return the list of published resources included the link sources of moved resources 
     */
    protected List addMovedLinkSources(CmsObject cms, List publishedResources) {

        publishedResources = new ArrayList(publishedResources);
        Set pubResources = new HashSet(publishedResources.size());
        // this is needed since the CmsPublishedResource#equals(Object) method just compares ids and not paths
        // and with moved files you have 2 entries with the same id and different paths...
        Iterator itPubRes = publishedResources.iterator();
        while (itPubRes.hasNext()) {
            CmsPublishedResource pubRes = (CmsPublishedResource)itPubRes.next();
            pubResources.add(pubRes.getRootPath());
        }
        boolean modified = true;
        // until no more resources are added
        while (modified) {
            modified = false;
            Iterator itPrePubRes = new ArrayList(publishedResources).iterator();
            while (itPrePubRes.hasNext()) {
                CmsPublishedResource res = (CmsPublishedResource)itPrePubRes.next();
                if (res.getMovedState() != CmsPublishedResource.STATE_MOVED_DESTINATION) {
                    // handle only resources that are destination of move operations
                    continue;
                }
                List relations = null;
                try {
                    // get all link sources to this resource
                    relations = cms.getRelationsForResource(
                        cms.getRequestContext().removeSiteRoot(res.getRootPath()),
                        CmsRelationFilter.SOURCES);
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                if ((relations == null) || relations.isEmpty()) {
                    // continue with next resource if no link sources found
                    continue;
                }
                Iterator itRelations = relations.iterator();
                while (itRelations.hasNext()) {
                    CmsRelation relation = (CmsRelation)itRelations.next();
                    CmsPublishedResource source = null;
                    try {
                        // get the link source
                        source = new CmsPublishedResource(relation.getSource(cms, CmsResourceFilter.ALL));
                    } catch (CmsException e) {
                        // should never happen
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                    }
                    if ((source == null) || pubResources.contains(source.getRootPath())) {
                        // continue if the link source could not been retrieved or if the list already contains it
                        continue;
                    }
                    // add it, and set the modified flag to give it another round
                    modified = true;
                    pubResources.add(source.getRootPath());
                    publishedResources.add(source);
                }
            }
        }
        return publishedResources;
    }

    /**
     * Returns a list of related files to purge.<p>
     * 
     * @param exportFileName the previous exported rfs filename (already purged)
     * @param vfsName the vfs name of the resource (to be used to compute more sofisticated sets of related files to purge 
     * 
     * @return a list of related files to purge
     */
    protected abstract List getRelatedFilesToPurge(String exportFileName, String vfsName);

    /**
     * Returns a list containing the root paths of all siblings of a resource.<p> 
     * 
     * @param cms the export user context
     * @param resPath the path of the resource to get the siblings for
     * @return a list containing the root paths of all siblings of a resource
     */
    protected List getSiblingsList(CmsObject cms, String resPath) {

        List siblings = new ArrayList();
        try {
            List li = cms.readSiblings(resPath, CmsResourceFilter.ALL);
            for (int i = 0, l = li.size(); i < l; i++) {
                String vfsName = ((CmsResource)li.get(i)).getRootPath();
                siblings.add(vfsName);
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // resource not found, probably because the export user has no read permission on the resource, ignore
        } catch (CmsSecurityException e) {
            // security exception, probably because the export user has no read permission on the resource, ignore
        } catch (CmsException e) {
            // ignore, nothing to do about this
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_FETCHING_SIBLINGS_FAILED_1, resPath), e);
            }
        }
        if (!siblings.contains(resPath)) {
            // always add the resource itself, this has to be done because if the resource was
            // deleted during publishing, the sibling lookup above will produce no results
            siblings.add(resPath);
        }
        return siblings;
    }

    /**
     * Deletes the given file from the RFS if it exists,
     * also deletes all parameter variations of the file.<p>
     * 
     * @param rfsFilePath the path of the RFS file to delete
     * @param vfsName the VFS name of the file to delete (required for logging)
     */
    protected void purgeFile(String rfsFilePath, String vfsName) {

        File rfsFile = new File(rfsFilePath);

        // first delete the base file
        deleteFile(rfsFile, vfsName);

        // now delete the file parameter variations
        // get the parent folder
        File parent = rfsFile.getParentFile();
        if (parent != null) {
            // list all files in the parent folder that are variations of the base file
            File[] paramVariants = parent.listFiles(new PrefixFileFilter(rfsFile));
            if (paramVariants != null) {
                for (int v = 0; v < paramVariants.length; v++) {
                    deleteFile(paramVariants[v], vfsName);
                }
            }
        }
    }

    /**
     * Scrub a single file or folder.<p>
     * 
     * @param cms an export cms object
     * @param res the resource to check
     * @param scrubbedFolders the list of already scrubbed folders
     * @param scrubbedFiles the list of already scrubbed files
     */
    protected void scrubResource(CmsObject cms, CmsPublishedResource res, Set scrubbedFolders, Set scrubbedFiles) {

        // ensure all siblings are scrubbed if the resource has one
        String resPath = cms.getRequestContext().removeSiteRoot(res.getRootPath());
        List siblings = getSiblingsList(cms, resPath);

        Iterator itSibs = siblings.iterator();
        while (itSibs.hasNext()) {
            String vfsName = (String)itSibs.next();

            // get the link name for the published file 
            String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CHECKING_STATIC_EXPORT_2, vfsName, rfsName));
            }
            if (rfsName.startsWith(OpenCms.getStaticExportManager().getRfsPrefix(vfsName))
                && (!scrubbedFiles.contains(rfsName))
                && (!scrubbedFolders.contains(CmsResource.getFolderPath(rfsName)))) {

                if (res.isFolder()) {
                    if (res.getState().isDeleted()) {
                        String exportFolderName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
                            vfsName)
                            + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix(vfsName).length()));
                        try {
                            File exportFolder = new File(exportFolderName);
                            // check if export folder exists, if so delete it
                            if (exportFolder.exists() && exportFolder.canWrite()) {
                                CmsFileUtil.purgeDirectory(exportFolder);
                                // write log message
                                if (LOG.isInfoEnabled()) {
                                    LOG.info(Messages.get().getBundle().key(
                                        Messages.LOG_FOLDER_DELETED_1,
                                        exportFolderName));
                                }
                                scrubbedFolders.add(rfsName);
                                continue;
                            }
                        } catch (Throwable t) {
                            // ignore, nothing to do about this
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(Messages.get().getBundle().key(
                                    Messages.LOG_FOLDER_DELETION_FAILED_2,
                                    vfsName,
                                    exportFolderName));
                            }
                        }
                    }
                    // add index.html to folder name
                    rfsName += CmsStaticExportManager.EXPORT_DEFAULT_FILE;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_FOLDER_1, rfsName));
                    }

                }

                String rfsExportFileName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
                    vfsName)
                    + rfsName.substring(OpenCms.getStaticExportManager().getRfsPrefix(vfsName).length()));

                purgeFile(rfsExportFileName, vfsName);
                scrubbedFiles.add(rfsName);

                List fileList = getRelatedFilesToPurge(rfsExportFileName, vfsName);
                Iterator iter = fileList.iterator();
                while (iter.hasNext()) {
                    File file = (File)iter.next();
                    purgeFile(file.getAbsolutePath(), vfsName);
                    rfsName = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getRfsPrefix(vfsName)
                        + "/"
                        + file.getAbsolutePath().substring(
                            OpenCms.getStaticExportManager().getExportPath(vfsName).length()));
                    rfsName = CmsStringUtil.substitute(rfsName, new String(new char[] {File.separatorChar}), "/");
                    scrubbedFiles.add(rfsName);
                }
            }
        }
    }

    /**
     * Deletes the given file from the RFS, with error handling and logging.<p>
     * 
     * @param file the file to delete
     * @param vfsName the VFS name of the file (required for logging)
     */
    private void deleteFile(File file, String vfsName) {

        try {
            if (file.exists() && file.canWrite()) {
                file.delete();
                // write log message
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_FILE_DELETED_1, getRfsName(file, vfsName)));
                }
            }
        } catch (Throwable t) {
            // ignore, nothing to do about this
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(Messages.LOG_FILE_DELETION_FAILED_1, getRfsName(file, vfsName)),
                    t);
            }
        }
    }

    /**
     * Returns the export file name starting from the OpenCms webapp folder.<p>
     * 
     * @param file the file to delete
     * @param vfsName the VFS name of the file (required for logging)
     * 
     * @return the export file name starting from the OpenCms webapp folder
     */
    private String getRfsName(File file, String vfsName) {

        String filePath = file.getAbsolutePath();
        String result = CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getRfsPrefix(vfsName)
            + filePath.substring(OpenCms.getStaticExportManager().getExportPath(vfsName).length()));
        return CmsStringUtil.substitute(result, new String(new char[] {File.separatorChar}), "/");
    }
}