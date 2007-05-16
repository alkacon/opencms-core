/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2007/05/16 15:57:30 $
 * Version: $Revision: 1.570.2.89 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.CmsHistoryFolder;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockManager;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.publish.CmsPublishEngine;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.publish.CmsPublishReport;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationSystemValidator;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.ObjectPool;

/**
 * The OpenCms driver manager.<p>
 * 
 * @author Alexander Kandzior 
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * @author Michael Emmerich 
 * @author Michael Moossen
 * 
 * @since 6.0.0
 */
public final class CmsDriverManager implements I_CmsEventListener {

    /**
     * Enumeration class for the mode parameter in the 
     * {@link CmsDriverManager#readChangedResourcesInsideProject(CmsDbContext, CmsUUID, CmsReadChangedProjectResourceMode)} 
     * method.<p>
     */
    private static class CmsReadChangedProjectResourceMode {

        /**
         * Default constructor.<p>
         */
        protected CmsReadChangedProjectResourceMode() {

            // noop
        }
    }

    /** Cache key for all properties. */
    public static final String CACHE_ALL_PROPERTIES = "_CAP_";

    /** 
     * Values indicating changes of a resource, 
     * ordered according to the scope of the change. 
     */
    /** Value to indicate a change in access control entries of a resource. */
    public static final int CHANGED_ACCESSCONTROL = 1;

    /** Value to indicate a content change. */
    public static final int CHANGED_CONTENT = 16;

    /** Value to indicate a change in the lastmodified settings of a resource. */
    public static final int CHANGED_LASTMODIFIED = 4;

    /** Value to indicate a change in the resource data. */
    public static final int CHANGED_RESOURCE = 8;

    /** Value to indicate a change in the availability timeframe. */
    public static final int CHANGED_TIMEFRAME = 2;

    /** 
     * "driver.backup" string in the configuration-file. 
     * @deprecated use {@link #CONFIGURATION_HISTORY} instead 
     */
    public static final String CONFIGURATION_BACKUP = "driver.backup";

    /** "cache" string in the configuration-file. */
    public static final String CONFIGURATION_CACHE = "cache";

    /** "db" string in the configuration-file. */
    public static final String CONFIGURATION_DB = "db";

    /** "driver.history" string in the configuration-file. */
    public static final String CONFIGURATION_HISTORY = "driver.history";

    /** "driver.project" string in the configuration-file. */
    public static final String CONFIGURATION_PROJECT = "driver.project";

    /** "driver.user" string in the configuration-file. */
    public static final String CONFIGURATION_USER = "driver.user";

    /** "driver.vfs" string in the configuration-file. */
    public static final String CONFIGURATION_VFS = "driver.vfs";

    /** The vfs path of the loast and found folder. */
    public static final String LOST_AND_FOUND_FOLDER = "/system/lost-found";

    /** The maximum length of a VFS resource path. */
    public static final int MAX_VFS_RESOURCE_PATH_LENGTH = 512;

    /** Key for indicating no changes. */
    public static final int NOTHING_CHANGED = 0;

    /** Indicates to ignore the resource path when matching resources. */
    public static final String READ_IGNORE_PARENT = null;

    /** Indicates to ignore the time value. */
    public static final long READ_IGNORE_TIME = 0L;

    /** Indicates to ignore the resource type when matching resources. */
    public static final int READ_IGNORE_TYPE = -1;

    /** Indicates to match resources NOT having the given state. */
    public static final int READMODE_EXCLUDE_STATE = 8;

    /** Indicates to match immediate children only. */
    public static final int READMODE_EXCLUDE_TREE = 1;

    /** Indicates to match resources NOT having the given type. */
    public static final int READMODE_EXCLUDE_TYPE = 4;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_IGNORESTATE = 0;

    /** Indicates to match resources in given project only. */
    public static final int READMODE_INCLUDE_PROJECT = 2;

    /** Indicates to match all successors. */
    public static final int READMODE_INCLUDE_TREE = 0;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_MATCHSTATE = 1;

    /** Indicates if only file resources should be read. */
    public static final int READMODE_ONLY_FILES = 128;

    /** Indicates if only folder resources should be read. */
    public static final int READMODE_ONLY_FOLDERS = 64;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_UNMATCHSTATE = 2;

    /** Key to indicate complete update. */
    public static final int UPDATE_ALL = 3;

    /** Key to indicate update of resource record. */
    public static final int UPDATE_RESOURCE = 4;

    /** Key to indicate update of lastmodified project reference. */
    public static final int UPDATE_RESOURCE_PROJECT = 6;

    /** Key to indicate update of resource state. */
    public static final int UPDATE_RESOURCE_STATE = 1;

    /** Key to indicate update of resource state including the content date. */
    public static final int UPDATE_RESOURCE_STATE_CONTENT = 7;

    /** Key to indicate update of structure record. */
    public static final int UPDATE_STRUCTURE = 5;

    /** Key to indicate update of structure state. */
    public static final int UPDATE_STRUCTURE_STATE = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDriverManager.class);

    /** Constant mode parameter to read all files and folders in the {@link #readChangedResourcesInsideProject(CmsDbContext, CmsUUID, CmsReadChangedProjectResourceMode)}} method. */
    private static final CmsReadChangedProjectResourceMode RCPRM_FILES_AND_FOLDERS_MODE = new CmsReadChangedProjectResourceMode();

    /** Constant mode parameter to read all files and folders in the {@link #readChangedResourcesInsideProject(CmsDbContext, CmsUUID, CmsReadChangedProjectResourceMode)}} method. */
    private static final CmsReadChangedProjectResourceMode RCPRM_FILES_ONLY_MODE = new CmsReadChangedProjectResourceMode();

    /** Constant mode parameter to read all files and folders in the {@link #readChangedResourcesInsideProject(CmsDbContext, CmsUUID, CmsReadChangedProjectResourceMode)}} method. */
    private static final CmsReadChangedProjectResourceMode RCPRM_FOLDERS_ONLY_MODE = new CmsReadChangedProjectResourceMode();

    /** The list of initialized JDBC pools. */
    private List m_connectionPools;

    /** The history driver. */
    private I_CmsHistoryDriver m_historyDriver;

    /** The HTML link validator. */
    private CmsRelationSystemValidator m_htmlLinkValidator;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /** The lock manager. */
    private CmsLockManager m_lockManager;

    /** The project driver. */
    private I_CmsProjectDriver m_projectDriver;

    /** The the configuration read from the <code>opencms.properties</code> file. */
    private ExtendedProperties m_propertyConfiguration;

    /* the publish engine. */
    private CmsPublishEngine m_publishEngine;

    /** The security manager (for access checks). */
    private CmsSecurityManager m_securityManager;

    /** The sql manager. */
    private CmsSqlManager m_sqlManager;

    /** The user driver. */
    private I_CmsUserDriver m_userDriver;

    /** The VFS driver. */
    private I_CmsVfsDriver m_vfsDriver;

    /**
     * Private constructor, initializes some required member variables.<p> 
     */
    private CmsDriverManager() {

        // intentionally left blank
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
     * @param publishEngine the publish engine
     * 
     * @return CmsDriverManager the instanciated driver manager
     * @throws CmsInitException if the driver manager couldn't be instanciated
     */
    public static CmsDriverManager newInstance(
        CmsConfigurationManager configurationManager,
        CmsSecurityManager securityManager,
        I_CmsDbContextFactory runtimeInfoFactory,
        CmsPublishEngine publishEngine) throws CmsInitException {

        // read the opencms.properties from the configuration
        ExtendedProperties config = (ExtendedProperties)configurationManager.getConfiguration();

        List drivers = null;
        String driverName = null;

        I_CmsVfsDriver vfsDriver = null;
        I_CmsUserDriver userDriver = null;
        I_CmsProjectDriver projectDriver = null;
        I_CmsHistoryDriver historyDriver = null;

        CmsDriverManager driverManager = null;
        try {
            // create a driver manager instance
            driverManager = new CmsDriverManager();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE1_0));
            }
            if ((runtimeInfoFactory == null) && CmsLog.INIT.isDebugEnabled()) {
                CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_RT_0));
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.LOG_ERR_DRIVER_MANAGER_START_0);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsInitException(message, exc);
        }

        // set the security manager
        driverManager.m_securityManager = securityManager;

        // set connection pools
        driverManager.m_connectionPools = new ArrayList();

        // set the lock manager
        driverManager.m_lockManager = new CmsLockManager(driverManager);

        // create and set the sql manager
        driverManager.m_sqlManager = new CmsSqlManager(driverManager);

        // set the publish engine
        driverManager.m_publishEngine = publishEngine;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE2_0));
        }

        // read the pool names to initialize
        String[] driverPoolNames = config.getStringArray(CmsDriverManager.CONFIGURATION_DB + ".pools");
        if (CmsLog.INIT.isInfoEnabled()) {
            String names = "";
            for (int p = 0; p < driverPoolNames.length; p++) {
                names += driverPoolNames[p] + " ";
            }
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_POOLS_1, names));
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(config, driverPoolNames[p]);
        }

        // initialize the runtime info factory with the generated driver manager
        if (runtimeInfoFactory != null) {
            runtimeInfoFactory.initialize(driverManager);
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE3_0));
        }

        // read the vfs driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_VFS));
        driverName = config.getString((String)drivers.get(0) + ".vfs.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        vfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the user driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_USER));
        driverName = config.getString((String)drivers.get(0) + ".user.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the project driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_PROJECT));
        driverName = config.getString((String)drivers.get(0) + ".project.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        projectDriver = (I_CmsProjectDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the history driver class properties and initialize a new instance 
        if (config.get(CmsDriverManager.CONFIGURATION_HISTORY) != null) {
            drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_HISTORY));
            driverName = config.getString((String)drivers.get(0) + ".history.driver");
            drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
            historyDriver = (I_CmsHistoryDriver)driverManager.newDriverInstance(
                configurationManager,
                driverName,
                drivers);
        } else {
            drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_BACKUP));
            driverName = config.getString((String)drivers.get(0) + ".backup.driver");
            drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
            historyDriver = (I_CmsHistoryDriver)driverManager.newDriverInstance(
                configurationManager,
                driverName,
                drivers);
        }

        // store the access objects
        driverManager.m_vfsDriver = vfsDriver;
        driverManager.m_userDriver = userDriver;
        driverManager.m_projectDriver = projectDriver;
        driverManager.m_historyDriver = historyDriver;

        // store the configuration
        driverManager.m_propertyConfiguration = config;

        // register the driver manager for required events
        org.opencms.main.OpenCms.addCmsEventListener(driverManager, new int[] {
            I_CmsEventListener.EVENT_UPDATE_EXPORTS,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_CLEAR_PRINCIPAL_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        // return the configured driver manager
        return driverManager;
    }

    /**
     * Adds a new relation to the given resource.<p>
     * 
     * @param dbc the database context
     * @param resource the resource to add the relation to
     * @param id the structure id of the target relation
     * @param target the target of the relation
     * @param type the type of the relation
     * 
     * @throws CmsException if something goes wrong
     */
    public void addRelationToResource(
        CmsDbContext dbc,
        CmsResource resource,
        CmsUUID id,
        String target,
        CmsRelationType type) throws CmsException {

        CmsRelation relation = new CmsRelation(
            resource.getStructureId(),
            resource.getRootPath(),
            id,
            target,
            0,
            0,
            type);
        m_vfsDriver.createRelation(dbc, dbc.currentProject().getUuid(), relation);

    }

    /**
     * Adds a resource to the given organizational unit.<p>
     * 
     * @param dbc the current db context
     * @param orgUnit the organizational unit to add the resource to
     * @param resource the resource that is to be added to the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     */
    public void addResourceToOrgUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource)
    throws CmsException {

        m_userDriver.addResourceToOrganizationalUnit(dbc, orgUnit, resource);
    }

    /**
     * Adds a user to a group.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * @param readRoles if reading roles or groups
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDbEntryNotFoundException if the given user or the given group was not found 
     * 
     * @see #removeUserFromGroup(CmsDbContext, String, String, boolean)
     */
    public void addUserToGroup(CmsDbContext dbc, String username, String groupname, boolean readRoles)
    throws CmsException, CmsDbEntryNotFoundException {

        //check if group exists
        CmsGroup group = readGroup(dbc, groupname);
        if (group == null) {
            // the group does not exists
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
        }
        if (group.isVirtual() && !readRoles) {
            // if adding a user from a virtual role treat it as removing the user from the role
            addUserToGroup(dbc, username, CmsRole.valueOf(group).getGroupName(), true);
            return;
        }
        if (group.isVirtual()) {
            // this is an hack so to prevent a unlimited recursive calls
            readRoles = false;
        }
        if ((readRoles && !group.isRole()) || (!readRoles && group.isRole())) {
            // we want a role but we got a group, or the other way
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
        }
        if (userInGroup(dbc, username, groupname, readRoles)) {
            // the user is already member of the group
            return;
        }
        //check if the user exists
        CmsUser user = readUser(dbc, username);
        if (user == null) {
            // the user does not exists
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_USER_1, username));
        }

        // if adding an user to a role
        if (readRoles) {
            CmsRole role = CmsRole.valueOf(group);
            // a role can only be set if the user has the given role
            m_securityManager.checkRole(dbc, role);
            // now we check if we already have the role 
            if (m_securityManager.hasRole(dbc, user, role)) {
                // do nothing
                return;
            }
            // and now we need to remove all possible child-roles
            List childs = role.getChilds(true);
            Iterator itUserGroups = getGroupsOfUser(
                dbc,
                username,
                group.getOuFqn(),
                true,
                true,
                true,
                dbc.getRequestContext().getRemoteAddress()).iterator();
            while (itUserGroups.hasNext()) {
                CmsGroup roleGroup = (CmsGroup)itUserGroups.next();
                if (childs.contains(CmsRole.valueOf(roleGroup))) {
                    // remove only child roles
                    removeUserFromGroup(dbc, username, roleGroup.getName(), true);
                }
            }
            // update virtual groups
            Iterator it = getVirtualGroupsForRole(dbc, role).iterator();
            while (it.hasNext()) {
                CmsGroup virtualGroup = (CmsGroup)it.next();
                // here we say readroles = true, to prevent an unlimited recursive calls
                addUserToGroup(dbc, username, virtualGroup.getName(), true);
            }
            // if setting a role that is not the workplace user role ensure the user is also wp user
            CmsRole wpUser = CmsRole.WORKPLACE_USER.forOrgUnit(group.getOuFqn());
            if (!role.equals(wpUser)
                && !role.getChilds(true).contains(wpUser)
                && !userInGroup(dbc, username, wpUser.getGroupName(), true)) {
                addUserToGroup(dbc, username, wpUser.getGroupName(), true);
            }
        }

        //add this user to the group
        m_userDriver.createUserInGroup(dbc, user.getId(), group.getId());
        // flush the cache
        OpenCms.getMemoryMonitor().flushUserGroups();
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to change the lock for
     * @param lockType the new lock type to set
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if something goes wrong
     * 
     * 
     * @see CmsObject#changeLock(String)
     * @see I_CmsResourceType#changeLock(CmsObject, CmsSecurityManager, CmsResource)
     * 
     * @see CmsSecurityManager#hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public void changeLock(CmsDbContext dbc, CmsResource resource, CmsLockType lockType)
    throws CmsException, CmsSecurityException {

        // get the current lock
        CmsLock currentLock = getLock(dbc, resource);
        // check if the resource is locked at all
        if (currentLock.getEditionLock().isUnlocked() && currentLock.getSystemLock().isUnlocked()) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_CHANGE_LOCK_UNLOCKED_RESOURCE_1,
                dbc.getRequestContext().getSitePath(resource)));
        } else if (lockType == CmsLockType.EXCLUSIVE
            && currentLock.isExclusiveOwnedBy(dbc.currentUser())
            && currentLock.isInProject(dbc.currentProject())) {
            // the current lock requires no change
            return;
        }

        // duplicate logic from CmsSecurityManager#hasPermissions() because lock state can't be ignored
        // if another user has locked the file, the current user can never get WRITE permissions with the default check
        int denied = 0;

        // check if the current user is admin
        boolean canIgnorePermissions = m_securityManager.hasRoleForResource(
            dbc,
            dbc.currentUser(),
            CmsRole.VFS_MANAGER,
            resource);
        // if the resource type is jsp
        // write is only allowed for administrators
        if (!canIgnorePermissions && (resource.getTypeId() == CmsResourceTypeJsp.getStaticTypeId())) {
            if (!m_securityManager.hasRoleForResource(dbc, dbc.currentUser(), CmsRole.DEVELOPER, resource)) {
                denied |= CmsPermissionSet.PERMISSION_WRITE;
            }
        }
        CmsPermissionSetCustom permissions;
        if (canIgnorePermissions) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSetCustom(~0);
        } else {
            // otherwise, get the permissions from the access control list
            permissions = getPermissions(dbc, resource, dbc.currentUser());
        }
        // revoke the denied permissions
        permissions.denyPermissions(denied);
        // now check if write permission is granted
        if ((CmsPermissionSet.ACCESS_WRITE.getPermissions() & permissions.getPermissions()) != CmsPermissionSet.ACCESS_WRITE.getPermissions()) {
            // check failed, throw exception
            m_securityManager.checkPermissions(
                dbc.getRequestContext(),
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                CmsSecurityManager.PERM_DENIED);
        }
        // if we got here write permission is granted on the target

        // remove the old lock
        m_lockManager.removeResource(dbc, resource, true, lockType.isSystem());
        // apply the new lock
        lockResource(dbc, resource, lockType);
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property, 
     * matching the current property's value with the given old value and replacing it by a given new value.<p>
     *
     * @param dbc the current database context
     * @param resource the resource on which property definition values are changed
     * @param propertyDefinition the name of the propertydefinition to change the value
     * @param oldValue the old value of the propertydefinition
     * @param newValue the new value of the propertydefinition
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     * 
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed
     *
     * @throws CmsVfsException for now only when the search for the oldvalue failed. 
     * @throws CmsException if operation was not successful
     */
    public List changeResourcesInFolderWithProperty(
        CmsDbContext dbc,
        CmsResource resource,
        String propertyDefinition,
        String oldValue,
        String newValue,
        boolean recursive) throws CmsVfsException, CmsException {

        // collect the resources to look up
        List resources = new ArrayList();
        if (recursive) {
            resources = readResourcesWithProperty(dbc, resource, propertyDefinition, null);
        } else {
            resources.add(resource);
        }

        Pattern oldPattern;
        try {
            // compile regular expression pattern
            oldPattern = Pattern.compile(oldValue);
        } catch (PatternSyntaxException e) {
            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_CHANGE_RESOURCES_IN_FOLDER_WITH_PROP_4,
                new Object[] {propertyDefinition, oldValue, newValue, resource.getRootPath()}), e);
        }

        List changedResources = new ArrayList(resources.size());
        // create permission set and filter to check each resource
        CmsPermissionSet perm = CmsPermissionSet.ACCESS_WRITE;
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
        for (int i = 0; i < resources.size(); i++) {
            // loop through found resources and check property values
            CmsResource res = (CmsResource)resources.get(i);
            // check resource state and permissions
            try {
                m_securityManager.checkPermissions(dbc, res, perm, true, filter);
            } catch (Exception e) {
                // resource is deleted or not writable for current user
                continue;
            }
            CmsProperty property = readPropertyObject(dbc, res, propertyDefinition, false);
            String structureValue = property.getStructureValue();
            String resourceValue = property.getResourceValue();
            boolean changed = false;
            if ((structureValue != null) && oldPattern.matcher(structureValue).matches()) {
                // change structure value
                property.setStructureValue(newValue);
                changed = true;
            }
            if ((resourceValue != null) && oldPattern.matcher(resourceValue).matches()) {
                // change resource value
                property.setResourceValue(newValue);
                changed = true;
            }
            if (changed) {
                // write property object if something has changed
                writePropertyObject(dbc, res, property);
                changedResources.add(res);
            }
        }
        return changedResources;
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
    public void chflags(CmsDbContext dbc, CmsResource resource, int flags) throws CmsException {

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
    public void chtype(CmsDbContext dbc, CmsResource resource, int type) throws CmsException {

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

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CMS_EVENT_1, new Integer(event.getType())));
        }

        I_CmsReport report;
        CmsDbContext dbc;

        switch (event.getType()) {

            case I_CmsEventListener.EVENT_UPDATE_EXPORTS:
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                updateExportPoints(dbc);
                break;

            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                writeExportPoints(dbc, report, publishHistoryId);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearcache(false);
                break;
            case I_CmsEventListener.EVENT_CLEAR_PRINCIPAL_CACHES:
                clearcache(true);
                break;
            default:
                // noop
        }
    }

    /**
     * Copies the access control entries of a given resource to a destination resorce.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * @param dbc the current database context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * @param updateLastModifiedInfo if true, user and date "last modified" information on the target resource will be updated
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(
        CmsDbContext dbc,
        CmsResource source,
        CmsResource destination,
        boolean updateLastModifiedInfo) throws CmsException {

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
        if (updateLastModifiedInfo) {
            setDateLastModified(dbc, destination, destination.getDateLastModified());
        }

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", destination);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is <code>null</code>
     * 
     * @see CmsObject#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     * @see I_CmsResourceType#copyResource(CmsObject, CmsSecurityManager, CmsResource, String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(
        CmsDbContext dbc,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode) throws CmsException, CmsIllegalArgumentException {

        // check the sibling mode to see if this resource has to be copied as a sibling
        boolean copyAsSibling = false;

        // siblings of folders are not supported
        if (!source.isFolder()) {
            // if the "copy as sibling" mode is used, set the flag to true
            if (siblingMode == CmsResource.COPY_AS_SIBLING) {
                copyAsSibling = true;
            }
            // if the mode is "preserve siblings", we have to check the sibling counter
            if (siblingMode == CmsResource.COPY_PRESERVE_SIBLING) {
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
            if (source instanceof CmsFile) {
                // resource already is a file
                content = ((CmsFile)source).getContents();
            }
            if ((content == null) || (content.length < 1)) {
                // no known content yet - read from database
                content = m_vfsDriver.readContent(dbc, dbc.currentProject().getUuid(), source.getResourceId());
            }
        }

        // determine desitnation folder        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_securityManager.readFolder(
            dbc,
            destinationFoldername,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // set user and creation timestamps
        long currentTime = System.currentTimeMillis();
        long dateLastModified;
        CmsUUID userLastModified;
        if (source.isFolder()) {
            // folders always get a new date and user when they are copied
            dateLastModified = currentTime;
            userLastModified = dbc.currentUser().getId();
        } else {
            // files keep the date and user last modified from the source
            dateLastModified = source.getDateLastModified();
            userLastModified = source.getUserLastModified();
        }

        // check the resource flags
        int flags = source.getFlags();
        if (source.isLabeled()) {
            // reset "labeled" link flag for new resource
            flags &= ~CmsResource.FLAG_LABELED;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            new CmsUUID(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getUuid(),
            CmsResource.STATE_NEW,
            currentTime,
            dbc.currentUser().getId(),
            dateLastModified,
            userLastModified,
            source.getDateReleased(),
            source.getDateExpired(),
            1,
            source.getLength(),
            source.getDateContent(),
            source.getVersion()); // version number does not matter since it will be computed later

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(dateLastModified);

        // create the resource
        newResource = createResource(dbc, destination, newResource, content, properties, false);

        // copy the access control entries to the created resource
        copyAccessControlEntries(dbc, source, newResource, false);

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

        // copy the resource to the project only if the resource is not already in the project
        if (!isInsideCurrentProject(dbc, resource.getRootPath())) {
            // check if there are already any subfolders of this resource
            if (resource.isFolder()) {
                List projectResources = m_projectDriver.readProjectResources(dbc, dbc.currentProject());
                for (int i = 0; i < projectResources.size(); i++) {
                    String resname = (String)projectResources.get(i);
                    if (resname.startsWith(resource.getRootPath())) {
                        // delete the existing project resource first
                        m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getUuid(), resname);
                    }
                }
            }
            try {
                m_projectDriver.createProjectResource(dbc, dbc.currentProject().getUuid(), resource.getRootPath());
            } catch (CmsException exc) {
                // if the subfolder exists already - all is ok
            } finally {
                OpenCms.getMemoryMonitor().flushProjectResources();

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                    "project",
                    dbc.currentProject())));
            }
        }
    }

    /**
     * Counts the locked resources in this project.<p>
     *
     * @param project the project to count the locked resources in
     * 
     * @return the amount of locked resources in this project
     */
    public int countLockedResources(CmsProject project) {

        // count locks
        return m_lockManager.countExclusiveLocksInProject(project);
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
     * @param parent the name of the parent group (or <code>null</code>)
     * 
     * @return new created group
     * 
     * @throws CmsException if the creation of the group failed
     * @throws CmsIllegalArgumentException if the length of the given name was below 1
     */
    public CmsGroup createGroup(CmsDbContext dbc, CmsUUID id, String name, String description, int flags, String parent)
    throws CmsIllegalArgumentException, CmsException {

        // check the groupname
        OpenCms.getValidationHandler().checkGroupName(name);
        // trim the name
        name = name.trim();

        // get the id of the parent group if necessary
        if (CmsStringUtil.isNotEmpty(parent)) {
            CmsGroup parentGroup = readGroup(dbc, parent);
            if (!parentGroup.isRole()
                && !CmsOrganizationalUnit.getParentFqn(parent).equals(CmsOrganizationalUnit.getParentFqn(name))) {
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_PARENT_GROUP_MUST_BE_IN_SAME_OU_3,
                    CmsOrganizationalUnit.getSimpleName(name),
                    CmsOrganizationalUnit.getParentFqn(name),
                    parent));
            }
        }

        // create the group
        CmsGroup group = m_userDriver.createGroup(dbc, id, name, description, flags, parent);

        // if group is virtualizing a role, initialize it
        if (group.isVirtual()) {
            // get all users that have the given role
            String groupname = CmsRole.valueOf(group).getGroupName();
            Iterator it = getUsersOfGroup(dbc, groupname, true, false, true).iterator();
            while (it.hasNext()) {
                CmsUser user = (CmsUser)it.next();
                // put them in the new group
                addUserToGroup(dbc, user.getName(), group.getName(), true);
            }
        }

        // put it into the cache
        OpenCms.getMemoryMonitor().cacheGroup(group);
        // return it
        return group;
    }

    /**
     * Creates a new organizational unit.<p>
     * 
     * @param dbc the current db context
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
        CmsDbContext dbc,
        String ouFqn,
        String description,
        int flags,
        CmsResource resource) throws CmsException {

        // normal case
        CmsOrganizationalUnit parent = readOrganizationalUnit(dbc, CmsOrganizationalUnit.getParentFqn(ouFqn));
        String name = CmsOrganizationalUnit.getSimpleName(ouFqn);
        if (name.endsWith(CmsOrganizationalUnit.SEPARATOR)) {
            name = name.substring(0, name.length() - 1);
        }
        // check the name
        OpenCms.getValidationHandler().checkOrganizationalUnitName(name);

        // trim the name
        name = name.trim();
        // create the organizational unit
        CmsOrganizationalUnit orgUnit = m_userDriver.createOrganizationalUnit(
            dbc,
            name,
            description,
            flags,
            parent,
            resource.getRootPath());
        // put the new created org unit into the cache
        OpenCms.getMemoryMonitor().cacheOrgUnit(orgUnit);
        // return it
        return orgUnit;
    }

    /**
     * Creates a project.<p>
     *
     * @param dbc the current database context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype the type of the project
     * 
     * @return the created project
     * 
     * @throws CmsIllegalArgumentException if the chosen <code>name</code> is already used 
     *         by the online project
     * @throws CmsDataAccessException if something goes wrong
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        String name,
        String description,
        String groupname,
        String managergroupname,
        CmsProject.CmsProjectType projecttype) throws CmsIllegalArgumentException, CmsDataAccessException {

        if (CmsProject.ONLINE_PROJECT_NAME.equals(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_CREATE_PROJECT_ONLINE_PROJECT_NAME_1,
                CmsProject.ONLINE_PROJECT_NAME));
        }
        // read the needed groups from the cms
        CmsGroup group = readGroup(dbc, groupname);
        CmsGroup managergroup = readGroup(dbc, managergroupname);

        return m_projectDriver.createProject(
            dbc,
            new CmsUUID(),
            dbc.currentUser(),
            group,
            managergroup,
            name,
            description,
            CmsProject.PROJECT_FLAG_NONE,
            projecttype);
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
    public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        CmsPropertyDefinition propertyDefinition = null;

        name = name.trim();
        // validate the property name
        CmsPropertyDefinition.checkPropertyName(name);
        int todo; // make the type a parameter
        try {
            try {
                propertyDefinition = m_vfsDriver.readPropertyDefinition(dbc, name, dbc.currentProject().getUuid());
            } catch (CmsException e) {
                propertyDefinition = m_vfsDriver.createPropertyDefinition(
                    dbc,
                    dbc.currentProject().getUuid(),
                    name,
                    CmsPropertyDefinition.TYPE_NORMAL);
            }

            try {
                m_vfsDriver.readPropertyDefinition(dbc, name, CmsProject.ONLINE_PROJECT_ID);
            } catch (CmsException e) {
                m_vfsDriver.createPropertyDefinition(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    name,
                    CmsPropertyDefinition.TYPE_NORMAL);
            }

            try {
                m_historyDriver.readPropertyDefinition(dbc, name);
            } catch (CmsException e) {
                m_historyDriver.createPropertyDefinition(dbc, name, CmsPropertyDefinition.TYPE_NORMAL);
            }
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_PROPERTY_DEFINITION_CREATED,
                Collections.singletonMap("propertyDefinition", propertyDefinition)));

        }

        return propertyDefinition;
    }

    /**
     * Creates a new publish job.<p>
     * 
     * @param dbc the current database context
     * @param publishJob the publish job to create
     * 
     * @throws CmsException if something goes wrong
     */
    public void createPublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsException {

        m_projectDriver.createPublishJob(dbc, publishJob);
    }

    /**
     * Creates a new resource with the provided content and properties.<p>
     * 
     * The <code>content</code> parameter may be <code>null</code> if the resource id 
     * already exists. If so, the created resource will be a sibling of the existing 
     * resource, the existing content will remain unchanged.<p>
     * 
     * This is used during file import for import of siblings as the 
     * <code>manifest.xml</code> only contains one binary copy per file.<p>
     *  
     * If the resource id exists but the <code>content</code> is not <code>null</code>,
     * the created resource will be made a sibling of the existing resource,
     * and both will share the new content.<p>
     * 
     * @param dbc the current database context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @param importCase if <code>true</code>, signals that this operation is done while 
     *                      importing resource, causing different lock behaviour and 
     *                      potential "lost and found" usage
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized CmsResource createResource(
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

            // check if the resource already exists by name
            CmsResource currentResourceByName = null;
            try {
                currentResourceByName = readResource(dbc, resourcePath, CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, we have to check the id later to decide what to do
            }

            // check if the resource already exists by id
            CmsResource currentResourceById = null;
            try {
                currentResourceById = readResource(dbc, resource.getStructureId(), CmsResourceFilter.ALL);
                // reset if it is the same as by name
                if ((currentResourceByName != null)
                    && currentResourceById.getRootPath().equals(currentResourceByName.getRootPath())) {
                    currentResourceById = null;
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, we have to check the id later to decide what to do
            }

            // need to provide the parent folder id for resource creation
            String parentFolderName = CmsResource.getParentFolder(resourcePath);
            CmsResource parentFolder = readFolder(dbc, parentFolderName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the permissions
            if (currentResourceByName == null) {
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
                    currentResourceByName,
                    CmsPermissionSet.ACCESS_WRITE,
                    !importCase,
                    CmsResourceFilter.ALL);
            }
            if (currentResourceById != null) {
                // resource already exists - check existing resource              
                m_securityManager.checkPermissions(
                    dbc,
                    currentResourceById,
                    CmsPermissionSet.ACCESS_WRITE,
                    !importCase,
                    CmsResourceFilter.ALL);
            }

            // first handle a possible moved resource
            if (currentResourceById != null) {
                // a resource in another place is using the same id
                if (!importCase) {
                    // direct "overwrite" of a resource is possible only during import, 
                    // or if the resource has been deleted
                    throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                        Messages.ERR_RESOURCE_WITH_ID_ALREADY_EXISTS_3,
                        dbc.removeSiteRoot(resource.getRootPath()),
                        currentResourceById.getRootPath(),
                        currentResourceById.getStructureId()));
                }
                // lock the resource by id, will throw an exception if not lockable
                lockResource(dbc, currentResourceById, CmsLockType.EXCLUSIVE);

                // deleted resources were not moved to L&F
                if (currentResourceById.getState().isDeleted()) {
                    if (!currentResourceById.isFolder()) {
                        // trigger createResource instead of writeResource
                        currentResourceById = null;
                    }
                } else {
                    // the resource already exists
                    if (!resource.isFolder()
                        && useLostAndFound
                        && (!currentResourceById.getResourceId().equals(resource.getResourceId()))) {
                        // semantic change: the current resource is moved to L&F and the imported resource will overwrite the old one                
                        // will leave the resource with state deleted, 
                        // but it does not matter, since the state will be set later again
                        moveToLostAndFound(dbc, currentResourceById, false);
                    }
                }

                if (currentResourceByName == null) {
                    // move resource back to original place
                    moveResource(dbc, currentResourceById, resourcePath, true);
                }
            }

            // now look for the resource by name
            if (currentResourceByName != null) {
                boolean overwrite = true;
                if (currentResourceByName.getState().isDeleted()) {
                    if (!currentResourceByName.isFolder()) {
                        // if a non-folder resource was deleted it's treated like a new resource
                        overwrite = false;
                    }
                } else {
                    if (!importCase) {
                        // direct "overwrite" of a resource is possible only during import, 
                        // or if the resource has been deleted
                        throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                            org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                            dbc.removeSiteRoot(resource.getRootPath())));
                    }
                    // the resource already exists
                    if (!resource.isFolder()
                        && useLostAndFound
                        && (!currentResourceByName.getResourceId().equals(resource.getResourceId()))) {
                        // semantic change: the current resource is moved to L&F and the imported resource will overwrite the old one                
                        // will leave the resource with state deleted, 
                        // but it does not matter, since the state will be set later again
                        moveToLostAndFound(dbc, currentResourceByName, false);
                    }
                }
                if (!overwrite) {
                    // lock the resource, will throw an exception if not lockable
                    lockResource(dbc, currentResourceByName, CmsLockType.EXCLUSIVE);
                    // trigger createResource instead of writeResource
                    currentResourceByName = null;
                }
            }
            // if null, create new resource, if not null write resource
            CmsResource overwrittenResource = (currentResourceById != null ? currentResourceById
            : currentResourceByName);

            // extract the name (without path)
            String targetName = CmsResource.getName(resourcePath);

            int contentLength;

            // modify target name and content length in case of folder creation
            if (resource.isFolder()) {
                // folders never have any content
                contentLength = -1;
                // must cut of trailing '/' for folder creation (or name check fails)
                if (CmsResource.isFolder(targetName)) {
                    targetName = targetName.substring(0, targetName.length() - 1);
                }
            } else {
                // otherwise ensure content and content length are set correctly
                if (content != null) {
                    // if a content is provided, in each case the length is the length of this content
                    contentLength = content.length;
                } else if (overwrittenResource != null) {
                    // we have no content, but an already existing resource - length remains unchanged
                    contentLength = overwrittenResource.getLength();
                } else {
                    // we have no content - length is used as set in the resource
                    contentLength = resource.getLength();
                }
            }

            // check if the target name is valid (forbitten chars etc.), 
            // if not throw an exception
            // must do this here since targetName is modified in folder case (see above)
            CmsResource.checkResourceName(targetName);

            // set structure and resource ids as given
            CmsUUID structureId = resource.getStructureId();
            CmsUUID resourceId = resource.getResourceId();

            // decide which structure id to use
            if (overwrittenResource != null) {
                // resource exists, re-use existing ids
                structureId = overwrittenResource.getStructureId();
            }
            if (structureId.isNullUUID()) {
                // need a new structure id
                structureId = new CmsUUID();
            }

            // decide which resource id to use
            if (overwrittenResource != null) {
                // if we are overwritting we have to assure the resource id is the same
                resourceId = overwrittenResource.getResourceId();
            }
            if (resourceId.isNullUUID()) {
                // need a new resource id
                resourceId = new CmsUUID();
            }

            try {
                // check online resource
                CmsResource onlineResource = m_vfsDriver.readResource(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    resourcePath,
                    true);
                // only allow to overwrite with diff id if importing (createResource will set the right id)
                try {
                    CmsResource offlineResource = m_vfsDriver.readResource(
                        dbc,
                        dbc.currentProject().getUuid(),
                        onlineResource.getStructureId(),
                        true);
                    if (!offlineResource.getRootPath().equals(onlineResource.getRootPath())) {
                        throw new CmsVfsOnlineResourceAlreadyExistsException(Messages.get().container(
                            Messages.ERR_ONLINE_RESOURCE_EXISTS_2,
                            dbc.removeSiteRoot(resourcePath),
                            dbc.removeSiteRoot(offlineResource.getRootPath())));
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // there is no problem for now
                    // but should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // ok, there is no online entry to worry about
            }

            // now create a resource object with all informations
            newResource = new CmsResource(
                structureId,
                resourceId,
                resourcePath,
                resource.getTypeId(),
                resource.isFolder(),
                resource.getFlags(),
                dbc.currentProject().getUuid(),
                resource.getState(),
                resource.getDateCreated(),
                resource.getUserCreated(),
                resource.getDateLastModified(),
                resource.getUserLastModified(),
                resource.getDateReleased(),
                resource.getDateExpired(),
                1,
                contentLength,
                resource.getDateContent(),
                resource.getVersion()); // version number does not matter since it will be computed later

            // ensure date is updated only if required
            if (resource.isTouched()) {
                // this will trigger the internal "is touched" state on the new resource
                newResource.setDateLastModified(resource.getDateLastModified());
            }

            if (resource.isFile()) {
                // check if a sibling to the imported resource lies in a marked site
                if (labelResource(dbc, resource, resourcePath, 2)) {
                    int flags = resource.getFlags();
                    flags |= CmsResource.FLAG_LABELED;
                    resource.setFlags(flags);
                }
                // ensure siblings don't overwrite existing resource records
                if (content == null) {
                    newResource.setState(CmsResource.STATE_KEEP);
                }
            }

            if (overwrittenResource == null) {
                CmsLock lock = getLock(dbc, newResource);
                if (lock.getEditionLock().isExclusive()) {
                    unlockResource(dbc, newResource, true, false);
                }
                // resource does not exist.
                newResource = m_vfsDriver.createResource(dbc, dbc.currentProject().getUuid(), newResource, content);
            } else {
                // lock the original resource
                lockResource(dbc, overwrittenResource, CmsLockType.EXCLUSIVE);

                // resource already exists. 
                // probably the resource is a merged page file that gets overwritten during import, or it gets 
                // overwritten by a copy operation. if so, the structure & resource state are not modified to changed.
                int updateStates = (overwrittenResource.getState().isNew() ? CmsDriverManager.NOTHING_CHANGED
                : CmsDriverManager.UPDATE_ALL);
                m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), newResource, updateStates);

                if ((content != null) && resource.isFile()) {
                    // also update file content if required                    
                    long contentModificationDate = m_vfsDriver.writeContent(
                        dbc,
                        dbc.currentProject().getUuid(),
                        newResource.getResourceId(),
                        content);
                    newResource = new CmsResource(
                        newResource.getStructureId(),
                        newResource.getResourceId(),
                        newResource.getRootPath(),
                        newResource.getTypeId(),
                        newResource.isFolder(),
                        newResource.getFlags(),
                        newResource.getProjectLastModified(),
                        newResource.getState(),
                        newResource.getDateCreated(),
                        newResource.getUserCreated(),
                        newResource.getDateLastModified(),
                        newResource.getUserLastModified(),
                        newResource.getDateReleased(),
                        newResource.getDateExpired(),
                        newResource.getSiblingCount(),
                        newResource.getLength(),
                        contentModificationDate,
                        newResource.getVersion());
                }
            }

            // write the properties (internal operation, no events or duplicate permission checks)
            writePropertyObjects(dbc, newResource, properties, false);

            // lock the created resource
            try {
                // if it is locked by another user (copied or moved resource) this lock should be preserved and 
                // the exception is OK: locks on created resources are a slave feature to original locks 
                lockResource(dbc, newResource, CmsLockType.EXCLUSIVE);
            } catch (CmsLockException cle) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.ERR_CREATE_RESOURCE_LOCK_1,
                        new Object[] {dbc.removeSiteRoot(newResource.getRootPath())}));
                }
            }
            // delete all relations for the resource, the content relations will be rebuild as soon as needed
            deleteRelationsForResource(dbc, newResource, CmsRelationFilter.TARGETS);
        } finally {
            // clear the internal caches
            clearAccessControlListCache();
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();

            if (newResource != null) {
                // fire an event that a new resource has been created
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_CREATED, Collections.singletonMap(
                    "resource",
                    newResource)));
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
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see I_CmsResourceType#createResource(CmsObject, CmsSecurityManager, String, byte[], List)
     */
    public CmsResource createResource(CmsDbContext dbc, String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        String targetName = resourcename;

        if (content == null) {
            // name based resource creation MUST have a content
            content = new byte[0];
        }
        int size;

        if (CmsFolder.isFolderType(type)) {
            // must cut of trailing '/' for folder creation
            if (CmsResource.isFolder(targetName)) {
                targetName = targetName.substring(0, targetName.length() - 1);
            }
            size = -1;
        } else {
            size = content.length;
        }

        // create a new resource
        CmsResource newResource = new CmsResource(CmsUUID.getNullUUID(), // uuids will be "corrected" later
            CmsUUID.getNullUUID(),
            targetName,
            type,
            CmsFolder.isFolderType(type),
            0,
            dbc.currentProject().getUuid(),
            CmsResource.STATE_NEW,
            0,
            dbc.currentUser().getId(),
            0,
            dbc.currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            size,
            0, // version number does not matter since it will be computed later
            0); // content time will be corrected later

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
     * @return the new created sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see I_CmsResourceType#createSibling(CmsObject, CmsSecurityManager, CmsResource, String, List)
     */
    public CmsResource createSibling(CmsDbContext dbc, CmsResource source, String destination, List properties)
    throws CmsException {

        if (source.isFolder()) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS_0));
        }

        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = readFolder(dbc, destinationFoldername, CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // check the resource flags
        int flags = source.getFlags();
        if (labelResource(dbc, source, destination, 1)) {
            // set "labeled" link flag for new resource
            flags |= CmsResource.FLAG_LABELED;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            source.getResourceId(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getUuid(),
            CmsResource.STATE_KEEP,
            source.getDateCreated(), // ensures current resource record remains untouched 
            source.getUserCreated(),
            source.getDateLastModified(),
            source.getUserLastModified(),
            source.getDateReleased(),
            source.getDateExpired(),
            source.getSiblingCount() + 1,
            source.getLength(),
            source.getDateContent(),
            source.getVersion()); // version number does not matter since it will be computed later

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
        OpenCms.fireCmsEvent(new CmsEvent(
            I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
            Collections.singletonMap("resources", modifiedResources)));

        return newResource;
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @param dbc the current database context
     * 
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsDbContext dbc) throws CmsException {

        // read the needed groups from the cms
        CmsGroup projectUserGroup = readGroup(dbc, dbc.currentProject().getGroupId());
        CmsGroup projectManagerGroup = readGroup(dbc, dbc.currentProject().getManagerGroupId());

        CmsProject tempProject = m_projectDriver.createProject(
            dbc,
            new CmsUUID(),
            dbc.currentUser(),
            projectUserGroup,
            projectManagerGroup,
            I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME,
            Messages.get().getBundle(dbc.getRequestContext().getLocale()).key(
                Messages.GUI_WORKPLACE_TEMPFILE_PROJECT_DESC_0),
            CmsProject.PROJECT_FLAG_HIDDEN,
            CmsProject.PROJECT_TYPE_NORMAL);
        m_projectDriver.createProjectResource(dbc, tempProject.getUuid(), "/");

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
            "project",
            tempProject)));

        return tempProject;
    }

    /**
     * Creates a new user.<p>
     *
     * @param dbc the current database context
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
     * @throws CmsIllegalArgumentException if the name for the user is not valid
     */
    public CmsUser createUser(CmsDbContext dbc, String name, String password, String description, Map additionalInfos)
    throws CmsException, CmsIllegalArgumentException {

        // no space before or after the name
        name = name.trim();
        String userName = CmsOrganizationalUnit.getSimpleName(name);
        // check the username
        OpenCms.getValidationHandler().checkUserName(userName);
        // check the password
        validatePassword(password);

        Map info = new HashMap();
        if (additionalInfos != null) {
            info.putAll(additionalInfos);
        }
        if (description != null) {
            info.put(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, description);
        }
        if ((name.length() > 0)) {
            return m_userDriver.createUser(
                dbc,
                new CmsUUID(),
                name,
                OpenCms.getPasswordHandler().digest(password),
                " ",
                " ",
                " ",
                0,
                I_CmsPrincipal.FLAG_ENABLED,
                0,
                info);
        } else {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_USER_1, name));
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
     * @param resourcename the name of the resource for which all properties should be deleted
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteAllProperties(CmsDbContext dbc, String resourcename) throws CmsException {

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
                false,
                CmsResourceFilter.ALL);

            // delete the property values
            if (resource.getSiblingCount() > 1) {
                // the resource has siblings- delete only the (structure) properties of this sibling
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    resource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES);
                resources.addAll(readSiblings(dbc, resource, CmsResourceFilter.ALL));

            } else {
                // the resource has no other siblings- delete all (structure+resource) properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    resource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                resources.add(resource);
            }
        } finally {
            // clear the driver manager cache
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();

            // fire an event that all properties of a resource have been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resources", resources)));
        }
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, int linkType) throws CmsException {

        m_projectDriver.deleteAllStaticExportPublishedResources(dbc, linkType);
    }

    /**
     * Deletes a group, where all permissions, users and childs of the group
     * are transfered to a replacement group.<p>
     * 
     * @param dbc the current request context
     * @param group the id of the group to be deleted
     * @param replacementId the id of the group to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDataAccessException if group to be deleted contains user
     */
    public void deleteGroup(CmsDbContext dbc, CmsGroup group, CmsUUID replacementId)
    throws CmsDataAccessException, CmsException {

        CmsGroup replacementGroup = null;
        if (replacementId != null) {
            replacementGroup = readGroup(dbc, replacementId);
        }
        // get all child groups of the group
        List childs = getChild(dbc, group);
        // get all users in this group
        List users = getUsersOfGroup(dbc, group.getName(), true, false, group.isRole());
        // get online project
        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        if (replacementGroup == null) {
            // remove users
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                if (userInGroup(dbc, user.getName(), group.getName(), group.isRole())) {
                    removeUserFromGroup(dbc, user.getName(), group.getName(), true);
                }
            }
            // transfer childs to grandfather if possible
            CmsUUID parentId = group.getParentId();
            if (parentId == null) {
                parentId = CmsUUID.getNullUUID();
            }
            Iterator itChilds = childs.iterator();
            while (itChilds.hasNext()) {
                CmsGroup child = (CmsGroup)itChilds.next();
                child.setParentId(parentId);
                writeGroup(dbc, child);
            }
        } else {
            // move childs
            Iterator itChilds = childs.iterator();
            while (itChilds.hasNext()) {
                CmsGroup child = (CmsGroup)itChilds.next();
                child.setParentId(replacementId);
                writeGroup(dbc, child);
            }
            // move users
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                addUserToGroup(dbc, user.getName(), replacementGroup.getName(), group.isRole());
                removeUserFromGroup(dbc, user.getName(), group.getName(), group.isRole());
            }
            // transfer for offline
            transferPrincipalResources(dbc, dbc.currentProject(), group.getId(), replacementId, true);
            // transfer for online
            transferPrincipalResources(dbc, onlineProject, group.getId(), replacementId, true);
        }
        // remove the group
        m_userDriver.removeAccessControlEntriesForPrincipal(dbc, dbc.currentProject(), onlineProject, group.getId());
        m_userDriver.deleteGroup(dbc, group.getName());
        // backup the group
        m_historyDriver.writePrincipal(dbc, group);

        // clear the relevant caches
        OpenCms.getMemoryMonitor().uncacheGroup(group);
        OpenCms.getMemoryMonitor().flushUserGroups();
        OpenCms.getMemoryMonitor().flushACLs();
    }

    /**
     * Deletes the versions from the history tables, keeping the given number of versions per resource.<p>
     * 
     * if the <code>cleanUp</code> option is set, additionally versions of deleted resources will be removed.<p>
     * 
     * @param dbc the current database context
     * @param folder the folder (with subresources) to delete historical versions for 
     * @param versionsToKeep number of versions to keep, is ignored if negative 
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not succesful
     */
    public void deleteHistoricalVersions(
        CmsDbContext dbc,
        CmsFolder folder,
        int versionsToKeep,
        int versionsDeleted,
        long timeDeleted,
        I_CmsReport report) throws CmsException {

        report.println(Messages.get().container(Messages.RPT_START_DELETE_VERSIONS_0), I_CmsReport.FORMAT_HEADLINE);
        if (versionsToKeep >= 0) {
            report.println(Messages.get().container(
                Messages.RPT_START_DELETE_ACT_VERSIONS_1,
                new Integer(versionsToKeep)), I_CmsReport.FORMAT_HEADLINE);

            List resources = m_historyDriver.getAllNotDeletedEntries(dbc, folder.getStructureId());
            if (resources.isEmpty()) {
                report.println(Messages.get().container(Messages.RPT_DELETE_NOTHING_0), I_CmsReport.FORMAT_OK);
            }
            int n = resources.size();
            int m = 1;
            Iterator itResources = resources.iterator();
            while (itResources.hasNext()) {
                I_CmsHistoryResource histResource = (I_CmsHistoryResource)itResources.next();

                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(histResource.getResource().getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    int deleted = m_historyDriver.deleteEntries(dbc, histResource, versionsToKeep, -1);

                    report.print(
                        Messages.get().container(Messages.RPT_VERSION_DELETING_1, new Integer(deleted)),
                        I_CmsReport.FORMAT_NOTE);
                    report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } catch (CmsDataAccessException e) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ERROR_0),
                        I_CmsReport.FORMAT_ERROR);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }

                m++;
            }

            report.println(
                Messages.get().container(Messages.RPT_END_DELETE_ACT_VERSIONS_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        if ((versionsDeleted >= 0) || (timeDeleted >= 0)) {
            if (timeDeleted >= 0) {
                report.println(Messages.get().container(
                    Messages.RPT_START_DELETE_DEL_VERSIONS_2,
                    new Integer(versionsToKeep),
                    new Date(timeDeleted)), I_CmsReport.FORMAT_HEADLINE);
            } else {
                report.println(Messages.get().container(
                    Messages.RPT_START_DELETE_DEL_VERSIONS_1,
                    new Integer(versionsToKeep)), I_CmsReport.FORMAT_HEADLINE);
            }
            List resources = m_historyDriver.getAllDeletedEntries(dbc, folder.getStructureId());
            if (resources.isEmpty()) {
                report.println(Messages.get().container(Messages.RPT_DELETE_NOTHING_0), I_CmsReport.FORMAT_OK);
            }
            int n = resources.size();
            int m = 1;
            Iterator itResources = resources.iterator();
            while (itResources.hasNext()) {
                I_CmsHistoryResource histResource = (I_CmsHistoryResource)itResources.next();

                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(histResource.getResource().getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    int deleted = m_historyDriver.deleteEntries(dbc, histResource, versionsToKeep, timeDeleted);

                    report.print(
                        Messages.get().container(Messages.RPT_VERSION_DELETING_1, new Integer(deleted)),
                        I_CmsReport.FORMAT_NOTE);
                    report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } catch (CmsDataAccessException e) {
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ERROR_0),
                        I_CmsReport.FORMAT_ERROR);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }

                m++;
            }
            report.println(
                Messages.get().container(Messages.RPT_END_DELETE_DEL_VERSIONS_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        report.println(Messages.get().container(Messages.RPT_END_DELETE_VERSIONS_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Deletes an organizational unit.<p>
     *
     * Only organizational units that contain no suborganizational unit can be deleted.<p>
     * 
     * The organizational unit can not be delete if it is used in the reuqest context, 
     * or if the current user belongs to it.<p>
     * 
     * All users and groups in the given organizational unit will be deleted.<p>
     * 
     * @param dbc the current db context
     * @param organizationalUnit the organizational unit to delete
     * 
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#deleteOrganizationalUnit(CmsObject, String)
     */
    public void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit)
    throws CmsException {

        // check organizational unit in context
        if (dbc.getRequestContext().getOuFqn().equals(organizationalUnit.getName())) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_DELETE_IN_CONTEXT_1,
                organizationalUnit.getName()));
        }
        // check organizational unit for user
        if (dbc.currentUser().getOuFqn().equals(organizationalUnit.getName())) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_DELETE_CURRENT_USER_1,
                organizationalUnit.getName()));
        }
        // check sub organizational units
        if (!getOrganizationalUnits(dbc, organizationalUnit, true).isEmpty()) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_DELETE_SUB_ORGUNITS_1,
                organizationalUnit.getName()));
        }
        List groups = getGroups(dbc, organizationalUnit, true, false);
        boolean hasGroups = (groups.size() > 4);
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext() && !hasGroups) {
            CmsGroup group = (CmsGroup)itGroups.next();
            if (!OpenCms.getDefaultUsers().isDefaultGroup(group.getName())) {
                hasGroups = true;
            }
        }
        // check groups
        if (hasGroups) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_DELETE_GROUPS_1,
                organizationalUnit.getName()));
        }
        // check users
        if (!getUsers(dbc, organizationalUnit, false).isEmpty()) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_DELETE_USERS_1,
                organizationalUnit.getName()));
        }

        // delete default groups if needed
        itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            deleteGroup(dbc, group, null);
        }

        // delete projects
        Iterator itProjects = m_projectDriver.readProjects(dbc, organizationalUnit.getName()).iterator();
        while (itProjects.hasNext()) {
            CmsProject project = (CmsProject)itProjects.next();
            deleteProject(dbc, project);
        }

        // delete roles
        Iterator itRoles = getGroups(dbc, organizationalUnit, true, true).iterator();
        while (itRoles.hasNext()) {
            CmsGroup role = (CmsGroup)itRoles.next();
            deleteGroup(dbc, role, null);
        }

        // remove the organizational unit itself
        m_userDriver.deleteOrganizationalUnit(dbc, organizationalUnit);

        // flush relevant caches
        clearAccessControlListCache();
        OpenCms.getMemoryMonitor().uncacheOrgUnit(organizationalUnit);
        OpenCms.getMemoryMonitor().flushProperties();
        OpenCms.getMemoryMonitor().flushPropertyLists();
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
    public void deleteProject(CmsDbContext dbc, CmsProject deleteProject) throws CmsException {

        CmsUUID projectId = deleteProject.getUuid();
        // changed/new/deleted files in the specified project
        List modifiedFiles = readChangedResourcesInsideProject(dbc, projectId, RCPRM_FILES_ONLY_MODE);
        // changed/new/deleted folders in the specified project
        List modifiedFolders = readChangedResourcesInsideProject(dbc, projectId, RCPRM_FOLDERS_ONLY_MODE);

        // all resources inside the project have to be be reset to their online state.
        // 1. step: delete all new files
        for (int i = 0; i < modifiedFiles.size(); i++) {
            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);
            if (currentFile.getState().isNew()) {
                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedBy(dbc.currentUser()) || !lock.isInProject(dbc.currentProject())) {
                    changeLock(dbc, currentFile, CmsLockType.EXCLUSIVE);
                }
                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFile,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                // delete the file
                m_vfsDriver.removeFile(dbc, dbc.currentProject().getUuid(), currentFile);
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), currentFile.getResourceId());
                // fire the corresponding event
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFile)));
            }
        }

        // 2. step: delete all new folders
        for (int i = 0; i < modifiedFolders.size(); i++) {
            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);
            if (currentFolder.getState().isNew()) {
                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                // delete the folder
                m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentFolder);
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), currentFolder.getResourceId());
                // fire the corresponding event
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFolder)));
            }
        }

        // 3. step: undo changes on all changed or deleted folders
        for (int i = 0; i < modifiedFolders.size(); i++) {
            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);
            if ((currentFolder.getState().isChanged()) || (currentFolder.getState().isDeleted())) {
                CmsLock lock = getLock(dbc, currentFolder);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFolder, CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedBy(dbc.currentUser()) || !lock.isInProject(dbc.currentProject())) {
                    changeLock(dbc, currentFolder, CmsLockType.EXCLUSIVE);
                }
                // undo all changes in the folder
                undoChanges(dbc, currentFolder, CmsResource.UNDO_CONTENT);
                // fire the corresponding event
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFolder)));
            }
        }

        // 4. step: undo changes on all changed or deleted files 
        for (int i = 0; i < modifiedFiles.size(); i++) {
            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);
            if (currentFile.getState().isChanged() || currentFile.getState().isDeleted()) {
                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedInProjectBy(dbc.currentUser(), dbc.currentProject())) {
                    if (lock.isLockableBy(dbc.currentUser())) {
                        changeLock(dbc, currentFile, CmsLockType.EXCLUSIVE);
                    }
                }
                // undo all changes in the file
                undoChanges(dbc, currentFile, CmsResource.UNDO_CONTENT);
                // fire the corresponding event
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFile)));
            }
        }

        // unlock all resources in the project
        m_lockManager.removeResourcesInProject(deleteProject.getUuid(), false);
        clearAccessControlListCache();
        clearResourceCache();

        // set project to online project if current project is the one which will be deleted 
        if (projectId.equals(dbc.currentProject().getUuid())) {
            dbc.getRequestContext().setCurrentProject(readProject(dbc, CmsProject.ONLINE_PROJECT_ID));
        }

        // delete the project itself
        m_projectDriver.deleteProject(dbc, deleteProject);
        OpenCms.getMemoryMonitor().uncacheProject(deleteProject);

        // fire the corresponding event
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
    public void deletePropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        CmsPropertyDefinition propertyDefinition = null;

        try {
            // first read and then delete the metadefinition.            
            propertyDefinition = readPropertyDefinition(dbc, name);
            m_vfsDriver.deletePropertyDefinition(dbc, propertyDefinition);
            m_historyDriver.deletePropertyDefinition(dbc, propertyDefinition);
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_PROPERTY_DEFINITION_MODIFIED,
                Collections.singletonMap("propertyDefinition", propertyDefinition)));
        }
    }

    /**
     * Deletes a publish job identified by its history id.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId the history id identifying the publish job
     * 
     * @throws CmsException if something goes wrong
     */
    public void deletePublishJob(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        m_projectDriver.deletePublishJob(dbc, publishHistoryId);
    }

    /**
     * Deletes the publish list assigned to a publish job.<p>
     * 
     * @param dbc the current database context 
     * @param publishHistoryId the history id identifying the publish job
     * @throws CmsException if something goes wrong
     */
    public void deletePublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        m_projectDriver.deletePublishList(dbc, publishHistoryId);
    }

    /**
     * Deletes all relations for the given resource matching the given filter.<p>
     * 
     * @param dbc the current db context
     * @param resource the resource to delete the relations for
     * @param filter the filter to use for deletion
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#deleteRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public void deleteRelationsForResource(CmsDbContext dbc, CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        m_vfsDriver.deleteRelations(dbc, dbc.currentProject().getUuid(), resource, filter);
    }

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, CmsResource.CmsResourceDeleteMode)
     * @see I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(CmsDbContext dbc, CmsResource resource, CmsResource.CmsResourceDeleteMode siblingMode)
    throws CmsException {

        // upgrade a potential inherited, non-shared lock into a common lock
        CmsLock currentLock = getLock(dbc, resource);
        if (currentLock.getEditionLock().isDirectlyInherited()) {
            // upgrade the lock status if required
            lockResource(dbc, resource, CmsLockType.EXCLUSIVE);
        }

        // check if siblings of the resource exist and must be deleted as well
        if (resource.isFolder()) {
            // folder can have no siblings
            siblingMode = CmsResource.DELETE_PRESERVE_SIBLINGS;
        }

        // if selected, add all siblings of this resource to the list of resources to be deleted    
        boolean allSiblingsRemoved;
        List resources;
        if (siblingMode == CmsResource.DELETE_REMOVE_SIBLINGS) {
            resources = new ArrayList(readSiblings(dbc, resource, CmsResourceFilter.ALL));
            allSiblingsRemoved = true;

            // ensure that the resource requested to be deleted is the last resource that gets actually deleted
            // to keep the shared locks of the siblings while those get deleted.
            resources.remove(resource);
            resources.add(resource);
        } else {
            // only delete the resource, no siblings
            resources = Collections.singletonList(resource);
            allSiblingsRemoved = false;
        }

        int size = resources.size();
        // if we have only one resource no further check is required
        if (size > 1) {
            CmsMultiException me = new CmsMultiException();
            // ensure that each sibling is unlocked or locked by the current user
            for (int i = 0; i < size; i++) {
                CmsResource currentResource = (CmsResource)resources.get(i);
                currentLock = getLock(dbc, currentResource);
                if (!currentLock.getEditionLock().isUnlocked() && !currentLock.isOwnedBy(dbc.currentUser())) {
                    // the resource is locked by a user different from the current user
                    CmsRequestContext context = dbc.getRequestContext();
                    me.addException(new CmsLockException(org.opencms.lock.Messages.get().container(
                        org.opencms.lock.Messages.ERR_SIBLING_LOCKED_2,
                        context.getSitePath(currentResource),
                        context.getSitePath(resource))));
                }
            }
            if (!me.getExceptions().isEmpty()) {
                throw me;
            }
        }

        boolean removeAce = true;

        if (resource.isFolder()) {
            // check if the folder has any resources in it
            Iterator childResources = m_vfsDriver.readChildResources(dbc, dbc.currentProject(), resource, true, true).iterator();

            CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
            if (dbc.currentProject().isOnlineProject()) {
                projectId = CmsUUID.getOpenCmsUUID(); // HACK: to get an offline project id
            }

            // collect the names of the resources inside the folder, excluding the moved resources
            StringBuffer errorResNames = new StringBuffer(128);
            while (childResources.hasNext()) {
                CmsResource errorRes = (CmsResource)childResources.next();
                if (errorRes.getState().isDeleted()) {
                    continue;
                }
                // if deleting offline, or not moved, or just renamed inside the deleted folder
                // so, it may remain some orphan online entries for moved resources
                // which will be fixed during the publishing of the moved resources
                boolean error = !dbc.currentProject().isOnlineProject();
                if (!error) {
                    try {
                        String originalPath = m_vfsDriver.readResource(dbc, projectId, errorRes.getRootPath(), true).getRootPath();
                        error = originalPath.equals(errorRes.getRootPath())
                            || originalPath.startsWith(resource.getRootPath());
                    } catch (CmsVfsResourceNotFoundException e) {
                        // ignore
                    }
                }
                if (error) {
                    if (errorResNames.length() != 0) {
                        errorResNames.append(", ");
                    }
                    errorResNames.append("[" + dbc.removeSiteRoot(errorRes.getRootPath()) + "]");
                }
            }

            // the current implementation only deletes empty folders
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(errorResNames.toString())) {
                throw new CmsVfsException(org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_DELETE_NONEMTY_FOLDER_2,
                    dbc.removeSiteRoot(resource.getRootPath()),
                    errorResNames.toString()));
            }
        }

        // delete all collected resources
        for (int i = 0; i < size; i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);

            // try to delete/remove the resource only if the user has write access to the resource
            // check permissions only for the sibling, the resource it self was already checked or 
            // is to be removed without write permissions, ie. while deleting a folder
            if (!currentResource.equals(resource)
                && (CmsSecurityManager.PERM_ALLOWED != m_securityManager.hasPermissions(
                    dbc,
                    currentResource,
                    CmsPermissionSet.ACCESS_WRITE,
                    true,
                    CmsResourceFilter.ALL))) {

                // no write access to sibling - must keep ACE (see below)
                allSiblingsRemoved = false;
            } else {
                // write access to sibling granted                 
                boolean existsOnline = m_vfsDriver.validateStructureIdExists(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    currentResource.getStructureId());

                if (!existsOnline) {
                    // the resource does not exist online => remove the resource
                    // this means the resoruce is "new" (blue) in the offline project                

                    // delete all properties of this resource
                    deleteAllProperties(dbc, currentResource.getRootPath());

                    if (currentResource.isFolder()) {
                        m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentResource);
                    } else {
                        // check labels
                        if (currentResource.isLabeled() && !labelResource(dbc, currentResource, null, 2)) {
                            // update the resource flags to "unlabel" the other siblings
                            int flags = currentResource.getFlags();
                            flags &= ~CmsResource.FLAG_LABELED;
                            currentResource.setFlags(flags);
                        }
                        m_vfsDriver.removeFile(dbc, dbc.currentProject().getUuid(), currentResource);
                    }

                    // ensure an exclusive lock is removed in the lock manager for a deleted new resource,
                    // otherwise it would "stick" in the lock manager, preventing other users from creating 
                    // a file with the same name (issue with tempfiles in editor)
                    m_lockManager.removeDeletedResource(dbc, currentResource.getRootPath());
                } else {
                    // the resource exists online => mark the resource as deleted
                    // structure record is removed during next publish
                    // if one (or more) siblings are not removed, the ACE can not be removed
                    removeAce = false;

                    // set resource state to deleted
                    currentResource.setState(CmsResource.STATE_DELETED);
                    m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), currentResource, UPDATE_STRUCTURE, false);

                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(
                        dbc,
                        dbc.currentProject(),
                        dbc.currentProject().getUuid(),
                        currentResource);
                }
            }
            deleteRelationsForResource(dbc, currentResource, CmsRelationFilter.TARGETS);
        }

        if ((resource.getSiblingCount() <= 1) || allSiblingsRemoved) {
            if (removeAce) {
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());
            } else {
                // mark access control entries as deleted
                m_userDriver.deleteAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());
            }
        }

        // flush all caches
        clearAccessControlListCache();
        OpenCms.getMemoryMonitor().flushProperties();
        OpenCms.getMemoryMonitor().flushPropertyLists();
        OpenCms.getMemoryMonitor().flushProjectResources();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_DELETED, Collections.singletonMap(
            "resources",
            resources)));
    }

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters ofthe resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsException {

        m_projectDriver.deleteStaticExportPublishedResource(dbc, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user, if given.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param username the name of the user to be deleted
     * @param replacementUsername the name of the user to be transfered, can be <code>null</code>
     * 
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsDbContext dbc, CmsProject project, String username, String replacementUsername)
    throws CmsException {

        // Test if the users exists
        CmsUser user = readUser(dbc, username);
        CmsUser replacementUser = null;
        if (replacementUsername != null) {
            replacementUser = readUser(dbc, replacementUsername);
        }

        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        boolean withACEs = true;
        if (replacementUser == null) {
            withACEs = false;
            replacementUser = readUser(dbc, OpenCms.getDefaultUsers().getUserDeletedResource());
        }

        boolean isVfsManager = m_securityManager.hasRole(dbc, replacementUser, CmsRole.VFS_MANAGER);

        for (boolean readRoles = false; !readRoles; readRoles = !readRoles) {
            Iterator itGroups = getGroupsOfUser(
                dbc,
                username,
                "",
                true,
                readRoles,
                true,
                dbc.getRequestContext().getRemoteAddress()).iterator();
            while (itGroups.hasNext()) {
                CmsGroup group = (CmsGroup)itGroups.next();
                if (!isVfsManager) {
                    // add replacement user to user groups
                    if (!userInGroup(dbc, replacementUser.getName(), group.getName(), false)) {
                        addUserToGroup(dbc, replacementUser.getName(), group.getName(), false);
                    }
                }
                // remove user from groups
                if (userInGroup(dbc, username, group.getName(), readRoles)) {
                    removeUserFromGroup(dbc, username, group.getName(), readRoles);
                }
            }
        }
        // remove all locks set for the deleted user
        m_lockManager.removeLocks(user.getId());
        // offline
        transferPrincipalResources(dbc, project, user.getId(), replacementUser.getId(), withACEs);
        // online
        transferPrincipalResources(dbc, onlineProject, user.getId(), replacementUser.getId(), withACEs);
        m_userDriver.removeAccessControlEntriesForPrincipal(dbc, project, onlineProject, user.getId());
        m_historyDriver.writePrincipal(dbc, user);
        m_userDriver.deleteUser(dbc, username);
        // delete user from cache
        clearUserCache(user);
    }

    /**
     * Destroys this driver manager and releases all allocated resources.<p>
     */
    public void destroy() {

        try {
            if (m_projectDriver != null) {
                try {
                    m_projectDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_PROJECT_DRIVER_0), t);
                }
                m_projectDriver = null;
            }
            if (m_userDriver != null) {
                try {
                    m_userDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_USER_DRIVER_0), t);
                }
                m_userDriver = null;
            }
            if (m_vfsDriver != null) {
                try {
                    m_vfsDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_VFS_DRIVER_0), t);
                }
                m_vfsDriver = null;
            }
            if (m_historyDriver != null) {
                try {
                    m_historyDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_HISTORY_DRIVER_0), t);
                }
                m_historyDriver = null;
            }

            if (m_connectionPools != null) {
                for (int i = 0; i < m_connectionPools.size(); i++) {
                    PoolingDriver driver = (PoolingDriver)m_connectionPools.get(i);
                    String[] pools = driver.getPoolNames();
                    for (int j = 0; j < pools.length; j++) {
                        try {
                            driver.closePool(pools[j]);
                            if (CmsLog.INIT.isDebugEnabled()) {
                                CmsLog.INIT.debug(Messages.get().getBundle().key(
                                    Messages.INIT_CLOSE_CONN_POOL_1,
                                    pools[j]));
                            }
                        } catch (Throwable t) {
                            LOG.error(Messages.get().getBundle().key(Messages.LOG_CLOSE_CONN_POOL_ERROR_1, pools[j]), t);
                        }
                    }
                }
                m_connectionPools = null;
            }

            clearcache(false);

            m_lockManager = null;
            m_htmlLinkValidator = null;
        } catch (Throwable t) {
            // ignore
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_DRIVER_MANAGER_DESTROY_1,
                getClass().getName()));
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

        return m_vfsDriver.validateResourceIdExists(dbc, dbc.currentProject().getUuid(), resourceId);
    }

    /**
     * Fills the given publish list with the the VFS resources that actually get published.<p>
     * 
     * Please refer to the source code of this method for the rules on how to decide whether a
     * new/changed/deleted <code>{@link CmsResource}</code> object can be published or not.<p>
     * 
     * @param dbc the current database context
     * @param publishList must be initialized with basic publish information (Project or direct publish operation),
     *                    the given publish list will be filled with all new/changed/deleted files from the current 
     *                    (offline) project that will be actually published 
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.db.CmsPublishList
     */
    public void fillPublishList(CmsDbContext dbc, CmsPublishList publishList) throws CmsException {

        if (!publishList.isDirectPublish()) {
            // when publishing a project
            // all modified resources with the last change done in the current project are candidates if unlocked
            List folderList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getUuid(),
                CmsDriverManager.READ_IGNORE_PARENT,
                CmsDriverManager.READ_IGNORE_TYPE,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_INCLUDE_TREE
                    | CmsDriverManager.READMODE_INCLUDE_PROJECT
                    | CmsDriverManager.READMODE_EXCLUDE_STATE
                    | CmsDriverManager.READMODE_ONLY_FOLDERS);

            publishList.addAll(filterResources(dbc, null, folderList));

            List fileList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getUuid(),
                CmsDriverManager.READ_IGNORE_PARENT,
                CmsDriverManager.READ_IGNORE_TYPE,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_INCLUDE_TREE
                    | CmsDriverManager.READMODE_INCLUDE_PROJECT
                    | CmsDriverManager.READMODE_EXCLUDE_STATE
                    | CmsDriverManager.READMODE_ONLY_FILES);

            publishList.addAll(filterResources(dbc, publishList, fileList));
        } else {
            // this is a direct publish
            Iterator it = publishList.getDirectPublishResources().iterator();
            while (it.hasNext()) {
                // iterate all resources in the direct publish list
                CmsResource directPublishResource = (CmsResource)it.next();
                if (directPublishResource.isFolder()) {
                    // when publishing a folder directly, 
                    // the folder and all modified resources within the tree below this folder 
                    // and with the last change done in the current project are candidates if lockable
                    CmsLock lock = getLock(dbc, directPublishResource);
                    if (!directPublishResource.getState().isUnchanged() && lock.isLockableBy(dbc.currentUser())) {

                        try {
                            m_securityManager.checkPermissions(
                                dbc,
                                directPublishResource,
                                CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                                false,
                                CmsResourceFilter.ALL);
                            publishList.add(directPublishResource);
                        } catch (CmsException e) {
                            // skip if not enough permissions
                        }
                    }

                    if (publishList.isPublishSubResources()) {
                        // add all sub resources of the folder
                        List folderList = m_vfsDriver.readResourceTree(
                            dbc,
                            dbc.currentProject().getUuid(),
                            directPublishResource.getRootPath(),
                            CmsDriverManager.READ_IGNORE_TYPE,
                            CmsResource.STATE_UNCHANGED,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READMODE_INCLUDE_TREE
                                | CmsDriverManager.READMODE_INCLUDE_PROJECT
                                | CmsDriverManager.READMODE_EXCLUDE_STATE
                                | CmsDriverManager.READMODE_ONLY_FOLDERS);

                        publishList.addAll(filterResources(dbc, publishList, folderList));

                        List fileList = m_vfsDriver.readResourceTree(
                            dbc,
                            dbc.currentProject().getUuid(),
                            directPublishResource.getRootPath(),
                            CmsDriverManager.READ_IGNORE_TYPE,
                            CmsResource.STATE_UNCHANGED,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READMODE_INCLUDE_TREE
                                | CmsDriverManager.READMODE_INCLUDE_PROJECT
                                | CmsDriverManager.READMODE_EXCLUDE_STATE
                                | CmsDriverManager.READMODE_ONLY_FILES);

                        publishList.addAll(filterResources(dbc, publishList, fileList));
                    }
                } else if (directPublishResource.isFile() && !directPublishResource.getState().isUnchanged()) {

                    // when publishing a file directly this file is the only candidate
                    // if it is modified and lockable
                    CmsLock lock = getLock(dbc, directPublishResource);
                    if (lock.isLockableBy(dbc.currentUser())) {
                        // check permissions
                        try {
                            m_securityManager.checkPermissions(
                                dbc,
                                directPublishResource,
                                CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                                false,
                                CmsResourceFilter.ALL);
                            publishList.add(directPublishResource);
                        } catch (CmsException e) {
                            // skip if not enough permissions
                        }
                    }
                }
            }
        }

        // Step 2: if desired, extend the list of files to publish with related siblings
        if (publishList.isPublishSiblings()) {
            List publishFiles = publishList.getFileList();
            int size = publishFiles.size();

            // Improved: first calculate closure of all siblings, then filter and add them
            Set siblingsClosure = new HashSet(publishFiles);
            for (int i = 0; i < size; i++) {
                CmsResource currentFile = (CmsResource)publishFiles.get(i);
                if (currentFile.getSiblingCount() > 1) {
                    siblingsClosure.addAll(readSiblings(dbc, currentFile, CmsResourceFilter.ALL_MODIFIED));
                }
            }
            publishList.addAll(filterSiblings(dbc, publishList, siblingsClosure));
        }
        publishList.initialize();
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(CmsDbContext dbc, CmsResource resource, boolean getInherited)
    throws CmsException {

        // get the ACE of the resource itself
        List ace = m_userDriver.readAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId(), false);

        // sort and check if we got the 'overwrite all' ace to stop looking up
        boolean overwriteAll = sortAceList(ace);

        // get the ACE of each parent folder
        // Note: for the immediate parent, get non-inherited access control entries too,
        // if the resource is not a folder
        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        int d = (resource.isFolder()) ? 1 : 0;

        while (!overwriteAll && getInherited && (parentPath != null)) {
            resource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getUuid(), parentPath);
            List entries = m_userDriver.readAccessControlEntries(
                dbc,
                dbc.currentProject(),
                resource.getResourceId(),
                d > 0);

            // sort and check if we got the 'overwrite all' ace to stop looking up
            overwriteAll = sortAceList(entries);

            for (Iterator i = entries.iterator(); i.hasNext();) {
                CmsAccessControlEntry e = (CmsAccessControlEntry)i.next();
                e.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
            }

            ace.addAll(entries);
            parentPath = CmsResource.getParentFolder(resource.getRootPath());
            d++;
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
    public CmsAccessControlList getAccessControlList(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return getAccessControlList(dbc, resource, false);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If <code>inheritedOnly</code> is set, only inherited access control entries 
     * are returned.<p>
     * 
     * Note: For file resources, *all* permissions set at the immediate parent folder are inherited,
     * not only these marked to inherit. 
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsDbContext dbc, CmsResource resource, boolean inheritedOnly)
    throws CmsException {

        return getAccessControlList(dbc, resource, inheritedOnly, resource.isFolder(), 0);
    }

    /** 
     * Returns the number of active connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of active connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getActiveConnections(String dbPoolUrl) throws CmsDbException {

        try {
            for (Iterator i = m_connectionPools.iterator(); i.hasNext();) {
                PoolingDriver d = (PoolingDriver)i.next();
                ObjectPool p = d.getConnectionPool(dbPoolUrl);
                return p.getNumActive();
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ACCESSING_POOL_1, dbPoolUrl);
            throw new CmsDbException(message, exc);
        }

        CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_POOL_URL_1, dbPoolUrl);
        throw new CmsDbException(message);
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible by the current user.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAllAccessibleProjects(CmsDbContext dbc) throws CmsException {

        Set projects = new HashSet();

        // get the ous where the user has the project manager role
        List ous = getOrgUnitsForRole(dbc, CmsRole.PROJECT_MANAGER.forOrgUnit(""), true);

        // get the groups of the user if needed
        Set userGroupIds = new HashSet();
        Iterator itGroups = getGroupsOfUser(dbc, dbc.currentUser().getName(), false).iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            userGroupIds.add(group.getId());
        }

        // get all projects
        projects.addAll(m_projectDriver.readProjects(dbc, ""));

        // filter hidden and not accessible projects
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsProject project = (CmsProject)itProjects.next();
            // if hidden
            if (project.isHidden()) {
                itProjects.remove();
                continue;
            }

            boolean accessible = false;
            // online project
            accessible = accessible || project.isOnlineProject();
            // if owner
            accessible = accessible || project.getOwnerId().equals(dbc.currentUser().getId());

            // project managers
            Iterator itOus = ous.iterator();
            while (!accessible && itOus.hasNext()) {
                CmsOrganizationalUnit ou = (CmsOrganizationalUnit)itOus.next();
                // for project managers check visibility
                accessible = accessible || project.getOuFqn().startsWith(ou.getName());
            }

            if (!accessible) {
                // if direct user or manager of project 
                CmsUUID groupId = null;
                if (userGroupIds.contains(project.getGroupId())) {
                    groupId = project.getGroupId();
                } else if (userGroupIds.contains(project.getManagerGroupId())) {
                    groupId = project.getManagerGroupId();
                }
                if (groupId != null) {
                    String oufqn = readGroup(dbc, groupId).getOuFqn();
                    accessible = accessible || (oufqn.startsWith(dbc.getRequestContext().getOuFqn()));
                }
            }
            if (!accessible) {
                // remove not accessible project
                itProjects.remove();
            }
        }

        ArrayList accessibleProjects = new ArrayList(projects);
        // sort the list of projects based on the project name
        Collections.sort(accessibleProjects);
        // ensure the online project is in first place
        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        if (accessibleProjects.contains(onlineProject)) {
            accessibleProjects.remove(onlineProject);
            accessibleProjects.add(0, onlineProject);
        }

        return accessibleProjects;
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @param dbc the current database context
     * 
     * @return list of <code>{@link CmsHistoryProject}</code> objects 
     *           with all projects from history.
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getAllHistoricalProjects(CmsDbContext dbc) throws CmsException {

        return m_historyDriver.readProjects(dbc);
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getAllManageableProjects(CmsDbContext dbc) throws CmsException {

        // the result set
        Set projects = new HashSet();

        if (m_securityManager.hasRole(dbc, dbc.currentUser(), CmsRole.PROJECT_MANAGER.forOrgUnit(""))) {
            // user is allowed to access all existing projects for the ous he has the project_manager role
            Iterator itOus = getOrgUnitsForRole(dbc, CmsRole.PROJECT_MANAGER, true).iterator();
            while (itOus.hasNext()) {
                CmsOrganizationalUnit ou = (CmsOrganizationalUnit)itOus.next();
                projects.addAll(m_projectDriver.readProjects(dbc, ou.getName()));
            }
        }
        // add all projects which are owned by the user
        projects.addAll(m_projectDriver.readProjectsForUser(dbc, dbc.currentUser()));

        // get all groups of the user
        List groups = getGroupsOfUser(dbc, dbc.currentUser().getName(), false);

        // add all projects, that the user can access with his groups
        for (int i = 0, n = groups.size(); i < n; i++) {
            projects.addAll(m_projectDriver.readProjectsForManagerGroup(dbc, (CmsGroup)groups.get(i)));
        }

        // remove the online-project, it is not manageable!
        projects.remove(readProject(dbc, CmsProject.ONLINE_PROJECT_ID));

        // filter hidden projects
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsProject project = (CmsProject)itProjects.next();
            if (project.isHidden()) {
                // remove hidden projects
                itProjects.remove();
                continue;
            }
        }

        // return the list of projects
        ArrayList manageableProjects = new ArrayList(projects);
        Collections.sort(manageableProjects);
        return manageableProjects;
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * @param dbc the current database context
     * @param group the group to get the child for
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getChild(CmsDbContext dbc, CmsGroup group) throws CmsException {

        return m_userDriver.readChildGroups(dbc, group.getName());
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * This method also returns all sub-child groups of the current group.
     *
     * @param dbc the current database context
     * @param group the group to get the children for
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getChilds(CmsDbContext dbc, CmsGroup group) throws CmsException {

        Set allChilds = new HashSet();
        // iterate all child groups
        Iterator it = m_userDriver.readChildGroups(dbc, group.getName()).iterator();
        while (it.hasNext()) {
            CmsGroup child = (CmsGroup)it.next();
            // add the group itself
            allChilds.add(child);
            // now get all subchilds for each group
            allChilds.addAll(getChilds(dbc, child));
        }
        return new ArrayList(allChilds);
    }

    /**
     * Returns all groups of the given organizational unit.<p>
     *
     * @param dbc the current db context
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
     */
    public List getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles)
    throws CmsException {

        return m_userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles);
    }

    /**
     * Returns the groups of an user filtered by the specified IP address.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user
     * @param readRoles if to read roles or groups
     * 
     * @return the groups of the given user, as a list of {@link CmsGroup} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getGroupsOfUser(CmsDbContext dbc, String username, boolean readRoles) throws CmsException {

        return getGroupsOfUser(dbc, username, "", true, readRoles, false, dbc.getRequestContext().getRemoteAddress());
    }

    /**
     * Returns the groups of an user filtered by the specified IP address.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user
     * @param ouFqn the fully qualified name of the organizational unit to restrict the result set for
     * @param includeChildOus include groups of child organizational units
     * @param readRoles if to read roles or groups
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect groups
     * @param remoteAddress the IP address to filter the groups in the result list 
     *
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(
        CmsDbContext dbc,
        String username,
        String ouFqn,
        boolean includeChildOus,
        boolean readRoles,
        boolean directGroupsOnly,
        String remoteAddress) throws CmsException {

        CmsUser user = readUser(dbc, username);
        String prefix = ouFqn + "_" + includeChildOus + "_" + directGroupsOnly + "_" + readRoles + "_" + remoteAddress;
        String cacheKey = m_keyGenerator.getCacheKeyForUserGroups(prefix, dbc, user);
        List groups = OpenCms.getMemoryMonitor().getCachedUserGroups(cacheKey);
        if (groups == null) {
            // get all groups of the user
            List directGroups = m_userDriver.readGroupsOfUser(
                dbc,
                user.getId(),
                readRoles ? "" : ouFqn,
                readRoles ? true : includeChildOus,
                remoteAddress,
                readRoles);
            Set allGroups = new HashSet();
            if (!readRoles) {
                allGroups.addAll(directGroups);
            }
            if (!directGroupsOnly) {
                if (!readRoles) {
                    // now get all parents of the groups
                    for (int i = 0; i < directGroups.size(); i++) {
                        CmsGroup parent = getParent(dbc, ((CmsGroup)directGroups.get(i)).getName());
                        while ((parent != null) && (!allGroups.contains(parent))) {
                            if (parent.getOuFqn().startsWith(ouFqn)) {
                                allGroups.add(parent);
                            }
                            // read next parent group
                            parent = getParent(dbc, parent.getName());
                        }
                    }
                }
            }
            if (readRoles) {
                // for each for role 
                for (int i = 0; i < directGroups.size(); i++) {
                    CmsGroup group = (CmsGroup)directGroups.get(i);
                    CmsRole role = CmsRole.valueOf(group);
                    if (!includeChildOus && role.getOuFqn().equals(ouFqn)) {
                        allGroups.add(group);
                    }
                    if (includeChildOus && role.getOuFqn().startsWith(ouFqn)) {
                        allGroups.add(group);
                    }
                    if (directGroupsOnly) {
                        continue;
                    }
                    // get the child roles
                    Iterator itChildRoles = role.getChilds(true).iterator();
                    while (itChildRoles.hasNext()) {
                        CmsRole childRole = (CmsRole)itChildRoles.next();
                        allGroups.add(readGroup(dbc, childRole.getGroupName()));
                    }
                    if (includeChildOus) {
                        // if needed include the roles of child ous 
                        Iterator itSubOus = getOrganizationalUnits(
                            dbc,
                            readOrganizationalUnit(dbc, group.getOuFqn()),
                            true).iterator();
                        while (itSubOus.hasNext()) {
                            CmsOrganizationalUnit subOu = (CmsOrganizationalUnit)itSubOus.next();
                            // add role in child ou
                            try {
                                allGroups.add(readGroup(dbc, role.forOrgUnit(subOu.getName()).getGroupName()));
                            } catch (CmsDbEntryNotFoundException e) {
                                // ignore, this may happen while deleting an orgunit
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(e.getLocalizedMessage(), e);
                                }
                            }
                            // add child roles in child ous
                            itChildRoles = role.getChilds(true).iterator();
                            while (itChildRoles.hasNext()) {
                                CmsRole childRole = (CmsRole)itChildRoles.next();
                                try {
                                    allGroups.add(readGroup(dbc, childRole.forOrgUnit(subOu.getName()).getGroupName()));
                                } catch (CmsDbEntryNotFoundException e) {
                                    // ignore, this may happen while deleting an orgunit
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(e.getLocalizedMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // make group list unmodifiable for caching
            groups = Collections.unmodifiableList(new ArrayList(allGroups));
            OpenCms.getMemoryMonitor().cacheUserGroups(cacheKey, groups);
        }

        return groups;
    }

    /**
     * Returns the history driver.<p>
     * 
     * @return the history driver
     */
    public I_CmsHistoryDriver getHistoryDriver() {

        return m_historyDriver;
    }

    /** 
     * Returns the number of idle connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of idle connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getIdleConnections(String dbPoolUrl) throws CmsDbException {

        try {
            for (Iterator i = m_connectionPools.iterator(); i.hasNext();) {
                PoolingDriver d = (PoolingDriver)i.next();
                ObjectPool p = d.getConnectionPool(dbPoolUrl);
                return p.getNumIdle();
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ACCESSING_POOL_1, dbPoolUrl);
            throw new CmsDbException(message, exc);
        }

        CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_POOL_URL_1, dbPoolUrl);
        throw new CmsDbException(message);
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
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return m_lockManager.getLock(dbc, resource);
    }

    /**
     * Returns all locked resources in a given folder.<p>
     *
     * @param dbc the current database context
     * @param resource the folder to search in
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     * 
     * @throws CmsException if the current project is locked
     */
    public List getLockedResources(CmsDbContext dbc, CmsResource resource, CmsLockFilter filter) throws CmsException {

        List lockedResources = new ArrayList();
        // get locked resources
        Iterator it = m_lockManager.getLocks(dbc, resource.getRootPath(), filter).iterator();
        while (it.hasNext()) {
            CmsLock lock = (CmsLock)it.next();
            lockedResources.add(dbc.removeSiteRoot(lock.getResourceName()));
        }
        Collections.sort(lockedResources);
        return lockedResources;
    }

    /**
     * Returns the next publish tag for the published historical resources.<p>
     *
     * @param dbc the current database context
     * 
     * @return the next available publish tag
     */
    public int getNextPublishTag(CmsDbContext dbc) {

        return m_historyDriver.readNextPublishTag(dbc);
    }

    /**
     * Returns all child organizational units of the given parent organizational unit including 
     * hierarchical deeper organization units if needed.<p>
     *
     * @param dbc the current db context
     * @param parent the parent organizational unit, or <code>null</code> for the root
     * @param includeChilds if hierarchical deeper organization units should also be returned
     * 
     * @return a list of <code>{@link CmsOrganizationalUnit}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getOrganizationalUnits(CmsObject, String, boolean)
     */
    public List getOrganizationalUnits(CmsDbContext dbc, CmsOrganizationalUnit parent, boolean includeChilds)
    throws CmsException {

        if (parent == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_PARENT_ORGUNIT_NULL_0));
        }
        return m_userDriver.getOrganizationalUnits(dbc, parent, includeChilds);
    }

    /**
     * Returns all deepest organizational units that contains the given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to look for
     * 
     * @return a list of organizational units that contains the given resource
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    public List getOrganizationalUnitsForResource(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        CmsFolder folder;
        // get the folder since ou can only be associated to folders
        if (resource.isFile()) {
            folder = getVfsDriver().readParentFolder(dbc, dbc.currentProject().getUuid(), resource.getStructureId());
        } else {
            folder = new CmsFolder(resource);
        }
        List orgUnits = new ArrayList();
        Iterator itOuFqns = m_userDriver.getOrganizationalUnitsForFolder(dbc, folder).iterator();
        while (itOuFqns.hasNext()) {
            String ouFqn = (String)itOuFqns.next();
            orgUnits.add(ouFqn);
        }
        return orgUnits;
    }

    /**
     * Returns all the organizational units for which the current user has the given role.<p>
     * 
     * @param dbc the current database context
     * @param role the role to check
     * @param includeSubOus if sub organizational units should be included in the search 
     *  
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getOrgUnitsForRole(CmsDbContext dbc, CmsRole role, boolean includeSubOus) throws CmsException {

        String ouFqn = role.getOuFqn();
        if (ouFqn == null) {
            ouFqn = "";
            role = role.forOrgUnit("");
        }
        CmsOrganizationalUnit ou = readOrganizationalUnit(dbc, ouFqn);
        List orgUnits = new ArrayList();
        if (m_securityManager.hasRole(dbc, dbc.currentUser(), role)) {
            orgUnits.add(ou);
        }
        if (includeSubOus) {
            Iterator it = getOrganizationalUnits(dbc, ou, true).iterator();
            while (it.hasNext()) {
                CmsOrganizationalUnit orgUnit = (CmsOrganizationalUnit)it.next();
                if (m_securityManager.hasRole(dbc, dbc.currentUser(), role.forOrgUnit(orgUnit.getName()))) {
                    orgUnits.add(orgUnit);
                }
            }
        }
        return orgUnits;
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group
     * 
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(CmsDbContext dbc, String groupname) throws CmsException {

        CmsGroup group = readGroup(dbc, groupname);
        if (group.getParentId().isNullUUID()) {
            return null;
        }

        // try to read from cache
        CmsGroup parent = OpenCms.getMemoryMonitor().getCachedGroup(group.getParentId().toString());
        if (parent == null) {
            parent = m_userDriver.readGroup(dbc, group.getParentId());
            OpenCms.getMemoryMonitor().cacheGroup(parent);
        }
        return parent;
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param user the user
     * 
     * @return bitset with allowed permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSetCustom getPermissions(CmsDbContext dbc, CmsResource resource, CmsUser user)
    throws CmsException {

        CmsAccessControlList acList = getAccessControlList(dbc, resource, false);
        return acList.getPermissions(user, getGroupsOfUser(dbc, user.getName(), false));
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
     * Returns the uuid id for the given id, remove this method
     * as soon as possible.<p>
     * 
     * @param dbc the current database context
     * @param id the old project id
     * 
     * @return the new uuid for the given id
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsUUID getProjectId(CmsDbContext dbc, int id) throws CmsException {

        Iterator itProjects = getAllAccessibleProjects(dbc).iterator();
        while (itProjects.hasNext()) {
            CmsProject project = (CmsProject)itProjects.next();
            if (project.getUuid().hashCode() == id) {
                return project.getUuid();
            }
        }
        return null;
    }

    /**
     * Returns the configuration read from the <code>opencms.properties</code> file.<p>
     *
     * @return the configuration read from the <code>opencms.properties</code> file
     */
    public ExtendedProperties getPropertyConfiguration() {

        return m_propertyConfiguration;
    }

    /**
     * Returns a new publish list that contains the unpublished resources related 
     * to all resources in the given publish list, the related resources exclude 
     * all resources in the given publish list and also locked (by other users) resources.<p>
     * 
     * @param dbc the current database context
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
        CmsDbContext dbc,
        CmsPublishList publishList,
        CmsRelationFilter filter) throws CmsException {

        Map relations = new HashMap();

        // get all resources to publish
        List publishResources = new ArrayList(publishList.getDeletedFolderList());
        publishResources.addAll(publishList.getFileList());
        publishResources.addAll(publishList.getFolderList());

        Iterator itCheckList = publishResources.iterator();
        // iterate over them
        while (itCheckList.hasNext()) {
            CmsResource checkResource = (CmsResource)itCheckList.next();
            // get and iterate over all related resources
            Iterator itRelations = getRelationsForResource(dbc, checkResource, filter).iterator();
            while (itRelations.hasNext()) {
                CmsRelation relation = (CmsRelation)itRelations.next();
                try {
                    // get the target of the relation, see CmsRelation#getTarget(CmsObject, CmsResourceFilter)
                    CmsResource target;
                    try {
                        // first look up by id
                        target = readResource(dbc, relation.getTargetId(), CmsResourceFilter.ALL);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // then look up by name, but from the root site
                        String storedSiteRoot = dbc.getRequestContext().getSiteRoot();
                        try {
                            dbc.getRequestContext().setSiteRoot("");
                            target = readResource(dbc, relation.getTargetPath(), CmsResourceFilter.ALL);
                        } finally {
                            dbc.getRequestContext().setSiteRoot(storedSiteRoot);
                        }
                    }
                    // just add resources that may come in question
                    if (!publishResources.contains(target)
                        && !relations.containsKey(target.getRootPath())
                        && !target.getState().isUnchanged()) {
                        relations.put(target.getRootPath(), target);
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // ignore broken links
                }
            }
        }

        CmsPublishList ret = new CmsPublishList(new ArrayList(relations.values()), false, false);
        fillPublishList(dbc, ret); // ensure consistency of locks/permissions
        return ret;
    }

    /**
     * Returns all relations for the given resource mathing the given filter.<p> 
     * 
     * @param dbc the current db context
     * @param resource the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return all {@link CmsRelation} objects for the given resource mathing the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List getRelationsForResource(CmsDbContext dbc, CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        return m_vfsDriver.readRelations(dbc, dbc.getProjectId().isNullUUID() ? dbc.currentProject().getUuid()
        : dbc.getProjectId(), resource, filter);
    }

    /**
     * Returns all resources of the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all resources for
     * 
     * @return all <code>{@link CmsResource}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     * @see org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean)
     */
    public List getResourcesForOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit) throws CmsException {

        return m_userDriver.getResourcesForOrganizationalUnit(dbc, orgUnit);
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
     * @param dbc the current database context
     * @param project the to read the entries from
     * @param principalId the id of the principal
     * @param permissions a set of permissions to match, can be <code>null</code> for all ACEs
     * @param includeAttr a flag to include resources associated by attributes
     * 
     * @return a list of <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getResourcesForPrincipal(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID principalId,
        CmsPermissionSet permissions,
        boolean includeAttr) throws CmsException {

        List resources = m_vfsDriver.readResourcesForPrincipalACE(dbc, project, principalId);
        if (permissions != null) {
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsAccessControlEntry ace = readAccessControlEntry(dbc, (CmsResource)itRes.next(), principalId);
                if ((ace.getPermissions().getPermissions() & permissions.getPermissions()) != permissions.getPermissions()) {
                    // remove if permissions does not match
                    itRes.remove();
                }
            }
        }
        if (includeAttr) {
            resources.addAll(m_vfsDriver.readResourcesForPrincipalAttr(dbc, project, principalId));
        }
        // remove duplicated
        Set resNames = new HashSet();
        Iterator itRes = resources.iterator();
        while (itRes.hasNext()) {
            String resName = ((CmsResource)itRes.next()).getRootPath();
            if (resNames.contains(resName)) {
                itRes.remove();
            } else {
                resNames.add(resName);
            }
        }
        return resources;
    }

    /**
     * Returns the security manager this driver manager belongs to.<p>
     * 
     * @return the security manager this driver manager belongs to
     */
    public CmsSecurityManager getSecurityManager() {

        return m_securityManager;
    }

    /**
     * Returns an instance of the common sql manager.<p>
     * 
     * @return an instance of the common sql manager
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
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
     * Returns all direct users of the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all users for
     * @param recursive if all groups of sub-organizational units should be retrieved too
     * 
     * @return all <code>{@link CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#getResourcesForOrganizationalUnit(CmsObject, String)
     * @see org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean)
     */
    public List getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive) throws CmsException {

        return m_userDriver.getUsers(dbc, orgUnit, recursive);
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group to list users from
     * @param includeOtherOuUsers include users of other organizational units
     * @param directUsersOnly if set only the direct assigned users will be returned, 
     *                        if not also indirect users, ie. members of parent roles, 
     *                        this parameter only works with roles
     * @param readRoles if to read roles or groups
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getUsersOfGroup(
        CmsDbContext dbc,
        String groupname,
        boolean includeOtherOuUsers,
        boolean directUsersOnly,
        boolean readRoles) throws CmsException {

        return internalUsersOfGroup(
            dbc,
            CmsOrganizationalUnit.getParentFqn(groupname),
            groupname,
            includeOtherOuUsers,
            directUsersOnly,
            readRoles);
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
     * Writes a vector of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.
     * Access is granted, if:<p>
     * <ul>
     * <li>the current user has control permission on the resource</li>
     * </ul>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsDbContext dbc, CmsResource resource, List acEntries) throws CmsException {

        m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), (CmsAccessControlEntry)i.next());
        }
        clearAccessControlListCache();
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param dateCreated the creation date
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser importUser(
        CmsDbContext dbc,
        String id,
        String name,
        String password,
        String firstname,
        String lastname,
        String email,
        int flags,
        long dateCreated,
        Map additionalInfos) throws CmsException {

        // no space before or after the name
        name = name.trim();
        // check the username
        OpenCms.getValidationHandler().checkUserName(CmsOrganizationalUnit.getSimpleName(name));

        CmsUser newUser = m_userDriver.createUser(
            dbc,
            new CmsUUID(id),
            name,
            password,
            firstname,
            lastname,
            email,
            0,
            flags,
            dateCreated,
            additionalInfos);
        return newUser;
    }

    /**
     * Initializes the driver and sets up all required modules and connections.<p>
     * 
     * @param configurationManager the configuration manager
     * 
     * @throws CmsException if something goes wrong
     * @throws Exception if something goes wrong
     */
    public void init(CmsConfigurationManager configurationManager) throws CmsException, Exception {

        // initialize the access-module.
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE4_0));
        }

        CmsSystemConfiguration systemConfiguation = (CmsSystemConfiguration)configurationManager.getConfiguration(CmsSystemConfiguration.class);
        CmsCacheSettings settings = systemConfiguation.getCacheSettings();

        // initialize the key generator
        m_keyGenerator = (I_CmsCacheKey)Class.forName(settings.getCacheKeyGenerator()).newInstance();

        // initialize the HTML link validator
        m_htmlLinkValidator = new CmsRelationSystemValidator(this);

        // fills the defaults if needed
        getUserDriver().fillDefaults(new CmsDbContext());
        getProjectDriver().fillDefaults(new CmsDbContext());
        // set the driver manager in the publish engine
        m_publishEngine.setDriverManager(this);
        // create the root organizational unit if needed
        CmsDbContext dbc = new CmsDbContext(new CmsRequestContext(
            readUser(new CmsDbContext(), OpenCms.getDefaultUsers().getUserAdmin()),
            readProject(new CmsDbContext(), CmsProject.ONLINE_PROJECT_ID),
            null,
            "",
            null,
            null,
            null,
            0,
            null,
            null,
            ""));
        getUserDriver().createRootOrganizationalUnit(dbc);
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the specified resource name (full path)
     * 
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(CmsDbContext dbc, String resourcename) {

        List projectResources = null;
        try {
            projectResources = readProjectResources(dbc, dbc.currentProject());
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CHECK_RESOURCE_INSIDE_CURRENT_PROJECT_2,
                    resourcename,
                    dbc.currentProject().getName()), e);
            }
            return false;
        }
        return CmsProject.isInsideProject(projectResources, resourcename);
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
     * @throws CmsDataAccessException if something goes wrong
     */
    public boolean labelResource(CmsDbContext dbc, CmsResource resource, String newResource, int action)
    throws CmsDataAccessException {

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
                    if (linkInside && sourceInside) {
                        break;
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
            List siblings = m_vfsDriver.readSiblings(dbc, dbc.currentProject().getUuid(), resource, false);
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
     * @return the user, who had locked the resource
     *
     * @throws CmsException will be thrown, if the user has not the rights for this resource
     */
    public CmsUser lockedBy(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return readUser(dbc, m_lockManager.getLock(dbc, resource).getEditionLock().getUserId());
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
     * @param dbc the current database context
     * @param resource the resource to lock
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     * @see CmsObject#lockResourceTemporary(String)
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(CmsObject, CmsSecurityManager, CmsResource, CmsLockType)
     */
    public void lockResource(CmsDbContext dbc, CmsResource resource, CmsLockType type) throws CmsException {

        // update the resource cache
        clearResourceCache();

        CmsProject project = dbc.currentProject();

        // add the resource to the lock dispatcher
        m_lockManager.addResource(dbc, resource, dbc.currentUser(), project, type);

        if (!resource.getState().isUnchanged() && !resource.getState().isKeep()) {
            // update the project flag of a modified resource as "last modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(dbc, project, project.getUuid(), resource);
        }

        // we must also clear the permission cache
        OpenCms.getMemoryMonitor().flushPermissions();

        // fire resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(NOTHING_CHANGED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * @param dbc the current database context
     * @param userName the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * 
     * @return the logged in user
     *
     * @throws CmsAuthentificationException if the login was not successful
     * @throws CmsDataAccessException in case of errors accessing the database
     * @throws CmsPasswordEncryptionException in case of errors encrypting the users password
     */
    public CmsUser loginUser(CmsDbContext dbc, String userName, String password, String remoteAddress)
    throws CmsAuthentificationException, CmsDataAccessException, CmsPasswordEncryptionException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(password)) {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_USER_1, userName));
        }
        CmsUser newUser;
        try {
            // read the user from the driver to avoid the cache
            newUser = m_userDriver.readUser(dbc, userName, password, remoteAddress);
        } catch (CmsDbEntryNotFoundException e) {
            // this indicates that the username / password combination does not exist
            // any other exception indicates database issues, these are not catched here

            // check if a user with this name exists at all 
            boolean userExists = true;
            try {
                readUser(dbc, userName);
            } catch (CmsDataAccessException e2) {
                // apparently this user does not exist in the database
                userExists = false;
            }

            if (userExists) {
                if (dbc.currentUser().isGuestUser()) {
                    // add an invalid login attempt for this user to the storage
                    OpenCms.getLoginManager().addInvalidLogin(userName, remoteAddress);
                }
                throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                    org.opencms.security.Messages.ERR_LOGIN_FAILED_2,
                    userName,
                    remoteAddress), e);
            } else {
                String userOu = CmsOrganizationalUnit.getParentFqn(userName);
                if (userOu != null) {
                    String parentOu = CmsOrganizationalUnit.getParentFqn(userOu);
                    if (parentOu != null) {
                        // try a higher level ou
                        String uName = CmsOrganizationalUnit.getSimpleName(userName);
                        return loginUser(dbc, parentOu + uName, password, remoteAddress);
                    }
                }
                throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                    org.opencms.security.Messages.ERR_LOGIN_FAILED_NO_USER_2,
                    userName,
                    remoteAddress), e);
            }
        }
        // check if the "enabled" flag is set for the user
        if (!newUser.isEnabled()) {
            // user is disabled, throw a securiy exception
            throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2,
                userName,
                remoteAddress));
        }

        if (dbc.currentUser().isGuestUser()) {
            // check if this account is temporarily disabled because of too many invalid login attempts
            // this will throw an exception if the test fails
            OpenCms.getLoginManager().checkInvalidLogins(userName, remoteAddress);
            // test successful, remove all previous invalid login attempts for this user from the storage
            OpenCms.getLoginManager().removeInvalidLogins(userName, remoteAddress);
        }

        if (!m_securityManager.hasRole(
            dbc,
            newUser,
            CmsRole.ADMINISTRATOR.forOrgUnit(dbc.getRequestContext().getOuFqn()))) {
            // new user is not Administrator, check if login is currently allowed
            OpenCms.getLoginManager().checkLoginAllowed();
        }

        // set the last login time to the current time
        newUser.setLastlogin(System.currentTimeMillis());

        // write the changed user object back to the user driver
        m_userDriver.writeUser(dbc, newUser);

        // update cache
        OpenCms.getMemoryMonitor().cacheUser(newUser);

        // invalidate all user dependent caches
        OpenCms.getMemoryMonitor().flushACLs();
        OpenCms.getMemoryMonitor().flushGroups();
        OpenCms.getMemoryMonitor().flushOrgUnits();
        OpenCms.getMemoryMonitor().flushUserGroups();
        OpenCms.getMemoryMonitor().flushPermissions();
        OpenCms.getMemoryMonitor().flushResourceLists();

        // return the user object read from the driver
        return newUser;
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param dbc the current database context
     * @param principalId the UUID of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>
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
     * @param dbc the current database context
     * @param principalName the name of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>
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
            CmsUser user = readUser(dbc, principalName);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Returns a new publish list that contains all resources of both given publish lists.<p>
     * 
     * @param dbc the current database context
     * @param pubList1 the first publish list
     * @param pubList2 the second publish list
     * 
     * @return a new publish list that contains all resources of both given publish lists
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.publish.CmsPublishManager#mergePublishLists(CmsObject, CmsPublishList, CmsPublishList)
     */
    public CmsPublishList mergePublishLists(CmsDbContext dbc, CmsPublishList pubList1, CmsPublishList pubList2)
    throws CmsException {

        // get all resources from the first list
        Set publishResources = new HashSet(pubList1.getDeletedFolderList());
        publishResources.addAll(pubList1.getFileList());
        publishResources.addAll(pubList1.getFolderList());

        // get all resources from the second list
        publishResources.addAll(pubList2.getDeletedFolderList());
        publishResources.addAll(pubList2.getFileList());
        publishResources.addAll(pubList2.getFolderList());

        // create merged publish list
        CmsPublishList ret = new CmsPublishList(readResource(dbc, "/", CmsResourceFilter.ALL), false);
        ret.addAll(new ArrayList(publishResources));
        ret.initialize(); // ensure consistency
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
     * @param dbc the current database context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param internal if set nothing more than the path is modified
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#moveResource(CmsRequestContext, CmsResource, String)
     */
    public void moveResource(CmsDbContext dbc, CmsResource source, String destination, boolean internal)
    throws CmsException {

        CmsFolder destinationFolder = readFolder(
            dbc,
            CmsResource.getParentFolder(destination),
            CmsResourceFilter.IGNORE_EXPIRATION);
        m_securityManager.checkPermissions(
            dbc,
            destinationFolder,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            CmsResourceFilter.IGNORE_EXPIRATION);

        m_vfsDriver.moveResource(dbc, dbc.getRequestContext().currentProject().getUuid(), source, destination);
        // move lock 
        m_lockManager.moveResource(source.getRootPath(), destination);

        if (!internal) {
            source.setState(source.getState().isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            // safe since this operation always uses the ids instead of the resource path
            m_vfsDriver.writeResourceState(
                dbc,
                dbc.currentProject(),
                source,
                CmsDriverManager.UPDATE_STRUCTURE_STATE,
                false);
        }

        // flush all relevant caches
        clearAccessControlListCache();
        OpenCms.getMemoryMonitor().flushProperties();
        OpenCms.getMemoryMonitor().flushPropertyLists();
        OpenCms.getMemoryMonitor().flushProjectResources();

        List resources = new ArrayList(4);
        // source
        resources.add(source);
        try {
            resources.add(readFolder(dbc, CmsResource.getParentFolder(source.getRootPath()), CmsResourceFilter.ALL));
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
        // destination
        CmsResource destRes = readResource(dbc, destination, CmsResourceFilter.ALL);
        resources.add(destRes);
        resources.add(destinationFolder);

        // fire the events
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MOVED, Collections.singletonMap(
            "resources",
            resources)));
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
     * @param resource the resource to apply this operation to
     * @param returnNameOnly if <code>true</code>, only the name of the resource in the "lost and found" 
     *        folder is returned, the move operation is not really performed
     * 
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     * 
     * @see CmsObject#moveToLostAndFound(String)
     * @see CmsObject#getLostAndFoundName(String)
     */
    public String moveToLostAndFound(CmsDbContext dbc, CmsResource resource, boolean returnNameOnly)
    throws CmsException, CmsIllegalArgumentException {

        String resourcename = dbc.removeSiteRoot(resource.getRootPath());

        String siteRoot = dbc.getRequestContext().getSiteRoot();
        dbc.getRequestContext().setSiteRoot("");
        String destination = CmsDriverManager.LOST_AND_FOUND_FOLDER + resourcename;
        // create the required folders if necessary
        try {
            // collect all folders...
            String folderPath = CmsResource.getParentFolder(destination);
            folderPath = folderPath.substring(1, folderPath.length() - 1); // cut out leading and trailing '/'
            Iterator folders = CmsStringUtil.splitAsList(folderPath, '/').iterator();
            // ...now create them....
            folderPath = "/";
            while (folders.hasNext()) {
                folderPath += folders.next().toString() + "/";
                try {
                    readFolder(dbc, folderPath, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (Exception e1) {
                    if (returnNameOnly) {
                        // we can use the original name without risk, and we do not need to recreate the parent folders 
                        break;
                    }
                    // the folder is not existing, so create it
                    createResource(
                        dbc,
                        folderPath,
                        CmsResourceTypeFolder.RESOURCE_TYPE_ID,
                        null,
                        Collections.EMPTY_LIST);
                }
            }
            // check if this resource name does already exist
            // if so add a postfix to the name
            String des = destination;
            int postfix = 1;
            boolean found = true;
            while (found) {
                try {
                    // try to read the file.....
                    found = true;
                    readResource(dbc, des, CmsResourceFilter.ALL);
                    // ....it's there, so add a postfix and try again
                    String path = destination.substring(0, destination.lastIndexOf('/') + 1);
                    String filename = destination.substring(destination.lastIndexOf('/') + 1, destination.length());

                    des = path;

                    if (filename.lastIndexOf('.') > 0) {
                        des += filename.substring(0, filename.lastIndexOf('.'));
                    } else {
                        des += filename;
                    }
                    des += "_" + postfix;
                    if (filename.lastIndexOf('.') > 0) {
                        des += filename.substring(filename.lastIndexOf('.'), filename.length());
                    }
                    postfix++;
                } catch (CmsException e3) {
                    // the file does not exist, so we can use this filename                               
                    found = false;
                }
            }
            destination = des;

            if (!returnNameOnly) {
                // do not use the move semantic here! to prevent links pointing to the lost & found folder
                copyResource(dbc, resource, destination, CmsResource.COPY_AS_SIBLING);
                deleteResource(dbc, resource, CmsResource.DELETE_PRESERVE_SIBLINGS);
            }
        } catch (CmsException e2) {
            throw e2;
        } finally {
            // set the site root to the old value again
            dbc.getRequestContext().setSiteRoot(siteRoot);
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
     * @throws CmsInitException if the selected driver could not be initialized
     */
    public Object newDriverInstance(
        CmsConfigurationManager configurationManager,
        String driverName,
        List successiveDrivers) throws CmsInitException {

        Class driverClass = null;
        I_CmsDriver driver = null;
        CmsDbContext dbc = new CmsDbContext();

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_START_1, driverName));
            }

            // try to create a instance
            driver = (I_CmsDriver)driverClass.newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INITIALIZING_1, driverName));
            }

            // invoke the init-method of this access class
            driver.init(dbc, configurationManager, successiveDrivers, this);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INIT_FINISHED_0));
            }

        } catch (Throwable t) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ERROR_INITIALIZING_DRIVER_1, driverName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), t);
            }
            throw new CmsInitException(message, t);
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

        Class[] initParamClasses = {ExtendedProperties.class, String.class, CmsDriverManager.class};
        Object[] initParams = {configuration, driverPoolUrl, this};

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_START_1, driverName));
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INITIALIZING_1, driverName));
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INIT_FINISHED_1, driverPoolUrl));
            }

        } catch (Exception exc) {

            CmsMessageContainer message = Messages.get().container(Messages.ERR_INIT_DRIVER_MANAGER_1);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsDbException(message, exc);

        }

        return driver;
    }

    /**
     * Method to create a new instance of a pool.<p>
     * 
     * @param configuration the configurations from the propertyfile
     * @param poolName the configuration name of the pool
     * 
     * @throws CmsInitException if the pools could not be initialized
     */
    public void newPoolInstance(Map configuration, String poolName) throws CmsInitException {

        PoolingDriver driver;

        try {
            driver = CmsDbPool.createDriverManagerConnectionPool(configuration, poolName);
        } catch (Exception e) {

            CmsMessageContainer message = Messages.get().container(Messages.ERR_INIT_CONN_POOL_1, poolName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), e);
            }
            throw new CmsInitException(message, e);
        }

        m_connectionPools.add(driver);
    }

    /**
     * Publishes the given publish job.<p>
     * 
     * @param cms the cms context
     * @param dbc the db context
     * @param publishList the list of resources to publish
     * @param report the report to write to
     * 
     * @throws CmsException if something goes wrong
     */
    public void publishJob(CmsObject cms, CmsDbContext dbc, CmsPublishList publishList, I_CmsReport report)
    throws CmsException {

        try {
            // check the parent folders
            checkParentFolders(dbc, publishList);

            CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);

            // clear the cache
            clearcache(false);

            int publishTag = getNextPublishTag(dbc);
            getProjectDriver().publishProject(dbc, report, onlineProject, publishList, publishTag);

            // iterate the initialized module action instances
            Iterator i = OpenCms.getModuleManager().getModuleNames().iterator();
            while (i.hasNext()) {
                CmsModule module = OpenCms.getModuleManager().getModule(i.next().toString());
                if ((module != null) && (module.getActionInstance() != null)) {
                    module.getActionInstance().publishProject(cms, publishList, publishTag, report);
                }
            }

            boolean temporaryProject = (cms.getRequestContext().currentProject().getType() == CmsProject.PROJECT_TYPE_TEMPORARY);
            // the project was stored in the history tables for history
            // it will be deleted if the project_flag is PROJECT_TYPE_TEMPORARY
            if ((temporaryProject) && (!publishList.isDirectPublish())) {
                try {
                    getProjectDriver().deleteProject(dbc, dbc.currentProject());
                } catch (CmsException e) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_DELETE_TEMP_PROJECT_FAILED_1,
                        cms.getRequestContext().currentProject().getName()));
                }
                // if project was temporary set context to online project
                cms.getRequestContext().setCurrentProject(onlineProject);
            }
        } finally {
            // clear the cache again
            clearcache(false);
        }
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     *
     * @param cms the current request context
     * @param dbc the current database context
     * @param publishList a publish list
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #fillPublishList(CmsDbContext, CmsPublishList)
     */
    public void publishProject(CmsObject cms, CmsDbContext dbc, CmsPublishList publishList, I_CmsReport report)
    throws CmsException {

        // check the parent folders
        checkParentFolders(dbc, publishList);

        // lock all resources with the special publish lock
        List allResources = new ArrayList(publishList.getFolderList());
        allResources.addAll(publishList.getDeletedFolderList());
        allResources.addAll(publishList.getFileList());
        Iterator itResources = allResources.iterator();
        while (itResources.hasNext()) {
            CmsResource resource = (CmsResource)itResources.next();
            CmsLock lock = getLock(dbc, resource);
            if (lock.getSystemLock().isUnlocked() && lock.isLockableBy(dbc.currentUser())) {
                if (lock.getEditionLock().isNullLock()) {
                    lockResource(dbc, resource, CmsLockType.PUBLISH);
                } else {
                    changeLock(dbc, resource, CmsLockType.PUBLISH);
                }
            } else if (lock.getSystemLock().isPublish()
                && lock.getSystemLock().isOwnedInProjectBy(dbc.currentUser(), dbc.currentProject())) {
                // this is a 'shared' publish lock
                // lock this sibling, so during publishing 
                // the siblings will be unlock when all siblings get published
                lockResource(dbc, resource, CmsLockType.PUBLISH);
            } else {
                // this is needed to fix TestPublishIsssues#testPublishScenarioE
                changeLock(dbc, resource, CmsLockType.PUBLISH);
            }
            // now recheck the lock state
            lock = getLock(dbc, resource);
            if (!lock.getSystemLock().isPublish()) {
                if (report != null) {
                    report.println(Messages.get().container(
                        Messages.RPT_PUBLISH_REMOVED_RESOURCE_1,
                        dbc.removeSiteRoot(resource.getRootPath())), I_CmsReport.FORMAT_WARNING);
                }
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.RPT_PUBLISH_REMOVED_RESOURCE_1,
                        dbc.removeSiteRoot(resource.getRootPath())));
                }
                // remove files that could not be locked
                publishList.remove(resource);
            }
        }

        // enqueue the publish job
        CmsException enqueueException = null;
        try {
            m_publishEngine.enqueuePublishJob(cms, publishList, report);
        } catch (CmsException exc) {
            enqueueException = exc;
        }

        // if an exception was raised, remove the publish locks
        // and throw the exception again
        if (enqueueException != null) {
            itResources = allResources.iterator();
            while (itResources.hasNext()) {
                CmsResource resource = (CmsResource)itResources.next();
                CmsLock lock = getLock(dbc, resource);
                if (lock.getSystemLock().isPublish()
                    && lock.getSystemLock().isOwnedBy(cms.getRequestContext().currentUser())) {
                    unlockResource(dbc, resource, true, true);
                }
            }

            throw enqueueException;
        }
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
    public CmsAccessControlEntry readAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsUUID principal)
    throws CmsException {

        return m_userDriver.readAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);
    }

    /**
     * Reads all versions of the given resource.<br>
     * 
     * This method returns a list with the history of the given resource, i.e.
     * the historical resource entries, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to read the history for
     * 
     * @return a list of file headers, as <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllAvailableVersions(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // read the historical resources
        return m_historyDriver.readAllAvailableVersions(dbc, resource.getStructureId());
    }

    /**
     * Reads all property definitions for the given mapping type.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertyDefinitions(CmsDbContext dbc) throws CmsException {

        List returnValue = m_vfsDriver.readPropertyDefinitions(dbc, dbc.currentProject().getUuid());
        Collections.sort(returnValue);
        return returnValue;
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
     * @param dbc the current database context
     * @param resource the resource to return the child resources for
     * @param filter the resource filter to use
     * @param getFolders if true the child folders are included in the result
     * @param getFiles if true the child files are included in the result
     * @param checkPermissions if the resources should be filtered with the current user permissions
     * 
     * @return a list of all child resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readChildResources(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles,
        boolean checkPermissions) throws CmsException {

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(new String[] {
            dbc.currentUser().getName(),
            getFolders ? (getFiles ? CmsCacheKey.CACHE_KEY_SUBALL : CmsCacheKey.CACHE_KEY_SUBFOLDERS)
            : CmsCacheKey.CACHE_KEY_SUBFILES,
            checkPermissions ? "+" : "-",
            filter.getCacheId(),
            resource.getRootPath()}, dbc.currentProject());

        List resourceList = OpenCms.getMemoryMonitor().getCachedResourceList(cacheKey);
        if (resourceList == null) {
            // read the result form the database
            resourceList = m_vfsDriver.readChildResources(dbc, dbc.currentProject(), resource, getFolders, getFiles);

            if (checkPermissions) {
                // apply the permission filter
                resourceList = filterPermissions(dbc, resourceList, filter);
            }
            // cache the sub resources
            OpenCms.getMemoryMonitor().cacheResourceList(cacheKey, resourceList);
        }

        // we must always apply the result filter and update the context dates
        return updateContextDates(dbc, resourceList, filter);
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
     *   <li>if still no file could be found, <code>null</code> is retuned
     * </ol>
     * 
     * @param dbc the database context
     * @param resource the folder to get the default file for
     * 
     * @return the default file for the given folder
     * 
     * @see CmsObject#readDefaultFile(String)
     * @see CmsDriverManager#readDefaultFile(CmsDbContext, CmsResource)
     */
    public CmsResource readDefaultFile(CmsDbContext dbc, CmsResource resource) {

        // resource exists, lets check if we have a file or a folder
        if (resource.isFolder()) {
            // the resource is a folder, check if PROPERTY_DEFAULT_FILE is set on folder
            try {
                String defaultFileName = readPropertyObject(
                    dbc,
                    resource,
                    CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                    false).getValue();
                if (defaultFileName != null) {
                    // property was set, so look up this file first
                    String folderName = CmsResource.getFolderPath(resource.getRootPath());
                    resource = readResource(dbc, folderName + defaultFileName, CmsResourceFilter.DEFAULT);
                }
            } catch (CmsException e) {
                // ignore all other exceptions and continue the lookup process
                LOG.error(e.getLocalizedMessage(), e);
            }
            if (resource.isFolder()) {
                String folderName = CmsResource.getFolderPath(resource.getRootPath());
                // resource is (still) a folder, check default files specified in configuration
                Iterator it = OpenCms.getDefaultFiles().iterator();
                while (it.hasNext()) {
                    String tmpResourceName = folderName + it.next().toString();
                    try {
                        resource = readResource(dbc, tmpResourceName, CmsResourceFilter.DEFAULT);
                        // no exception? So we have found the default file
                        // stop looking for default files   
                        break;
                    } catch (CmsException e) {
                        // ignore all other exceptions and continue the lookup process
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        if (resource.isFolder()) {
            // we only want files as a result for further processing
            resource = null;
        }
        return resource;
    }

    /**
     * Reads all deleted (historical) resources below the given path, 
     * including the full tree below the path, if required.<p>
     * 
     * @param dbc the current db context
     * @param resource the parent resource to read the resources from
     * @param readTree <code>true</code> to read all subresources
     * @param isVfsManager <code>true</code> if the current user has the vfs manager role
     * 
     * @return a list of <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, int)
     * @see CmsObject#readResources(String, CmsResourceFilter, boolean)
     * @see CmsObject#readDeletedResources(String, boolean)
     */
    public List readDeletedResources(CmsDbContext dbc, CmsResource resource, boolean readTree, boolean isVfsManager)
    throws CmsException {

        Set result = new HashSet();
        List deletedResources = m_historyDriver.readDeletedResources(
            dbc,
            resource.getStructureId(),
            isVfsManager ? null : dbc.currentUser().getId());
        result.addAll(deletedResources);
        List newResult = new ArrayList(result.size());
        Iterator it = result.iterator();
        while (it.hasNext()) {
            I_CmsHistoryResource histRes = (I_CmsHistoryResource)it.next();
            // adjust the paths
            try {
                if (!getVfsDriver().validateStructureIdExists(
                    dbc,
                    dbc.currentProject().getUuid(),
                    histRes.getResource().getStructureId())) {
                    // adjust the path in case of deleted files
                    String resourcePath = histRes.getResource().getRootPath();
                    String resName = CmsResource.getName(resourcePath);
                    String path = CmsResource.getParentFolder(resourcePath);

                    CmsUUID parentId = histRes.getParentId();
                    try {
                        // first look for the path through the parent id
                        path = readResource(dbc, parentId, CmsResourceFilter.IGNORE_EXPIRATION).getRootPath();
                    } catch (CmsDataAccessException e) {
                        // if the resource with the parent id is not found, try to get a new parent id with the path
                        try {
                            parentId = readResource(dbc, path, CmsResourceFilter.IGNORE_EXPIRATION).getStructureId();
                        } catch (CmsDataAccessException e1) {
                            // ignore, the parent folder has been completely deleted
                        }
                    }
                    resourcePath = path + resName;

                    boolean isFolder = resourcePath.endsWith("/");
                    if (isFolder) {
                        newResult.add(new CmsHistoryFolder(
                            histRes.getPublishTag(),
                            histRes.getResource().getStructureId(),
                            histRes.getResource().getResourceId(),
                            resourcePath,
                            histRes.getResource().getTypeId(),
                            histRes.getResource().getFlags(),
                            histRes.getResource().getProjectLastModified(),
                            histRes.getResource().getState(),
                            histRes.getResource().getDateCreated(),
                            histRes.getResource().getUserCreated(),
                            histRes.getResource().getDateLastModified(),
                            histRes.getResource().getUserLastModified(),
                            histRes.getResource().getDateReleased(),
                            histRes.getResource().getDateExpired(),
                            histRes.getResource().getVersion(),
                            parentId));
                    } else {
                        newResult.add(new CmsHistoryFile(
                            histRes.getPublishTag(),
                            histRes.getResource().getStructureId(),
                            histRes.getResource().getResourceId(),
                            resourcePath,
                            histRes.getResource().getTypeId(),
                            histRes.getResource().getFlags(),
                            histRes.getResource().getProjectLastModified(),
                            histRes.getResource().getState(),
                            histRes.getResource().getDateCreated(),
                            histRes.getResource().getUserCreated(),
                            histRes.getResource().getDateLastModified(),
                            histRes.getResource().getUserLastModified(),
                            histRes.getResource().getDateReleased(),
                            histRes.getResource().getDateExpired(),
                            histRes.getResource().getLength(),
                            histRes.getResource().getDateContent(),
                            histRes.getResource().getVersion(),
                            parentId,
                            null));
                    }
                } else {
                    newResult.add(histRes);
                }
            } catch (CmsDataAccessException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        if (readTree) {
            Iterator itDeleted = deletedResources.iterator();
            while (itDeleted.hasNext()) {
                I_CmsHistoryResource delResource = (I_CmsHistoryResource)itDeleted.next();
                if (delResource.getResource().isFolder()) {
                    newResult.addAll(readDeletedResources(dbc, delResource.getResource(), readTree, isVfsManager));
                }
            }
            try {
                readResource(dbc, resource.getStructureId(), CmsResourceFilter.ALL);
                // resource exists, so recurse
                Iterator itResources = readResources(dbc, resource, CmsResourceFilter.ALL.addRequireFolder(), readTree).iterator();
                while (itResources.hasNext()) {
                    CmsResource subResource = (CmsResource)itResources.next();
                    if (subResource.isFolder()) {
                        newResult.addAll(readDeletedResources(dbc, subResource, readTree, isVfsManager));
                    }
                }
            } catch (Exception e) {
                // resource does not exists
            }
        }
        Collections.sort(newResult, CmsResource.COMPARE_ROOT_PATH);
        return newResult;
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
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsDbContext dbc, CmsResource resource) throws CmsException {

        if (resource.isFolder()) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_ACCESS_FOLDER_AS_FILE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        CmsUUID projectId = dbc.currentProject().getUuid();
        CmsFile file;
        if (resource instanceof I_CmsHistoryResource) {
            file = new CmsHistoryFile((I_CmsHistoryResource)resource);
            file.setContents(m_historyDriver.readContent(dbc, resource.getStructureId(), resource.getVersion()));
        } else {
            file = new CmsFile(resource);
            file.setContents(m_vfsDriver.readContent(dbc, projectId, resource.getResourceId()));
        }
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
     * @throws CmsDataAccessException if something goes wrong
     *
     * @see #readResource(CmsDbContext, String, CmsResourceFilter)
     * @see CmsObject#readFolder(String)
     * @see CmsObject#readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(CmsDbContext dbc, String resourcename, CmsResourceFilter filter)
    throws CmsDataAccessException {

        CmsResource resource = readResource(dbc, resourcename, filter);

        return convertResourceToFolder(resource);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsProject project) {

        try {
            return readGroup(dbc, project.getGroupId());
        } catch (CmsException exc) {
            return new CmsGroup(
                CmsUUID.getNullUUID(),
                CmsUUID.getNullUUID(),
                project.getGroupId() + "",
                "deleted group",
                0);
        }
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param dbc the current database context
     * @param groupId the id of the group that is to be read
     * 
     * @return the requested group
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsException {

        CmsGroup group = null;
        // try to read group from cache
        group = OpenCms.getMemoryMonitor().getCachedGroup(groupId.toString());
        if (group == null) {
            group = m_userDriver.readGroup(dbc, groupId);
            OpenCms.getMemoryMonitor().cacheGroup(group);
        }
        return group;
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param dbc the current database context
     * @param groupname the name of the group that is to be read
     *
     * @return the requested group
     * 
     * @throws CmsDataAccessException if operation was not succesful
     */
    public CmsGroup readGroup(CmsDbContext dbc, String groupname) throws CmsDataAccessException {

        CmsGroup group = null;
        // try to read group from cache
        group = OpenCms.getMemoryMonitor().getCachedGroup(groupname);
        if (group == null) {
            group = m_userDriver.readGroup(dbc, groupname);
            OpenCms.getMemoryMonitor().cacheGroup(group);
        }
        return group;
    }

    /**
     * Reads a principal (an user or group) from the historical archive based on its ID.<p>
     * 
     * @param dbc the current database context
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
    public CmsHistoryPrincipal readHistoricalPrincipal(CmsDbContext dbc, CmsUUID principalId) throws CmsException {

        return m_historyDriver.readPrincipal(dbc, principalId);
    }

    /**
     * Returns a historical project entry.<p>
     *
     * @param dbc the current database context
     * @param publishTag the publish tag of the project
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsHistoryProject readHistoryProject(CmsDbContext dbc, int publishTag) throws CmsException {

        return m_historyDriver.readProject(dbc, publishTag);
    }

    /**
     * Reads the list of all <code>{@link CmsProperty}</code> objects that belongs to the given historical resource.<p>
     * 
     * @param dbc the current database context
     * @param historyResource the historical resource to read the properties for
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readHistoryPropertyObjects(CmsDbContext dbc, I_CmsHistoryResource historyResource) throws CmsException {

        return m_historyDriver.readProperties(dbc, historyResource);
    }

    /**
     * Reads the locks that were saved to the database in the previous run of OpenCms.<p>
     * 
     * @param dbc the current database context
     * 
     * @throws CmsException if something goes wrong
     */
    public void readLocks(CmsDbContext dbc) throws CmsException {

        m_lockManager.readLocks(dbc);
    }

    /**
     * Reads the manager group of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsDbContext dbc, CmsProject project) {

        try {
            return readGroup(dbc, project.getManagerGroupId());
        } catch (CmsException exc) {
            // the group does not exist any more - return a dummy-group
            return new CmsGroup(
                CmsUUID.getNullUUID(),
                CmsUUID.getNullUUID(),
                project.getManagerGroupId() + "",
                "deleted group",
                0);
        }
    }

    /**
     * Reads an organizational Unit based on its fully qualified name.<p>
     *
     * @param dbc the current db context
     * @param ouFqn the fully qualified name of the organizational Unit to be read
     * 
     * @return the organizational Unit that with the provided fully qualified name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsException {

        CmsOrganizationalUnit organizationalUnit = null;
        // try to read organizational unit from cache
        organizationalUnit = OpenCms.getMemoryMonitor().getCachedOrgUnit(ouFqn);
        if (organizationalUnit == null) {
            organizationalUnit = m_userDriver.readOrganizationalUnit(dbc, ouFqn);
            OpenCms.getMemoryMonitor().cacheOrgUnit(organizationalUnit);
        }
        return organizationalUnit;
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
     * Builds a list of resources for a given path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsDbContext dbc, CmsUUID projectId, String path, CmsResourceFilter filter)
    throws CmsException {

        // # of folders in the path
        int folderCount = 0;
        // true if the path doesn't end with a folder
        boolean lastResourceIsFile = false;
        // holds the CmsResource instances in the path
        List pathList = null;
        // the current path token
        String currentResourceName = null;
        // the current resource
        CmsResource currentResource = null;
        // this is a comment. i love comments!
        int i = 0, count = 0;
        // key to cache the resources
        String cacheKey = null;

        // splits the path into folder and filename tokens
        List tokens = CmsStringUtil.splitAsList(path, '/');

        // the root folder is no token in the path but a resource which has to be added to the path
        count = tokens.size() + 1;
        pathList = new ArrayList(count);

        folderCount = count;
        if (!path.endsWith("/")) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = "/";
        StringBuffer currentPath = new StringBuffer(64);
        currentPath.append('/');

        String cp = currentPath.toString();
        cacheKey = getCacheKey(null, false, projectId, cp);
        currentResource = OpenCms.getMemoryMonitor().getCachedResource(cacheKey);
        if (currentResource == null) {
            currentResource = m_vfsDriver.readFolder(dbc, projectId, cp);
            OpenCms.getMemoryMonitor().cacheResource(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);

        if (count == 1) {
            // the root folder was requested- no further operations required
            return pathList;
        }

        Iterator it = tokens.iterator();
        currentResourceName = (String)it.next();

        // read the folder resources in the path /a/b/c/
        for (i = 1; i < folderCount; i++) {
            currentPath.append(currentResourceName);
            currentPath.append('/');
            // read the folder
            cp = currentPath.toString();
            cacheKey = getCacheKey(null, false, projectId, cp);
            currentResource = OpenCms.getMemoryMonitor().getCachedResource(cacheKey);
            if (currentResource == null) {
                currentResource = m_vfsDriver.readFolder(dbc, projectId, cp);
                OpenCms.getMemoryMonitor().cacheResource(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);

            if (i < folderCount - 1) {
                currentResourceName = (String)it.next();
            }
        }

        // read the (optional) last file resource in the path /x.html
        if (lastResourceIsFile) {
            if (it.hasNext()) {
                // this will only be false if a resource in the 
                // top level root folder (e.g. "/index.html") was requested
                currentResourceName = (String)it.next();
            }
            currentPath.append(currentResourceName);

            // read the file
            cp = currentPath.toString();
            cacheKey = getCacheKey(null, false, projectId, cp);
            currentResource = OpenCms.getMemoryMonitor().getCachedResource(cacheKey);
            if (currentResource == null) {
                currentResource = m_vfsDriver.readResource(dbc, projectId, cp, filter.includeDeleted());
                OpenCms.getMemoryMonitor().cacheResource(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);
        }

        return pathList;
    }

    /**
     * Reads a project given the projects id.<p>
     *
     * @param dbc the current database context
     * @param id the id of the project
     * 
     * @return the project read
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    public CmsProject readProject(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

        CmsProject project = null;
        project = OpenCms.getMemoryMonitor().getCachedProject(id.toString());
        if (project == null) {
            project = m_projectDriver.readProject(dbc, id);
            OpenCms.getMemoryMonitor().cacheProject(project);
        }
        return project;
    }

    /**
     * Reads a project.<p>
     *
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(CmsDbContext, CmsUUID)} to ensure the right project is read.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the project
     * 
     * @return the project read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsDbContext dbc, String name) throws CmsException {

        CmsProject project = null;
        project = OpenCms.getMemoryMonitor().getCachedProject(name);
        if (project == null) {
            project = m_projectDriver.readProject(dbc, name);
            OpenCms.getMemoryMonitor().cacheProject(project);
        }
        return project;
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param dbc the current database context
     * @param project the project to get the project resources for
     * 
     * @return the list of all resources, as <code>{@link String}</code> objects 
     *              that define the "view" of the given project.
     * 
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
     * <li><code>{@link CmsResource#STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(CmsUUID, CmsResourceState)
     */
    public List readProjectView(CmsDbContext dbc, CmsUUID projectId, CmsResourceState state) throws CmsException {

        List resources;
        if (state.isNew() || state.isChanged() || state.isDeleted()) {
            // get all resources form the database that match the selected state
            resources = m_vfsDriver.readResources(dbc, projectId, state, CmsDriverManager.READMODE_MATCHSTATE);
        } else {
            // get all resources form the database that are somehow changed (i.e. not unchanged)
            resources = m_vfsDriver.readResources(
                dbc,
                projectId,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READMODE_UNMATCHSTATE);
        }

        // filter the permissions
        List result = filterPermissions(dbc, resources, CmsResourceFilter.ALL);
        // sort the result
        Collections.sort(result);
        // set the full resource names
        return updateContextDates(dbc, result);
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
     * @return the property definition that was read, 
     *          or <code>null</code> if there is no property definition with the given name.
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        return m_vfsDriver.readPropertyDefinition(dbc, name, dbc.currentProject().getUuid());
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
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, CmsResource resource, String key, boolean search)
    throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(key, search, dbc.currentProject().getUuid(), resource.getRootPath());
        CmsProperty value = OpenCms.getMemoryMonitor().getCachedProperty(cacheKey);

        if (value == null) {
            // check if the map of all properties for this resource is already cached
            String cacheKey2 = getCacheKey(
                CACHE_ALL_PROPERTIES,
                search,
                dbc.currentProject().getUuid(),
                resource.getRootPath());

            List allProperties = OpenCms.getMemoryMonitor().getCachedPropertyList(cacheKey2);

            if (allProperties != null) {
                // list of properties already read, look up value there 
                for (int i = 0; i < allProperties.size(); i++) {
                    CmsProperty property = (CmsProperty)allProperties.get(i);
                    if (property.getName().equals(key)) {
                        value = property;
                        break;
                    }
                }
            } else if (search) {
                // result not cached, look it up recursivly with search enabled
                String cacheKey3 = getCacheKey(key, search, dbc.currentProject().getUuid(), resource.getRootPath());
                value = OpenCms.getMemoryMonitor().getCachedProperty(cacheKey3);

                if ((value == null) || value.isNullProperty()) {
                    boolean cont;
                    do {
                        try {
                            value = readPropertyObject(dbc, resource, key, false);
                            cont = value.isNullProperty() && (resource.getRootPath().length() > 1);
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

            // freeze the value
            value.setFrozen(true);
            // store the result in the cache
            OpenCms.getMemoryMonitor().cacheProperty(cacheKey, value);
        }

        // ensure the result value is not frozen
        return value.cloneAsProperty();
    }

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * All properties in the result List will be in frozen (read only) state, so you can't change the values.<p>
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
     * 
     * @see CmsObject#readPropertyObjects(String, boolean)
     */
    public List readPropertyObjects(CmsDbContext dbc, CmsResource resource, boolean search) throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(
            CACHE_ALL_PROPERTIES,
            search,
            dbc.currentProject().getUuid(),
            resource.getRootPath());

        List properties = OpenCms.getMemoryMonitor().getCachedPropertyList(cacheKey);

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

                        cont = resource.getRootPath().length() > 1;
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

            // set all properties in the result lisst as frozen
            CmsProperty.setFrozen(properties);
            // store the result in the driver manager's cache
            OpenCms.getMemoryMonitor().cachePropertyList(cacheKey, properties);
        }

        return new ArrayList(properties);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishedResources(dbc, publishHistoryId);
    }

    /**
     * Reads a single publish job identified by its publish history id.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId unique id to identify the publish job in the publish history
     * @return an object of type <code>{@link CmsPublishJobInfoBean}</code> 
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPublishJobInfoBean readPublishJob(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishJob(dbc, publishHistoryId);
    }

    /**
     * Reads all available publish jobs.<p>
     * 
     * @param dbc the current database context
     * @param startTime the start of the time range for finish time
     * @param endTime the end of the time range for finish time
     * @return a list of objects of type <code>{@link CmsPublishJobInfoBean}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPublishJobs(CmsDbContext dbc, long startTime, long endTime) throws CmsException {

        return m_projectDriver.readPublishJobs(dbc, startTime, endTime);
    }

    /**
     * Reads the publish list assigned to a publish job.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId the history id identifying the publish job
     * @return the assigned publish list
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList readPublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishList(dbc, publishHistoryId);
    }

    /**
     * Reads the publish report assigned to a publish job.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId the history id identifying the publish job  
     * @return the content of the assigned publish report
     * @throws CmsException if something goes wrong
     */
    public byte[] readPublishReportContents(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishReportContents(dbc, publishHistoryId);
    }

    /**
     * Reads an historical resource entry for the given resource and with the given version number.<p>
     *
     * @param dbc the current db context
     * @param resource the resource to be read
     * @param version the version number to retrieve
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     * @see CmsObject#restoreResourceVersion(CmsUUID, int)
     * @see CmsObject#readResource(CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsDbContext dbc, CmsResource resource, int version) throws CmsException {

        return m_historyDriver.readResource(dbc, resource.getStructureId(), version);
    }

    /**
     * Reads a resource from the VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param structureID the structure id of the resource to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, CmsResourceFilter)
     * @see CmsObject#readResource(CmsUUID)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID structureID, CmsResourceFilter filter)
    throws CmsDataAccessException {

        // please note: the filter will be applied in the security manager later
        CmsResource resource = m_vfsDriver.readResource(
            dbc,
            dbc.getProjectId().isNullUUID() ? dbc.currentProject().getUuid() : dbc.getProjectId(),
            structureID,
            filter.includeDeleted());

        // context dates need to be updated
        updateContextDates(dbc, resource);

        // return the resource
        return resource;
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
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(CmsDbContext dbc, String resourcePath, CmsResourceFilter filter)
    throws CmsDataAccessException {

        // please note: the filter will be applied in the security manager later
        CmsResource resource = m_vfsDriver.readResource(
            dbc,
            dbc.getProjectId().isNullUUID() ? dbc.currentProject().getUuid() : dbc.getProjectId(),
            resourcePath,
            filter.includeDeleted());

        // context dates need to be updated 
        updateContextDates(dbc, resource);

        // return the resource
        return resource;
    }

    /**
     * Reads a resource in the current project with the given publish tag from the historical archive.<p>
     * 
     * @param dbc the current db context
     * @param resource the resource to restore from the archive
     * @param publishTag the publish tag of the resource
     * 
     * @return the file in the current project with the given publish tag from the historical archive, or
     *         {@link CmsVfsResourceNotFoundException} if not found 
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, int)
     * @see CmsObject#readResourceByPublishTag(CmsUUID, int)
     * 
     * @deprecated use {@link #readResource(CmsDbContext, CmsResource, int)} instead
     *             but notice that the <code>publishTag != version</code>
     */
    public I_CmsHistoryResource readResourceForPublishTag(CmsDbContext dbc, CmsResource resource, int publishTag)
    throws CmsException {

        return m_historyDriver.readFile(dbc, resource.getStructureId(), publishTag);
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param dbc the current database context
     * @param parent the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all subresources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     *  
     * @throws CmsDataAccessException if the bare reading of the resources fails
     * @throws CmsException if security and permission checks for the resources read fail 
     */
    public List readResources(CmsDbContext dbc, CmsResource parent, CmsResourceFilter filter, boolean readTree)
    throws CmsException, CmsDataAccessException {

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(new String[] {
            dbc.currentUser().getName(),
            filter.getCacheId(),
            readTree ? "+" : "-",
            parent.getRootPath()}, dbc.currentProject());

        List resourceList = OpenCms.getMemoryMonitor().getCachedResourceList(cacheKey);
        if (resourceList == null) {
            // read the result from the database
            resourceList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getUuid(),
                (readTree ? parent.getRootPath() : parent.getStructureId().toString()),
                filter.getType(),
                filter.getState(),
                filter.getModifiedAfter(),
                filter.getModifiedBefore(),
                filter.getReleaseAfter(),
                filter.getReleaseBefore(),
                filter.getExpireAfter(),
                filter.getExpireBefore(),
                (readTree ? CmsDriverManager.READMODE_INCLUDE_TREE : CmsDriverManager.READMODE_EXCLUDE_TREE)
                    | (filter.excludeType() ? CmsDriverManager.READMODE_EXCLUDE_TYPE : 0)
                    | (filter.excludeState() ? CmsDriverManager.READMODE_EXCLUDE_STATE : 0)
                    | ((filter.getOnlyFolders() != null) ? (filter.getOnlyFolders().booleanValue() ? CmsDriverManager.READMODE_ONLY_FOLDERS
                    : CmsDriverManager.READMODE_ONLY_FILES)
                    : 0));

            // HACK: do not take care of permissions if reading organizational units
            if (!parent.getRootPath().startsWith("/system/orgunits/")) {
                // apply permission filter
                resourceList = filterPermissions(dbc, resourceList, filter);
            }
            // store the result in the resourceList cache
            OpenCms.getMemoryMonitor().cacheResourceList(cacheKey, resourceList);
        }
        // we must always apply the result filter and update the context dates
        return updateContextDates(dbc, resourceList, filter);
    }

    /**
     * Reads all resources that have a value (containing the given value string) set 
     * for the specified property (definition) in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * @param dbc the current database context
     * @param folder the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * @param value the string to search in the value of the property
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(CmsDbContext dbc, CmsResource folder, String propertyDefinition, String value)
    throws CmsException {

        String cacheKey;
        if (value == null) {
            cacheKey = getCacheKey(
                new String[] {dbc.currentUser().getName(), folder.getRootPath(), propertyDefinition},
                dbc.currentProject());
        } else {
            cacheKey = getCacheKey(new String[] {
                dbc.currentUser().getName(),
                folder.getRootPath(),
                propertyDefinition,
                value}, dbc.currentProject());
        }
        List resourceList = OpenCms.getMemoryMonitor().getCachedResourceList(cacheKey);
        if (resourceList == null) {
            // first read the property definition
            CmsPropertyDefinition propDef = readPropertyDefinition(dbc, propertyDefinition);
            // now read the list of resources that have a value set for the property definition
            resourceList = m_vfsDriver.readResourcesWithProperty(
                dbc,
                dbc.currentProject().getUuid(),
                propDef.getId(),
                folder.getRootPath(),
                value);
            // apply permission filter
            resourceList = filterPermissions(dbc, resourceList, CmsResourceFilter.ALL);
            // store the result in the resourceList cache
            OpenCms.getMemoryMonitor().cacheResourceList(cacheKey, resourceList);
        }
        return resourceList;
    }

    /**
     * Returns the set of users that are responsible for a specific resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsiblePrincipals(CmsDbContext dbc, CmsResource resource) throws CmsException {

        Set result = new HashSet();
        Iterator aces = getAccessControlEntries(dbc, resource, true).iterator();
        while (aces.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aces.next();
            if (ace.isResponsible()) {
                I_CmsPrincipal p = lookupPrincipal(dbc, ace.getPrincipal());
                if (p != null) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Returns the set of users that are responsible for a specific resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsibleUsers(CmsDbContext dbc, CmsResource resource) throws CmsException {

        Set result = new HashSet();
        Iterator principals = readResponsiblePrincipals(dbc, resource).iterator();
        while (principals.hasNext()) {
            I_CmsPrincipal principal = (I_CmsPrincipal)principals.next();
            if (principal instanceof CmsGroup) {
                try {
                    result.addAll(getUsersOfGroup(dbc, principal.getName(), true, false, false));
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            } else {
                result.add(principal);
            }
        }
        return result;
    }

    /**
     * Returns a List of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * The result is a list of <code>{@link CmsResource}</code> objects.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to read the siblings for
     * @param filter a filter object
     * 
     * @return a list of <code>{@link CmsResource}</code> Objects that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself
     * 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(CmsDbContext dbc, CmsResource resource, CmsResourceFilter filter) throws CmsException {

        List siblings = m_vfsDriver.readSiblings(dbc, dbc.currentProject().getUuid(), resource, filter.includeDeleted());

        // important: there is no permission check done on the returned list of siblings
        // this is because of possible issues with the "publish all siblings" option,
        // moreover the user has read permission for the content through
        // the selected sibling anyway        
        return updateContextDates(dbc, siblings, filter);
    }

    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param dbc the current database context
     * @param rfsName the rfs name of the resource
     * 
     * @return the paramter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(CmsDbContext dbc, String rfsName) throws CmsException {

        return m_projectDriver.readStaticExportPublishedResourceParameters(dbc, rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param dbc the current database context
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp for reading the data from the db
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(CmsDbContext dbc, int parameterResources, long timestamp) throws CmsException {

        return m_projectDriver.readStaticExportResources(dbc, parameterResources, timestamp);
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * @param dbc the current database context
     * @param id the id of the user to read
     *
     * @return the user read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsException {

        CmsUser user = OpenCms.getMemoryMonitor().getCachedUser(id.toString());
        if (user == null) {
            user = m_userDriver.readUser(dbc, id);
            OpenCms.getMemoryMonitor().cacheUser(user);
        }
        return user;
    }

    /**
     * Returns a user object.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be read
     *
     * @return user read
     * 
     * @throws CmsDataAccessException if operation was not succesful
     */
    public CmsUser readUser(CmsDbContext dbc, String username) throws CmsDataAccessException {

        CmsUser user = OpenCms.getMemoryMonitor().getCachedUser(username);
        if (user == null) {
            user = m_userDriver.readUser(dbc, username);
            OpenCms.getMemoryMonitor().cacheUser(user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param dbc the current database context
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return user read
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsDbContext dbc, String username, String password) throws CmsException {

        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(dbc, username, password, null);
        OpenCms.getMemoryMonitor().cacheUser(user);
        return user;
    }

    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param principal the id of the principal to remove the the access control entry for
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsUUID principal) throws CmsException {

        // remove the ace
        m_userDriver.removeAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);

        // update the "last modified" information
        setDateLastModified(dbc, resource, resource.getDateLastModified());

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Removes a resource from the given organizational unit.<p>
     * 
     * @param dbc the current db context
     * @param orgUnit the organizational unit to remove the resource from
     * @param resourceName the root path of the resource that is to be removed from the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     * @see org.opencms.security.CmsOrgUnitManager#addResourceToOrgUnit(CmsObject, String, String)
     */
    public void removeResourceFromOrgUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, String resourceName)
    throws CmsException {

        m_userDriver.removeResourceFromOrganizationalUnit(dbc, orgUnit, resourceName);
    }

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void removeResourceFromProject(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // remove the resource to the project only if the resource is already in the project
        if (isInsideCurrentProject(dbc, resource.getRootPath())) {
            // check if there are already any subfolders of this resource
            if (resource.isFolder()) {
                List projectResources = m_projectDriver.readProjectResources(dbc, dbc.currentProject());
                for (int i = 0; i < projectResources.size(); i++) {
                    String resname = (String)projectResources.get(i);
                    if (resname.startsWith(resource.getRootPath())) {
                        // delete the existing project resource first
                        m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getUuid(), resname);
                    }
                }
            }
            try {
                m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getUuid(), resource.getRootPath());
            } catch (CmsException exc) {
                // if the subfolder exists already - all is ok
            } finally {
                OpenCms.getMemoryMonitor().flushProjectResources();

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                    "project",
                    dbc.currentProject())));
            }
        }
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * @param readRoles if to read roles or groups
     *
     * @throws CmsException if operation was not succesful
     * @throws CmsIllegalArgumentException if the given user was not member in the given group
     * @throws CmsDbEntryNotFoundException if the given group was not found 
     * @throws CmsSecurityException if the given user was <b>read as 'null' from the database</b>
     * 
     * @see #addUserToGroup(CmsDbContext, String, String, boolean)
     */
    public void removeUserFromGroup(CmsDbContext dbc, String username, String groupname, boolean readRoles)
    throws CmsException, CmsIllegalArgumentException, CmsDbEntryNotFoundException, CmsSecurityException {

        CmsGroup group = readGroup(dbc, groupname);
        //check if group exists
        if (group == null) {
            // the group does not exists
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
        }
        if (group.isVirtual() && !readRoles) {
            // if removing a user from a virtual role treat it as removing the user from the role
            removeUserFromGroup(dbc, username, CmsRole.valueOf(group).getGroupName(), true);
            return;
        }
        if (group.isVirtual()) {
            // this is an hack so to prevent a unlimited recursive calls
            readRoles = false;
        }
        if ((readRoles && !group.isRole()) || (!readRoles && group.isRole())) {
            // we want a role but we got a group, or the other way
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
        }

        // test if this user is existing in the group
        if (!userInGroup(dbc, username, groupname, readRoles)) {
            // user is not in the group, throw exception
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_GROUP_2,
                username,
                groupname));
        }

        CmsUser user = readUser(dbc, username);
        //check if the user exists
        if (user == null) {
            // the user does not exists
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_GROUP_2,
                username,
                groupname));
        }

        if (readRoles) {
            CmsRole role = CmsRole.valueOf(group);
            // the workplace user role can only be removed if no other user has no other role
            if (role.equals(CmsRole.WORKPLACE_USER.forOrgUnit(role.getOuFqn()))) {
                if (getGroupsOfUser(
                    dbc,
                    username,
                    role.getOuFqn(),
                    false,
                    true,
                    true,
                    dbc.getRequestContext().getRemoteAddress()).size() > 1) {
                    return;
                }
            }
            // update virtual groups
            Iterator it = getVirtualGroupsForRole(dbc, role).iterator();
            while (it.hasNext()) {
                CmsGroup virtualGroup = (CmsGroup)it.next();
                if (userInGroup(dbc, username, virtualGroup.getName(), false)) {
                    // here we say readroles = true, to prevent an unlimited recursive calls
                    removeUserFromGroup(dbc, username, virtualGroup.getName(), true);
                }
            }
        }
        m_userDriver.deleteUserInGroup(dbc, user.getId(), group.getId());
        OpenCms.getMemoryMonitor().flushUserGroups();
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
    public void replaceResource(CmsDbContext dbc, CmsResource resource, int type, byte[] content, List properties)
    throws CmsException {

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(dbc, resource, content, type);

        if ((properties != null) && (properties != Collections.EMPTY_LIST)) {
            // write the properties
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), resource, properties);
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();
        }

        // update the resource state
        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());

        setDateLastModified(dbc, resource, System.currentTimeMillis());

        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE, false);

        // clear the cache
        clearResourceCache();

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE | CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     * @throws CmsSecurityException if the specified username and old password could not be verified
     */
    public void resetPassword(CmsDbContext dbc, String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        if ((oldPassword != null) && (newPassword != null)) {

            CmsUser user = null;

            validatePassword(newPassword);

            // read the user as a system user to verify that the specified old password is correct
            try {
                user = m_userDriver.readUser(dbc, username, oldPassword, null);
            } catch (CmsDbEntryNotFoundException e) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username), e);
            }

            if ((user == null) || user.isManaged()) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username));
            }

            m_userDriver.writePassword(dbc, username, oldPassword, newPassword);

        } else if (CmsStringUtil.isEmpty(oldPassword)) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_PWD_OLD_MISSING_0));
        } else if (CmsStringUtil.isEmpty(newPassword)) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_PWD_NEW_MISSING_0));
        }
    }

    /**
     * Restores a deleted resource identified by its structure id from the historical archive.<p>
     * 
     * @param dbc the current database context
     * @param structureId the structure id of the resource to restore
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreDeletedResource(CmsUUID)
     */
    public void restoreDeletedResource(CmsDbContext dbc, CmsUUID structureId) throws CmsException {

        // get the last version, which should be the deleted one
        int version = m_historyDriver.readLastVersion(dbc, structureId);
        // get that version
        I_CmsHistoryResource histRes = m_historyDriver.readResource(dbc, structureId, version);

        // check the parent path
        CmsResource parent;
        try {
            // try to read the parent resource by id
            parent = m_vfsDriver.readResource(dbc, dbc.currentProject().getUuid(), histRes.getParentId(), false);
        } catch (CmsVfsResourceNotFoundException e) {
            // if not found try to read the parent resource by name
            try {
                // try to read the parent resource by id
                parent = m_vfsDriver.readResource(
                    dbc,
                    dbc.currentProject().getUuid(),
                    CmsResource.getParentFolder(histRes.getResource().getRootPath()),
                    false);
            } catch (CmsVfsResourceNotFoundException e1) {
                // if not found try to restore the parent resource
                restoreDeletedResource(dbc, histRes.getParentId());
                parent = readResource(dbc, histRes.getParentId(), CmsResourceFilter.IGNORE_EXPIRATION);
            }
        }
        // check write permissions
        m_securityManager.checkPermissions(
            dbc,
            parent,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // check the name
        String path = CmsResource.getParentFolder(histRes.getResource().getRootPath()); // path
        String resName = CmsResource.getName(histRes.getResource().getRootPath()); // name
        String ext = CmsFileUtil.getExtension(resName); // extension
        String nameWOExt = resName.substring(0, resName.length() - ext.length() - 1); // name without extension
        boolean nameOk = false;
        int i = 1;
        while (!nameOk) {
            try {
                readResource(dbc, path + resName, CmsResourceFilter.ALL);
                // try the next resource name with following schema: path/name_{i}.ext
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ext)) {
                    resName = nameWOExt + "_" + i + "." + ext;
                } else {
                    resName = nameWOExt + "_" + i;
                }
                i++;
            } catch (Exception e) {
                // ok, we found a not used resource name
                nameOk = true;
            }
        }

        // check structure id
        CmsUUID id = structureId;
        if (m_vfsDriver.validateStructureIdExists(dbc, dbc.currentProject().getUuid(), structureId)) {
            // should never happen, but if already exists create a new one
            id = new CmsUUID();
        }

        byte[] contents = null;
        boolean isFolder = true;

        // do we need the contents?
        if (histRes instanceof CmsFile) {
            contents = ((CmsFile)histRes).getContents();
            if ((contents == null) || (contents.length == 0)) {
                contents = m_historyDriver.readContent(dbc, structureId, version);
            }
            isFolder = false;
        }

        // now read the historical properties
        List properties = m_historyDriver.readProperties(dbc, histRes);

        // create the object to create
        CmsResource newResource = new CmsResource(
            id,
            histRes.getResource().getResourceId(),
            path + resName,
            histRes.getResource().getTypeId(),
            isFolder,
            histRes.getResource().getFlags(),
            dbc.currentProject().getUuid(),
            CmsResource.STATE_NEW,
            histRes.getResource().getDateCreated(),
            histRes.getResource().getUserCreated(),
            histRes.getResource().getDateLastModified(),
            dbc.currentUser().getId(),
            histRes.getResource().getDateReleased(),
            histRes.getResource().getDateExpired(),
            histRes.getResource().getSiblingCount(),
            histRes.getResource().getLength(),
            histRes.getResource().getDateContent(),
            histRes.getResource().getVersion() + 1);

        // prevent the date last modified is set to the current time
        newResource.setDateLastModified(newResource.getDateLastModified());
        // restore the resource!
        createResource(dbc, path + resName, newResource, contents, properties, true);
    }

    /**
     * Restores a resource in the current project with a version from the historical archive.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to restore from the archive
     * @param version the version number to restore from the archive
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceVersion(CmsUUID, int)
     * @see I_CmsResourceType#restoreResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsDbContext dbc, CmsResource resource, int version) throws CmsException {

        I_CmsHistoryResource historyResource = readResource(dbc, resource, version);
        CmsResourceState state = CmsResource.STATE_CHANGED;
        if (resource.getState().isNew()) {
            state = CmsResource.STATE_NEW;
        }
        int newVersion = resource.getVersion();
        if (resource.getState().isUnchanged()) {
            newVersion++;
        }
        CmsResource newResource = null;
        // is the resource a file?
        if (historyResource instanceof CmsFile) {
            // get the historical up flags 
            int flags = historyResource.getResource().getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= CmsResource.FLAG_LABELED;
            }
            CmsFile newFile = new CmsFile(
                resource.getStructureId(),
                resource.getResourceId(),
                resource.getRootPath(),
                historyResource.getResource().getTypeId(),
                flags,
                dbc.currentProject().getUuid(),
                state,
                resource.getDateCreated(),
                historyResource.getResource().getUserCreated(),
                resource.getDateLastModified(),
                dbc.currentUser().getId(),
                historyResource.getResource().getDateReleased(),
                historyResource.getResource().getDateExpired(),
                resource.getSiblingCount(),
                historyResource.getResource().getLength(),
                historyResource.getResource().getDateContent(),
                newVersion,
                readFile(dbc, (CmsHistoryFile)historyResource).getContents());

            newResource = writeFile(dbc, newFile);
        } else {
            // it is a folder!
            newResource = new CmsFolder(
                resource.getStructureId(),
                resource.getResourceId(),
                resource.getRootPath(),
                historyResource.getResource().getTypeId(),
                historyResource.getResource().getFlags(),
                dbc.currentProject().getUuid(),
                state,
                resource.getDateCreated(),
                historyResource.getResource().getUserCreated(),
                resource.getDateLastModified(),
                dbc.currentUser().getId(),
                historyResource.getResource().getDateReleased(),
                historyResource.getResource().getDateExpired(),
                newVersion);

            writeResource(dbc, newResource);
        }
        if (newResource != null) {
            // now read the historical properties
            List historyProperties = m_historyDriver.readProperties(dbc, historyResource);
            // remove all properties
            deleteAllProperties(dbc, newResource.getRootPath());
            // write them to the restored resource
            writePropertyObjects(dbc, newResource, historyProperties, false);

            clearResourceCache();
        }

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE | CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateExpired the new expire date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateExpired(String, long, boolean)
     * @see I_CmsResourceType#setDateExpired(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateExpired(CmsDbContext dbc, CmsResource resource, long dateExpired) throws CmsDataAccessException {

        resource.setDateExpired(dateExpired);
        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_STRUCTURE, false);

        // modify the last modified project reference
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_PROJECT, false);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_TIMEFRAME));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "last modified" timestamp of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateLastModified(String, long, boolean)
     * @see I_CmsResourceType#setDateLastModified(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateLastModified(CmsDbContext dbc, CmsResource resource, long dateLastModified)
    throws CmsDataAccessException {

        // modify the last modification date
        resource.setDateLastModified(dateLastModified);
        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        } else if (resource.getState().isNew() && (resource.getSiblingCount() > 1)) {
            // in case of new resources with siblings make sure the state is correct
            resource.setState(CmsResource.STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE, false);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_LASTMODIFIED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateReleased the new release date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateReleased(String, long, boolean)
     * @see I_CmsResourceType#setDateReleased(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateReleased(CmsDbContext dbc, CmsResource resource, long dateReleased)
    throws CmsDataAccessException {

        // modify the last modification date
        resource.setDateReleased(dateReleased);
        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_STRUCTURE, false);

        // modify the last modified project reference
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_PROJECT, false);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_TIMEFRAME));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Sets a new parent group for an already existing group.<p>
     *
     * @param dbc the current database context
     * @param groupName the name of the group that should be written
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDataAccessException if the group with <code>groupName</code> could not be read from VFS
     */
    public void setParentGroup(CmsDbContext dbc, String groupName, String parentGroupName)
    throws CmsException, CmsDataAccessException {

        CmsGroup group = readGroup(dbc, groupName);
        CmsUUID parentGroupId = CmsUUID.getNullUUID();

        // if the group exists, use its id, else set to unknown.
        if (parentGroupName != null) {
            parentGroupId = readGroup(dbc, parentGroupName).getId();
        }

        group.setParentId(parentGroupId);

        // write the changes to the cms
        writeGroup(dbc, group);
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user
     * @param newPassword the new password
     * 
     * @throws CmsException if operation was not succesfull
     * @throws CmsIllegalArgumentException if the user with the <code>username</code> was not found
     */
    public void setPassword(CmsDbContext dbc, String username, String newPassword)
    throws CmsException, CmsIllegalArgumentException {

        validatePassword(newPassword);

        // read the user as a system user to verify that the specified old password is correct
        m_userDriver.readUser(dbc, username);
        // only continue if not found and read user from web might succeed
        m_userDriver.writePassword(dbc, username, null, newPassword);
    }

    /**
     * Moves an user to the given organizational unit.<p>
     * 
     * @param dbc the current db context
     * @param orgUnit the organizational unit to add the resource to
     * @param user the user that is to be moved to the organizational unit
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.security.CmsOrgUnitManager#setUsersOrganizationalUnit(CmsObject, String, String)
     */
    public void setUsersOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsUser user)
    throws CmsException {

        if (!getGroupsOfUser(dbc, user.getName(), false).isEmpty()) {
            throw new CmsDbConsistencyException(Messages.get().container(
                Messages.ERR_ORGUNIT_MOVE_USER_2,
                orgUnit.getName(),
                user.getName()));
        }

        // move the principal
        m_userDriver.setUsersOrganizationalUnit(dbc, orgUnit, user);
        // remove the principal from cache
        clearUserCache(user);
    }

    /**
     * Undelete the resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undeleteResource(String, boolean)
     * @see I_CmsResourceType#undelete(CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undelete(CmsDbContext dbc, CmsResource resource) throws CmsException {

        if (!resource.getState().isDeleted()) {
            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_UNDELETE_FOR_RESOURCE_DELETED_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // set the state to changed
        resource.setState(CmsResourceState.STATE_CHANGED);
        // perform the changes
        updateState(dbc, resource, false);

        // clear the cache
        clearResourceCache();

        // fire change event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * @param mode the undo mode, one of the <code>{@link org.opencms.file.CmsResource.CmsResourceUndoMode}#UNDO_XXX</code> constants 
     *      please note that the recursive flag is ignored at this level
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, CmsResource.CmsResourceUndoMode)
     * @see I_CmsResourceType#undoChanges(CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceUndoMode)
     */
    public void undoChanges(CmsDbContext dbc, CmsResource resource, CmsResource.CmsResourceUndoMode mode)
    throws CmsException {

        if (resource.getState().isNew()) {
            // undo changes is impossible on a new resource
            throw new CmsVfsException(Messages.get().container(Messages.ERR_UNDO_CHANGES_FOR_RESOURCE_NEW_0));
        }

        // we need this for later use
        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        // read the resource from the online project
        CmsResource onlineResource = m_vfsDriver.readResource(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            resource.getStructureId(),
            true);

        CmsResource onlineResourceByPath = null;
        try {
            // this is needed to figure out if a moved resource overwrote a deleted one
            onlineResourceByPath = m_vfsDriver.readResource(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getRootPath(),
                true);

            // force undo move operation if needed
            if (!mode.isUndoMove() && !onlineResourceByPath.getRootPath().equals(onlineResource.getRootPath())) {
                mode = mode.includeMove();
            }
        } catch (Exception e) {
            // ok
        }

        boolean moved = !onlineResource.getRootPath().equals(resource.getRootPath());
        // undo move operation if required
        if (moved && mode.isUndoMove()) {
            moveResource(dbc, resource, onlineResource.getRootPath(), true);
            if ((onlineResourceByPath != null)
                && !onlineResourceByPath.getRootPath().equals(onlineResource.getRootPath())) {
                // was moved over deleted, so the deleted file has to be undone
                undoContentChanges(dbc, onlineProject, null, onlineResourceByPath, CmsResource.STATE_UNCHANGED, true);
            }
        }
        // undo content changes
        CmsResourceState newState = CmsResource.STATE_UNCHANGED;
        if (moved && !mode.isUndoMove()) {
            newState = CmsResource.STATE_CHANGED;
        }
        undoContentChanges(dbc, onlineProject, resource, onlineResource, newState, moved && mode.isUndoMove());
    }

    /**
     * Unlocks all resources in the given project.<p>
     * 
     * @param project the project to unlock the resources in
     */
    public void unlockProject(CmsProject project) {

        // unlock all resources in the project
        m_lockManager.removeResourcesInProject(project.getUuid(), false);
        clearResourceCache();
        OpenCms.getMemoryMonitor().flushProjects();
        // we must also clear the permission cache
        OpenCms.getMemoryMonitor().flushPermissions();
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to unlock
     * @param force <code>true</code>, if a resource is forced to get unlocked, no matter by which user and in which project the resource is currently locked
     * @param unlockSystemLock <code>true</code>, if you also want to remove system locks
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see I_CmsResourceType#unlockResource(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsDbContext dbc, CmsResource resource, boolean force, boolean unlockSystemLock)
    throws CmsException {

        // update the resource cache
        clearResourceCache();

        // now update lock status
        m_lockManager.removeResource(dbc, resource, force, unlockSystemLock);

        // we must also clear the permission cache
        OpenCms.getMemoryMonitor().flushPermissions();

        // fire resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(NOTHING_CHANGED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Update the export points.<p>
     * 
     * All files and folders "inside" an export point are written.<p>
     * 
     * @param dbc the current database context
     */
    public void updateExportPoints(CmsDbContext dbc) {

        try {
            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_EXPORT_POINTS_CONFIGURED_0));
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the export point hash table contains RFS export paths keyed by their internal VFS paths
            Iterator i = exportPointDriver.getExportPointPaths().iterator();
            while (i.hasNext()) {
                String currentExportPoint = (String)i.next();

                // print some report messages
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_WRITE_EXPORT_POINT_1, currentExportPoint));
                }

                try {
                    CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
                    List resources = m_vfsDriver.readResourceTree(
                        dbc,
                        CmsProject.ONLINE_PROJECT_ID,
                        currentExportPoint,
                        filter.getType(),
                        filter.getState(),
                        filter.getModifiedAfter(),
                        filter.getModifiedBefore(),
                        filter.getReleaseAfter(),
                        filter.getReleaseBefore(),
                        filter.getExpireAfter(),
                        filter.getExpireBefore(),
                        CmsDriverManager.READMODE_INCLUDE_TREE
                            | (filter.excludeType() ? CmsDriverManager.READMODE_EXCLUDE_TYPE : 0)
                            | (filter.excludeState() ? CmsDriverManager.READMODE_EXCLUDE_STATE : 0));

                    Iterator j = resources.iterator();
                    while (j.hasNext()) {
                        CmsResource currentResource = (CmsResource)j.next();

                        if (currentResource.isFolder()) {
                            // export the folder                        
                            exportPointDriver.createFolder(currentResource.getRootPath(), currentExportPoint);
                        } else {
                            // try to create the exportpoint folder
                            exportPointDriver.createFolder(currentExportPoint, currentExportPoint);
                            byte[] onlineContent = getVfsDriver().readContent(
                                dbc,
                                CmsProject.ONLINE_PROJECT_ID,
                                currentResource.getResourceId());
                            // export the file content online
                            exportPointDriver.writeFile(
                                currentResource.getRootPath(),
                                currentExportPoint,
                                onlineContent);
                        }
                    }
                } catch (CmsException e) {
                    // there might exist export points without corresponding resources in the VFS
                    // -> ignore exceptions which are not "resource not found" exception quiet here
                    if (e instanceof CmsVfsResourceNotFoundException) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXORT_POINTS_ERROR_0), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXORT_POINTS_ERROR_0), e);
            }
        }
    }

    /**
     * Updates/Creates the given relations for the given resource.<p>
     * 
     * @param dbc the db context
     * @param resource the resource to update the relations for
     * @param links the links to consider for updating
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#updateRelationsForResource(CmsRequestContext, CmsResource, List)
     */
    public void updateRelationsForResource(CmsDbContext dbc, CmsResource resource, List links) throws CmsException {

        // get all siblings
        List siblings;
        if (resource.getSiblingCount() > 1) {
            siblings = readSiblings(dbc, resource, CmsResourceFilter.IGNORE_EXPIRATION);
        } else {
            siblings = new ArrayList();
            siblings.add(resource);
        }
        // clean the relations in content for all siblings 
        Iterator it = siblings.iterator();
        while (it.hasNext()) {
            CmsResource sibling = (CmsResource)it.next();
            // clean the relation information for this sibling
            deleteRelationsForResource(dbc, sibling, CmsRelationFilter.TARGETS.filterDefinedInContent());
        }

        // build the links again only if needed
        if (links != null && !links.isEmpty()) {
            // the set of written relations
            Set writtenRelations = new HashSet();

            // create new relation information
            Iterator itLinks = links.iterator();
            while (itLinks.hasNext()) {
                CmsLink link = (CmsLink)itLinks.next();
                if (link.isInternal()) { // only update internal links

                    // get the target resource
                    CmsResource target = null;
                    try {
                        target = readResource(dbc, link.getTarget(), CmsResourceFilter.ALL);
                    } catch (Exception e) {
                        // ignore
                    }
                    CmsRelation originalRelation;
                    if (target == null) {
                        // if link is broken maintain name and default time window
                        originalRelation = new CmsRelation(
                            resource.getStructureId(),
                            resource.getRootPath(),
                            CmsUUID.getNullUUID(),
                            link.getTarget(),
                            CmsResource.DATE_RELEASED_DEFAULT,
                            CmsResource.DATE_EXPIRED_DEFAULT,
                            link.getType());
                    } else {
                        originalRelation = new CmsRelation(resource, target, link.getType());
                    }
                    // do not write twice the same relation
                    if (writtenRelations.contains(originalRelation)) {
                        continue;
                    }
                    writtenRelations.add(originalRelation);

                    // create the relations in content for all siblings 
                    Iterator itSiblings = siblings.iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource sibling = (CmsResource)itSiblings.next();
                        CmsRelation relation;
                        if (target == null) {
                            // if link is broken maintain name and default time window
                            relation = new CmsRelation(
                                sibling.getStructureId(),
                                sibling.getRootPath(),
                                CmsUUID.getNullUUID(),
                                link.getTarget(),
                                CmsResource.DATE_RELEASED_DEFAULT,
                                CmsResource.DATE_EXPIRED_DEFAULT,
                                link.getType());
                        } else {
                            relation = new CmsRelation(sibling, target, link.getType());
                        }
                        m_vfsDriver.createRelation(dbc, dbc.currentProject().getUuid(), relation);
                    }
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if a user is member of the given group.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user to check
     * @param groupname the name of the group to check
     * @param readRoles if to read roles or groups
     *
     * @return <code>true</code>, if the user is in the group, <code>false</code> otherwise
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean userInGroup(CmsDbContext dbc, String username, String groupname, boolean readRoles)
    throws CmsException {

        List groups = getGroupsOfUser(dbc, username, readRoles);
        for (int i = 0; i < groups.size(); i++) {
            CmsGroup group = (CmsGroup)groups.get(i);
            if (groupname.equals(group.getName()) || groupname.substring(1).equals(group.getName())) {
                return true;
            }
        }
        return false;
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

        OpenCms.getPasswordHandler().validatePassword(password);
    }

    /**
     * Validates the relations for the given resources.<p>
     * 
     * @param dbc the database context
     * @param resources the resources to validate during publishing 
     *              or <code>null</code> for all in current project
     * @param report a report to write the messages to
     * 
     * @return a map with lists of invalid links 
     *          (<code>{@link org.opencms.relations.CmsRelation}}</code> objects) 
     *          keyed by resource names
     * 
     * @throws Exception if something goes wrong
     */
    public Map validateRelations(CmsDbContext dbc, List resources, I_CmsReport report) throws Exception {

        return m_htmlLinkValidator.validateResources(dbc, resources, report);
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
    public void writeAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsAccessControlEntry ace)
    throws CmsException {

        // write the new ace
        m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), ace);

        // update the "last modified" information
        setDateLastModified(dbc, resource, resource.getDateLastModified());

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Writes all export points into the file system for the publish task 
     * specified by trhe given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @param publishHistoryId ID to identify the publish task in the publish history
     */
    public void writeExportPoints(CmsDbContext dbc, I_CmsReport report, CmsUUID publishHistoryId) {

        boolean printReportHeaders = false;
        try {
            // read the "published resources" for the specified publish history ID
            List publishedResources = m_projectDriver.readPublishedResources(dbc, publishHistoryId);
            if (publishedResources.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_EMPTY_PUBLISH_HISTORY_1, publishHistoryId));
                }
                return;
            }

            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_EXPORT_POINTS_CONFIGURED_0));
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the report may be null if the export point write was started by an event
            if (report == null) {
                if (dbc.getRequestContext() != null) {
                    report = new CmsLogReport(dbc.getRequestContext().getLocale(), getClass());
                } else {
                    report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), getClass());
                }
            }

            // iterate over all published resources to export them
            Iterator i = publishedResources.iterator();
            while (i.hasNext()) {
                CmsPublishedResource currentPublishedResource = (CmsPublishedResource)i.next();
                String currentExportPoint = exportPointDriver.getExportPoint(currentPublishedResource.getRootPath());

                if (currentExportPoint != null) {
                    if (!printReportHeaders) {
                        report.println(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_BEGIN_0),
                            I_CmsReport.FORMAT_HEADLINE);
                        printReportHeaders = true;
                    }

                    if (currentPublishedResource.isFolder()) {
                        // export the folder                        
                        if (currentPublishedResource.getState().isDeleted()) {
                            exportPointDriver.deleteResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            exportPointDriver.createFolder(currentPublishedResource.getRootPath(), currentExportPoint);
                        }
                    } else {
                        // export the file            
                        if (currentPublishedResource.getState().isDeleted()) {
                            exportPointDriver.deleteResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            // read the file content online
                            byte[] onlineContent = getVfsDriver().readContent(
                                dbc,
                                CmsProject.ONLINE_PROJECT_ID,
                                currentPublishedResource.getResourceId());
                            exportPointDriver.writeFile(
                                currentPublishedResource.getRootPath(),
                                currentExportPoint,
                                onlineContent);
                        }
                    }

                    // print report message
                    if (currentPublishedResource.getState().isDeleted()) {
                        report.print(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_DELETE_0),
                            I_CmsReport.FORMAT_NOTE);
                    } else {
                        report.print(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_0),
                            I_CmsReport.FORMAT_NOTE);
                    }
                    report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        currentPublishedResource.getRootPath()));
                    report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_WRITE_EXPORT_POINTS_ERROR_0), e);
            }
        } finally {
            if (printReportHeaders) {
                report.println(
                    Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
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
    public CmsFile writeFile(CmsDbContext dbc, CmsFile resource) throws CmsException {

        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), resource, UPDATE_RESOURCE_STATE);

        long contentModificationDate = m_vfsDriver.writeContent(
            dbc,
            dbc.currentProject().getUuid(),
            resource.getResourceId(),
            resource.getContents());
        resource = new CmsFile(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getRootPath(),
            resource.getTypeId(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getDateReleased(),
            resource.getDateExpired(),
            resource.getSiblingCount(),
            resource.getLength(),
            contentModificationDate,
            resource.getVersion(),
            resource.getContents());

        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));

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
     * @param dbc the current database context
     * @param group the group that should be written
     * 
     * @throws CmsException if operation was not succesfull
     */
    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsException {

        OpenCms.getMemoryMonitor().uncacheGroup(group);
        m_userDriver.writeGroup(dbc, group);
        OpenCms.getMemoryMonitor().cacheGroup(group);
    }

    /**
     * Creates an historical entry of the current project.<p>
     * 
     * @param dbc the current database context
     * @param publishTag the version
     * @param publishDate the date of publishing
     *
     * @throws CmsDataAccessException if operation was not succesful
     */
    public void writeHistoryProject(CmsDbContext dbc, int publishTag, long publishDate) throws CmsDataAccessException {

        m_historyDriver.writeProject(dbc, publishTag, publishDate);
    }

    /**
     * Writes the locks that are currently stored in-memory to the database to allow restoring them  
     * in future server startups.<p> 
     * 
     * This overwrites the locks previously stored in the underlying database table.<p>
     * 
     * @param dbc the current database context 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void writeLocks(CmsDbContext dbc) throws CmsException {

        m_lockManager.writeLocks(dbc);
    }

    /**
     * Writes an already existing organizational unit.<p>
     *
     * The organizational unit id has to be a valid OpenCms organizational unit id.<br>
     * 
     * The organizational unit with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current db context
     * @param organizationalUnit the organizational unit that should be written
     * 
     * @throws CmsException if operation was not successful
     * 
     * @see org.opencms.security.CmsOrgUnitManager#writeOrganizationalUnit(CmsObject, CmsOrganizationalUnit)
     */
    public void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsException {

        OpenCms.getMemoryMonitor().uncacheOrgUnit(organizationalUnit);
        m_userDriver.writeOrganizationalUnit(dbc, organizationalUnit);
        OpenCms.getMemoryMonitor().cacheOrgUnit(organizationalUnit);
    }

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param project the project that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeProject(CmsDbContext dbc, CmsProject project) throws CmsException {

        OpenCms.getMemoryMonitor().uncacheProject(project);
        m_projectDriver.writeProject(dbc, project);
        OpenCms.getMemoryMonitor().cacheProject(project);
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
    public void writePropertyObject(CmsDbContext dbc, CmsResource resource, CmsProperty property) throws CmsException {

        try {
            if (property == CmsProperty.getNullProperty()) {
                // skip empty or null properties
                return;
            }

            // test if and what state should be updated
            // 0: none, 1: str, 2: res
            int updateState = getUpdateState(dbc, resource, Collections.singletonList(property));

            // write the property
            m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);

            if (updateState > 0) {
                updateState(dbc, resource, updateState == 2);
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();

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
     * @param updateState if <code>true</code> the state of the resource will be updated
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see I_CmsResourceType#writePropertyObjects(CmsObject, CmsSecurityManager, CmsResource, List)
     */
    public void writePropertyObjects(CmsDbContext dbc, CmsResource resource, List properties, boolean updateState)
    throws CmsException {

        if ((properties == null) || (properties.size() == 0)) {
            // skip empty or null lists
            return;
        }

        try {

            // the specified list must not contain two or more equal property objects
            for (int i = 0, n = properties.size(); i < n; i++) {
                Set keyValidationSet = new HashSet();
                CmsProperty property = (CmsProperty)properties.get(i);
                if (!keyValidationSet.contains(property.getName())) {
                    keyValidationSet.add(property.getName());
                } else {
                    throw new CmsVfsException(Messages.get().container(
                        Messages.ERR_VFS_INVALID_PROPERTY_LIST_1,
                        property.getName()));
                }
            }

            // test if and what state should be updated
            // 0: none, 1: res, 2: str
            int updateStateValue = 0;
            if (updateState) {
                updateStateValue = getUpdateState(dbc, resource, properties);
            }

            for (int i = 0; i < properties.size(); i++) {
                // write the property
                CmsProperty property = (CmsProperty)properties.get(i);
                m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);
            }

            if (updateStateValue > 0) {
                updateState(dbc, resource, updateStateValue == 2);
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();

            // fire an event that the properties of a resource have been modified
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", resource)));
        }
    }

    /**
     * Updates a publish job.<p>
     * 
     * @param dbc the current database context
     * @param publishJob the publish job to update
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsException {

        m_projectDriver.writePublishJob(dbc, publishJob);
    }

    /**
     * Writes the publish report for a publish job.<p>
     * 
     * @param dbc the current database context
     * @param publishJob the publish job 
     * @throws CmsException if something goes wrong
     */
    public void writePublishReport(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsException {

        CmsPublishReport report = (CmsPublishReport)publishJob.removePublishReport();

        if (report != null) {
            m_projectDriver.writePublishReport(dbc, publishJob.getPublishHistoryId(), report.getContents());
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
    public void writeResource(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // access was granted - write the resource
        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), resource, UPDATE_RESOURCE_STATE);

        // make sure the written resource has the state correctly set
        if (resource.getState().isUnchanged()) {
            resource.setState(CmsResource.STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param dbc the current database context
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_projectDriver.writeStaticExportPublishedResource(dbc, resourceName, linkType, linkParameter, timestamp);
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsException {

        clearUserCache(user);
        m_userDriver.writeUser(dbc, user);
    }

    /** 
     * Converts a resource to a folder (if possible).<p>
     * 
     * @param resource the resource to convert
     * @return the converted resource 
     * 
     * @throws CmsVfsResourceNotFoundException if the resource is not a folder
     */
    protected CmsFolder convertResourceToFolder(CmsResource resource) throws CmsVfsResourceNotFoundException {

        if (resource.isFolder()) {
            return new CmsFolder(resource);
        }

        throw new CmsVfsResourceNotFoundException(Messages.get().container(
            Messages.ERR_ACCESS_FILE_AS_FOLDER_1,
            resource.getRootPath()));
    }

    /**
     * Returns the lock manager instance.<p>
     * 
     * @return the lock manager instance
     */
    protected CmsLockManager getLockManager() {

        return m_lockManager;
    }

    /**
     * Checks the parent of a resource during publishing.<p> 
     * 
     * @param dbc the current database context
     * @param deletedFolders a list of deleted folders
     * @param res a resource to check the parent for
     * 
     * @return <code>true</code> if the parent resource will be deleted during publishing
     */
    private boolean checkDeletedParentFolder(CmsDbContext dbc, List deletedFolders, CmsResource res) {

        String parentPath = CmsResource.getParentFolder(res.getRootPath());

        if (parentPath == null) {
            // resource has no parent
            return false;
        }

        CmsResource parent;
        try {
            parent = readResource(dbc, parentPath, CmsResourceFilter.ALL);
        } catch (Exception e) {
            // failure: if we cannot read the parent, we should not publish the resource
            return false;
        }

        if (!parent.getState().isDeleted()) {
            // parent is not deleted
            return false;
        }

        for (int j = 0; j < deletedFolders.size(); j++) {
            if (((CmsResource)deletedFolders.get(j)).getStructureId().equals(parent.getStructureId())) {
                // parent is deleted, and it will get published
                return true;
            }
        }

        // parent is new, but it will not get published
        return false;
    }

    /**
     * Checks that no one of the resources to be published has a 'new' parent (that has not been published yet).<p> 
     * 
     * @param dbc the db context
     * @param publishList the publish list to check
     * 
     * @throws CmsVfsException if there is a resource to be published with a 'new' parent
     */
    private void checkParentFolders(CmsDbContext dbc, CmsPublishList publishList) throws CmsVfsException {

        boolean directPublish = publishList.isDirectPublish();
        // if we direct publish a file, check if all parent folders are already published
        if (directPublish) {
            // first get the names of all parent folders
            Iterator it = publishList.getDirectPublishResources().iterator();
            List parentFolderNames = new ArrayList();
            while (it.hasNext()) {
                CmsResource res = (CmsResource)it.next();
                String parentFolderName = CmsResource.getParentFolder(res.getRootPath());
                if (parentFolderName != null) {
                    parentFolderNames.add(parentFolderName);
                }
            }
            // remove duplicate parent folder names
            parentFolderNames = CmsFileUtil.removeRedundancies(parentFolderNames);
            String parentFolderName = null;
            try {
                // now check all folders if they exist in the online project
                Iterator parentIt = parentFolderNames.iterator();
                while (parentIt.hasNext()) {
                    parentFolderName = (String)parentIt.next();
                    getVfsDriver().readFolder(dbc, CmsProject.ONLINE_PROJECT_ID, parentFolderName);
                }
            } catch (CmsException e) {
                throw new CmsVfsException(Messages.get().container(
                    Messages.RPT_PARENT_FOLDER_NOT_PUBLISHED_1,
                    parentFolderName));
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

        if (parentPath == null) {
            // resource has no parent
            return true;
        }

        CmsResource parent;
        try {
            parent = readResource(dbc, parentPath, CmsResourceFilter.ALL);
        } catch (Exception e) {
            // failure: if we cannot read the parent, we should not publish the resource
            return false;
        }

        if (!parent.getState().isNew()) {
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

        OpenCms.getMemoryMonitor().flushACLs();
        OpenCms.getMemoryMonitor().flushPermissions();
        clearResourceCache();
    }

    /**
     * Clears all internal caches.<p>
     * 
     * @param principalsOnly clear group and user caches only flag 
     */
    private void clearcache(boolean principalsOnly) {

        OpenCms.getMemoryMonitor().flushUsers();
        OpenCms.getMemoryMonitor().flushGroups();
        OpenCms.getMemoryMonitor().flushOrgUnits();
        OpenCms.getMemoryMonitor().flushUserGroups();
        OpenCms.getMemoryMonitor().flushACLs();
        OpenCms.getMemoryMonitor().flushPermissions();

        if (!principalsOnly) {
            OpenCms.getMemoryMonitor().flushProjects();
            OpenCms.getMemoryMonitor().flushResources();
            OpenCms.getMemoryMonitor().flushResourceLists();
            OpenCms.getMemoryMonitor().flushProperties();
            OpenCms.getMemoryMonitor().flushPropertyLists();
            OpenCms.getMemoryMonitor().flushProjectResources();
        }
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     */
    private void clearResourceCache() {

        OpenCms.getMemoryMonitor().flushResources();
        OpenCms.getMemoryMonitor().flushResourceLists();
    }

    /**
     * Clears the user cache for the given user.<p>
     * @param user the user
     */
    private void clearUserCache(CmsUser user) {

        OpenCms.getMemoryMonitor().uncacheUser(user);
        OpenCms.getMemoryMonitor().flushResourceLists();
    }

    /**
     * Filters the given list of resources, removes all resources where the current user
     * does not have READ permissions, plus the filter is applied.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of CmsResources
     * @param filter the resource filter to use
     * 
     * @return the filtered list of resources
     * 
     * @throws CmsException in case errors testing the permissions
     */
    private List filterPermissions(CmsDbContext dbc, List resourceList, CmsResourceFilter filter) throws CmsException {

        if (filter.requireTimerange()) {
            // never check time range here - this must be done later in #updateContextDates(...)
            filter = filter.addExcludeTimerange();
        }
        ArrayList result = new ArrayList(resourceList.size());
        for (int i = 0; i < resourceList.size(); i++) {
            // check the permission of all resources
            CmsResource currentResource = (CmsResource)resourceList.get(i);
            int perms = m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                filter);
            if (perms == CmsSecurityManager.PERM_ALLOWED) {
                // only return resources where permission was granted
                result.add(currentResource);
            }
        }
        // return the result
        return result;
    }

    /**
     * Returns a filtered list of resources for publishing.<p>
     * Contains all resources, which are not locked 
     * and which have a parent folder that is already published or will be published, too.
     * 
     * @param dbc the current database context
     * @param publishList the filling publish list
     * @param resourceList the list of resources to filter
     * @return a filtered list of resources
     */
    private List filterResources(CmsDbContext dbc, CmsPublishList publishList, List resourceList) {

        List result = new ArrayList();

        // local folder list for adding new publishing subfolders
        // this solves the {@link org.opencms.file.TestPublishIssues#testPublishScenarioD} problem.
        List newFolderList = new ArrayList(publishList == null ? resourceList : publishList.getFolderList());

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                CmsLock lock = getLock(dbc, res);
                if (!lock.isLockableBy(dbc.currentUser())) {
                    // checks if there is a shared lock and if the resource is deleted
                    // this solves the {@link org.opencms.file.TestPublishIssues#testPublishScenarioE} problem.
                    if (lock.isShared() && publishList != null) {
                        if (!res.getState().isDeleted()
                            || !checkDeletedParentFolder(dbc, publishList.getDeletedFolderList(), res)) {
                            continue;
                        }
                    } else {
                        // don't add locked resources
                        continue;
                    }
                }
                if (!"/".equals(res.getRootPath()) && !checkParentResource(dbc, newFolderList, res)) {
                    continue;
                }
                // check permissions
                try {
                    m_securityManager.checkPermissions(
                        dbc,
                        res,
                        CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                        false,
                        CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // skip if not enough permissions
                    continue;
                }
                if (res.isFolder()) {
                    newFolderList.add(res);
                }
                result.add(res);
            } catch (Exception e) {
                // noop
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns a filtered list of sibling resources for publishing.<p>
     * 
     * Contains all siblings of the given resources, which are not locked
     * and which have a parent folder that is already published or will be published, too.<p>
     * 
     * @param dbc the current database context 
     * @param publishList the unfinished publish list
     * @param resourceList the list of siblings to filter
     * 
     * @return a filtered list of sibling resources for publishing
     */
    private List filterSiblings(CmsDbContext dbc, CmsPublishList publishList, Collection resourceList) {

        List result = new ArrayList();

        // removed internal extendendable folder list, since iterated (sibling) resources are files in any case, never folders

        for (Iterator i = resourceList.iterator(); i.hasNext();) {
            CmsResource res = (CmsResource)i.next();
            try {
                CmsLock lock = getLock(dbc, res);
                if (!lock.isLockableBy(dbc.currentUser())) {
                    // checks if there is a shared lock and if the resource is deleted
                    // this solves the {@link org.opencms.file.TestPublishIssues#testPublishScenarioE} problem.
                    if (lock.isShared() && publishList != null) {
                        if (!res.getState().isDeleted()
                            || !checkDeletedParentFolder(dbc, publishList.getDeletedFolderList(), res)) {
                            continue;
                        }
                    } else {
                        // don't add locked resources
                        continue;
                    }
                }
                if (!"/".equals(res.getRootPath()) && !checkParentResource(dbc, publishList.getFolderList(), res)) {
                    // don't add resources that have no parent in the online project
                    continue;
                }
                // check permissions
                try {
                    m_securityManager.checkPermissions(
                        dbc,
                        res,
                        CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                        false,
                        CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    // skip if not enough permissions
                    continue;
                }
                result.add(res);
            } catch (Exception e) {
                // noop
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the access control list of a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param forFolder should be true if resource is a folder
     * @param depth the depth to include non-inherited access entries, also
     * @param inheritedOnly flag indicates to collect inherited permissions only
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsAccessControlList getAccessControlList(
        CmsDbContext dbc,
        CmsResource resource,
        boolean inheritedOnly,
        boolean forFolder,
        int depth) throws CmsException {

        String cacheKey = getCacheKey(new String[] {
            inheritedOnly ? "+" : "-",
            forFolder ? "+" : "-",
            Integer.toString(depth),
            resource.getStructureId().toString()}, dbc.currentProject());

        CmsAccessControlList acl = OpenCms.getMemoryMonitor().getCachedACL(cacheKey);

        // return the cached acl if already available
        if (acl != null) {
            return acl;
        }

        List aces = m_userDriver.readAccessControlEntries(
            dbc,
            dbc.currentProject(),
            resource.getResourceId(),
            (depth > 1) || ((depth > 0) && forFolder));

        // sort the list of aces
        boolean overwriteAll = sortAceList(aces);

        // if no 'overwrite all' ace was found
        if (!overwriteAll) {
            // get the acl of the parent
            CmsResource parentResource = null;
            try {
                // try to recurse over the id
                parentResource = m_vfsDriver.readParentFolder(
                    dbc,
                    dbc.currentProject().getUuid(),
                    resource.getStructureId());
            } catch (CmsVfsResourceNotFoundException e) {
                // should never happen, but try with the path
                String parentPath = CmsResource.getParentFolder(resource.getRootPath());
                if (parentPath != null) {
                    parentResource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getUuid(), parentPath);
                }
            }
            if (parentResource != null) {
                acl = (CmsAccessControlList)getAccessControlList(
                    dbc,
                    parentResource,
                    inheritedOnly,
                    forFolder,
                    depth + 1).clone();
            }
        }
        if (acl == null) {
            acl = new CmsAccessControlList();
        }

        if (!((depth == 0) && inheritedOnly)) {
            Iterator itAces = aces.iterator();
            while (itAces.hasNext()) {
                CmsAccessControlEntry acEntry = (CmsAccessControlEntry)itAces.next();
                if (depth > 0) {
                    acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
                }

                acl.add(acEntry);

                // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry
                // denied permissions are kept or extended
                if ((acEntry.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
                    acl.setAllowedPermissions(acEntry);
                }
            }
        }
        OpenCms.getMemoryMonitor().cacheACL(cacheKey, acl);
        return acl;
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param flag a boolean flag for the key (only used if prefix is not null)
     * @param projectId the project for which to generate the key
     * @param resource the resource for which to generate the key
     * 
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, boolean flag, CmsUUID projectId, String resource) {

        StringBuffer b = new StringBuffer(64);
        if (prefix != null) {
            b.append(prefix);
            b.append(flag ? '+' : '-');
        }
        b.append(CmsProject.isOnlineProject(projectId) ? '+' : '-');
        return b.append(resource).toString();
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param keys an array of keys to generate the cache key from
     * @param project the project for which to genertate the key
     *
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String[] keys, CmsProject project) {

        StringBuffer b = new StringBuffer(64);
        int len = keys.length;
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                b.append(keys[i]);
                b.append('_');
            }
        }
        if (project != null) {
            b.append(project.isOnlineProject() ? '+' : '-');
        }
        return b.toString();
    }

    /**
     * Returns if and what state needs to be updated.<p>
     * 
     * @param dbc the db context
     * @param resource the resource
     * @param properties the properties to check
     * 
     * @return 0: none, 1: structure, 2: resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    private int getUpdateState(CmsDbContext dbc, CmsResource resource, List properties) throws CmsDataAccessException {

        int updateState = 0;
        Iterator it = properties.iterator();
        while (it.hasNext() && (updateState < 2)) {
            CmsProperty property = (CmsProperty)it.next();

            // read existing property
            CmsProperty existingProperty = m_vfsDriver.readPropertyObject(
                dbc,
                property.getName(),
                dbc.currentProject(),
                resource);

            // check the shared property
            if (property.getResourceValue() != null) {
                if (property.isDeleteResourceValue()) {
                    if (existingProperty.getResourceValue() != null) {
                        updateState = 2; // deleted
                    }
                } else {
                    if (existingProperty.getResourceValue() == null) {
                        updateState = 2; // created
                    } else {
                        if (!property.getResourceValue().equals(existingProperty.getResourceValue())) {
                            updateState = 2; // updated
                        }
                    }
                }
            }
            if (updateState == 0) {
                // check the individual property only if needed
                if (property.getStructureValue() != null) {
                    if (property.isDeleteStructureValue()) {
                        if (existingProperty.getStructureValue() != null) {
                            updateState = 1; // deleted
                        }
                    } else {
                        if (existingProperty.getStructureValue() == null) {
                            updateState = 1; // created
                        } else {
                            if (!property.getStructureValue().equals(existingProperty.getStructureValue())) {
                                updateState = 1; // updated
                            }
                        }
                    }
                }
            }
        }
        return updateState;
    }

    /**
     * Returns all groups that are virtualizing the given role in the given ou.<p>
     * 
     * @param dbc the database context
     * @param role the role
     * 
     * @return all groups that are virtualizing the given role (or a child of it)
     * 
     * @throws CmsException if soemthing goes wrong 
     */
    private List getVirtualGroupsForRole(CmsDbContext dbc, CmsRole role) throws CmsException {

        Set roleFlags = new HashSet();
        // add role flag
        Integer flags = new Integer(role.getVirtualGroupFlags());
        roleFlags.add(flags);
        // collect all child role flags
        Iterator itChildRoles = role.getChilds(true).iterator();
        while (itChildRoles.hasNext()) {
            CmsRole child = (CmsRole)itChildRoles.next();
            flags = new Integer(child.getVirtualGroupFlags());
            roleFlags.add(flags);
        }
        // iterate all groups matching the flags
        List groups = new ArrayList();
        Iterator it = getGroups(dbc, readOrganizationalUnit(dbc, role.getOuFqn()), false, false).iterator();
        while (it.hasNext()) {
            CmsGroup group = (CmsGroup)it.next();
            if (group.isVirtual()) {
                CmsRole r = CmsRole.valueOf(group);
                if (roleFlags.contains(new Integer(r.getVirtualGroupFlags()))) {
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * @param dbc the current database context
     * @param ouFqn the organizational unit to get the users from 
     * @param groupname the name of the group to list users from
     * @param includeOtherOuUsers include users of other organizational units
     * @param directUsersOnly if set only the direct assigned users will be returned, 
     *                        if not also indirect users, ie. members of parent roles, 
     *                        this parameter only works with roles
     * @param readRoles if to read roles or groups
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     * 
     * @throws CmsException if operation was not succesful
     */
    private List internalUsersOfGroup(
        CmsDbContext dbc,
        String ouFqn,
        String groupname,
        boolean includeOtherOuUsers,
        boolean directUsersOnly,
        boolean readRoles) throws CmsException {

        CmsGroup group = readGroup(dbc, groupname); // check that the group really exists
        if ((group != null) && ((!readRoles && !group.isRole()) || (readRoles && group.isRole()))) {
            String prefix = "_" + includeOtherOuUsers + "_" + directUsersOnly + "_" + ouFqn;
            String cacheKey = m_keyGenerator.getCacheKeyForGroupUsers(prefix, dbc, group);
            List allUsers = OpenCms.getMemoryMonitor().getCachedUserGroups(cacheKey);
            if (allUsers == null) {
                Set users = new HashSet(m_userDriver.readUsersOfGroup(dbc, groupname, includeOtherOuUsers));
                if (readRoles && !directUsersOnly) {
                    CmsRole role = CmsRole.valueOf(group);
                    if (role.getParentRole() != null) {
                        try {
                            String parentGroup = role.getParentRole().getGroupName();
                            readGroup(dbc, parentGroup);
                            // iterate the parent roles
                            users.addAll(internalUsersOfGroup(
                                dbc,
                                ouFqn,
                                parentGroup,
                                includeOtherOuUsers,
                                directUsersOnly,
                                readRoles));
                        } catch (CmsDbEntryNotFoundException e) {
                            // ignore, this may happen while deleting an orgunit
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                    String parentOu = CmsOrganizationalUnit.getParentFqn(group.getOuFqn());
                    if (parentOu != null) {
                        // iterate the parent ou's
                        users.addAll(internalUsersOfGroup(
                            dbc,
                            ouFqn,
                            parentOu + group.getSimpleName(),
                            includeOtherOuUsers,
                            directUsersOnly,
                            readRoles));
                    }
                    // filter users from other ous
                    if (!includeOtherOuUsers) {
                        Iterator itUsers = users.iterator();
                        while (itUsers.hasNext()) {
                            CmsUser user = (CmsUser)itUsers.next();
                            if (!user.getOuFqn().equals(ouFqn)) {
                                itUsers.remove();
                            }
                        }
                    }
                }
                // make user list unmodifiable for caching
                allUsers = Collections.unmodifiableList(new ArrayList(users));
                OpenCms.getMemoryMonitor().cacheUserGroups(cacheKey, allUsers);
            }
            return allUsers;
        } else {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
        }
    }

    /**
     * Reads all resources that are inside and changed in a specified project.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the project
     * @param mode one of the {@link CmsReadChangedProjectResourceMode} constants
     * 
     * @return a List with all resources inside the specified project
     * 
     * @throws CmsException if something goes wrong
     */
    private List readChangedResourcesInsideProject(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsReadChangedProjectResourceMode mode) throws CmsException {

        String cacheKey = projectId + "_" + mode.toString();
        List result = OpenCms.getMemoryMonitor().getCachedProjectResources(cacheKey);
        if (result != null) {
            return result;
        }
        List projectResources = readProjectResources(dbc, readProject(dbc, projectId));
        result = new ArrayList();
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
                if (!(e instanceof CmsVfsResourceNotFoundException)) {
                    throw e;
                }
            }
        }

        for (int j = 0; j < resources.size(); j++) {
            currentResource = (CmsResource)resources.get(j);
            currentLock = getLock(dbc, currentResource).getEditionLock();

            if (!currentResource.getState().isUnchanged()) {
                if ((currentLock.isNullLock() && (currentResource.getProjectLastModified().equals(projectId)))
                    || (currentLock.isOwnedBy(dbc.currentUser()) && (currentLock.getProjectId().equals(projectId)))) {
                    // add only resources that are 
                    // - inside the project,
                    // - changed in the project,
                    // - either unlocked, or locked for the current user in the project
                    if ((mode == RCPRM_FILES_AND_FOLDERS_MODE)
                        || (currentResource.isFolder() && (mode == RCPRM_FOLDERS_ONLY_MODE))
                        || (currentResource.isFile() && (mode == RCPRM_FILES_ONLY_MODE))) {
                        result.add(currentResource);
                    }
                }
            }
        }

        resources.clear();
        resources = null;

        OpenCms.getMemoryMonitor().cacheProjectResources(cacheKey, result);
        return result;
    }

    /**
     * Sorts the given list of {@link CmsAccessControlEntry} objects.<p>
     * 
     * The the 'all others' ace in first place, the 'overwrite all' ace in second.<p>
     * 
     * @param aces the list of ACEs to sort
     * 
     * @return <code>true</code> if the list contains the 'overwrite all' ace
     */
    private boolean sortAceList(List aces) {

        // sort the list of entries 
        Collections.sort(aces, CmsAccessControlEntry.COMPARATOR_ACE);
        // after sorting just the first 2 positions come in question
        for (int i = 0; i < Math.min(aces.size(), 2); i++) {
            CmsAccessControlEntry acEntry = (CmsAccessControlEntry)aces.get(i);
            if (acEntry.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * All permissions and resources attributes of the principal
     * are transfered to a replacement principal.<p>
     *
     * @param dbc the current database context
     * @param project the current project
     * @param principalId the id of the principal to be replaced
     * @param replacementId the user to be transfered
     * @param withACEs flag to signal if the ACEs should also be transfered or just deleted
     * 
     * @throws CmsException if operation was not succesfull
     */
    private void transferPrincipalResources(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID principalId,
        CmsUUID replacementId,
        boolean withACEs) throws CmsException {

        // get all resources for the given user including resources associated by ACEs or attributes
        List resources = getResourcesForPrincipal(dbc, project, principalId, null, true);
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            CmsResource resource = (CmsResource)it.next();
            // check resource attributes
            boolean attrModified = false;
            CmsUUID createdUser = null;
            if (resource.getUserCreated().equals(principalId)) {
                createdUser = replacementId;
                attrModified = true;
            }
            CmsUUID lastModUser = null;
            if (resource.getUserLastModified().equals(principalId)) {
                lastModUser = replacementId;
                attrModified = true;
            }
            if (attrModified) {
                m_vfsDriver.transferResource(dbc, project, resource, createdUser, lastModUser);
                // clear the cache
                clearResourceCache();
            }
            boolean aceModified = false;
            // check aces
            if (withACEs) {
                Iterator itAces = m_userDriver.readAccessControlEntries(dbc, project, resource.getResourceId(), false).iterator();
                while (itAces.hasNext()) {
                    CmsAccessControlEntry ace = (CmsAccessControlEntry)itAces.next();
                    if (ace.getPrincipal().equals(principalId)) {
                        CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                            ace.getResource(),
                            replacementId,
                            ace.getAllowedPermissions(),
                            ace.getDeniedPermissions(),
                            ace.getFlags());
                        // write the new ace
                        m_userDriver.writeAccessControlEntry(dbc, project, newAce);
                        aceModified = true;
                    }
                }
                if (aceModified) {
                    // clear the cache
                    clearAccessControlListCache();
                }
            }
            if (attrModified || aceModified) {
                // fire the event
                HashMap data = new HashMap(2);
                data.put("resource", resource);
                data.put("change", new Integer(((attrModified) ? CHANGED_RESOURCE : 0)
                    | ((aceModified) ? CHANGED_ACCESSCONTROL : 0)));
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
            }
        }
    }

    /**
     * Undoes all content changes of a resource.<p>
     * 
     * @param dbc the database context
     * @param onlineProject the online project
     * @param offlineResource the offline resource, or <code>null</code> if deleted
     * @param onlineResource the online resource
     * @param newState the new resource state
     * @param moveUndone is a move operation on the same resource has been made
     * 
     * @throws CmsException if something goes wrong
     */
    private void undoContentChanges(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsResource onlineResource,
        CmsResourceState newState,
        boolean moveUndone) throws CmsException {

        String path = ((moveUndone || (offlineResource == null)) ? onlineResource.getRootPath()
        : offlineResource.getRootPath());

        // change folder or file?
        if (onlineResource.isFolder()) {
            CmsFolder restoredFolder = new CmsFolder(
                onlineResource.getStructureId(),
                onlineResource.getResourceId(),
                path,
                onlineResource.getTypeId(),
                onlineResource.getFlags(),
                dbc.currentProject().getUuid(),
                newState,
                onlineResource.getDateCreated(),
                onlineResource.getUserCreated(),
                onlineResource.getDateLastModified(),
                onlineResource.getUserLastModified(),
                onlineResource.getDateReleased(),
                onlineResource.getDateExpired(),
                onlineResource.getVersion()); // version number does not matter since it will be computed later

            // write the folder in the offline project
            // this sets a flag so that the folder date is not set to the current time
            restoredFolder.setDateLastModified(onlineResource.getDateLastModified());

            // write the folder
            m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), restoredFolder, NOTHING_CHANGED);

            // restore the properties from the online project
            m_vfsDriver.deletePropertyObjects(
                dbc,
                dbc.currentProject().getUuid(),
                restoredFolder,
                CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

            List propertyInfos = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineResource);
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), restoredFolder, propertyInfos);

            // restore the access control entries from the online project
            m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), onlineResource.getResourceId());
            ListIterator aceList = m_userDriver.readAccessControlEntries(
                dbc,
                onlineProject,
                onlineResource.getResourceId(),
                false).listIterator();

            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(
                    dbc,
                    dbc.currentProject(),
                    onlineResource.getResourceId(),
                    ace.getPrincipal(),
                    ace.getPermissions().getAllowedPermissions(),
                    ace.getPermissions().getDeniedPermissions(),
                    ace.getFlags());
            }
        } else {
            byte[] onlineContent = m_vfsDriver.readContent(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                onlineResource.getResourceId());

            CmsFile restoredFile = new CmsFile(
                onlineResource.getStructureId(),
                onlineResource.getResourceId(),
                path,
                onlineResource.getTypeId(),
                onlineResource.getFlags(),
                dbc.currentProject().getUuid(),
                newState,
                onlineResource.getDateCreated(),
                onlineResource.getUserCreated(),
                onlineResource.getDateLastModified(),
                onlineResource.getUserLastModified(),
                onlineResource.getDateReleased(),
                onlineResource.getDateExpired(),
                0,
                onlineResource.getLength(),
                onlineResource.getDateContent(),
                onlineResource.getVersion(), // version number does not matter since it will be computed later
                onlineContent);

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineResource.getDateLastModified());

            // collect the old properties
            List properties = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineResource);

            if (offlineResource != null) {
                // bugfix 1020: delete all properties (included shared), 
                // shared properties will be recreated by the next call of #createResource(...)
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // implementation notes: 
                // undo changes can become complex e.g. if a resource was deleted, and then 
                // another resource was copied over the deleted file as a sibling
                // therefore we must "clean" delete the offline resource, and then create 
                // an new resource with the create method
                // note that this does NOT apply to folders, since a folder cannot be replaced
                // like a resource anyway
                deleteResource(dbc, offlineResource, CmsResource.DELETE_PRESERVE_SIBLINGS);

            }
            CmsResource res = createResource(
                dbc,
                restoredFile.getRootPath(),
                restoredFile,
                restoredFile.getContents(),
                properties,
                false);

            // copy the access control entries from the online project
            if (offlineResource != null) {
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), onlineResource.getResourceId());
            }
            ListIterator aceList = m_userDriver.readAccessControlEntries(
                dbc,
                onlineProject,
                onlineResource.getResourceId(),
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

            // restore the state to unchanged 
            res.setState(newState);
            m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), res, UPDATE_ALL, false);
        }

        // update the cache
        clearResourceCache();
        OpenCms.getMemoryMonitor().flushProperties();
        OpenCms.getMemoryMonitor().flushPropertyLists();

        if (offlineResource != null) {
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", offlineResource)));
        } else {
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", onlineResource)));
        }
    }

    /**
     * Updates the current users context dates with the given resource.<p>
     * 
     * This checks the date information of the resource based on
     * {@link CmsResource#getDateLastModified()} as well as 
     * {@link CmsResource#getDateReleased()} and {@link CmsResource#getDateExpired()}.
     * The current users requerst context is updated with the the "latest" dates found.<p>
     * 
     * This is required in order to ensure proper setting of <code>"last-modified"</code> http headers
     * and also for expiration of cached elements in the Flex cache.
     * Consider the following use case: Page A is generated from resources x, y and z. 
     * If either x, y or z has an expiration / release date set, then page A must expire at a certain point 
     * in time. This is ensured by the context date check here.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the date information from
     */
    private void updateContextDates(CmsDbContext dbc, CmsResource resource) {

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        if (info != null) {
            info.updateFromResource(resource);
        }
    }

    /**
     * Updates the current users context dates with each {@link CmsResource} object in the given list.<p>
     * 
     * The given input list is returned unmodified.<p>
     * 
     * Please see {@link #updateContextDates(CmsDbContext, CmsResource)} for an explanation of what this method does.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of {@link CmsResource} objects
     * 
     * @return the original list of CmsResources with the full resource name set 
     */
    private List updateContextDates(CmsDbContext dbc, List resourceList) {

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        if (info != null) {
            for (int i = 0; i < resourceList.size(); i++) {
                CmsResource resource = (CmsResource)resourceList.get(i);
                info.updateFromResource(resource);
            }
        }
        return resourceList;
    }

    /**
     * Returns a List of {@link CmsResource} objects generated when applying the given filter to the given list,
     * also updates the current users context dates with each {@link CmsResource} object in the given list,
     * also applies the selected resource filter to all resources in the list and returns the remaining resources.<p>
     * 
     * Please see {@link #updateContextDates(CmsDbContext, CmsResource)} for an explanation of what this method does.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of {@link CmsResource} objects
     * @param filter the resource filter to use
     * 
     * @return a List of {@link CmsResource} objects generated when applying the given filter to the given list
     */
    private List updateContextDates(CmsDbContext dbc, List resourceList, CmsResourceFilter filter) {

        if (CmsResourceFilter.ALL == filter) {
            // if there is no filter required, then use the simpler method that does not apply the filter
            if (resourceList instanceof ArrayList) {
                // performance implementation for ArrayLists
                return (List)((ArrayList)(updateContextDates(dbc, resourceList))).clone();
            } else {
                return new ArrayList(updateContextDates(dbc, resourceList));
            }
        }

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        ArrayList result = new ArrayList(resourceList.size());
        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource resource = (CmsResource)resourceList.get(i);
            if (filter.isValid(dbc.getRequestContext(), resource)) {
                result.add(resource);
            }
            // must also include "invalid" resources for the update of context dates
            // since a resource may be invalid because of release / expiration date
            if (info != null) {
                info.updateFromResource(resource);
            }
        }
        return result;
    }

    /**
     * Updates the state of a resource, depending on the <code>resourceState</code> parameter.<p>
     * 
     * @param dbc the db context
     * @param resource the resource
     * @param resourceState if <code>true</code> the resource state will be updated, if not just the structure state.
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    private void updateState(CmsDbContext dbc, CmsResource resource, boolean resourceState)
    throws CmsDataAccessException {

        resource.setUserLastModified(dbc.currentUser().getId());
        if (resourceState) {
            // update the whole resource state
            m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), resource, UPDATE_RESOURCE_STATE);
        } else {
            // update the structure state
            m_vfsDriver.writeResource(dbc, dbc.currentProject().getUuid(), resource, UPDATE_STRUCTURE_STATE);
        }
    }

}