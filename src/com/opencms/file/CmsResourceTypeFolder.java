package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeFolder.java,v $
 * Date   : $Date: 2001/07/31 15:28:53 $
 * Version: $Revision: 1.17 $
 *
 * Copyright (C) 2000  The OpenCms Group
 *
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 *
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.core.*;
//import com.opencms.template.*;
//import com.opencms.util.*;
import java.util.*;

import java.io.*;
//import org.w3c.dom.*;
import com.opencms.file.genericSql.*;
//import com.opencms.file.genericSql.linkmanagement.*;

/**
 * Access class for resources of the type "Folder".
 *
 * @author
 * @version 1.0
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
	* @exception CmsException if operation was not successful.
	*/
	public void chgrp(CmsObject cms, String filename, String newGroup, boolean chRekursive) throws CmsException{

        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
                || (cms.userInGroup(cms.getRequestContext().currentUser().getName(),
                C_GROUP_ADMIN))) {
            cms.doChgrp(filename, newGroup);
            // now change the bodyfolder if exists
            String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder.getAbsolutePath();
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
                        bodyFolder = C_CONTENTBODYPATH.substring(0,
                                C_CONTENTBODYPATH.lastIndexOf("/")) + curfolder.getAbsolutePath();
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
                        cms.chgrp(newfile.getAbsolutePath(), newGroup);
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
        }
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
	* @exception CmsException if operation was not successful.
	* for this resource.
	*/
	public void chmod(CmsObject cms, String filename, int flags, boolean chRekursive) throws CmsException{

        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
            || (cms.userInGroup(cms.getRequestContext().currentUser().getName(),
            C_GROUP_ADMIN))) {
             // modify the access flags
            cms.doChmod(filename, flags);
            // now change the bodyfolder if exists
            String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder.getAbsolutePath();
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
                        bodyFolder = C_CONTENTBODYPATH.substring(0,
                                C_CONTENTBODYPATH.lastIndexOf("/")) + curfolder.getAbsolutePath();
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
                        cms.chmod(newfile.getAbsolutePath(), flags);
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
        }
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
	* Access is cranted, if:
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
	* @exception CmsException if operation was not successful.
	*/
	public void chown(CmsObject cms, String filename, String newOwner, boolean chRekursive) throws CmsException{

        CmsFolder folder = cms.readFolder(filename);
        // check if the current user has the right to change the owner of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if((cms.getRequestContext().currentUser().equals(cms.readOwner(folder)))
                || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))) {
            // change the owner
            cms.doChown(filename, newOwner);
            String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder.getAbsolutePath();
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
                        bodyFolder = C_CONTENTBODYPATH.substring(0,
                                C_CONTENTBODYPATH.lastIndexOf("/")) + curfolder.getAbsolutePath();
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
                        cms.chown(newfile.getAbsolutePath(), newOwner);
                    }
                }
            }
        }else{
            throw new CmsException("[" + this.getClass().getName() + "] " + filename,
                                    CmsException.C_NO_ACCESS);
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
	* @exception CmsException if operation was not successful.
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
	* @exception CmsException if the file couldn't be copied, or the user
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
            copyResource(cms, C_CONTENTBODYPATH + source.substring(1), C_CONTENTBODYPATH + destination.substring(1), keepFlags);
            // finaly lock the copy in content bodys if it exists.
            cms.lockResource(C_CONTENTBODYPATH + destination.substring(1));
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
    * @exception CmsException if operation was not successful.
	*/
    //public byte[] copyResourceToProject(CmsObject cms, I_CmsLinkManager linkManager, int resourceId, byte[] content) throws CmsException {
    public byte[] copyResourceToProject(CmsObject cms, String resourceName, byte[] content) throws CmsException {
        return content;
    }

	/**
	* Copies a resource from the online project to a new, specified project.
	* <br>
	* Copying a resource will copy the file header or folder into the specified
	* offline project and set its state to UNCHANGED.
	*
	* @param resource the name of the resource.
    * @exception CmsException if operation was not successful.
	*/
    public void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException {
        // copy the folder to the current project
        cms.doCopyResourceToProject(resourceName);
        // try to copy the corresponding folder in /content/bodys/ to the project
        try{
            CmsResource contentFolder = (CmsResource)cms.readFolder(C_CONTENTBODYPATH.substring(0, C_CONTENTBODYPATH.lastIndexOf("/"))+resourceName, true);
            if (contentFolder != null){
                cms.doCopyResourceToProject(contentFolder.getAbsolutePath());
            }
        } catch(CmsException e){
            // cannot read the folder in /content/bodys/ so do nothing
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
	* @exception CmsException or if the resourcetype is set to folder. The CmsException is also thrown, if the
	* filename is not valid or if the user has not the appropriate rights to create a new file.
	*/
	public CmsResource createResource(CmsObject cms, String folder, String name, Hashtable properties, byte[] contents) throws CmsException{
        CmsFolder res = cms.doCreateFolder(folder, name, properties);
        cms.lockResource(folder+name+"/");
        res.setLocked(cms.getRequestContext().currentUser().getId());
		return res;
    }


	/**
	* Deletes a resource.
	*
	* @param folder the complete path of the folder.
	*
	* @exception CmsException if the file couldn't be deleted, or if the user
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
                cms.deleteResource(curFile.getAbsolutePath());
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
        // delete the corresponding folder in /content/bodys/
        String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder;
        try {
            cms.readFolder(bodyFolder);
            cms.deleteResource(bodyFolder);
        }
        catch(CmsException ex) {
            // no folder is there, so do nothing
        }
    }

	/**
	* Uneletes a resource.
	*
	* @param folder the complete path of the folder.
	*
	* @exception CmsException if the file couldn't be undeleted, or if the user
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
        // undelete the corresponding folder in /content/bodys/
        String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder;
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
     * Imports a Folder.
     * Does the Linkmanagement when a resource is imported.
     * When a resource has to be imported, the URL´s of the
     * Links inside the resources have to be saved and changed to the corresponding ID´s
     *
     * @param file is the file that has to be changed
     */
    public CmsResource importResource(CmsObject cms, String source, String destination, String type, String user, String group, String access, Hashtable properties, String launcherStartClass, byte[] content, String importPath) throws CmsException {
        CmsResource cmsfolder = null;

        String path = importPath + destination.substring(0, destination.lastIndexOf("/") + 1);
		String name = destination.substring((destination.lastIndexOf("/") + 1), destination.length());
        String fullname = null;

        try {
            cmsfolder = createResource(cms, path, name, properties, content);
            if(cmsfolder != null){
                fullname = cmsfolder.getAbsolutePath();
                lockResource(cms, fullname, true);
            }
        } catch (CmsException e) {
            // an exception is thrown if the folder already exists
        }
        if(fullname == null){
            //the folder exists, check if properties has to be updated
            cmsfolder = cms.readFolder(path, name + "/");
            Hashtable oldProperties = cms.readAllProperties(cmsfolder.getAbsolutePath());
            if(oldProperties == null){
                oldProperties = new Hashtable();
            }
            if(properties == null){
                properties = new Hashtable();
            }
            if( !oldProperties.equals(properties)){
                fullname = cmsfolder.getAbsolutePath();
                lockResource(cms, fullname, true);
                cms.writeProperties(fullname, properties);
            }
            if(fullname == null){
                // properties are the same but what about the owner, group and access?
                if(cmsfolder.getFlags() != Integer.parseInt(access) ||
                        cmsfolder.getOwnerId() != cms.readUser(user).getId() ||
                        cmsfolder.getGroupId() != cms.readGroup(group).getId() ){
                    fullname = cmsfolder.getAbsolutePath();
                    lockResource(cms, fullname, true);
                }
            }
        }

        if(fullname != null){
            try{
                cms.chmod(fullname, Integer.parseInt(access));
            }catch(CmsException e){
                System.out.println("chmod(" + access + ") failed ");
            }
            try{
                cms.chgrp(fullname, group);
            }catch(CmsException e){
                System.out.println("chgrp(" + group + ") failed ");
            }
            try{
                cms.chown(fullname, user);
            }catch(CmsException e){
                System.out.println("chown((" + user + ") failed ");
            }
        }

        return cmsfolder;
    }

    /**
     *
     */
    public void linkmanagementSaveImportedResource(CmsObject cms, String importedResource) throws CmsException {
        // nothing to do here
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
	* @exception CmsException if the user has not the rights to lock this resource.
	* It will also be thrown, if there is a existing lock and force was set to false.
	*/
	public void lockResource(CmsObject cms, String resource, boolean force) throws CmsException{
        // first lock the folder in the /content/bodys/ path if it exists.
        try{
            cms.doLockResource(C_CONTENTBODYPATH  + resource.substring(1), force);
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
	* @exception CmsException if the user has not the rights to move this resource,
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
     *
     */
    //public byte[] publishResource(I_CmsLinkManager linkManager, int resourceId, byte[] content) throws CmsException {
    public byte[] publishResource(String resourceName, byte[] content) throws CmsException {

        // nothing to do here in terms of the linkmanagement
        // return null. the content of the resource will not be changed
        return null;
    }

	/**
	* Renames the file to the new name.
	*
	* @param oldname the complete path to the file which will be renamed.
	* @param newname the new name of the file.
	*
	* @exception CmsException if the user has not the rights
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
     * @exception CmsException  Throws CmsException if operation was not succesful.
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
	* @exception CmsException if the user has not the rights
	* to write this resource.
	*/
	public void undoChanges(CmsObject cms, String resource) throws CmsException{
        // we have to undo changes of the folder and all resources in the folder
        Vector allSubFolders = new Vector();
        Vector allSubFiles   = new Vector();
        getAllResources(cms, resource, allSubFiles, allSubFolders);
        String parent = ((CmsResource)cms.readFileHeader(resource)).getParent();
        if(!cms.accessWrite(resource)){
            throw new CmsException("[" + this.getClass().getName() + "]"+resource, CmsException.C_NO_ACCESS);
        }
        // first undo changes of the folder
        cms.doUndoChanges(resource);
        // now undo changes of the subfolders
        for (int i=0; i<allSubFolders.size(); i++){
            CmsFolder curFolder = (CmsFolder) allSubFolders.elementAt(i);
            if(curFolder.getState() != C_STATE_NEW){
                if(curFolder.getState() == C_STATE_DELETED){
                    undeleteResource(cms, curFolder.getAbsolutePath());
                    lockResource(cms, curFolder.getAbsolutePath(), true);
                }
                undoChanges(cms, curFolder.getAbsolutePath());
            } else {
                // if it is a new folder then delete the folder
                try{
                    deleteResource(cms, curFolder.getAbsolutePath());
                } catch (CmsException ex){
                    // do not throw exception when resource not exists
                    if(ex.getType() != CmsException.C_NOT_FOUND){
                        throw ex;
                    }
                }
            }
        }
        // now undo changes in the files
        for (int i=0; i<allSubFiles.size(); i++){
            CmsFile curFile = (CmsFile)allSubFiles.elementAt(i);
            if(curFile.getState() != C_STATE_NEW){
                if(curFile.getState() == C_STATE_DELETED){
                    cms.undeleteResource(curFile.getAbsolutePath());
                    cms.lockResource(curFile.getAbsolutePath(), true);
                }
                cms.undoChanges(curFile.getAbsolutePath());
            } else {
                // if it is a new file then delete the file
                try{
                    cms.deleteResource(curFile.getAbsolutePath());
                } catch (CmsException ex){
                    // do not throw exception when resource not exists
                    if(ex.getType() != CmsException.C_NOT_FOUND){
                        throw ex;
                    }
                }
            }
        }
	}

	/**
	* Unlocks a resource.
	* <br>
	* A user can unlock a resource, so other users may lock this file.
	*
	* @param resource the complete path to the resource to be unlocked.
	*
	* @exception CmsException if the user has not the rights
	* to unlock this resource.
	*/
	public void unlockResource(CmsObject cms, String resource) throws CmsException{
        // first unlock the folder in the /content/bodys/ path if it exists.
        try{
            cms.doUnlockResource(C_CONTENTBODYPATH  + resource.substring(1));
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
	 * @exception Throws CmsException if something goes wrong.
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
     * @exception Throws CmsException if something goes wrong.
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
     * @exception throws a exception, if the check fails.
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
        // change the corresponding folder in /content/bodys/
        String bodyFolder = C_CONTENTBODYPATH.substring(0,
                    C_CONTENTBODYPATH.lastIndexOf("/")) + resourcename;
        try {
            cms.readFolder(bodyFolder,true);
            changeLockedInProject(cms, newProjectId, bodyFolder);
        }
        catch(CmsException ex) {
            // no folder is there, so do nothing
        }
    }
}