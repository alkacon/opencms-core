/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2004/12/21 15:06:52 $
 * Version: $Revision: 1.464 $
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

package org.opencms.db;

import org.opencms.configuration.CmsConfigurationManager;

import org.opencms.file.*;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.validation.CmsHtmlLinkValidator;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.dbcp.PoolingDriver;

/**
 * The OpenCms driver manager.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.464 $ $Date: 2004/12/21 15:06:52 $
 * @since 5.1
 */
public final class CmsDriverManager extends Object implements I_CmsEventListener {

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     *
     * @author Alkexander Kandzior (a.kandzior@alkacon.com)
     */
    private class CacheId extends Object {

        /**
         * Name of the object.
         */
        public String m_name;

        /**
         * Id of the object.
         */
        public CmsUUID m_uuid;

        /**
         * Creates a new CacheId for a CmsGroup.<p>
         * 
         * @param group the group to create a cache id from
         */
        public CacheId(CmsGroup group) {

            m_name = group.getName();
            m_uuid = group.getId();
        }

        /**
         * Creates a new CacheId for a CmsResource.<p>
         * 
         * @param resource the resource to create a cache id from
         */
        public CacheId(CmsResource resource) {

            m_name = resource.getName();
            m_uuid = resource.getResourceId();
        }

        /**
         * Creates a new CacheId for a CmsUser.<p>
         * 
         * @param user the user to create a cache id from
         */
        public CacheId(CmsUser user) {

            m_name = user.getName() + user.getType();
            m_uuid = user.getId();
        }

        /**
         * Creates a new CacheId for a CmsUUID.<p>
         * 
         * @param uuid the uuid to create a cache id from
         */
        public CacheId(CmsUUID uuid) {

            m_uuid = uuid;
        }

        /**
         * Creates a new CacheId for a String.<p>
         * 
         * @param str the string to create a cache id from
         */
        public CacheId(String str) {

            m_name = str;
        }

        /**
         * Creates a new CacheId for a String and CmsUUID.<p>
         * 
         * @param name the string to create a cache id from
         * @param uuid the uuid to create a cache id from
         */
        public CacheId(String name, CmsUUID uuid) {

            m_name = name;
            m_uuid = uuid;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {

            if (o == null) {
                return false;
            }
            if (!(o instanceof CacheId)) {
                return false;
            }
            CacheId other = (CacheId)o;
            boolean result;
            if (m_uuid != null) {
                result = m_uuid.equals(other.m_uuid);
                if (result) {
                    return true;
                }
            }
            if (m_name != null) {
                result = m_name.equals(other.m_name);
                if (result) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {

            if (m_uuid == null) {
                return 509;
            } else {
                return m_uuid.hashCode();
            }
        }

    }

    /** Cache key for all properties. */
    public static final String C_CACHE_ALL_PROPERTIES = "_CAP_";

    /** Key for indicating no changes. */
    public static final int C_NOTHING_CHANGED = 0;

    /** Key to indicate complete update. */
    public static final int C_UPDATE_ALL = 3;

    /** Key to indicate update of resource record. */
    public static final int C_UPDATE_RESOURCE = 4;

    /** Key to indicate update of resource state. */
    public static final int C_UPDATE_RESOURCE_STATE = 1;

    /** Key to indicate update of structure record. */
    public static final int C_UPDATE_STRUCTURE = 5;

    /** Key to indicate update of structure state. */
    public static final int C_UPDATE_STRUCTURE_STATE = 2;

    /** Separator for user cache. */
    private static final char C_USER_CACHE_SEP = '\u0000';

    /** Cache for access control lists. */
    private Map m_accessControlListCache;

    /** The backup driver. */
    private I_CmsBackupDriver m_backupDriver;

    /** The configuration of the property-file. */
    private ExtendedProperties m_configuration;

    /** The list of initialized JDBC pools. */
    private List m_connectionPools;

    /** Cache for groups. */
    private Map m_groupCache;

    /** The HTML link validator. */
    private CmsHtmlLinkValidator m_htmlLinkValidator;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /** The lock manager. */
    private CmsLockManager m_lockManager = OpenCms.getLockManager();

    /** Cache for offline projects. */
    private Map m_projectCache;

    /** The project driver. */
    private I_CmsProjectDriver m_projectDriver;

    /** Cache for properties. */
    private Map m_propertyCache;

    /** Cache for resources. */
    private Map m_resourceCache;

    /** Cache for resource lists. */
    private Map m_resourceListCache;

    /** The security manager (for access checks). */
    private CmsSecurityManager m_securityManager;

    /** Cache for user data. */
    private Map m_userCache;

    /** The user driver. */
    private I_CmsUserDriver m_userDriver;

    /** Cache for user groups. */
    private Map m_userGroupsCache;

    /** The VFS driver. */
    private I_CmsVfsDriver m_vfsDriver;

    /** The workflow driver. */
    private I_CmsWorkflowDriver m_workflowDriver;

    /**
     * Private constructor, initializes some required member variables.<p> 
     */
    private CmsDriverManager() {

        m_connectionPools = new ArrayList();
    }
    
    /**
     * Reads the required configurations from the opencms.properties file and creates
     * the various drivers to access the cms resources.<p>
     * 
     * The initialization process of the driver manager and its drivers is split into
     * the following phases:
     * <ul>
     * <li>the database pool configuration is read</li>
     * <li>a plain and empty driver manager instance is created</li>
     * <li>an instance of each driver is created</li>
     * <li>the driver manager is passed to each driver during initialization</li>
     * <li>finally, the driver instances are passed to the driver manager during initialization</li>
     * </ul>
     * 
     * @param configurationManager the configuration manager
     * @param securityManager the security manager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * 
     * @return CmsDriverManager the instanciated driver manager.
     * @throws CmsException if the driver manager couldn't be instanciated.
     */
    public static CmsDriverManager newInstance(
        CmsConfigurationManager configurationManager,
        CmsSecurityManager securityManager,
        I_CmsDbContextFactory runtimeInfoFactory) throws CmsException {

        ExtendedProperties configuration = configurationManager.getConfiguration();

        // initialize static hastables
        CmsDbUtil.init();

        List drivers = null;
        String driverName = null;

        I_CmsVfsDriver vfsDriver = null;
        I_CmsUserDriver userDriver = null;
        I_CmsProjectDriver projectDriver = null;
        I_CmsWorkflowDriver workflowDriver = null;
        I_CmsBackupDriver backupDriver = null;

        CmsDriverManager driverManager = null;
        try {
            // create a driver manager instance
            driverManager = new CmsDriverManager();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 1 - initializing database");
            }
            if ((runtimeInfoFactory == null) && OpenCms.getLog(CmsDriverManager.class).isDebugEnabled()) {
                OpenCms.getLog(CmsDriverManager.class).debug(
                    ". Driver manager init  : optional runtime info factory not available");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        // set the security manager
        driverManager.m_securityManager = securityManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 2 - initializing pools");
        }

        // read the pool names to initialize
        String driverPoolNames[] = configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_DB + ".pools");
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            String names = "";
            for (int p = 0; p < driverPoolNames.length; p++) {
                names += driverPoolNames[p] + " ";
            }
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Resource pools       : " + names);
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(configuration, driverPoolNames[p]);
        }
        
        // initialize the runtime info factory with the generated driver manager
        if (runtimeInfoFactory != null) {
            runtimeInfoFactory.initialize(driverManager);
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 3 - initializing drivers");
        }

        // read the vfs driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_VFS));
        driverName = configuration.getString((String)drivers.get(0) + ".vfs.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        vfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the user driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_USER));
        driverName = configuration.getString((String)drivers.get(0) + ".user.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the project driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_PROJECT));
        driverName = configuration.getString((String)drivers.get(0) + ".project.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        projectDriver = (I_CmsProjectDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the workflow driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_WORKFLOW));
        driverName = configuration.getString((String)drivers.get(0) + ".workflow.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        workflowDriver = (I_CmsWorkflowDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the backup driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_BACKUP));
        driverName = configuration.getString((String)drivers.get(0) + ".backup.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        backupDriver = (I_CmsBackupDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        try {
            // invoke the init method of the driver manager
            driverManager.init(configuration, vfsDriver, userDriver, projectDriver, workflowDriver, backupDriver);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 4 ok - finished");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(message, exc);
            }

            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        // register the driver manager for required events
        org.opencms.main.OpenCms.addCmsEventListener(driverManager, new int[] {
            I_CmsEventListener.EVENT_UPDATE_EXPORTS,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        // return the configured driver manager
        return driverManager;
    }

    /**
     * Updates the state of the given task as accepted by the current user.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task to accept.
     *
     * @throws CmsException if something goes wrong.
     */
    public void acceptTask(CmsDbContext dbc, int taskId) throws CmsException {

        CmsTask task = m_workflowDriver.readTask(dbc, taskId);
        task.setPercentage(1);
        task = m_workflowDriver.writeTask(dbc, task);
        m_workflowDriver.writeSystemTaskLog(dbc, taskId, "Task was accepted from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + ".");
    }

    /**
     * Tests if the user can access the project.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @param projectId the id of the project
     * @return true, if the user has access, else returns false
     * @throws CmsException if something goes wrong
     */
    public boolean accessProject(CmsDbContext dbc, int projectId) throws CmsException {

        CmsProject testProject = readProject(dbc, projectId);

        if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return true;
        }

        // is the project unlocked?
        if (testProject.getFlags() != I_CmsConstants.C_PROJECT_STATE_UNLOCKED
            && testProject.getFlags() != I_CmsConstants.C_PROJECT_STATE_INVISIBLE) {
            return (false);
        }

        // is the current-user admin, or the owner of the project?
        if ((dbc.currentProject().getOwnerId().equals(dbc.currentUser().getId())) || isAdmin(dbc)) {
            return (true);
        }

        // get all groups of the user
        List groups = getGroupsOfUser(dbc, dbc.currentUser().getName());

        // test, if the user is in the same groups like the project.
        for (int i = 0; i < groups.size(); i++) {
            CmsUUID groupId = ((CmsGroup)groups.get(i)).getId();
            if ((groupId.equals(testProject.getGroupId())) || (groupId.equals(testProject.getManagerGroupId()))) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the user.
     * @param name the new name for the user.
     * @param password the new password for the user.
     * @param description the description for the user.
     * @param firstname the firstname of the user.
     * @param lastname the lastname of the user.
     * @param email the email of the user.
     * @param flags the flags for a user (e.g. <code>{@link I_CmsConstants#C_FLAG_ENABLED}</code>).
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user. These
     *                      infos may be stored into the Usertables (depending on the implementation).
     * @param address the address of the user.
     * @param type the type of the user.
     *
     * @return a new <code>{@link CmsUser}</code> object representing the added user.
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsUser addImportUser(
        CmsDbContext dbc,
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

        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);

        CmsUser newUser = m_userDriver.importUser(
            dbc,
            new CmsUUID(id),
            name,
            password,
            description,
            firstname,
            lastname,
            email,
            0,
            flags,
            additionalInfos,
            address,
            type,
            null);
        return newUser;
    }

    /**
     * Creates a new user.<p>
     *
     * @param dbc the current database context.
     * @param name the new name for the user.
     * @param password the new password for the user.
     * @param group the default groupname for the user.
     * @param description the description for the user.
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation).
     *
     * @return the new user will be returned.
     * 
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addUser(
        CmsDbContext dbc,
        String name,
        String password,
        String group,
        String description,
        Map additionalInfos) throws CmsException {

        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);

        if (name.length() > 0) {

            CmsGroup defaultGroup = readGroup(dbc, group);
            CmsUser newUser = m_userDriver.createUser(
                dbc,
                name,
                password,
                description,
                " ",
                " ",
                " ",
                0,
                I_CmsConstants.C_FLAG_ENABLED,
                additionalInfos,
                " ",
                I_CmsConstants.C_USER_TYPE_SYSTEMUSER);

            addUserToGroup(dbc, newUser.getName(), defaultGroup.getName());

            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }
    }

    /**
     * Adds a user to a group.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user that is to be added to the group.
     * @param groupname the name of the group.
     *
     * @throws CmsException if operation was not succesfull.
     */
    public void addUserToGroup(
        CmsDbContext dbc,
        String username,
        String groupname) throws CmsException {

        if (!userInGroup(dbc, username, groupname)) {
            // Check the security
            if (isAdmin(dbc)) {
                CmsUser user;
                CmsGroup group;
                try {
                    user = readUser(dbc, username);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_NO_USER) {
                        user = readWebUser(dbc, username);
                    } else {
                        throw e;
                    }
                }
                //check if the user exists
                if (user != null) {
                    group = readGroup(dbc, groupname);
                    //check if group exists
                    if (group != null) {
                        //add this user to the group
                        m_userDriver.createUserInGroup(dbc, user.getId(), group.getId(), null);
                        // update the cache
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
                    }
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + username, CmsException.C_NO_USER);
                }
            } else {
                throw new CmsSecurityException("["
                    + this.getClass().getName()
                    + "] addUserToGroup() "
                    + username
                    + " "
                    + groupname, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }
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
     * @param dbc the current database context.
     * @param name the new name for the user.
     * @param password the new password for the user.
     * @param group the default groupname for the user.
     * @param description the description for the user.
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user.
     *        Infos may be stored into the Usertables (depending on the implementation).
     *
     * @return the new user will be returned.
     * 
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(
        CmsDbContext dbc,
        String name,
        String password,
        String group,
        String description,
        Map additionalInfos) throws CmsException {

        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);

        if ((name.length() > 0)) {
            CmsUser newUser = m_userDriver.createUser(
                dbc,
                name,
                password,
                description,
                " ",
                " ",
                " ",
                0,
                I_CmsConstants.C_FLAG_ENABLED,
                additionalInfos,
                " ",
                I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;

            user = m_userDriver.readUser(dbc, newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);

            //check if the user exists
            if (user != null) {
                usergroup = readGroup(dbc, group);
                //check if group exists
                if (usergroup != null) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(dbc, user.getId(), usergroup.getId(), null);
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
                }
            } else {
                throw new CmsException("[" + getClass().getName() + "]" + name, CmsException.C_NO_USER);
            }

            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }

    }

    /**
     * Adds a web user to the Cms.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param dbc the current database context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param additionalGroup an additional group for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     *
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(
        CmsDbContext dbc,
        String name,
        String password,
        String group,
        String additionalGroup,
        String description,
        Map additionalInfos) throws CmsException {

        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);

        if ((name.length() > 0)) {
            CmsUser newUser = m_userDriver.createUser(
                dbc,
                name,
                password,
                description,
                " ",
                " ",
                " ",
                0,
                I_CmsConstants.C_FLAG_ENABLED,
                additionalInfos,
                " ",
                I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;
            CmsGroup addGroup;

            user = m_userDriver.readUser(dbc, newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);
            //check if the user exists
            if (user != null) {
                usergroup = readGroup(dbc, group);
                //check if group exists
                if (usergroup != null && isWebgroup(dbc, usergroup)) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(dbc, user.getId(), usergroup.getId(), null);
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
                }
                // if an additional groupname is given and the group does not belong to
                // Users, Administrators or Projectmanager add the user to this group
                if (additionalGroup != null && !"".equals(additionalGroup)) {
                    addGroup = readGroup(dbc, additionalGroup);
                    if (addGroup != null && isWebgroup(dbc, addGroup)) {
                        //add this user to the group
                        m_userDriver.createUserInGroup(dbc, user.getId(), addGroup.getId(), null);
                        // update the cache
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException(
                            "[" + getClass().getName() + "]" + additionalGroup,
                            CmsException.C_NO_GROUP);
                    }
                }
            } else {
                throw new CmsException("[" + getClass().getName() + "]" + name, CmsException.C_NO_USER);
            }
            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }
    }

    /**
     * Creates a backup of the published project.<p>
     *
     * @param dbc the current database context
     * @param backupProject the project to be backuped
     * @param tagId the version of the backup
     * @param publishDate the date of publishing
     * @throws CmsException if operation was not succesful
     */
    public void backupProject(CmsDbContext dbc, CmsProject backupProject, int tagId, long publishDate)
    throws CmsException {

        m_backupDriver.writeBackupProject(dbc, backupProject, tagId, publishDate, dbc.currentUser());
    }

    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * @param dbc the current database context
     * @param resource theresource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see I_CmsResourceType#changeLastModifiedProjectId(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLastModifiedProjectId(
        CmsDbContext dbc,
        CmsResource resource) throws CmsException {

        // update the project id of a modified resource as "modified inside the current project"
        m_vfsDriver.writeLastModifiedProjectId(
            dbc,
            dbc.currentProject(),
            dbc.currentProject().getId(),
            resource);

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap(
            "resource",
            resource)));
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to change the lock for
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLock(String)
     * @see I_CmsResourceType#changeLock(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLock(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        // Stealing a lock: checking permissions will throw an exception because the
        // resource is still locked for the other user. Therefore the resource is unlocked
        // before the permissions of the new user are checked. If the new user 
        // has insufficient permissions, the previous lock is restored later.

        // save the lock of the resource itself
        if (getLock(dbc, resource).isNullLock()) {
            throw new CmsLockException(
                "Unable to change lock on unlocked resource " + resource.getRootPath(),
                CmsLockException.C_RESOURCE_UNLOCKED);
        }

        // save the lock of the resource's exclusive locked sibling
        CmsLock exclusiveLock = m_lockManager.getExclusiveLockedSibling(this, dbc, resource);

        // remove the lock
        m_lockManager.removeResource(this, dbc, resource, true);

        // clear permission cache so the change is detected
        m_securityManager.clearPermissionCache();

        try {
            // try to lock the resource
            lockResource(dbc, resource, CmsLock.C_MODE_COMMON);
        } catch (CmsSecurityException e) {
            // restore the lock of the exclusive locked sibling in case a lock gets stolen by 
            // a new user with insufficient permissions on the resource
            m_lockManager.addResource(
                this, 
                dbc, 
                resource, 
                exclusiveLock.getUserId(), 
                exclusiveLock.getProjectId(), 
                CmsLock.C_MODE_COMMON);
            throw e;
        }
    }

    /**
     * Changes the user type of the user.<p>
     * 
     * @param dbc the current database context.
     * @param user the user to change.
     * @param userType the new usertype of the user.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void changeUserType(CmsDbContext dbc, CmsUser user, int userType) throws CmsException {

        // try to remove user from cache
        clearUserCache(user);
        m_userDriver.writeUserType(dbc, user.getId(), userType);
    }

    /**
     * Changes the user type of the user.<p>
     * 
     * @param dbc the current database context.
     * @param userId the id of the user to change.
     * @param userType the new usertype of the user.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void changeUserType(CmsDbContext dbc, CmsUUID userId, int userType) throws CmsException {

        CmsUser theUser = m_userDriver.readUser(dbc, userId);
        changeUserType(dbc, theUser, userType);
    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user to change
     * @param userType the new usertype of the user
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsDbContext dbc, String username, int userType) throws CmsException {

        CmsUser theUser = null;
        try {
            // try to read the webuser
            theUser = readWebUser(dbc, username);
        } catch (CmsException e) {
            // try to read the systemuser
            if (e.getType() == CmsException.C_NO_USER) {
                theUser = readUser(dbc, username);
            } else {
                throw e;
            }
        }
        changeUserType(dbc, theUser, userType);
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chflags(String, int)
     * @see I_CmsResourceType#chflags(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsDbContext dbc, CmsResource resource, int flags)
    throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        clone.setFlags(flags);
        writeResource(dbc, clone);
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
     * @param dbc the current database context
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see I_CmsResourceType#chtype(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chtype(CmsDbContext dbc, CmsResource resource, int type)
    throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        I_CmsResourceType newType = OpenCms.getResourceManager().getResourceType(type);
        clone.setType(newType.getTypeId());
        writeResource(dbc, clone);
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        if (org.opencms.main.OpenCms.getLog(this).isDebugEnabled()) {
            org.opencms.main.OpenCms.getLog(this).debug("Handling event: " + event.getType());
        }

        I_CmsReport report;
        CmsDbContext dbc;
        
        switch (event.getType()) {

            case I_CmsEventListener.EVENT_UPDATE_EXPORTS:
                report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                updateExportPoints(dbc, report);
                break;

            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                int projectId = ((Integer)event.getData().get(I_CmsEventListener.KEY_PROJECTID)).intValue();
                writeExportPoints(dbc, projectId, report, publishHistoryId);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearcache();
                break;
            default:
        // noop
        }
    }

    /**
     * Copies the access control entries of a given resource to a destination resorce.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param dbc the current database context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(
        CmsDbContext dbc,
        CmsResource source,
        CmsResource destination) throws CmsException {

        // get the entries to copy
        ListIterator aceList = m_userDriver.readAccessControlEntries(
            dbc,
            dbc.currentProject(),
            source.getResourceId(),
            false).listIterator();

        // remove the current entries from the destination
        m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), destination.getResourceId());

        // now write the new entries
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
            m_userDriver.createAccessControlEntry(
                dbc,
                dbc.currentProject(),
                destination.getResourceId(),
                ace.getPrincipal(),
                ace.getPermissions().getAllowedPermissions(),
                ace.getPermissions().getDeniedPermissions(),
                ace.getFlags());
        }

        // update the "last modified" information
        touch(
            dbc,
            destination,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED);

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap(
            "resource",
            destination)));
    }

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is anabsolute, vaild and
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
     * @param dbc the current database context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see I_CmsResourceType#copyResource(CmsObject, CmsSecurityManager, CmsResource, String, int)
     */
    public void copyResource(
        CmsDbContext dbc,
        CmsResource source,
        String destination,
        int siblingMode) throws CmsException {

        // check the sibling mode to see if this resource has to be copied as a sibling
        boolean copyAsSibling = false;

        // siblings of folders are not supported
        if (!source.isFolder()) {
            // if the "copy as sibling" mode is used, set the flag to true
            if (siblingMode == I_CmsConstants.C_COPY_AS_SIBLING) {
                copyAsSibling = true;
            }
            // if the mode is "preserve siblings", we have to check the sibling counter
            if (siblingMode == I_CmsConstants.C_COPY_PRESERVE_SIBLING) {
                if (source.getSiblingCount() > 1) {
                    copyAsSibling = true;
                }
            }
        }

        // read the source properties
        List properties = readPropertyObjects(dbc, source, false);

        if (copyAsSibling) {
            // create a sibling of the source file at the destination  
            createSibling(dbc, source, destination, properties);
            // after the sibling is created the copy operation is finished
            return;
        }

        // prepare the content if required
        byte[] content = null;
        if (source.isFile()) {
            CmsFile file;
            if (source instanceof CmsFile) {
                // resource already is a file
                file = (CmsFile)source;
                content = file.getContents();
            }
            if ((content == null) || (content.length < 1)) {
                // no known content yet - read from database
                file = m_vfsDriver.readFile(dbc, dbc.currentProject().getId(), false, source.getStructureId());
                content = file.getContents();
            }
        }

        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);
        String destinationResourceName = destination.substring(destinationFoldername.length());

        if (CmsResource.isFolder(destinationResourceName)) {
            // must cut of trailing '/' on destination folders
            destinationResourceName = destinationResourceName.substring(0, destinationResourceName.length() - 1);
        }

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_securityManager.readFolder(
            dbc,
            destinationFoldername,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // set user and creation timestamps
        long currentTime = System.currentTimeMillis();

        // check the resource flags
        int flags = source.getFlags();
        if (source.isLabeled()) {
            // reset "labeled" link flag for new resource
            flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            new CmsUUID(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getId(),
            I_CmsConstants.C_STATE_NEW,
            currentTime,
            dbc.currentUser().getId(),
            source.getDateCreated(),
            source.getUserLastModified(),
            source.getDateReleased(),
            source.getDateExpired(),
            1,
            source.getLength());

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(source.getDateLastModified());

        // create the resource
        newResource = createResource(dbc, destination, newResource, content, properties, false);

        // copy the access control entries to the created resource
        copyAccessControlEntries(dbc, source, newResource);

        // clear the cache
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_COPIED, Collections.singletonMap(
            "resources",
            modifiedResources)));

    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // is the current project an "offline" project?
        // and is the current user the manager of the project?
        // and is the current project state UNLOCKED?
        if ((!dbc.currentProject().isOnlineProject())
            && (isManagerOfProject(dbc))
            && (dbc.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // copy the resource to the project only if the resource is not already in the project
            if (!isInsideCurrentProject(dbc, resource.getRootPath())) {
                // check if there are already any subfolders of this resource
                if (resource.isFolder()) {
                    List projectResources = m_projectDriver.readProjectResources(dbc, dbc.currentProject());
                    for (int i = 0; i < projectResources.size(); i++) {
                        String resname = (String)projectResources.get(i);
                        if (resname.startsWith(resource.getRootPath())) {
                            // delete the existing project resource first
                            m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getId(), resname);
                        }
                    }
                }
                try {
                    m_projectDriver.createProjectResource(
                        dbc,
                        dbc.currentProject().getId(),
                        resource.getRootPath(),
                        null);
                } catch (CmsException exc) {
                    // if the subfolder exists already - all is ok
                } finally {
                    OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections
                        .singletonMap("project", dbc.currentProject())));
                }
            }
        } else {
            // no changes on the onlineproject!
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] " + dbc.currentProject().getName(),
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }    

    /**
     * Counts the locked resources in this project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param dbc the current database context
     * @param id the id of the project
     * @return the amount of locked resources in this project.
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsDbContext dbc, int id) throws CmsException {

        // read the project.
        CmsProject project = readProject(dbc, id);
        // check the security
        if (isAdmin(dbc)
            || isManagerOfProject(dbc)
            || (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // count locks
            return m_lockManager.countExclusiveLocksInProject(project);
        } else if (!isAdmin(dbc) && !isManagerOfProject(dbc)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] countLockedResources()",
                CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] countLockedResources()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Counts the locked resources in a given folder.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param dbc the current database context
     * @param foldername the folder to search in
     * @return the amount of locked resources in this project
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsDbContext dbc, String foldername) throws CmsException {

        // check the security
        if (isAdmin(dbc)
            || isManagerOfProject(dbc)
            || (dbc.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // count locks
            return m_lockManager.countExclusiveLocksInFolder(foldername);
        } else if (!isAdmin(dbc) && !isManagerOfProject(dbc)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] countLockedResources()",
                CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] countLockedResources()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the new group
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * 
     * @return new created group
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(
        CmsDbContext dbc,
        CmsUUID id,
        String name,
        String description,
        int flags,
        String parent) throws CmsException {

        name = name.trim();
        validFilename(name);

        // check the lenght of the groupname
        if (name.length() > 1) {
            return m_userDriver.createGroup(dbc, id, name, description, flags, parent, null);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }

    }

    /**
     * Creates a new user group.<p>
     *
     * @param dbc the current database context.
     * @param name the name of the new group.
     * @param description the description for the new group.
     * @param flags the flags for the new group.
     * @param parent the name of the parent group (or <code>null</code>).
     * 
     * @return a <code>{@link CmsGroup}</code> object representing the newly created group.
     * 
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(CmsDbContext dbc, String name, String description, int flags, String parent)
    throws CmsException {

        return createGroup(dbc, new CmsUUID(), name, description, flags, parent);
    }

    /**
     * Creates a new project for task handling.<p>
     *
     * @param dbc the current database context
     * @param projectName name of the project
     * @param roleName usergroup for the project
     * @param timeout time when the Project must finished
     * @param priority priority for the Project
     * @return The new task project
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask createProject(
        CmsDbContext dbc,
        String projectName,
        String roleName,
        long timeout,
        int priority) throws CmsException {

        CmsGroup role = null;

        // read the role
        if (roleName != null && !roleName.equals("")) {
            role = readGroup(dbc, roleName);
        }
        // create the timestamp
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        return m_workflowDriver.createTask(
            dbc,
            0,
            0,
            1, // standart project type,
            dbc.currentUser().getId(),
            dbc.currentUser().getId(),
            role.getId(),
            projectName,
            now,
            timestamp,
            priority);
    }

    /**
     * Creates a project.<p>
     *
     * Only the users which are in the admin or projectmanager groups are granted.<p>
     *
     * @param dbc the current database context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype type the type of the project
     * @return the created project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        String name,
        String description,
        String groupname,
        String managergroupname,
        int projecttype) throws CmsException {

        if (isAdmin(dbc) || isProjectManager(dbc)) {
            if (I_CmsConstants.C_PROJECT_ONLINE.equals(name)) {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
            // read the needed groups from the cms
            CmsGroup group = readGroup(dbc, groupname);
            CmsGroup managergroup = readGroup(dbc, managergroupname);

            // create a new task for the project
            CmsTask task = createProject(
                dbc,
                name,
                group.getName(),
                System.currentTimeMillis(),
                I_CmsConstants.C_TASK_PRIORITY_NORMAL);

            return m_projectDriver.createProject(
                dbc,
                dbc.currentUser(),
                group,
                managergroup,
                task,
                name,
                description,
                I_CmsConstants.C_PROJECT_STATE_UNLOCKED,
                projecttype,
                null);
            
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createProject()",
                CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition createPropertydefinition(
        CmsDbContext dbc,
        String name) throws CmsException {

        CmsPropertydefinition propertyDefinition = null;

        name = name.trim();
        validFilename(name);

        try {
            try {
                propertyDefinition = m_vfsDriver.readPropertyDefinition(
                    dbc,
                    name,
                    dbc.currentProject().getId());
            } catch (CmsException e) {
                propertyDefinition = m_vfsDriver.createPropertyDefinition(
                    dbc,
                    dbc.currentProject().getId(),
                    name);
            }
    
            try {
                m_vfsDriver.readPropertyDefinition(dbc, name, I_CmsConstants.C_PROJECT_ONLINE_ID);
            } catch (CmsException e) {
                m_vfsDriver.createPropertyDefinition(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID, name);
            }
    
            try {
                m_backupDriver.readBackupPropertyDefinition(dbc, name);
            } catch (CmsException e) {
                m_backupDriver.createBackupPropertyDefinition(dbc, name);
            }
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_PROPERTY_DEFINITION_CREATED, 
                    Collections.singletonMap("propertyDefinition", propertyDefinition)));
            
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
     * @param dbc the current database context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @param importCase if true, signals that this operation is done while importing resource,
     *      causing different lock behaviour and potential "lost and found" usage
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(
        CmsDbContext dbc,
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List properties,
        boolean importCase) throws CmsException {

        CmsResource newResource = null;

        try {
            // check import configuration of "lost and found" folder
            boolean useLostAndFound = importCase && !OpenCms.getImportExportManager().overwriteCollidingResources();

            // check if the resource already exists
            CmsResource currentResource = null;

            try {
                currentResource = readResource(dbc, resourcePath, CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, we need to either overwrite it,
                // or create a sibling - this will be handled later
            }

            CmsResource parentFolder;
            String parentFolderName;
            String createdResourceName = resourcePath;
            int contentLength;

            if (currentResource != null) {
                if (currentResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                    if (!currentResource.isFolder()) {
                        // if a non-folder resource was deleted it's treated like a new resource
                        currentResource = null;
                    }
                } else {
                    if (!importCase) {
                        // direct "overwrite" of a resource is possible only during import, 
                        // or if the resource has been deleted
                        throw new CmsVfsException("Resource '"
                            + dbc.removeSiteRoot(resourcePath)
                            + "' already exists", CmsVfsException.C_VFS_RESOURCE_ALREADY_EXISTS);
                    }
                    // the resource already exists
                    if (!resource.isFolder()
                        && useLostAndFound
                        && (!currentResource.getResourceId().equals(resource.getResourceId()))) {
                        // new resource must be created in "lost and found"                
                        createdResourceName = moveToLostAndFound(dbc, resourcePath, false);
                        // current resource must remain unchanged, new will be created in "lost and found"
                        currentResource = null;
                    }
                }
            }

            // need to provide the parent folder id for resource creation
            parentFolderName = CmsResource.getParentFolder(createdResourceName);
            parentFolder = readFolder(dbc, parentFolderName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the permissions
            if (currentResource == null) {
                // resource does not exist - check parent folder
                m_securityManager.checkPermissions(
                    dbc,
                    parentFolder,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION);
            } else {
                // resource already exists - check existing resource              
                m_securityManager.checkPermissions(
                    dbc,
                    currentResource,
                    CmsPermissionSet.ACCESS_WRITE,
                    !importCase,
                    CmsResourceFilter.ALL);
            }

            // extract the name (without path)
            String targetName = CmsResource.getName(createdResourceName);

            // modify target name and content length in case of folder creation
            if (resource.isFolder()) {
                // folders never have any content
                contentLength = -1;
                // must cut of trailing '/' for folder creation (or name check fails)
                if (targetName.charAt(targetName.length() - 1) == '/') {
                    targetName = targetName.substring(0, targetName.length() - 1);
                }
            } else {
                // otherwise ensure content and content length are set correctly
                if (content != null) {
                    // if a content is provided, in each case the length is the length of this content
                    contentLength = content.length;
                } else if (currentResource != null) {
                    // we have no content, but an already existing resource - length remains unchanged
                    contentLength = currentResource.getLength();
                } else {
                    // we have no content - length is used as set in the resource
                    contentLength = resource.getLength();
                }
            }

            // check if the target name is valid (forbitten chars etc.), 
            // if not throw an exception
            // must do this here since targetName is modified in folder case (see above)
            validFilename(targetName);

            // set strcuture and resource ids
            CmsUUID structureId;
            CmsUUID resourceId;
            if (currentResource != null) {
                // resource exists, re-use existing ids
                structureId = currentResource.getStructureId();
                resourceId = currentResource.getResourceId();
            } else {
                // new resoruce always get a new structure id
                structureId = new CmsUUID();
                if (!resource.getResourceId().isNullUUID()) {
                    // re-use existing resource id 
                    resourceId = resource.getResourceId();
                } else {
                    // need a new resource id
                    resourceId = new CmsUUID();
                }
            }

            // now create a resource object will all informations
            newResource = new CmsResource(
                structureId,
                resourceId,
                createdResourceName,
                resource.getTypeId(),
                resource.isFolder(),
                resource.getFlags(),
                dbc.currentProject().getId(),
                resource.getState(),
                resource.getDateCreated(),
                resource.getUserCreated(),
                resource.getDateLastModified(),
                resource.getUserLastModified(),
                resource.getDateReleased(),                
                resource.getDateExpired(),
                1, 
                contentLength);
            
            // ensure date is updated only if required
            if (resource.isTouched()) {
                // this will trigger the internal "is touched" state on the new resource
                newResource.setDateLastModified(resource.getDateLastModified());
            }

            if (resource.isFile()) {
                // check if a sibling to the imported resource lies in a marked site
                if (labelResource(dbc, resource, resourcePath, 2)) {
                    int flags = resource.getFlags();
                    flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    resource.setFlags(flags);
                }
                // ensure siblings don't overwrite existing resource records
                if (content == null) {
                    newResource.setState(I_CmsConstants.C_STATE_KEEP);
                }
            }

            if (currentResource == null) {
                
                // resource does not exist.
                newResource = m_vfsDriver.createResource(
                    dbc, 
                    dbc.currentProject(), 
                    newResource, 
                    content); 
                
            } else {
                
                // resource already exists. 
                // probably the resource is a merged page file that gets overwritten during import, or it gets 
                // overwritten by a copy operation. if so, the structure & resource state are not modified to changed.
                int updateStates = (currentResource.getState() == I_CmsConstants.C_STATE_NEW) ? CmsDriverManager.C_NOTHING_CHANGED : CmsDriverManager.C_UPDATE_ALL;
                m_vfsDriver.writeResource(
                    dbc,
                    dbc.currentProject(), 
                    newResource, 
                    updateStates);
                
                if ((content != null) && resource.isFile()) {
                    // also update file content if required
                    m_vfsDriver.writeContent(
                        dbc,
                        dbc.currentProject(),
                        currentResource.getResourceId(),
                        content);
                }
                
            }

            // write the properties (internal operation, no events or duplicate permission checks)
            writePropertyObjects(dbc, newResource, properties);

            // lock the created resource (internal operation, no events or duplicate permission checks)
            lockResource(dbc, newResource, CmsLock.C_MODE_COMMON);

        } finally {

            // clear the internal caches
            clearAccessControlListCache();
            m_propertyCache.clear();

            if (newResource != null) {
                // fire an event that a new resource has been created
                OpenCms.fireCmsEvent(
                    new CmsEvent(
                        I_CmsEventListener.EVENT_RESOURCE_CREATED, 
                        Collections.singletonMap("resource", newResource)));
            }
        }

        return newResource;
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * If the provided content is null and the resource is not a folder,
     * the content will be set to an empty byte array.<p>  
     * 
     * @param dbc the current database context
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see I_CmsResourceType#createResource(CmsObject, CmsSecurityManager, String, byte[], List)
     */
    public CmsResource createResource(
        CmsDbContext dbc,
        String resourcename,
        int type,
        byte[] content,
        List properties) throws CmsException {

        String targetName = resourcename;

        if (content == null) {
            // name based resource creation MUST have a content
            content = new byte[0];
        }
        int size;

        if (CmsFolder.isFolderType(type)) {
            // must cut of trailing '/' for folder creation
            if (targetName.charAt(targetName.length() - 1) == '/') {
                targetName = targetName.substring(0, targetName.length() - 1);
            }
            size = -1;
        } else {
            size = content.length;
        }

        // create a new resource
        CmsResource newResource = new CmsResource(
            CmsUUID.getNullUUID(), // uuids will be "corrected" later
            CmsUUID.getNullUUID(),
            targetName,
            type,
            CmsFolder.isFolderType(type),
            0,
            dbc.currentProject().getId(),
            I_CmsConstants.C_STATE_NEW,
            0,
            dbc.currentUser().getId(),
            0,
            dbc.currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            size);

        return createResource(dbc, targetName, newResource, content, properties, false);
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param dbc the current database context
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see I_CmsResourceType#createSibling(CmsObject, CmsSecurityManager, CmsResource, String, List)
     */
    public void createSibling(
        CmsDbContext dbc,
        CmsResource source,
        String destination,
        List properties) throws CmsException {

        if (source.isFolder()) {
            throw new CmsVfsException(CmsVfsException.C_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS);
        }

        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = readFolder(
            dbc,
            destinationFoldername,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // check the resource flags
        int flags = source.getFlags();
        if (labelResource(dbc, source, destination, 1)) {
            // set "labeled" link flag for new resource
            flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            source.getResourceId(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getId(),
            I_CmsConstants.C_STATE_KEEP,
            source.getDateCreated(), // ensures current resource record remains untouched 
            source.getUserCreated(),
            source.getDateLastModified(),
            source.getUserLastModified(),
            source.getDateReleased(),
            source.getDateExpired(),
            source.getSiblingCount() + 1,
            source.getLength());

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(newResource.getDateLastModified());

        // create the resource (null content signals creation of sibling)
        newResource = createResource(dbc, destination, newResource, null, properties, false);

        // clear the caches
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED, Collections
            .singletonMap("resources", modifiedResources)));
    }

    /**
     * Creates a new task.<p>
     *
     * @param dbc the current database context.
     * @param currentUser the current user.
     * @param projectid the current project id.
     * @param agentName user who will edit the task.
     * @param roleName usergroup for the task.
     * @param taskName name of the task.
     * @param taskType type of the task.
     * @param taskComment description of the task.
     * @param timeout time when the task must finished.
     * @param priority Id for the priority.
     * 
     * @return a new task object.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(
        CmsDbContext dbc,
        CmsUser currentUser,
        int projectid,
        String agentName,
        String roleName,
        String taskName,
        String taskComment,
        int taskType,
        long timeout,
        int priority) throws CmsException {

        CmsUser agent = readUser(dbc, agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsGroup role = m_userDriver.readGroup(dbc, roleName);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        validTaskname(taskName); // check for valid Filename

        CmsTask task = m_workflowDriver.createTask(
            dbc,
            projectid,
            projectid,
            taskType,
            currentUser.getId(),
            agent.getId(),
            role.getId(),
            taskName,
            now,
            timestamp,
            priority);
        
        if (taskComment != null && !taskComment.equals("")) {
            m_workflowDriver.writeTaskLog(
                dbc,
                task.getId(), 
                currentUser.getId(), 
                new java.sql.Timestamp(System.currentTimeMillis()), 
                taskComment, 
                I_CmsConstants.C_TASKLOG_USER);
        }
        
        return task;
    }

    /**
     * Creates a new task.<p>
     * 
     * This is just a more limited version of the 
     * <code>{@link #createTask(CmsDbContext, CmsUser, int, String, String, String, String, int, long, int)}</code>
     * method, where: <br>
     * <ul>
     * <il>the project id is the current project id.</il>
     * <il>the task type is the standard task type <b>1</b>.</il>
     * <il>with no comments</il>
     * </ul><p>
     * 
     * @param dbc the current database context.
     * @param agentName the user who will edit the task.
     * @param roleName a usergroup for the task.
     * @param taskname the name of the task.
     * @param timeout the time when the task must finished.
     * @param priority the id for the priority of the task.
     * 
     * @return the created task.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(
        CmsDbContext dbc,
        String agentName,
        String roleName,
        String taskname,
        long timeout,
        int priority) throws CmsException {

        CmsGroup role = m_userDriver.readGroup(dbc, roleName);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        CmsUUID agentId = CmsUUID.getNullUUID();
        validTaskname(taskname); // check for valid Filename
        try {
            agentId = readUser(dbc, agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId();
        } catch (Exception e) {
            // ignore that this user doesn't exist and create a task for the role
        }
        return m_workflowDriver.createTask(
            dbc,
            dbc.currentProject().getTaskId(),
            dbc.currentProject().getTaskId(),
            1, // standart Task Type
            dbc.currentUser().getId(),
            agentId,
            role.getId(),
            taskname,
            now,
            timestamp,
            priority);
    }

    /**
     * Creates the project for the temporary files.<p>
     *
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * @param dbc the current database context
     * @return the new tempfile project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsDbContext dbc) throws CmsException {

        if (isAdmin(dbc)) {
            // read the needed groups from the cms
            CmsGroup group = readGroup(dbc, OpenCms.getDefaultUsers().getGroupUsers());
            CmsGroup managergroup = readGroup(dbc, OpenCms.getDefaultUsers().getGroupAdministrators());

            // create a new task for the project
            CmsTask task = createProject(
                dbc, 
                CmsWorkplaceManager.C_TEMP_FILE_PROJECT_NAME, 
                group.getName(), 
                System.currentTimeMillis(), 
                I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            
            CmsProject tempProject = m_projectDriver.createProject(
                dbc,
                dbc.currentUser(),
                group,
                managergroup,
                task,
                CmsWorkplaceManager.C_TEMP_FILE_PROJECT_NAME,
                CmsWorkplaceManager.C_TEMP_FILE_PROJECT_DESCRIPTION,
                I_CmsConstants.C_PROJECT_STATE_INVISIBLE,
                I_CmsConstants.C_PROJECT_STATE_INVISIBLE,
                null);
            m_projectDriver.createProjectResource(dbc, tempProject.getId(), "/", null);
            
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                "project",
                tempProject)));

            return tempProject;
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] createTempfileProject() ",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }
    
    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * If there are no other siblings than the specified resource,
     * both the structure and resource property values get deleted.
     * If the specified resource has siblings, only the structure
     * property values get deleted.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the name of the resource for which all properties should be deleted.
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteAllProperties(CmsDbContext dbc, String resourcename)
    throws CmsException {

        CmsResource resource = null;
        List resources = new ArrayList();

        try {
            // read the resource
            resource = readResource(dbc, resourcename, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the security
            m_securityManager.checkPermissions(
                dbc,
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                true,
                CmsResourceFilter.ALL);

            // delete the property values
            if (resource.getSiblingCount() > 1) {
                // the resource has siblings- delete only the (structure) properties of this sibling
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    resource,
                    CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES);
                resources.addAll(readSiblings(dbc, resource, CmsResourceFilter.ALL));
                
            } else {
                // the resource has no other siblings- delete all (structure+resource) properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    resource,
                    CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                resources.add(resource);
            }
        } finally {
            // clear the driver manager cache
            m_propertyCache.clear();

            // fire an event that all properties of a resource have been deleted
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED, 
                    Collections.singletonMap("resources", resources)));
        }
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param dbc the current database context.
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter).
     * 
     * @throws CmsException if something goes wrong.
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, int linkType) throws CmsException {

        m_projectDriver.deleteAllStaticExportPublishedResources(dbc, dbc.currentProject(), linkType);
    }

    /**
     * Deletes all backup versions of a single resource.<p>
     * 
     * @param dbc the current database context
     * @param res the resource to delete all backups from
     * 
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackup(CmsDbContext dbc, CmsResource res) throws CmsException {

        // we need a valid CmsBackupResource, so get all backup file headers of the
        // requested resource
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(dbc, res.getRootPath());
        // check if we have some results
        if (backupFileHeaders.size() > 0) {
            // get the first backup resource
            CmsBackupResource backupResource = (CmsBackupResource)backupFileHeaders.get(0);
            // create a timestamp slightly in the future
            long timestamp = System.currentTimeMillis() + 100000;
            // get the maximum tag id and add ne to include the current publish process as well
            int maxTag = m_backupDriver.readBackupProjectTag(dbc, timestamp) + 1;
            int resVersions = m_backupDriver.readBackupMaxVersion(dbc, res.getResourceId());
            // delete the backups
            m_backupDriver.deleteBackup(dbc, backupResource, maxTag, resVersions);
        }
    }

    /**
     * Deletes the versions from the backup tables that are older then the given timestamp  and/or number of remaining versions.<p>
     * 
     * The number of verions always wins, i.e. if the given timestamp would delete more versions than given in the
     * versions parameter, the timestamp will be ignored.
     * Deletion will delete file header, content and properties.
     * 
     * @param dbc the current database context
     * @param timestamp the max age of backup resources
     * @param versions the number of remaining backup versions for each resource
     * @param report the report for output logging
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackups(CmsDbContext dbc, long timestamp, int versions, I_CmsReport report)
    throws CmsException {

        if (isAdmin(dbc)) {
            // get all resources from the backup table
            // do only get one version per resource
            List allBackupFiles = m_backupDriver.readBackupFileHeaders(dbc);
            int counter = 1;
            int size = allBackupFiles.size();
            // get the tagId of the oldest Backupproject which will be kept in the database
            int maxTag = m_backupDriver.readBackupProjectTag(dbc, timestamp);
            Iterator i = allBackupFiles.iterator();
            while (i.hasNext()) {
                // now check get a single backup resource
                CmsBackupResource res = (CmsBackupResource)i.next();

                report.print("( " + counter + " / " + size + " ) ", I_CmsReport.C_FORMAT_NOTE);
                report.print(report.key("report.history.checking"), I_CmsReport.C_FORMAT_NOTE);
                report.print(res.getRootPath() + " ");

                // now delete all versions of this resource that have more than the maximun number
                // of allowed versions and which are older then the maximum backup date
                int resVersions = m_backupDriver.readBackupMaxVersion(dbc, res.getResourceId());
                int versionsToDelete = resVersions - versions;

                // now we know which backup versions must be deleted, so remove them now
                if (versionsToDelete > 0) {
                    report.print(report.key("report.history.deleting") + report.key("report.dots"));
                    m_backupDriver.deleteBackup(dbc, res, maxTag, versionsToDelete);
                } else {
                    report.print(report.key("report.history.nothing") + report.key("report.dots"));
                }
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                counter++;

                // TODO: delete the old backup projects as well
                m_projectDriver.deletePublishHistory(dbc, dbc.currentProject().getId(), maxTag);
            }
        }
    }

    /**
     * Deletes a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param dbc the current database context.
     * @param name the name of the group that is to be deleted.
     *
     * @throws CmsException if operation was not succesfull.
     */
    public void deleteGroup(CmsDbContext dbc, String name) throws CmsException {

        List childs = null;
        List users = null;
        CmsGroup group = readGroup(dbc, name);
        // get all child groups of the group
        childs = getChild(dbc, name);
        // get all users in this group
        users = getUsersOfGroup(dbc, name);
        // delete group only if it has no childs and there are no users in this group.
        if ((childs == null) && ((users == null) || (users.size() == 0))) {
            CmsProject onlineProject = readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID);
            m_userDriver.deleteGroup(dbc, name);
            m_userDriver.removeAccessControlEntriesForPrincipal(dbc, dbc.currentProject(), onlineProject, group.getId());
            m_groupCache.remove(new CacheId(name));
        } else {
            throw new CmsException(name, CmsException.C_GROUP_NOT_EMPTY);
        }
    }

    /**
     * Deletes a project.<p>
     *
     * Only the admin or the owner of the project can do this.
     * 
     * @param dbc the current database context
     * @param deleteProject the project to be deleted
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteProject(CmsDbContext dbc, CmsProject deleteProject)
    throws CmsException {
      
        int projectId = deleteProject.getId();

        // changed/new/deleted files in the specified project
        List modifiedFiles = readChangedResourcesInsideProject(dbc, projectId, 1);
        
        // changed/new/deleted folders in the specified project
        List modifiedFolders = readChangedResourcesInsideProject(
            dbc,
            projectId,
            CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);

        
        // all resources inside the project have to be be reset to their online state.
        
        // 1. step: delete all new files
        for (int i = 0; i < modifiedFiles.size(); i++) {

            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);
            
            if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {

                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, CmsLock.C_MODE_COMMON);
                } else if (!lock.getUserId().equals(dbc.currentUser().getId())
                    || lock.getProjectId() != dbc.currentProject().getId()) {
                    changeLock(dbc, currentFile);
                }

                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFile,
                    CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // delete the file
                m_vfsDriver.removeFile(dbc, dbc.currentProject(), currentFile, true);

                // remove the access control entries
                m_userDriver.removeAccessControlEntries(
                    dbc, 
                    dbc.currentProject(), 
                    currentFile.getResourceId());

                OpenCms.fireCmsEvent(
                    new CmsEvent(
                        I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, 
                        Collections.singletonMap("resource", currentFile)));
            }
        }
        
        // 2. step: delete all new folders
        for (int i = 0; i < modifiedFolders.size(); i++) {

            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);            
            if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {

                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFolder,
                    CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentFolder);

                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), currentFolder.getResourceId());

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections
                .singletonMap("resource", currentFolder)));
            }
         }


        // 3. step: undo changes on all changed or deleted folders
        for (int i = 0; i < modifiedFolders.size(); i++) {

            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);
       
            if ((currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED)
                || (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED)) {
                CmsLock lock = getLock(dbc, currentFolder);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFolder, CmsLock.C_MODE_COMMON);
                } else if (!lock.getUserId().equals(dbc.currentUser().getId())
                    || lock.getProjectId() != dbc.currentProject().getId()) {
                    changeLock(dbc, currentFolder);
                }

                // undo all changes in the folder
                undoChanges(dbc, currentFolder);
           
             OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections
                    .singletonMap("resource", currentFolder)));
            }
        }
        
        // 4. step: undo changes on all changed or deleted files 
        for (int i = 0; i < modifiedFiles.size(); i++) {

            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);
            
            if ((currentFile.getState() == I_CmsConstants.C_STATE_CHANGED)
                || (currentFile.getState() == I_CmsConstants.C_STATE_DELETED)) {
               
                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, CmsLock.C_MODE_COMMON);
                } else if (!lock.getUserId().equals(dbc.currentUser().getId())
                    || lock.getProjectId() != dbc.currentProject().getId()) {
                    changeLock(dbc, currentFile);
                }

                // undo all changes in the file
                undoChanges(dbc, currentFile);           

                OpenCms.fireCmsEvent(
                    new CmsEvent(
                        I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, 
                        Collections.singletonMap("resource", currentFile)));
            }    
        }

        // unlock all resources in the project
        m_lockManager.removeResourcesInProject(deleteProject.getId());
        clearAccessControlListCache();
        clearResourceCache();

        // set project to online project if current project is the one which will be deleted 
        if (projectId == dbc.currentProject().getId()) {
            dbc.getRequestContext().setCurrentProject(readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID));            
        }

        // delete the project itself
        m_projectDriver.deleteProject(dbc, deleteProject);
        m_projectCache.remove(new Integer(projectId));

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
            "project",
            deleteProject)));
        
    }

    /**
     * Deletes a property definition.<p>
     *
     * @param dbc the current database context
     * @param name the name of the property definition to delete
     * 
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(
        CmsDbContext dbc,
        String name) throws CmsException {

        CmsPropertydefinition propertyDefinition = null;

        try {
            // first read and then delete the metadefinition.            
            propertyDefinition = readPropertydefinition(dbc, name);
            m_vfsDriver.deletePropertyDefinition(dbc, propertyDefinition);
            m_backupDriver.deleteBackupPropertyDefinition(dbc, propertyDefinition);
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_PROPERTY_DEFINITION_MODIFIED, 
                    Collections.singletonMap("propertyDefinition", propertyDefinition)));
        }
    }

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(
        CmsDbContext dbc,
        CmsResource resource,
        int siblingMode) throws CmsException {

        // upgrade a potential inherited, non-shared lock into a common lock
        CmsLock currentLock = getLock(dbc, resource);
        if (currentLock.getType() == CmsLock.C_TYPE_INHERITED) {
            // upgrade the lock status if required
            lockResource(dbc, resource, CmsLock.C_MODE_COMMON);
        }

        // check if siblings of the resource exist and must be deleted as well
        List resources;
        if (resource.isFolder()) {
            // folder can have no siblings
            siblingMode = I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS;
        }

        // if selected, add all siblings of this resource to the list of resources to be deleted    
        boolean allSiblingsRemoved;
        if (siblingMode == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS) {
            resources = new ArrayList(readSiblings(dbc, resource, CmsResourceFilter.ALL));
            allSiblingsRemoved = true;
        } else {
            // only delete the resource, no siblings
            resources = Collections.singletonList(resource);
            allSiblingsRemoved = false;
        }

        int size = resources.size();
        // if we have only one resource no further check is required
        if (size > 1) {
            // ensure that each sibling is unlocked or locked by the current user
            for (int i = 0; i < size; i++) {

                CmsResource currentResource = (CmsResource)resources.get(i);
                currentLock = getLock(dbc, currentResource);

                if (!currentLock.equals(CmsLock.getNullLock())
                    && !currentLock.getUserId().equals(dbc.currentUser().getId())) {
                    // the resource is locked by a user different from the current user
                    int exceptionType = 
                        currentLock.getUserId().equals(dbc.currentUser().getId()) 
                            ? CmsLockException.C_RESOURCE_LOCKED_BY_CURRENT_USER
                            : CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER;
                    throw new CmsLockException("Sibling "
                        + currentResource.getRootPath()
                        + " pointing to "
                        + resource.getRootPath()
                        + " is locked by another user!", exceptionType);
                }
            }
        }

        boolean removeAce = true;

        // delete all collected resources
        for (int i = 0; i < size; i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);

            // try to delete/remove the resource only if the user has write access to the resource            
            if (CmsSecurityManager.PERM_ALLOWED != m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_WRITE,
                true,
                CmsResourceFilter.ALL)) {

                // no write access to sibling - must keep ACE (see below)
                allSiblingsRemoved = false;

            } else {

                // write access to sibling granted                 
                boolean existsOnline = m_vfsDriver.validateStructureIdExists(
                    dbc,
                    I_CmsConstants.C_PROJECT_ONLINE_ID,
                    currentResource.getStructureId());

                if (!existsOnline) {
                    // the resource does not exist online => remove the resource
                    // this means the resoruce is "new" (blue) in the offline project                

                    // delete all properties of this resource
                    deleteAllProperties(dbc, currentResource.getRootPath());

                    if (currentResource.isFolder()) {
                        m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentResource);
                    } else {
                        // check lables
                        if (currentResource.isLabeled() && !labelResource(dbc, currentResource, null, 2)) {
                            // update the resource flags to "unlabel" the other siblings
                            int flags = currentResource.getFlags();
                            flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                            currentResource.setFlags(flags);
                        }
                        m_vfsDriver.removeFile(dbc, dbc.currentProject(), currentResource, true);
                    }

                } else {
                    // the resource exists online => mark the resource as deleted
                    // strcuture record is removed during next publish

                    // if one (or more) siblings are not removed, the ACE can not be removed
                    removeAce = false;

                    // set resource state to deleted
                    currentResource.setState(I_CmsConstants.C_STATE_DELETED);
                    m_vfsDriver.writeResourceState(
                        dbc,
                        dbc.currentProject(),
                        currentResource,
                        C_UPDATE_STRUCTURE_STATE);

                    // add the project id as a property, this is later used for publishing
                    m_vfsDriver.writePropertyObject(
                        dbc,
                        dbc.currentProject(),
                        currentResource,
                        new CmsProperty(
                            I_CmsConstants.C_PROPERTY_INTERNAL, 
                            String.valueOf(dbc.currentProject().getId()),
                            null));

                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(
                        dbc, 
                        dbc.currentProject(), 
                        dbc.currentProject().getId(), 
                        currentResource);
                }
            }
        }

        if ((resource.getSiblingCount() <= 1) || allSiblingsRemoved) {
            if (removeAce) {
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(
                    dbc, 
                    dbc.currentProject(), 
                    resource.getResourceId());
            } else {
                // mark access control entries as deleted
                m_userDriver.deleteAccessControlEntries(
                    dbc, 
                    dbc.currentProject(), 
                    resource.getResourceId());
            }
        }

        // flush all caches
        clearAccessControlListCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_DELETED, 
                Collections.singletonMap("resources", resources)));
    }
    
    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param dbc the current database context.
     * @param resourceName The name of the resource to be deleted in the static export.
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter).
     * @param linkParameter the parameters ofthe resource.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsException {

        m_projectDriver.deleteStaticExportPublishedResource(
            dbc,
            dbc.currentProject(),
            resourceName,
            linkType,
            linkParameter);
    }

    /**
     * Deletes a user.<p>
     *
     * @param dbc the current database context.
     * @param project the current project.
     * @param userId the Id of the user to be deleted.
     * 
     * @throws CmsException if operation was not succesfull.
     */
    public void deleteUser(CmsDbContext dbc, CmsProject project, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(dbc, userId);
        deleteUser(dbc, project, user.getName());
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param username the name of the user to be deleted
     * 
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsDbContext dbc, CmsProject project, String username) throws CmsException {

        // Test is this user is existing
        CmsUser user = readUser(dbc, username);

        CmsProject onlineProject = readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID);
        m_userDriver.deleteUser(dbc, username);    
        m_userDriver.removeAccessControlEntriesForPrincipal(dbc, project, onlineProject, user.getId());
        // delete user from cache
        clearUserCache(user);

    }

    /**
     * Deletes a web user from the Cms.<p>
     * 
     * @param dbc the current database context
     * @param userId the Id of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteWebUser(CmsDbContext dbc, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(dbc, userId);
        m_userDriver.deleteUser(dbc, user.getName());
        // delete user from cache
        clearUserCache(user);
    }

    /**
     * Destroys this driver manager.<p>
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
     * Ends a task.<p>
     *
     * @param dbc the current databsae context.
     * @param taskid the ID of the task to end.
     *
     * @throws CmsException if something goes wrong.
     */
    public void endTask(CmsDbContext dbc, int taskid) throws CmsException {

        m_workflowDriver.endTask(dbc, taskid);
        if (dbc.currentUser() == null) {
            m_workflowDriver.writeSystemTaskLog(dbc, taskid, "Task finished.");

        } else {
            m_workflowDriver.writeSystemTaskLog(dbc, taskid, "Task finished by "
                + dbc.currentUser().getFirstname()
                + " "
                + dbc.currentUser().getLastname()
                + ".");
        }
    }

    /**
     * Tests if a resource with the given resourceId does already exist in the Database.<p>
     * 
     * @param dbc the current database context
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
    public boolean existsResourceId(CmsDbContext dbc, CmsUUID resourceId) throws CmsException {

        return m_vfsDriver.validateResourceIdExists(dbc, dbc.currentProject().getId(), resourceId);
    }

    /**
     * Forwards a task to a new user.<p>
     *
     * @param dbc the current database context.
     * @param taskid the Id of the task to forward.
     * @param newRoleName the new group name for the task.
     * @param newUserName the new user who gets the task. if it is empty, a new agent will automatic selected.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void forwardTask(CmsDbContext dbc, int taskid, String newRoleName, String newUserName)
    throws CmsException {

        CmsGroup newRole = m_userDriver.readGroup(dbc, newRoleName);
        CmsUser newUser = null;
        if (newUserName.equals("")) {
            newUser = readUser(dbc, m_workflowDriver.readAgent(dbc, newRole.getId()));
        } else {
            newUser = readUser(dbc, newUserName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        }

        m_workflowDriver.forwardTask(dbc, taskid, newRole.getId(), newUser.getId());
        m_workflowDriver.writeSystemTaskLog(dbc, taskid, "Task fowarded from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + " to "
            + newUser.getFirstname()
            + " "
            + newUser.getLastname()
            + ".");
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param dbc the current database context.
     * @param resource the resource to read the access control entries for.
     * @param getInherited true if the result should include all access control entries inherited by parent folders.
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource.
     * 
     * @throws CmsException if something goes wrong.
     */
    public List getAccessControlEntries(CmsDbContext dbc, CmsResource resource, boolean getInherited)
    throws CmsException {

        // get the ACE of the resource itself
        List ace = m_userDriver.readAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId(), false);

        // get the ACE of each parent folder
        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        while (getInherited && parentPath != null) {
            resource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getId(), parentPath);
            ace.addAll(m_userDriver.readAccessControlEntries(
                dbc,
                dbc.currentProject(),
                resource.getResourceId(),
                getInherited));
            parentPath = CmsResource.getParentFolder(resource.getRootPath());
        }

        return ace;
    }

    /**
     * Returns the full access control list of a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        return getAccessControlList(dbc, resource, false);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If <code>inheritedOnly</code> is set, only inherited access control entries 
     * are returned.<p>
     * 
     * @param dbc the current database context.
     * @param resource the resource.
     * @param inheritedOnly skip non-inherited entries if set.
     * 
     * @return the access control list of the resource.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsAccessControlList getAccessControlList(
        CmsDbContext dbc,
        CmsResource resource,
        boolean inheritedOnly) throws CmsException {

        String cacheKey = getCacheKey(inheritedOnly + "_", dbc.currentProject(), resource.getStructureId()
            .toString());
        CmsAccessControlList acl = (CmsAccessControlList)m_accessControlListCache.get(cacheKey);

        // return the cached acl if already available
        if (acl != null) {
            return acl;
        }

        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        // otherwise, get the acl of the parent or a new one
        if (parentPath != null) {
            CmsResource parentResource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getId(), parentPath);
            // recurse
            acl = (CmsAccessControlList)getAccessControlList(dbc, parentResource, true).clone();
        } else {
            acl = new CmsAccessControlList();
        }

        // add the access control entries belonging to this resource
        ListIterator ace = m_userDriver.readAccessControlEntries(
            dbc,
            dbc.currentProject(),
            resource.getResourceId(),
            inheritedOnly).listIterator();

        while (ace.hasNext()) {
            CmsAccessControlEntry acEntry = (CmsAccessControlEntry)ace.next();

            acl.add(acEntry);

            // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry
            // denied permissions are kept or extended
            if ((acEntry.getFlags() & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {
                acl.setAllowedPermissions(acEntry);
            }
        }

        m_accessControlListCache.put(cacheKey, acl);

        return acl;
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @return a list of Cms projects
     * @throws CmsException if something goes wrong
     */
    public List getAllAccessibleProjects(CmsDbContext dbc) throws CmsException {

        CmsProject project = null;

        // get all groups of the user
        List groups = getGroupsOfUser(dbc, dbc.currentUser().getName());

        // get all projects which are owned by the user.
        List projects = m_projectDriver.readProjectsForUser(dbc, dbc.currentUser());

        // get all projects, that the user can access with his groups.
        for (int i = 0, n = groups.size(); i < n; i++) {
            List projectsByGroup = new ArrayList();

            // is this the admin-group?
            if (((CmsGroup)groups.get(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup.addAll(m_projectDriver.readProjects(dbc, I_CmsConstants.C_PROJECT_STATE_UNLOCKED));
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup.addAll(m_projectDriver.readProjectsForGroup(dbc, (CmsGroup)groups.get(i)));
            }

            // merge the projects to the vector
            for (int j = 0, m = projectsByGroup.size(); j < m; j++) {
                project = (CmsProject)projectsByGroup.get(j);
                // add only projects, which are new
                if (!projects.contains(project)) {
                    projects.add(project);
                }
            }
        }

        // return the vector of projects
        return projects;
    }

    /**
     * Returns a Vector with all projects from history.<p>
     *
     * @param dbc the current database context
     * 
     * @return Vector with all projects from history.
     * @throws CmsException if operation was not succesful.
     */
    public List getAllBackupProjects(CmsDbContext dbc) throws CmsException {

        return m_backupDriver.readBackupProjects(dbc);
    }

    /**
     * Returns all projects which are owned by the user or which are manageable for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @return a list of Cms projects
     * @throws CmsException if operation was not succesful
     */
    public List getAllManageableProjects(CmsDbContext dbc) throws CmsException {

        CmsProject project = null;

        // get all groups of the user
        List groups = getGroupsOfUser(dbc, dbc.currentUser().getName());

        // get all projects which are owned by the user.
        List projects = m_projectDriver.readProjectsForUser(dbc, dbc.currentUser());

        // get all projects, that the user can manage with his groups.
        for (int i = 0, n = groups.size(); i < n; i++) {
            // get all projects, which can be managed by the current group
            List projectsByGroup = new ArrayList();

            // is this the admin-group?
            if (((CmsGroup)groups.get(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup.addAll(m_projectDriver.readProjects(dbc, I_CmsConstants.C_PROJECT_STATE_UNLOCKED));
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup.addAll(m_projectDriver.readProjectsForManagerGroup(dbc, (CmsGroup)groups.get(i)));
            }

            // merge the projects to the vector
            for (int j = 0, m = projectsByGroup.size(); j < m; j++) {
                // add only projects, which are new
                project = (CmsProject)projectsByGroup.get(j);
                if (!projects.contains(project)) {
                    projects.add(project);
                }
            }
        }

        // remove the online-project, it is not manageable!
        projects.remove(readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID));

        return projects;
    }

    /**
     * Returns the backup driver.<p>
     * 
     * @return the backup driver
     */
    public I_CmsBackupDriver getBackupDriver() {

        return m_backupDriver;
    }

    /**
     * Get the next version id for the published backup resources.<p>
     *
     * @param dbc the current database context
     * 
     * @return the new version id
     */
    public int getBackupTagId(CmsDbContext dbc) {

        return m_backupDriver.readNextBackupTagId(dbc);
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * @param dbc the current database context.
     * @param groupname the name of the group.
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getChild(CmsDbContext dbc, String groupname) throws CmsException {

        // check security
        if (!dbc.currentUser().isGuestUser()) {
            return m_userDriver.readChildGroups(dbc, groupname);
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getChild()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * This method also returns all sub-child groups of the current group.
     *
     * @param dbc the current database context.
     * @param groupname the name of the group.
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getChilds(CmsDbContext dbc, String groupname) throws CmsException {

        // check security
        if (!dbc.currentUser().isGuestUser()) {
            List childs = null;
            List allChilds = new Vector();
            List subchilds = new Vector();
            CmsGroup group = null;

            // get all child groups if the user group
            childs = m_userDriver.readChildGroups(dbc, groupname);
            if (childs != null) {
                allChilds = childs;
                // now get all subchilds for each group
                Iterator it = childs.iterator();
                while (it.hasNext()) {
                    group = (CmsGroup)it.next();
                    subchilds = getChilds(dbc, group.getName());
                    //add the subchilds to the already existing groups
                    Iterator itsub = subchilds.iterator();
                    while (itsub.hasNext()) {
                        group = (CmsGroup)itsub.next();
                        allChilds.add(group);
                    }
                }
            }
            return allChilds;
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getChilds()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Method to access the configurations of the properties-file.<p>
     *
     * All users are granted.
     *
     * @return the Configurations of the properties-file
     */
    public ExtendedProperties getConfigurations() {

        return m_configuration;
    }

    /**
     * Returns the list of groups to which the user directly belongs to.<p>
     *
     * @param dbc the current database context.
     * @param username The name of the user.
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects.
     * 
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public List getDirectGroupsOfUser(CmsDbContext dbc, String username) throws CmsException {

        CmsUser user = readUser(dbc, username);
        return m_userDriver.readGroupsOfUser(dbc, user.getId(), dbc.getRequestContext().getRemoteAddress());
    }

    /**
     * Returns all groups.<p>
     *
     * @param dbc the current database context.
     * 
     * @return a list of all <code>{@link CmsGroup}</code> objects.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getGroups(CmsDbContext dbc) throws CmsException {

        // check security
        if (!dbc.currentUser().isGuestUser()) {
            return m_userDriver.readGroups(dbc);
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getGroups()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns the groups of a user.<p>
     * 
     * @param dbc the current database context.
     * @param username the name of the user.
     *
     * @return a list of <code>{@link CmsGroup}</code> objects.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getGroupsOfUser(CmsDbContext dbc, String username)
    throws CmsException {

        return getGroupsOfUser(dbc, username, dbc.getRequestContext().getRemoteAddress());
    }

    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     * 
     * @param dbc the current database context.
     * @param username the name of the user.
     * @param remoteAddress the IP address to filter the groups in the result list.
     *
     * @return a list of <code>{@link CmsGroup}</code> objects.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getGroupsOfUser(
        CmsDbContext dbc,
        String username,
        String remoteAddress) throws CmsException {

        CmsUser user = readUser(dbc, username);
        String cacheKey = m_keyGenerator.getCacheKeyForUserGroups(remoteAddress, dbc, user);

        List allGroups = (List)m_userGroupsCache.get(cacheKey);
        if ((allGroups == null) || (allGroups.size() == 0)) {

            CmsGroup subGroup;
            CmsGroup group;
            // get all groups of the user
            List groups = m_userDriver.readGroupsOfUser(dbc, user.getId(), remoteAddress);
            allGroups = new Vector();
            // now get all childs of the groups
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                group = (CmsGroup)it.next();

                subGroup = getParent(dbc, group.getName());
                while ((subGroup != null) && (!allGroups.contains(subGroup))) {

                    allGroups.add(subGroup);
                    // read next sub group
                    subGroup = getParent(dbc, subGroup.getName());
                }

                if (!allGroups.contains(group)) {
                    allGroups.add(group);
                }
            }
            m_userGroupsCache.put(cacheKey, allGroups);
        }

        return allGroups;
    }

    /**
     * Returns the HTML link validator.<p>
     * 
     * @return the HTML link validator
     * @see CmsHtmlLinkValidator
     */
    public CmsHtmlLinkValidator getHtmlLinkValidator() {

        return m_htmlLinkValidator;
    }

    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to return the lock state for
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        return m_lockManager.getLock(this, dbc, resource);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param dbc the current database context.
     * @param groupname the name of the group.
     * 
     * @return group the parent group or <code>null</code>.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsGroup getParent(CmsDbContext dbc, String groupname) throws CmsException {

        CmsGroup group = readGroup(dbc, groupname);
        if (group.getParentId().isNullUUID()) {
            return null;
        }

        // try to read from cache
        CmsGroup parent = (CmsGroup)m_groupCache.get(new CacheId(group.getParentId()));
        if (parent == null) {
            parent = m_userDriver.readGroup(dbc, group.getParentId());
            m_groupCache.put(new CacheId(parent), parent);
        }
        return parent;
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param dbc the current database context.
     * @param resource the resource.
     * @param user the user.
     * 
     * @return bitset with allowed permissions.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsPermissionSetCustom getPermissions(CmsDbContext dbc, CmsResource resource, CmsUser user)
    throws CmsException {

        CmsAccessControlList acList = getAccessControlList(dbc, resource, false);
        return acList.getPermissions(user, getGroupsOfUser(dbc, user.getName()));
    }

    /**
     * Returns the project driver.<p>
     *
     * @return the project driver
     */
    public I_CmsProjectDriver getProjectDriver() {

        return m_projectDriver;
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
     * @param dbc the current database context
     * @param directPublishResource a Cms resource to be published directly (in case 2), or null (in case 1)
     * @param publishSiblings true, if all eventual siblings of the direct published resource should also get published (in case 2)
     * 
     * @return a publish list with all new/changed/deleted files from the current (offline) project that will be published actually
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.CmsPublishList
     */
    public synchronized CmsPublishList getPublishList(
        CmsDbContext dbc,
        CmsResource directPublishResource,
        boolean publishSiblings) throws CmsException {

        CmsPublishList publishList = new CmsPublishList(directPublishResource);

        if (directPublishResource == null) {
            
            // when publishing a project, 
            // all modified resources with the last change done in the current project are candidates if unlocked

            List folderList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                I_CmsConstants.C_READ_IGNORE_PARENT,
                I_CmsConstants.C_READ_IGNORE_TYPE,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READMODE_INCLUDE_TREE
                    | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                    | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                    | I_CmsConstants.C_READMODE_ONLY_FOLDERS);   
            
            publishList.addFolders(filterResources(dbc, folderList, folderList));
            
            List fileList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                I_CmsConstants.C_READ_IGNORE_PARENT,
                I_CmsConstants.C_READ_IGNORE_TYPE,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READMODE_INCLUDE_TREE
                    | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                    | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                    | I_CmsConstants.C_READMODE_ONLY_FILES);
            
            publishList.addFiles(filterResources(dbc, publishList.getFolderList(), fileList));
            
        } else if (directPublishResource.isFolder()) {
            
            // when publishing a folder directly, 
            // the folder and all modified resources within the tree below this folder 
            // and with the last change done in the current project are candidates if unlocked
            
            if (I_CmsConstants.C_STATE_UNCHANGED != directPublishResource.getState()
                && getLock(dbc, directPublishResource).isNullLock()) {
                publishList.addFolder(directPublishResource);
            }
            
            List folderList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                directPublishResource.getRootPath(),
                I_CmsConstants.C_READ_IGNORE_TYPE,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READMODE_INCLUDE_TREE
                    | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                    | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                    | I_CmsConstants.C_READMODE_ONLY_FOLDERS);    
            
            publishList.addFolders(filterResources(dbc, publishList.getFolderList(), folderList));
            
            List fileList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                directPublishResource.getRootPath(),
                I_CmsConstants.C_READ_IGNORE_TYPE,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READ_IGNORE_TIME,
                I_CmsConstants.C_READMODE_INCLUDE_TREE
                    | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                    | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                    | I_CmsConstants.C_READMODE_ONLY_FILES);
            
            publishList.addFiles(filterResources(dbc, publishList.getFolderList(), fileList));
            
        } else if (directPublishResource.isFile()
            && I_CmsConstants.C_STATE_UNCHANGED != directPublishResource.getState()) {
            
            // when publishing a file directly this file is the only candidate
            // if it is modified and unlocked
            
            if (getLock(dbc, directPublishResource).isNullLock()) {
                publishList.addFile(directPublishResource);
            }
            
        }

        // Step 2: if desired, extend the list of files to publish with related siblings
        if (publishSiblings) {
            
            List publishFiles = publishList.getFileList();
            int size = publishFiles.size();
            
            for (int i = 0; i < size; i++) {
                CmsResource currentFile = (CmsResource)publishFiles.get(i);
                if (currentFile.getSiblingCount() > 1) {
                    publishList.addFiles(filterSiblings(
                        dbc,
                        currentFile,
                        publishList.getFolderList(),
                        readSiblings(
                            dbc, 
                            currentFile, 
                            CmsResourceFilter.ALL_MODIFIED)));
                }
            }
        }

        publishList.initialize();
        return publishList;
    }

    /**
     * Returns a list with all sub resources of a given folder that have benn modified in a given time range.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @param folder the folder to get the subresources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesInTimeRange(CmsDbContext dbc, String folder, long starttime, long endtime)
    throws CmsException {

        return m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                folder,
                I_CmsConstants.C_READ_IGNORE_TYPE,
                I_CmsConstants.C_READ_IGNORE_STATE,
                starttime,
                endtime,
                I_CmsConstants.C_READMODE_INCLUDE_TREE);
    }

    /**
     * Returns the value of the given parameter for the given task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task.
     * @param parName name of the parameter.
     * 
     * @return task parameter value.
     * 
     * @throws CmsException if something goes wrong.
     */
    public String getTaskPar(CmsDbContext dbc, int taskId, String parName) throws CmsException {

        return m_workflowDriver.readTaskParameter(dbc, taskId, parName);
    }

    /**
     * Returns the template task id for a given taskname.<p>
     *
     * @param dbc the current database context.
     * @param taskName the name of the task.
     * 
     * @return the id of the task template.
     * 
     * @throws CmsException if operation was not successful.
     */
    public int getTaskType(CmsDbContext dbc, String taskName) throws CmsException {

        return m_workflowDriver.readTaskType(dbc, taskName);
    }

    /**
     * Returns the user driver.<p>
     * 
     * @return the user driver
     */
    public I_CmsUserDriver getUserDriver() {

        return m_userDriver;
    }

    /**
     * Returns all users.<p>
     *
     * @param dbc the current database context.
     * 
     * @return a list of all <code>{@link CmsUser}</code> objects.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getUsers(CmsDbContext dbc) throws CmsException {

        // check security
        if (!dbc.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(dbc, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getUsers()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns all users from a given type.<p>
     *
     * @param dbc the current database context.
     * @param type the type of the users.
     * 
     * @return a list of all <code>{@link CmsUser}</code> objects of the given type.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public List getUsers(CmsDbContext dbc, int type) throws CmsException {

        // check security
        if (!dbc.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(dbc, type);
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getUsers()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * @param dbc the current database context.
     * @param groupname the name of the group to list users from.
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group.
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getUsersOfGroup(CmsDbContext dbc, String groupname) throws CmsException {

        // check the security
        if (!dbc.currentUser().isGuestUser()) {
            return m_userDriver.readUsersOfGroup(dbc, groupname, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsSecurityException(
                "[" + getClass().getName() + "] getUsersOfGroup()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns the VFS driver.<p>
     * 
     * @return the VFS driver
     */
    public I_CmsVfsDriver getVfsDriver() {

        return m_vfsDriver;
    }

    /**
     * Returns the workflow driver.<p>
     * 
     * @return the workflow driver
     */
    public I_CmsWorkflowDriver getWorkflowDriver() {

        return m_workflowDriver;
    }

    /**
     * Writes a vector of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.
     * Access is granted, if:<p>
     * <ul>
     * <li>the current user has control permission on the resource</li>
     * </ul>
     * 
     * @param dbc the current database context.
     * @param resource the resource.
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects.
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsDbContext dbc, CmsResource resource, List acEntries)
    throws CmsException {

        m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), (CmsAccessControlEntry)i.next());
        }
        clearAccessControlListCache();
    }

    /**
     * Imports an import-resource (folder or zipfile).<p>
     *
     * It is important that a <code>manifest.xml</code> is present in the 
     * given folder or the root path inside the zip file, if not a 
     * <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param cms the cms-object to use for the export.
     * @param dbc the current database context.
     * @param importFile the name (absolute Path) of the import resource (zip or folder).
     * @param importPath the name (absolute Path) of folder in which should be imported.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void importFolder(CmsObject cms, CmsDbContext dbc, String importFile, String importPath)
    throws CmsException {

        if (isAdmin(dbc)) {
            clearcache();
            new CmsImportFolder(importFile, importPath, cms);
            clearcache();
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] importFolder()",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Initializes the driver and sets up all required modules and connections.<p>
     * 
     * @param config the OpenCms configuration
     * @param vfsDriver the vfsdriver
     * @param userDriver the userdriver
     * @param projectDriver the projectdriver
     * @param workflowDriver the workflowdriver
     * @param backupDriver the backupdriver
     * @throws CmsException if something goes wrong
     * @throws Exception if something goes wrong
     */
    public void init(
        ExtendedProperties config,
        I_CmsVfsDriver vfsDriver,
        I_CmsUserDriver userDriver,
        I_CmsProjectDriver projectDriver,
        I_CmsWorkflowDriver workflowDriver,
        I_CmsBackupDriver backupDriver) throws CmsException, Exception {

        // initialize the access-module.
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 4 - connecting to the database");
        }

        // store the access objects
        m_vfsDriver = vfsDriver;
        m_userDriver = userDriver;
        m_projectDriver = projectDriver;
        m_workflowDriver = workflowDriver;
        m_backupDriver = backupDriver;

        m_configuration = config;

        // initialize the key generator
        m_keyGenerator = (I_CmsCacheKey)Class.forName(
            config.getString(I_CmsConstants.C_CONFIGURATION_CACHE + ".keygenerator")).newInstance();

        // initalize the caches
        LRUMap hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".user", 50));
        m_userCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_userCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".group", 50));
        m_groupCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_groupCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".usergroups", 50));
        m_userGroupsCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_userGroupsCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".project", 50));
        m_projectCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_projectCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resource", 2500));
        m_resourceCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_resourceCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resourcelist", 100));
        m_resourceListCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_resourceListCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".property", 5000));
        m_propertyCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_propertyCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".accesscontrollists", 1000));
        m_accessControlListCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_accessControlListCache", hashMap);
        }

        getProjectDriver().fillDefaults(new CmsDbContext());

        // initialize the HTML link validator
        m_htmlLinkValidator = new CmsHtmlLinkValidator(this);
    }

    /**
     * Checks if the current user has "Administrator" permissions.<p>
     * 
     * Administrator permissions means that the user is a member of the 
     * administrators group, which per default is called "Administrators".<p>
     *
     * @param dbc the current database context.
     * 
     * @return <code>true</code>, if the current user has "Administrator" permissions.
     * 
     * @see CmsObject#isAdmin()
     */
    public boolean isAdmin(CmsDbContext dbc) {

        try {
            return userInGroup(
                dbc,                 
                dbc.currentUser().getName(), 
                OpenCms.getDefaultUsers().getGroupAdministrators());
            
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param dbc the current database context.
     * @param resourcename the specified resource name (full path).
     * 
     * @return <code>true</code>, if the specified resource is inside the current project.
     */
    public boolean isInsideCurrentProject(CmsDbContext dbc, String resourcename) {

        List projectResources = null;

        try {
            projectResources = readProjectResources(dbc, dbc.currentProject());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(
                    "[CmsDriverManager.isInsideProject()] error reading project resources " + e.getMessage());
            }
            return false;
        }
        return CmsProject.isInsideProject(projectResources, resourcename);
    }

    /**
     * 
     * Proves if a resource is locked.<p>
     * 
     * @see org.opencms.lock.CmsLockManager#isLocked(org.opencms.db.CmsDriverManager, CmsDbContext, CmsResource)
     * 
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return true, if and only if the resource is currently locked
     * @throws CmsException if something goes wrong
     */
    public boolean isLocked(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return m_lockManager.isLocked(this, dbc, resource);
    }

    /**
     * Checks if the current user has management access to the project.<p>
     *
     * Please note: This is NOT the same as the <code>{@link #isProjectManager(CmsDbContext)}</code> 
     * check. If the user has management access to a project depends on the
     * project settings.<p>
     * 
     * @param dbc the current database context.
     *
     * @return <code>true</code>, if the user has management access to the project.
     * 
     * @see CmsObject#isManagerOfProject()
     * @see #isProjectManager(CmsDbContext)
     */
    public boolean isManagerOfProject(CmsDbContext dbc) {

        if (isAdmin(dbc)) {
            // user is Admin
            return true;
        }
        if (dbc.currentUser().getId().equals(dbc.currentProject().getOwnerId())) {
            // user is the owner of the current project
            return true;
        }

        // get all groups of the user
        List groups;
        try {
            groups = getGroupsOfUser(dbc, dbc.currentUser().getName());
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }

        for (int i = 0; i < groups.size(); i++) {
            // check if the user is a member in the current projects manager group
            if (((CmsGroup)groups.get(i)).getId().equals(dbc.currentProject().getManagerGroupId())) {
                // this group is manager of the project
                return true;
            }
        }

        // the user is not manager of the current project
        return false;
    }

    /**
     * Checks if the current user is a member of the project manager group.<p>
     *
     * Please note: This is NOT the same as the <code>{@link #isManagerOfProject(CmsDbContext)}</code> 
     * check. If the user is a member of the project manager group, 
     * he can create new projects.<p>
     *
     * @param dbc the current database context
     * 
     * @return <code>true</code>, if the user is a member of the project manager group.
     * 
     * @see CmsObject#isProjectManager()
     * @see #isManagerOfProject(CmsDbContext)
     */
    public boolean isProjectManager(CmsDbContext dbc) {

        try {
            return userInGroup(
                dbc,
                dbc.currentUser().getName(), 
                OpenCms.getDefaultUsers().getGroupProjectmanagers());
            
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }
    }

    /**
     * Checks if a project is the tempfile project.<p>
     * @param project the project to test
     * @return true if the project is the tempfile project
     */
    public boolean isTempfileProject(CmsProject project) {

        return project.getName().equals("tempFileProject");
    }

    /**
     * Determines if the user is a member of the default users group.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @return true, if the users current group is the projectleader-group, else it returns false
     * @throws CmsException if operation was not succesful
     */
    public boolean isUser(CmsDbContext dbc) throws CmsException {

        return userInGroup(dbc, dbc.currentUser().getName(), OpenCms.getDefaultUsers().getGroupUsers());
    }

    /**
     * Checks if one of the resources (except the resource itself) 
     * is a sibling in a "labeled" site folder.<p>
     * 
     * This method is used when creating a new sibling 
     * (use the newResource parameter & action = 1) 
     * or deleting/importing a resource (call with action = 2).<p> 
     *   
     * @param dbc the current database context
     * @param resource the resource
     * @param newResource absolute path for a resource sibling which will be created
     * @param action the action which has to be performed (1 = create VFS link, 2 all other actions)
     * @return true if the flag should be set for the resource, otherwise false
     * @throws CmsException if something goes wrong
     */
    public boolean labelResource(CmsDbContext dbc, CmsResource resource, String newResource, int action)
    throws CmsException {

        // get the list of labeled site folders from the runtime property
        List labeledSites = OpenCms.getWorkplaceManager().getLabelSiteFolders();

        if (labeledSites.size() == 0) {
            // no labeled sites defined, just return false 
            return false;
        }

        if (action == 1) {
            // CASE 1: a new resource is created, check the sites
            if (!resource.isLabeled()) {
                // source isn't labeled yet, so check!
                boolean linkInside = false;
                boolean sourceInside = false;
                for (int i = 0; i < labeledSites.size(); i++) {
                    String curSite = (String)labeledSites.get(i);
                    if (newResource.startsWith(curSite)) {
                        // the link lies in a labeled site
                        linkInside = true;
                    }
                    if (resource.getRootPath().startsWith(curSite)) {
                        // the source lies in a labeled site
                        sourceInside = true;
                    }
                }
                // return true when either source or link is in labeled site, otherwise false
                return (linkInside != sourceInside);
            }
            // resource is already labeled
            return false;

        } else {
            // CASE 2: the resource will be deleted or created (import)
            // check if at least one of the other siblings resides inside a "labeled site"
            // and if at least one of the other siblings resides outside a "labeled site"
            boolean isInside = false;
            boolean isOutside = false;
            // check if one of the other vfs links lies in a labeled site folder
            List siblings = m_vfsDriver.readSiblings(dbc, dbc.currentProject(), resource, false);
            updateContextDates(dbc, siblings);
            Iterator i = siblings.iterator();
            while (i.hasNext() && (!isInside || !isOutside)) {
                CmsResource currentResource = (CmsResource)i.next();
                if (currentResource.equals(resource)) {
                    // dont't check the resource itself!
                    continue;
                }
                String curPath = currentResource.getRootPath();
                boolean curInside = false;
                for (int k = 0; k < labeledSites.size(); k++) {
                    if (curPath.startsWith((String)labeledSites.get(k))) {
                        // the link is in the labeled site
                        isInside = true;
                        curInside = true;
                        break;
                    }
                }
                if (!curInside) {
                    // the current link was not found in labeled site, so it is outside
                    isOutside = true;
                }
            }
            // now check the new resource name if present
            if (newResource != null) {
                boolean curInside = false;
                for (int k = 0; k < labeledSites.size(); k++) {
                    if (newResource.startsWith((String)labeledSites.get(k))) {
                        // the new resource is in the labeled site
                        isInside = true;
                        curInside = true;
                        break;
                    }
                }
                if (!curInside) {
                    // the new resource was not found in labeled site, so it is outside
                    isOutside = true;
                }
            }
            return (isInside && isOutside);
        }
    }

    /**
     * Returns the user, who had locked the resource.<p>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param dbc the current database context
     * @param resource the resource
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights for this resource
     */
    public CmsUser lockedBy(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return readUser(dbc, m_lockManager.getLock(this, dbc, resource).getUserId());    }

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
     * @param dbc the current database context
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see I_CmsResourceType#lockResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void lockResource(CmsDbContext dbc, CmsResource resource, int mode)
    throws CmsException {

        // update the resource cache
        clearResourceCache();

        // add the resource to the lock dispatcher
        m_lockManager.addResource(
            this,
            dbc,
            resource,
            dbc.currentUser().getId(),
            dbc.currentProject().getId(),
            mode);

        if ((resource.getState() != I_CmsConstants.C_STATE_UNCHANGED)
            && (resource.getState() != I_CmsConstants.C_STATE_KEEP)) {
            // update the project flag of a modified resource as "last modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(dbc, dbc.currentProject(), dbc.currentProject().getId(), resource);
        }

        // we must also clear the permission cache
        m_securityManager.clearPermissionCache();

        // fire resource modification event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap(
            "resource",
            resource)));
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * For security reasons, all error / exceptions that occur here are "blocked" and 
     * a simple security exception is thrown.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * @param userType the user type to log in (System user or Web user)
     * 
     * @return the logged in users name
     *
     * @throws CmsSecurityException if login was not succesful
     */
    public CmsUser loginUser(CmsDbContext dbc, String username, String password, String remoteAddress, int userType)
    throws CmsSecurityException {

        CmsUser newUser;

        try {
            // read the user from the driver to avoid the cache
            newUser = m_userDriver.readUser(dbc, username, password, remoteAddress, userType);
        } catch (Throwable t) {
            // any error here: throw a security exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, t);
        }

        // check if the "enabled" flag is set for the user
        if (newUser.getFlags() != I_CmsConstants.C_FLAG_ENABLED) {
            // user is disabled, throw a securiy exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED);
        }

        // set the last login time to the current time
        newUser.setLastlogin(System.currentTimeMillis());

        try {
            // write the changed user object back to the user driver
            m_userDriver.writeUser(dbc, newUser);
        } catch (Throwable t) {
            // any error here: throw a security exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, t);
        }

        // update cache
        putUserInCache(newUser);

        // invalidate all user depdent caches
        m_accessControlListCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_resourceListCache.clear();
        m_securityManager.clearPermissionCache();

        // return the user object read from the driver
        return newUser;
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param dbc the current database context.
     * @param principalId the UUID of the principal to lookup.
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>.
     */
    public I_CmsPrincipal lookupPrincipal(CmsDbContext dbc, CmsUUID principalId) {

        try {
            CmsGroup group = m_userDriver.readGroup(dbc, principalId);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception 
        }

        try {
            CmsUser user = readUser(dbc, principalId);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Lookup and read the user or group with the given name.<p>
     * 
     * @param dbc the current database context.
     * @param principalName the name of the principal to lookup.
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>.
     */
    public I_CmsPrincipal lookupPrincipal(CmsDbContext dbc, String principalName) {

        try {
            CmsGroup group = m_userDriver.readGroup(dbc, principalName);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        try {
            CmsUser user = readUser(dbc, principalName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The method can also be used to check get the name of a resource
     * in the "lost and found" folder only without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * @param dbc the current database context
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
    public String moveToLostAndFound(
        CmsDbContext dbc,
        String resourcename,
        boolean returnNameOnly) throws CmsException {

        CmsRequestContext context = dbc.getRequestContext();
        String siteRoot = context.getSiteRoot();
        Stack storage = new Stack();
        context.setSiteRoot("/");
        String destination = I_CmsConstants.C_VFS_LOST_AND_FOUND + resourcename;
        // create the require folders if nescessary
        String des = destination;
        // collect all folders...
        try {
            while (des.indexOf('/') == 0) {
                des = des.substring(0, des.lastIndexOf('/'));
                storage.push(des.concat("/"));
            }
            // ...now create them....
            while (storage.size() != 0) {
                des = (String)storage.pop();
                try {
                    readFolder(dbc, des, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (Exception e1) {
                    // the folder is not existing, so create it
                    createResource(
                        dbc,
                        des,
                        CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
                        null,
                        Collections.EMPTY_LIST);
                }
            }
            // check if this resource name does already exist
            // if so add a psotfix to the name
            des = destination;
            int postfix = 1;
            boolean found = true;
            while (found) {
                try {
                    // try to read the file.....
                    found = true;
                    readResource(dbc, des, CmsResourceFilter.ALL);
                    // ....it's there, so add a postfix and try again
                    String path = destination.substring(0, destination.lastIndexOf("/") + 1);
                    String filename = destination.substring(destination.lastIndexOf("/") + 1, destination.length());

                    des = path;

                    if (filename.lastIndexOf(".") > 0) {
                        des += filename.substring(0, filename.lastIndexOf("."));
                    } else {
                        des += filename;
                    }
                    des += "_" + postfix;
                    if (filename.lastIndexOf(".") > 0) {
                        des += filename.substring(filename.lastIndexOf("."), filename.length());
                    }
                    postfix++;
                } catch (CmsException e3) {
                    // the file does not exist, so we can use this filename                               
                    found = false;
                }
            }
            destination = des;

            if (!returnNameOnly) {
                // move the existing resource to the lost and foud folder
                CmsResource resource = readResource(dbc, resourcename, CmsResourceFilter.ALL);
                copyResource(dbc, resource, destination, I_CmsConstants.C_COPY_AS_SIBLING);
                deleteResource(dbc, resource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            }
        } catch (CmsException e2) {
            throw e2;
        } finally {
            // set the site root to the old value again
            context.setSiteRoot(siteRoot);
        }
        return destination;
    }

    /**
     * Gets a new driver instance.<p>
     * 
     * @param configurationManager the configuration manager
     * @param driverName the driver name
     * @param successiveDrivers the list of successive drivers
     * 
     * @return the driver object
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(
        CmsConfigurationManager configurationManager,
        String driverName,
        List successiveDrivers) throws CmsException {

        Class driverClass = null;
        I_CmsDriver driver = null;
        CmsDbContext dbc = new CmsDbContext();

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : starting " + driverName);
            }

            // try to create a instance
            driver = (I_CmsDriver)driverClass.newInstance();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : initializing " + driverName);
            }
           
            // invoke the init-method of this access class
            driver.init(dbc, configurationManager, successiveDrivers, this);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : ok, finished");
            }

        } catch (Exception exc) {
            String message = "Critical error while initializing " + driverName;
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return driver;
    }

    /**
     * Method to create a new instance of a driver.<p>
     * 
     * @param configuration the configurations from the propertyfile
     * @param driverName the class name of the driver
     * @param driverPoolUrl the pool url for the driver
     * @return an initialized instance of the driver
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(ExtendedProperties configuration, String driverName, String driverPoolUrl)
    throws CmsException {

        Class initParamClasses[] = {ExtendedProperties.class, String.class, CmsDriverManager.class};
        Object initParams[] = {configuration, driverPoolUrl, this};

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : starting " + driverName);
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : initializing " + driverName);
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                    ". Driver init          : finished, assigned pool " + driverPoolUrl);
            }

        } catch (Exception exc) {
            String message = "Critical error while initializing " + driverName;
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return driver;
    }

    /**
     * Method to create a new instance of a pool.<p>
     * 
     * @param configurations the configurations from the propertyfile
     * @param poolName the configuration name of the pool
     * @throws CmsException if something goes wrong
     */
    public void newPoolInstance(ExtendedProperties configurations, String poolName) throws CmsException {

        PoolingDriver driver;

        try {
            driver = CmsDbPool.createDriverManagerConnectionPool(configurations, poolName);
        } catch (Exception exc) {
            String message = "Critical error while initializing connection pool " + poolName;
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        m_connectionPools.add(driver);
    }

    /**
     * Publishes a project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param cms the current CmsObject
     * @param dbc the current database context
     * @param publishList a Cms publish list
     * @param report a report object to provide the loggin messages
     * 
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsDbContext, CmsResource, boolean)
     */
    public synchronized void publishProject(
        CmsObject cms,
        CmsDbContext dbc,
        CmsPublishList publishList,
        I_CmsReport report) throws Exception {

        int publishProjectId = dbc.currentProject().getId();
        boolean temporaryProject = (dbc.currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
        boolean backupEnabled = OpenCms.getSystemInfo().isVersionHistoryEnabled();
        int backupTagId = 0;

        try {
            if (backupEnabled) {
                backupTagId = getBackupTagId(dbc);
            } else {
                backupTagId = 0;
            }

            int maxVersions = OpenCms.getSystemInfo().getVersionHistoryMaxCount();

            // if we direct publish a file, check if all parent folders are already published
            if (publishList.isDirectPublish()) {
                try {
                    getVfsDriver().readFolder(
                        dbc,
                        I_CmsConstants.C_PROJECT_ONLINE_ID,
                        CmsResource.getParentFolder(publishList.getDirectPublishResource().getRootPath()));
                } catch (CmsException e) {
                    report.println("Parent folder not published for resource "
                        + publishList.getDirectPublishResource().getRootPath(), I_CmsReport.C_FORMAT_ERROR);
                    return;
                }
            }

            // clear the cache
            clearcache();

            m_projectDriver.publishProject(
                dbc, 
                report,
                readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID),
                publishList,
                OpenCms.getSystemInfo().isVersionHistoryEnabled(),
                backupTagId,
                maxVersions);

            // iterate the initialized module action instances
            Iterator i = OpenCms.getModuleManager().getActionInstances();
            while (i.hasNext()) {
                I_CmsModuleAction moduleActionInstance = (I_CmsModuleAction)i.next();
                moduleActionInstance.publishProject(cms, publishList, backupTagId, report);
            }

            // the project was stored in the backuptables for history
            // it will be deleted if the project_flag is C_PROJECT_TYPE_TEMPORARY
            if (temporaryProject) {
                try {
                    m_projectDriver.deleteProject(dbc, dbc.currentProject());
                } catch (CmsException e) {
                    OpenCms.getLog(this).error("Could not delete temporary project " + publishProjectId);
                }
                // if project was temporary set context to online project
                cms.getRequestContext().setCurrentProject(readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID));
            }

        } finally {
            clearcache();

            // fire an event that a project has been published
            Map eventData = new HashMap();
            eventData.put(I_CmsEventListener.KEY_REPORT, report);
            eventData.put(I_CmsEventListener.KEY_PUBLISHID, publishList.getPublishHistoryId().toString());
            eventData.put(I_CmsEventListener.KEY_PROJECTID, new Integer(publishProjectId));
            eventData.put(I_CmsEventListener.KEY_DBCONTEXT, dbc);
            CmsEvent exportPointEvent = new CmsEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData);
            OpenCms.fireCmsEvent(exportPointEvent);
        }
    }

    /**
     * Reactivates a task.<p>
     * 
     * Setting its state to <code>{@link I_CmsConstants#C_TASK_STATE_STARTED}</code> and
     * the percentage to <b>zero</b>.<p>
     *
     * @param dbc the current database context.
     * @param taskId the id of the task to reactivate.
     *
     * @throws CmsException if something goes wrong.
     */
    public void reactivateTask(CmsDbContext dbc, int taskId) throws CmsException {

        CmsTask task = m_workflowDriver.readTask(dbc, taskId);
        task.setState(I_CmsConstants.C_TASK_STATE_STARTED);
        task.setPercentage(0);
        task = m_workflowDriver.writeTask(dbc, task);
        m_workflowDriver.writeSystemTaskLog(dbc, taskId, "Task was reactivated from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + ".");
    }

    /**
     * Reads an access control entry from the cms.<p>
     * 
     * The access control entries of a resource are readable by everyone.
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param principal the id of a group or a user any other entity
     * @return an access control entry that defines the permissions of the entity for the given resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlEntry readAccessControlEntry(
        CmsDbContext dbc,
        CmsResource resource,
        CmsUUID principal) throws CmsException {

        return m_userDriver.readAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);
    }

    /**
     * Reads the agent of a task.<p>
     *
     * @param dbc the current database context.
     * @param task the task to read the agent from.
     * 
     * @return the owner of a task.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsUser readAgent(CmsDbContext dbc, CmsTask task) throws CmsException {

        return readUser(dbc, task.getAgentUser());
    }

    /**
     * Reads all available backup resources for the specified resource from the OpenCms VFS.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to read the backup resources for
     * 
     * @return a List of backup resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllBackupFileHeaders(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // read the backup resources
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(dbc, resource.getRootPath());

        if (backupFileHeaders != null && backupFileHeaders.size() > 1) {
            // change the order of the list
            Collections.reverse(backupFileHeaders);
        }

        return backupFileHeaders;
    }

    /**
     * Returns a list with all project resources for a given project.<p>
     *
     * @param dbc the current database context
     * @param projectId the ID of the project
     * @return a list of all project resources
     * @throws CmsException if operation was not succesful
     */
    public List readAllProjectResources(CmsDbContext dbc, int projectId) throws CmsException {

        CmsProject project = m_projectDriver.readProject(dbc, projectId);
        List result = updateContextDates(dbc, m_projectDriver.readProjectResources(dbc, project));
        return result;
    }

    /**
     * Reads all propertydefinitions for the given mapping type.<p>
     *
     * All users are granted.
     *
     * @param dbc the current database context
     * @param mappingtype the mapping type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the mapping type. The Vector is maybe empty.
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertydefinitions(CmsDbContext dbc, int mappingtype) throws CmsException {

        List returnValue = m_vfsDriver.readPropertyDefinitions(dbc, dbc.currentProject().getId(), mappingtype);
        Collections.sort(returnValue);
        return returnValue;
    }

    /**
     * Reads a resource from the history table of the VFS.<p>
     * 
     * The reading includes the filecontent.
     * A file is read from the backup resources.
     *
     * @param dbc the current database context
     * @param tagId the id of the historic version to read
     * @param resource the resource to read the historic version of
     * 
     * @return the file read from the Cms.
     * @throws CmsException if operation was not succesful
     */
    public CmsBackupResource readBackupFile(CmsDbContext dbc, int tagId, CmsResource resource) throws CmsException {

        return m_backupDriver.readBackupFile(dbc, tagId, resource.getRootPath());
    }

    /**
     * Reads the backupinformation of a project from the Cms.<p>
     *
     * @param dbc the current database context
     * @param tagId the tagId of the project
     * 
     * @return the backup project
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(CmsDbContext dbc, int tagId) throws CmsException {

        return m_backupDriver.readBackupProject(dbc, tagId);
    }

    /**
     * Reads all resources that are inside and changed in a specified project.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the project
     * @param resourceType &lt;0 if files and folders should be read, 0 if only folders should be read, &gt;0 if only files should be read
     * @return a List with all resources inside the specified project
     * @throws CmsException if somethong goes wrong
     */
    public List readChangedResourcesInsideProject(CmsDbContext dbc, int projectId, int resourceType)
    throws CmsException {

        List projectResources = readProjectResources(dbc, readProject(dbc, projectId));
        List result = new ArrayList();
        String currentProjectResource = null;
        List resources = new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;

        for (int i = 0; i < projectResources.size(); i++) {
            // read all resources that are inside the project by visiting each project resource
            currentProjectResource = (String)projectResources.get(i);

            try {
                currentResource = readResource(dbc, currentProjectResource, CmsResourceFilter.ALL);

                if (currentResource.isFolder()) {
                    resources.addAll(readResources(dbc, currentResource, CmsResourceFilter.ALL, true));
                } else {
                    resources.add(currentResource);
                }
            } catch (CmsException e) {
                // the project resource probably doesnt exist (anymore)...
                if (e.getType() != CmsException.C_NOT_FOUND) {
                    throw e;
                }
            }
        }

        for (int j = 0; j < resources.size(); j++) {
            currentResource = (CmsResource)resources.get(j);
            currentLock = getLock(dbc, currentResource);

            if (currentResource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                if ((currentLock.isNullLock() && currentResource.getProjectLastModified() == projectId)
                    || (currentLock.getUserId().equals(dbc.currentUser().getId()) && currentLock.getProjectId() == projectId)) {
                    // add only resources that are 
                    // - inside the project,
                    // - changed in the project,
                    // - either unlocked, or locked for the current user in the project
                    if ((currentResource.isFolder() && resourceType <= 0) 
                    || (currentResource.isFile() && resourceType != 0)) { 
                        result.add(currentResource);
                    }
                }
            }
        }

        resources.clear();
        resources = null;

        // TODO the calculated resource lists should be cached

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
     * This method is mainly used by the workplace explorer.<p> 
     * 
     * @param dbc the current database context.
     * @param resource the resource to return the child resources for.
     * @param filter the resource filter to use.
     * @param getFolders if true the child folders are included in the result.
     * @param getFiles if true the child files are included in the result.
     * 
     * @return a list of all child resources.
     * 
     * @throws CmsException if something goes wrong.
     */
    public List readChildResources(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles) throws CmsException {

        // try to get the sub resources from the cache
        String cacheKey;
        if (getFolders && getFiles) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBALL;
        } else if (getFolders) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFOLDERS;
        } else {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFILES;
        }
        cacheKey = getCacheKey(
            dbc.currentUser().getName() + cacheKey + filter.getCacheId(), 
            dbc.currentProject(), 
            resource.getRootPath());
        
        List subResources = (List)m_resourceListCache.get(cacheKey);

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must however still apply the result filter and update the context dates
            return updateContextDates(dbc, subResources, filter);
        }

        // read the result form the database
        subResources = m_vfsDriver.readChildResources(
            dbc, 
            dbc.currentProject(), 
            resource, 
            getFolders, 
            getFiles);

        for (int i = 0; i < subResources.size(); i++) {
            CmsResource currentResource = (CmsResource)subResources.get(i);
            int perms = m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                filter);
            if (CmsSecurityManager.PERM_DENIED == perms) {
                subResources.remove(i--);
            }
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        // apply the result filter and update the context dates
        return updateContextDates(dbc, subResources, filter);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(CmsDbContext, String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the base file resource (without content)
     * @param filter the filter object
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsDbContext dbc, CmsResource resource, CmsResourceFilter filter)
    throws CmsException {

        if (resource.isFolder()) {
            throw new CmsException(
                "Trying to access a folder as file " + "(" + resource.getRootPath() + ")",
                CmsException.C_NOT_FOUND);
        }

        CmsFile file = m_vfsDriver.readFile(
            dbc, 
            dbc.currentProject().getId(), 
            filter.includeDeleted(), 
            resource.getStructureId());

        return file;
    }

    /**
     * Reads a folder from the VFS,
     * using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsException if something goes wrong
     *
     * @see #readResource(CmsDbContext, String, CmsResourceFilter)
     * @see CmsObject#readFolder(String)
     * @see CmsObject#readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(
        CmsDbContext dbc,
        String resourcename,
        CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(dbc, resourcename, filter);

        return convertResourceToFolder(resource);
    }

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link I_CmsConstants#C_TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul>
     *
     * @param dbc the current database context
     * @param projectId the id of the project in which the tasks are defined.
     * @param ownerName the owner of the task.
     * @param taskType the type of task you want to read.
     * @param orderBy specifies how to order the tasks.
     * @param sort sorting of the tasks.
     * 
     * @return a list of given <code>{@link CmsTask}</code> objects for a user for a project.
     * 
     * @throws CmsException if operation was not successful.
     */
    public List readGivenTasks(CmsDbContext dbc, int projectId, String ownerName, int taskType, String orderBy, String sort)
    throws CmsException {

        CmsProject project = null;

        CmsUser owner = null;

        if (ownerName != null) {
            owner = readUser(dbc, ownerName);
        }

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(dbc, projectId);
        }

        return m_workflowDriver.readTasks(dbc, project, null, owner, null, taskType, orderBy, sort);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param dbc the current database context.
     * @param project the project to read from.
     * 
     * @return the group of a resource.
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsProject project) {

        // try to read group form cache
        CmsGroup group = (CmsGroup)m_groupCache.get(new CacheId(project.getGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(dbc, project.getGroupId());
            } catch (CmsException exc) {
                if (exc.getType() == CmsException.C_NO_GROUP) {
                    // the group does not exist any more - return a dummy-group
                    return new CmsGroup(
                        CmsUUID.getNullUUID(),
                        CmsUUID.getNullUUID(),
                        project.getGroupId() + "",
                        "deleted group",
                        0);
                }
            }
            m_groupCache.put(new CacheId(group), group);
        }

        return group;
    }

    /**
     * Reads the group (role) of a task.<p>
     *
     * @param dbc the current database context.
     * @param task the task to read from.
     * 
     * @return the group of a resource.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsTask task) throws CmsException {

        return m_userDriver.readGroup(dbc, task.getRole());
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param dbc the current database context.
     * @param groupId the id of the group that is to be read.
     * 
     * @return the requested group.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsException {

        return m_userDriver.readGroup(dbc, groupId);
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param dbc the current database context.
     * @param groupname the name of the group that is to be read.
     *
     * @return the requested group.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsDbContext dbc, String groupname) throws CmsException {

        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(groupname));
        if (group == null) {
            group = m_userDriver.readGroup(dbc, groupname);
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Reads the manager group of a project.<p>
     *
     * @param dbc the current database context.
     * @param project the project to read from.
     * 
     * @return the group of a resource.
     */
    public CmsGroup readManagerGroup(CmsDbContext dbc, CmsProject project) {

        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(project.getManagerGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(dbc, project.getManagerGroupId());
            } catch (CmsException exc) {
                if (exc.getType() == CmsException.C_NO_GROUP) {
                    // the group does not exist any more - return a dummy-group
                    return new CmsGroup(
                        CmsUUID.getNullUUID(),
                        CmsUUID.getNullUUID(),
                        project.getManagerGroupId() + "",
                        "deleted group",
                        0);
                }
            }
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Reads the original agent of a task.<p>
     *
     * @param dbc the current database context.
     * @param task the task to read the original agent from.
     * 
     * @return the owner of a task.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsUser readOriginalAgent(CmsDbContext dbc, CmsTask task) throws CmsException {

        return readUser(dbc, task.getOriginalUser());
    }

    /**
     * Reads the owner of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to get the owner from
     * 
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsDbContext dbc, CmsProject project) throws CmsException {

        return readUser(dbc, project.getOwnerId());
    }

    /**
     * Reads the owner (initiator) of a task.<p>
     * 
     * @param dbc the current database context.
     * @param task the task to read the owner from.
     * 
     * @return the owner of a task.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsUser readOwner(CmsDbContext dbc, CmsTask task) throws CmsException {

        return readUser(dbc, task.getInitiatorUser());
    }

    /**
     * Reads the owner of a tasklog.<p>
     *
     * @param dbc the current database context.
     * @param log the tasklog.
     * 
     * @return the owner of a resource.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsUser readOwner(CmsDbContext dbc, CmsTaskLog log) throws CmsException {

        return readUser(dbc, log.getUser());
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsDbContext dbc, int projectId, String path, CmsResourceFilter filter) throws CmsException {

        // splits the path into folder and filename tokens
        StringTokenizer tokens = null;
        // # of folders in the path
        int folderCount = 0;
        // true if the path doesn't end with a folder
        boolean lastResourceIsFile = false;
        // holds the CmsResource instances in the path
        List pathList = null;
        // the current path token
        String currentResourceName = null;
        // the current path
        String currentPath = null;
        // the current resource
        CmsResource currentResource = null;
        // this is a comment. i love comments!
        int i = 0, count = 0;
        // key to cache the resources
        String cacheKey = null;

        tokens = new StringTokenizer(path, I_CmsConstants.C_FOLDER_SEPARATOR);

        // the root folder is no token in the path but a resource which has to be added to the path
        count = tokens.countTokens() + 1;
        pathList = new ArrayList(count);

        folderCount = count;
        if (!path.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = I_CmsConstants.C_ROOT;
        currentPath = I_CmsConstants.C_ROOT;
        cacheKey = getCacheKey(null, projectId, currentPath);
        if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
            currentResource = m_vfsDriver.readFolder(dbc, projectId, currentPath);
            m_resourceCache.put(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);

        if (count == 1) {
            // the root folder was requested- no further operations required
            return pathList;
        }

        currentResourceName = tokens.nextToken();

        // read the folder resources in the path /a/b/c/
        for (i = 1; i < folderCount; i++) {
            currentPath += currentResourceName + I_CmsConstants.C_FOLDER_SEPARATOR;

            // read the folder
            cacheKey = getCacheKey(null, projectId, currentPath);
            if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
                currentResource = m_vfsDriver.readFolder(dbc, projectId, currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);

            if (i < folderCount - 1) {
                currentResourceName = tokens.nextToken();
            }
        }

        // read the (optional) last file resource in the path /x.html
        if (lastResourceIsFile) {
            if (tokens.hasMoreTokens()) {
                // this will only be false if a resource in the 
                // top level root folder (e.g. "/index.html") was requested
                currentResourceName = tokens.nextToken();
            }
            currentPath += currentResourceName;

            // read the file
            cacheKey = getCacheKey(null, projectId, currentPath);
            if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
                currentResource = m_vfsDriver.readResource(dbc, projectId, currentPath, filter.includeDeleted());
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);
        }

        return pathList;
    }

    /**
     * Reads a project of a given task.<p>
     *
     * @param dbc the current database context.
     * @param task the task to read the project of.
     * 
     * @return the project of the task.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsDbContext dbc, CmsTask task) throws CmsException {

        // read the parent of the task, until it has no parents.
        while (task.getParent() != 0) {
            task = readTask(dbc, task.getParent());
        }
        return m_workflowDriver.readProject(dbc, task);
    }

    /**
     * Reads a project from the Cms given the projects name.<p>
     *
     * @param dbc the current database context
     * @param id the id of the project
     * 
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsDbContext dbc, int id) throws CmsException {

        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(dbc, id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(CmsDbContext, int)} to ensure the right project is read.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the project
     * 
     * @return the project read from the cms
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsDbContext dbc, String name) throws CmsException {

        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(name);
        if (project == null) {
            project = m_projectDriver.readProject(dbc, name);
            m_projectCache.put(name, project);
        }
        return project;
    }

    /**
     * Reads all task log entries for a project.
     *
     * @param dbc the current database context.
     * @param projectId the id of the project for which the tasklog will be read.
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects.
     * 
     * @throws CmsException if something goes wrong.
     */
    public List readProjectLogs(CmsDbContext dbc, int projectId) throws CmsException {

        return m_workflowDriver.readProjectLogs(dbc, projectId);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param dbc the current database context
     * @param project the project to get the project resources for
     * 
     * @return the list of all resource names that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsException {

        return m_projectDriver.readProjectResources(dbc, project);
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
     * @param dbc the current database context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria.
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(int, int)
     */
    public List readProjectView(CmsDbContext dbc, int projectId, int state) throws CmsException {

        List resources;
        if ((state == I_CmsConstants.C_STATE_NEW)
            || (state == I_CmsConstants.C_STATE_CHANGED)
            || (state == I_CmsConstants.C_STATE_DELETED)) {
            // get all resources form the database that match the selected state
            resources = m_vfsDriver.readResources(dbc, projectId, state, I_CmsConstants.C_READMODE_MATCHSTATE);
        } else {
            // get all resources form the database that are somehow changed (i.e. not unchanged)
            resources = m_vfsDriver.readResources(
                dbc,
                projectId,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READMODE_UNMATCHSTATE);
        }

        List result = new ArrayList(resources.size());
        for (int i = 0; i < resources.size(); i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);
            if (CmsSecurityManager.PERM_ALLOWED == m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                CmsResourceFilter.ALL)) {

                result.add(currentResource);
            }
        }

        // free memory
        resources.clear();
        resources = null;

        // set the full resource names
        updateContextDates(dbc, result);
        // sort the result
        Collections.sort(result);

        return result;
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the property definition to read
     *
     * @return the property definition that was read, or null if there is no property definition with the given name
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition readPropertydefinition(CmsDbContext dbc, String name)
    throws CmsException {

        return m_vfsDriver.readPropertyDefinition(dbc, name, dbc.currentProject().getId());
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource where the property is read from
     * @param key the property key name
     * @param search if <code>true</code>, the property is searched on all parent folders of the resource. 
     *      if it's not found attached directly to the resource.
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found.
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, CmsResource resource, String key, boolean search)
    throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(key + search, dbc.currentProject().getId(), resource.getRootPath());
        CmsProperty value = (CmsProperty)m_propertyCache.get(cacheKey);

        if (value == null) {
            // check if the map of all properties for this resource is already cached
            String cacheKey2 = getCacheKey(
                C_CACHE_ALL_PROPERTIES + search, 
                dbc.currentProject().getId(), 
                resource.getRootPath());
            
            List allProperties = (List)m_propertyCache.get(cacheKey2);

            if (allProperties != null) {
                // list of properties already read, look up value there 
                for (int i = 0; i < allProperties.size(); i++) {
                    CmsProperty property = (CmsProperty)allProperties.get(i);
                    if (property.getKey().equals(key)) {
                        value = property;
                        break;
                    }
                }
            } else if (search) {
                // result not cached, look it up recursivly with search enabled
                String cacheKey3 = getCacheKey(key + false, dbc.currentProject().getId(), resource.getRootPath());
                value = (CmsProperty)m_propertyCache.get(cacheKey3);

                if ((value == null) || value.isNullProperty()) {
                    boolean cont;
                    do {
                        try {
                            value = readPropertyObject(dbc, resource, key, false);
                            cont = (value.isNullProperty() && (!"/".equals(resource.getRootPath())));
                        } catch (CmsSecurityException se) {
                            // a security exception (probably no read permission) we return the current result                      
                            cont = false;
                        }
                        if (cont) {
                            // no permission check on parent folder is required since we must have "read" 
                            // permissions to read the child resource anyway
                            resource = readResource(
                                dbc,
                                CmsResource.getParentFolder(resource.getRootPath()),
                                CmsResourceFilter.ALL);
                        }
                    } while (cont);
                }
            } else {
                // result not cached, look it up in the DB without search
                value = m_vfsDriver.readPropertyObject(dbc, key, dbc.currentProject(), resource);
            }
            if (value == null) {
                value = CmsProperty.getNullProperty();
            }

            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }

        return value;
    }

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * Returns an empty list if no properties are found at all.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource where the properties are read from
     * @param search true, if the properties should be searched on all parent folders  if not found on the resource
     * 
     * @return a list of CmsProperty objects containing the structure and/or resource value
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(CmsDbContext dbc, CmsResource resource, boolean search)
    throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(
            C_CACHE_ALL_PROPERTIES + search, 
            dbc.currentProject().getId(), 
            resource.getRootPath());
        
        List properties = (List)m_propertyCache.get(cacheKey);

        if (properties == null) {
            // result not cached, let's look it up in the DB
            if (search) {
                boolean cont;
                properties = new ArrayList();
                List parentProperties = null;

                do {
                    try {
                        parentProperties = readPropertyObjects(dbc, resource, false);

                        // make sure properties from lower folders "overwrite" properties from upper folders
                        parentProperties.removeAll(properties);
                        parentProperties.addAll(properties);

                        properties.clear();
                        properties.addAll(parentProperties);

                        cont = !"/".equals(resource.getRootPath());
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        // no permission check on parent folder is required since we must have "read" 
                        // permissions to read the child resource anyway
                        resource = readResource(
                            dbc,
                            CmsResource.getParentFolder(resource.getRootPath()),
                            CmsResourceFilter.ALL);
                    }
                } while (cont);
            } else {
                properties = m_vfsDriver.readPropertyObjects(dbc, dbc.currentProject(), resource);
            }

            // store the result in the driver manager's cache
            m_propertyCache.put(cacheKey, properties);
        }

        return new ArrayList(properties);
    }
    
    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * 
     * @return a List of CmsPublishedResource objects
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishedResources(dbc, dbc.currentProject().getId(), publishHistoryId);
    }

    /**
     * Reads all project resources that belong to a given view criteria. <p>
     * 
     * A view criteria can be "new", "changed" and "deleted" and the result 
     * contains those resources in the project whose
     * state is equal to the selected value.
     * 
     * @param dbc the current database context
     * @param projectId the preoject to read from
     * @param criteria the view criteria, can be "new", "changed" or "deleted"
     * 
     * @return all project resources that belong to the given view criteria
     * @throws CmsException if something goes wrong
     */
    public List readPublishProjectView(CmsDbContext dbc, int projectId, String criteria) throws CmsException {

        List retValue = new Vector();
        List resources = m_projectDriver.readProjectView(dbc, projectId, criteria);
        boolean onlyLocked = false;

        // check if only locked resources should be displayed
        if ("locked".equalsIgnoreCase(criteria)) {
            onlyLocked = true;
        }

        // check the security
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource currentResource = (CmsResource)i.next();
            if (CmsSecurityManager.PERM_ALLOWED == m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                CmsResourceFilter.ALL)) {

                if (onlyLocked) {
                    // check if resource is locked
                    CmsLock lock = getLock(dbc, currentResource);
                    if (!lock.isNullLock()) {
                        retValue.add(currentResource);
                    }
                } else {
                    // add all resources with correct permissions
                    retValue.add(currentResource);
                }
            }
        }

        return retValue;

    }

    /**
     * Reads a resource from the VFS, using the specified resource filter.<p>
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
    public CmsResource readResource(
        CmsDbContext dbc,
        String resourcePath,
        CmsResourceFilter filter) throws CmsException {

        CmsResource resource = m_vfsDriver.readResource(
            dbc,
            dbc.currentProject().getId(),
            resourcePath,
            filter.includeDeleted());

        // context dates need to be updated even if filter was applied
        updateContextDates(dbc, resource);

        // return the resource
        return resource;
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param dbc the current database context.
     * @param parent the parent path to read the resources from.
     * @param filter the filter.
     * @param readTree <code>true</code> to read all subresources.
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria.
     *  
     * @throws CmsException if something goes wrong
     */
    public List readResources(CmsDbContext dbc, CmsResource parent, CmsResourceFilter filter, boolean readTree)
    throws CmsException {

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(
            dbc.currentUser().getName() + filter.getCacheId() + readTree, 
            dbc.currentProject(), 
            parent.getRootPath());

        List subResources = (List)m_resourceListCache.get(cacheKey);

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must however still apply the result filter and update the context dates
            return updateContextDates(dbc, subResources, filter);
        }

        // read the result form the database
        subResources = m_vfsDriver
            .readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                (readTree ? parent.getRootPath() : parent.getStructureId().toString()),
                filter.getType(),
                filter.getState(),
                filter.getModifiedAfter(),
                filter.getModifiedBefore(),
                (readTree ? I_CmsConstants.C_READMODE_INCLUDE_TREE : I_CmsConstants.C_READMODE_EXCLUDE_TREE)
                    | (filter.excludeType() ? I_CmsConstants.C_READMODE_EXCLUDE_TYPE : 0)
                    | (filter.excludeState() ? I_CmsConstants.C_READMODE_EXCLUDE_STATE : 0)
                    | ((filter.getOnlyFolders() != null) 
                        ? (filter.getOnlyFolders().booleanValue() 
                            ? I_CmsConstants.C_READMODE_ONLY_FOLDERS
                            : I_CmsConstants.C_READMODE_ONLY_FILES)
                        : 0));

        for (int i = 0; i < subResources.size(); i++) {
            CmsResource currentResource = (CmsResource)subResources.get(i);
            int perms = m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                filter);
            
            if (perms != CmsSecurityManager.PERM_ALLOWED) {
                subResources.remove(i--);
            }
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        // apply the result filter and update the context dates
        return updateContextDates(dbc, subResources, filter);
    }
    
    /**
     * Reads all resources that have a value set for the specified property (definition) in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param dbc the current database context
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * 
     * @return all resources that have a value set for the specified property (definition) in the given path
     * 
     * @throws CmsException if something goes wrong
     */    
    public List readResourcesWithProperty(CmsDbContext dbc, String path, String propertyDefinition)
    throws CmsException {

        List extractedResources = null;
        
        String cacheKey = getCacheKey(
            "_ResourcesWithProperty", 
            dbc.currentProject(), 
            path + "_" + propertyDefinition);
        
        if ((extractedResources = (List)m_resourceListCache.get(cacheKey)) == null) {
            
            // first read the property definition
            CmsPropertydefinition propDef = readPropertydefinition(dbc, propertyDefinition);            
            
            // now read the list of resources that have a value set for the property definition
            List resources = m_vfsDriver.readResourcesWithProperty(dbc, dbc.currentProject().getId(), propDef.getId());
            
            if ("/".equals(path)) {            
                // root path - return all resources found 
                extractedResources = resources;                            
            } else {
                // other path - sort out resources that are in the required folder
                extractedResources = new ArrayList();

                for (Iterator i = resources.iterator(); i.hasNext();) {
                    CmsResource res = (CmsResource)i.next();
                    if (res.getRootPath().startsWith(path, 0)) {
                        extractedResources.add(res);
                    }
                }            
            }

            m_resourceListCache.put(cacheKey, extractedResources);
        }

        return extractedResources;
    }

    /**
     * Returns a List of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to read the siblings for
     * @param filter a filter object
     * 
     * @return a List of CmsResources that are siblings to the specified resource, including the specified resource itself 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceFilter filter) throws CmsException {

        List siblings = m_vfsDriver.readSiblings(
            dbc, 
            dbc.currentProject(), 
            resource, 
            filter.includeDeleted());

        // important: there is no permission check done on the returned list of siblings
        // this is because of possible issues with the "publish all siblings" option,
        // moreover the user has read permission for the content through
        // the selected sibling anyway

        return updateContextDates(dbc, siblings, filter);
    }

    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param dbc the current database context.
     * @param rfsName the rfs name of the resource.
     * 
     * @return the paramter string of the requested resource.
     * 
     * @throws CmsException if something goes wrong.
     */
    public String readStaticExportPublishedResourceParameters(CmsDbContext dbc, String rfsName)
    throws CmsException {

        return m_projectDriver.readStaticExportPublishedResourceParameters(dbc, dbc.currentProject(), rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param dbc the current database context.
     * @param parameterResources flag for reading resources with parameters (1) or without (0).
     * @param timestamp for reading the data from the db.
     * 
     * @return a list of template resources as <code>{@link String}</code> objects.
     * 
     * @throws CmsException if something goes wrong.
     */
    public List readStaticExportResources(CmsDbContext dbc, int parameterResources, long timestamp)
    throws CmsException {

        return m_projectDriver.readStaticExportResources(dbc, dbc.currentProject(), parameterResources, timestamp);
    }

    /**
     * Reads the task with the given id.<p>
     *
     * @param dbc the current database context.
     * @param id the id for the task to read.
     * 
     * @return the task with the given id.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsTask readTask(CmsDbContext dbc, int id) throws CmsException {

        return m_workflowDriver.readTask(dbc, id);
    }

    /**
     * Reads log entries for a task.<p>
     *
     * @param dbc the current satabase context.
     * @param taskid the task for the tasklog to read.
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects.
     * 
     * @throws CmsException if something goes wrong.
     */
    public List readTaskLogs(CmsDbContext dbc, int taskid) throws CmsException {

        return m_workflowDriver.readTaskLogs(dbc, taskid);
    }

    /**
     * Reads all tasks for a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values are:<br>
     * <ul>
     * <il><code>{@link I_CmsConstants#C_TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul><p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project in which the tasks are defined. Can be null to select all tasks.
     * @param tasktype the type of task you want to read.
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order: C_SORT_ASC, C_SORT_DESC, or null.
     * 
     * @return a list of <code>{@link CmsTask}</code> objects for the project.
     * 
     * @throws CmsException if operation was not successful.
     */
    public List readTasksForProject(CmsDbContext dbc, int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        CmsProject project = null;

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(dbc, projectId);
        }
        return m_workflowDriver.readTasks(dbc, project, null, null, null, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a role in a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link I_CmsConstants#C_TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul><p>
     *
     * @param dbc the current database context.
     * @param projectId the id of the Project in which the tasks are defined.
     * @param roleName the role who has to process the task.
     * @param tasktype the type of task you want to read.
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null.
     * 
     * @return list of <code>{@link CmsTask}</code> objects for the role.
     * 
     * @throws CmsException if operation was not successful.
     */
    public List readTasksForRole(CmsDbContext dbc, int projectId, String roleName, int tasktype, String orderBy, String sort)
    throws CmsException {

        CmsProject project = null;
        CmsGroup role = null;

        if (roleName != null) {
            role = readGroup(dbc, roleName);
        }

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(dbc, projectId);
        }

        return m_workflowDriver.readTasks(dbc, project, null, null, role, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a user in a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link I_CmsConstants#C_TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link I_CmsConstants#C_TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul>
     *
     * @param dbc the current database context.
     * @param projectId the id of the Project in which the tasks are defined.
     * @param userName the user who has to process the task.
     * @param taskType the type of task you want to read.
     * @param orderBy specifies how to order the tasks.
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null.
     * 
     * @return a list of <code>{@link CmsTask}</code> objects for the user .
     * 
     * @throws CmsException if operation was not successful.
     */
    public List readTasksForUser(CmsDbContext dbc, int projectId, String userName, int taskType, String orderBy, String sort)
    throws CmsException {

        CmsUser user = readUser(dbc, userName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsProject project = null;
        // try to read the project, if projectId == -1 we must return the tasks of all projects
        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = m_projectDriver.readProject(dbc, projectId);
        }
        return m_workflowDriver.readTasks(dbc, project, user, null, null, taskType, orderBy, sort);
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * @param dbc the current database context.
     * @param id the id of the user to read.
     *
     * @return the user read.
     * 
     * @throws CmsException if something goes wrong.
     */
    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsException {

        CmsUser user = null;
        user = getUserFromCache(id);
        if (user == null) {
            user = m_userDriver.readUser(dbc, id);
            putUserInCache(user);
        }
        return user;
    }

    /**
     * Returns a user object.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user that is to be read.
     *
     * @return user read.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsUser readUser(CmsDbContext dbc, String username) throws CmsException {

        return readUser(dbc, username, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
    }

    /**
     * Returns a user object.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user that is to be read.
     * @param type the type of the user.
     *
     * @return user read.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsUser readUser(CmsDbContext dbc, String username, int type) throws CmsException {

        CmsUser user = getUserFromCache(username, type);
        if (user == null) {
            user = m_userDriver.readUser(dbc, username, type);
            putUserInCache(user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param dbc the current database context.
     * @param username the username of the user that is to be read.
     * @param password the password of the user that is to be read.
     * 
     * @return user read.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public CmsUser readUser(CmsDbContext dbc, String username, String password) throws CmsException {

        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(dbc, username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Read a web user from the database.<p>
     * 
     * @param dbc the current database context.
     * @param username the web user to read.
     * 
     * @return the read web user.
     * 
     * @throws CmsException if the user could not be read. 
     */
    public CmsUser readWebUser(CmsDbContext dbc, String username) throws CmsException {

        return readUser(dbc, username, I_CmsConstants.C_USER_TYPE_WEBUSER);
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param dbc the current database context.
     * @param username the username of the user that is to be read.
     * @param password the password of the user that is to be read.
     * 
     * @return the webuser read.
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsDbContext dbc, String username, String password) throws CmsException {

        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(dbc, username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * @param dbc the current database context.
     * @param resource the resource.
     * @param principal the id of the principal to remove the the access control entry for.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void removeAccessControlEntry(
        CmsDbContext dbc,
        CmsResource resource,
        CmsUUID principal) throws CmsException {

        // remove the ace
        m_userDriver.removeAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);

        // update the "last modified" information
        touch(
            dbc,
            resource,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED);

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user that is to be removed from the group.
     * @param groupname the name of the group.
     *
     * @throws CmsException if operation was not succesful.
     */
    public void removeUserFromGroup(
        CmsDbContext dbc,
        String username,
        String groupname) throws CmsException {

        // test if this user is existing in the group
        if (!userInGroup(dbc, username, groupname)) {
            // user already there, throw exception
            throw new CmsException(
                "[" + getClass().getName() + "] remove " + username + " from " + groupname,
                CmsException.C_NO_USER);
        }

        CmsUser user;
        CmsGroup group;

        user = readUser(dbc, username);

        //check if the user exists
        if (user != null) {
            group = readGroup(dbc, groupname);
            //check if group exists
            if (group != null) {
                m_userDriver.deleteUserInGroup(dbc, user.getId(), group.getId());
                m_userGroupsCache.clear();
            } else {
                throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
            }
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] removeUserFromGroup()",
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see I_CmsResourceType#replaceResource(CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsDbContext dbc,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException {

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(
            dbc,
            dbc.currentUser(),
            dbc.currentProject(),
            resource,
            content,
            type);

        if ((properties != null) && (properties != Collections.EMPTY_LIST)) {
            // write the properties
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), resource, properties);
            m_propertyCache.clear();
        }

        // update the resource state
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());

        touch(
            dbc,
            resource,
            System.currentTimeMillis(),
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED);

        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, C_UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();
        content = null;

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }    

    /**
     * Resets the password for a specified user.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user.
     * @param oldPassword the old password.
     * @param newPassword the new password.
     * 
     * @throws CmsException if the user data could not be read from the database.
     * @throws CmsSecurityException if the specified username and old password could not be verified.
     */
    public void resetPassword(CmsDbContext dbc, String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        if (oldPassword != null && newPassword != null) {

            CmsUser user = null;

            validatePassword(newPassword);

            // read the user as a system user to verify that the specified old password is correct
            try {
                user = m_userDriver.readUser(dbc, username, oldPassword, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NO_USER) {
                    throw new CmsException("["
                        + getClass().getName()
                        + "] Error resetting password for user '"
                        + username
                        + "'", CmsException.C_UNKNOWN_EXCEPTION);
                }
            }

            // dito as a web user
            try {
                user = (user != null) ? user : m_userDriver.readUser(
                    dbc,
                    username,
                    oldPassword,
                    I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NO_USER) {
                    throw new CmsException("["
                        + getClass().getName()
                        + "] Error resetting password for user '"
                        + username
                        + "'", CmsException.C_UNKNOWN_EXCEPTION);
                }
            }

            if (user == null) {
                // the specified username + old password don't match
                throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED);
            }

            m_userDriver.writePassword(dbc, username, user.getType(), oldPassword, newPassword);

        } else {
            throw new CmsException(
                "[" + getClass().getName() + "] Missing old/new password",
                CmsException.C_UNKNOWN_EXCEPTION);
        }
    }

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see I_CmsResourceType#restoreResourceBackup(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsDbContext dbc, CmsResource resource, int tag)
    throws CmsException {

        int state = I_CmsConstants.C_STATE_CHANGED;

        CmsBackupResource backupFile = readBackupFile(dbc, tag, resource);
        if (resource.getState() == I_CmsConstants.C_STATE_NEW) {
            state = I_CmsConstants.C_STATE_NEW;
        }

        if (backupFile != null) {
            // get the backed up flags 
            int flags = backupFile.getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }

            CmsFile newFile = new CmsFile(
                resource.getStructureId(),
                resource.getResourceId(),
                backupFile.getContentId(),
                resource.getRootPath(),
                backupFile.getTypeId(),
                flags,
                dbc.currentProject().getId(),
                state,
                resource.getDateCreated(),
                backupFile.getUserCreated(),
                resource.getDateLastModified(),
                dbc.currentUser().getId(),
                backupFile.getDateReleased(),
                backupFile.getDateExpired(),
                backupFile.getSiblingCount(),
                backupFile.getLength(),
                backupFile.getContents());

            writeFile(dbc, newFile);

            // now read the backup properties
            List backupProperties = m_backupDriver.readBackupProperties(dbc, backupFile);
            // remove all properties
            deleteAllProperties(dbc, newFile.getRootPath());
            // write them to the restored resource
            writePropertyObjects(dbc, newFile, backupProperties);

            clearResourceCache();
        }

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Set a new name for a task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task to set the percentage.
     * @param name the new name value.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void setName(CmsDbContext dbc, int taskId, String name) throws CmsException {

        if ((name == null) || name.length() == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }
        CmsTask task = m_workflowDriver.readTask(dbc, taskId);
        task.setName(name);
        task = m_workflowDriver.writeTask(dbc, task);
        m_workflowDriver.writeSystemTaskLog(dbc, taskId, "Name was set to "
            + name
            + "% from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + ".");
    }

    /**
     * Sets a new parent-group for an already existing group.<p>
     *
     * @param dbc the current database context.
     * @param groupName the name of the group that should be written.
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     *
     * @throws CmsException if operation was not succesfull.
     */
    public void setParentGroup(
        CmsDbContext dbc,
        String groupName,
        String parentGroupName) throws CmsException {

        // Check the security
        if (isAdmin(dbc)) {
            CmsGroup group = readGroup(dbc, groupName);
            CmsUUID parentGroupId = CmsUUID.getNullUUID();

            // if the group exists, use its id, else set to unknown.
            if (parentGroupName != null) {
                parentGroupId = readGroup(dbc, parentGroupName).getId();
            }

            group.setParentId(parentGroupId);

            // write the changes to the cms
            writeGroup(dbc, group);
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] setParentGroup() " + groupName,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param dbc the current database context.
     * @param username the name of the user.
     * @param newPassword the new password.
     * 
     * @throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsDbContext dbc, String username, String newPassword) throws CmsException {

        if (isAdmin(dbc)) {

            CmsUser user = null;

            validatePassword(newPassword);

            // read the user as a system user to verify that the specified old password is correct
            try {
                user = m_userDriver.readUser(dbc, username, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NO_USER) {
                    throw new CmsException("["
                        + getClass().getName()
                        + "] Error resetting password for user '"
                        + username
                        + "'", CmsException.C_UNKNOWN_EXCEPTION);
                }
            }

            // dito as a web user
            try {
                user = (user != null) ? user : m_userDriver.readUser(
                    dbc,
                    username,
                    I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NO_USER) {
                    throw new CmsException("["
                        + getClass().getName()
                        + "] Error resetting password for user '"
                        + username
                        + "'", CmsException.C_UNKNOWN_EXCEPTION);
                }
            }

            if (user == null) {
                // the specified username + old password don't match
                throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED);
            }

            m_userDriver.writePassword(dbc, username, user.getType(), null, newPassword);

        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] setPassword() " + username,
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Set priority of a task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task to set the percentage.
     * @param priority the priority value.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void setPriority(CmsDbContext dbc, int taskId, int priority) throws CmsException {

        CmsTask task = m_workflowDriver.readTask(dbc, taskId);
        task.setPriority(priority);
        task = m_workflowDriver.writeTask(dbc, task);
        m_workflowDriver.writeSystemTaskLog(dbc, taskId, "Priority was set to "
            + priority
            + " from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + ".");
    }

    /**
     * Set a Parameter for a task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task.
     * @param parName name of the parameter.
     * @param parValue value if the parameter.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void setTaskPar(CmsDbContext dbc, int taskId, String parName, String parValue) throws CmsException {

        m_workflowDriver.writeTaskParameter(dbc, taskId, parName, parValue);
    }

    /**
     * Set the timeout of a task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task to set the percentage.
     * @param timeout new timeout value.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void setTimeout(CmsDbContext dbc, int taskId, long timeout) throws CmsException {

        CmsTask task = m_workflowDriver.readTask(dbc, taskId);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        task.setTimeOut(timestamp);
        task = m_workflowDriver.writeTask(dbc, task);
        m_workflowDriver.writeSystemTaskLog(dbc, taskId, "Timeout was set to "
            + timeout
            + " from "
            + dbc.currentUser().getFirstname()
            + " "
            + dbc.currentUser().getLastname()
            + ".");
    }

    /**
     * Change the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expires" date of a resource.<p>
     * 
     * @param dbc the current database context
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
     * @see I_CmsResourceType#touch(CmsObject, CmsSecurityManager, CmsResource, long, long, long, boolean)
     */
    public void touch(
        CmsDbContext dbc,
        CmsResource resource,
        long dateLastModified,
        long dateReleased,
        long dateExpired) throws CmsException {

        // modify the last modification date if it's not set to C_DATE_UNCHANGED
        if (dateLastModified != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateLastModified(dateLastModified);
        }
        // modify the release date if it's not set to C_DATE_UNCHANGED
        if (dateReleased != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateReleased(dateReleased);
        }
        // modify the expired date if it's not set to C_DATE_UNCHANGED
        if (dateReleased != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateExpired(dateExpired);
        }
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, C_UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();

        // fire the event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see I_CmsResourceType#undoChanges(CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undoChanges(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        if (resource.getState() == I_CmsConstants.C_STATE_NEW) {
            // undo changes is impossible on a new resource
            throw new CmsVfsException("Undo changes is not possible on a new resource '"
                + dbc.removeSiteRoot(resource.getRootPath())
                + "'", CmsVfsException.C_VFS_UNDO_CHANGES_NOT_POSSIBLE_ON_NEW_RESOURCE);
        }

        // we need this for later use
        CmsProject onlineProject = readProject(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID);

        // change folder or file?
        if (resource.isFolder()) {

            // read the resource from the online project
            CmsFolder onlineFolder = m_vfsDriver.readFolder(
                dbc, 
                I_CmsConstants.C_PROJECT_ONLINE_ID, 
                resource.getRootPath());
            
            CmsFolder restoredFolder = new CmsFolder(
                resource.getStructureId(),
                resource.getResourceId(),
                resource.getRootPath(),
                onlineFolder.getTypeId(),
                onlineFolder.getFlags(),
                dbc.currentProject().getId(),
                I_CmsConstants.C_STATE_UNCHANGED,
                onlineFolder.getDateCreated(),
                onlineFolder.getUserCreated(),
                onlineFolder.getDateLastModified(),
                onlineFolder.getUserLastModified(),
                resource.getSiblingCount(),
                onlineFolder.getDateReleased(),
                onlineFolder.getDateExpired());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFolder.setDateLastModified(onlineFolder.getDateLastModified());

            // write the folder
            m_vfsDriver.writeResource(dbc, dbc.currentProject(), restoredFolder, C_NOTHING_CHANGED);

            // restore the properties form the online project
            m_vfsDriver.deletePropertyObjects(
                dbc,
                dbc.currentProject().getId(),
                restoredFolder,
                CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            
            List propertyInfos = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineFolder);
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), restoredFolder, propertyInfos);

            // restore the access control entries form the online project
            m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());
            ListIterator aceList = 
                m_userDriver.readAccessControlEntries(dbc, onlineProject, resource.getResourceId(), false).listIterator();
            
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(
                    dbc,
                    dbc.currentProject(),
                    resource.getResourceId(),
                    ace.getPrincipal(),
                    ace.getPermissions().getAllowedPermissions(),
                    ace.getPermissions().getDeniedPermissions(),
                    ace.getFlags());
            }
        } else {

            // read the file from the online project
            CmsFile onlineFile = this.m_vfsDriver.readFile(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID, true, resource
                .getStructureId());

            CmsFile restoredFile = 
                new CmsFile(
                    onlineFile.getStructureId(), 
                    onlineFile.getResourceId(), 
                    onlineFile.getContentId(), 
                    resource.getRootPath(), 
                    onlineFile.getTypeId(), 
                    onlineFile.getFlags(), 
                    dbc.currentProject().getId(), 
                    I_CmsConstants.C_STATE_UNCHANGED, 
                    onlineFile.getDateCreated(),
                    onlineFile.getUserCreated(),
                    onlineFile.getDateLastModified(),
                    onlineFile.getUserLastModified(),
                    onlineFile.getDateReleased(), 
                    onlineFile.getDateExpired(), 
                    0, 
                    onlineFile.getLength(), 
                    onlineFile.getContents());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineFile.getDateLastModified());

            // collect the properties
            List properties = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineFile);

            // implementation notes: 
            // undo changes can become complex e.g. if a resource was deleted, and then 
            // another resource was copied over the deleted file as a sibling
            // therefore we must "clean" delete the offline resource, and then create 
            // an new resource with the create method
            // note that this does NOT apply to folders, since a folder cannot be replaced
            // like a resource anyway
            deleteResource(dbc, resource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            CmsResource res = createResource(
                dbc,
                restoredFile.getRootPath(),
                restoredFile,
                restoredFile.getContents(),
                properties,
                false);

            // copy the access control entries form the online project
            m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());            
            ListIterator aceList = m_userDriver.readAccessControlEntries(
                dbc,
                onlineProject,
                onlineFile.getResourceId(),
                false).listIterator();
            
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(
                    dbc, 
                    dbc.currentProject(), 
                    res.getResourceId(),
                    ace.getPrincipal(),
                    ace.getPermissions().getAllowedPermissions(),
                    ace.getPermissions().getDeniedPermissions(),
                    ace.getFlags());
            }

            // rest the state to unchanged 
            res.setState(I_CmsConstants.C_STATE_UNCHANGED);
            m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), res, C_UPDATE_ALL);
        }

        // update the cache
        clearResourceCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Unlocks all resources in the given project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param dbc the current database context
     * @param projectId the id of the project to be published
     * @throws CmsException if something goes wrong
     */
    public void unlockProject(CmsDbContext dbc, int projectId) throws CmsException {

        // read the project
        CmsProject project = readProject(dbc, projectId);
        // check the security
        if ((isAdmin(dbc) || isManagerOfProject(dbc))
            && (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // unlock all resources in the project
            m_lockManager.removeResourcesInProject(projectId);
            clearResourceCache();
            m_projectCache.clear();
            // we must also clear the permission cache
            m_securityManager.clearPermissionCache();

        } else if (!isAdmin(dbc) && !isManagerOfProject(dbc)) {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] unlockProject() " + projectId,
                CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] unlockProject() " + projectId,
                CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to unlock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see I_CmsResourceType#unlockResource(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        // update the resource cache
        clearResourceCache();

        // now update lock status
        m_lockManager.removeResource(this, dbc, resource, false);

        // we must also clear the permission cache
        m_securityManager.clearPermissionCache();

        // fire resource modification event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Updates the context dates with each resource in a list of CmsResources.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of CmsResources
     * 
     * @return the original list of CmsResources with the full resource name set 
     */
    public List updateContextDates(CmsDbContext dbc, List resourceList) {

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            updateContextDates(dbc, res);
        }

        return resourceList;
    }

    /**
     * Update the export points.<p>
     * 
     * All files and folders "inside" an export point are written.<p>
     * 
     * @param dbc the current database context
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     */
    public void updateExportPoints(CmsDbContext dbc, I_CmsReport report) {

        try {
            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the report may be null if the export point update was started by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }

            // the export point hash table contains RFS export paths keyed by their internal VFS paths
            Iterator i = exportPointDriver.getExportPointPaths().iterator();
            while (i.hasNext()) {
                String currentExportPoint = (String)i.next();

                // print some report messages
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Writing export point " + currentExportPoint);
                }

                try {
                    CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
                    List resources = m_vfsDriver.readResourceTree(
                        dbc,
                        I_CmsConstants.C_PROJECT_ONLINE_ID,
                        currentExportPoint,
                        filter.getType(),
                        filter.getState(),
                        filter.getModifiedAfter(),
                        filter.getModifiedBefore(),
                        I_CmsConstants.C_READMODE_INCLUDE_TREE
                            | (filter.excludeType() ? I_CmsConstants.C_READMODE_EXCLUDE_TYPE : 0)
                            | (filter.excludeState() ? I_CmsConstants.C_READMODE_EXCLUDE_STATE : 0));

                    Iterator j = resources.iterator();
                    while (j.hasNext()) {
                        CmsResource currentResource = (CmsResource)j.next();

                        if (currentResource.isFolder()) {
                            // export the folder                        
                            exportPointDriver.createFolder(currentResource.getRootPath(), currentExportPoint);
                        } else {
                            // try to create the exportpoint folder
                            exportPointDriver.createFolder(currentExportPoint, currentExportPoint);
                            // export the file content online          
                            CmsFile file = getVfsDriver().readFile(
                                dbc,
                                I_CmsConstants.C_PROJECT_ONLINE_ID,
                                false,
                                currentResource.getStructureId());
                            exportPointDriver.writeFile(file.getRootPath(), currentExportPoint, file.getContents());
                        }
                    }
                } catch (CmsException e) {
                    // there might exist export points without corresponding resources in the VFS
                    // -> ingore exceptions which are not "resource not found" exception quiet here
                    if (e.getType() != CmsException.C_NOT_FOUND) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error updating export points", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error updating export points", e);
            }
        }
    }

    /**
     * Tests if a user is member of the given group.<p>
     * 
     * @param dbc the current database context.
     * @param username the name of the user to check.
     * @param groupname the name of the group to check.
     *
     * @return <code>true</code>, if the user is in the group; or <code>false</code> otherwise.
     * 
     * @throws CmsException if operation was not succesful.
     */
    public boolean userInGroup(
        CmsDbContext dbc,
        String username,
        String groupname) throws CmsException {

        List groups = getGroupsOfUser(dbc, username);
        CmsGroup group;
        for (int z = 0; z < groups.size(); z++) {
            group = (CmsGroup)groups.get(z);
            if (groupname.equals(group.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the HTML links in the unpublished files of the specified
     * publish list, if a file resource type implements the interface 
     * <code>{@link org.opencms.validation.I_CmsHtmlLinkValidatable}</code>.<p>
     * 
     * @param cms the current user's Cms object
     * @param publishList an OpenCms publish list.
     * @param report a report to write the messages to.
     * 
     * @return a map with lists of invalid links (<code>String</code> objects) keyed by resource names.
     * 
     * @throws Exception if something goes wrong.
     * 
     * @see #getPublishList(CmsDbContext, CmsResource, boolean)
     */
    public Map validateHtmlLinks(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {

        return getHtmlLinkValidator().validateResources(cms, publishList.getFileList(), report);
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class implementing the 
     * <code>{@link org.opencms.security.I_CmsPasswordHandler}<code> 
     * interface and configured in the opencms.properties file.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked.
     * 
     * @throws CmsSecurityException if the password is not valid.
     */
    public void validatePassword(String password) throws CmsSecurityException {

        OpenCms.getPasswordHandler().validatePassword(password);
    }

    /**
     * Checks if the provided file name is a valid file name, that is contains only
     * valid characters.<p>
     *
     * @param filename the file name to check
     * @throws CmsException C_BAD_NAME if the check fails
     */
    public void validFilename(String filename) throws CmsException {

        if (filename == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
        }

        int l = filename.length();

        if (l == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
        }

        for (int i = 0; i < l; i++) {
            char c = filename.charAt(i);
            if (((c < 'a') || (c > 'z'))
                && ((c < '0') || (c > '9'))
                && ((c < 'A') || (c > 'Z'))
                && (c != '-')
                && (c != '.')
                && (c != '_')
                && (c != '~')
                && (c != '$')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
            }
        }
    }

    /**
     * Writes an access control entries to a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param ace the entry to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(
        CmsDbContext dbc,
        CmsResource resource,
        CmsAccessControlEntry ace) throws CmsException {

        // write the new ace
        m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), ace);

        // update the "last modified" information
        touch(
            dbc,
            resource,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED,
            I_CmsConstants.C_DATE_UNCHANGED);

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap(
            "resource",
            resource)));
    }

    /**
     * Writes all export points into the file system for a publish task 
     * specified by its publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project that was published
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * 
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     */
    public void writeExportPoints(CmsDbContext dbc, int projectId, I_CmsReport report, CmsUUID publishHistoryId) {

        boolean printReportHeaders = false;
        try {
            // read the "published resources" for the specified publish history ID
            List publishedResources = m_projectDriver.readPublishedResources(dbc, projectId, publishHistoryId);
            if (publishedResources.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn(
                        "No published resources in the publish history for the specified ID "
                            + publishHistoryId
                            + " found.");
                }
                return;
            }

            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the report may be null if the export point write was started by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }

            // iterate over all published resources to export them eventually
            Iterator i = publishedResources.iterator();
            while (i.hasNext()) {
                CmsPublishedResource currentPublishedResource = (CmsPublishedResource)i.next();
                String currentExportPoint = exportPointDriver.getExportPoint(currentPublishedResource.getRootPath());

                if (currentExportPoint != null) {
                    if (!printReportHeaders) {
                        report.println(report.key("report.export_points_write_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                        printReportHeaders = true;
                    }

                    if (currentPublishedResource.isFolder()) {
                        // export the folder                        
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(
                                currentPublishedResource.getRootPath(), 
                                currentExportPoint);
                        } else {
                            exportPointDriver.createFolder(currentPublishedResource.getRootPath(), currentExportPoint);
                        }
                    } else {
                        // export the file            
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(
                                currentPublishedResource.getRootPath(), 
                                currentExportPoint);
                        } else {
                            // read the file content online
                            CmsFile file = getVfsDriver().readFile(
                                dbc,
                                I_CmsConstants.C_PROJECT_ONLINE_ID,
                                false,
                                currentPublishedResource.getStructureId());
                            exportPointDriver.writeFile(file.getRootPath(), currentExportPoint, file.getContents());
                        }
                    }

                    // print some report messages
                    if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                        report.print(report.key("report.export_points_delete"), I_CmsReport.C_FORMAT_NOTE);
                        report.print(currentPublishedResource.getRootPath());
                        report.print(report.key("report.dots"));
                        report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    } else {
                        report.print(report.key("report.export_points_write"), I_CmsReport.C_FORMAT_NOTE);
                        report.print(currentPublishedResource.getRootPath());
                        report.print(report.key("report.dots"));
                        report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    }
                }
            }
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error writing export points", e);
            }
        } finally {
            if (printReportHeaders) {
                report.println(report.key("report.export_points_write_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }
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
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see I_CmsResourceType#writeFile(CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsDbContext dbc, CmsFile resource)
    throws CmsException {

        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, C_UPDATE_RESOURCE_STATE);

        m_vfsDriver.writeContent(
            dbc, 
            dbc.currentProject(), 
            resource.getResourceId(), 
            resource.getContents());

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));

        return resource;
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group id has to be a valid OpenCms group id.<br>
     * 
     * The group with the given id will be completely overriden
     * by the given data.<p>
     * 
     * @param dbc the current database context.
     * @param group the group that should be written.
     * 
     * @throws CmsException if operation was not succesfull.
     */
    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsException {

        m_userDriver.writeGroup(dbc, group);
        m_groupCache.put(new CacheId(group), group);
    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see I_CmsResourceType#writePropertyObject(CmsObject, CmsSecurityManager, CmsResource, CmsProperty)
     */
    public void writePropertyObject(
        CmsDbContext dbc,
        CmsResource resource,
        CmsProperty property) throws CmsException {

        try {
            if (property == CmsProperty.getNullProperty()) {
                // skip empty or null properties
                return;
            }

            // write the property
            m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);

            // update the resource state
            resource.setUserLastModified(dbc.currentUser().getId());
            m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, C_UPDATE_RESOURCE_STATE);

        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that a property of a resource has been modified
            Map data = new HashMap();
            data.put("resource", resource);
            data.put("property", property);
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROPERTY_MODIFIED, data));
        }
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see I_CmsResourceType#writePropertyObjects(CmsObject, CmsSecurityManager, CmsResource, List)
     */
    public void writePropertyObjects(
        CmsDbContext dbc,
        CmsResource resource,
        List properties) throws CmsException {

        if ((properties == null) || (properties.size() == 0)) {
            // skip empty or null lists
            return;
        }

        try {

            // the specified list must not contain two or more equal property objects
            for (int i = 0, n = properties.size(); i < n; i++) {
                Set keyValidationSet = new HashSet();
                CmsProperty property = (CmsProperty)properties.get(i);
                if (!keyValidationSet.contains(property.getKey())) {
                    keyValidationSet.add(property.getKey());
                } else {
                    throw new CmsVfsException("Invalid multiple occurence of property named '"
                        + property.getKey()
                        + "' detected.", CmsVfsException.C_VFS_INVALID_PROPERTY_LIST);
                }
            }

            for (int i = 0; i < properties.size(); i++) {
                // write the property
                CmsProperty property = (CmsProperty)properties.get(i);
                m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that the properties of a resource have been modified
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections
                .singletonMap("resource", resource)));
        }
    }

    /**
     * Writes a resource to the OpenCms VFS.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write
     *
     * @throws CmsException if something goes wrong
     */
    public void writeResource(CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        // access was granted - write the resource
        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, C_UPDATE_STRUCTURE_STATE);

        // make sure the written resource has the state corretly set
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                Collections.singletonMap("resource", resource)));
    }

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param dbc the current database context.
     * @param resourceName The name of the resource to be added to the static export.
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter).
     * @param linkParameter the parameters added to the resource.
     * @param timestamp a timestamp for writing the data into the db.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_projectDriver.writeStaticExportPublishedResource(
            dbc,
            dbc.currentProject(),
            resourceName,
            linkType,
            linkParameter,
            timestamp);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * @param dbc the current database context.
     * @param taskid the Id of the task.
     * @param comment description for the log.
     * 
     * @throws CmsException if something goes wrong.
     */
    public void writeTaskLog(CmsDbContext dbc, int taskid, String comment) throws CmsException {

        m_workflowDriver.writeTaskLog(
            dbc,
            taskid, 
            dbc.currentUser().getId(), 
            new java.sql.Timestamp(System.currentTimeMillis()), 
            comment, 
            I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * Writes a new task log entry for a task.<p>
     *
     * @param dbc the current database context.
     * @param taskId the Id of the task.
     * @param comment description for the log.
     * @param type type of the tasklog. User tasktypes must be greater then 100.
     * 
     * @throws CmsException something goes wrong.
     */
    public void writeTaskLog(CmsDbContext dbc, int taskId, String comment, int type) throws CmsException {

        m_workflowDriver.writeTaskLog(
            dbc,
            taskId, 
            dbc.currentUser().getId(), 
            new java.sql.Timestamp(System.currentTimeMillis()),
            comment,
            type);
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context.
     * @param user the user to be updated.
     *
     * @throws CmsException if operation was not succesful.
     */
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsException {

        // prevent the admin to be set disabled!
        if (isAdmin(dbc)) {
            user.setEnabled();
        }
        m_userDriver.writeUser(dbc, user);
        // update the cache
        clearUserCache(user);
        putUserInCache(user);
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
     * @param dbc the current database context.
     * @param user the user to be updated.
     *
     * @throws CmsException if operation was not succesful.
     */
    public void writeWebUser(CmsDbContext dbc, CmsUser user) throws CmsException {

        m_userDriver.writeUser(dbc, user);
        // update the cache
        clearUserCache(user);
        putUserInCache(user);
    }
    
    /** 
     * Converts a resource to a folder (if possible).<p>
     * 
     * @param resource the resource to convert
     * @return the converted resource 
     * 
     * @throws CmsException if the resource is not a folder
     */
    protected CmsFolder convertResourceToFolder(CmsResource resource) throws CmsException {

        if (resource.isFolder()) {
            return new CmsFolder(resource);
        }

        throw new CmsException(
            "Trying to access a file as a folder " + "(" + resource.getRootPath() + ")",
            CmsException.C_NOT_FOUND);
    }

    /**
     * Releases any allocated resources during garbage collection.<p>
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            clearcache();

            try {
                m_projectDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing project driver", t);
            }
            try {
                m_userDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing user driver", t);
            }
            try {
                m_vfsDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing VFS driver", t);
            }
            try {
                m_workflowDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing workflow driver", t);
            }
            try {
                m_backupDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing backup driver", t);
            }

            for (int i = 0; i < m_connectionPools.size(); i++) {
                PoolingDriver driver = (PoolingDriver)m_connectionPools.get(i);
                String[] pools = driver.getPoolNames();
                for (int j = 0; j < pools.length; j++) {
                    try {
                        driver.closePool(pools[j]);
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug(". Shutting down        : closed connection pool '" + pools[j] + "'");
                        }
                    } catch (Throwable t) {
                        OpenCms.getLog(this).error("Error closing connection pool '" + pools[j] + "'", t);
                    }
                }
            }

            m_userCache = null;
            m_groupCache = null;
            m_userGroupsCache = null;
            m_projectCache = null;
            m_propertyCache = null;
            m_resourceCache = null;
            m_resourceListCache = null;
            m_accessControlListCache = null;

            m_projectDriver = null;
            m_userDriver = null;
            m_vfsDriver = null;
            m_workflowDriver = null;
            m_backupDriver = null;

            m_htmlLinkValidator = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }

    /**
     * Checks if this is a valid group for webusers.<p>
     * 
     * @param dbc the current database context
     * @param group the group to be checked
     *
     * @return true if the group does not belong to users, administrators or projectmanagers
     * @throws CmsException if operation was not succesful
     */
    protected boolean isWebgroup(CmsDbContext dbc, CmsGroup group) throws CmsException {

        try {
            CmsUUID user = m_userDriver.readGroup(
                dbc, 
                OpenCms.getDefaultUsers().getGroupUsers()).getId();
            CmsUUID admin = m_userDriver.readGroup(
                dbc, 
                OpenCms.getDefaultUsers().getGroupAdministrators()).getId();
            CmsUUID manager = m_userDriver.readGroup(
                dbc, 
                OpenCms.getDefaultUsers().getGroupProjectmanagers()).getId();

            if ((group.getId().equals(user)) || (group.getId().equals(admin)) || (group.getId().equals(manager))) {
                return false;
            } else {
                CmsUUID parentId = group.getParentId();
                // check if the group belongs to Users, Administrators or Projectmanager
                if (!parentId.isNullUUID()) {
                    // check is the parentgroup is a webgroup
                    return isWebgroup(dbc, m_userDriver.readGroup(dbc, parentId));
                }
            }
        } catch (CmsException e) {
            throw e;
        }
        return true;
    }

    /**
     * Checks if characters in a String are allowed for filenames.<p>
     *
     * @param taskname String to check
     * @throws CmsException C_BAD_NAME if the check fails
     */
    protected void validTaskname(String taskname) throws CmsException {

        if (taskname == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
        }

        int l = taskname.length();

        if (l == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
        }

        for (int i = 0; i < l; i++) {
            char c = taskname.charAt(i);
            if (((c < '?') || (c > '?'))
                && ((c < '?') || (c > '?'))
                && ((c < 'a') || (c > 'z'))
                && ((c < '0') || (c > '9'))
                && ((c < 'A') || (c > 'Z'))
                && (c != '-')
                && (c != '.')
                && (c != '_')
                && (c != '~')
                && (c != ' ')
                && (c != '?')
                && (c != '/')
                && (c != '(')
                && (c != ')')
                && (c != '\'')
                && (c != '#')
                && (c != '&')
                && (c != ';')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
            }
        }
    }

    /**
     * Checks the parent of a resource during publishing.<p> 
     * 
     * @param dbc the current database context
     * @param folderList a list of folders
     * @param res a resource to check the parent for
     * 
     * @return true if the resource should be published
     */
    private boolean checkParentResource(CmsDbContext dbc, List folderList, CmsResource res) {

        String parentPath = CmsResource.getParentFolder(res.getRootPath());
        CmsResource parent;

        if (parentPath == null) {
            // resource has no parent
            return true;
        }

        try {
            parent = readResource(dbc, parentPath, CmsResourceFilter.ALL);
        } catch (Exception e) {
            // failure: if we cannot read the parent, we should not publish the resource
            return false;
        }

        if (parent.getState() != I_CmsConstants.C_STATE_NEW) {
            // parent is already published
            return true;
        }

        for (int j = 0; j < folderList.size(); j++) {
            if (((CmsResource)folderList.get(j)).getStructureId().equals(parent.getStructureId())) {
                // parent is new, but it will get published
                return true;
            }
        }

        // parent is new, but it will not get published
        return false;
    }

    /**
     * Clears the access control list cache when access control entries are changed.<p>
     */
    private void clearAccessControlListCache() {

        m_accessControlListCache.clear();
        m_securityManager.clearPermissionCache();
        clearResourceCache();
    }

    /**
     * Clears all internal caches.<p>
     */
    private void clearcache() {

        m_userCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_projectCache.clear();
        m_resourceCache.clear();
        m_resourceListCache.clear();
        m_propertyCache.clear();
        m_accessControlListCache.clear();
        m_securityManager.clearPermissionCache();
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     */
    private void clearResourceCache() {

        m_resourceCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Clears the user cache for the given user.<p>
     * @param user the user
     */
    private void clearUserCache(CmsUser user) {

        removeUserFromCache(user);
        m_resourceListCache.clear();
    }

    /**
     * Returns a filtered list of resources for publishing.<p>
     * Contains all resources, which are not locked 
     * and which have a parent folder that is already published or will be published, too.
     * 
     * @param dbc the current database context
     * @param folderList the list of folders that will be published
     * @param resourceList the list of resources to filter
     * @return a filtered list of resources
     */
    private List filterResources(CmsDbContext dbc, List folderList, List resourceList) {

        List result = new ArrayList();

        if (folderList == null) {
            folderList = result;
        }

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                if (!getLock(dbc, res).isNullLock()) {
                    continue;
                }

                if (!I_CmsConstants.C_ROOT.equals(res.getRootPath()) && !checkParentResource(dbc, folderList, res)) {
                    continue;
                }

                result.add(res);

            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }

    /**
     * Returns a filtered list of sibling resources for publishing.<p>
     * 
     * Contains all other siblings of the given resources, which are not locked
     * and which have a parent folder that is already published or will be published, too.<p>
     * 
     * @param dbc the current database context
     * @param currentResource the resource to lookup siblings 
     * @param folderList the list of folders that will be published
     * @param resourceList 
     * @return a filtered list of sibling resources for publishing
     */
    private List filterSiblings(
        CmsDbContext dbc,
        CmsResource currentResource,
        List folderList,
        List resourceList) {

        List result = new ArrayList();

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                if (res.getStructureId().equals(currentResource.getStructureId())) {
                    // don't add if sibling is equal to current resource
                    // note: it's also required to check for sibling duplicates in the 
                    // publish list itself
                    continue;
                }

                if (!getLock(dbc, res).isNullLock()) {
                    // don't add locked resources
                    continue;
                }

                if (!I_CmsConstants.C_ROOT.equals(res.getRootPath()) && !checkParentResource(dbc, folderList, res)) {
                    // don't add resources that have no parent in the online project
                    continue;
                }

                result.add(res);

            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param project the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, CmsProject project, String resource) {

        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        if (project != null) {
            if (project.isOnlineProject()) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
            buffer.append("_");
        }
        buffer.append(resource);
        return buffer.toString();
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param projectId the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, int projectId, String resource) {

        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        if (projectId >= I_CmsConstants.C_PROJECT_ONLINE_ID) {
            if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
            buffer.append("_");
        }
        buffer.append(resource);
        return buffer.toString();
    }

    /**
     * Gets a user cache key.<p>
     * 
     * @param id the user uuid
     * @return the user cache key
     */
    private String getUserCacheKey(CmsUUID id) {

        return id.toString();
    }

    /**
     * Gets a user cache key.<p>
     * 
     * @param username the name of the user
     * @param type the user type
     * @return the user cache key
     */
    private String getUserCacheKey(String username, int type) {

        StringBuffer result = new StringBuffer(32);
        result.append(username);
        result.append(C_USER_CACHE_SEP);
        result.append(CmsUser.isSystemUser(type));
        return result.toString();
    }

    /**
     * Gets a user from cache.<p>
     * 
     * @param id the user uuid
     * @return CmsUser from cache
     */
    private CmsUser getUserFromCache(CmsUUID id) {

        return (CmsUser)m_userCache.get(getUserCacheKey(id));
    }

    /**
     * Gets a user from cache.<p>
     * 
     * @param username the username
     * @param type the user tpye
     * @return CmsUser from cache
     */
    private CmsUser getUserFromCache(String username, int type) {

        return (CmsUser)m_userCache.get(getUserCacheKey(username, type));
    }

    /**
     * Stores a user in the user cache.<p>
     * 
     * @param user the user to be stored in the cache
     */
    private void putUserInCache(CmsUser user) {

        m_userCache.put(getUserCacheKey(user.getName(), user.getType()), user);
        m_userCache.put(getUserCacheKey(user.getId()), user);
    }

    /**
     * Removes user from Cache.<p>
     * 
     * @param user the user to remove
     */
    private void removeUserFromCache(CmsUser user) {

        m_userCache.remove(getUserCacheKey(user.getName(), user.getType()));
        m_userCache.remove(getUserCacheKey(user.getId()));
    }

    /**
     * Updates the date information in the database context.<p>
     * 
     * @param dbc the context to update
     * @param resource the resource to get the date information from
     */
    private void updateContextDates(CmsDbContext dbc, CmsResource resource) {

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        
        if (info != null) {
            info.updateFromResource(resource);
        }
    }

    /**
     * Updates the context dates with each resource in a list of CmsResources,
     * also applies the selected resource filter to all resources in the list.<p>
     *
     * @param dbc the current database context
     * @param resourceList a list of CmsResources
     * @param filter the resource filter to use
     * 
     * @return fltered list of CmsResources with the full resource name set 
     */
    private List updateContextDates(CmsDbContext dbc, List resourceList, CmsResourceFilter filter) {

        if (CmsResourceFilter.ALL == filter) {
            if (resourceList instanceof ArrayList) {
                return (List)((ArrayList)(updateContextDates(dbc, resourceList))).clone();
            } else {
                return new ArrayList(updateContextDates(dbc, resourceList));
            }
        }

        ArrayList result = new ArrayList(resourceList.size());
        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource resource = (CmsResource)resourceList.get(i);
            if (filter.isValid(dbc.getRequestContext(), resource)) {
                result.add(resource);
            }
            // must also include "invalid" resources for the update of context dates
            // since a resource may be invalid because of release / expiration date
            updateContextDates(dbc, resource);
        }
        return result;
    }
}