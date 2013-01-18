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

package org.opencms.synchronize;

import org.opencms.db.CmsDbIoException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

/**
 * Contains all methods to synchronize the VFS with the "real" FS.<p>
 * 
 * @since 6.0.0 
 */
public class CmsSynchronize {

    /** Flag to import a deleted resource in the VFS. */
    static final int DELETE_VFS = 3;

    /** Flag to export a resource from the VFS to the FS. */
    static final int EXPORT_VFS = 1;

    /** File name of the synclist file on the server FS. */
    static final String SYNCLIST_FILENAME = "#synclist.txt";

    /** Flag to import a resource from the FS to the VFS. */
    static final int UPDATE_VFS = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSynchronize.class);

    /** List to store all file modification interface implementations. */
    private static List<I_CmsSynchronizeModification> m_synchronizeModifications = new ArrayList<I_CmsSynchronizeModification>();

    /** The CmsObject. */
    private CmsObject m_cms;

    /** Counter for logging. */
    private int m_count;

    /** The path in the "real" file system where the resources have to be synchronized to. */
    private String m_destinationPathInRfs;

    /** Hash map for the new synchronization list of the current sync process. */
    private HashMap<String, CmsSynchronizeList> m_newSyncList;

    /** The report to write the output to. */
    private I_CmsReport m_report;

    /** Hash map for the synchronization list of the last sync process. */
    private HashMap<String, CmsSynchronizeList> m_syncList;

    /**
     * Creates a new CmsSynchronize object which automatically start the 
     * synchronization process.<p>
     *
     * @param cms the current CmsObject
     * @param settings the synchronization settings to use
     * @param report the report to write the output to
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSynchronizeException if the synchronization process cannot be started
     */
    public CmsSynchronize(CmsObject cms, CmsSynchronizeSettings settings, I_CmsReport report)
    throws CmsSynchronizeException, CmsException {

        // TODO: initialize the list of file modification interface implementations

        // do the synchronization only if the synchronization folders in the VFS
        // and the FS are valid
        if ((settings != null) && (settings.isSyncEnabled())) {

            // create an independent copy of the provided user context
            m_cms = OpenCms.initCmsObject(cms);
            // set site to root site
            m_cms.getRequestContext().setSiteRoot("/");

            m_report = report;
            m_count = 1;

            // get the destination folder
            m_destinationPathInRfs = settings.getDestinationPathInRfs();

            // check if target folder exists and is writable
            File destinationFolder = new File(m_destinationPathInRfs);
            if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
                // destination folder does not exist
                throw new CmsSynchronizeException(Messages.get().container(
                    Messages.ERR_RFS_DESTINATION_NOT_THERE_1,
                    m_destinationPathInRfs));
            }
            if (!destinationFolder.canWrite()) {
                // destination folder can't be written to
                throw new CmsSynchronizeException(Messages.get().container(
                    Messages.ERR_RFS_DESTINATION_NO_WRITE_1,
                    m_destinationPathInRfs));
            }

            // create the sync list for this run
            m_syncList = readSyncList();
            m_newSyncList = new HashMap<String, CmsSynchronizeList>();

            Iterator<String> i = settings.getSourceListInVfs().iterator();
            while (i.hasNext()) {
                // iterate all source folders
                String sourcePathInVfs = i.next();
                String destPath = m_destinationPathInRfs + sourcePathInVfs.replace('/', File.separatorChar);

                report.println(
                    org.opencms.workplace.threads.Messages.get().container(
                        org.opencms.workplace.threads.Messages.RPT_SYNCHRONIZE_FOLDERS_2,
                        sourcePathInVfs,
                        destPath),
                    I_CmsReport.FORMAT_HEADLINE);
                // synchronize the VFS and the RFS
                syncVfsToRfs(sourcePathInVfs);
            }

            // remove files from the RFS
            removeFromRfs(m_destinationPathInRfs);
            i = settings.getSourceListInVfs().iterator();

            while (i.hasNext()) {
                // add new files from the RFS
                copyFromRfs(i.next());
            }

            // write the sync list
            writeSyncList();

            // free memory
            m_syncList = null;
            m_newSyncList = null;
            m_cms = null;
        } else {
            throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_INIT_SYNC_0));
        }
    }

    /**
     * Returns the count.<p>
     *
     * @return the count
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Copys all resources from the FS which are not existing in the VFS yet. <p>
     * 
     * @param folder the folder in the VFS to be synchronized with the FS
     * @throws CmsException if something goes wrong
     */
    private void copyFromRfs(String folder) throws CmsException {

        // get the corresponding folder in the FS
        File[] res;
        File fsFile = getFileInRfs(folder);
        // first of all, test if this folder existis in the VFS. If not, create it
        try {
            m_cms.readFolder(translate(folder), CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // the folder could not be read, so create it
            String foldername = translate(folder);
            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    String.valueOf(m_count++)),
                I_CmsReport.FORMAT_NOTE);
            m_report.print(Messages.get().container(Messages.RPT_IMPORT_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                fsFile.getAbsolutePath().replace('\\', '/')));
            m_report.print(Messages.get().container(Messages.RPT_FROM_FS_TO_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                foldername));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            CmsResource newFolder = m_cms.createResource(foldername, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
            // now check if there is some external method to be called which 
            // should modify the imported resource in the VFS
            Iterator<I_CmsSynchronizeModification> i = m_synchronizeModifications.iterator();
            while (i.hasNext()) {
                try {
                    i.next().modifyVfs(m_cms, newFolder, fsFile);
                } catch (CmsSynchronizeException e1) {
                    break;
                }
            }
            // we have to read the new resource again, to get the correct timestamp
            newFolder = m_cms.readFolder(foldername, CmsResourceFilter.IGNORE_EXPIRATION);
            String resourcename = m_cms.getSitePath(newFolder);
            // add the folder to the sync list
            CmsSynchronizeList sync = new CmsSynchronizeList(
                folder,
                resourcename,
                newFolder.getDateLastModified(),
                fsFile.lastModified());
            m_newSyncList.put(resourcename, sync);

            m_report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        }
        // since the check has been done on folder basis, this must be a folder
        if (fsFile.isDirectory()) {
            // get all resources in this folder
            res = fsFile.listFiles();

            // now loop through all resources
            for (int i = 0; i < res.length; i++) {
                if(isExcluded(res[i])) continue;
                // get the relative filename
                String resname = res[i].getAbsolutePath();
                resname = resname.substring(m_destinationPathInRfs.length());
                // translate the folder separator if necessary
                resname = resname.replace(File.separatorChar, '/');
                // now check if this resource was already processed, by looking 
                // up the new sync list
                if (res[i].isFile()) {
                    if (!m_newSyncList.containsKey(translate(resname))) {
                        // this file does not exist in the VFS, so import it
                        importToVfs(res[i], resname, folder);
                    }
                } else {
                    // do a recursion if the current resource is a folder
                    copyFromRfs(resname + "/");
                }
            }
        }
    }

    /**
     * Creates a new file on the server FS.<p>
     *
     * @param newFile the file that has to be created
     * @throws CmsException if something goes wrong
     */
    private void createNewLocalFile(File newFile) throws CmsException {

        if (newFile.exists()) {
            throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_EXISTENT_FILE_1, newFile.getPath()));
        }
        FileOutputStream fOut = null;
        try {
            File parentFolder = new File(newFile.getPath().replace('/', File.separatorChar).substring(
                0,
                newFile.getPath().lastIndexOf(File.separator)));
            parentFolder.mkdirs();
            if (parentFolder.exists()) {
                fOut = new FileOutputStream(newFile);
            } else {
                throw new CmsSynchronizeException(
                    Messages.get().container(Messages.ERR_CREATE_DIR_1, newFile.getPath()));
            }
        } catch (IOException e) {
            throw new CmsSynchronizeException(Messages.get().container(
                Messages.ERR_CREATE_FILE_1,
                this.getClass().getName(),
                newFile.getPath()), e);
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Deletes a resource in the VFS and updates the synchronisation lists.<p>
     * 
     * @param res The resource to be deleted
     * 
     * @throws CmsException if something goes wrong
     */
    private void deleteFromVfs(CmsResource res) throws CmsException {

        String resourcename = m_cms.getSitePath(res);

        m_report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_1,
                String.valueOf(m_count++)),
            I_CmsReport.FORMAT_NOTE);
        if (res.isFile()) {
            m_report.print(Messages.get().container(Messages.RPT_DEL_FILE_0), I_CmsReport.FORMAT_NOTE);
        } else {
            m_report.print(Messages.get().container(Messages.RPT_DEL_FOLDER_0), I_CmsReport.FORMAT_NOTE);
        }
        m_report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_ARGUMENT_1,
            resourcename));
        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        // lock the file in the VFS, so that it can be updated
        m_cms.lockResource(resourcename);
        m_cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        // Remove it from the sync list
        m_syncList.remove(translate(resourcename));

        m_report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.FORMAT_OK);
    }

    /**
     * Exports a resource from the VFS to the FS and updates the 
     * synchronization lists.<p>
     * 
     * @param res the resource to be exported
     * 
     * @throws CmsException if something goes wrong
     */
    private void exportToRfs(CmsResource res) throws CmsException {

        CmsFile vfsFile;
        File fsFile;
        String resourcename;
        // to get the name of the file in the FS, we must look it up in the
        // sync list. This is necessary, since the VFS could use a translated
        // filename.
        CmsSynchronizeList sync = m_syncList.get(translate(m_cms.getSitePath(res)));
        // if no entry in the sync list was found, its a new resource and we 
        // can use the name of the VFS resource.
        if (sync != null) {
            resourcename = sync.getResName();
        } else {
            // otherwise use the original non-translated name
            resourcename = m_cms.getSitePath(res);

            // the parent folder could contain a translated names as well, so 
            // make a lookup in the sync list to get its original 
            // non-translated name
            String parent = CmsResource.getParentFolder(resourcename);
            CmsSynchronizeList parentSync = m_newSyncList.get(parent);
            // use the non-translated pathname
            if (parentSync != null) {
                resourcename = parentSync.getResName() + res.getName();
            }
        }
        if ((res.isFolder()) && (!resourcename.endsWith("/"))) {
            resourcename += "/";
        }
        fsFile = getFileInRfs(resourcename);

        try {
            // if the resource is marked for deletion, do not export it!
            if (!res.getState().isDeleted()) {
                // if its a file, create export the file to the FS
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_1,
                        String.valueOf(m_count++)),
                    I_CmsReport.FORMAT_NOTE);
                if (res.isFile()) {

                    m_report.print(Messages.get().container(Messages.RPT_EXPORT_FILE_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        m_cms.getSitePath(res)));
                    m_report.print(Messages.get().container(Messages.RPT_TO_FS_AS_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        fsFile.getAbsolutePath().replace('\\', '/')));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                    // create the resource if nescessary
                    if (!fsFile.exists()) {
                        createNewLocalFile(fsFile);
                    }
                    // write the file content to the FS
                    vfsFile = m_cms.readFile(m_cms.getSitePath(res), CmsResourceFilter.IGNORE_EXPIRATION);
                    try {
                        writeFileByte(vfsFile.getContents(), fsFile);
                    } catch (IOException e) {
                        throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_WRITE_FILE_0));
                    }
                    // now check if there is some external method to be called 
                    // which should modify the exported resource in the FS
                    Iterator<I_CmsSynchronizeModification> i = m_synchronizeModifications.iterator();
                    while (i.hasNext()) {
                        try {
                            i.next().modifyFs(m_cms, vfsFile, fsFile);
                        } catch (CmsSynchronizeException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(
                                    Messages.get().getBundle().key(
                                        Messages.LOG_SYNCHRONIZE_EXPORT_FAILED_1,
                                        res.getRootPath()),
                                    e);
                            }
                            break;
                        }
                    }
                    fsFile.setLastModified(res.getDateLastModified());
                } else {

                    m_report.print(Messages.get().container(Messages.RPT_EXPORT_FOLDER_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        m_cms.getSitePath(res)));
                    m_report.print(Messages.get().container(Messages.RPT_TO_FS_AS_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        fsFile.getAbsolutePath().replace('\\', '/')));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                    // its a folder, so create a folder in the FS
                    fsFile.mkdirs();
                }
                // add resource to synchronization list
                CmsSynchronizeList syncList = new CmsSynchronizeList(
                    resourcename,
                    translate(resourcename),
                    res.getDateLastModified(),
                    fsFile.lastModified());
                m_newSyncList.put(translate(resourcename), syncList);
                // and remove it fomr the old one
                m_syncList.remove(translate(resourcename));
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
            // free mem
            vfsFile = null;

        } catch (CmsException e) {
            throw new CmsSynchronizeException(e.getMessageContainer(), e);
        }
    }

    /**
     * Gets the corresponding file to a resource in the VFS. <p>
     * 
     * @param res path to the resource inside the VFS
     * @return the corresponding file in the FS
     */
    private File getFileInRfs(String res) {

        String path = m_destinationPathInRfs + res.substring(0, res.lastIndexOf("/"));
        String fileName = res.substring(res.lastIndexOf("/") + 1);
        return new File(path, fileName);
    }

    /**
     * Gets the corresponding filename of the VFS to a resource in the FS. <p>
     * 
     * @param res the resource in the FS
     * @return the corresponding filename in the VFS
     */
    private String getFilenameInVfs(File res) {

        String resname = res.getAbsolutePath();
        if (res.isDirectory()) {
            resname += "/";
        }
        // translate the folder separator if necessary
        resname = resname.replace(File.separatorChar, '/');
        return resname.substring(m_destinationPathInRfs.length());
    }

    /**
     * Imports a new resource from the FS into the VFS and updates the 
     * synchronization lists.<p>
     * 
     * @param fsFile the file in the FS
     * @param resName the name of the resource in the VFS
     * @param folder the folder to import the file into
     * @throws CmsException if something goes wrong
     */
    private void importToVfs(File fsFile, String resName, String folder) throws CmsException {

        try {
            // get the content of the FS file
            byte[] content = CmsFileUtil.readFile(fsFile);

            // create the file
            String filename = translate(fsFile.getName());

            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    String.valueOf(m_count++)),
                I_CmsReport.FORMAT_NOTE);
            if (fsFile.isFile()) {
                m_report.print(Messages.get().container(Messages.RPT_IMPORT_FILE_0), I_CmsReport.FORMAT_NOTE);
            } else {
                m_report.print(Messages.get().container(Messages.RPT_IMPORT_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            }

            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                fsFile.getAbsolutePath().replace('\\', '/')));
            m_report.print(Messages.get().container(Messages.RPT_FROM_FS_TO_0), I_CmsReport.FORMAT_NOTE);

            // get the file type of the FS file
            int resType = OpenCms.getResourceManager().getDefaultTypeForName(resName).getTypeId();
            CmsResource newFile = m_cms.createResource(
                translate(folder) + filename,
                resType,
                content,
                new ArrayList<CmsProperty>());

            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                m_cms.getSitePath(newFile)));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            // now check if there is some external method to be called which
            // should modify the imported resource in the VFS
            Iterator<I_CmsSynchronizeModification> i = m_synchronizeModifications.iterator();
            while (i.hasNext()) {
                try {
                    i.next().modifyVfs(m_cms, newFile, fsFile);
                } catch (CmsSynchronizeException e) {
                    break;
                }
            }
            // we have to read the new resource again, to get the correct timestamp
            m_cms.setDateLastModified(m_cms.getSitePath(newFile), fsFile.lastModified(), false);
            CmsResource newRes = m_cms.readResource(m_cms.getSitePath(newFile));
            // add resource to synchronization list
            CmsSynchronizeList syncList = new CmsSynchronizeList(
                resName,
                translate(resName),
                newRes.getDateLastModified(),
                fsFile.lastModified());
            m_newSyncList.put(translate(resName), syncList);

            m_report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        } catch (IOException e) {
            throw new CmsSynchronizeException(
                Messages.get().container(Messages.ERR_READING_FILE_1, fsFile.getName()),
                e);
        }
    }

    /**
     * Determine if this file is to be excluded
     * @param filename the name of the file
     * @return
     */
    private boolean isExcluded(File file){
        ArrayList<Pattern> excludes = OpenCms.getWorkplaceManager().getSynchronizeExcludePatterns();
        for(Pattern pattern : excludes){
            if(pattern.matcher(file.getName()).find()) {
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_1,
                        String.valueOf(m_count++)),
                    I_CmsReport.FORMAT_NOTE);
                m_report.print(Messages.get().container(Messages.RPT_EXCLUDING_0), I_CmsReport.FORMAT_NOTE);
                m_report.println(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    file.getAbsolutePath().replace("\\", "/")));

                return true;
            }
        }
        return false;
    }

    /**
     * Reads the synchronization list from the last sync process form the file
     * system and stores the information in a HashMap. <p>
     * 
     * Filenames are stored as keys, CmsSynchronizeList objects as values.
     * @return HashMap with synchronization information of the last sync process
     * @throws CmsException if something goes wrong
     */
    private HashMap<String, CmsSynchronizeList> readSyncList() throws CmsException {

        HashMap<String, CmsSynchronizeList> syncList = new HashMap<String, CmsSynchronizeList>();

        // the sync list file in the server fs
        File syncListFile;
        syncListFile = new File(m_destinationPathInRfs, SYNCLIST_FILENAME);
        // try to read the sync list file if it is there
        if (syncListFile.exists()) {
            // prepare the streams to write the data
            FileReader fIn = null;
            LineNumberReader lIn = null;
            try {
                fIn = new FileReader(syncListFile);
                lIn = new LineNumberReader(fIn);
                // read one line from the file
                String line = lIn.readLine();
                while (line != null) {
                    line = lIn.readLine();
                    // extract the data and create a CmsSychroizedList object
                    //  from it
                    if (line != null) {
                        StringTokenizer tok = new StringTokenizer(line, ":");
                        String resName = tok.nextToken();
                        String tranResName = tok.nextToken();
                        long modifiedVfs = new Long(tok.nextToken()).longValue();
                        long modifiedFs = new Long(tok.nextToken()).longValue();
                        CmsSynchronizeList sync = new CmsSynchronizeList(resName, tranResName, modifiedVfs, modifiedFs);
                        syncList.put(translate(resName), sync);
                    }
                }
            } catch (IOException e) {
                throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_READ_SYNC_LIST_0), e);
            } finally {
                // close all streams that were used
                try {
                    if (lIn != null) {
                        lIn.close();
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return syncList;
    }

    /**
     * Removes all resources in the RFS which are deleted in the VFS.<p>
     * 
     * @param folder the folder in the FS to check
     * @throws CmsException if something goes wrong
     */
    private void removeFromRfs(String folder) throws CmsException {

        // get the corresponding folder in the FS
        File[] res;
        File rfsFile = new File(folder);
        // get all resources in this folder
        res = rfsFile.listFiles();
        // now loop through all resources
        for (int i = 0; i < res.length; i++) {
            if(isExcluded(res[i])) continue;
            // get the corresponding name in the VFS
            String vfsFile = getFilenameInVfs(res[i]);
            // recurse if it is an directory, we must go depth first to delete 
            // files
            if (res[i].isDirectory()) {
                removeFromRfs(res[i].getAbsolutePath());
            }
            // now check if this resource is still in the old sync list.
            // if so, then it does not exist in the FS anymore andm ust be 
            // deleted
            CmsSynchronizeList sync = m_syncList.get(translate(vfsFile));

            // there is an entry, so delete the resource
            if (sync != null) {

                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_1,
                        String.valueOf(m_count++)),
                    I_CmsReport.FORMAT_NOTE);
                if (res[i].isFile()) {
                    m_report.print(Messages.get().container(Messages.RPT_DEL_FS_FILE_0), I_CmsReport.FORMAT_NOTE);
                } else {
                    m_report.print(Messages.get().container(Messages.RPT_DEL_FS_FOLDER_0), I_CmsReport.FORMAT_NOTE);
                }
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    res[i].getAbsolutePath().replace('\\', '/')));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                res[i].delete();
                m_syncList.remove(translate(vfsFile));

                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        }
    }

    /**
     * Updates the synchronization lists if a resource is not used during the
     * synchronization process.<p>
     * 
     * @param res the resource whose entry must be updated
     */
    private void skipResource(CmsResource res) {

        // add the file to the new sync list...
        String resname = m_cms.getSitePath(res);
        CmsSynchronizeList syncList = m_syncList.get(translate(resname));
        m_newSyncList.put(translate(resname), syncList);
        // .. and remove it from the old one
        m_syncList.remove(translate(resname));
        // update the report
        m_report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_1,
                String.valueOf(m_count++)),
            I_CmsReport.FORMAT_NOTE);
        m_report.print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
        m_report.println(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_ARGUMENT_1,
            resname));
    }

    /**
     * Synchronizes resources from the VFS to the RFS. <p>
     *
     * During the synchronization process, the following actions will be done:<p>
     * 
     * <ul>
     * <li>Export modified resources from the VFS to the FS</li>
     * <li>Update resources in the VFS if the corresponding resource in the FS 
     * has changed</li>
     * <li>Delete resources in the VFS if the corresponding resource in the FS
     * has been deleted</li>
     * </ul>
     * 
     * @param folder The folder in the VFS to be synchronized with the FS
     * @throws CmsException if something goes wrong
     */
    private void syncVfsToRfs(String folder) throws CmsException {

        int action = 0;
        //get all resources in the given folder
        List<CmsResource> resources = m_cms.getResourcesInFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
        // now look through all resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = resources.get(i);
            // test if the resource is marked as deleted. if so,
            // do nothing, the corresponding file in the FS will be removed later
            if (!res.getState().isDeleted()) {
                // do a recursion if the current resource is a folder
                if (res.isFolder()) {
                    // first check if this folder must be synchronized
                    action = testSyncVfs(res);
                    // do the correct action according to the test result
                    if (action == EXPORT_VFS) {
                        exportToRfs(res);
                    } else if (action != DELETE_VFS) {
                        skipResource(res);
                    }
                    // recurse into the sub folders. This must be done before 
                    // the folder might be deleted!
                    syncVfsToRfs(m_cms.getSitePath(res));
                    if (action == DELETE_VFS) {
                        deleteFromVfs(res);
                    }
                } else {
                    // if the current resource is a file, check if it has to 
                    // be synchronized
                    action = testSyncVfs(res);
                    // do the correct action according to the test result
                    switch (action) {
                        case EXPORT_VFS:
                            exportToRfs(res);
                            break;

                        case UPDATE_VFS:
                            updateFromRfs(res);
                            break;

                        case DELETE_VFS:
                            deleteFromVfs(res);
                            break;

                        default:
                            skipResource(res);

                    }
                }
                // free memory
                res = null;
            }
        }
        //  free memory
        resources = null;
    }

    /**
     * Determines the synchronization status of a VFS resource. <p>
     *  
     * @param res the VFS resource to check
     * @return integer value for the action to be done for this VFS resource
     */
    private int testSyncVfs(CmsResource res) {

        int action = 0;
        File fsFile;
        //data from sync list
        String resourcename = m_cms.getSitePath(res);

        if (m_syncList.containsKey(translate(resourcename))) {
            // this resource was already used in a previous synchronization process
            CmsSynchronizeList sync = m_syncList.get(translate(resourcename));
            // get the corresponding resource from the FS
            fsFile = getFileInRfs(sync.getResName());
            // now check what to do with this resource.
            // if the modification date is newer than the logged modification 
            // date in the sync list, this resource must be exported too
            if (res.getDateLastModified() > sync.getModifiedVfs()) {
                // now check if the resource in the FS is newer, then the 
                // resource from the FS must be imported

                // check if it has been modified since the last sync process 
                // and its newer than the resource in the VFS, only then this 
                // resource must be imported form the FS
                if ((fsFile.lastModified() > sync.getModifiedFs())
                    && (fsFile.lastModified() > res.getDateLastModified())) {
                    action = UPDATE_VFS;
                } else {

                    action = EXPORT_VFS;
                }
            } else {
                // test if the resource in the FS does not exist anymore.
                // if so, remove the resource in the VFS
                if (!fsFile.exists()) {
                    action = DELETE_VFS;
                } else {
                    // now check if the resource in the FS might have changed
                    if (fsFile.lastModified() > sync.getModifiedFs()) {
                        action = UPDATE_VFS;
                    }
                }
            }
        } else {
            // the resource name was not found in the sync list
            // this is a new resource
            action = EXPORT_VFS;
        }
        //free memory
        fsFile = null;
        return action;
    }

    /**
     * Translates the resource name.  <p>
     * 
     * This is necessary since the server RFS does allow different naming 
     * conventions than the VFS.
     * 
     * @param name the resource name to be translated
     * @return the translated resource name
     */
    private String translate(String name) {

        String translation = null;
        // test if an external translation should be used
        Iterator<I_CmsSynchronizeModification> i = m_synchronizeModifications.iterator();
        while (i.hasNext()) {
            try {
                translation = i.next().translate(m_cms, name);
            } catch (CmsSynchronizeException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_EXTERNAL_TRANSLATION_1, name), e);
                }
                break;
            }
        }
        // if there was no external method called, do the default OpenCms 
        // RFS-VFS translation
        if (translation == null) {
            translation = m_cms.getRequestContext().getFileTranslator().translateResource(name);
        }
        return translation;
    }

    /**
     * Imports a resource from the FS to the VFS and updates the
     * synchronization lists.<p>
     * 
     * @param res the resource to be exported
     * 
     * @throws CmsSynchronizeException if the resource could not be synchronized
     * @throws CmsException if something goes wrong
     */
    private void updateFromRfs(CmsResource res) throws CmsSynchronizeException, CmsException {

        CmsFile vfsFile;
        // to get the name of the file in the FS, we must look it up in the
        // sync list. This is necessary, since the VFS could use a translated
        // filename.
        String resourcename = m_cms.getSitePath(res);
        CmsSynchronizeList sync = m_syncList.get(translate(resourcename));
        File fsFile = getFileInRfs(sync.getResName());

        m_report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_1,
                String.valueOf(m_count++)),
            I_CmsReport.FORMAT_NOTE);
        m_report.print(Messages.get().container(Messages.RPT_UPDATE_FILE_0), I_CmsReport.FORMAT_NOTE);
        m_report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_ARGUMENT_1,
            resourcename));
        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        // lock the file in the VFS, so that it can be updated
        m_cms.lockResource(resourcename);
        // read the file in the VFS
        vfsFile = m_cms.readFile(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        // import the content from the FS
        try {
            vfsFile.setContents(CmsFileUtil.readFile(fsFile));
        } catch (IOException e) {
            throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_IMPORT_1, fsFile.getName()));
        }
        m_cms.writeFile(vfsFile);
        // now check if there is some external method to be called which 
        // should modify
        // the updated resource in the VFS
        Iterator<I_CmsSynchronizeModification> i = m_synchronizeModifications.iterator();
        while (i.hasNext()) {
            try {
                i.next().modifyVfs(m_cms, vfsFile, fsFile);
            } catch (CmsSynchronizeException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(Messages.LOG_SYNCHRONIZE_UPDATE_FAILED_1, res.getRootPath()),
                        e);
                }
                break;
            }
        }
        // everything is done now, so unlock the resource
        // read the resource again, necessary to get the actual timestamp
        m_cms.setDateLastModified(resourcename, fsFile.lastModified(), false);
        res = m_cms.readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);

        //add resource to synchronization list
        CmsSynchronizeList syncList = new CmsSynchronizeList(
            sync.getResName(),
            translate(resourcename),
            res.getDateLastModified(),
            fsFile.lastModified());
        m_newSyncList.put(translate(resourcename), syncList);
        // and remove it from the old one
        m_syncList.remove(translate(resourcename));
        vfsFile = null;

        m_report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.FORMAT_OK);
    }

    /**
     * This writes the byte content of a resource to the file on the server
     * file system.<p>
     *
     * @param content the content of the file in the VFS
     * @param file the file in SFS that has to be updated with content
     * 
     * @throws IOException if something goes wrong
     */
    private void writeFileByte(byte[] content, File file) throws IOException {

        FileOutputStream fOut = null;
        DataOutputStream dOut = null;
        try {
            // write the content to the file in server filesystem
            fOut = new FileOutputStream(file);
            dOut = new DataOutputStream(fOut);
            dOut.write(content);
            dOut.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                // ignore
            }
            try {
                if (dOut != null) {
                    dOut.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Writes the synchronization list of the current sync process to the 
     * server file system. <p>
     * 
     * The file can be found in the synchronization folder
     * @throws CmsException if something goes wrong
     */
    private void writeSyncList() throws CmsException {

        // the sync list file in the server file system
        File syncListFile;
        syncListFile = new File(m_destinationPathInRfs, SYNCLIST_FILENAME);

        // prepare the streams to write the data
        FileOutputStream fOut = null;
        PrintWriter pOut = null;
        try {
            fOut = new FileOutputStream(syncListFile);
            pOut = new PrintWriter(fOut);
            pOut.println(CmsSynchronizeList.getFormatDescription());

            // get all keys from the hash map and make an iterator on it
            Iterator<CmsSynchronizeList> values = m_newSyncList.values().iterator();
            // loop through all values and write them to the sync list file in 
            // a human readable format
            while (values.hasNext()) {
                CmsSynchronizeList sync = values.next();
                //fOut.write(sync.toString().getBytes());
                pOut.println(sync.toString());
            }
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_IO_WRITE_SYNCLIST_0), e);
        } finally {
            // close all streams that were used
            try {
                if (pOut != null) {
                    pOut.flush();
                    pOut.close();
                }
                if (fOut != null) {
                    fOut.flush();
                    fOut.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}