/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsResourceType.java,v $
* Date   : $Date: 2003/07/04 12:03:06 $
* Version: $Revision: 1.20 $
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

import com.opencms.core.CmsException;

import java.util.Map;

/**
 * Defines of all methods that a specific resource type has to implement.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public interface I_CmsResourceType {
    
    /**
     * init a new CmsResourceType object.
     *
     * @param resourceType The id of the resource type.
     * @param launcherType The id of the required launcher.
     * @param resourceTypeName The printable name of the resource type.
     * @param launcherClass The Java class that should be invoked by the launcher.
     * This value is <b> null </b> if the default invokation class should be used.
     */
    void init(int resourceType, int launcherType,
                           String resourceTypeName, String launcherClass);

     /**
     * Returns the name of the Java class loaded by the launcher.
     * This method returns <b>null</b> if the default class for this type is used.
     *
     * @return the name of the Java class.
     */
     String getLauncherClass();

     /**
     * Returns the launcher type needed for this resource-type.
     *
     * @return the launcher type for this resource-type.
     */
     int getLauncherType();

    /**
     * Returns the name for this resource-type.
     *
     * @return the name for this resource-type.
     */
     String getResourceTypeName();

    /**
     * Returns the type of this resource-type.
     *
     * @return the type of this resource-type.
     */
    int getResourceType();

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
    void chgrp(CmsObject cms, String filename, String newGroup, boolean chRekursive) throws CmsException;

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
    void chmod(CmsObject cms, String filename, int flags, boolean chRekursive) throws CmsException;

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
    void chown(CmsObject cms, String filename, String newOwner, boolean chRekursive) throws CmsException;
    
    /**
     * Change the timestamp of a resource.
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param boolean flag to touch recursively all sub-resources in case of a folder
     */
    void touch( CmsObject cms, String resourceName, long timestamp, boolean touchRecursive ) throws CmsException;

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
    void chtype(CmsObject cms, String filename, String newType) throws CmsException;


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
    void copyResource(CmsObject cms, String source, String destination, boolean keepFlags) throws CmsException;


    /**
     * Copies the resourcename to the current offline project
     * @param cms The CmsObject
     * @param resourceName The name of the resource
     *
     * @throws CmsException if operation was not successful.
     */
    void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException;

    /**
    * Creates a new resource.<br>
    *
    * @param folder the complete path to the folder in which the file will be created.
    * @param filename the name of the new file.
    * @param contents the contents of the new file.
    * @param type the resourcetype of the new file.
    *
    * @return file a <code>CmsFile</code> object representing the newly created file.
    *
    * @throws CmsException if the resourcetype is set to folder. The CmsException is also thrown, if the
    * filename is not valid or if the user has not the appropriate rights to create a new file.
    */
    CmsResource createResource(CmsObject cms, String newResourceName, Map properties, byte[] contents, Object parameter) throws CmsException;

    /**
    * Deletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    void deleteResource(CmsObject cms, String filename) throws CmsException;

    /**
    * Deletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    void undeleteResource(CmsObject cms, String filename) throws CmsException;

    /**
     * Does the Linkmanagement when a resource will be exported.
     * When a resource has to be exported, the ID큦 inside the
     * Linkmanagement-Tags have to be changed to the corresponding URL큦
     *
     * @param file is the file that has to be changed
     */
    CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException;

    /**
     * Does the Linkmanagement when a resource is imported.
     * When a resource has to be imported, the URL큦 of the
     * Links inside the resources have to be saved and changed to the corresponding ID큦
     *
     * @param file is the file that has to be changed
     */
    CmsResource importResource(CmsObject cms, String source, String destination, String type, String user, String group, String access, long lastmodified, Map properties, String launcherStartClass, byte[] content, String importPath) throws CmsException;

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
    void lockResource(CmsObject cms, String resource, boolean force) throws CmsException;

    /**
    * Moves a file to the given destination.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfile.
    *
    * @throws CmsException if the user has not the rights to move this resource,
    * or if the file couldn't be moved.
    */
    void moveResource(CmsObject cms, String source, String destination) throws CmsException;

    /**
    * Renames the file to the new name.
    *
    * @param oldname the complete path to the file which will be renamed.
    * @param newname the new name of the file.
    *
    * @throws CmsException if the user has not the rights
    * to rename the file, or if the file couldn't be renamed.
    */
    void renameResource(CmsObject cms, String oldname, String newname) throws CmsException;

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param cms The CmsObject
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    void restoreResource(CmsObject cms, int versionId, String filename) throws CmsException;

    /**
     * Undo all changes in the resource, restore the online file.
     *
     * @param resourceName The name of the resource to be restored.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void undoChanges(CmsObject cms, String filename) throws CmsException;

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
    void unlockResource(CmsObject cms, String resource) throws CmsException;

    /**
     * Changes the project-id of the resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The Id of the new project
     * @param resourcename The name of the resource to change
     */
    void changeLockedInProject(CmsObject cms, int newProjectId, String resourcename)
        throws CmsException;

    /**
     * Replaces the content and properties of an existing resource.<p>
     * 
     * @param cms the CmSsObject
     * @param resourceName the absolute path and name of the resource that is replaced
     * @param resourceProperties the properties of the resource
     * @param resourceContent the content of the resource
     * @throws CmsException if something goes wrong
     */
    void replaceResource(CmsObject cms, String resName, Map newResProperties, byte[] newResContent, String newResType) throws CmsException;

}
