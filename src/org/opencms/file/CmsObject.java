/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsObject.java,v $
 * Date   : $Date: 2004/05/26 09:37:57 $
 * Version: $Revision: 1.36 $
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
 
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsSessionInfoManager;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This pivotal class provides all authorized access to the OpenCms resources.<p>
 * 
 * It encapsulates user identification and permissions.
 * Think of it as the initialized "Shell" to access the OpenCms resources.
 * Every call to a method here will be checked for user permissions
 * according to the context the CmsObject instance was created with.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.36 $
 */
public class CmsObject {

    /**
     * The request context.
     */
    private CmsRequestContext m_context;

    /**
     * The driver manager to access the cms.
     */
    private CmsDriverManager m_driverManager;

    /**
     * Method that can be invoked to find out all currently logged in users.
     */
    private CmsSessionInfoManager m_sessionStorage;

    /**
     * The default constructor.
     */
    public CmsObject() {
        // noop
    }

    /**
     * Initializes this CmsObject with the provided user context and database connection.<p>
     *
     * @param driverManager the driver manager to access the database
     * @param context the request context that contains the user authentification
     * @param sessionStorage the core session
     */
    public void init(
            CmsDriverManager driverManager,
            CmsRequestContext context,
            CmsSessionInfoManager sessionStorage
    ) {
        m_sessionStorage = sessionStorage;
        m_driverManager = driverManager;
        m_context = context;
    }
    
    /**
     * Accept a task from the Cms.
     *
     * @param taskId the id of the task to accept.
     *
     * @throws CmsException if operation was not successful.
     */
    public void acceptTask(int taskId) throws CmsException {
        m_driverManager.acceptTask(m_context, taskId);
    }

    /**
     * Checks if the user can access the project.
     *
     * @param projectId the id of the project.
     * @return <code>true</code>, if the user may access this project; <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful.
     */
    public boolean accessProject(int projectId) throws CmsException {
        return (m_driverManager.accessProject(m_context, projectId));
    }

    /**
     * Adds a file extension to the list of known file extensions.
     * <p>
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a file extension.
     *
     * @param extension a file extension like "html","txt" etc.
     * @param resTypeName name of the resource type associated with the extension.
     *
     * @throws CmsException if operation was not successful.
     */

    public void addFileExtension(String extension, String resTypeName) throws CmsException {
        m_driverManager.addFileExtension(m_context, extension, resTypeName);
    }

    /**
     * Adds a user to the Cms by import.
     * <p>
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a user.
     *
     * @param id the id of the user
     * @param name the new name for the user.
     * @param password the new password for the user.
     * @param recoveryPassword the new password for the user.
     * @param description the description for the user.
     * @param firstname the firstname of the user.
     * @param lastname the lastname of the user.
     * @param email the email of the user.
     * @param flags the flags for a user (e.g. C_FLAG_ENABLED).
     * @param additionalInfos a Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param defaultGroup the default groupname for the user.
     * @param address the address of the user.
     * @param section the section of the user.
     * @param type the type of the user.
     *
     * @return a <code>CmsUser</code> object representing the added user.
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsUser addImportUser(String id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, int flags, Hashtable additionalInfos, String defaultGroup, String address, String section, int type) throws CmsException {
        return m_driverManager.addImportUser(m_context, id, name, password, recoveryPassword, description, firstname, lastname, email, flags, additionalInfos, defaultGroup, address, section, type);
    }

    /**
     * Returns the name of a resource with the complete site root name,
     * (e.g. /default/vfs/index.html) by adding the currently set site root prefix.<p>
     *
     * @param resourcename the resource name
     * @return the resource name including site root
     */
    private String addSiteRoot(String resourcename) {
        return getRequestContext().addSiteRoot(resourcename);
    }

    /**
     * Adds a user to the OpenCms user table.<p>
     * 
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a user.<p>
     *
     * @param name the new name for the user
     * @param password the password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user
     * @return the newly created user
     * @throws CmsException if something goes wrong
     */
    public CmsUser addUser(String name, String password, String group, String description, Hashtable additionalInfos) throws CmsException {
        return m_driverManager.addUser(m_context, name, password, group, description, additionalInfos);
    }

    /**
     * Adds a user to a group.<p>
     * 
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a user to a group.
     *
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * @throws CmsException if something goes wrong
     */
    public void addUserToGroup(String username, String groupname) throws CmsException {
        m_driverManager.addUserToGroup(m_context, username, groupname);
    }

    /**
     * Adds a web user to the OpenCms user table.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     * Moreover, a web user can be created by any user, the intention being that
     * a "Guest"user can create a personalized account from himself.
     *
     * @param name the name for the user
     * @param password the password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user
     * @return the newly created user
     * @throws CmsException if something goes wrong
     */
    public CmsUser addWebUser(String name, String password, String group, String description, Hashtable additionalInfos) throws CmsException {
        return m_driverManager.addWebUser(name, password, group, description, additionalInfos);
    }

    /**
     * Creates a backup of the published project.<p>
     *
     * @param projectId The id of the project in which the resource was published
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void backupProject(int projectId, int versionId, long publishDate) throws CmsException {
        CmsProject backupProject = m_driverManager.readProject(projectId);
        m_driverManager.backupProject(m_context, backupProject, versionId, publishDate);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user)
     * @param principalName name of the principal
     * @param allowedPermissions bitset of allowed permissions
     * @param deniedPermissions bitset of denied permissions
     * @param flags flags
     * @throws CmsException if something goes wrong
     */
    public void chacc(String resourceName, String principalType, String principalName, int allowedPermissions, int deniedPermissions, int flags) throws CmsException {
        CmsResource res = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsAccessControlEntry acEntry = null;
        I_CmsPrincipal principal = null;

        if ("group".equals(principalType.toLowerCase())) {
            principal = readGroup(principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), allowedPermissions, deniedPermissions, flags);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_GROUP);
        } else if ("user".equals(principalType.toLowerCase())) {
            principal = readUser(principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), allowedPermissions, deniedPermissions, flags);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_USER);
        }

        m_driverManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (group or user)
     * @param principalName name of the principal
     * @param permissionString the permissions in the format ((+|-)(r|w|v|c|i))*
     * @throws CmsException if something goes wrong
     */
    public void chacc(String resourceName, String principalType, String principalName, String permissionString) throws CmsException {
        CmsResource res = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsAccessControlEntry acEntry = null;
        I_CmsPrincipal principal = null;

        if ("group".equals(principalType.toLowerCase())) {
            principal = readGroup(principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), permissionString);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_GROUP);
        } else if ("user".equals(principalType.toLowerCase())) {
            principal = readUser(principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), permissionString);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_USER);
        }

        m_driverManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly.<p>
     *
     * @param projectId The new project-id
     * @param resourcename The name of the resource to change
     * @throws CmsException if something goes wrong
     */
    public void changeLockedInProject(int projectId, String resourcename) throws CmsException {
        // must include files marked as deleted when publishing
        getResourceType(readFileHeader(resourcename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).changeLockedInProject(this, projectId, resourcename);
    }

    /**
     * Changes the type of the user
     *
     * @param userId The id of the user to change
     * @param userType The new type of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsUUID userId, int userType) throws CmsException {
        m_driverManager.changeUserType(m_context, userId, userType);
    }

    /**
     * Changes the type of the user to webusertype
     *
     * @param username The name of the user to change
     * @param userType The new type of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(String username, int userType) throws CmsException {
        m_driverManager.changeUserType(m_context, username, userType);
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
    public void chtype(String filename, int newType) throws CmsException {
        getResourceType(readFileHeader(filename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).chtype(this, filename, newType);
    }

    /**
     * Copies a file.
     *
     * @param source the complete path of the sourcefile.
     * @param destination the complete path of the destinationfolder.
     *
     * @throws CmsException if the file couldn't be copied, or the user
     * has not the appropriate rights to copy the file.
     *
     * @deprecated Use copyResource instead.
     */
    public void copyFile(String source, String destination) throws CmsException {
        copyResource(source, destination, false, true, I_CmsConstants.C_COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a folder.
     *
     * @param source the complete path of the sourcefolder.
     * @param destination the complete path of the destinationfolder.
     *
     * @throws CmsException if the folder couldn't be copied, or if the
     * user has not the appropriate rights to copy the folder.
     *
     * @deprecated Use copyResource instead.
     */
    public void copyFolder(String source, String destination) throws CmsException {
        copyResource(source, destination, false, false, I_CmsConstants.C_COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a file.
     *
     * @param source the complete path of the sourcefile.
     * @param destination the complete path of the destinationfolder.
     *
     * @throws CmsException if the file couldn't be copied, or the user
     * has not the appropriate rights to copy the file.
     */
    public void copyResource(String source, String destination) throws CmsException {
        getResourceType(readFileHeader(source, CmsResourceFilter.IGNORE_EXPIRATION).getType()).copyResource(this, source, destination, false, true, I_CmsConstants.C_COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a file.
     *
     * @param source the complete path of the sourcefile.
     * @param destination the complete path of the destinationfolder.
     * @param keepFlags <code>true</code> if the copy should keep the source file's flags,
     *        <code>false</code> if the copy should get the user's default flags.
     * @param lockCopy indicates if the copy should left locked after operation
     * @param copyMode mode of the copy operation, described how to handle linked resourced during copy.
     * Possible values are: 
     * <ul>
     * <li>C_COPY_AS_NEW</li>
     * <li>C_COPY_AS_SIBLING</li>
     * <li>C_COPY_PRESERVE_SIBLING</li>
     * </ul>
     * @throws CmsException if the file couldn't be copied, or the user
     * has not the appropriate rights to copy the file.
     */
    public void copyResource(String source, String destination, boolean keepFlags, boolean lockCopy, int copyMode) throws CmsException {
        getResourceType(readFileHeader(source, CmsResourceFilter.IGNORE_EXPIRATION).getType()).copyResource(this, source, destination, keepFlags, lockCopy, copyMode);
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
    public void copyResourceToProject(String resource) throws CmsException {
        getResourceType(readFileHeader(resource, CmsResourceFilter.IGNORE_EXPIRATION).getType()).copyResourceToProject(this, resource);
    }

    /**
     * Counts the locked resources in a project.
     *
     * @param id the id of the project
     * @return the number of locked resources in this project.
     *
     * @throws CmsException if operation was not successful.
     */
    public int countLockedResources(int id) throws CmsException {
        return m_driverManager.countLockedResources(m_context, id);
    }

    /**
     * Counts the locked resources within a folder.<p>
     *
     * @param foldername the name of the folder
     * @return the number of locked resources in this folder
     *
     * @throws CmsException if operation was not successful.
     */
    public int countLockedResources(String foldername) throws CmsException {
        return m_driverManager.countLockedResources(m_context, addSiteRoot(foldername));
    }

    /**
     * Copies access control entries of a given resource to another resource.<p>
     * 
     * @param sourceName the name of the resource of which the access control entries are copied
     * @param destName the name of the resource to which the access control entries are applied
     * @throws CmsException if something goes wrong
     */
    public void cpacc(String sourceName, String destName) throws CmsException {
        CmsResource source = readFileHeader(sourceName);
        CmsResource dest = readFileHeader(destName);
        m_driverManager.copyAccessControlEntries(m_context, source, dest);
    }

    /**
     * Creates a new channel.
     *
     * @param parentChannel the complete path to the channel in which the new channel
     * will be created.
     * @param newChannelName the name of the new channel.
     *
     * @return folder a <code>CmsFolder</code> object representing the newly created channel.
     *
     * @throws CmsException if the channelname is not valid, or if the user has not the appropriate rights to create
     * a new channel.
     *
     */
    public CmsFolder createChannel(String parentChannel, String newChannelName) throws CmsException {
        getRequestContext().saveSiteRoot();
        try {
            setContextToCos();
            Hashtable properties = new Hashtable();
            int newChannelId = org.opencms.db.CmsDbUtil.nextId(I_CmsConstants.C_TABLE_CHANNELID);
            properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, newChannelId + "");
            return (CmsFolder)createResource(parentChannel, newChannelName, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, properties);
        } finally {
            getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * Creates a new file with the given content and resourcetype.<br>
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
     *
     * @deprecated Use createResource instead.
     */
    public CmsFile createFile(String folder, String filename, byte[] contents, int type) throws CmsException {
        return (CmsFile)createResource(folder, filename, type, null, contents);
    }
    /**
     * Creates a new file with the given content and resourcetype.
     *
     * @param folder the complete path to the folder in which the file will be created.
     * @param filename the name of the new file.
     * @param contents the contents of the new file.
     * @param type the resourcetype of the new file.
     * @param properties A Hashtable of properties, that should be set for this file.
     * The keys for this Hashtable are the names for properties, the values are
     * the values for the properties.
     *
     * @return file a <code>CmsFile</code> object representing the newly created file.
     *
     * @throws CmsException or if the resourcetype is set to folder.
     * The CmsException is also thrown, if the filename is not valid or if the user
     * has not the appropriate rights to create a new file.
     *
     * @deprecated Use createResource instead.
     */
    public CmsFile createFile(String folder, String filename, byte[] contents, int type, Hashtable properties) throws CmsException {
        return (CmsFile)createResource(folder, filename, type, properties, contents);
    }

    /**
     * Creates a new folder.
     *
     * @param folder the complete path to the folder in which the new folder
     * will be created.
     * @param newFolderName the name of the new folder.
     *
     * @return folder a <code>CmsFolder</code> object representing the newly created folder.
     *
     * @throws CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
     * a new folder.
     *
     * @deprecated Use createResource instead.
     */
    public CmsFolder createFolder(String folder, String newFolderName) throws CmsException {
        return (CmsFolder)createResource(folder, newFolderName, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
    }
    /**
     * Adds a new group to the Cms.<p>
     * 
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a new group.
     *
     * @param name the name of the new group
     * @param description the description of the new group
     * @param flags the flags for the new group
     * @param parent the parent group
     *
     * @return a <code>CmsGroup</code> object representing the newly created group.
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsGroup createGroup(String name, String description, int flags, String parent) throws CmsException {
        return (m_driverManager.createGroup(m_context, name, description, flags, parent));
    }

    /**
     * Adds a new group to the Cms.<p>
     * 
     * <b>Security:</b>
     * Only members of the group administrators are allowed to add a new group.
     * 
     * @param id the id of the group
     * @param name the name of the new group
     * @param description the description of the new group
     * @param flags the flags for the new group
     * @param parent the parent group
     * @return a <code>CmsGroup</code> object representing the newly created group.
     * @throws CmsException if something goes wrong
     */
    public CmsGroup createGroup(String id, String name, String description, int flags, String parent) throws CmsException {
        return m_driverManager.createGroup(m_context, id, name, description, flags, parent);
    }

    /**
     * Creates a new project.
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @return the created project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(String name, String description, String groupname, String managergroupname) throws CmsException {
        CmsProject newProject = m_driverManager.createProject(m_context, name, description, groupname, managergroupname, I_CmsConstants.C_PROJECT_TYPE_NORMAL);
        return (newProject);
    }

    /**
     * Creates a new project.
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @param projecttype the type of the project (normal or temporary)
     * @return the created project
     * @throws CmsException if operation was not successful.
     */
    public CmsProject createProject(String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException {
        CmsProject newProject = m_driverManager.createProject(m_context, name, description, groupname, managergroupname, projecttype);
        return (newProject);
    }

    /**
     * Creates the property-definition for a resource type.
     *
     * @param name the name of the property-definition to overwrite
     * @param resourcetype the name of the resource-type for the property-definition
     * @return the new property definition
     * 
     * @throws CmsException if operation was not successful.
     */
    public CmsPropertydefinition createPropertydefinition(String name, int resourcetype) throws CmsException {
        return (m_driverManager.createPropertydefinition(m_context, name, resourcetype));
    }

    /**
     * Creates a new resource.<p>
     * 
     * @param newResourceName name of the resource
     * @param type type of the resource
     * @param properties properties of the resource
     * @param contents content of the resource
     * @param parameter additional parameter
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(String newResourceName, int type, Map properties, byte[] contents, Object parameter) throws CmsException {
        return getResourceType(type).createResource(this, newResourceName, properties, contents, parameter);
    }

    /**
     * Creates a new resource.<p>
     * 
     * @param folder name of the parent folder
     * @param name name of the resource
     * @param type type of the resource
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(String folder, String name, int type) throws CmsException {
        return createResource(folder + name, type, new HashMap(), new byte[0], null);
    }

    /**
     * Creates a new resource.<p>
     * 
     * @param folder name of the parent folder
     * @param name name of the resource
     * @param type type of the resource
     * @param properties properties of the resource
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(String folder, String name, int type, Map properties) throws CmsException {
        return createResource(folder + name, type, properties, new byte[0], null);
    }

    /**
     * Creates a new resource.<p>
     * 
     * @param folder name of the parent folder
     * @param name name of the resource
     * @param type type of the resource
     * @param properties properties of the resource
     * @param contents content of the resource
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(String folder, String name, int type, Map properties, byte[] contents) throws CmsException {
        return createResource(folder + name, type, properties, contents, null);
    }

    /**
      * Creates a new task.<p>
      * 
      * <B>Security:</B>
      * All users can create a new task.
      *
      * @param projectid the Id of the current project task of the user
      * @param agentName the User who will edit the task
      * @param roleName a Usergroup for the task
      * @param taskname a Name of the task
      * @param tasktype the type of the task
      * @param taskcomment a description of the task
      * @param timeout the time when the task must finished
      * @param priority the Id for the priority of the task
      * @return the created task
      * @throws CmsException if something goes wrong
      */
    public CmsTask createTask(int projectid, String agentName, String roleName, String taskname, String taskcomment, int tasktype, long timeout, int priority) throws CmsException {
        return m_driverManager.createTask(m_context.currentUser(), projectid, agentName, roleName, taskname, taskcomment, tasktype, timeout, priority);
    }

    /**
      * Creates a new task.<p>
      * 
      * <B>Security:</B>
      * All users can create a new task.
      * 
      * @param agentName the User who will edit the task
      * @param roleName a Usergroup for the task
      * @param taskname the name of the task
      * @param timeout the time when the task must finished
      * @param priority the Id for the priority of the task
      * @return the created task
      * @throws CmsException if something goes wrong
      */
    public CmsTask createTask(String agentName, String roleName, String taskname, long timeout, int priority) throws CmsException {
        return (m_driverManager.createTask(m_context, agentName, roleName, taskname, timeout, priority));
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @return the created project for the temporary workplace files
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject() throws CmsException {
        return m_driverManager.createTempfileProject(m_context);
    }

    /**
     * Creates a new sibling of the target resource.<p>
     * 
     * @param siblingName name of the new link
     * @param targetName name of the target
     * @param siblingProperties additional properties of the link resource
     * @return the new link resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createSibling(String siblingName, String targetName, List siblingProperties) throws CmsException {
        return m_driverManager.createSibling(m_context, addSiteRoot(siblingName), addSiteRoot(targetName), siblingProperties, true);
    }
    
    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * You may specify which whether just structure or resource property values should
     * be deleted, or both of them.<p>
     *
     * @param resourcename the name of the resource for which all properties should be deleted.
     * @param deleteOption determines which property values should be deleted
     * @throws CmsException if operation was not successful
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_VALUES
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_RESOURCE_VALUES
     */
    public void deleteAllProperties(String resourcename, int deleteOption) throws CmsException {
        m_driverManager.deleteAllProperties(m_context, addSiteRoot(resourcename), deleteOption);
    }

    /**
     * Deletes the versions from the backup tables that are older then the given timestamp  and/or number of remaining versions.<p>
     * 
     * The number of verions always wins, i.e. if the given timestamp would delete more versions than given in the
     * versions parameter, the timestamp will be ignored.
     * Deletion will delete file header, content and properties.
     * 
     * @param timestamp timestamp which defines the date after which backup resources must be deleted
     * @param versions the number of versions per file which should kept in the system.
     * @param report the report for output logging
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteBackups(long timestamp, int versions, I_CmsReport report) throws CmsException {
       m_driverManager.deleteBackups(m_context, timestamp, versions, report);
    }

    /**
     * Deletes a folder.
     * <br>
     * This is a very complex operation, because all sub-resources may be
     * deleted too.
     *
     * @param foldername the complete path of the folder.
     *
     * @throws CmsException if the folder couldn't be deleted, or if the user
     * has not the rights to delete this folder.
     *
     */
    public void deleteEmptyFolder(String foldername) throws CmsException {
        m_driverManager.deleteFolder(m_context, addSiteRoot(foldername));
    }

    /**
     * Deletes a file.
     *
     * @param filename the complete path of the file.
     *
     * @throws CmsException if the file couldn't be deleted, or if the user
     * has not the appropriate rights to delete the file.
     *
     * @deprecated Use deleteResource instead.
     */
    public void deleteFile(String filename) throws CmsException {
        deleteResource(filename, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
    }

    /**
     * Deletes a folder.
     * <br>
     * This is a very complex operation, because all sub-resources may be
     * deleted too.
     *
     * @param foldername the complete path of the folder.
     *
     * @throws CmsException if the folder couldn't be deleted, or if the user
     * has not the rights to delete this folder.
     *
     * @deprecated Use deleteResource instead.
     */
    public void deleteFolder(String foldername) throws CmsException {
        deleteResource(foldername, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
    }

    /**
     * Deletes a group.
     * <p>
     * <b>Security:</b>
     * Only the admin user is allowed to delete a group.
     *
     * @param delgroup the name of the group.
     * @throws CmsException  if operation was not successful.
     */
    public void deleteGroup(String delgroup) throws CmsException {
        m_driverManager.deleteGroup(m_context, delgroup);
    }

    /**
     * Deletes a project.<p>
     *
     * @param id the id of the project to delete.
     *
     * @throws CmsException if operation was not successful.
     */
    public void deleteProject(int id) throws CmsException {
        m_driverManager.deleteProject(m_context, id);
    }

    /**
     * Deletes a property for a file or folder.<p>
     *
     * @param resourcename the name of a resource for which the property should be deleted
     * @param key the name of the property
     * @throws CmsException if something goes wrong
     * @deprecated use {@link #writePropertyObject(String, CmsProperty)} instead
     */
    public void deleteProperty(String resourcename, String key) throws CmsException {
        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(CmsProperty.C_DELETE_VALUE);
        
        m_driverManager.writePropertyObject(m_context, addSiteRoot(resourcename), property);        
    }

    /**
     * Deletes the property-definition for a resource type.<p>
     *
     * @param name the name of the property-definition to delete
     * @param resourcetype the name of the resource-type for the property-definition.
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(String name, int resourcetype) throws CmsException {
        m_driverManager.deletePropertydefinition(m_context, name, resourcetype);
    }

    /**
     * Deletes a resource.<p>
     *
     * @param filename the filename of the resource exlucing the site root
     * @param deleteOption signals how VFS links pointing to this resource should be handled 
     * @throws CmsException if the user has insufficient acces right to delete the resource
     * @see org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS
     * @see org.opencms.main.I_CmsConstants#C_DELETE_OPTION_IGNORE_SIBLINGS
     * @see org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS
     */
    public void deleteResource(String filename, int deleteOption) throws CmsException {
        getResourceType(readFileHeader(filename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).deleteResource(this, filename, deleteOption);
    }

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(String resourceName, int linkType, String linkParameter) throws CmsException {
        m_driverManager.deleteStaticExportPublishedResource(m_context, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(int linkType) throws CmsException {
        m_driverManager.deleteAllStaticExportPublishedResources(m_context, linkType);
    }
    
    
    /**
     * Deletes a user from the Cms.<p>
     * 
     * <b>Security:</b>
     * Only a admin user is allowed to delete a user.
     *
     * @param userId the Id of the user to be deleted.
     *
     * @throws CmsException if operation was not successful.
     */
    public void deleteUser(CmsUUID userId) throws CmsException {
        m_driverManager.deleteUser(m_context, userId);
    }

    /**
     * Deletes a user from the Cms.<p>
     * 
     * <b>Security:</b>
     * Only a admin user is allowed to delete a user.
     *
     * @param username the name of the user to be deleted.
     *
     * @throws CmsException if operation was not successful.
     */
    public void deleteUser(String username) throws CmsException {
        m_driverManager.deleteUser(m_context, username);
    }

    /**
     * Deletes a web user from the Cms.<p>
     *
     * @param userId the id of the user to be deleted.
     *
     * @throws CmsException if operation was not successful.
     */
    public void deleteWebUser(CmsUUID userId) throws CmsException {
        m_driverManager.deleteWebUser(userId);
    }

    /**
     * Method to encrypt the passwords.<p>
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    public String digest(String value) {
        return m_driverManager.digest(value);
    }

    /**
     * Changes the project id of a resource to the current project of the user.<p>
     *
     * @param resourcename the resource to change
     * @throws CmsException if something goes wrong
     */
    protected void doChangeLockedInProject(String resourcename) throws CmsException {
        m_driverManager.changeLockedInProject(m_context, addSiteRoot(resourcename));
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
    protected void doChtype(String filename, int newType) throws CmsException {
        m_driverManager.chtype(m_context, addSiteRoot(filename), newType);
    }

    /**
     * Copies a file.
     *
     * @param source the complete path of the sourcefile.
     * @param destination the complete path of the destinationfolder.
     * @param lockCopy flag to lock the copied resource
     * @param copyMode mode of the copy operation, described how to handle linked resourced during copy.
     * Possible values are: 
     * <ul>
     * <li>C_COPY_AS_NEW</li>
     * <li>C_COPY_AS_SIBLING</li>
     * <li>C_COPY_PRESERVE_SIBLING</li>
     * </ul>
     * @throws CmsException if the file couldn't be copied, or the user
     * has not the appropriate rights to copy the file.
     */
    protected void doCopyFile(String source, String destination, boolean lockCopy, int copyMode) throws CmsException {
        m_driverManager.copyFile(m_context, addSiteRoot(source), addSiteRoot(destination), lockCopy, false, copyMode);
    }

    /**
     * Copies a folder.
     *
     * @param source the complete path of the sourcefolder.
     * @param destination the complete path of the destinationfolder.
     * @param lockCopy indicates if the copy of the folder should remain locked after operation
     * @param preserveTimestamps true if the timestamps and users of the folder should be kept
     * @throws CmsException if the folder couldn't be copied, or if the
     * user has not the appropriate rights to copy the folder.
     */
    protected void doCopyFolder(String source, String destination, boolean lockCopy, boolean preserveTimestamps) throws CmsException {
        m_driverManager.copyFolder(m_context, addSiteRoot(source), addSiteRoot(destination), lockCopy, preserveTimestamps);
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
    protected void doCopyResourceToProject(String resource) throws CmsException {
        m_driverManager.copyResourceToProject(m_context, addSiteRoot(resource));
    }

    /**
     * Creates a new file with the given content and resourcetype.<br>
     *
     * @param newFileName the name of the new file.
     * @param contents the contents of the new file.
     * @param type the resourcetype of the new file.
     *
     * @return file a <code>CmsFile</code> object representing the newly created file.
     *
     * @throws CmsException if the resourcetype is set to folder. The CmsException is also thrown, if the
     * filename is not valid or if the user has not the appropriate rights to create a new file.
     */
    protected CmsFile doCreateFile(String newFileName, byte[] contents, String type) throws CmsException {
        CmsFile file = m_driverManager.createFile(m_context, addSiteRoot(newFileName), contents, type, Collections.EMPTY_LIST);
        return file;
    }

    /**
     * Creates a new file with the given content and resourcetype.
     *
     * @param newFileName the name of the new file.
     * @param contents the contents of the new file.
     * @param type the resourcetype of the new file.
     * @param properties A Hashtable of properties, that should be set for this file.
     * The keys for this Hashtable are the names for properties, the values are
     * the values for the properties.
     *
     * @return file a <code>CmsFile</code> object representing the newly created file.
     *
     * @throws CmsException if the wrong properties are given, or if the resourcetype is set to folder.
     * The CmsException is also thrown, if the filename is not valid or if the user
     * has not the appropriate rights to create a new file.
     */
    protected CmsFile doCreateFile(String newFileName, byte[] contents, String type, Map properties) throws CmsException {
        // avoid null-pointer exceptions
        if (properties == null) {
            properties = Collections.EMPTY_MAP;
        }
        
        // TODO new property model: creating files does not support the new property model yet
        CmsFile file = m_driverManager.createFile(m_context, addSiteRoot(newFileName), contents, type, CmsProperty.toList(properties));
        return file;
    }

    /**
     * Creates a new folder.
     *
     * @param newFolderName the name of the new folder.
     * @param properties A Hashtable of properties, that should be set for this folder.
     * The keys for this Hashtable are the names for property-definitions, the values are
     * the values for the properties.
     *
     * @return a <code>CmsFolder</code> object representing the newly created folder.
     * @throws CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
     * a new folder.
     *
     */
    protected CmsFolder doCreateFolder(String newFolderName, Map properties) throws CmsException {
        // TODO new property model: creating folders does not support the new property model yet
        CmsFolder cmsFolder = m_driverManager.createFolder(m_context, addSiteRoot(newFolderName), CmsProperty.toList(properties));
        return cmsFolder;
    }

    /**
     * Creates a new folder.
     *
     * @param folder the complete path to the folder in which the new folder
     * will be created.
     * @param newFolderName the name of the new folder.
     *
     * @return folder a <code>CmsFolder</code> object representing the newly created folder.
     *
     * @throws CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
     * a new folder.
     */
    protected CmsFolder doCreateFolder(String folder, String newFolderName) throws CmsException {
        CmsFolder cmsFolder = m_driverManager.createFolder(m_context, addSiteRoot(folder + newFolderName + I_CmsConstants.C_FOLDER_SEPARATOR), Collections.EMPTY_LIST);
        return cmsFolder;
    }

    /**
     * Deletes a file.
     *
     * @param filename the complete path of the file
     * @param deleteOption flag to delete siblings as well
     *
     * @throws CmsException if the file couldn't be deleted, or if the user
     * has not the appropriate rights to delete the file.
     */
    protected void doDeleteFile(String filename, int deleteOption) throws CmsException {
        m_driverManager.deleteFile(m_context, addSiteRoot(filename), deleteOption);
    }

    /**
     * Deletes a folder.
     * <br>
     * This is a very complex operation, because all sub-resources may be
     * deleted too.
     *
     * @param foldername the complete path of the folder.
     *
     * @throws CmsException if the folder couldn't be deleted, or if the user
     * has not the rights to delete this folder.
     */
    protected void doDeleteFolder(String foldername) throws CmsException {
        m_driverManager.deleteFolder(m_context, addSiteRoot(foldername));
    }

    /**
     * Creates a new resource.<p>
     *
     * @param resource the resource to be imported
     * @param content the content of the resource if it is of type file
     * @param properties A Hashtable of propertyinfos, that should be set for this folder
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos
     * @param destination  the name of the new resource 
     *
     * @return a <code>CmsFolder</code> object representing the newly created folder
     * @throws CmsException if the resourcename is not valid, or if the user has not the appropriate rights to create
     * a new resource
     *
     */
    protected CmsResource doImportResource(CmsResource resource, byte content[], Map properties, String destination) throws CmsException {
        // TODO new property model: the import/export does not support the new property model yet
        CmsResource cmsResource = m_driverManager.importResource(m_context, addSiteRoot(destination), resource, content, CmsProperty.toList(properties));
        return cmsResource;
    }

    /**
     * Locks the given resource.<p>
     *
     * @param resource the complete path to the resource
     * @param mode flag indicating the mode (temporary or common) of a lock
     * @throws CmsException if something goes wrong
     */
    protected void doLockResource(String resource, int mode) throws CmsException {
        m_driverManager.lockResource(m_context, addSiteRoot(resource), mode);
    }

    /**
     * Moves a file to the given destination.<p>
     *
     * @param source the complete path of the sourcefile
     * @param destination the complete path of the destinationfile
     * @throws CmsException if something goes wrong
     */
    protected void doMoveResource(String source, String destination) throws CmsException {
        m_driverManager.moveResource(m_context, addSiteRoot(source), addSiteRoot(destination));
    }

    /**
    * Moves a resource to the lost and found folder.<p>
    *
    * @param resourcename the complete path of the sourcefile
    * @param copyResource true, if the resource should be copied to its destination inside the lost+found folder
    * @return location of the moved resource
    * @throws CmsException if the user does not have the rights to move this resource,
    * or if the file couldn't be moved
    */
    protected String doCopyToLostAndFound(String resourcename, boolean copyResource) throws CmsException {
        return m_driverManager.copyToLostAndFound(m_context, addSiteRoot(resourcename), copyResource);
    }
       
    /**
     * Renames a resource.<p>
     *
     * @param oldname the complete path to the resource which will be renamed
     * @param newname the new name of the resource
     * @throws CmsException if the user has not the rights
     * to rename the resource, or if the file couldn't be renamed
     */
    protected void doRenameResource(String oldname, String newname) throws CmsException {
        m_driverManager.renameResource(m_context, addSiteRoot(oldname), newname);
    }

    /**
     * Replaces an already existing resource with new resource data.<p>
     * 
     * @param resName name of the resource
     * @param newResContent new content of the resource
     * @param newResType new type of the resource
     * @param newResProps new properties
     * @return the replaced resource
     * @throws CmsException if something goes wrong
     */
    protected CmsResource doReplaceResource(String resName, byte[] newResContent, int newResType, Map newResProps) throws CmsException {
        CmsResource res = null;

        // TODO new property model: replacing resource does not yet support the new property model
        res = m_driverManager.replaceResource(m_context, addSiteRoot(resName), newResType, CmsProperty.toList(newResProps), newResContent);
        return res;
    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param tagId The tag id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    protected void doRestoreResource(int tagId, String filename) throws CmsException {
        m_driverManager.restoreResource(m_context, tagId, addSiteRoot(filename));
    }

    /**
     * Access the driver manager underneath to change the timestamp of a resource.
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param user the user who is inserted as userladtmodified
     * @throws CmsException if something goes wrong
     */
    protected void doTouch(String resourceName, long timestamp, CmsUUID user) throws CmsException {
        m_driverManager.touch(m_context, addSiteRoot(resourceName), timestamp, user);
    }

    /**
     * Undeletes a file.
     *
     * @param filename the complete path of the file.
     *
     * @throws CmsException if the file couldn't be undeleted, or if the user
     * has not the appropriate rights to undelete the file.
     */
    protected void doUndeleteFile(String filename) throws CmsException {
        m_driverManager.undeleteResource(m_context, addSiteRoot(filename));
    }

    /**
     * Undeletes a folder.
     * <br>
     * This is a very complex operation, because all sub-resources may be
     * undeleted too.
     *
     * @param foldername the complete path of the folder.
     *
     * @throws CmsException if the folder couldn't be undeleted, or if the user
     * has not the rights to undelete this folder.
     */
    protected void doUndeleteFolder(String foldername) throws CmsException {
        m_driverManager.undeleteResource(m_context, addSiteRoot(foldername));
    }

    /**
     * Undo changes in a file.
     * <br>
     *
     * @param resource the complete path to the resource to be unlocked.
     *
     * @throws CmsException if the user has not the rights
     * to write this resource.
     */
    protected void doUndoChanges(String resource) throws CmsException {
        m_driverManager.undoChanges(m_context, addSiteRoot(resource));
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
    protected void doUnlockResource(String resource) throws CmsException {
        m_driverManager.unlockResource(m_context, addSiteRoot(resource));
    }

    /**
     * Writes a file to the VFS.<p>
     *
     * @param file the file to write.
     *
     * @throws CmsException if resourcetype is set to folder. The CmsException will also be thrown,
     * if the user has not the rights write the file.
     */
    protected void doWriteFile(CmsFile file) throws CmsException {    
        m_driverManager.writeFile(m_context, file);
    }
    
    /**
    * Writes a resource and its properties to the VFS.<p>
    *
    * @param resourcename the name of the resource to write
    * @param properties the properties of the resource
    * @param filecontent the new filecontent of the resource
    * @throws CmsException if something goes wrong
    */
    protected void doWriteResource(String resourcename, Map properties, byte[] filecontent) throws CmsException {
        // TODO new property model: the import/export does not support the new property model yet
        m_driverManager.writeResource(m_context, addSiteRoot(resourcename), CmsProperty.toList(properties), filecontent);
    }

    /**
     * Ends a task.<p>
     *
     * @param taskid the ID of the task to end.
     *
     * @throws CmsException if operation was not successful.
     */
    public void endTask(int taskid) throws CmsException {
        m_driverManager.endTask(m_context, taskid);
    }


    /**
     * Tests if a resource with the given resourceId does already exist in the Database.<p>
     * 
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
      public boolean existsResourceId (CmsUUID resourceId) throws CmsException {
          return m_driverManager.existsResourceId(m_context, resourceId);          
      }

    /**
     * Exports a resource.
     * 
     * @param file the resource to export
     * @return the resource that was exported
     * @throws CmsException if something goes wrong
     */
    public CmsFile exportResource(CmsFile file) throws CmsException {
        return getResourceType(file.getType()).exportResource(this, file);
    }

    /**
     * Reads all siblings that point to the resource record of a specified resource name.<p>
     * 
     * @param resourcename name of a resource
     * @return List of siblings of the resource
     * @throws CmsException if something goes wrong
     */
    public List getAllVfsLinks(String resourcename) throws CmsException {
        return m_driverManager.readSiblings(m_context, addSiteRoot(resourcename), true, CmsResourceFilter.ALL);
    }

    /**
     * Reads all siblings that point to the resource record of a specified resource name,
     * excluding the specified resource from the result.<p>
     * 
     * @param resourcename name of a resource
     * @return List of siblings of the resource
     * @throws CmsException if something goes wrong 
     */
    public List getAllVfsSoftLinks(String resourcename) throws CmsException {       
        return m_driverManager.readSiblings(m_context, addSiteRoot(resourcename), false, CmsResourceFilter.ALL);
    } 

    /**
     * Fires a CmsEvent
     *
     * @param type The type of the event
     * @param data A data object that contains data used by the event listeners
     */
    private void fireEvent(int type, Object data) {
        OpenCms.fireCmsEvent(this, type, Collections.singletonMap("data", data));
    }

    /**
     * Forwards a task to a new user.
     *
     * @param taskid the id of the task which will be forwarded.
     * @param newRoleName the new group for the task.
     * @param newUserName the new user who gets the task.
     *
     * @throws CmsException if operation was not successful.
     */
    public void forwardTask(int taskid, String newRoleName, String newUserName) throws CmsException {
        m_driverManager.forwardTask(m_context, taskid, newRoleName, newUserName);
    }

    /**
     * Returns the vector of access control entries of a resource.
     * 
     * @param resourceName the name of the resource.
     * @return a vector of access control entries
     * @throws CmsException if something goes wrong
     */
    public Vector getAccessControlEntries(String resourceName) throws CmsException {
        return getAccessControlEntries(resourceName, true);
    }

    /**
     * Returns the vector of access control entries of a resource.
     * 
     * @param resourceName the name of the resource.
     * @param getInherited true, if inherited access control entries should be returned, too
     * @return a vector of access control entries
     * @throws CmsException if something goes wrong
     */
    public Vector getAccessControlEntries(String resourceName, boolean getInherited) throws CmsException {
        CmsResource res = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_driverManager.getAccessControlEntries(m_context, res, getInherited);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.
     * 
     * @param resourceName the name of the resource
     * @return the access control list of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName) throws CmsException {
        return getAccessControlList(resourceName, false);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.
     * 
     * @param resourceName the name of the resource
     * @param inheritedOnly if set, the non-inherited entries are skipped
     * @return the access control list of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName, boolean inheritedOnly) throws CmsException {
        CmsResource res = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_driverManager.getAccessControlList(m_context, res, inheritedOnly);
    }

    /**
     * Returns all projects, which the current user can access.
     *
     * @return a Vector of objects of type <code>CmsProject</code>.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getAllAccessibleProjects() throws CmsException {
        return (m_driverManager.getAllAccessibleProjects(m_context));
    }

    /**
    * Returns a Vector with all projects from history
    *
    * @return Vector with all projects from history.
    *
    * @throws CmsException  Throws CmsException if operation was not succesful.
    */
    public Vector getAllBackupProjects() throws CmsException {
        return m_driverManager.getAllBackupProjects();
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.
     *
     * @return a Vector of objects of type <code>CmsProject</code>.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getAllManageableProjects() throws CmsException {
        return (m_driverManager.getAllManageableProjects(m_context));
    }

    /**
     * Returns a List with all initialized resource types.<p>
     *
     * @return a List with all initialized resource types
     */
    public List getAllResourceTypes() {
        I_CmsResourceType resourceTypes[] = m_driverManager.getAllResourceTypes();
        List result = new ArrayList(resourceTypes.length);
        
        for (int i = 0; i < resourceTypes.length; i++) {
            if (resourceTypes[i] != null) {
                result.add(resourceTypes[i]);
            }
        }
        
        return result;
    }

    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupTagId() {
        return m_driverManager.getBackupTagId();
    }

    /**
     * Returns all child groups of a group.
     *
     * @param groupname the name of the group.
     * @return groups a Vector of all child groups or null.
     * @throws CmsException if operation was not successful.
     */
    public Vector getChild(String groupname) throws CmsException {
        return (m_driverManager.getChild(m_context, groupname));
    }

    /**
     * Returns all child groups of a group.
     * <br>
     * This method also returns all sub-child groups of the current group.
     *
     * @param groupname the name of the group.
     * @return groups a Vector of all child groups or null.
     * @throws CmsException if operation was not successful.
     */
    public Vector getChilds(String groupname) throws CmsException {
        return (m_driverManager.getChilds(m_context, groupname));
    }

    /**
     * Gets the configurations of the properties-file.
     * @return the configurations of the properties-file.
     */
    public ExtendedProperties getConfigurations() {
        return m_driverManager.getConfigurations();
    }

    /**
     * Gets all groups to which a given user directly belongs.
     *
     * @param username the name of the user to get all groups for.
     * @return a Vector of all groups of a user.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getDirectGroupsOfUser(String username) throws CmsException {
        return (m_driverManager.getDirectGroupsOfUser(m_context, username));
    }

    /**
     * Returns the generic driver objects.<p>
     * 
     * @return a mapping of class names to driver objects
     */
    public Map getDrivers() {
        HashMap drivers = new HashMap();
        drivers.put(this.m_driverManager.getVfsDriver().getClass().getName(), this.m_driverManager.getVfsDriver());
        drivers.put(this.m_driverManager.getUserDriver().getClass().getName(), this.m_driverManager.getUserDriver());
        drivers.put(this.m_driverManager.getProjectDriver().getClass().getName(), this.m_driverManager.getProjectDriver());
        drivers.put(this.m_driverManager.getWorkflowDriver().getClass().getName(), this.m_driverManager.getWorkflowDriver());
        drivers.put(this.m_driverManager.getBackupDriver().getClass().getName(), this.m_driverManager.getBackupDriver());    
        return drivers;
    }
    
    /**
     * Returns a Vector with all files of a given folder.
     * (only the direct subfiles, not the files in subfolders)
     * <br>
     * Files of a folder can be read from an offline Project and the online Project.
     *
     * @param foldername the complete path to the folder.
     *
     * @return subfiles a Vector with all files of the given folder.
     *
     * @throws CmsException if the user has not hte appropriate rigths to access or read the resource.
     */
    public List getFilesInFolder(String foldername) throws CmsException {
        return (m_driverManager.getSubFiles(m_context, addSiteRoot(foldername), CmsResourceFilter.DEFAULT));
    }

    /**
     * Returns a Vector with all files of a given folder.
     * <br>
     * Files of a folder can be read from an offline Project and the online Project.
     *
     * @param foldername the complete path to the folder.
     * @param filter a filter object to filter the resources
     *
     * @return subfiles a Vector with all files of the given folder.
     *
     * @throws CmsException if the user has not hte appropriate rigths to access or read the resource.
     */
    public List getFilesInFolder(String foldername, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.getSubFiles(m_context, addSiteRoot(foldername), filter));
    }

    /**
     * Returns a Vector with all resource-names of the resources that have set the given property to the given value.
     *
     * @param propertyDefinition the name of the property-definition to check.
     * @param propertyValue the value of the property for the resource.
     *
     * @return a Vector with all names of the resources.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getFilesWithProperty(String propertyDefinition, String propertyValue) throws CmsException {
        return m_driverManager.getFilesWithProperty(m_context, propertyDefinition, propertyValue);
    }

    /**
     * This method can be called, to determine if the file-system was changed in the past.
     * <br>
     * A module can compare its previously stored number with the returned number.
     * If they differ, the file system has been changed.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemFolderChanges() {
        return (m_driverManager.getFileSystemFolderChanges());
    }

    /**
     * Returns all groups in the Cms.
     *
     * @return a Vector of all groups in the Cms.
     *
     * @throws CmsException if operation was not successful
     */
    public Vector getGroups() throws CmsException {
        return (m_driverManager.getGroups(m_context));
    }

    /**
     * Returns the groups of a Cms user.<p>
     *
     * @param username the name of the user
     * @return a vector of Cms groups
     * @throws CmsException if operation was not succesful.
     */
    public Vector getGroupsOfUser(String username) throws CmsException {
        return m_driverManager.getGroupsOfUser(m_context, username);
    }
    
    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     *
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result vector
     * @return a vector of Cms groups filtered by the specified IP address
     * @throws CmsException if operation was not succesful.
     */
    public Vector getGroupsOfUser(String username, String remoteAddress) throws CmsException {
        return m_driverManager.getGroupsOfUser(m_context, username, remoteAddress);
    }
    
    /**
     * Returns all groups with a name like the specified pattern.<p>
     *
     * @param namePattern pattern for the group name
     * @return a Vector of all groups with a name like the given pattern
     *
     * @throws CmsException if something goes wrong
     */
    public Vector getGroupsLike(String namePattern) throws CmsException {
        return (m_driverManager.getGroupsLike(m_context, namePattern));
    }
    
    /**
     * Checks if a user is a direct member of a group having a name like the specified pattern.<p>
     *
     * @param username the name of the user to get all groups for.
     * @param groupNamePattern pattern for the group name
     * @return <code>true</code> if the given user is a direct member of a group having a name like the specified pattern
     * @throws CmsException if something goes wrong
     */
    public boolean hasDirectGroupsOfUserLike(String username, String groupNamePattern) throws CmsException {
        return (m_driverManager.hasDirectGroupsOfUserLike(m_context, username, groupNamePattern));
    }       

    /**
     * This is the port the workplace access is limited to. With the opencms.properties
     * the access to the workplace can be limited to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.
     * @return the portnumber or -1 if no port is set.
     */
    public int getLimitedWorkplacePort() {
        return m_driverManager.getLimitedWorkplacePort();
    }

    /**
     * Returns a list of all currently logged in users.
     * This method is only allowed for administrators.
     * 
     * @return a vector of users that are currently logged in
     * @throws CmsException if something goes wrong
     */
    public Vector getLoggedInUsers() throws CmsException {
        if (isAdmin()) {
            if (m_sessionStorage != null) {
                return m_sessionStorage.getLoggedInUsers();
            } else {
                return null;
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] getLoggedInUsers()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Returns the parent group of a group.
     *
     * @param groupname the name of the group.
     * @return group the parent group or null.
     * @throws CmsException if operation was not successful.
     */
    public CmsGroup getParent(String groupname) throws CmsException {
        return (m_driverManager.getParent(groupname));
    }

    /**
     * Returns the set set of permissions of the current user for a given resource. 
     * 
     * @param resourceName the name of the resource
     * @return the set of the permissions of the current user
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName) throws CmsException {
        // reading permissions is allowed even if the resource is marked as deleted
        CmsResource resource = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsUser user = m_context.currentUser();

        return m_driverManager.getPermissions(m_context, resource, user);
    }

    /**
     * Returns the set set of permissions of a given user for a given resource.
     * 
     * @param resourceName the name of the resource
     * @param userName the name of the user
     * @return the current permissions on this resource
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName, String userName) throws CmsException {
        CmsAccessControlList acList = getAccessControlList(resourceName);
        CmsUser user = readUser(userName);
        return acList.getPermissions(user, getGroupsOfUser(userName));
    }

    /**
     * Returns the current OpenCms registry.<p>
     *
     * @return the current OpenCms registry
     */
    public CmsRegistry getRegistry() {
        return m_driverManager.getRegistry(this);
    }

    /**
     * Returns the current request-context.
     *
     * @return the current request-context.
     */
    public CmsRequestContext getRequestContext() {
        return (m_context);
    }

    /**
     * Returns a Vector with the sub resources for a folder.<br>
     *
     * @param folder the name of the folder to get the subresources from.
     * @return subfolders a Vector with resources
     * @throws CmsException  if operation was not succesful
     */
    public Vector getResourcesInFolder(String folder) throws CmsException {
        return m_driverManager.getResourcesInFolder(m_context, addSiteRoot(folder));
    }

    /**
    * Returns a List with all sub resources of a given folder that have benn modified
    * in a given time range.<p>
    * 
    * The rertuned list is sorted descending (newest resource first).
    *
    * <B>Security:</B>
    * All users are granted.
    *
    * @param folder the folder to get the subresources from
    * @param starttime the begin of the time range
    * @param endtime the end of the time range
    * @return List with all resources
    *
    * @throws CmsException if operation was not succesful 
    */
    public List getResourcesInTimeRange(String folder, long starttime, long endtime) throws CmsException {
        return m_driverManager.getResourcesInTimeRange(m_context, addSiteRoot(folder), starttime, endtime);
    }

    /**
      * Returns a Vector with all resources of the given type that have set the given property.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param propertyDefinition the name of the propertydefinition to check.
      *
      * @return Vector with all resources.
      *
      * @throws CmsException if operation was not succesful.
      */
    public Vector getResourcesWithPropertyDefinition(String propertyDefinition) throws CmsException {
        return m_driverManager.getResourcesWithPropertyDefinition(m_context, propertyDefinition);
    }

    /**
     * Returns a List with the complete sub tree of a given folder that have set the given property.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param folder the folder to get the subresources from
     * @param propertyDefinition the name of the propertydefinition to check
     * @return List with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(String folder, String propertyDefinition) throws CmsException {
        return m_driverManager.getResourcesWithProperty(m_context, addSiteRoot(folder), propertyDefinition);
    }

    /**
     * Returns a List with resources that have set the given property.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param propertyDefinition the name of the propertydefinition to check
     * @return List with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(String propertyDefinition) throws CmsException {
        return m_driverManager.getResourcesWithProperty(m_context, "/", propertyDefinition);
    }

    /**
      * Returns a Vector with all resources of the given type that have set the given property to the given value.
      *
      * <B>Security:</B>
      * All users are granted.
      *
      * @param propertyDefinition the name of the propertydefinition to check.
      * @param propertyValue the value of the property for the resource.
      * @param resourceType The resource type of the resource
      *
      * @return Vector with all resources.
      *
      * @throws CmsException Throws CmsException if operation was not succesful.
      */
    public Vector getResourcesWithPropertyDefintion(String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        return m_driverManager.getResourcesWithPropertyDefintion(m_context, propertyDefinition, propertyValue, resourceType);
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     * 
     * @param resourceType the id of the resourceType to get
     * @return the initialized resource type instance for the given id
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getResourceType(int resourceType) throws CmsException {
        return m_driverManager.getResourceType(resourceType);
    }

    /**
     * Returns the resource type id for the given resource type name.<p>
     * 
     * @param resourceType the name of the resourceType to get the id for
     * @return the resource type id for the given resource type name
     * @throws CmsException if something goes wrong
     */
    public int getResourceTypeId(String resourceType) throws CmsException {
        I_CmsResourceType type = m_driverManager.getResourceType(resourceType);
        if (type != null) {
            return type.getResourceType();
        } else {
            return I_CmsConstants.C_UNKNOWN_ID;
        }
    }

    
    /**
     * Returns a Vector with all subfolders of a given folder.<p>
     *
     * @param foldername the complete path to the folder
     * @return all subfolders (CmsFolder Objects) for the given folder
     * @throws CmsException if the user has not the permissions to access or read the resource
     */
    public List getSubFolders(String foldername) throws CmsException {
        return (m_driverManager.getSubFolders(m_context, addSiteRoot(foldername), CmsResourceFilter.DEFAULT));
    }

    /**
     * Returns a Vector with all subfolders of a given folder.<p>
     *
     * @param foldername the complete path to the folder
     * @param filter a filter object to filter the resources
     * @return all subfolders (CmsFolder Objects) for the given folder
     * @throws CmsException if the user has not the permissions to access or read the resource
     */
    public List getSubFolders(String foldername, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.getSubFolders(m_context, addSiteRoot(foldername), filter));
    }

    /**
      * Get a parameter value for a task.
      *
      * @param taskid the id of the task.
      * @param parname the name of the parameter.
      * @return the parameter value.
      *
      * @throws CmsException if operation was not successful.
      */
    public String getTaskPar(int taskid, String parname) throws CmsException {
        return (m_driverManager.getTaskPar(taskid, parname));
    }

    /**
     * Get the template task id fo a given taskname.
     *
     * @param taskname the name of the task.
     *
     * @return the id of the task template.
     *
     * @throws CmsException if operation was not successful.
     */
    public int getTaskType(String taskname) throws CmsException {
        return m_driverManager.getTaskType(taskname);
    }

    /**
     * Returns all users in the Cms.
     *
     * @return a Vector of all users in the Cms.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getUsers() throws CmsException {
        return (m_driverManager.getUsers(m_context));
    }

    /**
     * Returns all users of the given type in the Cms.
     *
     * @param type the type of the users.
     *
     * @return vector of all users of the given type in the Cms.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getUsers(int type) throws CmsException {
        return (m_driverManager.getUsers(m_context, type));
    }

    /**
    * Returns all users from a given type that start with a specified string<P/>
    *
    * @param type the type of the users.
    * @param namefilter The filter for the username
    * @return vector of all users of the given type in the Cms.
    *
    * @throws CmsException if operation was not successful.
    * @deprecated
    */
    public Vector getUsers(int type, String namefilter) throws CmsException {
        return m_driverManager.getUsers(m_context, type, namefilter);
    }

    /**
     * Gets all users with a certain Lastname.
     *
     * @param Lastname      the start of the users lastname
     * @param UserType      webuser or systemuser
     * @param UserStatus    enabled, disabled
     * @param wasLoggedIn   was the user ever locked in?
     * @param nMax          max number of results
     *
     * @return the users.
     *
     * @throws CmsException if operation was not successful.
     * @deprecated
     */
    public Vector getUsersByLastname(String Lastname, int UserType, int UserStatus, int wasLoggedIn, int nMax) throws CmsException {

        return m_driverManager.getUsersByLastname(m_context, Lastname, UserType, UserStatus, wasLoggedIn, nMax);
    }

    /**
     * Gets all users of a group.
     *
     * @param groupname the name of the group to get all users for.
     * @return all users in the group.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getUsersOfGroup(String groupname) throws CmsException {
        return (m_driverManager.getUsersOfGroup(m_context, groupname));
    }

    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users that have read and view access are granted.
     *
     * @param propertyDefinition the name of the propertydefinition to check.
     * @param propertyValue the value of the property for the resource.
     * @param resourceType the resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getVisibleResourcesWithProperty(String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        return m_driverManager.getVisibleResourcesWithProperty(m_context, propertyDefinition, propertyValue, resourceType);
    }

    /**
     * Checks if the current user has required permissions to access a given resource.
     * 
     * @param resource the resource that will be accessed
     * @param requiredPermissions the set of required permissions
     * @return true if the required permissions are satisfied
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {
        return m_driverManager.hasPermissions(m_context, resource, requiredPermissions, false);
    }

    /**
     * Checks if the current user has required permissions to access a given resource
     * 
     * @param resourceName the name of the resource that will be accessed
     * @param requiredPermissions the set of required permissions
     * @return true if the required permissions are satisfied
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(String resourceName, CmsPermissionSet requiredPermissions) throws CmsException {
        CmsResource resource = readFileHeader(resourceName);
        return m_driverManager.hasPermissions(m_context, resource, requiredPermissions, false);
    }

    /**
     * Imports a import-resource (folder or zipfile) to the cms.
     *
     * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
     * @param importPath the name (absolute Path) of the folder in which should be imported.
     *
     * @throws CmsException if operation was not successful.
     */
    public void importFolder(String importFile, String importPath) throws CmsException {
        
        // OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
        m_driverManager.clearcache();
        
        // import the resources
        m_driverManager.importFolder(this, m_context, importFile, importPath);
        
        // OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
        m_driverManager.clearcache();
    }

    /**
     * Imports a resource to the cms.<p>
     *
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * @param importpath the name of the resource destinaition
     * @return the imported CmsResource
     * @throws CmsException if operation was not successful
     */
    public CmsResource importResource(CmsResource resource, byte[] content, Map properties, String importpath) throws CmsException {
        return getResourceType(resource.getType()).importResource(this, resource, content, properties, importpath);
    }

    /**
     * Checks, if the users current group is the admin-group.
     *
     *
     * @return <code>true</code>, if the users current group is the admin-group; <code>false</code> otherwise.
     * @throws CmsException if operation was not successful.
     */
    public boolean isAdmin() throws CmsException {
        return m_driverManager.isAdmin(m_context);
    }

    /**
     * Checks if the user has management access to the project.
     *
     * Please note: This is NOT the same as the {@link CmsObject#isProjectManager()} 
     * check. If the user has management access to a project depends on the
     * project settings.<p>
     *
     * @return true if the user has management access to the project
     * @throws CmsException if operation was not successful.
     * @see #isProjectManager()
     */
    public boolean isManagerOfProject() throws CmsException {
        return m_driverManager.isManagerOfProject(m_context);
    }
    
    /**
     * Checks if the user is a member of the project manager group.<p>
     *
     * Please note: This is NOT the same as the {@link CmsObject#isManagerOfProject()()} 
     * check. If the user is a member of the project manager group, 
     * he can create new projects.<p>
     *
     * @return true if the user is a member of the project manager group
     * @throws CmsException if operation was not successful.
     * @see #isManagerOfProject()
     */    
    public boolean isProjectManager() throws CmsException {
        return m_driverManager.isProjectManager(m_context);
    }
    
    /**
     * Returns the user, who has locked a given resource.
     * <br>
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, who has locked a resource.
     *
     * @param resource the resource to check.
     *
     * @return the user who has locked the resource.
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsUser lockedBy(CmsResource resource) throws CmsException {
        return (m_driverManager.lockedBy(m_context, resource));
    }

    /**
        * Returns the user, who has locked a given resource.
        * <br>
        * A user can lock a resource, so he is the only one who can write this
        * resource. This methods checks, who has locked a resource.
        *
        * @param resource The complete path to the resource.
        *
        * @return the user who has locked a resource.
        *
        * @throws CmsException if operation was not successful.
        */
    public CmsUser lockedBy(String resource) throws CmsException {
        return (m_driverManager.lockedBy(m_context, addSiteRoot(resource)));
    }

    /**
     * Locks the given resource.
     * <br>
     * A user can lock a resource, so he is the only one who can write this
     * resource.
     *
     * @param resource The complete path to the resource to lock.
     *
     * @throws CmsException if the user has not the rights to lock this resource.
     * It will also be thrown, if there is an existing lock.
     *
     */
    public void lockResource(String resource) throws CmsException {
        // try to lock the resource, prevent from overwriting an existing lock
        lockResource(resource, false, CmsLock.C_MODE_COMMON);
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
    public void lockResource(String resource, boolean force) throws CmsException {
        lockResource(resource, force, CmsLock.C_MODE_COMMON);
    }
    
    /**
     * Locks a given resource.
     * <br>
     * A user can lock a resource, so he is the only one who can write this
     * resource.
     *
     * @param resource the complete path to the resource to lock.
     * @param force if force is <code>true</code>, a existing locking will be overwritten.
     * @param mode flag indicating the mode (temporary or common) of a lock
     *
     * @throws CmsException if the user has not the rights to lock this resource.
     * It will also be thrown, if there is a existing lock and force was set to false.
     */
    public void lockResource(String resource, boolean force, int mode) throws CmsException {
        getResourceType(readFileHeader(resource, CmsResourceFilter.IGNORE_EXPIRATION).getType()).lockResource(this, resource, force, mode);
    }
    
    /**
     * Changes the lock of a resource.<p>
     * 
     * @param resourcename name of the resource
     * @throws CmsException if something goes wrong
     */
    public void changeLock(String resourcename) throws CmsException {
        m_driverManager.changeLock(m_context, addSiteRoot(resourcename));
    }

    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @return the name of the logged in user
     *
     * @throws CmsSecurityException if operation was not successful
     */
    public String loginUser(String username, String password) throws CmsSecurityException {
        return loginUser(username, password, m_context.getRemoteAddress());
    }
    
    /**
     * Logs a user with a given ip address into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param remoteAddress the ip address
     * @return the name of the logged in user
     *
     * @throws CmsSecurityException if operation was not successful
     */    
    public String loginUser(String username, String password, String remoteAddress) throws CmsSecurityException {
        return loginUser(username, password, remoteAddress, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
    }
    
    /**
     * Logs a user with a given type and a given ip address into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param remoteAddress the ip address
     * @param type the user type (System or Web user)
     * @return the name of the logged in user
     *
     * @throws CmsSecurityException if operation was not successful
     */    
    public String loginUser(String username, String password, String remoteAddress, int type) throws CmsSecurityException {    
        // login the user
        CmsUser newUser = m_driverManager.loginUser(username, password, remoteAddress, type);
        // set the project back to the "Online" project
        CmsProject newProject;
        try {
            newProject = m_driverManager.readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        } catch (CmsException e) {
            // should not happen since the online project is always available
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, e);
        }
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject);
        // init this CmsObject with the new user
        init(m_driverManager, m_context, m_sessionStorage);
        // fire a login event
        this.fireEvent(org.opencms.main.I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }
    
    /**
     * Logs a web user into the Cms, if the password is correct.
     *
     * @param username the name of the user.
     * @param password the password of the user.
     * @return the name of the logged in user.
     *
     * @throws CmsSecurityException if operation was not successful
     */
    public String loginWebUser(String username, String password) throws CmsSecurityException {
        return loginUser(username, password, m_context.getRemoteAddress(), I_CmsConstants.C_USER_TYPE_WEBUSER);
    }

    /**
     * Lookup and reads the user or group with the given UUID.<p>
     *   
     * @param principalId the uuid of a user or group
     * @return the user or group with the given UUID
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {
        return m_driverManager.lookupPrincipal(principalId);
    }

    /**
     * Lookup and reads the user or group with the given name.<p>
     * 
     * @param principalName the name of the user or group
     * @return the user or group with the given name
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {
        return m_driverManager.lookupPrincipal(principalName);
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
    public void moveResource(String source, String destination) throws CmsException {
        getResourceType(readFileHeader(source, CmsResourceFilter.IGNORE_EXPIRATION).getType()).moveResource(this, source, destination);
    }


    /**
     * Moves a resource to the lost and found folder
     *
     * @param source the complete path of the sourcefile.
     * @return location of the moved resource
     * @throws CmsException if the user has not the rights to move this resource,
     * or if the file couldn't be moved.
     */
    public String copyToLostAndFound(String source) throws CmsException {
        return getResourceType(readFileHeader(source, CmsResourceFilter.IGNORE_EXPIRATION).getType()).copyToLostAndFound(this, source, true);
    }

    /**
     * Publishes the current project, printing messages to a shell report.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void publishProject() throws Exception {
        publishProject(new CmsShellReport());
    }

    /**
     * Publishes the current project.<p>
     *
     * @param report an instance of I_CmsReport to print messages
     * @throws CmsException if something goes wrong
     */
    public void publishProject(I_CmsReport report) throws CmsException {
        publishProject(report, null, false);
    }

    /**
     * Direct publishes a specified resource.<p>
     * 
     * @param report an instance of I_CmsReport to print messages
     * @param directPublishResource a CmsResource that gets directly published; or null if an entire project gets published
     * @param directPublishSiblings if a CmsResource that should get published directly is provided as an argument, all eventual siblings of this resource get publish too, if this flag is true
     * @throws CmsException if something goes wrong
     * @see #publishResource(String)
     * @see #publishResource(String, boolean, I_CmsReport)
     */
    public void publishProject(I_CmsReport report, CmsResource directPublishResource, boolean directPublishSiblings) throws CmsException {
        CmsPublishList publishList = getPublishList(directPublishResource, directPublishSiblings, report);
        publishProject(report, publishList);
    }
    
    /**
     * Publishes the resources of a specified publish list.<p>
     * 
     * @param report an instance of I_CmsReport to print messages
     * @param publishList a publish list
     * @throws CmsException if something goes wrong
     * @see #getPublishList(I_CmsReport)
     * @see #getPublishList(CmsResource, boolean, I_CmsReport)
     */
    public void publishProject(I_CmsReport report, CmsPublishList publishList) throws CmsException {
        // TODO check if useful/neccessary
        m_driverManager.clearcache();

        synchronized (m_driverManager) {            
            try {                                
                m_driverManager.publishProject(this, m_context, publishList, report);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(e);
                }
                
                throw e;
            } catch (Exception e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(e);
                }

                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
            } finally {
                // TODO check if useful/neccessary
                m_driverManager.clearcache();
                
                // set current project to online project if the published project was temporary
                // and the published project is still the current project
                if (m_context.currentProject().getId() == m_context.currentProject().getId() && (m_context.currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY)) {
                    m_context.setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
                }

                // fire an event that a project has been published
                Map eventData = new HashMap();
                eventData.put("report", report);
                eventData.put("publishHistoryId", publishList.getPublishHistoryId().toString());
                eventData.put("context", m_context);
                CmsEvent exportPointEvent = new CmsEvent(this, I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData, false);
                OpenCms.fireCmsEvent(exportPointEvent);                 
            }
        }
    }    

    /**
     * Publishes a single resource.<p>
     *
     * @param resourcename the name of the resource to be published
     * @throws Exception if something goes wrong
     */
    public void publishResource(String resourcename) throws Exception {
        publishResource(resourcename, false, new CmsShellReport());
    }

    /**
     * Publishes a single resource.<p>
     * 
     * @param resourcename the name of the resource to be published
     * @param directPublishSiblings if true, all siblings of the resource are also published
     * @param report the report to write the progress information to
     * @throws Exception if something goes wrong
     */
    public void publishResource(String resourcename, boolean directPublishSiblings, I_CmsReport report) throws Exception {
        CmsResource resource = null;
        
        try {
            resource = readFileHeader(resourcename, CmsResourceFilter.ALL);
            publishProject(report, resource, directPublishSiblings);
        } catch (CmsException e) {
            throw e;
        } finally {
            OpenCms.fireCmsEvent(new CmsEvent(this, I_CmsEventListener.EVENT_PUBLISH_RESOURCE, Collections.singletonMap("resource", resource)));
        }
    }

    /**
     * Returns the absolute path of a given resource.<p>
     * The absolute path is the root path without the site information.
     * 
     * @param resource the resource
     * @return the absolute path
     */
    public String readAbsolutePath(CmsResource resource) {
        return readAbsolutePath(resource, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns the absolute path of a given resource.<p>
     * The absolute path is the root path without the site information.
     * 
     * @param resource the resource
     * @param filter  a filter object to filter the resources
     * @return the absolute path
     */
    public String readAbsolutePath(CmsResource resource, CmsResourceFilter filter) {
        if (!resource.hasFullResourceName()) {
            try {
                m_driverManager.readPath(m_context, resource, filter);
            } catch (CmsException e) {
                OpenCms.getLog(this).error("Could not read absolute path for resource " + resource, e);
                resource.setFullResourceName(null);
            }
        }

        // adjust the resource path for the current site root
        return removeSiteRoot(resource.getRootPath());
    }

    /**
     * Reads the agent of a task.<p>
     *
     * @param task the task to read the agent from
     * @return the agent of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readAgent(CmsTask task) throws CmsException {
        return (m_driverManager.readAgent(task));
    }

    /**
     * Reads all file headers of a file in the OpenCms.
     * <br>
     * This method returns a vector with the history of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the filecontent.
     *
     * @param filename the name of the file to be read.
     *
     * @return a Vector of file headers read from the Cms.
     *
     * @throws CmsException  if operation was not successful.
     */
    public List readAllBackupFileHeaders(String filename) throws CmsException {
        return (m_driverManager.readAllBackupFileHeaders(m_context, addSiteRoot(filename)));
    }

    /**
     * select all projectResources from an given project
     *
     * @param projectId id of the project in which the resource is used.
     * @return vector of all resources belonging to the given project
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException {
        return m_driverManager.readAllProjectResources(m_context, projectId);
    }

    /**
     * Returns a list of all properties of a file or folder.
     *
     * @param filename the name of the resource for which the property has to be read
     *
     * @return a Vector of Strings
     *
     * @throws CmsException if operation was not succesful
     * @deprecated use readProperties(String) instead
     */
    public Map readAllProperties(String filename) throws CmsException {
        Map result = new HashMap();
        Map properties = readProperties(filename, false);
        
        if (properties != null) {
            result.putAll(properties);
        }
        
        return result;
    }

    /**
     * Reads all property definitions for the given resource type by id.<p>
     *
     * @param resourceType the resource type to read the property-definitions for.<p>
     *
     * @return a Vector with the property defenitions for the resource type (may be empty)
     *
     * @throws CmsException if something goes wrong
     */
    public Vector readAllPropertydefinitions(int resourceType) throws CmsException {
        return m_driverManager.readAllPropertydefinitions(m_context, resourceType);
    }

    /**
     * Reads all property definitions for the given resource type by name.<p>
     *
     * @param resourceType the resource type to read the property-definitions for.<p>
     *
     * @return a Vector with the property defenitions for the resource type (may be empty)
     *
     * @throws CmsException if something goes wrong
     */
    public Vector readAllPropertydefinitions(String resourceType) throws CmsException {
        return m_driverManager.readAllPropertydefinitions(m_context, resourceType);
    }

    /**
     * Reads a file from the Cms for history.
     * <br>
     * The reading includes the filecontent.
     *
     * @param filename the complete path of the file to be read.
     * @param tagId the tag id of the resource
     *
     * @return file the read file.
     *
     * @throws CmsException , if the user has not the rights
     * to read the file, or if the file couldn't be read.
     */
    public CmsBackupResource readBackupFile(String filename, int tagId) throws CmsException {
        return (m_driverManager.readBackupFile(m_context, tagId, addSiteRoot(filename)));
    }

    /**
     * Reads a file header from the Cms for history.
     * <br>
     * The reading excludes the filecontent.
     *
     * @param filename the complete path of the file to be read.
     * @param tagId the version id of the resource
     *
     * @return file the read file.
     *
     * @throws CmsException , if the user has not the rights
     * to read the file headers, or if the file headers couldn't be read.
     */
    public CmsResource readBackupFileHeader(String filename, int tagId) throws CmsException {
        return (m_driverManager.readBackupFileHeader(m_context, tagId, addSiteRoot(filename)));
    }

    /**
     * Reads a backup project from the Cms.
     *
     * @param tagId  the tag of the backup project to be read
     * @return CmsBackupProject object of the requested project
     * @throws CmsException if operation was not successful.
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {
        return (m_driverManager.readBackupProject(tagId));
    }

    /**
     * Gets the Crontable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @return the crontable.
     * @throws CmsException if something goes wrong
     */
    public String readCronTable() throws CmsException {
        return m_driverManager.readCronTable();
    }

    /**
     * Reads the package path of the system.
     * This path is used for db-export and db-import and all module packages.
     *
     * @return the package path
     * @throws CmsException if operation was not successful
     */
    public String readPackagePath() throws CmsException {
        return m_driverManager.readPackagePath();
    }

    /**
     * Reads a file from the Cms.
     *
     * @param filename the complete path to the file.
     *
     * @return file the read file.
     *
     * @throws CmsException if the user has not the rights to read this resource,
     * or if the file couldn't be read.
     */
    public CmsFile readFile(String filename) throws CmsException {
        return m_driverManager.readFile(m_context, addSiteRoot(filename));
    }

    /**
     * Reads a file from the Cms.
     *
     * @param filename the complete path to the file.
     * @param filter a filter object to filter the resources
     *
     * @return file the read file.
     *
     * @throws CmsException if the user has not the rights to read this resource,
     * or if the file couldn't be read.
     */
    public CmsFile readFile(String filename, CmsResourceFilter filter) throws CmsException {
        return m_driverManager.readFile(m_context, addSiteRoot(filename), filter);
    }

    /**
     * Reads a file from the Cms.
     *
     * @param folder the complete path to the folder from which the file will be read.
     * @param filename the name of the file to be read.
     *
     * @return file the read file.
     *
     * @throws CmsException , if the user has not the rights
     * to read this resource, or if the file couldn't be read.
     */
    public CmsFile readFile(String folder, String filename) throws CmsException {
        return (m_driverManager.readFile(m_context, addSiteRoot(folder + filename)));
    }

    /**
     * Gets the known file extensions (=suffixes).
     *
     *
     * @return a Hashtable with all known file extensions as Strings.
     *
     * @throws CmsException if operation was not successful.
     */
    public Hashtable readFileExtensions() throws CmsException {
        return m_driverManager.readFileExtensions();
    }

    /**
     * Reads a file header from the Cms.
     * <br>
     * The reading excludes the filecontent.
     *
     * @param filename the complete path of the file to be read.
     *
     * @return file the read file.
     *
     * @throws CmsException , if the user has not the rights
     * to read the file headers, or if the file headers couldn't be read.
     */
    public CmsResource readFileHeader(String filename) throws CmsException {
        return (m_driverManager.readFileHeader(m_context, addSiteRoot(filename)));
    }

    /**
     * Reads a file header from the Cms.
     * <br>
     * The reading excludes the filecontent.
     *
     * @param filename the complete path of the file to be read
     * @param filter a filter object to filter the resources
     *
     * @return file the read file header
     *
     * @throws CmsException if the user has not the rights
     * to read the file headers, or if the file headers couldn't be read
     */
    public CmsResource readFileHeader(String filename, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.readFileHeader(m_context, addSiteRoot(filename), filter));
    }

    /**
     * Reads a file header from the Cms.
     * <br>
     * The reading excludes the filecontent.
     *
     * @param filename the complete path of the file to be read.
     * @param projectId the id of the project where the resource should belong to
     * @param filter a filter object to filter the resources
     * 
     * @return file the read file.
     *
     * @throws CmsException , if the user has not the rights
     * to read the file headers, or if the file headers couldn't be read.
     */
    public CmsResource readFileHeader(String filename, int projectId, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.readFileHeaderInProject(projectId, addSiteRoot(filename), filter));
    }

    /**
     * Reads a file header from the Cms.
     * <br>
     * The reading excludes the filecontent.
     *
     * @param folder the complete path to the folder from which the file will be read.
     * @param filename the name of the file to be read.
     *
     * @return file the read file.
     *
     * @throws CmsException if the user has not the rights
     * to read the file header, or if the file header couldn't be read.
     */
    public CmsResource readFileHeader(String folder, String filename) throws CmsException {
        return (m_driverManager.readFileHeader(m_context, addSiteRoot(folder + filename)));
    }

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
    public Vector readFilesByType(int projectId, int resourcetype) throws CmsException {
        return m_driverManager.readFilesByType(m_context, projectId, resourcetype);
    }

    /**
     * Reads a folder from the Cms.
     *
     * @param folderId the id of the folder to be read
     * @param filter a filter object to filter the resources
     *
     * @return folder the read folder
     *
     * @throws CmsException if the user does not have the permissions
     * to read this folder, or if the folder couldn't be read
     */
    public CmsFolder readFolder(CmsUUID folderId, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.readFolder(m_context, folderId, filter));
    }

    /**
     * Reads a folder from the Cms.
     *
     * @param folderName the name of the folder to be read
     *
     * @return The read folder
     *
     * @throws CmsException if the user does not have the permissions
     * to read this folder, or if the folder couldn't be read
     */
    public CmsFolder readFolder(String folderName) throws CmsException {
        return (m_driverManager.readFolder(m_context, addSiteRoot(folderName)));
    }

    /**
     * Reads a folder from the Cms.
     *
     * @param folderName the complete path to the folder to be read
     * @param filter a filter object to filter the resources
     *
     * @return The read folder 
     *
     * @throws CmsException If the user does not have the permissions
     * to read this folder, or if the folder couldn't be read
     */
    public CmsFolder readFolder(String folderName, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.readFolder(m_context, addSiteRoot(folderName), filter));
    }

    /**
      * Reads all given tasks from a user for a project.
      *
      * @param projectId the id of the project in which the tasks are defined.
      * @param ownerName the owner of the task.
      * @param taskType the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
      * @param orderBy specifies how to order the tasks.
      * @param sort sorting of the tasks
      * @return vector of given tasks for a user for a project 
      * 
      * @throws CmsException if operation was not successful.
      */
    public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort) throws CmsException {
        return (m_driverManager.readGivenTasks(projectId, ownerName, taskType, orderBy, sort));
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param project the project to read the group from
     * @return the group of the given project
     */
    public CmsGroup readGroup(CmsProject project) {
        return m_driverManager.readGroup(project);
    }

    /**
     * Reads the group (role) of a task.<p>
     *
     * @param task the task to read the group (role) from
     * @return the group (role) of the task
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(CmsTask task) throws CmsException {
        return m_driverManager.readGroup(task);
    }

    /**
     * Reads a group of the Cms based on its id.<p>
     *
     * @param groupId the id of the group to be read
     * @return the group that has the provided id
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {
        return m_driverManager.readGroup(groupId);
    }

    /**
     * Reads a group of the Cms based on its name.
     * @param groupName the name of the group to be read
     * @return the group that has the provided name
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(String groupName) throws CmsException {
        return (m_driverManager.readGroup(groupName));
    }

    /**
     * Gets the Linkchecktable.
     *
     * <B>Security:</B>
     * All users are granted<BR/>
     *
     * @return the linkchecktable
     * 
     * @throws CmsException if something goes wrong 
     */
    public Hashtable readLinkCheckTable() throws CmsException {
        return m_driverManager.readLinkCheckTable();
    }

    /**
     * Reads the project manager group of a project.<p>
     *
     * @param project the project 
     * @return the managergroup of the project
     */
    public CmsGroup readManagerGroup(CmsProject project) {
        return m_driverManager.readManagerGroup(project);
    }

    /**
     * Reads the original agent of a task from the Cms.<p>
     *
     * @param task the task to read the original agent from
     * @return the original agent of the task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOriginalAgent(CmsTask task) throws CmsException {
        return m_driverManager.readOriginalAgent(task);
    }
    
    /**
     * Reads the owner of a project.
     *
     * @param project the project to read the owner from
     * @return the owner of the project
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {
        return m_driverManager.readOwner(project);
    }

    /**
     * Reads the owner (initiator) of a task.<p>
     *
     * @param task the task to read the owner from
     * @return the owner of the task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTask task) throws CmsException {
        return m_driverManager.readOwner(task);
    }

    /**
     * Reads the owner of a task log.<p>
     *
     * @param log the task log
     * @return the owner of the task log
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTaskLog log) throws CmsException {
        return m_driverManager.readOwner(log);
    }
    
    /**
     * Builds a list of resources for a given path.<p>
     * 
     * Use this method if you want to select a resource given by it's full filename and path. 
     * This is done by climbing down the path from the root folder using the parent-ID's and
     * resource names. Use this method with caution! Results are cached but reading path's 
     * inevitably increases runtime costs.
     * 
     * @param path the requested path
     * @param filter a filter object to filter the resources
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(String path, CmsResourceFilter filter) throws CmsException {
        return (m_driverManager.readPath(m_context, m_context.addSiteRoot(path), filter));
    }

    /**
     * Reads the project in which a resource was last modified.
     *
     * @param res the resource
     * @return the project in which the resource was last modified
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsProject readProject(CmsResource res) throws CmsException {
        return m_driverManager.readProject(res.getProjectLastModified());
    }

    /**
     * Reads a project of a given task from the Cms.
     *
     * @param task the task for which the project will be read.
     * @return the project of the task
     * 
     * @throws CmsException if operation was not successful.
     */
    public CmsProject readProject(CmsTask task) throws CmsException {
        return m_driverManager.readProject(task);
    }

    /**
     * Reads a project from the Cms.
     *
     * @param id the id of the project
     * @return the project with the given id
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsProject readProject(int id) throws CmsException {
        return m_driverManager.readProject(id);
    }
    
    /**
     * Reads a project from the Cms.<p>
     *
     * @param name the name of the project
     * @return the project with the given name
     * @throws CmsException if operation was not successful.
     */
    public CmsProject readProject(String name) throws CmsException {
        return m_driverManager.readProject(name);
    }     

    /**
      * Reads log entries for a project.
      *
      * @param projectId the id of the project for which the tasklog will be read.
      * @return a Vector of new TaskLog objects
      * @throws CmsException if operation was not successful.
      */
    public Vector readProjectLogs(int projectId) throws CmsException {
        return m_driverManager.readProjectLogs(projectId);
    }

    /**
     * Reads all file headers of a project from the Cms.
     *
     * @param projectId the id of the project to read the file headers for.
     * @param filter The filter for the resources (all, new, changed, deleted, locked)
     *
     * @return a Vector (of CmsResources objects) of resources.
     * 
     * @throws CmsException if something goes wrong
     *
     */
    public Vector readProjectView(int projectId, String filter) throws CmsException {
        return m_driverManager.readProjectView(m_context, projectId, filter);
    }

    /**
     * Reads the property-definition for the resource type.
     *
     * @param name the name of the property-definition to read.
     * @param resourcetype the name of the resource type for the property-definition.
     * @return the property-definition.
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsPropertydefinition readPropertydefinition(String name, int resourcetype) throws CmsException {
        return (m_driverManager.readPropertydefinition(m_context, name, resourcetype));
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp a timestamp for reading the data from the db
     * @return List of template resources
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(int parameterResources, long timestamp) throws CmsException {
        return m_driverManager.readStaticExportResources(m_context, parameterResources, timestamp);
    }


    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     * @param rfsName the rfs name of the resource
     * @return the paramter string of the requested resource
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParamters(String rfsName) throws CmsException {
        return  m_driverManager.readStaticExportPublishedResourceParamters(m_context, rfsName);
    }
    
    
    /**
     * Reads the task with the given id.
     *
     * @param id the id of the task to be read.
     * @return the task with the given id
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsTask readTask(int id) throws CmsException {
        return (m_driverManager.readTask(id));
    }

    /**
     * Reads log entries for a task.
     *
     * @param taskid the task for which the tasklog will be read.
     * @return a Vector of new TaskLog objects.
     * @throws CmsException if operation was not successful.
     */
    public Vector readTaskLogs(int taskid) throws CmsException {
        return m_driverManager.readTaskLogs(taskid);
    }

    /**
     * Reads all tasks for a project.
     *
     * @param projectId the id of the project in which the tasks are defined. Can be null to select all tasks.
     * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order: C_SORT_ASC, C_SORT_DESC, or null.
     * @return vector of tasks for the project
     * 
     * @throws CmsException if operation was not successful.
     */
    public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {
        return (m_driverManager.readTasksForProject(projectId, tasktype, orderBy, sort));
    }

    /**
     * Reads all tasks for a role in a project.
     *
     * @param projectId the id of the Project in which the tasks are defined.
     * @param roleName the role who has to process the task.
     * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return vector of tasks for the role
     * 
     * @throws CmsException if operation was not successful.
     */
    public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort) throws CmsException {
        return (m_driverManager.readTasksForRole(projectId, roleName, tasktype, orderBy, sort));
    }

    /**
     * Reads all tasks for a user in a project.
     *
     * @param projectId the id of the Project in which the tasks are defined.
     * @param userName the user who has to process the task.
     * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return vector of tasks for the user 
     * 
     * @throws CmsException if operation was not successful.
     */
    public Vector readTasksForUser(int projectId, String userName, int tasktype, String orderBy, String sort) throws CmsException {
        return (m_driverManager.readTasksForUser(projectId, userName, tasktype, orderBy, sort));
    }

    /**
     * Reads a user based on its id.<p>
     *
     * @param userId the id of the user to be read
     * @return the user with the given id
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException {
        return m_driverManager.readUser(userId);
    }

    /**
     * Reads a user based on its name.<p>
     *
     * @param username the name of the user to be read
     * @return the user with the given name
     * @throws CmsException if somthing goes wrong
     */
    public CmsUser readUser(String username) throws CmsException {
        return m_driverManager.readUser(username);
    }

    /**
     * Returns a user in the Cms.
     *
     * @param username the name of the user to be returned.
     * @param type the type of the user.
     * @return a user in the Cms.
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, int type) throws CmsException {
        return (m_driverManager.readUser(username, type));
    }

    /**
     * Returns a user in the Cms, if the password is correct.
     *
     * @param username the name of the user to be returned.
     * @param password the password of the user to be returned.
     * @return a user in the Cms.
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, String password) throws CmsException {
        return (m_driverManager.readUser(username, password));
    }

    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param username The username of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
    */
    public CmsUser readWebUser(String username) throws CmsException {
        return (m_driverManager.readWebUser(username));
    }

    /**
     * Returns a web user object if the password for the user is correct.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return a web user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {
        return (m_driverManager.readWebUser(username, password));
    }
    
    /**
     * Reactivates a task.<p>
     *
     * @param taskId the Id of the task to reactivate
     *
     * @throws CmsException if something goes wrong
     */
    public void reaktivateTask(int taskId) throws CmsException {
        m_driverManager.reaktivateTask(m_context, taskId);
    }

    /**
     * Sets a new password if the user knows his recovery-password.
     *
     * @param username the name of the user.
     * @param recoveryPassword the recovery password.
     * @param newPassword the new password.
     *
     * @throws CmsException if operation was not successfull.
     */
    public void recoverPassword(String username, String recoveryPassword, String newPassword) throws CmsException {
        m_driverManager.recoverPassword(username, recoveryPassword, newPassword);
    }

    /**
     * Removes the current site root prefix from the absolute path in the resource name,
     * i.e. adjusts the resource name for the current site root.<p> 
     * 
     * @param resourcename the resource name
     * @return the resource name adjusted for the current site root
     */
    private String removeSiteRoot(String resourcename) {
        return getRequestContext().removeSiteRoot(resourcename);
    }

    /**
     * Removes a user from a group.
     *
     * <p>
     * <b>Security:</b>
     * Only the admin user is allowed to remove a user from a group.
     *
     * @param username the name of the user that is to be removed from the group.
     * @param groupname the name of the group.
     * @throws CmsException if operation was not successful.
     */
    public void removeUserFromGroup(String username, String groupname) throws CmsException {
        m_driverManager.removeUserFromGroup(m_context, username, groupname);
    }

    /**
     * Renames the file to the new name.
     *
     * @param oldname the complete path to the file which will be renamed.
     * @param newname the new name of the file.
     *
     * @throws CmsException if the user has not the rights
     * to rename the file, or if the file couldn't be renamed.
     *
     * @deprecated Use renameResource instead.
     */
    public void renameFile(String oldname, String newname) throws CmsException {
        renameResource(oldname, newname);
    }

    /**
     * Renames the resource to the new name.
     *
     * @param oldname the complete path to the file which will be renamed.
     * @param newname the new name of the file.
     *
     * @throws CmsException if the user has not the rights
     * to rename the file, or if the file couldn't be renamed.
     */
    public void renameResource(String oldname, String newname) throws CmsException {
        getResourceType(readFileHeader(oldname, CmsResourceFilter.IGNORE_EXPIRATION).getType()).renameResource(this, oldname, newname);
    }

    /**
     * Replaces and existing resource by another file with different content
     * and different file type.<p>
     * 
     * @param resourcename the resource to replace
     * @param type the type of the new resource
     * @param properties the properties of the new resource
     * @param content the content of the new resource
     * @throws CmsException if something goes wrong
     */
    public void replaceResource(String resourcename, int type, Map properties, byte[] content) throws CmsException {
        // read the properties of the existing file
        Map resProps = null;
        try {
            resProps = readAllProperties(resourcename);
        } catch (CmsException e) {
            resProps = new HashMap();
        }

        // add the properties that might have been collected during a file-upload
        if (properties != null) {
            resProps.putAll(properties);
        }

        getResourceType(readFileHeader(resourcename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).replaceResource(this, resourcename, resProps, content, type);
    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param tagId The tag id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(int tagId, String filename) throws CmsException {
        getResourceType(readFileHeader(filename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).restoreResource(this, tagId, filename);
    }

    /**
     * Removes an access control entry of a griven principal from a given resource.<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user)
     * @param principalName name of the principal
     * @throws CmsException if something goes wrong
     */
    public void rmacc(String resourceName, String principalType, String principalName) throws CmsException {

        CmsResource res = readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        I_CmsPrincipal principal = null;

        if ("group".equals(principalType.toLowerCase())) {
            principal = readGroup(principalName);
        } else if ("user".equals(principalType.toLowerCase())) {
            principal = readUser(principalName);
        }

        m_driverManager.removeAccessControlEntry(m_context, res, principal.getId());
    }

    /**
     * Returns the root-folder object.
     *
     * @return the root-folder object.
     * @throws CmsException if operation was not successful.
     */
    public CmsFolder rootFolder() throws CmsException {
        return (readFolder(I_CmsConstants.C_ROOT));
    }

    /**
     * Send a broadcast message to all currently logged in users.<p>
     * 
     * This method is only allowed for administrators.
     * 
     * @param message the message to send
     * @throws CmsException if something goes wrong
     */
    public void sendBroadcastMessage(String message) throws CmsException {
        if (isAdmin()) {
            if (m_sessionStorage != null) {
                m_sessionStorage.sendBroadcastMessage(message);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] sendBroadcastMessage()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Sets the name of the current site root of the content objects system
     */
    public void setContextToCos() {
        getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_COS);
    }
    
    /**
     * Set a new name for a task.<p>
     *
     * @param taskId the id of the task
     * @param name the new name of the task
     * @throws CmsException if something goes wrong
     */
    public void setName(int taskId, String name) throws CmsException {
        m_driverManager.setName(m_context, taskId, name);
    }

    /**
     * Sets a new parent-group for an already existing group in the Cms.
     *
     * @param groupName the name of the group that should be written to the Cms.
     * @param parentGroupName the name of the parentGroup to set, or null if the parent
     * group should be deleted.
     * @throws CmsException  if operation was not successfull.
     */
    public void setParentGroup(String groupName, String parentGroupName) throws CmsException {
        m_driverManager.setParentGroup(m_context, groupName, parentGroupName);
    }

    /**
     * Sets the password for a user.
     *
     * @param username the name of the user.
     * @param newPassword the new password.
     *
     * @throws CmsException if operation was not successful.
     */
    public void setPassword(String username, String newPassword) throws CmsException {
        m_driverManager.setPassword(m_context, username, newPassword);
    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * @throws CmsException if the user data could not be read from the database
     */
    public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {
        m_driverManager.resetPassword(username, oldPassword, newPassword);
    }

    /**
     * Sets the priority of a task.<p>
     *
     * @param taskId the id of the task
     * @param priority the new priority value
     * @throws CmsException if something goes wrong
     */
    public void setPriority(int taskId, int priority) throws CmsException {
        m_driverManager.setPriority(m_context, taskId, priority);
    }

    /**
     * Sets the recovery password for a user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old (current) password
     * @param newPassword the new recovery password
     * @throws CmsException if something goes wrong
     */
    public void setRecoveryPassword(String username, String oldPassword, String newPassword) throws CmsException {
        m_driverManager.setRecoveryPassword(username, oldPassword, newPassword);
    }

    /**
     * Set a parameter for a task.<p>
     *
     * @param taskid the Id of the task
     * @param parname the ame of the parameter
     * @param parvalue the value of the parameter
     * @throws CmsException if something goes wrong
     */
    public void setTaskPar(int taskid, String parname, String parvalue) throws CmsException {
        m_driverManager.setTaskPar(taskid, parname, parvalue);
    }

    /**
     * Sets the timeout of a task.<p>
     *
     * @param taskId the id of the task
     * @param timeout the new timeout value
     * @throws CmsException if something goes wrong
     */
    public void setTimeout(int taskId, long timeout) throws CmsException {
        m_driverManager.setTimeout(m_context, taskId, timeout);
    }

    /**
     * Change the timestamp of a resource.<p>
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param touchRecursive if true, touch recursively all sub-resources (only for folders)
     * @param user the user who is inserted as userladtmodified 
     * @throws CmsException if something goes wrong
     */
    public void touch(String resourceName, long timestamp, boolean touchRecursive, CmsUUID user) throws CmsException {
        getResourceType(readFileHeader(resourceName, CmsResourceFilter.IGNORE_EXPIRATION).getType()).touch(this, resourceName, timestamp, touchRecursive, user);
    }

    /**
     * Change the timestamp of a resource.<p>
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param touchRecursive if true, touch recursively all sub-resources (only for folders)
     * @throws CmsException if something goes wrong
     */
    public void touch(String resourceName, long timestamp, boolean touchRecursive) throws CmsException {
        touch(resourceName, timestamp, touchRecursive, getRequestContext().currentUser().getId());
    }

    /**
     * Undeletes a resource.
     *
     * @param filename the complete path of the file.
     *
     * @throws CmsException if the file couldn't be undeleted, or if the user
     * has not the appropriate rights to undelete the file.
     */
    public void undeleteResource(String filename) throws CmsException {
        //read the file header including deleted
        getResourceType(readFileHeader(filename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).undeleteResource(this, filename);
    }

    /**
     * Undo changes in a file by copying the online file.
     *
     * @param filename the complete path of the file.
     * @param recursive if true, undo changed recursively all sub-resources (only for folders)
     * @throws CmsException if the file couldn't be deleted, or if the user
     * has not the appropriate rights to write the file.
     */
    public void undoChanges(String filename, boolean recursive) throws CmsException {
        //read the file header including deleted
        getResourceType(readFileHeader(filename, CmsResourceFilter.IGNORE_EXPIRATION).getType()).undoChanges(this, filename, recursive);
    }

    /**
     * Unlocks all resources of a project.
     *
     * @param id the id of the project to be unlocked.
     *
     * @throws CmsException if operation was not successful.
     */
    public void unlockProject(int id) throws CmsException {
        m_driverManager.unlockProject(m_context, id);
    }

    /**
     * Unlocks a resource.<p>
     *
     * @param resource the complete path to the resource to be unlocked
     * @param forceRecursive if true, also unlock all sub-resources (of a folder) 
     * @throws CmsException if the user has no write permission for the resource
     */
    public void unlockResource(String resource, boolean forceRecursive) throws CmsException {
        getResourceType(readFileHeader(resource, CmsResourceFilter.IGNORE_EXPIRATION).getType()).unlockResource(this, resource, forceRecursive);
    }

    /**
     * Tests, if a user is member of the given group.
     *
     * @param username the name of the user to test.
     * @param groupname the name of the group to test.
     * @return <code>true</code>, if the user is in the group; <code>else</code> false otherwise.
     *
     * @throws CmsException if operation was not successful.
     */
    public boolean userInGroup(String username, String groupname) throws CmsException {
        return (m_driverManager.userInGroup(m_context, username, groupname));
    }

    /**
     * Writes access control entries for a given resource
     * 
     * @param resource the resource to attach the control entries to
     * @param acEntries a vector of access control entries
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsResource resource, Vector acEntries) throws CmsException {
        m_driverManager.importAccessControlEntries(m_context, resource, acEntries);
    }

    /**
     * Writes the Crontable.<p>
     *
     * <B>Security:</B>
     * Only a administrator can do this.<p>
     *
     * @param crontable the crontable to write
     * @throws CmsException if something goes wrong
     */
    public void writeCronTable(String crontable) throws CmsException {
        m_driverManager.writeCronTable(m_context, crontable);
    }

    /**
     * Writes the package for the system.<p>
     * 
     * This path is used for db-export and db-import as well as module packages.<p>
     *
     * @param path the package path
     * @throws CmsException if operation ws not successful
     */
    public void writePackagePath(String path) throws CmsException {
        m_driverManager.writePackagePath(m_context, path);
    }

    /**
     * Writes a file to the OpenCms VFS.<p>
     *
     * @param file the file to write
     * @return the written file
     * @throws CmsException if something goes wrong
     */
    public CmsFile writeFile(CmsFile file) throws CmsException {
       
        return getResourceType(file.getType()).writeFile(this, file);
    }

    /**
     * Writes the file extension mappings.<p>
     *
     * <B>Security:</B>
     * Only the admin user is allowed to write file extensions.
     *
     * @param extensions holds extensions as keys and resourcetypes (Strings) as values
     * @throws CmsException if something goes wrong
     */
    public void writeFileExtensions(Hashtable extensions) throws CmsException {
        m_driverManager.writeFileExtensions(m_context, extensions);
    }

    /**
     * Writes a file-header to the Cms.
     *
     * @param file the file to write.
     *
     * @throws CmsException if resourcetype is set to folder. The CmsException will also be thrown,
     * if the user has not the rights to write the file header..
     */
    public void writeFileHeader(CmsFile file) throws CmsException {
        m_driverManager.writeFileHeader(m_context, file);
    }

    /**
     * Writes an already existing group to the Cms.
     *
     * @param group the group that should be written to the Cms.
     * @throws CmsException  if operation was not successful.
     */
    public void writeGroup(CmsGroup group) throws CmsException {
        m_driverManager.writeGroup(m_context, group);
    }

    /**
     * Writes the Linkchecktable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param linkchecktable The hashtable that contains the links that were not reachable
     * @throws CmsException if something goes wrong
     */
    public void writeLinkCheckTable(Hashtable linkchecktable) throws CmsException {
        m_driverManager.writeLinkCheckTable(m_context, linkchecktable);
    }

    /**
     * Writes a couple of properties as structure values for a file or folder.
     *
     * @param resourceName the resource-name of which the Property has to be set.
     * @param properties a Hashtable with property-definitions and property values as Strings.
     * @throws CmsException if operation was not successful
     * @deprecated use {@link #writePropertyObjects(String, List)} instead
     */
    public void writeProperties(String resourceName, Map properties) throws CmsException {
        m_driverManager.writePropertyObjects(m_context, addSiteRoot(resourceName), CmsProperty.toList(properties));
    }

    /**
     * Writes a couple of Properties for a file or folder.
     *
     * @param name the resource-name of which the Property has to be set.
     * @param properties a Hashtable with property-definitions and property values as Strings.
     * @param addDefinition flag to indicate if unknown definitions should be added
     * @throws CmsException if operation was not successful.
     * @deprecated use {@link #writePropertyObjects(String, List)} instead
     */
    public void writeProperties(String name, Map properties, boolean addDefinition) throws CmsException {
        m_driverManager.writePropertyObjects(m_context, addSiteRoot(name), CmsProperty.setAutoCreatePropertyDefinitions(CmsProperty.toList(properties), addDefinition));
    }
    
    /**
     * Writes a property as a structure value for a file or folder.<p>
     *
     * @param resourceName the resource-name for which the property will be set
     * @param key the property-definition name
     * @param value the value for the property to be set
     * @throws CmsException if operation was not successful
     * @deprecated use {@link #writePropertyObject(String, CmsProperty)} instead
     */
    public void writeProperty(String resourceName, String key, String value) throws CmsException {
        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(value);
        
        m_driverManager.writePropertyObject(m_context, addSiteRoot(resourceName), property);        
    }

    /**
     * Writes a property for a file or folder.
     *
     * @param name the resource-name for which the property will be set.
     * @param key the property-definition name.
     * @param value the value for the property to be set.
     * @param addDefinition flag to indicate if unknown definitions should be added
     * @throws CmsException if operation was not successful.
     * @deprecated use {@link #writePropertyObject(String, CmsProperty)} instead
     */
    public void writeProperty(String name, String key, String value, boolean addDefinition) throws CmsException {
        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(value);
        property.setAutoCreatePropertyDefinition(addDefinition);
        
        m_driverManager.writePropertyObject(m_context, addSiteRoot(name), property); 
    }

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(String resourceName, int linkType, String linkParameter, long timestamp) throws CmsException {
        m_driverManager.writeStaticExportPublishedResource(m_context, resourceName, linkType, linkParameter, timestamp);
    }

    /**
     * Writes a new user tasklog for a task.
     *
     * @param taskid the Id of the task.
     * @param comment the description for the log.
     *
     * @throws CmsException if operation was not successful.
     */
    public void writeTaskLog(int taskid, String comment) throws CmsException {
        m_driverManager.writeTaskLog(m_context, taskid, comment);
    }

    /**
     * Writes a new user tasklog for a task.
     *
     * @param taskId the id of the task 
     * @param comment the description for the log
     * @param taskType the type of the tasklog, user task types must be greater than 100
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(int taskId, String comment, int taskType) throws CmsException {
        m_driverManager.writeTaskLog(m_context, taskId, comment, taskType);
    }

    /**
     * Updates the user information.
     * <p>
     * <b>Security:</b>
     * Only the admin user is allowed to update the user information.
     *
     * @param user the user to be written.
     *
     * @throws CmsException if operation was not successful.
     */
    public void writeUser(CmsUser user) throws CmsException {
        m_driverManager.writeUser(m_context, user);
    }

    /**
     * Updates the user information of a web user.
     * <br>
     * Only a web user can be updated this way.
     *
     * @param user the user to be written.
     *
     * @throws CmsException if operation was not successful.
     */
    public void writeWebUser(CmsUser user) throws CmsException {
        m_driverManager.writeWebUser(user);
    }
    
    /**
     * Gets the lock state for a specified resource.<p>
     * 
     * @param resource the specified resource
     * @return the CmsLock object for the specified resource
     * @throws CmsException if somethong goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {
        return m_driverManager.getLock(m_context, resource);
    }    
    
    /**
     * Gets the lock state for a specified resource name.<p>
     * 
     * @param resourcename the specified resource name
     * @return the CmsLock object for the specified resource name
     * @throws CmsException if somethong goes wrong
     */
    public CmsLock getLock(String resourcename) throws CmsException {
        return m_driverManager.getLock(m_context, m_context.addSiteRoot(resourcename));
    }
    
    /**
     * Proves if a specified resource is inside the current project.<p>
     * 
     * @param resource the specified resource
     * @return true, if the resource name of the specified resource matches any of the current project's resources
     */
    public boolean isInsideCurrentProject(CmsResource resource) {
        return m_driverManager.isInsideCurrentProject(m_context, resource);
    }      
    
    /**
     * Returns the list of all resources that define the "view" of the given project.<p>
     * 
     * @param project the project to get the project resources for
     * @return the list of all resources that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsProject project) throws CmsException {
        return m_driverManager.readProjectResources(project);
    }
        
    /**
     * Recovers a resource from the online project back to the 
     * offline project as an unchanged resource.<p>
     * 
     * @param resourcename the name of the resource which is recovered
     * @return the recovered resource in the offline project
     * @throws CmsException if somethong goes wrong
     */
    public CmsResource recoverResource(String resourcename) throws CmsException {
        return m_driverManager.recoverResource(m_context, m_context.addSiteRoot(resourcename));        
    }
    
    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class configured in opencms.properties.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     *
     * @throws CmsSecurityException if the password is not valid
     */    
    public void validatePassword(String password) throws CmsSecurityException {
        m_driverManager.validatePassword(password);
    }           

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * @return a List of CmsPublishedResource objects
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsUUID publishHistoryId) throws CmsException {
        return m_driverManager.readPublishedResources(m_context, publishHistoryId);
    }
    
    /**
     * Completes all post-publishing tasks for a "directly" published COS resource.<p>
     * 
     * @param publishedBoResource the CmsPublishedResource onject representing the published COS resource
     * @param publishId unique int ID to identify each publish task in the publish history
     * @param tagId the backup tag revision
     */
    public void postPublishBoResource(CmsPublishedResource publishedBoResource, CmsUUID publishId, int tagId) {
        try {
            m_driverManager.postPublishBoResource(m_context, publishedBoResource, publishId, tagId);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error writing publish history entry for COS resource " + publishedBoResource.toString(), e);
            }
        } finally {
            Map eventData = new HashMap();
            eventData.put("publishHistoryId", publishId.toString());
            
            // a "directly" published COS resource can be handled totally equal to a published project            
            OpenCms.fireCmsEvent(new CmsEvent(this, I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData));
        }
    }
    
    /**
     * Validates the HTML links (hrefs and images) in all unpublished Cms files of the current 
     * (offline) project, if the files resource types implement the interface 
     * {@link org.opencms.validation.I_CmsHtmlLinkValidatable}.<p>
     * 
     * Please refer to the Javadoc of the I_CmsHtmlLinkValidatable interface to see which classes
     * implement this interface (and so, which file types get validated by the HTML link 
     * validator).<p>
     * 
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable
     */
    public Map validateHtmlLinks(I_CmsReport report) throws Exception {
        CmsPublishList publishList = m_driverManager.getPublishList(m_context, null, false, report);
        return m_driverManager.validateHtmlLinks(this, publishList, report);    
    }
    
    /**
     * Validates the HTML links (hrefs and images) in the unpublished Cms file of the current 
     * (offline) project, if the file's resource type implements the interface 
     * {@link org.opencms.validation.I_CmsHtmlLinkValidatable}.<p>
     * 
     * Please refer to the Javadoc of the I_CmsHtmlLinkValidatable interface to see which classes
     * implement this interface (and so, which file types get validated by the HTML link 
     * validator).<p>
     * 
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings true, if all eventual siblings of the direct published resource should also get published
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable
     */
    public Map validateHtmlLinks(CmsResource directPublishResource, boolean directPublishSiblings, I_CmsReport report) throws Exception {
        CmsPublishList publishList = null;
        Map result = null;
                   
        publishList = m_driverManager.getPublishList(m_context, directPublishResource, directPublishSiblings, report);
        result = m_driverManager.validateHtmlLinks(this, publishList, report);
        
        return result;
    }
    
    /**
     * Validates the HTML links (hrefs and images) in the unpublished Cms files of the specified
     * Cms publish list, if the files resource types implement the interface 
     * {@link org.opencms.validation.I_CmsHtmlLinkValidatable}.<p>
     * 
     * Please refer to the Javadoc of the I_CmsHtmlLinkValidatable interface to see which classes
     * implement this interface (and so, which file types get validated by the HTML link 
     * validator).<p>
     * 
     * @param publishList a Cms publish list
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable
     */    
    public Map validateHtmlLinks(CmsPublishList publishList, I_CmsReport report) throws Exception {       
        return m_driverManager.validateHtmlLinks(this, publishList, report);  
    }
    
    /**
     * Returns a publish list for the specified Cms resource to be published directly, plus 
     * optionally it's siblings.<p>
     * 
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings true, if all eventual siblings of the direct published resource should also get published
     * @param report an instance of I_CmsReport to print messages
     * @return a publish list
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(CmsResource directPublishResource, boolean directPublishSiblings, I_CmsReport report) throws CmsException {
        return m_driverManager.getPublishList(m_context, directPublishResource, directPublishSiblings, report);
    }
    
    /**
     * Returns a publish list with all new/changed/deleted Cms resources of the current (offline)
     * project that actually get published.<p>
     * 
     * @param report an instance of I_CmsReport to print messages
     * @return a publish list
     * @throws Exception if something goes wrong
     */
    public CmsPublishList getPublishList(I_CmsReport report) throws Exception {
        return getPublishList(null, false, report);
    }    
    
    /**
     * Reads a property object from the database specified by it's key name mapped to a resource.<p>
     * 
     * Returns {@link CmsProperty#getNullProperty()} if the property is not found.<p>
     * 
     * @param resourceName the name of resource where the property is mapped to
     * @param propertyName the property key name
     * @param search true, if the property should be searched on all parent folders  if not found on the resource
     * @return a CmsProperty object containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */    
    public CmsProperty readPropertyObject(String resourceName, String propertyName, boolean search) throws CmsException {
        return m_driverManager.readPropertyObject(m_context, m_context.addSiteRoot(resourceName), m_context.getAdjustedSiteRoot(resourceName), propertyName, search);
    }
    
    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * Returns an empty list if no properties are found at all.<p>
     * 
     * @param resourceName the name of resource where the property is mapped to
     * @param search true, if the properties should be searched on all parent folders  if not found on the resource
     * @return a list of CmsProperty objects containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */    
    public List readPropertyObjects(String resourceName, boolean search) throws CmsException {
        return m_driverManager.readPropertyObjects(m_context, addSiteRoot(resourceName), m_context.getAdjustedSiteRoot(resourceName), search);
    }
    
    /**
     * Writes a property object to the database mapped to a specified resource.<p>
     * 
     * @param resourceName the name of resource where the property is mapped to
     * @param property a CmsProperty object containing a structure and/or resource value
     * @throws CmsException if something goes wrong
     */    
    public void writePropertyObject(String resourceName, CmsProperty property) throws CmsException {
        m_driverManager.writePropertyObject(m_context, addSiteRoot(resourceName), property);
    }
    
    /**
     * Writes a list of property objects to the database mapped to a specified resource.<p>
     * 
     * @param resourceName the name of resource where the property is mapped to
     * @param properties a list of CmsPropertys object containing a structure and/or resource value
     * @throws CmsException if something goes wrong
     */    
    public void writePropertyObjects(String resourceName, List properties) throws CmsException {
        m_driverManager.writePropertyObjects(m_context, addSiteRoot(resourceName), properties);
    }      
    
    /**
     * Reads the (compound) values of all properties mapped to a specified resource.<p>
     * 
     * @param resource the resource to look up the property for
     * @return Map of Strings representing all properties of the resource
     * @throws CmsException in case there where problems reading the properties
     * @deprecated use {@link #readPropertyObjects(String, boolean)} instead
     */
    public Map readProperties(String resource) throws CmsException {
        List properties = m_driverManager.readPropertyObjects(m_context, m_context.addSiteRoot(resource), m_context.getAdjustedSiteRoot(resource), false);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) values of all properties mapped to a specified resource
     * with optional direcory upward cascading.<p>
     * 
     * @param resource the resource to look up the property for
     * @param search if <code>true</code>, the properties will also be looked up on all parent folders and the results will be merged, if <code>false</code> not (ie. normal property lookup)
     * @return Map of Strings representing all properties of the resource
     * @throws CmsException in case there where problems reading the properties
     * @deprecated use {@link #readPropertyObjects(String, boolean)} instead
     */
    public Map readProperties(String resource, boolean search) throws CmsException {
        List properties = m_driverManager.readPropertyObjects(m_context, m_context.addSiteRoot(resource), m_context.getAdjustedSiteRoot(resource), search);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource.<p>
     *
     * @param resource the resource to look up the property for
     * @param property the name of the property to look up
     * @return the value of the property found, <code>null</code> if nothing was found
     * @throws CmsException in case there where problems reading the property
     * @see CmsProperty#getValue()
     * @deprecated use new Object based methods
     */
    public String readProperty(String resource, String property) throws CmsException {
        CmsProperty value = m_driverManager.readPropertyObject(m_context, m_context.addSiteRoot(resource), m_context.getAdjustedSiteRoot(resource), property, false);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional direcory upward cascading.<p>
     * 
     * @param resource the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if false not (ie. normal property lookup)
     * @return the value of the property found, <code>null</code> if nothing was found
     * @throws CmsException in case there where problems reading the property
     * @see CmsProperty#getValue()
     * @deprecated use new Object based methods
     */
    public String readProperty(String resource, String property, boolean search) throws CmsException {
        CmsProperty value = m_driverManager.readPropertyObject(m_context, m_context.addSiteRoot(resource), m_context.getAdjustedSiteRoot(resource), property, search);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional direcory upward cascading, a default value will be returned if the property 
     * is not found on the resource (or it's parent folders in case search is set to <code>true</code>).<p>
     * 
     * @param resource the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if <code>false</code> not (ie. normal property lookup)
     * @param propertyDefault a default value that will be returned if the property was not found on the selected resource
     * @return the value of the property found, if nothing was found the value of the <code>propertyDefault</code> parameter is returned
     * @throws CmsException in case there where problems reading the property
     * @see CmsProperty#getValue()
     * @deprecated use new Object based methods
     */
    public String readProperty(String resource, String property, boolean search, String propertyDefault) throws CmsException {
        CmsProperty value = m_driverManager.readPropertyObject(m_context, m_context.addSiteRoot(resource), m_context.getAdjustedSiteRoot(resource), property, search);
        return value.isNullProperty() ? propertyDefault : value.getValue();
    }    
}
