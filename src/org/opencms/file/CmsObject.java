/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsObject.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.109 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTaskService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * @author Michael Moossen (m.mmoossen@alkacon.com)
 * 
 * @version $Revision: 1.109 $
 */
/**
 * Comment for <code>CmsObject</code>.<p>
 */
public class CmsObject {

    /**
     * The request context.
     */
    protected CmsRequestContext m_context;

    /**
     * The security manager to access the cms.
     */
    protected CmsSecurityManager m_securityManager;

    /**
     * Connects an OpenCms user context to a running database.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentification
     */
    public CmsObject(CmsSecurityManager securityManager, CmsRequestContext context) {

        init(securityManager, context);
    }

    /**
     * Returns the current session info manager object.<p>
     * 
     * @return the current session info manager object
     */
    public CmsTaskService getTaskService() {

        return new CmsTaskService(m_context, m_securityManager);
    }

    /**
     * Checks if the user can access a given project.<p>
     *
     * @param projectId the id of the project
     * 
     * @return <code>true</code>, if the user may access this project; <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful
     */
    public boolean accessProject(int projectId) throws CmsException {

        return (m_securityManager.accessProject(m_context, projectId));
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param description the description for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param flags the flags for a user (e.g. <code>{@link I_CmsConstants#C_FLAG_ENABLED}</code>)
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user. These
     *                      infos may be stored into the Usertables (depending on the implementation)
     * @param address the address of the user
     * @param type the type of the user
     *
     * @return a new <code>{@link CmsUser}</code> object representing the added user
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser addImportUser(
        String id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        int flags,
        Map additionalInfos,
        String address,
        int type) throws CmsException {

        return m_securityManager.addImportUser(
            m_context,
            id,
            name,
            password,
            description,
            firstname,
            lastname,
            email,
            flags,
            additionalInfos,
            address,
            type);
    }

    /**
     * Creates a new user.<p>
     * 
     * @param name the name for the new user
     * @param password the password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user
     * 
     * @return the newly created user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser addUser(String name, String password, String group, String description, Map additionalInfos)
    throws CmsException {

        return m_securityManager.addUser(m_context, name, password, group, description, additionalInfos);
    }

    /**
     * Adds a user to a group.<p>
     * 
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if something goes wrong
     */
    public void addUserToGroup(String username, String groupname) throws CmsException {

        m_securityManager.addUserToGroup(m_context, username, groupname);
    }

    /**
     * Creates a new web user.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<br>
     * 
     * Moreover, a web user can be created by any user, the intention being that
     * a "Guest" user can create a personalized account for himself.<p>
     *
     * @param name the name for the new web user
     * @param password the password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user
     * 
     * @return the newly created user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser addWebUser(String name, String password, String group, String description, Map additionalInfos)
    throws CmsException {

        return m_securityManager.addWebUser(m_context, name, password, group, description, additionalInfos);
    }

    /**
     * Creates a backup of the published project.<p>
     *
     * @param projectId the id of the project in which the resource was published
     * @param versionId the version of the backup
     * @param publishDate the date of publishing
     *
     * @throws CmsException if operation was not succesful
     */
    public void backupProject(int projectId, int versionId, long publishDate) throws CmsException {

        CmsProject backupProject = m_securityManager.readProject(projectId);
        m_securityManager.backupProject(m_context, backupProject, versionId, publishDate);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#C_PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#C_PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param allowedPermissions bitset of allowed permissions
     * @param deniedPermissions bitset of denied permissions
     * @param flags additional flags of the access control entry
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(
        String resourceName,
        String principalType,
        String principalName,
        int allowedPermissions,
        int deniedPermissions,
        int flags) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        CmsAccessControlEntry acEntry = null;
        I_CmsPrincipal principal = null;

        if (I_CmsPrincipal.C_PRINCIPAL_GROUP.equalsIgnoreCase(principalType)) {
            principal = readGroup(principalName);
            acEntry = new CmsAccessControlEntry(
                res.getResourceId(),
                principal.getId(),
                allowedPermissions,
                deniedPermissions,
                flags);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_GROUP);
        } else if (I_CmsPrincipal.C_PRINCIPAL_USER.equalsIgnoreCase(principalType)) {
            principal = readUser(principalName);
            acEntry = new CmsAccessControlEntry(
                res.getResourceId(),
                principal.getId(),
                allowedPermissions,
                deniedPermissions,
                flags);
            acEntry.setFlags(I_CmsConstants.C_ACCESSFLAGS_USER);
        }

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#C_PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#C_PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param permissionString the permissions in the format ((+|-)(r|w|v|c|i|o))*
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(String resourceName, String principalType, String principalName, String permissionString)
    throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
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

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * This information is used while publishing. Only resources inside the 
     * project folders that are new/modified/changed <i>and</i> that "belong" 
     * to the project (i.e. have the id of the project set) are published
     * with the project.<p>
     * 
     * @param resourcename the name of the resource to change the project id for (full path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeLastModifiedProjectId(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).changeLastModifiedProjectId(this, m_securityManager, resource);
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * This is the "steal lock" operation.<p>
     * 
     * @param resourcename the name of the resource to change the lock with complete path
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeLock(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).changeLock(this, m_securityManager, resource);
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property, 
     * matching the current property's value with the given old value and replacing it by a given new value.<p>
     *
     * @param resourcename the name of the resource to change the property value
     * @param property the name of the property to change the value
     * @param oldValue the old value of the property, can be a regular expression
     * @param newValue the new value of the property
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     *
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed
     *
     * @throws CmsException if operation was not successful
     */
    public List changeResourcesInFolderWithProperty(
        String resourcename,
        String property,
        String oldValue,
        String newValue,
        boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.changeResourcesInFolderWithProperty(
            m_context,
            resource,
            property,
            oldValue,
            newValue,
            recursive);
    }

    /**
     * Changes the type of a user given its id.<p>
     *
     * @param userId The id of the user to change
     * @param userType The new type of the user
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsUUID userId, int userType) throws CmsException {

        m_securityManager.changeUserType(m_context, userId, userType);
    }

    /**
     * Changes the type of a user given its name.<p>
     *
     * @param username The name of the user to change
     * @param userType The new type of the user
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(String username, int userType) throws CmsException {

        m_securityManager.changeUserType(m_context, username, userType);
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     *
     * @param resourcename the name of the resource to change the flags for (full path)
     * @param flags the new flags for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chflags(String resourcename, int flags) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).chflags(this, m_securityManager, resource, flags);
    }

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resources according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image, etc.<p> 
     *
     * @param resourcename the name of the resource to change the type for (full path)
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chtype(String resourcename, int type) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).chtype(this, m_securityManager, resource, type);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * Siblings will be treated according to the
     * <code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code> mode.<p>
     * 
     * @param source the name of the resource to copy (full path)
     * @param destination the name of the copy destination (full path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #copyResource(String, String, int)
     */
    public void copyResource(String source, String destination) throws CmsException {

        copyResource(source, destination, I_CmsConstants.C_COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param source the name of the resource to copy (full path)
     * @param destination the name of the copy destination (full path)
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyResource(String source, String destination, int siblingMode) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).copyResource(this, m_securityManager, resource, destination, siblingMode);
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that the resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param resourcename the name of the resource to copy to the current project (full path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyResourceToProject(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).copyResourceToProject(this, m_securityManager, resource);
    }

    /**
     * Counts the locked resources in a project.<p>
     *
     * @param id the id of the project
     * 
     * @return the number of locked resources in this project
     *
     * @throws CmsException if operation was not successful
     */
    public int countLockedResources(int id) throws CmsException {

        return m_securityManager.countLockedResources(m_context, id);
    }

    /**
     * Counts the locked resources within a folder.<p>
     *
     * @param foldername the name of the folder
     * 
     * @return the number of locked resources in this folder
     *
     * @throws CmsException if operation was not successful
     */
    public int countLockedResources(String foldername) throws CmsException {

        return m_securityManager.countLockedResources(m_context, addSiteRoot(foldername));
    }

    /**
     * Copies access control entries of a given resource to another resource.<p>
     * 
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param sourceName the name of the resource of which the access control entries are copied
     * @param destName the name of the resource to which the access control entries are applied
     * 
     * @throws CmsException if something goes wrong
     */
    public void cpacc(String sourceName, String destName) throws CmsException {

        CmsResource source = readResource(sourceName);
        CmsResource dest = readResource(destName);
        m_securityManager.copyAccessControlEntries(m_context, source, dest);
    }

    /**
     * Creates a new user group.<p>
     * 
     * @param name the name of the new group
     * @param description the description of the new group
     * @param flags the flags for the new group
     * @param parent the parent group (or <code>null</code>)
     *
     * @return a <code>{@link CmsGroup}</code> object representing the newly created group
     *
     * @throws CmsException if operation was not successful
     */
    public CmsGroup createGroup(String name, String description, int flags, String parent) throws CmsException {

        return m_securityManager.createGroup(m_context, name, description, flags, parent);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * 
     * @return the created project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(String name, String description, String groupname, String managergroupname)
    throws CmsException {

        return m_securityManager.createProject(
            m_context,
            name,
            description,
            groupname,
            managergroupname,
            I_CmsConstants.C_PROJECT_TYPE_NORMAL);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @param projecttype the type of the project (normal or temporary)
     * 
     * @return the created project
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject createProject(
        String name,
        String description,
        String groupname,
        String managergroupname,
        int projecttype) throws CmsException {

        return m_securityManager.createProject(m_context, name, description, groupname, managergroupname, projecttype);
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition createPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.createPropertyDefinition(m_context, name));
    }

    /**
     * Creates a new resource of the given resource type with 
     * empty content and no properties.<p>
     * 
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #createResource(String, int, byte[], List)
     */
    public CmsResource createResource(String resourcename, int type) throws CmsException {

        return createResource(resourcename, type, new byte[0], Collections.EMPTY_LIST);
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param properties the properties for the new resource
     * @param content the contents for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(String resourcename, int type, byte[] content, List properties)
    throws CmsException {

        return getResourceType(type).createResource(this, m_securityManager, resourcename, content, properties);
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param source the name of the resource to create a sibling for with complete path
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     */
    public void createSibling(String source, String destination, List properties) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).createSibling(this, m_securityManager, resource, destination, properties);
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject() throws CmsException {

        return m_securityManager.createTempfileProject(m_context);
    }

    /**
     * Deletes all published resource entries.<p>
     * 
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(int linkType) throws CmsException {

        m_securityManager.deleteAllStaticExportPublishedResources(m_context, linkType);
    }

    /**
     * Deletes the versions from the backup tables that are older then the given timestamp  
     * and/or number of remaining versions.<p>
     * 
     * The number of verions always wins, i.e. if the given timestamp would delete more versions 
     * than given in the versions parameter, the timestamp will be ignored. <p>
     * 
     * Deletion will delete file header, content and properties. <p>
     * 
     * @param timestamp timestamp which defines the date after which backup resources must be deleted
     * @param versions the number of versions per file which should kept in the system
     * @param report the report for output logging
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteBackups(long timestamp, int versions, I_CmsReport report) throws CmsException {

        m_securityManager.deleteBackups(m_context, timestamp, versions, report);
    }

    /**
     * Deletes a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param delgroup the name of the group
     * 
     * @throws CmsException  if operation was not successful
     */
    public void deleteGroup(String delgroup) throws CmsException {

        m_securityManager.deleteGroup(m_context, delgroup);
    }

    /**
     * Deletes a project.<p>
     *
     * All resources inside the project have to be be reset to their online state.<p>
     * 
     * @param id the id of the project to delete
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteProject(int id) throws CmsException {

        m_securityManager.deleteProject(m_context, id);
    }

    /**
     * Deletes a property for a file or folder.<p>
     *
     * @param resourcename the name of a resource for which the property should be deleted
     * @param key the name of the property
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void deleteProperty(String resourcename, String key) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(CmsProperty.C_DELETE_VALUE);

        writePropertyObject(resourcename, property);
    }

    /**
     * Deletes a property definition.<p>
     *
     * @param name the name of the property definition to delete
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertyDefinition(String name) throws CmsException {

        m_securityManager.deletePropertyDefinition(m_context, name);
    }

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteResource(String resourcename, int siblingMode) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).deleteResource(this, m_securityManager, resource, siblingMode);
    }

    /**
     * Deletes a published resource entry.<p>
     * 
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(String resourceName, int linkType, String linkParameter)
    throws CmsException {

        m_securityManager.deleteStaticExportPublishedResource(m_context, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes a user.<p>
     *
     * @param userId the id of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsUUID userId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId);
    }

    /**
     * Deletes a user.<p>
     * 
     * @param username the name of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(String username) throws CmsException {

        m_securityManager.deleteUser(m_context, username);
    }

    /**
     * Deletes a web user.<p>
     *
     * @param userId the id of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteWebUser(CmsUUID userId) throws CmsException {

        m_securityManager.deleteWebUser(m_context, userId);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full path)
     *
     * @return <code>true</code> if the resource is available
     *
     * @see #readResource(String)
     * @see #existsResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename) {

        return existsResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>  
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full path)
     * @param filter the resource filter to use while checking
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see #readResource(String)
     * @see #readResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename, CmsResourceFilter filter) {

        return m_securityManager.existsResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(String resourceName) throws CmsException {

        return getAccessControlEntries(resourceName, true);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * @param getInherited <code>true</code>, if inherited access control entries should be returned, too
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(String resourceName, boolean getInherited) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlEntries(m_context, res, getInherited);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName) throws CmsException {

        return getAccessControlList(resourceName, false);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * If <code>inheritedOnly</code> is set, only inherited access control entries are returned.<p>
     * 
     * @param resourceName the name of the resource
     * @param inheritedOnly if set, the non-inherited entries are skipped
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName, boolean inheritedOnly) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlList(m_context, res, inheritedOnly);
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * @return a list of objects of type <code>{@link CmsProject}</code>
     *
     * @throws CmsException if operation was not successful
     */
    public List getAllAccessibleProjects() throws CmsException {

        return m_securityManager.getAllAccessibleProjects(m_context);
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @return list of <code>{@link CmsBackupProject}</code> objects 
     *           with all projects from history.
     *
     * @throws CmsException  if operation was not succesful
     */
    public List getAllBackupProjects() throws CmsException {

        return m_securityManager.getAllBackupProjects(m_context);
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @return a list of objects of type <code>{@link CmsProject}</code>
     *
     * @throws CmsException if operation was not successful
     */
    public List getAllManageableProjects() throws CmsException {

        return m_securityManager.getAllManageableProjects(m_context);
    }

    /**
     * Returns the next version id for the published backup resources.<p>
     *
     * @return int the new version id
     */
    public int getBackupTagId() {

        return m_securityManager.getBackupTagId(m_context);
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List getChild(String groupname) throws CmsException {

        return (m_securityManager.getChild(m_context, groupname));
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List getChilds(String groupname) throws CmsException {

        return (m_securityManager.getChilds(m_context, groupname));
    }

    /**
     * Gets the configurations of the OpenCms properties file.<p>
     * 
     * @return the configurations of the properties file
     */
    public ExtendedProperties getConfigurations() {

        return m_securityManager.getConfigurations();
    }

    /**
     * Returns all groups to which a given user directly belongs.<p>
     *
     * @param username the name of the user to get all groups for
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List getDirectGroupsOfUser(String username) throws CmsException {

        return (m_securityManager.getDirectGroupsOfUser(m_context, username));
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for. 
     * 
     * @return a list of all child file <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getFilesInFolder(String, CmsResourceFilter)
     */
    public List getFilesInFolder(String resourcename) throws CmsException {

        return getFilesInFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for. 
     * 
     * @return a list of all child file <code>{@link CmsResource}</code>s
     * @param filter the resource filter to use
     * 
     * @throws CmsException if something goes wrong
     */
    public List getFilesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, false, true);
    }

    /**
     * Returns all groups.<p>
     *
     * @return a list of all <code>{@link CmsGroup}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List getGroups() throws CmsException {

        return (m_securityManager.getGroups(m_context));
    }

    /**
     * Returns all the groups the given user, directly or indirectly, belongs to.<p>
     *
     * @param username the name of the user
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(String username) throws CmsException {

        return m_securityManager.getGroupsOfUser(m_context, username);
    }

    /**
     * Returns the groups of a user filtered by the specified IP address.<p>
     *
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result list
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects filtered by the specified IP address
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(String username, String remoteAddress) throws CmsException {

        return m_securityManager.getGroupsOfUser(m_context, username, remoteAddress);
    }

    /**
     * Returns the lock state for a specified resource.<p>
     * 
     * @param resource the resource to return the lock state for
     * 
     * @return the lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {

        return m_securityManager.getLock(m_context, resource);
    }

    /**
     * Returns the lock state for a specified resource name.<p>
     * 
     * @param resourcename the name if the resource to get the lock state for (full path)
     * 
     * @return the lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(String resourcename) throws CmsException {
        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return getLock(resource);
    }

    /**
     * Returns the name a resource would have if it were moved to the
     * "lost and found" folder. <p>
     * 
     * In general, it is the same name as the given resource has, the only exception is
     * if a resource in the "lost and found" folder with the same name already exists. 
     * In such case, a counter is added to the resource name.<p>
     * 
     * @param resourcename the name of the resource to get the "lost and found" name for (full path)
     *
     * @return the tentative name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveToLostAndFound(String)
     */
    public String getLostAndFoundName(String resourcename) throws CmsException {

        return m_securityManager.moveToLostAndFound(m_context, resourcename, true);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param groupname the name of the group
     * 
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup getParent(String groupname) throws CmsException {

        return m_securityManager.getParent(m_context, groupname);
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the bitset of the permissions of the current user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName) throws CmsException {

        // reading permissions is allowed even if the resource is marked as deleted
        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        CmsUser user = m_context.currentUser();

        return m_securityManager.getPermissions(m_context, resource, user);
    }

    /**
     * Returns the set of permissions of a given user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * @param userName the name of the user
     * 
     * @return the current permissions on this resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName, String userName) throws CmsException {

        CmsAccessControlList acList = getAccessControlList(resourceName);
        CmsUser user = readUser(userName);
        return acList.getPermissions(user, getGroupsOfUser(userName));
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published.<p>
     * 
     * @return a publish list
     * 
     * @throws Exception if something goes wrong
     */
    public CmsPublishList getPublishList() throws Exception {

        return getPublishList(null, false);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published.<p>
     * 
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct 
     *                      published resource should also get published.
     * 
     * @return a publish list
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(CmsResource directPublishResource, boolean directPublishSiblings)
    throws CmsException {

        return m_securityManager.getPublishList(m_context, directPublishResource, directPublishSiblings);
    }

    /**
     * Returns the current users request context.<p>
     *
     * This request context is used to authenticate the user for all 
     * OpenCms operations. It also contains the request runtime settings, e.g.
     * about the current site this request was made on.<p>
     *
     * @return the current users request context
     */
    public CmsRequestContext getRequestContext() {

        return m_context;
    }

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * This method is mainly used by the workplace explorer.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List getResourcesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, true);
    }

    /**
     * Returns a list with all sub resources of the given parent folder (and all of it's subfolders) 
     * that have been modified in the given time range.<p>
     * 
     * The result list is descending sorted (newest resource first).<p>
     *
     * @param folder the folder to get the subresources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * 
     * @return a list with all <code>{@link CmsResource}</code> objects 
     *               that have been modified in the given time range.
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesInTimeRange(String folder, long starttime, long endtime) throws CmsException {

        return m_securityManager.getResourcesInTimeRange(m_context, addSiteRoot(folder), starttime, endtime);
    }

    /**
     * Adjusts the absolute resource root path for the current site.<p> 
     * 
     * The full root path of a resource is always available using
     * <code>{@link CmsResource#getRootPath()}</code>. From this name this method cuts 
     * of the current site root using 
     * <code>{@link CmsRequestContext#removeSiteRoot(String)}</code>.<p>
     * 
     * @param resource the resource to get the adjusted site root path for
     * 
     * @return the absolute resource path adjusted for the current site
     * 
     * @see CmsRequestContext#removeSiteRoot(String)
     * @see CmsRequestContext#getSitePath(CmsResource)
     * @see CmsResource#getRootPath()
     */
    public String getSitePath(CmsResource resource) {

        return m_context.getSitePath(resource);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for. 
     * 
     * @return a list of all child file <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getSubFolders(String, CmsResourceFilter)
     */
    public List getSubFolders(String resourcename) throws CmsException {

        return getSubFolders(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for. 
     * 
     * @return a list of all child folder <code>{@link CmsResource}</code>s
     * @param filter the resource filter to use
     * 
     * @throws CmsException if something goes wrong
     */
    public List getSubFolders(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, false);
    }

    /**
     * Returns all users.<p>
     *
     * @return a list of all <code>{@link CmsUser}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List getUsers() throws CmsException {

        return m_securityManager.getUsers(m_context);
    }

    /**
     * Returns all users of the given type.<p>
     *
     * @param type the type of the users
     * 
     * @return a list of all <code>{@link CmsUser}</code> objects of the given type
     * 
     * @throws CmsException if operation was not successful
     */
    public List getUsers(int type) throws CmsException {

        return (m_securityManager.getUsers(m_context, type));
    }

    /**
     * Returns all users of a given group.<p>
     *
     * @param groupname the name of the group to get all users for
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List getUsersOfGroup(String groupname) throws CmsException {

        return (m_securityManager.getUsersOfGroup(m_context, groupname));
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {

        return CmsSecurityManager.PERM_ALLOWED == m_securityManager.hasPermissions(
            m_context,
            resource,
            requiredPermissions,
            true,
            CmsResourceFilter.ALL);
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * @param checkLock if <code>true</code>, a lock for the current user is required for 
     *      all write operations, if <code>false</code> it's ok to write as long as the resource
     *      is not locked by another user.
     * @param filter the resource filter to use
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        return CmsSecurityManager.PERM_ALLOWED == m_securityManager.hasPermissions(
            m_context,
            resource,
            requiredPermissions,
            checkLock,
            filter);
    }

    /**
     * Checks if the given resource or the current project can be published by the current user 
     * using his current OpenCms context.<p>
     * 
     * If the resource parameter is <code>null</code>, then the current project is checked,
     * otherwise the resource is checked for direct publish permissions.<p>
     * 
     * @param resourcename the direct publish resource name (optional, if null only the current project is checked)
     * 
     * @return <code>true</code>, if the current user can direct publish the given resource in his current context
     */
    public boolean hasPublishPermissions(String resourcename) {

        CmsResource resource = null;
        if (resourcename != null) {
            // resource name is optional
            try {
                resource = readResource(resourcename, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // if any exception (e.g. security) occurs the result is false
                return false;
            }
        }
        // now perform the permission test
        return m_securityManager.hasPublishPermissions(m_context, resource);
    }

    /**
     * Writes a list of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.<p>
     * 
     * @param resource the resource to attach the control entries to
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsResource resource, List acEntries) throws CmsException {

        m_securityManager.importAccessControlEntries(m_context, resource, acEntries);
    }

    /**
     * Imports an import-resource (folder or zipfile).<p>
     *
     * It is important that a <code>manifest.xml</code> is present in the 
     * given folder or the root path inside the zip file, if not a 
     * <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param importFile the name (absolute Path) of the import resource (zipfile or folder)
     * @param importPath the name (absolute Path) of the folder in which should be imported
     * 
     * @throws CmsException if operation was not successful
     */
    public void importFolder(String importFile, String importPath) throws CmsException {

        // import the folder
        m_securityManager.importFolder(this, m_context, importFile, importPath);
    }

    /**
     * Imports a resource to the OpenCms VFS.<p>
     * 
     * If a resource already exists in the VFS (i.e. has the same name and 
     * same id) it is replaced by the imported resource.<p>
     * 
     * If a resource with the same name but a different id exists, 
     * the imported resource is (usually) moved to the "lost and found" folder.<p> 
     *
     * @param resourcename the name for the resource after import (full path)
     * @param resource the resource object to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     */
    public CmsResource importResource(String resourcename, CmsResource resource, byte[] content, List properties)
    throws CmsException {

        return getResourceType(resource.getTypeId()).importResource(
            this,
            m_securityManager,
            resourcename,
            resource,
            content,
            properties);
    }

    /**
     * Checks if the current user has "Administrator" permissions.<p>
     * 
     * Administrator permissions means that the user is a member of the 
     * administrators group, which per default is called "Administrators".<p>
     *
     * @return <code>true</code>, if the current user has "Administrator" permissions
     */
    public boolean isAdmin() {

        return m_securityManager.isAdmin(m_context);
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param resourcename the specified resource name (full path)
     * 
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(String resourcename) {

        return m_securityManager.isInsideCurrentProject(m_context, addSiteRoot(resourcename));
    }

    /**
     * Checks if the current user has management access to the project.<p>
     *
     * Please note: This is NOT the same as the <code>{@link CmsObject#isProjectManager()}</code> 
     * check. If the user has management access to a project depends on the
     * project settings.<p>
     *
     * @return <code>true</code>, if the user has management access to the project
     * 
     * @see #isProjectManager()
     */
    public boolean isManagerOfProject() {

        return m_securityManager.isManagerOfProject(m_context);
    }

    /**
     * Checks if the current user is a member of the project manager group.<p>
     *
     * Please note: This is NOT the same as the <code>{@link CmsObject#isManagerOfProject()}</code>
     * check. If the user is a member of the project manager group, 
     * he can create new projects.<p>
     *
     * @return <code>true</code>, if the user is a member of the project manager group
     * 
     * @see #isManagerOfProject()
     */
    public boolean isProjectManager() {

        return m_securityManager.isProjectManager(m_context);
    }

    /**
     * Locks a resource.<p>
     *
     * The mode for the lock is <code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code>.<p>
     *
     * @param resourcename the name of the resource to lock (full path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     */
    public void lockResource(String resourcename) throws CmsException {

        lockResource(resourcename, CmsLock.C_MODE_COMMON);
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_TEMP}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to lock (full path)
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     */
    public void lockResource(String resourcename, int mode) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).lockResource(this, m_securityManager, resource, mode);
    }

    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * 
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
     * 
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
     * 
     * @return the name of the logged in user
     *
     * @throws CmsSecurityException if operation was not successful
     */
    public String loginUser(String username, String password, String remoteAddress, int type)
    throws CmsSecurityException {

        // login the user
        CmsUser newUser = m_securityManager.loginUser(m_context, username, password, remoteAddress, type);
        // set the project back to the "Online" project
        CmsProject newProject;
        try {
            newProject = m_securityManager.readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        } catch (CmsException e) {
            // should not happen since the online project is always available
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, e);
        }
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject);
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        // fire a login event
        this.fireEvent(org.opencms.main.I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }

    /**
     * Logs a web user into the Cms, if the password is correct.
     *
     * @param username the name of the user
     * @param password the password of the user
     * 
     * @return the name of the logged in user
     *
     * @throws CmsSecurityException if operation was not successful
     */
    public String loginWebUser(String username, String password) throws CmsSecurityException {

        return loginUser(username, password, m_context.getRemoteAddress(), I_CmsConstants.C_USER_TYPE_WEBUSER);
    }

    /**
     * Lookups and reads the user or group with the given UUID.<p>
     *   
     * @param principalId the uuid of a user or group
     * 
     * @return the user or group with the given UUID
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        return m_securityManager.lookupPrincipal(m_context, principalId);
    }

    /**
     * Lookups and reads the user or group with the given name.<p>
     * 
     * @param principalName the name of the user or group
     * 
     * @return the user or group with the given name
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        return m_securityManager.lookupPrincipal(m_context, principalName);
    }

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param source the name of the resource to move (full path)
     * @param destination the destination resource name (full path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #renameResource(String, String)
     */
    public void moveResource(String source, String destination) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).moveResource(this, m_securityManager, resource, destination);
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The "lost and found" folder is a special system folder. 
     * This operation is used e.g. during import of resources
     * when a resource with the same name but a different resource ID
     * already exists in the VFS. In this case, the imported resource is 
     * moved to the "lost and found" folder.<p>
     * 
     * @param resourcename the name of the resource to move to "lost and found" (full path)
     *
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getLostAndFoundName(String)
     */
    public String moveToLostAndFound(String resourcename) throws CmsException {

        return m_securityManager.moveToLostAndFound(m_context, resourcename, false);
    }

    /**
     * Publishes the current project, printing messages to a shell report.<p>
     *
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsShellReport
     */
    public CmsUUID publishProject() throws Exception {

        return publishProject(new CmsShellReport());
    }

    /**
     * Publishes the current project.<p>
     *
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUUID publishProject(I_CmsReport report) throws CmsException {

        return publishProject(report, null, false);
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     * 
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param publishList a publish list
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getPublishList()
     * @see #getPublishList(CmsResource, boolean)
     */
    public CmsUUID publishProject(I_CmsReport report, CmsPublishList publishList) throws CmsException {

        synchronized (m_securityManager) {
            return m_securityManager.publishProject(this, publishList, report);
        }
    }

    /**
     * Direct publishes a specified resource.<p>
     * 
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param directPublishResource a <code>{@link CmsResource}</code> that gets directly published; 
     *                          or <code>null</code> if an entire project gets published.
     * @param directPublishSiblings if a <code>{@link CmsResource}</code> that should get published directly is 
     *                          provided as an argument, all eventual siblings of this resource 
     *                          get publish too, if this flag is <code>true</code>.
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #publishResource(String)
     * @see #publishResource(String, boolean, I_CmsReport)
     */
    public CmsUUID publishProject(I_CmsReport report, CmsResource directPublishResource, boolean directPublishSiblings)
    throws CmsException {

        CmsPublishList publishList = getPublishList(directPublishResource, directPublishSiblings);
        return publishProject(report, publishList);
    }

    /**
     * Publishes a single resource, printing messages to a shell report.<p>
     * 
     * The siblings of the resource will not be published.<p>
     *
     * @param resourcename the name of the resource to be published
     * 
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsShellReport
     */
    public CmsUUID publishResource(String resourcename) throws Exception {

        return publishResource(resourcename, false, new CmsShellReport());
    }

    /**
     * Publishes a single resource.<p>
     * 
     * @param resourcename the name of the resource to be published
     * @param publishSiblings if <code>true</code>, all siblings of the resource are also published
     * @param report the report to write the progress information to
     * 
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     */
    public CmsUUID publishResource(String resourcename, boolean publishSiblings, I_CmsReport report) throws Exception {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return publishProject(report, resource, publishSiblings);
    }

    /**
     * Reads all file headers of a file.<br>
     * 
     * This method returns a list with the history of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     *
     * @param filename the name of the file to be read
     *
     * @return a list of file headers, as <code>{@link CmsBackupResource}</code> objects, read from the Cms
     *
     * @throws CmsException  if operation was not successful
     */
    public List readAllBackupFileHeaders(String filename) throws CmsException {

        CmsResource resource = readResource(filename, CmsResourceFilter.ALL);
        return (m_securityManager.readAllBackupFileHeaders(m_context, resource));
    }

    /**
     * Returns a list with all project resources for a given project.<p>
     * 
     * @param projectId the ID of the project
     * 
     * @return a list of all project <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List readAllProjectResources(int projectId) throws CmsException {

        return m_securityManager.readAllProjectResources(m_context, projectId);
    }

    /**
     * Reads all property definitions.<p>
     *
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     *
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertyDefinitions() throws CmsException {

        return m_securityManager.readAllPropertyDefinitions(m_context, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
    }

    /**
     * Returns a file from the history.<br>
     * 
     * The reading includes the file content.<p>
     *
     * @param filename the complete path of the file to be read
     * @param tagId the tag id of the resource
     *
     * @return the file read
     *
     * @throws CmsException if the user has not the rights to read the file, or 
     *                      if the file couldn't be read.
     */
    public CmsBackupResource readBackupFile(String filename, int tagId) throws CmsException {

        CmsResource resource = readResource(filename, CmsResourceFilter.ALL);
        return m_securityManager.readBackupFile(m_context, tagId, resource);
    }

    /**
     * Returns a backup project.<p>
     *
     * @param tagId the tag of the backup project to be read
     * 
     * @return the requested backup project
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {

        return (m_securityManager.readBackupProject(m_context, tagId));
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     *  
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String)}</code> instead.<p>
     *
     * @param resourcename the name of the resource to read (full path)
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readResource(String)
     */
    public CmsFile readFile(String resourcename) throws CmsException {

        return readFile(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     *
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFile readFile(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return m_securityManager.readFile(m_context, resource, filter);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * @param resourcename the name of the resource to read (full path)
     *
     * @return the file resource that was read
     *
     * @throws CmsException if something goes wrong
     *
     * @deprecated use <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.
     */
    public CmsResource readFileHeader(String resourcename) throws CmsException {

        return readResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * @param resourcename the name of the folder resource to read (full path)
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readResource(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename) throws CmsException {

        return readFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the specified resource filter.<p>
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the folder resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readFolder(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param project the project to read the group from
     * 
     * @return the group of the given project
     */
    public CmsGroup readGroup(CmsProject project) {

        return m_securityManager.readGroup(m_context, project);
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param groupId the id of the group to be read
     * 
     * @return the group that has the provided id
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {

        return m_securityManager.readGroup(m_context, groupId);
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param groupName the name of the group to be read
     * 
     * @return the group that has the provided name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(String groupName) throws CmsException {

        return (m_securityManager.readGroup(m_context, groupName));
    }

    /**
     * Returns the project manager group of a project.<p>
     *
     * @param project the project
     * 
     * @return the manager group of the project
     */
    public CmsGroup readManagerGroup(CmsProject project) {

        return m_securityManager.readManagerGroup(m_context, project);
    }

    /**
     * Reads the owner of a project.<p>
     *
     * @param project the project to read the owner from
     * 
     * @return the owner of the project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {

        return m_securityManager.readOwner(m_context, project);
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPath(String path, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readPath(m_context, m_context.currentProject().getId(), addSiteRoot(path), filter);
    }

    /**
     * Reads the project with the given id.<p>
     *
     * @param id the id of the project
     * 
     * @return the project with the given id
     *
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(int id) throws CmsException {

        return m_securityManager.readProject(id);
    }

    /**
     * Reads the project with the given name.<p>
     *
     * @param name the name of the project
     * 
     * @return the project with the given name
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(String name) throws CmsException {

        return m_securityManager.readProject(name);
    }

    /**
     * Returns the list of all resources that define the "view" of the given project.<p>
     * 
     * @param project the project to get the project resources for
     * 
     * @return the list of all resources, as <code>{@link String}</code> objects 
     *              that define the "view" of the given project.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsProject project) throws CmsException {

        return m_securityManager.readProjectResources(m_context, project);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * Possible values for the <code>state</code> parameter are:<br>
     * <ul>
     * <li><code>{@link I_CmsConstants#C_STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link I_CmsConstants#C_STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link I_CmsConstants#C_STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link I_CmsConstants#C_STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match
     *
     * @return all <code>{@link CmsResource}</code>s of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     */
    public List readProjectView(int projectId, int state) throws CmsException {

        return m_securityManager.readProjectView(m_context, projectId, state);
    }

    /**
     * Reads the (compound) values of all properties mapped to a specified resource.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * 
     * @return a map of <code>String</code> objects representing all properties of the resource
     * 
     * @throws CmsException in case there where problems reading the properties
     * 
     * @deprecated use <code>{@link #readPropertyObjects(String, boolean)}</code> instead.
     */
    public Map readProperties(String resourcePath) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        List properties = m_securityManager.readPropertyObjects(m_context, resource, false);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) values of all properties mapped to a specified resource
     * with optional direcory upward cascading.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param search if <code>true</code>, the properties will also be looked up on all parent folders and the results will be merged, if <code>false</code> not (ie. normal property lookup)
     * 
     * @return Map of <code>String</code> objects representing all properties of the resource
     * 
     * @throws CmsException in case there where problems reading the properties
     * 
     * @deprecated use <code>{@link #readPropertyObjects(String, boolean)}</code> instead.
     */
    public Map readProperties(String resourcePath, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        List properties = m_securityManager.readPropertyObjects(m_context, resource, search);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource.<p>
     *
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * 
     * @return the value of the property found, <code>null</code> if nothing was found
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use new Object based methods.
     */
    public String readProperty(String resourcePath, String property) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, false);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional direcory upward cascading.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if false not (ie. normal property lookup)
     * 
     * @return the value of the property found, <code>null</code> if nothing was found
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use new Object based methods.
     */
    public String readProperty(String resourcePath, String property, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, search);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional direcory upward cascading, a default value will be returned if the property 
     * is not found on the resource (or it's parent folders in case search is set to <code>true</code>).<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if <code>false</code> not (ie. normal property lookup)
     * @param propertyDefault a default value that will be returned if the property was not found on the selected resource
     * 
     * @return the value of the property found, if nothing was found the value of the <code>propertyDefault</code> parameter is returned
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use new Object based methods.
     */
    public String readProperty(String resourcePath, String property, boolean search, String propertyDefault)
    throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, search);
        return value.isNullProperty() ? propertyDefault : value.getValue();
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param name the name of the property definition to read
     * 
     * @return the property definition that was read, 
     *          or <code>null</code> if there is no property definition with the given name.
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition readPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.readPropertyDefinition(m_context, name));
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param resourcePath the name of resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource. 
     *      if it's not found attached directly to the resource.
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(String resourcePath, String property, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObject(m_context, resource, property, search);
    }

    /**
     * Reads all property objects from a resource.<p>
     * 
     * Returns an empty list if no properties are found.<p>
     * 
     * If the <code>search</code> parameter is <code>true</code>, the properties of all 
     * parent folders of the resource are also read. The results are merged with the 
     * properties directly attached to the resource. While merging, a property
     * on a parent folder that has already been found will be ignored.
     * So e.g. if a resource has a property "Title" attached, and it's parent folder 
     * has the same property attached but with a differrent value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param resourcePath the name of resource where the property is mapped to
     * @param search if <code>true</code>, the properties of all parent folders of the resource 
     *      are merged with the resource properties.
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(String resourcePath, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObjects(m_context, resource, search);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsUUID publishHistoryId) throws CmsException {

        return m_securityManager.readPublishedResources(m_context, publishHistoryId);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link CmsFile#upgrade(CmsResource, CmsObject)}</code>.<p> 
     *
     * @param resourcename the name of the resource to read (full path)
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readFile(String) 
     * @see #readResource(String, CmsResourceFilter)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(String resourcename) throws CmsException {

        return readResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutly
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link CmsFile#upgrade(CmsResource, CmsObject)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads all resources below the given path matching the filter criteria, 
     * including the full tree below the path.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readResources(String, CmsResourceFilter, boolean)
     */
    public List readResources(String resourcename, CmsResourceFilter filter) throws CmsException {

        return readResources(resourcename, filter, true);
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all subresources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResources(String resourcename, CmsResourceFilter filter, boolean readTree) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readResources(m_context, resource, filter, readTree);
    }

    /**
     * Reads all resources that have a value set for the specified property.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param propertyDefinition the name of the property to check for
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String propertyDefinition) throws CmsException {

        return m_securityManager.readResourcesWithProperty(m_context, "/", propertyDefinition);
    }

    /**
     * Reads all resources that have a value set for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String path, String propertyDefinition) throws CmsException {

        return m_securityManager.readResourcesWithProperty(m_context, addSiteRoot(path), propertyDefinition);
    }

    /**
     * Returns a list of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param resourcename the name of the specified resource
     * @param filter a resource filter
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return m_securityManager.readSiblings(m_context, resource, filter);
    }

    /**
     * Returns the parameters of a resource in the list of all published template resources.<p>
     * 
     * @param rfsName the rfs name of the resource
     * 
     * @return the parameter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(String rfsName) throws CmsException {

        return m_securityManager.readStaticExportPublishedResourceParameters(m_context, rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * 
     * @param timestamp a timestamp for reading the data from the db
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(int parameterResources, long timestamp) throws CmsException {

        return m_securityManager.readStaticExportResources(m_context, parameterResources, timestamp);
    }

    /**
     * Reads a user based on its id.<p>
     *
     * @param userId the id of the user to be read
     * 
     * @return the user with the given id
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException {

        return m_securityManager.readUser(m_context, userId);
    }

    /**
     * Reads a user based on its name.<p>
     *
     * @param username the name of the user to be read
     * 
     * @return the user with the given name
     * 
     * @throws CmsException if somthing goes wrong
     */
    public CmsUser readUser(String username) throws CmsException {

        return m_securityManager.readUser(username);
    }

    /**
     * Returns a user given its name and type.
     *
     * @param username the name of the user to be returned
     * @param type the type of the user
     * 
     * @return the user with the given name and type
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, int type) throws CmsException {

        return m_securityManager.readUser(m_context, username, type);
    }

    /**
     * Returns a user, if the password is correct.<p>
     * 
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param username the name of the user to be returned
     * @param password the password of the user to be returned
     * 
     * @return the validated user
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        return m_securityManager.readUser(m_context, username, password);
    }

    /**
     * Returns a webuser.<p>
     *
     * @param username the username of the webuser that is to be read
     * 
     * @return the webuser
     *
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(String username) throws CmsException {

        return m_securityManager.readWebUser(m_context, username);
    }

    /**
     * Returns a web user if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return a web user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {

        return m_securityManager.readWebUser(m_context, username, password);
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void removeUserFromGroup(String username, String groupname) throws CmsException {

        m_securityManager.removeUserFromGroup(m_context, username, groupname);
    }

    /**
     * Renames a resource to the given destination name,
     * this is identical to a <code>move</code> operation.<p>
     *
     * @param source the name of the resource to rename (full path)
     * @param destination the new resource name (full path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveResource(String, String)
     */
    public void renameResource(String source, String destination) throws CmsException {

        moveResource(source, destination);
    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param resourcename the name of the resource to replace (full path)
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void replaceResource(String resourcename, int type, byte[] content, List properties) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).replaceResource(
            this,
            m_securityManager,
            resource,
            type,
            content,
            properties);
    }

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param resourcename the name of the resource to restore from the archive (full path)
     * @param tag the tag (version) id to resource form the archive
     *
     * @throws CmsException if something goes wrong
     */
    public void restoreResourceBackup(String resourcename, int tag) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).restoreResourceBackup(this, m_securityManager, resource, tag);
    }

    /**
     * Removes an access control entry of a griven principal from a given resource.<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user)
     * @param principalName name of the principal
     * 
     * @throws CmsException if something goes wrong
     */
    public void rmacc(String resourceName, String principalType, String principalName) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        I_CmsPrincipal principal = null;

        if (I_CmsPrincipal.C_PRINCIPAL_GROUP.equalsIgnoreCase(principalType)) {
            principal = readGroup(principalName);
        } else if (I_CmsPrincipal.C_PRINCIPAL_USER.equalsIgnoreCase(principalType)) {
            principal = readUser(principalName);
        }

        m_securityManager.removeAccessControlEntry(m_context, res, principal.getId());
    }

    /**
     * Sets a new parent-group for an already existing group.<p>
     *
     * @param groupName the name of the group that should be updated
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     * 
     * @throws CmsException  if operation was not successfull
     */
    public void setParentGroup(String groupName, String parentGroupName) throws CmsException {

        m_securityManager.setParentGroup(m_context, groupName, parentGroupName);
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param username the name of the user
     * @param newPassword the new password
     *
     * @throws CmsException if operation was not successful
     */
    public void setPassword(String username, String newPassword) throws CmsException {

        m_securityManager.setPassword(m_context, username, newPassword);
    }

    /**
     * Sets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     */
    public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {

        m_securityManager.resetPassword(m_context, username, oldPassword, newPassword);
    }

    /**
     * Changes the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expire" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full path)
     * @param dateLastModified timestamp the new timestamp of the changed resource
     * @param dateReleased the new release date of the changed resource, 
     *              set it to <code>{@link I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged.
     * @param dateExpired the new expire date of the changed resource. 
     *              set it to <code>{@link I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged.
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void touch(String resourcename, long dateLastModified, long dateReleased, long dateExpired, boolean recursive)
    throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).touch(
            this,
            m_securityManager,
            resource,
            dateLastModified,
            dateReleased,
            dateExpired,
            recursive);
    }

    /**
     * Undeletes a resource (this is the same operation as "undo changes").<p>
     * 
     * Only resources that have already been published once can be undeleted,
     * if a "new" resource is deleted it can not be undeleted.<p>
     * 
     * Internally, this method undos all changes to a resource by restoring 
     * the version from the online project, that is to the state of last 
     * publishing.<p>
     * 
     * @param resourcename the name of the resource to undelete (full path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     */
    public void undeleteResource(String resourcename) throws CmsException {

        undoChanges(resourcename, true);
    }

    /**
     * Undos all changes to a resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param resourcename the name of the resource to undo the changes for (full path)
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     */
    public void undoChanges(String resourcename, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).undoChanges(this, m_securityManager, resource, recursive);
    }

    /**
     * Unlocks all resources of a project.
     *
     * @param id the id of the project to be unlocked
     *
     * @throws CmsException if operation was not successful
     */
    public void unlockProject(int id) throws CmsException {

        m_securityManager.unlockProject(m_context, id);
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param resourcename the name of the resource to unlock (full path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource.getTypeId()).unlockResource(this, m_securityManager, resource);
    }

    /**
     * Tests if a user is member of the given group.<p>
     *
     * @param username the name of the user to test
     * @param groupname the name of the group to test
     * 
     * @return <code>true</code>, if the user is in the group; or <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful
     */
    public boolean userInGroup(String username, String groupname) throws CmsException {

        return (m_securityManager.userInGroup(m_context, username, groupname));
    }

    /**
     * Validates the HTML links in the unpublished files of the specified
     * publish list, if a file resource type implements the interface 
     * <code>{@link org.opencms.validation.I_CmsHtmlLinkValidatable}</code>.<p>
     * 
     * @param publishList an OpenCms publish list
     * @param report a report to write the messages to
     * 
     * @return a map with lists of invalid links (<code>String</code> objects) keyed by resource names
     * 
     * @throws Exception if something goes wrong
     * 
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable
     */
    public Map validateHtmlLinks(CmsPublishList publishList, I_CmsReport report) throws Exception {

        return m_securityManager.validateHtmlLinks(this, publishList, report);
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class implementing the 
     * <code>{@link org.opencms.security.I_CmsPasswordHandler}</code> 
     * interface and configured in the opencms.properties file.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     *
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {

        m_securityManager.validatePassword(password);
    }

    /**
     * Writes a resource to the OpenCms VFS, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * i.e. resources that have a binary content attached.<p>
     * 
     * Certain resource types might apply content validation or transformation rules 
     * before the resource is actually written to the VFS. The returned result
     * might therefore be a modified version from the provided original.<p>
     *
     * @param resource the resource to write
     *
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile writeFile(CmsFile resource) throws CmsException {

        return getResourceType(resource.getTypeId()).writeFile(this, m_securityManager, resource);
    }

    /**
     * Writes a file-header.<p>
     *
     * @param file the file to write
     *
     * @throws CmsException if resourcetype is set to folder, or
     *                      if the user has not the rights to write the file header.
     */
    public void writeFileHeader(CmsFile file) throws CmsException {

        m_securityManager.writeResource(m_context, file);
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group id has to be a valid OpenCms group id.<br>
     * 
     * The group with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param group the group that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeGroup(CmsGroup group) throws CmsException {

        m_securityManager.writeGroup(m_context, group);
    }

    /**
     * Writes a couple of properties as structure values for a file or folder.
     *
     * @param resourceName the resource-name of which the Property has to be set
     * @param properties a Hashtable with property-definitions and property values as Strings
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObjects(String, List)}</code> instead.
     */
    public void writeProperties(String resourceName, Map properties) throws CmsException {

        writePropertyObjects(resourceName, CmsProperty.toList(properties));
    }

    /**
     * Writes a couple of Properties for a file or folder.
     *
     * @param name the resource-name of which the Property has to be set
     * @param properties a Hashtable with property-definitions and property values as Strings
     * @param addDefinition flag to indicate if unknown definitions should be added
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObjects(String, List)}</code> instead.
     */
    public void writeProperties(String name, Map properties, boolean addDefinition) throws CmsException {

        writePropertyObjects(name, CmsProperty.setAutoCreatePropertyDefinitions(
            CmsProperty.toList(properties),
            addDefinition));
    }

    /**
     * Writes a property as a structure value for a file or folder.<p>
     *
     * @param resourceName the resource-name for which the property will be set
     * @param key the property definition name
     * @param value the value for the property to be set
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void writeProperty(String resourceName, String key, String value) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(value);

        writePropertyObject(resourceName, property);
    }

    /**
     * Writes a property for a file or folder.<p>
     *
     * @param resourcename the resource-name for which the property will be set
     * @param key the property-definition name
     * @param value the value for the property to be set
     * @param addDefinition flag to indicate if unknown definitions should be added
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void writeProperty(String resourcename, String key, String value, boolean addDefinition) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setKey(key);
        property.setStructureValue(value);
        property.setAutoCreatePropertyDefinition(addDefinition);

        writePropertyObject(resourcename, property);
    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObject(String resourcename, CmsProperty property) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).writePropertyObject(this, m_securityManager, resource, property);
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObjects(String resourcename, List properties) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource.getTypeId()).writePropertyObjects(this, m_securityManager, resource, properties);
    }

    /**
     * Writes a published resource entry.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_securityManager.writeStaticExportPublishedResource(
            m_context,
            resourceName,
            linkType,
            linkParameter,
            timestamp);
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param user the user to be written
     *
     * @throws CmsException if operation was not successful
     */
    public void writeUser(CmsUser user) throws CmsException {

        m_securityManager.writeUser(m_context, user);
    }

    /**
     * Updates the user information of a web user.<br>
     * 
     * Only a web user can be updated this way.<p>
     *
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param user the user to be written
     *
     * @throws CmsException if operation was not successful
     */
    public void writeWebUser(CmsUser user) throws CmsException {

        m_securityManager.writeWebUser(m_context, user);
    }

    /**
     * Convenience method to add the site root from the current user's 
     * request context to the given resource name.<p>
     *
     * @param resourcename the resource name
     * 
     * @return the resource name with the site root added
     * 
     * @see CmsRequestContext#addSiteRoot(String)
     */
    private String addSiteRoot(String resourcename) {

        return m_context.addSiteRoot(resourcename);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     * 
     * The event will be given to all registered <code>{@link I_CmsEventListener}</code>s.<p>
     * 
     * @param type the type of the event
     * @param data a data object that contains data used by the event listeners
     * 
     * @see OpenCms#addCmsEventListener(I_CmsEventListener)
     * @see OpenCms#addCmsEventListener(I_CmsEventListener, int[])
     */
    private void fireEvent(int type, Object data) {

        OpenCms.fireCmsEvent(type, Collections.singletonMap("data", data));
    }

    /**
     * Convenience method to return the initialized resource type 
     * instance for the given id.<p>
     * 
     * @param resourceType the id of the resource type to get
     * 
     * @return the initialized resource type instance for the given id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    private I_CmsResourceType getResourceType(int resourceType) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(resourceType);
    }

    /**
     * Initializes this <code>{@link CmsObject}</code> with the provided user context and database connection.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentification
     */
    private void init(CmsSecurityManager securityManager, CmsRequestContext context) {

        m_securityManager = securityManager;
        m_context = context;
    }
}