/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsSecurityManager.java,v $
 * Date   : $Date: 2004/10/22 14:36:02 $
 * Version: $Revision: 1.1 $
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

import org.opencms.file.*;
import org.opencms.lock.CmsLock;
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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * The OpenCms security manager.<p>
 * 
 * The security manager checks the permissions required for a user action invoke by the Cms object. If permissions 
 * are granted, the security manager invokes a method on the OpenCms driver manager to access the database.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.5.2
 */
public class CmsSecurityManager {

    /** The initialized OpenCms driver manager to access the database. */
    protected CmsDriverManager m_driverManager;

    /** The factory to create runtime info objects. */
    protected I_CmsRuntimeInfoFactory m_runtimeInfoFactory;

    /**
     * Default constructor.<p>
     */
    private CmsSecurityManager() {

        // intentionally left blank
    }

    /**
     * Creates a new instance of the OpenCms security manager.<p>
     * 
     * @param configuration the opencms.properties configuration file
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * 
     * @return a new instance of the OpenCms security manager
     * @throws CmsException if something goes wrong
     */
    public static CmsSecurityManager newInstance(
        ExtendedProperties configuration,
        I_CmsRuntimeInfoFactory runtimeInfoFactory) throws CmsException {

        CmsSecurityManager securityManager = new CmsSecurityManager();
        securityManager.init(configuration, runtimeInfoFactory);

        return securityManager;
    }

    /**
     * @see org.opencms.db.CmsDriverManager#acceptTask(org.opencms.file.CmsRequestContext, int)
     */
    public void acceptTask(CmsRequestContext context, int taskId) throws CmsException {

        m_driverManager.acceptTask(context, taskId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#accessProject(org.opencms.file.CmsRequestContext, int)
     */
    public boolean accessProject(CmsRequestContext context, int projectId) throws CmsException {

        return m_driverManager.accessProject(context, projectId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#addImportUser(I_CmsRuntimeInfo, org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.util.Hashtable, java.lang.String, int)
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
                    context,
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
     * @see org.opencms.db.CmsDriverManager#addUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Hashtable)
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
     * @see org.opencms.db.CmsDriverManager#addUserToGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String)
     */
    public void addUserToGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        m_driverManager.addUserToGroup(context, null, username, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#addWebUser(I_CmsRuntimeInfo, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Hashtable)
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
     * @see org.opencms.db.CmsDriverManager#addWebUser(I_CmsRuntimeInfo, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Hashtable)
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
     * @see org.opencms.db.CmsDriverManager#backupProject(org.opencms.file.CmsRequestContext, org.opencms.file.CmsProject, int, long)
     */
    public void backupProject(CmsRequestContext context, CmsProject backupProject, int tagId, long publishDate)
    throws CmsException {

        m_driverManager.backupProject(context, backupProject, tagId, publishDate);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#changeLastModifiedProjectId(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.CmsDriverManager#changeLock(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource)
     */
    public void changeLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        m_driverManager.changeLock(context, null, resource);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#changeUserType(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.util.CmsUUID, int)
     */
    public void changeUserType(CmsRequestContext context, CmsUUID userId, int userType) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.changeUserType(context, runtimeInfo, userId, userType);
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
     * @see org.opencms.db.CmsDriverManager#changeUserType(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, int)
     */
    public void changeUserType(CmsRequestContext context, String username, int userType) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.changeUserType(context, runtimeInfo, username, userType);
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
     * @see org.opencms.db.CmsDriverManager#checkPermissions(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, org.opencms.security.CmsPermissionSet, boolean, org.opencms.file.CmsResourceFilter)
     */
    public void checkPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException, CmsSecurityException, CmsVfsResourceNotFoundException {

        m_driverManager.checkPermissions(context, resource, requiredPermissions, checkLock, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#chflags(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, int)
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
     * @see org.opencms.db.CmsDriverManager#chtype(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, int)
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
     * @see CmsDriverManager#clearcache()
     */
    public void clearcache() {

        m_driverManager.clearcache();
    }

    /**
     * @see org.opencms.db.CmsDriverManager#copyAccessControlEntries(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.CmsDriverManager#copyResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, java.lang.String, int)
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
     * @see org.opencms.db.CmsDriverManager#copyResourceToProject(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource)
     */
    public void copyResourceToProject(CmsRequestContext context, CmsResource resource) throws CmsException {

        m_driverManager.copyResourceToProject(context, resource);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#countLockedResources(org.opencms.file.CmsRequestContext, int)
     */
    public int countLockedResources(CmsRequestContext context, int id) throws CmsException {

        return m_driverManager.countLockedResources(context, id);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#countLockedResources(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public int countLockedResources(CmsRequestContext context, String foldername) throws CmsException {

        return m_driverManager.countLockedResources(context, foldername);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#createGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.util.CmsUUID, java.lang.String, java.lang.String, int, java.lang.String)
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
                newGroup = m_driverManager.createGroup(context, runtimeInfo, id, name, description, flags, parent);
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
     * @see org.opencms.db.CmsDriverManager#createGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String, int, java.lang.String)
     */
    public CmsGroup createGroup(CmsRequestContext context, String name, String description, int flags, String parent)
    throws CmsException {

        CmsGroup newGroup;

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                newGroup = m_driverManager.createGroup(context, runtimeInfo, name, description, flags, parent);
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
     * @see org.opencms.db.CmsDriverManager#createProject(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
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
     * @see org.opencms.db.CmsDriverManager#createPropertydefinition(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, int)
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
     * @see org.opencms.db.CmsDriverManager#createResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, org.opencms.file.CmsResource, byte[], java.util.List, boolean)
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
     * @see org.opencms.db.CmsDriverManager#createResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, int, byte[], java.util.List)
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
     * @see org.opencms.db.CmsDriverManager#createSibling(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, java.lang.String, java.util.List)
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
     * @see org.opencms.db.CmsDriverManager#createTask(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String, java.lang.String, long, int)
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
     * @see org.opencms.db.CmsDriverManager#createTask(org.opencms.file.CmsUser, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, long, int)
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
     * @see org.opencms.db.CmsDriverManager#createTempfileProject(org.opencms.file.CmsRequestContext)
     */
    public CmsProject createTempfileProject(CmsRequestContext context) throws CmsException {

        return m_driverManager.createTempfileProject(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#deleteAllStaticExportPublishedResources(org.opencms.file.CmsRequestContext, int)
     */
    public void deleteAllStaticExportPublishedResources(CmsRequestContext context, int linkType) throws CmsException {

        m_driverManager.deleteAllStaticExportPublishedResources(context, linkType);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#deleteBackups(org.opencms.file.CmsRequestContext, long, int, org.opencms.report.I_CmsReport)
     */
    public void deleteBackups(CmsRequestContext context, long timestamp, int versions, I_CmsReport report)
    throws CmsException {

        m_driverManager.deleteBackups(context, timestamp, versions, report);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#deleteGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String)
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
     * @see org.opencms.db.CmsDriverManager#deleteProject(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, CmsProject)
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
     * @see org.opencms.db.CmsDriverManager#deletePropertydefinition(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, int)
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
     * @see org.opencms.db.CmsDriverManager#deleteResource(I_CmsRuntimeInfo, org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, int)
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
     * @see org.opencms.db.CmsDriverManager#deleteStaticExportPublishedResource(org.opencms.file.CmsRequestContext, java.lang.String, int, java.lang.String)
     */
    public void deleteStaticExportPublishedResource(
        CmsRequestContext context,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsException {

        m_driverManager.deleteStaticExportPublishedResource(context, resourceName, linkType, linkParameter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#deleteUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.util.CmsUUID)
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
                m_driverManager.deleteUser(context, runtimeInfo, userId);
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
     * @see org.opencms.db.CmsDriverManager#deleteUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String)
     */
    public void deleteUser(CmsRequestContext context, String username) throws CmsException {

        if (isAdmin(context)
            && !(username.equals(OpenCms.getDefaultUsers().getUserAdmin()) || username.equals(OpenCms.getDefaultUsers()
                .getUserGuest()))) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);

            try {
                m_driverManager.deleteUser(context, runtimeInfo, username);
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
     * @see org.opencms.db.CmsDriverManager#deleteWebUser(I_CmsRuntimeInfo, org.opencms.util.CmsUUID)
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
     * @see org.opencms.db.CmsDriverManager#digest(java.lang.String)
     */
    public String digest(String value) throws CmsException {

        return m_driverManager.digest(value);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#endTask(org.opencms.file.CmsRequestContext, int)
     */
    public void endTask(CmsRequestContext context, int taskid) throws CmsException {

        m_driverManager.endTask(context, taskid);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#forwardTask(org.opencms.file.CmsRequestContext, int, java.lang.String, java.lang.String)
     */
    public void forwardTask(CmsRequestContext context, int taskid, String newRoleName, String newUserName)
    throws CmsException {

        m_driverManager.forwardTask(context, taskid, newRoleName, newUserName);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getAccessControlEntries(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, boolean)
     */
    public Vector getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited)
    throws CmsException {

        return m_driverManager.getAccessControlEntries(context, resource, getInherited);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getAccessControlList(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, boolean)
     */
    public CmsAccessControlList getAccessControlList(
        CmsRequestContext context,
        CmsResource resource,
        boolean inheritedOnly) throws CmsException {

        return m_driverManager.getAccessControlList(context, resource, inheritedOnly);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getAllAccessibleProjects(org.opencms.file.CmsRequestContext)
     */
    public List getAllAccessibleProjects(CmsRequestContext context) throws CmsException {

        return m_driverManager.getAllAccessibleProjects(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getAllBackupProjects()
     */
    public Vector getAllBackupProjects() throws CmsException {

        return m_driverManager.getAllBackupProjects();
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getAllManageableProjects(org.opencms.file.CmsRequestContext)
     */
    public List getAllManageableProjects(CmsRequestContext context) throws CmsException {

        return m_driverManager.getAllManageableProjects(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getBackupTagId()
     */
    public int getBackupTagId() {

        return m_driverManager.getBackupTagId();
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getChild(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public Vector getChild(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getChild(context, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getChilds(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public Vector getChilds(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getChilds(context, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getConfigurations()
     */
    public ExtendedProperties getConfigurations() {

        return m_driverManager.getConfigurations();
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getDirectGroupsOfUser(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public Vector getDirectGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        return m_driverManager.getDirectGroupsOfUser(context, username);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getGroups(org.opencms.file.CmsRequestContext)
     */
    public Vector getGroups(CmsRequestContext context) throws CmsException {

        return m_driverManager.getGroups(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getGroupsOfUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String)
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        return m_driverManager.getGroupsOfUser(context, null, username);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getGroupsOfUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String)
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username, String remoteAddress) throws CmsException {

        return m_driverManager.getGroupsOfUser(context, null, username, remoteAddress);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getLock(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource)
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        return m_driverManager.getLock(context, null, resource);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getLock(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String)
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) throws CmsException {

        return m_driverManager.getLock(context, null, resourcename);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getParent(java.lang.String)
     */
    public CmsGroup getParent(String groupname) throws CmsException {

        return m_driverManager.getParent(groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getPermissions(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, org.opencms.file.CmsUser)
     */
    public CmsPermissionSetCustom getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user)
    throws CmsException {

        return m_driverManager.getPermissions(context, resource, user);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getPublishList(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, boolean)
     */
    public synchronized CmsPublishList getPublishList(
        CmsRequestContext context,
        CmsResource directPublishResource,
        boolean publishSiblings) throws CmsException {

        return m_driverManager.getPublishList(context, directPublishResource, publishSiblings);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getResourcesInTimeRange(org.opencms.file.CmsRequestContext, java.lang.String, long, long)
     */
    public List getResourcesInTimeRange(CmsRequestContext context, String folder, long starttime, long endtime)
    throws CmsException {

        return m_driverManager.getResourcesInTimeRange(context, folder, starttime, endtime);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getResourcesWithProperty(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String)
     */
    public List getResourcesWithProperty(CmsRequestContext context, String path, String propertyDefinition)
    throws CmsException {

        return m_driverManager.getResourcesWithProperty(context, path, propertyDefinition);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getResourcesWithPropertyDefinition(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public List getResourcesWithPropertyDefinition(CmsRequestContext context, String propertyDefinition)
    throws CmsException {

        return m_driverManager.getResourcesWithPropertyDefinition(context, propertyDefinition);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getTaskPar(int, java.lang.String)
     */
    public String getTaskPar(int taskId, String parName) throws CmsException {

        return m_driverManager.getTaskPar(taskId, parName);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getTaskType(java.lang.String)
     */
    public int getTaskType(String taskName) throws CmsException {

        return m_driverManager.getTaskType(taskName);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getUsers(org.opencms.file.CmsRequestContext)
     */
    public Vector getUsers(CmsRequestContext context) throws CmsException {

        return m_driverManager.getUsers(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getUsers(org.opencms.file.CmsRequestContext, int)
     */
    public Vector getUsers(CmsRequestContext context, int type) throws CmsException {

        return m_driverManager.getUsers(context, type);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#getUsersOfGroup(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public Vector getUsersOfGroup(CmsRequestContext context, String groupname) throws CmsException {

        return m_driverManager.getUsersOfGroup(context, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#hasPermissions(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, org.opencms.security.CmsPermissionSet, boolean, org.opencms.file.CmsResourceFilter)
     */
    public int hasPermissions(
        CmsRequestContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        return m_driverManager.hasPermissions(context, resource, requiredPermissions, checkLock, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#importAccessControlEntries(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, java.util.Vector)
     */
    public void importAccessControlEntries(CmsRequestContext context, CmsResource resource, Vector acEntries)
    throws CmsException {

        m_driverManager.importAccessControlEntries(context, resource, acEntries);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#importFolder(org.opencms.file.CmsObject, org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String)
     */
    public void importFolder(CmsObject cms, CmsRequestContext context, String importFile, String importPath)
    throws CmsException {

        m_driverManager.importFolder(cms, context, importFile, importPath);
    }

    /**
     * Initializes this security manager with a given driver manager.<p>
     * 
     * @param configuration the opencms.properties configuration file
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * @throws CmsException if something goes wrong
     */
    public void init(ExtendedProperties configuration, I_CmsRuntimeInfoFactory runtimeInfoFactory) throws CmsException {

        m_driverManager = CmsDriverManager.newInstance(configuration, runtimeInfoFactory);

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
     * @see org.opencms.db.CmsDriverManager#isAdmin(org.opencms.file.CmsRequestContext)
     */
    public boolean isAdmin(CmsRequestContext context) {

        return m_driverManager.isAdmin(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#isInsideCurrentProject(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, String resourcename) {

        return m_driverManager.isInsideCurrentProject(context, resourcename);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#isManagerOfProject(org.opencms.file.CmsRequestContext)
     */
    public boolean isManagerOfProject(CmsRequestContext context) {

        return m_driverManager.isManagerOfProject(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#isProjectManager(org.opencms.file.CmsRequestContext)
     */
    public boolean isProjectManager(CmsRequestContext context) {

        return m_driverManager.isProjectManager(context);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#lockResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, int)
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
     * @see org.opencms.db.CmsDriverManager#loginUser(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public CmsUser loginUser(String username, String password, String remoteAddress, int userType)
    throws CmsSecurityException {

        return m_driverManager.loginUser(username, password, remoteAddress, userType);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#lookupPrincipal(org.opencms.util.CmsUUID)
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        return m_driverManager.lookupPrincipal(principalId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#lookupPrincipal(java.lang.String)
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        return m_driverManager.lookupPrincipal(principalName);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#moveToLostAndFound(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, boolean)
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
     * @see org.opencms.db.CmsDriverManager#newDriverInstance(org.apache.commons.collections.ExtendedProperties, java.lang.String, java.lang.String)
     */
    public Object newDriverInstance(ExtendedProperties configuration, String driverName, String driverPoolUrl)
    throws CmsException {

        return m_driverManager.newDriverInstance(configuration, driverName, driverPoolUrl);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#postPublishBoResource(org.opencms.file.CmsRequestContext, org.opencms.db.CmsPublishedResource, org.opencms.util.CmsUUID, int)
     */
    public void postPublishBoResource(
        CmsRequestContext context,
        CmsPublishedResource publishedBoResource,
        CmsUUID publishId,
        int tagId) throws CmsException {

        m_driverManager.postPublishBoResource(context, publishedBoResource, publishId, tagId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#publishProject(org.opencms.file.CmsObject, I_CmsRuntimeInfo, org.opencms.db.CmsPublishList, org.opencms.report.I_CmsReport)
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
            hasPublishPermissions |= (CmsDriverManager.PERM_ALLOWED == hasPermissions(
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
     * @see org.opencms.db.CmsDriverManager#readAgent(org.opencms.workflow.CmsTask)
     */
    public CmsUser readAgent(CmsTask task) throws CmsException {

        return m_driverManager.readAgent(task);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readAllBackupFileHeaders(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public List readAllBackupFileHeaders(CmsRequestContext context, String filename) throws CmsException {

        return m_driverManager.readAllBackupFileHeaders(context, filename);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readAllProjectResources(org.opencms.file.CmsRequestContext, int)
     */
    public List readAllProjectResources(CmsRequestContext context, int projectId) throws CmsException {

        return m_driverManager.readAllProjectResources(context, projectId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readAllPropertydefinitions(org.opencms.file.CmsRequestContext, int)
     */
    public List readAllPropertydefinitions(CmsRequestContext context, int mappingtype) throws CmsException {

        return m_driverManager.readAllPropertydefinitions(context, mappingtype);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readBackupFile(org.opencms.file.CmsRequestContext, int, java.lang.String)
     */
    public CmsBackupResource readBackupFile(CmsRequestContext context, int tagId, String filename) throws CmsException {

        return m_driverManager.readBackupFile(context, tagId, filename);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readBackupFileHeader(org.opencms.file.CmsRequestContext, int, java.lang.String)
     */
    public CmsBackupResource readBackupFileHeader(CmsRequestContext context, int tagId, String filename)
    throws CmsException {

        return m_driverManager.readBackupFileHeader(context, tagId, filename);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readBackupProject(int)
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {

        return m_driverManager.readBackupProject(tagId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readChildResources(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, org.opencms.file.CmsResourceFilter, boolean, boolean)
     */
    public List readChildResources(
        CmsRequestContext context,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles) throws CmsException {

        return m_driverManager.readChildResources(context, resource, filter, getFolders, getFiles);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readFile(org.opencms.file.CmsRequestContext, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsFile readFile(CmsRequestContext context, String filename, CmsResourceFilter filter) throws CmsException {

        return m_driverManager.readFile(context, filename, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readFilesByType(org.opencms.file.CmsRequestContext, int, int)
     */
    public List readFilesByType(CmsRequestContext context, int projectId, int resourcetype) throws CmsException {

        return m_driverManager.readFilesByType(context, projectId, resourcetype);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readFolder(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsFolder readFolder(CmsRequestContext context, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        return m_driverManager.readFolder(context, null, resourcename, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readGivenTasks(int, java.lang.String, int, java.lang.String, java.lang.String)
     */
    public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readGivenTasks(projectId, ownerName, taskType, orderBy, sort);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readGroup(org.opencms.file.CmsProject)
     */
    public CmsGroup readGroup(CmsProject project) {

        return m_driverManager.readGroup(project);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readGroup(org.opencms.workflow.CmsTask)
     */
    public CmsGroup readGroup(CmsTask task) throws CmsException {

        return m_driverManager.readGroup(task);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readGroup(org.opencms.util.CmsUUID)
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {

        return m_driverManager.readGroup(groupId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readGroup(I_CmsRuntimeInfo, java.lang.String)
     */
    public CmsGroup readGroup(String groupname) throws CmsException {

        return m_driverManager.readGroup(null, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readManagerGroup(org.opencms.file.CmsProject)
     */
    public CmsGroup readManagerGroup(CmsProject project) {

        return m_driverManager.readManagerGroup(project);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readOriginalAgent(org.opencms.workflow.CmsTask)
     */
    public CmsUser readOriginalAgent(CmsTask task) throws CmsException {

        return m_driverManager.readOriginalAgent(task);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readOwner(org.opencms.file.CmsProject)
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {

        return m_driverManager.readOwner(project);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readOwner(org.opencms.workflow.CmsTask)
     */
    public CmsUser readOwner(CmsTask task) throws CmsException {

        return m_driverManager.readOwner(task);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readOwner(org.opencms.workflow.CmsTaskLog)
     */
    public CmsUser readOwner(CmsTaskLog log) throws CmsException {

        return m_driverManager.readOwner(log);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readPath(int, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public List readPath(int projectId, String path, CmsResourceFilter filter) throws CmsException {

        return m_driverManager.readPath(projectId, path, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProject(org.opencms.workflow.CmsTask)
     */
    public CmsProject readProject(CmsTask task) throws CmsException {

        return m_driverManager.readProject(task);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProject(int)
     */
    public CmsProject readProject(int id) throws CmsException {

        return m_driverManager.readProject(id);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProject(java.lang.String)
     */
    public CmsProject readProject(String name) throws CmsException {

        return m_driverManager.readProject(name);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProjectLogs(int)
     */
    public List readProjectLogs(int projectId) throws CmsException {

        return m_driverManager.readProjectLogs(projectId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProjectResources(org.opencms.file.CmsProject)
     */
    public List readProjectResources(CmsProject project) throws CmsException {

        return m_driverManager.readProjectResources(project);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readProjectView(org.opencms.file.CmsRequestContext, int, int)
     */
    public List readProjectView(CmsRequestContext context, int projectId, int state) throws CmsException {

        return m_driverManager.readProjectView(context, projectId, state);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readPropertydefinition(org.opencms.file.CmsRequestContext, java.lang.String, int)
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name, int mappingtype)
    throws CmsException {

        return m_driverManager.readPropertydefinition(context, name, mappingtype);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readPropertyObject(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public CmsProperty readPropertyObject(
        CmsRequestContext context,
        String resourceName,
        String siteRoot,
        String key,
        boolean search) throws CmsException {

        return m_driverManager.readPropertyObject(context, resourceName, siteRoot, key, search);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readPropertyObjects(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String, boolean)
     */
    public List readPropertyObjects(CmsRequestContext context, String resourceName, String siteRoot, boolean search)
    throws CmsException {

        return m_driverManager.readPropertyObjects(context, resourceName, siteRoot, search);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readPublishedResources(org.opencms.file.CmsRequestContext, org.opencms.util.CmsUUID)
     */
    public List readPublishedResources(CmsRequestContext context, CmsUUID publishHistoryId) throws CmsException {

        return m_driverManager.readPublishedResources(context, publishHistoryId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsResource readResource(CmsRequestContext context, String resourcePath, CmsResourceFilter filter)
    throws CmsException {

        return m_driverManager.readResource(context, null, resourcePath, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readResources(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource, org.opencms.file.CmsResourceFilter, boolean)
     */
    public List readResources(CmsRequestContext context, CmsResource parent, CmsResourceFilter filter, boolean readTree)
    throws CmsException {

        return m_driverManager.readResources(context, parent, filter, readTree);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readSiblings(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public List readSiblings(CmsRequestContext context, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        return m_driverManager.readSiblings(context, null, resourcename, filter);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readStaticExportPublishedResourceParameters(org.opencms.file.CmsRequestContext, java.lang.String)
     */
    public String readStaticExportPublishedResourceParameters(CmsRequestContext context, String rfsName)
    throws CmsException {

        return m_driverManager.readStaticExportPublishedResourceParameters(context, rfsName);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readStaticExportResources(org.opencms.file.CmsRequestContext, int, long)
     */
    public List readStaticExportResources(CmsRequestContext context, int parameterResources, long timestamp)
    throws CmsException {

        return m_driverManager.readStaticExportResources(context, parameterResources, timestamp);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readTask(int)
     */
    public CmsTask readTask(int id) throws CmsException {

        return m_driverManager.readTask(id);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readTaskLogs(int)
     */
    public Vector readTaskLogs(int taskid) throws CmsException {

        return m_driverManager.readTaskLogs(taskid);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readTasksForProject(int, int, java.lang.String, java.lang.String)
     */
    public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        return m_driverManager.readTasksForProject(projectId, tasktype, orderBy, sort);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readTasksForRole(int, java.lang.String, int, java.lang.String, java.lang.String)
     */
    public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readTasksForRole(projectId, roleName, tasktype, orderBy, sort);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readTasksForUser(int, java.lang.String, int, java.lang.String, java.lang.String)
     */
    public Vector readTasksForUser(int projectId, String userName, int taskType, String orderBy, String sort)
    throws CmsException {

        return m_driverManager.readTasksForUser(projectId, userName, taskType, orderBy, sort);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readUser(I_CmsRuntimeInfo, org.opencms.util.CmsUUID)
     */
    public CmsUser readUser(CmsUUID id) throws CmsException {

        return m_driverManager.readUser(null, id);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readUser(I_CmsRuntimeInfo, java.lang.String)
     */
    public CmsUser readUser(String username) throws CmsException {

        return m_driverManager.readUser((I_CmsRuntimeInfo)null, username);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readUser(I_CmsRuntimeInfo, java.lang.String, int)
     */
    public CmsUser readUser(String username, int type) throws CmsException {

        return m_driverManager.readUser(null, username, type);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readUser(java.lang.String, java.lang.String)
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        return m_driverManager.readUser(username, password);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readWebUser(java.lang.String)
     */
    public CmsUser readWebUser(String username) throws CmsException {

        return m_driverManager.readWebUser(username);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#readWebUser(java.lang.String, java.lang.String)
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {

        return m_driverManager.readWebUser(username, password);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#reactivateTask(org.opencms.file.CmsRequestContext, int)
     */
    public void reactivateTask(CmsRequestContext context, int taskId) throws CmsException {

        m_driverManager.reactivateTask(context, taskId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#removeAccessControlEntry(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, org.opencms.util.CmsUUID)
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
     * @see org.opencms.db.CmsDriverManager#removeUserFromGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String)
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
     * @see org.opencms.db.CmsDriverManager#replaceResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, int, byte[], java.util.List)
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
     * @see org.opencms.db.CmsDriverManager#resetPassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void resetPassword(String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        m_driverManager.resetPassword(username, oldPassword, newPassword);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#restoreResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, int)
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
     * @see org.opencms.db.CmsDriverManager#setName(org.opencms.file.CmsRequestContext, int, java.lang.String)
     */
    public void setName(CmsRequestContext context, int taskId, String name) throws CmsException {

        m_driverManager.setName(context, taskId, name);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#setParentGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String)
     */
    public void setParentGroup(CmsRequestContext context, String groupName, String parentGroupName) throws CmsException {

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
     * @see org.opencms.db.CmsDriverManager#setPassword(org.opencms.file.CmsRequestContext, java.lang.String, java.lang.String)
     */
    public void setPassword(CmsRequestContext context, String username, String newPassword) throws CmsException {

        m_driverManager.setPassword(context, username, newPassword);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#setPriority(org.opencms.file.CmsRequestContext, int, int)
     */
    public void setPriority(CmsRequestContext context, int taskId, int priority) throws CmsException {

        m_driverManager.setPriority(context, taskId, priority);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#setTaskPar(int, java.lang.String, java.lang.String)
     */
    public void setTaskPar(int taskId, String parName, String parValue) throws CmsException {

        m_driverManager.setTaskPar(taskId, parName, parValue);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#setTimeout(org.opencms.file.CmsRequestContext, int, long)
     */
    public void setTimeout(CmsRequestContext context, int taskId, long timeout) throws CmsException {

        m_driverManager.setTimeout(context, taskId, timeout);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#touch(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, long, long, long)
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
     * @see org.opencms.db.CmsDriverManager#undoChanges(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.CmsDriverManager#unlockProject(org.opencms.file.CmsRequestContext, int)
     */
    public void unlockProject(CmsRequestContext context, int projectId) throws CmsException {

        m_driverManager.unlockProject(context, projectId);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#unlockResource(org.opencms.file.CmsRequestContext, org.opencms.file.CmsResource)
     */
    public void unlockResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        m_driverManager.unlockResource(context, resource);

    }

    /**
     * @see org.opencms.db.CmsDriverManager#userInGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, java.lang.String, java.lang.String)
     */
    public boolean userInGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        return m_driverManager.userInGroup(context, null, username, groupname);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#validateHtmlLinks(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, org.opencms.report.I_CmsReport)
     */
    public Map validateHtmlLinks(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {

        return m_driverManager.validateHtmlLinks(cms, publishList, report);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#validatePassword(java.lang.String)
     */
    public void validatePassword(String password) throws CmsSecurityException {

        m_driverManager.validatePassword(password);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#writeAccessControlEntry(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, org.opencms.security.CmsAccessControlEntry)
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
     * @see org.opencms.db.CmsDriverManager#writeFile(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsFile)
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
     * @see org.opencms.db.CmsDriverManager#writeGroup(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsGroup)
     */
    public void writeGroup(CmsRequestContext context, CmsGroup group) throws CmsException {

        if (isAdmin(context)) {

            I_CmsRuntimeInfo runtimeInfo = m_runtimeInfoFactory.getRuntimeInfo(
                m_driverManager,
                I_CmsRuntimeInfo.C_RUNTIMEINFO_USER);
            
            try {
                m_driverManager.writeGroup(context, runtimeInfo, group);
                runtimeInfo.pop();
            } catch (CmsException e) {
                runtimeInfo.report(null, "Error writing group " + group.getName(), e);
                throw e;
            } finally {
                runtimeInfo.clear();
            }
            
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeGroup() " + group.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
    }

    /**
     * @see org.opencms.db.CmsDriverManager#writePropertyObject(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
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
     * @see org.opencms.db.CmsDriverManager#writePropertyObjects(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource, java.util.List)
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
     * @see org.opencms.db.CmsDriverManager#writeResource(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.CmsDriverManager#writeStaticExportPublishedResource(org.opencms.file.CmsRequestContext, java.lang.String, int, java.lang.String, long)
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
     * @see org.opencms.db.CmsDriverManager#writeTaskLog(org.opencms.file.CmsRequestContext, int, java.lang.String)
     */
    public void writeTaskLog(CmsRequestContext context, int taskid, String comment) throws CmsException {

        m_driverManager.writeTaskLog(context, taskid, comment);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#writeTaskLog(org.opencms.file.CmsRequestContext, int, java.lang.String, int)
     */
    public void writeTaskLog(CmsRequestContext context, int taskId, String comment, int type) throws CmsException {

        m_driverManager.writeTaskLog(context, taskId, comment, type);
    }

    /**
     * @see org.opencms.db.CmsDriverManager#writeUser(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.file.CmsUser)
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeUser() " + user.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
    }

    /**
     * @see org.opencms.db.CmsDriverManager#writeWebUser(I_CmsRuntimeInfo, org.opencms.file.CmsUser)
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeWebUser() " + user.getName(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
       
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

}