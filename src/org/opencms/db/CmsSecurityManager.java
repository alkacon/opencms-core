/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockManager;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishEngine;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsDefaultPermissionHandler;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPermissionHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * The OpenCms security manager.<p>
 * 
 * The security manager checks the permissions required for a user action invoke by the Cms object. If permissions 
 * are granted, the security manager invokes a method on the OpenCms driver manager to access the database.<p>
 * 
 * @since 6.0.0
 */
public final class CmsSecurityManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecurityManager.class);

    /** The factory to create runtime info objects. */
    protected I_CmsDbContextFactory m_dbContextFactory;

    /** The initialized OpenCms driver manager to access the database. */
    protected CmsDriverManager m_driverManager;

    /** The lock manager. */
    private CmsLockManager m_lockManager;

    /** Permission handler implementation. */
    private I_CmsPermissionHandler m_permissionHandler;

    /**
     * Default constructor.<p>
     */
    private CmsSecurityManager() {

        // intentionally left blank
    }

    /**
     * Creates a new instance of the OpenCms security manager.<p>
     * 
     * @param configurationManager the configuration manager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * @param publishEngine the publish engine
     * 
     * @return a new instance of the OpenCms security manager
     * 
     * @throws CmsInitException if the security manager could not be initialized
     */
    public static CmsSecurityManager newInstance(
        CmsConfigurationManager configurationManager,
        I_CmsDbContextFactory runtimeInfoFactory,
        CmsPublishEngine publishEngine) throws CmsInitException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            // OpenCms is already initialized
            throw new CmsInitException(org.opencms.main.Messages.get().container(
                org.opencms.main.Messages.ERR_ALREADY_INITIALIZED_0));
        }

        CmsSecurityManager securityManager = new CmsSecurityManager();
        securityManager.init(configurationManager, runtimeInfoFactory, publishEngine);

        return securityManager;
    }

    /**
     * Adds a new relation to a given resource.<p>
     * 
     * @param context the request context
     * @param resource the resource to add the relation to
     * @param target the target of the relation
     * @param type the type of the relation
     * @param importCase if importing relations
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #deleteRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     * @see CmsObject#addRelationToResource(String, String, String)
     */
    public void addRelationToResource(
        CmsRequestContext context,
        CmsResource resource,
        CmsResource target,
        CmsRelationType type,
        boolean importCase) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.addRelationToResource(dbc, resource, target, type, importCase);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_ADD_RELATION_TO_RESOURCE_3,
                    context.getSitePath(resource),
                    context.getSitePath(target),
                    type),
                e);

        } finally {
            dbc.clear();
        }
    }

    /**
     * Adds a resource to the given organizational unit.<p>
     * 
     * @param context the current request context
     * @param orgUnit the organizational unit to add the resource to
     * @param resource the resource that is to be added to the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     * @see org.opencms.security.CmsOrgUnitManager#removeResourceFromOrgUnit(CmsObject, String, String)
     */
    public void addResourceToOrgUnit(CmsRequestContext context, CmsOrganizationalUnit orgUnit, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(orgUnit.getName()));
            m_driverManager.addResourceToOrgUnit(dbc, orgUnit, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_ADD_RESOURCE_TO_ORGUNIT_2,
                    orgUnit.getName(),
                    dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Adds a user to a group.<p>
     *
     * @param context the current request context
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * @param readRoles if reading roles or groups
     *
     * @throws CmsException if operation was not successful
     */
    public void addUserToGroup(CmsRequestContext context, String username, String groupname, boolean readRoles)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsRole role = CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(username));
            checkRoleForUserModification(dbc, username, role);
            m_driverManager.addUserToGroup(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                CmsOrganizationalUnit.removeLeadingSeparator(groupname),
                readRoles);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_ADD_USER_GROUP_FAILED_2, username, groupname), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Changes the lock of a resource to the current user, that is "steals" the lock from another user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the lock for
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.types.I_CmsResourceType#changeLock(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        checkOfflineProject(dbc);
        try {
            m_driverManager.changeLock(dbc, resource, CmsLockType.EXCLUSIVE);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_CHANGE_LOCK_OF_RESOURCE_2,
                    context.getSitePath(resource),
                    " - " + e.getMessage()),
                e);
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
     * @param propertyDefinition the name of the property definition to change the value
     * @param oldValue the old value of the property definition
     * @param newValue the new value of the property definition
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     * 
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed
     *
     * @throws CmsVfsException for now only when the search for the old value fails 
     * @throws CmsException if operation was not successful
     */
    public synchronized List<CmsResource> changeResourcesInFolderWithProperty(
        CmsRequestContext context,
        CmsResource resource,
        String propertyDefinition,
        String oldValue,
        String newValue,
        boolean recursive) throws CmsException, CmsVfsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            result = m_driverManager.changeResourcesInFolderWithProperty(
                dbc,
                resource,
                propertyDefinition,
                oldValue,
                newValue,
                recursive);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_CHANGE_RESOURCES_IN_FOLDER_WITH_PROP_4,
                    new Object[] {propertyDefinition, oldValue, newValue, context.getSitePath(resource)}),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Checks if the current user has management access to the given project.<p>
     * 
     * @param dbc the current database context
     * @param project the project to check
     *
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     */
    public void checkManagerOfProjectRole(CmsDbContext dbc, CmsProject project) throws CmsRoleViolationException {

        boolean hasRole = false;
        try {
            if (hasRole(dbc, dbc.currentUser(), CmsRole.ROOT_ADMIN)) {
                return;
            }
            hasRole = m_driverManager.getAllManageableProjects(
                dbc,
                m_driverManager.readOrganizationalUnit(dbc, project.getOuFqn()),
                false).contains(project);
        } catch (CmsException e) {
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (!hasRole) {
            throw new CmsRoleViolationException(org.opencms.security.Messages.get().container(
                org.opencms.security.Messages.ERR_NOT_MANAGER_OF_PROJECT_2,
                dbc.currentUser().getName(),
                dbc.currentProject().getName()));
        }
    }

    /**
     * Checks if the project in the given database context is not the "Online" project,
     * and throws an Exception if this is the case.<p>
     *  
     * This is used to ensure a user is in an "Offline" project
     * before write access to VFS resources is granted.<p>
     * 
     * @param dbc the current OpenCms users database context
     * 
     * @throws CmsVfsException if the project in the given database context is the "Online" project
     */
    public void checkOfflineProject(CmsDbContext dbc) throws CmsVfsException {

        if (dbc.currentProject().isOnlineProject()) {
            throw new CmsVfsException(org.opencms.file.Messages.get().container(
                org.opencms.file.Messages.ERR_NOT_ALLOWED_IN_ONLINE_PROJECT_0));
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
     * 
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, I_CmsPermissionHandler.CmsPermissionCheckResult)
     */
    public void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, requiredPermissions, checkLock, filter);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Checks if the current user has the permissions to publish the given publish list 
     * (which contains the information about the resources / project to publish).<p>
     * 
     * @param dbc the current OpenCms users database context
     * @param publishList the publish list to check (contains the information about the resources / project to publish)
     * 
     * @throws CmsException if the user does not have the required permissions because of project lock state
     * @throws CmsMultiException if issues occur like a direct publish is attempted on a resource 
     *         whose parent folder is new or deleted in the offline project, 
     *         or if the current user has no management access to the current project
     */
    public void checkPublishPermissions(CmsDbContext dbc, CmsPublishList publishList)
    throws CmsException, CmsMultiException {

        // is the current project an "offline" project?
        checkOfflineProject(dbc);

        // check if this is a "direct publish" attempt        
        if (!publishList.isDirectPublish()) {
            // check if the user is a manager of the current project, in this case he has publish permissions
            checkManagerOfProjectRole(dbc, dbc.getRequestContext().getCurrentProject());
        } else {
            // direct publish, create exception containers
            CmsMultiException resourceIssues = new CmsMultiException();
            CmsMultiException permissionIssues = new CmsMultiException();
            // iterate all resources in the direct publish list
            Iterator<CmsResource> it = publishList.getDirectPublishResources().iterator();
            List<String> parentFolders = new ArrayList<String>();
            while (it.hasNext()) {
                CmsResource res = it.next();
                // the parent folder must not be new or deleted
                String parentFolder = CmsResource.getParentFolder(res.getRootPath());
                if ((parentFolder != null) && !parentFolders.contains(parentFolder)) {
                    // check each parent folder only once
                    CmsResource parent = readResource(dbc, parentFolder, CmsResourceFilter.ALL);
                    if (parent.getState().isDeleted()) {
                        if (!(publishList.isUserPublishList() && publishList.getDeletedFolderList().contains(parent))) {
                            // parent folder is deleted - direct publish not allowed
                            resourceIssues.addException(new CmsVfsException(Messages.get().container(
                                Messages.ERR_DIRECT_PUBLISH_PARENT_DELETED_2,
                                dbc.getRequestContext().removeSiteRoot(res.getRootPath()),
                                parentFolder)));
                        }
                    }
                    if (parent.getState().isNew()) {
                        if (!(publishList.isUserPublishList() && publishList.getFolderList().contains(parent))) {
                            // parent folder is new - direct publish not allowed
                            resourceIssues.addException(new CmsVfsException(Messages.get().container(
                                Messages.ERR_DIRECT_PUBLISH_PARENT_NEW_2,
                                dbc.removeSiteRoot(res.getRootPath()),
                                parentFolder)));
                        }
                    }
                    // add checked parent folder to prevent duplicate checks
                    parentFolders.add(parentFolder);
                }
                // check if the user has the explicit permission to direct publish the selected resource
                if (I_CmsPermissionHandler.PERM_ALLOWED != hasPermissions(
                    dbc.getRequestContext(),
                    res,
                    CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                    true,
                    CmsResourceFilter.ALL)) {

                    // the user has no "direct publish" permissions on the resource
                    permissionIssues.addException(new CmsSecurityException(Messages.get().container(
                        Messages.ERR_DIRECT_PUBLISH_NO_PERMISSIONS_1,
                        dbc.removeSiteRoot(res.getRootPath()))));
                }
            }
            if (resourceIssues.hasExceptions() || permissionIssues.hasExceptions()) {
                // there are issues, permission check has failed
                resourceIssues.addExceptions(permissionIssues.getExceptions());
                throw resourceIssues;
            }
        }
        // no issues have been found , permissions are granted
    }

    /**
     * Checks if the user of the current database context has permissions to impersonate the given role
     * in the given organizational unit.<p>
     *  
     * If the organizational unit is <code>null</code>, this method will check if the
     * given user has the given role for at least one organizational unit.<p>
     *  
     * @param dbc the current OpenCms users database context
     * @param role the role to check
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * 
     * @see org.opencms.security.CmsRoleManager#checkRole(CmsObject, CmsRole)
     */
    public void checkRole(CmsDbContext dbc, CmsRole role) throws CmsRoleViolationException {

        if (!hasRole(dbc, dbc.currentUser(), role)) {
            if (role.getOuFqn() != null) {
                throw role.createRoleViolationExceptionForOrgUnit(dbc.getRequestContext(), role.getOuFqn());
            } else {
                throw role.createRoleViolationException(dbc.getRequestContext());
            }
        }
    }

    /**
     * Checks if the user of the current context has permissions to impersonate the given role.<p>
     *  
     * If the organizational unit is <code>null</code>, this method will check if the
     * given user has the given role for at least one organizational unit.<p>
     *  
     * @param context the current request context
     * @param role the role to check
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     */
    public void checkRole(CmsRequestContext context, CmsRole role) throws CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, role);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Checks if the user of the current database context has permissions to impersonate the given role 
     * for the given resource.<p>
     *  
     * @param dbc the current OpenCms users database context
     * @param role the role to check
     * @param resource the resource to check the role for
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * 
     * @see org.opencms.security.CmsRoleManager#checkRole(CmsObject, CmsRole)
     */
    public void checkRoleForResource(CmsDbContext dbc, CmsRole role, CmsResource resource)
    throws CmsRoleViolationException {

        if (!hasRoleForResource(dbc, dbc.currentUser(), role, resource)) {
            throw role.createRoleViolationExceptionForResource(dbc.getRequestContext(), resource);
        }
    }

    /**
     * Checks if the user of the current context has permissions to impersonate the given role
     * for the given resource.<p>
     *  
     * @param context the current request context
     * @param role the role to check
     * @param resource the resource to check the role for
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     */
    public void checkRoleForResource(CmsRequestContext context, CmsRole role, CmsResource resource)
    throws CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRoleForResource(dbc, role, resource);
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
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (({@link CmsPermissionSet#ACCESS_WRITE} required)
     * 
     * @see org.opencms.file.types.I_CmsResourceType#chflags(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsRequestContext context, CmsResource resource, int flags)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.chflags(dbc, resource, flags);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_CHANGE_RESOURCE_FLAGS_1, context.getSitePath(resource)),
                e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (({@link CmsPermissionSet#ACCESS_WRITE} required))
     * 
     * @see org.opencms.file.types.I_CmsResourceType#chtype(CmsObject, CmsSecurityManager, CmsResource, int)
     * @see CmsObject#chtype(String, int)
     */
    public void chtype(CmsRequestContext context, CmsResource resource, int type)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            if (CmsResourceTypeJsp.isJspTypeId(type)) {
                // security check preventing the creation of a jsp file without permissions
                checkRoleForResource(dbc, CmsRole.DEVELOPER, resource);
            }
            m_driverManager.chtype(dbc, resource, type);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_CHANGE_RESOURCE_TYPE_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Copies the access control entries of a given resource to a destination resource.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param context the current request context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_CONTROL} required)
     */
    public void copyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource destination)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, source, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            checkPermissions(dbc, destination, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.copyAccessControlEntries(dbc, source, destination, true);
        } catch (Exception e) {
            CmsRequestContext rc = context;
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_COPY_ACE_2,
                    rc.removeSiteRoot(source.getRootPath()),
                    rc.removeSiteRoot(destination.getRootPath())),
                e);
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
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if resource could not be copied 
     * 
     * @see CmsObject#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(CmsObject, CmsSecurityManager, CmsResource, String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(
        CmsRequestContext context,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, source, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            if (source.isFolder() && destination.startsWith(source.getRootPath())) {
                throw new CmsVfsException(Messages.get().container(
                    Messages.ERR_RECURSIVE_INCLUSION_2,
                    dbc.removeSiteRoot(source.getRootPath()),
                    dbc.removeSiteRoot(destination)));
            }
            // target permissions will be checked later
            m_driverManager.copyResource(dbc, source, destination, siblingMode);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_COPY_RESOURCE_2,
                    dbc.removeSiteRoot(source.getRootPath()),
                    dbc.removeSiteRoot(destination)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not have management access to the project
     * 
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsRequestContext context, CmsResource resource)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkManagerOfProjectRole(dbc, context.getCurrentProject());

            m_driverManager.copyResourceToProject(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_COPY_RESOURCE_TO_PROJECT_2,
                    context.getSitePath(resource),
                    context.getCurrentProject().getName()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Counts the locked resources in this project.<p>
     *
     * @param context the current request context
     * @param id the id of the project
     * 
     * @return the amount of locked resources in this project
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not have management access to the project
     */
    public int countLockedResources(CmsRequestContext context, CmsUUID id)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsProject project = null;
        int result = 0;
        try {
            project = m_driverManager.readProject(dbc, id);
            checkManagerOfProjectRole(dbc, project);
            result = m_driverManager.countLockedResources(project);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_COUNT_LOCKED_RESOURCES_PROJECT_2,
                    (project == null) ? "<failed to read>" : project.getName(),
                    id),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Counts the total number of users which match the given search criteria.<p>
     * 
     * @param requestContext the request context 
     * @param searchParams the search criteria object
     *  
     * @return the number of users which match the search criteria 
     * @throws CmsException if something goes wrong 
     */
    public long countUsers(CmsRequestContext requestContext, CmsUserSearchParameters searchParams) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(requestContext);
        try {
            return m_driverManager.countUsers(dbc, searchParams);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_COUNT_USERS_0), e);
            return -1;
        } finally {
            dbc.clear();
        }
    }

    /**
     * Creates a new user group.<p>
     *
     * @param context the current request context
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or <code>null</code>)
     * 
     * @return a <code>{@link CmsGroup}</code> object representing the newly created group
     * 
     * @throws CmsException if operation was not successful.
     * @throws CmsRoleViolationException if the  role {@link CmsRole#ACCOUNT_MANAGER} is not owned by the current user
     */
    public CmsGroup createGroup(CmsRequestContext context, String name, String description, int flags, String parent)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        CmsGroup result = null;
        try {
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(name)));
            result = m_driverManager.createGroup(dbc, new CmsUUID(), name, description, flags, parent);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_GROUP_1, name), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Creates a new organizational unit.<p>
     * 
     * @param context the current request context
     * @param ouFqn the fully qualified name of the new organizational unit
     * @param description the description of the new organizational unit
     * @param flags the flags for the new organizational unit
     * @param resource the first associated resource
     *
     * @return a <code>{@link CmsOrganizationalUnit}</code> object representing 
     *          the newly created organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#createOrganizationalUnit(CmsObject, String, String, int, String)
     */
    public CmsOrganizationalUnit createOrganizationalUnit(
        CmsRequestContext context,
        String ouFqn,
        String description,
        int flags,
        CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsOrganizationalUnit result = null;
        try {
            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(getParentOrganizationalUnit(ouFqn)));
            checkOfflineProject(dbc);
            result = m_driverManager.createOrganizationalUnit(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(ouFqn),
                description,
                flags,
                resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_ORGUNIT_1, ouFqn), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Creates a project.<p>
     *
     * @param context the current request context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype the type of the project
     * 
     * @return the created project
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not own the role {@link CmsRole#PROJECT_MANAGER}
     */
    public CmsProject createProject(
        CmsRequestContext context,
        String name,
        String description,
        String groupname,
        String managergroupname,
        CmsProject.CmsProjectType projecttype) throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsProject result = null;
        try {
            checkRole(dbc, CmsRole.PROJECT_MANAGER.forOrgUnit(getParentOrganizationalUnit(name)));
            result = m_driverManager.createProject(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(name),
                description,
                CmsOrganizationalUnit.removeLeadingSeparator(groupname),
                CmsOrganizationalUnit.removeLeadingSeparator(managergroupname),
                projecttype);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_PROJECT_1, name), e);
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
     * @throws CmsSecurityException if the current project is online.
     * @throws CmsRoleViolationException if the current user does not own the role {@link CmsRole#WORKPLACE_MANAGER}
     */
    public CmsPropertyDefinition createPropertyDefinition(CmsRequestContext context, String name)
    throws CmsException, CmsSecurityException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsPropertyDefinition result = null;

        try {
            checkOfflineProject(dbc);
            checkRole(dbc, CmsRole.WORKPLACE_MANAGER.forOrgUnit(null));
            result = m_driverManager.createPropertyDefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_PROPDEF_1, name), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Creates a new resource with the provided content and properties.<p>
     * An exception is thrown if a resource with the given name already exists.<p> 
     * 
     * @param context the current request context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
    * 
     * @return the created resource
     * 
     * @throws CmsVfsResourceAlreadyExistsException if a resource with the given name already exists
     * @throws CmsVfsException if the project in the given database context is the "Online" project
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(
        CmsRequestContext context,
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties) throws CmsVfsResourceAlreadyExistsException, CmsVfsException, CmsException {

        if (existsResource(context, resourcePath, CmsResourceFilter.IGNORE_EXPIRATION)) {
            // check if the resource already exists by name
            throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                resource.getRootPath()));
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource newResource = null;
        try {
            checkOfflineProject(dbc);
            newResource = m_driverManager.createResource(dbc, resourcePath, resource, content, properties, false);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_IMPORT_RESOURCE_2, context.getSitePath(resource), resourcePath),
                e);
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
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.types.I_CmsResourceType#createResource(CmsObject, CmsSecurityManager, String, byte[], List)
     */
    public synchronized CmsResource createResource(
        CmsRequestContext context,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        if (existsResource(context, resourcename, CmsResourceFilter.IGNORE_EXPIRATION)) {
            // check if the resource already exists by name
            throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                resourcename));
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource newResource = null;
        try {
            checkOfflineProject(dbc);
            newResource = m_driverManager.createResource(dbc, resourcename, type, content, properties);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_RESOURCE_1, resourcename), e);
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
     * 
     * @return the new created sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(CmsObject, CmsSecurityManager, CmsResource, String, List)
     */
    public CmsResource createSibling(
        CmsRequestContext context,
        CmsResource source,
        String destination,
        List<CmsProperty> properties) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        CmsResource sibling = null;
        try {
            checkOfflineProject(dbc);
            sibling = m_driverManager.createSibling(dbc, source, destination, properties);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_CREATE_SIBLING_1, context.removeSiteRoot(source.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
        return sibling;
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @param context the current request context
     * 
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsRequestContext context) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        CmsProject result = null;
        try {
            checkRole(dbc, CmsRole.PROJECT_MANAGER.forOrgUnit(null));
            result = m_driverManager.createTempfileProject(dbc);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_TEMPFILE_PROJECT_0), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Creates a new user.<p>
     *
     * @param context the current request context
     * @param name the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * @param additionalInfos the additional infos for the user
     *
     * @return the created user
     * 
     * @see CmsObject#createUser(String, String, String, Map)
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     */
    public CmsUser createUser(
        CmsRequestContext context,
        String name,
        String password,
        String description,
        Map<String, Object> additionalInfos) throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        CmsUser result = null;
        try {
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(name)));
            result = m_driverManager.createUser(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(name),
                password,
                description,
                additionalInfos);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_CREATE_USER_1, name), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param context the current request context
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsRequestContext context, int linkType) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.deleteAllStaticExportPublishedResources(dbc, linkType);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_STATEXP_PUBLISHED_RESOURCES_0), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes a group, where all permissions, users and children of the group
     * are transfered to a replacement group.<p>
     * 
     * @param context the current request context
     * @param groupId the id of the group to be deleted
     * @param replacementId the id of the group to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not successful
     * @throws CmsSecurityException if the group is a default group.
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     */
    public void deleteGroup(CmsRequestContext context, CmsUUID groupId, CmsUUID replacementId)
    throws CmsException, CmsRoleViolationException, CmsSecurityException {

        CmsGroup group = readGroup(context, groupId);
        if (group.isRole()) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_DELETE_ROLE_GROUP_1, group.getName()));
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // catch own exception as special cause for general "Error deleting group". 
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(group.getName())));
            // this is needed because 
            // I_CmsUserDriver#removeAccessControlEntriesForPrincipal(CmsDbContext, CmsProject, CmsProject, CmsUUID)
            // expects an offline project, if not, data will become inconsistent
            checkOfflineProject(dbc);
            m_driverManager.deleteGroup(dbc, group, replacementId);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_GROUP_1, group.getName()), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Delete a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p> 
     * 
     * @param context the current request context
     * @param name the name of the group that is to be deleted
     *
     * @throws CmsException if operation was not successful
     * @throws CmsSecurityException if the group is a default group.
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     */
    public void deleteGroup(CmsRequestContext context, String name)
    throws CmsException, CmsRoleViolationException, CmsSecurityException {

        CmsGroup group = readGroup(context, name);
        if (group.isRole()) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_DELETE_ROLE_GROUP_1, name));
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // catch own exception as special cause for general "Error deleting group". 
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(name)));
            // this is needed because 
            // I_CmsUserDriver#removeAccessControlEntriesForPrincipal(CmsDbContext, CmsProject, CmsProject, CmsUUID)
            // expects an offline project, if not data will become inconsistent
            checkOfflineProject(dbc);
            m_driverManager.deleteGroup(dbc, group, null);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_GROUP_1, name), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes the versions from the history tables, keeping the given number of versions per resource.<p>
     * 
     * @param context the current request context
     * @param versionsToKeep number of versions to keep, is ignored if negative 
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not successful
     * @throws CmsRoleViolationException if the current user does not own the role {@link CmsRole#WORKPLACE_MANAGER}
     */
    public void deleteHistoricalVersions(
        CmsRequestContext context,
        int versionsToKeep,
        int versionsDeleted,
        long timeDeleted,
        I_CmsReport report) throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsFolder root = readFolder(dbc, "/", CmsResourceFilter.ALL);
            checkRole(dbc, CmsRole.WORKPLACE_MANAGER.forOrgUnit(null));
            checkPermissions(dbc, root, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
            m_driverManager.deleteHistoricalVersions(dbc, versionsToKeep, versionsDeleted, timeDeleted, report);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_DELETE_HISTORY_4,
                    new Object[] {"/", new Integer(versionsToKeep), new Integer(versionsDeleted), new Date(timeDeleted)}),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes all log entries matching the given filter.<p>
     * 
     * @param context the current user context
     * @param filter the filter to use for deletion
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getLogEntries(CmsRequestContext, CmsLogFilter)
     * @see CmsObject#deleteLogEntries(CmsLogFilter)
     */
    public void deleteLogEntries(CmsRequestContext context, CmsLogFilter filter) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, CmsRole.WORKPLACE_MANAGER);
            m_driverManager.deleteLogEntries(dbc, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_LOG_0), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes an organizational unit.<p>
     *
     * Only organizational units that contain no sub organizational unit can be deleted.<p>
     * 
     * The organizational unit can not be delete if it is used in the request context, 
     * or if the current user belongs to it.<p>
     * 
     * All users and groups in the given organizational unit will be deleted.<p>
     * 
     * @param context the current request context
     * @param organizationalUnit the organizational unit to delete
     * 
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#deleteOrganizationalUnit(CmsObject, String)
     */
    public void deleteOrganizationalUnit(CmsRequestContext context, CmsOrganizationalUnit organizationalUnit)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check for root ou
            if (organizationalUnit.getParentFqn() == null) {
                throw new CmsDataAccessException(org.opencms.security.Messages.get().container(
                    org.opencms.security.Messages.ERR_ORGUNIT_ROOT_EDITION_0));
            }

            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(getParentOrganizationalUnit(organizationalUnit.getName())));
            checkOfflineProject(dbc);
            m_driverManager.deleteOrganizationalUnit(dbc, organizationalUnit);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_ORGUNIT_1, organizationalUnit.getName()), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes a project.<p>
     *
     * All modified resources currently inside this project will be reset to their online state.<p>
     * 
     * @param context the current request context
     * @param projectId the ID of the project to be deleted
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not own management access to the project
     */
    public void deleteProject(CmsRequestContext context, CmsUUID projectId)
    throws CmsException, CmsRoleViolationException {

        if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            // online project must not be deleted
            throw new CmsVfsException(org.opencms.file.Messages.get().container(
                org.opencms.file.Messages.ERR_NOT_ALLOWED_IN_ONLINE_PROJECT_0));
        }

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsProject deleteProject = null;
        try {
            // read the project that should be deleted
            deleteProject = m_driverManager.readProject(dbc, projectId);
            checkManagerOfProjectRole(dbc, deleteProject);
            m_driverManager.deleteProject(dbc, deleteProject);
        } catch (Exception e) {
            String projectName = (deleteProject == null ? String.valueOf(projectId) : deleteProject.getName());
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_PROJECT_1, projectName), e);
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
     * @throws CmsSecurityException if the project to delete is the "Online" project
     * @throws CmsRoleViolationException if the current user does not own the role {@link CmsRole#WORKPLACE_MANAGER}
     */
    public void deletePropertyDefinition(CmsRequestContext context, String name)
    throws CmsException, CmsSecurityException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkRole(dbc, CmsRole.WORKPLACE_MANAGER.forOrgUnit(null));
            m_driverManager.deletePropertyDefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_PROPERTY_1, name), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes all relations for the given resource matching the given filter.<p>
     * 
     * @param context the current user context
     * @param resource the resource to delete the relations for
     * @param filter the filter to use for deletion
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #addRelationToResource(CmsRequestContext, CmsResource, CmsResource, CmsRelationType, boolean)
     * @see CmsObject#deleteRelationsFromResource(String, CmsRelationFilter)
     */
    public void deleteRelationsForResource(CmsRequestContext context, CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.deleteRelationsForResource(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_DELETE_RELATIONS_1, dbc.removeSiteRoot(resource.getRootPath())),
                e);
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
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user does not have {@link CmsPermissionSet#ACCESS_WRITE} on the given resource
     *  
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(
        CmsRequestContext context,
        CmsResource resource,
        CmsResource.CmsResourceDeleteMode siblingMode) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            checkSystemLocks(dbc, resource);

            // check write permissions for subresources in case of deleting a folder
            if (resource.isFolder()) {
                dbc.getRequestContext().setAttribute(I_CmsVfsDriver.REQ_ATTR_CHECK_PERMISSIONS, Boolean.TRUE);
                try {
                    m_driverManager.getVfsDriver(dbc).removeFolder(dbc, dbc.currentProject(), resource);
                } catch (CmsDataAccessException e) {
                    // unwrap the permission violation exception
                    if (e.getCause() instanceof CmsPermissionViolationException) {
                        throw (CmsPermissionViolationException)e.getCause();
                    } else {
                        throw e;
                    }
                }
                dbc.getRequestContext().removeAttribute(I_CmsVfsDriver.REQ_ATTR_CHECK_PERMISSIONS);
            }

            deleteResource(dbc, resource, siblingMode);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_RESOURCE_1, context.getSitePath(resource)), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param context the current request context
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * 
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
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_DELETE_STATEXP_PUBLISHES_RESOURCE_1, resourceName),
                e);
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
        deleteUser(context, user, null);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user.<p>
     *
     * @param context the current request context
     * @param userId the id of the user to be deleted
     * @param replacementId the id of the user to be transfered
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsRequestContext context, CmsUUID userId, CmsUUID replacementId) throws CmsException {

        CmsUser user = readUser(context, userId);
        CmsUser replacementUser = null;
        if ((replacementId != null) && !replacementId.isNullUUID()) {
            replacementUser = readUser(context, replacementId);
        }
        deleteUser(context, user, replacementUser);
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

        CmsUser user = readUser(context, username);
        deleteUser(context, user, null);
    }

    /**
     * Destroys this security manager.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public synchronized void destroy() throws Throwable {

        try {
            if (m_driverManager != null) {
                if (m_driverManager.getLockManager() != null) {
                    try {
                        writeLocks();
                    } catch (Throwable t) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                org.opencms.lock.Messages.get().getBundle().key(
                                    org.opencms.lock.Messages.ERR_WRITE_LOCKS_FINAL_0),
                                t);
                        }
                    }
                }
                m_driverManager.destroy();
            }
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_DRIVER_MANAGER_CLOSE_0), t);
            }
        }

        m_driverManager = null;
        m_dbContextFactory = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_SECURITY_MANAGER_SHUTDOWN_1,
                this.getClass().getName()));
        }
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
     * @param context the current request context
     * @param structureId the structure id of the resource to check
     * @param filter the resource filter to use while reading
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see CmsObject#existsResource(CmsUUID, CmsResourceFilter)
     * @see CmsObject#existsResource(CmsUUID)
     */
    public boolean existsResource(CmsRequestContext context, CmsUUID structureId, CmsResourceFilter filter) {

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            readResource(dbc, structureId, filter);
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            dbc.clear();
        }
        return result;
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
     * @param context the current request context
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see CmsObject#existsResource(String, CmsResourceFilter)
     * @see CmsObject#existsResource(String)
     */
    public boolean existsResource(CmsRequestContext context, String resourcePath, CmsResourceFilter filter) {

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
     * Fills the given publish list with the the VFS resources that actually get published.<p>
     * 
     * Please refer to the source code of this method for the rules on how to decide whether a
     * new/changed/deleted <code>{@link CmsResource}</code> object can be published or not.<p>
     * 
     * @param context the current request context
     * @param publishList must be initialized with basic publish information (Project or direct publish operation)
     * 
     * @return the given publish list filled with all new/changed/deleted files from the current (offline) project 
     *      that will be published actually
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.db.CmsPublishList
     */
    public CmsPublishList fillPublishList(CmsRequestContext context, CmsPublishList publishList) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.fillPublishList(dbc, publishList);
            checkPublishPermissions(dbc, publishList);
        } catch (Exception e) {
            if (publishList.isDirectPublish()) {
                dbc.report(
                    null,
                    Messages.get().container(
                        Messages.ERR_GET_PUBLISH_LIST_DIRECT_1,
                        CmsFileUtil.formatResourceNames(context, publishList.getDirectPublishResources())),
                    e);
            } else {
                dbc.report(
                    null,
                    Messages.get().container(
                        Messages.ERR_GET_PUBLISH_LIST_PROJECT_1,
                        context.getCurrentProject().getName()),
                    e);
            }
        } finally {
            dbc.clear();
        }
        return publishList;
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param context the current request context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsAccessControlEntry> getAccessControlEntries(
        CmsRequestContext context,
        CmsResource resource,
        boolean getInherited) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsAccessControlEntry> result = null;
        try {
            result = m_driverManager.getAccessControlEntries(dbc, resource, getInherited);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_ACL_ENTRIES_1, context.getSitePath(resource)), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * If <code>inheritedOnly</code> is set, only inherited access control entries are returned.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * 
     * @return the access control list of the resource
     * 
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
            dbc.report(null, Messages.get().container(Messages.ERR_GET_ACL_ENTRIES_1, context.getSitePath(resource)), e);

        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to search project in
     * @param includeSubOus if to include sub organizational units
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProject> getAllAccessibleProjects(
        CmsRequestContext context,
        CmsOrganizationalUnit orgUnit,
        boolean includeSubOus) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsProject> result = null;
        try {
            result = m_driverManager.getAllAccessibleProjects(dbc, orgUnit, includeSubOus);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_GET_ALL_ACCESSIBLE_PROJECTS_1, dbc.currentUser().getName()),
                e);
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
     * @return list of <code>{@link CmsHistoryProject}</code> objects 
     *           with all projects from history.
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsHistoryProject> getAllHistoricalProjects(CmsRequestContext context) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsHistoryProject> result = null;
        try {
            result = m_driverManager.getAllHistoricalProjects(dbc);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_GET_ALL_ACCESSIBLE_PROJECTS_1, dbc.currentUser().getName()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to search project in
     * @param includeSubOus if to include sub organizational units
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsProject> getAllManageableProjects(
        CmsRequestContext context,
        CmsOrganizationalUnit orgUnit,
        boolean includeSubOus) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsProject> result = null;
        try {
            result = m_driverManager.getAllManageableProjects(dbc, orgUnit, includeSubOus);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_GET_ALL_MANAGEABLE_PROJECTS_1, dbc.currentUser().getName()),
                e);
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
     * @param context the current request context
     * @param groupname the name of the group
     * @param includeSubChildren if set also returns all sub-child groups of the given group
     *
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getChildren(CmsRequestContext context, String groupname, boolean includeSubChildren)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsGroup> result = null;
        try {
            result = m_driverManager.getChildren(
                dbc,
                m_driverManager.readGroup(dbc, CmsOrganizationalUnit.removeLeadingSeparator(groupname)),
                includeSubChildren);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_CHILD_GROUPS_TRANSITIVE_1, groupname), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the date when the resource was last visited by the user.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param user the user to check the date
     * @param resource the resource to check the date
     * 
     * @return the date when the resource was last visited by the user
     * 
     * @throws CmsException if something goes wrong
     */
    public long getDateLastVisitedBy(CmsRequestContext context, String poolName, CmsUser user, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        long result = 0;
        try {
            result = m_driverManager.getDateLastVisitedBy(dbc, poolName, user, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_GET_DATE_LASTVISITED_2,
                    user.getName(),
                    context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all groups of the given organizational unit.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to get the groups for
     * @param includeSubOus if all groups of sub-organizational units should be retrieved too
     * @param readRoles if to read roles or groups
     * 
     * @return all <code>{@link CmsGroup}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     */
    public List<CmsGroup> getGroups(
        CmsRequestContext context,
        CmsOrganizationalUnit orgUnit,
        boolean includeSubOus,
        boolean readRoles) throws CmsException {

        List<CmsGroup> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getGroups(dbc, orgUnit, includeSubOus, readRoles);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ORGUNIT_GROUPS_1, orgUnit.getName()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the list of groups to which the user directly belongs to.<p>
     *
     * @param context the current request context
     * @param username The name of the user
     * @param ouFqn the fully qualified name of the organizational unit to restrict the result set for
     * @param includeChildOus include groups of child organizational units
     * @param readRoles if to read roles or groups
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * @param remoteAddress the IP address to filter the groups in the result list 
     *
     * @return a list of <code>{@link CmsGroup}</code> objects filtered by the given IP address
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getGroupsOfUser(
        CmsRequestContext context,
        String username,
        String ouFqn,
        boolean includeChildOus,
        boolean readRoles,
        boolean directGroupsOnly,
        String remoteAddress) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsGroup> result = null;
        try {
            result = m_driverManager.getGroupsOfUser(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                CmsOrganizationalUnit.removeLeadingSeparator(ouFqn),
                includeChildOus,
                readRoles,
                directGroupsOnly,
                remoteAddress);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_GROUPS_OF_USER_2, username, remoteAddress), e);
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
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsLock result = null;
        try {
            result = m_driverManager.getLock(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_LOCK_1, context.getSitePath(resource)), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all locked resources in a given folder.<p>
     *
     * @param context the current request context
     * @param resource the folder to search in
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     * 
     * @throws CmsException if something goes wrong
     */
    public List<String> getLockedResources(CmsRequestContext context, CmsResource resource, CmsLockFilter filter)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<String> result = null;
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, false, CmsResourceFilter.ALL);
            result = m_driverManager.getLockedResources(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_COUNT_LOCKED_RESOURCES_FOLDER_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all locked resources in a given folder.<p>
     *
     * @param context the current request context
     * @param resource the folder to search in
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getLockedResourcesObjects(
        CmsRequestContext context,
        CmsResource resource,
        CmsLockFilter filter) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, false, CmsResourceFilter.ALL);
            result = m_driverManager.getLockedResourcesObjects(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_COUNT_LOCKED_RESOURCES_FOLDER_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all locked resources in a given folder, but uses a cache for resource lookups.<p>
     *
     * @param context the current request context
     * @param resource the folder to search in
     * @param filter the lock filter
     * @param cache the cache to use 
     * 
     * @return a list of locked resource paths (relative to current site)
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getLockedResourcesObjectsWithCache(
        CmsRequestContext context,
        CmsResource resource,
        CmsLockFilter filter,
        Map<String, CmsResource> cache) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, false, CmsResourceFilter.ALL);
            result = m_driverManager.getLockedResourcesObjectsWithCache(dbc, resource, filter, cache);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_COUNT_LOCKED_RESOURCES_FOLDER_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the lock manger.<p> 
     * 
     * @return the lock manager
     */
    public CmsLockManager getLockManager() {

        return m_lockManager;
    }

    /**
     * Returns all log entries matching the given filter.<p> 
     * 
     * @param context the current user context
     * @param filter the filter to match the log entries
     * 
     * @return all log entries matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#getLogEntries(CmsLogFilter)
     */
    public List<CmsLogEntry> getLogEntries(CmsRequestContext context, CmsLogFilter filter) throws CmsException {

        List<CmsLogEntry> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getLogEntries(dbc, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_LOG_ENTRIES_0), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all resources of organizational units for which the current user has 
     * the given role role.<p>
     * 
     * @param context the current request context
     * @param role the role to check
     *  
     * @return a list of {@link org.opencms.file.CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getManageableResources(CmsRequestContext context, CmsRole role) throws CmsException {

        List<CmsResource> resources;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            resources = getManageableResources(dbc, role);
        } finally {
            dbc.clear();
        }
        return resources;
    }

    /**
     * Returns all child organizational units of the given parent organizational unit including 
     * hierarchical deeper organization units if needed.<p>
     *
     * @param context the current request context
     * @param parent the parent organizational unit
     * @param includeChildren if hierarchical deeper organization units should also be returned
     * 
     * @return a list of <code>{@link CmsOrganizationalUnit}</code> objects
     * 
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getOrganizationalUnits(CmsObject, String, boolean)
     */
    public List<CmsOrganizationalUnit> getOrganizationalUnits(
        CmsRequestContext context,
        CmsOrganizationalUnit parent,
        boolean includeChildren) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsOrganizationalUnit> result = null;
        try {
            result = m_driverManager.getOrganizationalUnits(dbc, parent, includeChildren);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_ORGUNITS_1, parent.getName()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all the organizational units for which the current user has the given role.<p>
     * 
     * @param requestContext the current request context
     * @param role the role to check
     * @param includeSubOus if sub organizational units should be included in the search 
     *  
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsOrganizationalUnit> getOrgUnitsForRole(
        CmsRequestContext requestContext,
        CmsRole role,
        boolean includeSubOus) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(requestContext);
        List<CmsOrganizationalUnit> result = null;
        try {
            result = m_driverManager.getOrgUnitsForRole(dbc, role, includeSubOus);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_GET_ORGUNITS_ROLE_1, role.getName(requestContext.getLocale())),
                e);
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
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup getParent(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.getParent(dbc, CmsOrganizationalUnit.removeLeadingSeparator(groupname));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_PARENT_GROUP_1, groupname), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param user the user
     * 
     * @return bit set with allowed permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSetCustom getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsPermissionSetCustom result = null;
        try {
            result = m_driverManager.getPermissions(dbc, resource, user);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_GET_PERMISSIONS_2, user.getName(), context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the uuid id for the given id, 
     * remove this method as soon as possible.<p>
     * 
     * @param context the current cms context
     * @param id the old project id
     * 
     * @return the new uuid for the given id
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsUUID getProjectId(CmsRequestContext context, int id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUUID result = null;
        try {
            result = m_driverManager.getProjectId(dbc, id);
        } catch (CmsException e) {
            dbc.report(null, e.getMessageContainer(), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a new publish list that contains the unpublished resources related 
     * to all resources in the given publish list, the related resources exclude 
     * all resources in the given publish list and also locked (by other users) resources.<p>
     * 
     * @param context the current cms context
     * @param publishList the publish list to exclude from result
     * @param filter the relation filter to use to get the related resources
     * 
     * @return a new publish list that contains the related resources
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.publish.CmsPublishManager#getRelatedResourcesToPublish(CmsObject, CmsPublishList)
     */
    public CmsPublishList getRelatedResourcesToPublish(
        CmsRequestContext context,
        CmsPublishList publishList,
        CmsRelationFilter filter) throws CmsException {

        if (!publishList.isDirectPublish()) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_GET_RELATED_RESOURCES_PUBLISH_PROJECT_0));
        }

        CmsPublishList ret = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            ret = m_driverManager.getRelatedResourcesToPublish(dbc, publishList, filter);
            checkPublishPermissions(dbc, ret);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_GET_RELATED_RESOURCES_PUBLISH_DIRECT_1,
                    CmsFileUtil.formatResourceNames(context, publishList.getDirectPublishResources())),
                e);
        } finally {
            dbc.clear();
        }
        return ret;
    }

    /**
     * Returns all relations for the given resource matching the given filter.<p> 
     * 
     * @param context the current user context
     * @param resource the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return all {@link org.opencms.relations.CmsRelation} objects for the given resource matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#getRelationsForResource(String, CmsRelationFilter)
     */
    public List<CmsRelation> getRelationsForResource(
        CmsRequestContext context,
        CmsResource resource,
        CmsRelationFilter filter) throws CmsException {

        List<CmsRelation> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            if (resource != null) {
                checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_VIEW, false, CmsResourceFilter.ALL);
            }
            result = m_driverManager.getRelationsForResource(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READ_RELATIONS_1,
                    (resource != null) ? context.removeSiteRoot(resource.getRootPath()) : "null"),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all resources of the given organizational unit.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to get all resources for
     * 
     * @return all <code>{@link CmsResource}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     */
    public List<CmsResource> getResourcesForOrganizationalUnit(CmsRequestContext context, CmsOrganizationalUnit orgUnit)
    throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getResourcesForOrganizationalUnit(dbc, orgUnit);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ORGUNIT_RESOURCES_1, orgUnit.getName()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all resources associated to a given principal via an ACE with the given permissions.<p> 
     * 
     * If the <code>includeAttr</code> flag is set it returns also all resources associated to 
     * a given principal through some of following attributes.<p> 
     * 
     * <ul>
     *    <li>User Created</li>
     *    <li>User Last Modified</li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param principalId the id of the principal
     * @param permissions a set of permissions to match, can be <code>null</code> for all ACEs
     * @param includeAttr a flag to include resources associated by attributes
     * 
     * @return a set of <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<CmsResource> getResourcesForPrincipal(
        CmsRequestContext context,
        CmsUUID principalId,
        CmsPermissionSet permissions,
        boolean includeAttr) throws CmsException {

        Set<CmsResource> dependencies;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            dependencies = m_driverManager.getResourcesForPrincipal(
                dbc,
                dbc.currentProject(),
                principalId,
                permissions,
                includeAttr);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_RESOURCES_FOR_PRINCIPAL_LOG_1, principalId), e);
            dependencies = new HashSet<CmsResource>();
        } finally {
            dbc.clear();
        }
        return dependencies;
    }

    /**
     * Gets the groups which constitute a given role.<p>
     * 
     * @param context the request context 
     * @param role the role 
     * @param directUsersOnly if true, only direct users of the role group will be returned 
     * 
     * @return the role's groups 
     * 
     * @throws CmsException if something goes wrong 
     */
    public Set<CmsGroup> getRoleGroups(CmsRequestContext context, CmsRole role, boolean directUsersOnly)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.getRoleGroups(dbc, role.getGroupName(), directUsersOnly);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_ROLE_GROUPS_1, role.toString()), e);
            return null; // will never be executed 
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns all roles the given user has for the given resource.<p>
     *  
     * @param context the current request context
     * @param user the user to check
     * @param resource the resource to check the roles for
     * 
     * @return a list of {@link CmsRole} objects
     * 
     * @throws CmsException is something goes wrong
     */
    public List<CmsRole> getRolesForResource(CmsRequestContext context, CmsUser user, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsRole> result = null;
        try {
            result = m_driverManager.getRolesForResource(dbc, user, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_GET_ROLES_FOR_RESOURCE_2,
                    user.getName(),
                    context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns an instance of the common sql manager.<p>
     * 
     * @return an instance of the common sql manager
     */
    public CmsSqlManager getSqlManager() {

        return m_driverManager.getSqlManager();
    }

    /**
     * Returns all users of the given organizational unit.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to get the users for
     * @param recursive if all users of sub-organizational units should be retrieved too
     * 
     * @return all <code>{@link CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     */
    public List<CmsUser> getUsers(CmsRequestContext context, CmsOrganizationalUnit orgUnit, boolean recursive)
    throws CmsException {

        List<CmsUser> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getUsers(dbc, orgUnit, recursive);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ORGUNIT_USERS_1, orgUnit.getName()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * @param context the current request context
     * @param groupname the name of the group to list users from
     * @param includeOtherOuUsers include users of other organizational units
     * @param directUsersOnly if set only the direct assigned users will be returned, 
     *                          if not also indirect users, ie. members of child groups
     * @param readRoles if to read roles or groups
     *
     * @return all <code>{@link CmsUser}</code> objects in the group
     * 
     * @throws CmsException if operation was not successful
     */
    public List<CmsUser> getUsersOfGroup(
        CmsRequestContext context,
        String groupname,
        boolean includeOtherOuUsers,
        boolean directUsersOnly,
        boolean readRoles) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsUser> result = null;
        try {
            result = m_driverManager.getUsersOfGroup(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(groupname),
                includeOtherOuUsers,
                directUsersOnly,
                readRoles);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_GET_USERS_OF_GROUP_1, groupname), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the current user's publish list.<p>
     * 
     * @param context the request context
     * 
     * @return the current user's publish list
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getUsersPubList(CmsRequestContext context) throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getUsersPubList(dbc, context.getCurrentUser().getId());
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_USER_PUBLIST_1, context.getCurrentUser().getName()),
                e);

        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all users of the given organizational unit.<p>
     *
     * @param context the current request context
     * @param orgUnit the organizational unit to get the users for
     * @param recursive if all users of sub-organizational units should be retrieved too
     * 
     * @return all <code>{@link CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     */
    public List<CmsUser> getUsersWithoutAdditionalInfo(
        CmsRequestContext context,
        CmsOrganizationalUnit orgUnit,
        boolean recursive) throws CmsException {

        List<CmsUser> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.getUsersWithoutAdditionalInfo(dbc, orgUnit, recursive);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ORGUNIT_USERS_1, orgUnit.getName()), e);
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
     * <li><code>{@link I_CmsPermissionHandler#PERM_ALLOWED}</code></li>
     * <li><code>{@link I_CmsPermissionHandler#PERM_FILTERED}</code></li>
     * <li><code>{@link I_CmsPermissionHandler#PERM_DENIED}</code></li></ul><p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required for the operation
     * @param checkLock if true, a lock for the current user is required for 
     *      all write operations, if false it's ok to write as long as the resource
     *      is not locked by another user
     * @param filter the resource filter to use
     * 
     * @return <code>{@link I_CmsPermissionHandler#PERM_ALLOWED}</code> if the user has sufficient permissions on the resource
     *      for the requested operation
     * 
     * @throws CmsException in case of i/o errors (NOT because of insufficient permissions)
     * 
     * @see #hasPermissions(CmsDbContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public I_CmsPermissionHandler.CmsPermissionCheckResult hasPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        I_CmsPermissionHandler.CmsPermissionCheckResult result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = hasPermissions(dbc, resource, requiredPermissions, checkLock, filter);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Checks if the given user has the given role in the given organizational unit.<p>
     *  
     * If the organizational unit is <code>null</code>, this method will check if the
     * given user has the given role for at least one organizational unit.<p>
     *  
     * @param dbc the current OpenCms users database context
     * @param user the user to check the role for
     * @param role the role to check
     * 
     * @return <code>true</code> if the given user has the given role in the given organizational unit
     */
    public boolean hasRole(CmsDbContext dbc, CmsUser user, CmsRole role) {

        // try to read from cache
        String key = user.getName().toString() + role.getGroupName() + role.getOuFqn();
        Boolean result = OpenCms.getMemoryMonitor().getCachedRole(key);
        if (result != null) {
            return result.booleanValue();
        }

        // read all roles of the current user
        List<CmsGroup> roles;
        try {
            roles = m_driverManager.getGroupsOfUser(
                dbc,
                user.getName(),
                "",
                true,
                true,
                false,
                dbc.getRequestContext().getRemoteAddress());
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            // any exception: return false
            return false;
        }

        result = Boolean.valueOf(hasRole(role, roles));
        OpenCms.getMemoryMonitor().cacheRole(key, result.booleanValue());
        return result.booleanValue();
    }

    /**
     * Checks if the given user has the given role in the given organizational unit.<p>
     *  
     * If the organizational unit is <code>null</code>, this method will check if the
     * given user has the given role for at least one organizational unit.<p>
     *  
     * @param context the current request context
     * @param user the user to check the role for
     * @param role the role to check
     * 
     * @return <code>true</code> if the given user has the given role in the given organizational unit
     */
    public boolean hasRole(CmsRequestContext context, CmsUser user, CmsRole role) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        boolean result;
        try {
            result = hasRole(dbc, user, role);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Checks if the given user has the given role for the given resource.<p>
     *  
     * @param dbc the current OpenCms users database context
     * @param user the user to check the role for
     * @param role the role to check
     * @param resource the resource to check the role for
     * 
     * @return <code>true</code> if the given user has the given role for the given resource
     */
    public boolean hasRoleForResource(CmsDbContext dbc, CmsUser user, CmsRole role, CmsResource resource) {

        // guest user has no role
        if (user.isGuestUser()) {
            return false;
        }

        // try to read from cache
        String key = user.getId().toString() + role.getGroupName() + resource.getRootPath();
        Boolean result = OpenCms.getMemoryMonitor().getCachedRole(key);
        if (result != null) {
            return result.booleanValue();
        }

        // read all roles of the current user
        List<CmsGroup> roles;
        try {
            roles = new ArrayList<CmsGroup>(m_driverManager.getGroupsOfUser(
                dbc,
                user.getName(),
                "",
                true,
                true,
                true,
                dbc.getRequestContext().getRemoteAddress()));
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            // any exception: return false
            return false;
        }

        // first check the user has the role at all
        if (!hasRole(role.forOrgUnit(null), roles)) {
            result = Boolean.FALSE;
        }

        // then check if one applies to the given resource
        Iterator<CmsGroup> it = roles.iterator();
        while ((result == null) && it.hasNext()) {
            CmsGroup group = it.next();
            CmsRole givenRole = CmsRole.valueOf(group);
            if (hasRole(role.forOrgUnit(null), Collections.singletonList(group))) {
                // we have the same role, now check the resource if needed
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(givenRole.getOuFqn())) {
                    try {
                        CmsOrganizationalUnit orgUnit = m_driverManager.readOrganizationalUnit(
                            dbc,
                            givenRole.getOuFqn());
                        Iterator<CmsResource> itResources = m_driverManager.getResourcesForOrganizationalUnit(
                            dbc,
                            orgUnit).iterator();
                        while (itResources.hasNext()) {
                            CmsResource givenResource = itResources.next();
                            if (resource.getRootPath().startsWith(givenResource.getRootPath())) {
                                result = Boolean.TRUE;
                                break;
                            }
                        }
                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                        // ignore
                    }
                } else {
                    result = Boolean.TRUE;
                }
            }
        }

        if (result == null) {
            result = Boolean.FALSE;
        }
        OpenCms.getMemoryMonitor().cacheRole(key, result.booleanValue());
        return result.booleanValue();
    }

    /**
     * Checks if the given user has the given role for the given resource.<p>
     *  
     * @param context the current request context
     * @param user the user to check
     * @param role the role to check
     * @param resource the resource to check the role for
     * 
     * @return <code>true</code> if the given user has the given role for the given resource
     */
    public boolean hasRoleForResource(CmsRequestContext context, CmsUser user, CmsRole role, CmsResource resource) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        boolean result;
        try {
            result = hasRoleForResource(dbc, user, role, resource);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Writes a list of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.<p>
     * 
     * Access is granted, if:<p>
     * <ul>
     * <li>the current user has control permission on the resource</li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the required permissions are not satisfied
     */
    public void importAccessControlEntries(
        CmsRequestContext context,
        CmsResource resource,
        List<CmsAccessControlEntry> acEntries) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.importAccessControlEntries(dbc, resource, acEntries);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_IMPORT_ACL_ENTRIES_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
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
     * @param context the current request context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @param importCase if <code>true</code>, signals that this operation is done while importing resource, 
     *                   causing different lock behavior and potential "lost and found" usage
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource importResource(
        CmsRequestContext context,
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties,
        boolean importCase) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource newResource = null;
        try {
            checkOfflineProject(dbc);
            newResource = m_driverManager.createResource(dbc, resourcePath, resource, content, properties, importCase);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_IMPORT_RESOURCE_2, context.getSitePath(resource), resourcePath),
                e);
        } finally {
            dbc.clear();
        }
        return newResource;
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param context the current request context
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param firstname the first name of the user
     * @param lastname the last name of the user
     * @param email the email of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param dateCreated the creation date
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the  role {@link CmsRole#ACCOUNT_MANAGER} is not owned by the current user.
     */
    public CmsUser importUser(
        CmsRequestContext context,
        String id,
        String name,
        String password,
        String firstname,
        String lastname,
        String email,
        int flags,
        long dateCreated,
        Map<String, Object> additionalInfos) throws CmsException, CmsRoleViolationException {

        CmsUser newUser = null;

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        try {
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(name)));
            newUser = m_driverManager.importUser(
                dbc,
                id,
                CmsOrganizationalUnit.removeLeadingSeparator(name),
                password,
                firstname,
                lastname,
                email,
                flags,
                dateCreated,
                additionalInfos);

        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_IMPORT_USER_7,
                    new Object[] {
                        id,
                        name,
                        firstname,
                        lastname,
                        email,
                        new Integer(flags),
                        new Date(dateCreated),
                        additionalInfos}),
                e);
        } finally {
            dbc.clear();
        }

        return newUser;
    }

    /**
     * Increments a counter and returns its old value.<p>
     * 
     * @param context the request context 
     * @param name the name of the counter 
     *  
     * @return the value of the counter before incrementing 
     *    
     * @throws CmsException if something goes wrong 
     */
    public int incrementCounter(CmsRequestContext context, String name) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.incrementCounter(dbc, name);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_INCREMENT_COUNTER_1, name), e);
            return -1; // will never be reached  
        } finally {
            dbc.clear();
        }
    }

    /**
     * Initializes this security manager with a given runtime info factory.<p>
     * 
     * @param configurationManager the configurationManager
     * @param dbContextFactory the initialized OpenCms runtime info factory
     * @param publishEngine the publish engine
     * 
     * @throws CmsInitException if the initialization fails
     */
    public void init(
        CmsConfigurationManager configurationManager,
        I_CmsDbContextFactory dbContextFactory,
        CmsPublishEngine publishEngine) throws CmsInitException {

        if (dbContextFactory == null) {
            throw new CmsInitException(org.opencms.main.Messages.get().container(
                org.opencms.main.Messages.ERR_CRITICAL_NO_DB_CONTEXT_0));
        }

        m_dbContextFactory = dbContextFactory;

        CmsSystemConfiguration systemConfiguration = (CmsSystemConfiguration)configurationManager.getConfiguration(CmsSystemConfiguration.class);

        // create the driver manager
        m_driverManager = CmsDriverManager.newInstance(configurationManager, this, dbContextFactory, publishEngine);

        try {
            // invoke the init method of the driver manager
            m_driverManager.init(configurationManager, dbContextFactory);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE4_OK_0));
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.LOG_ERR_DRIVER_MANAGER_START_0);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsInitException(message, exc);
        }

        // create a new lock manager
        m_lockManager = m_driverManager.getLockManager();

        // initialize the permission handler
        String permHandlerClassName = systemConfiguration.getPermissionHandler();
        if (permHandlerClassName == null) {
            // use default implementation
            m_permissionHandler = new CmsDefaultPermissionHandler();
        } else {
            // use configured permission handler
            try {
                m_permissionHandler = (I_CmsPermissionHandler)Class.forName(permHandlerClassName).newInstance();
            } catch (Exception e) {
                throw new CmsInitException(org.opencms.main.Messages.get().container(
                    org.opencms.main.Messages.ERR_CRITICAL_CLASS_CREATION_1,
                    permHandlerClassName), e);
            }
        }

        m_permissionHandler.init(m_driverManager, systemConfiguration);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SECURITY_MANAGER_INIT_0));
        }
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
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, String resourcename) {

        boolean result = false;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.isInsideCurrentProject(dbc, resourcename);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Checks if the current user has management access to the current project.<p>
     *
     * @param context the current request context
     *
     * @return <code>true</code>, if the user has management access to the current project
     */
    public boolean isManagerOfProject(CmsRequestContext context) {

        try {
            return getAllManageableProjects(
                context,
                readOrganizationalUnit(context, context.getCurrentProject().getOuFqn()),
                false).contains(context.getCurrentProject());
        } catch (CmsException e) {
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    /**
     * Checks whether the subscription driver is available.<p>
     * 
     * @return true if the subscription driver is available 
     */
    public boolean isSubscriptionDriverAvailable() {

        return m_driverManager.isSubscriptionDriverAvailable();
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>type</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLockType#EXCLUSIVE}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#TEMPORARY}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#PUBLISH}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the resource to lock
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     * @see CmsObject#lockResourceTemporary(String)
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(CmsObject, CmsSecurityManager, CmsResource, CmsLockType)
     */
    public void lockResource(CmsRequestContext context, CmsResource resource, CmsLockType type) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
            m_driverManager.lockResource(dbc, resource, type);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_LOCK_RESOURCE_2, context.getSitePath(resource), type.toString()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * @param context the current request context
     * @param username the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * 
     * @return the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public CmsUser loginUser(CmsRequestContext context, String username, String password, String remoteAddress)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.loginUser(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                password,
                remoteAddress);
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
     * @return the principal (group or user) if found, otherwise <code>null</code>
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
     * @return the principal (group or user) if found, otherwise <code>null</code>
     */
    public I_CmsPrincipal lookupPrincipal(CmsRequestContext context, String principalName) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        I_CmsPrincipal result = null;
        try {
            result = m_driverManager.lookupPrincipal(dbc, CmsOrganizationalUnit.removeLeadingSeparator(principalName));
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Mark the given resource as visited by the user.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param resource the resource to mark as visited
     * @param user the user that visited the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void markResourceAsVisitedBy(CmsRequestContext context, String poolName, CmsResource resource, CmsUser user)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.markResourceAsVisitedBy(dbc, poolName, resource, user);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_MARK_RESOURCE_AS_VISITED_2,
                    context.getSitePath(resource),
                    user.getName()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns a new publish list that contains all resources of both given publish lists.<p>
     * 
     * @param context the current request context
     * @param pubList1 the first publish list
     * @param pubList2 the second publish list
     * 
     * @return a new publish list that contains all resources of both given publish lists
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.publish.CmsPublishManager#mergePublishLists(CmsObject, CmsPublishList, CmsPublishList)
     */
    public CmsPublishList mergePublishLists(CmsRequestContext context, CmsPublishList pubList1, CmsPublishList pubList2)
    throws CmsException {

        CmsPublishList ret = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // get all resources from the first list
            Set<CmsResource> publishResources = new HashSet<CmsResource>(pubList1.getAllResources());
            // get all resources from the second list
            publishResources.addAll(pubList2.getAllResources());

            // create merged publish list
            ret = new CmsPublishList(
                pubList1.getDirectPublishResources(),
                pubList1.isPublishSiblings(),
                pubList1.isPublishSubResources());
            ret.addAll(publishResources, false); // ignore files that should not be published
            ret.initialize(); // ensure sort order

            checkPublishPermissions(dbc, ret);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_MERGING_PUBLISH_LISTS_0), e);
        } finally {
            dbc.clear();
        }
        return ret;
    }

    /**
     * Moves a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * The moved resource will always be locked to the current user
     * after the move operation.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * @param context the current request context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if resource could not be copied 
     * 
     * @see CmsObject#moveResource(String, String)
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(CmsObject, CmsSecurityManager, CmsResource, String)
     */
    public void moveResource(CmsRequestContext context, CmsResource source, String destination)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            // checking if the destination folder exists and is not marked as deleted
            readResource(context, CmsResource.getParentFolder(destination), CmsResourceFilter.IGNORE_EXPIRATION);
            checkPermissions(dbc, source, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            checkPermissions(dbc, source, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

            checkSystemLocks(dbc, source);

            // check write permissions for subresources in case of moving a folder
            if (source.isFolder()) {
                dbc.getRequestContext().setAttribute(I_CmsVfsDriver.REQ_ATTR_CHECK_PERMISSIONS, Boolean.TRUE);
                try {
                    m_driverManager.getVfsDriver(dbc).moveResource(
                        dbc,
                        dbc.currentProject().getUuid(),
                        source,
                        destination);
                } catch (CmsDataAccessException e) {
                    // unwrap the permission violation exception
                    if (e.getCause() instanceof CmsPermissionViolationException) {
                        throw (CmsPermissionViolationException)e.getCause();
                    } else {
                        throw e;
                    }
                }
                dbc.getRequestContext().removeAttribute(I_CmsVfsDriver.REQ_ATTR_CHECK_PERMISSIONS);
            }
            moveResource(dbc, source, destination);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_MOVE_RESOURCE_2,
                    dbc.removeSiteRoot(source.getRootPath()),
                    dbc.removeSiteRoot(destination)),
                e);
        } finally {
            dbc.clear();
        }
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
     * @param resource the resource to apply this operation to
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
    public String moveToLostAndFound(CmsRequestContext context, CmsResource resource, boolean returnNameOnly)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        String result = null;
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            if (!returnNameOnly) {
                checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            }
            result = m_driverManager.moveToLostAndFound(dbc, resource, returnNameOnly);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_MOVE_TO_LOST_AND_FOUND_1,
                    dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     *
     * @param cms the current request context
     * @param publishList a publish list
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #fillPublishList(CmsRequestContext, CmsPublishList)
     */
    public CmsUUID publishProject(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws CmsException {

        CmsRequestContext context = cms.getRequestContext();
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check if the current user has the required publish permissions
            checkPublishPermissions(dbc, publishList);
            m_driverManager.publishProject(cms, dbc, publishList, report);
        } finally {
            dbc.clear();
        }
        return publishList.getPublishHistoryId();
    }

    /**
     * Reads all historical versions of a resource.<p>
     * 
     * The reading excludes the file content, if the resource is a file.<p>
     *
     * @param context the current request context
     * @param resource the resource to be read
     * 
     * @return a list of historical versions, as <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<I_CmsHistoryResource> readAllAvailableVersions(CmsRequestContext context, CmsResource resource)
    throws CmsException {

        List<I_CmsHistoryResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readAllAvailableVersions(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_ALL_HISTORY_FILE_HEADERS_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all property definitions for the given mapping type.<p>
     *
     * @param context the current request context
     * 
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsPropertyDefinition> readAllPropertyDefinitions(CmsRequestContext context) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsPropertyDefinition> result = null;
        try {
            result = m_driverManager.readAllPropertyDefinitions(dbc);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ALL_PROPDEF_0), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns all resources subscribed by the given user or group.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param principal the principal to read the subscribed resources
     * 
     * @return all resources subscribed by the given user or group
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readAllSubscribedResources(
        CmsRequestContext context,
        String poolName,
        CmsPrincipal principal) throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readAllSubscribedResources(dbc, poolName, principal);
        } catch (Exception e) {
            if (principal instanceof CmsUser) {
                dbc.report(
                    null,
                    Messages.get().container(Messages.ERR_READ_SUBSCRIBED_RESOURCES_ALL_USER_1, principal.getName()),
                    e);
            } else {
                dbc.report(
                    null,
                    Messages.get().container(Messages.ERR_READ_SUBSCRIBED_RESOURCES_ALL_GROUP_1, principal.getName()),
                    e);
            }
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all URL name mapping entries for a given structure id.<p>
     * 
     * @param context the request context 
     * @param id the structure id
     * 
     * @return the list of URL names for the given structure id 
     * @throws CmsException if something goes wrong 
     */
    public List<String> readAllUrlNameMappingEntries(CmsRequestContext context, CmsUUID id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            List<CmsUrlNameMappingEntry> entries = m_driverManager.readUrlNameMappingEntries(
                dbc,
                context.getCurrentProject().isOnlineProject(),
                CmsUrlNameMappingFilter.ALL.filterStructureId(id));
            List<String> result = new ArrayList<String>();
            for (CmsUrlNameMappingEntry entry : entries) {
                result.add(entry.getName());
            }
            return result;
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_READ_NEWEST_URLNAME_FOR_ID_1,
                id.toString());
            dbc.report(null, message, e);
            return null;
        } finally {
            dbc.clear();
        }

    }

    /**
     * Returns the first ancestor folder matching the filter criteria.<p>
     * 
     * If no folder matching the filter criteria is found, null is returned.<p>
     * 
     * @param context the context of the current request
     * @param resource the resource to start
     * @param filter the resource filter to match while reading the ancestors
     * 
     * @return the first ancestor folder matching the filter criteria or <code>null</code> if no folder was found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readAncestor(CmsRequestContext context, CmsResource resource, CmsResourceFilter filter)
    throws CmsException {

        // get the full folder path of the resource to start from
        String path = CmsResource.getFolderPath(resource.getRootPath());
        do {
            // check if the current folder matches the given filter
            if (existsResource(context, path, filter)) {
                // folder matches, return it
                return readFolder(context, path, filter);
            } else {
                // folder does not match filter criteria, go up one folder
                path = CmsResource.getParentFolder(path);
            }

            if (CmsStringUtil.isEmpty(path) || !path.startsWith(context.getSiteRoot())) {
                // site root or root folder reached and no matching folder found
                return null;
            }
        } while (true);
    }

    /**
     * Reads the newest URL name which is mapped to the given structure id.<p>
     * 
     * If the structure id is not mapped to any name, null will be returned.<p>
     *
     * @param context the request context 
     * @param id the structure id for which the newest mapped name should be returned
     * @param locale the locale for the mapping 
     * @param defaultLocales the default locales to use if there is no URL name mapping for the requested locale 
     * 
     * @return an URL name or null
     *  
     * @throws CmsException if something goes wrong 
     */
    public String readBestUrlName(CmsRequestContext context, CmsUUID id, Locale locale, List<Locale> defaultLocales)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.readBestUrlName(dbc, id, locale, defaultLocales);
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_READ_NEWEST_URLNAME_FOR_ID_1,
                id.toString());
            dbc.report(null, message, e);
            return null; // will never be reached 
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns the child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the parameters <code>getFolders</code> and <code>getFiles</code>
     * you can control what type of resources you want in the result list:
     * files, folders, or both.<p>
     * 
     * This method is mainly used by the workplace explorer.<p> 
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (read is required)
     * 
     */
    public List<CmsResource> readChildResources(
        CmsRequestContext context,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles) throws CmsException, CmsSecurityException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            result = m_driverManager.readChildResources(dbc, resource, filter, getFolders, getFiles, true);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_CHILD_RESOURCES_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the default file for the given folder.<p>
     * 
     * If the given resource is a file, then this file is returned.<p>
     * 
     * Otherwise, in case of a folder:<br> 
     * <ol>
     *   <li>the {@link CmsPropertyDefinition#PROPERTY_DEFAULT_FILE} is checked, and
     *   <li>if still no file could be found, the configured default files in the 
     *       <code>opencms-vfs.xml</code> configuration are iterated until a match is 
     *       found, and
     *   <li>if still no file could be found, <code>null</code> is returned
     * </ol><p>
     * 
     * @param context the request context
     * @param resource the folder to get the default file for
     * @param resourceFilter the resource filter
     *
     * @return the default file for the given folder
     * 
     * @throws CmsSecurityException if the user has no permissions to read the resulting file
     * 
     * @see CmsObject#readDefaultFile(String)
     */
    public CmsResource readDefaultFile(CmsRequestContext context, CmsResource resource, CmsResourceFilter resourceFilter)
    throws CmsSecurityException {

        CmsResource result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readDefaultFile(dbc, resource, resourceFilter);
            if (result != null) {
                // check if the user has read access to the resource
                checkPermissions(dbc, result, CmsPermissionSet.ACCESS_READ, true, resourceFilter);
            }
        } catch (CmsSecurityException se) {
            // permissions deny access to the resource
            throw se;
        } catch (CmsException e) {
            // ignore all other exceptions
            LOG.debug(e.getLocalizedMessage(), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all deleted (historical) resources below the given path, 
     * including the full tree below the path, if required.<p>
     * 
     * @param context the current request context
     * @param resource the parent resource to read the resources from
     * @param readTree <code>true</code> to read all subresources
     * 
     * @return a list of <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, int)
     * @see CmsObject#readResources(String, CmsResourceFilter, boolean)
     * @see CmsObject#readDeletedResources(String, boolean)
     */
    public List<I_CmsHistoryResource> readDeletedResources(
        CmsRequestContext context,
        CmsResource resource,
        boolean readTree) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<I_CmsHistoryResource> result = null;
        try {
            boolean isVfsManager = hasRoleForResource(dbc, dbc.currentUser(), CmsRole.VFS_MANAGER, resource);
            result = m_driverManager.readDeletedResources(dbc, resource, readTree, isVfsManager);
        } catch (CmsException e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READING_DELETED_RESOURCES_1,
                    dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(CmsRequestContext, String, CmsResourceFilter)}</code> instead.<p>
     *
     * @param context the current request context
     * @param resource the resource to be read
     * 
     * @return the file read from the VFS
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFile readFile(CmsRequestContext context, CmsResource resource) throws CmsException {

        CmsFile result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readFile(dbc, resource);
        } catch (Exception e) {
            if (resource instanceof I_CmsHistoryResource) {
                dbc.report(
                    null,
                    Messages.get().container(
                        Messages.ERR_READ_FILE_HISTORY_2,
                        context.getSitePath(resource),
                        new Integer(resource.getVersion())),
                    e);
            } else {
                dbc.report(null, Messages.get().container(Messages.ERR_READ_FILE_1, context.getSitePath(resource)), e);
            }
        } finally {
            dbc.clear();
        }
        return result;
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
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = readFolder(dbc, resourcename, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_FOLDER_2, resourcename, filter), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param context the current request context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsProject project) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, project);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param context the current request context
     * @param groupId the id of the group that is to be read
     *
     * @return the requested group
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsUUID groupId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, groupId);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_GROUP_FOR_ID_1, groupId.toString()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param context the current request context
     * @param groupname the name of the group that is to be read
     *
     * @return the requested group
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup readGroup(CmsRequestContext context, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.readGroup(dbc, CmsOrganizationalUnit.removeLeadingSeparator(groupname));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_GROUP_FOR_NAME_1, groupname), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a principal (an user or group) from the historical archive based on its ID.<p>
     * 
     * @param context the current request context
     * @param principalId the id of the principal to read
     * 
     * @return the historical principal entry with the given id
     * 
     * @throws CmsException if something goes wrong, ie. {@link CmsDbEntryNotFoundException}
     * 
     * @see CmsObject#readUser(CmsUUID)
     * @see CmsObject#readGroup(CmsUUID)
     * @see CmsObject#readHistoryPrincipal(CmsUUID)
     */
    public CmsHistoryPrincipal readHistoricalPrincipal(CmsRequestContext context, CmsUUID principalId)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsHistoryPrincipal result = null;
        try {
            result = m_driverManager.readHistoricalPrincipal(dbc, principalId);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_HISTORY_PRINCIPAL_1, principalId), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the latest historical project entry with the given id.<p>
     *
     * @param context the current request context
     * @param projectId the project id
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsHistoryProject readHistoryProject(CmsRequestContext context, CmsUUID projectId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsHistoryProject result = null;
        try {
            result = m_driverManager.readHistoryProject(dbc, projectId);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_HISTORY_PROJECT_2, projectId, dbc.currentProject().getName()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a historical project entry.<p>
     *
     * @param context the current request context
     * @param publishTag the publish tag of the project
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsHistoryProject readHistoryProject(CmsRequestContext context, int publishTag) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsHistoryProject result = null;
        try {
            result = m_driverManager.readHistoryProject(dbc, publishTag);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READ_HISTORY_PROJECT_2,
                    new Integer(publishTag),
                    dbc.currentProject().getName()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the list of all <code>{@link CmsProperty}</code> objects that belong to the given historical resource.<p>
     * 
     * @param context the current request context
     * @param resource the historical resource entry to read the properties for
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProperty> readHistoryPropertyObjects(CmsRequestContext context, I_CmsHistoryResource resource)
    throws CmsException {

        List<CmsProperty> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readHistoryPropertyObjects(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READ_PROPS_FOR_RESOURCE_1,
                    context.getSitePath((CmsResource)resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the structure id which is mapped to the given URL name, or null if the name is not 
     * mapped to any structure IDs.<p>
     * 
     * @param context the request context
     * @param name an URL name 
     * 
     * @return the structure ID which is mapped to the given name
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsUUID readIdForUrlName(CmsRequestContext context, String name) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.readIdForUrlName(dbc, name);
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_READ_ID_FOR_URLNAME_1, name);
            dbc.report(null, message, e);
            return null; // will never be reached 
        } finally {
            dbc.clear();
        }

    }

    /**
     * Reads the locks that were saved to the database in the previous run of OpenCms.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void readLocks() throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            m_driverManager.readLocks(dbc);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Reads the manager group of a project.<p>
     *
     * @param context the current request context
     * @param project the project to read from
     *
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsRequestContext context, CmsProject project) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsGroup result = null;
        try {
            result = m_driverManager.readManagerGroup(dbc, project);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads an organizational Unit based on its fully qualified name.<p>
     *
     * @param context the current request context
     * @param ouFqn the fully qualified name of the organizational Unit to be read
     * 
     * @return the organizational Unit that with the provided fully qualified name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsOrganizationalUnit readOrganizationalUnit(CmsRequestContext context, String ouFqn) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsOrganizationalUnit result = null;
        try {
            result = m_driverManager.readOrganizationalUnit(dbc, CmsOrganizationalUnit.removeLeadingSeparator(ouFqn));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_ORGUNIT_1, ouFqn), e);
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
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsRequestContext context, CmsProject project) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.readOwner(dbc, project);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_OWNER_FOR_PROJECT_2, project.getName(), project.getUuid()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the parent folder to the given structure id.<p>
     * 
     * @param context the current request context
     * @param structureId the child structure id
     * 
     * @return the parent folder <code>{@link CmsResource}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource readParentFolder(CmsRequestContext context, CmsUUID structureId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsResource result = null;
        try {
            result = m_driverManager.readParentFolder(dbc, structureId);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PARENT_FOLDER_2, dbc.currentProject().getName(), structureId),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param context the current request context
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readPath(CmsRequestContext context, String path, CmsResourceFilter filter)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            result = m_driverManager.readPath(dbc, path, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PATH_2, dbc.currentProject().getName(), path),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a project given the projects id.<p>
     *
     * @param id the id of the project
     * 
     * @return the project read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsUUID id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        CmsProject result = null;
        try {
            result = m_driverManager.readProject(dbc, id);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_PROJECT_FOR_ID_1, id), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a project.<p>
     *
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(CmsUUID)} to ensure the right project is read.<p>
     * 
     * @param name the name of the project
     * 
     * @return the project read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(String name) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        CmsProject result = null;
        try {
            result = m_driverManager.readProject(dbc, CmsOrganizationalUnit.removeLeadingSeparator(name));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_PROJECT_FOR_NAME_1, name), e);
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
     * @return the list of all resources, as <code>{@link String}</code> objects 
     *              that define the "view" of the given project
     * 
     * @throws CmsException if something goes wrong
     */
    public List<String> readProjectResources(CmsRequestContext context, CmsProject project) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<String> result = null;
        try {
            result = m_driverManager.readProjectResources(dbc, project);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PROJECT_RESOURCES_2, project.getName(), project.getUuid()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * Possible values for the <code>state</code> parameter are:<br>
     * <ul>
     * <li><code>{@link CmsResource#STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(CmsUUID, CmsResourceState)
     */
    public List<CmsResource> readProjectView(CmsRequestContext context, CmsUUID projectId, CmsResourceState state)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            result = m_driverManager.readProjectView(dbc, projectId, state);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_PROJECT_VIEW_1, projectId), e);
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
     * @return the property definition that was read
     * 
     * @throws CmsException a CmsDbEntryNotFoundException is thrown if the property definition does not exist
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsRequestContext context, String name) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsPropertyDefinition result = null;
        try {
            result = m_driverManager.readPropertyDefinition(dbc, name);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_PROPDEF_1, name), e);
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
     * @param resource the resource where the property is mapped to
     * @param key the property key name
     * @param search if <code>true</code>, the property is searched on all parent folders of the resource. 
     *      if it's not found attached directly to the resource.
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsRequestContext context, CmsResource resource, String key, boolean search)
    throws CmsException {

        CmsProperty result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readPropertyObject(dbc, resource, key, search);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PROP_FOR_RESOURCE_2, key, context.getSitePath(resource)),
                e);
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
     * has the same property attached but with a different value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param context the context of the current request
     * @param resource the resource where the property is mapped to
     * @param search <code>true</code>, if the properties should be searched on all parent folders  if not found on the resource
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsProperty> readPropertyObjects(CmsRequestContext context, CmsResource resource, boolean search)
    throws CmsException {

        List<CmsProperty> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readPropertyObjects(dbc, resource, search);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PROPS_FOR_RESOURCE_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param context the current request context
     * @param publishHistoryId unique ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsPublishedResource> readPublishedResources(CmsRequestContext context, CmsUUID publishHistoryId)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsPublishedResource> result = null;
        try {
            result = m_driverManager.readPublishedResources(dbc, publishHistoryId);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_PUBLISHED_RESOURCES_FOR_ID_1, publishHistoryId.toString()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the historical resource entry for the given resource with the given version number.<p>
     *
     * @param context the current request context
     * @param resource the resource to be read the version for
     * @param version the version number to retrieve
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsObject#readFile(CmsResource)
     * @see CmsObject#restoreResourceVersion(CmsUUID, int)
     * @see CmsObject#readResource(CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsRequestContext context, CmsResource resource, int version)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        I_CmsHistoryResource result = null;
        try {
            result = m_driverManager.readResource(dbc, resource, version);
        } catch (CmsException e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READING_RESOURCE_VERSION_2,
                    dbc.removeSiteRoot(resource.getRootPath()),
                    new Integer(version)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link CmsObject#readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param context the current request context
     * @param structureID the ID of the structure which will be used)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsObject#readResource(CmsUUID, CmsResourceFilter)
     * @see CmsObject#readResource(CmsUUID)
     * @see CmsObject#readFile(CmsResource)
     */
    public CmsResource readResource(CmsRequestContext context, CmsUUID structureID, CmsResourceFilter filter)
    throws CmsException {

        CmsResource result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = readResource(dbc, structureID, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_RESOURCE_FOR_ID_1, structureID), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link CmsObject#readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param context the current request context
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsObject#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String)
     * @see CmsObject#readFile(CmsResource)
     */
    public CmsResource readResource(CmsRequestContext context, String resourcePath, CmsResourceFilter filter)
    throws CmsException {

        CmsResource result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = readResource(dbc, resourcePath, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_RESOURCE_1, dbc.removeSiteRoot(resourcePath)),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param context the current request context
     * @param parent the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all subresources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     *  
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (read is required)
     * @throws CmsException if something goes wrong
     * 
     */
    public List<CmsResource> readResources(
        CmsRequestContext context,
        CmsResource parent,
        CmsResourceFilter filter,
        boolean readTree) throws CmsException, CmsSecurityException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            // check the access permissions
            checkPermissions(dbc, parent, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);
            result = m_driverManager.readResources(dbc, parent, filter, readTree);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_RESOURCES_1, context.removeSiteRoot(parent.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the resources that were visited by a user set in the filter.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param filter the filter that is used to get the visited resources
     * 
     * @return the resources that were visited by a user set in the filter
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesVisitedBy(
        CmsRequestContext context,
        String poolName,
        CmsVisitedByFilter filter) throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readResourcesVisitedBy(dbc, poolName, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_VISITED_RESOURCES_1, filter.toString()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads all resources that have a value (containing the specified value) set 
     * for the specified property (definition) in the given path.<p>
     * 
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param context the current request context
     * @param folder the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * @param value the string to search in the value of the property
     * @param filter the resource filter to apply to the result set
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesWithProperty(
        CmsRequestContext context,
        CmsResource folder,
        String propertyDefinition,
        String value,
        CmsResourceFilter filter) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<CmsResource> result = null;
        try {
            result = m_driverManager.readResourcesWithProperty(dbc, folder, propertyDefinition, value, filter);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_READ_RESOURCES_FOR_PROP_VALUE_3,
                    context.removeSiteRoot(folder.getRootPath()),
                    propertyDefinition,
                    value),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a set of users that are responsible for a specific resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<I_CmsPrincipal> readResponsiblePrincipals(CmsRequestContext context, CmsResource resource)
    throws CmsException {

        Set<I_CmsPrincipal> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readResponsiblePrincipals(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_RESPONSIBLE_USERS_1, resource.getRootPath()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a set of users that are responsible for a specific resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set<CmsUser> readResponsibleUsers(CmsRequestContext context, CmsResource resource) throws CmsException {

        Set<CmsUser> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readResponsibleUsers(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_RESPONSIBLE_USERS_1, resource.getRootPath()), e);
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
     * @param resource the specified resource
     * @param filter a filter object
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readSiblings(CmsRequestContext context, CmsResource resource, CmsResourceFilter filter)
    throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readSiblings(dbc, resource, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_SIBLINGS_1, context.getSitePath(resource)), e);
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
     * 
     * @return the parameter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(CmsRequestContext context, String rfsName)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        String result = null;
        try {
            result = m_driverManager.readStaticExportPublishedResourceParameters(dbc, rfsName);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_STATEXP_PUBLISHED_RESOURCE_PARAMS_1, rfsName),
                e);
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
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<String> readStaticExportResources(CmsRequestContext context, int parameterResources, long timestamp)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        List<String> result = null;
        try {
            result = m_driverManager.readStaticExportResources(dbc, parameterResources, timestamp);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_STATEXP_RESOURCES_1, new Date(timestamp)), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the subscribed history resources that were deleted.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param user the user that subscribed to the resource
     * @param groups the groups to check subscribed resources for
     * @param parent the parent resource (folder) of the deleted resources, if <code>null</code> all deleted resources will be returned
     * @param includeSubFolders indicates if the sub folders of the specified folder path should be considered, too
     * @param deletedFrom the time stamp from which the resources should have been deleted 
     * 
     * @return the subscribed history resources that were deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public List<I_CmsHistoryResource> readSubscribedDeletedResources(
        CmsRequestContext context,
        String poolName,
        CmsUser user,
        List<CmsGroup> groups,
        CmsResource parent,
        boolean includeSubFolders,
        long deletedFrom) throws CmsException {

        List<I_CmsHistoryResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readSubscribedDeletedResources(
                dbc,
                poolName,
                user,
                groups,
                parent,
                includeSubFolders,
                deletedFrom);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_READ_SUBSCRIBED_DELETED_RESOURCES_1, user.getName()),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the resources that were subscribed by a user or group set in the filter.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param filter the filter that is used to get the subscribed resources
     * 
     * @return the resources that were subscribed by a user or group set in the filter
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readSubscribedResources(
        CmsRequestContext context,
        String poolName,
        CmsSubscriptionFilter filter) throws CmsException {

        List<CmsResource> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.readSubscribedResources(dbc, poolName, filter);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_SUBSCRIBED_RESOURCES_1, filter.toString()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Reads the newest URL names of a structure id for all locales.<p>
     * 
     * @param context the current context 
     * @param id a structure id 
     * 
     * @return the list of URL names for all
     * @throws CmsException if something goes wrong 
     */
    public List<String> readUrlNamesForAllLocales(CmsRequestContext context, CmsUUID id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.readUrlNamesForAllLocales(dbc, id);
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_READ_NEWEST_URLNAME_FOR_ID_1,
                id.toString());
            dbc.report(null, message, e);
            return null; // will never be reached 
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * @param context the current request context
     * @param id the id of the user to read
     *
     * @return the user read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsRequestContext context, CmsUUID id) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, id);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_USER_FOR_ID_1, id.toString()), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a user object.<p>
     *
     * @param context the current request context
     * @param username the name of the user that is to be read
     *
     * @return user read form the cms
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(CmsRequestContext context, String username) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, CmsOrganizationalUnit.removeLeadingSeparator(username));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_USER_FOR_NAME_1, username), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/password pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param context the current request context
     * @param username the user name of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return user read
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(CmsRequestContext context, String username, String password) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsUser result = null;
        try {
            result = m_driverManager.readUser(dbc, CmsOrganizationalUnit.removeLeadingSeparator(username), password);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_READ_USER_FOR_NAME_1, username), e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (control of access control is required).
     * 
     */
    public void removeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            m_driverManager.removeAccessControlEntry(dbc, resource, principal);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_REMOVE_ACL_ENTRY_2,
                    context.getSitePath(resource),
                    principal.toString()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Removes a resource from the given organizational unit.<p>
     * 
     * @param context the current request context
     * @param orgUnit the organizational unit to remove the resource from
     * @param resource the resource that is to be removed from the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     */
    public void removeResourceFromOrgUnit(CmsRequestContext context, CmsOrganizationalUnit orgUnit, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(orgUnit.getName()));
            checkOfflineProject(dbc);
            m_driverManager.removeResourceFromOrgUnit(dbc, orgUnit, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_REMOVE_RESOURCE_FROM_ORGUNIT_2,
                    orgUnit.getName(),
                    dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not have management access to the project
     * 
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void removeResourceFromProject(CmsRequestContext context, CmsResource resource)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkManagerOfProjectRole(dbc, context.getCurrentProject());

            m_driverManager.removeResourceFromProject(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_COPY_RESOURCE_TO_PROJECT_2,
                    context.getSitePath(resource),
                    context.getCurrentProject().getName()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Removes the given resource to the given user's publish list.<p>
     * 
     * @param context the request context
     * @param structureIds the collection of structure IDs to remove
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromUsersPubList(CmsRequestContext context, Collection<CmsUUID> structureIds)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.removeResourceFromUsersPubList(dbc, context.getCurrentUser().getId(), structureIds);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_REMOVE_RESOURCE_FROM_PUBLIST_2,
                    context.getCurrentUser().getName(),
                    structureIds),
                e);

        } finally {
            dbc.clear();
        }
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param context the current request context
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * @param readRoles if to read roles or groups
     * 
     * @throws CmsException if operation was not successful
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     * 
     */
    public void removeUserFromGroup(CmsRequestContext context, String username, String groupname, boolean readRoles)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsRole role = CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(groupname));
            checkRoleForUserModification(dbc, username, role);
            m_driverManager.removeUserFromGroup(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                CmsOrganizationalUnit.removeLeadingSeparator(groupname),
                readRoles);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_REMOVE_USER_FROM_GROUP_2, username, groupname), e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsRequestContext context,
        CmsResource resource,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            if (CmsResourceTypeJsp.isJspTypeId(type)) {
                // security check preventing the creation of a jsp file without permissions
                checkRoleForResource(dbc, CmsRole.DEVELOPER, resource);
            }
            m_driverManager.replaceResource(dbc, resource, type, content, properties);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_REPLACE_RESOURCE_1, context.getSitePath(resource)),
                e);
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
     * @throws CmsSecurityException if the specified user name and old password could not be verified
     */
    public void resetPassword(CmsRequestContext context, String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.resetPassword(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                oldPassword,
                newPassword);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns the original path of given resource, that is the online path for the resource.<p>
     *  
     * If it differs from the offline path, the resource has been moved.<p>
     * 
     * @param context the current request context
     * @param resource the resource to get the path for
     * 
     * @return the online path
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.workplace.commons.CmsUndoChanges#resourceOriginalPath(CmsObject, String)
     */
    public String resourceOriginalPath(CmsRequestContext context, CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        String result = null;
        try {
            checkOfflineProject(dbc);
            result = m_driverManager.getVfsDriver(dbc).readResource(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getStructureId(),
                true).getRootPath();
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_TEST_MOVED_RESOURCE_1, dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Restores a deleted resource identified by its structure id from the historical archive.<p>
     * 
     * @param context the current request context
     * @param structureId the structure id of the resource to restore
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreDeletedResource(CmsUUID)
     */
    public void restoreDeletedResource(CmsRequestContext context, CmsUUID structureId) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            // write permissions on parent folder are checked later
            m_driverManager.restoreDeletedResource(dbc, structureId);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_RESTORE_DELETED_RESOURCE_1, structureId), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Restores a resource in the current project with the given version from the historical archive.<p>
     * 
     * @param context the current request context
     * @param resource the resource to restore from the archive
     * @param version the version number to restore
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#restoreResourceVersion(CmsUUID, int)
     * @see org.opencms.file.types.I_CmsResourceType#restoreResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsRequestContext context, CmsResource resource, int version)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.restoreResource(dbc, resource, version);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_RESTORE_RESOURCE_2,
                    context.getSitePath(resource),
                    new Integer(version)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Searches users by search criteria.<p>
     * 
     * @param requestContext the request context 
     * @param searchParams the search criteria object 
     * 
     * @return a list of users 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsUser> searchUsers(CmsRequestContext requestContext, CmsUserSearchParameters searchParams)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(requestContext);
        try {
            return m_driverManager.searchUsers(dbc, searchParams);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_SEARCH_USERS_0), e);
            return null;
        } finally {
            dbc.clear();
        }
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateExpired the new expire date of the changed resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#setDateExpired(String, long, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#setDateExpired(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateExpired(CmsRequestContext context, CmsResource resource, long dateExpired)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.setDateExpired(dbc, resource, dateExpired);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_SET_DATE_EXPIRED_2,
                    new Object[] {new Date(dateExpired), context.getSitePath(resource)}),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Changes the "last modified" time stamp of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateLastModified the new time stamp of the changed resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#setDateLastModified(String, long, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#setDateLastModified(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateLastModified(CmsRequestContext context, CmsResource resource, long dateLastModified)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.setDateLastModified(dbc, resource, dateLastModified);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_SET_DATE_LAST_MODIFIED_2,
                    new Object[] {new Date(dateLastModified), context.getSitePath(resource)}),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateReleased the new release date of the changed resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#setDateReleased(String, long, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#setDateReleased(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateReleased(CmsRequestContext context, CmsResource resource, long dateReleased)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.setDateReleased(dbc, resource, dateReleased);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_SET_DATE_RELEASED_2,
                    new Object[] {new Date(dateReleased), context.getSitePath(resource)}),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Sets a new parent-group for an already existing group.<p>
     *
     * @param context the current request context
     * @param groupName the name of the group that should be written
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     * 
     * @throws CmsException if operation was not successful
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     * 
     */
    public void setParentGroup(CmsRequestContext context, String groupName, String parentGroupName)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);

        try {
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(groupName)));
            m_driverManager.setParentGroup(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(groupName),
                CmsOrganizationalUnit.removeLeadingSeparator(parentGroupName));
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_SET_PARENT_GROUP_2, parentGroupName, groupName), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param context the current request context
     * @param username the name of the user
     * @param newPassword the new password
     * 
     * @throws CmsException if operation was not successful
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     */
    public void setPassword(CmsRequestContext context, String username, String newPassword)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsRole role = CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(username));
            checkRoleForUserModification(dbc, username, role);
            m_driverManager.setPassword(dbc, CmsOrganizationalUnit.removeLeadingSeparator(username), newPassword);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_SET_PASSWORD_1, username), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Marks a subscribed resource as deleted.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param resource the subscribed resource to mark as deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public void setSubscribedResourceAsDeleted(CmsRequestContext context, String poolName, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.setSubscribedResourceAsDeleted(dbc, poolName, resource);
        } catch (Exception e) {

            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_SET_SUBSCRIBED_RESOURCE_AS_DELETED_1,
                    context.getSitePath(resource)),
                e);

        } finally {
            dbc.clear();
        }
    }

    /**
     * Moves an user to the given organizational unit.<p>
     * 
     * @param context the current request context
     * @param orgUnit the organizational unit to add the principal to
     * @param user the user that is to be move to the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#setUsersOrganizationalUnit(CmsObject, String, String)
     */
    public void setUsersOrganizationalUnit(CmsRequestContext context, CmsOrganizationalUnit orgUnit, CmsUser user)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(orgUnit.getName()));
            checkOfflineProject(dbc);
            m_driverManager.setUsersOrganizationalUnit(dbc, orgUnit, user);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_SET_USERS_ORGUNIT_2, orgUnit.getName(), user.getName()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Subscribes the user or group to the resource.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param principal the principal that subscribes to the resource
     * @param resource the resource to subscribe to
     * 
     * @throws CmsException if something goes wrong
     */
    public void subscribeResourceFor(
        CmsRequestContext context,
        String poolName,
        CmsPrincipal principal,
        CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.subscribeResourceFor(dbc, poolName, principal, resource);
        } catch (Exception e) {
            if (principal instanceof CmsUser) {
                dbc.report(
                    null,
                    Messages.get().container(
                        Messages.ERR_SUBSCRIBE_RESOURCE_FOR_USER_2,
                        context.getSitePath(resource),
                        principal.getName()),
                    e);
            } else {
                dbc.report(
                    null,
                    Messages.get().container(
                        Messages.ERR_SUBSCRIBE_RESOURCE_FOR_GROUP_2,
                        context.getSitePath(resource),
                        principal.getName()),
                    e);
            }
        } finally {
            dbc.clear();
        }
    }

    /**
     * Undelete the resource by resetting it's state.<p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undeleteResource(String, boolean)
     * @see org.opencms.file.types.I_CmsResourceType#undelete(CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undelete(CmsRequestContext context, CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            checkSystemLocks(dbc, resource);

            m_driverManager.undelete(dbc, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_UNDELETE_FOR_RESOURCE_1, context.getSitePath(resource)),
                e);
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
     * @param mode the undo mode, one of the <code>{@link CmsResource}#UNDO_XXX</code> constants
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#undoChanges(String, CmsResource.CmsResourceUndoMode)
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceUndoMode)
     */
    public void undoChanges(CmsRequestContext context, CmsResource resource, CmsResource.CmsResourceUndoMode mode)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            checkSystemLocks(dbc, resource);

            m_driverManager.undoChanges(dbc, resource, mode);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_UNDO_CHANGES_FOR_RESOURCE_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Unlocks all resources in this project.<p>
     *
     * @param context the current request context
     * @param projectId the id of the project to be published
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsRoleViolationException if the current user does not own the required permissions
     */
    public void unlockProject(CmsRequestContext context, CmsUUID projectId)
    throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsProject project = m_driverManager.readProject(dbc, projectId);

        try {
            checkManagerOfProjectRole(dbc, project);
            m_driverManager.unlockProject(project);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_UNLOCK_PROJECT_2, projectId, dbc.currentUser().getName()),
                e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource (write access permission is required)
     * 
     * @see CmsObject#unlockResource(String)
     * @see org.opencms.file.types.I_CmsResourceType#unlockResource(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsRequestContext context, CmsResource resource)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.unlockResource(dbc, resource, false, false);
        } catch (CmsException e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_UNLOCK_RESOURCE_3,
                    context.getSitePath(resource),
                    dbc.currentUser().getName(),
                    e.getLocalizedMessage(dbc.getRequestContext().getLocale())),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Unsubscribes all deleted resources that were deleted before the specified time stamp.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param deletedTo the time stamp to which the resources have been deleted
     * 
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeAllDeletedResources(CmsRequestContext context, String poolName, long deletedTo)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.unsubscribeAllDeletedResources(dbc, poolName, deletedTo);
        } catch (Exception e) {

            dbc.report(null, Messages.get().container(Messages.ERR_UNSUBSCRIBE_ALL_DELETED_RESOURCES_USER_0), e);

        } finally {
            dbc.clear();
        }
    }

    /**
     * Unsubscribes the user or group from all resources.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param principal the principal that unsubscribes from all resources
     * 
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeAllResourcesFor(CmsRequestContext context, String poolName, CmsPrincipal principal)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.unsubscribeAllResourcesFor(dbc, poolName, principal);
        } catch (Exception e) {
            if (principal instanceof CmsUser) {
                dbc.report(
                    null,
                    Messages.get().container(Messages.ERR_UNSUBSCRIBE_ALL_RESOURCES_USER_1, principal.getName()),
                    e);
            } else {
                dbc.report(
                    null,
                    Messages.get().container(Messages.ERR_UNSUBSCRIBE_ALL_RESOURCES_GROUP_1, principal.getName()),
                    e);
            }
        } finally {
            dbc.clear();
        }
    }

    /**
     * Unsubscribes the principal from the resource.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param principal the principal that unsubscribes from the resource
     * @param resource the resource to unsubscribe from
     * 
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceFor(
        CmsRequestContext context,
        String poolName,
        CmsPrincipal principal,
        CmsResource resource) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.unsubscribeResourceFor(dbc, poolName, principal, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_UNSUBSCRIBE_RESOURCE_FOR_GROUP_2,
                    context.getSitePath(resource),
                    principal.getName()),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Unsubscribes all groups and users from the resource.<p>
     * 
     * @param context the request context
     * @param poolName the name of the database pool to use
     * @param resource the resource to unsubscribe all groups and users from
     * 
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceForAll(CmsRequestContext context, String poolName, CmsResource resource)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.unsubscribeResourceForAll(dbc, poolName, resource);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_UNSUBSCRIBE_RESOURCE_ALL_1, context.getSitePath(resource)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Logs everything that has not been written to DB jet.<p>
     * 
     * @throws CmsException if something goes wrong 
     */
    public void updateLog() throws CmsException {

        if (m_dbContextFactory == null) {
            // already shutdown
            return;
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            m_driverManager.updateLog(dbc);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Updates/Creates the relations for the given resource.<p>
     * 
     * @param context the current user context
     * @param resource the resource to update the relations for
     * @param relations the relations to update
     * 
     * @throws CmsException if something goes wrong 
     * 
     * @see CmsDriverManager#updateRelationsForResource(CmsDbContext, CmsResource, List)
     */
    public void updateRelationsForResource(CmsRequestContext context, CmsResource resource, List<CmsLink> relations)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.updateRelationsForResource(dbc, resource, relations);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_UPDATE_RELATIONS_1, dbc.removeSiteRoot(resource.getRootPath())),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Tests if a user is member of the given group.<p>
     *
     * @param context the current request context
     * @param username the name of the user to check
     * @param groupname the name of the group to check
     * 
     * @return <code>true</code>, if the user is in the group; or <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful
     */
    public boolean userInGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        boolean result = false;
        try {
            result = m_driverManager.userInGroup(
                dbc,
                CmsOrganizationalUnit.removeLeadingSeparator(username),
                CmsOrganizationalUnit.removeLeadingSeparator(groupname),
                false);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_USER_IN_GROUP_2, username, groupname), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Checks if a new password follows the rules for
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

        m_driverManager.validatePassword(password);
    }

    /**
     * Validates the relations for the given resources.<p>
     * 
     * @param context the current request context
     * @param publishList the resources to validate during publishing 
     * @param report a report to write the messages to
     * 
     * @return a map with lists of invalid links 
     *          (<code>{@link org.opencms.relations.CmsRelation}}</code> objects) 
     *          keyed by root paths
     * 
     * @throws Exception if something goes wrong
     */
    public Map<String, List<CmsRelation>> validateRelations(
        CmsRequestContext context,
        CmsPublishList publishList,
        I_CmsReport report) throws Exception {

        Map<String, List<CmsRelation>> result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            result = m_driverManager.validateRelations(dbc, publishList, report);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_VALIDATE_RELATIONS_0), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Writes an access control entries to a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param ace the entry to write
     * 
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_CONTROL} required)
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsAccessControlEntry ace)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
            if (ace.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                // only vfs managers can set the overwrite all ACE
                checkRoleForResource(dbc, CmsRole.VFS_MANAGER, resource);
            }
            m_driverManager.writeAccessControlEntry(dbc, resource, ace);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_ACL_ENTRY_1, context.getSitePath(resource)), e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_WRITE} required)
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsRequestContext context, CmsFile resource) throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        CmsFile result = null;
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            result = m_driverManager.writeFile(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_FILE_1, context.getSitePath(resource)), e);
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group id has to be a valid OpenCms group id.<br>
     * 
     * The group with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param context the current request context
     * @param group the group that should be written
     *
     * @throws CmsRoleViolationException if the current user does not own the role {@link CmsRole#ACCOUNT_MANAGER} for the current project
     * @throws CmsException if operation was not successful
     */
    public void writeGroup(CmsRequestContext context, CmsGroup group) throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(group.getName())));
            m_driverManager.writeGroup(dbc, group);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_GROUP_1, group.getName()), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Creates a historical entry of the current project.<p>
     * 
     * @param context the current request context
     * @param publishTag the correlative publish tag
     * @param publishDate the date of publishing
     *
     * @throws CmsException if operation was not successful
     */
    public void writeHistoryProject(CmsRequestContext context, int publishTag, long publishDate) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            m_driverManager.writeHistoryProject(dbc, publishTag, publishDate);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_HISTORY_PROJECT_4,
                    new Object[] {
                        new Integer(publishTag),
                        dbc.currentProject().getName(),
                        dbc.currentProject().getUuid(),
                        new Long(publishDate)}),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Writes the locks that are currently stored in-memory to the database to allow restoring them in 
     * later startups.<p> 
     * 
     * This overwrites the locks previously stored in the underlying database table.<p>
     * 
     * @throws CmsException if something goes wrong 
     */
    public void writeLocks() throws CmsException {

        if (m_dbContextFactory == null) {
            // already shutdown
            return;
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            m_driverManager.writeLocks(dbc);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Writes an already existing organizational unit.<p>
     *
     * The organizational unit id has to be a valid OpenCms organizational unit id.<p>
     * 
     * The organizational unit with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param context the current request context
     * @param organizationalUnit the organizational unit that should be written
     * 
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#writeOrganizationalUnit(CmsObject, CmsOrganizationalUnit)
     */
    public void writeOrganizationalUnit(CmsRequestContext context, CmsOrganizationalUnit organizationalUnit)
    throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkRole(dbc, CmsRole.ADMINISTRATOR.forOrgUnit(organizationalUnit.getName()));
            checkOfflineProject(dbc);
            m_driverManager.writeOrganizationalUnit(dbc, organizationalUnit);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_ORGUNIT_1, organizationalUnit.getName()), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param project the project that should be written
     * @param context the current request context
     * 
     * @throws CmsRoleViolationException if the current user does not own the required permissions
     * @throws CmsException if operation was not successful
     */
    public void writeProject(CmsRequestContext context, CmsProject project)
    throws CmsRoleViolationException, CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkManagerOfProjectRole(dbc, project);
            m_driverManager.writeProject(dbc, project);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_PROJECT_1, project.getName()), e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_WRITE} required)
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObject(CmsObject, CmsSecurityManager, CmsResource, CmsProperty)
     */
    public void writePropertyObject(CmsRequestContext context, CmsResource resource, CmsProperty property)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            m_driverManager.writePropertyObject(dbc, resource, property);
        } catch (Exception e) {
            dbc.report(
                null,
                Messages.get().container(Messages.ERR_WRITE_PROP_2, property.getName(), context.getSitePath(resource)),
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_WRITE} required)
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObjects(CmsObject, CmsSecurityManager, CmsResource, List)
     */
    public void writePropertyObjects(CmsRequestContext context, CmsResource resource, List<CmsProperty> properties)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
            // write the properties
            m_driverManager.writePropertyObjects(dbc, resource, properties, true);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_PROPS_1, context.getSitePath(resource)), e);
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
     * @throws CmsSecurityException if the user has insufficient permission for the given resource ({@link CmsPermissionSet#ACCESS_WRITE} required)
     * @throws CmsException if something goes wrong
     */
    public void writeResource(CmsRequestContext context, CmsResource resource)
    throws CmsException, CmsSecurityException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            checkOfflineProject(dbc);
            checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
            m_driverManager.writeResource(dbc, resource);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_RESOURCE_1, context.getSitePath(resource)), e);
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
     * @param linkType the type of resource exported (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a time stamp for writing the data into the db
     * 
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
            dbc.report(
                null,
                Messages.get().container(
                    Messages.ERR_WRITE_STATEXP_PUBLISHED_RESOURCES_3,
                    resourceName,
                    linkParameter,
                    new Date(timestamp)),
                e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Writes a new URL name mapping for a given resource.<p>
     * 
     * The first name from the given sequence which is not already mapped to another resource will be used for 
     * the URL name mapping.<p>
     * 
     * @param context the request context 
     * @param nameSeq the sequence of URL name candidates  
     * @param structureId the structure id which should be mapped to the name
     * @param locale the locale for the mapping 
     *  
     * @return the name which was actually mapped to the structure id
     *  
     * @throws CmsException if something goes wrong 
     */
    public String writeUrlNameMapping(
        CmsRequestContext context,
        Iterator<String> nameSeq,
        CmsUUID structureId,
        String locale) throws CmsException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            return m_driverManager.writeUrlNameMapping(dbc, nameSeq, structureId, locale);
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_ADD_URLNAME_MAPPING_2,
                nameSeq.toString(),
                structureId.toString());
            dbc.report(null, message, e);
            return null;
        } finally {
            dbc.clear();
        }
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param context the current request context
     * @param user the user to be updated
     *
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER} for the current project
     * @throws CmsException if operation was not successful
     */
    public void writeUser(CmsRequestContext context, CmsUser user) throws CmsException, CmsRoleViolationException {

        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsRole role = CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(user.getName()));
            checkRoleForUserModification(dbc, user.getName(), role);
            m_driverManager.writeUser(dbc, user);
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_WRITE_USER_1, user.getName()), e);
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
     * 
     * @see #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    protected void checkPermissions(
        CmsDbContext dbc,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException {

        // get the permissions
        I_CmsPermissionHandler.CmsPermissionCheckResult permissions = hasPermissions(
            dbc,
            resource,
            requiredPermissions,
            checkLock,
            filter);
        if (!permissions.isAllowed()) {
            checkPermissions(dbc.getRequestContext(), resource, requiredPermissions, permissions);
        }
    }

    /**
     * Applies the permission check result of a previous call 
     * to {@link #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)}.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param permissions the permissions to check
     * 
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsLockException if the lock status is not as required
     * @throws CmsVfsResourceNotFoundException if the required resource has been filtered
     */
    protected void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        I_CmsPermissionHandler.CmsPermissionCheckResult permissions)
    throws CmsSecurityException, CmsLockException, CmsVfsResourceNotFoundException {

        if (permissions == I_CmsPermissionHandler.PERM_FILTERED) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_PERM_FILTERED_1,
                context.getSitePath(resource)));
        }
        if (permissions == I_CmsPermissionHandler.PERM_DENIED) {
            throw new CmsPermissionViolationException(Messages.get().container(
                Messages.ERR_PERM_DENIED_2,
                context.getSitePath(resource),
                requiredPermissions.getPermissionString()));
        }
        if (permissions == I_CmsPermissionHandler.PERM_NOTLOCKED) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_PERM_NOTLOCKED_2,
                context.getSitePath(resource),
                context.getCurrentUser().getName()));
        }
    }

    /**
     * Checks that the current user has enough permissions to modify the given user.<p>
     * 
     * @param dbc the database context
     * @param username the name of the user to modify
     * @param role the needed role
     * 
     * @throws CmsDataAccessException if something goes wrong
     * @throws CmsRoleViolationException if the user has not the needed permissions
     */
    protected void checkRoleForUserModification(CmsDbContext dbc, String username, CmsRole role)
    throws CmsDataAccessException, CmsRoleViolationException {

        CmsUser user = m_driverManager.readUser(dbc, CmsOrganizationalUnit.removeLeadingSeparator(username));
        // a user is allowed to write his own data
        if (!dbc.currentUser().equals(user)) {
            // check if the user to be modified is root admin
            if (hasRole(dbc, user, CmsRole.ROOT_ADMIN)) {
                // check the user that is going to do the modification is root admin
                checkRole(dbc, CmsRole.ROOT_ADMIN);
                // check if the user to be modified is administrator
            } else if (hasRole(dbc, user, CmsRole.ADMINISTRATOR)) {
                // check the user that is going to do the modification is administrator
                checkRole(dbc, CmsRole.ADMINISTRATOR);
            } else {
                // check the user that is going to do the modification has the given role
                checkRole(dbc, role);
            }
        }
    }

    /**
     * Checks if the given resource contains a resource that has a system lock.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to check
     * 
     * @throws CmsException in case there is a system lock contained in the given resource
     */
    protected void checkSystemLocks(CmsDbContext dbc, CmsResource resource) throws CmsException {

        if (m_lockManager.hasSystemLocks(dbc, resource)) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_RESOURCE_SYSTEM_LOCKED_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }
    }

    /**
     * Internal recursive method for deleting a resource.<p>
     * 
     * @param dbc the db context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @throws CmsException if something goes wrong
     */
    protected void deleteResource(CmsDbContext dbc, CmsResource resource, CmsResource.CmsResourceDeleteMode siblingMode)
    throws CmsException {

        if (resource.isFolder()) {
            // collect all resources in the folder (but exclude deleted ones)
            List<CmsResource> resources = m_driverManager.readChildResources(
                dbc,
                resource,
                CmsResourceFilter.IGNORE_EXPIRATION,
                true,
                true,
                false);

            Set<CmsUUID> deletedResources = new HashSet<CmsUUID>();
            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                if ((siblingMode == CmsResource.DELETE_REMOVE_SIBLINGS)
                    && deletedResources.contains(childResource.getResourceId())) {
                    // sibling mode is "delete all siblings" and another sibling of the current child resource has already
                    // been deleted- do nothing and continue with the next child resource.
                    continue;
                }
                if (childResource.isFolder()) {
                    // recurse into this method for subfolders
                    deleteResource(dbc, childResource, siblingMode);
                } else {
                    // handle child resources
                    m_driverManager.deleteResource(dbc, childResource, siblingMode);
                }
                deletedResources.add(childResource.getResourceId());
            }
            deletedResources.clear();
        }
        // handle the resource itself
        m_driverManager.deleteResource(dbc, resource, siblingMode);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user, if given.<p>
     *
     * @param context the current request context
     * @param user the user to be deleted
     * @param replacement the user to be transfered, can be <code>null</code>
     * 
     * @throws CmsRoleViolationException if the current user does not own the rule {@link CmsRole#ACCOUNT_MANAGER}
     * @throws CmsSecurityException in case the user is a default user 
     * @throws CmsException if something goes wrong
     */
    protected void deleteUser(CmsRequestContext context, CmsUser user, CmsUser replacement)
    throws CmsException, CmsSecurityException, CmsRoleViolationException {

        if (OpenCms.getDefaultUsers().isDefaultUser(user.getName())) {
            throw new CmsSecurityException(org.opencms.security.Messages.get().container(
                org.opencms.security.Messages.ERR_CANT_DELETE_DEFAULT_USER_1,
                user.getName()));
        }
        if (context.getCurrentUser().equals(user)) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_USER_CANT_DELETE_ITSELF_USER_0));
        }
        CmsDbContext dbc = m_dbContextFactory.getDbContext(context);
        try {
            CmsRole role = CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParentOrganizationalUnit(user.getName()));
            checkRoleForUserModification(dbc, user.getName(), role);
            // this is needed because 
            // I_CmsUserDriver#removeAccessControlEntriesForPrincipal(CmsDbContext, CmsProject, CmsProject, CmsUUID)
            // expects an offline project, if not data will become inconsistent
            checkOfflineProject(dbc);
            if (replacement == null) {
                m_driverManager.deleteUser(dbc, context.getCurrentProject(), user.getName(), null);
            } else {
                m_driverManager.deleteUser(dbc, context.getCurrentProject(), user.getName(), replacement.getName());
            }
        } catch (Exception e) {
            dbc.report(null, Messages.get().container(Messages.ERR_DELETE_USER_1, user.getName()), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns all resources of organizational units for which the current user has 
     * the given role role.<p>
     * 
     * @param dbc the current database context
     * @param role the role to check
     *  
     * @return a list of {@link org.opencms.file.CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getManageableResources(CmsDbContext dbc, CmsRole role) throws CmsException {

        CmsOrganizationalUnit ou = m_driverManager.readOrganizationalUnit(dbc, role.getOuFqn());
        if (hasRole(dbc, dbc.currentUser(), role)) {
            return m_driverManager.getResourcesForOrganizationalUnit(dbc, ou);
        }
        List<CmsResource> resources = new ArrayList<CmsResource>();
        Iterator<CmsOrganizationalUnit> it = m_driverManager.getOrganizationalUnits(dbc, ou, false).iterator();
        while (it.hasNext()) {
            CmsOrganizationalUnit orgUnit = it.next();
            resources.addAll(getManageableResources(dbc, role.forOrgUnit(orgUnit.getName())));
        }
        return resources;
    }

    /**
     * Returns the organizational unit for the parent of the given fully qualified name.<p>
     * 
     * @param fqn the fully qualified name to get the parent organizational unit for
     * 
     * @return the parent organizational unit for the fully qualified name
     */
    protected String getParentOrganizationalUnit(String fqn) {

        String ouFqn = CmsOrganizationalUnit.getParentFqn(CmsOrganizationalUnit.removeLeadingSeparator(fqn));
        if (ouFqn == null) {
            ouFqn = "";
        }
        return ouFqn;
    }

    /**
     * Performs a non-blocking permission check on a resource.<p>
     * 
     * This test will not throw an exception in case the required permissions are not
     * available for the requested operation. Instead, it will return one of the 
     * following values:<ul>
     * <li><code>{@link I_CmsPermissionHandler#PERM_ALLOWED}</code></li>
     * <li><code>{@link I_CmsPermissionHandler#PERM_FILTERED}</code></li>
     * <li><code>{@link I_CmsPermissionHandler#PERM_DENIED}</code></li></ul><p>
     * 
     * @param dbc the current database context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required for the operation
     * @param checkLock if true, a lock for the current user is required for 
     *      all write operations, if false it's ok to write as long as the resource
     *      is not locked by another user
     * @param filter the resource filter to use
     * 
     * @return <code>{@link I_CmsPermissionHandler#PERM_ALLOWED}</code> if the user has sufficient permissions on the resource
     *      for the requested operation
     * 
     * @throws CmsException in case of i/o errors (NOT because of insufficient permissions)
     */
    protected I_CmsPermissionHandler.CmsPermissionCheckResult hasPermissions(
        CmsDbContext dbc,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        return m_permissionHandler.hasPermissions(dbc, resource, requiredPermissions, checkLock, filter);
    }

    /**
     * Returns <code>true</code> if at least one of the given group names is equal to a group name
     * of the given role in the given organizational unit.<p>
     * 
     * This checks the given list against the group of the given role as well as against the role group 
     * of all parent roles.<p>
     * 
     * If the organizational unit is <code>null</code>, this method will check if the
     * given user has the given role for at least one organizational unit.<p>
     *  
     * @param role the role to check
     * @param roles the groups to match the role groups against
     * 
     * @return <code>true</code> if at last one of the given group names is equal to a group name
     *      of this role
     */
    protected boolean hasRole(CmsRole role, List<CmsGroup> roles) {

        // iterates the roles the user are in
        Iterator<CmsGroup> itGroups = roles.iterator();
        while (itGroups.hasNext()) {
            String groupName = (itGroups.next()).getName();
            // iterate the role hierarchy
            Iterator<String> itDistinctGroupNames = role.getDistinctGroupNames().iterator();
            while (itDistinctGroupNames.hasNext()) {
                String distictGroupName = itDistinctGroupNames.next();
                if (distictGroupName.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
                    // this is a ou independent role 
                    // we need an exact match, and we ignore the ou parameter
                    if (groupName.equals(distictGroupName.substring(1))) {
                        return true;
                    }
                } else {
                    // first check if the user has the role at all
                    if (groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + distictGroupName)
                        || groupName.equals(distictGroupName)) {
                        // this is a ou dependent role
                        if (role.getOuFqn() == null) {
                            // ou parameter is null, so the user needs to have the role in at least one ou does not matter which
                            return true;
                        } else {
                            // the user needs to have the role in the given ou or in a parent ou
                            // now check that the ou matches
                            String groupFqn = CmsOrganizationalUnit.getParentFqn(groupName);
                            if (role.getOuFqn().startsWith(groupFqn)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Internal recursive method to move a resource.<p>
     * 
     * @param dbc the db context
     * @param source the source resource
     * @param destination the destination path
     * 
     * @throws CmsException if something goes wrong
     */
    protected void moveResource(CmsDbContext dbc, CmsResource source, String destination) throws CmsException {

        List<CmsResource> resources = null;

        if (source.isFolder()) {
            if (!CmsResource.isFolder(destination)) {
                // ensure folder name end's with a /
                destination = destination.concat("/");
            }
            // collect all resources in the folder without checking permissions
            resources = m_driverManager.readChildResources(dbc, source, CmsResourceFilter.ALL, true, true, false);
        }

        // target permissions will be checked later
        m_driverManager.moveResource(dbc, source, destination, false);

        // make sure lock is set
        CmsResource destinationResource = m_driverManager.readResource(dbc, destination, CmsResourceFilter.ALL);
        try {
            // the destination must always get a new lock
            m_driverManager.lockResource(dbc, destinationResource, CmsLockType.EXCLUSIVE);
        } catch (Exception e) {
            // could happen with with shared locks on single files
            if (LOG.isWarnEnabled()) {
                LOG.warn(e);
            }
        }

        if (resources != null) {
            // now walk through all sub-resources in the folder
            for (int i = 0; i < resources.size(); i++) {
                CmsResource childResource = resources.get(i);
                String childDestination = destination.concat(childResource.getName());
                // recurse with child resource
                moveResource(dbc, childResource, childDestination);
            }
        }
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
    protected CmsFolder readFolder(CmsDbContext dbc, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(dbc, resourcename, filter);
        return m_driverManager.convertResourceToFolder(resource);
    }

    /**
     * Reads a resource from the OpenCms VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param structureID the ID of the structure to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, CmsResourceFilter)
     * @see CmsObject#readResource(CmsUUID)
     * @see CmsObject#readFile(CmsResource)
     */
    protected CmsResource readResource(CmsDbContext dbc, CmsUUID structureID, CmsResourceFilter filter)
    throws CmsException {

        // read the resource from the VFS
        CmsResource resource = m_driverManager.readResource(dbc, structureID, filter);

        // check if the user has read access to the resource
        checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, filter);

        // access was granted - return the resource
        return resource;
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
     * @see CmsObject#readFile(CmsResource)
     */
    protected CmsResource readResource(CmsDbContext dbc, String resourcePath, CmsResourceFilter filter)
    throws CmsException {

        // read the resource from the VFS
        CmsResource resource = m_driverManager.readResource(dbc, resourcePath, filter);

        // check if the user has read access to the resource
        checkPermissions(dbc, resource, CmsPermissionSet.ACCESS_READ, true, filter);

        // access was granted - return the resource
        return resource;
    }

}
