/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsSynchronize.java,v $
 * Date   : $Date: 2003/07/16 13:01:52 $
 * Version: $Revision: 1.11 $
 * Date   : $Date: 2003/07/16 13:01:52 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Contains all methods to synchronize the VFS with the "real" FS.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.11 $ $Date: 2003/07/16 13:01:52 $
 */
public class CmsSynchronize implements I_CmsConstants, I_CmsLogChannels {

    /**
     * flag to export a resource form the VFS to the FS
     */
    static final int C_EXPORT_VFS = 1;

    /**
     * flag to import a resource form the FS to the VFS
     */
    static final int C_UPDATE_VFS = 2;

    /**
     * flag to import a delete a resource in the VFS
     */
    static final int C_DELETE_VFS = 3;

    /**
     * filname of the synclist file on the server FS
     */
    static final String C_SYNCLIST_FILENAME = "#synclist.txt";

    /**
     * the path in the server FS where the resources have to be synchronized
     */
    private String m_synchronizePath = null;

    /**
     * the path in the VFS where the resources have to be synchronized
     */
    private String m_resourcePath = null;

    /**
     * the CmsObject
     */
    private CmsObject m_cms;

    /** 
     * hashmap for the synchroisation list of the last sync process
     */
    private HashMap m_syncList;

    /** 
     * hashmap for the new synchroisation list of the current sync process
     */
    private HashMap m_newSyncList;

    /** 
     * hashmap for the error listcurrent sync process
     */
    private HashMap m_errorList;

    /**
     * list to store all file modifiaction interface implementations
     */
    private static List m_synchronizeModifications;

    /**
     * Creates a new CmsSynchronize object which automatically start the 
     * synchronisation process.<p>
     *
     * @param cms the current CmsObject
     * @param resourcePath the folder of the VFS to synchronize
     * @throws CmsException if something goes wrong
     */
    public CmsSynchronize(CmsObject cms, String resourcePath) throws CmsException {
        m_cms = cms;
        // test if the list of all file modifiaction interface implementations
        // has to be generatetd. 
        // This has only made once.
        if (m_synchronizeModifications == null) {
            m_synchronizeModifications = createSyncModificationImplentations();
        }

        m_synchronizePath = m_cms.getRegistry().getSystemValue(C_SYNCHRONISATION_PATH);
        //check if the synchronize path in the FS ends with a seperator. If so, remove it
        if (m_synchronizePath.endsWith(File.separator)) {
            m_synchronizePath = m_synchronizePath.substring(0, m_synchronizePath.length() - 1);
        }

        m_resourcePath = resourcePath;
        // do the synchronization only if the synchonization folders in the VFS
        // and the FS are valid
        if ((m_resourcePath != null) && (m_synchronizePath != null)) {
            // get the sync from the previous run list
            m_syncList = readSyncList();
            // create the sync list for this run
            m_newSyncList = new HashMap();
            // create the error sync list
            m_errorList = new HashMap();
            // synchronice the VFS and the FS
            syncVfsFs(m_resourcePath);
            // remove files from the FS
            removeFromFs(m_synchronizePath);
            // add new files from the FS
            copyFromFs(m_resourcePath);
            // write the sync list
            writeSyncList();
            // free mem
            m_syncList = null;
            m_newSyncList = null;

            m_errorList = null;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error during synchronization, cannot initialize sync process.");
        }
    }

    /**
     * Synchronizes resources from the VFS to the FS. <p>
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
    private void syncVfsFs(String folder) throws CmsException {
        int action = 0;
        //get all resources in the given folder
        Vector resources = m_cms.getResourcesInFolder(folder);
        // now look through all resources in the folder
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.elementAt(i);
            // test if the resource is marked as deleted. if so,
            // do nothing, the corrsponding file in the FS will be removed later
            if (res.getState() != C_STATE_DELETED) {
                // do a recursion if the current resource is a folder
                if (res.isFolder()) {
                    // first check if this folder must be synchronised
                    action = testSyncVFS(res);
                    // do the correct action according to the test result
                    if (action == C_EXPORT_VFS) {
                        exportToFS(res);
                    } else if (action != C_DELETE_VFS) {
                        skipResource(res);
                    }
                    // recurse into the subfolders. This must be done before 
                    // the folder might be deleted!
                    syncVfsFs(m_cms.readAbsolutePath(res));
                    if (action == C_DELETE_VFS) {
                        deleteFromVfs(res);
                    }
                } else {
                    // if the current resource is a file, check if it has to 
                    // be synchronized
                    action = testSyncVFS(res);
                    // do the correct action according to the test result
                    switch (action) {
                        case C_EXPORT_VFS :
                            exportToFS(res);
                            break;

                        case C_UPDATE_VFS :
                            updateFromFs(res);
                            break;

                        case C_DELETE_VFS :
                            deleteFromVfs(res);
                            break;

                        default :
                            skipResource(res);

                    }
                }
                // free mem
                res = null;
            }
        }
        //  free mem
        resources = null;
    }

    /**
     * Removes all resources in the FS which are deleted in the VFS.<p>
     * 
     * @param folder the folder in the FS to check
     * @throws CmsException if something goes wrong
     */
    private void removeFromFs(String folder) throws CmsException {
        // get the corresponding folder in the FS
        File[] res;
        File fsFile = new File(folder);
        // get all resources in this folder
        res = fsFile.listFiles();
        // now loop through all resources
        for (int i = 0; i < res.length; i++) {
            // get the corrsponding name in the VFS
            String vfsFile = getFilenameInVfs(res[i]);
            // recurse if it is an directory, we must go depth first to delete 
            // files
            if (res[i].isDirectory()) {
                removeFromFs(res[i].getAbsolutePath());
            }
            // now check if this resource is still in the old sync list.
            // if so, then it does not exist in the FS anymore andm ust be 
            // deleted
            CmsSynchronizeList sync = (CmsSynchronizeList)m_syncList.get(translate(vfsFile));

            // there is an entry, so delete the resource
            if (sync != null) {
                res[i].delete();
                m_syncList.remove(translate(vfsFile));
            }
        }
        // free mem
        res = null;
        fsFile = null;
    }

    /**
     * Copys all resources from the FS which are not existing in the VFS yet. <p>
     * 
     * @param folder the folder in the VFS to be synchronized with the FS
     * @throws CmsException if something goes wrong
     */
    private void copyFromFs(String folder) throws CmsException {
        // get the corresponding folder in the FS
        File[] res;
        File fsFile = getFileInFs(folder);
        // first of all, test if this folder existis in the VFS. If not, create it
        try {
            m_cms.readFolder(translate(folder));
        } catch (CmsException e) {
            // the folder could not be read, so create it
            // extract the foldername
            CmsResource newFolder = m_cms.createResource(translate(folder), CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, new HashMap(), new byte[0], null);
            // now check if there is some external method to be called which 
            // should modify the imported resource in the VFS
            Iterator i = m_synchronizeModifications.iterator();
            while (i.hasNext()) {
                try {
                    ((I_CmsSynchonizeModification)i.next()).modifyVfs(m_cms, newFolder, fsFile);
                } catch (CmsSynchronizeException e1) {
                    break;
                }
            }
            m_cms.unlockResource(translate(folder), false);
            // we have to read the new resource again, to get the correct
            // timestamp.
            newFolder = m_cms.readFolder(translate(folder));
            String resourcename = m_cms.readAbsolutePath(newFolder);
            // add the folder to the sync list
            CmsSynchronizeList sync = new CmsSynchronizeList(folder, resourcename, newFolder.getDateLastModified(), fsFile.lastModified());
            m_newSyncList.put(resourcename, sync);
        }
        // since the check has been done on folder basis, this must be a folder
        if (fsFile.isDirectory()) {
            // get all resources in this folder
            res = fsFile.listFiles();

            // now loop through all resources
            for (int i = 0; i < res.length; i++) {
                // get the relative filename
                String resname = res[i].getAbsolutePath();
                resname = resname.substring(m_synchronizePath.length(), resname.length());
                // translate the folder seperator if nescessary
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
                    copyFromFs(resname + "/");
                }
            }
        }
        // free mem
        res = null;
    }

    /**
     * Determines the synchronisation status of a VFS resource. <p>
     *  
     * @param res the VFS resource to check
     * @return integer value for the action to be done for this VFS resource
     */
    private int testSyncVFS(CmsResource res) {
        int action = 0;
        File fsFile;
        //data from sync list
        String resourcename = m_cms.readAbsolutePath(res);
        if (m_syncList.containsKey(resourcename)) {
            // this resource was already used in a previous syncprocess
            CmsSynchronizeList sync = (CmsSynchronizeList)m_syncList.get(translate(resourcename));
            // get the corresponding resource from the FS
            fsFile = getFileInFs(sync.getResName());
            // now check what to do with this resource.
            // if the modification date is newer than the logged modification 
            // date in the sync list, this resource must be exported too
            if (res.getDateLastModified() > sync.getModifiedVfs()) {
                // now check if the resource in the FS is newer, then the 
                // resource from the FS must be imported

                // check if it has been modified since the last sync process 
                // and its newer than the resource in the VFS, only then this 
                // resource must be imported form the FS
                if ((fsFile.lastModified() > sync.getModifiedFs()) && (fsFile.lastModified() > res.getDateLastModified())) {
                    action = C_UPDATE_VFS;
                } else {

                    action = C_EXPORT_VFS;
                }
            } else {
                // test if the resource in the FS does not exist anymore.
                // if so, remove the resource in the VFS
                if (!fsFile.exists()) {
                    action = C_DELETE_VFS;
                } else {
                    // now check if the resource in the FS might have changed
                    if (fsFile.lastModified() > sync.getModifiedFs()) {
                        action = C_UPDATE_VFS;
                    }
                }
            }
        } else {
            // the resource name was not found in the sync list
            // this is a new resource
            action = C_EXPORT_VFS;
        }
        //free mem
        fsFile = null;
        return action;
    }

    /**
     * Updates the synchroinisation lists if a resource is not used during the
     * synchronisation process.<p>
     * 
     * @param res the resource whose entry must be updated
     * @throws CmsException if something goes wrong
     */
    private void skipResource(CmsResource res) throws CmsException {
        // add the file to the new sync list...
        String resname = m_cms.readAbsolutePath(res);
        CmsSynchronizeList syncList = (CmsSynchronizeList)m_syncList.get(translate(resname));
        m_newSyncList.put(translate(resname), syncList);
        // .. and remove it from the old one
        m_syncList.remove(translate(resname));
    }

    /**
     * Imports a new resource from the FS into the VFS and updates the 
     * synchronisation lists.<p>
     * 
     * @param fsFile the file in the FS
     * @param resName the name of the resource in the VFS
     * @param folder the folder to import the file into
     * @throws CmsException if something goes wrong
     */
    private void importToVfs(File fsFile, String resName, String folder) throws CmsException {
        try {
            // get the content of the FS file
            byte[] content = getFileBytes(fsFile);
            // get the file type of the FS file
            String type = getFileType(resName);
            // create the file
            String filename = translate(fsFile.getName());
            CmsFile newFile = (CmsFile)m_cms.createResource(translate(folder), filename, m_cms.getResourceTypeId(type), null, content);
            // now check if there is some external method to be called which
            // should modify the imported resource in the VFS
            Iterator i = m_synchronizeModifications.iterator();
            while (i.hasNext()) {
                try {
                    ((I_CmsSynchonizeModification)i.next()).modifyVfs(m_cms, newFile, fsFile);
                } catch (CmsSynchronizeException e) {
                    break;
                }
            }
            // unlock it

            m_cms.unlockResource(m_cms.readAbsolutePath(newFile), false);
            // we have to read the new resource again, to get the correct
            // timestamp.
            CmsResource newRes = m_cms.readFileHeader(m_cms.readAbsolutePath(newFile));
            // m_cms.add resource to synchronisation list
            CmsSynchronizeList syncList = new CmsSynchronizeList(resName, translate(resName), newRes.getDateLastModified(), fsFile.lastModified());
            m_newSyncList.put(translate(resName), syncList);
            // free mem  
            newFile = null;
            content = null;
        } catch (Exception e) {
            throw new CmsException(e.toString());
        }
    }

    /**
     * Exports a resource from the VFS to the FS and updates the 
     * synchronisation lists.<p>
     * 
     * @param res the resource to be exported
     * @throws CmsException if something goes wrong
     */
    private void exportToFS(CmsResource res) throws CmsException {
        CmsFile vfsFile;
        File fsFile;
        String resourcename;
        // to get the name of the file in the FS, we must look it up in the
        // sync list. This is nescessary, since the VFS could use a tranlated
        // filename.
        CmsSynchronizeList sync = (CmsSynchronizeList)m_syncList.get(translate(m_cms.readAbsolutePath(res)));
        // if no entry in the sync list was found, its a new resource and we 
        // can use the name of the VFS resource.
        if (sync != null) {
            resourcename = sync.getResName();
        } else {
            // otherwise use the original non-translated name
            resourcename = m_cms.readAbsolutePath(res);

            // the parent folder could contain a translated names as well, so 
            // make a lookup in the sync list ot get its original 
            // non-translated name
            String parent = res.getParent();
            CmsSynchronizeList parentSync = (CmsSynchronizeList)m_newSyncList.get(parent);
            // use the non-translated pathname
            if (parentSync != null) {
                resourcename = parentSync.getResName() + res.getResourceName();
            }
        }
        if ((res.isFolder()) && (!resourcename.endsWith("/"))) {
            resourcename += "/";
        }
        fsFile = getFileInFs(resourcename);

        try {
            // if the resource is marked for deletion, do not export it!
            if (res.getState() != C_STATE_DELETED) {
                // if its a file, create export the file to the FS
                if (res.isFile()) {
                    // create the resource if nescessary
                    if (!fsFile.exists()) {
                        createNewLocalFile(fsFile);
                    }
                    // write the file content to the FS
                    vfsFile = m_cms.readFile(m_cms.readAbsolutePath(res));
                    writeFileByte(vfsFile.getContents(), fsFile);
                    // now check if there is some external method to be called 
                    // which should modify the exported resource in the FS
                    Iterator i = m_synchronizeModifications.iterator();
                    while (i.hasNext()) {
                        try {
                            ((I_CmsSynchonizeModification)i.next()).modifyFs(m_cms, vfsFile, fsFile);
                        } catch (CmsSynchronizeException e) {
                            break;
                        }
                    }
                } else {
                    // its a folder, so create a folder in the FS
                    fsFile.mkdir();
                }
                // add resource to synchronisation list
                CmsSynchronizeList syncList = new CmsSynchronizeList(resourcename, translate(resourcename), res.getDateLastModified(), fsFile.lastModified());
                m_newSyncList.put(translate(resourcename), syncList);
                // and remove it fomr the old one
                m_syncList.remove(translate(resourcename));
            }
            // free mem
            vfsFile = null;

        } catch (Exception e) {
            throw new CmsException(e.getMessage());
        }
    }

    /**
     * Imports a resource from the FS to the VFS and updates the
     * synchronisation lists.<p>
     * 
     * @param res the resource to be exported
     * @throws CmsException if something goes wrong
     */
    private void updateFromFs(CmsResource res) throws CmsException {
        CmsFile vfsFile;
        // to get the name of the file in the FS, we must look it up in the
        // sync list. This is nescessary, since the VFS could use a tranlated
        // filename.
        String resourcename = m_cms.readAbsolutePath(res);
        CmsSynchronizeList sync = (CmsSynchronizeList)m_syncList.get(translate(resourcename));
        File fsFile = getFileInFs(sync.getResName());
        try {
            // lock the file in the VFS, so that it can be updated
            m_cms.lockResource(resourcename);
            // read the file in the VFS
            vfsFile = m_cms.readFile(resourcename);
            // import the content from the FS
            vfsFile.setContents(getFileBytes(fsFile));
            m_cms.writeFile(vfsFile);
            // now check if there is some external method to be called which 
            // should modify
            // the updated resource in the VFS
            Iterator i = m_synchronizeModifications.iterator();
            while (i.hasNext()) {
                try {
                    ((I_CmsSynchonizeModification)i.next()).modifyVfs(m_cms, vfsFile, fsFile);
                } catch (CmsSynchronizeException e) {
                    break;
                }
            }
            // everything is done now, so unlock the resource
            m_cms.unlockResource(resourcename, false);
            //read the resource again, nescessary to get the actual timestamps
            res = m_cms.readFileHeader(resourcename);
            //add resource to synchronisation list
            CmsSynchronizeList syncList = new CmsSynchronizeList(sync.getResName(), translate(resourcename), res.getDateLastModified(), fsFile.lastModified());
            m_newSyncList.put(translate(resourcename), syncList);
            // and remove it from the old one
            m_syncList.remove(translate(resourcename));
            vfsFile = null;
        } catch (CmsException ex) {
            // if this resource could not be locked, it could not be 
            // synchronized
            // mark this in the error list and the new sync list
            if (ex.getType() == 13) {
                // add the file to the new sync list...
                CmsSynchronizeList syncList = (CmsSynchronizeList)m_syncList.get(translate(resourcename));
                m_newSyncList.put(translate(resourcename), syncList);
                m_errorList.put(translate(resourcename), syncList);
                // and remove it from the old one
                m_syncList.remove(translate(resourcename));
            } else {
                throw ex;
            }
        } catch (Exception e) {
            throw new CmsException(e.getMessage());
        }
    }

    /**
     * Deletes a resource in the VFS and updates the synchronisation lists.<p>
     * 
     * @param res The resource to be deleted
     * @throws CmsException if something goes wrong
     */
    private void deleteFromVfs(CmsResource res) throws CmsException {

        String resourcename = m_cms.readAbsolutePath(res);

        try {
            // lock the file in the VFS, so that it can be updated
            m_cms.lockResource(resourcename);
            m_cms.deleteResource(resourcename);
            // Remove it from the sync list
            m_syncList.remove(translate(resourcename));

        } catch (CmsException ex) {
            // if this resource could not be locked, it could not be 
            // synchronized mark this in the error list and the new sync list
            if (ex.getType() == 13) {
                // add the file to the new sync list...
                CmsSynchronizeList syncList = (CmsSynchronizeList)m_syncList.get(translate(resourcename));
                m_newSyncList.put(translate(resourcename), syncList);
                m_errorList.put(translate(resourcename), syncList);
                // and remove it from the old one
                m_syncList.remove(translate(resourcename));
            } else {
                throw ex;
            }
        } catch (Exception e) {
            throw new CmsException(e.getMessage());
        }
    }

    /**
     * Reads the synchronisation list from the last sync process form the file
     * system and stores the information in a HashMap. <p>
     * 
     * Filenames are stored as keys, CmsSynchronizeList objects as values.
     * @return HashMap with synchronisation infomration of the last sync process
     * @throws CmsException if something goes wrong
     */
    private HashMap readSyncList() throws CmsException {
        HashMap syncList = new HashMap();
        String line = "";
        StringTokenizer tok;

        // check the registry if the sync process was run on this computer before.
        // if not, do NOT read the sync list form the server fielsysten, otherweise
        // all entries in the synchronization folder would be deleted.
        String syncrun = m_cms.getRegistry().getSystemValue("syncrun");
        if (syncrun != null) {
            //the sync list file in the server fs
            File syncListFile;
            syncListFile = new File(m_synchronizePath, C_SYNCLIST_FILENAME);
            // try to read the sync list file if it is there
            if (syncListFile.exists()) {
                // prepare the streams to write the data
                FileReader fIn = null;
                LineNumberReader lIn = null;
                try {
                    fIn = new FileReader(syncListFile);
                    lIn = new LineNumberReader(fIn);
                    // read one line from the file
                    line = lIn.readLine();
                    while (line != null) {
                        line = lIn.readLine();
                        // extract the data and create a CmsSychroizedList object
                        //  from it
                        if (line != null) {
                            tok = new StringTokenizer(line, ":");
                            if (tok != null) {
                                String resName = tok.nextToken();
                                String tranResName = tok.nextToken();
                                long modifiedVfs = new Long(tok.nextToken()).longValue();
                                long modifiedFs = new Long(tok.nextToken()).longValue();
                                CmsSynchronizeList sync = new CmsSynchronizeList(resName, tranResName, modifiedVfs, modifiedFs);
                                syncList.put(translate(resName), sync);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new CmsException(e.getMessage());
                } finally {
                    // close all streams that were used
                    try {
                        if (lIn != null) {
                            lIn.close();
                        }
                        if (fIn != null) {
                            fIn.close();
                        }
                    } catch (IOException e) { }
                }
            }
        }
        return syncList;
    }

    /**
     * Writes the synchronisation list of the current sync process to the 
     * server file system. <p>
     * 
     * The file can be found in the synchronization folder
     * @throws CmsException if something goes wrong
     */
    private void writeSyncList() throws CmsException {
        // the sync list file in the server fs
        File syncListFile;
        syncListFile = new File(m_synchronizePath, C_SYNCLIST_FILENAME);

        // prepare the streams to write the data
        FileOutputStream fOut = null;
        PrintWriter pOut = null;
        try {
            fOut = new FileOutputStream(syncListFile);
            pOut = new PrintWriter(fOut);
            pOut.println(CmsSynchronizeList.getFormatDescription());

            // get all keys from the hashmap and make an iterator on it
            Iterator values = m_newSyncList.values().iterator();
            // loop throush all values and write them to the sync list file in 
            // a human readable format
            while (values.hasNext()) {
                CmsSynchronizeList sync = (CmsSynchronizeList)values.next();
                //fOut.write(sync.toString().getBytes());
                pOut.println(sync.toString());
            }
        } catch (IOException e) {
            throw new CmsException(e.getMessage());
        } finally {
            // update the registry and mark that the sync process has run at least
            // one time
            m_cms.getRegistry().setSystemValue("syncrun", "true");
            // close all streams that were used
            try {
                pOut.flush();
                fOut.flush();
                if (pOut != null) {
                    pOut.close();
                }
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) { }
        }
    }

    /**
     * Creates a list of class instances implmenting the I_CmsSyncModification 
     * interface.<p>
     * 
     * Those classes are can be used to modify the resources during the sync 
     * process.
     * @return list of classes implmenting the I_CmsSyncModification interface
     */
    private List createSyncModificationImplentations() {
        List interfaceList = new ArrayList();
        // initialize 1 instance per class listed in the syncmodification node
        try {
            Hashtable syncmodificationNode = m_cms.getRegistry().getSystemValues("syncmodification");
            if (syncmodificationNode != null) {
                for (int i = 1; i <= syncmodificationNode.size(); i++) {
                    String currentClass = (String)syncmodificationNode.get("class" + i);
                    try {
                        interfaceList.add(Class.forName(currentClass).newInstance());
                        if (C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO))
                            A_OpenCms.log(C_OPENCMS_INFO, ". CmsSyncModification class init : " + currentClass + " instanciated");
                    } catch (Exception e1) {
                        if (C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO))
                            A_OpenCms.log(C_OPENCMS_INFO, ". CmsSyncModification class init : non-critical error " + e1.toString());
                    }
                }
            }
        } catch (Exception e2) {
            if (C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO))
                A_OpenCms.log(C_OPENCMS_INFO, ". CmsSyncModification  init : non-critical error " + e2.toString());
        }
        return interfaceList;
    }

    /**
     * Gets the corresponding file to a resource in the VFS. <p>
     * 
     * @param res path to the resource inside the VFS
     * @return the corresponding file in the FS
     */
    private File getFileInFs(String res) {
        String path = m_synchronizePath + res.substring(0, res.lastIndexOf("/"));
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
        // translate the folder seperator if nescessary
        resname = resname.replace(File.separatorChar, '/');
        return resname.substring(m_synchronizePath.length());
    }

    /**
     * Translates the resource name.  <p>
     * 
     * This is nescessary since the server FS does allow different naming 
     * conventions than the VFS.
     * 
     * @param name the resource name to be translated
     * @return the translated resource name
     */
    private String translate(String name) {
        String translation = null;
        // test if an external translation should be used
        Iterator i = m_synchronizeModifications.iterator();
        while (i.hasNext()) {
            try {
                translation = ((I_CmsSynchonizeModification)i.next()).translate(m_cms, name);
            } catch (CmsSynchronizeException e) {
                break;
            }
        }
        // if there was no external method called, do the default OpenCms 
        // FS-VFS translation
        if (translation == null) {
            translation = m_cms.getRequestContext().getFileTranslator().translateResource(name);
        }
        return translation;
    }

    /**
     * Creates a new file on the server FS.<p>
     *
     * @param newFile the file that has to be created
     * @throws CmsException if something goes wrong
     */
    private void createNewLocalFile(File newFile) throws CmsException {
        FileOutputStream fOut = null;
        if (newFile.exists()) {
            throw new CmsException("[" + this.getClass().getName() + "] " + newFile.getPath() + " already exists on filesystem");
        }
        try {
            File parentFolder = new File(newFile.getPath().replace('/', File.separatorChar).substring(0, newFile.getPath().lastIndexOf(File.separator)));
            parentFolder.mkdirs();
            if (parentFolder.exists()) {
                fOut = new FileOutputStream(newFile);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "]" + " Cannot create directories for " + newFile.getPath());
            }
        } catch (IOException e) {
            throw new CmsException("[" + this.getClass().getName() + "]" + " Cannot create file " + newFile.getPath() + " on filesystem", e);
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) { }
            }
        }
    }

    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * @return bytes[] the content of the file
     * @throws Exception if something goes wrong
     */
    private byte[] getFileBytes(File file) throws Exception {
        byte[] buffer = null;
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) { }
        }
    }

    /**
     * Gets the file type for the filename.<p>
     *
     * @param filename the resource to get the type
     * @return String the type for the resource
     * @throws CmsException if something goes wrong
     */
    private String getFileType(String filename) throws CmsException {
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        suffix = suffix.toLowerCase(); // file extension of filename

        // read the known file extensions from the database
        Hashtable extensions = m_cms.readFileExtensions();
        String resType = new String();
        if (extensions != null) {
            resType = (String)extensions.get(suffix);
        }
        if (resType == null) {
            resType = "plain";
        }
        return resType;
    }

    /**
     * This writes the byte content of a resource to the file on the server
     * filesystem.<p>
     *
     * @param content the content of the file in the VFS
     * @param file the file in SFS that has to be updated with content
     * @throws Exception if something goes wrong
     */
    private void writeFileByte(byte[] content, File file) throws Exception {
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
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) { }
        }
    }

}
