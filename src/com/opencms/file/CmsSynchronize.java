/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsSynchronize.java,v $
* Date   : $Date: 2003/06/25 13:52:12 $
* Version: $Revision: 1.17 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Holds the functionality to synchronize resources from the filesystem
 * into the cms and back.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.17 $ $Date: 2003/06/25 13:52:12 $
 */
public class CmsSynchronize implements I_CmsConstants{

    /**
     * flag to synchronize no filesystem,
     */
    static final int C_SYNC_NONE = 0;

    /**
     * flag to synchronize the server filesystem
     */
    static final int C_SYNC_SFS = 1;

    /**
     * flag to synchronize the virtual filesystem
     */
    static final int C_SYNC_VFS = 2;

    /**
     * flag to synchronize the server filesystem,
     * rename the old file and copy the new file from VFS
     */
    static final int C_SYNC_SFSNEW = 3;

    /**
     * the path in the server filesystem where the resource has to be synchronized
     */
    private String m_synchronizePath = null;

    /**
     * the CmsObject
     */
    private CmsObject m_cms;

    /**
     * the CmsSynchronizeList
     */
    private CmsSynchronizeList m_synchronizeList;

    /**
     * Hashtables for folders and files of the resource in the virtual filesystem
     * is needed to compare with folders and files in server filesystem
     */
    private Hashtable m_vfsFolders;
    private Hashtable m_vfsFiles;

    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param resourceName the resource to synchronize.
     * @param syncPath the path of the filesystem to synchronize.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsSynchronize(CmsObject cms, String resourceName)
        throws CmsException {
        File syncpath = null;
        m_cms = cms;
        m_synchronizePath = m_cms.getRegistry().getSystemValue(C_SYNCHRONISATION_PATH);

        if ((resourceName != null) && (m_synchronizePath != null)){
            // get the synchronisation-list
            m_synchronizeList = new CmsSynchronizeList(m_synchronizePath);
            // start the synchronisation
            m_vfsFolders = new Hashtable();
            m_vfsFiles = new Hashtable();
            // create the syncpath if necessary
            syncpath = new File(m_synchronizePath.replace('/', File.separatorChar));
            if (!syncpath.exists()){
                syncpath.mkdirs();
            }
            // first synchronize the SFS with the resources from VFS
            synchronizeServer(resourceName);
            // if the resource is a folder then synchronize the VFS with the SFS
            if (resourceName.endsWith("/")){
                synchronizeVirtual(resourceName);
            }
            // save the synchronisation-list
            //System.out.println(m_synchronizeList.toString());
            m_synchronizeList.saveSyncList();
            m_vfsFolders.clear();
            m_vfsFiles.clear();
        } else {
            throw new CmsException("["+this.getClass().getName()+"] "+"Cannot read the registry entries for synchronisation.");
        }
    }

    /**
     * starts the synchronisation of the given resource
     *
     * @param resourceName the resource to synchronize.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    private void synchronizeServer(String resourceName)
        throws CmsException{
        Vector filesInFolder = null;
        Vector foldersInFolder = null;
        CmsResource startResource = null;
        CmsFile syncFile = null;
        CmsFolder syncFolder = null;
        File startFolder = null;
        File sfsFile = null;
        int onlineProject = I_CmsConstants.C_PROJECT_ONLINE_ID;
        startResource = m_cms.readFileHeader(resourceName);
        if (startResource == null || startResource.getProjectId() == onlineProject){
            throw new CmsException("["+this.getClass().getName()+"] "+resourceName, CmsException.C_NOT_FOUND);
        }
        if (startResource.getAbsolutePath().endsWith("/")){
            // it's a folder, so all files and folders in this have to be synchronized
            // first create the start folder
            startFolder = new File((m_synchronizePath+startResource.getAbsolutePath()).replace('/', File.separatorChar));
            startFolder.mkdirs();
            // now put the resource in the hashtable for VFS resources
            m_vfsFolders.put(m_synchronizePath+startResource.getAbsolutePath(), startResource);
            // read the resources from the virtual filesystem
            // and compare each with the file in the server filesystem
            filesInFolder = m_cms.getFilesInFolder(resourceName);
            for (int i = 0; i < filesInFolder.size(); i++){
                syncFile = ((CmsFile) filesInFolder.elementAt(i));
                if ((syncFile.getProjectId() != onlineProject) && (!syncFile.getName().startsWith("~"))){
                    if (syncFile.getState() == C_STATE_DELETED) {
                        // the file has to be deleted from the server filesystem
                        sfsFile = new File(m_synchronizePath+syncFile.getAbsolutePath());
                        if (sfsFile.exists()){
                            sfsFile.delete();
                        }
                        m_synchronizeList.remove(syncFile.getAbsolutePath());
                    } else {
                        syncFile = m_cms.readFile(syncFile.getAbsolutePath());
                        synchronizeFile(syncFile);
                        // now put the resource in the hashtable for VFS resources
                        m_vfsFiles.put(m_synchronizePath+syncFile.getAbsolutePath(), syncFile);
                    }
                }
            }
            foldersInFolder = m_cms.getSubFolders(resourceName);
            for (int i = 0; i < foldersInFolder.size(); i++){
                syncFolder = ((CmsFolder) foldersInFolder.elementAt(i));
                if (syncFolder.getProjectId() != onlineProject){
                    if (syncFolder.getState() == C_STATE_DELETED) {
                        // the folder has to be deleted from the server filesystem
                        sfsFile = new File(m_synchronizePath+syncFolder.getAbsolutePath());
                        if (sfsFile.exists()){
                            if (!deleteDirectory(sfsFile)){
                                throw new CmsException("["+this.getClass().getName()+"] "+"Could not delete "+sfsFile.getPath());
                            }
                        }
                    } else {
                        sfsFile = new File((m_synchronizePath+syncFolder.getAbsolutePath()).replace('/', File.separatorChar));
                        sfsFile.mkdir();
                        // now put the resource in the hashtable for VFS resources
                        m_vfsFolders.put(m_synchronizePath+syncFolder.getAbsolutePath(), syncFolder);
                        synchronizeServer(syncFolder.getAbsolutePath());
                    }
                }
            }
        } else {
            // it's only one file that has to be synchronized
            if ((startResource.getProjectId() != onlineProject) && (!startResource.getName().startsWith("~"))){
                if (startResource.getState() == C_STATE_DELETED) {
                    // the file has to be deleted from the server filesystem
                    sfsFile = new File(m_synchronizePath+startResource.getAbsolutePath());
                    if (sfsFile.exists()){
                        sfsFile.delete();
                    }
                    m_synchronizeList.remove(startResource.getAbsolutePath());
                } else {
                    syncFile = m_cms.readFile(resourceName);
                    synchronizeFile(syncFile);
                }
            }
        }
    }

    /**
     * Read all resources from the server filesystem and compare it with the
     * virtual filesystem. Add folders and files that does not exist in VFS
     *
     * @param resourceName the resource to synchronize.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    private void synchronizeVirtual(String resourceName)
        throws CmsException{
        // read the resources from the server filesystem
        // and add the new resources to the virtual filesystem
        File sfsFile = new File(m_synchronizePath+resourceName);
        //String sfsAbsolutePath = sfsFile.getPath().substring(m_synchronizePath.length()).replace(sfsFile.separatorChar,'/');
        String sfsAbsolutePath = resourceName;
        String[] diskFiles = sfsFile.list();
        File currentFile;
        CmsFile hashFile = null;
        CmsFolder hashFolder = null;
        boolean notCreated = false;
        for (int i = 0; i < diskFiles.length; i++){
            currentFile = new File(sfsFile, diskFiles[i]);
            if (currentFile.isDirectory()){
                //hashFolder = (CmsFolder)m_vfsFolders.get(sfsFile.getPath().replace(sfsFile.separatorChar,'/')+"/"+currentFile.getName()+"/");
                hashFolder = (CmsFolder)m_vfsFolders.get(m_synchronizePath+resourceName+currentFile.getName()+"/");
                if (hashFolder == null){
                    // the folder does not exist in the VFS, so add it from SFS
                    try {
                        m_cms.createResource(sfsAbsolutePath, currentFile.getName(), C_TYPE_FOLDER_NAME);
                        m_cms.lockResource(sfsAbsolutePath+currentFile.getName()+"/");
                    } catch (CmsException e){
                        // if the folder already exists do nothing
                        if (e.getType() != CmsException.C_FILE_EXISTS &&
                            e.getType() != CmsException.C_LOCKED){
                            if (e.getType() == CmsException.C_BAD_NAME) {
                                notCreated = true;
                                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO,"["+this.getClass().getName()+"] Bad name: "+currentFile.getName());
                                }
                            }else{
                                throw e;
                            }
                        }
                    }
                }
                hashFolder = null;
                // now read the filelist of this folder
                if (!notCreated){
                    synchronizeVirtual(sfsAbsolutePath+currentFile.getName()+"/");
                }
            } else {
                hashFile = (CmsFile)m_vfsFiles.get(m_synchronizePath+resourceName+currentFile.getName());
                if (hashFile == null){
                    // the file does not exist in the VFS, so add it from SFS
                    try {
                        byte[] content = getFileBytes(currentFile);
                        String type = getFileType(currentFile.getName());
                        if ((!currentFile.getName().startsWith("$")) &&
                            (!currentFile.getName().startsWith("~")) &&
                            (!currentFile.getName().equals(CmsSynchronizeList.C_SYNCLIST_FILE))){
                            try {
                                CmsFile newFile = (CmsFile)m_cms.createResource(sfsAbsolutePath, currentFile.getName(), type, null, content);
                                m_cms.lockResource(sfsAbsolutePath+currentFile.getName());
                                m_synchronizeList.putDates(sfsAbsolutePath+currentFile.getName(), newFile.getDateLastModified(), currentFile.lastModified());
                            } catch (CmsException e) {
                                if (e.getType() != CmsException.C_FILE_EXISTS &&
                                    e.getType() != CmsException.C_LOCKED){
                                    if(e.getType() == CmsException.C_BAD_NAME){
                                        A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO,"["+this.getClass().getName()+"] Bad name: "+currentFile.getName());
                                    } else {
                                        throw e;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new CmsException("["+this.getClass().getName()+"]"+" Cannot create file in virtual filesystem",e);
                    }
                }
                hashFile = null;
            }
        }
    }

    /**
     * Synchronizes a file in the virtual or the server filesystem.
     *
     * @param vfsFile the file to synchronize.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    private void synchronizeFile(CmsFile vfsFile)
        throws CmsException{
        File sfsFile = null;
        CmsFile updVfsFile = null;
        int action = C_SYNC_NONE;

        String path = m_synchronizePath+vfsFile.getAbsolutePath().substring(0,vfsFile.getAbsolutePath().lastIndexOf("/"));
        String fileName = vfsFile.getAbsolutePath().substring(vfsFile.getAbsolutePath().lastIndexOf("/")+1);
        sfsFile = new File(path, fileName);

        if (!sfsFile.exists()){
            // if the file does not exist in SFS, it has to be created
            createNewLocalFile(sfsFile);
            sfsFile = new File(path, fileName);
            action = C_SYNC_SFS;
        } else {
            // if the file exists in SFS, compare the versions of the file
            action = compareDate(vfsFile.getAbsolutePath(), vfsFile.getDateLastModified(), sfsFile.lastModified());
        }
        switch (action){
        case 1 :
            // the file from VFS has changed, so update the SFS
            try {
                writeFileByte(vfsFile.getContents(), sfsFile);
                m_synchronizeList.putDates(vfsFile.getAbsolutePath(), vfsFile.getDateLastModified(), sfsFile.lastModified());
            } catch (Exception e){
                throw new CmsException("["+this.getClass().getName()+"]"+" Error while updating server filesystem",e);
            }
            break;
        case 2 :
            // the file from SFS has changed, so update the VFS
            try {
                byte [] content = getFileBytes(sfsFile);
                m_cms.lockResource(vfsFile.getAbsolutePath(), true);
                updVfsFile = m_cms.readFile(vfsFile.getAbsolutePath());
                updVfsFile.setContents(content);
                m_cms.writeFile(updVfsFile);
                m_synchronizeList.putDates(vfsFile.getAbsolutePath(), m_cms.readFile(updVfsFile.getAbsolutePath()).getDateLastModified(), sfsFile.lastModified());
            } catch (Exception e) {
                throw new CmsException("["+this.getClass().getName()+"]"+" Error while updating virtual filesystem",e);
            }
            break;
        case 3 :
            // both files have changed, so copy the existing file in the SFS to a backup-file
            // and update the file from VFS to SFS
            File backupFile = new File(path,"$"+fileName);
            if (copyServerFile(sfsFile, backupFile)){
                // file has copied successfully, now copy the file from VFS
                try {
                    writeFileByte(vfsFile.getContents(), sfsFile);
                    m_synchronizeList.putDates(vfsFile.getAbsolutePath(), vfsFile.getDateLastModified(), sfsFile.lastModified());
                } catch (Exception e) {
                    throw new CmsException("["+this.getClass().getName()+"]"+" Error while updating server filesystem",e);
                }
            } else {
                // throw an exception if the file could not be renamed
                throw new CmsException("["+this.getClass().getName()+"]"+" The file "+sfsFile.getName()+" could not be copied");
            }
            break;
        default :
            // do nothing
            break;
        }
    }

    /**
     * Compares the date of the resource in the virtual and the server filesystem
     * with the date in the synchronize list
     *
     * @param resourceName the resource to compare.
     * @param vfsDate the date of the resource in the virtual filesystem.
     * @param sfsDate the date of the resource in the server filesystem.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     * @return int the number of the action for synchronizeFile
     */
    private int compareDate(String resourceName, long vfsDate, long sfsDate)
        throws CmsException{
        long syncVfsDate = m_synchronizeList.getVfsDate(resourceName);
        long syncSfsDate = m_synchronizeList.getSfsDate(resourceName);

        if (syncSfsDate == 0){
            return C_SYNC_SFS;
        }

        if (vfsDate > syncVfsDate){
            if (sfsDate > syncSfsDate){
                return C_SYNC_SFSNEW;
            } else {
                return C_SYNC_SFS;
            }
        } else {
            if (sfsDate > syncSfsDate){
                return C_SYNC_VFS;
            } else {
                return C_SYNC_NONE;
            }
        }
    }

    /**
     * Creates the new file on the SFS
     *
     * @param newFile the file that has to be created.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    private void createNewLocalFile(File newFile) throws CmsException {
        FileOutputStream fOut = null;
        if (newFile.exists()){
            throw new CmsException("["+this.getClass().getName()+"] "+newFile.getPath()+" already exists on filesystem");
        }
        try {
            File parentFolder = new File(newFile.getPath().replace('/', File.separatorChar).substring(0,newFile.getPath().lastIndexOf(File.separator)));
            parentFolder.mkdirs();
            if (parentFolder.exists()){
                fOut = new FileOutputStream(newFile);
            } else {
                throw new CmsException("["+this.getClass().getName()+"]"+" Cannot create directories for "+newFile.getPath());
            }
        } catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+" Cannot create file "+newFile.getPath()+" on filesystem", e);
        } finally {
            if (fOut != null){
                try {
                    fOut.close();
                } catch (IOException e){
                }
            }
        }
    }

    /**
     * this copies a file on the server filesystem
     *
     * @param fromFile the file that has to be copied.
     * @param toFile the copy of the file fromFile.
     * @return boolean if the file could be copied
     */
    private boolean copyServerFile(File fromFile, File toFile){
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        try {
            fIn = new FileInputStream(fromFile);
            fOut = new FileOutputStream(toFile);
            synchronized (fIn){
                synchronized (fOut){
                    byte[] buffer = new byte[256];
                    while (true){
                        int bytesRead = fIn.read(buffer);
                        if (bytesRead == -1){
                            break;
                        }
                        fOut.write(buffer, 0, bytesRead);
                    }
                }
            }
            return true;
        } catch (IOException e){
            return false;
        } finally {
            try {
                fIn.close();
            } catch (IOException e){
            }
            try {
                fOut.close();
            } catch (IOException e){
            }
        }
    }

    /**
     * Deletes the folder and all subresources from SFS
     * The folder has to be empty before it can be deleted
     *
     * @param delFolder the folder that has to be deleted.
     * @return boolean is true if all directories could be deleted
     */
    private boolean deleteDirectory (File delFolder){
        String[] diskFiles = delFolder.list();
        File currentFile;
        String resourceName;
        for (int i = 0; i < diskFiles.length; i++){
            currentFile = new File(delFolder, diskFiles[i]);
            if (currentFile.isDirectory()){
                deleteDirectory(currentFile);
                if (!currentFile.delete()){
                    return false;
                }
                resourceName = currentFile.getParent().substring(m_synchronizePath.length())+currentFile.getName()+File.separatorChar;
            } else {
                if (!currentFile.delete()){
                    return false;
                }
                resourceName = currentFile.getParent().substring(m_synchronizePath.length(), currentFile.getParent().lastIndexOf(File.separatorChar)+1)+currentFile.getName();
                // if a file is deleted it has to be removed from the synchronize list
                m_synchronizeList.remove(resourceName);
            }
        }
        return delFolder.delete();
    }

    /**
     * Returns a byte array containing the content of the file.
     *
     * @param filename The name of the file to read.
     * @return bytes[] The content of the file.
     */
    private byte[] getFileBytes(File file)
        throws Exception{
        byte[] buffer = null;
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while(charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Gets the file type for the filename.
     *
     * @param filename the resource to get the type.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     * @return String the type for the resource
     */
    private String getFileType(String filename)
        throws CmsException {
        String suffix = filename.substring(filename.lastIndexOf('.')+1);
        suffix = suffix.toLowerCase(); // file extension of filename

        // read the known file extensions from the database
        Hashtable extensions = m_cms.readFileExtensions();
        String resType = new String();
        if (extensions != null) {
            resType = (String) extensions.get(suffix);
        }
        if (resType == null) {
            resType = "plain";
        }
        return resType;
    }

    /**
     * This writes the byte content of a resource to the file on the server filesystem
     *
     * @param content the content of the file in the VFS.
     * @param file the file in SFS that has to be updated with content.
     * @throws Exception the Exception is thrown if something goes wrong.
     */
    private void writeFileByte(byte[] content, File file)
        throws Exception {
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
            } catch (IOException e) {
            }
        }
    }
}
