/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsResourceTypeFolder.java,v $
 * Date   : $Date: 2004/06/14 14:25:57 $
 * Version: $Revision: 1.18 $
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


import org.opencms.db.CmsNotImplementedException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;


/**
 * Access class for resources of the type "Folder".
 *
 * @version $Revision: 1.18 $
 */
public class CmsResourceTypeFolder implements I_CmsResourceType {
    
    /** Flag for support of old duplicate "/system/bodies/" folder. */
    public static final boolean C_BODY_MIRROR = false;

    /** The type id of this resource. */
    public static final int C_RESOURCE_TYPE_ID = 0;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "folder";

    /** Internal debug flag. */
    private static final int DEBUG = 0;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {
        // this configuration does not support parameters 
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("addConfigurationParameter(" + paramName + ", " + paramValue + ") called on " + this);
        }            
    }    

    /**
     * @see org.opencms.file.I_CmsResourceType#changeLockedInProject(org.opencms.file.CmsObject, int, java.lang.String)
     */
    public void changeLockedInProject(CmsObject cms, int newProjectId, String resourcename) throws CmsException {
        // we have to change the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles = new Vector();
        getAllResources(cms, resourcename, allSubFiles, allSubFolders);
        // first change all the files
        for (int i = 0; i < allSubFiles.size(); i++) {
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if (curFile.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                // must include files already deleted for publishing deleted resources
                cms.changeLockedInProject(newProjectId, cms.readAbsolutePath(curFile, CmsResourceFilter.ALL));
            }
        }
        // now all the subfolders
        for (int i = 0; i < allSubFolders.size(); i++) {
            CmsFolder curFolder = (CmsFolder)allSubFolders.elementAt(i);
            if (curFolder.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                changeLockedInProject(cms, newProjectId, cms.readAbsolutePath(curFolder));
            }
        }
        // finally the folder
        cms.doChangeLockedInProject(resourcename);
        if (C_BODY_MIRROR) {
            // change the corresponding folder in C_VFS_PATH_BODIES
            String bodyFolder = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + resourcename;
            try {
                cms.readFolder(bodyFolder, CmsResourceFilter.ALL);
                changeLockedInProject(cms, newProjectId, bodyFolder);
            } catch (CmsException ex) {
                // no folder is there, so do nothing
            }
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#chtype(org.opencms.file.CmsObject, java.lang.String, int)
     */
    public void chtype(CmsObject cms, String filename, int newType) throws CmsException {
        // it is not possible to change the type of a folder
        throw new CmsNotImplementedException("[" + this.getClass().getName() + "] " + filename);
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean, boolean, int)
     */
    public void copyResource(CmsObject cms, String source, String destination, boolean keepFlags, boolean lockCopy, int copyMode) throws CmsException {

        // we have to copy the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles = new Vector();
        // first valid the destination name
        validResourcename(destination);

        getAllResources(cms, source, allSubFiles, allSubFolders);
        
        if (!destination.endsWith("/")) {
            destination = destination + "/";
        }
        
        if (!destination.startsWith("/")) {
            destination = "/" + destination;
        }
        
        // first the folder
        cms.doCopyFolder(source, destination, lockCopy, keepFlags);
        
        // now the subfolders
        for (int i = 0; i < allSubFolders.size(); i++) {
            CmsFolder curFolder = (CmsFolder)allSubFolders.elementAt(i);
            if (curFolder.getState() != I_CmsConstants.C_STATE_DELETED) {
                String curDestination = destination + cms.readAbsolutePath(curFolder).substring(source.length() + 1);
                cms.doCopyFolder(cms.readAbsolutePath(curFolder), curDestination, false, keepFlags);
            }
        }
        
        // now all the little files
        for (int i = 0; i < allSubFiles.size(); i++) {
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if (curFile.getState() != I_CmsConstants.C_STATE_DELETED) {
                // both destination and readAbsolutePath have a trailing/leading slash !
                String curDest = destination + cms.readAbsolutePath(curFile).substring(source.length() + 1);
                cms.copyResource(cms.readAbsolutePath(curFile), curDest, keepFlags, false, copyMode);
            }
        }
        
        if (C_BODY_MIRROR) {
            // copy the content bodys
            try {
                copyResource(cms, I_CmsWpConstants.C_VFS_PATH_BODIES + source.substring(1), I_CmsWpConstants.C_VFS_PATH_BODIES + destination.substring(1), keepFlags, true, copyMode);
                // finaly lock the copy in content bodys if it exists.
                cms.lockResource(I_CmsWpConstants.C_VFS_PATH_BODIES + destination.substring(1));
            } catch (CmsException e) {
                // ignore
            }
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#copyResourceToProject(org.opencms.file.CmsObject, java.lang.String)
     */
    public void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException {
        // copy the folder to the current project
        cms.doCopyResourceToProject(resourceName);
        if (C_BODY_MIRROR) {
            // try to copy the corresponding folder in C_VFS_PATH_BODIES to the project
            try {
                CmsResource contentFolder = cms.readFolder(I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + resourceName, CmsResourceFilter.ALL);
                if (contentFolder != null) {
                    cms.doCopyResourceToProject(cms.readAbsolutePath(contentFolder));
                }
            } catch (CmsException e) {
                // cannot read the folder in C_VFS_PATH_BODIES so do nothing
            }
        }
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#copyToLostAndFound(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public String copyToLostAndFound(CmsObject cms, String resourcename, boolean copyResource) {
        // nothing to do here,
        return null;
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#createResource(org.opencms.file.CmsObject, java.lang.String, List, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String newFolderName, List properties, byte[] contents, Object parameter) throws CmsException {
        contents = null;
        if (!newFolderName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            newFolderName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }
        CmsFolder res = cms.doCreateFolder(newFolderName, properties);
        cms.lockResource(newFolderName);
        return res;
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, java.lang.String, int)
     */
    public void deleteResource(CmsObject cms, String folder, int deleteOption) throws CmsException {

        Vector allSubFolders = new Vector();
        Vector allSubFiles = new Vector();
        getAllResources(cms, folder, allSubFiles, allSubFolders);
        // first delete all the files
        for (int i = 0; i < allSubFiles.size(); i++) {
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if (curFile.getState() != I_CmsConstants.C_STATE_DELETED) {
                try {
                    cms.deleteResource(cms.readAbsolutePath(curFile), I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
                } catch (CmsException e) {
                    if (e.getType() != CmsException.C_RESOURCE_DELETED) {
                        throw e;
                    }
                }
            }
        }
        // now all the empty subfolders
        for (int i = allSubFolders.size() - 1; i > -1; i--) {
            CmsFolder curFolder = (CmsFolder)allSubFolders.elementAt(i);
            if (curFolder.getState() != I_CmsConstants.C_STATE_DELETED) {
                cms.doDeleteFolder(cms.readAbsolutePath(curFolder));
            }
        }

        // finally the folder
        cms.doDeleteFolder(folder);
        
        if (C_BODY_MIRROR) {
            // delete the corresponding folder in C_VFS_PATH_BODIES
            String bodyFolder = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + folder;
            try {
                cms.readFolder(bodyFolder);
                cms.deleteResource(bodyFolder, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
            } catch (CmsException ex) {
                // no folder is there, so do nothing
            }
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#exportResource(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public CmsFile exportResource(CmsObject cms, CmsFile file) {
        // nothing to do here, because there couldn´t be any Linkmanagement-Tags inside a folder-resource
        return file;
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {
        return null;
    }    
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {
        // this configuration does not support parameters
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getConfiguration() called on " + this);
        }          
        return null;
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return -1;
    }     

    /**
     * @see org.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }


    /**
     * @see org.opencms.file.I_CmsResourceType#importResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource, byte[], List, java.lang.String)
     */
    public CmsResource importResource(CmsObject cms, CmsResource resource, byte[] content, List properties, String destination) throws CmsException {
        CmsResource importedResource = null;
        CmsProperty property = null, oldProperty = null;
        int found = 0;
        
        if (!destination.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            destination += I_CmsConstants.C_FOLDER_SEPARATOR;
        }
        boolean changed = true;
        //try to create the resource
        try {
            importedResource = cms.doImportResource(resource, content, properties, destination);
            content = null;
            if (importedResource != null) {
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the folder already exists
        }
        
        if (changed) {
            changed = false;
            //the resource exists, check if properties has to be updated
            importedResource = cms.readFolder(destination);
            List oldProperties = cms.readPropertyObjects(cms.readAbsolutePath(importedResource), false);
            if (oldProperties == null) {
                oldProperties = Collections.EMPTY_LIST;
            }
            if (properties == null) {
                properties = Collections.EMPTY_LIST;
            }
            
            // find the delta between imported and existing properties
            if (properties.size() > 0) {
                if (oldProperties.size() != properties.size()) {
                    changed = true;
                } else {
                    for (int i = 0, n = properties.size(); i < n; i++) {
                        found = -1;
                        property = (CmsProperty)properties.get(i);

                        if ((found = oldProperties.indexOf(property)) == -1
                            || (oldProperty = (CmsProperty)oldProperties.get(found)) == null) {
                            changed = true;
                            break;
                        }
                        
                        if (!oldProperty.isIdentical(property)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            
            // check changes of the resourcetype
            if (importedResource.getType() != getResourceType()) {
                changed = true;
            }
            
            // update the folder if something has changed
            if (changed) {
                lockResource(cms, cms.readAbsolutePath(importedResource), true, CmsLock.C_MODE_COMMON);
                cms.doImportUpdateResource(cms.readAbsolutePath(importedResource), properties, null);
            } 
        }
        // get the updated folder
        return importedResource;
    }
        
    /**
     * @see org.opencms.file.I_CmsResourceType#initConfiguration()
     */
    public void initConfiguration() {

        // no initialization is required for folders
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {
        return false;
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#lockResource(org.opencms.file.CmsObject, java.lang.String, boolean, int)
     */
    public void lockResource(CmsObject cms, String resource, boolean force, int mode) throws CmsException {        
        if (C_BODY_MIRROR) {
            // first lock the folder in the C_VFS_PATH_BODIES path if it exists.
            try {
                cms.doLockResource(I_CmsWpConstants.C_VFS_PATH_BODIES + resource.substring(1), mode);
            } catch (CmsException e) {
                // ignore the error. this folder doesent exist.
            }
        }
        // now lock the folder
        cms.doLockResource(resource, mode);
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void moveResource(CmsObject cms, String source, String destination) throws CmsException {
        this.copyResource(cms, source, destination, true, true, I_CmsConstants.C_COPY_AS_SIBLING);
        this.deleteResource(cms, source, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);        
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#renameResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void renameResource(CmsObject cms, String oldname, String newname) throws CmsException {
        validResourcename(newname.replace('/', '\n'));

        // rename the folder itself
        // cms.doRenameResource(oldname, newname);
        this.copyResource(cms, oldname, newname, true, true, I_CmsConstants.C_COPY_AS_SIBLING);
        this.deleteResource(cms, oldname, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);

        if (C_BODY_MIRROR) {
            oldname = oldname.substring(1);
            String bodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES + oldname;

            // rename the corresponding body folder
            // cms.doRenameResource(bodyPath, newname);
            this.copyResource(cms, bodyPath, newname, true, true, I_CmsConstants.C_COPY_AS_SIBLING);
            this.deleteResource(cms, bodyPath, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, java.lang.String, List, byte[], int)
     */
    public void replaceResource(CmsObject cms, String resourceName, List resourceProperties, byte[] resourceContent, int newResType) {
        // folders cannot be replaced yet...
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#restoreResource(org.opencms.file.CmsObject, int, java.lang.String)
     */
    public void restoreResource(CmsObject cms, int versionId, String filename) throws CmsException {
        throw new CmsNotImplementedException("[" + this.getClass().getName() + "] Cannot restore folders.");
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[ResourceType]:");
        output.append(getResourceTypeName());
        output.append(", Id=");
        output.append(getResourceType());
        output.append(", LoaderId=");
        output.append(getLoaderId());
        return output.toString();
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#touch(org.opencms.file.CmsObject, java.lang.String, long, long, long, boolean, org.opencms.util.CmsUUID)
     */
    public void touch(CmsObject cms, String resourceName, long timestamp,  long releasedate, long expiredate, boolean touchRecursive, CmsUUID user) throws CmsException {
        List allFolders = new ArrayList();
        List allFiles = new ArrayList();

        // create a valid resource
        CmsFolder currentFolder = cms.readFolder(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFile currentFile;
        
        // check the access rights
        // TODO: normally, only resources with write access should be collected here
        // in order to prevent exceptions while touching
        if (!cms.hasPermissions(currentFolder, I_CmsConstants.C_WRITE_ACCESS)) {
            return;
        }        
        
        if (touchRecursive) {
            getAllResources(cms, resourceName, allFiles, allFolders);
        }  
        allFolders.add(currentFolder);
        
        // touch the folders that we collected before
        Iterator touchFolders = allFolders.iterator();
        while (touchFolders.hasNext()) {
            currentFolder = (CmsFolder)touchFolders.next();           
            if (DEBUG > 0) {
                System.err.println("touching: " + cms.readAbsolutePath(currentFolder));
            }
            // touch the folder itself
            cms.doTouch(cms.readAbsolutePath(currentFolder), timestamp, releasedate, expiredate, user);

            if (C_BODY_MIRROR) {
                // touch its counterpart under content/bodies
                String bodyFolder = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + cms.readAbsolutePath(currentFolder);
                try {
                    cms.readFolder(bodyFolder);
                    cms.doTouch(bodyFolder, timestamp, releasedate, expiredate, user);
                } catch (CmsException e) {
                    // ignore
                }
            }
        }

        // touch the files/resources that we collected before
        Iterator touchFiles = allFiles.iterator();
        while (touchFiles.hasNext()) {
            currentFile = (CmsFile)touchFiles.next();

            if (DEBUG > 0) {
                System.err.println("touching: " + cms.readAbsolutePath(currentFile));
            }
            if (currentFile.getState() != I_CmsConstants.C_STATE_DELETED) {
                // touch the file itself
                cms.touch(cms.readAbsolutePath(currentFile), timestamp, releasedate, expiredate, false);
            }
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#undeleteResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public void undeleteResource(CmsObject cms, String folder) throws CmsException {

        // we have to undelete the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles = new Vector();
        getAllResources(cms, folder, allSubFiles, allSubFolders);
        // first undelete all the files
        for (int i = 0; i < allSubFiles.size(); i++) {
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if (curFile.getState() == I_CmsConstants.C_STATE_DELETED) {
                cms.undeleteResource(cms.readAbsolutePath(curFile, CmsResourceFilter.ALL));
            }
        }
        // now all the empty subfolders
        for (int i = 0; i < allSubFolders.size(); i++) {
            CmsFolder curFolder = (CmsFolder)allSubFolders.elementAt(i);
            if (curFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                cms.doUndeleteFolder(cms.readAbsolutePath(curFolder, CmsResourceFilter.ALL));
            }
        }
        // finally the folder
        cms.doUndeleteFolder(folder);

        if (folder.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
            return;
        }
    
        if (C_BODY_MIRROR) {
            // undelete the corresponding folder in C_VFS_PATH_BODIES
            String bodyFolder = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + folder;
            try {
                cms.readFolder(bodyFolder, CmsResourceFilter.ALL);
                cms.undeleteResource(bodyFolder);
            } catch (CmsException ex) {
                // no folder is there, so do nothing
            }
        }
    }

    /**
     * @see org.opencms.file.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void undoChanges(CmsObject cms, String resource, boolean recursive) throws CmsException {
        List allFolders = new ArrayList();
        List allFiles = new ArrayList();

        // create a valid resource
        CmsFolder currentFolder = cms.readFolder(resource, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFile currentFile;
        
        // check the access rights
        // TODO: normally, only resources with write access should be collected here
        // in order to prevent exceptions while touching
        if (!cms.hasPermissions(currentFolder, I_CmsConstants.C_WRITE_ACCESS)) {
            return;
        }        
        
        if (recursive) {
            getAllResources(cms, resource, allFiles, allFolders);
        }  
        allFolders.add(currentFolder);
                
        // undo changes to the folders that we collected before
        Iterator undoFolders = allFolders.iterator();
        while (undoFolders.hasNext()) {
            currentFolder = (CmsFolder)undoFolders.next();           
            if (DEBUG > 0) {
                System.err.println("undo changes: " + cms.readAbsolutePath(currentFolder));
            }
            // touch the folder itself
            cms.doUndoChanges(cms.readAbsolutePath(currentFolder));

            if (C_BODY_MIRROR) {
                // touch its counterpart under content/bodies
                String bodyFolder = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + cms.readAbsolutePath(currentFolder);
                try {
                    cms.readFolder(bodyFolder);
                    cms.doUndoChanges(bodyFolder);
                } catch (CmsException e) {
                    // ignore
                }
            }
        }

        // undo changes to the files/resources that we collected before
        Iterator undoFiles = allFiles.iterator();
        while (undoFiles.hasNext()) {
            currentFile = (CmsFile)undoFiles.next();

            if (DEBUG > 0) {
                System.err.println("undo changes: " + cms.readAbsolutePath(currentFile));
            }
            if (currentFile.getState() != I_CmsConstants.C_STATE_DELETED) {
                // touch the file itself
                cms.doUndoChanges(cms.readAbsolutePath(currentFile));
            }
        }
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#unlockResource(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void unlockResource(CmsObject cms, String resource, boolean forceRecursive) throws CmsException {       
        if (C_BODY_MIRROR) {
            // first unlock the folder in the C_VFS_PATH_BODIES path if it exists.
            try {
                cms.doUnlockResource(I_CmsWpConstants.C_VFS_PATH_BODIES + resource.substring(1));
            } catch (CmsException e) {
                // ignore the error. this folder doesent exist.
            }
        }
        // now unlock the folder
        cms.doUnlockResource(resource);

        /*
        if (forceRecursive) {
            List allSubFolders = (List)new ArrayList();
            List allSubFiles = (List)new ArrayList();

            getAllResources(cms, resource, allSubFiles, allSubFolders);

            for (int i = 0; i < allSubFiles.size(); i++) {
                CmsFile curFile = (CmsFile)allSubFiles.get(i);
                cms.unlockResource(cms.readAbsolutePath(curFile,true), false);
            }

            for (int i = 0; i < allSubFolders.size(); i++) {
                CmsFolder curFolder = (CmsFolder)allSubFolders.get(i);
                cms.doUnlockResource(cms.readAbsolutePath(curFolder,true));
            }
        }
        */
    }

    
    /**
     * @see org.opencms.file.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsFile file) {
        // folders can never be written like a file
        throw new RuntimeException("Attempt to write a folder like as if it where a file");
    }
    
    /**
     * Checks if there are at least one character in the resourcename.<p>
     *
     * @param resourcename String to check
     * @throws CmsException if something goes wrong
     */
    protected void validResourcename(String resourcename) throws CmsException {
        if ((resourcename == null) || (resourcename.trim().length() == 0)) {
            throw new CmsException("[" + this.getClass().getName() + "] " + resourcename, CmsException.C_BAD_NAME);
        }
    }

    /**
     * Gets all resources - files and subfolders - of a given folder.<p>
     * 
     * @param cms The CmsObject.
     * @param rootFolder The name of the given folder.
     * @param allFiles Vector containing all files found so far
     * @param allFolders Vector containing all folders found so far
     * @throws CmsException if something goes wrong
     */
    private void getAllResources(CmsObject cms, String rootFolder, List allFiles, List allFolders) throws CmsException {
        List folders = new ArrayList();
        List files = new ArrayList();

        // get files and folders of this rootFolder
        folders = cms.getSubFolders(rootFolder, CmsResourceFilter.ALL);
        files = cms.getFilesInFolder(rootFolder, CmsResourceFilter.ALL);

        //copy the values into the allFiles and allFolders Vectors
        for (int i = 0; i < folders.size(); i++) {
            allFolders.add(folders.get(i));
            getAllResources(cms, cms.readAbsolutePath((CmsFolder)folders.get(i), CmsResourceFilter.ALL), allFiles, allFolders);
        }
        for (int i = 0; i < files.size(); i++) {
            allFiles.add(files.get(i));
        }
    }
}