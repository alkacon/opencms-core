/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeFolder.java,v $
* Date   : $Date: 2003/06/12 09:39:08 $
* Version: $Revision: 1.48 $
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Access class for resources of the type "Folder".
 *
 * @version $Revision: 1.48 $
 */
public class CmsResourceTypeFolder implements I_CmsResourceType, I_CmsConstants, Serializable, com.opencms.workplace.I_CmsWpConstants {

     /**
      * The id of resource type.
      */
    private int m_resourceType;

    /**
     * The id of the launcher used by this resource.
     */
    private int m_launcherType;

    /**
     * The resource type name.
     */
    private String m_resourceTypeName;

    /**
     * The class name of the Java class launched by the launcher.
     */
    private String m_launcherClass;
    
    /** Internal debug flag */
    private static final int DEBUG = 0;


    /**
     * init a new CmsResourceType object.
     *
     * @param resourceType The id of the resource type.
     * @param launcherType The id of the required launcher.
     * @param resourceTypeName The printable name of the resource type.
     * @param launcherClass The Java class that should be invoked by the launcher.
     * This value is <b> null </b> if the default invokation class should be used.
     */
    public void init(int resourceType, int launcherType,
                           String resourceTypeName, String launcherClass){

        m_resourceType=resourceType;
        m_launcherType=launcherType;
        m_resourceTypeName=resourceTypeName;
        m_launcherClass=launcherClass;
    }
     /**
     * Returns the name of the Java class loaded by the launcher.
     * This method returns <b>null</b> if the default class for this type is used.
     *
     * @return the name of the Java class.
     */
     public String getLauncherClass() {
         if ((m_launcherClass == null) || (m_launcherClass.length()<1)) {
            return C_UNKNOWN_LAUNCHER;
         } else {
            return m_launcherClass;
         }
     }
     /**
     * Returns the launcher type needed for this resource-type.
     *
     * @return the launcher type for this resource-type.
     */
     public int getLauncherType() {
         return m_launcherType;
     }
    /**
     * Returns the name for this resource-type.
     *
     * @return the name for this resource-type.
     */
     public String getResourceTypeName() {
         return m_resourceTypeName;
     }
    /**
     * Returns the type of this resource-type.
     *
     * @return the type of this resource-type.
     */
    public int getResourceType() {
         return m_resourceType;
     }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
     public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[ResourceType]:");
        output.append(m_resourceTypeName);
        output.append(" , Id=");
        output.append(m_resourceType);
        output.append(" , launcherType=");
        output.append(m_launcherType);
        output.append(" , launcherClass=");
        output.append(m_launcherClass);
        return output.toString();
      }

    /**
    * Changes the group of a resource.
    * <br>
    * Only the group of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newGroup the name of the new group for this resource.
    * @param chRekursive shows if the subResources (of a folder) should be changed too.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chgrp(CmsObject cms, String filename, String newGroup, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
                || (cms.userInGroup(cms.getRequestContext().currentUser().getName(),
                C_GROUP_ADMIN))) {
            cms.doChgrp(filename, newGroup);
            // now change the bodyfolder if exists
            String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + folder.getAbsolutePath();
            try {
                cms.readFolder(bodyFolder);
                cms.doChgrp(bodyFolder, newGroup);
            }
            catch(CmsException ex) {
            // no folder is there, so do nothing
            }
            // the rekursive flag was set do a recursive chown on all files and subfolders
            if(chRekursive) {
                // get all subfolders and files
                Vector allFolders = new Vector();
                Vector allFiles = new Vector();
                getAllResources(cms, filename, allFiles, allFolders);
                // now modify all subfolders
                for(int i = 0;i < allFolders.size();i++) {
                    CmsFolder curfolder = (CmsFolder)allFolders.elementAt(i);
                    if(curfolder.getState() != C_STATE_DELETED) {
                        cms.doChgrp(curfolder.getAbsolutePath(), newGroup);
                        // check if there is a corresponding directory in the content body folder
                        bodyFolder = C_VFS_PATH_BODIES.substring(0,
                                C_VFS_PATH_BODIES.lastIndexOf("/")) + curfolder.getAbsolutePath();
                        try {
                            cms.readFolder(bodyFolder);
                            cms.doChgrp(bodyFolder, newGroup);
                        }
                        catch(CmsException ex) {
                        // no folder is there, so do nothing
                        }
                    }
                }
                // now modify all files in the subfolders
                for(int i = 0;i < allFiles.size();i++) {
                    CmsFile newfile = (CmsFile)allFiles.elementAt(i);
                    if(newfile.getState() != C_STATE_DELETED) {
                        try{
                            cms.chgrp(newfile.getAbsolutePath(), newGroup);
                        } catch (CmsException e){
                            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] "+newfile.getAbsolutePath()+": "+e.getStackTraceAsString());
                            }
                        }
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
        }
*/
        }

    /**
    * Changes the flags of a resource.
    * <br>
    * Only the flags of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change the flags, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user can write the resource</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param flags the new flags for the resource.
    * @param chRekursive shows if the subResources (of a folder) should be changed too.
    *
    * @throws CmsException if operation was not successful.
    * for this resource.
    */
    public void chmod(CmsObject cms, String filename, int flags, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
            || (cms.userInGroup(cms.getRequestContext().currentUser().getName(),
            C_GROUP_ADMIN))) {
             // modify the access flags
            cms.doChmod(filename, flags);
            // now change the bodyfolder if exists
            String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + folder.getAbsolutePath();
            try {
                cms.readFolder(bodyFolder);
                cms.doChmod(bodyFolder, flags);
            }
            catch(CmsException ex) {
            // no folder is there, so do nothing
            }
            // the rekursive flag was set do a recursive chmod on all files and subfolders
            if(chRekursive) {
                // get all subfolders and files
                Vector allFolders = new Vector();
                Vector allFiles = new Vector();
                getAllResources(cms, filename, allFiles, allFolders);
                // now modify all subfolders
                for(int i = 0;i < allFolders.size();i++) {
                    CmsFolder curfolder = (CmsFolder)allFolders.elementAt(i);
                    if(curfolder.getState() != C_STATE_DELETED) {
                        cms.doChmod(curfolder.getAbsolutePath(), flags);
                        // check if there is a corresponding directory in the content body folder
                        bodyFolder = C_VFS_PATH_BODIES.substring(0,
                                C_VFS_PATH_BODIES.lastIndexOf("/")) + curfolder.getAbsolutePath();
                        try {
                            cms.readFolder(bodyFolder);
                            cms.doChmod(bodyFolder, flags);
                        }catch(CmsException ex) {
                        // no folder is there, so do nothing
                        }
                    }
                }
                // now modify all files in the subfolders
                for(int i = 0;i < allFiles.size();i++) {
                    CmsFile newfile = (CmsFile)allFiles.elementAt(i);
                    if(newfile.getState() != C_STATE_DELETED) {
                        try{
                            cms.chmod(newfile.getAbsolutePath(), flags);
                        } catch (CmsException e){
                            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] "+newfile.getAbsolutePath()+": "+e.getStackTraceAsString());
                            }
                        }
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
        }
*/
        }

    /**
    * Changes the owner of a resource.
    * <br>
    * Only the owner of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change this, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or the user is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newOwner the name of the new owner for this resource.
    * @param chRekursive shows if the subResources (of a folder) should be changed too.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chown(CmsObject cms, String filename, String newOwner, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the owner of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
                || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))) {
            // change the owner
            cms.doChown(filename, newOwner);
            String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + folder.getAbsolutePath();
            try {
                cms.readFolder(bodyFolder);
                cms.doChown(bodyFolder, newOwner);
            }
            catch(CmsException ex) {
            // no folder is there, so do nothing
            }
            // the rekursive flag was set do a recursive chmod on all files and subfolders
            if(chRekursive) {
                // get all subfolders and files
                Vector allFolders = new Vector();
                Vector allFiles = new Vector();
                getAllResources(cms, filename, allFiles, allFolders);
                // now modify all subfolders
                for(int i = 0;i < allFolders.size();i++) {
                    CmsFolder curfolder = (CmsFolder)allFolders.elementAt(i);
                    if(curfolder.getState() != C_STATE_DELETED) {
                        cms.doChown(curfolder.getAbsolutePath(), newOwner);
                        // check if there is a corresponding directory in the content body folder
                        bodyFolder = C_VFS_PATH_BODIES.substring(0,
                                C_VFS_PATH_BODIES.lastIndexOf("/")) + curfolder.getAbsolutePath();
                        try {
                            cms.readFolder(bodyFolder);
                            cms.doChown(bodyFolder, newOwner);
                        }catch(CmsException ex) {
                        // no folder is there, so do nothing
                        }
                    }
                }
                // now modify all files in the subfolders
                for(int i = 0;i < allFiles.size();i++) {
                    CmsFile newfile = (CmsFile)allFiles.elementAt(i);
                    if(newfile.getState() != C_STATE_DELETED) {
                        try{
                            cms.chown(newfile.getAbsolutePath(), newOwner);
                        } catch (CmsException e){
                            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] "+newfile.getAbsolutePath()+": "+e.getStackTraceAsString());
                            }
                        }
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
        }
*/
        }
    
    /**
     * Change the timestamp of a folder.
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param boolean flag to touch recursively all sub-resources in case of a folder
     */  
    public void touch( CmsObject cms, String resourceName, long timestamp, boolean touchRecursive ) throws CmsException {
        Vector allFolders = new Vector();
        Vector allFiles = new Vector();
        Vector unvisited = new Vector();
        
        // create a valid resource
        CmsFolder currentFolder = cms.readFolder( resourceName );
        CmsFile currentFile = null;
        
        // check the access rights
        // TODO: normally, only resources with write access should be collected here
        // in order to prevent exceptions while touching
        if (!cms.checkPermissions(currentFolder, I_CmsConstants.C_WRITE_ACCESS))
        	return;
        
        if (touchRecursive) {
            // collect all folders and files by traversing the tree on a breadth first search
            unvisited.add( currentFolder );
            
            while (unvisited.size()>0) {
                // visit all unvisited folders
                Enumeration unvisitedFolders = unvisited.elements();
                while (unvisitedFolders.hasMoreElements()) {
                    currentFolder = (CmsFolder)unvisitedFolders.nextElement();
                    
                    // remove the current folder from the unvisited folders
                    unvisited.remove( (CmsFolder)currentFolder );
                    // add the current folder to the set of all folders to be touched
                    allFolders.add( (CmsFolder)currentFolder );
                    // add the files in the current folder to the set of all files to be touched
                    allFiles.addAll( cms.getFilesInFolder(currentFolder.getAbsolutePath(), true) );
                    // add all sub-folders in the current folder to visit them in the next iteration                        
                    unvisited.addAll( cms.getSubFolders(currentFolder.getAbsolutePath(),true) );
                }
            }
        }
        else {
            allFolders.add( (CmsFolder)currentFolder );
        }
        
        // touch the folders that we collected before
        Enumeration touchFolders = allFolders.elements();
        while (touchFolders.hasMoreElements()) {
            currentFolder = (CmsFolder)touchFolders.nextElement();
            
            if (DEBUG>0) System.err.println( "touching: " + currentFolder.getAbsolutePath() );

            // touch the folder itself
            cms.doTouch( currentFolder.getAbsolutePath(), timestamp );
            
            // touch its counterpart under content/bodies
            String bodyFolder = C_VFS_PATH_BODIES.substring( 0, C_VFS_PATH_BODIES.lastIndexOf("/")) + currentFolder.getAbsolutePath();
            try {
                cms.readFolder( bodyFolder );
                cms.doTouch( bodyFolder, timestamp );
            }
            catch(CmsException e) {}                
        }
        
        // touch the files/resources that we collected before
        Enumeration touchFiles = allFiles.elements();
        while (touchFiles.hasMoreElements()) {
            currentFile = (CmsFile)touchFiles.nextElement();
            
            if (DEBUG>0) System.err.println( "touching: " + currentFile.getAbsolutePath() );
            
            if(currentFile.getState()!=I_CmsConstants.C_STATE_DELETED) {
                // touch the file itself
                cms.touch( currentFile.getAbsolutePath(), timestamp, false );
            }
        }            
    }    

    /**
    * Changes the resourcetype of a resource.
    * <br>
    * Only the resourcetype of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not exisiting in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change this, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newType the name of the new resourcetype for this resource.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chtype(CmsObject cms, String filename, String newType) throws CmsException{

        // it is not possible to change the type of a folder
        throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_ACCESS_DENIED);
    }


    /**
    * Copies a Resource.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfolder.
    * @param keepFlags <code>true</code> if the copy should keep the source file's flags,
    *        <code>false</code> if the copy should get the user's default flags.
    *
    * @throws CmsException if the file couldn't be copied, or the user
    * has not the appropriate rights to copy the file.
    */
    public void copyResource(CmsObject cms, String source, String destination, boolean keepFlags) throws CmsException{

       // we have to copy the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        // first valid the destination name
        validResourcename(destination);

        getAllResources(cms, source, allSubFiles, allSubFolders);
        if (!destination.endsWith("/")){
            destination = destination +"/";
        }
        if (!destination.startsWith("/")){
            destination = "/"+destination ;
        }
        // first the folder
        cms.doCopyFolder(source, destination);
        if(!keepFlags){
            setDefaultFlags(cms, destination);
        }
        // now the subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_DELETED){
                String curDestination = destination + curFolder.getAbsolutePath().substring(source.length());
                cms.doCopyFolder(curFolder.getAbsolutePath(), curDestination );
                if(!keepFlags){
                    setDefaultFlags(cms, curDestination);
                }
            }
        }
        // now all the little files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_DELETED){
                String curDest = destination + curFile.getAbsolutePath().substring(source.length());
                cms.copyResource(curFile.getAbsolutePath(), curDest, keepFlags);
            }
        }
        // copy the content bodys
        try{
            copyResource(cms, C_VFS_PATH_BODIES + source.substring(1), C_VFS_PATH_BODIES + destination.substring(1), keepFlags);
            // finaly lock the copy in content bodys if it exists.
            cms.lockResource(C_VFS_PATH_BODIES + destination.substring(1));
        }catch(CmsException e){
        }
    }

    /**
    * Copies a resource from the online project to a new, specified project.
    * <br>
    * Copying a resource will copy the file header or folder into the specified
    * offline project and set its state to UNCHANGED.
    *
    * @param resource the name of the resource.
    * @throws CmsException if operation was not successful.
    */
    public void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException {
        // copy the folder to the current project
        cms.doCopyResourceToProject(resourceName);
        // try to copy the corresponding folder in C_VFS_PATH_BODIES to the project
        try{
            CmsResource contentFolder = (CmsResource)cms.readFolder(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/"))+resourceName, true);
            if (contentFolder != null){
                cms.doCopyResourceToProject(contentFolder.getAbsolutePath());
            }
        } catch(CmsException e){
            // cannot read the folder in C_VFS_PATH_BODIES so do nothing
        }
    }

    /**
    * Creates a new resource.<br>
    *
    * @param folder the complete path to the folder in which the file will be created.
    * @param filename the name of the new resource.
    * @param contents the contents of the new file.
    * @param type the resourcetype of the new file.
    *
    * @return file a <code>CmsFile</code> object representing the newly created file.
    *
    * @throws CmsException or if the resourcetype is set to folder. The CmsException is also thrown, if the
    * filename is not valid or if the user has not the appropriate rights to create a new file.
    */
    public CmsResource createResource(CmsObject cms, String newFolderName, Map properties, byte[] contents, Object parameter) throws CmsException{
        if (! newFolderName.endsWith(C_FOLDER_SEPARATOR)) newFolderName += C_FOLDER_SEPARATOR;
        CmsFolder res = cms.doCreateFolder(newFolderName, properties);        
        cms.lockResource(newFolderName);
        res.setLocked(cms.getRequestContext().currentUser().getId());
        return res;
    }


    /**
    * Deletes a resource.
    *
    * @param folder the complete path of the folder.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    public void deleteResource(CmsObject cms, String folder) throws CmsException{

        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, folder, allSubFiles, allSubFolders);
        // first delete all the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_DELETED){
                try{
                    cms.deleteResource(curFile.getAbsolutePath());
                }catch(CmsException e){
                    if(e.getType() != CmsException.C_RESOURCE_DELETED){
                        throw e;
                    }
                }
            }
        }
        // now all the empty subfolders
        for (int i=allSubFolders.size() - 1; i > -1; i--){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_DELETED){
                cms.doDeleteFolder(curFolder.getAbsolutePath());
            }
        }

        // finaly the folder
        cms.doDeleteFolder(folder);
        // delete the corresponding folder in C_VFS_PATH_BODIES
        String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + folder;
        try {
            cms.readFolder(bodyFolder);
            cms.deleteResource(bodyFolder);
        }
        catch(CmsException ex) {
            // no folder is there, so do nothing
        }
    }

    /**
    * Undeletes a resource.
    *
    * @param folder the complete path of the folder.
    *
    * @throws CmsException if the file couldn't be undeleted, or if the user
    * has not the appropriate rights to undelete the file.
    */
    public void undeleteResource(CmsObject cms, String folder) throws CmsException{

       // we have to undelete the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, folder, allSubFiles, allSubFolders);
        // first undelete all the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() == C_STATE_DELETED){
                cms.undeleteResource(curFile.getAbsolutePath());
            }
        }
        // now all the empty subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() == C_STATE_DELETED){
                cms.doUndeleteFolder(curFolder.getAbsolutePath());
            }
        }
        // finally the folder
        cms.doUndeleteFolder(folder);
        // undelete the corresponding folder in C_VFS_PATH_BODIES
        String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + folder;
        try {
            cms.readFolder(bodyFolder,true);
            cms.undeleteResource(bodyFolder);
        }
        catch(CmsException ex) {
            // no folder is there, so do nothing
        }
    }

    /**
     * Does the Linkmanagement when a resource will be exported.
     * When a resource has to be exported, the ID´s inside the
     * Linkmanagement-Tags have to be changed to the corresponding URL´s
     *
     * @param file is the file that has to be changed
     */
    public CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException {
        // nothing to do here, because there couldn´t be any Linkmanagement-Tags inside a folder-resource
        return file;
    }

    /**
     * Imports a resource.
     *
     * @param cms The current CmsObject.
     * @param source The sourcepath of the resource to import.
     * @param destination The destinationpath of the resource to import.
     * @param type The type of the resource to import.
     * @param user The name of the owner of the resource.
     * @param group The name of the group of the resource.
     * @param access The access flags of the resource.
     * @param properties A Hashtable with the properties of the resource.
     * The key is the name of the propertydefinition, the value is the propertyvalue.
     * @param launcherStartClass The name of the launcher startclass.
     * @param content The filecontent if the resource is of type file
     * @param importPath The name of the import path
     * 
     * @return CmsResource The imported resource.
     * 
     * @throws Throws CmsException if the resource could not be imported
     * 
     */
    public CmsResource importResource(CmsObject cms, String source, String destination, String type,
                                       String user, String group, String access, long lastmodified, 
                                       Map properties, String launcherStartClass, byte[] content, String importPath) 
                       throws CmsException {
        CmsResource importedResource = null;
        destination = importPath + destination;
        if (! destination.endsWith(C_FOLDER_SEPARATOR)) destination += C_FOLDER_SEPARATOR;

        boolean changed = true;
        // try to read the new owner and group
        // TODO: fix this later
        CmsUser resowner = null;
        CmsGroup resgroup = null;
        int resaccess = 0;
        try{
        	resowner = cms.readUser(user);
        } catch (CmsException e){
        	if (DEBUG>0) System.err.println("[" + this.getClass().getName() + ".importResource/1] User " + user + " not found");
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + ".importResource/1] User " + user + " not found");
            }            
        	resowner = cms.getRequestContext().currentUser();	
        }
        try{
        	resgroup = cms.readGroup(group);
        } catch (CmsException e){
            if (DEBUG>0) System.err.println("[" + this.getClass().getName() + ".importResource/2] Group " + group + " not found");
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + ".importResource/2] Group " + group + " not found");
            }             
        	resgroup = cms.getRequestContext().currentGroup();	
        } 
        try {
        	resaccess = Integer.parseInt(access);
        } catch (Exception e){
        	// 
        }
        // try to create the resource		
        try {
            importedResource = cms.doImportResource(destination, C_TYPE_FOLDER, properties, C_UNKNOWN_LAUNCHER_ID, 
                                             C_UNKNOWN_LAUNCHER, resowner.getName(), resgroup.getName(), resaccess, lastmodified, new byte[0]);
            if(importedResource != null){
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the folder already exists
        }
        if(changed){
        	changed = false;
            //the resource exists, check if properties has to be updated
            importedResource = cms.readFolder(destination);
            Map oldProperties = cms.readProperties(importedResource.getAbsolutePath());
            if(oldProperties == null){
                oldProperties = new HashMap();
            }
            if(properties == null){
                properties = new Hashtable();
            }
            if (properties.size() > 0) {
                // if no properties are to be imported we do not need to check the old properties
                if (oldProperties.size() != properties.size()) {
                    changed = true;
                } else {
                    // check each of the properties
                    Iterator i = properties.keySet().iterator();
                    while (i.hasNext()) {
                        String curKey = (String) i.next();
                        String value = (String) properties.get(curKey);
                        String oldValue = (String) oldProperties.get(curKey);
                        if ((oldValue == null) || !(value.trim().equals(oldValue.trim()))) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            // check changes of the owner, group and access
            // TODO: fix this later
            //if(importedResource.getAccessFlags() != resaccess ||
            //       (!importedResource.getOwnerId().equals(resowner.getId())) ||
            //       (!importedResource.getGroupId().equals(resgroup.getId()))){
            //    changed = true;            
            //}
            // check changes of the resourcetype
            if(importedResource.getType() != cms.getResourceType(type).getResourceType()){
            	changed = true;
            }
            // update the folder if something has changed
            if(changed){
            	lockResource(cms,importedResource.getAbsolutePath(), true);
            	cms.doWriteResource(importedResource.getAbsolutePath(),properties,resowner.getName(), resgroup.getName(),resaccess,C_TYPE_FOLDER,new byte[0]);
            }
        }

        return importedResource;
    }

    /**
    * Locks a given resource.
    * <br>
    * A user can lock a resource, so he is the only one who can write this
    * resource.
    *
    * @param resource the complete path to the resource to lock.
    * @param force if force is <code>true</code>, a existing locking will be overwritten.
    *
    * @throws CmsException if the user has not the rights to lock this resource.
    * It will also be thrown, if there is a existing lock and force was set to false.
    */
    public void lockResource(CmsObject cms, String resource, boolean force) throws CmsException{
        // first lock the folder in the C_VFS_PATH_BODIES path if it exists.
        try{
            cms.doLockResource(C_VFS_PATH_BODIES  + resource.substring(1), force);
        }catch(CmsException e){
            // ignore the error. this folder doesent exist.
        }
        // now lock the folder
        cms.doLockResource(resource, force);
    }

    /**
    * Moves a file to the given destination.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfile.
    *
    * @throws CmsException if the user has not the rights to move this resource,
    * or if the file couldn't be moved.
    */
    public void moveResource(CmsObject cms, String source, String destination) throws CmsException{
        // it is a folder so we need the end /
        destination = destination +"/";
       // we have to copy the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, source, allSubFiles, allSubFolders);
        if(!cms.accessWrite(source)){
            throw new CmsException(source, CmsException.C_NO_ACCESS);
        }
        // first copy the folder
        cms.doCopyFolder(source, destination);
        // now copy the subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_DELETED){
                String curDestination = destination + curFolder.getAbsolutePath().substring(source.length());
                cms.doCopyFolder(curFolder.getAbsolutePath(), curDestination );
            }
        }
        // now move the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_DELETED){
                String curDest = destination + curFile.getAbsolutePath().substring(source.length());
                cms.moveResource(curFile.getAbsolutePath(), curDest);
            }
        }
        // finaly remove the original folders
        deleteResource(cms, source);
    }

    /**
    * Renames the file to the new name.
    *
    * @param oldname the complete path to the file which will be renamed.
    * @param newname the new name of the file.
    *
    * @throws CmsException if the user has not the rights
    * to rename the file, or if the file couldn't be renamed.
    */
    public void renameResource(CmsObject cms, String oldname, String newname) throws CmsException{
        // first of all check the new name
        validResourcename(newname.replace('/','\n'));
       // we have to copy the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, oldname, allSubFiles, allSubFolders);
        String parent = ((CmsResource)cms.readFileHeader(oldname)).getParent();
        if(!cms.accessWrite(oldname)){
            throw new CmsException(oldname, CmsException.C_NO_ACCESS);
        }
        if(!newname.endsWith("/")){
            newname = newname+"/";
        }
        // first copy the folder
        cms.doCopyFolder(oldname, parent + newname);
        // now copy the subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_DELETED){
                String curDestination = parent + newname
                                        + curFolder.getAbsolutePath().substring(oldname.length());
                cms.doCopyFolder(curFolder.getAbsolutePath(), curDestination );
            }
        }
        // now move the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_DELETED){
                String curDest = parent + newname
                                + curFile.getAbsolutePath().substring(oldname.length());
                cms.moveResource(curFile.getAbsolutePath(), curDest);
            }
        }
        // finaly remove the original folders
        deleteResource(cms, oldname);

    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param cms The CmsObject
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(CmsObject cms, int versionId, String filename) throws CmsException{
        throw new CmsException("[" + this.getClass().getName() + "] Cannot restore folders.",CmsException.C_ACCESS_DENIED);
    }

    /**
    * Undo changes in a resource.
    * <br>
    *
    * @param resource the complete path to the resource to be restored.
    *
    * @throws CmsException if the user has not the rights
    * to write this resource.
    */
    public void undoChanges(CmsObject cms, String resource) throws CmsException{
        // we have to undo changes of the folder and all resources in the folder
//        Vector allSubFolders = new Vector();
//        Vector allSubFiles   = new Vector();
//        getAllResources(cms, resource, allSubFiles, allSubFolders);
        
        if(!cms.accessWrite(resource)){
            throw new CmsException("[" + this.getClass().getName() + "]"+resource, CmsException.C_NO_ACCESS);
        }
        // first undo changes of the folder
        cms.doUndoChanges(resource);
        
        // check if there is a corrosponding body folder
        String bodyPath = C_VFS_PATH_BODIES  + resource.substring(1);
        boolean hasBodyFolder = false;
        try {
            cms.readFileHeader(bodyPath);
            hasBodyFolder = true;
        } catch (CmsException e) {
            // body folder not found or no access, so ignore it
        }
        // undo changes in the corresponding folder in C_VFS_PATH_BODIES          
        if (hasBodyFolder) {
            cms.doUndoChanges(bodyPath);
        }
        
        // TODO: Implement optional "recurse" function 
        // now undo changes of the subfolders
//        for (int i=0; i<allSubFolders.size(); i++){
//            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
//            if(curFolder.getState() != C_STATE_NEW){
//                if(curFolder.getState() == C_STATE_DELETED){
//                    undeleteResource(cms, curFolder.getAbsolutePath());
//                    lockResource(cms, curFolder.getAbsolutePath(), true);
//                }
//                undoChanges(cms, curFolder.getAbsolutePath());
//            } else {
//                // if it is a new folder then delete the folder
//                try{
//                    deleteResource(cms, curFolder.getAbsolutePath());
//                } catch (CmsException ex){
//                    // do not throw exception when resource not exists
//                    if(ex.getType() != CmsException.C_NOT_FOUND){
//                        throw ex;
//                    }
//                }
//            }
//        }
//        // now undo changes in the files
//        for (int i=0; i<allSubFiles.size(); i++){
//            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
//            if(curFile.getState() != C_STATE_NEW){
//                if(curFile.getState() == C_STATE_DELETED){
//                    cms.undeleteResource(curFile.getAbsolutePath());
//                    cms.lockResource(curFile.getAbsolutePath(), true);
//                }
//                cms.undoChanges(curFile.getAbsolutePath());
//            } else {
//                // if it is a new file then delete the file
//                try{
//                    cms.deleteResource(curFile.getAbsolutePath());
//                } catch (CmsException ex){
//                    // do not throw exception when resource not exists
//                    if(ex.getType() != CmsException.C_NOT_FOUND){
//                        throw ex;
//                    }
//                }
//            }
//        }
    }

    /**
    * Unlocks a resource.
    * <br>
    * A user can unlock a resource, so other users may lock this file.
    *
    * @param resource the complete path to the resource to be unlocked.
    *
    * @throws CmsException if the user has not the rights
    * to unlock this resource.
    */
    public void unlockResource(CmsObject cms, String resource) throws CmsException{
        // first unlock the folder in the C_VFS_PATH_BODIES path if it exists.
        try{
            cms.doUnlockResource(C_VFS_PATH_BODIES  + resource.substring(1));
        }catch(CmsException e){
            // ignore the error. this folder doesent exist.
        }
        // now unlock the folder
        cms.doUnlockResource(resource);
    }
    /**
     * Set the access flags of the copied Folder to the default values.
     * @param cms The CmsObject.
     * @param foldername The name of the folder.
     * @throws Throws CmsException if something goes wrong.
     */
    private void setDefaultFlags(CmsObject cms, String foldername)
        throws CmsException {

        Hashtable startSettings=null;
        Integer accessFlags=null;
        startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
        if (startSettings != null) {
            accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
        }
        if (accessFlags == null) {
            accessFlags = new Integer(C_ACCESS_DEFAULT_FLAGS);
        }
        chmod(cms, foldername, accessFlags.intValue(), false);
    }
    /**
     * Gets all resources - files and subfolders - of a given folder.
     * @param cms The CmsObject.
     * @param rootFolder The name of the given folder.
     * @param allFiles Vector containing all files found so far. All files of this folder
     * will be added here as well.
     * @param allolders Vector containing all folders found so far. All subfolders of this folder
     * will be added here as well.
     * @throws Throws CmsException if something goes wrong.
     */

    private void getAllResources(CmsObject cms, String rootFolder, Vector allFiles,
                    Vector allFolders) throws CmsException {
        Vector folders = new Vector();
        Vector files = new Vector();

        // get files and folders of this rootFolder
        folders = cms.getSubFolders(rootFolder, true);
        files = cms.getFilesInFolder(rootFolder, true);

        //copy the values into the allFiles and allFolders Vectors
        for(int i = 0;i < folders.size();i++) {
            allFolders.addElement((CmsFolder)folders.elementAt(i));
            getAllResources(cms, ((CmsFolder)folders.elementAt(i)).getAbsolutePath(),
                allFiles, allFolders);
        }
        for(int i = 0;i < files.size();i++) {
            allFiles.addElement((CmsFile)files.elementAt(i));
        }
    }

    /**
     * Checks if there are at least one character in the resourcename
     *
     * @param resourcename String to check
     *
     * @throws throws a exception, if the check fails.
     */
    protected void validResourcename( String resourcename )
        throws CmsException {
        if (resourcename == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + resourcename,
                CmsException.C_BAD_NAME);
        }

        int l = resourcename.trim().length();

        if (l == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + resourcename,
                CmsException.C_BAD_NAME);
        }
    }

    /**
     * Changes the project-id of the resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The Id of the new project
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(CmsObject cms, int newProjectId, String resourcename)
        throws CmsException{
        // we have to change the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, resourcename, allSubFiles, allSubFolders);
        // first change all the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_UNCHANGED){
                cms.changeLockedInProject(newProjectId, curFile.getAbsolutePath());
            }
        }
        // now all the subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_UNCHANGED){
                changeLockedInProject(cms, newProjectId, curFolder.getAbsolutePath());
            }
        }
        // finally the folder
        cms.doChangeLockedInProject(newProjectId, resourcename);
        // change the corresponding folder in C_VFS_PATH_BODIES
        String bodyFolder = C_VFS_PATH_BODIES.substring(0,
                    C_VFS_PATH_BODIES.lastIndexOf("/")) + resourcename;
        try {
            cms.readFolder(bodyFolder,true);
            changeLockedInProject(cms, newProjectId, bodyFolder);
        }
        catch(CmsException ex) {
            // no folder is there, so do nothing
        }
    }
}