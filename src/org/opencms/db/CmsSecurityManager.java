/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsSecurityManager.java,v $
 * Date   : $Date: 2004/11/10 16:12:32 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.*;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;

/**
 * The OpenCms security manager.<p>
 * 
 * The security manager checks the permissions required for a user action invoke by the Cms object. If permissions 
 * are granted, the security manager invokes a method on the OpenCms driver manager to access the database.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.11 $
 * @since 5.5.2
 */
public final class CmsSecurityManager {
    
    /** Indicates allowed permissions. */
    public static final int PERM_ALLOWED = 0;

    /** Indicates denied permissions. */
    public static final int PERM_DENIED = 1;

    /** Indicates a resource was filtered during permission check. */
    public static final int PERM_FILTERED = 2;

    /** Indicates a resource was not locked for a write / control operation. */
    public static final int PERM_NOTLOCKED = 3;

    /** Indicates allowed permissions. */
    private static final Integer PERM_ALLOWED_INTEGER = new Integer(PERM_ALLOWED);

    /** Indicates denied permissions. */
    private static final Integer PERM_DENIED_INTEGER = new Integer(PERM_DENIED);

    /** The initialized OpenCms driver manager to access the database. */
    protected CmsDriverManager m_driverManager;

    /** The factory to create runtime info objects. */
    protected I_CmsRuntimeInfoFactory m_runtimeInfoFactory;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /** Cache for permission checks. */
    private Map m_permissionCache;

    /**
     * Default constructor.<p>
     */
    private CmsSecurityManager() {

        // intentionally left blank
    }

    /**
     * Creates a new instance of the OpenCms security manager.<p>
     * 
     * @param configurationManager the configuation manager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * 
     * @return a new instance of the OpenCms security manager
     * @throws CmsException if something goes wrong
     */
    public static CmsSecurityManager newInstance(
        CmsConfigurationManager configurationManager,
        I_CmsRuntimeInfoFactory runtimeInfoFactory) throws CmsException {

        CmsSecurityManager securityManager = new CmsSecurityManager();
        securityManager.init(configurationManager, runtimeInfoFactory);

        return securityManager;
    }

    /**
     * Accept a task from the Cms.<p>
     * 
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task to accept.
     *
     * @throws CmsException if something goes wrong
     */
    public void acceptTask(CmsRequestContext context, int taskId) throws CmsException {

        m_driverManager.acceptTask(context, taskId);
    }

    /**
     * Tests if the user can access the project.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param projectId the id of the project
     * @return true, if the user has access, else returns false
     * @throws CmsException if something goes wrong
     */
    public boolean accessProject(CmsRequestContext context, int projectId) throws CmsException {

        return m_driverManager.accessProject(context, projectId);
    }

    /**
     * Adds a user to the Cms.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param id the id of the user
     * @param name the name for the user
     * @param password the password for the user
     * @param description the description for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @param additionalInfos a Hashtable with additional infos for the user, these infos may be stored into the Usertables (depending on the implementation)
     * @param address the address of the user
     * @param type the type of the user
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addImportUser(
        CmsRequestContext context,
        String id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        int flags,
        Hashtable additionalInfos,
        String address,
        int type) throws CmsException {

        CmsUser newUser;

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                newUser = m_driverManager.addImportUser(
                    runtimeInfo,
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

                runtimeInfo.pop();
            } catch (CmsException e) {

                if (e.getType() != CmsException.C_USER_ALREADY_EXISTS) {
                    runtimeInfo.report(null, "Error importing user " + name, e);
                }

                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] addImportUser() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        return newUser;
    }

    /**
     * Adds a user to the Cms.<p>
     *
     * Only a adminstrator can add users to the cms.
     * Only users, which are in the group "administrators" are granted.
     * 
     * @param context the current request context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these infos may be stored into the Usertables (depending on the implementation)
     * @return the new user will be returned
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addUser(
        CmsRequestContext context,
        String name,
        String password,
        String group,
        String description,
        Hashtable additionalInfos) throws CmsException {

        CmsUser newUser;

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                newUser = m_driverManager.addUser(
                    context,
                    runtimeInfo,
                    name,
                    password,
                    group,
                    description,
                    additionalInfos);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error adding user " + name, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] addUser() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        return newUser;
    }

    /**
     * Adds a user to a group.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     *
     * @throws CmsException if operation was not succesfull
     */
    public void addUserToGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

        try {
            m_driverManager.addUserToGroup(context, runtimeInfo, username, groupname);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error adding user " + username + " to group " + groupname, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Adds a web user to the Cms.<p>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these infos may be stored into the Usertables (depending on the implementation)
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(String name, String password, String group, String description, Hashtable additionalInfos)
    throws CmsException {

        CmsUser newUser;

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

        try {
            newUser = m_driverManager.addWebUser(runtimeInfo, name, password, group, description, additionalInfos);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error adding web user " + name, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return newUser;
    }

    /**
     * Adds a web user to the Cms.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param additionalGroup an additional group for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these infos may be stored into the Usertables (depending on the implementation)
     *
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(
        String name,
        String password,
        String group,
        String additionalGroup,
        String description,
        Hashtable additionalInfos) throws CmsException {

        CmsUser newUser;

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

        try {
            newUser = m_driverManager.addWebUser(
                runtimeInfo,
                name,
                password,
                group,
                additionalGroup,
                description,
                additionalInfos);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error adding web user " + name, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return newUser;
    }

    /**
     * Creates a backup of the published project.<p>
     *
     * @param context the current request context
     * @param backupProject the project to be backuped
     * @param tagId the version of the backup
     * @param publishDate the date of publishing
     * @throws CmsException if operation was not succesful
     */
    public void backupProject(CmsRequestContext context, CmsProject backupProject, int tagId, long publishDate)
    throws CmsException {

        m_driverManager.backupProject(context, backupProject, tagId, publishDate);
    }

    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * @param context the current request context
     * @param resource theresource to apply this operation to
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLastModifiedProjectId(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check the access permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.changeLastModifiedProjectId(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(
                null,
                "Error changing last-modified-in-project-ID of resource " + resource.getRootPath(),
                e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Changes the lock of a resource to the current user, that is "steals" the lock from another user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the lock for
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#changeLock(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.changeLock(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error changing lock of resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }
    
    /**
     * Changes the value of the specified propertydefinition on resources from the old value to the new value.<p>
     *
     * @param context the current request context
     * @param resource the resource on which property definition values are changed
     * @param propertyDefinition the name of the propertydefinition to change the value
     * @param oldValue the old value of the propertydefinition
     * @param newValue the new value of the propertydefinition
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     * 
     * @return the resources where the property value has been changed
     *
     * @throws CmsException if operation was not successful
     */
    public List changePropertyValue(CmsRequestContext context, CmsResource resource, String propertyDefinition, String oldValue, String newValue, boolean recursive) throws CmsException {
        
        // collect the resources to look up
        List resources = new ArrayList();
        if (recursive) {
            resources = getResourcesWithProperty(context, resource.getRootPath(), propertyDefinition);
        } else {
            resources.add(resource);
        }
        
        Pattern oldPattern;
        try {
            // compile regular expression pattern
            oldPattern = Pattern.compile(oldValue);
        } catch (PatternSyntaxException e) {
            throw new CmsException(e.getMessage());    
        }
        
        List changedResources = new ArrayList(resources.size());
        for (int i=0; i<resources.size(); i++) {
            // loop through found resources and check property values
            CmsResource res = (CmsResource)resources.get(i);
            CmsProperty property = readPropertyObject(context, res.getRootPath(), propertyDefinition, false);
            String structureValue = property.getStructureValue();
            String resourceValue = property.getResourceValue();
            boolean changed = false;
            if (structureValue != null && oldPattern.matcher(structureValue).matches()) {
                // change structure value
                property.setStructureValue(newValue);
                changed = true;
            }
            if (resourceValue != null && oldPattern.matcher(resourceValue).matches()) {
                // change resource value
                property.setResourceValue(newValue);
                changed = true;
            }
            if (changed) {
                // write property object if something has changed
                writePropertyObject(context, res, property);
                changedResources.add(res);
            }           
        }
        return changedResources;
    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.<p>
     * 
     * @param context the current request context
     * @param userId the id of the user to change
     * @param userType the new usertype of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsRequestContext context, CmsUUID userId, int userType) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.changeUserType(runtimeInfo, userId, userType);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error changing type of user ID " + userId.toString(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] changeUserType() " + userId.toString(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.<p>
     * 
     * @param context the current request context
     * @param username the name of the user to change
     * @param userType the new usertype of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsRequestContext context, String username, int userType) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.changeUserType(runtimeInfo, username, userType);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error changing type of user " + username, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] changeUserType() " + username,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Performs a blocking permission check on a resource.<p>
     *
     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an exception is thrown.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param checkLock if true, the lock status of the resource is also checked 
     * @param filter the filter for the resource
     * 
     * @throws CmsException in case of any i/o error
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsVfsResourceNotFoundException if the required resource is not readable
     * 
     * @see #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException, CmsVfsResourceNotFoundException {

        // get the permissions
        int permissions = hasPermissions(context, resource, requiredPermissions, checkLock, filter);
        if (permissions != 0) {
            checkPermissions(context, resource, requiredPermissions, permissions);
        }
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#chflags(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsRequestContext context, CmsResource resource, int flags) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.chflags(context, runtimeInfo, resource, flags);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error changing flags of " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Changes the resource type of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#chtype(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chtype(CmsRequestContext context, CmsResource resource, int type) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.chtype(context, runtimeInfo, resource, type);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error changing resource type of " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Copies the access control entries of a given resource to a destination resorce.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * @param context the current request context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource destination)
    throws CmsException {

        // check the permissions
        checkPermissions(context, destination, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.copyAccessControlEntries(context, runtimeInfo, source, destination);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error copying ACEs from "
                + source.getRootPath()
                + " to "
                + destination.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, vaild and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(CmsObject, CmsSecurityManager, CmsResource, String, int)
     */
    public void copyResource(CmsRequestContext context, CmsResource source, String destination, int siblingMode)
    throws CmsException {

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.copyResource(context, runtimeInfo, source, destination, siblingMode);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error copying resource " + source.getRootPath() + " to " + destination, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to apply this operation to
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsRequestContext context, CmsResource resource) throws CmsException {

        m_driverManager.copyResourceToProject(context, resource);
    }

    /**
     * Counts the locked resources in this project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     *
     * @param context the current request context
     * @param id the id of the project
     * @return the amount of locked resources in this project.
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsRequestContext context, int id) throws CmsException {

        return m_driverManager.countLockedResources(context, id);
    }

    /**
     * Counts the locked resources in a given folder.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param context the current request context
     * @param foldername the folder to search in
     * @return the amount of locked resources in this project
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsRequestContext context, String foldername) throws CmsException {

        return m_driverManager.countLockedResources(context, foldername);
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.<p>
     * 
     * @param context the current request context
     * @param id the id of the new group
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * @return new created group
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(
        CmsRequestContext context,
        CmsUUID id,
        String name,
        String description,
        int flags,
        String parent) throws CmsException {

        CmsGroup newGroup;

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                newGroup = m_driverManager.createGroup(runtimeInfo, id, name, description, flags, parent);
                runtimeInfo.pop();
            } catch (CmsException e) {

                if (e.getType() != CmsException.C_GROUP_ALREADY_EXISTS) {
                    runtimeInfo.report(null, "Error creating group " + name, e);
                }

                throw e;
            } finally {
                runtimeInfo.clear();
            }
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        return newGroup;
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.<p>
     * 
     * @param context the current request context
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * @return new created group
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(CmsRequestContext context, String name, String description, int flags, String parent)
    throws CmsException {

        CmsGroup newGroup;

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                newGroup = m_driverManager.createGroup(runtimeInfo, name, description, flags, parent);
                runtimeInfo.pop();
            } catch (CmsException e) {

                if (e.getType() != CmsException.C_GROUP_ALREADY_EXISTS) {
                    runtimeInfo.report(null, "Error creating group " + name, e);
                }

                throw e;
            } finally {
                runtimeInfo.clear();
            }
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        return newGroup;
    }

    /**
     * Creates a project.<p>
     *
     * Only the users which are in the admin or projectmanager groups are granted.<p>
     *
     * @param context the current request context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype type the type of the project
     * @return the created project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(
        CmsRequestContext context,
        String name,
        String description,
        String groupname,
        String managergroupname,
        int projecttype) throws CmsException {

        return m_driverManager.createProject(context, name, description, groupname, managergroupname, projecttype);
    }

    /**
     * Creates the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.
     * @param context the current request context
     * @param name the name of the propertydefinition to overwrite
     * @param mappingtype the mapping type of the propertydefinition. Currently only the mapping type C_PROPERYDEFINITION_RESOURCE is supported
     * @return the created propertydefinition
     * @throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(CmsRequestContext context, String name, int mappingtype)
    throws CmsException {

        CmsPropertydefinition propertyDefinition;

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createPropertydefinition() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS + I_CmsRuntimeInfo.C_RUNTIMEINFO_BACKUP);

        try {
            propertyDefinition = m_driverManager.createPropertydefinition(context, runtimeInfo, name, mappingtype);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error creating property definition " + name, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return propertyDefinition;
    }

    /**
     * Creates a new resource with the provided content and properties.<p>
     * 
     * The <code>content</code> parameter may be null if the resource id already exists.
     * If so, the created resource will be made a sibling of the existing resource,
     * the existing content will remain unchanged.
     * This is used during file import for import of siblings as the 
     * <code>manifest.xml</code> only contains one binary copy per file. 
     * If the resource id exists but the <code>content</code> is not null,
     * the created resource will be made a sibling of the existing resource,
     * and both will share the new content.<p>
     * 
     * Note: the id used to identify the content record (pk of the record) is generated
     * on each call of this method (with valid content) !
     * 
     * @param context the current request context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @param importCase if true, signals that this operation is done while importing resource, causing different lock behaviour and potential "lost and found" usage
     * @return the created resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(
        CmsRequestContext context,
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List properties,
        boolean importCase) throws CmsException {

        CmsResource newResource;

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            newResource = m_driverManager.createResource(
                context,
                runtimeInfo,
                resourcePath,
                resource,
                content,
                properties,
                importCase);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error creating resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return newResource;
    }

    /**
     * Creates a new resource of the given resource type with the provided content and properties.<p>
     * 
     * If the provided content is null and the resource is not a folder, the content will be set to an empty byte array.<p>  
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @return the created resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#createResource(CmsObject, CmsSecurityManager, String, byte[], List)
     */
    public CmsResource createResource(
        CmsRequestContext context,
        String resourcename,
        int type,
        byte[] content,
        List properties) throws CmsException {

        CmsResource newResource;

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS + I_CmsRuntimeInfo.C_RUNTIMEINFO_BACKUP);

        try {
            newResource = m_driverManager.createResource(context, runtimeInfo, resourcename, type, content, properties);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error creating resource " + resourcename, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return newResource;
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param context the current request context
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(CmsObject, CmsSecurityManager, CmsResource, String, List)
     */
    public void createSibling(CmsRequestContext context, CmsResource source, String destination, List properties)
    throws CmsException {

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.createSibling(context, runtimeInfo, source, destination, properties);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error creating sibling of " + source.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Creates a new task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param agentName username who will edit the task
     * @param roleName usergroupname for the task
     * @param taskname name of the task
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * @return A new Task Object
     * @throws CmsException if something goes wrong
     */
    public CmsTask createTask(
        CmsRequestContext context,
        String agentName,
        String roleName,
        String taskname,
        long timeout,
        int priority) throws CmsException {

        return m_driverManager.createTask(context, agentName, roleName, taskname, timeout, priority);
    }

    /**
     * Creates a new task.<p>
     *
     * All users are granted.<p>
     *
     * @param currentUser the current user
     * @param projectid the current project id
     * @param agentName user who will edit the task
     * @param roleName usergroup for the task
     * @param taskName name of the task
     * @param taskType type of the task
     * @param taskComment description of the task
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * @return a new task object
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(
        CmsUser currentUser,
        int projectid,
        String agentName,
        String roleName,
        String taskName,
        String taskComment,
        int taskType,
        long timeout,
        int priority) throws CmsException {

        return m_driverManager.createTask(
            currentUser,
            projectid,
            agentName,
            roleName,
            taskName,
            taskComment,
            taskType,
            timeout,
            priority);
    }

    /**
     * Creates the project for the temporary files.<p>
     *
     * Only the users which are in the admin or projectleader-group are granted.<p>
     *
     * @param context the current request context
     * @return the new tempfile project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsRequestContext context) throws CmsException {

        return m_driverManager.createTempfileProject(context);
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param context the current request context
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsRequestContext context, int linkType) throws CmsException {

        m_driverManager.deleteAllStaticExportPublishedResources(context, linkType);
    }

    /**
     * Deletes the versions from the backup tables that are older then the given timestamp  and/or number of 
     * remaining versions.<p>
     * 
     * The number of verions always wins, i.e. if the given timestamp would delete more versions than given in the
     * versions parameter, the timestamp will be ignored. Deletion will delete file header, content and properties.<p>
     * 
     * @param context the current request context
     * @param timestamp the max age of backup resources
     * @param versions the number of remaining backup versions for each resource
     * @param report the report for output logging
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackups(CmsRequestContext context, long timestamp, int versions, I_CmsReport report)
    throws CmsException {

        m_driverManager.deleteBackups(context, timestamp, versions, report);
    }

    /**
     * Delete a group from the Cms.<p>
     *
     * Only groups that contain no subgroups can be deleted. Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param name the name of the group that is to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteGroup(CmsRequestContext context, String name) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.deleteGroup(context, runtimeInfo, name);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error deleteing group " + name, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param context the current request context
     * @param projectId the ID of the project to be deleted
     * @throws CmsException if something goes wrong
     */
    public void deleteProject(CmsRequestContext context, int projectId) throws CmsException {

        // read the project that should be deleted
        CmsProject deleteProject = readProject(projectId);

        if ((isAdmin(context) || isManagerOfProject(context)) && (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER + I_CmsRuntimeInfo.C_RUNTIMEINFO_PROJECT);

            try {
                m_driverManager.deleteProject(context, runtimeInfo, deleteProject);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error deleting project " + deleteProject.getName(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }
        } else if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            throw new CmsSecurityException("["
                + this.getClass().getName()
                + "] deleteProject() "
                + deleteProject.getName(), CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        } else {
            throw new CmsSecurityException("["
                + this.getClass().getName()
                + "] deleteProject() "
                + deleteProject.getName(), CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Delete the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.<p>
     * 
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param mappingtype the name of the resource type for which the propertydefinition is valid
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(CmsRequestContext context, String name, int mappingtype) throws CmsException {

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deletePropertydefinition() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS + I_CmsRuntimeInfo.C_RUNTIMEINFO_BACKUP);

        try {
            m_driverManager.deletePropertydefinition(context, runtimeInfo, name, mappingtype);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error deleting property definition " + name, e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }
    }

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<p>
     * 
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(CmsRequestContext context, CmsResource resource, int siblingMode) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.deleteResource(context, runtimeInfo, resource, siblingMode);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error deleting resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }
    }

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param context the current request context
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters ofthe resource
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(
        CmsRequestContext context,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsException {

        m_driverManager.deleteStaticExportPublishedResource(context, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * Only a adminstrator can do this. Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param userId the Id of the user to be deleted
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsRequestContext context, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(userId);
        String username = user.getName();

        if (isAdmin(context)
            && !(username.equals(OpenCms.getDefaultUsers().getUserAdmin()) || username.equals(OpenCms.getDefaultUsers()
                .getUserGuest()))) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.deleteUser(runtimeInfo, context.currentProject(), userId);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error deleting user " + username, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else if (username.equals(OpenCms.getDefaultUsers().getUserAdmin())
            || username.equals(OpenCms.getDefaultUsers().getUserGuest())) {

            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + username,
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        } else {

            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + username,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param username the name of the user to be deleted
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsRequestContext context, String username) throws CmsException {

        if (isAdmin(context)
            && !(username.equals(OpenCms.getDefaultUsers().getUserAdmin()) || username.equals(OpenCms.getDefaultUsers()
                .getUserGuest()))) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.deleteUser(runtimeInfo, context.currentProject(), username);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error deleting user " + username, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else if (username.equals(OpenCms.getDefaultUsers().getUserAdmin())
            || username.equals(OpenCms.getDefaultUsers().getUserGuest())) {

            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + username,
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        } else {

            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + username,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a web user from the Cms.<p>
     * 
     * @param userId the Id of the user to be deleted
     * @throws CmsException if operation was not succesfull
     */
    public void deleteWebUser(CmsUUID userId) throws CmsException {

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

        try {
            m_driverManager.deleteWebUser(runtimeInfo, userId);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error deleting user ID " + userId.toString(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }
    }

    /**
     * Destroys this security manager.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void destroy() throws Throwable {

        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }
    }

    /**
     * Encrypts the given password with the default encryption method/encoding.<p>
     * 
     * @param password the password to encrypt
     * @return the encrypted password
     * @throws CmsException if something goes wrong
     */
    public String digest(String password) throws CmsException {

        return m_driverManager.digest(password);
    }

    /**
     * Ends a task from the Cms.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskid the ID of the task to end
     *
     * @throws CmsException if something goes wrong
     */
    public void endTask(CmsRequestContext context, int taskid) throws CmsException {

        m_driverManager.endTask(context, taskid);
    }

    /**
     * Forwards a task to a new user.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskid the Id of the task to forward
     * @param newRoleName the new group name for the task
     * @param newUserName the new user who gets the task. if its "" the a new agent will automatic selected
     * @throws CmsException if something goes wrong
     */
    public void forwardTask(CmsRequestContext context, int taskid, String newRoleName, String newUserName)
    throws CmsException {

        m_driverManager.forwardTask(context, taskid, newRoleName, newUserName);
    }

    /**
     * Reads all access control entries for a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * @return a vector of access control entries defining all permissions for the given resource
     * @throws CmsException if something goes wrong
     */
    public Vector getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited)
    throws CmsException {

        return m_driverManager.getAccessControlEntries(context, resource, getInherited);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If <code>inheritedOnly</code> is set, only inherited access control entries are returned.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * @return the access control list of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(
        CmsRequestContext context,
        CmsResource resource,
        boolean inheritedOnly) throws CmsException {

        return m_driverManager.getAccessControlList(context, resource, inheritedOnly);
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @return a list of Cms projects
     * @throws CmsException if something goes wrong
     */
    public List getAllAccessibleProjects(CmsRequestContext context) throws CmsException {

        return m_driverManager.getAllAccessibleProjects(context);
    }

    /**
     * Returns a Vector with all projects from history.<p>
     *
     * @return Vector with all projects from history.
     * @throws CmsException if operation was not succesful.
     */
    public Vector getAllBackupProjects() throws CmsException {

        return m_driverManager.getAllBackupProjects();
    }

    /**
     * Returns all projects which are owned by the user or which are manageable for the group of the user.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @return a list of Cms projects
     * @throws CmsException if operation was not succesful
     */
    public List getAllManageableProjects(CmsRequestContext context) throws CmsException {

        return m_driverManager.getAllManageableProjects(context);
    }

    /**
     * Get the next version id for the published backup resources.<p>
     *
     * @return the new version id
     */
    public int getBackupTagId() {

        return m_driverManager.getBackupTagId();
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return groups a Vector of all child groups or null
     * @throws CmsException if operation was not succesful.
     */
    public Vector getChild(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getChild(context, groupname);
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * This method also returns all sub-child groups of the current group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return a Vector of all child groups or null
     * @throws CmsException if operation was not succesful
     */
    public Vector getChilds(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getChilds(context, groupname);
    }

    /**
     * Method to access the configurations of the properties-file.<p>
     *
     * All users are granted.<p>
     *
     * @return the Configurations of the properties-file
     */
    public ExtendedProperties getConfigurations() {

        return m_driverManager.getConfigurations();
    }

    /**
     * Returns the list of groups to which the user directly belongs to<P/>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getDirectGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        return m_driverManager.getDirectGroupsOfUser(context, username);
    }

    /**
     * Returns all groups.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @return users a Vector of all existing groups
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroups(CmsRequestContext context) throws CmsException {

        return m_driverManager.getGroups(context);
    }

    /**
     * Returns the groups of a Cms user.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @return a vector of Cms groups filtered by the specified IP address
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        return m_driverManager.getGroupsOfUser(context, null, username);
    }

    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result vector
     * @return a vector of Cms groups
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username, String remoteAddress) throws CmsException {

        return m_driverManager.getGroupsOfUser(context, null, username, remoteAddress);
    }

    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to return the lock state for
     * @return the lock state of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        return m_driverManager.getLock(context, null, resource);
    }

    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to return the lock state for (full path)
     * @return the lock state of the resource 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) throws CmsException {

        return m_driverManager.getLock(context, null, resourcename);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param groupname the name of the group
     * @return group the parent group or null
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(String groupname) throws CmsException {

        return m_driverManager.getParent(groupname);
    }

    /**
     * Returns a users the permissions on a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param user the user
     * @return bitset with allowed permissions
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSetCustom getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user)
    throws CmsException {

        return m_driverManager.getPermissions(context, resource, user);
    }

    /**
     * Returns a Cms publish list object containing the Cms resources that actually get published.<p>
     * 
     * <ul>
     * <li>
     * <b>Case 1 (publish project)</b>: all new/changed/deleted Cms file resources in the current (offline)
     * project are inspected whether they would get published or not.
     * </li> 
     * <li>
     * <b>Case 2 (direct publish a resource)</b>: a specified Cms file resource and optionally it's siblings 
     * are inspected whether they get published.
     * </li>
     * </ul>
     * 
     * All Cms resources inside the publish ist are equipped with their full resource name including
     * the site root.<p>
     * 
     * Please refer to the source code of this method for the rules on how to decide whether a
     * new/changed/deleted Cms resource can be published or not.<p>
     * 
     * @param context the current request context
     * @param directPublishResource a Cms resource to be published directly (in case 2), or null (in case 1)
     * @param publishSiblings true, if all eventual siblings of the direct published resource should also get published (in case 2)
     * 
     * @return a publish list with all new/changed/deleted files from the current (offline) project that will be published actually
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.CmsPublishList
     */
    public synchronized CmsPublishList getPublishList(
        CmsRequestContext context,
        CmsResource directPublishResource,
        boolean publishSiblings) throws CmsException {

        return m_driverManager.getPublishList(context, directPublishResource, publishSiblings);
    }

    /**
     * Returns a list with all sub resources of a given folder that have benn modified in a given time range.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesInTimeRange(CmsRequestContext context, String folder, long starttime, long endtime)
    throws CmsException {

        return m_driverManager.getResourcesInTimeRange(context, folder, starttime, endtime);
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param path the folder to get the subresources from
     * @param propertyDefinition the name of the propertydefinition to check
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(CmsRequestContext context, String path, String propertyDefinition)
    throws CmsException {

        return m_driverManager.getResourcesWithProperty(context, path, propertyDefinition);
    }

    /**
     * Reads all resources that have set the specified property.<p>
     * 
     * A property definition is the "key name" of a property.<p>
     *
     * @param context the current request context
     * @param propertyDefinition the name of the property definition
     * @return list of Cms resources having set the specified property definition
     * @throws CmsException if operation was not successful
     */
    public List getResourcesWithPropertyDefinition(CmsRequestContext context, String propertyDefinition)
    throws CmsException {

        return m_driverManager.getResourcesWithPropertyDefinition(context, propertyDefinition);
    }

    /**
     * Get a parameter value for a task.<p>
     *
     * All users are granted.<p>
     *
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @return task parameter value
     * @throws CmsException if something goes wrong
     */
    public String getTaskPar(int taskId, String parName) throws CmsException {

        return m_driverManager.getTaskPar(taskId, parName);
    }

    /**
     * Get the template task id for a given taskname.<p>
     *
     * @param taskName name of the task
     * @return id from the task template
     * @throws CmsException if something goes wrong
     */
    public int getTaskType(String taskName) throws CmsException {

        return m_driverManager.getTaskType(taskName);
    }

    /**
     * Returns all users.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @return a Vector of all existing users
     * @throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsRequestContext context) throws CmsException {

        return m_driverManager.getUsers(context);
    }

    /**
     * Returns all users from a given type.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param type the type of the users
     * @return a Vector of all existing users
     * @throws CmsException if operation was not succesful
     */
    public Vector getUsers(CmsRequestContext context, int type) throws CmsException {

        return m_driverManager.getUsers(context, type);
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group to list users from
     * @return vector of users
     * @throws CmsException if operation was not succesful
     */
    public Vector getUsersOfGroup(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getUsersOfGroup(context, groupname);
    }

    /**
     * Performs a non-blocking permission check on a resource.<p>
     * 
     * This test will not throw an exception in case the required permissions are not
     * available for the requested operation. Instead, it will return one of the 
     * following values:<ul>
     * <li><code>{@link #PERM_ALLOWED}</code></li>
     * <li><code>{@link #PERM_FILTERED}</code></li>
     * <li><code>{@link #PERM_DENIED}</code></li></ul><p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required for the operation
     * @param checkLock if true, a lock for the current user is required for 
     *      all write operations, if false it's ok to write as long as the resource
     *      is not locked by another user
     * @param filter the resource filter to use
     * 
     * @return <code>PERM_ALLOWED</code> if the user has sufficient permissions on the resource
     *      for the requested operation
     * 
     * @throws CmsException in case of i/o errors (NOT because of insufficient permissions)
     * 
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public int hasPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        // check if the resource is valid according to the current filter
        // if not, throw a CmsResourceNotFoundException
        if (!filter.isValid(context, resource)) {
            return PERM_FILTERED;
        }

        // checking the filter is less cost intensive then checking the cache,
        // this is why basic filter results are not cached
        String cacheKey = m_keyGenerator.getCacheKeyForUserPermissions(
            String.valueOf(filter.requireVisible()),
            context,
            resource,
            requiredPermissions);
        Integer cacheResult = (Integer)m_permissionCache.get(cacheKey);
        if (cacheResult != null) {
            return cacheResult.intValue();
        }

        int denied = 0;

        // if this is the onlineproject, write is rejected 
        if (context.currentProject().isOnlineProject()) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }

        // check if the current user is admin
        boolean isAdmin = isAdmin(context);

        // if the resource type is jsp
        // write is only allowed for administrators
        if (!isAdmin && (resource.getTypeId() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }

        // check lock status 
        if (requiredPermissions.requiresWritePermission()
        || requiredPermissions.requiresControlPermission()) {
            // check lock state only if required
            CmsLock lock = m_driverManager.getLock(context, null, resource);
            // if the resource is not locked by the current user, write and control 
            // access must case a permission error that must not be cached
            if (checkLock || !lock.isNullLock()) {
                if (!context.currentUser().getId().equals(lock.getUserId())) {
                    return PERM_NOTLOCKED;
                }
            }
        }

        CmsPermissionSetCustom permissions;
        if (isAdmin) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSetCustom(~0);
        } else {
            // otherwise, get the permissions from the access control list
            permissions = getPermissions(context, resource, context.currentUser());
        }

        // revoke the denied permissions
        permissions.denyPermissions(denied);

        if ((permissions.getPermissions() & CmsPermissionSet.PERMISSION_VIEW) == 0) {
            // resource "invisible" flag is set for this user
            if (filter.requireVisible()) {
                // filter requires visible permission - extend required permission set
                requiredPermissions = new CmsPermissionSet(
                    requiredPermissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    requiredPermissions.getDeniedPermissions());
            } else {
                // view permissions can be ignored by filter
                permissions.setPermissions(
                    // modify permissions so that view is allowed
                    permissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    permissions.getDeniedPermissions() & ~CmsPermissionSet.PERMISSION_VIEW);                
            }
        }

        Integer result;
        if ((requiredPermissions.getPermissions() & (permissions.getPermissions()))
            == requiredPermissions.getPermissions()) {
            
            result = PERM_ALLOWED_INTEGER;
        } else {
            result = PERM_DENIED_INTEGER;
        }
        m_permissionCache.put(cacheKey, result);

        if ((result != PERM_ALLOWED_INTEGER) && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Access to resource "
                    + resource.getRootPath()
                    + " "
                    + "not permitted for user "
                    + context.currentUser().getName()
                    + ", "
                    + "required permissions "
                    + requiredPermissions.getPermissionString()
                    + " "
                    + "not satisfied by "
                    + permissions.getPermissionString());
        }

        return result.intValue();
    }

    /**
     * Writes a vector of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.<p>
     * 
     * Access is granted, if:<p>
     * 
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param acEntries list of access control entries applied to the resource
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsRequestContext context, CmsResource resource, List acEntries)
    throws CmsException {

        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        m_driverManager.importAccessControlEntries(context, resource, acEntries);
    }

    /**
     * Imports a import-resource (folder or zipfile) to the cms.<p>
     *
     * Only Administrators can do this.<p>
     *
     * @param cms the cms-object to use for the export
     * @param context the current request context
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @throws CmsException if something goes wrong
     */
    public void importFolder(CmsObject cms, CmsRequestContext context, String importFile, String importPath)
    throws CmsException {

        m_driverManager.importFolder(cms, context, importFile, importPath);
    }

    /**
     * Initializes this security manager with a given runtime info factory.<p>
     * 
     * @param configurationManager the configurationManager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * @throws CmsException if something goes wrong
     */
    public void init(CmsConfigurationManager configurationManager, I_CmsRuntimeInfoFactory runtimeInfoFactory)
    throws CmsException {

        ExtendedProperties config = configurationManager.getConfiguration();

        try {
            // initialize the key generator
            m_keyGenerator = (I_CmsCacheKey)Class.forName(
                config.getString(I_CmsConstants.C_CONFIGURATION_CACHE + ".keygenerator")).newInstance();
        } catch (Exception e) {
            throw new CmsException("Unable to create security manager classes", e);
        }

        LRUMap hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".permissions", 1000));
        m_permissionCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_permissionCache", hashMap);
        }

        m_driverManager = CmsDriverManager.newInstance(configurationManager, this, runtimeInfoFactory);

        if (runtimeInfoFactory == null) {
            String message = "Critical error while loading security manager: runtime info factory is null";
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(message);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR);
        } else {
            m_runtimeInfoFactory = runtimeInfoFactory;
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Security manager init: ok - finished");
        }
    }

    /**
     * Checks if the current user has "Administrator" permissions.<p>
     * 
     * Administrator permissions means that the user is a member of the 
     * administrators group, which per default is called "Administrators".<p>
     *
     * @param context the current request context
     * @return true, if the current user has "Administrator" permissions
     */
    public boolean isAdmin(CmsRequestContext context) {

        return m_driverManager.isAdmin(context);
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param context the current request context
     * @param resourcename the specified resource name (full path)
     * 
     * @return true, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, String resourcename) {

        return m_driverManager.isInsideCurrentProject(context, resourcename);
    }

    /**
     * Checks if the current user has management access to the project.<p>
     *
     * Please note: This is NOT the same as the {@link #isProjectManager(CmsRequestContext)} 
     * check. If the user has management access to a project depends on the
     * project settings.<p>
     * 
     * @param context the current request context
     * @return true if the user has management access to the project
     * @see CmsObject#isManagerOfProject()
     * @see #isProjectManager(CmsRequestContext)
     */
    public boolean isManagerOfProject(CmsRequestContext context) {

        return m_driverManager.isManagerOfProject(context);
    }

    /**
     * Checks if the current user is a member of the project manager group.<p>
     *
     * Please note: This is NOT the same as the {@link #isManagerOfProject(CmsRequestContext)} 
     * check. If the user is a member of the project manager group, 
     * he can create new projects.<p>
     *
     * @param context the current request context
     * @return true if the user is a member of the project manager group
     * @see CmsObject#isProjectManager()
     * @see #isManagerOfProject(CmsRequestContext)
     */
    public boolean isProjectManager(CmsRequestContext context) {

        return m_driverManager.isProjectManager(context);
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_TEMP}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void lockResource(CmsRequestContext context, CmsResource resource, int mode) throws CmsException {

        // check if the user has write access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.lockResource(context, runtimeInfo, resource, mode);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error deleting resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * For security reasons, all error / exceptions that occur here are "blocked" and 
     * a simple security exception is thrown.<p>
     * 
     * @param username the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * @param userType the user type to log in (System user or Web user)
     * @return the logged in users name
     *
     * @throws CmsSecurityException if login was not succesful
     */
    public CmsUser loginUser(String username, String password, String remoteAddress, int userType)
    throws CmsSecurityException {

        return m_driverManager.loginUser(username, password, remoteAddress, userType);
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param principalId the UUID of the principal to lookup
     * @return the principal (group or user) if found, otherwise null
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        return m_driverManager.lookupPrincipal(principalId);
    }

    /**
     * Lookup and read the user or group with the given name.<p>
     * 
     * @param principalName the name of the principal to lookup
     * @return the principal (group or user) if found, otherwise null
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        return m_driverManager.lookupPrincipal(principalName);
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The method can also be used to check get the name of a resource
     * in the "lost and found" folder only without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to apply this operation to
     * @param returnNameOnly if <code>true</code>, only the name of the resource in the "lost and found" 
     *        folder is returned, the move operation is not really performed
     * 
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     * @see CmsObject#getLostAndFoundName(String)
     */
    public String moveToLostAndFound(CmsRequestContext context, String resourcename, boolean returnNameOnly)
    throws CmsException {

        String lostAndFoundPath;

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            lostAndFoundPath = m_driverManager.moveToLostAndFound(context, runtimeInfo, resourcename, returnNameOnly);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error moving resource " + resourcename + " to lost+found", e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return lostAndFoundPath;
    }

    /**
     * Publishes a project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param cms the current CmsObject
     * @param publishList a Cms publish list
     * @param report a report object to provide the loggin messages
     * 
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean)
     */
    public synchronized void publishProject(CmsObject cms, CmsPublishList publishList, I_CmsReport report)
    throws Exception {

        CmsRequestContext context = cms.getRequestContext();
        int publishProjectId = context.currentProject().getId();

        // has the current user the required permissions to publish the current project/direct published resource?
        boolean hasPublishPermissions = false;

        // the current user either has to be a member of the administrators group
        hasPublishPermissions |= isAdmin(context);

        // or he has to be a member of the project managers group
        hasPublishPermissions |= isManagerOfProject(context);

        if (publishList.isDirectPublish()) {

            CmsResource directPublishResource = publishList.getDirectPublishResource();

            // or he has the explicit permission to direct publish a resource
            hasPublishPermissions |= (PERM_ALLOWED == hasPermissions(
                context,
                directPublishResource,
                CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                true,
                CmsResourceFilter.ALL));
        }

        // and the current project must be different from the online project
        hasPublishPermissions &= (publishProjectId != I_CmsConstants.C_PROJECT_ONLINE_ID);

        // and the project flags have to be set to zero
        hasPublishPermissions &= (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED);

        if (hasPublishPermissions) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_PUBLISH);

            try {
                m_driverManager.publishProject(cms, runtimeInfo, publishList, report);
            } finally {
                runtimeInfo.clear();
            }

        } else if (publishProjectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {

            throw new CmsSecurityException("["
                + getClass().getName()
                + "] could not publish project "
                + publishProjectId, CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        } else if (!isAdmin(context) && !isManagerOfProject(context)) {

            throw new CmsSecurityException("["
                + getClass().getName()
                + "] could not publish project "
                + publishProjectId, CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {

            throw new CmsSecurityException("["
                + getClass().getName()
                + "] could not publish project "
                + publishProjectId, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Reaktivates a task from the Cms.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task to accept
     * @throws CmsException if something goes wrong
     */
    public void reactivateTask(CmsRequestContext context, int taskId) throws CmsException {

        m_driverManager.reactivateTask(context, taskId);
    }

    /**
     * Reads the agent of a task from the OpenCms.<p>
     *
     * @param task the task to read the agent from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readAgent(CmsTask task) throws CmsException {

        return m_driverManager.readAgent(task);
    }

    /**
     * Reads all available backup resources for the specified resource from the OpenCms VFS.<p>
     *
     * @param context the current request context
     * @param resourcename the name of the file to be read
     * 
     * @return a List of backup resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllBackupFileHeaders(CmsRequestContext context, String resourcename) throws CmsException {

        // read the resource and check the permissions first 
        CmsResource resource = readResource(context, null, resourcename, CmsResourceFilter.ALL);

        // now read the list of backup resources
        return m_driverManager.readAllBackupFileHeaders(resource);
    }

    /**
     * Returns a list with all project resources for a given project.<p>
     *
     * @param context the current request context
     * @param projectId the ID of the project
     * @return a list of all project resources
     * @throws CmsException if operation was not succesful
     */
    public List readAllProjectResources(CmsRequestContext context, int projectId) throws CmsException {

        return m_driverManager.readAllProjectResources(context, projectId);
    }

    /**
     * Reads all propertydefinitions for the given mapping type.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param mappingtype the mapping type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the mapping type. The Vector is maybe empty.
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertydefinitions(CmsRequestContext context, int mappingtype) throws CmsException {

        return m_driverManager.readAllPropertydefinitions(context, mappingtype);
    }

    /**
     * Reads a file from the history of the Cms.<p>
     * 
     * The reading includes the filecontent. A file is read from the backup resources.<p>
     *
     * @param context the current request context
     * @param tagId the id of the tag of the file
     * @param filename the name of the file to be read
     * @return the file read from the Cms.
     * @throws CmsException if operation was not succesful
     */
    public CmsBackupResource readBackupFile(CmsRequestContext context, int tagId, String filename) throws CmsException {

        // read the resource (also checks read permission)
        CmsResource resource = readResource(context, null, filename, CmsResourceFilter.ALL);
        
        // now read the backup resource
        return m_driverManager.readBackupFile(tagId, resource);
    }

    /**
     * Reads the backupinformation of a project from the Cms.<p>
     *
     * @param tagId the tagId of the project
     * @return the backup project
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {

        return m_driverManager.readBackupProject(tagId);
    }

    /**
     * Returns the child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the parameters <code>getFolders</code> and <code>getFiles</code>
     * you can control what type of resources you want in the result list:
     * files, folders, or both.<p>
     * 
     * @param context the current request context
     * @param resource the resource to return the child resources for
     * @param filter the resource filter to use
     * @param getFolders if true the child folders are included in the result
     * @param getFiles if true the child files are included in the result
     * 
     * @return a list of all child resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readChildResources(
        CmsRequestContext context,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles) throws CmsException {

        // check the access permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

        return m_driverManager.readChildResources(context, resource, filter, getFolders, getFiles);
    }

    /**
     * Reads a file (including it's content) from the OpenCms VFS.<p>
     * 
     * @param context the current request context
     * @param filename the name of the file to be read
     * @param filter the filter object
     * 
     * @return the file read from the VFS
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFile readFile(CmsRequestContext context, String filename, CmsResourceFilter filter) throws CmsException {

        // first read as resource, this also checks the permissions
        CmsResource resource = readResource(context, null, filename, filter);

        return m_driverManager.readFile(context, resource, filter);
    }
    
    /**
     * Reads a folder from the VFS, using the specified resource filter.<p>
     * 
     * @param context the current request context
     * @param runtimeInfo the current runtime info
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsException if something goes wrong
     *
     * @see CmsObject#readFolder(String)
     * @see CmsObject#readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(CmsRequestContext context, I_CmsRuntimeInfo runtimeInfo, String resourcename, CmsResourceFilter filter)
    throws CmsException {
        
        CmsResource resource = readResource(context, runtimeInfo, resourcename, filter);
        
        return m_driverManager.convertResourceToFolder(resource);
    }

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param ownerName owner of the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy chooses, how to order the tasks
     * @param sort sorting of the tasks
     * @return vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readGivenTasks(projectId, ownerName, taskType, orderBy, sort);
    }

    /**
     * Reads the group of a project from the OpenCms.<p>
     *
     * @param project the project to read from
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsProject project) {

        return m_driverManager.readGroup(project);
    }

    /**
     * Reads the group (role) of a task from the OpenCms.<p>
     *
     * @param task the task to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsTask task) throws CmsException {

        return m_driverManager.readGroup(task);
    }

    /**
     * Returns a group object.<p>
     *
     * All users are granted.<p>
     *
     * @param groupId the id of the group that is to be read
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {

        return m_driverManager.readGroup(groupId);
    }

    /**
     * Returns a group object.<p>
     * 
     * @param groupname the name of the group that is to be read
     *
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(String groupname) throws CmsException {

        return m_driverManager.readGroup(null, groupname);
    }

    /**
     * Reads the manager group of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param project the project to read from
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsProject project) {

        return m_driverManager.readManagerGroup(project);
    }

    /**
     * Reads the original agent of a task from the OpenCms.<p>
     *
     * @param task the task to read the original agent from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOriginalAgent(CmsTask task) throws CmsException {

        return m_driverManager.readOriginalAgent(task);
    }

    /**
     * Reads the owner of a project from the OpenCms.<p>
     *
     * @param project the project to get the owner from
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {

        return m_driverManager.readOwner(project);
    }

    /**
     * Reads the owner (initiator) of a task from the OpenCms.<p>
     *
     * @param task the task to read the owner from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTask task) throws CmsException {

        return m_driverManager.readOwner(task);
    }

    /**
     * Reads the owner of a tasklog from the OpenCms.<p>
     *
     * @param log the tasklog
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTaskLog log) throws CmsException {

        return m_driverManager.readOwner(log);
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(int projectId, String path, CmsResourceFilter filter) throws CmsException {

        return m_driverManager.readPath(projectId, path, filter);
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * @param task the task to read the project of
     * @return the project read from the cms
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsTask task) throws CmsException {

        return m_driverManager.readProject(task);
    }

    /**
     * Reads a project from the Cms given the projects name.<p>
     *
     * @param id the id of the project
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(int id) throws CmsException {

        return m_driverManager.readProject(id);
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(int)} to ensure the right project is read.<p>
     * 
     * @param name the name of the project
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(String name) throws CmsException {

        return m_driverManager.readProject(name);
    }

    /**
     * Reads log entries for a project.<p>
     *
     * @param projectId the id of the projec for tasklog to read
     * @return a list of new TaskLog objects
     * @throws CmsException if something goes wrong.
     */
    public List readProjectLogs(int projectId) throws CmsException {

        return m_driverManager.readProjectLogs(projectId);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param project the project to get the project resources for
     * @return the list of all resource names that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsProject project) throws CmsException {

        return m_driverManager.readProjectResources(project);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     *
     * @param context the current request context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return all resources of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(int, int)
     */
    public List readProjectView(CmsRequestContext context, int projectId, int state) throws CmsException {

        return m_driverManager.readProjectView(context, projectId, state);
    }

    /**
     * Reads a definition for the given resource type.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param mappingtype the mapping type of this propery definition
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition.
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name, int mappingtype)
    throws CmsException {

        return m_driverManager.readPropertydefinition(context, name, mappingtype);
    }

    /**
     * Reads a property object from the database specified by it's key name mapped to a resource.<p>
     * 
     * Returns null if the property is not found.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param key the property key name
     * @param search true, if the property should be searched on all parent folders  if not found on the resource
     * 
     * @return a CmsProperty object containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsRequestContext context, String resourceName, String key, boolean search)
    throws CmsException {

        // read the resource first (also checks the permissions)
        CmsResource resource = readResource(context, null, resourceName, CmsResourceFilter.ALL);

        return m_driverManager.readPropertyObject(context, resource, key, search);
    }

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * Returns an empty list if no properties are found at all.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param search true, if the properties should be searched on all parent folders  if not found on the resource
     * 
     * @return a list of CmsProperty objects containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(CmsRequestContext context, String resourceName, boolean search) 
    throws CmsException {

        // read the resource first (also checks the permissions)
        CmsResource resource = readResource(context, null, resourceName, CmsResourceFilter.ALL);

        return m_driverManager.readPropertyObjects(context, resource, search);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param context the current request context
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * @return a List of CmsPublishedResource objects
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsRequestContext context, CmsUUID publishHistoryId) throws CmsException {

        return m_driverManager.readPublishedResources(context, publishHistoryId);
    }

    /**
     * Reads a resource from the OpenCms VFS, using the specified resource filter.<p>
     * 
     * @param context the current request context
     * @param runtimeInfo the current runtime info
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(
        CmsRequestContext context,
        I_CmsRuntimeInfo runtimeInfo,
        String resourcePath,
        CmsResourceFilter filter) throws CmsException {

        // read the resource from the VFS
        CmsResource resource = m_driverManager.readResource(context, runtimeInfo, resourcePath, filter);

        // check if the user has read access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, filter);

        // access was granted - return the resource
        return resource;
    }

    /**
     * Reads all resources below the given parent matching the filter criteria.<p>
     * 
     * @param context the current request context
     * @param parent the parent to read the resources from
     * @param filter the filter criteria to apply
     * @param readTree true to indicate to read all subresources, false to read immediate children only
     * @return a list with resources below parentPath matchin the filter criteria
     *  
     * @throws CmsException if something goes wrong
     */
    public List readResources(CmsRequestContext context, CmsResource parent, CmsResourceFilter filter, boolean readTree)
    throws CmsException {

        // check the access permissions
        checkPermissions(context, parent, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

        return m_driverManager.readResources(context, parent, filter, readTree);
    }

    /**
     * Returns a List of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param context the request context
     * @param resourcename the name of the specified resource
     * @param filter a filter object
     * 
     * @return a List of CmsResources that are siblings to the specified resource, including the specified resource itself 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(CmsRequestContext context, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        // read the base resource first (will also check the permissions)
        CmsResource resource = readResource(context, null, resourcename, filter);

        return m_driverManager.readSiblings(context, null, resource, filter);
    }

    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param context the current request context
     * @param rfsName the rfs name of the resource
     * @return the paramter string of the requested resource
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(CmsRequestContext context, String rfsName)
    throws CmsException {

        return m_driverManager.readStaticExportPublishedResourceParameters(context, rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param context the current request context
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp for reading the data from the db
     * @return List of template resources
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(CmsRequestContext context, int parameterResources, long timestamp)
    throws CmsException {

        return m_driverManager.readStaticExportResources(context, parameterResources, timestamp);
    }

    /**
     * Read a task by id.<p>
     *
     * @param id the id for the task to read
     * @return a task
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(int id) throws CmsException {

        return m_driverManager.readTask(id);
    }

    /**
     * Reads log entries for a task.<p>
     *
     * @param taskid the task for the tasklog to read
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    public Vector readTaskLogs(int taskid) throws CmsException {

        return m_driverManager.readTaskLogs(taskid);
    }

    /**
     * Reads all tasks for a project.<p>
     *
     * All users are granted.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined. Can be null for all tasks
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException  if something goes wrong
     */
    public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        return m_driverManager.readTasksForProject(projectId, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a role in a project.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param roleName the user who has to process the task
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readTasksForRole(projectId, roleName, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a user in a project.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param userName the user who has to process the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readTasksForUser(int projectId, String userName, int taskType, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readTasksForUser(projectId, userName, taskType, orderBy, sort);
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * All users are granted.<p>
     * 
     * @param id the id of the user to read
     *
     * @return the user read 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsUUID id) throws CmsException {

        return m_driverManager.readUser(null, id);
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.<p>
     * 
     * @param username the name of the user that is to be read
     *
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username) throws CmsException {

        return m_driverManager.readUser((I_CmsRuntimeInfo)null, username);
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.<p>
     * 
     * @param username the name of the user that is to be read
     * @param type the type of the user
     *
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, int type) throws CmsException {

        return m_driverManager.readUser(null, username, type);
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.<p>
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        return m_driverManager.readUser(username, password);
    }

    /**
     * Read a web user from the database.<p>
     * 
     * @param username the web user to read
     * @return the read web user
     * @throws CmsException if the user could not be read 
     */
    public CmsUser readWebUser(String username) throws CmsException {

        return m_driverManager.readWebUser(username);
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.<p>
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {

        return m_driverManager.readWebUser(username, password);
    }

    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param principal the id of the principal to remove the the access control entry for
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal)
    throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.removeAccessControlEntry(context, runtimeInfo, resource, principal);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error removing ACE on resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Removes a user from a group.<p>
     *
     * Only users, which are in the group "administrators" are granted.
     * 
     * @param context the current request context
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     *
     * @throws CmsException if operation was not succesful
     */
    public void removeUserFromGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.removeUserFromGroup(context, runtimeInfo, username, groupname);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error removing user " + username + " from group " + groupname, e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] removeUserFromGroup()",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to apply this operation to
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsRequestContext context,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.replaceResource(context, runtimeInfo, resource, type, content, properties);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error replacing resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * @throws CmsException if the user data could not be read from the database
     * @throws CmsSecurityException if the specified username and old password could not be verified
     */
    public void resetPassword(String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        m_driverManager.resetPassword(username, oldPassword, newPassword);
    }

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param context the current request context
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see org.opencms.file.types.I_CmsResourceType#restoreResourceBackup(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsRequestContext context, CmsResource resource, int tag) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.restoreResource(context, runtimeInfo, resource, tag);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error restoring resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Set a new name for a task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param name the new name value
     * @throws CmsException if something goes wrong
     */
    public void setName(CmsRequestContext context, int taskId, String name) throws CmsException {

        m_driverManager.setName(context, taskId, name);
    }

    /**
     * Sets a new parent-group for an already existing group in the Cms.<p>
     *
     * Only the admin can do this.<p>
     * 
     * @param context the current request context
     * @param groupName the name of the group that should be written to the Cms
     * @param parentGroupName the name of the parentGroup to set, or null if the parent group should be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void setParentGroup(CmsRequestContext context, String groupName, String parentGroupName) 
    throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.setParentGroup(context, runtimeInfo, groupName, parentGroupName);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(
                    null,
                    "Error setting parent group to " + parentGroupName + " of group " + groupName,
                    e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] setParentGroup() " + groupName,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Sets the password for a user.<p>
     *
     * Only users in the group "Administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @param newPassword the new password
     * @throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsRequestContext context, String username, String newPassword) throws CmsException {

        m_driverManager.setPassword(context, username, newPassword);
    }

    /**
     * Set priority of a task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param priority the priority value
     * @throws CmsException if something goes wrong
     */
    public void setPriority(CmsRequestContext context, int taskId, int priority) throws CmsException {

        m_driverManager.setPriority(context, taskId, priority);
    }

    /**
     * Set a Parameter for a task.<p>
     *
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @param parValue value if the parameter
     * @throws CmsException if something goes wrong.
     */
    public void setTaskPar(int taskId, String parName, String parValue) throws CmsException {

        m_driverManager.setTaskPar(taskId, parName, parValue);
    }

    /**
     * Set timeout of a task.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param timeout new timeout value
     * @throws CmsException if something goes wrong
     */
    public void setTimeout(CmsRequestContext context, int taskId, long timeout) throws CmsException {

        m_driverManager.setTimeout(context, taskId, timeout);
    }

    /**
     * Change the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expires" date of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * @param dateReleased the new release date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param dateExpired the new expire date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#touch(String, long, long, long, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#touch(CmsObject, CmsSecurityManager, CmsResource, long, long, long, boolean)
     */
    public void touch(
        CmsRequestContext context,
        CmsResource resource,
        long dateLastModified,
        long dateReleased,
        long dateExpired) throws CmsException {

        //  check if the user has write access
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.touch(context, runtimeInfo, resource, dateLastModified, dateReleased, dateExpired);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error touching resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undoChanges(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.undoChanges(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error undoing changes of resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Unlocks all resources in this project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     *
     * @param context the current request context
     * @param projectId the id of the project to be published
     * @throws CmsException if something goes wrong
     */
    public void unlockProject(CmsRequestContext context, int projectId) throws CmsException {

        m_driverManager.unlockProject(context, projectId);
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to unlock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see org.opencms.file.types.I_CmsResourceType#unlockResource(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.unlockResource(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error undoing changes of resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Checks if a user is member of a group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     * 
     * @param context the current request context
     * @param username the name of the user to check
     * @param groupname the name of the group to check
     *
     * @return true or false
     * @throws CmsException if operation was not succesful
     */
    public boolean userInGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        return m_driverManager.userInGroup(context, null, username, groupname);
    }

    /**
     * Validates the Cms resources in a Cms publish list.<p>
     * 
     * @param cms the current user's Cms object
     * @param publishList a Cms publish list
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean)
     */
    public Map validateHtmlLinks(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {

        return m_driverManager.validateHtmlLinks(cms, publishList, report);
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class configured in opencms.properties.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {

        m_driverManager.validatePassword(password);
    }

    /**
     * Writes an access control entries to a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param ace the entry to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsAccessControlEntry ace)
    throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS_AND_USER);

        try {
            m_driverManager.writeAccessControlEntry(context, runtimeInfo, resource, ace);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error writing ACE on resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

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
     * @param context the current request context
     * @param resource the resource to apply this operation to
     * 
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsRequestContext context, CmsFile resource) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        CmsFile file;

        try {
            file = m_driverManager.writeFile(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error writing file " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

        return file;
    }

    /**
     * Writes an already existing group in the Cms.<p>
     *
     * Only the admin can do this.
     * 
     * @param context the current request context
     * @param group the group that should be written to the Cms
     *
     * @throws CmsException if operation was not succesfull
     */
    public void writeGroup(CmsRequestContext context, CmsGroup group) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.writeGroup(runtimeInfo, group);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error writing group " + group.getName(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeGroup() " + group.getName(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObject(CmsObject, CmsSecurityManager, CmsResource, CmsProperty)
     */
    public void writePropertyObject(CmsRequestContext context, CmsResource resource, CmsProperty property)
    throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS + I_CmsRuntimeInfo.C_RUNTIMEINFO_BACKUP);

        try {
            m_driverManager.writePropertyObject(context, runtimeInfo, resource, property);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error writing property "
                + property.getKey()
                + " on resource "
                + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param context the current request context
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObjects(CmsObject, CmsSecurityManager, CmsResource, List)
     */
    public void writePropertyObjects(CmsRequestContext context, CmsResource resource, List properties)
    throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS + I_CmsRuntimeInfo.C_RUNTIMEINFO_BACKUP);

        try {
            // write the properties
            m_driverManager.writePropertyObjects(context, runtimeInfo, resource, properties);

            // update the resource state
            resource.setUserLastModified(context.currentUser().getId());
            m_driverManager.getVfsDriver().writeResource(
                runtimeInfo,
                context.currentProject(),
                resource,
                CmsDriverManager.C_UPDATE_RESOURCE_STATE);

            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error writing properties on resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Writes a resource to the OpenCms VFS.<p>
     * 
     * @param context the current request context
     * @param resource the resource to write
     *
     * @throws CmsException if something goes wrong
     */
    public void writeResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
            m_driverManager,
            I_CmsRuntimeInfo.C_RUNTIMEINFO_VFS);

        try {
            m_driverManager.writeResource(context, runtimeInfo, resource);
            runtimeInfo.pop();
        } catch (CmsException e) {
            runtimeInfo.report(null, "Error writing resource " + resource.getRootPath(), e);
            throw e;
        } finally {
            runtimeInfo.clear();
        }

    }

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param context the current request context
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        CmsRequestContext context,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_driverManager.writeStaticExportPublishedResource(context, resourceName, linkType, linkParameter, timestamp);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskid the Id of the task
     * @param comment description for the log
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(CmsRequestContext context, int taskid, String comment) throws CmsException {

        m_driverManager.writeTaskLog(context, taskid, comment);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param comment description for the log
     * @param type type of the tasklog. User tasktypes must be greater then 100
     * @throws CmsException something goes wrong
     */
    public void writeTaskLog(CmsRequestContext context, int taskId, String comment, int type) throws CmsException {

        m_driverManager.writeTaskLog(context, taskId, comment, type);
    }

    /**
     * Updates the user information.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param user The  user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeUser(CmsRequestContext context, CmsUser user) throws CmsException {

        if (isAdmin(context) || (context.currentUser().equals(user))) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.writeUser(context, runtimeInfo, user);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error writing user " + user.getName(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

    }

    /**
     * Updates the user information of a web user.<p>
     *
     * Only users of the user type webuser can be updated this way.<p>
     * 
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsUser user) throws CmsException {

        if (user.isWebUser()) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.writeWebUser(runtimeInfo, user);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error writing web user " + user.getName(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeWebUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

    }

    /**
     * Clears the permission cache.<p>
     */
    protected void clearPermissionCache() {

        m_permissionCache.clear();
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_driverManager != null) {
                m_driverManager.destroy();
            }
        } catch (Throwable t) {
            OpenCms.getLog(this).error("Error closing driver manager", t);
        }

        m_driverManager = null;
        m_runtimeInfoFactory = null;

        super.finalize();

    }

    /**
     * Applies the permission check result of a previous call 
     * to {@link #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)}.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * 
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsVfsResourceNotFoundException if the required resource has been filtered
     * @throws CmsLockException if the lock status is not as required
     * 
     * @see #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    private void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        int permissions) throws CmsSecurityException, CmsVfsResourceNotFoundException, CmsLockException {

        switch (permissions) {
            case PERM_FILTERED:
                throw new CmsVfsResourceNotFoundException("Resource not found '" + context.getSitePath(resource) + "'");
                
            case PERM_DENIED:
                throw new CmsSecurityException("Denied access to resource '"
                    + context.getSitePath(resource)
                    + "', required permissions are "
                    + requiredPermissions.getPermissionString(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
                
            case PERM_NOTLOCKED:
                throw new CmsLockException("Resource '"
                    + context.getSitePath(resource)
                    + "' not locked to current user!", CmsLockException.C_RESOURCE_NOT_LOCKED_BY_CURRENT_USER);
                
            case PERM_ALLOWED:
            default:
                return;
        }
    }
}