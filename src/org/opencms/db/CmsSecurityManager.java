/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsSecurityManager.java,v $
 * Date   : $Date: 2004/12/17 16:15:23 $
 * Version: $Revision: 1.25 $
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
import java.util.List;
import java.util.Map;
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
 * @author Michael Moossen (m.mmoossen@alkacon.com)
 * 
 * @version $Revision: 1.25 $
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

    /** The factory to create runtime info objects. */
    protected I_CmsDbContextFactory m_dbContextFactory;

    /** The initialized OpenCms driver manager to access the database. */
    protected CmsDriverManager m_driverManager;

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
        I_CmsDbContextFactory runtimeInfoFactory) throws CmsException {

        CmsSecurityManager securityManager = new CmsSecurityManager();
        securityManager.init(configurationManager, runtimeInfoFactory);

        return securityManager;
    }

    /**
     * Accept a task from the Cms.<p>
     * 
     * All users are granted.<p>
     *
     * @param context the current database context
     * @param taskId the Id of the task to accept.
     *
     * @throws CmsException if something goes wrong
     */
    public void acceptTask(CmsRequestContext context, int taskId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            m_driverManager.acceptTask(dbc, taskId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            result = m_driverManager.accessProject(dbc, projectId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
        return result;        
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
        Map additionalInfos,
        String address,
        int type) throws CmsException {

        CmsUser newUser = null;

        if (isAdmin(context)) {

            CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
            
            try {
                newUser = m_driverManager.addImportUser(
                    dbc,
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

            } catch (Exception e) {
                dbc.report(null, "Error importing user " + name, e);
            } finally {
                dbc.clear();
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
        Map additionalInfos) throws CmsException {

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] addUser() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.addUser(dbc, name, password, group, description, additionalInfos);
        } catch (Exception e) {
            dbc.report(null, "Error adding user " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);                
        try {
            m_driverManager.addUserToGroup(dbc, username, groupname);
        } catch (Exception e) {
            dbc.report(null, "Error adding user " + username + " to group " + groupname, e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Adds a web user to the Cms.<p>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param context the current request context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these infos may be stored into the Usertables (depending on the implementation)
     * 
     * @return the new user will be returned
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsRequestContext context, String name, String password, String group, String description, Map additionalInfos)
    throws CmsException {

        CmsUser result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            result = m_driverManager.addWebUser(dbc, name, password, group, description, additionalInfos);
        } catch (Exception e) {
            dbc.report(null, "Error adding web user " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Adds a web user to the Cms.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param context the current request context
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
        CmsRequestContext context,
        String name,
        String password,
        String group,
        String additionalGroup,
        String description,
        Map additionalInfos) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.addWebUser(
                dbc,
                name,
                password,
                group,
                additionalGroup,
                description,
                additionalInfos);
        } catch (Exception e) {
            dbc.report(null, "Error adding web user " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            m_driverManager.backupProject(dbc, backupProject, tagId, publishDate);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.changeLastModifiedProjectId(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                "Error changing last-modified-in-project id of resource " + resource.getRootPath(),
                e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);           
        try {
            m_driverManager.changeLock(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, "Error changing lock of resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
        }
    }
    
    /**
     * Returns a list with all sub resources of a given folder that have set the given property, 
     * matching the current property's value with the given old value and replacing it by a given new value.<p>
     *
     * @param context the current request context
     * @param resource the resource on which property definition values are changed
     * @param propertyDefinition the name of the propertydefinition to change the value
     * @param oldValue the old value of the propertydefinition
     * @param newValue the new value of the propertydefinition
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     * 
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed.
     *
     * @throws CmsException if operation was not successful
     */
    public List changeResourcesInFolderWithProperty(CmsRequestContext context, CmsResource resource, String propertyDefinition, String oldValue, String newValue, boolean recursive) throws CmsException {
        
        // collect the resources to look up
        List resources = new ArrayList();
        if (recursive) {
            resources = readResourcesWithProperty(context, resource.getRootPath(), propertyDefinition);
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

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] changeUserType() " + userId.toString(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.changeUserType(dbc, userId, userType);
        } catch (Exception e) {
            dbc.report(null, "Error changing type of user ID " + userId.toString(), e);
        } finally {
            dbc.clear();
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

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] changeUserType() " + username,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.changeUserType(dbc, username, userType);
        } catch (Exception e) {
            dbc.report(null, "Error changing type of user " + username, e);
        } finally {
            dbc.clear();
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
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, int)
     */
    public void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException, CmsVfsResourceNotFoundException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);      
        try {
            // check the access permissions
            checkPermissions(dbc, resource, requiredPermissions, checkLock, filter);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#chflags(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsRequestContext context, CmsResource resource, int flags) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);               
            m_driverManager.chflags(dbc, resource, flags);
        } catch (Exception e) {
            dbc.report(null, "Error changing flags of " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
        }
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
     * @param context the current request context
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.types.I_CmsResourceType#chtype(CmsObject, CmsSecurityManager, CmsResource, int)
     * @see CmsObject#chtype(String, int)
     */
    public void chtype(CmsRequestContext context, CmsResource resource, int type) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.chtype(dbc, resource, type);
        } catch (Exception e) {
            dbc.report(null, "Error changing resource type of " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, destination, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.copyAccessControlEntries(dbc, source, destination);
        } catch (Exception e) {
            dbc.report(null, "Error copying ACEs from " + source.getRootPath() + " to " + destination.getRootPath(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.copyResource(dbc, source, destination, siblingMode);
        } catch (Exception e) {
            dbc.report(null, "Error copying resource " + source.getRootPath() + " to " + destination, e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.copyResourceToProject(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        int result = 0;
        try {
            result = m_driverManager.countLockedResources(dbc, id);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        int result = 0;
        try {
            result = m_driverManager.countLockedResources(dbc, foldername);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.createGroup(dbc, id, name, description, flags, parent);
        } catch (Exception e) {
            dbc.report(null, "Error creating group " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        if (! isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.createGroup(dbc, name, description, flags, parent);
        } catch (Exception e) {
            dbc.report(null, "Error creating group " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsProject result = null;
        try {
            result = m_driverManager.createProject(dbc, name, description, groupname, managergroupname, projecttype);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param context the current request context
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition createPropertydefinition(CmsRequestContext context, String name) throws CmsException {

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createPropertydefinition() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsPropertydefinition result = null;
        try {
            result = m_driverManager.createPropertydefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, "Error creating property definition " + name, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource newResource = null;
        try {
            newResource = m_driverManager.createResource(dbc, resourcePath, resource, content, properties, importCase);
        } catch (Exception e) {
            dbc.report(null, "Error creating resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource newResource = null;
        try {
            newResource = m_driverManager.createResource(dbc, resourcename, type, content, properties);
        } catch (Exception e) {
            dbc.report(null, "Error creating resource " + resourcename, e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.createSibling(dbc, source, destination, properties);
        } catch (Exception e) {
            dbc.report(null, "Error creating sibling of " + source.getRootPath(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Creates a new task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param currentUser the current user
     * @param projectid the current project id
     * @param agentName user who will edit the task
     * @param roleName usergroup for the task
     * @param taskName name of the task
     * @param taskType type of the task
     * @param taskComment description of the task
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * 
     * @return a new task object
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(
        CmsRequestContext context,
        CmsUser currentUser,
        int projectid,
        String agentName,
        String roleName,
        String taskName,
        String taskComment,
        int taskType,
        long timeout,
        int priority) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsTask result = null;
        try {
            result = m_driverManager.createTask(
                dbc,
                currentUser,
                projectid,
                agentName,
                roleName,
                taskName,
                taskComment,
                taskType,
                timeout,
                priority);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;        
    }

    /**
     * Creates a new task.<p>
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsTask result = null;
        try {
            result = m_driverManager.createTask(dbc, agentName, roleName, taskname, timeout, priority);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;                  
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsProject result = null;
        try {
            result = m_driverManager.createTempfileProject(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param context the current request context
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsRequestContext context, int linkType) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.deleteAllStaticExportPublishedResources(dbc, linkType);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.deleteBackups(dbc, timestamp, versions, report);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteGroup() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
        if (OpenCms.getDefaultUsers().isDefaultGroup(name)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteGroup() " + name,
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);            
        }
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.deleteGroup(dbc, name);
        } catch (Exception e) {
            dbc.report(null, "Error deleteing group " + name, e);
        } finally {
            dbc.clear();
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

        if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            // online project must not be deleted
            throw new CmsSecurityException("["
                + this.getClass().getName()
                + "] deleteProject() "
                + deleteProject.getName(), CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        }

        if ((!isAdmin(context) && !isManagerOfProject(context))) {
            throw new CmsSecurityException("["
                + this.getClass().getName()
                + "] deleteProject() "
                + deleteProject.getName(), CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.deleteProject(dbc, deleteProject);
        } catch (Exception e) {
            dbc.report(null, "Error deleting project " + deleteProject.getName(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes a property definition.<p>
     * 
     * @param context the current request context
     * @param name the name of the property definition to delete
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(CmsRequestContext context, String name) throws CmsException {

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deletePropertydefinition() " + name,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
        if (CmsProject.isOnlineProject(context.currentProject().getId())) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deletePropertydefinition() " + name,
                CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT); 
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.deletePropertydefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, "Error deleting property definition " + name, e);
        } finally {
            dbc.clear();
        }
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
     * @param context the current request context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * @throws CmsException if something goes wrong
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(CmsRequestContext context, CmsResource resource, int siblingMode) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.deleteResource(dbc, resource, siblingMode);
        } catch (Exception e) {
            dbc.report(null, "Error deleting resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.deleteStaticExportPublishedResource(dbc, resourceName, linkType, linkParameter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Deletes a user.<p>
     *
     * @param context the current request context
     * @param userId the Id of the user to be deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteUser(CmsRequestContext context, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(context, userId);
        deleteUser(context, user);        
    }
    
    /**
     * Deletes a user.<p>
     *
     * @param context the current request context
     * @param username the name of the user to be deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = readUser(context, username, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        deleteUser(context, user);
    }

    /**
     * Deletes a web user.<p>
     *
     * @param context the current request context
     * @param userId the Id of the web user to be deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteWebUser(CmsRequestContext context, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(context, userId);
        deleteUser(context, user);
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.endTask(dbc, taskid);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.forwardTask(dbc, taskid, newRoleName, newUserName);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }   
    }

    /**
     * Reads all access control entries for a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * @return a list of access control entries defining all permissions for the given resource
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getAccessControlEntries(dbc, resource, getInherited);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsAccessControlList result = null;
        try {
            result = m_driverManager.getAccessControlList(dbc, resource, inheritedOnly);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getAllAccessibleProjects(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @param context the current request context
     * 
     * @return list with all projects from history.
     * @throws CmsException if operation was not succesful.
     */
    public List getAllBackupProjects(CmsRequestContext context) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getAllBackupProjects(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getAllManageableProjects(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Get the next version id for the published backup resources.<p>
     *
     * @param context the current request context
     * 
     * @return the new version id
     */
    public int getBackupTagId(CmsRequestContext context) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        int result = 0;
        try {
            result = m_driverManager.getBackupTagId(dbc);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return groups a list of all child groups or null
     * @throws CmsException if operation was not succesful.
     */
    public List getChild(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getChild(dbc, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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
     * @return a list of all child groups or null
     * @throws CmsException if operation was not succesful
     */
    public List getChilds(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getChilds(dbc, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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
     * @return list of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public List getDirectGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getDirectGroupsOfUser(dbc, username);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns all groups.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @return users a list of all existing groups
     * @throws CmsException if operation was not succesful
     */
    public List getGroups(CmsRequestContext context) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getGroups(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns the groups of a Cms user.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @return a list of Cms groups filtered by the specified IP address
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getGroupsOfUser(dbc, username);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result list
     * @return a list of Cms groups
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(CmsRequestContext context, String username, String remoteAddress) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getGroupsOfUser(dbc, username, remoteAddress);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsLock result = null;
        try {
            result = m_driverManager.getLock(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsLock result = null;
        try {
            result = m_driverManager.getLock(dbc, resourcename);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group
     * 
     * @return group the parent group or null
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsGroup result = null;
        try {
            result = m_driverManager.getParent(dbc, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsPermissionSetCustom result = null;
        try {
            result = m_driverManager.getPermissions(dbc, resource, user);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsPublishList result = null;
        try {
            result = m_driverManager.getPublishList(dbc, directPublishResource, publishSiblings);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getResourcesInTimeRange(dbc, folder, starttime, endtime);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Get a parameter value for a task.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * 
     * @return task parameter value
     * @throws CmsException if something goes wrong
     */
    public String getTaskPar(CmsRequestContext context, int taskId, String parName) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        String result = null;
        try {
            result = m_driverManager.getTaskPar(dbc, taskId, parName);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Get the template task id for a given taskname.<p>
     *
     * @param context the current request context
     * @param taskName name of the task
     * 
     * @return id from the task template
     * @throws CmsException if something goes wrong
     */
    public int getTaskType(CmsRequestContext context, String taskName) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        int result = 0;
        try {
            result = m_driverManager.getTaskType(dbc, taskName);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns all users.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @return a list of all existing users
     * @throws CmsException if operation was not succesful.
     */
    public List getUsers(CmsRequestContext context) throws CmsException {
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getUsers(dbc);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Returns all users from a given type.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param type the type of the users
     * @return a list of all existing users
     * @throws CmsException if operation was not succesful
     */
    public List getUsers(CmsRequestContext context, int type) throws CmsException {
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getUsers(dbc, type);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * All users are granted, except the anonymous user.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group to list users from
     * @return list of users
     * @throws CmsException if operation was not succesful
     */
    public List getUsersOfGroup(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.getUsersOfGroup(dbc, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
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
     * @see #hasPermissions(CmsDbContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public int hasPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {
        
        int result = 0;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            result = hasPermissions(dbc, resource, requiredPermissions, checkLock, filter);
        } finally {
            dbc.clear();
        }        
        return result;
    }
    
    /**
     * Checks if the given resource or the current project can be published by the current user 
     * using his current OpenCms context.<p>
     * 
     * If the resource parameter is <code>null</code>, then the current project is checked,
     * otherwise the resource is checked for direct publish permissions.<p>
     * 
     * @param context the current request context
     * @param directPublishResource the direct publish resource (optional, if null only the current project is checked)
     * 
     * @return true if the current user can direct publish the given resource in his current context
     */
    public boolean hasPublishPermissions(CmsRequestContext context, CmsResource directPublishResource) {

        // has the current user the required permissions to publish the current project/direct published resource?
        boolean hasPublishPermissions = false;

        // the current user either has to be a member of the administrators group
        hasPublishPermissions |= isAdmin(context);

        if (!hasPublishPermissions) {
            // or he has to be a member of the project managers group
            hasPublishPermissions |= isManagerOfProject(context);
        }

        if (directPublishResource != null) {
            // this is a "direct publish" attempt
            try {
                // or he has the explicit permission to direct publish a resource
                hasPublishPermissions |= (PERM_ALLOWED == hasPermissions(
                    context,
                    directPublishResource,
                    CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                    true,
                    CmsResourceFilter.ALL));

                // and the parent folder must not be new or deleted
                String parentFolder = CmsResource.getParentFolder(directPublishResource.getRootPath());
                if (parentFolder != null) {
                    CmsResource parent = readResource(context, parentFolder, CmsResourceFilter.ALL);
                    if ((parent.getState() == I_CmsConstants.C_STATE_DELETED)
                        || (parent.getState() == I_CmsConstants.C_STATE_NEW)) {
                        // parent folder is deleted or new - direct publish not allowed
                        return false;
                    }
                }

            } catch (CmsException e) {
                // any exception here means the user has no publish permissions
                return false;
            }
        }

        // and the current project must be different from the online project
        hasPublishPermissions &= (context.currentProject().getId() != I_CmsConstants.C_PROJECT_ONLINE_ID);

        // and the project flags have to be set to zero
        hasPublishPermissions &= (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED);

        // return the result
        return hasPublishPermissions;
    }

    /**
     * Writes a list of access control entries as new access control entries of a given resource.<p>
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.importAccessControlEntries(dbc, resource, acEntries);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.importFolder(cms, dbc, importFile, importPath);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Initializes this security manager with a given runtime info factory.<p>
     * 
     * @param configurationManager the configurationManager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * @throws CmsException if something goes wrong
     */
    public void init(CmsConfigurationManager configurationManager, I_CmsDbContextFactory runtimeInfoFactory)
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
            m_dbContextFactory = runtimeInfoFactory;
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
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        boolean result = false;
        try {
            result = m_driverManager.isAdmin(dbc);
        } finally {
            dbc.clear();
        }        
        return result;
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

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            result =  m_driverManager.isInsideCurrentProject(dbc, resourcename);
        } finally {
            dbc.clear();
        }
        return result;
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

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            result = m_driverManager.isManagerOfProject(dbc);
        } finally {
            dbc.clear();
        }
        return result;
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

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);        
        try {
            result = m_driverManager.isProjectManager(dbc);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
            m_driverManager.lockResource(dbc, resource, mode);
        } catch (Exception e) {
            dbc.report(null, "Error deleting resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * For security reasons, all error / exceptions that occur here are "blocked" and 
     * a simple security exception is thrown.<p>
     * 
     * @param context the current request context
     * @param username the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * @param userType the user type to log in (System user or Web user)
     * @return the logged in users name
     *
     * @throws CmsSecurityException if login was not succesful
     */
    public CmsUser loginUser(CmsRequestContext context, String username, String password, String remoteAddress, int userType)
    throws CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.loginUser(dbc, username, password, remoteAddress, userType);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param context the current request context
     * @param principalId the UUID of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise null
     */
    public I_CmsPrincipal lookupPrincipal(CmsRequestContext context, CmsUUID principalId) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        I_CmsPrincipal result = null;
        try {
            result = m_driverManager.lookupPrincipal(dbc, principalId);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Lookup and read the user or group with the given name.<p>
     * 
     * @param context the current request context
     * @param principalName the name of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise null
     */
    public I_CmsPrincipal lookupPrincipal(CmsRequestContext context, String principalName) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        I_CmsPrincipal result = null;
        try {
            result = m_driverManager.lookupPrincipal(dbc, principalName);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The method can also be used to check get the name of a resource
     * in the "lost and found" folder only without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * In general, it is the same name as the given resource has, the only exception is
     * if a resource in the "lost and found" folder with the same name already exists. 
     * In such case, a counter is added to the resource name.<p>
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        String result = null;
        try {
            result = m_driverManager.moveToLostAndFound(dbc, resourcename, returnNameOnly);
        } catch (Exception e) {
            dbc.report(null, "Error moving resource " + resourcename + " to lost+found", e);
        } finally {
            dbc.clear();
        }
        return result;
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
     * @return the publish history id of the published project
     * @throws Exception if something goes wrong
     * 
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean)
     */
    public synchronized CmsUUID publishProject(CmsObject cms, CmsPublishList publishList, I_CmsReport report)
    throws Exception {

        CmsRequestContext context = cms.getRequestContext();
        int publishProjectId = context.currentProject().getId();

        // has the current user the required permissions to publish the current project/direct published resource?
        boolean hasPublishPermissions;

        if (publishList.isDirectPublish()) {
            // pass the direct publish resource to the permission test
            CmsResource directPublishResource = publishList.getDirectPublishResource();
            hasPublishPermissions = hasPublishPermissions(context, directPublishResource);
        } else {
            // pass null, will only check the current project
            hasPublishPermissions = hasPublishPermissions(context, null);
        }

        if (hasPublishPermissions) {

            CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
            try {
                m_driverManager.publishProject(cms, dbc, publishList, report);
            } finally {
                dbc.clear();
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

        return publishList.getPublishHistoryId();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.reactivateTask(dbc, taskId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Reads the agent of a task from the OpenCms.<p>
     *
     * @param context the current request context
     * @param task the task to read the agent from
     * 
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readAgent(CmsRequestContext context, CmsTask task) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readAgent(dbc, task);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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
        
        List result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, resourcename, CmsResourceFilter.ALL);        
            result = m_driverManager.readAllBackupFileHeaders(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);      
        List result = null;
        try {
            result = m_driverManager.readAllProjectResources(dbc, projectId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads all propertydefinitions for the given mapping type.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param mappingtype the mapping type to read the propertydefinitions for
     * @return propertydefinitions a list with propertydefefinitions for the mapping type. The list is maybe empty.
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertydefinitions(CmsRequestContext context, int mappingtype) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);      
        List result = null;
        try {
            result = m_driverManager.readAllPropertydefinitions(dbc, mappingtype);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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
        
        CmsBackupResource result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context); 
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, filename, CmsResourceFilter.ALL);
            result = m_driverManager.readBackupFile(dbc, tagId, resource);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the backupinformation of a project from the Cms.<p>
     *
     * @param context the current request context
     * @param tagId the tagId of the project
     * 
     * @return the backup project
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(CmsRequestContext context, int tagId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);      
        CmsBackupProject result = null;
        try {
            result = m_driverManager.readBackupProject(dbc, tagId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        List result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);      
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            result = m_driverManager.readChildResources(dbc, resource, filter, getFolders, getFiles);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(CmsRequestContext, String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
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

        CmsFile result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context); 
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, filename, filter);
            result = m_driverManager.readFile(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }
    
    /**
     * Reads a folder from the VFS, using the specified resource filter.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readFolder(CmsRequestContext context, String resourcename, CmsResourceFilter filter)
    throws CmsException {
        
        CmsFolder result = null;
        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        try {
            result = readFolder(dbc, resourcename, filter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }
        

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param ownerName owner of the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy chooses, how to order the tasks
     * @param sort sorting of the tasks
     * 
     * @return list of tasks
     * @throws CmsException if something goes wrong
     */
    public List readGivenTasks(CmsRequestContext context, int projectId, String ownerName, int taskType, String orderBy, String sort)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readGivenTasks(dbc, projectId, ownerName, taskType, orderBy, sort);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Reads the group of a project from the OpenCms.<p>
     *
     * @param context the current request context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsProject project) {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, project);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the group (role) of a task from the OpenCms.<p>
     *
     * @param context the current request context
     * @param task the task to read from
     * 
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsTask task) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, task);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a group object.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param groupId the id of the group that is to be read
     * 
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsUUID groupId) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, groupId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a group object.<p>
     * 
     * @param context the current request context
     * @param groupname the name of the group that is to be read
     *
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the manager group of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsRequestContext context, CmsProject project) {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsGroup result = null;
        try {
            result = m_driverManager.readManagerGroup(dbc, project);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the original agent of a task from the OpenCms.<p>
     *
     * @param context the current request context
     * @param task the task to read the original agent from
     * 
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOriginalAgent(CmsRequestContext context, CmsTask task) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsUser result = null;
        try {
            result = m_driverManager.readOriginalAgent(dbc, task);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the owner of a project from the OpenCms.<p>
     *
     * @param context the current request context
     * @param project the project to get the owner from
     * 
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsRequestContext context, CmsProject project) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsUser result = null;
        try {
            result = m_driverManager.readOwner(dbc, project);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the owner (initiator) of a task from the OpenCms.<p>
     *
     * @param context the current request context
     * @param task the task to read the owner from
     * 
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTask task) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsUser result = null;
        try {
            result = m_driverManager.readOwner(dbc, task);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads the owner of a tasklog from the OpenCms.<p>
     *
     * @param context the current request context
     * @param log the tasklog
     * 
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTaskLog log) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsUser result = null;
        try {
            result = m_driverManager.readOwner(dbc, log);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param context the current request context
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsRequestContext context, int projectId, String path, CmsResourceFilter filter) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        List result = null;
        try {
            result = m_driverManager.readPath(dbc, projectId, path, filter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * @param context the current request context
     * @param task the task to read the project of
     * 
     * @return the project read from the cms
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsRequestContext context, CmsTask task) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsProject result = null;
        try {
            result = m_driverManager.readProject(dbc, task);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads a project from the Cms given the projects name.<p>
     *
     * @param id the id of the project
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(int id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();   
        CmsProject result = null;
        try {
            result = m_driverManager.readProject(dbc, id);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext();   
        CmsProject result = null;
        try {
            result = m_driverManager.readProject(dbc, name);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads log entries for a project.<p>
     *
     * @param context the current request context
     * @param projectId the id of the projec for tasklog to read
     * 
     * @return a list of new TaskLog objects
     * @throws CmsException if something goes wrong.
     */
    public List readProjectLogs(CmsRequestContext context, int projectId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readProjectLogs(dbc, projectId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param context the current request context
     * @param project the project to get the project resources for
     * 
     * @return the list of all resource names that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsRequestContext context, CmsProject project) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readProjectResources(dbc, project);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readProjectView(dbc, projectId, state);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param context the current request context
     * @param name the name of the property definition to read
     * 
     * @return the property definition that was read, or null if there is no property definition with the given name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name)
    throws CmsException {
        
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsPropertydefinition result = null;
        try {
            result = m_driverManager.readPropertydefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param key the property key name
     * @param search if <code>true</code>, the property is searched on all parent folders of the resource. 
     *      if it's not found attached directly to the resource.
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found.
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsRequestContext context, String resourceName, String key, boolean search)
    throws CmsException {

        CmsProperty result = null;
        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, resourceName, CmsResourceFilter.ALL);
            result = m_driverManager.readPropertyObject(dbc, resource, key, search);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
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
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param search <code>true</code>, if the properties should be searched on all parent folders  if not found on the resource
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(CmsRequestContext context, String resourceName, boolean search) 
    throws CmsException {

        List result = null;
        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, resourceName, CmsResourceFilter.ALL);
            result = m_driverManager.readPropertyObjects(dbc, resource, search);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        List result = null;
        try {
            result = m_driverManager.readPublishedResources(dbc, publishHistoryId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads a resource from the OpenCms VFS, using the specified resource filter.<p>
     * 
     * @param context the current request context
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
        String resourcePath,
        CmsResourceFilter filter) throws CmsException {

        CmsResource result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context); 
        try {
            result = readResource(dbc, resourcePath, filter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Checks the availability of a resource from the OpenCms VFS, 
     * using the specified resource filter.<p>
     * 
     * @param context the current request context
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return <code>true</code> if the resource is available.
     * 
     * @see CmsObject#existsResource(String, CmsResourceFilter)
     * @see CmsObject#existsResource(String)
     */
    public boolean existsResource(
        CmsRequestContext context,
        String resourcePath,
        CmsResourceFilter filter) {

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context); 
        try {
            readResource(dbc, resourcePath, filter);
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            dbc.clear();
        }        
        return result;
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

        List result = null;
        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        try {
            // check the access permissions
            checkPermissions(dbc, parent, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            result = m_driverManager.readResources(dbc, parent, filter, readTree);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads all resources that have a value set for the specified property (definition) in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param context the current request context
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * 
     * @return all resources that have a value set for the specified property (definition) in the given path
     * 
     * @throws CmsException if something goes wrong
     */   
    public List readResourcesWithProperty(CmsRequestContext context, String path, String propertyDefinition)
    throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        List result = null;
        try {
            result = m_driverManager.readResourcesWithProperty(dbc, path, propertyDefinition);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        List result = null;
        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        try {
            // read the resource first (this will check for read permissions)
            CmsResource resource = readResource(dbc, resourcename, filter);
            result = m_driverManager.readSiblings(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        String result = null;
        try {
            result = m_driverManager.readStaticExportPublishedResourceParameters(dbc, rfsName);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        List result = null;
        try {
            result = m_driverManager.readStaticExportResources(dbc, parameterResources, timestamp);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Read a task by id.<p>
     *
     * @param context the current request context
     * @param id the id for the task to read
     * 
     * @return a task
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(CmsRequestContext context, int id) throws CmsException {

        CmsDbContext dbc =  m_dbContextFactory.getDbContext(context); 
        CmsTask result = null;
        try {
            result = m_driverManager.readTask(dbc, id);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads log entries for a task.<p>
     *
     * @param context the current request context
     * @param taskid the task for the tasklog to read
     * 
     * @return a list of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    public List readTaskLogs(CmsRequestContext context, int taskid) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readTaskLogs(dbc, taskid);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Reads all tasks for a project.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined. Can be null for all tasks
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return a list of tasks
     * @throws CmsException  if something goes wrong
     */
    public List readTasksForProject(CmsRequestContext context, int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readTasksForProject(dbc, projectId, tasktype, orderBy, sort);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result; 
    }

    /**
     * Reads all tasks for a role in a project.<p>
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param roleName the user who has to process the task
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return a list of tasks
     * @throws CmsException if something goes wrong
     */
    public List readTasksForRole(CmsRequestContext context, int projectId, String roleName, int tasktype, String orderBy, String sort)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readTasksForRole(dbc, projectId, roleName, tasktype, orderBy, sort);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Reads all tasks for a user in a project.<p>
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param userName the user who has to process the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return a list of tasks
     * @throws CmsException if something goes wrong
     */
    public List readTasksForUser(CmsRequestContext context, int projectId, String userName, int taskType, String orderBy, String sort)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        List result = null;
        try {
            result = m_driverManager.readTasksForUser(dbc, projectId, userName, taskType, orderBy, sort);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * All users are granted.<p>
     * 
     * @param context the current request context
     * @param id the id of the user to read
     *
     * @return the user read 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsRequestContext context, CmsUUID id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, id);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.<p>
     * 
     * @param context the current request context
     * @param username the name of the user that is to be read
     * @param type the type of the user
     *
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsRequestContext context, String username, int type) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, username, type);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsRequestContext context, String username, String password) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, username, password);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext();   
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, username);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Read a web user from the database.<p>
     * 
     * @param context the current request context
     * @param username the web user to read
     * 
     * @return the read web user
     * @throws CmsException if the user could not be read 
     */
    public CmsUser readWebUser(CmsRequestContext context, String username) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readWebUser(dbc, username);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.<p>
     *
     * @param context the current request context
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsRequestContext context, String username, String password) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        CmsUser result = null;
        try {
            result = m_driverManager.readWebUser(dbc, username, password);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.removeAccessControlEntry(dbc, resource, principal);
        } catch (Exception e) {
            dbc.report(null, "Error removing ACE on resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] removeUserFromGroup()",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.removeUserFromGroup(dbc, username, groupname);
        } catch (Exception e) {
            dbc.report(null, "Error removing user " + username + " from group " + groupname, e);
        } finally {
            dbc.clear();
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
     
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.replaceResource(dbc, resource, type, content, properties);
        } catch (Exception e) {
            dbc.report(null, "Error replacing resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param context the current request context
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     * @throws CmsSecurityException if the specified username and old password could not be verified
     */
    public void resetPassword(CmsRequestContext context, String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.resetPassword(dbc, username, oldPassword, newPassword);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.restoreResource(dbc, resource, tag);
        } catch (Exception e) {
            dbc.report(null, "Error restoring resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.setName(dbc, taskId, name);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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
    public void setParentGroup(CmsRequestContext context, String groupName, String parentGroupName) throws CmsException {

        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] setParentGroup() " + groupName,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.setParentGroup(dbc, groupName, parentGroupName);
        } catch (Exception e) {
            dbc.report(null, "Error setting parent group to " + parentGroupName + " of group " + groupName, e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.setPassword(dbc, username, newPassword);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.setPriority(dbc, taskId, priority);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Set a Parameter for a task.<p>
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @param parValue value if the parameter
     * 
     * @throws CmsException if something goes wrong
     */
    public void setTaskPar(CmsRequestContext context, int taskId, String parName, String parValue) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.setTaskPar(dbc, taskId, parName, parValue);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }           
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.setTimeout(dbc, taskId, timeout);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
    }

    /**
     * Changes the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expire" date of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateLastModified timestamp the new timestamp of the changed resource.
     * @param dateReleased the new release date of the changed resource. 
     *              Set it to <code>{@link I_CmsConstants#C_DATE_UNCHANGED}<code> to keep it unchanged.
     * @param dateExpired the new expire date of the changed resource. 
     *              Set it to <code>{@link I_CmsConstants#C_DATE_UNCHANGED}<code> to keep it unchanged.
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.touch(dbc, resource, dateLastModified, dateReleased, dateExpired);
        } catch (Exception e) {
            dbc.report(null, "Error touching resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.undoChanges(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, "Error undoing changes of resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.unlockProject(dbc, projectId);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.unlockResource(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, "Error undoing changes of resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        boolean result = false;
        try {
            result = m_driverManager.userInGroup(dbc, username, groupname);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
        return result;
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.writeAccessControlEntry(dbc, resource, ace);
        } catch (Exception e) {
            dbc.report(null, "Error writing ACE on resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsFile result = null;
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            result = m_driverManager.writeFile(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, "Error writing file " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
        }
        return result;
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

        if (! isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeGroup() " + group.getName(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.writeGroup(dbc, group);
        } catch (Exception e) {
            dbc.report(null, "Error writing group " + group.getName(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.writePropertyObject(dbc, resource, property);
        } catch (Exception e) {
            dbc.report(
                null,
                "Error writing property " + property.getKey() + " on resource " + resource.getRootPath(),
                e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);

            // write the properties
            m_driverManager.writePropertyObjects(dbc, resource, properties);

            // update the resource state
            resource.setUserLastModified(context.currentUser().getId());
            m_driverManager.getVfsDriver().writeResource(
                dbc,
                context.currentProject(),
                resource,
                CmsDriverManager.C_UPDATE_RESOURCE_STATE);

        } catch (Exception e) {
            dbc.report(null, "Error writing properties on resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.writeResource(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, "Error writing resource " + resource.getRootPath(), e);
        } finally {
            dbc.clear();
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.writeStaticExportPublishedResource(dbc, resourceName, linkType, linkParameter, timestamp);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }        
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.writeTaskLog(dbc, taskid, comment);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }            
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

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);   
        try {
            m_driverManager.writeTaskLog(dbc, taskId, comment, type);
        } catch (Exception e) {
            dbc.report(null, null, e);
        } finally {
            dbc.clear();
        }
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

        if (!isAdmin(context) && (!context.currentUser().equals(user))) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.writeUser(dbc, user);
        } catch (Exception e) {
            dbc.report(null, "Error writing user " + user.getName(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Updates the user information of a web user.<p>
     *
     * Only users of the user type webuser can be updated this way.<p>
     * 
     * @param context the current request context
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsRequestContext context, CmsUser user) throws CmsException {

        if (!user.isWebUser()) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] writeWebUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.writeWebUser(dbc, user);
        } catch (Exception e) {
            dbc.report(null, "Error writing web user " + user.getName(), e);
        } finally {
            dbc.clear();
        }
    }
    
    /**
     * Performs a blocking permission check on a resource.<p>
     *
     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an exception is thrown.<p>
     * 
     * @param dbc the current database context
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
    protected void checkPermissions(
        CmsDbContext dbc,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException, CmsVfsResourceNotFoundException {

        // get the permissions
        int permissions = hasPermissions(dbc, resource, requiredPermissions, checkLock, filter);
        if (permissions != 0) {
            checkPermissions(dbc.getRequestContext(), resource, requiredPermissions, permissions);
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
        m_dbContextFactory = null;

        super.finalize();

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
     * @param context the current database context
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
     */    
    protected int hasPermissions(
        CmsDbContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {
            
        // check if the resource is valid according to the current filter
        // if not, throw a CmsResourceNotFoundException
        if (!filter.isValid(context.getRequestContext(), resource)) {
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
        boolean isAdmin = m_driverManager.isAdmin(context);

        // if the resource type is jsp
        // write is only allowed for administrators
        if (!isAdmin && (resource.getTypeId() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }

        // check lock status 
        if (requiredPermissions.requiresWritePermission()
        || requiredPermissions.requiresControlPermission()) {
            // check lock state only if required
            CmsLock lock = m_driverManager.getLock(context, resource);
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
            permissions = m_driverManager.getPermissions(context, resource, context.currentUser());
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
     * Reads a folder from the VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsException if something goes wrong
     */    
    protected CmsFolder readFolder(CmsDbContext dbc, String resourcename, CmsResourceFilter filter)
    throws CmsException {
              
        CmsResource resource = readResource(dbc, resourcename, filter);       
        return m_driverManager.convertResourceToFolder(resource);
    }
    
    /**
     * Reads a resource from the OpenCms VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
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
    protected CmsResource readResource(
        CmsDbContext dbc,
        String resourcePath,
        CmsResourceFilter filter) throws CmsException {

        // read the resource from the VFS
        CmsResource resource = m_driverManager.readResource(dbc, resourcePath, filter);

        // check if the user has read access to the resource
        checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, filter);

        // access was granted - return the resource
        return resource;
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

    /**
     * Deletes a user.<p>
     *
     * @param context the current request context
     * @param user the user to be deleted
     * 
     * @throws CmsException if something goes wrong
     */    
    private void deleteUser(CmsRequestContext context, CmsUser user) throws CmsException {
        
        if (!isAdmin(context)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        if (OpenCms.getDefaultUsers().isDefaultUser(user.getName())) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] deleteUser() " + user.getName(),
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.deleteUser(dbc, context.currentProject(), user.getId());
        } catch (Exception e) {
            dbc.report(null, "Error deleting user " + user.getName(), e);
        } finally {
            dbc.clear();
        }
    }
}