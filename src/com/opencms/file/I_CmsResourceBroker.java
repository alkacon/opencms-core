/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsResourceBroker.java,v $
* Date   : $Date: 2003/05/21 10:25:00 $
* Version: $Revision: 1.205 $
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

import com.opencms.core.CmsException;
import com.opencms.file.genericSql.CmsDbAccess;
import com.opencms.file.genericSql.CmsUserAccess;
import com.opencms.file.genericSql.CmsVfsAccess;
import com.opencms.file.genericSql.I_CmsUserAccess;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;

/**
 * This interface describes THE resource broker. All DB-specific access modules must
 * implement this interface.
 * The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.205 $ $Date: 2003/05/21 10:25:00 $
 *
 */

public interface I_CmsResourceBroker {

     /**
      * Accept a task from the Cms.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to accept.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void acceptTask(CmsUser currentUser, CmsProject currentProject, int taskId)
         throws CmsException ;
    /**
     * Checks, if the user may create this resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The resource to check.
     *
     * @return wether the user has access, or not.
     */
    public boolean accessCreate(CmsUser currentUser, CmsProject currentProject,
                                 String resourceName) throws CmsException;
    /**
     * Checks, if the user may lock this resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The resource to check.
     *
     * @return wether the user may lock this resource, or not.
     */
    public boolean accessLock(CmsUser currentUser, CmsProject currentProject,
                               String resourceName) throws CmsException;
    // Methods working with projects

    /**
     * Tests if the user can access the project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param projectId the id of the project.
     * @return true, if the user has access, else returns false.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public boolean accessProject(CmsUser currentUser, CmsProject currentProject,
                                 int projectId)
        throws CmsException;
    /**
     * Checks, if the user may read this resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The resource to check.
     *
     * @return wether the user has access, or not.
     */
    public boolean accessRead(CmsUser currentUser, CmsProject currentProject,
                               String resourceName) throws CmsException;
    /**
     * Checks, if the user may write this resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The resource to check.
     *
     * @return wether the user has access, or not.
     */
    public boolean accessWrite(CmsUser currentUser, CmsProject currentProject,
                                String resourceName) throws CmsException;
    /**
     * adds a file extension to the list of known file extensions
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param extension a file extension like 'html'
     * @param resTypeName name of the resource type associated to the extension
     */

    public void addFileExtension(CmsUser currentUser, CmsProject currentProject,
                                 String extension, String resTypeName)
        throws CmsException;
    /**
     * Add a new group to the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the new group.
     * @param description The description for the new group.
     * @param flags The flags for the new group.
     * @param name The name of the parent group (or null).
     *
     * @return Group
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsGroup addGroup(CmsUser currentUser, CmsProject currentProject,
                               String name, String description, int flags, String parent)
        throws CmsException;

    /**
     * Adds a user to the Cms.
     *
     * Only a adminstrator can add users to the cms.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addUser(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                             String name, String password,
                      String group, String description,
                      Hashtable additionalInfos, int flags)
        throws CmsException;

    /**
     * Adds a user to the Cms by import.
     *
     * Only a adminstrator can add users to the cms.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name for the user.
     * @param password The password for the user.
     * @param recoveryPassword The recoveryPassword for the user.
     * @param description The description for the user.
     * @param firstname The firstname of the user.
     * @param lastname The lastname of the user.
     * @param email The email of the user.
     * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param defaultGroup The default groupname for the user.
     * @param address The address of the user
     * @param section The section of the user
     * @param type The type of the user
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addImportUser(CmsUser currentUser, CmsProject currentProject,
        String name, String password, String recoveryPassword, String description,
        String firstname, String lastname, String email, int flags, Hashtable additionalInfos,
        String defaultGroup, String address, String section, int type)
        throws CmsException;

    /**
     * Adds a user to a group.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user that is to be added to the group.
     * @param groupname The name of the group.
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void addUserToGroup(CmsUser currentUser, CmsProject currentProject,
                               String username, String groupname)
        throws CmsException;
     /**
     * Adds a web user to the Cms. <br>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms,CmsUser currentUser, CmsProject currentProject,
                             String name, String password,
                      String group, String description,
                      Hashtable additionalInfos, int flags)
        throws CmsException;
     /**
     * Adds a web user to the Cms. <br>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param additionalGroup An additional group for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                             String name, String password,
                             String group, String additionalGroup, String description,
                             Hashtable additionalInfos, int flags)
        throws CmsException;
    /**
     * Returns the anonymous user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return the anonymous user object.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser anonymousUser(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
     /**
     * Changes the group for this resource<br>
     *
     * Only the group of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change this, if he is admin of the resource. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path to the resource.
     * @param newGroup The name of the new group for this resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void chgrp(CmsUser currentUser, CmsProject currentProject,
                      String filename, String newGroup)
        throws CmsException;
    /**
     * Changes the flags for this resource.<br>
     *
     * Only the flags of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change the flags, if he is admin of the resource <br>.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path to the resource.
     * @param flags The new accessflags for the resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void chmod(CmsUser currentUser, CmsProject currentProject,
                      String filename, int flags)
        throws CmsException;
    /**
     * Changes the owner for this resource.<br>
     *
     * Only the owner of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change this, if he is admin of the resource. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or the user is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path to the resource.
     * @param newOwner The name of the new owner for this resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void chown(CmsUser currentUser, CmsProject currentProject,
                      String filename, String newOwner)
        throws CmsException;
        
    /**
     * Access the resource broker underneath to change the timestamp of a resource.
     * 
     * @param currentUser the currentuser who requested this method
     * @param currentProject the current project of the user 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     */
    public void touch(CmsUser currentUser, CmsProject currentProject, String resourceName, long timestamp ) throws CmsException;    
            
     /**
     * Changes the state for this resource<BR/>
     *
     * The user may change this, if he is admin of the resource.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param filename The complete path to the resource.
     * @param state The new state of this resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public void chstate(CmsUser currentUser, CmsProject currentProject,
                        String filename, int state)
        throws CmsException;
     /**
     * Changes the resourcetype for this resource<br>
     *
     * Only the resourcetype of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change this, if he is admin of the resource. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path to the resource.
     * @param newType The name of the new resourcetype for this resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void chtype(CmsUser currentUser, CmsProject currentProject,
                      String filename, String newType)
        throws CmsException;
     /**
     * Clears all internal DB-Caches.
     */
    public void clearcache();
    /**
     * Copies a file in the Cms. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param source The complete path of the sourcefile.
     * @param destination The complete path to the destination.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyFile(CmsUser currentUser, CmsProject currentProject,
                         String source, String destination)
        throws CmsException;
     /**
     * Copies a folder in the Cms. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param source The complete path of the sourcefolder.
     * @param destination The complete path to the destination.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyFolder(CmsUser currentUser, CmsProject currentProject,
                         String source, String destination)
        throws CmsException;
    /**
     * Copies a resource from the online project to a new, specified project.<br>
     * Copying a resource will copy the file header or folder into the specified
     * offline project and set its state to UNCHANGED.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user is the owner of the project</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The name of the resource.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyResourceToProject(CmsUser currentUser,
                                      CmsProject currentProject,
                                      String resource)
        throws CmsException;
    /**
     * Counts the locked resources in this project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the project
     * @return the amount of locked resources in this project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int countLockedResources(CmsUser currentUser, CmsProject currentProject, int id)
        throws CmsException;
    /**
     * Creates a new file with the given content and resourcetype. <br>
     *
     * Files can only be created in an offline project, the state of the new file
     * is set to NEW (2). <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the folder-resource is not locked by another user</li>
     * <li>the file doesn't exist</li>
     * </ul>
     *
     * @param currentUser The user who owns this file.
     * @param currentGroup The group who owns this file.
     * @param currentProject The project in which the resource will be used.
     * @param newFileName The name of the new file with full path information.
     * @param contents The contents of the new file.
     * @param type The name of the resourcetype of the new file.
     * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     * @return file The created file.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsFile createFile(CmsUser currentUser, CmsGroup currentGroup,
                               CmsProject currentProject,
                               String newFileName, byte[] contents, String type,
                               Map propertyinfos)

         throws CmsException;
    /**
     * Creates a new folder.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is not locked by another user</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentGroup The group who requested this method.
     * @param currentProject The current project of the user.
     * @param newFolderName The name of the new folder (No pathinformation allowed).
     * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     *
     * @return file The created file.
     *
     * @throws CmsException will be thrown for missing propertyinfos, for worng propertydefs
     * or if the filename is not valid. The CmsException will also be thrown, if the
     * user has not the rights for this resource.
     */
    public CmsFolder createFolder(CmsUser currentUser, CmsGroup currentGroup,
                                  CmsProject currentProject,
                                  String folderName,
                                  Map propertyinfos)
        throws CmsException;

    /**
     * Creates a new resource.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is not locked by another user</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentGroup The group who requested this method.
     * @param currentProject The current project of the user.
     * @param folder The complete path to the folder in which the new resource will
     * be created.
     * @param newResourceName The name of the new resource (No pathinformation allowed).
     * @param resourceType The resourcetype of the new resource
     * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     * @param launcherType The launcher type of the new resource
     * @param launcherClassname The name of the launcherclass of the new resource
     * @param ownername The name of the owner of the new resource
     * @param groupname The name of the group of the new resource
     * @param accessFlags The accessFlags of the new resource
     * @param filecontent The content of the resource if it is of type file 
     * 
     * @return CmsResource The created resource.
     *
     * @throws CmsException will be thrown for missing propertyinfos, for worng propertydefs
     * or if the filename is not valid. The CmsException will also be thrown, if the
     * user has not the rights for this resource.
     */
    public CmsResource importResource(CmsUser currentUser, CmsProject currentProject,
                                       String newResourceName,
                                       int resourceType, Map propertyinfos, int launcherType,
                                       String launcherClassname,
                                       String ownername, String groupname, int accessFlags,
                                       long lastmodified, byte[] filecontent)
        throws CmsException;
/**
 * Creates a project.
 *
 * <B>Security</B>
 * Only the users which are in the admin or projectleader-group are granted.
 *
 * Changed: added the parent id
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param name The name of the project to read.
 * @param description The description for the new project.
 * @param group the group to be set.
 * @param managergroup the managergroup to be set.
 * @param parentId the parent project
 * @throws CmsException Throws CmsException if something goes wrong.
 */
public CmsProject createProject(CmsUser currentUser, CmsProject currentProject, String name, String description, String groupname, String managergroupname) throws com.opencms.core.CmsException;

/**
 * Creates a project.
 *
 * <B>Security</B>
 * Only the users which are in the admin or projectleader-group are granted.
 *
 * Changed: added the project type
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param name The name of the project to read.
 * @param description The description for the new project.
 * @param group the group to be set.
 * @param managergroup the managergroup to be set.
 * @param projecttype the project type (normal or temporary)
 * @throws CmsException Throws CmsException if something goes wrong.
 */
public CmsProject createProject(CmsUser currentUser, CmsProject currentProject, String name, String description, String groupname, String managergroupname, int projecttype) throws com.opencms.core.CmsException;

    /**
      * Creates a new project for task handling.
      *
      * @param owner User who creates the project
      * @param projectname Name of the project
      * @param projectType Type of the Project
      * @param role Usergroup for the project
      * @param timeout Time when the Project must finished
      * @param priority Priority for the Project
      *
      * @return The new task project
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public CmsTask createProject(CmsUser currentUser, String projectname, int projectType,
                                    String roleName, long timeout,
                                    int priority)
         throws CmsException;
    // Methods working with properties and propertydefinitions

    /**
     * Creates a project for the direct publish.
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group of the current project are granted.
     *
     * Changed: added the project type
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the project to read.
     * @param description The description for the new project.
     * @param group the group to be set.
     * @param managergroup the managergroup to be set.
     * @param project type the type of the project
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createDirectPublishProject(CmsUser currentUser, CmsProject currentProject, String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException;

    /**
     * Creates a project for the temporary files.
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * Changed: added the project type
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createTempfileProject(CmsObject cms, CmsUser currentUser, CmsProject currentProject) throws CmsException;

    /**
     * Creates the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the propertydefinition to overwrite.
     * @param resourcetype The name of the resource-type for the propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(CmsUser currentUser,
                                                    CmsProject currentProject,
                                                    String name,
                                                    String resourcetype)
        throws CmsException;

     /**
      * Creates a new task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param projectid The Id of the current project task of the user.
      * @param agentname User who will edit the task
      * @param rolename Usergroup for the task
      * @param taskname Name of the task
      * @param tasktype Type of the task
      * @param taskcomment Description of the task
      * @param timeout Time when the task must finished
      * @param priority Id for the priority
      *
      * @return A new Task Object
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName,
                                 String taskname, String taskcomment, int tasktype,
                                 long timeout, int priority)
         throws CmsException;
     /**
      * Creates a new task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param agent User who will edit the task
      * @param role Usergroup for the task
      * @param taskname Name of the task
      * @param taskcomment Description of the task
      * @param timeout Time when the task must finished
      * @param priority Id for the priority
      *
      * @return A new Task Object
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */

     public CmsTask createTask(CmsUser currentUser, CmsProject currentProject,
                                 String agentName, String roleName,
                                 String taskname, String taskcomment,
                                 long timeout, int priority)
         throws CmsException;
    /**
     * Deletes all propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The name of the resource of which the propertyinformations
     * have to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(CmsUser currentUser,
                                          CmsProject currentProject,
                                          String resource)
        throws CmsException;
    /**
     * Deletes a file in the Cms.<br>
     *
     * A file can only be deleteed in an offline project.
     * A file is deleted by setting its state to DELETED (3). <br>
     *
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path of the file.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteFile(CmsUser currentUser, CmsProject currentProject,
                           String filename)
        throws CmsException;
     /**
     * Deletes a folder in the Cms.<br>
     *
     * Only folders in an offline Project can be deleted. A folder is deleted by
     * setting its state to DELETED (3). <br>
     *
     * In its current implmentation, this method can ONLY delete empty folders.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and write this resource and all subresources</li>
     * <li>the resource is not locked</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param foldername The complete path of the folder.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteFolder(CmsUser currentUser, CmsProject currentProject,
                             String foldername)
        throws CmsException;

    /**
     * Undeletes a resource in the Cms.<br>
     *
     * A resource can only be undeleted in an offline project.
     * A resource is undeleted by setting its state to CHANGED (1). <br>
     *
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The complete path of the resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void undeleteResource(CmsUser currentUser, CmsProject currentProject,
                           String filename)
        throws CmsException;
    /**
     * Delete a group from the Cms.<BR/>
     * Only groups that contain no subgroups can be deleted.
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param delgroup The name of the group that is to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void deleteGroup(CmsUser currentUser, CmsProject currentProject,
                            String delgroup)
        throws CmsException;
    /**
     * Deletes a project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the project to be published.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteProject(CmsUser currentUser, CmsProject currentProject,
                              int id)
        throws CmsException;
    /**
     * Deletes a propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The name of the resource of which the propertyinformation
     * has to be read.
     * @param property The propertydefinition-name of which the propertyinformation has to be set.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProperty(CmsUser currentUser, CmsProject currentProject,
                                      String resource, String property)
        throws CmsException;
    /**
     * Delete the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the propertydefinition to read.
     * @param resourcetype The name of the resource type for which the
     * propertydefinition is valid.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deletePropertydefinition(CmsUser currentUser, CmsProject currentProject,
                                     String name, String resourcetype)
        throws CmsException;
    /**
     * Deletes a user from the Cms.
     *
     * Only a adminstrator can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param userId The Id of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteUser(CmsUser currentUser, CmsProject currentProject,
                           CmsUUID userId)
        throws CmsException;
    /**
     * Deletes a user from the Cms.
     *
     * Only a adminstrator can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteUser(CmsUser currentUser, CmsProject currentProject,
                           String username)
        throws CmsException;
    /**
     * Deletes a web user from the Cms.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param userId The Id of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteWebUser(CmsUser currentUser, CmsProject currentProject,
                           CmsUUID userId)
        throws CmsException;
     /**
     * Destroys the resource borker and required modules and connections.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void destroy()
        throws CmsException;
     /**
      * Ends a task from the Cms.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The ID of the task to end.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void endTask(CmsUser currentUser, CmsProject currentProject, int taskid)
         throws CmsException;
    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folders from which should be exported
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportPaths, CmsObject cms)
        throws CmsException;
    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folders from which should be exported
     * @param includeSystem, desides if to include the system resources to the export.
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportPaths, CmsObject cms, boolean includeSystem, boolean excludeUnchanged)
        throws CmsException;

    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folders from which should be exported
     * @param cms the cms-object to use for the export.
     * @param includeSystem, desides if to include the system resources to the export.
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param contentAge Max age of content to be exported (timestamp)
     * @param report the cmsReport to handle the log messages.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportPaths, CmsObject cms, boolean includeSystem, boolean excludeUnchanged, boolean exportUserdata, long contentAge, I_CmsReport report)
        throws CmsException;

    /**
     * Exports channels and moduledata to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportChannels the names (absolute Path) of channels from which should be exported
     * @param exportModules the names of modules from which should be exported
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportModuledata(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportChannels, String[] exportModules, CmsObject cms, I_CmsReport report)
        throws CmsException;

    /**
     * Creates a static export of a Cmsresource in the filesystem
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param cms the cms-object to use for the export.
     * @param startpoints the startpoints for the export.
     * @param report the cmsReport to handle the log messages.
     *
     * @throws CmsException if operation was not successful.
     */
    public void exportStaticResources(CmsUser currentUser, CmsProject currentProject,
                 CmsObject cms, Vector startpoints, Vector projectResources, Vector allExportedLinks,
                 CmsPublishedResources changedResources, I_CmsReport report) throws CmsException ;

    /**
     * Creates a static export in the filesystem. This method is used only
     * on a slave system in a cluster. The Vector is generated in the static export
     * on the master system (in the Vector allExportdLinks), so in this method the
     * database must not be updated.
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param cms the cms-object to use for the export.
     * @param linksToExport all links that where exported by the master OpenCms.
     *
     * @throws CmsException if operation was not successful.
     */
    public void exportStaticResources(CmsUser currentUser, CmsProject currentProject,
                 CmsObject cms, Vector linksToExport) throws CmsException ;

     /**
      * Forwards a task to a new user.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to forward.
      * @param newRole The new Group for the task
      * @param newUser The new user who gets the task.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void forwardTask(CmsUser currentUser, CmsProject currentProject, int taskid,
                             String newRoleName, String newUserName)
         throws CmsException;
    /**
     * Returns all projects, which are owned by the user or which are accessible
     * for the group of the user.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return a Vector of projects.
     */
     public Vector getAllAccessibleProjects(CmsUser currentUser,
                                            CmsProject currentProject)
         throws CmsException;
    /**
     * Returns all projects, which are owned by the user or which are manageable
     * for the group of the user.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return a Vector of projects.
     */
     public Vector getAllManageableProjects(CmsUser currentUser,
                                            CmsProject currentProject)
         throws CmsException;

    /**
     * Returns a Vector with all projects from history
     *
     * @return Vector with all projects from history.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public Vector getAllBackupProjects() throws CmsException;


    /**
     * Returns a Vector with all export links
     *
     * @return Vector (Strings) with all export links.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public Vector getAllExportLinks() throws CmsException;

    /**
     * Returns a Vector with all I_CmsResourceTypes.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * Returns a Hashtable with all I_CmsResourceTypes.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Hashtable getAllResourceTypes(CmsUser currentUser,
                                         CmsProject currentProject)
        throws CmsException;
    /**
     * Returns informations about the cache.
     *
     * @return a hashtable with informations about the cache.
     */
    public Hashtable getCacheInfo();
    /**
     * Returns all child groups of a group<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupname The name of the group.
     * @return groups A Vector of all child groups or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getChild(CmsUser currentUser, CmsProject currentProject,
                           String groupname)
        throws CmsException ;
    /**
     * Returns all child groups of a group<P/>
     * This method also returns all sub-child groups of the current group.
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupname The name of the group.
     * @return groups A Vector of all child groups or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getChilds(CmsUser currentUser, CmsProject currentProject,
                            String groupname)
        throws CmsException ;
    // Method to access the configuration

    /**
     * Method to access the configurations of the properties-file.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The Configurations of the properties-file.
     */
    public Configurations getConfigurations(CmsUser currentUser, CmsProject currentProject);
    /**
     * Returns the list of groups to which the user directly belongs to<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getDirectGroupsOfUser(CmsUser currentUser, CmsProject currentProject,
                                        String username)
        throws CmsException;
     /**
     * Returns a Vector with all files of a folder.<br>
     *
     * Files of a folder can be read from an offline Project and the online Project.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param foldername the complete path to the folder.
     *
     * @return subfiles A Vector with all subfiles for the overgiven folder.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getFilesInFolder(CmsUser currentUser, CmsProject currentProject,
                                   String foldername)
        throws CmsException;

     /**
     * Returns a Vector with all files of a folder.<br>
     *
     * Files of a folder can be read from an offline Project and the online Project.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param foldername the complete path to the folder.
     * @param includeDeleted Include if the folder is marked as deleted
     *
     * @return subfiles A Vector with all subfiles for the overgiven folder.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getFilesInFolder(CmsUser currentUser, CmsProject currentProject,
                                   String foldername, boolean includeDeleted)
        throws CmsException;

/**
 * Returns a Vector with all resource-names that have set the given property to the given value.
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param foldername the complete path to the folder.
 * @param propertydef, the name of the propertydefinition to check.
 * @param property, the value of the property for the resource.
 *
 * @return Vector with all names of resources.
 *
 * @throws CmsException Throws CmsException if operation was not succesful.
 */
public Vector getFilesWithProperty(CmsUser currentUser, CmsProject currentProject, String propertyDefinition, String propertyValue) throws CmsException;
    /**
     * This method can be called, to determine if the file-system was changed
     * in the past. A module can compare its previosly stored number with this
     * returned number. If they differ, a change was made.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemChanges(CmsUser currentUser, CmsProject currentProject);
    /**
     * This method can be called, to determine if the file-system was changed(only Folders)
     * in the past. A module can compare its previosly stored number with this
     * returned number. If they differ, a change was made.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemFolderChanges(CmsUser currentUser, CmsProject currentProject);
    /**
     * Returns a Vector with the complete folder-tree for this project.<br>
     *
     * Subfolders can be read from an offline project and the online project. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param rootName The name of the root, e.g. /default/vfs
     *
     * @return subfolders A Vector with the complete folder-tree for this project.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getFolderTree(CmsUser currentUser, CmsProject currentProject, String rootName)
        throws CmsException;
    /**
     * Returns all groups<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return users A Vector of all existing groups.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getGroups(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Returns a list of groups of a user.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsUser currentUser, CmsProject currentProject,
                                  String username)
        throws CmsException;
    /**
     * Returns the parent group of a group<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupname The name of the group.
     * @return group The parent group or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup getParent(CmsUser currentUser, CmsProject currentProject,
                                String groupname)
        throws CmsException ;
    /**
     * Returns the parent resource of a resouce.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsResource getParentResource(CmsUser currentUser, CmsProject currentProject,
                                         String resourcename)
        throws CmsException;
    /**
     * Checks which Group can read the resource and all the parent folders.
     *
     * @param projectid the project to check the permission.
     * @param res The resource name to be checked.
     * @return The Group Id of the Group which can read the resource.
     *          null for all Groups and
     *          Admingroup for no Group.
     */
    public String getReadingpermittedGroup(int projectId, String resource) throws CmsException;
    /**
     * Gets the Registry.<BR/>
     *
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param cms The actual CmsObject.
     * @throws Throws CmsException if access is not allowed.
     */

     public I_CmsRegistry getRegistry(CmsUser currentUser, CmsProject currentProject, CmsObject cms)
        throws CmsException;
    /**
     * Returns a Vector with the subresources for a folder.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param folder The name of the folder to get the subresources from.
     *
     * @return subfolders A Vector with resources.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesInFolder(CmsUser currentUser, CmsProject currentProject, String folder)
        throws CmsException;

   /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(CmsUser currentUser, CmsProject currentProject, String propertyDefinition,
                                           String propertyValue, int resourceType) throws CmsException;

   /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(CmsUser currentUser, CmsProject currentProject,
            String propertyDefinition) throws CmsException;

    /**
     * Returns a CmsResourceTypes.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourceType the id of the resourceType to get.
     *
     * Returns a CmsResourceTypes.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public I_CmsResourceType getResourceType(CmsUser currentUser,
                                             CmsProject currentProject,
                                             int resourceType)
        throws CmsException;
    /**
     * Returns a CmsResourceTypes.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourceType the name of the resource to get.
     *
     * Returns a CmsResourceTypes.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public I_CmsResourceType getResourceType(CmsUser currentUser,
                                             CmsProject currentProject,
                                             String resourceType)
        throws CmsException;
    /**
     * Returns a Vector with all subfolders.<br>
     *
     * Subfolders can be read from an offline project and the online project. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param foldername the complete path to the folder.
     *
     * @return subfolders A Vector with all subfolders for the given folder.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getSubFolders(CmsUser currentUser, CmsProject currentProject,
                                String foldername)
        throws CmsException;

    /**
     * Returns a Vector with all subfolders.<br>
     *
     * Subfolders can be read from an offline project and the online project. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param foldername the complete path to the folder.
     * @param includeDeleted Include if the folder is marked as deleted
     *
     * @return subfolders A Vector with all subfolders for the given folder.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getSubFolders(CmsUser currentUser, CmsProject currentProject,
                                String foldername, boolean includeDeleted)
        throws CmsException;

     /**
      * Get a parameter value for a task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task.
      * @param parname Name of the parameter.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public String getTaskPar(CmsUser currentUser, CmsProject currentProject,
                              int taskid, String parname)
         throws CmsException;
     /**
     * Get the template task id fo a given taskname.
     *
     * @param taskname Name of the Task
     *
     * @return id from the task template
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int getTaskType(String taskname)
        throws CmsException;
    /**
     * Returns all users<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Returns all users from a given type<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param type The type of the users.
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsUser currentUser, CmsProject currentProject, int type)
        throws CmsException;
     /**
     * Returns all users from a given type that start with a specified string<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param type The type of the users.
     * @param namestart The filter for the username
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsUser currentUser, CmsProject currentProject, int type, String namestart)
        throws CmsException;
    /**
     * Returns a list of users in a group.<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupname The name of the group to list users from.
     * @return Vector of users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsersOfGroup(CmsUser currentUser, CmsProject currentProject,
                                  String groupname)
        throws CmsException;

    /**
     * Gets all users with a certain Lastname.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param Lastname      the start of the users lastname
     * @param UserType      webuser or systemuser
     * @param UserStatus    enabled, disabled
     * @param wasLoggedIn   was the user ever locked in?
     * @param nMax          max number of results
     *
     * @return the users.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getUsersByLastname(CmsUser currentUser,
                                     CmsProject currentProject, String Lastname,
                                     int UserType, int UserStatus,
                                     int wasLoggedIn, int nMax)
        throws CmsException;
    /**
     * Imports a import-resource (folder or zipfile) to the cms.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param cms the cms-object to use for the import.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void importFolder(CmsUser currentUser,  CmsProject currentProject, String importFile, String importPath, CmsObject cms)
        throws CmsException;
    // Methods working with database import and export

    /**
     * Imports a import-resource (folder or zipfile) to the cms.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param cms the cms-object to use for the import.
     * @param report A report object to provide the loggin messages.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void importResources(CmsUser currentUser,  CmsProject currentProject, String importFile, String importPath, CmsObject cms, I_CmsReport report)
        throws CmsException;
    // Internal ResourceBroker methods


    /**
     * Initializes the resource broker and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void init(Configurations config, CmsVfsAccess vfsAccess, CmsUserAccess userAccess, CmsDbAccess dbAccess)
        throws CmsException, Exception;
    /**
     * Determines, if the users current group is the admin-group.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return true, if the users current group is the admin-group,
     * else it returns false.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isAdmin(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Determines, if the users may manage a project.<BR/>
     * Only the manager of a project may publish it.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return true, if the may manage this project.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Determines, if the users current group is the projectleader-group.<BR/>
     * All projectleaders can create new projects, or close their own projects.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return true, if the users current group is the projectleader-group,
     * else it returns false.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isProjectManager(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Returns the user, who had locked the resource.<BR/>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param user The user who wants to lock the file.
     * @param project The project in which the resource will be used.
     * @param resource The resource.
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
                              CmsResource resource)
        throws CmsException;
    /**
     * Returns the user, who had locked the resource.<BR/>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param user The user who wants to lock the file.
     * @param project The project in which the resource will be used.
     * @param resource The complete path to the resource.
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
                              String resource)
        throws CmsException;
    /**
     * Locks a resource.<br>
     *
     * Only a resource in an offline project can be locked. The state of the resource
     * is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * A user can lock a resource, so he is the only one who can write this
     * resource. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is not locked by another user</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The complete path to the resource to lock.
     * @param force If force is true, a existing locking will be oberwritten.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     * It will also be thrown, if there is a existing lock
     * and force was set to false.
     */
    public void lockResource(CmsUser currentUser, CmsProject currentProject,
                             String resourcename, boolean force)
        throws CmsException;
    //  Methods working with user and groups

    /**
     * Logs a user into the Cms, if the password is correct.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user to be returned.
     * @param password The password of the user to be returned.
     * @return the logged in user.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser loginUser(CmsUser currentUser, CmsProject currentProject,
                               String username, String password)
        throws CmsException;
    /**
     * Logs a web user into the Cms, if the password is correct.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user to be returned.
     * @param password The password of the user to be returned.
     * @return the logged in user.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser loginWebUser(CmsUser currentUser, CmsProject currentProject,
                                String username, String password)
        throws CmsException;
    /**
     * Moves the file.
     *
     * This operation includes a copy and a delete operation. These operations
     * are done with their security-checks.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param source The complete path of the sourcefile.
     * @param destination The complete path of the destinationfile.
     *
     * @throws CmsException will be thrown, if the file couldn't be moved.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public void moveFile(CmsUser currentUser, CmsProject currentProject,
                         String source, String destination)
        throws CmsException;
    /**
     * Returns the onlineproject. This is the default project. All anonymous
     * (CmsUser callingUser, or guest) user will see the rersources of this project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return the onlineproject object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject onlineProject(CmsUser currentUser,
                                    CmsProject currentProject)
        throws CmsException;
    /**
     * Publishes a project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param cms The CmsObject.
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the project to be published.
     * @param report A report object to provide the loggin messages.
     * @return CmsPublishedResources The object includes the vectors with changed resources.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPublishedResources publishProject(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                                 int id, I_CmsReport report)
        throws CmsException;
    /**
     * Reads the agent of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read the agent from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readAgent(CmsUser currentUser, CmsProject currentProject,
                               CmsTask task)
        throws CmsException ;
     /**
     * Reads all file headers of a file in the OpenCms.<BR>
     * This method returns a vector with all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the filecontent.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return Vector of file headers read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public Vector readAllFileHeaders(CmsUser currentUser, CmsProject currentProject,
                                      String filename)
         throws CmsException;

     /**
     * Reads all file headers of a file in the OpenCms.<BR>
     * This method returns a vector with the histroy of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the filecontent.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return Vector of file headers read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public Vector readAllFileHeadersForHist(CmsUser currentUser, CmsProject currentProject,
                                      String filename)
         throws CmsException;

    /**
     * select all projectResources from an given project
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException;
        
    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject,
                                             int resourcetype)
        throws CmsException;
    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourcetype The name of the resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject,
                                         String resourcetype)
        throws CmsException;
    // Methods working with system properties

/****************     methods for link management            ****************************/

/**
 * deletes all entrys in the link table that belong to the pageId
 *
 * @param pageId The resourceId (offline) of the page whose links should be deleted
 */
public void deleteLinkEntrys(CmsUUID pageId)throws CmsException;

/**
 * creates a link entry for each of the link targets in the linktable.
 *
 * @param pageId The resourceId (offline) of the page whose liks should be traced.
 * @param linkTarget A vector of strings (the linkdestinations).
 */
public void createLinkEntrys(CmsUUID pageId, Vector linkTargets)throws CmsException;

/**
 * returns a Vector (Strings) with the link destinations of all links on the page with
 * the pageId.
 *
 * @param pageId The resourceId (offline) of the page whose liks should be read.
 */
public Vector readLinkEntrys(CmsUUID pageId)throws CmsException;

/**
 * deletes all entrys in the online link table that belong to the pageId
 *
 * @param pageId The resourceId (online) of the page whose links should be deleted
 */
public void deleteOnlineLinkEntrys(CmsUUID pageId)throws CmsException;

/**
 * creates a link entry for each of the link targets in the online linktable.
 *
 * @param pageId The resourceId (online) of the page whose liks should be traced.
 * @param linkTarget A vector of strings (the linkdestinations).
 */
public void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets)throws CmsException;

/**
 * returns a Vector (Strings) with the link destinations of all links on the page with
 * the pageId.
 *
 * @param pageId The resourceId (online) of the page whose liks should be read.
 */
public Vector readOnlineLinkEntrys(CmsUUID pageId)throws CmsException;

/**
 * serches for broken links in the online project.
 *
 * @return A Vector with a CmsPageLinks object for each page containing broken links
 *          this CmsPageLinks object contains all links on the page withouth a valid target.
 */
public Vector getOnlineBrokenLinks() throws CmsException;

/**
 * checks a project for broken links that would appear if the project is published.
 *
 * @param projectId
 * @param changed A vecor (of CmsResources) with the changed resources in the project.
 * @param deleted A vecor (of CmsResources) with the deleted resources in the project.
 * @param newRes A vecor (of CmsResources) with the new resources in the project.
 */
 public void getBrokenLinks(int projectId, I_CmsReport report, Vector changed, Vector deleted, Vector newRes)throws CmsException;

/**
 * When a project is published this method aktualises the online link table.
 *
 * @param deleted A Vector (of CmsResources) with the deleted resources of the project.
 * @param changed A Vector (of CmsResources) with the changed resources of the project.
 * @param newRes A Vector (of CmsResources) with the newRes resources of the project.
 */
public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;

/****************  end  methods for link management          ****************************/

    /**
     * Reads the export-path for the system.
     * This path is used for db-export and db-import.
     *
     * <B>Security:</B>
     * All users are granted.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return the exportpath.
     */
    public String readExportPath(CmsUser currentUser, CmsProject currentProject)
        throws CmsException ;

    /**
     * Reads a exportrequest from the Cms.<BR/>
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsExportLink readExportLink(String filename)
        throws CmsException;

    /**
     * Reads a exportrequest without the dependencies from the Cms.<BR/>
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsExportLink readExportLinkHeader(String request)
        throws CmsException;
    /**
     * Writes an exportlink to the Cms.
     *
     * @param link the cmsexportlink object to write.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLink(CmsExportLink link) throws CmsException;

    /**
     * Deletes an exportlink in the database.
     *
     * @param link the name of the link
     */
    public void deleteExportLink(String link) throws CmsException;

    /**
     * Deletes an exportlink in the database.
     *
     * @param link the cmsExportLink object to delete.
     */
    public void deleteExportLink(CmsExportLink link) throws CmsException ;

    /**
     * Reads all export links that depend on the resource.
     * @param res. The resourceName() of the resources that has changed (or the String
     *              that describes a contentdefinition).
     * @return a Vector(of Strings) with the linkrequest names.
     */
     public Vector getDependingExportLinks(Vector res) throws CmsException;

    /**
     * Sets one exportLink to procecced.
     *
     * @param link the cmsexportlink.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLinkProcessedState(CmsExportLink link) throws CmsException ;

    /**
     * Reads a file from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     * */
     public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
                             String filename)
        throws CmsException;

    /**
     * Reads a file from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     * @param includeDeleted If true the deleted file will be returned, too
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     * */
     public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
                             String filename, boolean includeDeleted)
        throws CmsException;
    /**
     * Gets the known file extensions (=suffixes)
     *
     * <B>Security:</B>
     * All users are granted access<BR/>
     *
     * @param currentUser The user who requested this method, not used here
     * @param currentProject The current project of the user, not used here
     *
     * @return Hashtable with file extensions as Strings
     */

    public Hashtable readFileExtensions(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
      /**
     * Reads a file header a previous project of the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param projectId The id of the project to read the file from.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsResource readFileHeader(CmsUser currentUser,
                                       CmsProject currentProject,
                                       int projectId,
                                       String filename)
         throws CmsException;
     /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsResource readFileHeader(CmsUser currentUser,
                                         CmsProject currentProject, String filename)
         throws CmsException;
     /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param filename The name of the file to be read.
     * @param includeDeleted If false then throw exception if the file is marked as deleted
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsResource readFileHeader(CmsUser currentUser,
                                         CmsProject currentProject, String filename,
                                         boolean includeDeleted)
         throws CmsException;

    /**
     * Reads a file header from the history of the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * The file header is read from the backup resources.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param versionId The versionid of the file.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsBackupResource readFileHeaderForHist(CmsUser currentUser,
                                                    CmsProject currentProject,
                                                    int versionId,
                                                    String filename)
         throws CmsException;

    /**
     * Reads a file from the history of the Cms.<BR/>
     * The reading includes the filecontent. <br>
     *
     * The file is read from the backup resources.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param versionId The versionid of the file.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
     public CmsBackupResource readFileForHist(CmsUser currentUser,
                                              CmsProject currentProject,
                                              int versionId,
                                              String filename)
         throws CmsException;

    /**
     * Reads all file headers for a project from the Cms.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param projectId The id of the project to read the resources for.
     *
     * @return a Vector of resources.
     *
     * @throws CmsException will be thrown, if the file couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public Vector readFileHeaders(CmsUser currentUser, CmsProject currentProject,
                                  int projectId)
        throws CmsException;
      /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param folder The complete path of the folder to be read.
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
                                String folder)
        throws CmsException ;        

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param folder The complete pathname of the folder to be read.
     * @param includeDeleted Include the folder if it is marked as deleted
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
                                String folder, boolean includeDeleted)
        throws CmsException ;

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param folderid The id of the folder to be read.
     * @param includeDeleted Include the folder if it is marked as deleted
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
                                CmsUUID folderId, boolean includeDeleted)
        throws CmsException ;
     /**
      * Reads all given tasks from a user for a project.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param projectId The id of the Project in which the tasks are defined.
      * @param owner Owner of the task.
      * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
      * @param orderBy Chooses, how to order the tasks.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readGivenTasks(CmsUser currentUser, CmsProject currentProject,
                                  int projectId, String ownerName, int taskType,
                                  String orderBy, String sort)
         throws CmsException;
    /**
     * Reads the group of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject,
                                CmsProject project)
        throws CmsException;
    /**
     * Reads the group of a resource from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject,
                               CmsResource resource)
        throws CmsException ;
    /**
     * Reads the group (role) of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read from.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject,
                               CmsTask task)
        throws CmsException ;
    /**
     * Returns a group object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupname The name of the group that is to be read.
     * @return Group.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject,
                                String groupname)
        throws CmsException;
    /**
     * Returns a group object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupid The id of the group that is to be read.
     * @return Group.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject,
                              CmsUUID groupId)
        throws CmsException;
    /**
     * Reads the managergroup of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readManagerGroup(CmsUser currentUser, CmsProject currentProject,
                                       CmsProject project)
        throws CmsException;
    /**
     * Gets the MimeTypes.
     * The Mime-Types will be returned.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the mime-types.
     */
    public Hashtable readMimeTypes(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;
    /**
     * Reads the original agent of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read the original agent from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOriginalAgent(CmsUser currentUser, CmsProject currentProject,
                                       CmsTask task)
        throws CmsException ;
    /**
     * Reads the owner of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject,
                               CmsProject project)
        throws CmsException;
    /**
     * Reads the owner of a resource from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject,
                               CmsResource resource)
        throws CmsException ;
    /**
     * Reads the owner (initiator) of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read the owner from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject,
                               CmsTask task)
        throws CmsException;
    /**
     * Reads the owner of a tasklog from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, CmsTaskLog log)
        throws CmsException ;
    /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the project to read.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
     public CmsProject readProject(CmsUser currentUser, CmsProject currentProject,
                                   int id)
         throws CmsException ;
     /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param res The resource to read the project of.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
     public CmsProject readProject(CmsUser currentUser, CmsProject currentProject,
                                   CmsResource res)
         throws CmsException ;
    /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read the project of.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
     public CmsProject readProject(CmsUser currentUser, CmsProject currentProject,
                                     CmsTask task)
         throws CmsException ;

    /**
     * Reads all file headers of a project from the Cms.
     *
     * @param projectId the id of the project to read the file headers for.
     * @param filter The filter for the resources (all, new, changed, deleted, locked)
     *
     * @return a Vector of resources.
     *
     */
    public Vector readProjectView(CmsUser currentUser, CmsProject currentProject,
                                  int projectId, String filter) throws CmsException;

    /**
     * Reads the backup of a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param versionId The versionId of the project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
     public CmsBackupProject readBackupProject(CmsUser currentUser, CmsProject currentProject, int versionId)
         throws CmsException;

     /**
      * Reads log entries for a project.
      *
      * @param projectId The id of the projec for tasklog to read.
      * @return A Vector of new TaskLog objects
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readProjectLogs(CmsUser currentUser, CmsProject currentProject,
                                   int projectId)
         throws CmsException;

    /**
     * Looks up a specified property with optional direcory upward cascading.<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read the resource.
     * 
     * @param currentUser the current user
     * @param currentProject the current project of the user
     * @param resource the resource to look up the property for
     * @param siteroot the site root where to stop the cascading
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if false not (ie. normal 
     *   property lookup)
     * @return the value of the property found, <code>null</code> if nothing was found
     * @throws CmsException in case there where problems reading the property
     */
    public String readProperty(CmsUser currentUser, CmsProject currentProject, String resource, String siteRoot, String property, boolean search)
    throws CmsException;

    /**
     * Looks up a specified property with optional direcory upward cascading,
     * a default value will be returned if the property is not found on the
     * resource (or it's parent folders in case search is set to <code>true</code>).<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read the resource.
     * 
     * @param currentUser the current user
     * @param currentProject the current project of the user
     * @param resource the resource to look up the property for
     * @param siteroot the site root where to stop the cascading
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if <code>false</code> not (ie. normal 
     *   property lookup)
     * @param propertyDefault a default value that will be returned if
     *   the property was not found on the selected resource
     * @return the value of the property found, if nothing was found the value of the <code>propertyDefault</code> parameter is returned
     * @throws CmsException in case there where problems reading the property
     */
    public String readProperty(CmsUser currentUser, CmsProject currentProject, String resource, String siteRoot, String property, boolean search, String propertyDefault)
    throws CmsException;
        
    /**
     * Looks up all properties for a resource with optional direcory upward cascading.<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read the resource.
     * 
     * @param currentUser the current user
     * @param currentProject the current project of the user
     * @param resource the resource to look up the property for
     * @param siteroot the site root where to stop the cascading
     * @param search if <code>true</code>, the properties will also be looked up on all parent folders
     *   and the results will be merged, if <code>false</code> not (ie. normal property lookup)
     * @return Map of Strings representing all properties of the resource
     * @throws CmsException in case there where problems reading the properties
     */
    public Map readProperties(CmsUser currentUser, CmsProject currentProject, String resource, String siteRoot, boolean search)
    throws CmsException; 
        
    /**
     * Reads a definition for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param name The name of the propertydefinition to read.
     * @param resourcetype The name of the resource type for which the propertydefinition
     * is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition readPropertydefinition(CmsUser currentUser,
                                                  CmsProject currentProject,
                                                  String name, String resourcetype)
        throws CmsException;
/**
 * Insert the method's description here.
 * Creation date: (09-10-2000 09:30:47)
 * @return java.util.Vector
 * @param project com.opencms.file.CmsProject
 * @throws com.opencms.core.CmsException The exception description.
 */
public Vector readResources(CmsProject project) throws com.opencms.core.CmsException;
     /**
      * Read a task by id.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param id The id for the task to read.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public CmsTask readTask(CmsUser currentUser, CmsProject currentProject,
                               int id)
         throws CmsException;
     /**
      * Reads log entries for a task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The task for the tasklog to read .
      * @return A Vector of new TaskLog objects
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readTaskLogs(CmsUser currentUser, CmsProject currentProject,
                                int taskid)
         throws CmsException;
     /**
      * Reads all tasks for a project.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param projectId The id of the Project in which the tasks are defined. Can be null for all tasks
      * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
      * @param orderBy Chooses, how to order the tasks.
      * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readTasksForProject(CmsUser currentUser, CmsProject currentProject,
                                       int projectId, int tasktype,
                                       String orderBy, String sort)
         throws CmsException;
     /**
      * Reads all tasks for a role in a project.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param projectId The id of the Project in which the tasks are defined.
      * @param user The user who has to process the task.
      * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
      * @param orderBy Chooses, how to order the tasks.
      * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readTasksForRole(CmsUser currentUser, CmsProject currentProject,
                                    int projectId, String roleName, int tasktype,
                                    String orderBy, String sort)
         throws CmsException;
     /**
      * Reads all tasks for a user in a project.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param projectId The id of the Project in which the tasks are defined.
      * @param role The user who has to process the task.
      * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
      * @param orderBy Chooses, how to order the tasks.
      * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public Vector readTasksForUser(CmsUser currentUser, CmsProject currentProject,
                                    int projectId, String userName, int tasktype,
                                    String orderBy, String sort)
         throws CmsException;
        /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the user that is to be read.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsUser currentUser, CmsProject currentProject,
                              CmsUUID userId)
        throws CmsException ;
    /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user that is to be read.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsUser currentUser, CmsProject currentProject,
                              String userName)
        throws CmsException;
     /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user that is to be read.
     * @param type The type of the user.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsUser currentUser, CmsProject currentProject,
                              String username,int type)
        throws CmsException ;
    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The username of the user that is to be read.
     * @param password The password of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsUser currentUser, CmsProject currentProject,
                              String username, String password)
        throws CmsException;
    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The username of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsUser currentUser, CmsProject currentProject,
                              String username)
        throws CmsException;
    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The username of the user that is to be read.
     * @param password The password of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsUser currentUser, CmsProject currentProject,
                              String username, String password)
        throws CmsException;
     /**
      * Reaktivates a task from the Cms.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to accept.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void reaktivateTask(CmsUser currentUser, CmsProject currentProject,
                                int taskId)
         throws CmsException;
    /**
     * Sets a new password only if the user knows his recovery-password.
     *
     * All users can do this if he knows the recovery-password.<P/>
     *
     * <B>Security:</B>
     * All users can do this if he knows the recovery-password.<P/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @param recoveryPassword The recovery password.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void recoverPassword(CmsObject cms,CmsUser currentUser, CmsProject currentProject,
                            String username, String recoveryPassword, String newPassword)
        throws CmsException;
    /**
     * Removes a user from a group.
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user that is to be removed from the group.
     * @param groupname The name of the group.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void removeUserFromGroup(CmsUser currentUser, CmsProject currentProject,
                                    String username, String groupname)
        throws CmsException;
    /**
     * Renames the file to a new name. <br>
     *
     * Rename can only be done in an offline project. To rename a file, the following
     * steps have to be done:
     * <ul>
     * <li> Copy the file with the oldname to a file with the new name, the state
     * of the new file is set to NEW (2).
     * <ul>
     * <li> If the state of the original file is UNCHANGED (0), the file content of the
     * file is read from the online project. </li>
     * <li> If the state of the original file is CHANGED (1) or NEW (2) the file content
     * of the file is read from the offline project. </li>
     * </ul>
     * </li>
     * <li> Set the state of the old file to DELETED (3). </li>
     * </ul>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param oldname The complete path to the resource which will be renamed.
     * @param newname The new name of the resource (CmsUser callingUser, No path information allowed).
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void renameFile(CmsUser currentUser, CmsProject currentProject,
                           String oldname, String newname)
        throws CmsException;
    /**
     * This method loads old sessiondata from the database. It is used
     * for sessionfailover.
     *
     * @param oldSessionId the id of the old session.
     * @return the old sessiondata.
     */
    public Hashtable restoreSession(String oldSessionId)
        throws CmsException;

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param currentUser The current user
     * @param currentProject The current project
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(CmsUser currentUser, CmsProject currentProject,
                                   int versionId, String filename) throws CmsException;

     /**
      * Set a new name for a task
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to set the percentage.
      * @param name The new name value
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void setName(CmsUser currentUser, CmsProject currentProject,
                         int taskId, String name)
         throws CmsException;
    /**
     * Sets a new parent-group for an already existing group in the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param groupName The name of the group that should be written to the Cms.
     * @param parentGroupName The name of the parentGroup to set, or null if the parent
     * group should be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void setParentGroup(CmsUser currentUser, CmsProject currentProject,
                               String groupName, String parentGroupName)
        throws CmsException;
    /**
     * Sets the password for a user.
     *
     * Only a adminstrator can do this.<P/>
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                            String username, String newPassword)
        throws CmsException;
    /**
     * Sets the password for a user.
     *
     * Every user who knows the username and the password can do this.<P/>
     *
     * <B>Security:</B>
     * Users, who knows the username and the password are granted.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @param oldPassword The new password.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                            String username, String oldPassword, String newPassword)
        throws CmsException;
     /**
      * Set priority of a task
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to set the percentage.
      * @param new priority value
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void setPriority(CmsUser currentUser, CmsProject currentProject,
                             int taskId, int priority)
         throws CmsException;
    /**
     * Sets the recovery password for a user.
     *
     * Only a adminstrator or the curretuser can do this.<P/>
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     * Current users can change their own password.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param username The name of the user.
     * @param oldPassword The old password.
     * @param newPassword The recovery password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setRecoveryPassword(CmsObject cms, CmsUser currentUser, CmsProject currentProject,
                                    String username, String password, String newPassword)
        throws CmsException;
     /**
      * Set a Parameter for a task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task.
      * @param parname Name of the parameter.
      * @param parvalue Value if the parameter.
      *
      * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void setTaskPar(CmsUser currentUser, CmsProject currentProject,
                           int taskid, String parname, String parvalue)
         throws CmsException;
     /**
      * Set timeout of a task
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task to set the percentage.
      * @param new timeout value
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void setTimeout(CmsUser currentUser, CmsProject currentProject,
                            int taskId, long timeout)
         throws CmsException;
    /**
     * This method stores sessiondata into the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @param isNew determines, if the session is new or not.
     * @return data the sessionData.
     */
    public void storeSession(String sessionId, Hashtable sessionData)
        throws CmsException;

    /**
     * Undo all changes in the resource, restore the online file.
     *
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourceName The name of the resource to be restored.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void undoChanges(CmsUser currentUser, CmsProject currentProject, String resourceName)
        throws CmsException;

    /**
     * Unlocks all resources in this project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param id The id of the project to be published.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void unlockProject(CmsUser currentUser, CmsProject currentProject, int id)
        throws CmsException;
    /**
     * Unlocks a resource.<br>
     *
     * Only a resource in an offline project can be unlock. The state of the resource
     * is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * Only the user who locked a resource can unlock it.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user had locked the resource before</li>
     * </ul>
     *
     * @param user The user who wants to lock the file.
     * @param project The project in which the resource will be used.
     * @param resourcename The complete path to the resource to lock.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void unlockResource(CmsUser currentUser,CmsProject currentProject,
                               String resourcename)
        throws CmsException;
    /**
     * Checks if a user is member of a group.<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param callingUser The user who wants to use this method.
     * @param nameuser The name of the user to check.
     * @param groupname The name of the group to check.
     * @return True or False
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public boolean userInGroup(CmsUser currentUser, CmsProject currentProject,
                               String username, String groupname)
        throws CmsException;
    /**
     * Writes the export-path for the system.
     * This path is used for db-export and db-import.
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param mountpoint The mount point in the Cms filesystem.
     */
    public void writeExportPath(CmsUser currentUser, CmsProject currentProject, String path)
        throws CmsException ;
     /**
     * Writes a file to the Cms.<br>
     *
     * A file can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who own this file.
     * @param currentProject The project in which the resource will be used.
     * @param file The name of the file to write.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeFile(CmsUser currentUser, CmsProject currentProject,
                          CmsFile file)
        throws CmsException ;
        
     /**
     * Writes a resource and its properties to the Cms.<br>
     *
     * A resource can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * <li>the user is the owner of the resource or administrator<li>
     * </ul>
     *
     * @param currentUser The current user.
     * @param currentProject The project in which the resource will be used.
     * @param resourcename The name of the resource to write.
     * @param properties The properties of the resource.
     * @param username The name of the new owner of the resource
     * @param groupname The name of the new group of the resource
     * @param accessFlags The new accessFlags of the resource
     * @param resourceType The new type of the resource
     * @param filecontent The new filecontent of the resource
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeResource(CmsUser currentUser, CmsProject currentProject,
                               String resourcename, Map properties,
                               String username, String groupname, int accessFlags,
                               int resourceType, byte[] filecontent)
        throws CmsException;
        
    /**
     * Writes the file extensions
     *
     * <B>Security:</B>
     * Users, which are in the group "Administrators" are authorized.<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param extensions Holds extensions as keys and resourcetypes (Stings) as values
     */

    public void writeFileExtensions(CmsUser currentUser, CmsProject currentProject,
                                    Hashtable extensions)
        throws CmsException;
     /**
     * Writes a fileheader to the Cms.<br>
     *
     * A file can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param currentUser The user who own this file.
     * @param currentProject The project in which the resource will be used.
     * @param file The file to write.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeFileHeader(CmsUser currentUser, CmsProject currentProject,
                                CmsFile file)
        throws CmsException;
     /**
     * Writes an already existing group in the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param group The group that should be written to the Cms.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void writeGroup(CmsUser currentUser, CmsProject currentProject,
                           CmsGroup group)
        throws CmsException;
    /**
     * Writes a couple of propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The name of the resource of which the propertyinformation
     * has to be read.
     * @param propertyinfos A Hashtable with propertydefinition- propertyinfo-pairs as strings.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperties(CmsUser currentUser, CmsProject currentProject,
                                      String resource, Map propertyinfos)
        throws CmsException;
    /**
     * Writes a propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The name of the resource of which the propertyinformation has
     * to be read.
     * @param property The propertydefinition-name of which the propertyinformation has to be set.
     * @param value The value for the propertyinfo to be set.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperty(CmsUser currentUser, CmsProject currentProject,
                                     String resource, String property, String value)
        throws CmsException;
    /**
     * Updates the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param propertydef The propertydef to be deleted.
     *
     * @return The propertydefinition, that was written.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition writePropertydefinition(CmsUser currentUser,
                                                   CmsProject currentProject,
                                                   CmsPropertydefinition propertydef)
        throws CmsException;
     /**
      * Writes a new user tasklog for a task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task .
      * @param comment Description for the log
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void writeTaskLog(CmsUser currentUser, CmsProject currentProject,
                              int taskid, String comment)
         throws CmsException ;
     /**
      * Writes a new user tasklog for a task.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param currentUser The user who requested this method.
      * @param currentProject The current project of the user.
      * @param taskid The Id of the task .
      * @param comment Description for the log
      * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
      *
      * @throws CmsException Throws CmsException if something goes wrong.
      */
     public void writeTaskLog(CmsUser currentUser, CmsProject currentProject,
                              int taskid, String comment, int taskType)
         throws CmsException;
    /**
     * Updates the user information.<BR/>
     *
     * Only the administrator can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param user The  user to be updated.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeUser(CmsUser currentUser, CmsProject currentProject,
                          CmsUser user)
        throws CmsException;
    /**
     * Updates the user information of a web user.<BR/>
     *
     * Only a web user can be updated this way.<P/>
     *
     * <B>Security:</B>
     * Only users of the user type webuser can be updated this way.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param user The  user to be updated.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsUser currentUser, CmsProject currentProject,
                          CmsUser user)
        throws CmsException;

    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The new project-id
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(int projectId, String resourcename, CmsUser currentUser) throws CmsException;

    /**
     * Check if the history is enabled
     * 
     * @param cms The CmsObject
     * @return boolean Is true if history is enabled
     */
    public boolean isHistoryEnabled(CmsObject cms);

    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupVersionId();

    /**
     * Creates a backup of the published project
     *
     * @param project The project in which the resource was published.
     * @param projectresources The resources of the project
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     * @param userId The id of the user who had published the project
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */

    public void backupProject(int projectId, int versionId,
                              long publishDate, CmsUser currentUser) throws CmsException;

    /**
     * Gets the Crontable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the crontable.
     */
    public String readCronTable(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;

    /**
     * Writes the Crontable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the crontable.
     */
    public void writeCronTable(CmsUser currentUser, CmsProject currentProject, String crontable)
        throws CmsException;


    /**
     * Method to encrypt the passwords.
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    public String digest(String value);

    /**
     * This is the port the workplace access is limited to. With the opencms.properties
     * the access to the workplace can be limited to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.
     * @return the portnumber or -1 if no port is set.
     */
    public int getLimitedWorkplacePort();

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param currentUser The current user
     * @param currentProject The current project
     * @param userId The id of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsUser currentUser, CmsProject currentProject, CmsUUID userId, int userType) throws CmsException;

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param currentUser The current user
     * @param currentProject The current project
     * @param username The name of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsUser currentUser, CmsProject currentProject, String username, int userType) throws CmsException;

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param currentUser The current user
     * @param currentProject The current project
     * @param user The CmsUser object of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsUser currentUser, CmsProject currentProject, CmsUser user, int userType) throws CmsException;

    /**
     * Returns a Vector with the resources that contains the given part in the resourcename.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and view this resource</li>
     * </ul>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resourcename A part of resourcename
     *
     * @return subfolders A Vector with resources.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector readResourcesLikeName(CmsUser currentUser, CmsProject currentProject, String resourcename) throws CmsException;

    /**
     * Reads all files from the Cms, that are of the given type.<BR/>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFilesByType(CmsUser currentUser, CmsProject currentProject, int projectId, int resourcetype) throws CmsException;

    /**
     * Writes the Linkchecktable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param linkchecktable The hashtable that contains the links that were not reachable
     *
     * @return the linkchecktable.
     */
    public void writeLinkCheckTable(CmsUser currentUser, CmsProject currentProject, Hashtable linkchecktable)
        throws CmsException;

    /**
     * Gets the Linkchecktable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the linkchecktable.
     */
    public Hashtable readLinkCheckTable(CmsUser currentUser, CmsProject currentProject)
        throws CmsException;    
        
    /**
     * Deletes the versions from the backup tables that are older then the given weeks
     * 
     * @param cms The CmsObject for reading the registry
     * @param currentUser The current user
     * @param currentProject The currently used project
     * @param weeks The number of weeks: the max age of the remaining versions
     * @return int The oldest remaining version
     */
    public int deleteBackups(CmsObject cms, CmsUser currentUser, CmsProject currentProject, int weeks) 
        throws CmsException;        
        
    /**
     * Checks, if the user may read this resource and if it is visible to him.
     * NOTE: If the ressource is in the project you never have to fallback.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param resource The resource to check.
     *
     * @return weather the user has access, or not.
     */
    public boolean accessReadVisible(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException;
    
    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users that have read and view access are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getVisibleResourcesWithProperty(CmsUser currentUser, CmsProject currentProject, String propertyDefinition,
                                           String propertyValue, int resourceType) throws CmsException;
                                           
    /**
     * Rebuilds the internal datastructure to join links with their targets. Each target saves the total count of
     * links pointing to itself. Each links saves the ID of it's target.
     * 
     * @param cms the user's CmsObject instance
     * @param theUser the user
     * @param theProject the project
     * @param theReport the report to print the output
     * @return an ArrayList with the resources which were identified as broken links
     * @see com.opencms.file.genericSql.CmsDbAccess#updateResourceFlags
     * @see com.opencms.file.genericSql.CmsDbAccess#fetchAllVfsLinks
     * @see com.opencms.file.genericSql.CmsDbAccess#fetchResourceID
     * @see com.opencms.file.genericSql.CmsDbAccess#updateAllResourceFlags
     */      
    public ArrayList joinLinksToTargets(CmsObject cms, CmsUser theUser, CmsProject theProject, I_CmsReport theReport)
        throws CmsException; 
        
    /**
     * Fetches all VFS links pointing to a given resource name.
     * 
     * @param cms the current user's CmsObject instance
     * @param theProject the current project
     * @param theResourceName the name of the resource of which the VFS links are fetched
     * @return an ArrayList with the resource names of the fetched VFS links
     * @throws CmsException
     */        
    public ArrayList fetchVfsLinksForResource( CmsUser theUser, CmsProject theProject, String theResourceName ) 
        throws CmsException;
        
    /**
     * Decrement the VFS link counter for a resource. 
     * 
     * @param theProject the current project
     * @param theResourceName the name of the resource for which the link count is decremented
     * @throws CmsException
     * @return the current link count of the specified resource
     */         
    public int decrementLinkCountForResource( CmsProject theProject, String theResourceName ) 
        throws CmsException;     
        
    /**
     * Increment the VFS link counter for a resource. 
     * 
     * @param theProject the current project
     * @param theResourceName the name of the resource for which the link count is incremented
     * @throws CmsException
     * @return the current link count of the specified resource
     */        
    public int incrementLinkCountForResource( CmsProject theProject, String theResourceName ) 
        throws CmsException; 
        
    /**
     * Save the ID of the target resource for a VFS link.
     * The target ID is saved in the RESOURCE_FLAGS table attribute.
     * 
     * @param theProject the current project
     * @param theLinkResourceName the resource name of the VFS link
     * @param theTargetResourceName the name of the link's target resource
     * @throws CmsException
     */         
    public void linkResourceToTarget( CmsProject theProject, String theLinkResourceName, String theTargetResourceName ) 
        throws CmsException;
                            
    public CmsVfsAccess getVfsAccess();     
    
    public I_CmsUserAccess getUserAccess();                          
}
