/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypePlain.java,v $
* Date   : $Date: 2003/06/11 17:04:23 $
* Version: $Revision: 1.25 $
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
import java.util.Hashtable;
import java.util.Map;

/**
 * Implementation of a "basic and plain" resource type for any kind of resource.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public class CmsResourceTypePlain implements I_CmsResourceType, I_CmsConstants, Serializable {

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
     * Constructor, creates a new CmsResourceType object.
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
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chgrp(CmsObject cms, String filename, String newGroup, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        cms.doChgrp(filename, newGroup);
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
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    * for this resource.
    */
    public void chmod(CmsObject cms, String filename, int flags, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        cms.doChmod(filename, flags);
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
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chown(CmsObject cms, String filename, String newOwner, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        cms.doChown(filename, newOwner);
*/
        }
    
    /**
     * Change the timestamp of a resource.
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param boolean flag to touch recursively all sub-resources in case of a folder
     */  
    public void touch( CmsObject cms, String resourceName, long timestamp, boolean touchRecursive ) throws CmsException{
        cms.doTouch( resourceName, timestamp );
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
        cms.doChtype(filename, newType);
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
        cms.doCopyFile(source, destination);
        if(!keepFlags) {
            setDefaultFlags(cms, destination);
        }
    }

    /**
     * Copies the resourcename to the current offline project
     * @param cms The CmsObject
     * @param resourceName The name of the resource
     *
     * @throws CmsException if operation was not successful.
     */
    public void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException{
        cms.doCopyResourceToProject(resourceName);
    };

    /**
     * Creates a new resource
     *
     * @param cms The CmsObject
     * @param folder The name of the parent folder
     * @param name The name of the file
     * @param properties The properties of the file
     * @param contents The file content
     * @param parameter an object (e.g. a HashMap) holding parameters (e.g. key/value coded) to create the new resource
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsResource createResource(CmsObject cms, String newFileName, Map properties, byte[] contents, Object parameter) throws CmsException{
        CmsResource res;
        if (m_resourceTypeName == null) {
            res = cms.doCreateFile(newFileName, contents, I_CmsConstants.C_TYPE_PLAIN_NAME, properties); 
        } else {            
            res = cms.doCreateFile(newFileName, contents, m_resourceTypeName, properties);
        }
        // lock the new file
        cms.doLockResource(res.getAbsolutePath(), true);
        return res;
    }



    /**
    * Deletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    public void deleteResource(CmsObject cms, String filename) throws CmsException{
        cms.doDeleteFile(filename);
    }

    /**
    * Deletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    public void undeleteResource(CmsObject cms, String filename) throws CmsException{
        cms.doUndeleteFile(filename);
    }

    /**
     * Does the Linkmanagement when a resource will be exported.
     * When a resource has to be exported, the ID´s inside the
     * Linkmanagement-Tags have to be changed to the corresponding URL´s
     *
     * @param file is the file that has to be changed
     */
    public CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException {
        // nothing to do here, because there couldn´t be any Linkmanagement-Tags inside a plain-resource
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

        boolean changed = true;
        int resourceType = cms.getResourceType(type).getResourceType();
		int launcherType = cms.getResourceType(type).getLauncherType();
		if((launcherStartClass == null) || ("".equals(launcherStartClass))){
			launcherStartClass = cms.getResourceType(type).getLauncherClass();
        }
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
        } catch (Exception e) {
        	//
        }
        try {
            importedResource = cms.doImportResource(destination, resourceType ,properties, launcherType, 
                                             launcherStartClass, resowner.getName(), resgroup.getName(), resaccess, lastmodified, content);
            if(importedResource != null){
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the resource already exists
        }
        if(changed){
        	// if the resource already exists it must be updated
            lockResource(cms,destination, true);
            cms.doWriteResource(destination,properties,resowner.getName(), resgroup.getName(),resaccess,resourceType,content);
            importedResource = cms.readFileHeader(destination);
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
        cms.doLockResource(resource, force);
    }

    /**
    * Moves a resource to the given destination.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfile.
    *
    * @throws CmsException if the user has not the rights to move this resource,
    * or if the file couldn't be moved.
    */
    public void moveResource(CmsObject cms, String source, String destination) throws CmsException{
        cms.doMoveFile(source, destination);
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
        cms.doRenameFile(oldname, newname);
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
        if(!cms.accessWrite(filename)){
            throw new CmsException(filename, CmsException.C_NO_ACCESS);
        }
        cms.doRestoreResource(versionId, filename);
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
        if(!cms.accessWrite(resource)){
            throw new CmsException(resource, CmsException.C_NO_ACCESS);
        }
        cms.doUndoChanges(resource);
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
        cms.doUnlockResource(resource);
    }

    /**
     * Set the access flags of the copied resource to the default values.
     * @param cms The CmsObject.
     * @param filename The name of the file.
     * @throws Throws CmsException if something goes wrong.
     */
    protected void setDefaultFlags(CmsObject cms, String filename)
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
        chmod(cms, filename, accessFlags.intValue(), false);
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
        cms.doChangeLockedInProject(newProjectId, resourcename);
    }
}
